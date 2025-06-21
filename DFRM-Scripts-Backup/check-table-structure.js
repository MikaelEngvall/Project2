/**
 * Detta skript kontrollerar strukturen av tabellerna i databasen
 */

const mysql = require('mysql2/promise');
const dbConfig = require('./db-config');

async function main() {
  try {
    console.log('Ansluter till databasen för att kontrollera tabellstruktur...');
    
    // Upprätta databasanslutning
    const connection = await mysql.createConnection(dbConfig);
    console.log('Databasanslutning upprättad');
    
    // Kontrollera tenant-tabellen
    console.log('\n### TENANT-TABELLEN ###');
    try {
      const [tenantColumns] = await connection.execute('DESCRIBE tenants');
      console.log('Kolumner i tenants:');
      tenantColumns.forEach(col => {
        console.log(`- ${col.Field} (${col.Type})${col.Key === 'PRI' ? ' [PRIMARY KEY]' : ''}`);
      });
    } catch (error) {
      console.error('Kunde inte hämta struktur för tenants:', error.message);
    }
    
    // Kontrollera apartment-tabellen
    console.log('\n### APARTMENT-TABELLEN ###');
    try {
      const [apartmentColumns] = await connection.execute('DESCRIBE apartments');
      console.log('Kolumner i apartments:');
      apartmentColumns.forEach(col => {
        console.log(`- ${col.Field} (${col.Type})${col.Key === 'PRI' ? ' [PRIMARY KEY]' : ''}`);
      });
    } catch (error) {
      console.error('Kunde inte hämta struktur för apartments:', error.message);
    }
    
    // Lista alla tabeller
    console.log('\n### ALLA TABELLER ###');
    try {
      const [tables] = await connection.execute('SHOW TABLES');
      console.log('Tabeller i databasen:');
      tables.forEach(table => {
        const tableName = Object.values(table)[0];
        console.log(`- ${tableName}`);
      });
    } catch (error) {
      console.error('Kunde inte lista tabeller:', error.message);
    }
    
    // Stäng anslutning
    await connection.end();
    console.log('\nDatabasanslutning stängd');
    
  } catch (error) {
    console.error('Ett fel uppstod:', error.message);
    process.exit(1);
  }
}

// Kör huvudfunktionen
main().catch(error => {
  console.error('Oväntat fel:', error);
  process.exit(1);
}); 