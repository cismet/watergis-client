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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

import java.sql.Timestamp;

import java.util.Date;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.PersistentFeature;
import de.cismet.cismap.commons.util.FilePersistenceManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class JumpFeature implements Feature {

    //~ Static fields/initializers ---------------------------------------------

    private static final boolean DATE_AS_STRING = true;

    //~ Instance fields --------------------------------------------------------

    private final FeatureServiceFeature feature;
    private FeatureSchema schema = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JumpFeature object.
     *
     * @param  feature  DOCUMENT ME!
     */
    public JumpFeature(final FeatureServiceFeature feature) {
        this.feature = feature;
    }

    /**
     * Creates a new JumpFeature object.
     *
     * @param  feature  DOCUMENT ME!
     * @param  schema   DOCUMENT ME!
     */
    public JumpFeature(final FeatureServiceFeature feature, final FeatureSchema schema) {
        this.feature = feature;
        this.schema = schema;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FilePersistenceManager getPersistenceManager() {
        if (feature instanceof PersistentFeature) {
            return ((PersistentFeature)feature).getPersistenceManager();
        } else {
            return null;
        }
    }

    @Override
    public void setAttributes(final Object[] attributes) {
    }

    @Override
    public void setSchema(final FeatureSchema schema) {
        this.schema = schema;
    }

    @Override
    public int getID() {
        return feature.getId();
    }

    @Override
    public void setAttribute(final int attributeIndex, final Object newAttribute) {
    }

    @Override
    public void setAttribute(final String attributeName, final Object newAttribute) {
    }

    @Override
    public void setGeometry(final Geometry geometry) {
    }

    @Override
    public Object getAttribute(final int i) {
        Object value = feature.getProperty(schema.getAttributeName(i));
        if (value instanceof Boolean) {
            value = String.valueOf(value);
        } else if (DATE_AS_STRING && ((value instanceof Timestamp) || (value instanceof Date))) {
            value = String.valueOf(value);
        }

        if ((schema.getAttributeType(schema.getAttributeName(i)) == AttributeType.DOUBLE)
                    && (value instanceof Integer)) {
            value = ((Integer)value).doubleValue();
        }
        return value;
    }

    @Override
    public Object getAttribute(final String name) {
        return feature.getProperty(name);
    }

    @Override
    public String getString(final int attributeIndex) {
        Object value = feature.getProperty(schema.getAttributeName(attributeIndex));

        if (value instanceof Boolean) {
            value = String.valueOf(value);
        } else if (DATE_AS_STRING && ((value instanceof Timestamp) || (value instanceof Date))) {
            value = String.valueOf(value);
        }

        return (String)value;
    }

    @Override
    public int getInteger(final int attributeIndex) {
        return (Integer)feature.getProperty(schema.getAttributeName(attributeIndex));
    }

    @Override
    public double getDouble(final int attributeIndex) {
        return (Double)feature.getProperty(schema.getAttributeName(attributeIndex));
    }

    @Override
    public String getString(final String attributeName) {
        return (String)feature.getProperty(attributeName);
    }

    @Override
    public Geometry getGeometry() {
        return feature.getGeometry();
    }

    @Override
    public FeatureSchema getSchema() {
        return schema;
    }

    @Override
    public Feature clone(final boolean deep) {
        return new JumpFeature(feature);
    }

    @Override
    public Object[] getAttributes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(final Object o) {
        if (o instanceof JumpFeature) {
            return feature.getId() - ((JumpFeature)o).getID();
        }

        return -1;
    }

    @Override
    public Object clone() {
        return new JumpFeature(feature);
    }
}
