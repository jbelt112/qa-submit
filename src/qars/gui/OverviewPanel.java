package qars.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * The OverviewPanel presents a list of items and a total for each line. As a
 * table is updated, the value is changed if the column is a counting column.
 * 
 * @author Jaren Belt
 */
public class OverviewPanel extends JPanel implements QAConstants,
                                                     TableModelListener {
	private static final long serialVersionUID = 7670909717327547653L;
	private JLabel title;
    private java.util.ArrayList<CompoundLinePanel> lines;
    
    public OverviewPanel(String title) {
        this.title = new JLabel(title);
        this.lines = new java.util.ArrayList<CompoundLinePanel>();
        initializeGUI();
    }
    
    public void tableChanged(javax.swing.event.TableModelEvent tme) {
        if (tme instanceof DisplayableTableModelEvent) {
            TableModel mod = (TableModel) tme.getSource();
            DisplayableTableModelEvent e = (DisplayableTableModelEvent) tme;
            for (int i = 0; i < this.lines.size(); i++) {
                CompoundLinePanel clp = this.lines.get(i);
                if (clp.ownedBy(mod)) {
                    clp.update(e.getComments());
                }
            }
        }
    }
    
    public void addItem(String label, int start, TableModel mod) {
        CompoundLinePanel p = new CompoundLinePanel(label, start, mod);
        this.lines.add(p);
        this.add(p);
    }
    
    public int getTotal() {
        int num = 0;
        for (int i = 0; i < this.lines.size(); i++) {
            num += this.lines.get(i).getCount();
        }
        return num;
    }
    
    private void initializeGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        int width = (int) ((FR_WIDTH - DIV_SIZE) * (1 - LP_RATIO)) - 3;
        int height = (int) ((CT_HEIGHT - DIV_SIZE) * (1 - TP_RATIO)) - 6;
        Dimension minPrefSize = new Dimension(width, height);
        this.setMinimumSize(minPrefSize);
        this.setPreferredSize(minPrefSize);
        this.setBackground(Color.black);
        
        this.title.setForeground(Color.white);
        JPanel titlePanel = new JPanel(new FlowLayout());
        titlePanel.setOpaque(false);
        height = ((int) (this.title.getPreferredSize().getHeight())) + 6;
        titlePanel.setMaximumSize(new Dimension(MAX_DIM, height));
        minPrefSize = new Dimension(width, height);
        titlePanel.setMinimumSize(minPrefSize);
        titlePanel.setPreferredSize(minPrefSize);
        titlePanel.add(this.title);
        this.add(titlePanel);
    }
    
    private class CompoundLinePanel extends JPanel {
		private static final long serialVersionUID = 1192929391456303785L;
		private int count;
        private int aux;
        private TableModel myModel;
        private JLabel countLabel;
        public CompoundLinePanel(String label, int start, TableModel mod) {
            super(new GridLayout(1, 2));
            this.count = start;
            this.aux = 0;
            this.myModel = mod;
            this.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 8));
            this.setOpaque(false);
            JLabel name = new JLabel(label);
            int height = (int) (name.getPreferredSize().getHeight() + 3);
            this.setMaximumSize(new Dimension(MAX_DIM, height));
            name.setForeground(Color.white);
            this.add(name);
            countLabel = new JLabel(start + "", SwingConstants.RIGHT);
            countLabel.setForeground(Color.white);
            this.add(countLabel);
        }
        public boolean ownedBy(TableModel mod) {
            return this.myModel == mod;
        }
        public void update(String note) {
            if (note.equals("add 1") || note.equals("update 1")) {
                this.count++;
            } else if (note.equals("remove") || note.equals("update -1")) {
                this.count--;
                if (this.count < 0) {
                    this.count = 0;
                }
            } else if (note.equals("1")) {
                this.aux++;
            } else if (note.equals("-1")) {
                this.aux--;
                if (this.aux < 0) {
                    this.aux = 0;
                }
            }
            this.countLabel.setText(count + "");
        }
        public int getCount() {
            return this.count + this.aux;
        }
    }
}