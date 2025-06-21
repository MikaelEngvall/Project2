/**
 * Skript för att koppla ihop hyresgäster med lägenheter
 * 
 * Detta skript använder CSV-filen för att koppla ihop hyresgäster med lägenheter
 * baserat på deras namn och lägenhetsinformation.
 */

const fs = require('fs');
const mysql = require('mysql2/promise');
const dbConfig = require('./db-config');

const CSV_FILE = 'Hyresgästsammanställning_final_postnummer.csv';

async function main() {
  try {
    console.log('Startar skript för att koppla hyresgäster med lägenheter...');
    
    // Steg 1: Upprätta databasanslutning
    const connection = await mysql.createConnection(dbConfig);
    console.log('Databasanslutning upprättad');
    
    // Steg 2: Kontrollera att tenant_apartments-tabellen finns
    console.log('Kontrollerar att kopplingstabell finns...');
    
    try {
      const [tables] = await connection.execute('SHOW TABLES');
      const tableNames = tables.map(t => Object.values(t)[0]);
      
      if (!tableNames.includes('tenant_apartments')) {
        // Skapa tabellen om den inte finns
        console.log('tenant_apartments-tabellen saknas, skapar den nu...');
        
        await connection.execute(`
          CREATE TABLE tenant_apartments (
            tenant_id VARCHAR(36) NOT NULL,
            apartment_id VARCHAR(36) NOT NULL,
            PRIMARY KEY (tenant_id, apartment_id),
            CONSTRAINT fk_ta_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
            CONSTRAINT fk_ta_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id)
          )
        `);
        console.log('tenant_apartments-tabellen har skapats');
      } else {
        console.log('tenant_apartments-tabellen finns redan');
      }
    } catch (error) {
      console.error('Fel vid kontroll av tabeller:', error.message);
      throw error;
    }
    
    // Steg 3: Läs in CSV-filen för att hämta relationer
    console.log('Läser in CSV-fil för att hämta relationer...');
    
    const csvData = fs.readFileSync(CSV_FILE, 'utf8');
    const lines = csvData.split('\n').filter(line => line.trim());
    
    // Identifiera header/kolumnnamn
    const headers = lines[0].split(';').map(h => h.trim());
    const columnMap = createColumnMap(headers);
    
    // Skapa mappningar från CSV-filen
    const relationMap = new Map(); // key: firstName+lastName -> {streetInfo, aptNumber}
    
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i];
      if (!line.trim()) continue;
      
      const values = line.split(';');
      
      // Extrahera data
      const firstName = values[columnMap['Förnamn']] || '';
      const lastName = values[columnMap['Efternamn']] || '';
      const street = values[columnMap['Gata']] || '';
      const number = values[columnMap['Nummer']] || '';
      const apartmentNumber = values[columnMap['Lägenhetsnummer']] || '';
      const postalCode = values[columnMap['Postnummer']] || '';
      const city = values[columnMap['Ort']] || '';
      
      if (firstName && lastName) {
        // Skapa en unik nyckel för denna hyresgäst
        const tenantKey = normalizeName(`${firstName} ${lastName}`);
        
        relationMap.set(tenantKey, {
          street,
          number,
          apartmentNumber,
          postalCode,
          city
        });
      }
    }
    
    console.log(`Läste ${relationMap.size} relationer från CSV-filen`);
    
    // Steg 4: Hämta alla hyresgäster och lägenheter från databasen
    console.log('Hämtar alla hyresgäster från databasen...');
    const [tenants] = await connection.execute('SELECT * FROM tenants');
    console.log(`Hittade ${tenants.length} hyresgäster`);
    
    console.log('Hämtar alla lägenheter från databasen...');
    const [apartments] = await connection.execute('SELECT * FROM apartments');
    console.log(`Hittade ${apartments.length} lägenheter`);
    
    // Steg 5: Skapa effektivare mappningar för sökning
    const tenantsMap = new Map(); // key: firstName+lastName -> tenant
    tenants.forEach(tenant => {
      const key = normalizeName(`${tenant.first_name} ${tenant.last_name}`);
      tenantsMap.set(key, tenant);
    });
    
    const apartmentsMap = new Map(); // flera olika nycklar -> apartment
    
    // Skapa olika typer av nycklar för att hitta lägenheter på flera sätt
    apartments.forEach(apt => {
      // Nyckel baserad på gata + nummer + lägenhetsnummer
      const fullAddressKey = normalizeAddress(`${apt.street} ${apt.number || ''} ${apt.apartment_number || ''}`);
      apartmentsMap.set(fullAddressKey, apt);
      
      // Nyckel baserad på gata + lägenhetsnummer
      const streetAptKey = normalizeAddress(`${apt.street} ${apt.apartment_number || ''}`);
      apartmentsMap.set(streetAptKey, apt);
      
      // Nyckel baserad på bara lägenhetsnummer om det är unikt
      if (apt.apartment_number) {
        apartmentsMap.set(apt.apartment_number.trim().toLowerCase(), apt);
      }
    });
    
    // Steg 6: Koppla hyresgäster till lägenheter baserat på CSV-relationer
    console.log('Börjar koppla hyresgäster till lägenheter...');
    
    let successCount = 0;
    let failCount = 0;
    
    // Börja en transaktion
    await connection.beginTransaction();
    
    try {
      // Rensa eventuella tidigare kopplingar
      console.log('Rensar befintliga kopplingar...');
      await connection.execute('DELETE FROM tenant_apartments');
      
      // För varje relation i CSV-filen, hitta motsvarande hyresgäst och lägenhet
      for (const [tenantKey, aptInfo] of relationMap.entries()) {
        const tenant = tenantsMap.get(tenantKey);
        
        if (!tenant) {
          console.log(`Kunde inte hitta hyresgäst för "${tenantKey}"`);
          failCount++;
          continue;
        }
        
        // Skapa olika adressnycklar för sökning
        const addressKeys = [
          normalizeAddress(`${aptInfo.street} ${aptInfo.number || ''} ${aptInfo.apartmentNumber || ''}`),
          normalizeAddress(`${aptInfo.street} ${aptInfo.apartmentNumber || ''}`),
          aptInfo.apartmentNumber ? aptInfo.apartmentNumber.trim().toLowerCase() : null
        ].filter(Boolean); // Ta bort null-värden
        
        // Försök hitta lägenheten med någon av nycklarna
        let apartment = null;
        for (const key of addressKeys) {
          apartment = apartmentsMap.get(key);
          if (apartment) break;
        }
        
        if (apartment) {
          // Lägg till koppling
          await connection.execute(
            'INSERT INTO tenant_apartments (tenant_id, apartment_id) VALUES (?, ?)',
            [tenant.id, apartment.id]
          );
          
          console.log(`Kopplat: ${tenant.first_name} ${tenant.last_name} -> ${apartment.street} ${apartment.apartment_number || ''}`);
          successCount++;
        } else {
          // Försök med en direktsökning i databasen som backup
          const [rows] = await connection.execute(`
            SELECT a.* FROM apartments a
            WHERE 
              (a.street = ? OR a.street LIKE CONCAT(?, '%')) 
              AND (? IS NULL OR a.apartment_number = ?)
            LIMIT 1
          `, [aptInfo.street, aptInfo.street, aptInfo.apartmentNumber, aptInfo.apartmentNumber]);
          
          if (rows.length > 0) {
            const foundApt = rows[0];
            
            // Lägg till koppling
            await connection.execute(
              'INSERT INTO tenant_apartments (tenant_id, apartment_id) VALUES (?, ?)',
              [tenant.id, foundApt.id]
            );
            
            console.log(`Kopplat (via DB-sökning): ${tenant.first_name} ${tenant.last_name} -> ${foundApt.street} ${foundApt.apartment_number || ''}`);
            successCount++;
          } else {
            console.log(`Kunde inte hitta lägenhet för ${tenant.first_name} ${tenant.last_name}, adress: ${aptInfo.street} ${aptInfo.apartmentNumber || ''}`);
            failCount++;
          }
        }
      }
      
      // Om allt gick bra, bekräfta transaktionen
      await connection.commit();
      console.log('Transaktion bekräftad');
      
    } catch (error) {
      // Om något gick fel, rulla tillbaka transaktionen
      await connection.rollback();
      console.error('Transaktion rullades tillbaka p.g.a. fel:', error.message);
      throw error;
    }
    
    // Steg 7: Sammanställ resultat
    console.log('\nKoppling slutförd!');
    console.log(`Lyckades koppla: ${successCount} hyresgäster`);
    console.log(`Misslyckades: ${failCount} hyresgäster`);
    
    if (successCount === 0) {
      console.log('\nIngen koppling lyckades. Försök med alternativ metod...');
      await tryAlternativeMethod(connection, tenants, apartments);
    }
    
    // Stäng anslutning
    await connection.end();
    console.log('Databasanslutning stängd');
    
  } catch (error) {
    console.error('Ett kritiskt fel uppstod:', error.message);
    process.exit(1);
  }
}

