/**
 * Script to authenticate and import tenants and apartments from CSV file
 */
const fs = require('fs');
const path = require('path');
const axios = require('axios');

// Login credentials
const credentials = {
  email: "mikael.engvall.me@gmail.com",
  password: "Tr4d1l0s!"
};

// API base URL
const API_BASE_URL = "http://localhost:8080";

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

// Main import function
async function importTenantsFromCSV(csvData, token) {
  try {
    console.log('Starting import process...');
    
    // Parse CSV data
    const lines = csvData.split('\n');
    const headers = lines[0].split(';');
    
    // Create objects for each row
    const tenants = [];
    const apartments = [];
    
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
      
      apartments.push(apartment);
      tenants.push(tenant);
    }
    
    console.log(`Parsed ${tenants.length} tenants and apartments from CSV`);
    
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
        const updateTenantResponse = await axios.post(`${API_BASE_URL}/api/tenants/${createdTenant.id}/apartments/${createdApartment.id}`, {}, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        results.push({
          tenant: createdTenant,
          apartment: createdApartment
        });
        
        console.log(`Successfully processed ${tenant.firstName} ${tenant.lastName}`);
        
        // Add a small delay to avoid overwhelming the server
        await new Promise(resolve => setTimeout(resolve, 100));
      } catch (error) {
        console.error(`Error processing entry ${i+1}:`, error.message);
        if (error.response) {
          console.error('Response status:', error.response.status);
          console.error('Response data:', error.response.data);
        }
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
    // First, authenticate to get token
    const token = await login();
    
    // Read the CSV file
    const csvFile = path.join(__dirname, 'Hyresgästsammanställning_fixed.csv');
    console.log(`Reading CSV file from: ${csvFile}`);
    const csvData = fs.readFileSync(csvFile, 'utf8');
    
    // Run the import with the authentication token
    const result = await importTenantsFromCSV(csvData, token);
    
    console.log('Import result:', result);
    return result;
  } catch (error) {
    console.error('Error running import:', error);
  }
}

// Run the import if this script is executed directly
if (require.main === module) {
  console.log('Starting import script with authentication...');
  runImport();
} 