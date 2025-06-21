/**
 * Script för att hämta information om entiteten med ID 4febdf29-bd98-417d-940e-3edabae6f86f
 */
const mysql = require('mysql2/promise');

// Databasanslutning
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '1234',
  database: 'dfrm'
};

async function queryEntityById() {
  let connection;
  
  try {
    console.log('Ansluter till MySQL-databasen...');
    connection = await mysql.createConnection(dbConfig);
    console.log('Ansluten till databasen!');
    
    const id = '4febdf29-bd98-417d-940e-3edabae6f86f';
    
    // Kontrollera om ID:t finns i hyresgästtabellen
    console.log(`\nSöker efter hyresgäst med ID: ${id}`);
    const [tenantRows] = await connection.execute(`
      SELECT * FROM tenants 
      WHERE BIN_TO_UUID(id) = ?`, [id]);
    
    if (tenantRows.length > 0) {
      console.log('Hyresgäst hittad:');
      console.log(tenantRows[0]);
      
      // Hämta eventuella lägenheter för denna hyresgäst
      console.log('\nHämtar lägenheter för denna hyresgäst:');
      const [apartmentRows] = await connection.execute(`
        SELECT a.* FROM apartments a
        JOIN tenant_apartments ta ON a.id = ta.apartment_id
        WHERE BIN_TO_UUID(ta.tenant_id) = ?`, [id]);
      
      if (apartmentRows.length > 0) {
        console.log('Lägenheter hittade:');
        console.log(apartmentRows);
      } else {
        console.log('Inga lägenheter hittades för denna hyresgäst');
      }
      
      // Hämta eventuella nycklar för denna hyresgäst
      console.log('\nHämtar nycklar för denna hyresgäst:');
      const [keyRows] = await connection.execute(`
        SELECT k.* FROM nycklar k
        WHERE BIN_TO_UUID(k.tenant_id) = ?`, [id]);
      
      if (keyRows.length > 0) {
        console.log('Nycklar hittade:');
        console.log(keyRows);
      } else {
        console.log('Inga nycklar hittades för denna hyresgäst');
      }
      
      return;
    }
    
    // Kontrollera om ID:t finns i lägenhetstabellen
    console.log(`\nSöker efter lägenhet med ID: ${id}`);
    const [apartmentRows] = await connection.execute(`
      SELECT * FROM apartments 
      WHERE BIN_TO_UUID(id) = ?`, [id]);
    
    if (apartmentRows.length > 0) {
      console.log('Lägenhet hittad:');
      console.log(apartmentRows[0]);
      
      // Hämta eventuella hyresgäster för denna lägenhet
      console.log('\nHämtar hyresgäster för denna lägenhet:');
      const [tenantRows] = await connection.execute(`
        SELECT t.* FROM tenants t
        JOIN tenant_apartments ta ON t.id = ta.tenant_id
        WHERE BIN_TO_UUID(ta.apartment_id) = ?`, [id]);
      
      if (tenantRows.length > 0) {
        console.log('Hyresgäster hittade:');
        console.log(tenantRows);
      } else {
        console.log('Inga hyresgäster hittades för denna lägenhet');
      }
      
      return;
    }
    
    // Kontrollera om ID:t finns i nyckeltabellen
    console.log(`\nSöker efter nyckel med ID: ${id}`);
    const [keyRows] = await connection.execute(`
      SELECT * FROM nycklar 
      WHERE BIN_TO_UUID(id) = ?`, [id]);
    
    if (keyRows.length > 0) {
      console.log('Nyckel hittad:');
      console.log(keyRows[0]);
      return;
    }
    
    console.log('Ingen entitet med detta ID hittades i databasen');
    
  } catch (error) {
    console.error('Fel vid sökning efter entitet:', error);
  } finally {
    if (connection) {
      console.log('Stänger databasanslutningen...');
      await connection.end();
    }
  }
}

queryEntityById().catch(console.error); 