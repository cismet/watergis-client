/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DeleteFeatureListener.java
 *
 * Created on 20. April 2005, 11:22
 */
package de.cismet.watergis.broker.listener;

import com.vividsolutions.jts.geom.*;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.InvalidPolygonTooltip;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureAddEntityAction;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureAddHoleAction;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureCreateAction;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class DeleteFeatureListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DeleteFeatureListener.class);
    public static final String FEATURE_DELETE_REQUEST_NOTIFICATION = "FEATURE_DELETE_REQUEST_NOTIFICATION"; // NOI18N
    private static final ClassComparator comparator = new ClassComparator();

    //~ Instance fields --------------------------------------------------------

    private PFeature featureRequestedForDeletion = null;
    private final InvalidPolygonTooltip multiPolygonPointerAnnotation = new InvalidPolygonTooltip();
    private final MappingComponent mc;
    private Class[] allowedFeatureClassesToDelete = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeleteFeatureListener object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public DeleteFeatureListener(final MappingComponent mc) {
        this.mc = mc;
        mc.getCamera().addChild(multiPolygonPointerAnnotation);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseMoved(final PInputEvent pInputEvent) {
        multiPolygonPointerAnnotation.setOffset(
            pInputEvent.getCanvasPosition().getX()
                    + 20.0d,
            pInputEvent.getCanvasPosition().getY()
                    + 20.0d);
        final Collection selectedFeatures = mc.getFeatureCollection().getSelectedFeatures();
        if (selectedFeatures.size() != 1) {
            multiPolygonPointerAnnotation.setMode(InvalidPolygonTooltip.Mode.SELECT_FEATURE);
            multiPolygonPointerAnnotation.setVisible(true);
        } else {
            multiPolygonPointerAnnotation.setVisible(false);
        }
    }

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            final MappingComponent mappingComponent = (MappingComponent)pInputEvent.getComponent();
            final GeometryFactory geometryFactory = new GeometryFactory(
                    new PrecisionModel(PrecisionModel.FLOATING),
                    CrsTransformer.extractSridFromCrs(
                        mappingComponent.getMappingModel().getSrs().getCode()));
            final double x = mappingComponent.getWtst().getWorldX(pInputEvent.getPosition().getX());
            final double y = mappingComponent.getWtst().getWorldY(pInputEvent.getPosition().getY());
            final Geometry cursorPoint = geometryFactory.createPoint(new Coordinate(x, y));

            final PFeature clickedPFeature = (PFeature)PFeatureTools.getFirstValidObjectUnderPointer(
                    pInputEvent,
                    new Class[] { PFeature.class },
                    true);

            if ((clickedPFeature != null) && (clickedPFeature.getFeature() != null)
                        && (allowedFeatureClassesToDelete != null)
                        && (Arrays.binarySearch(
                                allowedFeatureClassesToDelete,
                                clickedPFeature.getFeature().getClass(),
                                comparator) < 0)) {
                return;
            }

            final Collection selectedFeatures = mappingComponent.getFeatureCollection().getSelectedFeatures();
            if (selectedFeatures.size() == 1) {                                                                     // es ist genau ein feature selektiert
                final PFeature selectedPFeature = mappingComponent.getPFeatureHM()
                            .get((Feature)selectedFeatures.toArray()[0]);
                if ((selectedPFeature != null) && selectedPFeature.getFeature().canBeSelected()
                            && selectedPFeature.getFeature().isEditable()
                            && ((selectedPFeature.getFeature().getGeometry() instanceof MultiPolygon)
                                && ((selectedPFeature.getFeature().getGeometry().getNumGeometries() > 1)
                                    || ((selectedPFeature.getFeature().getGeometry().getNumGeometries() == 1)
                                        && !selectedPFeature.getFeature().getGeometry().getGeometryN(0).intersects(
                                            cursorPoint))))) {
                    if ((selectedPFeature.getNumOfEntities() == 1) && (selectedPFeature.equals(clickedPFeature))) { // hat nur ein teil-polygon
                        // "normales" löschen des geklickten features
                        deletePFeature(selectedPFeature, mappingComponent);
                    } else { // hat mehrere teil-polygone
                        // koordinate der maus berechnen
                        final Coordinate mouseCoord = new Coordinate(
                                mappingComponent.getWtst().getSourceX(
                                    pInputEvent.getPosition().getX()
                                            - mappingComponent.getClip_offset_x()),
                                mappingComponent.getWtst().getSourceY(
                                    pInputEvent.getPosition().getY()
                                            - mappingComponent.getClip_offset_y()));
                        // teil-polygon unter der maus suchen
                        final Point mousePoint = CrsTransformer.transformToGivenCrs(geometryFactory.createPoint(
                                    mouseCoord),
                                CrsTransformer.createCrsFromSrid(
                                    selectedPFeature.getFeature().getGeometry().getSRID()));

                        final int selectedEntityPosition = selectedPFeature.getEntityPositionUnderPoint(mousePoint);
                        if (selectedEntityPosition >= 0) { // gefunden => teil-polygon entfernen
                            final Polygon entity = selectedPFeature.getEntityByPosition(selectedEntityPosition);
                            selectedPFeature.removeEntity(selectedEntityPosition);
                            mappingComponent.getMemUndo()
                                    .addAction(new FeatureAddEntityAction(
                                            mappingComponent,
                                            selectedPFeature.getFeature(),
                                            entity));
                            mappingComponent.getMemRedo().clear();
                        } else {                           // nicht gefunden => es muss also ein loch sein => suchen
                                                           // und entfernen (komplex)
                            final int entityPosition = selectedPFeature.getMostInnerEntityUnderPoint(mousePoint);
                            final int holePosition = selectedPFeature.getHolePositionUnderPoint(
                                    mousePoint,
                                    entityPosition);
                            if ((entityPosition != -1) && (holePosition != -1)) {
                                final LineString hole = selectedPFeature.getHoleByPosition(
                                        entityPosition,
                                        holePosition);
                                selectedPFeature.removeHoleUnderPoint(mousePoint);
                                mappingComponent.getMemUndo()
                                        .addAction(new FeatureAddHoleAction(
                                                mappingComponent,
                                                selectedPFeature.getFeature(),
                                                entityPosition,
                                                hole));
                                mappingComponent.getMemRedo().clear();
                            }
                        }
                    }
                }
            } else {                                       // mehrere features selektiert => alt-selektionsmodus
                if (clickedPFeature != null) {
                    mappingComponent.getFeatureCollection().select(clickedPFeature.getFeature());
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pFeature          DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     */
    private void deletePFeature(final PFeature pFeature, final MappingComponent mappingComponent) {
        if (pFeature.getFeature().isEditable() && pFeature.getFeature().canBeSelected()) {
            featureRequestedForDeletion = (PFeature)pFeature.clone();
            mappingComponent.getFeatureCollection().removeFeature(pFeature.getFeature());
            mappingComponent.getMemUndo().addAction(new FeatureCreateAction(mappingComponent, pFeature.getFeature()));
            mappingComponent.getMemRedo().clear();
            postFeatureDeleteRequest();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postFeatureDeleteRequest() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(FEATURE_DELETE_REQUEST_NOTIFICATION, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getFeatureRequestedForDeletion() {
        return featureRequestedForDeletion;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the allowedFeatrueClassesToDelete
     */
    public Class[] getAllowedFeatureClassesToDelete() {
        return allowedFeatureClassesToDelete;
    }

    /**
     * Set the feature types, which are allowed to remove. If null is set, all feature types are allowed to remove. The
     * given array will be sorted.
     *
     * @param  allowedFeatureClassesToDelete  the allowedFeatrueClassesToDelete to set
     */
    public void setAllowedFeatureClassesToDelete(final Class[] allowedFeatureClassesToDelete) {
        this.allowedFeatureClassesToDelete = allowedFeatureClassesToDelete;

        if (allowedFeatureClassesToDelete != null) {
            Arrays.sort(this.allowedFeatureClassesToDelete, comparator);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class Tooltip extends PNode {

        //~ Instance fields ----------------------------------------------------

        private final Color COLOR_BACKGROUND = new Color(255, 255, 222, 200);

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Tooltip object.
         *
         * @param  text  DOCUMENT ME!
         */
        public Tooltip(final String text) {
            final PText pText = new PText(text);

            final Font defaultFont = pText.getFont();
            final Font boldDefaultFont = new Font(defaultFont.getName(),
                    defaultFont.getStyle()
                            + Font.BOLD,
                    defaultFont.getSize());
            pText.setFont(boldDefaultFont);
            pText.setOffset(5, 5);

            final PPath background = new PPath(new RoundRectangle2D.Double(
                        0,
                        0,
                        pText.getWidth()
                                + 15,
                        pText.getHeight()
                                + 15,
                        10,
                        10));
            background.setPaint(COLOR_BACKGROUND);

            background.addChild(pText);

            setTransparency(0.85f);
            addChild(background);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class ClassComparator implements Comparator<Class> {

        //~ Methods ------------------------------------------------------------

        @Override
        public int compare(final Class o1, final Class o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
