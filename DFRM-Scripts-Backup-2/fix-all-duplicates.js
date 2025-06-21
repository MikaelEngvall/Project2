/**
 * Skript för att fixa problemet med dubletter i alla lägenheter
 * Skriptet skapar kopior av lägenheter så att varje hyresgäst får sin egen
 */

const mysql = require('mysql2/promise');
const { v4: uuidv4 } = require('uuid');
const dbConfig = require('./db-config');

async function main() {
  try {
    console.log('Startar fix för alla lägenheter med flera hyresgäster...');
    
    // Upprätta databasanslutning
    const connection = await mysql.createConnection(dbConfig);
    console.log('Databasanslutning upprättad');
    
    // Hitta alla lägenheter med flera hyresgäster
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
        a.street, a.number, a.apartment_number
    `);
    
    console.log(`Hittade ${duplicateApartments.length} lägenheter med flera hyresgäster`);
    
    if (duplicateApartments.length === 0) {
      console.log('Inga lägenheter med flera hyresgäster hittades. Allt är redan fixat!');
      await connection.end();
      return;
    }
    
    // Starta en transaktion
    await connection.beginTransaction();
    
    try {
      let fixedCount = 0;
      
      // För varje lägenhet med flera hyresgäster
      for (const apt of duplicateApartments) {
        console.log(`\nFixar lägenhet: ${apt.street} ${apt.number || ''} ${apt.apartment_number || ''} (${apt.tenant_count} hyresgäster)`);
        
        // Hämta original lägenhetens data
        const [aptDetails] = await connection.execute(`
          SELECT * FROM apartments WHERE id = ?
        `, [apt.apartment_id]);
        
        if (aptDetails.length === 0) {
          console.log(`Kunde inte hitta lägenhet med ID ${apt.apartment_id}`);
          continue;
        }
        
        const originalApt = aptDetails[0];
        
        // Hämta alla hyresgäster för denna lägenhet
        const [tenants] = await connection.execute(`
          SELECT t.id, t.first_name, t.last_name
          FROM tenants t
          JOIN tenant_apartments ta ON t.id = ta.tenant_id
          WHERE ta.apartment_id = ?
        `, [apt.apartment_id]);
        
        console.log(`  Lägenhet har ${tenants.length} hyresgäster`);
        
        // Behåll första hyresgästen med originallägenheten
        const firstTenant = tenants[0];
        console.log(`  Behåller hyresgäst: ${firstTenant.first_name} ${firstTenant.last_name} för originallägenheten`);
        
        // För resterande hyresgäster, skapa en kopia av lägenheten
        for (let i = 1; i < tenants.length; i++) {
          const tenant = tenants[i];
          
          // Skapa en kopia av lägenheten med ett nytt ID
          const newAptId = uuidv4();
          
          // Lägg till ett suffix för att göra lägenheten unik
          const newAptNumber = originalApt.apartment_number 
            ? `${originalApt.apartment_number}-${i}` 
            : `${i}`;
          
          // Skapa ny lägenhet
          await connection.execute(`
            INSERT INTO apartments (
              id, street, number, apartment_number, postal_code, city, 
              rooms, area, price, electricity, internet, is_temporary, storage
            ) VALUES (
              ?, ?, ?, ?, ?, ?, 
              ?, ?, ?, ?, ?, ?, ?
            )
          `, [
            newAptId,
            originalApt.street,
            originalApt.number,
            newAptNumber,
            originalApt.postal_code,
            originalApt.city,
            originalApt.rooms,
            originalApt.area,
            originalApt.price,
            originalApt.electricity,
            originalApt.internet,
            originalApt.is_temporary,
            originalApt.storage
          ]);
          
          // Uppdatera kopplingen för hyresgästen
          await connection.execute(`
            UPDATE tenant_apartments 
            SET apartment_id = ? 
            WHERE tenant_id = ? AND apartment_id = ?
          `, [newAptId, tenant.id, apt.apartment_id]);
          
          console.log(`  Skapade ny lägenhet ${originalApt.street} ${originalApt.number || ''} ${newAptNumber} för ${tenant.first_name} ${tenant.last_name}`);
          fixedCount++;
        }
      }
      
      // Bekräfta transaktionen
      await connection.commit();
      console.log(`\nFixade ${fixedCount} kopplingar genom att skapa nya lägenheter`);
      
    } catch (error) {
      await connection.rollback();
      console.error('Transaktion rullades tillbaka p.g.a. fel:', error.message);
      throw error;
    }
    
    // Kontrollera slutresultatet
    const [tenantCount] = await connection.execute('SELECT COUNT(*) as count FROM tenants');
    console.log(`\nAntal hyresgäster efter fix: ${tenantCount[0].count}`);
    
    const [aptCount] = await connection.execute('SELECT COUNT(*) as count FROM apartments');
    console.log(`Antal lägenheter efter fix: ${aptCount[0].count}`);
    
    const [connectionCount] = await connection.execute('SELECT COUNT(*) as count FROM tenant_apartments');
    console.log(`Antal kopplingar efter fix: ${connectionCount[0].count}`);
    
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
    
    if (remainingDuplicates[0].count > 0) {
      console.log('\nVarning: Det finns fortfarande lägenheter med flera hyresgäster!');
      
      const [problemApts] = await connection.execute(`
        SELECT 
          a.id, a.street, a.number, a.apartment_number, 
          COUNT(ta.tenant_id) AS tenant_count
        FROM 
          apartments a
        JOIN 
          tenant_apartments ta ON a.id = ta.apartment_id
        GROUP BY 
          a.id
        HAVING 
          COUNT(ta.tenant_id) > 1
        LIMIT 5
      `);
      
      console.log('Exempel på problematiska lägenheter:');
      problemApts.forEach((apt, i) => {
        console.log(`  ${i+1}. ${apt.street} ${apt.number || ''} ${apt.apartment_number || ''} (${apt.tenant_count} hyresgäster)`);
      });
    } else {
      console.log('\nAlla lägenheter har nu endast en hyresgäst! ✓');
    }
    
    // Stäng anslutning
    await connection.end();
    console.log('\nFix av lägenheter slutförd!');
    
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