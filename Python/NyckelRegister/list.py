import tkinter as tk
from tkinter import ttk, messagebox
import sqlite3

def list_menu():
    conn = sqlite3.connect('keys.db')
    c = conn.cursor()

    result_window = tk.Toplevel()
    result_window.title("List All Data")
    result_window.geometry("1000x600")

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

    # Apartments Tab
    apartments_tree = ttk.Treeview(apartments_frame, columns=('ID', 'Address', 'Number', 'Area'), show='headings')
    apartments_tree.heading('ID', text='ID')
    apartments_tree.heading('Address', text='Address')
    apartments_tree.heading('Number', text='Number')
    apartments_tree.heading('Area', text='Area')

    apartments_tree.pack(fill='both', expand=True)

    c.execute("SELECT * FROM apartments")
    for apt in c.fetchall():
        apartments_tree.insert('', 'end', values=apt)

    # Keys Tab
    keys_tree = ttk.Treeview(keys_frame, columns=('ID', 'Number', 'Type', 'Apartment ID'), show='headings')
    keys_tree.heading('ID', text='ID')
    keys_tree.heading('Number', text='Number')
    keys_tree.heading('Type', text='Type')
    keys_tree.heading('Apartment ID', text='Apartment ID')

    keys_tree.pack(fill='both', expand=True)

    c.execute("SELECT * FROM keys")
    for key in c.fetchall():
        keys_tree.insert('', 'end', values=key)

    # Residents Tab
    residents_tree = ttk.Treeview(residents_frame, columns=('ID', 'First Name', 'Last Name', 'Phone Number', 'Email', 'Apartment ID'), show='headings')
    residents_tree.heading('ID', text='ID')
    residents_tree.heading('First Name', text='First Name')
    residents_tree.heading('Last Name', text='Last Name')
    residents_tree.heading('Phone Number', text='Phone Number')
    residents_tree.heading('Email', text='Email')
    residents_tree.heading('Apartment ID', text='Apartment ID')

    residents_tree.pack(fill='both', expand=True)

    c.execute("SELECT * FROM residents")
    for res in c.fetchall():
        residents_tree.insert('', 'end', values=res)

    def on_item_click(event, tree, edit_function):
        selected_item = tree.selection()[0]
        item_values = tree.item(selected_item, 'values')
        edit_function(tree, selected_item, item_values, conn)

    def edit_apartment(tree, item_id, values, conn):
        edit_row(tree, item_id, values, conn, save_apartment, delete_apartment)

    def edit_key(tree, item_id, values, conn):
        edit_row(tree, item_id, values, conn, save_key, delete_key)

    def edit_resident(tree, item_id, values, conn):
        edit_row(tree, item_id, values, conn, save_resident, delete_resident)

    def edit_row(tree, item_id, values, conn, save_function, delete_function):
        # Clear existing row
        tree.item(item_id, values=('',)*len(values))

        entry_widgets = []

        # Calculate x_positions dynamically based on the number of columns
        num_columns = len(tree.cget('columns'))
        col_widths = [tree.column(col, 'width') for col in tree.cget('columns')]
        x_positions = [sum(col_widths[:i]) for i in range(num_columns)]

        # Get the y position based on the item's bounding box
        y_position = tree.bbox(item_id)[1] + tree.winfo_y()

        # Create Entry widgets for editing
        for i, val in enumerate(values):
            entry = tk.Entry(tree, width=int(col_widths[i] // 8))  # Adjust width based on column width
            entry.insert(0, val)
            entry.place(x=x_positions[i], y=y_position)
            entry_widgets.append(entry)

        # Adjust positions of the buttons to appear at the end of the line
        button_x_position = x_positions[-1] + col_widths[-1] + 10  # Offset the button position

        save_button = ttk.Button(tree, text="💾", command=lambda: save_function(tree, item_id, entry_widgets, conn))
        save_button.place(x=button_x_position, y=y_position)

        delete_button = ttk.Button(tree, text="🗑️", command=lambda: confirm_delete(lambda: delete_function(tree, item_id, values[0], conn)))
        delete_button.place(x=button_x_position + 30, y=y_position)  # Adjust spacing for the second button

    def save_apartment(tree, item_id, entry_widgets, conn):
        c = conn.cursor()
        new_values = [entry.get() for entry in entry_widgets]
        c.execute("UPDATE apartments SET address=?, number=?, area=? WHERE id=?", (*new_values[1:], new_values[0]))
        conn.commit()
        refresh_tree(tree, 'apartments', conn)

    def delete_apartment(tree, item_id, apartment_id, conn):
        c = conn.cursor()
        c.execute("DELETE FROM apartments WHERE id=?", (apartment_id,))
        conn.commit()
        refresh_tree(tree, 'apartments', conn)

    def save_key(tree, item_id, entry_widgets, conn):
        c = conn.cursor()
        new_values = [entry.get() for entry in entry_widgets]
        c.execute("UPDATE keys SET number=?, type=?, apartment_id=? WHERE id=?", (*new_values[1:], new_values[0]))
        conn.commit()
        refresh_tree(tree, 'keys', conn)

    def delete_key(tree, item_id, key_id, conn):
        c = conn.cursor()
        c.execute("DELETE FROM keys WHERE id=?", (key_id,))
        conn.commit()
        refresh_tree(tree, 'keys', conn)

    def save_resident(tree, item_id, entry_widgets, conn):
        c = conn.cursor()
        new_values = [entry.get() for entry in entry_widgets]
        c.execute("UPDATE residents SET first_name=?, last_name=?, phone_number=?, email=?, apartment_id=? WHERE id=?", (*new_values[1:], new_values[0]))
        conn.commit()
        refresh_tree(tree, 'residents', conn)

    def delete_resident(tree, item_id, resident_id, conn):
        c = conn.cursor()
        c.execute("DELETE FROM residents WHERE id=?", (resident_id,))
        conn.commit()
        refresh_tree(tree, 'residents', conn)

    def confirm_delete(delete_command):
        if messagebox.askyesno("Confirm Delete", "Are you sure you want to delete this entry?"):
            delete_command()

    def refresh_tree(tree, table, conn):
        c = conn.cursor()
        for row in tree.get_children():
            tree.delete(row)

        if table == 'apartments':
            c.execute("SELECT * FROM apartments")
        elif table == 'keys':
            c.execute("SELECT * FROM keys")
        elif table == 'residents':
            c.execute("SELECT * FROM residents")

        for item in c.fetchall():
            tree.insert('', 'end', values=item)

    # Bind item click events to open the editing mode
    apartments_tree.bind("<Double-1>", lambda e: on_item_click(e, apartments_tree, edit_apartment))
    keys_tree.bind("<Double-1>", lambda e: on_item_click(e, keys_tree, edit_key))
    residents_tree.bind("<Double-1>", lambda e: on_item_click(e, residents_tree, edit_resident))

    result_window.protocol("WM_DELETE_WINDOW", lambda: close_connection(conn))

def close_connection(conn):
    conn.close()
    root.destroy()

if __name__ == "__main__":
    root = tk.Tk()
    root.withdraw()  # Hide the root window
    list_menu()
    root.mainloop()
