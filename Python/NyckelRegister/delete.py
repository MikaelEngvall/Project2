import tkinter as tk
import sqlite3
from tkinter import messagebox
from database import create_connection

def delete_menu():
    delete_window = tk.Toplevel()
    delete_window.title("Delete Key/Apartment/Resident")

    tk.Button(delete_window, text="Delete Apartment", command=delete_apartment).pack()
    tk.Button(delete_window, text="Delete Key", command=delete_key).pack()
    tk.Button(delete_window, text="Delete Resident", command=delete_resident).pack()

def delete_apartment():
    def delete():
        apartment_id = id_entry.get()
        if apartment_id:
            conn = create_connection()
            c = conn.cursor()
            c.execute("DELETE FROM apartments WHERE id = ?", (apartment_id,))
            conn.commit()
            conn.close()
            messagebox.showinfo("Success", "Apartment deleted successfully")
        else:
            messagebox.showerror("Error", "Apartment ID is required")

    window = tk.Toplevel()
    window.title("Delete Apartment")

    tk.Label(window, text="Apartment ID").pack()
    id_entry = tk.Entry(window)
    id_entry.pack()

    tk.Button(window, text="Delete", command=delete).pack()

def delete_key():
    def delete():
        key_id = id_entry.get()
        if key_id:
            conn = create_connection()
            c = conn.cursor()
            c.execute("DELETE FROM keys WHERE id = ?", (key_id,))
            conn.commit()
            conn.close()
            messagebox.showinfo("Success", "Key deleted successfully")
        else:
            messagebox.showerror("Error", "Key ID is required")

    window = tk.Toplevel()
    window.title("Delete Key")

    tk.Label(window, text="Key ID").pack()
    id_entry = tk.Entry(window)
    id_entry.pack()

    tk.Button(window, text="Delete", command=delete).pack()

def delete_resident():
    def delete():
        resident_id = id_entry.get()
        if resident_id:
            conn = create_connection()
            c = conn.cursor()
            c.execute("DELETE FROM residents WHERE id = ?", (resident_id,))
            conn.commit()
            conn.close()
            messagebox.showinfo("Success", "Resident deleted successfully")
        else:
            messagebox.showerror("Error", "Resident ID is required")

    window = tk.Toplevel()
    window.title("Delete Resident")

    tk.Label(window, text="Resident ID").pack()
    id_entry = tk.Entry(window)
    id_entry.pack()

    tk.Button(window, text="Delete", command=delete).pack()
