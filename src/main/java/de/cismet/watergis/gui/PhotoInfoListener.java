/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.gui;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PLocator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class PhotoInfoListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    // private static final Color COLOR_SUBLINE = new Color(255, 91, 0);
    private static final float CURSOR_PANEL_TRANSPARENCY = 1f;

    public static final String MODE = "FOTO_INFO_LISTENER";

    //~ Instance fields --------------------------------------------------------

    protected MappingComponent mc;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private float cursorX = Float.MIN_VALUE;
    private float cursorY = Float.MIN_VALUE;
    private final PhotoInfoPHandle cursorPHandle;
    private final Collection<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of SimpleMoveListener.
     *
     * @param  mc  DOCUMENT ME!
     */
    public PhotoInfoListener(final MappingComponent mc) {
        super();
        this.mc = mc;

        final PLocator l = new PLocator() {

                @Override
                public double locateX() {
                    return cursorX;
                }

                @Override
                public double locateY() {
                    return cursorY;
                }
            };
        cursorPHandle = new PhotoInfoPHandle(l, mc);
        cursorPHandle.setInfoPanelTransparency(CURSOR_PANEL_TRANSPARENCY);
        cursorPHandle.setPaint(null);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void setPhotoSize() {
        cursorPHandle.setPhotoSize();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addPropertyChangeListener(final PropertyChangeListener listener) {
        return listeners.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removePropertyChangeListener(final PropertyChangeListener listener) {
        return listeners.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    protected void firePropertyChange(final PropertyChangeEvent evt) {
        for (final PropertyChangeListener listener : listeners) {
            listener.propertyChange(evt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseDragged(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseReleased(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseClicked(final PInputEvent event) {
        // nothing to do
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getPLayer() {
        return mc.getHandleLayer();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  show  DOCUMENT ME!
     */
    private void showCursor(final boolean show) {
        showToolTip(getPLayer(), cursorPHandle, show);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  father  DOCUMENT ME!
     * @param  child   DOCUMENT ME!
     * @param  show    DOCUMENT ME!
     */
    private void showToolTip(final PNode father, final PPath child, final boolean show) {
        boolean found = false;
        for (final Object o : father.getChildrenReference()) {
            if ((o != null) && o.equals(child)) {
                found = true;
                break;
            }
        }
        if (!found && show) {
            father.addChild(child);
        }
        if (found && !show) {
            father.removeChild(child);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseMoved(final PInputEvent event) {
        if (mc.getInteractionMode().equals(MODE)) {
            updateCursor(event);
            cursorPHandle.setMarkPosition();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    private void updateCursor(final PInputEvent event) {
        final Collection c = PFeatureTools.getValidObjectsUnderPointer(
                event,
                new Class[] { PFeature.class },
                0.001);
        boolean cursorIsVisible = false;
        final List<CidsLayerFeature> fotoFeature = new ArrayList<CidsLayerFeature>();

        for (final Object o : c) {
            if (o instanceof PFeature) {
                final PFeature feature = (PFeature)o;

                if (feature.getFeature() instanceof CidsLayerFeature) {
                    final CidsLayerFeature cFeature = (CidsLayerFeature)feature.getFeature();

                    if (cFeature.getLayerProperties().getFeatureService() instanceof CidsLayer) {
                        if (((CidsLayer)cFeature.getLayerProperties().getFeatureService()).getMetaClass().getName()
                                    .equalsIgnoreCase("foto")) {
                            cursorIsVisible = true;
                            fotoFeature.add(cFeature);
                        }
                    }
                }
            }
        }
        showCursor(cursorIsVisible);
        cursorPHandle.setVisible(cursorIsVisible);

        if (fotoFeature != null) {
            cursorPHandle.setFeature(fotoFeature);
        }

        cursorX = (float)event.getPosition().getX();
        cursorY = (float)event.getPosition().getY();
    }
}
