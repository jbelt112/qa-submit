/* qarsConstants.java */
package qars.gui;

/**
 * <p>This interface provides public static constants primarily for determining
 * dimensions for various gui components.</p>
 * 
 * @author Jaren Belt
 */
public interface QAConstants {
    public static final int FR_WIDTH = 400;     // frame width
    public static final int FR_HEIGHT = 500;    // frame height
    public static final int FR_BORDER = 4;      // pixels for frame border
    public static final int FR_TITLE = 30;      // pixels for title bar
    public static final int TB_HEIGHT = 39;     // toolbar height
    public static final int SB_HEIGHT = 25;     // status bar height
    // remainder is the content height
    public static final int CT_HEIGHT = FR_HEIGHT - TB_HEIGHT - SB_HEIGHT;
    
    public static final int MAX_DIM = 32000;    // arbitrary large number
    public static final int DIV_SIZE = 5;       // split pane divider size
    
    // TriplePanel
    public static final double TP_RATIO = 0.58; // top panel %
    public static final double LP_RATIO = 0.7;  // left panel %
    
    // FileInfoPanel
    public static final int FIP_TOP = 34;       // panel for file name
    
    // SelectDialog
    public static final int SD_ROWS = 5;
    public static final int SD_COLS = 30;
}