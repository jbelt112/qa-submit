package qars.net;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import qars.util.*;

/**
 * The ReleaseServer provides synchronized methods for obtaining information
 * about the QA release. This allows several users to be connected to the QA
 * Release System (QARS) at the same time.
 * 
 * @author Jaren Belt
 */
public class ReleaseServer {
    /** No access allowed */
    public static final int NOAC = -14;
    /** Developer, limited access */
    public static final int DVLP = 0;
    /** QA associate access */
    public static final int QAAS = 1;
    /** Administrator access */
    public static final int ADMN = 2;
    /** Communication port */
    public static final int PORT = 7277;
    /** Server host **/
    public static final String HOST = "dukat.as.csg.com";

    // action constants
    /** Success. */
    public static final int SUCCESS = 99;
    /** An error occurred. */
    public static final int ERROR = -2;
    /** Signifies that either the server or client is shutting down. */
    public static final int SHUTDOWN = -1;
    /** Acknowledgement, no op. */
    public static final int NO_OP = 0;
    /** Send client tracking number. */
    public static final int TRK = 1;
    /** Send SCR list. */
    public static final int SCRS = 2;
    /** Send Request list. */
    public static final int RQSTS = 3;
    /** Add the entity to its list. */
    public static final int ADD = 4;
    /** Delete the entitiy from its list. */
    public static final int DELETE = 5;
    /** Logs the user's name for the given tracking number. */
    public static final int LOGNAME = 6;
    /** Begin a transfer. */
    public static final int TRANSFER = 7;
    /** Log the supplied message to the log file. */
    public static final int LOG = 8;
    /** Check for the success of webspeed compile. */
    public static final int WEBSPEED = 9;
    /** Send email of successful release. */
    public static final int EMAIL = 98;
    /** Reload user list. */
    public static final int USER = 22;
    /** Obtain directory contents. */
    public static final int DIR = 17;
    /** Does this directory exist? */
    public static final int EXISTS = 18;
    /** Retrieve impacted files */
    public static final int IMPACT = 19;
    
    // default tracking number value if tracking id cannot be loaded
    private final int DEFTRACK = 10000;
    // name of tracking number file
    private final String TRACKFILE = System.getenv("TRACK") != null ?
        System.getenv("TRACK") : "transaction.id";
    // name of All Active SCR file
    private final String ALLACTIVE = System.getenv("ACTIVE") != null ?
        System.getenv("ACTIVE") : "qars.active";
    // name of pending Request file
    private final String PENDING = System.getenv("PENDING") != null ?
        System.getenv("PENDING") : "qars.pending";
    // name of release file
    private final String INSTRUCTIONS = System.getenv("MFFILE") != null ?
        System.getenv("MFFILE") : "qa_rel";
    // name of user file
    private final String USERS = System.getenv("USERSFILE") != null ?
        System.getenv("USERSFILE") : ".userxref";
    // address for super admin user
    private final String SUPERADMIN = System.getenv("SUPERADMIN") != null ?
        System.getenv("SUPERADMIN") : "jbelt";
    
    // instance variables
    private ServerSocket server;              // accept new client connections
    private ArrayList<ClientThread> clients;      // list of connected clients
    private int myTrack;             // tracking number for this ReleaseServer
    private int tracking;                   // tracking number for next client
    private String dataDir;                  // directory for persistent files
    private FileWriter log;                                        // log file
    private ArrayList<SCR> scrs;                 // list of all available SCRs
    private ArrayList<Request> pending;                    // pending requests
    private ArrayList<Request> transfer; // list of requests sent for transfer
    private HashMap<String, User> users;  // allowed users and security levels
    
    // constructors -------------------------------------------------------
    
