package qars.gui;

import javax.swing.event.TableModelEvent;

/**
 * <p>The DisplayableTableModelEvent class is used by a DisplayableTableModel to
 * notify TableModelListeners of a change to an editable cell within the table.
 * This class extends javax.swing.event.TableModelEvent to pass additional
 * comments to the listener.</p>
 * 
 * @author Jaren Belt
 */
public class DisplayableTableModelEvent extends TableModelEvent {
	private static final long serialVersionUID = -6329830260202295601L;
	private String notes;
    private DisplayableTableModel child;
    
    // constructors -------------------------------------------------------
    
    /**
     * Create a new DisplayableTableModelEvent.
     * @param mod The DisplayableTableModel that is creating this event.
     * @param firstRow The first row in the change.
     * @param lastRow The last row in the change.
     * @param column The column being changed.
     * @param type See TableModelEvent.
     * @param comments Comments to be passed on to the listener.
     */
    public DisplayableTableModelEvent(javax.swing.table.TableModel mod,
                                      int firstRow, int lastRow, int column,
                                      int type, String comments) {
        this(mod, firstRow, lastRow, column, type, comments, null);
    }
    
    /**
     * Create a new DisplayableTableModelEvent.
     * @param mod The DisplayableTableModel that is creating this event.
     * @param firstRow The first row in the change.
     * @param lastRow the last row in the change.
     * @param column the column being changed.
     * @param type See TableModelEvent.
     * @param comments Comments to be passed on to the listener.
     * @param child Dependent table model.
     */
    public DisplayableTableModelEvent(javax.swing.table.TableModel mod,
                                      int firstRow, int lastRow, int column,
                                      int type, String comments,
                                      DisplayableTableModel child) {
        super(mod, firstRow, lastRow, column, type);
        this.notes = comments;
        this.child = child;
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Retrieves the stored comments for this event.
     */
    public String getComments() {
        return this.notes;
    }
    
    /**
     * Retrieves the dependent table model or null if there isn't one
     */
    public DisplayableTableModel getChild() {
        return this.child;
    }
}