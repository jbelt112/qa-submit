package qars.gui;

import java.awt.Dimension;
import javax.swing.*;

public class TabHolderPanel extends JTabbedPane implements QAConstants {
    
	private static final long serialVersionUID = 8410815376755441886L;

	public TabHolderPanel() {
        initializeGUI();
    }
    
    public void addComponent(java.awt.Component component) {
        this.add(component);
    }
    
    private void initializeGUI() {
        int height = (int) ((CT_HEIGHT - DIV_SIZE) * TP_RATIO) - 6;
        Dimension minPrefSize = new Dimension(FR_WIDTH - 3, height);
        this.setMinimumSize(minPrefSize);
        this.setPreferredSize(minPrefSize);
    }
}