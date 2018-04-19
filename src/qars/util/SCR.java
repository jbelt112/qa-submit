package qars.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import qars.gui.Displayable;

/**
 * <p>This class contains all information about a given SCR. It may grow over
 * time as a developer adds more files. Information is captured when items are
 * sent to qars.</p>
 *
 * <p>An SCR can have source files, deltas, setups, undo scripts, data, and
 * files impacted.</p>
 * 
 * @author Jaren Belt
 */
public class SCR implements Serializable, Displayable {
    // public static constants
    /** Category for source files. */
    public static final int SOURCE = 0;
    /** Category for delta files. */
    public static final int DELTA  = 1;
    /** Category for setup files. */
    public static final int SETUP  = 2;
    /** Category for undo files. */
    public static final int UNDO   = 3;
    /** Category for data files. */
    public static final int DATA   = 4;
    /** Category for files impacted by include source files. */
    public static final int IMPACT = 5;
    /** SCR name. */
    public static final String IR = "SCR";
    /** SCR's author. */
    public static final String AU = "Author";
    
    public static final long serialVersionUID = 528884719;
    public static final String clName = "qars.util.SCR";
    
    // private constants
    private static final String[] categories = {"SOURCE", "DELTAS", "SETUPS", 
        "UNDOS", "DATA", "IMPACT"};
    private static final String specDir = "/g1/dev/spec";
    private static final String[] srcDir = {"/g1/dev/source",
        "/g1/dev/schema/delta", specDir, specDir, specDir, "/g1/adp/source"};
    private static final String[] destDir = {"source", "delta", "setup",
        "setup", "setup", ""};
    private static final int NUMLISTS = 6;
    
    // private instance variables
    private HashMap<String, Object> objects;
    private HashMap<String, ArrayList<File>> files;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new SCR without an id.
     */
    public SCR() {
        this("", "");
    }
    
