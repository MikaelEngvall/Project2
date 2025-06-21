import sqlite3

def check_tables():
    conn = sqlite3.connect('keys.db')
    c = conn.cursor()
    c.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = c.fetchall()
    conn.close()
    print("Tables in database:", tables)

if __name__ == "__main__":
    check_tables()
