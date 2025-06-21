import tkinter as tk
from add import add_resident, add_apartment, add_key
from delete import delete_key
from list import list_apartments, list_keys, list_residents

def main():
    root = tk.Tk()
    root.title("Apartment Keys Management")

    menu_frame = tk.Frame(root)
    menu_frame.pack(padx=10, pady=10)

    tk.Button(menu_frame, text="Add Apartment", command=add_apartment).grid(row=0, column=0, pady=5)
    tk.Button(menu_frame, text="Add Key", command=add_key).grid(row=1, column=0, pady=5)
    tk.Button(menu_frame, text="Add Resident", command=add_resident).grid(row=2, column=0, pady=5)
    tk.Button(menu_frame, text="Delete Key", command=delete_key).grid(row=3, column=0, pady=5)
    tk.Button(menu_frame, text="List Apartments", command=list_apartments).grid(row=4, column=0, pady=5)
    tk.Button(menu_frame, text="List Keys", command=list_keys).grid(row=5, column=0, pady=5)
    tk.Button(menu_frame, text="List Residents", command=list_residents).grid(row=6, column=0, pady=5)
    tk.Button(menu_frame, text="Exit", command=root.quit).grid(row=16, column=0, pady=5)

    root.mainloop()

if __name__ == "__main__":
    main()