    /**
     * Creates a new SCR with given id and author.
     * @param id SCR id.
     */
    public SCR(String id, String author) {
        this.objects = new HashMap<String, Object>();
        this.objects.put(IR, id);
        this.objects.put(AU, author);
        this.files = new HashMap<String, ArrayList<File>>();
        for (int i = 0; i < NUMLISTS; i++) {
            ArrayList<File> fileList = new ArrayList<File>();
            this.files.put(categories[i], fileList);
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
     * Sets an attribute of this File.
     * @param key The key for the attribute being changed.
     * @param obj The new value for the attribute.
     * @return true if operation succeeds.
     */
    public boolean setValue(String key, Object obj) {
        boolean success = this.objects.containsKey(key);
        if (success) {
            success = false;
            // check type
            if ((key.equals(IR) || key.equals(AU)) && obj instanceof String) {
                success = true;
            }
            if (success) {
                this.objects.put(key, obj);
            }
        }
        return success;
    }

    
    /**
     * Adds a file to the appropriate list. Overwrites a file if it is
     * already in the list.
     * @param f The qars.util.File to add.
     * @return true if f is successfully added.
     */
    public boolean add(File f) {
        return add(f, ((Integer) f.query(File.CT)).intValue());
    }
    
    /**
     * Adds a file to the appropriate list. Overwrites a file if it is
     * already in the list.
     * @param f The qars.util.File to add.
     * @param list The list category id.
     * @return true if f is successfully added.
     */
    public boolean add(File f, int list) {
        boolean success = false;
        if (this.files.containsKey(categories[list])) {
            ArrayList<File> al = this.files.get(categories[list]);
            if (f != null) {
                if (al.contains(f)) {
                    al.remove(f);
                }
                success = al.add(f);
            }
        }
        return success;
    }
    
    /**
     * Removes a file of given name if it exists in a certain list.
     * @param f The qars.util.File to remove.
     * @param list The list category id to remove the file from.
     * @return true if f is in list and successfully removed.
     */
    public boolean remove(File f, int list) {
        boolean wasInList = false;
        if (this.files.containsKey(categories[list])) {
            ArrayList<File> al = this.files.get(categories[list]);
            if (f != null) {
                wasInList = al.remove(f);
            }
        }
        return wasInList;
    }
    
    /**
     * Gets the contents of a given list. Warning, this returns a reference
     * to the list. Any modification afterward will alter the contents.
     * @param list The list category id to be returned.
     * @return An ArrayList<File> reference or null if list does not point to a
     * valid list.
     */
    public ArrayList<File> getContents(int list) {
        if (list >= 0 && list < categories.length) {
            return this.getContents(categories[list]);
        } else {
            return null;
        }
    }
    
    /**
     * Gets the contents of a given list. Warning, this returns a reference
     * to the list. Any modification afterward will alter the contents.
     * @param list The list name to be returned.
     * @return An ArrayList<File> reference or null if list does not point to a
     * valid list.
     */
    public ArrayList<File> getContents(String list) {
        ArrayList<File> al = null;
        if (this.files.containsKey(list)) {
            al = this.files.get(list);
        }
        return al;
    }
    
    /**
     * Gets the SCR id for this SCR.
     * @return The SCR id associated with this SCR.
     */
    public String toString() {
        return (String) this.query(IR);
    }
    
    /**
     * Compares two SCRs by id.
     * @param s2 Another SCR object.
     * @return A positive integer if this SCR should follow s2, a negative
     * integer if this SCR should come before s2, and 0 if they are the same.
     * @throws ClassCastException if s2 is not of type SCR.
     */
    public int compareTo(Displayable s2) throws ClassCastException {
        String ir1 = (String) this.query(IR);
        String ir2 = (String) ((SCR) s2).query(IR);
        return ir1.compareToIgnoreCase(ir2);
    }

    /**
     * Two SCRs are equal if they have the same SCR id. 
     * @param o Another SCR to compare with.
     * @return true if o is an SCR and has the same SCR id as this SCR.
     */
    public boolean equals(Object o) {
        boolean doEqual = false;
        if (o instanceof SCR) {
            SCR s2 = (SCR) o;
            doEqual = (this.compareTo(s2) == 0);
        }
        return doEqual;
    }
    
    /**
     * Obtains the category name given the category id.
     * @param cat Category id.
     * @return A String of the category name associated with cat.
     */
    public static String getCategory(int cat) {
        String s = null;
        if (cat >= 0 && cat < NUMLISTS) {
            s = categories[cat];
        }
        return s;
    }
    
    /**
     * Retrieves the default source directory for a particular type of file.
     * @param cat Category id.
     * @return The name of the directory on bones as default source.
     */
    public static String getSrc(int cat) {
        String s = null;
        if (cat >= 0 && cat < NUMLISTS) {
            s = srcDir[cat];
        }
        return s;
    }
    
    /**
     * Obtains the default destination directory for a category.
     * @param cat Category id.
     * @return The name of the directory on qars to use as destination.
     */
    public static String getDest(int cat) {
        String s = null;
        if (cat >= 0 && cat < NUMLISTS) {
            s = destDir[cat];
        }
        return s;
    }
    
    /**
     * Makes a copy of this SCR.
     */
    public Displayable clone() {
        SCR s = new SCR((String) this.query(IR), (String) this.query(AU));
        for (int i = 0; i < NUMLISTS; i++) {
            ArrayList<File> list = this.files.get(categories[i]);
            for (int j = 0; j < list.size(); j++) {
                s.add((File) list.get(j).clone());
            }
        }
        return s;
    }
    
    /**
     * Resets all include and run variables for all files.
     */
    public void clear() {
        Boolean doNot = new Boolean(false);
        for (int i = 0; i < categories.length; i++) {
            ArrayList<File> list = this.files.get(categories[i]);
            for (int j = 0; j < list.size(); j++) {
                File f = list.get(j);
                f.setValue(File.IN, doNot);
                f.setValue(File.RN, doNot);
                f.setValue(File.RF, new Integer(0));
            }
        }
    }
    
}
