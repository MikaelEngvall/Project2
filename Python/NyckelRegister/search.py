import tkinter as tk
from tkinter import ttk, messagebox
import sqlite3
import webbrowser

def search_menu():
    def search():
        query = search_entry.get()
        if query:
            conn = sqlite3.connect('keys.db')
            c = conn.cursor()

            # Perform a search across relevant columns
            c.execute('''SELECT residents.id, residents.first_name, residents.last_name, residents.phone_number, residents.email, apartments.address, apartments.number
                         FROM residents
                         JOIN apartments ON residents.apartment_id = apartments.id
                         JOIN keys ON keys.apartment_id = apartments.id
                         WHERE residents.first_name LIKE ? 
                         OR residents.last_name LIKE ? 
                         OR residents.phone_number LIKE ? 
                         OR residents.email LIKE ? 
                         OR apartments.address LIKE ? 
                         OR apartments.number LIKE ?
                         OR keys.number LIKE ?''', 
                         ('%' + query + '%',) * 7)
            
            results = c.fetchall()
            conn.close()

            # Clear previous search results
            for widget in results_frame.winfo_children():
                widget.destroy()

            if results:
                checkbox_vars = []

                def send_email():
                    selected_emails = [var.get() for var in checkbox_vars if var.get()]
                    if selected_emails:
                        mailto_link = "mailto:" + ",".join(selected_emails)
                        webbrowser.open(mailto_link)
                    else:
                        messagebox.showerror("Error", "No recipients selected")

                def select_all():
                    for var in checkbox_vars:
                        var.set(var.email)

                def deselect_all():
                    for var in checkbox_vars:
                        var.set("")

                select_all_btn = ttk.Button(results_frame, text="Select All", command=select_all)
                select_all_btn.grid(row=0, column=0, sticky=tk.W, pady=5)

                deselect_all_btn = ttk.Button(results_frame, text="Deselect All", command=deselect_all)
                deselect_all_btn.grid(row=0, column=1, sticky=tk.W, pady=5)

                send_email_btn = ttk.Button(results_frame, text="Send Email", command=send_email)
                send_email_btn.grid(row=0, column=2, sticky=tk.W, pady=5)

                for idx, res in enumerate(results, start=1):
                    var = tk.StringVar(value="")
                    var.email = res[4]  # Store the email in the variable for later use
                    checkbox_vars.append(var)

                    label_text = f"Name: {res[1]} {res[2]}, Email: {res[4]}, Address: {res[5]}, Apartment Number: {res[6]}"
                    checkbox = ttk.Checkbutton(results_frame, text=label_text, variable=var, onvalue=res[4], offvalue="")
                    checkbox.grid(row=idx, column=0, columnspan=3, sticky=tk.W, pady=5)
            else:
                messagebox.showinfo("No Results", "No residents found matching the search criteria.")

    search_window = tk.Toplevel()
    search_window.title("Search Residents")
    search_window.geometry("600x400")

    style = ttk.Style()
    style.configure('TButton', font=('Helvetica', 12), padding=10)
    style.configure('TLabel', font=('Helvetica', 14), padding=10)
    style.configure('TFrame', background='#f0f0f0')

    frame = ttk.Frame(search_window, padding="10 10 10 10")
    frame.grid(column=0, row=0, sticky=(tk.W, tk.E, tk.N, tk.S))

    ttk.Label(frame, text="Search Residents", font=('Helvetica', 18)).grid(column=0, row=0, columnspan=3, pady=10)

    ttk.Label(frame, text="Enter search term:").grid(row=1, column=0, padx=10, pady=10)
    
    search_entry = ttk.Entry(frame, width=40)
    search_entry.grid(row=1, column=1, padx=10, pady=10)

    search_button = ttk.Button(frame, text="Search", command=search)
    search_button.grid(row=1, column=2, padx=10, pady=10)

    results_frame = ttk.Frame(frame)
    results_frame.grid(row=2, column=0, columnspan=3, padx=10, pady=10)

if __name__ == "__main__":
    root = tk.Tk()
    root.withdraw()  # Hide the root window
    search_menu()
    root.mainloop()
