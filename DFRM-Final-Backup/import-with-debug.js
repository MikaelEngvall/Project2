/**
 * Debug script to investigate API authorization issues
 */
const axios = require('axios');

// Login credentials
const credentials = {
  email: "mikael.engvall.me@gmail.com",
  password: "Tr4d1l0s!"
};

// API base URL
const API_BASE_URL = "http://localhost:8080";

// Login function to get auth token
async function login() {
  try {
    console.log(`Attempting to log in with email: ${credentials.email}`);
    const response = await axios.post(`${API_BASE_URL}/api/auth/login`, credentials);
    
    if (response.data && response.data.token) {
      console.log('Login successful! Auth token obtained.');
      console.log(`Token: ${response.data.token}`);
      
      // If there's user info, log it
      if (response.data.user) {
        console.log('User info:', response.data.user);
      }
      
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

// Function to test user permissions
async function testPermissions(token) {
  try {
    console.log('Testing API permissions with the provided token');
    
    // Test 1: Get current user info
    console.log('Test 1: Getting current user info');
    try {
      const userResponse = await axios.get(`${API_BASE_URL}/api/users/me`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      console.log('User info response:', userResponse.data);
    } catch (error) {
      console.error('Error getting user info:', error.message);
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
    }
    
    // Test 2: List all available endpoints
    console.log('Test 2: Checking API endpoints');
    try {
      const endpointsResponse = await axios.get(`${API_BASE_URL}/api`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      console.log('Available endpoints:', endpointsResponse.data);
    } catch (error) {
      console.error('Error getting API endpoints:', error.message);
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
    }
    
    // Test 3: Create a test apartment
    console.log('Test 3: Creating a test apartment');
    let testApartmentId = null;
    try {
      const testApartment = {
        street: 'Test Street',
        number: '123',
        apartmentNumber: 'TEST',
        postalCode: '371 00',
        city: 'Karlskrona',
        rooms: 2,
        area: 50,
        price: 5000,
        electricity: true,
        storage: true,
        internet: true,
        isTemporary: false
      };
      
      const apartmentResponse = await axios.post(`${API_BASE_URL}/api/apartments`, testApartment, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });
      
      testApartmentId = apartmentResponse.data.id;
      console.log('Test apartment created:', apartmentResponse.data);
    } catch (error) {
      console.error('Error creating test apartment:', error.message);
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
    }
    
    // Test 4: Create a test tenant
    console.log('Test 4: Creating a test tenant');
    let testTenantId = null;
    try {
      const testTenant = {
        firstName: 'Test',
        lastName: 'Tenant',
        email: 'test.tenant@example.com',
        phone: '123456789',
        movedInDate: new Date().toISOString().split('T')[0],
        isTemporary: false
      };
      
      const tenantResponse = await axios.post(`${API_BASE_URL}/api/tenants`, testTenant, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });
      
      testTenantId = tenantResponse.data.id;
      console.log('Test tenant created:', tenantResponse.data);
    } catch (error) {
      console.error('Error creating test tenant:', error.message);
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
    }
    
    // Test 5: Try to link the test tenant and apartment
    if (testTenantId && testApartmentId) {
      console.log('Test 5: Linking test tenant to test apartment');
      try {
        // Try with POST method
        const linkResponse = await axios.post(`${API_BASE_URL}/api/tenants/${testTenantId}/apartments/${testApartmentId}`, {}, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        console.log('Link tenant to apartment response:', linkResponse.data);
      } catch (error) {
        console.error('Error linking with POST:', error.message);
        if (error.response) {
          console.error('Response status:', error.response.status);
          console.error('Response data:', error.response.data);
        }
        
        // If POST fails, try PUT
        console.log('Trying with PUT method instead...');
        try {
          const linkResponse = await axios.put(`${API_BASE_URL}/api/tenants/${testTenantId}/apartments/${testApartmentId}`, {}, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          console.log('Link tenant to apartment response (PUT):', linkResponse.data);
        } catch (putError) {
          console.error('Error linking with PUT:', putError.message);
          if (putError.response) {
            console.error('Response status:', putError.response.status);
            console.error('Response data:', putError.response.data);
          }
        }
      }
    }
    
    // Test 6: Try to retrieve the database schema
    console.log('Test 6: Retrieving database schema (if available)');
    try {
      const schemaResponse = await axios.get(`${API_BASE_URL}/api/admin/schema`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      console.log('Database schema:', schemaResponse.data);
    } catch (error) {
      console.error('Error retrieving database schema:', error.message);
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
    }
    
    // Test 7: Check tenant's endpoints
    console.log('Test 7: Checking tenant endpoints');
    try {
      const tenantEndpointsResponse = await axios.get(`${API_BASE_URL}/api/tenants`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      console.log('Tenant endpoints response:', tenantEndpointsResponse.data);
    } catch (error) {
      console.error('Error getting tenant endpoints:', error.message);
      if (error.response) {
        console.error('Response status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
    }
    
    console.log('Permission tests completed');
  } catch (error) {
    console.error('Error testing permissions:', error);
  }
}

// Main function
async function runTests() {
  try {
    // First, authenticate to get token
    const token = await login();
    
    // Test permissions with the token
    await testPermissions(token);
    
    console.log('Tests completed');
  } catch (error) {
    console.error('Error running tests:', error);
  }
}

// Run the function if this script is executed directly
if (require.main === module) {
  console.log('Starting debug tests...');
  runTests();
} 