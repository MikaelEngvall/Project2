import tkinter as tk
from tkinter import ttk
import sqlite3
from database import create_connection

def add_menu():
    add_window = tk.Toplevel()
    add_window.title("Add Key/Apartment/Resident")

    ttk.Button(add_window, text="Add Apartment", command=add_apartment).pack()
    ttk.Button(add_window, text="Add Key", command=add_key).pack()
    ttk.Button(add_window, text="Add Resident", command=add_resident).pack()

def add_apartment():
    window = tk.Toplevel()
    window.title("Add Apartment")

    ttk.Label(window, text="Address").pack()
    address_entry = ttk.Entry(window)
    address_entry.pack()

    ttk.Label(window, text="Apartment Number").pack()
    number_entry = ttk.Entry(window)
    number_entry.pack()

    ttk.Label(window, text="Area").pack()
    area_entry = ttk.Entry(window)
    area_entry.pack()

    ttk.Button(window, text="Save Apartment", command=lambda: save_apartment(address_entry.get(), number_entry.get(), area_entry.get())).pack()

def save_apartment(address, number, area):
    conn = create_connection()
    c = conn.cursor()
    c.execute("INSERT INTO apartments (address, number, area) VALUES (?, ?, ?)", (address, number, area))
    conn.commit()
    conn.close()
    tk.messagebox.showinfo("Success", "Apartment added successfully")

def add_key():
    window = tk.Toplevel()
    window.title("Add Key")

    ttk.Label(window, text="Key Number").pack()
    number_entry = ttk.Entry(window)
    number_entry.pack()

    ttk.Label(window, text="Key Type").pack()
    type_entry = ttk.Entry(window)
    type_entry.pack()

    ttk.Label(window, text="Apartment ID").pack()
    apartment_id_entry = ttk.Entry(window)
    apartment_id_entry.pack()

    ttk.Button(window, text="Save Key", command=lambda: save_key(number_entry.get(), type_entry.get(), apartment_id_entry.get())).pack()

def save_key(number, key_type, apartment_id):
    conn = create_connection()
    c = conn.cursor()
    c.execute("INSERT INTO keys (number, type, apartment_id) VALUES (?, ?, ?)", (number, key_type, apartment_id))
    conn.commit()
    conn.close()
    tk.messagebox.showinfo("Success", "Key added successfully")

def add_resident():
    window = tk.Toplevel()
    window.title("Add Resident")

    ttk.Label(window, text="First Name").pack()
    first_name_entry = ttk.Entry(window)
    first_name_entry.pack()

    ttk.Label(window, text="Last Name").pack()
    last_name_entry = ttk.Entry(window)
    last_name_entry.pack()

    ttk.Label(window, text="Phone Number").pack()
    phone_number_entry = ttk.Entry(window)
    phone_number_entry.pack()

    ttk.Label(window, text="Email").pack()
    email_entry = ttk.Entry(window)
    email_entry.pack()

    ttk.Label(window, text="Apartment ID").pack()
    apartment_id_entry = ttk.Entry(window)
    apartment_id_entry.pack()

    ttk.Button(window, text="Save Resident", command=lambda: save_resident(first_name_entry.get(), last_name_entry.get(), phone_number_entry.get(), email_entry.get(), apartment_id_entry.get())).pack()

def save_resident(first_name, last_name, phone_number, email, apartment_id):
    conn = create_connection()
    c = conn.cursor()
    c.execute("INSERT INTO residents (first_name, last_name, phone_number, email, apartment_id) VALUES (?, ?, ?, ?, ?)", 
              (first_name, last_name, phone_number, email, apartment_id))
    conn.commit()
    conn.close()
    tk.messagebox.showinfo("Success", "Resident added successfully")
