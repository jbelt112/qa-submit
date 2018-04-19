package qars.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import qars.gui.Displayable;

/**
 * <p>A Request contains information about a single QA release request. It may
 * include all items from a given SCR or only a subset as well as compiling
 * instructions.</p>
 * 
 * @author Jaren Belt
 */
public class Request implements Serializable, Displayable {
    // public constants
    /** Requestor's id. */
    public static final String RQ = "Requestor";
    /** The SCR attached to this request. */
    public static final String IR = "SCR";
    /** A tracking number for this request. */
    public static final String TK = "Tracking#";
    /** Number of deltas included with this request. */
    public static final String DL = "Number of Deltas";
    /** Async compile needed? */
    public static final String AS = "Async";
    /** WebSpeed compile needed? */
    public static final String WS = "WebSpeed";
    /** Host library compile requested? */
    public static final String HC = "Host Request";
    /** Time this request was submitted. */
    public static final String TS = "Timestamp";
    /** Deltas forcing a total compile? */
    public static final String DC = "Delta Forced Compile";
    /** Include this request with a transfer? */
    public static final String IN = "Include";
    
    public static final long serialVersionUID = 3351762805468108448L;
    
    // instance variables
    private HashMap<String, Object> objects;
    private HashMap<String, ArrayList<File>> files;     // files to be handled
    private ArrayList<String> fileNames;
    private int numWS;
    private int numAS;
    private int numHS;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new empty Request associated with a given SCR.
     * @param scr The SCR associated with this Request.
     */
    public Request(SCR scr) {
        this(scr, null, 0);
    }
    
    /**
     * Creates a new Request with a given SCR as its underlying base.
     * @param scr The SCR associated with this Request.
     * @param requestor Person making this request; may not be the same as
     * author of SCR.
     * @param track A unique tracking number for this Request, or zero if no
     * tracking number is assigned yet.
     */
    public Request(SCR scr, String requestor, int track) {
        this.objects = new HashMap<String, Object>();
        this.files = new HashMap<String, ArrayList<File>>();
        if (scr != null) {
            this.objects.put(IR, scr.clone());
        } else {
            this.objects.put(IR, scr);
        }
        this.objects.put(DL, new Integer(0));
        this.objects.put(AS, new Boolean(false));
        this.objects.put(WS, new Boolean(false));
        this.objects.put(HC, new Boolean(false));
        this.objects.put(DC, new Boolean(false));
        this.objects.put(TK, new Integer(track));
        this.objects.put(RQ, requestor);
        StringBuffer sb = new StringBuffer();
        TimeStamp.punch(sb);
        this.objects.put(TS, sb.toString());
        this.objects.put(IN, new Boolean(false));
        this.numWS = 0;
        this.numAS = 0;
        this.numHS = 0;
        this.fileNames = new ArrayList<String>();
        
        int i = 0;
        while (i >= 0) {
            String categoryName = SCR.getCategory(i);
            if (categoryName != null) {
                this.files.put(categoryName, new ArrayList<File>());
                if (scr != null) {
                    ArrayList<File> scrList = scr.getContents(i);
                    for (int j = 0; j < scrList.size(); j++) {
                        File f = scrList.get(j);
                        if (((Boolean) f.query(File.IN)).booleanValue() ||
                            ((Boolean) f.query(File.RN)).booleanValue()) {
                            this.add((File) f.clone());
                        }
                    }
                }
                i++;
            } else {
                i = -1;
            }
        }
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Retrieves the object version of an attribute.
     * @param key The key for the attribute being returned.
     * @return The object version of the attribute requested or null if the key
     * does not exist.
     */
    public Object query(String key) {
        Object retVal = null;
        if (this.objects.containsKey(key)) {
            retVal = this.objects.get(key);
        }
        return retVal;
    }
    
    /**
     * @return The number of host files to be compiled
     */
    public int getHost() {
        return this.numHS;
    }

    /**
     * Returns a deep copy of this Request.
     * @return A copy of this Request.
     */
    public Displayable clone() {
        SCR scr = (SCR) this.query(IR);
        String requestor = (String) this.query(RQ);
        int track = ((Integer) this.query(TK)).intValue();
        Request r = new Request(scr, requestor, track);
        java.util.Set<String> keys = this.objects.keySet();
        java.util.Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Object o = this.objects.get(key);
            if (o != null) {
                if (o instanceof String) {
                    o = new String(((String) o).toCharArray());
                } else if (o instanceof Boolean) {
                    o = new Boolean(((Boolean) o).booleanValue());
                } else if (o instanceof Integer) {
                    o = new Integer(((Integer) o).intValue());
                }
                r.setValue(key, o);
            }
        }
        return r;
    }
    
