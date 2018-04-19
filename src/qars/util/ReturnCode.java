package qars.util;

/**
 * <p>A ReturnCode stores information about the desired action and result of a
 * ReleaseServer transaction. Since java only allows one return value per method,
 * the server can update the code and the recipient will see the change.</p>
 * 
 * @author Jaren Belt
 */
public class ReturnCode {
    private int code;
    
    // constructors -------------------------------------------------------
    
    /**
     * Creates a new ReturnCode initialized to a ReleaseServer action code.
     * @param code See ReleaseServer
     */
    public ReturnCode(int code) {
        this.code = code;
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Sets the stored code to a new value. The old value is returned.
     */
    public int setCode(int code) {
        int returnValue = this.code;
        this.code = code;
        return returnValue;
    }
    
    /**
     * Obtains the stored value for this ReturnCode.
     */
    public int getCode() {
        return this.code;
    }
}