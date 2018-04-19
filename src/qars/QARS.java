package qars;

import java.awt.event.*;

import java.util.ArrayList;
import javax.swing.*;
import qars.gui.*;
import qars.net.*;
import qars.util.*;
import javax.swing.event.*;

/**
 * QARS is the controller for the QA Automated Release System application.
 * 
 * @author Jaren Belt
 */
public class QARS implements ActionListener, ChangeListener {
    // private instance variables
    private QarsFrame appWindow;
    private java.util.ArrayList<TabPanel> filePanels;
    private MouseOverHintManager mohm;
    private MenuPanel mp;
    private String user;
    private ReleaseClient rClient;         // performs communication with server
    private TabHolderPanel thp;
    private FileInfoPanel fip;
    private OverviewPanel op;
    private RequestViewPanel rvp;
    private ItemViewPanel ivp;
    private SCR currentSCR;
    private int userSecurity;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new qarsApplication object with options suited to the user's
     * security level. Sets up a connection with the ReleaseServer and displays
     * the menu options.
     */
    public QARS() throws java.io.IOException {
        String user = System.getProperty("user.name");
        if (user == null) {
            user = JOptionPane.showInputDialog("Enter your Windows user id:");
        }
        try {
            this.rClient = new ReleaseClient(ReleaseServer.PORT, user.toLowerCase());
        } catch (java.net.ConnectException ce) {
            JOptionPane.showMessageDialog(null, "Server is not running! " +
                                          "Please notify the release " +
                                          "coordinator.");
            System.exit(1);
        }
        this.user = this.rClient.getUnixName();
        if (this.user.length() == 0) {
            JOptionPane.showMessageDialog(null, "Could not validate user");
            throw new java.io.IOException("Validation not successful");
        }
        this.appWindow = new QarsFrame("QA Automated Release System", this.user);
        this.mohm = new MouseOverHintManager(appWindow);
        this.userSecurity = this.rClient.getSecurity();
        this.mp = createMenu(this.userSecurity);
        this.thp = null;
        this.currentSCR = null;
        this.appWindow.setView(this.mp);
        this.appWindow.pack();
        this.appWindow.setLocationRelativeTo(null);
        this.appWindow.setVisible(true);
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Captures asynchronous events triggered by buttons.
     * @param ae Triggered event.
     */
    public void actionPerformed(java.awt.event.ActionEvent ae) {
        String cmd = ae.getActionCommand();
        
        if (cmd.equals("M0")) {   // Create New Request
            createRequest();
        } else if (cmd.equals("M1")) {   // Cancel Request
            viewRequests(1);
            JButton dltButton = new JButton("DELETE");
            dltButton.setActionCommand("Delete Request");
            dltButton.addActionListener(this);
            this.mohm.addHintFor(dltButton, "Cancel the selected request");
            this.appWindow.addButton(null);
            this.appWindow.addButton(dltButton);
        } else if (cmd.equals("M4")) {   // Remove SCR
            SCR[] list = getSCRList();
            SelectDialog sd = new SelectDialog(this.appWindow, 
                "Select a Project ID to delete", "Select an SCR to delete.");
            // populate drop down list with current SCRs
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    sd.addItem(list[i]);
                }
            }
            // show the dialog and wait for user to select
            sd.showDialog();
            int sdval = sd.getButton();
            if (sdval == SelectDialog.OK_BUTTON) {
                Object selItem = sd.getSelectedItem();
                if (selItem instanceof SCR) {
                    SCR s = (SCR) selItem;
                    String scrnum = (String) s.query(SCR.IR);
                    int chose = JOptionPane.showConfirmDialog(this.appWindow,
                        "Are you sure you want to\npermanently remove " +
                        scrnum + "?");
                    if (chose == JOptionPane.YES_OPTION) {
                        ReturnCode action = 
                            new ReturnCode(ReleaseServer.DELETE);
                        try {
                            this.rClient.send(action, s);
                        } catch (java.io.IOException ioe) {
                            System.err.println("Could not delete " + s);
                        }
                    }
                }
            }
            resetToMenu();
        } else if (cmd.equals("M2")) {   // Schedule Transfer
            viewRequests(2);
            JButton schdButton = new JButton("SUBMIT");
            schdButton.setActionCommand("Schedule Transfer");
            schdButton.addActionListener(this);
            this.mohm.addHintFor(schdButton, "Schedule the selected requests " +
                                 "to be transferred to QA");
            this.appWindow.addButton(null);
            this.appWindow.addButton(schdButton);
        } else if (cmd.equals("M3")) {   // View Pending Requests
            viewRequests(3);
        } else if (cmd.equals("TabPanel Add")) {
            addFiles();
        } else if (cmd.equals("TabPanel Remove")) {
            qars.util.File f = this.fip.getSelectedFile();
            if (f != null) {
                int response = JOptionPane.showConfirmDialog(this.appWindow,
                    "Are you sure you want to remove " + f + "?");
                if (response == JOptionPane.YES_OPTION) {
                    TabPanel tabP = (TabPanel) this.thp.getSelectedComponent();
                    DisplayableTableModel mod = 
                        (DisplayableTableModel) tabP.getTableModel();
                    this.fip.clear();
                    Boolean inc = (Boolean) f.query(qars.util.File.IN);
                    if (((String) f.query(qars.util.File.FN)).endsWith(".i") ||
                        ((String) f.query(qars.util.File.FN)).endsWith(".f")) {
                        qars.util.File[] ifiles = f.getImpactList();
                        String impact = SCR.getCategory(SCR.IMPACT);
                        int index = this.thp.indexOfTab(impact);
                        tabP = (TabPanel) this.thp.getComponentAt(index);
                        DisplayableTableModel mod2 =
                            (DisplayableTableModel) tabP.getTableModel();
                        //if (inc.booleanValue() && ifiles != null) {
                        if (ifiles != null) {
                            for (int i = 0; i < ifiles.length; i++) {
                                System.out.println("Checking to delete " + ifiles[i]);
                                qars.util.File iFile = (qars.util.File) mod2.get(ifiles[i]);
                                if (iFile != null) {
                                    if (inc.booleanValue()) {
                                        iFile.reference(-1);
                                    }
                                    iFile.removeParent(f);
                                    if (iFile.getNumParents() == 0) {
                                        int dex = mod2.indexOf(ifiles[i]);
                                        //mod2.setValueAt(new Boolean(false), dex, 0);
                                        mod2.remove(mod2.get(dex), inc.booleanValue());
                                    }
                                }
                            }
                        }
                    }
                    mod.remove(f, inc.booleanValue());
                    if (!inc.booleanValue()) {
                        this.thp.repaint();
                    }
                }
            }
        } else if (cmd.equals("Save Request")) {
            buildSCR();
            ReturnCode action = new ReturnCode(ReleaseServer.ADD);
            try {
                this.rClient.send(action, this.currentSCR);
            } catch (java.io.IOException ioe) {
                if (action.getCode() == ReleaseServer.SHUTDOWN) {
                    notifyServerShutdown(0);
                    System.exit(1);
                }
                System.err.println("SCR not saved!");
            }
            resetToMenu();
        } else if (cmd.equals("Submit Request")) {
            if (this.op.getTotal() > 0) {
                buildSCR();
                ReturnCode action = new ReturnCode(ReleaseServer.ADD);
                Request req = new Request(this.currentSCR, this.user, -1);
                try {
                    this.rClient.send(action, req);
                } catch (java.io.IOException ioe) {
                    System.err.println("Request not saved!");
                    if (action.getCode() == ReleaseServer.SHUTDOWN) {
                        notifyServerShutdown(0);
                        System.exit(1);
                    }
                }
                SCR clonedSCR = null;
                if (action.getCode() == ReleaseServer.SUCCESS) {
                    this.currentSCR.clear();
                    clonedSCR = (SCR) this.currentSCR.clone();
                } else {
                    clonedSCR = this.currentSCR;
                }
                action.setCode(ReleaseServer.ADD);
                try {
                    this.rClient.send(action, clonedSCR);
                } catch (java.io.IOException ioe) {
                    System.err.println("SCR not saved!");
                    if (action.getCode() == ReleaseServer.SHUTDOWN) {
                        notifyServerShutdown(0);
                        System.exit(1);
                    }
                }
                resetToMenu();
            } else {
                JOptionPane.showMessageDialog(this.appWindow, "You must " +
                    "include at least one file to submit a request");
            }
        } else if (cmd.equals("Delete Request")) {
            Request r = this.rvp.getRequest();
            if (r != null) {
                int sel = JOptionPane.showConfirmDialog(this.appWindow,
                    "Are you sure you want to delete request #" + r + "?",
                    "Please confirm", JOptionPane.YES_NO_OPTION);
                if (sel == JOptionPane.YES_OPTION) {
                    ReturnCode action = new ReturnCode(ReleaseServer.DELETE);
                    try {
                        this.rClient.send(action, r);
                    } catch (java.io.IOException ioe) {
                        System.err.println("Request not deleted!");
                        if (action.getCode() == ReleaseServer.SHUTDOWN) {
                            notifyServerShutdown(0);
                            System.exit(1);
                        }
                    }
                    if (action.getCode() == ReleaseServer.SUCCESS) {
                        DisplayableTableModel mod = 
                            (DisplayableTableModel) this.ivp.getTableModel();
                        mod.remove(r);
                        this.rvp.updateRequest(null);
                    }
                }
            }
        } else if (cmd.equals("M5")) {   // Exit
            try {
                this.rClient.send(new ReturnCode(ReleaseServer.SHUTDOWN), null);
            } catch (java.io.IOException ioe) {
            }
            this.appWindow.setVisible(false);
            this.appWindow.dispose();
            System.exit(0);
        } else if (cmd.equals("ReturnMain")) {
            resetToMenu();
        } else if (cmd.equals("Schedule Transfer")) {
            DisplayableTableModel mod = 
                (DisplayableTableModel) this.ivp.getTableModel();
            ArrayList<Request> list = new ArrayList<Request>();
            for (int i = 0; i < mod.getRowCount(); i++) {
                Request r = (Request) mod.get(i);
                if (((Boolean) r.query(Request.IN)).booleanValue()) {
                    list.add(r);
                }
            }
            if (list.size() > 0) {
                ReturnCode action = new ReturnCode(ReleaseServer.TRANSFER);
                try {
                    this.rClient.send(action, list);
                } catch (java.io.IOException ioe) {
                    if (action.getCode() == ReleaseServer.SHUTDOWN) {
                        notifyServerShutdown(0);
                        System.exit(1);
                    }
                }
                resetToMenu();
            } else {
                JOptionPane.showMessageDialog(this.appWindow, "You must " +
                    "select at least one request to schedule a transfer");
            }
        }
    }
    
    /**
     * Captures change events.
     * @param ce Triggered event.
     */
    public void stateChanged(ChangeEvent ce) {
        if (this.filePanels != null) {
            this.fip.clear();
            for (int i = 0; i < filePanels.size(); i++) {
                ((TabPanel) this.filePanels.get(i)).clearSelection();
            }
        }
    }

    // private methods ----------------------------------------------------
    
    private void resetToMenu() {
        this.mp.clearAll();
        this.appWindow.removeButtons();
        this.appWindow.setView(this.mp);
        this.appWindow.repaint();
        this.currentSCR = null;
        this.mohm.clear();
    }
    
    private boolean notifyServerShutdown(int severity) {
        boolean saved = false;
        String message = "The connection to the server has been severed!";
        if (severity == 0) {
            message += "\nThe application will now shut down.";
        }
        message += "\n\nIf you get this message again, please notify\n" +
            "the release coordinator.";
        JOptionPane.showMessageDialog(this.appWindow, message, "Server Error", 
                                      JOptionPane.ERROR_MESSAGE);
        return saved;
    }

    private void buildSCR() {
        for (int i = 0; i < this.filePanels.size(); i++) {
            TabPanel tabP = this.filePanels.get(i);
            DisplayableTableModel mod =
                (DisplayableTableModel) tabP.getTableModel();
            int fileType = tabP.getFileType();
            // clear existing
            qars.util.File[] existFiles = new qars.util.File[this.currentSCR.getContents(fileType).size()];
            this.currentSCR.getContents(fileType).toArray(existFiles);
            for (qars.util.File f : existFiles) {
            	this.currentSCR.remove(f, fileType);
            }
            for (int j = 0; j < mod.getRowCount(); j++) {
                qars.util.File f = (qars.util.File) mod.get(j);
                this.currentSCR.add(f, fileType);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private SCR[] getSCRList() {
        Object scrs = null;
        ReturnCode retcode = new ReturnCode(ReleaseServer.SCRS);
        try {
            scrs = this.rClient.send(retcode, scrs);
        } catch (java.io.IOException ioe) {
            if (retcode.getCode() == ReleaseServer.SHUTDOWN) {
                notifyServerShutdown(0);
                System.exit(1);
            }
        }
        SCR[] retVal = new SCR[1];
        if (scrs != null && scrs instanceof ArrayList) {
            ArrayList<SCR> list = (ArrayList<SCR>) scrs;
            java.util.Collections.sort(list);
            retVal = list.toArray(retVal);
        }
        return retVal;
    }
    
    @SuppressWarnings("unchecked")
    private Request[] getRequestList() {
        Object requests = null;
        ReturnCode retcode = new ReturnCode(ReleaseServer.RQSTS);
        try {
            requests = this.rClient.send(retcode, requests);
        } catch (java.io.IOException ioe) {
            if (retcode.getCode() == ReleaseServer.SHUTDOWN) {
                notifyServerShutdown(0);
                System.exit(1);
            }
        }
        Request[] retVal = new Request[1];
        if (requests != null && requests instanceof ArrayList) {
            ArrayList<Request> list = (ArrayList<Request>) requests;
            java.util.Collections.sort(list);
            retVal = list.toArray(retVal);
        }
        return retVal;
    }
    
    private void addFiles() {
        TabPanel tabP = (TabPanel) this.thp.getSelectedComponent();
        String name = tabP.getName();
        int fType = tabP.getFileType();
        String currentDir = SCR.getSrc(fType);
        switch (fType) {
            case SCR.SETUP:
            case SCR.UNDO:
            case SCR.DATA:
                String cDir = currentDir + "/" + this.currentSCR.query(SCR.IR);
                ReturnCode code = new ReturnCode(ReleaseServer.EXISTS);
                Object o = null;
                try {
                    o = this.rClient.send(code, cDir);
                } catch (java.io.IOException ioe) {
                }
                if (o != null && o instanceof Boolean) {
                    if (((Boolean) o).booleanValue()) {
                        currentDir = cDir;
                    }
                }
                break;
        }
        String destDir = SCR.getDest(tabP.getFileType());
        RemoteFileSystemView rview = new RemoteFileSystemView(currentDir,
                                                              this.rClient);
        JFileChooser chooser = new JFileChooser(rview);
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Select file(s) to add to the " + name + 
                               " category");
        int returnVal = chooser.showDialog(this.appWindow, "ADD");
        this.appWindow.repaint();
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File[] selFiles = chooser.getSelectedFiles();
            DisplayableTableModel mod = 
                (DisplayableTableModel) tabP.getTableModel();
            Boolean yesMan = new Boolean(true);
            for (int i = 0; i < selFiles.length; i++) {
                qars.io.RemoteFile f = (qars.io.RemoteFile) selFiles[i];
                if (fType == SCR.SOURCE) {
                    if (!currentDir.equalsIgnoreCase(f.getParent())) {
                        //System.out.println(f.getParent());
                        JOptionPane.showMessageDialog(this.appWindow,
                            "Source files must be in " + currentDir + ".\n" +
                            "Please copy your source files to the required\n" +
                            "directory before continuing.");
                        break;
                    }
                }
                qars.util.File qfile = new qars.util.File(f.getName(), fType);
                if (fType == SCR.SOURCE && (f.getName().endsWith(".i") ||
                                            f.getName().endsWith(".f"))) {
                    this.appWindow.display("Looking up impacted files. " +
                                           "Please wait...");
                    String impact = SCR.getCategory(SCR.IMPACT);
                    int index = this.thp.indexOfTab(impact);
                    tabP = (TabPanel) this.thp.getComponentAt(index);
                    DisplayableTableModel mod2 =
                            (DisplayableTableModel) tabP.getTableModel();
                    int fType2 = tabP.getFileType();
                    try {
                        ReturnCode code = new ReturnCode(ReleaseServer.IMPACT);
                        Object o = this.rClient.send(code, f.getName());
                        if (o != null && o instanceof ArrayList) {
                            @SuppressWarnings("rawtypes")
							ArrayList iFiles = (ArrayList) o;
                            String adding = "The following files were found " +
                                "to be impacted\nby " + f.getName() + ":\n";
                            for (int j = 0; j < iFiles.size(); j++) {
                                o = iFiles.get(j);
                                if (o instanceof String) {
                                    if (j < 11) {
                                        adding += "   " + (String) o + "\n";
                                    } else if (j == 11) {
                                        adding += "   + " + (iFiles.size() - 11) + " others";
                                    }
                                    qars.util.File ifile = 
                                        new qars.util.File((String) o, fType2);
                                    ifile.setValue(qars.util.File.SC,
                                                   SCR.getCategory(fType2));
                                    ifile.setValue(qars.util.File.IN, yesMan);
                                    ifile.setValue(qars.util.File.RF,
                                                   new Integer(1));
                                    qars.util.File pFile =
                                        (qars.util.File) mod2.get(ifile);
                                    if (pFile != null) {
                                        pFile.reference(1);
                                        qfile.addImpact(pFile);
                                        pFile.addParent(qfile);
                                    } else {
                                        ifile.addParent(qfile);
                                        mod2.add(ifile);
                                        qfile.addImpact(ifile);
                                    }
                                }
                            }
                            JOptionPane.showMessageDialog(this.appWindow,
                                                          adding);
                        }
                    } catch (java.io.IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                qfile.setValue(qars.util.File.SC, f.getParent());
                qfile.setValue(qars.util.File.DN, destDir);
                qfile.setValue(qars.util.File.IN, yesMan);
                qfile.setValue(qars.util.File.SZ, new Long(f.length()));
                mod.add(qfile);
            }
        }
    }
    
    private void viewRequests(int type) {
        Request[] requests = getRequestList();
        DualPanel dp = new DualPanel();
        this.rvp = new RequestViewPanel();
        this.ivp = new ItemViewPanel(this.rvp, dp);
        dp.setView(this.ivp, DualPanel.TOP);
        dp.setView(this.rvp, DualPanel.BOTTOM);
        
        String[] colnoinc = {Request.TK, Request.IR, Request.RQ};
        String[] colinc = {Request.IN, Request.TK, Request.IR, Request.RQ};
        String[] columns = (type == 2 ? colinc : colnoinc);
        DisplayableTableModel mod =
            new DisplayableTableModel(null, columns, 0);
        mod.addTableModelListener(this.ivp);
        this.ivp.setTableModel(mod);
        for (int i = 0; i < columns.length; i++) {
            int width = 0;
            if (columns[i].equals(Request.TK) || columns[i].equals(Request.RQ)){
                width = 75;
            } else if (columns[i].equals(Request.IR)) {
                width = 0;
            } else if (columns[i].equals(Request.IN)) {
                width = 50;
            }
            javax.swing.table.TableColumn col = ivp.getColumn(i);
            if (width > 0 && col != null) {
                col.setMaxWidth(width);
            }
        }
        for (int i = 0; i < requests.length; i++) {
            if (requests[i] != null) {
                String requestor = (String) requests[i].query(Request.RQ);
                if (this.userSecurity == ReleaseServer.DVLP) {
                    if (requestor.equals(this.user)) {
                        mod.add(requests[i]);
                    }
                } else {
                    mod.add(requests[i]);
                }
            }
        }
        
        JButton rtrn = new JButton("RETURN");
        rtrn.setActionCommand("ReturnMain");
        rtrn.addActionListener(this);
        this.mohm.addHintFor(rtrn, "Return to the main menu");
        this.appWindow.addButton(rtrn);
        
        this.appWindow.setView(dp);
        this.appWindow.repaint();
    }
    
    private void createRequest() {
        this.filePanels = new java.util.ArrayList<TabPanel>();
        boolean yes_no = false;    // signifies end of SCR selection
        SCR entry = null;
        while (!yes_no) {
            // prompt for SCR number
            SelectDialog sd = new SelectDialog(this.appWindow, 
                "Select a Project ID", "Choose from the list below or type a " +
                "new Project ID.\n\rThe Project ID should be the same as its " +
                "corresponding /g1/dev/spec directory name.");
            // populate drop down list with current SCRs
            SCR[] scrs = getSCRList();
            if (scrs != null) {
                //System.out.println(scrs.length);
                for (int i = 0; i < scrs.length; i++) {
                    if (scrs[i] != null) {
                        sd.addItem(scrs[i]);
                    }
                }
            }
            // show the dialog and wait for user to select
            sd.showDialog();
            int sdval = sd.getButton();
            if (sdval == SelectDialog.OK_BUTTON) {
                Object selItem = sd.getSelectedItem();
                // check to see if user clicked an existing SCR
                if (selItem instanceof SCR) {
                    entry = (SCR) selItem;
                    yes_no = true;
                // or entered a new one
                } else if (selItem instanceof String) {
                    // make sure they didn't type in an existing scr
                    String sSelItem = (String) selItem;
                    for (int i = 0; i < scrs.length; i++) {
                        if (scrs[i] != null && sSelItem.equalsIgnoreCase(scrs[i].toString())) {
                            entry = scrs[i];
                            yes_no = true;
                        }
                    }
                    if (entry == null) {
                        entry = new SCR(sSelItem, this.user);
                    
                        // check to see if there is a spec directory for it
                        boolean dirExists = false;
                        String dir = SCR.getSrc(SCR.SETUP) + "/" + sSelItem;
                        try {
                            ReturnCode code = 
                                new ReturnCode(ReleaseServer.EXISTS);
                            Object o = this.rClient.send(code, dir);
                            if (o != null && o instanceof Boolean) {
                                dirExists = ((Boolean) o).booleanValue();
                            }
                        } catch (java.io.IOException ioe) {
                        }
                        if (!dirExists) {
                            int sel = JOptionPane.showConfirmDialog(this.appWindow,
                                "The directory " + dir + " does not exist." +
                                "\n\nPlease create it if necessary before " +
                                "continuing. Continue?");
                            if (sel == JOptionPane.YES_OPTION) {
                                yes_no = true;
                            } else {
                                yes_no = false;
                            }
                        } else {
                            yes_no = true;
                        }
                    }
                }
            } else if (sdval == SelectDialog.CANCEL_BUTTON) {
                yes_no = true;
                entry = null;
            }
        }
        
        if (entry != null) {
            this.currentSCR = entry;
            
            // create window components
            TriplePanel tp = new TriplePanel();
            this.fip = new FileInfoPanel(this.mohm);
            tp.setView(fip, TriplePanel.LEFT);
            this.op = new OverviewPanel("Overview");
            tp.setView(this.op, TriplePanel.RIGHT);
            this.mohm.addHintFor(this.op, "Displays number of each type of " +
                                 "file to be copied to QA");
            this.thp = new TabHolderPanel();
            this.thp.addChangeListener(this);
            tp.setView(thp, TriplePanel.TOP);
            JButton rtrn = new JButton("RETURN");
            rtrn.setActionCommand("ReturnMain");
            rtrn.addActionListener(this);
            this.mohm.addHintFor(rtrn, "Return to the main menu, all changes " +
                                 "will be lost");
            this.appWindow.addButton(rtrn);
            this.appWindow.addButton(null);
            JButton saveBtn = new JButton("SAVE");
            saveBtn.addActionListener(this);
            saveBtn.setActionCommand("Save Request");
            this.mohm.addHintFor(saveBtn, "Save progress without submitting " +
                                 "a transfer request");
            this.appWindow.addButton(saveBtn);
            JButton submitBtn = new JButton("SUBMIT");
            submitBtn.addActionListener(this);
            submitBtn.setActionCommand("Submit Request");
            this.mohm.addHintFor(submitBtn, "Save and submit transfer request");
            this.appWindow.addButton(submitBtn);
            
            // add each of the categories
            int[] cats = {SCR.SOURCE, SCR.SETUP, SCR.DELTA, SCR.UNDO, SCR.DATA,
                SCR.IMPACT};
            DisplayableTableModel srcModl = null;
            String[] colnorun = {qars.util.File.IN, qars.util.File.FN};
            String[] colrun = {qars.util.File.IN, qars.util.File.FN,
                qars.util.File.RN};
            for (int i = 0; i < cats.length; i++) {
                boolean runAble = (cats[i] == SCR.SETUP || cats[i] == SCR.UNDO);
                TabPanel tabP = new TabPanel(entry.toString(), this, fip,
                                             cats[i], this.mohm);
                tabP.setName(SCR.getCategory(cats[i]));
                this.filePanels.add(tabP);
                
                String[] columns = (runAble ? colrun : colnorun);
                DisplayableTableModel mod =
                    new DisplayableTableModel(null, columns, 0);
                if (i == SCR.SOURCE) {
                    srcModl = mod;
                }
                if (i == SCR.IMPACT) {
                    mod.setEditable(false);
                    srcModl.setChildModel(mod);
                }
                mod.addTableModelListener(tabP);
                mod.addTableModelListener(this.op);
                tabP.setTableModel(mod);
                op.addItem(SCR.getCategory(cats[i]), 0, mod);
                javax.swing.table.TableColumn col = tabP.getColumn(0);
                if (col != null) {
                    col.setMaxWidth(50);
                }
                if (runAble) {
                    col = tabP.getColumn(2);
                    if (col != null) {
                        col.setMaxWidth(50);
                    }
                }
                thp.addComponent(tabP);
                ArrayList<qars.util.File> files = 
                    this.currentSCR.getContents(cats[i]);
                if (files != null) {
                    for (int j = 0; j < files.size(); j++) {
                        mod.add(files.get(j));
                    }
                }
            }
            this.appWindow.setView(tp);
            this.appWindow.repaint();
        } else {
            this.mp.clearAll();
        }
    }
    
    private MenuPanel createMenu(int userLevel) {
        MenuPanel mp = new MenuPanel("Main Menu");
        JRadioButton b = null;
        String[][] options = {
            {"Create New Request", 
                "Enter a new request for transferring items to QA", "M0"},
            {"Cancel Request",
                "Cancel a pending request", "M1"},
            {"Schedule Transfer",
                "Select requests to process for transfer to QA", "M2"},
            {"View Pending Requests",
                "View requests waiting to be approved", "M3"},
            {"Remove SCR", "Remove an SCR from the list", "M4"},
            {"Exit", "Close this window", "M5"}
        };
        int[] allowedOptions = {ReleaseServer.DVLP, ReleaseServer.DVLP, 
            ReleaseServer.QAAS, ReleaseServer.QAAS, ReleaseServer.ADMN, 
            ReleaseServer.DVLP};
        
        for (int i = 0; i < allowedOptions.length; i++) {
            if (allowedOptions[i] <= userLevel) {
                b = makeButton(options[i][0], options[i][1], options[i][2]);
                mp.addOption(b);
            }
        }
        
        return mp;
    }
    
    private JRadioButton makeButton(String label, String hint, String ac) {
        JRadioButton btn = new JRadioButton(label);
        btn.setActionCommand(ac);
        btn.addActionListener(this);
        this.mohm.addHintFor(btn, hint, true);
        return btn;
    }
    
    // main ---------------------------------------------------------------
    
    public static void main(String[] args) {
        try {
            @SuppressWarnings("unused")
			QARS qft = new QARS();
        } catch (java.io.IOException ioe) {
            System.err.println(ioe);
        }
    }
}
