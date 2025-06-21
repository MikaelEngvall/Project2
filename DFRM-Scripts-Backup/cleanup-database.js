/**
 * Skript för att rensa databasen och behålla endast 138 hyresgäster med en lägenhet var
 */

const mysql = require('mysql2/promise');
const dbConfig = require('./db-config');

async function main() {
  try {
    console.log('Startar rensning av databasen...');
    
    // Upprätta databasanslutning
    const connection = await mysql.createConnection(dbConfig);
    console.log('Databasanslutning upprättad');
    
    // Kontrollera aktuell status
    const [tenantCount] = await connection.execute('SELECT COUNT(*) as count FROM tenants');
    console.log(`Antal hyresgäster före rensning: ${tenantCount[0].count}`);
    
    const [aptCount] = await connection.execute('SELECT COUNT(*) as count FROM apartments');
    console.log(`Antal lägenheter före rensning: ${aptCount[0].count}`);
    
    const [connectionCount] = await connection.execute('SELECT COUNT(*) as count FROM tenant_apartments');
    console.log(`Antal kopplingar före rensning: ${connectionCount[0].count}`);
    
    // Hämta alla hyresgäster som HAR en koppling till en lägenhet
    console.log('\nHämtar hyresgäster med kopplingar...');
    const [linkedTenants] = await connection.execute(`
      SELECT DISTINCT t.id, t.first_name, t.last_name 
      FROM tenants t
      JOIN tenant_apartments ta ON t.id = ta.tenant_id
    `);
    
    console.log(`Hittade ${linkedTenants.length} hyresgäster med kopplingar`);
    
    if (linkedTenants.length !== 138) {
      console.log(`Varning: Förväntade 138 kopplade hyresgäster, hittade ${linkedTenants.length}`);
    }
    
    // Hämta alla apartments som HAR en koppling till en tenant
    const [linkedApartments] = await connection.execute(`
      SELECT DISTINCT a.id
      FROM apartments a
      JOIN tenant_apartments ta ON a.id = ta.apartment_id
    `);
    
    console.log(`Hittade ${linkedApartments.length} lägenheter med kopplingar`);
    
    // Starta en transaktion
    await connection.beginTransaction();
    
    try {
      // 1. Först spara IDs för hyresgäster som ska behållas
      const tenantIdsToKeep = linkedTenants.map(t => t.id);
      
      // 2. Hämta IDs för lägenheter som ska behållas
      const apartmentIdsToKeep = linkedApartments.map(a => a.id);
      
      // 3. Ta bort alla hyresgäster som inte är kopplade till en lägenhet
      const [deleteTenantsResult] = await connection.execute(`
        DELETE FROM tenants 
        WHERE id NOT IN (${tenantIdsToKeep.map(id => '?').join(',')})
      `, tenantIdsToKeep);
      
      console.log(`Raderade ${deleteTenantsResult.affectedRows} hyresgäster som inte hade kopplingar`);
      
      // 4. Ta bort alla lägenheter som inte är kopplade till en hyresgäst
      const [deleteApartmentsResult] = await connection.execute(`
        DELETE FROM apartments 
        WHERE id NOT IN (${apartmentIdsToKeep.map(id => '?').join(',')})
      `, apartmentIdsToKeep);
      
      console.log(`Raderade ${deleteApartmentsResult.affectedRows} lägenheter som inte hade kopplingar`);
      
      // Bekräfta transaktionen
      await connection.commit();
      console.log('Transaktion bekräftad');
      
    } catch (error) {
      // Återställ transaktionen vid fel
      await connection.rollback();
      console.error('Transaktion rullades tillbaka p.g.a. fel:', error.message);
      throw error;
    }
    
    // Kontrollera ny status
    const [newTenantCount] = await connection.execute('SELECT COUNT(*) as count FROM tenants');
    console.log(`\nAntal hyresgäster efter rensning: ${newTenantCount[0].count}`);
    
    const [newAptCount] = await connection.execute('SELECT COUNT(*) as count FROM apartments');
    console.log(`Antal lägenheter efter rensning: ${newAptCount[0].count}`);
    
    const [newConnectionCount] = await connection.execute('SELECT COUNT(*) as count FROM tenant_apartments');
    console.log(`Antal kopplingar efter rensning: ${newConnectionCount[0].count}`);
    
    // Stäng anslutning
    await connection.end();
    console.log('\nDatabasrensning slutförd!');
    
  } catch (error) {
    console.error('Ett kritiskt fel uppstod:', error.message);
    process.exit(1);
  }
}

// Kör huvudfunktionen
main().catch(error => {
  console.error('Oväntat fel:', error);
  process.exit(1);
}); 