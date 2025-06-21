/**
 * Script to link tenants with apartments directly in the database
 */
const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');

// Database configuration
const dbConfig = {
  host: 'localhost',
  user: 'root',
  password: '1234',
  database: 'dfrm'
};

// Helper function to normalize apartment number
const normalizeApartmentNumber = (num) => {
  if (!num) return '';
  // Convert "Lokal" to something we can identify
  if (num.toLowerCase() === 'lokal') return 'LOKAL';
  return num.toString().trim();
};

// Helper function to normalize names for comparison
const normalizeName = (name) => {
  if (!name) return '';
  return name.toLowerCase().trim()
    .replace(/\s+/g, ' ')  // Normalize spaces
    .replace(/[^\w\s]/g, ''); // Remove special characters
};

// Main function to link tenants with their apartments
async function linkTenantsWithApartments() {
  let connection;
  
  try {
    console.log('Connecting to MySQL database...');
    connection = await mysql.createConnection(dbConfig);
    console.log('Connected to database!');
    
    // Read the CSV file
    const csvFile = path.join(__dirname, 'Hyresgästsammanställning_fixed.csv');
    console.log(`Reading CSV file from: ${csvFile}`);
    const csvData = fs.readFileSync(csvFile, 'utf8');
    
    // Parse CSV data
    const lines = csvData.split('\n');
    const headers = lines[0].split(';');
    
    // Get all tenants from database
    console.log('Fetching all tenants from database...');
    const [tenants] = await connection.execute('SELECT id, first_name, last_name, email, phone_number FROM tenants');
    console.log(`Found ${tenants.length} tenants in the database`);
    
    // Get all apartments from database
    console.log('Fetching all apartments from database...');
    const [apartments] = await connection.execute('SELECT id, street, number, apartment_number FROM apartments');
    console.log(`Found ${apartments.length} apartments in the database`);
    
    // Check for existing links
    console.log('Checking for existing tenant-apartment links...');
    const [existingLinks] = await connection.execute('SELECT tenant_id, apartment_id FROM tenant_apartments');
    console.log(`Found ${existingLinks.length} existing links`);
    
    // Create a map of existing links to avoid duplicates
    const existingLinksMap = new Map();
    existingLinks.forEach(link => {
      // Convert Buffer to hex string
      const tenantIdHex = link.tenant_id.toString('hex');
      const apartmentIdHex = link.apartment_id.toString('hex');
      existingLinksMap.set(`${tenantIdHex}-${apartmentIdHex}`, true);
    });
    
    // Process each line in the CSV
    const links = [];
    let createdLinks = 0;
    
    console.log('Processing CSV data to create tenant-apartment links...');
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;
      
      const values = line.split(';');
      if (values.length < 7) continue;
      
      const firstName = values[0] || '';
      const lastName = values[1] || '';
      const street = values[2] || '';
      const number = values[3] || '';
      const email = values[4] || '';
      const phone = values[5] || '';
      const apartmentNumber = normalizeApartmentNumber(values[6] || '');
      
      // Find matching tenant in database
      const matchingTenant = tenants.find(tenant => {
        // Try to match by email first (most reliable)
        if (email && tenant.email && email.toLowerCase() === tenant.email.toLowerCase()) {
          return true;
        }
        
        // Then try by name
        const csvFullName = normalizeName(`${firstName} ${lastName}`);
        const dbFullName = normalizeName(`${tenant.first_name} ${tenant.last_name}`);
        
        // If name matches and at least one additional field matches (phone)
        if (csvFullName === dbFullName) {
          if (!phone || !tenant.phone_number) return true; // If no phone is provided, accept name match
          
          // Compare phone numbers but ignore non-digits
          const csvPhone = phone.replace(/\D/g, '');
          const dbPhone = tenant.phone_number.replace(/\D/g, '');
          
          // Accept partial phone match (phone numbers can be messy)
          if (csvPhone.includes(dbPhone) || dbPhone.includes(csvPhone)) {
            return true;
          }
        }
        
        return false;
      });
      
      // Find matching apartment in database
      const matchingApartment = apartments.find(apartment => {
        // Match by all three criteria: street, number, and apartment number
        const streetMatch = street.toLowerCase() === apartment.street.toLowerCase();
        const numberMatch = number.toLowerCase() === apartment.number.toLowerCase();
        const apartmentNumberMatch = normalizeApartmentNumber(apartment.apartment_number) === apartmentNumber;
        
        return streetMatch && numberMatch && apartmentNumberMatch;
      });
      
      if (matchingTenant && matchingApartment) {
        // Convert Buffer IDs to hex strings for map lookup
        const tenantIdHex = matchingTenant.id.toString('hex');
        const apartmentIdHex = matchingApartment.id.toString('hex');
        const linkKey = `${tenantIdHex}-${apartmentIdHex}`;
        
        // Check if link already exists
        if (!existingLinksMap.has(linkKey)) {
          links.push({
            tenant: matchingTenant,
            apartment: matchingApartment,
            tenantId: matchingTenant.id,
            apartmentId: matchingApartment.id
          });
          
          // Add to map to avoid duplicates
          existingLinksMap.set(linkKey, true);
          
          console.log(`Linked tenant ${matchingTenant.first_name} ${matchingTenant.last_name} with apartment at ${matchingApartment.street} ${matchingApartment.number}, apt ${matchingApartment.apartment_number}`);
          createdLinks++;
        } else {
          console.log(`Link between ${matchingTenant.first_name} ${matchingTenant.last_name} and apartment at ${matchingApartment.street} ${matchingApartment.number}, apt ${matchingApartment.apartment_number} already exists`);
        }
      } else {
        if (!matchingTenant) {
          console.log(`Warning: No matching tenant found for ${firstName} ${lastName} (${email})`);
        }
        if (!matchingApartment) {
          console.log(`Warning: No matching apartment found for ${street} ${number}, apt ${apartmentNumber}`);
        }
      }
    }
    
    // Insert links into database
    if (links.length > 0) {
      console.log(`\nInserting ${links.length} links into database...`);
      
      // Insert in batches to avoid issues with large numbers of inserts
      const BATCH_SIZE = 20;
      for (let i = 0; i < links.length; i += BATCH_SIZE) {
        const batch = links.slice(i, i + BATCH_SIZE);
        const values = batch.map(link => [link.tenantId, link.apartmentId]);
        
        await connection.query(
          'INSERT INTO tenant_apartments (tenant_id, apartment_id) VALUES ?',
          [values]
        );
        
        console.log(`Inserted batch ${i/BATCH_SIZE + 1} of ${Math.ceil(links.length/BATCH_SIZE)}`);
      }
      
      console.log(`Successfully inserted ${links.length} tenant-apartment links`);
    } else {
      console.log('No new links to insert');
    }
    
    return {
      tenantsInDatabase: tenants.length,
      apartmentsInDatabase: apartments.length,
      existingLinks: existingLinks.length,
      newLinksCreated: createdLinks
    };
    
  } catch (error) {
    console.error('Error linking tenants with apartments:', error);
    throw error;
  } finally {
    if (connection) {
      console.log('Closing database connection...');
      await connection.end();
    }
  }
}

// Run the script
console.log('Starting tenant-apartment linking process...');
linkTenantsWithApartments()
  .then(result => {
    console.log('\nLinking process completed successfully!');
    console.log('Results:');
    console.log(result);
  })
  .catch(error => {
    console.error('\nLinking process failed:', error);
    process.exit(1);
  }); 