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

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.LinearReferencedPointMarkPHandle;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.panels.Photo;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class PhotoAngleListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    // private static final Color COLOR_SUBLINE = new Color(255, 91, 0);
    private static final float CURSOR_PANEL_TRANSPARENCY = 1f;

    public static final String MODE = "FOTO_ANGLE_LISTENER";

    //~ Instance fields --------------------------------------------------------

    protected MappingComponent mc;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private float cursorX = Float.MIN_VALUE;
    private float cursorY = Float.MIN_VALUE;
//    private final FotoInfoPHandle cursorPHandle;
    private final Collection<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of SimpleMoveListener.
     *
     * @param  mc  DOCUMENT ME!
     */
    public PhotoAngleListener(final MappingComponent mc) {
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
//        cursorPHandle = new FotoInfoPHandle(l, mc);
//        cursorPHandle.setInfoPanelTransparency(CURSOR_PANEL_TRANSPARENCY);
//        cursorPHandle.setPaint(null);
    }

    //~ Methods ----------------------------------------------------------------

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
//        if (!isDragging() && event.isShiftDown()) {
//            startLineMark();
//        }
//        updateCursor(event.getPosition());
//        cursorPHandle.setMarkPosition(getCurrentPosition());
//        if (isDragging()) {
//            updateLineMark();
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseReleased(final PInputEvent event) {
//        log.fatal("mouse released");
//        if (isDragging()) {
//            finishLineMark();
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseClicked(final PInputEvent event) {
        if (mc.getInteractionMode().equals(MODE)) {
            final CidsLayerFeature feature = Photo.selectedFeature;
            if (feature != null) {
                final double pointX = feature.getGeometry().getCoordinate().x;
                final double pointY = feature.getGeometry().getCoordinate().y;
                final double mouseX = AppBroker.getInstance()
                            .getMappingComponent()
                            .getWtst()
                            .getWorldX(event.getPosition().getX());
                final double mouseY = AppBroker.getInstance()
                            .getMappingComponent()
                            .getWtst()
                            .getWorldY(event.getPosition().getY());

                final double dx = mouseX - pointX;
                final double dy = mouseY - pointY;
                double angle;

                final double scalar = dy;
                final double prodLaenge = Math.pow((dx * dx) + (dy * dy), 0.5) * Math.pow(1, 0.5);

                angle = Math.acos(scalar / prodLaenge);

                if (dx > 0) {
                    angle = (2 * Math.PI) - angle;
                }

                // rechtsdrehend
                angle = (2 * Math.PI) - angle;

                // in grad
                angle = angle * 360 / (2 * Math.PI);

                if (angle < 0) {
                    angle = angle + 360;
                }

                try {
                    feature.getBean().setProperty("winkel", Math.round(angle * 100) / 100.0);
                    feature.setProperty("winkel", Math.round(angle * 100) / 100.0);

                    final AbstractFeatureService service = feature.getLayerProperties().getFeatureService();
                    if (service != null) {
                        final List<PFeature> pfeatureList = service.getPNode().getChildrenReference();

                        for (final PFeature pf : pfeatureList) {
                            final Feature f = pf.getFeature();

                            if (f instanceof FeatureServiceFeature) {
                                if (((FeatureServiceFeature)f).getId() == feature.getId()) {
                                    ((FeatureServiceFeature)f).setProperty("winkel", Math.round(angle * 100) / 100.0);
                                    pf.visualize();
                                    pf.refreshDesign();
                                    final PFeature mapFeature = pf.getViewer().getPFeatureHM().get(feature);

                                    if (mapFeature != null) {
                                        ((FeatureServiceFeature)mapFeature.getFeature()).setProperty(
                                            "winkel",
                                            Math.round(angle * 100)
                                                    / 100.0);
                                        mapFeature.visualize();
                                        mapFeature.refreshDesign();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Cannot set winkel property", e);
                }
            }
        }
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
     * @param  father  DOCUMENT ME!
     * @param  child   DOCUMENT ME!
     * @param  show    DOCUMENT ME!
     */
// private void showCursor(final boolean show) {
// showToolTip(getPLayer(), cursorPHandle, show);
// }

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
//            updateCursor(event.getPosition(), event);
//            cursorPHandle.setMarkPosition();
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param    trigger  DOCUMENT ME!
     * @param    event    DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
// private void updateCursor(final Point2D trigger, final PInputEvent event) {
// final Collection c = PFeatureTools.getValidObjectsUnderPointer(
// event,
// new Class[] { PFeature.class },
// 0.001);
// boolean cursorIsVisible = false;
// CidsLayerFeature fotoFeature = null;
//
// for (final Object o : c) {
// if (o instanceof PFeature) {
// final PFeature feature = (PFeature)o;
//
// if (feature.getFeature() instanceof CidsLayerFeature) {
// final CidsLayerFeature cFeature = (CidsLayerFeature)feature.getFeature();
//
// if (cFeature.getLayerProperties().getFeatureService() instanceof CidsLayer) {
// if (((CidsLayer)cFeature.getLayerProperties().getFeatureService()).getMetaClass().getName()
// .equalsIgnoreCase("foto")) {
// cursorIsVisible = true;
// fotoFeature = cFeature;
// break;
// }
// }
// }
// }
// }
// showCursor(cursorIsVisible);
// cursorPHandle.setVisible(cursorIsVisible);
//
// if (fotoFeature != null) {
// cursorPHandle.setFeature(fotoFeature);
// }
//
// cursorX = (float)event.getPosition().getX();
// cursorY = (float)event.getPosition().getY();
// }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class PointMark {

        //~ Instance fields ----------------------------------------------------

        private double position;
        private LinearReferencedPointMarkPHandle pHandle;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PointMark object.
         *
         * @param  position  DOCUMENT ME!
         * @param  handle    DOCUMENT ME!
         */
        PointMark(final double position, final LinearReferencedPointMarkPHandle handle) {
            this.pHandle = handle;
            this.position = position;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public double getPosition() {
            return position;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public LinearReferencedPointMarkPHandle getPHandle() {
            return pHandle;
        }
    }
}
