/**
 * Script för att hitta en matchning i CSV-filen baserat på något unik attribut
 */
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

// UUID att hitta
const targetUuid = '4febdf29-bd98-417d-940e-3edabae6f86f';

async function findMatches() {
  try {
    console.log(`Söker efter information relaterad till UUID: ${targetUuid}`);
    
    // Läs in CSV-filen
    const csvPath = path.join(__dirname, 'Hyresgästsammanställning_fixed.csv');
    const csvData = fs.readFileSync(csvPath, 'utf8');
    
    // Parsa CSV-data
    const lines = csvData.split('\n');
    const headers = lines[0].split(';');
    
    // Skapa objekt för varje rad
    const tenants = [];
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;
      
      const values = line.split(';');
      const tenant = {};
      
      headers.forEach((header, index) => {
        tenant[header] = values[index] ? values[index].trim() : '';
      });
      
      tenants.push(tenant);
    }
    
    // Skapa ett mini UUID-baserat på unika attribut
    function calculateHash(tenant) {
      const uniqueKey = `${tenant['Förnamn']}-${tenant['Efternamn']}-${tenant['Gata']}-${tenant['Lägenhetsnummer']}`;
      return crypto.createHash('md5').update(uniqueKey).digest('hex');
    }
    
    // Generera UUID-v5 baserat på unika attribut
    function generateUuidV5(tenant) {
      const namespace = '6ba7b810-9dad-11d1-80b4-00c04fd430c8'; // DNS namespace UUID
      const uniqueKey = `${tenant['Förnamn']}-${tenant['Efternamn']}-${tenant['Gata']}-${tenant['Lägenhetsnummer']}`;
      
      // UUIDv5 generering
      const hmac = crypto.createHash('sha1');
      const namespaceBytes = [];
      
      namespace.split('-').join('').match(/.{2}/g).forEach(hex => {
        namespaceBytes.push(parseInt(hex, 16));
      });
      
      hmac.update(Buffer.from(namespaceBytes));
      hmac.update(uniqueKey);
      
      const hash = hmac.digest();
      
      // Ställ in korrekt version (5) och variant
      hash[6] = (hash[6] & 0x0f) | 0x50;
      hash[8] = (hash[8] & 0x3f) | 0x80;
      
      // Omvandla tillbaka till UUID-sträng
      let result = '';
      for (let i = 0; i < 16; i++) {
        if (i === 4 || i === 6 || i === 8 || i === 10) {
          result += '-';
        }
        let hex = hash[i].toString(16);
        if (hex.length === 1) {
          hex = '0' + hex;
        }
        result += hex;
      }
      
      return result;
    }
    
    console.log(`Totalt antal hyresgäster i CSV: ${tenants.length}`);
    console.log('Kontrollerar om någon hyresgäst matchar UUID...');
    
    // Skriv ut några testvärden för att jämföra
    console.log('\nTestvärden för några hyresgäster:');
    for (let i = 0; i < Math.min(5, tenants.length); i++) {
      const tenant = tenants[i];
      const hash = calculateHash(tenant);
      const uuid = generateUuidV5(tenant);
      console.log(`${tenant['Förnamn']} ${tenant['Efternamn']} (${tenant['Gata']} ${tenant['Lägenhetsnummer']}): ${hash.substring(0, 8)} / ${uuid}`);
    }
    
    // Leta efter matchningar
    console.log('\nSöker efter exakta matchningar för UUID...');
    const exactMatches = tenants.filter(tenant => {
      const uuid = generateUuidV5(tenant);
      return uuid === targetUuid;
    });
    
    if (exactMatches.length > 0) {
      console.log('Exakta matchningar hittade:');
      exactMatches.forEach(match => {
        console.log(`${match['Förnamn']} ${match['Efternamn']}, ${match['Gata']} ${match['Nummer']}, Lgh ${match['Lägenhetsnummer']}`);
      });
    } else {
      console.log('Inga exakta matchningar hittades.');
      
      // Sök efter partiella matchningar baserat på de första 8 tecknen i hashen
      console.log('\nSöker efter partiella matchningar baserat på hashvärden...');
      const partialMatches = tenants.filter(tenant => {
        const hash = calculateHash(tenant);
        return targetUuid.startsWith(hash.substring(0, 8));
      });
      
      if (partialMatches.length > 0) {
        console.log('Möjliga matchningar (baserat på hashvärden):');
        partialMatches.forEach(match => {
          const hash = calculateHash(match);
          console.log(`${match['Förnamn']} ${match['Efternamn']}, ${match['Gata']} ${match['Nummer']}, Lgh ${match['Lägenhetsnummer']} - Hash: ${hash.substring(0, 8)}`);
        });
      } else {
        console.log('Inga partiella matchningar hittades heller.');
        
        // Visa alla hyresgäster i CSV-filen
        console.log('\nAlla hyresgäster i CSV-filen:');
        tenants.forEach((tenant, index) => {
          console.log(`${index + 1}. ${tenant['Förnamn']} ${tenant['Efternamn']}, ${tenant['Gata']} ${tenant['Nummer']}, Lgh ${tenant['Lägenhetsnummer']}`);
        });
      }
    }
    
  } catch (error) {
    console.error('Ett fel inträffade:', error);
  }
}

findMatches().catch(console.error); 