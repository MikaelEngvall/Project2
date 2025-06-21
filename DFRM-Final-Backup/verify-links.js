/**
 * Script to verify tenant-apartment links in the database
 */
const mysql = require('mysql2/promise');

// Database configuration
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '1234',
  database: 'dfrm'
};

async function verifyLinks() {
  let connection;
  
  try {
    console.log('Connecting to MySQL database...');
    connection = await mysql.createConnection(dbConfig);
    console.log('Connected to database!');
    
    // Check existing links
    console.log('Checking tenant-apartment links...');
    const [links] = await connection.execute(`
      SELECT 
        t.id AS tenant_id, 
        t.first_name, 
        t.last_name, 
        t.email,
        a.id AS apartment_id, 
        a.street, 
        a.number, 
        a.apartment_number,
        a.price
      FROM tenant_apartments ta
      JOIN tenants t ON ta.tenant_id = t.id
      JOIN apartments a ON ta.apartment_id = a.id
    `);
    
    console.log(`Found ${links.length} tenant-apartment links`);
    
    // Show sample of links
    console.log('\nSample of tenant-apartment links (first 10):');
    links.slice(0, 10).forEach(link => {
      console.log(`${link.first_name} ${link.last_name} (${link.email}) -> ${link.street} ${link.number}, apt ${link.apartment_number}`);
    });
    
    // Check tenants with more than one apartment
    console.log('\nChecking tenants with multiple apartments:');
    const [multiTenants] = await connection.execute(`
      SELECT 
        t.id, 
        t.first_name, 
        t.last_name,
        COUNT(ta.apartment_id) AS apartment_count
      FROM tenants t
      JOIN tenant_apartments ta ON t.id = ta.tenant_id
      GROUP BY t.id
      HAVING COUNT(ta.apartment_id) > 1
    `);
    
    if (multiTenants.length > 0) {
      console.log(`Found ${multiTenants.length} tenants with multiple apartments:`);
      for (const tenant of multiTenants) {
        console.log(`${tenant.first_name} ${tenant.last_name} has ${tenant.apartment_count} apartments`);
        
        // Get details of this tenant's apartments
        const [apartments] = await connection.execute(`
          SELECT a.street, a.number, a.apartment_number
          FROM apartments a
          JOIN tenant_apartments ta ON a.id = ta.apartment_id
          WHERE ta.tenant_id = ?
        `, [tenant.id]);
        
        apartments.forEach(apt => {
          console.log(`  - ${apt.street} ${apt.number}, apt ${apt.apartment_number}`);
        });
      }
    } else {
      console.log('No tenants with multiple apartments found.');
    }
    
    // Check apartments with more than one tenant
    console.log('\nChecking apartments with multiple tenants:');
    const [multiApartments] = await connection.execute(`
      SELECT 
        a.id, 
        a.street, 
        a.number, 
        a.apartment_number,
        COUNT(ta.tenant_id) AS tenant_count
      FROM apartments a
      JOIN tenant_apartments ta ON a.id = ta.apartment_id
      GROUP BY a.id
      HAVING COUNT(ta.tenant_id) > 1
    `);
    
    if (multiApartments.length > 0) {
      console.log(`Found ${multiApartments.length} apartments with multiple tenants:`);
      for (const apartment of multiApartments) {
        console.log(`${apartment.street} ${apartment.number}, apt ${apartment.apartment_number} has ${apartment.tenant_count} tenants`);
        
        // Get details of this apartment's tenants
        const [tenants] = await connection.execute(`
          SELECT t.first_name, t.last_name, t.email
          FROM tenants t
          JOIN tenant_apartments ta ON t.id = ta.tenant_id
          WHERE ta.apartment_id = ?
        `, [apartment.id]);
        
        tenants.forEach(tenant => {
          console.log(`  - ${tenant.first_name} ${tenant.last_name} (${tenant.email})`);
        });
      }
    } else {
      console.log('No apartments with multiple tenants found.');
    }
    
    // Check tenants without apartments
    console.log('\nChecking tenants without apartments:');
    const [unlinkedTenants] = await connection.execute(`
      SELECT id, first_name, last_name, email
      FROM tenants t
      WHERE NOT EXISTS (
        SELECT 1 FROM tenant_apartments ta WHERE ta.tenant_id = t.id
      )
    `);
    
    if (unlinkedTenants.length > 0) {
      console.log(`Found ${unlinkedTenants.length} tenants without apartments:`);
      unlinkedTenants.forEach(tenant => {
        console.log(`- ${tenant.first_name} ${tenant.last_name} (${tenant.email})`);
      });
    } else {
      console.log('All tenants have apartments assigned.');
    }
    
    // Check apartments without tenants
    console.log('\nChecking apartments without tenants:');
    const [unlinkedApartments] = await connection.execute(`
      SELECT id, street, number, apartment_number
      FROM apartments a
      WHERE NOT EXISTS (
        SELECT 1 FROM tenant_apartments ta WHERE ta.apartment_id = a.id
      )
    `);
    
    if (unlinkedApartments.length > 0) {
      console.log(`Found ${unlinkedApartments.length} apartments without tenants:`);
      unlinkedApartments.forEach(apt => {
        console.log(`- ${apt.street} ${apt.number}, apt ${apt.apartment_number}`);
      });
    } else {
      console.log('All apartments have tenants assigned.');
    }
    
    return {
      totalLinks: links.length,
      tenantsWithMultipleApartments: multiTenants.length,
      apartmentsWithMultipleTenants: multiApartments.length,
      tenantsWithoutApartments: unlinkedTenants.length,
      apartmentsWithoutTenants: unlinkedApartments.length
    };
    
  } catch (error) {
    console.error('Error verifying links:', error);
    throw error;
  } finally {
    if (connection) {
      console.log('\nClosing database connection...');
      await connection.end();
    }
  }
}

// Run the script
console.log('Starting verification of tenant-apartment links...');
verifyLinks()
  .then(result => {
    console.log('\nVerification completed successfully!');
    console.log('Summary:');
    console.log(result);
  })
  .catch(error => {
    console.error('\nVerification failed:', error);
    process.exit(1);
  }); 