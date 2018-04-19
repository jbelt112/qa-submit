package qars.gui;

import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * <p>A DisplayableTableModel is used when showing Displayable objects in a
 * table. The column headings are stored as well as a column for keeping a
 * tally on checked fields.</p>
 * 
 * @author Jaren Belt
 */
public class DisplayableTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -8273935324297915249L;
	private ArrayList<Displayable> items;
    private String[] columns;
    private int countColumn;
    private boolean isEditable;
    private DisplayableTableModel child;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new DisplayableTableModel. The items are kept sorted.
     * @param data An initial list of Displayable items.
     * @param columns An array of column names.
     * @param cc Optional counting column number. This column will be counted
     * @param fileType File type for this model
     * if it is of type Boolean and the value is on.
     */
    public DisplayableTableModel(ArrayList<Displayable> data, 
                                 String[] columns, int cc) {
        //this.items = data = new ArrayList<Displayable>();
        this.items = new ArrayList<Displayable>();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                this.add(data.get(i));
            }
        }
        this.columns = columns;
        this.countColumn = cc;
        this.isEditable = true;
        this.child = null;
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Retrieves a reference to the item at a particular index.
     * @param index The index (or row) of the item to retrieve.
     * @return A reference to the item or null if the index is out of bounds.
     */
    public Displayable get(int index) {
        Displayable retVal = null;
        if (index >= 0 && index < this.items.size()) {
            retVal = this.items.get(index);
        }
        return retVal;
    }
    
    /**
     * Sets whether checkboxes are editable (enabled)
     * @param isEditable Can updatable columns be modified?
     */
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }
    
    /**
     * Sets the dependent model. Changes made to an item in this model may
     * affect the items in another model.
     * @param child The child DisplayableTableModel
     */
    public void setChildModel(DisplayableTableModel child) {
        this.child = child;
    }
    
    /**
     * Determines the index (or row) of a given item.
     * @param item A Displayable object to match to the desired item.
     * @return The row number of the item if it exists, or -1 if the item does
     * not exist.
     */
    public int indexOf(Displayable item) {
        return this.items.indexOf(item);
    }
    
    /**
     * Given a Displayable object, this method returns the reference to a 
     * matching object being stored by this model.
     * @param item A Displayable object to match to the desired item.
     * @return A reference to the matched Displayable, or null.
     */
    public Displayable get(Displayable item) {
        Displayable retVal = null;
        for (int i = 0; i < this.items.size(); i++) {
            Displayable itemsItem = this.items.get(i);
            if (item.equals(itemsItem)) {
                retVal = itemsItem;
                break;
            }
        }
        return retVal;
    }
    
    /**
     * Adds a Displayable object to the list; the object is first cloned. This
     * method will fire a DisplayableTableModelEvent to any table model
     * listeners.
     * @param item The Displayable object to add.
     * @return true if the item was not in the list and was added.
     */
    public boolean add(Displayable item) {
        boolean retVal = true;
        if (item == null || this.items.contains(item)) {
            retVal = false;
        } else {
            this.items.add(item.clone());
            java.util.Collections.sort(this.items);
            String action = "add";
            Object qry = item.query(this.columns[this.countColumn]);
            if (qry instanceof Boolean && ((Boolean) qry).booleanValue()) {
                action += " 1";
            }
            DisplayableTableModelEvent e = new DisplayableTableModelEvent(this,
                0, this.getRowCount(), TableModelEvent.ALL_COLUMNS, 
                TableModelEvent.INSERT, action);
            this.fireTableChanged(e);
        }
        return retVal;
    }
    
    /**
     * Removes a given item if it exists. This method fires a 
     * DisplayableTableModelEvent to any table listeners.
     * @param item The Displayable to be removed.
     * @return A reference to the Displayable that was matched to item,
     * or null if the operation failed.
     */
    public Displayable remove(Displayable item) {
        return this.remove(item, true);
    }
    
    /**
     * Removes a given item if it exists. This method fires a
     * DisplayableTableModelEvent to any table listeners.
     * @param item The Displayable to be removed.
     * @param notify Should listeners be notified of the removal?
     * @return A reference to the actual Displayable that was matched to item,
     * or null if the operation failed.
     */
    public Displayable remove(Displayable item, boolean notify) {
        int index = this.items.indexOf(item);
        Displayable retVal = null;
        if (index >= 0) {
            DisplayableTableModelEvent e = new DisplayableTableModelEvent(this,
                0, this.getRowCount(), TableModelEvent.ALL_COLUMNS,
                TableModelEvent.DELETE, "remove");
            retVal = this.items.remove(index);
            if (notify) {
                this.fireTableChanged(e);
            }
        }
        return retVal;
    }
    
    /**
     * Gives the number of rows stored in this model.
     */
    public int getRowCount() {
        return items.size();
    }
    
    /**
     * Gives the number of columns stored in this model.
     */
    public int getColumnCount() {
        int retVal = 0;
        if (columns != null) {
            retVal = columns.length;
        }
        return retVal;
    }
    
    /**
     * Retrieves the value of a cell in the table.
     * @param r The row of the value.
     * @param c The column of the value.
     * @return The value in the form of an Object reference or null if the given
     * coordinates do not exist.
     */
    public Object getValueAt(int r, int c) {
        Object retVal = null;
        if (r >= 0 && r < items.size() && c >= 0 && c < columns.length) {
            Displayable item = items.get(r);
            retVal = item.query(columns[c]);
        }
        return retVal;
    }
    
    /**
     * Retrieves the stored column name of a given column.
     * @return Column name or null if the column is not valid for this model.
     */
    public String getColumnName(int col) {
        String retVal = null;
        if (col >= 0 && col < columns.length) {
            retVal = columns[col];
        }
        return retVal;
    }
    
    /**
     * Retrieves the class of the specified column. This method is called to
     * determine how a table should display the value. A Boolean will be
     * displayed as a checkbox.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int col) {
        Object obj = getValueAt(0, col);
        if (obj != null) {
            return obj.getClass();
        } else {
            return null;
        }
    }
    
    /**
     * Determines whether the cell can be edited. Only Boolean values are
     * allowed to be edited.
     * @param row The row of the cell in question.
     * @param col The column.
     * @return true if the cell exists and is of type Boolean.
     */
    public boolean isCellEditable(int row, int col) {
        Object cell = getValueAt(row, col);
        boolean retVal = false;
        if (this.isEditable && cell != null && cell instanceof Boolean) {
            retVal = true;
        }
        return retVal;
    }
    
    /**
     * Sets the value of a cell. The underlying Displayable object will have
     * the value noted by the column name to the new value. A
     * DisplayableTableModelEvent is only fired if the value is changing.
     * @param val The new value for the cell. Only Boolean values are allowed.
     * @param r The row of the cell.
     * @param c The column of the cell.
     */
    public void setValueAt(Object val, int r, int c) {
        if (r >= 0 && r < items.size() && c >= 0 && c < columns.length) {
            Displayable item = items.get(r);
            Object o = item.query(columns[c]);
            item.setValue(columns[c], val);
            if (this.isCellEditable(r, c)) {
                Boolean bo = (Boolean) o;
                Boolean bval = (Boolean) val;
                if (!bo.equals(bval)) {
                    String notes = bval.booleanValue() ? "1" : "-1";
                    if (this.columns[c].equals(this.columns[this.countColumn])){
                        notes = "update " + notes;
                    }
                    DisplayableTableModelEvent e = 
                        new DisplayableTableModelEvent(this, r, r, c, 
                            TableModelEvent.UPDATE, notes, this.child);
                    this.fireTableChanged(e);
                }
            } else {
                fireTableCellUpdated(r, c);
            }
        }
    }
}