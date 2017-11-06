/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jump.feature.Feature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.PersistentFeature;
import de.cismet.cismap.commons.util.FilePersistenceManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PersistentGeometryWrapper extends Geometry {

    //~ Instance fields --------------------------------------------------------

    Feature feature = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PersistentGeometryWrapper object.
     *
     * @param  factory  DOCUMENT ME!
     * @param  f        DOCUMENT ME!
     */
    public PersistentGeometryWrapper(final GeometryFactory factory, final Feature f) {
        super(factory);
        this.feature = f;
    }

    /**
     * Creates a new PersistentGeometryWrapper object.
     *
     * @param  factory  DOCUMENT ME!
     * @param  geo      DOCUMENT ME!
     * @param  pm       DOCUMENT ME!
     */
    public PersistentGeometryWrapper(final GeometryFactory factory,
            final Geometry geo,
            final FilePersistenceManager pm) {
        super(factory);
        // todo persistent feature nutzen
// this.feature = new DefaultFeatureServiceFeature();
        final DefaultFeatureServiceFeature innerFeature = new DefaultFeatureServiceFeature(1, geo, null);
        innerFeature.setGeometry(geo);
        final PersistentFeature f = new PersistentFeature(innerFeature, pm);
        this.feature = new JumpFeature(f, null);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getGeom() {
        return feature.getGeometry();
    }

    @Override
    public String getGeometryType() {
        return getGeom().getGeometryType();
    }

    @Override
    public Coordinate getCoordinate() {
        return getGeom().getCoordinate();
    }

    @Override
    public Coordinate[] getCoordinates() {
        return getGeom().getCoordinates();
    }

    @Override
    public int getNumPoints() {
        return getGeom().getNumPoints();
    }

    @Override
    public boolean isEmpty() {
        return getGeom().isEmpty();
    }

    @Override
    public int getDimension() {
        return getGeom().getDimension();
    }

    @Override
    public Geometry getBoundary() {
        return getGeom().getBoundary();
    }

    @Override
    public int getBoundaryDimension() {
        return getGeom().getBoundaryDimension();
    }

    @Override
    public Geometry reverse() {
        return getGeom().reverse();
    }

    @Override
    public boolean equalsExact(final Geometry other, final double tolerance) {
        return getGeom().equalsExact(other, tolerance);
    }

    @Override
    public void apply(final CoordinateFilter filter) {
        getGeom().apply(filter);
    }

    @Override
    public void apply(final CoordinateSequenceFilter filter) {
        getGeom().apply(filter);
    }

    @Override
    public void apply(final GeometryFilter filter) {
        getGeom().apply(filter);
    }

    @Override
    public void apply(final GeometryComponentFilter filter) {
        getGeom().apply(filter);
    }

    @Override
    public void normalize() {
        getGeom().normalize();
    }

    @Override
    protected Envelope computeEnvelopeInternal() {
        final Geometry envGeom = getGeom().getEnvelope();
        final CoordinateSequence s = envGeom.getFactory()
                    .getCoordinateSequenceFactory()
                    .create(envGeom.getCoordinates());

        return s.expandEnvelope(new Envelope());
    }

    @Override
    protected int compareToSameClass(final Object o) {
        return getGeom().compareTo(o);
    }

    @Override
    protected int compareToSameClass(final Object o, final CoordinateSequenceComparator comp) {
        return getGeom().compareTo(o, comp);
    }
}
