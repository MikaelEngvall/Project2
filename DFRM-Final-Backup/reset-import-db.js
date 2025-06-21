/**
 * Script för att tömma tabeller i databasen och importera från rensad CSV-fil
 */
const fs = require('fs');
const path = require('path');
const axios = require('axios');
const mysql = require('mysql2/promise');

// API base URL
const API_BASE_URL = "http://localhost:8080";

// Login credentials
const credentials = {
  email: "mikael.engvall.me@gmail.com",
  password: "Tr4d1l0s!"
};

// Database config från application.properties
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '1234',
  database: 'dfrm'
};

// Login funktion för att få auth token
async function login() {
  try {
    console.log(`Loggar in med ${credentials.email}...`);
    const response = await axios.post(`${API_BASE_URL}/api/auth/login`, credentials);
    
    if (response.data && response.data.token) {
      console.log('Login lyckades! Auth token erhållen.');
      return response.data.token;
    } else {
      throw new Error('Login lyckades men ingen token mottogs');
    }
  } catch (error) {
    console.error('Login misslyckades:', error.message);
    if (error.response) {
      console.error('Response status:', error.response.status);
      console.error('Response data:', error.response.data);
    }
    throw new Error('Autentisering misslyckades');
  }
}

// Funktion för att tömma tabeller
async function clearTables(connection) {
  try {
    console.log('Tömmer tabeller...');
    
    // Först ta bort relationer mellan tabeller (deaktivera foreign key checks)
    await connection.query('SET FOREIGN_KEY_CHECKS = 0');
    
    // Rensa tenant_apartments relationer (join table)
    await connection.query('TRUNCATE TABLE tenant_apartments');
    console.log('Tömt tenant_apartments (relation-tabell)');
    
    // Rensa tenant_keys relationer om tabellen existerar
    try {
      await connection.query('TRUNCATE TABLE tenant_keys');
      console.log('Tömt tenant_keys (relation-tabell)');
    } catch (error) {
      console.log('Kunde inte tömma tenant_keys tabell - existerar kanske inte');
    }
    
    // Rensa apartment_keys relationer om tabellen existerar
    try {
      await connection.query('TRUNCATE TABLE apartment_keys');
      console.log('Tömt apartment_keys (relation-tabell)');
    } catch (error) {
      console.log('Kunde inte tömma apartment_keys tabell - existerar kanske inte');
    }
    
    // Rensa nyckel-tabell om den existerar
    try {
      await connection.query('TRUNCATE TABLE nycklar');
      console.log('Tömt nycklar tabell');
    } catch (error) {
      console.log('Kunde inte tömma nycklar tabell - existerar kanske inte');
    }
    
    // Rensa huvudtabellerna
    await connection.query('TRUNCATE TABLE tenants');
    console.log('Tömt tenants tabell');
    
    await connection.query('TRUNCATE TABLE apartments');
    console.log('Tömt apartments tabell');
    
    // Aktivera foreign key checks igen
    await connection.query('SET FOREIGN_KEY_CHECKS = 1');
    
    console.log('Alla tabeller har tömts');
  } catch (error) {
    console.error('Fel vid tömning av tabeller:', error);
    throw error;
  }
}

// Ta bort oanvända tabeller
async function dropUnusedTables(connection) {
  try {
    console.log('Hämtar lista på alla tabeller...');
    const [tables] = await connection.query('SHOW TABLES');
    
    // Lista på tabeller vi vill behålla
    const keepTables = [
      'tenants', 
      'apartments', 
      'users', 
      'tenant_apartments', 
      'nycklar', 
      'tenant_keys', 
      'apartment_keys', 
      'roles',
      'user_roles',
      'password_reset_tokens',
      'admin_settings'
    ];
    
    // Konvertera tabeller till en enkel array av namn
    const tableNames = tables.map(row => Object.values(row)[0]);
    
    // Identifiera vilka tabeller som ska tas bort
    const tablesToDrop = tableNames.filter(name => !keepTables.includes(name));
    
    if (tablesToDrop.length === 0) {
      console.log('Inga oanvända tabeller att ta bort');
      return;
    }
    
    console.log('Tabeller som kommer att tas bort:', tablesToDrop.join(', '));
    
    // Deaktivera foreign key checks
    await connection.query('SET FOREIGN_KEY_CHECKS = 0');
    
    // Ta bort varje tabell
    for (const tableName of tablesToDrop) {
      try {
        await connection.query(`DROP TABLE ${tableName}`);
        console.log(`Tabell ${tableName} borttagen`);
      } catch (error) {
        console.error(`Kunde inte ta bort tabell ${tableName}:`, error.message);
      }
    }
    
    // Aktivera foreign key checks igen
    await connection.query('SET FOREIGN_KEY_CHECKS = 1');
    
  } catch (error) {
    console.error('Fel vid borttagning av oanvända tabeller:', error);
    throw error;
  }
}

// Helper funktion för att parsa belopp med svenskt format (t.ex. "27 484,35 kr")
const parseAmount = (amountString) => {
  if (!amountString) return 0;
  return parseFloat(amountString.replace(/\s/g, '').replace(/,/g, '.').replace(/kr/g, '').trim());
};

// Helper funktion för att parsa area från CSV
const parseArea = (areaString) => {
  if (!areaString) return 0;
  return parseFloat(areaString);
};

