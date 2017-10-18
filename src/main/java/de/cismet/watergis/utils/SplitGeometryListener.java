/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.utils;

import edu.umd.cs.piccolo.nodes.PPath;

import java.awt.Color;

import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.creator.GeometryFinishedListener;
import de.cismet.cismap.commons.gui.piccolo.CustomFixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;

import static de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface.LINESTRING;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SplitGeometryListener extends CreateNewGeometryListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String LISTENER_KEY = "SplitGeometryListener";

    //~ Instance fields --------------------------------------------------------

    private GeometryFinishedListener geometryFinishedListener;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreaterGeometryListener object.
     *
     * @param  mc                        DOCUMENT ME!
     * @param  geometryFinishedListener  DOCUMENT ME!
     */
    public SplitGeometryListener(final MappingComponent mc, final GeometryFinishedListener geometryFinishedListener) {
        super(mc);
        this.geometryFinishedListener = geometryFinishedListener;
        setMode(CreateNewGeometryListener.LINESTRING);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void finishGeometry(final AbstractNewFeature newFeature) {
        mappingComponent.getTmpFeatureLayer().removeAllChildren();
        geometryFinishedListener.geometryFinished(newFeature.getGeometry());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected PPath createNewTempFeature() {
        final PPath newTempFeaturePath = new PPath();
        newTempFeaturePath.setStroke(new FixedWidthStroke());

        if (!isInMode(LINESTRING)) {
            final Color fillingColor = new Color(1f, 0f, 0f, 0.5f);
            newTempFeaturePath.setStrokePaint(fillingColor.darker());
            newTempFeaturePath.setPaint(fillingColor);
        } else {
            final Color fillingColor = new Color(1f, 0f, 0f, 0.5f);
            newTempFeaturePath.setStroke(new CustomFixedWidthStroke(3));
            newTempFeaturePath.setStrokePaint(fillingColor);
        }
        return newTempFeaturePath;
    }
}
