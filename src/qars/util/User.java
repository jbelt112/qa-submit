package qars.util;

import java.io.Serializable;

/**
 * Represents a user with Windows id, Unix id, and security level.
 * 
 * @author Jaren Belt
 */
public class User implements Serializable, Comparable<User> {
	private static final long serialVersionUID = -7652619814487050693L;
	public static final int NOTSET = -2;
    // private instance variables
    private String winID;
    private String unixID;
    private int securityLevel;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a user with a Windows id
     * @param winID Windows login user name
     */
    public User(String winID) {
        this(winID, "", NOTSET);
    }
    
    /**
     * Creates a user with a Windows id, Unix id, and security level
     * @param winID Windows login user name
     * @param unixID Unix user name
     * @param s Security level as described in ReleaseServer
     */
    public User(String winID, String unixID, int s) {
        this.winID = winID;
        this.unixID = unixID;
        if (qars.net.ReleaseServer.validSecurity(s)) {
            this.securityLevel = s;
        } else {
            this.securityLevel = NOTSET;
        }
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Sets the unix id for this User
     * @param unixID Unix user name
     * @return Previous unixID
     */
    public String setUnix(String unixID) {
        String oldName = this.unixID;
        this.unixID = unixID;
        return oldName;
    }
    
    /**
     * Sets the security level for this User
     * @param s Security level as described in ReleaseServer
     * @return The old security level or -1 if s is invalid
     */
    public int setSecurity(int s) {
        int retVal = -1;
        if (qars.net.ReleaseServer.validSecurity(s)) {
            retVal = this.securityLevel;
            this.securityLevel = s;
        }
        return retVal;
    }
    
    /**
     * Get the Windows id for this User
     * @return user name when signed on to Windows
     */
    public String getWin() {
        return this.winID;
    }
    
    /**
     * Get the Unix id for this User
     * @return user name when signed on to Unix
     */
    public String getUnix() {
        return this.unixID;
    }
    
    /**
     * Retrieve the security level for this User
     * @return the security level as described in ReleaseServer or NOTSET if
     * security level hasn't been set yet
     */
    public int getSecurity() {
        return this.securityLevel;
    }
    
    /**
     * Makes deep copy of this User.
     * @return A new User identical to this User
     */
    public User clone() {
        return new User(this.winID, this.unixID, this.securityLevel);
    }
    
    /**
     * Compares two Users based on Windows IDs
     * @param u2 User to compare against
     * @return A negative integer if this User should come before, 0 if equal
     * to, and a positive integer if this User should come after u2
     */
    public int compareTo(User u2) {
        return this.winID.compareTo(u2.winID);
    }
}