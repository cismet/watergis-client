/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.utils;
import java.awt.event.MouseEvent;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class JPopupMenuButton extends de.cismet.tools.gui.JPopupMenuButton {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of JPopupMenuButton.
     */
    public JPopupMenuButton() {
        this(true);
    }

    /**
     * Creates a new instance of JPopupMenuButton.
     *
     * @param  showPopupMenu  do not use the popup menu funtionality
     */
    public JPopupMenuButton(final boolean showPopupMenu) {
        super(showPopupMenu);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final MouseEvent e) {
        super.mouseClicked(e);

        actionPerformed(null);
    }

    /**
     * Returns whether the given point x,y is in the popup area or not.
     *
     * @param   x  e x DOCUMENT ME!
     * @param   y  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isOverMenuPopupArea(final int x, final int y) {
        return (x >= (getWidth() - getIcon().getIconWidth() + arrowXOffset - getInsets().right))
                    && (x <= (getWidth() - 1));
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed.
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mouseMoved(final java.awt.event.MouseEvent e) {
        mouseInPopupArea = isOverMenuPopupArea((int)e.getPoint().getX(), (int)e.getPoint().getY());
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged. <code>MOUSE_DRAGGED</code> events will
     * continue to be delivered to the component where the drag originated until the mouse button is released
     * (regardless of whether the mouse position is within the bounds of the component).
     *
     * <p>Due to platform-dependent Drag&Drop implementations, <code>MOUSE_DRAGGED</code> events may not be delivered
     * during a native Drag&Drop operation.</p>
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mouseDragged(final java.awt.event.MouseEvent e) {
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mouseReleased(final java.awt.event.MouseEvent e) {
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mousePressed(final java.awt.event.MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void mouseExited(final java.awt.event.MouseEvent e) {
        mouseInPopupArea = false;
    }
}
