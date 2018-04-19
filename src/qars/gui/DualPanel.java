package qars.gui;

import java.awt.Dimension;
import javax.swing.*;

/**
 * <p>A DualPanel contains two display areas that can be resized. Within each
 * area is a JScrollPane that provides the view of its gui component.</p>
 * 
 * @author Jaren Belt
 */
public class DualPanel extends JPanel implements QAConstants {
	private static final long serialVersionUID = -8611378580547785576L;
	/** Upper view. */
    public static final int TOP = 1;
    /** Lower view. */
    public static final int BOTTOM = 2;
    
    private JScrollPane topPane;
    private JScrollPane bottomPane;
    private Runnable topPaneSnap;
    private Runnable bottomPaneSnap;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new DualPanel with empty panels.
     */
    public DualPanel() {
        this.topPane = new JScrollPane();
        this.bottomPane = new JScrollPane();
        this.topPaneSnap = new Runnable() {
            public void run() {
                topPane.getHorizontalScrollBar().setValue(0);
                topPane.getVerticalScrollBar().setValue(0);
            }
        };
        this.bottomPaneSnap = new Runnable() {
            public void run() {
                bottomPane.getHorizontalScrollBar().setValue(0);
                bottomPane.getVerticalScrollBar().setValue(0);
            }
        };
        initializeGUI();
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Sets the component that will be visible through one of the viewports of
     * the JScrollPane.
     * @param component The component to display.
     * @param pane Either TOP or BOTTOM.
     * @return true if pane is valid.
     */
    public boolean setView(java.awt.Component component, int pane) {
        boolean success = true;
        switch (pane) {
            case TOP: 
                this.topPane.setViewportView(component); 
                break;
            case BOTTOM: 
                this.bottomPane.setViewportView(component); 
                break;
            default: success = false;
        }
        return success;
    }
    
    /**
     * Resets the scrollbar to the upper left corner.
     * @param pane Either TOP or BOTTOM.
     */
    public void resetScroll(int pane) {
        switch (pane) {
            case TOP:
                SwingUtilities.invokeLater(this.topPaneSnap);
                break;
            case BOTTOM:
                SwingUtilities.invokeLater(this.bottomPaneSnap);
                break;
        }
    }
    
    // private methods ----------------------------------------------------
    
    /**
     * Sets dimensions of various components according to qarsConstants.
     */
    private void initializeGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        Dimension minPrefSize = new Dimension(FR_WIDTH, CT_HEIGHT);
        this.setMinimumSize(minPrefSize);
        this.setPreferredSize(minPrefSize);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(DIV_SIZE);
        splitPane.setResizeWeight(1 - TP_RATIO);
        
        int topHeight = (int) ((CT_HEIGHT - DIV_SIZE) * (1 - TP_RATIO));
        minPrefSize = new Dimension(FR_WIDTH, topHeight);
        this.topPane.setPreferredSize(minPrefSize);
        splitPane.setTopComponent(this.topPane);
        
        int bottomHeight = (CT_HEIGHT - DIV_SIZE) - topHeight;
        minPrefSize = new Dimension(FR_WIDTH, bottomHeight);
        this.bottomPane.setPreferredSize(minPrefSize);
        splitPane.setBottomComponent(this.bottomPane);
        
        this.add(splitPane);
    }
}