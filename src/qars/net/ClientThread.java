package qars.net;

import java.io.*;
import java.net.*;
import qars.util.ReturnCode;

/**
 * <p>Used by a ReleaseServer to handle multiple connections. Each ClientThread
 * is spawned when a new connection is made to the server.</p>
 * 
 * @author Jaren Belt
 */
public class ClientThread extends Thread implements Comparable<ClientThread> {
    private ReleaseServer rs;
    private Socket scon;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private int myTrack;
    private boolean quit;
    
    // constructors -------------------------------------------------------
    
    /**
     * Constructs a new ClientThread.
     * @param rs ReleaseServer instance for this ClientThread.
     * @param scon Socket connection from accept method of ServerSocket.
     * @param track Unique tracking id assigned to this ClientThread.
     */
    public ClientThread(ReleaseServer rs, Socket scon, int track)
                        throws IOException {
        this.rs = rs;
        this.scon = scon;
        this.myTrack = track;
        if (scon != null) {
            this.is = new ObjectInputStream(this.scon.getInputStream());
            this.os = new ObjectOutputStream(this.scon.getOutputStream());
            // have to flush or else client will block
            this.os.flush();
        }
        this.quit = false;
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Continually read for new data on connection and pass it on to the server.
     */
    public void run() {
        ReturnCode action = new ReturnCode(0);
        Object o = null;
        boolean sentEmail = false;
        while (!this.quit && !sentEmail) {
            try {
                // get action code from client
                action.setCode(this.is.readInt());
                // get associated object
                try {
                    o = this.is.readObject();
                } catch (Exception e) {
                    System.err.println("Failed on object " + o);
                    throw new IOException();
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.err.println("Could not read " + this.myTrack);
                this.quit = true;
            }
            if (this.quit) {
                // this thread is going to commit suicide, notify server
                this.rs.reportSuicide(this);
            } else {
                /* if the client just wants directory contents, no need to
                   bother the server */
                switch (action.getCode()) {
                    case ReleaseServer.DIR:
                        o = getDirectoryContents(o);
                        break;
                    case ReleaseServer.EXISTS:
                        o = dirExists(o);
                        break;
                    case ReleaseServer.IMPACT:
                        o = getImpact(o);
                        break;
                    case ReleaseServer.EMAIL:
                        sentEmail = true;
                    default:
                        // pass information over to server
                        o = this.rs.performClientAction(this.myTrack, action, o);
                }
                if (sentEmail && action.getCode() != ReleaseServer.SUCCESS) {
                    sentEmail = false;
                }
            }
            if (action.getCode() == ReleaseServer.SHUTDOWN) {
                this.quit = true;
            }
            if (!this.quit) {
                synchronized (os) {
                    try {
                        // send data back to client
                        this.os.writeInt(action.getCode());
                        try {
                            this.os.writeObject(o);
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                            throw new IOException();
                        }
                        System.err.print("writed the dang code " + action.getCode() + " ");
                        if (o == null) System.err.println("null");
                        else System.err.println(o);
                    } catch (IOException ioe) {
                        ioe.printStackTrace(System.err);
                        System.err.println("Could not write " + this.myTrack);
                        this.rs.reportSuicide(this);
                        this.quit = true;
                    }
                }
            }
            o = null;
            action.setCode(0);
        }
        shutdown();
    }
    
    /**
     * Shuts down the connection to the client.
     */
    public void shutdown() {
        synchronized (this.os) {
            try {
                this.os.writeInt(ReleaseServer.SHUTDOWN);
                this.os.close();
                this.is.close();
                this.scon.shutdownInput();
                this.scon.shutdownOutput();
            } catch (IOException ioe) {
            }
        }
    }
    
    /**
     * Compares this ClientThread to another based on tracking id.
     * @param ct2 ClientThread to compare with this ClientThread.
     * @return A negative integer if this ClientThread should come before ct2,
     * a positive integer if this ClientThread should come after ct2, or 0 if
     * they are the same.
     */
    public int compareTo(ClientThread ct2) {
        return ct2.myTrack - this.myTrack;
    }
    
    /**
     * Determines if this ClientThread has the same tracking number.
     * @param o ClientThread to compare with this ClientThread.
     * @return true if they are the same.
     */
    public boolean equals(Object o) {
        ClientThread ct2 = (ClientThread) o;
        return ct2.myTrack == this.myTrack;
    }
    
    // private methods ----------------------------------------------------
    
    /** Gathers directory contents */
    private Object getDirectoryContents(Object o) {
        Object contents = null;
        if (o != null && o instanceof String) {
            contents = qars.io.DirectoryViewer.view((String) o);
        }
        return contents;
    }
    
    /** Determines if the given directory exists */
    private Boolean dirExists(Object o) {
        Boolean ex = null;
        if (o != null && o instanceof String) {
            java.io.File f = new java.io.File((String) o);
            if (f.exists() && f.isDirectory()) {
                ex = new Boolean(true);
            } else {
                ex = new Boolean(false);
            }
        }
        return ex;
    }
    
    /** Runs check-impact and returns results */
    private java.util.ArrayList<String> getImpact(Object o) {
        java.util.ArrayList<String> files = null;
        if (o != null && o instanceof String) {
            String doti = (String) o;
            if (doti.endsWith(".i")) {
                try {
                    String list[] = qars.io.Filer.getImpact(this.myTrack + "", 
                                                             doti);
                    files = new java.util.ArrayList<String>();
                    for (int i = 0; i < list.length; i++) {
                        files.add(list[i]);
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                }
            }
        }
        return files;
    }
}