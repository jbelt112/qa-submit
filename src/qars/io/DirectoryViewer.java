package qars.io;

import java.io.File;
import java.util.ArrayList;
import qars.io.RemoteFile;

/**
 * <p>Provides access to the unix file system.</p>
 * 
 * @author Jaren Belt
 */
public class DirectoryViewer {
    
    /**
     * The only method provided is a static method to return the directory
     * contents of a given directory.
     * @param dir The directory to return contents of
     * @return An ArrayList of RemoteFile objects
     */
    public static ArrayList<RemoteFile> view(String dir) {
        ArrayList<RemoteFile> files = null;
        File dirFile = new File(dir);
        if (dirFile.exists() && dirFile.isDirectory()) {
            File fileList[] = dirFile.listFiles();
            if (fileList != null) {
                files = new ArrayList<RemoteFile>();
                for (int i = 0; i < fileList.length; i++) {
                    File f = fileList[i];
                    if (f.canRead() && !f.isHidden()) {
                        RemoteFile rf = new RemoteFile(f.getPath(),
                                                       f.isDirectory(),
                                                       f.length());
                        rf.setLastModified(f.lastModified());
                        files.add(rf);
                    }
                }
            }
        }
        return files;
    }
}