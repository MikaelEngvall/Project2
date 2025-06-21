import tkinter as tk
from tkinter import messagebox
from database import execute_query, fetch_query

def add_resident():
    def save_resident():
        first_name = first_name_entry.get()
        last_name = last_name_entry.get()
        apartment_id = apartment_id_entry.get()
        key_id = key_id_entry.get()

        # Insert the new resident
        resident_query = "INSERT INTO Residents (FirstName, LastName, ApartmentID) VALUES (?, ?, ?)"
        execute_query(resident_query, (first_name, last_name, apartment_id))

        # Get the ResidentID of the newly inserted resident
        resident_id = fetch_query("SELECT last_insert_rowid()")[0][0]

        # Assign the key to the resident
        assign_key_query = "INSERT INTO ResidentKeys (KeyID, ResidentID) VALUES (?, ?)"
        execute_query(assign_key_query, (key_id, resident_id))

        window.destroy()
        messagebox.showinfo("Success", "Resident added and assigned to apartment and key successfully!")

    window = tk.Toplevel()
    window.title("Add Resident")

    tk.Label(window, text="First Name:").grid(row=1, column=0)
    first_name_entry = tk.Entry(window)
    first_name_entry.grid(row=1, column=1)

    tk.Label(window, text="Last Name:").grid(row=2, column=0)
    last_name_entry = tk.Entry(window)
    last_name_entry.grid(row=2, column=1)

    tk.Label(window, text="Apartment ID:").grid(row=3, column=0)
    apartment_id_entry = tk.Entry(window)
    apartment_id_entry.grid(row=3, column=1)

    tk.Label(window, text="Key ID:").grid(row=4, column=0)
    key_id_entry = tk.Entry(window)
    key_id_entry.grid(row=4, column=1)

    tk.Button(window, text="Save", command=save_resident).grid(row=5, column=0, columnspan=2)

def add_apartment():
    def save_apartment():
        apartment_number = apartment_number_entry.get()
        building = building_entry.get()
        floor = floor_entry.get()
        
        query = "INSERT INTO Apartments (ApartmentNumber, Building, Floor) VALUES (?, ?, ?)"
        execute_query(query, (apartment_number, building, floor))
        window.destroy()
        messagebox.showinfo("Success", "Apartment added successfully!")

    window = tk.Toplevel()
    window.title("Add Apartment")

    tk.Label(window, text="Apartment Number:").grid(row=0, column=0)
    apartment_number_entry = tk.Entry(window)
    apartment_number_entry.grid(row=0, column=1)

    tk.Label(window, text="Building:").grid(row=1, column=0)
    building_entry = tk.Entry(window)
    building_entry.grid(row=1, column=1)

    tk.Label(window, text="Floor:").grid(row=2, column=0)
    floor_entry = tk.Entry(window)
    floor_entry.grid(row=2, column=1)

    tk.Button(window, text="Save", command=save_apartment).grid(row=3, column=0, columnspan=2)

def add_key():
    def save_key():
        key_code = key_code_entry.get()
        key_type = key_type_entry.get()

        # Check for uniqueness of KeyCode
        result = fetch_query("SELECT * FROM Keys WHERE KeyCode = ?", (key_code,))
        if result:
            messagebox.showerror("Error", "Key Code already exists.")
            return
        
        query = "INSERT INTO Keys (KeyCode, KeyType) VALUES (?, ?)"
        execute_query(query, (key_code, key_type))
        window.destroy()
        messagebox.showinfo("Success", "Key added successfully!")

    window = tk.Toplevel()
    window.title("Add Key")

    tk.Label(window, text="Key Code:").grid(row=0, column=0)
    key_code_entry = tk.Entry(window)
    key_code_entry.grid(row=0, column=1)

    tk.Label(window, text="Key Type:").grid(row=1, column=0)
    key_type_entry = tk.Entry(window)
    key_type_entry.grid(row=1, column=1)

    tk.Button(window, text="Save", command=save_key).grid(row=2, column=0, columnspan=2)
