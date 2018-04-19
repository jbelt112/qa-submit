package qars.gui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import qars.util.SCR;

/**
 * Panel that contains a list of files to be handled in a transfer request.
 * Files are listed in a table format. If the included checkbox is checked, the
 * file will be transferred. If it is not checked, the file will not be handled
 * in the transfer, but will be available for future requests. It is the
 * DisplayableTableModel that maintains the list of files.
 * 
 * @author Jaren Belt
 */
public class TabPanel extends JPanel implements QAConstants,
                                                ListSelectionListener,
                                                MouseListener,
                                                TableModelListener {
	private static final long serialVersionUID = -2211640959464107410L;
	private JTable files;
    private FileInfoPanel fip;
    private int fileType;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new TabPanel.
     * @param scrName The SCR ID assigned to the request
     * @param parent Listener who will receive change notifications
     * @param fip Panel to display information about the selected file
     * @param fileType see SCR
     * @param mohm Handler for hint messages
     */
    public TabPanel(String scrName, ActionListener parent, FileInfoPanel fip,
                    int fileType, MouseOverHintManager mohm) {
        super(new BorderLayout());
        this.fileType = fileType;
        this.files = new JTable();
        this.files.getTableHeader().addMouseListener(this);
        this.fip = fip;
        ListSelectionModel listModel = this.files.getSelectionModel();
        listModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listModel.addListSelectionListener(this);
        this.files.setSelectionModel(listModel);
        initializeGUI(scrName, parent, mohm, (fileType == SCR.IMPACT ? false : true));
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Sets the table model used to display files.
     * @param model A DisplayableTableModel for this type of file
     */
    public void setTableModel(javax.swing.table.TableModel model) {
        this.files.setModel(model);
    }
    
    /**
     * Returns the DisplayableTableModel for this type of file
     * @return the DisplayableTableModel for this type of file
     */
    public javax.swing.table.TableModel getTableModel() {
        return this.files.getModel();
    }
    
    /**
     * Deselects all selected columns and rows.
     */
    public void clearSelection() {
        this.files.clearSelection();
    }
    
    /**
     * Obtains a reference to the specified column of the table.
     * @param column The column number
     * @return Reference to column or null if column is not valid.
     */
    public javax.swing.table.TableColumn getColumn(int column) {
        javax.swing.table.TableColumn col = null;
        try {
            col = this.files.getColumnModel().getColumn(column);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
        return col;
    }
    
    /**
     * Retrieves the file type of this table. See SCR.
     * @return File type 
     */
    public int getFileType() {
        return this.fileType;
    }
    
    /**
     * Handles events when an item is added, removed, or changed (checkbox is
     * checked).
     * @param tme The event: INSERT, DELETE, or UPDATE
     */
    public void tableChanged(javax.swing.event.TableModelEvent tme) {
        if (this.fileType == qars.util.SCR.SOURCE) {
            /*DisplayableTableModel mod =
                (DisplayableTableModel) this.files.getModel();*/
            DisplayableTableModelEvent dtme = null;
            if (tme instanceof DisplayableTableModelEvent) {
                dtme = (DisplayableTableModelEvent) tme;
            }
            String s = "";
            int diff = 0;
            switch (tme.getType()) {
                /*case javax.swing.event.TableModelEvent.INSERT: 
                    s = "INSERT";
                    if (dtme.getComments().equals("add 1")) {
                        diff = 1;
                    }
                    break;
                case javax.swing.event.TableModelEvent.DELETE: 
                    s = "DELETE";
                    if (dtme.getComments().equals("remove")) {
                        diff = -1;
                    }
                    break;*/
                case javax.swing.event.TableModelEvent.UPDATE: 
                    s = "UPDATE";
                    if (dtme.getComments().equals("update 1")) {
                        diff = 1;
                    } else if (dtme.getComments().equals("update -1")) {
                        diff = -1;
                    }
                    break;
            }
            qars.util.File f = this.fip.getSelectedFile();
            if (f != null && s.equals("UPDATE")) {
                if (((String) f.query(qars.util.File.FN)).endsWith(".i") ||
                    ((String) f.query(qars.util.File.FN)).endsWith(".f")) {
                    qars.util.File[] ifiles = f.getImpactList();
                    DisplayableTableModel mod2 = dtme.getChild();
                    if (ifiles != null && mod2 != null) {
                        for (int i = 0; i < ifiles.length; i++) {
                            qars.util.File iFile = (qars.util.File) mod2.get(ifiles[i]);
                            if (iFile != null) {
                                int ref = iFile.getRef();
                                iFile.reference(diff);
                                if (ref == 0 && diff > 0) {
                                    dtme = new DisplayableTableModelEvent(mod2,
                                        tme.getFirstRow(), tme.getLastRow(),
                                        tme.getColumn(), tme.getType(),
                                        "update 1");
                                    mod2.fireTableChanged(dtme);
                                } else if (ref == 1 && diff < 0) {
                                    dtme = new DisplayableTableModelEvent(mod2,
                                        tme.getFirstRow(), tme.getLastRow(),
                                        tme.getColumn(), tme.getType(),
                                        "update -1");
                                    mod2.fireTableChanged(dtme);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Invoked when the row selection changes. Passes notification to the
     * FileInfoPanel to display the currently selected file.
     * @param lse Event generated by the system when new row is selected
     */
    public void valueChanged(javax.swing.event.ListSelectionEvent lse) {
        if (lse.getValueIsAdjusting()) {
            DisplayableTableModel mod = 
                (DisplayableTableModel) this.files.getModel();
            this.fip.update(mod.get(this.files.getSelectedRow()));
        }
    }
    
    /**
     * Invoked when the mouse button has been clicked on a component. Checks to
     * see if the row/column is editable. Also changes all check boxes in a
     * column if the column header is clicked.
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        int column = this.files.getTableHeader().columnAtPoint(e.getPoint());
        DisplayableTableModel mod =
            (DisplayableTableModel) this.files.getModel();
        if (mod.isCellEditable(0, column)) {
            int numRows = mod.getRowCount();
            boolean anyOff = false;
            for (int r = 0; r < numRows && !anyOff; r++) {
                Boolean check = (Boolean) mod.getValueAt(r, column);
                if (!check.booleanValue()) {
                    anyOff = true;
                }
            }
            Boolean newValue = new Boolean(anyOff);
            for (int r = 0; r < numRows; r++) {
                mod.setValueAt(newValue, r, column);
            }
        }
    }
    public void mouseEntered(java.awt.event.MouseEvent e) {}
    public void mouseExited(java.awt.event.MouseEvent e) {}
    public void mousePressed(java.awt.event.MouseEvent e) {}
    public void mouseReleased(java.awt.event.MouseEvent e) {}
    
    // private methods ----------------------------------------------------
    
    private void initializeGUI(String scrName, ActionListener parent,
                               MouseOverHintManager mohm, boolean edit) {
        int width = FR_WIDTH - 12;
        int height = (int) ((CT_HEIGHT - DIV_SIZE) * TP_RATIO) - FR_TITLE;
        Dimension minPrefSize = new Dimension(width, height);
        this.setMinimumSize(minPrefSize);
        this.setPreferredSize(minPrefSize);
        
        JPanel scrLabelPanel = new JPanel(new FlowLayout());
        JLabel scrLabel = new JLabel(scrName);
        height = (int) (scrLabel.getPreferredSize().getHeight() + 6);
        scrLabelPanel.setMaximumSize(new Dimension(MAX_DIM, height));
        minPrefSize = new Dimension(width, height);
        scrLabelPanel.setMinimumSize(minPrefSize);
        scrLabelPanel.add(scrLabel);
        this.add(scrLabelPanel, BorderLayout.PAGE_START);
        
        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("ADD");
        addBtn.setEnabled(edit);
        addBtn.addActionListener(parent);
        addBtn.setActionCommand("TabPanel Add");
        mohm.addHintFor(addBtn, "Add files to this tab");
        height = (int) (addBtn.getPreferredSize().getHeight() + 6);
        btnPanel.setMaximumSize(new Dimension(MAX_DIM, height));
        minPrefSize = new Dimension(width, height);
        btnPanel.setMinimumSize(minPrefSize);
        btnPanel.setPreferredSize(minPrefSize);
        btnPanel.add(addBtn);
        JButton remBtn = new JButton("REMOVE");
        remBtn.setEnabled(edit);
        remBtn.addActionListener(parent);
        remBtn.setActionCommand("TabPanel Remove");
        mohm.addHintFor(remBtn, "Remove the selected file from this tab");
        btnPanel.add(remBtn);
        this.add(btnPanel, BorderLayout.PAGE_END);
        
        JScrollPane jsp = new JScrollPane();
        jsp.setViewportView(this.files);
        this.files.setOpaque(false);
        this.files.setShowHorizontalLines(false);
        this.files.setShowVerticalLines(false);
        this.files.setShowGrid(false);
        this.add(jsp, BorderLayout.CENTER);
    }
}