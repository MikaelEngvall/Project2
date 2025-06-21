/**
 * Script för att söka efter UUID i alla tabeller i databasen
 */
const mysql = require('mysql2/promise');

// Databasanslutning
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '1234',
  database: 'dfrm'
};

async function searchAllTables() {
  let connection;
  
  try {
    console.log('Ansluter till MySQL-databasen...');
    connection = await mysql.createConnection(dbConfig);
    console.log('Ansluten till databasen!');
    
    // Hämta alla tabeller i databasen
    console.log('Hämtar alla tabeller...');
    const [tables] = await connection.execute(`
      SELECT table_name
      FROM information_schema.tables
      WHERE table_schema = 'dfrm'
      AND table_type = 'BASE TABLE'`);
    
    console.log(`Hittade ${tables.length} tabeller.`);
    
    // UUID att söka efter
    const uuid = '4febdf29-bd98-417d-940e-3edabae6f86f';
    
    // Iterera genom varje tabell och sök efter UUID-kolumner
    let results = [];
    for (const table of tables) {
      const tableName = table.TABLE_NAME || table.table_name;
      
      // Hämta alla kolumner för tabellen
      const [columns] = await connection.execute(`
        SELECT column_name, data_type
        FROM information_schema.columns
        WHERE table_schema = 'dfrm'
        AND table_name = ?`, [tableName]);
      
      // Identifiera UUID-kolumner (BINARY(16) eller CHAR(36))
      for (const column of columns) {
        const columnName = column.COLUMN_NAME || column.column_name;
        const dataType = column.DATA_TYPE || column.data_type;
        
        if (dataType === 'binary' || dataType === 'char' || dataType === 'varchar') {
          // Sök efter UUID i den här kolumnen
          let queryResult;
          
          if (dataType === 'binary') {
            // För binära UUID-kolumner
            [queryResult] = await connection.execute(`
              SELECT *
              FROM ${tableName}
              WHERE BIN_TO_UUID(${columnName}) = ?`, [uuid]);
          } else {
            // För textbaserade UUID-kolumner
            [queryResult] = await connection.execute(`
              SELECT *
              FROM ${tableName}
              WHERE ${columnName} = ?`, [uuid]);
          }
          
          if (queryResult.length > 0) {
            results.push({
              table: tableName,
              column: columnName,
              dataType: dataType,
              count: queryResult.length,
              rows: queryResult
            });
          }
        }
      }
    }
    
    // Hämta alla vyer i databasen
    console.log('\nHämtar alla vyer...');
    const [views] = await connection.execute(`
      SELECT table_name
      FROM information_schema.tables
      WHERE table_schema = 'dfrm'
      AND table_type = 'VIEW'`);
    
    console.log(`Hittade ${views.length} vyer.`);
    
    // Iterera genom varje vy och sök efter UUID-kolumner
    for (const view of views) {
      const viewName = view.TABLE_NAME || view.table_name;
      
      // Hämta alla kolumner för vyn
      const [columns] = await connection.execute(`
        SELECT column_name, data_type
        FROM information_schema.columns
        WHERE table_schema = 'dfrm'
        AND table_name = ?`, [viewName]);
      
      // Identifiera kolumner som kan innehålla UUID
      for (const column of columns) {
        const columnName = column.COLUMN_NAME || column.column_name;
        const dataType = column.DATA_TYPE || column.data_type;
        
        // Försök hitta matchande värden
        try {
          // För alla kolumner, försök med LIKE-sökning för texter
          const [queryResult] = await connection.execute(`
            SELECT *
            FROM ${viewName}
            WHERE ${columnName} LIKE ?`, [`%${uuid}%`]);
          
          if (queryResult.length > 0) {
            results.push({
              table: `${viewName} (VIEW)`,
              column: columnName,
              dataType: dataType,
              count: queryResult.length,
              rows: queryResult
            });
          }
        } catch (error) {
          // Ignorera fel som kan uppstå vid vissa datatyper
        }
      }
    }
    
    // Visa resultaten
    if (results.length > 0) {
      console.log(`\nHittade träffar för UUID '${uuid}' i följande tabeller/vyer:`);
      
      results.forEach(result => {
        console.log(`\nTabell/Vy: ${result.table}`);
        console.log(`Kolumn: ${result.column} (${result.dataType})`);
        console.log(`Antal träffar: ${result.count}`);
        
        console.log('Rader:');
        result.rows.forEach((row, index) => {
          console.log(`  ${index + 1}:`, row);
        });
      });
    } else {
      console.log(`\nHittade inga träffar för UUID '${uuid}' i någon tabell eller vy.`);
      
      // Försök lista alla tabeller med UUID-kolumner
      console.log('\nTabeller med UUID-kolumner (BINARY(16) eller CHAR(36)):');
      
      let uuidTables = [];
      for (const table of tables) {
        const tableName = table.TABLE_NAME || table.table_name;
        
        // Hämta eventuella UUID-kolumner
        const [uuidColumns] = await connection.execute(`
          SELECT column_name, data_type
          FROM information_schema.columns
          WHERE table_schema = 'dfrm'
          AND table_name = ?
          AND (data_type = 'binary' OR data_type = 'char' OR data_type = 'varchar')`, [tableName]);
        
        if (uuidColumns.length > 0) {
          uuidTables.push({
            table: tableName,
            columns: uuidColumns.map(col => `${col.COLUMN_NAME || col.column_name} (${col.DATA_TYPE || col.data_type})`)
          });
        }
      }
      
      if (uuidTables.length > 0) {
        uuidTables.forEach(table => {
          console.log(`- ${table.table}:`);
          table.columns.forEach(col => {
            console.log(`  - ${col}`);
          });
        });
      } else {
        console.log('  Inga tabeller med UUID-kolumner hittades.');
      }
    }
    
  } catch (error) {
    console.error('Ett fel inträffade:', error);
  } finally {
    if (connection) {
      console.log('\nStänger databasanslutningen...');
      await connection.end();
    }
  }
}

searchAllTables().catch(console.error); 