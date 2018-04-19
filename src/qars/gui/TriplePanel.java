package qars.gui;

import java.awt.Dimension;
import javax.swing.*;

public class TriplePanel extends JPanel implements QAConstants {
	private static final long serialVersionUID = 6813990392438590613L;
	public static final int TOP = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    
    private JScrollPane topPane;
    private JScrollPane leftPane;
    private JScrollPane rightPane;
    
    public TriplePanel() {
        topPane = new JScrollPane();
        leftPane = new JScrollPane();
        rightPane = new JScrollPane();
        initializeGUI();
    }
    
    public boolean setView(java.awt.Component component, int pane) {
        boolean success = true;
        switch (pane) {
            case TOP: topPane.setViewportView(component); break;
            case LEFT: leftPane.setViewportView(component); break;
            case RIGHT: rightPane.setViewportView(component); break;
            default: success = false;
        }
        return success;
    }
    
    private void initializeGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        Dimension minPrefSize = new Dimension(FR_WIDTH, CT_HEIGHT);
        this.setMinimumSize(minPrefSize);
        this.setPreferredSize(minPrefSize);
        
        JSplitPane outerPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        outerPane.setDividerSize(DIV_SIZE);
        outerPane.setResizeWeight(TP_RATIO);
        JSplitPane innerPane = new JSplitPane();
        innerPane.setDividerSize(DIV_SIZE);
        innerPane.setResizeWeight(LP_RATIO);
        outerPane.setBottomComponent(innerPane);
        
        int topHeight = (int) ((CT_HEIGHT - DIV_SIZE) * TP_RATIO);
        minPrefSize = new Dimension(FR_WIDTH, topHeight);
        topPane.setMinimumSize(minPrefSize);
        topPane.setPreferredSize(minPrefSize);
        outerPane.setTopComponent(topPane);
        
        int leftWidth = (int) ((FR_WIDTH - DIV_SIZE) * LP_RATIO);
        int bottomHeight = (CT_HEIGHT - DIV_SIZE) - topHeight;
        minPrefSize = new Dimension(leftWidth, bottomHeight);
        leftPane.setMinimumSize(minPrefSize);
        leftPane.setPreferredSize(minPrefSize);
        innerPane.setLeftComponent(leftPane);
        
        int rightWidth = FR_WIDTH - DIV_SIZE - leftWidth;
        minPrefSize = new Dimension(rightWidth, bottomHeight);
        rightPane.setMinimumSize(minPrefSize);
        rightPane.setPreferredSize(minPrefSize);
        innerPane.setRightComponent(rightPane);
        
        this.add(outerPane);
    }
}