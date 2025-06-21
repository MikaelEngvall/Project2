/**
 * Skript för att kontrollera kopplingar mellan hyresgäster och lägenheter
 */

const mysql = require('mysql2/promise');
const dbConfig = require('./db-config');

async function main() {
  try {
    console.log('Ansluter till databas för att kontrollera kopplingar...');
    
    // Upprätta databasanslutning
    const connection = await mysql.createConnection(dbConfig);
    console.log('Databasanslutning upprättad');
    
    // Kontrollera att varje hyresgäst har exakt en lägenhet
    const [tenantConnectionCounts] = await connection.execute(`
      SELECT 
        tenant_id, 
        COUNT(apartment_id) AS apartment_count
      FROM 
        tenant_apartments
      GROUP BY 
        tenant_id
      HAVING 
        COUNT(apartment_id) <> 1
      ORDER BY 
        COUNT(apartment_id) DESC
    `);
    
    console.log(`\nHyresgäster med annat än exakt en lägenhet: ${tenantConnectionCounts.length}`);
    
    if (tenantConnectionCounts.length > 0) {
      console.log('Hyresgäster med problem:');
      for (const tenant of tenantConnectionCounts) {
        const [tenantInfo] = await connection.execute(`
          SELECT id, first_name, last_name FROM tenants WHERE id = ?
        `, [tenant.tenant_id]);
        
        if (tenantInfo.length > 0) {
          console.log(`  ${tenantInfo[0].first_name} ${tenantInfo[0].last_name}: ${tenant.apartment_count} lägenheter`);
        } else {
          console.log(`  Okänd hyresgäst (ID: ${tenant.tenant_id}): ${tenant.apartment_count} lägenheter`);
        }
      }
    } else {
      console.log('Alla hyresgäster har exakt en koppling till en lägenhet! ✓');
    }
    
    // Kontrollera om det finns lägenheter med flera hyresgäster
    const [apartmentConnectionCounts] = await connection.execute(`
      SELECT 
        apartment_id, 
        COUNT(tenant_id) AS tenant_count
      FROM 
        tenant_apartments
      GROUP BY 
        apartment_id
      HAVING 
        COUNT(tenant_id) > 1
      ORDER BY 
        COUNT(tenant_id) DESC
    `);
    
    console.log(`\nLägenheter med flera hyresgäster: ${apartmentConnectionCounts.length}`);
    
    if (apartmentConnectionCounts.length > 0) {
      console.log('Lägenheter med problem:');
      for (const apt of apartmentConnectionCounts) {
        const [aptInfo] = await connection.execute(`
          SELECT id, street, number, apartment_number FROM apartments WHERE id = ?
        `, [apt.apartment_id]);
        
        if (aptInfo.length > 0) {
          console.log(`  ${aptInfo[0].street} ${aptInfo[0].number || ''} ${aptInfo[0].apartment_number || ''}: ${apt.tenant_count} hyresgäster`);
        } else {
          console.log(`  Okänd lägenhet (ID: ${apt.apartment_id}): ${apt.tenant_count} hyresgäster`);
        }
      }
    } else {
      console.log('Alla lägenheter har högst en hyresgäst! ✓');
    }
    
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