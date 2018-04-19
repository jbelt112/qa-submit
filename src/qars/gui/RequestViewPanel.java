package qars.gui;

import javax.swing.*;
import qars.util.*;

public class RequestViewPanel extends JPanel implements QAConstants {
	private static final long serialVersionUID = -8756831144634009528L;
	private Request request;
    private JTextArea info;
    
    public RequestViewPanel() {
        this.request = null;
        this.info = new JTextArea();
        initializeGUI();
    }
    
    public Request getRequest() {
        return this.request;
    }
    
    public void updateRequest(Displayable therequest) {
        if (therequest != null && therequest instanceof Request) {
            this.request = (Request) therequest;
            this.info.setText(this.request.toStringAllInfo());
        } else {
            this.info.setText("");
            this.request = null;
        }
    }
    
    private void initializeGUI() {
        this.info.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN,
                                            12));
        this.add(this.info);
    }
}