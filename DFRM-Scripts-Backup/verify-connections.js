/**
 * Verifierar kopplingar mellan hyresgäster och lägenheter
 */

const mysql = require('mysql2/promise');
const dbConfig = require('./db-config');

async function main() {
  try {
    console.log('Verifierar kopplingar mellan hyresgäster och lägenheter...');
    
    // Upprätta databasanslutning
    const connection = await mysql.createConnection(dbConfig);
    console.log('Databasanslutning upprättad');
    
    // Kontrollera hur många kopplingar som finns i tenant_apartments
    const [connectionResults] = await connection.execute(
      'SELECT COUNT(*) as count FROM tenant_apartments'
    );
    const connectionCount = connectionResults[0].count;
    console.log(`Antal kopplingar i tenant_apartments: ${connectionCount}`);
    
    // Hämta antal hyresgäster och lägenheter
    const [tenantResults] = await connection.execute(
      'SELECT COUNT(*) as count FROM tenants'
    );
    const tenantCount = tenantResults[0].count;
    console.log(`Antal hyresgäster i databasen: ${tenantCount}`);
    
    const [apartmentResults] = await connection.execute(
      'SELECT COUNT(*) as count FROM apartments'
    );
    const apartmentCount = apartmentResults[0].count;
    console.log(`Antal lägenheter i databasen: ${apartmentCount}`);
    
    // Hämta några exempel på kopplingar för verifiering
    console.log('\nExempel på kopplingar (10 stycken):');
    const [connections] = await connection.execute(`
      SELECT 
        t.first_name, t.last_name, 
        a.street, a.number, a.apartment_number
      FROM tenant_apartments ta
      JOIN tenants t ON ta.tenant_id = t.id
      JOIN apartments a ON ta.apartment_id = a.id
      LIMIT 10
    `);
    
    connections.forEach((connection, index) => {
      console.log(`${index+1}. ${connection.first_name} ${connection.last_name} -> ${connection.street} ${connection.number || ''} ${connection.apartment_number || ''}`);
    });
    
    // Kontrollera om några hyresgäster saknar kopplingar
    const [unlinkedTenants] = await connection.execute(`
      SELECT t.id, t.first_name, t.last_name
      FROM tenants t
      LEFT JOIN tenant_apartments ta ON t.id = ta.tenant_id
      WHERE ta.tenant_id IS NULL
      LIMIT 5
    `);
    
    console.log(`\nAntal hyresgäster utan koppling: ${unlinkedTenants.length}`);
    if (unlinkedTenants.length > 0) {
      console.log('Exempel på hyresgäster utan koppling:');
      unlinkedTenants.forEach((tenant, index) => {
        console.log(`${index+1}. ${tenant.first_name} ${tenant.last_name}`);
      });
    }
    
    // Kontrollera om några lägenheter saknar kopplingar
    const [unlinkedApartments] = await connection.execute(`
      SELECT a.id, a.street, a.number, a.apartment_number
      FROM apartments a
      LEFT JOIN tenant_apartments ta ON a.id = ta.apartment_id
      WHERE ta.apartment_id IS NULL
      LIMIT 5
    `);
    
    console.log(`\nAntal lägenheter utan koppling: ${unlinkedApartments.length}`);
    if (unlinkedApartments.length > 0) {
      console.log('Exempel på lägenheter utan koppling:');
      unlinkedApartments.forEach((apartment, index) => {
        console.log(`${index+1}. ${apartment.street} ${apartment.number || ''} ${apartment.apartment_number || ''}`);
      });
    }
    
    // Stäng anslutning
    await connection.end();
    console.log('\nVerifiering slutförd!');
    
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