/**
 * Normaliserar namn för matchning
 */
function normalizeName(name) {
  return name.trim().toLowerCase()
    .replace(/\s+/g, ' ') // Konsolidera mellanslag
    .normalize('NFD').replace(/[\u0300-\u036f]/g, ''); // Ta bort accenter
}

/**
 * Normaliserar adress för matchning
 */
function normalizeAddress(address) {
  return address.trim().toLowerCase()
    .replace(/\s+/g, ' ') // Konsolidera mellanslag
    .replace(/[,.-]/g, ''); // Ta bort skiljetecken
}

/**
 * Hjälpfunktion för att skapa kolumnmappning från CSV-header
 */
function createColumnMap(headers) {
  const map = {};
  headers.forEach((header, index) => {
    map[header] = index;
  });
  return map;
}

/**
 * Alternativ metod för att koppla hyresgäster med lägenheter
 */
async function tryAlternativeMethod(connection, tenants, apartments) {
  console.log('Försöker koppla hyresgäster med lägenheter på ett annat sätt...');
  
  let successCount = 0;
  let failCount = 0;
  
  // Börja en transaktion
  await connection.beginTransaction();
  
  try {
    // Rensa eventuella tidigare kopplingar
    await connection.execute('DELETE FROM tenant_apartments');
    
    // Sortera lägenheter och hyresgäster efter importordning
    // Detta förutsätter att de importerades i samma ordning
    if (tenants.length === apartments.length) {
      console.log('Lika många hyresgäster som lägenheter, försöker koppla dem i importordning...');
      
      for (let i = 0; i < tenants.length; i++) {
        const tenant = tenants[i];
        const apartment = apartments[i];
        
        await connection.execute(
          'INSERT INTO tenant_apartments (tenant_id, apartment_id) VALUES (?, ?)',
          [tenant.id, apartment.id]
        );
        
        console.log(`Kopplat (via index): ${tenant.first_name} ${tenant.last_name} -> ${apartment.street} ${apartment.apartment_number || ''}`);
        successCount++;
      }
      
      console.log(`Kopplat ${successCount} hyresgäster till lägenheter baserat på importordning`);
    } else {
      console.log('Olika antal hyresgäster och lägenheter, kan inte använda indexbaserad koppling');
      failCount = tenants.length;
    }
    
    // Bekräfta transaktion
    await connection.commit();
    
  } catch (error) {
    await connection.rollback();
    console.error('Alternativ metod misslyckades:', error.message);
    throw error;
  }
  
  console.log('\nAlternativ koppling slutförd!');
  console.log(`Lyckades koppla: ${successCount} hyresgäster`);
  console.log(`Misslyckades: ${failCount} hyresgäster`);
}

// Kör huvudfunktionen
main().catch(error => {
  console.error('Oväntat fel:', error);
  process.exit(1);
}); 