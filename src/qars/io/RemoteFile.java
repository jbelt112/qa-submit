package qars.io;

import java.io.File;
import java.io.Serializable;

/**
 * <p>Allows file information to be sent to a remote process. Due to
 * inheritance issues, cannot use qars.util.File for this purpose.</p>
 * 
 * @author Jaren Belt
 */
public class RemoteFile extends File implements Serializable {
    private String pathname;
    private String name;
    private boolean isDir;
    private String parent;
    private long size;
    private static final long serialVersionUID = 546184980054084984L;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new MyFile object using the supplied pathname and whether the
     * file is a directory. The size of the file is stored as zero bytes.
     * @param pathname Full pathname to file on remote system
     * @param isDir True if the file is a directory
     */
    public RemoteFile(String pathname, boolean isDir) {
        this(pathname, isDir, 0);
    }
    
    /**
     * Creates a new MyFile object using the supplied pathname, if the file is
     * a directory, and the size of the file in bytes.
     * @param pathname Full pathname to file on remote system
     * @param isDir True if the file is a directory
     * @param size Size in bytes of the file
     */
    public RemoteFile(String pathname, boolean isDir, long size) {
        super(pathname);
        this.pathname = pathname;
        this.isDir = isDir;
        this.size = size;
        if (pathname.equals("/")) {
            this.name = pathname;
            this.parent = "";
        } else {
            if (pathname.endsWith("/")) {
                // trim off trailing slash
                pathname = pathname.substring(0, pathname.length()-1);
            }
            // name of file follows last slash
            int index = pathname.lastIndexOf("/");
            this.name = pathname.substring(index+1);
            if (index == 0) {
                index = 1;
            }
            this.parent = pathname.substring(0, index);
        }
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Sets whether the file is a directory or not.
     * @param newValue True if the file should be a directory
     * @return The old value
     */
    public boolean setIsDirectory(boolean newValue) {
        boolean oldValue = this.isDir;
        this.isDir = newValue;
        return oldValue;
    }
    
    /**
     * Sets the size of the file if it was not known during creation.
     * @param size Size in bytes of the file
     * @return The old value of the stored size
     */
    public long setSize(long size) {
        long oldSize = this.size;
        this.size = size;
        return oldSize;
    }
    
    // public methods overridden from parent ------------------------------
    
    /**
     * The remote process only sends files that can be read.
     * @return Always returns true
     */
    public boolean canRead() {
        return true;
    }
    
    /**
     * We don't care about writing the file.
     * @return Always returns false
     */
    public boolean canWrite() {
        return false;
    }
    
    /**
     * Compares entire pathname of two RemoteFiles.
     * @param pathname RemoteFile for comparison
     * @return see return value from Comparable, will return 0 if supplied File
     * is not an instance of RemoteFile
     */
    public int compareTo(File pathname) {
        int c = 0;
        if (pathname != null && pathname instanceof RemoteFile) {
            RemoteFile f = (RemoteFile) pathname;
            c = this.pathname.compareTo(f.pathname);
        }
        return c;
    }
    
    /**
     * We won't be creating files.
     * @return Always returns false
     */
    public boolean createNewFile() {
        return false;
    }
    
    /**
     * We won't be creating files.
     * @return Always returns null
     */
    public static File createTempFile(String prefix, String suffix) {
        return null;
    }
    
    /**
     * We won't be creating files.
     * @return Always returns null
     */
    public static File createTempFile(String prefix, String suffix, File dir) {
        return null;
    }
    
    /**
     * No need to delete files.
     * @return Always returns false
     */
    public boolean delete() {
        return false;
    }
    
    /**
     * Does nothing.
     */
    public void deleteOnExit() {
    }
    
    /**
     * Compares a RemoteFile to see if the pathnames are the same.
     * @param obj RemoteFile to compare
     * @return true if both RemoteFiles have the same pathname
     */
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj != null) {
            if (obj instanceof RemoteFile && compareTo((RemoteFile) obj) == 0) {
                ret = true;
            }
        }
        return ret;
    }
    
    /**
     * The remote process only sends files that exist.
     * @return Always returns true
     */
    public boolean exists() {
        return true;
    }
    
    /**
     * @return A reference to this RemoteFile
     */
    public File getAbsoluteFile() {
        return this;
    }
    
    /**
     * @return The full pathname to this RemoteFile
     */
    public String getAbsolutePath() {
        return this.pathname;
    }
    
    /**
     * @return A reference to this RemoteFile
     */
    public File getCanonicalFile() {
        return this;
    }
    
    /**
     * @return The full pathname to this RemoteFile
     */
    public String getCanonicalPath() {
        return this.pathname;
    }
    
    /**
     * @return Just the file name without path
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @return The parent directory for this RemoteFile
     */
    public String getParent() {
        return this.parent;
    }
    
    /**
     * @return A reference to a RemoteFile parent directory or null if this is
     * the root directory
     */
    public File getParentFile() {
        RemoteFile f = null;
        if (this.parent.length() > 0) {
            f = new RemoteFile(this.parent, true);
        }
        return f;
    }
    
    /**
     * @return The full pathname to this RemoteFile
     */
    public String getPath() {
        return this.pathname;
    }
    
    /**
     * Files sent are never relative.
     * @return Always returns true
     */
    public boolean isAbsolute() {
        return true;
    }
    
    /**
     * @return true if this RemoteFile is a directory
     */
    public boolean isDirectory() {
        return this.isDir;
    }
    
    /**
     * @return true if this RemoteFile is not a directory
     */
    public boolean isFile() {
        return !this.isDir;
    }
    
    /**
     * The remote process only sends visible files.
     * @return Always returns false
     */
    public boolean isHidden() {
        return false;
    }
    
    /**
     * @return Size of file
     */
    public long length() {
        return this.size;
    }
    
    /**
     * We won't be creating directories.
     * @return Always returns false
     */
    public boolean mkdir() {
        return false;
    }
    
    /**
     * We won't be creating directories.
     * @return Always returns false
     */
    public boolean mkdirs() {
        return false;
    }
    
    /**
     * Cannot change the name of a remote file.
     * @return Always returns false
     */
    public boolean renameTo(File dest) {
        return false;
    }

    /**
     * We won't be affecting a file's permissions.
     * @return Always returns false
     */
    public boolean setReadOnly() {
        return false;
    }
    
    /**
     * @return The full pathname to this RemoteFile
     */
    public String toString() {
        return this.pathname;
    }
    
    /**
     * Does nothing.
     * @return Always returns null
     */
    public java.net.URI toURI() {
        return null;
    }
}