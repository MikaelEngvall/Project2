/**
 * Simple script to query the tenant view and format results nicely
 */
const mysql = require('mysql2/promise');

// Database configuration
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '1234',
  database: 'dfrm'
};

async function queryTenantView() {
  let connection;
  
  try {
    console.log('Connecting to MySQL database...');
    connection = await mysql.createConnection(dbConfig);
    console.log('Connected to database!');
    
    // Query all tenants with their apartment info
    console.log('Fetching all tenants with apartment information:');
    const [rows] = await connection.execute(`
      SELECT 
        id, 
        first_name, 
        last_name, 
        email, 
        phone_number, 
        moved_in_date, 
        apartment_number,
        street,
        number,
        area,
        price,
        full_address
      FROM 
        tenant_with_apartment
      ORDER BY
        last_name, first_name
      LIMIT 25
    `);
    
    // Format and display the results in a table-like format
    console.log('\n===== TENANTS WITH APARTMENT INFORMATION =====');
    console.log('ID | NAME | EMAIL | PHONE | MOVED IN | ADDRESS | AREA | PRICE');
    console.log('-----------------------------------------------------------------------------------');
    
    rows.forEach(row => {
      // Format date
      const movedInDate = row.moved_in_date ? new Date(row.moved_in_date).toLocaleDateString('sv-SE') : 'N/A';
      
      // Format price with Swedish formatting
      const price = row.price ? new Intl.NumberFormat('sv-SE', { 
        style: 'currency', 
        currency: 'SEK',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0 
      }).format(row.price) : 'N/A';
      
      // Format area
      const area = row.area ? `${row.area} m²` : 'N/A';
      
      // Print the row
      console.log(`${row.id.toString('hex').substring(0, 8)} | ${row.first_name} ${row.last_name} | ${row.email || 'N/A'} | ${row.phone_number || 'N/A'} | ${movedInDate} | ${row.full_address || 'Ingen lägenhet'} | ${area} | ${price}`);
    });
    
    // Check for tenants with multiple apartments
    console.log('\n\n===== TENANTS WITH MULTIPLE APARTMENTS =====');
    const [multiAptTenants] = await connection.execute(`
      SELECT 
        id, 
        first_name, 
        last_name, 
        email, 
        phone_number,
        apartment_count, 
        all_apartments
      FROM 
        tenant_apartments_summary
      WHERE
        apartment_count > 1
      ORDER BY
        apartment_count DESC, last_name, first_name
    `);
    
    if (multiAptTenants.length > 0) {
      console.log('NAME | EMAIL | PHONE | APARTMENT COUNT | APARTMENTS');
      console.log('-----------------------------------------------------------------------------------');
      
      multiAptTenants.forEach(row => {
        console.log(`${row.first_name} ${row.last_name} | ${row.email || 'N/A'} | ${row.phone_number || 'N/A'} | ${row.apartment_count} | ${row.all_apartments}`);
      });
    } else {
      console.log('No tenants with multiple apartments found');
    }
    
    // Check for tenants without apartments
    console.log('\n\n===== TENANTS WITHOUT APARTMENTS =====');
    const [noAptTenants] = await connection.execute(`
      SELECT 
        id, 
        first_name, 
        last_name, 
        email, 
        phone_number,
        moved_in_date
      FROM 
        tenant_with_apartment
      WHERE
        apartment_id IS NULL
      ORDER BY
        last_name, first_name
    `);
    
    if (noAptTenants.length > 0) {
      console.log('NAME | EMAIL | PHONE | MOVED IN');
      console.log('-----------------------------------------------------------------------------------');
      
      noAptTenants.forEach(row => {
        // Format date
        const movedInDate = row.moved_in_date ? new Date(row.moved_in_date).toLocaleDateString('sv-SE') : 'N/A';
        
        console.log(`${row.first_name} ${row.last_name} | ${row.email || 'N/A'} | ${row.phone_number || 'N/A'} | ${movedInDate}`);
      });
    } else {
      console.log('All tenants have apartments assigned');
    }
    
    return {
      success: true,
      tenantCount: rows.length
    };
  } catch (error) {
    console.error('Error querying tenant view:', error);
    return {
      success: false,
      message: `Failed to query view: ${error.message}`
    };
  } finally {
    if (connection) {
      console.log('\nClosing database connection...');
      await connection.end();
    }
  }
}

// Run the script
console.log('Starting to query tenant view...');
queryTenantView()
  .then(result => {
    console.log('\nOperation completed:');
    console.log(result);
  })
  .catch(error => {
    console.error('\nOperation failed:', error);
  }); 