import sqlite3
from tkinter import messagebox

def connect_db():
    return sqlite3.connect('keys.db')

def fetch_all(table, frame, table_name, update_func, delete_func, conn):
    tree = create_treeview(frame, *get_columns(table_name))
    tree.pack(fill='both', expand=True)

    c = conn.cursor()
    c.execute(f"SELECT * FROM {table}")
    rows = c.fetchall()

    for row in rows:
        tree.insert('', 'end', values=row)
    
    def edit_function(tree, item_id, values, conn):
        edit_row(tree, item_id, values, update_func, delete_func, conn)

    def delete_function(tree, item_id, item_id_value, conn):
        delete_record(tree, item_id, item_id_value, conn)

    return tree

def update_record(table, new_values, item_id, conn):
    c = conn.cursor()
    columns = get_columns(table)[1:]  # Exclude the ID column for updates
    set_clause = ', '.join([f"{col}=?" for col in columns])
    c.execute(f"UPDATE {table} SET {set_clause} WHERE ID=?", (*new_values, item_id))
    conn.commit()

def delete_record(tree, item_id, item_id_value, conn):
    c = conn.cursor()
    c.execute(f"DELETE FROM {table} WHERE id=?", (item_id_value,))
    conn.commit()
    refresh_tree(tree, table, conn)

def refresh_tree(tree, table, conn):
    for row in tree.get_children():
        tree.delete(row)
    
    c = conn.cursor()
    c.execute(f"SELECT * FROM {table}")
    rows = c.fetchall()

    for row in rows:
        tree.insert('', 'end', values=row)

def get_columns(table_name):
    columns = {
        'apartments': ('ID', 'Address', 'Number', 'Area'),
        'keys': ('ID', 'Number', 'Type', 'Apartment ID'),
        'residents': ('ID', 'First Name', 'Last Name', 'Phone Number', 'Email', 'Apartment ID')
    }
    return columns.get(table_name, [])