    /**
     * Sets an attribute of this Request.
     * @param key The key for the attribute being changed.
     * @param obj The new value for the attribute.
     * @return true if operation succeeds.
     */
    public boolean setValue(String key, Object obj) {
        boolean success = this.objects.containsKey(key);
        if (success) {
            success = false;
            // check type
            if ((key.equals(RQ) || key.equals(TS)) && obj instanceof String) {
                success = true;
            } else if ((key.equals(AS) || key.equals(WS) || key.equals(HC) ||
                        key.equals(DC) || key.equals(IN)) && 
                       obj instanceof Boolean) {
                success = true;
            } else if ((key.equals(TK) || key.equals(DL)) && 
                       obj instanceof Integer) {
                success = true;
            } else if (key.equals(IR) && obj instanceof SCR) {
                success = true;
            }
            if (success) {
                this.objects.put(key, obj);
            }
        }
        return success;
    }

    /**
     * Compares two Requests based on tracking number.
     * @param r2 Another Request object.
     * @return A positive integer if this Request should follow r2, a negative
     * integer if this Request should come before r2, and 0 if they are the
     * same.
     * @throws ClassCastException if r2 is not of type Request.
     */
    public int compareTo(Displayable r2) throws ClassCastException {
        int tn1 = ((Integer) this.query(TK)).intValue();
        int tn2 = ((Integer) (((Request) r2).query(TK))).intValue();
        return tn1 - tn2;
    }

    /**
     * Adds a file to the list unless it already exists.
     * @param f The File object to add.
     * @return true if the file was added.
     */
    public boolean add(File f) {
        boolean success = false;
        int index = ((Integer) f.query(File.CT)).intValue();
        String cat = SCR.getCategory(index);
        if (this.files.containsKey(cat)) {
            ArrayList<File> list = this.files.get(cat);
            if (!list.contains(f)) {
                String fileName = (String) f.query(File.FN);
                success = list.add(f);
                if (success && index == SCR.DELTA) {
                    int numDeltas = ((Integer) this.objects.get(DL)).intValue();
                    numDeltas++;
                    this.objects.put(DL, new Integer(numDeltas));
                    Boolean yesno = (Boolean) this.objects.get(DC);
                    if (!yesno.booleanValue()) {
                        this.objects.put(DC, new Boolean(true));
                    }
                } else if (success) {
                    if (((Boolean) f.query(File.WS)).booleanValue()) {
                        if (numWS == 0) {
                            this.objects.put(WS, new Boolean(true));
                        }
                        if (!this.fileNames.contains(fileName)) {
                            numWS++;
                        }
                    }
                    if (((Boolean) f.query(File.AS)).booleanValue()) {
                        if (numAS == 0) {
                            this.objects.put(AS, new Boolean(true));
                        }
                        if (!this.fileNames.contains(fileName)) {
                            numAS++;
                        }
                    }
                    if (((Boolean) f.query(File.HS)).booleanValue()) {
                        if (!this.fileNames.contains(fileName)) {
                            numHS++;
                        }
                    }
                }
                if (!this.fileNames.contains(fileName)) {
                    this.fileNames.add(fileName);
                }
            }
        }
        return success;
    }
    
    /**
     * Removes a File from the list given the file name.
     * @param fileName The file name of the File to be removed.
     * @param category The category the file should be found under.
     * @return The File reference if it existed in the list, or null.
     */
    public File remove(String fileName, int category) {
        File f = null;
        String cat = SCR.getCategory(category);
        if (this.files.containsKey(cat)) {
            ArrayList<File> list = this.files.get(cat);
            int index = list.indexOf(new File(fileName, category));
            if (index >= 0) {
                f = list.remove(index);
                if (f != null && category == SCR.DELTA) {
                    int numDeltas = ((Integer) this.objects.get(DL)).intValue();
                    numDeltas--;
                    this.objects.put(DL, new Integer(numDeltas));
                    if (numDeltas == 0) {
                        this.objects.put(DC, new Boolean(false));
                    }
                } else if (f != null) {
                    if (((Boolean) f.query(File.WS)).booleanValue()) {
                        numWS--;
                        if (numWS == 0) {
                            this.objects.put(WS, new Boolean(false));
                        }
                    } 
                    if (((Boolean) f.query(File.AS)).booleanValue()) {
                        numAS--;
                        if (numAS == 0) {
                            this.objects.put(AS, new Boolean(false));
                        }
                    }
                }
            }
        }
        return f;
    }
    