    /**
     * Constructs a new ReleaseServer by grabbing an available port.
     * @param dataDir Path to directory where data files are found.
     * @param logFile Name of the log file.
     * @throws IOException If the server cannot be initialized.
     */
    public ReleaseServer(String dataDir, String logFile) throws IOException {
        Runtime.getRuntime().addShutdownHook(new RSShutdownThread(this));
        if (!dataDir.endsWith("/")) {
            dataDir += "/";
        }
        this.dataDir = dataDir;
        this.myTrack = getTracking();
        this.tracking = this.myTrack + 1;
        this.server = new ServerSocket(PORT);
        this.log = new FileWriter(this.dataDir + logFile, true);
        this.scrs = new ArrayList<SCR>();
        this.scrs = handleList(this.scrs, ALLACTIVE, false);
        this.pending = new ArrayList<Request>();
        this.pending = handleList(this.pending, PENDING, false);
        this.users = loadUsers();
        msg(this.myTrack, "Server started on port " + PORT);
        this.clients = new ArrayList<ClientThread>();
        while (true) {
            try {
                Socket s = this.server.accept();
                int tempTrack = this.tracking;
                msg(tempTrack, "New connection accepted");
                ClientThread ct = new ClientThread(this, s, this.tracking++);
                synchronized (this.clients) {
                    this.clients.add(ct);
                }
                ct.start();
            } catch (SocketTimeoutException ste) {
            }
        }
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Handles incoming requested actions from clients.
     * @param track Tracking number for client.
     * @param action Action to be performed.
     * @param o Associated object reference.
     * @return Object dependent upon action and successful completion.
     */
    @SuppressWarnings("rawtypes")
	public Object performClientAction(int track, ReturnCode action, Object o) {
        Object returnObject = null;
        int thisAction = NO_OP;
        if (action != null) {
            thisAction = action.getCode();
            action.setCode(SUCCESS);
        }
        System.err.println("performClientAction: " + thisAction);
        switch (thisAction) {
            // client is shutting down
            case SHUTDOWN:
                try {
                    remove(new ClientThread(null, null, track));
                    msg(track, "Client shut down");
                } catch (IOException ioe) {
                }
                action.setCode(SHUTDOWN);
                break;
            // client wants to know its tracking number
            case TRK:
            	try {msg(track, "Tracking number requested");}catch(IOException ioe){}
                returnObject = new Integer(track);
                break;
            // client is supplying its Windows id, convert to User if match
            case LOGNAME:
                if (o != null && o instanceof String) {
                    returnObject = buildUser(track, (String) o);
                } else {
                    action.setCode(ERROR);
                }
                break;
            // record incoming message to log file
            case LOG:
            	try {msg(track, "Log update requested");}catch(IOException ioe){}
                if (o != null && o instanceof String) {
                    try {
                        msg(track, (String) o);
                    } catch (IOException ioe) {
                    }
                } else {
                    action.setCode(ERROR);
                }
                break;
            // qars client checking if webspeed compile was successful
            case WEBSPEED:
            	try {msg(track, "Webspeed status requested");}catch(IOException ioe){}
                if (o != null && o instanceof String) {
                    String flagFile = (String) o;
                    java.io.File f2 = new java.io.File(flagFile);
                    if (f2.exists()) {
                        action.setCode(SUCCESS);
                        returnObject = new Boolean(true);
                    } else {
                        action.setCode(ERROR);
                        returnObject = new Boolean(false);
                    }
                } else {
                    action.setCode(ERROR);
                }
                break;
            // send successful release email
            case EMAIL:
            	try {msg(track, "Email requested");}catch(IOException ioe){}
                java.io.File f = new java.io.File(this.dataDir + INSTRUCTIONS);
                // check to see that a transfer has already started
                if (f.exists()) {
                    notifySuccess(track);
                    // remove instruction file
                    if (!f.delete()) {
                        try {
                            msg(track, "Unable to delete instruction file");
                        } catch (IOException ioe) {
                        }
                    }
                }
                break;
            // get current list of SCRs
            case SCRS:
            	try {msg(track, "SCR list requested");}catch(IOException ioe){}
                action.setCode(NO_OP);
                if (this.scrs != null) {
                    synchronized (this.scrs) {
                        if (this.scrs.size() > 0) {
                            // make a copy before sending
                            ArrayList<SCR> list = 
                                new ArrayList<SCR>(this.scrs.size());
                            for (int i = 0; i < this.scrs.size(); i++) {
                                list.add((SCR) this.scrs.get(i).clone());
                                action.setCode(SUCCESS);
                            }
                            returnObject = list;
                        } else {
                            returnObject = new ArrayList<SCR>();
                        }
                    }
                } else {
                    action.setCode(ERROR);
                }
                break;
            // get current list of Requests
            case RQSTS:
            	try {msg(track, "Request list requested");}catch(IOException ioe){}
                action.setCode(NO_OP);
                if (this.pending != null) {
                    synchronized (this.pending) {
                        if (this.pending.size() > 0) {
                            // make a copy before sending
                            ArrayList<Request> list =
                                new ArrayList<Request>(this.pending.size());
                            for (int i = 0; i < this.pending.size(); i++) {
                                list.add((Request) this.pending.get(i).clone());
                                action.setCode(SUCCESS);
                            }
                            returnObject = list;
                        } else {
                            returnObject = new ArrayList<Request>();
                        }
                    }
                } else {
                    action.setCode(ERROR);
                }
                break;
            // add a new SCR or Request
            case ADD:
            	try {msg(track, "Add requested");}catch(IOException ioe){}
                if (o != null && o instanceof SCR) {
                    SCR s = (SCR) o;
                    String addOrUpdate = "added";
                    synchronized (this.scrs) {
                        // check to see if this is new or update
                        int index = this.scrs.indexOf(s);
                        if (index >= 0) {
                        	addOrUpdate = "updated";
                            // SCR already existed, return old copy
                            returnObject = this.scrs.remove(index);
                            // keep same author
                            s.setValue(SCR.AU, ((SCR) returnObject).query(SCR.AU));
                        }
                        if (this.scrs.add(s)) {
                            try {
                                msg(track, s + " " + addOrUpdate);
                                this.handleList(this.scrs, ALLACTIVE, false);
                            } catch (IOException ioe) {
                            }
                        } else {
                            action.setCode(ERROR);
                        }
                    }
                } else if (o != null && o instanceof Request) {
                    Request r = (Request) o;
                    int tk = ((Integer) r.query(Request.TK)).intValue();
                    if (tk < 0) {
                        // assign a new tracking number to it
                        tk = this.tracking++;
                        r.setValue(Request.TK, new Integer(tk));
                    }
                    synchronized (this.pending) {
                        // check to see if this is new or update
                        int index = this.pending.indexOf(r);
                        if (index >= 0) {
                            // request already exists, return old version
                            returnObject = this.pending.remove(index);
                        }
                        if (this.pending.add(r)) {
                            sendEmail(r, track);
                            try {
                                msg(track, "Request " + tk + " submitted");
                                this.handleList(this.pending, PENDING, false);
                            } catch (IOException ioe) {
                            }
                        } else {
                            action.setCode(ERROR);
                        }
                    }
                } else {
                    action.setCode(ERROR);
                }
                break;
            // delete an SCR or Request
            case DELETE:
            	try {msg(track, "Delete requested");}catch(IOException ioe){}
                if (o != null && o instanceof SCR) {
                    SCR s = (SCR) o;
                    synchronized (this.scrs) {
                        // check to see if this SCR exists in the list
                        int index = this.scrs.indexOf(s);
                        if (index >= 0) {
                            // SCR exists, return old copy
                            returnObject = this.scrs.remove(index);
                            try {
								this.handleList(this.scrs, ALLACTIVE, true);
							} catch (IOException e) {
							}
                        } else {
                            action.setCode(ERROR);
                        }
                    }
                } else if (o != null && o instanceof Request) {
                    Request r = (Request) o;
                    int tk = ((Integer) r.query(Request.TK)).intValue();
                    synchronized (this.pending) {
                        // check to see if this is new or update
                        int index = this.pending.indexOf(r);
                        if (index >= 0) {
                            // request exists, return old version
                            returnObject = this.pending.remove(index);
                            try {
                                msg(track, "Request " + tk + " deleted");
                                this.handleList(this.pending, PENDING, true);
                            } catch (IOException ioe) {
                            }
                        } else {
                            action.setCode(ERROR);
                        }
                    }
                }
                break;
            case TRANSFER:
            	try {msg(track, "Xfer requested");}catch(IOException ioe){}
                // QA request
                if (this.transfer == null) {
                	System.err.println("request is null dummy!");
                    if (o != null && o instanceof ArrayList) {
                    	System.err.println("arrayList good grief");
                        ArrayList list = (ArrayList) o;
                        this.transfer = new ArrayList<Request>();
                        String mess = "Transfer of ";
                        for (int i = 0; i < list.size(); i++) {
                            Object listObject = list.get(i);
                            if (listObject instanceof Request) {
                                Request or = (Request) listObject;
                                Integer tk = (Integer) or.query(Request.TK);
                                mess += tk + ",";
                                this.transfer.add(or);
                                synchronized (this.pending) {
                                    this.pending.remove(or);
                                }
                            }
                        }
                        // write text file transfer instructions
                        try {
                            createXferFile();
                            msg(track, mess);
                        } catch (IOException ioe) {
                            action.setCode(ERROR);
                            returnObject = new Integer(ERROR);
                        }
                        if (action.getCode() != ERROR) {
                        }
                    }
                // client from qars performing the transfer
                } else if (o != null && o instanceof Integer &&
                           ((Integer) o).intValue() == TRANSFER) {
                	System.err.println("Xfer integer baby");
                    returnObject = this.transfer;
                    synchronized (this.pending) {
                    	try {
							this.handleList(this.pending, PENDING, true);
						} catch (IOException e) {
						}
                    }
                    System.err.println("Xfer handled splendidly");
                } else {
                    action.setCode(ERROR);
                }
                break;
            // reload users because someone just updated the master list
            case USER:
            	try {msg(track, "User update requested");}catch(IOException ioe){}
                synchronized (this.users) {
                    this.users = loadUsers();
                }
                try {
                    msg(track, "User file updated");
                } catch (IOException ioe) {
                }
                break;
        }
        writeNum(TRACKFILE, this.tracking, false);
        return returnObject;
    }
    
    /**
     * Reports a client connection suicide so its thread can be removed.
     * @param victim The ClientThread that killed itself.
     */
    public void reportSuicide(ClientThread victim) {
        remove(victim);
    }
    
    /**
     * Shuts down all client connections.
     */
    public void shutdown() {
        if (this.clients != null) {
            for (int i = 0; i < this.clients.size(); i++) {
                ClientThread ct = this.clients.get(i);
                CTShutdownThread ctst = new CTShutdownThread(ct);
                ctst.start();
            }
        }
        writeNum(TRACKFILE, this.tracking, false);
        try {
            msg(this.myTrack, "Server died");
            this.log.close();
        } catch (IOException ioe) {
        }
        try {
            synchronized (this.scrs) {
                handleList(this.scrs, ALLACTIVE, true);
            }
            synchronized (this.pending) {
                if (this.transfer != null && this.transfer.size() > 0) {
                    for (int i = 0; i < this.transfer.size(); i++) {
                        this.pending.add(this.transfer.get(i));
                    }
                    java.io.File f = new java.io.File(this.dataDir + INSTRUCTIONS);
                    f.delete();
                }
                handleList(this.pending, PENDING, true);
            }
        } catch (IOException ioe) {
        }
    }
    
    /**
     * Validates a security level.
     * @param s Security level in question
     * @return true if s is a valid access level, false otherwise
     */
    public static boolean validSecurity(int s) {
        boolean retVal = false;
        switch (s) {
            case NOAC:
            case DVLP:
            case QAAS:
            case ADMN:
                retVal = true;
        }
        return retVal;
    }
    
    // private methods ----------------------------------------------------
    
    /* Sends an email to QA and all requestors of successful release. */
    private void notifySuccess(int track) {
        OutputStream os = null;
        String eDist = System.getenv("QAEMAIL");
        for (int i = 0; i < this.transfer.size(); i++) {
            Request r = this.transfer.get(i);
            eDist += " " + (String) r.query(Request.RQ);
            if (((Boolean) r.query(Request.DC)).booleanValue()) {
                eDist += " " + System.getenv("DBEMAIL");
            }
        }
        try {
            os = qars.io.Filer.sendEmail("QA Release Success", eDist);
            PrintStream ps = new PrintStream(os);
            ps.println("The following items were successfully released on QA. " +
                       "Testing may resume.");
            ps.println();
            for (int i = 0; i < this.transfer.size(); i++) {
                Request r = this.transfer.get(i);
                r.print(os);
            }
            os.close();
        } catch (IOException ioe) {
            try {
                msg(track, "Email delivery failed (2)");
            } catch (IOException ioe2) {
            }
        }
        this.transfer = null;
    }
    
    /* Transfers the contents of a User ArrayList to a HashMap */
    private HashMap<String, User> loadUsers() {
        ArrayList<User> ulist = new ArrayList<User>();
        try {
            ulist = handleList(ulist, USERS, false);
        } catch (IOException ioe) {
        }
        HashMap<String, User> map = new HashMap<String, User>();
        for (int i = 0; i < ulist.size(); i++) {
            User u = ulist.get(i);
            map.put(u.getWin(), u);
        }
        return map;
    }
    
    /* Sends an email to QA and the requestor. */
    private void sendEmail(Object o, int track) {
        OutputStream os = null;
        String eDist = System.getenv("QAEMAIL");
        if (eDist != null) {
            eDist += " "; 
        } else {
            eDist = "";
        }
        //eDist += Thread.currentThread().getName();
        if (o instanceof Request) {
            Request or = (Request) o;
            eDist += (String) or.query(Request.RQ);
            if (((Boolean) or.query(Request.DC)).booleanValue()) {
                eDist += " " + System.getenv("DBEMAIL");
            }
            try {
                os = qars.io.Filer.sendEmail("QA Transfer Request", eDist);
                PrintStream ps = new PrintStream(os);
                ps.println("A transfer request has been submitted for QA. " +
                           "The details are shown below.");
                ps.println();
                ((Request) o).print(os);
                os.close();
            } catch (IOException ioe) {
                try {
                    msg(track, "Email delivery failed (1)");
                } catch (IOException ioe2) {
                }
            }
        }
    }
    
    /* Writes the transfer instructions to a file. */
    private void createXferFile() throws IOException {
        java.io.File f = new java.io.File(this.dataDir + INSTRUCTIONS);
        // check to see that a transfer hasn't already started
        if (f.exists()) {
            throw new IOException("Instruction file already exists");
        }
        FileOutputStream fos = new FileOutputStream(f);
        PrintStream ps = new PrintStream(fos);
        /* temporary */
        OutputStream os = qars.io.Filer.sendEmail("QA Transfer Submitted", SUPERADMIN);
        for (int i = 0; i < this.transfer.size(); i++) {
            Request r = this.transfer.get(i);
            r.print(fos);
            r.print(os);
        }
        ps.close();
        fos.close();
        os.close();
    }
    
    /* Removes a client thread from the list. Returns a reference to the
     * thread if it existed. */
    private synchronized ClientThread remove(ClientThread ct) {
        ClientThread ctReturn = null;
        if (this.clients != null) {
            int index = this.clients.indexOf(ct);
            if (index >= 0) {
                ctReturn = this.clients.remove(index);
            }
        }
        return ctReturn;
    }
    
    /* Opens the tracking file and returns the id. */
    private int getTracking() throws IOException {
        int t = DEFTRACK;
        BufferedReader br = null;
        try {
            FileReader fr = new FileReader(this.dataDir + TRACKFILE);
            br = new BufferedReader(fr);
            try {
                t = Integer.parseInt(br.readLine());
            } catch (NumberFormatException nfe) {
                br.close();
                throw new IOException("Tracking number could not be read!");
            }
            try {
                br.close();
            } catch (IOException ioe) {
            }
        } catch (FileNotFoundException fnfe) {
            // just use default tracking id
        }
        return t;
    }
    
    /* Writes an integer to the given file. Returns true if successful.
     * @param fileName Name of the file.
     * @param content The integer value to be written to the file.
     * @param doe Delete On Exit?
     */
    private boolean writeNum(String fileName, int content, boolean doe) {
        boolean success = false;
        try {
            java.io.File f = new java.io.File(this.dataDir + fileName);
            if (doe) {
                f.deleteOnExit();
            }
            FileWriter fw = new FileWriter(f);
            fw.write(content + "");
            fw.close();
            success = true;
        } catch (IOException ioe) {
        }
        return success;
    }
    
    /* Writes a message along with tracking number and timestamp to the log. */
    private void msg(int track, String message) throws IOException {
        StringBuffer timestamp = new StringBuffer(track + " ");
        TimeStamp.punch(timestamp);
        synchronized (this.log) {
            this.log.write(timestamp.toString() + " " + message + "\n");
            this.log.flush();
        }
    }
    
    /* Opens a file and returns the list. If the file does not exist, a new
     * empty list is returned. */
    @SuppressWarnings("unchecked")
    private <T> ArrayList<T> handleList(ArrayList<T> list, String file, boolean overrideSave)
                             throws IOException {
        java.io.File f = new java.io.File(this.dataDir + file);
        boolean saveFile = false;
        if (f.exists() && f.length() > 0 && !overrideSave) {
            if (list.isEmpty()) {
                // load the data
                FileInputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                    list = (ArrayList<T>) ois.readObject();
                    Object o = list.get(0);
                    if (o != null && 
                        !(o instanceof SCR || o instanceof Request ||
                          o instanceof User)) {
                    	try {
                    		ois.close();
                    	} catch (IOException ioe) {}
                        throw new ClassNotFoundException();
                    }
                } catch (ClassNotFoundException cnfe) {
                	try {
                		ois.close();
                	} catch (IOException ioe) {}
                    throw new IOException("Unrecognized class");
                } catch (IndexOutOfBoundsException ioobe) {
                }
                fis.close();
            } else {
                // otherwise, save it
                saveFile = true;
            }
        } else {
            saveFile = true;
        }
        
        if (saveFile && (!list.isEmpty() || overrideSave)) {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(list);
            fos.close();
        }
        return list;
    }
    
