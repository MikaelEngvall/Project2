import sqlite3
from odf.opendocument import OpenDocumentText
from odf.text import P
from tkinter import messagebox
from database import create_connection

def save_to_odf():
    conn = create_connection()
    c = conn.cursor()

    doc = OpenDocumentText()
    c.execute('''
        SELECT keys.number, keys.type, apartments.address, apartments.number, residents.first_name, residents.last_name, residents.phone_number, residents.email
        FROM keys
        LEFT JOIN apartments ON keys.apartment_id = apartments.id
        LEFT JOIN residents ON apartments.id = residents.apartment_id
    ''')
    records = c.fetchall()

    for record in records:
        para = P(text=str(record))
        doc.text.addElement(para)

    doc.save("key_register.odt")
    conn.close()
    messagebox.showinfo("Success", "Database saved to key_register.odt")
