package qars.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.filechooser.FileSystemView;
import qars.io.RemoteFile;
import qars.net.ReleaseClient;
import qars.util.ReturnCode;

/**
 * <p>A RemoteFileSystemView connects to a remote filesystem so the client can
 * select files. Users are supplied with a read-only snapshot of a directory's
 * contents.</p>
 * 
 * @author Jaren Belt
 */
public class RemoteFileSystemView extends FileSystemView {
    private ArrayList<RemoteFile> files;
    private RemoteFile listType[];
    private String home;
    private String current;
    private ReleaseClient rc;
    private RemoteFile roots[];
    private static final int GET = qars.net.ReleaseServer.DIR;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new RemoteFileSystemView with a given home directory and the
     * client to contact the remote filesystem.
     * @param home Starting directory
     * @param rc ReleaseClient already established
     * @throws NullPointerException if the ReleaseClient is null
     */
    public RemoteFileSystemView(String home, ReleaseClient rc) {
        if (rc != null) {
            this.listType = new RemoteFile[0];
            this.rc = rc;
            if (home == null) {
                home = "/";
            } else if (!home.endsWith("/")) {
                home += "/";
            }
            this.home = home;
            this.current = home;
            this.files = getFiles(current);
            this.roots = new RemoteFile[1];
            this.roots[0] = new RemoteFile("/", true);
        } else {
            throw new NullPointerException("Requires a release client");
        }
    }
    
    // public methods overriden from parent -------------------------------
    
    /**
     * Not used.
     * @return Always returns null
     */
    public File createNewFolder(File containingDir) {
        return null;
    }
    
    /**
     * Not used.
     * @return Always returns null
     */
    public File createFileObject(File dir, String filename) {
        return null;
    }
    
    /**
     * Called when user clicks Open on the file chooser.
     * @param path File name of file to be opened
     * @return A reference to the selected RemoteFile
     */
    public File createFileObject(String path) {
        RemoteFile f = null;
        boolean found = false;
        for (int i = 0; !found && i < this.files.size(); i++) {
            RemoteFile rf = this.files.get(i);
            if (rf.getName().equals(path)) {
                f = rf;
                found = true;
            }
        }
        return f;
    }
    
    /**
     * Not used.
     * @return Always returns null
     */
    protected File createFileSystemRoot(File f) {
        return null;
    }
    
    /**
     * Not used.
     * @return Always returns null
     */
    public File getChild(File parent, String fileName) {
        return null;
    }
    
    /**
     * @return A reference to a temporary RemoteFile of the starting directory
     */
    public File getDefaultDirectory() {
        return new RemoteFile(this.home, true);
    }
    
    /**
     * Fetches the listing of files on the remote filesystem for the given
     * directory and changes the current directory.
     * @param dir The directory on the remote filesystem
     * @param useFileHiding Not used
     * @return An array of the RemoteFiles to be displayed
     */
    public File[] getFiles(File dir, boolean useFileHiding) {
        String path = dir.getPath();
        if (!path.equals(this.current)) {
            this.current = path;
            this.files = getFiles(path);
        }
        return this.files.toArray(this.listType);
    }
    
    /**
     * Called by the file chooser when the user clicks the home button.
     * @return see getDefaultDirectory
     */
    public File getHomeDirectory() {
        return this.getDefaultDirectory();
    }
    
    /**
     * Called by the file chooser when the user clicks the up button.
     * @return see qars.io.RemoteFile.getParentFile
     */
    public File getParentDirectory(File dir) {
        return (RemoteFile) dir.getParentFile();
    }
    
    /**
     * There is only one root on bones.
     * @return A one-element array with "/"
     */
    public File[] getRoots() {
        return this.roots;
    }
    
    /**
     * Extracts the file name from the RemoteFile. This is what will be
     * displayed in the file chooser.
     * @param f RemoteFile object
     * @return The file name or null if f is not a RemoteFile
     */
    public String getSystemDisplayName(File f) {
        String display = null;
        if (f instanceof RemoteFile) {
            RemoteFile mf = (RemoteFile) f;
            display = mf.getName();
        }
        return display;
    }
    
    /**
     * There is only one file system.
     * @return Always returns false
     */
    public boolean isFileSystem(File f) {
        return false;
    }
    
    /**
     * Checks if the supplied directory is the root.
     * @param dir The directory to check
     * @return true if the supplied directory is "/"
     */
    public boolean isFileSystemRoot(File dir) {
        return this.roots[0].equals(dir);
    }
    
    /**
     * None of our files are hidden.
     * @return Always returns false
     */
    public boolean isHiddenFile(File f) {
        return false;
    }
    
    /**
     * Determines if the supplied folder is the direct parent of the given file
     * on the remote filesystem.
     * @param folder Supposed parent directory
     * @param file The RemoteFile to check
     * @return true if file exists in folder
     */
    public boolean isParent(File folder, File file) {
        boolean parent = false;
        String fpath = folder.getPath();
        String fparent = file.getParent();
        if (!fpath.endsWith("/")) {
            fpath += "/";
        }
        if (!fparent.endsWith("/")) {
            fparent += "/";
        }
        if (fpath.equals(fparent)) {
            parent = true;
        }
        return parent;
    }
    
    /**
     * See isFileSystemRoot
     */
    public boolean isRoot(File f) {
        return isFileSystemRoot(f);
    }
    
    /**
     * The remote process only sends directories that can be accessed.
     * @return Always returns true
     */
    public Boolean isTraversable(File f) {
        return new Boolean(true);
    }
    
    // private methods ----------------------------------------------------
    
    /** Retrieves the directory contents */
    @SuppressWarnings("rawtypes")
	private ArrayList<RemoteFile> getFiles(String dir) {
        ArrayList<RemoteFile> files = new ArrayList<RemoteFile>();
        Object o = null;
        try {
            o = this.rc.send(new ReturnCode(GET), dir);
        } catch (IOException ioe) {
        }
        if (o != null && o instanceof ArrayList) {
            ArrayList list = (ArrayList) o;
            for (int i = 0; i < list.size(); i++) {
                Object f = list.get(i);
                if (f != null && f instanceof RemoteFile) {
                    files.add((RemoteFile) f);
                }
            }
        }
        return files;
    }
}