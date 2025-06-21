/**
 * Skript för att räkna antalet hyresgäster i databasen
 */

const mysql = require('mysql2/promise');
const dbConfig = require('./db-config');

async function main() {
  try {
    console.log('Ansluter till databas för att räkna hyresgäster...');
    
    // Upprätta databasanslutning
    const connection = await mysql.createConnection(dbConfig);
    console.log('Databasanslutning upprättad');
    
    // Räkna antalet hyresgäster
    const [tenantCount] = await connection.execute('SELECT COUNT(*) as count FROM tenants');
    console.log(`\nAntal hyresgäster i databasen: ${tenantCount[0].count}`);
    
    // Räkna antalet lägenheter
    const [aptCount] = await connection.execute('SELECT COUNT(*) as count FROM apartments');
    console.log(`Antal lägenheter i databasen: ${aptCount[0].count}`);
    
    // Räkna antalet kopplingar
    const [connectionCount] = await connection.execute('SELECT COUNT(*) as count FROM tenant_apartments');
    console.log(`Antal kopplingar mellan hyresgäster och lägenheter: ${connectionCount[0].count}`);
    
    // Stäng anslutning
    await connection.end();
    
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