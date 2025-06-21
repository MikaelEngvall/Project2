import sqlite3

DATABASE = 'apartment_keys.db'

def create_connection():
    """Create and return a database connection."""
    return sqlite3.connect(DATABASE)

def create_tables():
    conn = create_connection()
    with conn:
        conn.execute('''
        CREATE TABLE IF NOT EXISTS Apartments (
            ApartmentID INTEGER PRIMARY KEY AUTOINCREMENT,
            ApartmentNumber TEXT NOT NULL,
            Building TEXT,
            Floor INTEGER
        )
        ''')

        conn.execute('''
        CREATE TABLE IF NOT EXISTS Keys (
            KeyID INTEGER PRIMARY KEY AUTOINCREMENT,
            KeyCode TEXT UNIQUE NOT NULL,
            KeyType TEXT
        )
        ''')

        conn.execute('''
        CREATE TABLE IF NOT EXISTS Residents (
            ResidentID INTEGER PRIMARY KEY AUTOINCREMENT,
            FirstName TEXT,
            LastName TEXT,
            ApartmentID INTEGER,
            FOREIGN KEY (ApartmentID) REFERENCES Apartments (ApartmentID)
        )
        ''')

        conn.execute('''
        CREATE TABLE IF NOT EXISTS ResidentKeys (
            ResidentKeyID INTEGER PRIMARY KEY AUTOINCREMENT,
            KeyID INTEGER,
            ResidentID INTEGER,
            FOREIGN KEY (KeyID) REFERENCES Keys (KeyID),
            FOREIGN KEY (ResidentID) REFERENCES Residents (ResidentID)
        )
        ''')

    conn.close()

if __name__ == "__main__":
    create_tables()
    print("Database and tables created successfully.")
