/**
 * Script to clean the CSV file by keeping only the latest tenant for each apartment
 */
const fs = require('fs');
const path = require('path');

// Läser in CSV-filen
const csvFile = path.join(__dirname, 'Hyresgästsammanställning_fixed.csv');
const csvData = fs.readFileSync(csvFile, 'utf8');

// Parsera CSV
const lines = csvData.split('\n');
const headers = lines[0];
const dataRows = lines.slice(1).filter(line => line.trim() !== '');

// Hjälpfunktion för att jämföra datum
const isNewerDate = (date1, date2) => {
  if (!date1) return false;
  if (!date2) return true;
  return new Date(date1) > new Date(date2);
};

// Gruppera hyresgäster efter lägenhet
const tenantsByApartment = {};

for (const row of dataRows) {
  const values = row.split(';');
  if (values.length < 7) continue;

  const firstName = values[0] || '';
  const lastName = values[1] || '';
  const street = values[2] || '';
  const number = values[3] || '';
  const email = values[4] || '';
  const phone = values[5] || '';
  const apartmentNumber = values[6] || '';
  const area = values[7] || '';
  const contractDate = values[8] || '';
  const monthlyAmount = values[9] || '';

  // Skapa nyckel för lägenheten
  const apartmentKey = `${street}-${number}-${apartmentNumber}`;
  
  // Skapa hyresgästobjekt med hela raden
  const tenant = {
    row,
    firstName,
    lastName,
    contractDate
  };

  // Om lägenheten redan finns i mappen
  if (apartmentKey in tenantsByApartment) {
    const existingTenants = tenantsByApartment[apartmentKey];
    
    // Om existerande och nuvarande hyresgäst har samma datum, lägg till den nuvarande
    if (existingTenants[0].contractDate === tenant.contractDate) {
      existingTenants.push(tenant);
      continue;
    }
    
    // Om nuvarande hyresgäst har ett senare datum
    if (isNewerDate(tenant.contractDate, existingTenants[0].contractDate)) {
      // Rensa arrayen och lägg till den nya hyresgästen
      tenantsByApartment[apartmentKey] = [tenant];
    }
    // Annars behåller vi de existerande hyresgästerna med senare datum
  } else {
    // Första hyresgästen för denna lägenhet
    tenantsByApartment[apartmentKey] = [tenant];
  }
}

// Extrahera unika rader
const uniqueRows = [];
for (const key in tenantsByApartment) {
  tenantsByApartment[key].forEach(tenant => {
    uniqueRows.push(tenant.row);
  });
}

// Skapa ny CSV med unika rader
const newCsvContent = [headers, ...uniqueRows].join('\n');

// Spara till ny fil
const outputFile = path.join(__dirname, 'Hyresgästsammanställning_cleaned.csv');
fs.writeFileSync(outputFile, newCsvContent, 'utf8');

console.log(`Ursprunglig CSV: ${dataRows.length} rader`);
console.log(`Rensad CSV: ${uniqueRows.length} rader`);
console.log(`Borttagna dubblettrader: ${dataRows.length - uniqueRows.length}`);
console.log(`Den nya filen har sparats som: ${outputFile}`); 