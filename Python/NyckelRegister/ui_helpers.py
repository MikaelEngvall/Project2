import tkinter as tk
from tkinter import ttk
from tkinter import messagebox

def create_treeview(frame, *columns):
    tree = ttk.Treeview(frame, columns=columns, show='headings')
    for col in columns:
        tree.heading(col, text=col)
    return tree

def edit_row(tree, item_id, values, save_function, delete_function, conn):
    tree.item(item_id, values=('',)*len(values))

    entry_widgets = []
    num_columns = len(tree.cget('columns'))
    col_widths = [tree.column(col, 'width') for col in tree.cget('columns')]
    x_positions = [sum(col_widths[:i]) for i in range(num_columns)]
    y_position = tree.bbox(item_id)[1] + tree.winfo_y()

    for i, val in enumerate(values):
        entry = tk.Entry(tree, width=int(col_widths[i] // 8))
        entry.insert(0, val)
        entry.place(x=x_positions[i], y=y_position)
        entry_widgets.append(entry)

    button_x_position = x_positions[-1] + col_widths[-1] + 10

    save_button = ttk.Button(tree, text="💾", command=lambda: save_function(tree, item_id, entry_widgets, conn))
    save_button.place(x=button_x_position, y=y_position)

    delete_button = ttk.Button(tree, text="🗑️", command=lambda: confirm_delete(lambda: delete_function(tree, item_id, values[0], conn)))
    delete_button.place(x=button_x_position + 30, y=y_position)

def on_item_click(event, tree, table_name, update_function, delete_function, conn):
    selected_item = tree.selection()[0]
    item_values = tree.item(selected_item, 'values')
    edit_row(tree, selected_item, item_values, update_function, delete_function, conn)

def confirm_delete(delete_command):
    if messagebox.askyesno("Confirm Delete", "Are you sure you want to delete this entry?"):
        delete_command()
