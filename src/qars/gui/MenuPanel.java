package qars.gui;

import javax.swing.*;

public class MenuPanel extends JPanel implements QAConstants {
	private static final long serialVersionUID = 6492736059474868451L;
	private ButtonGroup btnGroup;
    private javax.swing.border.Border optionBorder;
    private java.util.Vector<AbstractButton> options;
    private AbstractButton noButton;
    
    public MenuPanel(String title) {
        this.btnGroup = new ButtonGroup();
        this.optionBorder = BorderFactory.createEmptyBorder(10, 100, 1, 1);
        this.options = new java.util.Vector<AbstractButton>();
        this.noButton = new JToggleButton();
        this.noButton.setSelected(true);
        this.btnGroup.add(noButton);
        initializeGUI(title);
    }
    
    public boolean addOption(AbstractButton btn) {
        boolean success = true;
        if (options.contains(btn)) {
            success = false;
        } else {
            btnGroup.add(btn);
            btn.setBorder(optionBorder);
            this.add(btn);
        }
        return success;
    }
    
    public void clearAll() {
        this.noButton.setSelected(true);
    }
    
    private void initializeGUI(String title) {
        java.awt.Dimension minPrefSize = new java.awt.Dimension(FR_WIDTH,
                                                                CT_HEIGHT);
        this.setPreferredSize(minPrefSize);
        this.setMinimumSize(minPrefSize);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new java.awt.Font(null, java.awt.Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 75, 20, 1));
        this.add(titleLabel);
    }
}