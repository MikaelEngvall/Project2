/**
 * Script to check MySQL database structure
 */
const mysql = require('mysql2/promise');

// Database configuration
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '1234',
  database: 'dfrm'
};

async function checkDatabase() {
  let connection;
  
  try {
    console.log('Connecting to MySQL database...');
    connection = await mysql.createConnection(dbConfig);
    console.log('Connected to database!');
    
    // List all tables
    console.log('Listing all tables in the database:');
    const [tables] = await connection.execute('SHOW TABLES');
    console.log('Tables:');
    tables.forEach(table => {
      const tableName = table[Object.keys(table)[0]];
      console.log(`- ${tableName}`);
    });
    
    // For each table, get its structure
    for (const table of tables) {
      const tableName = table[Object.keys(table)[0]];
      console.log(`\nStructure of ${tableName}:`);
      
      // Get columns
      const [columns] = await connection.execute(`DESCRIBE ${tableName}`);
      columns.forEach(column => {
        console.log(`  - ${column.Field} (${column.Type}) ${column.Key === 'PRI' ? 'PRIMARY KEY' : ''}`);
      });
      
      // Get sample data
      console.log(`\nSample data from ${tableName} (first 3 rows):`);
      try {
        const [rows] = await connection.execute(`SELECT * FROM ${tableName} LIMIT 3`);
        console.log(rows);
      } catch (error) {
        console.error(`  Error getting sample data: ${error.message}`);
      }
    }
    
    // Get relationship tables
    console.log('\nLooking for relationship tables:');
    const relationTables = tables.filter(table => {
      const tableName = table[Object.keys(table)[0]];
      return tableName.includes('_') || tableName.includes('tenant') || tableName.includes('apartment');
    });
    
    if (relationTables.length > 0) {
      console.log('Potential relationship tables:');
      relationTables.forEach(table => {
        console.log(`- ${table[Object.keys(table)[0]]}`);
      });
    } else {
      console.log('No obvious relationship tables found.');
    }
    
  } catch (error) {
    console.error('Error checking database:', error);
  } finally {
    if (connection) {
      console.log('\nClosing database connection...');
      await connection.end();
    }
  }
}

// Run the script
console.log('Starting database structure check...');
checkDatabase()
  .then(() => console.log('Check completed'))
  .catch(error => console.error('Check failed:', error)); 