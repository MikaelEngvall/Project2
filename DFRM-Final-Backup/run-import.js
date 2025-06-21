/**
 * Script to import tenants and apartments from CSV file
 */
const fs = require('fs');
const path = require('path');
const axios = require('axios');

// API base URL
const API_BASE_URL = "http://localhost:8080";

// Login credentials
const credentials = {
  email: "mikael.engvall.me@gmail.com",
  password: "Tr4d1l0s!"
};

// Login function to get auth token
async function login() {
  try {
    console.log(`Attempting to log in with email: ${credentials.email}`);
    const response = await axios.post(`${API_BASE_URL}/api/auth/login`, credentials);
    
    if (response.data && response.data.token) {
      console.log('Login successful! Auth token obtained.');
      return response.data.token;
    } else {
      throw new Error('Login successful but no token received');
    }
  } catch (error) {
    console.error('Login failed:', error.message);
    if (error.response) {
      console.error('Response status:', error.response.status);
      console.error('Response data:', error.response.data);
    }
    throw new Error('Authentication failed');
  }
}

// Helper function to parse amounts with Swedish formatting (e.g. "27 484,35 kr")
const parseAmount = (amountString) => {
  if (!amountString) return 0;
  return parseFloat(amountString.replace(/\s/g, '').replace(/,/g, '.').replace(/kr/g, '').trim());
};

// Helper function to parse area from the CSV
const parseArea = (areaString) => {
  if (!areaString) return 0;
  return parseFloat(areaString);
};

// Helper function to compare dates and get the most recent one
const isMoreRecent = (date1, date2) => {
  if (!date1) return false;
  if (!date2) return true;
  return new Date(date1) > new Date(date2);
};

