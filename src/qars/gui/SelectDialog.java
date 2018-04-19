package qars.gui;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.*;

public class SelectDialog extends JDialog implements QAConstants,
                                                     ActionListener {
	private static final long serialVersionUID = 6521861299485138286L;
	public static final int NO_BUTTON = 0;
    public static final int OK_BUTTON = 1;
    public static final int CANCEL_BUTTON = 2;
    
    private JTextArea instructions;
    @SuppressWarnings("rawtypes")
	private JComboBox selection;
    private JLabel warning;
    private JButton okBtn;
    private JButton cancelBtn;
    private int btnClicked;
    
    public SelectDialog(JFrame owner, String title) {
        this(owner, title, "");
    }
    
    @SuppressWarnings("rawtypes")
	public SelectDialog(JFrame owner, String title, String text) {
        super(owner, title, true);
        this.instructions = new JTextArea(SD_ROWS, SD_COLS);
        this.selection = new JComboBox();
        this.okBtn = new JButton("OK");
        this.okBtn.addActionListener(this);
        this.cancelBtn = new JButton("CANCEL");
        this.cancelBtn.addActionListener(this);
        this.btnClicked = NO_BUTTON;
        this.warning = new JLabel("This field cannot be blank");
        java.awt.Point p = owner.getLocationOnScreen();
        this.setLocation((int) (20 + p.getX()), (int) (100 + p.getY()));
        initializeGUI(text);
    }
    
    @SuppressWarnings("unchecked")
	public void addItem(Object item) {
        this.selection.addItem(item);
    }
    
    public Object getSelectedItem() {
        return this.selection.getSelectedItem();
    }
    
    public int getButton() {
        return this.btnClicked;
    }
    
    public void actionPerformed(java.awt.event.ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if (cmd.equals("OK")) {
            this.btnClicked = OK_BUTTON;
        } else if (cmd.equals("CANCEL")) {
            this.btnClicked = CANCEL_BUTTON;
        }
        boolean inputAccepted = true;
        if (this.btnClicked == OK_BUTTON) {
            Object valObject = this.getSelectedItem();
            String val = null;
            if (valObject != null) {
                val = valObject.toString();
            }
            if (val == null || val.trim().equals("")) {
                this.warning.setVisible(true);
                inputAccepted = false;
            }
        }
        if (inputAccepted) {
            this.setVisible(false);
            this.dispose();
        } else {
            this.btnClicked = NO_BUTTON;
        }
    }
    
    public void setText(String text) {
        this.instructions.setText(text);
    }
    
    public void showDialog() {
        this.pack();
        this.setVisible(true);
    }
    
    @SuppressWarnings("unchecked")
	private void initializeGUI(String text) {
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        this.setUndecorated(false);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        this.instructions.setFont(new Font(null, Font.PLAIN, 12));
        this.instructions.setLineWrap(true);
        this.instructions.setWrapStyleWord(true);
        this.instructions.setAutoscrolls(false);
        this.instructions.setEditable(false);
        this.instructions.setFocusable(false);
        this.instructions.setMargin(new java.awt.Insets(15, 15, 15, 15));
        this.instructions.setOpaque(false);
        this.instructions.setRequestFocusEnabled(false);
        this.instructions.setText(text);
        p.add(this.instructions);
        
        this.warning.setForeground(java.awt.Color.red);
        this.warning.setVisible(false);
        p.add(this.warning);
        
        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.LINE_AXIS));
        comboPanel.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));
        this.selection.setEditable(true);
        this.selection.addItem(null);
        comboPanel.add(this.selection);
        p.add(comboPanel);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(this.okBtn);
        btnPanel.add(this.cancelBtn);
        p.add(btnPanel);
        
        this.setContentPane(p);
    }
}