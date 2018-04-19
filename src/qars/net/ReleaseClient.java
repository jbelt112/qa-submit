package qars.net;

import java.io.*;
import java.net.*;
import qars.util.*;

/**
 * Handles client side of client-server communication. Accepts server responses
 * over a Socket.
 */
public class ReleaseClient {
    // instance variables
    private Socket sock;          // communications channel with ReleaseServer
    private ObjectInputStream is;                  // messages from the server
    private ObjectOutputStream os;                   // messages to the server
    private User user;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new ReleaseClient, opens a socket to the ReleaseServer, and
     * waits for client to send data.
     * @param port Server listening port
     * @param id User ID of client.
     */
    public ReleaseClient(int port, String id) throws IOException {
        Runtime.getRuntime().addShutdownHook(new RCShutdownThread(this));
        this.sock = new Socket(ReleaseServer.HOST, port);
        this.os = new ObjectOutputStream(sock.getOutputStream());
        // have to flush or else server will block
        this.os.flush();
        this.is = new ObjectInputStream(sock.getInputStream());
        Object ob = send(new ReturnCode(ReleaseServer.LOGNAME), id);
        if (ob instanceof User) {
            this.user = (User) ob;
        } else {
            throw new IOException("No name returned");
        }
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Shuts down this client as gracefully as possible. This method is called
     * in the event of a system interrupt signal received by the executing
     * thread.
     */
    public void shutdown() {
        // notify server that we'll be shutting down
        if (os != null) {
            synchronized (os) {
                try {
                    os.writeInt(ReleaseServer.SHUTDOWN);
                    os.writeObject(new Integer(ReleaseServer.SHUTDOWN));
                } catch (IOException ioe) {
                }
            }
            // close input and output to the socket
            if (is != null) {
                try {
                    synchronized (is) {
                        is.close();
                    }
                    os.close();
                    sock.close();
                } catch (IOException ioe) {
                }
            }
        }
    }
    
    /**
     * Send an action and an object to the server. The object will be modified
     * and available to the calling entity.
     * @param action The action to be performed.
     * @param obj An object to deliver to the server.
     * @return Response code from server after processing action.
     * @throws IOException if error occurs while sending or receiving data.
     */
    public Object send(ReturnCode action, Object obj) throws IOException {
        Object returnObject = null;
        // check to see if server has shut down
        synchronized (is) {
            try {
                if (is.available() > 0) {
                    action.setCode(is.readInt());
                    try {
                        returnObject = is.readObject();
                    } catch (ClassNotFoundException cnfe) {
                    }
                    if (action.getCode() == ReleaseServer.SHUTDOWN) {
                        throw new IOException("Server unavailable");
                    }
                }
            } catch (IOException ioe) {
            }
        }
        synchronized (os) {
            try {
                os.writeInt(action.getCode());
                os.writeObject(obj);
            } catch (IOException ioe) {
                throw new IOException("Error during transmission");
            }
        }
        synchronized (is) {
            try {
                action.setCode(is.readInt());
                returnObject = is.readObject();
            } catch (IOException ioe) {
                throw new IOException("Error during server response");
            } catch (ClassNotFoundException cnfe) {
                throw new IOException("Could not find return object class");
            }
        }
        return returnObject;
    }
    
    /**
     * Returns the unix user name, which may differ from Windows.
     * @return Unix user name returned from server
     */
    public String getUnixName() {
        return this.user.getUnix();
    }
    
    /**
     * Returns the security level of this user.
     * @return One of ReleaseServer's access codes 
     */
    public int getSecurity() {
        return this.user.getSecurity();
    }
    
    // private methods ----------------------------------------------------
    
    /** Opens the port file and reads the server's listening port. */
    @SuppressWarnings("unused")
	private int getServerPort(String portFile) throws IOException {
        int port = 0;
        try {
            FileReader fr = new FileReader(portFile);
            BufferedReader br = new BufferedReader(fr);
            try {
                port = Integer.parseInt(br.readLine());
            } catch (NumberFormatException nfe) {
            	br.close();
                throw new IOException("Server port file corrupt!");
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException fnfe) {
            throw new IOException("Server port file does not exist!");
        }
        return port;
    }
    
    // private classes ----------------------------------------------------
    
    /** Handles shutdown of client in event of interrupt. */
    private class RCShutdownThread extends Thread {
        private ReleaseClient rc;
        public RCShutdownThread(ReleaseClient rc) {
            this.rc = rc;
        }
        public void run() {
            rc.shutdown();
        }
    }
}