// Main import funktion
async function importTenantsFromCSV(csvData, token) {
  try {
    console.log('Startar import...');
    
    // Parse CSV data
    const lines = csvData.split('\n');
    const headers = lines[0].split(';');
    const dataRows = lines.slice(1).filter(line => line.trim() !== '');
    
    console.log(`CSV innehåller ${dataRows.length} rader att importera`);
    
    // Start the import process
    const results = [];
    const errors = [];
    
    // Process each tenant-apartment pair
    for (let i = 0; i < dataRows.length; i++) {
      try {
        const values = dataRows[i].split(';');
        if (values.length < 7) continue; // Skip incomplete lines
        
        // Map CSV columns to object fields
        const firstName = values[0] || '';
        const lastName = values[1] || '';
        const street = values[2] || '';
        const number = values[3] || '';
        const email = values[4] || '';
        const phone = values[5] || '';
        const apartmentNumber = values[6] || '';
        const area = parseArea(values[7]) || 0;
        const contractDate = values[8] || '';
        const monthlyAmount = parseAmount(values[9]) || 0;
        
        // Create apartment object
        const apartment = {
          street,
          number,
          apartmentNumber,
          postalCode: '371 00', // Default postal code for Karlskrona
          city: 'Karlskrona',
          rooms: Math.ceil(area / 25) || 1, // Estimate rooms based on area
          area,
          price: monthlyAmount,
          electricity: true, 
          storage: true,
          internet: true,
          isTemporary: false
        };
        
        // Create tenant object
        const tenant = {
          firstName,
          lastName,
          email: email || `${firstName.toLowerCase()}.${lastName.toLowerCase()}@example.com`,
          phone,
          movedInDate: contractDate || new Date().toISOString().split('T')[0], // Use contract date or today
          isTemporary: false
        };
        
        console.log(`Importerar ${i+1}/${dataRows.length}: ${tenant.firstName} ${tenant.lastName} - ${apartment.street} ${apartment.number}, apt ${apartment.apartmentNumber}`);
        
        // Step 1: Create the apartment
        console.log('Skapar lägenhet...');
        const apartmentResponse = await axios.post(`${API_BASE_URL}/api/apartments`, apartment, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });
        
        const createdApartment = apartmentResponse.data;
        console.log('Lägenhet skapad:', createdApartment.id);
        
        // Step 2: Create the tenant
        console.log('Skapar hyresgäst...');
        const tenantResponse = await axios.post(`${API_BASE_URL}/api/tenants`, tenant, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });
        
        const createdTenant = tenantResponse.data;
        console.log('Hyresgäst skapad:', createdTenant.id);
        
        // Step 3: Associate tenant with apartment
        console.log('Kopplar hyresgäst till lägenhet...');
        
        // Använd PUT-metoden för att koppla hyresgäst till lägenhet
        try {
          const updateTenantResponse = await axios.put(`${API_BASE_URL}/api/tenants/${createdTenant.id}/apartment?apartmentId=${createdApartment.id}`, {}, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          console.log('Koppling lyckades (tenant PUT)');
        } catch (linkError) {
          console.error('Fel vid koppling med tenant PUT:', linkError.message);
          
          // Försök med apartment PUT endpoint om tenant endpoint misslyckades
          try {
            const updateApartmentResponse = await axios.put(`${API_BASE_URL}/api/apartments/${createdApartment.id}/tenant?tenantId=${createdTenant.id}`, {}, {
              headers: {
                'Authorization': `Bearer ${token}`
              }
            });
            console.log('Koppling lyckades (apartment PUT)');
          } catch (linkError2) {
            console.error('Fel vid koppling med apartment PUT:', linkError2.message);
            throw new Error('Kunde inte koppla hyresgäst till lägenhet');
          }
        }
        
        results.push({
          tenant: createdTenant,
          apartment: createdApartment
        });
        
        console.log(`Framgångsrikt importerat ${tenant.firstName} ${tenant.lastName}`);
      } catch (error) {
        console.error(`Fel vid import av rad ${i+1}:`, error.message);
        errors.push({
          index: i,
          line: dataRows[i],
          error: error.message
        });
      }
    }
    
    console.log('Import slutförd.');
    console.log(`Framgångsrikt importerat ${results.length} hyresgäster och lägenheter.`);
    if (errors.length > 0) {
      console.log(`Misslyckades med att importera ${errors.length} rader. Se felmeddelanden för detaljer.`);
    }
    
    return {
      success: results.length,
      failed: errors.length,
      results,
      errors
    };
  } catch (error) {
    console.error('Import misslyckades:', error);
    throw error;
  }
}

// Main funktion
async function main() {
  let connection;
  try {
    // Steg 1: Logga in för att få token
    const token = await login();
    
    // Steg 2: Anslut till databasen
    connection = await mysql.createConnection(dbConfig);
    console.log('Ansluten till databasen');
    
    // Steg 3: Töm befintliga tabeller
    await clearTables(connection);
    
    // Steg 4: Ta bort oanvända tabeller
    await dropUnusedTables(connection);
    
    // Steg 5: Läs CSV filen
    const csvFile = path.join(__dirname, 'Hyresgästsammanställning_cleaned.csv');
    console.log(`Läser CSV fil från: ${csvFile}`);
    const csvData = fs.readFileSync(csvFile, 'utf8');
    
    // Steg 6: Importera data från CSV
    const result = await importTenantsFromCSV(csvData, token);
    
    console.log('Resultat av import:', result);
  } catch (error) {
    console.error('Fel i huvudfunktionen:', error);
  } finally {
    // Stäng databasanslutningen om den finns
    if (connection) {
      await connection.end();
      console.log('Databasanslutning stängd');
    }
  }
}

// Kör huvudfunktionen
main().then(() => {
  console.log('Process avslutad');
}).catch(error => {
  console.error('Processen avslutades med fel:', error);
}); 