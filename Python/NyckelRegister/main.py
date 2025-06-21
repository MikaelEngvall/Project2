import tkinter as tk
from tkinter import ttk
from add import add_menu
from delete import delete_menu
from list import list_menu
from search import search_menu
from save import save_to_odf

def main_menu():
    root = tk.Tk()
    root.title("Key Register Application")
    root.geometry("500x400")
    
    style = ttk.Style()
    style.configure('TButton', font=('Helvetica', 12), padding=10)
    style.configure('TLabel', font=('Helvetica', 14), padding=10)
    style.configure('TFrame', background='#f0f0f0')

    frame = ttk.Frame(root, padding="10 10 10 10")
    frame.grid(column=0, row=0, sticky=(tk.W, tk.E, tk.N, tk.S))

    ttk.Label(frame, text="Key Register Application", font=('Helvetica', 18)).grid(column=0, row=0, columnspan=2)
    
    ttk.Button(frame, text="Add Data", command=add_menu).grid(column=0, row=1, sticky=tk.W, pady=5)
    ttk.Button(frame, text="Delete Data", command=delete_menu).grid(column=1, row=1, sticky=tk.W, pady=5)
    ttk.Button(frame, text="List Data", command=list_menu).grid(column=0, row=2, sticky=tk.W, pady=5)
    ttk.Button(frame, text="Search Data", command=search_menu).grid(column=1, row=2, sticky=tk.W, pady=5)
    ttk.Button(frame, text="Save to ODF", command=save_to_odf).grid(column=0, row=3, sticky=tk.W, pady=5)
    ttk.Button(frame, text="Exit", command=root.quit).grid(column=1, row=3, sticky=tk.W, pady=5)

    for child in frame.winfo_children(): 
        child.grid_configure(padx=5, pady=5)

    root.mainloop()

if __name__ == "__main__":
    main_menu()
