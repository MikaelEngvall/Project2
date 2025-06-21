/**
 * Script to read and display the cleaned CSV file
 */
const fs = require('fs');
const path = require('path');

// Find files with names containing "cleaned" in the current directory
const files = fs.readdirSync(__dirname);
const cleanedFile = files.find(file => file.includes('cleaned') && file.endsWith('.csv'));

if (!cleanedFile) {
  console.error('Cleaned CSV file not found!');
  process.exit(1);
}

console.log(`Found cleaned file: ${cleanedFile}`);

// Read the cleaned CSV file
const csvFile = path.join(__dirname, cleanedFile);
const csvData = fs.readFileSync(csvFile, 'utf8');

// Parse CSV
const lines = csvData.split('\n');
const headers = lines[0];
const dataRows = lines.slice(1).filter(line => line.trim() !== '');

console.log(`Total rows in cleaned CSV: ${dataRows.length}`);

// Find rows for specific apartments
// Example: Find all rows for Valhallavägen 10A, apt 1202
const valhallaRows = dataRows.filter(row => {
  const values = row.split(';');
  return values[2] === 'Valhallavägen' && values[3] === '10A' && values[6] === '1202';
});

console.log('\nHyresgäster för Valhallavägen 10A, lägenhet 1202:');
valhallaRows.forEach(row => {
  const values = row.split(';');
  console.log(`${values[0]} ${values[1]}, inflyttning: ${values[8]}`);
});

// Example: Find all rows for Utridarevägen 3B, apt 1201
const utridareRows = dataRows.filter(row => {
  const values = row.split(';');
  return values[2] === 'Utridarevägen' && values[3] === '3B' && values[6] === '1201';
});

console.log('\nHyresgäster för Utridarevägen 3B, lägenhet 1201:');
utridareRows.forEach(row => {
  const values = row.split(';');
  console.log(`${values[0]} ${values[1]}, inflyttning: ${values[8]}`);
});

// Example: Find all rows for Styrmansgatan 38, apt 1101
const styrmanRows = dataRows.filter(row => {
  const values = row.split(';');
  return values[2] === 'Styrmansgatan' && values[3] === '38' && values[6] === '1101';
});

console.log('\nHyresgäster för Styrmansgatan 38, lägenhet 1101:');
styrmanRows.forEach(row => {
  const values = row.split(';');
  console.log(`${values[0]} ${values[1]}, inflyttning: ${values[8]}`);
});

// Find all duplicate apartment addresses that still exist
const apartmentKeys = {};
const duplicateKeys = new Set();

dataRows.forEach(row => {
  const values = row.split(';');
  const street = values[2];
  const number = values[3];
  const apartmentNumber = values[6];
  
  const key = `${street}-${number}-${apartmentNumber}`;
  
  if (apartmentKeys[key]) {
    duplicateKeys.add(key);
  } else {
    apartmentKeys[key] = true;
  }
});

console.log('\nLägenheter med flera hyresgäster (samma inflyttningsdatum):');
if (duplicateKeys.size === 0) {
  console.log('Inga lägenheter med flera hyresgäster hittades.');
} else {
  [...duplicateKeys].forEach(key => {
    const [street, number, apartmentNumber] = key.split('-');
    console.log(`\n${street} ${number}, lägenhet ${apartmentNumber}:`);
    
    const matches = dataRows.filter(row => {
      const values = row.split(';');
      return values[2] === street && values[3] === number && values[6] === apartmentNumber;
    });
    
    matches.forEach(row => {
      const values = row.split(';');
      console.log(`${values[0]} ${values[1]}, inflyttning: ${values[8]}`);
    });
  });
} 