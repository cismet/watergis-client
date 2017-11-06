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
import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Dimension;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.util.Assert;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GeometryCollectionWrapper extends GeometryCollection {
    //~ Static fields/initializers ---------------------------------------------

// With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
    private static final long serialVersionUID = -5694727726395021467L;

    //~ Instance fields --------------------------------------------------------

    /** Internal representation of this <code>GeometryCollection</code>. */
    protected PersistentGeometryWrapper[] geometries;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeometryCollectionWrapper object.
     *
     * @param   geometries  the <code>Geometry</code>s for this <code>GeometryCollection</code>, or <code>null</code> or
     *                      an empty array to create the empty geometry. Elements may be empty <code>Geometry</code>s,
     *                      but not <code>null</code>s.
     * @param   factory     DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public GeometryCollectionWrapper(PersistentGeometryWrapper[] geometries, final GeometryFactory factory) {
        super(new Geometry[] { geometries[0] }, factory);
        if (geometries == null) {
            geometries = new PersistentGeometryWrapper[] {};
        }
        if (hasNullElements(geometries)) {
            throw new IllegalArgumentException("geometries must not contain null elements");
        }
        this.geometries = geometries;
    }

    /**
     * Creates a new GeometryCollectionWrapper object.
     *
     * @param       geometries      DOCUMENT ME!
     * @param       precisionModel  DOCUMENT ME!
     * @param       SRID            DOCUMENT ME!
     *
     * @deprecated  Use GeometryFactory instead
     */
    public GeometryCollectionWrapper(final PersistentGeometryWrapper[] geometries,
            final PrecisionModel precisionModel,
            final int SRID) {
        this(geometries, new GeometryFactory(precisionModel, SRID));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Coordinate getCoordinate() {
        if (isEmpty()) {
            return null;
        }
        return geometries[0].getCoordinate();
    }

    /**
     * Collects all coordinates of all subgeometries into an Array.
     *
     * <p>Note that while changes to the coordinate objects themselves may modify the Geometries in place, the returned
     * Array as such is only a temporary container which is not synchronized back.</p>
     *
     * @return  the collected coordinates
     */
    @Override
    public Coordinate[] getCoordinates() {
        final Coordinate[] coordinates = new Coordinate[getNumPoints()];
        int k = -1;
        for (int i = 0; i < geometries.length; i++) {
            final Coordinate[] childCoordinates = geometries[i].getCoordinates();
            for (int j = 0; j < childCoordinates.length; j++) {
                k++;
                coordinates[k] = childCoordinates[j];
            }
        }
        return coordinates;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < geometries.length; i++) {
            if (!geometries[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getDimension() {
        int dimension = Dimension.FALSE;
        for (int i = 0; i < geometries.length; i++) {
            dimension = Math.max(dimension, geometries[i].getDimension());
        }
        return dimension;
    }

    @Override
    public int getBoundaryDimension() {
        int dimension = Dimension.FALSE;
        for (int i = 0; i < geometries.length; i++) {
            dimension = Math.max(dimension, ((Geometry)geometries[i]).getBoundaryDimension());
        }
        return dimension;
    }

    @Override
    public int getNumGeometries() {
        return geometries.length;
    }

    @Override
    public Geometry getGeometryN(final int n) {
        return geometries[n].getGeom();
    }

    @Override
    public int getNumPoints() {
        int numPoints = 0;
        for (int i = 0; i < geometries.length; i++) {
            numPoints += ((Geometry)geometries[i]).getNumPoints();
        }
        return numPoints;
    }

    @Override
    public String getGeometryType() {
        return "GeometryCollection";
    }

    @Override
    public Geometry getBoundary() {
        checkNotGeometryCollection(this);
        Assert.shouldNeverReachHere();
        return null;
    }

    /**
     * Returns the area of this <code>GeometryCollection.</code>
     *
     * @return  the area of the polygon
     */
    @Override
    public double getArea() {
        double area = 0.0;
        for (int i = 0; i < geometries.length; i++) {
            area += geometries[i].getArea();
        }
        return area;
    }

    @Override
    public double getLength() {
        double sum = 0.0;
        for (int i = 0; i < geometries.length; i++) {
            sum += (geometries[i]).getLength();
        }
        return sum;
    }

    @Override
    public boolean equalsExact(final Geometry other, final double tolerance) {
        if (!isEquivalentClass(other)) {
            return false;
        }
        final GeometryCollectionWrapper otherCollection = (GeometryCollectionWrapper)other;
        if (geometries.length != otherCollection.geometries.length) {
            return false;
        }
        for (int i = 0; i < geometries.length; i++) {
            if (!((Geometry)geometries[i]).equalsExact(otherCollection.geometries[i], tolerance)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void apply(final CoordinateFilter filter) {
        for (int i = 0; i < geometries.length; i++) {
            geometries[i].apply(filter);
        }
    }

    @Override
    public void apply(final CoordinateSequenceFilter filter) {
        if (geometries.length == 0) {
            return;
        }
        for (int i = 0; i < geometries.length; i++) {
            geometries[i].apply(filter);
            if (filter.isDone()) {
                break;
            }
        }
        if (filter.isGeometryChanged()) {
            geometryChanged();
        }
    }

    @Override
    public void apply(final GeometryFilter filter) {
        filter.filter(this);
        for (int i = 0; i < geometries.length; i++) {
            geometries[i].apply(filter);
        }
    }

    @Override
    public void apply(final GeometryComponentFilter filter) {
        filter.filter(this);
        for (int i = 0; i < geometries.length; i++) {
            geometries[i].apply(filter);
        }
    }

    /**
     * Creates and returns a full copy of this {@link GeometryCollection} object. (including all coordinates contained
     * by it).
     *
     * @return  a clone of this instance
     */
    @Override
    public Object clone() {
        final GeometryCollectionWrapper gc = (GeometryCollectionWrapper)super.clone();
        gc.geometries = new PersistentGeometryWrapper[geometries.length];
        for (int i = 0; i < geometries.length; i++) {
            gc.geometries[i] = (PersistentGeometryWrapper)geometries[i].clone();
        }
        return gc; // return the clone
    }

    @Override
    public void normalize() {
        for (int i = 0; i < geometries.length; i++) {
            geometries[i].normalize();
        }
        Arrays.sort(geometries);
    }

    @Override
    protected Envelope computeEnvelopeInternal() {
        final Envelope envelope = new Envelope();
        for (int i = 0; i < geometries.length; i++) {
            envelope.expandToInclude(geometries[i].getEnvelopeInternal());
        }
        return envelope;
    }

    @Override
    protected int compareToSameClass(final Object o) {
        final TreeSet theseElements = new TreeSet(Arrays.asList(geometries));
        final TreeSet otherElements = new TreeSet(Arrays.asList(((GeometryCollectionWrapper)o).geometries));
        return compare(theseElements, otherElements);
    }

    @Override
    protected int compareToSameClass(final Object o, final CoordinateSequenceComparator comp) {
        final GeometryCollectionWrapper gc = (GeometryCollectionWrapper)o;

        final int n1 = getNumGeometries();
        final int n2 = gc.getNumGeometries();
        int i = 0;
        while ((i < n1) && (i < n2)) {
            final Geometry thisGeom = getGeometryN(i);
            final Geometry otherGeom = gc.getGeometryN(i);
            final int holeComp = thisGeom.compareTo(otherGeom, comp);
            if (holeComp != 0) {
                return holeComp;
            }
            i++;
        }
        if (i < n1) {
            return 1;
        }
        if (i < n2) {
            return -1;
        }
        return 0;
    }

    /**
     * Creates a {@link GeometryCollection} with every component reversed. The order of the components in the collection
     * are not reversed.
     *
     * @return  a {@link GeometryCollection} in the reverse order
     */
    @Override
    public Geometry reverse() {
        final int n = geometries.length;
        final Geometry[] revGeoms = new Geometry[n];
        for (int i = 0; i < geometries.length; i++) {
            revGeoms[i] = geometries[i].reverse();
        }
        return getFactory().createGeometryCollection(revGeoms);
    }
}
