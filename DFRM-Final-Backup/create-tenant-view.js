/**
 * Script to create a database view for tenants with their apartments
 */
const mysql = require('mysql2/promise');

// Database configuration
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '1234',
  database: 'dfrm'
};

async function createTenantView() {
  let connection;
  
  try {
    console.log('Connecting to MySQL database...');
    connection = await mysql.createConnection(dbConfig);
    console.log('Connected to database!');
    
    // First check if view already exists and drop it
    console.log('Checking if tenant view already exists...');
    try {
      await connection.execute('DROP VIEW IF EXISTS tenant_with_apartment');
      console.log('Dropped existing view if it existed');
    } catch (error) {
      console.error('Error dropping view:', error.message);
    }
    
    // Create the view
    console.log('Creating new tenant_with_apartment view...');
    const createViewSQL = `
    CREATE VIEW tenant_with_apartment AS
    SELECT 
      t.id,
      t.first_name,
      t.last_name,
      t.email,
      t.phone_number,
      t.moved_in_date,
      t.resiliation_date,
      t.personnummer,
      t.comment,
      t.is_temporary,
      a.id AS apartment_id,
      a.street,
      a.number,
      a.apartment_number,
      a.postal_code,
      a.city,
      a.area,
      a.rooms,
      a.price,
      CONCAT(a.street, ' ', a.number, ', lägenhet ', a.apartment_number) AS full_address
    FROM 
      tenants t
    LEFT JOIN 
      tenant_apartments ta ON t.id = ta.tenant_id
    LEFT JOIN 
      apartments a ON ta.apartment_id = a.id
    `;
    
    await connection.execute(createViewSQL);
    console.log('Successfully created tenant_with_apartment view');
    
    // Check that the view works
    console.log('Verifying the view with a sample query...');
    const [rows] = await connection.execute('SELECT id, first_name, last_name, email, full_address FROM tenant_with_apartment LIMIT 10');
    
    console.log('Sample data from the new view:');
    rows.forEach(row => {
      console.log(`${row.first_name} ${row.last_name} (${row.email}) - Address: ${row.full_address || 'No apartment assigned'}`);
    });
    
    // Create an additional view for those with multiple apartments
    console.log('\nCreating tenant_apartments_summary view for tenants with multiple apartments...');
    await connection.execute('DROP VIEW IF EXISTS tenant_apartments_summary');
    
    const createSummaryViewSQL = `
    CREATE VIEW tenant_apartments_summary AS
    SELECT 
      t.id,
      t.first_name,
      t.last_name,
      t.email,
      t.phone_number,
      COUNT(ta.apartment_id) AS apartment_count,
      GROUP_CONCAT(
        CONCAT(a.street, ' ', a.number, ', lägenhet ', a.apartment_number) 
        SEPARATOR ' | '
      ) AS all_apartments
    FROM 
      tenants t
    LEFT JOIN 
      tenant_apartments ta ON t.id = ta.tenant_id
    LEFT JOIN 
      apartments a ON ta.apartment_id = a.id
    GROUP BY 
      t.id, t.first_name, t.last_name, t.email, t.phone_number
    `;
    
    await connection.execute(createSummaryViewSQL);
    console.log('Successfully created tenant_apartments_summary view');
    
    // Check the summary view
    console.log('Verifying the summary view...');
    const [summaryRows] = await connection.execute('SELECT * FROM tenant_apartments_summary WHERE apartment_count > 1');
    
    if (summaryRows.length > 0) {
      console.log('\nTenants with multiple apartments:');
      summaryRows.forEach(row => {
        console.log(`${row.first_name} ${row.last_name} (${row.email}) - Apartments (${row.apartment_count}): ${row.all_apartments}`);
      });
    } else {
      console.log('No tenants with multiple apartments found in the summary view');
    }
    
    return {
      success: true,
      message: 'Views created successfully'
    };
  } catch (error) {
    console.error('Error creating tenant view:', error);
    return {
      success: false,
      message: `Failed to create view: ${error.message}`
    };
  } finally {
    if (connection) {
      console.log('\nClosing database connection...');
      await connection.end();
    }
  }
}

// Run the script
console.log('Starting to create tenant with apartment view...');
createTenantView()
  .then(result => {
    console.log('\nOperation completed:');
    console.log(result);
  })
  .catch(error => {
    console.error('\nOperation failed:', error);
  }); 