import tkinter as tk
from tkinter import ttk, messagebox
from database import execute_query, fetch_query

def delete_key():
    def confirm_delete():
        selected_item = table.focus()
        key_id = table.item(selected_item)['values'][0]
        
        query = "DELETE FROM Keys WHERE KeyID = ?"
        execute_query(query, (key_id,))
        
        window.destroy()
        messagebox.showinfo("Success", "Key deleted successfully!")

    window = tk.Toplevel()
    window.title("Delete Key")

    keys = fetch_query("SELECT * FROM Keys")
    
    table = ttk.Treeview(window, columns=("KeyID", "KeyCode", "KeyType"), show='headings')
    table.heading("KeyID", text="KeyID")
    table.heading("KeyCode", text="KeyCode")
    table.heading("KeyType", text="KeyType")
    
    for key in keys:
        table.insert('', 'end', values=key)
    
    table.pack(expand=True, fill='both')
    
    tk.Button(window, text="Delete", command=confirm_delete).pack(pady=10)
    tk.Button(window, text="Cancel", command=window.destroy).pack(pady=10)
