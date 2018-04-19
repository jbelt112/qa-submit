package qars.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * This class allows various components to register their mouse over hints with
 * a frame's status bar. When a user hovers the mouse over a component that has
 * registered with this MouseOverHintManager, the tip is displayed.
 * 
 * @author Jaren Belt
 */
public class MouseOverHintManager implements MouseListener {
    // instance variables
    private Map<Component, String> hintMap;
    private HashMap<Component, Boolean> permanentComponents;
    private QarsFrame frame;
    
    // constructors -------------------------------------------------------
    
    /**
     * Sets up a new MouseOverHintManager ready to have components register.
     * @param hintFrame The frame containing the status bar.
     */
    public MouseOverHintManager(QarsFrame hintFrame) {
        this.hintMap = new HashMap<Component, String>();
        this.permanentComponents = new HashMap<Component, Boolean>();
        this.frame = hintFrame;
    }
    
    // public methods -----------------------------------------------------
    
    /**
     * Registers a component's hint text with this manager. Then enables the
     * hints to be displayed by adding a mouse listener to the component. All
     * hints added through this method are considered temporary.
     * @param comp The registering component.
     * @param hintText Text to be displayed in status bar when mouse over the
     * component.
     */
    public void addHintFor(Component comp, String hintText) {
        addHintFor(comp, hintText, false);
    }
    
    /**
     * Registers a component's hint text with this manager. Then enabled the
     * hints to be displayed by adding a mouse listener to the component.
     * @param comp The registering component.
     * @param hintText Text to be displayed in status bar when mouse over the
     * component.
     * @param permanent Whether this component is permanent or temporary.
     */
    public void addHintFor(Component comp, String hintText, boolean permanent) {
        this.hintMap.put(comp, hintText);
        this.permanentComponents.put(comp, new Boolean(permanent));
        comp.addMouseListener(this);
    }
    
    /**
     * Clears all temporary hints.
     */
    public void clear() {
        Set<Component> keys = this.hintMap.keySet();
        Iterator<Component> it = keys.iterator();
        ArrayList<Component> removeThese = new ArrayList<Component>();
        while (it.hasNext()) {
            Component c = it.next();
            if (!this.permanentComponents.get(c).booleanValue()) {
                removeThese.add(c);
            }
        }
        for (int i = 0; i < removeThese.size(); i++) {
            Component c = removeThese.get(i);
            this.permanentComponents.remove(c);
            this.hintMap.remove(c);
            c.removeMouseListener(this);
        }
    }
    
    /**
     * Called when the mouse enters a component's space.
     * @param e MouseEvent triggered by the mouse movement.
     */
    public void mouseEntered(MouseEvent e) {
        Component comp = (Component) e.getSource();
        frame.display(getHintFor(comp));
    }
    
    /**
     * Called when the mouse exits a component's space. Clears the status bar.
     * @param e MouseEvent triggered by the mouse movement.
     */
    public void mouseExited(MouseEvent e) {
        frame.display(" ");
    }
    
    /*
     * Empty methods.
     */
    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    
    // private methods ----------------------------------------------------
    
    /**
     * Gets the tip hint text for a given component.
     * @param comp The component registered with this manager.
     * @return The hint text for that component.
     */
    private String getHintFor(Component comp) {
        String hint = this.hintMap.get(comp);
        if (hint == null) {
            hint = " ";
        }
        return hint;
    }
}
