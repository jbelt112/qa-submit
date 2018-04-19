package qars.util;

import java.io.Serializable;
import java.util.HashMap;
import qars.gui.Displayable;

/**
 * <p>A release file has source and destination information, name, size and
 * category.</p>
 * 
 * @author Jaren Belt
 */
public class File implements Serializable, Displayable {
    // public constants
    /** File name */
    public static final String FN = "File Name";
    /** Include the file */
    public static final String IN = "Include";
    /** File's source directory */
    public static final String SC = "Source";
    /** File's destination directory */
    public static final String DN = "Destination";
    /** File's size */
    public static final String SZ = "Size";
    /** File's category */
    public static final String CT = "Category";
    /** Is the file a WebSpeed file? */
    public static final String WS = "WebSpeed";
    /** Is the file an Async file? */
    public static final String AS = "Async";
    /** Is the file a Host file? */
    public static final String HS = "Host";
    /** Run the file */
    public static final String RN = "Run";
    /** Number of files pointing to this file. */
    public static final String RF = "Reference";
    /** Is the file a directory? */
    public static final String DR = "Directory";
    
    public static final long serialVersionUID = 81020374400156184L;
    
    // private instance variables
    private HashMap<String, Object> objects;   // object version of attributes
    private java.util.ArrayList<File> impact;  // files impacted by this File
    private java.util.ArrayList<File> parents; // parent Files
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new File with category.
     * @see #File(String, long, int)
     */
    public File(int category) {
        this("", 0, category);
    }
    
    /**
     * Creates a new File with given name and category, but no size.
     * @see #File(String, long, int)
     */
    public File(String fileName, int category) {
        this(fileName, 0, category);
    }
    
