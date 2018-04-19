package qars.io;

import java.io.*;
import java.util.ArrayList;

/**
 * <p>Offers static methods for obtaining information about a file or setting
 * up email.</p>
 * 
 * @author Jaren Belt
 */
public class Filer {
    /**
     * Obtains impacted files for include files. This is accomplished by calling
     * a Perl script, which saves the results to the temp directory. This method
     * then reads the saved file and returns the results.
     * @param id User id calling this method. This allows different users to
     * perform this operation concurrently.
     * @param fileName The include file name.
     * @return An array of file names that are impacted by the given include
     * file.
     * @throws IOException if reading the results file failed.
     */
    public static String[] getImpact(String id, String fileName)
             throws IOException {
        String tempDir = System.getenv("TEMPDIR");
        if (tempDir == null) {
            tempDir = "/tmp/";
        } else if (!tempDir.endsWith("/")) {
            tempDir += "/";
        }
        String sDir = System.getenv("MFDIR");
        if (sDir == null) {
            sDir = "/g1/test/qa/mf/";
        } else if (!sDir.endsWith("/")) {
            sDir += "/";
        }
        String cImpact = System.getenv("CKIMPACT");
        if (cImpact == null) {
            cImpact = "check-impact";
        }
        String[] cmd = {sDir + cImpact, fileName, tempDir + id + ".impact"};
        
        final class WaitThread implements Runnable {
            private Thread parent;
            public WaitThread(Thread parent) {
                this.parent = parent;
            }
            public void run() {
                try {
                    Thread.sleep(30000);
                    this.parent.interrupt();
                } catch (InterruptedException ie) {
                }
            }
        }
        Thread wt = new Thread(new WaitThread(Thread.currentThread()));
        Process p = Runtime.getRuntime().exec(cmd);
        wt.start();
        try {
            p.waitFor();
            wt.interrupt();
        } catch (InterruptedException ie) {
        }
            
        java.io.File f = new java.io.File(cmd[2]);
        if (f.exists()) {
            java.io.FileReader fr = new java.io.FileReader(f);
            java.io.BufferedReader br = new java.io.BufferedReader(fr);
            ArrayList<String> files = new ArrayList<String>();
            String s = null;
            do {
                s = br.readLine();
                if (s != null) {
                    files.add(s);
                }
            } while (s != null);
            String[] retVal = new String[files.size()];
            for (int i = 0; i < files.size(); i++) {
                retVal[i] = files.get(i);
            }
            f.delete();
            if (f.exists()) {
                System.err.println("File was not deleted!");
            }
            br.close();
            return retVal;
        } else {
            throw new java.io.IOException("Could not find " + cmd[2]);
        }
    }
    
    /**
     * Sets up an email by making a unix call to mailx and obtaining the
     * OutputStream from that process. The calling entity should handle closing
     * the OutputStream after finished writing to it.
     * @param subject Text to appear as the subject line of the email.
     * @param users A space-separated list of users to deliver the email to.
     * @return The OutputStream associated with the mailx process.
     * @throws IOException if unable to get output stream.
     */
    public static OutputStream sendEmail(String subject, String users)
             throws IOException {
        Runtime r = Runtime.getRuntime();
        String addr = "@Autosource.AudatexSolutions.com";
        String[] cmd = {"mailx", "-s " + subject, "-r donotreply" + addr,
            users};
        Process p = r.exec(cmd);
        return p.getOutputStream();
    }
}