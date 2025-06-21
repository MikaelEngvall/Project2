import tkinter as tk
from tkinter import ttk
from database import fetch_query

def show_table(data, headers):
    window = tk.Toplevel()
    window.title("Data")

    table = ttk.Treeview(window, columns=headers, show='headings')
    for header in headers:
        table.heading(header, text=header)
        table.column(header, minwidth=0, width=150)

    for row in data:
        table.insert('', 'end', values=row)

    table.pack(expand=True, fill='both')

    tk.Button(window, text="Close", command=window.destroy).pack()

def list_apartments():
    query = """
    SELECT a.ApartmentID, a.ApartmentNumber, a.Building, a.Floor, 
           r.FirstName || ' ' || r.LastName AS Resident, k.KeyCode
    FROM Apartments a
    LEFT JOIN Residents r ON a.ApartmentID = r.ApartmentID
    LEFT JOIN ResidentKeys rk ON r.ResidentID = rk.ResidentID
    LEFT JOIN Keys k ON rk.KeyID = k.KeyID
    """
    result = fetch_query(query)
    headers = ["ApartmentID", "ApartmentNumber", "Building", "Floor", "Resident", "KeyCode"]
    show_table(result, headers)

def list_keys():
    query = """
    SELECT k.KeyID, k.KeyCode, k.KeyType, 
           r.FirstName || ' ' || r.LastName AS Resident, a.ApartmentNumber
    FROM Keys k
    LEFT JOIN ResidentKeys rk ON k.KeyID = rk.KeyID
    LEFT JOIN Residents r ON rk.ResidentID = r.ResidentID
    LEFT JOIN Apartments a ON r.ApartmentID = a.ApartmentID
    """
    result = fetch_query(query)
    headers = ["KeyID", "KeyCode", "KeyType", "Resident", "ApartmentNumber"]
    show_table(result, headers)

def list_residents():
    query = """
    SELECT r.ResidentID, r.FirstName, r.LastName, a.ApartmentNumber, k.KeyCode
    FROM Residents r
    LEFT JOIN Apartments a ON r.ApartmentID = a.ApartmentID
    LEFT JOIN ResidentKeys rk ON r.ResidentID = rk.ResidentID
    LEFT JOIN Keys k ON rk.KeyID = k.KeyID
    """
    result = fetch_query(query)
    headers = ["ResidentID", "FirstName", "LastName", "ApartmentNumber", "KeyCode"]
    show_table(result, headers)
