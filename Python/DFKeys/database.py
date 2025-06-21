import sqlite3

DATABASE = 'apartment_keys.db'

def execute_query(query, params=()):
    with sqlite3.connect(DATABASE) as conn:
        c = conn.cursor()
        c.execute(query, params)
        conn.commit()

def fetch_query(query, params=()):
    with sqlite3.connect(DATABASE) as conn:
        c = conn.cursor()
        c.execute(query, params)
        result = c.fetchall()
    return result
