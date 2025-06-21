import sqlite3
import os

DATABASE = 'keys.db'

def create_connection():
    return sqlite3.connect(DATABASE)

def get_db_path():
    return os.path.join(os.path.dirname(__file__), DATABASE)

def create_tables():
    conn = create_connection()
    with conn:
        conn.execute('''CREATE TABLE IF NOT EXISTS apartments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        address TEXT NOT NULL,
                        number TEXT NOT NULL,
                        area TEXT NOT NULL  -- Corrected area to be a text field representing location
                    )''')
                
        conn.execute('''CREATE TABLE IF NOT EXISTS keys (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        number TEXT NOT NULL,
                        type TEXT NOT NULL,
                        apartment_id INTEGER NOT NULL,
                        FOREIGN KEY (apartment_id) REFERENCES apartments (id)
                    )''')
        
        conn.execute('''CREATE TABLE IF NOT EXISTS residents (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        first_name TEXT NOT NULL,
                        last_name TEXT NOT NULL,
                        phone_number TEXT NOT NULL,
                        email TEXT NOT NULL,  -- Added email field
                        apartment_id INTEGER NOT NULL,
                        FOREIGN KEY (apartment_id) REFERENCES apartments (id)
                    )''')
        
    conn.close()
    
if __name__ == "__main__":
    create_tables()
    print("Database and tables created successfully.")
