package qars.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import qars.util.File;

/**
 * <p>The FileInfoPanel shows detailed information about a file such as the
 * file name, where it resides on bones and its destination folder on qars,
 * and whether it is a webspeed or async file. When attributes are changed,
 * the underlying File object is also updated.</p>
 * 
 * @author Jaren Belt
 */
public class FileInfoPanel extends JPanel implements QAConstants,
                                                     ActionListener {
	private static final long serialVersionUID = 835321162653568447L;
	private JCheckBox hostCheck;
    private JCheckBox webspeedCheck;
    private JCheckBox asyncCheck;
    private JLabel fileName;
    @SuppressWarnings("rawtypes")
	private JComboBox srcCombo;
    @SuppressWarnings("rawtypes")
	private JComboBox destCombo;
    private boolean systemChange;                   // if file is being switched
    private File file;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new panel. All inputs are disabled.
     */
    @SuppressWarnings("rawtypes")
	public FileInfoPanel(MouseOverHintManager mohm) {
        super(new BorderLayout());
        this.systemChange = false;
        this.fileName = new JLabel();
        
        this.hostCheck = new JCheckBox("Host");
        this.hostCheck.addActionListener(this);
        this.hostCheck.setEnabled(false);
        mohm.addHintFor(this.hostCheck, "Is this file a Host file?");
        
        this.webspeedCheck = new JCheckBox("WebSpeed");
        this.webspeedCheck.addActionListener(this);
        this.webspeedCheck.setEnabled(false);
        mohm.addHintFor(this.webspeedCheck, "Is this file a WebSpeed file?");
        
        this.asyncCheck = new JCheckBox("Async");
        this.asyncCheck.addActionListener(this);
        this.asyncCheck.setEnabled(false);
        mohm.addHintFor(this.asyncCheck, "Is this file an Async file?");
        
        this.srcCombo = new JComboBox();
        this.srcCombo.addActionListener(this);
        this.srcCombo.setEnabled(false);
        
        this.destCombo = new JComboBox();
        this.destCombo.addActionListener(this);
        this.destCombo.setEnabled(false);
        
        initializeGUI();
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Obtain the file currently displayed in the panel.
     * @return A File reference or null if no file is displayed.
     */
    public File getSelectedFile() {
        return this.file;
    }
    
    /**
     * Switches the displayed file.
     * @param thefile A new File to be displayed.
     */
    @SuppressWarnings("unchecked")
	public void update(Displayable thefile) {
        // set so we can ignore ActionEvents fired
        this.systemChange = true;
        if (this.file == null) {
            this.enableForm(true);
        }
        this.file = (File) thefile;
        int fType = ((Integer) this.file.query(File.CT)).intValue();
        // must be /g1/dev/source
        if (fType == qars.util.SCR.SOURCE) {
            this.srcCombo.setEnabled(false);
        }
        this.fileName.setText((String) this.file.query(File.FN));
        Boolean sel = (Boolean) this.file.query(File.WS);
        this.webspeedCheck.setSelected(sel.booleanValue());
        sel = (Boolean) this.file.query(File.AS);
        this.asyncCheck.setSelected(sel.booleanValue());
        sel = (Boolean) this.file.query(File.HS);
        this.hostCheck.setSelected(sel.booleanValue());
        this.srcCombo.removeAllItems();
        this.srcCombo.addItem(this.file.query(File.SC));
        this.destCombo.setSelectedItem(this.file.query(File.DN));
        this.systemChange = false;
    }
    
    /**
     * Intercepts ActionEvents fired by changing an input field.
     * @param ae The event being intercepted.
     */
    @SuppressWarnings("rawtypes")
	public void actionPerformed(java.awt.event.ActionEvent ae) {
        if (!this.systemChange && this.file != null) {
            String cmd = ae.getActionCommand();
            if (cmd.equals("WebSpeed")) {
                boolean val = this.webspeedCheck.isSelected();
                this.file.setValue(File.WS, new Boolean(val));
            } else if (cmd.equals("Async")) {
                boolean val = this.asyncCheck.isSelected();
                this.file.setValue(File.AS, new Boolean(val));
            } else if (cmd.equals("Host")) {
                boolean val = this.hostCheck.isSelected();
                this.file.setValue(File.HS, new Boolean(val));
            } else if (cmd.equals("comboBoxEdited") || 
                       cmd.equals("comboBoxChanged")) {
                JComboBox box = (JComboBox) ae.getSource();
                if (box == this.srcCombo) {
                    String val = (String) this.srcCombo.getSelectedItem();
                    this.file.setValue(File.SC, val);
                } else if (box == this.destCombo) {
                    String val = (String) this.destCombo.getSelectedItem();
                    this.file.setValue(File.DN, val);
                }
            }
        }
    }
    
    /**
     * Disables input fields and removes underlying File reference.
     */
    public void clear() {
        this.enableForm(false);
        this.file = null;
        this.fileName.setText("");
        this.srcCombo.setSelectedItem(null);
        this.destCombo.setSelectedItem(null);
        this.hostCheck.setSelected(false);
        this.webspeedCheck.setSelected(false);
        this.asyncCheck.setSelected(false);
    }

    // private methods ----------------------------------------------------
    
    /**
     * Creates input fields and places them in aligned rows.
     */
    @SuppressWarnings("unchecked")
	private void initializeGUI() {
        int width = (int) ((FR_WIDTH - DIV_SIZE) * LP_RATIO) - 3;
        int height = (int) ((CT_HEIGHT - DIV_SIZE) * (1 - TP_RATIO)) - 3;
        this.setMinimumSize(new Dimension(width, height));
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        Dimension maxSize = new Dimension(MAX_DIM, FIP_TOP);
        top.setMaximumSize(maxSize);
        Dimension minPrefSize = new Dimension(width, FIP_TOP);
        top.setMinimumSize(minPrefSize);
        top.setPreferredSize(minPrefSize);
        this.fileName.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        top.add(this.fileName);
        this.add(top, BorderLayout.PAGE_START);
        
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btm.setMaximumSize(maxSize);
        btm.setMinimumSize(minPrefSize);
        btm.setPreferredSize(minPrefSize);
        btm.add(this.hostCheck);
        btm.add(this.webspeedCheck);
        btm.add(this.asyncCheck);
        this.add(btm, BorderLayout.PAGE_END);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        JPanel mid = new JPanel(gridbag);
        
        java.awt.Font font = new java.awt.Font(null, java.awt.Font.PLAIN, 11);
        JLabel srcLabel = new JLabel("Source");
        srcLabel.setFont(font);
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        addWithConstraints(mid, srcLabel, gridbag, c);
        
        this.srcCombo.setFont(font);
        this.srcCombo.setEditable(true);
        c.gridwidth = 3;
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        addWithConstraints(mid, this.srcCombo, gridbag, c);
        
        JLabel destLabel = new JLabel("Destination");
        destLabel.setFont(font);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        addWithConstraints(mid, destLabel, gridbag, c);
        
        this.destCombo.setFont(font);
        this.destCombo.setEditable(true);
        this.destCombo.addItem(null);
        this.destCombo.addItem("source");
        this.destCombo.addItem("setup");
        this.destCombo.addItem("delta");
        c.gridwidth = 3;
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        addWithConstraints(mid, this.destCombo, gridbag, c);
        
        this.add(mid, BorderLayout.CENTER);
    }

    /**
     * Sets all input fields to either enabled or disabled.
     * @param en If true, all fields will be enabled.
     */
    private void enableForm(boolean en) {
        this.hostCheck.setEnabled(en);
        this.webspeedCheck.setEnabled(en);
        this.asyncCheck.setEnabled(en);
        this.srcCombo.setEnabled(en);
        this.destCombo.setEnabled(en);
    }
    
    /**
     * Adds a component to a GridBagLayout with the given constraints.
     */
    private void addWithConstraints(java.awt.Container cont,
                                    java.awt.Component comp, GridBagLayout gbl,
                                    GridBagConstraints c) {
        gbl.setConstraints(comp, c);
        cont.add(comp);
    }
}