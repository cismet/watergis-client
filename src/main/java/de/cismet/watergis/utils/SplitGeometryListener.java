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

import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.creator.GeometryFinishedListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;

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
}
