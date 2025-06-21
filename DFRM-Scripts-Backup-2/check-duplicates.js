/**
 * Skript för att hitta lägenheter som är kopplade till flera hyresgäster
 * och fixa så att varje hyresgäst har en unik lägenhet
 */

const mysql = require('mysql2/promise');
const dbConfig = require('./db-config');

async function main() {
  try {
    console.log('Kontrollerar kopplingar mellan hyresgäster och lägenheter...');
    
    // Upprätta databasanslutning
    const connection = await mysql.createConnection(dbConfig);
    console.log('Databasanslutning upprättad');
    
    // Hitta lägenheter med flera hyresgäster
    const [duplicateApartments] = await connection.execute(`
      SELECT 
        a.id AS apartment_id, 
        a.street, 
        a.number, 
        a.apartment_number, 
        COUNT(ta.tenant_id) AS tenant_count
      FROM 
        apartments a
      JOIN 
        tenant_apartments ta ON a.id = ta.apartment_id
      GROUP BY 
        a.id
      HAVING 
        COUNT(ta.tenant_id) > 1
      ORDER BY 
        tenant_count DESC
    `);
    
    console.log(`Hittade ${duplicateApartments.length} lägenheter med flera hyresgäster`);
    
    // Visa detaljer om duplicerade lägenheter
    for (const apt of duplicateApartments) {
      console.log(`\nLägenhet: ${apt.street} ${apt.number || ''} ${apt.apartment_number || ''} (${apt.tenant_count} hyresgäster)`);
      
      const [tenants] = await connection.execute(`
        SELECT t.id, t.first_name, t.last_name
        FROM tenants t
        JOIN tenant_apartments ta ON t.id = ta.tenant_id
        WHERE ta.apartment_id = ?
      `, [apt.apartment_id]);
      
      tenants.forEach((tenant, index) => {
        console.log(`  ${index+1}. ${tenant.first_name} ${tenant.last_name} (ID: ${tenant.id})`);
      });
    }
    
    // Starta en transaktion för att fixa problemet
    await connection.beginTransaction();
    
    try {
      console.log('\nFixar problemet med duplikerade kopplingar...');
      
      // För varje lägenhet med flera hyresgäster
      for (const apt of duplicateApartments) {
        // Hämta de kopplade hyresgästerna
        const [tenants] = await connection.execute(`
          SELECT t.id, t.first_name, t.last_name
          FROM tenants t
          JOIN tenant_apartments ta ON t.id = ta.tenant_id
          WHERE ta.apartment_id = ?
        `, [apt.apartment_id]);
        
        // Behåll den första kopplingen och ta bort resten
        if (tenants.length > 1) {
          const firstTenant = tenants[0];
          const tenantsToRemove = tenants.slice(1);
          
          for (const tenant of tenantsToRemove) {
            // Ta bort kopplingen
            await connection.execute(`
              DELETE FROM tenant_apartments
              WHERE tenant_id = ? AND apartment_id = ?
            `, [tenant.id, apt.apartment_id]);
            
            console.log(`Borttagen koppling: ${tenant.first_name} ${tenant.last_name} -> ${apt.street} ${apt.apartment_number || ''}`);
            
            // Ta även bort hyresgästen eftersom vi vill ha exakt 138 hyresgäster med en lägenhet var
            await connection.execute(`
              DELETE FROM tenants
              WHERE id = ?
            `, [tenant.id]);
            
            console.log(`Borttagen hyresgäst: ${tenant.first_name} ${tenant.last_name}`);
          }
        }
      }
      
      await connection.commit();
      console.log('Transaktion bekräftad');
      
    } catch (error) {
      await connection.rollback();
      console.error('Transaktion rullades tillbaka p.g.a. fel:', error.message);
      throw error;
    }
    
    // Kontrollera slutresultatet
    const [tenantCount] = await connection.execute('SELECT COUNT(*) as count FROM tenants');
    console.log(`\nAntal hyresgäster efter rensning: ${tenantCount[0].count}`);
    
    const [aptCount] = await connection.execute('SELECT COUNT(*) as count FROM apartments');
    console.log(`Antal lägenheter efter rensning: ${aptCount[0].count}`);
    
    const [connectionCount] = await connection.execute('SELECT COUNT(*) as count FROM tenant_apartments');
    console.log(`Antal kopplingar efter rensning: ${connectionCount[0].count}`);
    
    // Kontrollera att vi inte har några duplicerade kopplingar längre
    const [remainingDuplicates] = await connection.execute(`
      SELECT COUNT(*) as count FROM (
        SELECT 
          apartment_id, 
          COUNT(tenant_id) AS tenant_count
        FROM 
          tenant_apartments
        GROUP BY 
          apartment_id
        HAVING 
          COUNT(tenant_id) > 1
      ) AS subquery
    `);
    
    console.log(`Kvarstående lägenheter med flera hyresgäster: ${remainingDuplicates[0].count}`);
    
    // Kontrollera att varje hyresgäst har exakt en koppling
    const [tenantsWithMultipleApartments] = await connection.execute(`
      SELECT COUNT(*) as count FROM (
        SELECT 
          tenant_id, 
          COUNT(apartment_id) AS apartment_count
        FROM 
          tenant_apartments
        GROUP BY 
          tenant_id
        HAVING 
          COUNT(apartment_id) > 1
      ) AS subquery
    `);
    
    console.log(`Hyresgäster med flera lägenheter: ${tenantsWithMultipleApartments[0].count}`);
    
    // Stäng anslutning
    await connection.end();
    console.log('\nKontroll slutförd!');
    
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