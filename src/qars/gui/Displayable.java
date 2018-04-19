package qars.gui;

/**
 * <p>A class implementing the Displayable interface allows graphical user
 * interface components to grab display fields.</p>
 * 
 * @author Jaren Belt
 */
public interface Displayable extends Comparable<Displayable> {
    /**
     * Retrieves a stored value from the Displayable object.
     * @param lookup A unique String associated with the value desired.
     * @return The Displayable class implementing this method should return a
     * reference to the value or null if lookup does not equate to any value.
     */
    public Object query(String lookup);
    
    /**
     * Associates the given value with a unique String. Subsequent calls to
     * query using the lookup value will return a reference to the value.
     * @param lookup A unique String associated with the given value.
     * @param value Object reference to be stored.
     * @return true if lookup is a valid lookup field and the value was 
     * successfully associated; false if the underlying object type does not
     * match the expected class or if lookup is not valid.
     */
    public boolean setValue(String lookup, Object value);
    
    /**
     * Allows the Displayable object to be compared with another.
     * @param o The Displayable object to compare to this Displayable.
     * @return A negative integer if this Displayable is "less than" o, zero if
     * they are equivalent, or a positive integer if this Displayable is
     * "greater than" o.
     */
    public int compareTo(Displayable o);
    
    /**
     * Provides a deep copy of this Displayable object.
     */
    public Displayable clone();
}