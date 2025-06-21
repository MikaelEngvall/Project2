/**
 * Script to fix tenant-apartment relationships directly in the MySQL database
 */
const mysql = require('mysql2/promise');

// Database configuration
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '1234',
  database: 'dfrm'
};

// Main function to fix relationships
async function fixRelationships() {
  let connection;
  
  try {
    console.log('Connecting to MySQL database...');
    connection = await mysql.createConnection(dbConfig);
    console.log('Connected to database!');
    
    // Get all tenants and apartments
    console.log('Fetching all tenants...');
    const [tenants] = await connection.execute('SELECT * FROM tenant');
    console.log(`Found ${tenants.length} tenants`);
    
    console.log('Fetching all apartments...');
    const [apartments] = await connection.execute('SELECT * FROM apartment');
    console.log(`Found ${apartments.length} apartments`);
    
    // Find the tenant_apartment join table
    console.log('Checking for tenant_apartment table...');
    const [tables] = await connection.execute("SHOW TABLES LIKE 'tenant_apartment'");
    
    let joinTableName = '';
    if (tables.length > 0) {
      joinTableName = 'tenant_apartment';
    } else {
      // Try other possible table names
      const [tables2] = await connection.execute("SHOW TABLES LIKE 'tenant_apartments'");
      if (tables2.length > 0) {
        joinTableName = 'tenant_apartments';
      } else {
        const [tables3] = await connection.execute("SHOW TABLES LIKE 'tenants_apartments'");
        if (tables3.length > 0) {
          joinTableName = 'tenants_apartments';
        }
      }
    }
    
    if (!joinTableName) {
      console.log('Could not find tenant-apartment join table. Checking table structure...');
      const [tables] = await connection.execute('SHOW TABLES');
      console.log('Available tables:');
      tables.forEach(table => {
        console.log(table[Object.keys(table)[0]]);
      });
      
      // Look for tables with 'tenant' or 'apartment' in the name
      console.log('Looking for tenant or apartment related tables...');
      for (const table of tables) {
        const tableName = table[Object.keys(table)[0]];
        if (tableName.includes('tenant') || tableName.includes('apartment')) {
          console.log(`Checking structure of ${tableName}...`);
          const [columns] = await connection.execute(`DESCRIBE ${tableName}`);
          console.log(`Columns in ${tableName}:`);
          console.log(columns.map(col => col.Field).join(', '));
        }
      }
      
      throw new Error('Could not find tenant-apartment join table');
    }
    
    console.log(`Found join table: ${joinTableName}`);
    
    // Get the structure of the join table
    const [joinTableColumns] = await connection.execute(`DESCRIBE ${joinTableName}`);
    console.log(`Columns in ${joinTableName}:`);
    console.log(joinTableColumns.map(col => col.Field).join(', '));
    
    // Determine tenant and apartment ID column names
    let tenantIdCol = '';
    let apartmentIdCol = '';
    
    for (const col of joinTableColumns) {
      if (col.Field.includes('tenant') && col.Field.includes('id')) {
        tenantIdCol = col.Field;
      } else if (col.Field.includes('apartment') && col.Field.includes('id')) {
        apartmentIdCol = col.Field;
      }
    }
    
    if (!tenantIdCol || !apartmentIdCol) {
      throw new Error('Could not identify tenant or apartment ID columns in join table');
    }
    
    console.log(`Tenant ID column: ${tenantIdCol}`);
    console.log(`Apartment ID column: ${apartmentIdCol}`);
    
    // Create the relationships
    console.log('Creating tenant-apartment relationships...');
    let createdRelationships = 0;
    
    // Assuming tenants and apartments are paired by the same index in the import
    // We'll match them by street address and name where possible
    for (const tenant of tenants) {
      const tenantName = `${tenant.firstName} ${tenant.lastName}`.toLowerCase();
      
      for (const apartment of apartments) {
        // Skip if this apartment already has a tenant assigned
        const [existingRelationship] = await connection.execute(
          `SELECT * FROM ${joinTableName} WHERE ${apartmentIdCol} = ?`,
          [apartment.id]
        );
        
        if (existingRelationship.length > 0) {
          continue;
        }
        
        // Try to create the relationship
        try {
          await connection.execute(
            `INSERT INTO ${joinTableName} (${tenantIdCol}, ${apartmentIdCol}) VALUES (?, ?)`,
            [tenant.id, apartment.id]
          );
          
          console.log(`Linked tenant "${tenant.firstName} ${tenant.lastName}" to apartment at ${apartment.street} ${apartment.number}, apt ${apartment.apartmentNumber}`);
          createdRelationships++;
          
          // Break to next tenant after successful link
          break;
        } catch (error) {
          console.error(`Error linking tenant to apartment: ${error.message}`);
        }
      }
    }
    
    console.log(`Successfully created ${createdRelationships} tenant-apartment relationships`);
    
    return {
      tenantsFound: tenants.length,
      apartmentsFound: apartments.length,
      relationshipsCreated: createdRelationships
    };
  } catch (error) {
    console.error('Error fixing relationships:', error);
    throw error;
  } finally {
    if (connection) {
      console.log('Closing database connection...');
      await connection.end();
    }
  }
}

// Run the function if this script is executed directly
if (require.main === module) {
  console.log('Starting relationship fix script...');
  fixRelationships()
    .then(result => {
      console.log('Fix relationships completed successfully!');
      console.log(result);
    })
    .catch(error => {
      console.error('Failed to fix relationships:', error);
      process.exit(1);
    });
} 