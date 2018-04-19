package qars.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class QarsFrame extends JFrame implements QAConstants {
	private static final long serialVersionUID = 4272217536613993661L;
	private final int MINWIDTH = FR_WIDTH + FR_BORDER + FR_BORDER + 10;
    private final int MINHEIGHT = FR_HEIGHT + FR_TITLE + FR_BORDER;
    private JPanel toolbarPanel;
    private java.util.Vector<AbstractButton> buttons;
    private JViewport contentView;
    private JLabel statusBar;
    
    public QarsFrame(String title, String user) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.buttons = new java.util.Vector<AbstractButton>();
        initializeGUI(user);
    }
    
    public String display(String text) {
        String old = statusBar.getText();
        statusBar.setText(text);
        return old;
    }
    
    public void setView(Component view) {
        contentView.setView(view);
    }
    
    public boolean addButton(AbstractButton btn) {
        boolean success = true;
        if (btn != null) {
            if (buttons.contains(btn)) {
                success = false;
            } else {
                buttons.add(btn);
                toolbarPanel.add(btn);
            }
        } else {
            toolbarPanel.add(new JSeparator(SwingConstants.VERTICAL));
        }
        return success;
    }
    
    public void removeButtons() {
        toolbarPanel.removeAll();
        buttons.removeAllElements();
    }
    
    private void initializeGUI(String user) {
        Dimension minPrefSize = new Dimension(MINWIDTH, MINHEIGHT);
        this.setMinimumSize(minPrefSize);
        this.setPreferredSize(minPrefSize);
        
        JPanel cp = new JPanel(new BorderLayout());
        
        toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        toolbarPanel.setMaximumSize(new Dimension(MAX_DIM, TB_HEIGHT));
        minPrefSize = new Dimension(FR_WIDTH, TB_HEIGHT);
        toolbarPanel.setMinimumSize(minPrefSize);
        toolbarPanel.setPreferredSize(minPrefSize);
        cp.add(toolbarPanel, BorderLayout.PAGE_START);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));
        contentPanel.setBorder(BorderFactory.createEtchedBorder());
        minPrefSize = new Dimension(FR_WIDTH, CT_HEIGHT);
        contentPanel.setMinimumSize(minPrefSize);
        contentPanel.setPreferredSize(minPrefSize);
        contentView = new JViewport();
        contentPanel.add(contentView);
        contentView.setOpaque(false);
        cp.add(contentPanel, BorderLayout.CENTER);
        
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(Color.white);
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setMaximumSize(new Dimension(MAX_DIM, SB_HEIGHT));
        minPrefSize = new Dimension(FR_WIDTH, SB_HEIGHT);
        statusPanel.setMinimumSize(minPrefSize);
        statusPanel.setPreferredSize(minPrefSize);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
        statusBar = new JLabel("Hello " + user);
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        statusBar.setFont(new Font(null, Font.PLAIN, 10));
        statusPanel.add(statusBar);
        cp.add(statusPanel, BorderLayout.PAGE_END);
        
        this.setContentPane(cp);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                QarsFrame qf = new QarsFrame("QARS", "");
                qf.pack();
                qf.setVisible(true);
            }
        });
    }
}