    /**
     * Removes a File from the list given a reference to the File.
     * @param f The File to be removed.
     * @return true if the list contained the file and it was removed.
     */
    public boolean remove(File f) {
        String fileName = (String) f.query(File.FN);
        int category = ((Integer) f.query(File.CT)).intValue();
        File removedFile = this.remove(fileName, category);
        return (removedFile != null);
    }
    
    /**
     * Retrieves the list of files for this Request. Warning: this method
     * returns a reference to the list object. Any changes made externally will
     * affect the contents.
     * @param category The category to be returned.
     * @return A list of Files being handled by this Request.
     */
    public ArrayList<File> getFiles(int category) {
        return this.files.get(SCR.getCategory(category));
    }
    
    /**
     * Two Requests are equal if they have the same tracking number.
     * @param o Request to compare this Request to.
     * @return true if both Requests have the same tracking number.
     */
    public boolean equals(Object o) {
        if (o != null && o instanceof Request) {
            Integer tk1 = (Integer) this.objects.get(TK);
            Integer tk2 = (Integer) ((Request) o).query(TK);
            return tk1.equals(tk2);
        }
        return false;
    }
    
    /**
     * Prints the Request to an OutputStream.
     * @param out The OutputStream to write this Request to.
     */
    public void print(OutputStream out) throws java.io.IOException {
        PrintWriter pw = new PrintWriter(out);
        pw.println(this.toStringAllInfo());
        pw.println();
        pw.println();
        pw.flush();
    }
    
    /**
     * Returns a string containing the tracking number for this Request.
     */
    public String toString() {
        return ((Integer) this.objects.get(TK)).toString();
    }
    
    /**
     * Generates a string with all information about this Request.
     */
    public String toStringAllInfo() {
        StringBuffer pw = new StringBuffer();
        String newLine = "\n";
        for (int i = 0; i < 20; i++) {
            pw.append('-');
        }
        pw.append(newLine);
        String requestor = (String) this.objects.get(RQ);
        if (requestor != null) {
            pw.append("USER: " + requestor + newLine);
        }
        String timestamp = (String) this.objects.get(TS);
        pw.append("TIME: " + timestamp + newLine);
        SCR scr = (SCR) this.objects.get(IR);
        pw.append("CR NUMBER: " + scr.toString() + newLine);
        int i = 0;
        while (i >= 0) {
            String cat = SCR.getCategory(i);
            ArrayList<File> list = this.files.get(cat);
            if (list != null) {
                if (!list.isEmpty()) {
                    java.util.Collections.sort(list);
                    pw.append(cat + ": " + list.size() + newLine);
                    for (int j = 0; j < list.size(); j++) {
                        File f = list.get(j);
                        String source = (String) f.query(File.SC);
                        String dest = (String) f.query(File.DN);
                        String fileName = (String) f.query(File.FN);
                        String sep = (source.indexOf('/') >= 0 ? "/" : "\\");
                        boolean run = 
                            ((Boolean) f.query(File.RN)).booleanValue();
                        if (i == SCR.IMPACT ||
                            (!((Boolean) f.query(File.IN)).booleanValue() &&
                             run)) {
                            pw.append(fileName + newLine);
                        } else {
                            pw.append(source + sep + fileName + " " + dest +
                                      (run ? " *" : "") + newLine);
                        }
                    }
                    pw.append(newLine);
                }
                i++;
            } else {
                i = -1;
            }
        }
        boolean forceCompile = ((Boolean) this.objects.get(HC)).booleanValue();
        boolean deltaForce = ((Boolean) this.objects.get(DC)).booleanValue();
        boolean asyncCompile = ((Boolean) this.objects.get(AS)).booleanValue();
        boolean webCompile = ((Boolean) this.objects.get(WS)).booleanValue();
        if (forceCompile || deltaForce || asyncCompile || webCompile) {
            pw.append("COMPILE: ");
        }
        if (forceCompile || deltaForce) {
            pw.append("Library ");
        }
        if (asyncCompile) {
            pw.append("Async ");
        }
        if (webCompile) {
            pw.append("WebSpeed");
        }
        pw.append(newLine);
        pw.append("TRACKING: " + (Integer) this.objects.get(TK) + newLine);
        
        return pw.toString();
    }
}