    /* Retrieves user from user list if valid, if not, return null */
    private User buildUser(int track, String userName) {
        User u = null;
        synchronized (this.users) {
            if (this.users.containsKey(userName)) {
                u = this.users.get(userName);
            } else {
                u = new User(userName);
            }
        }
        if (u != null && u.getUnix().length() > 0) {
            userName = u.getUnix();
        } else {
            try {
                OutputStream os = 
                    qars.io.Filer.sendEmail("Unrecognized QARS User", SUPERADMIN);
                PrintStream ps = new PrintStream(os);
                ps.println(userName + " attempted to access the QA" +
                           " Automated Release System");
                os.close();
            } catch (IOException ioe) {
                try {
                    msg(track, "Unrecognized user: " + userName);
                } catch (IOException ioe2) {
                }
            }
        }
        try {
            msg(track, "User: " + userName);
            Thread.currentThread().setName(userName);
        } catch (IOException ioe) {
        }
        return u;
    }
    
    // main ---------------------------------------------------------------
    
    /** Allows a ReleaseServer to be started from the command-line. */
    @SuppressWarnings("unused")
	public static void main(String[] args) {
        if (args != null && args.length == 2) {
            try {
                ReleaseServer rs = new ReleaseServer(args[0], args[1]);
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        } else {
            System.err.println("usage: java [-cp <classpath>] <datadir> <logfilename>");
        }
    }
    
    // private classes ----------------------------------------------------
    
    /**
     * Handles server shutdown in the event of a system interrupt.
     */
    private class RSShutdownThread extends Thread {
        private ReleaseServer rs;
        public RSShutdownThread(ReleaseServer rs) {
            this.rs = rs;
        }
        public void run() {
            if (this.rs != null) {
                this.rs.shutdown();
            }
        }
    }
    
    /**
     * Handles client shutdown to prevent the server from blocking.
     */
    private class CTShutdownThread extends Thread {
        private ClientThread ct;
        public CTShutdownThread(ClientThread ct) {
            this.ct = ct;
        }
        public void run() {
            if (this.ct != null) {
                this.ct.shutdown();
            }
        }
    }
}
    