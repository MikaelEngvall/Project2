/**
 * Direct Import Script to MySQL
 * 
 * Detta skript kringgår helt Spring-applikationen och importerar data
 * direkt till MySQL-databasen.
 */

const fs = require('fs');
const mysql = require('mysql2/promise');
const { v4: uuidv4 } = require('uuid');
const dbConfig = require('./db-config');

const CSV_FILE = 'Hyresgästsammanställning_final_postnummer.csv';

async function main() {
  try {
    console.log('Startar direktimport till MySQL database...');
    
    // Steg 1: Upprätta databasanslutning
    const connection = await mysql.createConnection(dbConfig);
    console.log('Databasanslutning upprättad');
    
    // Steg 2: Läs CSV-fil
    const csvData = fs.readFileSync(CSV_FILE, 'utf8');
    const lines = csvData.split('\n').filter(line => line.trim());
    
    // Steg 3: Identifiera header/kolumnnamn
    const headers = lines[0].split(';').map(h => h.trim());
    console.log('CSV-header:', headers);
    
    // Steg 4: Skapa mappning av kolumnindex
    const columnMap = createColumnMap(headers);
    console.log('Kolumnmappning:', columnMap);
    
    // Verifiera att vi har de kolumner vi behöver
    const requiredColumns = ['Förnamn', 'Efternamn', 'Emailaddress', 'Telefonnummer', 
                          'Lägenhetsnummer', 'Gata', 'Nummer', 'Postnummer', 'Ort', 
                          'Kvm', 'Hyresobjekt', 'Kontrakt fr.o.m', 'Månadsbelopp'];
    
    const missingColumns = requiredColumns.filter(col => columnMap[col] === undefined);
    if (missingColumns.length > 0) {
      throw new Error(`Saknar nödvändiga kolumner: ${missingColumns.join(', ')}`);
    }
    
    // Steg 5: Bearbeta varje rad och skapa data
    console.log('Bearbetar', lines.length - 1, 'rader från CSV-filen...');
    
    const importResults = {
      totalRows: lines.length - 1,
      successfulApartments: 0,
      successfulTenants: 0,
      successfulLinks: 0,
      errors: []
    };
    
    // Börja en transaktion
    await connection.beginTransaction();
    
    try {
      // Hoppa över första raden (header) och behandla resten
      for (let i = 1; i < lines.length; i++) {
        const line = lines[i];
        if (!line.trim()) continue;
        
        const values = line.split(';');
        
        try {
          // Extrahera data för hyresgäst
          const firstName = values[columnMap['Förnamn']] || '';
          const lastName = values[columnMap['Efternamn']] || '';
          const email = values[columnMap['Emailaddress']] || '';
          const phone = values[columnMap['Telefonnummer']] || '';
          const movedInDate = formatDate(values[columnMap['Kontrakt fr.o.m']]) || '';
          
          // Skapa bara giltiga hyresgäster (med både förnamn och efternamn)
          if (firstName && lastName) {
            // Extrahera data för lägenhet
            const street = values[columnMap['Gata']] || '';
            const number = values[columnMap['Nummer']] || '';
            const apartmentNumber = values[columnMap['Lägenhetsnummer']] || '';
            const postalCode = values[columnMap['Postnummer']] || '';
            const city = values[columnMap['Ort']] || '';
            const area = parseFloat(values[columnMap['Kvm']]) || 0;
            const price = parseFloat(values[columnMap['Månadsbelopp']].replace(/,/g, '.')) || 0;
            const rooms = Math.ceil((parseFloat(values[columnMap['Kvm']]) || 30) / 20) || 1; // Uppskatta antal rum
            const propertyType = values[columnMap['Hyresobjekt']] || 'Lägenhet';
            
            // Skapa UUID för lägenhet och hyresgäst
            const apartmentId = uuidv4();
            const tenantId = uuidv4();
            
            // Sparar först tenant
            const tenantQuery = `
              INSERT INTO tenants 
              (id, first_name, last_name, email, phone_number, is_temporary, moved_in_date) 
              VALUES (?, ?, ?, ?, ?, 0, ?)
            `;
            
            await connection.execute(tenantQuery, [
              tenantId, 
              firstName, 
              lastName, 
              email, 
              phone,
              movedInDate
            ]);
            
            importResults.successfulTenants++;
            
            // Sedan sparar vi apartment
            const apartmentQuery = `
              INSERT INTO apartments 
              (id, street, number, apartment_number, postal_code, city, rooms, area, price, 
               electricity, internet, is_temporary) 
              VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 1, 0)
            `;
            
            await connection.execute(apartmentQuery, [
              apartmentId,
              street,
              number,
              apartmentNumber,
              postalCode,
              city,
              rooms,
              area,
              price
            ]);
            
            importResults.successfulApartments++;
            
            // Koppla tenant till apartment genom att uppdatera till tenant_id i apartment
            const linkQuery = `
              UPDATE apartments SET tenant_id = ? WHERE id = ?
            `;
            
            await connection.execute(linkQuery, [
              tenantId,
              apartmentId
            ]);
            
            importResults.successfulLinks++;
            
            console.log(`[${i}/${lines.length-1}] Importerade: ${firstName} ${lastName}, lägenhet: ${street} ${apartmentNumber}`);
          }
        } catch (rowError) {
          console.error(`Fel vid import av rad ${i}:`, rowError.message);
          importResults.errors.push({
            row: i,
            error: rowError.message
          });
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
    
    // Steg 6: Sammanställ resultat
    console.log('\nImport slutförd!');
    console.log(`Bearbetade totalt: ${importResults.totalRows} rader`);
    console.log(`Skapade hyresgäster: ${importResults.successfulTenants}`);
    console.log(`Skapade lägenheter: ${importResults.successfulApartments}`);
    console.log(`Kopplade samman: ${importResults.successfulLinks}`);
    console.log(`Antal fel: ${importResults.errors.length}`);
    
    if (importResults.errors.length > 0) {
      console.log('\nFel uppstod på följande rader:');
      importResults.errors.forEach(err => {
        console.log(`Rad ${err.row}: ${err.error}`);
      });
    }
    
    // Stäng anslutning
    await connection.end();
    console.log('Databasanslutning stängd');
    
  } catch (error) {
    console.error('Ett kritiskt fel uppstod:', error.message);
    process.exit(1);
  }
}

// Hjälpfunktion för att skapa kolumnmappning
function createColumnMap(headers) {
  const map = {};
  headers.forEach((header, index) => {
    map[header] = index;
  });
  return map;
}

// Hjälpfunktion för att formatera datum
function formatDate(dateString) {
  if (!dateString) return '';
  
  // Om det redan är i YYYY-MM-DD format
  if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
    return dateString;
  }
  
  // Annars, försök tolka och formatera
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
      return new Date().toISOString().split('T')[0]; // Använd dagens datum som fallback
    }
    return date.toISOString().split('T')[0];
  } catch (e) {
    return new Date().toISOString().split('T')[0]; // Använd dagens datum om tolkning misslyckas
  }
}

// Kör huvudfunktionen
main().catch(error => {
  console.error('Oväntat fel:', error);
  process.exit(1);
}); 