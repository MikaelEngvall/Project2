import tkinter as tk
from tkinter import ttk
from list_database import fetch_all, update_record, delete_record
from ui_helpers import create_treeview, edit_row, on_item_click

def list_menu():
    conn = None  # We'll initialize the connection later
    def close_connection():
        if conn:
            conn.close()
        root.destroy()

    result_window = tk.Toplevel()
    result_window.title("List All Data")
    result_window.geometry("1000x600")
    result_window.protocol("WM_DELETE_WINDOW", close_connection)

    style = ttk.Style()
    style.configure("Treeview.Heading", font=('Helvetica', 12, 'bold'))
    style.configure("Treeview", font=('Helvetica', 10))

    notebook = ttk.Notebook(result_window)
    notebook.pack(fill='both', expand=True)

    apartments_frame = ttk.Frame(notebook)
    keys_frame = ttk.Frame(notebook)
    residents_frame = ttk.Frame(notebook)

    notebook.add(apartments_frame, text='Apartments')
    notebook.add(keys_frame, text='Keys')
    notebook.add(residents_frame, text='Residents')

    conn = fetch_all('apartments', apartments_frame, 'apartments', update_record, delete_record, conn)
    fetch_all('keys', keys_frame, 'keys', update_record, delete_record, conn)
    fetch_all('residents', residents_frame, 'residents', update_record, delete_record, conn)

    # Bind item click events to open the editing mode
    apartments_tree = create_treeview(apartments_frame, 'ID', 'Address', 'Number', 'Area')
    keys_tree = create_treeview(keys_frame, 'ID', 'Number', 'Type', 'Apartment ID')
    residents_tree = create_treeview(residents_frame, 'ID', 'First Name', 'Last Name', 'Phone Number', 'Email', 'Apartment ID')

    apartments_tree.bind("<Double-1>", lambda e: on_item_click(e, apartments_tree, 'apartments', update_record, delete_record, conn))
    keys_tree.bind("<Double-1>", lambda e: on_item_click(e, keys_tree, 'keys', update_record, delete_record, conn))
    residents_tree.bind("<Double-1>", lambda e: on_item_click(e, residents_tree, 'residents', update_record, delete_record, conn))

    root = tk.Tk()
    root.withdraw()  # Hide the root window
    list_menu()
    root.mainloop()
