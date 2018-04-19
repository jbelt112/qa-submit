package qars.net;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import qars.util.*;

/**
 * <p>After the QA team has submitted a request, the qarsClient is responsible
 * for performing the required actions to implement a release. After finishing,
 * if successful, the server is notified.</p>
 * 
 * @author Jaren Belt
 */
public class QarsClient {
    // instance variables
    private Socket sock;          // communications channel with ReleaseServer
    private ObjectInputStream is;                  // messages from the server
    private ObjectOutputStream os;                   // messages to the server
    private ArrayList<Request> xfer;                      // transfer requests
    
    private final String MFDIR_DFLT = "/as/test/qa/mf";

    // constructors -------------------------------------------------------
    
    /**
     * Creates a new qarsClient, opens a socket to the ReleaseServer, and
     * waits for client to send data.
     */
    @SuppressWarnings("rawtypes")
	public QarsClient() throws IOException {
        Runtime.getRuntime().addShutdownHook(new QCShutdownThread(this));
        sock = new Socket(ReleaseServer.HOST, ReleaseServer.PORT);
        os = new ObjectOutputStream(sock.getOutputStream());
        // have to flush or else server will block
        os.flush();
        is = new ObjectInputStream(sock.getInputStream());
        this.xfer = new ArrayList<Request>();
        Object o = send(new ReturnCode(ReleaseServer.TRANSFER), 
                        new Integer(ReleaseServer.TRANSFER));
        if (o instanceof ArrayList) {
            ArrayList ol = (ArrayList) o;
            for (int i = 0; i < ol.size(); i++) {
                Object r = ol.get(i);
                if (r instanceof Request) {
                    this.xfer.add((Request) r);
                }
            }
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
        synchronized (os) {
            try {
                os.writeInt(ReleaseServer.SHUTDOWN);
                os.writeObject(new Integer(ReleaseServer.SHUTDOWN));
            } catch (IOException ioe) {
            }
            // close input and output to the socket
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
    
    /**
     * Performs the release actions and sends results back to server.
     * @return 0 if successful, 1 if error
     */
    private int performRelease() {
        int retCode = 1;
        String mfDir = getEnv("MFDIR", this.MFDIR_DFLT);
        if (this.xfer.size() > 0) {
            Request request = combineRequests();
            // run any setup scripts
            retCode = performSetups(request, mfDir);
            // run host compile
            if (retCode == 0) {
                retCode = performHostCompile(request, mfDir);
            }
            // run async compile
            if (retCode == 0) {
                retCode = performAsyncCompile(request, mfDir);
            }
            // run webspeed compile
            if (retCode == 0) {
                //retCode = performWebspeedCompile(request);
            }
            // send email and notify server of success
            if (retCode == 0) {
                try {
                    send(new ReturnCode(ReleaseServer.EMAIL), null);
                } catch (IOException ioe) {
                }
            }
        }
        return retCode;
    }
    
    // private methods ----------------------------------------------------
    
    /**
     * Checks environment variable. If it does not exist, returns the passed
     * default.
     * @param var Environment variable name.
     * @param def String to use if environment variable does not exist.
     * @return Either the stored environment variable contents or the default.
     */
    private String getEnv(String var, String def) {
        String retVal = System.getenv(var);
        if (retVal == null) {
            retVal = def;
        }
        return retVal;
    }
    
    /**
     * Runs undo scripts if there are any and then runs setups.
     * @param request Master request for this release.
     * @param mfDir Script directory.
     * @return 0 on success, 1 on failure
     */
    private int performSetups(Request request, String mfDir) {
        int success = 0;
        // get undos if there are any
        ArrayList<qars.util.File> files = request.getFiles(SCR.UNDO);
        // check to see if any require that they be run
        ArrayList<String> undoRun = gatherRun(files);
        // get setups if there are any
        files = request.getFiles(SCR.SETUP);
        // check to see if any require that they be run
        ArrayList<String> setupRun = gatherRun(files);
        // set up command
        int numArg = 1;
        numArg += (undoRun.size() > 0 ? undoRun.size() + 1 : 0);
        numArg += (setupRun.size() > 0 ? setupRun.size() + 1 : 0);
        String[] cmd = new String[numArg];
        cmd[0] = mfDir + getEnv("RUNSETUP", "runSetups");
        if (cmd.length > 1) {
            int i = 1;
            if (undoRun.size() > 0) {
                cmd[i++] = "-u";
                for (int j = 0; j < undoRun.size(); j++) {
                    cmd[i++] = undoRun.get(j);
                }
            }
            if (setupRun.size() > 0) {
                cmd[i++] = "-s";
                for (int j = 0; j < setupRun.size(); j++) {
                    cmd[i++] = setupRun.get(j);
                }
            }
            // kick it off
            success = processCommand(cmd);
            String message = success == 0 ? "Setups successful" :
                "Setups failed";
            try {
                send(new ReturnCode(ReleaseServer.LOG), message);
            } catch (IOException ioe) {
            }
        }
        return success;
    }
    
    /**
     * Fires off a unix command and returns the result.
     * @param cmd String array of command-line arguments.
     * @return 0 on success, 1 on failure.
     */
    private int processCommand(String[] cmd) {
        System.err.println("Processing Command:");
        for (int i = 0; i < cmd.length; i++) {
            System.err.print(" " + cmd[i]);
        }
        System.err.println();
        // consider failure unless success
        int success = 1;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            System.err.println(p);
            try {
                p.waitFor();
            } catch (InterruptedException ie) {
            }
            System.out.println("Reading output");
            String line = "";
            while ((line = stdout.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("Reading error");
            while ((line = stderr.readLine()) != null) {
                System.out.println(line);
            }
            stdout.close();
            stderr.close();
            success = p.exitValue();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return success;
    }
    
    /**
     * Cycles through a list of runnable files and gathers run requests.
     * @param list The list to cycle through.
     * @return A list of file names that need to be run.
     */
    private ArrayList<String> gatherRun(ArrayList<qars.util.File> list) {
        ArrayList<String> run = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            qars.util.File f = list.get(i);
            if (((Boolean) f.query(qars.util.File.RN)).booleanValue()) {
                run.add((String) f.query(qars.util.File.FN));
            }
        }
        return run;
    }
    
    /**
     * Compiles host code.
     * @param request Master request for this release.
     * @param mfDir Script directory.
     * @return 0 on success, 1 on failure
     */
    private int performHostCompile(Request request, String mfDir) {
        int success = 0;
        // get changed files if there are any
        ArrayList<qars.util.File> files = request.getFiles(SCR.SOURCE);
        // get impacted files if there are any
        ArrayList<qars.util.File> imfiles = request.getFiles(SCR.IMPACT);
        // set up command
        int numArg = 1 + files.size();
        numArg += imfiles.size() > 0 ? imfiles.size() + 1 : 0;
        String[] cmd = new String[numArg];
        cmd[0] = mfDir + getEnv("RUNCMPILE", "runCompile");
        if (cmd.length > 1) {
            int i = 1;
            if (files.size() > 0) {
                for (int j = 0; j < files.size(); j++) {
                    cmd[i++] = (String) files.get(j).query(qars.util.File.FN);
                }
            }
            if (imfiles.size() > 0) {
                cmd[i++] = "-i";
                for (int j = 0; j < imfiles.size(); j++) {
                    qars.util.File f = imfiles.get(j);
                    cmd[i++] = (String) f.query(qars.util.File.FN);
                }
            }
            // kick it off
            success = processCommand(cmd);
            String message = success == 0 ? "Host compile successful" :
                "Host compile failed";
            try {
                send(new ReturnCode(ReleaseServer.LOG), message);
            } catch (IOException ioe) {
            }
        }
        return success;
    }
    
    /**
     * Compiles async code.
     * @param request Master request for this release.
     * @param mfDir Script directory.
     * @return 0 on success, 1 on failure
     */
    private int performAsyncCompile(Request request, String mfDir) {
        int success = 0;
        if (((Boolean) request.query(Request.AS)).booleanValue()) {
            String[] cmd = {mfDir + getEnv("ASCMPL", "async-compile")};
            success = processCommand(cmd);
            String message = success == 0 ? "Async compile successful" :
                "Async compile failed";
            try {
                send(new ReturnCode(ReleaseServer.LOG), message);
            } catch (IOException ioe) {
            }
        }
        return success;
    }
            
    /**
     * Combines all requests into one master request.
     * @return The combination of all requests to be performed.
     */
    private Request combineRequests() {
        Request combo = new Request(null);
        for (int i = 0; i < this.xfer.size(); i++) {
            Request r = this.xfer.get(i);
            int j = 0;
            while (j >= 0) {
                String categoryName = SCR.getCategory(j);
                if (categoryName != null) {
                    ArrayList<qars.util.File> files = r.getFiles(j);
                    for (int k = 0; k < files.size(); k++) {
                        combo.add(files.get(k));
                    }
                    j++;
                } else {
                    j = -1;
                }
            }
        }
        return combo;
    }
    
    /**
     * Send an action and an object to the server. The object will be modified
     * and available to the calling entity.
     * @param action The action to be performed.
     * @param obj An object to deliver to the server.
     * @return Response code from server after processing action.
     * @throws IOException if error occurs while sending or receiving data.
     */
    private Object send(ReturnCode action, Object obj) throws IOException {
        Object returnObject = null;
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
    
    // main ---------------------------------------------------------------
    
    public static void main(String[] args) {
        QarsClient qc = null;
        // assume failure unless successful completion
        int retCode = 1;
        try {
            qc = new QarsClient();
        } catch (IOException ioe) {
            System.err.println("Unable to create client");
        }
        if (qc != null) {
            retCode = qc.performRelease();
        }
        System.exit(retCode);
    }
    
    // private classes ----------------------------------------------------
    
    /** Handles shutdown of client in event of interrupt. */
    private class QCShutdownThread extends Thread {
        private QarsClient qc;
        public QCShutdownThread(QarsClient qc) {
            this.qc = qc;
        }
        public void run() {
            qc.shutdown();
        }
    }
}