    /**
     * Creates a new File with given name, size, and category.
     * @param fileName Name of the file without path information.
     * @param size Size in bytes.
     * @param category User-defined category.
     */
    public File(String fileName, long size, int category) {
        this.objects = new HashMap<String, Object>();
        this.objects.put(FN, fileName);
        this.objects.put(IN, new Boolean(false));
        this.objects.put(SC, "");
        this.objects.put(DN, "");
        this.objects.put(SZ, new Long(size));
        this.objects.put(CT, new Integer(category));
        this.objects.put(WS, new Boolean(false));
        this.objects.put(AS, new Boolean(false));
        this.objects.put(RN, new Boolean(false));
        this.objects.put(DR, new Boolean(false));
        this.objects.put(HS, new Boolean(true));
        this.impact = new java.util.ArrayList<File>();
        this.parents = new java.util.ArrayList<File>();
        this.objects.put(RF, new Integer(0));
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Two files are equal if they have the same name.
     * @param o Another File object.
     * @return true if both Files have the same name.
     */
    public boolean equals(Object o) {
        boolean doEqual = false;
        if (o instanceof File) {
            String fn1 = (String) this.query(FN);
            String fn2 = (String) ((File) o).query(FN);
            doEqual = fn1.equals(fn2);
        }
        return doEqual;
    }
    
    /**
     * Compares two Files based on name.
     * @param f2 Another file object.
     * @return A positive integer if this File should follow f2, a negative
     * integer if this File should come before f2, and 0 if they are the same.
     */
    public int compareTo(Displayable f2) {
        String fn1 = (String) this.query(FN);
        String fn2 = (String) ((File) f2).query(FN);
        return fn1.compareTo(fn2);
    }
    
    /**
     * Provides an exact copy of this File.
     * @return A copy of this File.
     */
    public Displayable clone() {
        String fn = (String) this.query(FN);
        long size = ((Long) this.query(SZ)).longValue();
        int category = ((Integer) this.query(CT)).intValue();
        File f = new File(fn, size, category);
        f.setValue(SC, new String((String) this.query(SC)));
        f.setValue(DN, new String((String) this.query(DN)));
        f.setValue(WS, new Boolean(((Boolean) this.query(WS)).booleanValue()));
        f.setValue(AS, new Boolean(((Boolean) this.query(AS)).booleanValue()));
        f.setValue(IN, new Boolean(((Boolean) this.query(IN)).booleanValue()));
        f.setValue(RN, new Boolean(((Boolean) this.query(RN)).booleanValue()));
        f.setValue(RF, new Integer(((Integer) this.query(RF)).intValue()));
        f.setValue(DR, new Boolean(((Boolean) this.query(DR)).booleanValue()));
        f.setValue(HS, new Boolean(((Boolean) this.query(HS)).booleanValue()));
        for (int i = 0; i < this.impact.size(); i++) {
            // don't want to clone it, need to retain reference
            f.addImpact(this.impact.get(i));
        }
        if (this.parents == null) {
            this.parents = new java.util.ArrayList<File>();
        } else {
            for (int i = 0; i < this.parents.size(); i++) {
                f.addParent(this.parents.get(i));
            }
        }
        return f;
    }
    
    /**
     * Retrieves the files impacted by this file if there are any.
     * @return An array containing all files impacted or null if no files.
     */
    public File[] getImpactList() {
        File[] list = null;
        if (!this.impact.isEmpty()) {
            list = new File[this.impact.size()];
            for (int i = 0; i < this.impact.size(); i++) {
                list[i] = this.impact.get(i);
            }
        }
        return list;
    }
    
    /**
     * Adds a file to the impact list.
     * @param im File impacted by this File.
     * @return true if successful, false if already in list.
     */
    public boolean addImpact(File im) {
        boolean success = false;
        if (this.impact.indexOf(im) < 0) {
            this.impact.add(im);
            success = true;
        }
        return success;
    }
    
    /**
     * Adds a parent to the impact file.
     * @param parent File acting as parent to this File.
     * @return true if successful, false if already in list or this File is
     * not an impacted file */
    public boolean addParent(File parent) {
        boolean success = false;
        if (this.parents.indexOf(parent) < 0) {
            Integer cat = (Integer) this.query(CT);
            if (cat.intValue() == SCR.IMPACT) {
                this.parents.add(parent);
                success = true;
            }
        }
        return success;
    }
    
    /**
     * Removes a file from the impact list.
     * @param im File to be removed.
     * @return true if successful, false if not in the list.
     */
    public boolean removeImpact(File im) {
        return this.impact.remove(im);
    }
    
    /**
     * Removes a parent file from the parent list.
     * @param parent File to be removed.
     * @return true if successful, false if not in the list or this File is
     * not an impacted file */
    public boolean removeParent(File parent) {
        boolean success = false;
        Integer cat = (Integer) this.query(CT);
        if (cat.intValue() == SCR.IMPACT && this.parents.indexOf(parent) >= 0) {
            success = this.parents.remove(parent);
        }
        return success;
    }       
    
    /**
     * Adds or removes a reference to this File.
     * @param diff 1 to add or -1 to subtract.
     * @return The number of references for this File.
     */
    public int reference(int diff) {
        int numRef = ((Integer) this.query(RF)).intValue();
        numRef += diff;
        if (numRef >= 0) {
            this.objects.put(RF, new Integer(numRef));
        } else {
            numRef = 0;
        }
        return numRef;
    }
    
    /**
     * Gets the number of references to this File.
     * @return Number of files pointing to this File.
     */
    public int getRef() {
        return ((Integer) this.query(RF)).intValue();
    }
    
    /**
     * Gets the number of parents for this File.
     * @return Number of parents.
     */
    public int getNumParents() {
        return this.parents.size();
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
            if ((key.equals(FN) || key.equals(SC) || key.equals(DN)) &&
                obj instanceof String) {
                success = true;
            } else if ((key.equals(IN) || key.equals(WS) || key.equals(AS) ||
                        key.equals(RN) || key.equals(DR) || key.equals(HS)) && 
                       obj instanceof Boolean) {
                success = true;
            } else if ((key.equals(CT) || key.equals(RF)) && 
                       obj instanceof Integer) {
                success = true;
            } else if (key.equals(SZ) && obj instanceof Long) {
                success = true;
            }
            if (success) {
                this.objects.put(key, obj);
            }
        }
        return success;
    }
    
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
            if (retVal instanceof Boolean && key.equals(IN)) {
                Integer cat = (Integer) this.query(CT);
                if (cat.intValue() == qars.util.SCR.IMPACT) {
                    Integer ref = (Integer) this.query(RF);
                    retVal = new Boolean(ref.intValue() > 0);
                }
            }
        }
        return retVal;
    }
    
    /**
     * Returns the name of the file.
     */
    public String toString() {
        return (String) this.objects.get(FN);
    }
}