// Main import function
async function importTenantsFromCSV(csvData, token) {
  try {
    console.log('Starting import process...');
    
    // Parse CSV data
    const lines = csvData.split('\n');
    const headers = lines[0].split(';');
    
    // Create objects for each row
    const tenantsByApartment = {};
    
    // Process each line in CSV
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue; // Skip empty lines
      
      const values = line.split(';');
      if (values.length < 7) continue; // Skip incomplete lines
      
      // Map CSV columns to object fields
      const firstName = values[0] || '';
      const lastName = values[1] || '';
      const street = values[2] || '';
      const number = values[3] || '';
      const email = values[4] || '';
      const phone = values[5] || '';
      const apartmentNumber = values[6] || '';
      const area = parseArea(values[7]) || 0;
      const contractDate = values[8] || '';
      const monthlyAmount = parseAmount(values[9]) || 0;
      
      // Create apartment object
      const apartment = {
        street,
        number,
        apartmentNumber,
        postalCode: '371 00', // Default postal code for Karlskrona
        city: 'Karlskrona',
        rooms: Math.ceil(area / 25) || 1, // Estimate rooms based on area
        area,
        price: monthlyAmount,
        electricity: true, 
        storage: true,
        internet: true,
        isTemporary: false
      };
      
      // Create tenant object
      const tenant = {
        firstName,
        lastName,
        email: email || `${firstName.toLowerCase()}.${lastName.toLowerCase()}@example.com`,
        phone,
        movedInDate: contractDate || new Date().toISOString().split('T')[0], // Use contract date or today
        isTemporary: false
      };
      
      // Create a unique key for the apartment
      const apartmentKey = `${street}-${number}-${apartmentNumber}`;
      
      // Check if we already have a tenant for this apartment
      if (apartmentKey in tenantsByApartment) {
        const existing = tenantsByApartment[apartmentKey];
        
        // Only replace if the new tenant has a more recent move-in date
        if (isMoreRecent(tenant.movedInDate, existing.tenant.movedInDate)) {
          console.log(`Replacing tenant ${existing.tenant.firstName} ${existing.tenant.lastName} with ${tenant.firstName} ${tenant.lastName} for apartment ${apartmentKey} due to more recent date (${existing.tenant.movedInDate} -> ${tenant.movedInDate})`);
          tenantsByApartment[apartmentKey] = { tenant, apartment };
        } else {
          console.log(`Keeping tenant ${existing.tenant.firstName} ${existing.tenant.lastName} for apartment ${apartmentKey} as move-in date is more recent (${existing.tenant.movedInDate} vs ${tenant.movedInDate})`);
        }
      } else {
        // This is the first tenant we've seen for this apartment
        tenantsByApartment[apartmentKey] = { tenant, apartment };
      }
    }
    
    // Convert the map back to arrays for processing
    const tenants = [];
    const apartments = [];
    
    for (const key in tenantsByApartment) {
      tenants.push(tenantsByApartment[key].tenant);
      apartments.push(tenantsByApartment[key].apartment);
    }
    
    console.log(`Parsed ${tenants.length} unique tenants and apartments from CSV after filtering duplicates`);
    
    // Start the import process
    const results = [];
    const errors = [];
    
    // Process each tenant-apartment pair
    for (let i = 0; i < tenants.length; i++) {
      try {
        const tenant = tenants[i];
        const apartment = apartments[i];
        
        console.log(`Processing ${i+1}/${tenants.length}: ${tenant.firstName} ${tenant.lastName} - ${apartment.street} ${apartment.number}, apt ${apartment.apartmentNumber}`);
        
        // Step 1: Create the apartment
        console.log('Creating apartment...');
        const apartmentResponse = await axios.post(`${API_BASE_URL}/api/apartments`, apartment, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });
        
        const createdApartment = apartmentResponse.data;
        console.log('Apartment created:', createdApartment);
        
        // Step 2: Create the tenant
        console.log('Creating tenant...');
        const tenantResponse = await axios.post(`${API_BASE_URL}/api/tenants`, tenant, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });
        
        const createdTenant = tenantResponse.data;
        console.log('Tenant created:', createdTenant);
        
        // Step 3: Associate tenant with apartment
        console.log('Linking tenant to apartment...');
        
        // Try using the tenant PUT/PATCH endpoints with query parameter
        try {
          console.log('Trying tenant PUT endpoint...');
          const updateTenantResponse = await axios.put(`${API_BASE_URL}/api/tenants/${createdTenant.id}/apartment?apartmentId=${createdApartment.id}`, {}, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          console.log('Tenant-apartment link successful (tenant PUT)');
        } catch (linkError1) {
          console.error('Error with tenant PUT endpoint:', linkError1.message);
          
          // Try the apartment PUT endpoint if tenant endpoint failed
          try {
            console.log('Trying apartment PUT endpoint...');
            const updateApartmentResponse = await axios.put(`${API_BASE_URL}/api/apartments/${createdApartment.id}/tenant?tenantId=${createdTenant.id}`, {}, {
              headers: {
                'Authorization': `Bearer ${token}`
              }
            });
            console.log('Tenant-apartment link successful (apartment PUT)');
          } catch (linkError2) {
            console.error('Error with apartment PUT endpoint:', linkError2.message);
            
            // Try PATCH methods if PUT methods failed
            try {
              console.log('Trying tenant PATCH endpoint...');
              const patchTenantResponse = await axios.patch(`${API_BASE_URL}/api/tenants/${createdTenant.id}/apartment?apartmentId=${createdApartment.id}`, {}, {
                headers: {
                  'Authorization': `Bearer ${token}`
                }
              });
              console.log('Tenant-apartment link successful (tenant PATCH)');
            } catch (linkError3) {
              console.error('Error with tenant PATCH endpoint:', linkError3.message);
              
              // Try the apartment PATCH endpoint as last resort
              try {
                console.log('Trying apartment PATCH endpoint...');
                const patchApartmentResponse = await axios.patch(`${API_BASE_URL}/api/apartments/${createdApartment.id}/tenant?tenantId=${createdTenant.id}`, {}, {
                  headers: {
                    'Authorization': `Bearer ${token}`
                  }
                });
                console.log('Tenant-apartment link successful (apartment PATCH)');
              } catch (linkError4) {
                console.error('Error with apartment PATCH endpoint:', linkError4.message);
                throw new Error('Failed to link tenant and apartment after trying all available methods');
              }
            }
          }
        }
        
        results.push({
          tenant: createdTenant,
          apartment: createdApartment
        });
        
        console.log(`Successfully processed ${tenant.firstName} ${tenant.lastName}`);
      } catch (error) {
        console.error(`Error processing entry ${i+1}:`, error.message);
        errors.push({
          index: i,
          tenant: tenants[i],
          apartment: apartments[i],
          error: error.message
        });
      }
    }
    
    console.log('Import completed.');
    console.log(`Successfully imported ${results.length} tenants and apartments.`);
    if (errors.length > 0) {
      console.log(`Failed to import ${errors.length} entries. See errors for details.`);
    }
    
    return {
      success: results.length,
      failed: errors.length,
      results,
      errors
    };
  } catch (error) {
    console.error('Import failed:', error);
    throw error;
  }
}

// Main function to run the import
async function runImport() {
  try {
    // First, authenticate to get a valid token
    const token = await login();
    
    // Read the CSV file
    const csvFile = path.join(__dirname, 'Hyresgästsammanställning_fixed.csv');
    console.log(`Reading CSV file from: ${csvFile}`);
    const csvData = fs.readFileSync(csvFile, 'utf8');
    
    // Run the import with the auth token
    const result = await importTenantsFromCSV(csvData, token);
    
    console.log('Import result:', result);
    return result;
  } catch (error) {
    console.error('Error running import:', error);
  }
}

// Run the import if this script is executed directly
if (require.main === module) {
  console.log('Starting import script...');
  runImport();
} 