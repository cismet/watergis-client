/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.gui;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.util.PLocator;

import pswing.PSwing;
import pswing.PSwingCanvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.watergis.gui.dialog.GafOptionsDialog;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class GafInfoPHandle extends PPath {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GafInfoPHandle.class);

    public static final double DEFAULT_HANDLE_SIZE = 10;
    public static final Shape DEFAULT_HANDLE_SHAPE = new Ellipse2D.Double(
            0f,
            0f,
            DEFAULT_HANDLE_SIZE,
            DEFAULT_HANDLE_SIZE);
    public static final Color DEFAULT_COLOR = Color.BLUE;

    //~ Instance fields --------------------------------------------------------

    private PLocator locator;
    private MappingComponent mc = null;
    private GafInfoPanel gafInfoPanel;
    private PSwing pswingComp;

    //~ Constructors -----------------------------------------------------------

    /**
     * Construct a new handle that will use the given locator to locate itself on its parent node.
     *
     * @param  locator  DOCUMENT ME!
     * @param  mc       DOCUMENT ME!
     */
    public GafInfoPHandle(final PLocator locator,
            final MappingComponent mc) {
        super(DEFAULT_HANDLE_SHAPE);

        this.mc = mc;
        this.locator = locator;

        installEventListener();

        setPaint(DEFAULT_COLOR);
        installHandleEventHandlers();
        startResizeBounds();

        initPanel();

        relocateHandle();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  zeroToOne  DOCUMENT ME!
     */
    public void setInfoPanelTransparency(final float zeroToOne) {
        pswingComp.setTransparency(zeroToOne);
    }

    /**
     * DOCUMENT ME!
     */
    private void installEventListener() {
        final PBasicInputEventHandler moveAndClickListener = new PBasicInputEventHandler() {

                @Override
                public void mouseClicked(final PInputEvent pInputEvent) {
                    handleClicked(pInputEvent);
                }

                @Override
                public void mouseEntered(final PInputEvent pInputEvent) {
                }
            };

        addInputEventListener(moveAndClickListener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    private void handleClicked(final PInputEvent pInputEvent) {
    }

    /**
     * DOCUMENT ME!
     */
    private void initPanel() {
        gafInfoPanel = new GafInfoPanel(this);

        pswingComp = new PSwing((PSwingCanvas)mc, gafInfoPanel);
        addChild(pswingComp);
    }

    /**
     * DOCUMENT ME!
     */
    public void setImageSize() {
        final Dimension dim = GafOptionsDialog.getInstance().getImageSize();
        gafInfoPanel.setImageSize();
        pswingComp.setBounds(0, 0, dim.width, dim.height);
        startResizeBounds();
        relocateHandle();
    }

    /**
     * DOCUMENT ME!
     */
    public void setMarkPosition() {
        relocateHandle();
        repaint();
    }

    /**
     * DOCUMENT ME!
     */
    protected void installHandleEventHandlers() {
        addPropertyChangeListener(PNode.PROPERTY_TRANSFORM, new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
                    relocateHandle();
                }
            });
    }

    /**
     * Get the locator that this handle uses to position itself on its parent node.
     *
     * @return  DOCUMENT ME!
     */
    public PLocator getLocator() {
        return locator;
    }

    /**
     * Set the locator that this handle uses to position itself on its parent node.
     *
     * @param  locator  DOCUMENT ME!
     */
    public void setLocator(final PLocator locator) {
        this.locator = locator;
        invalidatePaint();
        relocateHandle();
    }

    @Override
    public void setParent(final PNode newParent) {
        super.setParent(newParent);
        relocateHandle();
    }

    @Override
    public void parentBoundsChanged() {
        relocateHandle();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public void setFeature(final CidsLayerFeature feature) {
        gafInfoPanel.setFeature(feature);
    }

    /**
     * Force this handle to relocate itself using its locator.
     */
    public void relocateHandle() {
        if (locator != null) {
            final PBounds b = getBoundsReference();
            final Point2D aPoint = locator.locatePoint(null);
            mc.getCamera().viewToLocal(aPoint);

            final double newCenterX = aPoint.getX();
            final double newCenterY = aPoint.getY();

            pswingComp.setOffset(newCenterX + DEFAULT_HANDLE_SIZE, newCenterY - (pswingComp.getHeight() / 2));

            if ((newCenterX != b.getCenterX()) || (newCenterY != b.getCenterY())) {
                this.setBounds(0, 0, DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
                centerBoundsOnPoint(newCenterX, newCenterY);
            }
        }
    }
}
