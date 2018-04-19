package qars.gui;

import java.awt.Dimension;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

public class ItemViewPanel extends JPanel implements ListSelectionListener,
                                                     QAConstants,
                                                     TableModelListener {
	private static final long serialVersionUID = -4711304879933236556L;
	private JTable items;
    private RequestViewPanel rvp;
    private JLabel noItems;
    private JScrollPane jsp;
    private DualPanel parent;
    
    public ItemViewPanel(RequestViewPanel rvp, DualPanel holder) {
        this.rvp = rvp;
        this.parent = holder;
        this.items = new JTable();
        ListSelectionModel listModel = this.items.getSelectionModel();
        listModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listModel.addListSelectionListener(this);
        this.items.setSelectionModel(listModel);
        this.noItems = new JLabel("There are no items to view");
        initializeGUI();
    }
    
    public void tableChanged(javax.swing.event.TableModelEvent tme) {
        if (items.getModel().getRowCount() == 0) {
            this.noItems.setVisible(true);
            this.jsp.setVisible(false);
        } else {
            this.noItems.setVisible(false);
            this.jsp.setVisible(true);
        }
    }
    
    public void valueChanged(javax.swing.event.ListSelectionEvent lse) {
        if (lse.getValueIsAdjusting()) {
            DisplayableTableModel mod = 
                (DisplayableTableModel) this.items.getModel();
            this.rvp.updateRequest(mod.get(this.items.getSelectedRow()));
            this.parent.resetScroll(DualPanel.BOTTOM);
        }
    }
    
    public javax.swing.table.TableModel getTableModel() {
        return this.items.getModel();
    }
    
    public void setTableModel(javax.swing.table.TableModel model) {
        this.items.setModel(model);
    }

    public javax.swing.table.TableColumn getColumn(int column) {
        javax.swing.table.TableColumn col = null;
        try {
            col = this.items.getColumnModel().getColumn(column);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
        return col;
    }

    private void initializeGUI() {
        int width = FR_WIDTH - 20;
        int height = (int) ((CT_HEIGHT - DIV_SIZE) * (1 -TP_RATIO)) - FR_TITLE;
        Dimension minPrefSize = new Dimension(width, height);

        this.add(noItems);
        
        this.jsp = new JScrollPane();
        this.jsp.setMinimumSize(minPrefSize);
        this.jsp.setPreferredSize(minPrefSize);
        this.jsp.setViewportView(this.items);
        this.jsp.setVisible(false);
        this.add(this.jsp);
    }
}