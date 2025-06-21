import pandas as pd
import re

# Read the CSV file with latin-1 encoding
df = pd.read_csv('Hyresgästsammanställning (2).csv', sep=';', encoding='latin-1')

# Replace 'Valhallav.' with 'Valhallavägen'
df['Adress'] = df['Adress'].str.replace('Valhallav.', 'Valhallavägen')

# Replace 'S.' with 'Södra'
df['Adress'] = df['Adress'].str.replace('S.', 'Södra')

# Replace 'V.' with 'Västra'
df['Adress'] = df['Adress'].str.replace('V.', 'Västra')

# Replace address formats
df['Adress'] = df['Adress'].str.replace('3 A', '3A')
df['Adress'] = df['Adress'].str.replace('3 B', '3B')
df['Adress'] = df['Adress'].str.replace('31 A', '31A')
df['Adress'] = df['Adress'].str.replace('31 B', '31B')

# Split Hyresgäst into Förnamn and Efternamn
df[['Förnamn', 'Efternamn']] = df['Hyresgäst'].str.split(' ', n=1, expand=True)

# Drop the original Hyresgäst column
df = df.drop('Hyresgäst', axis=1)

# Rename Objektnummer to Lägenhetsnummer
df = df.rename(columns={'Objektnummer': 'Lägenhetsnummer'})

# Split Adress into Gata and Nummer
df[['Gata', 'Nummer']] = df['Adress'].str.extract(r'(.*?)\s+(\d+.*)$')

# Drop the original Adress column
df = df.drop('Adress', axis=1)

# Drop the Hyresobjekt column
df = df.drop('Hyresobjekt', axis=1)

# Drop additional columns
df = df.drop(['Kontrakt t.om', 'Vakant', 'Årsbelopp'], axis=1)

# Format phone numbers
def format_phone(phone):
    if pd.isna(phone):
        return phone
    # Remove all non-digit characters
    digits = re.sub(r'\D', '', str(phone))
    # If we have 10 digits, format as nnnn-nnnnnn
    if len(digits) == 10:
        return f"{digits[:4]}-{digits[4:]}"
    # If we have 9 digits, format as nnn-nnnnnn
    elif len(digits) == 9:
        return f"{digits[:3]}-{digits[3:]}"
    # If we have 8 digits, format as nnn-nnnnn
    elif len(digits) == 8:
        return f"{digits[:3]}-{digits[3:]}"
    # Return original if no match
    return phone

df['Telefonnummer'] = df['Telefonnummer'].apply(format_phone)

# Clean up Lägenhetsnummer
df['Lägenhetsnummer'] = df['Lägenhetsnummer'].str.replace('Lgh ', '')  # Remove 'Lgh '
df['Lägenhetsnummer'] = df['Lägenhetsnummer'].str.replace('Lokal ', '')  # Remove 'Lokal '
df['Lägenhetsnummer'] = df['Lägenhetsnummer'].str.split('/').str[0]  # Keep only the part before '/'

# Move letter from Lägenhetsnummer to Nummer if it exists
def move_letter(row):
    if pd.isna(row['Lägenhetsnummer']):
        return row
    # Check if the last character is a letter
    if row['Lägenhetsnummer'][-1] in ['A', 'B', 'C']:
        letter = row['Lägenhetsnummer'][-1]
        # Remove the letter from Lägenhetsnummer
        row['Lägenhetsnummer'] = row['Lägenhetsnummer'][:-1]
        # Add the letter to Nummer
        if pd.isna(row['Nummer']):
            row['Nummer'] = letter
        else:
            row['Nummer'] = row['Nummer'] + letter
    return row

df = df.apply(move_letter, axis=1)

# Reorder columns to put Förnamn and Efternamn first, followed by Gata and Nummer
cols = ['Förnamn', 'Efternamn', 'Gata', 'Nummer'] + [col for col in df.columns if col not in ['Förnamn', 'Efternamn', 'Gata', 'Nummer']]
df = df[cols]

# Save the file with UTF-8 encoding
df.to_csv('Hyresgästsammanställning_fixed.csv', sep=';', encoding='utf-8', index=False) 