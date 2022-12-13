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
package de.cismet.watergis.profile;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.deegree.model.crs.UnknownCRSException;

import java.util.HashMap;
import java.util.Map;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.CrsTransformer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ProfileLine {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ProfileLine.class);

    //~ Instance fields --------------------------------------------------------

    private String[] data;
    private final Map<ProfileReader.GAF_FIELDS, Integer> fieldMap;
    private CidsLayerFeature bezug;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ProfileLine object.
     *
     * @param  fieldMap  DOCUMENT ME!
     * @param  data      DOCUMENT ME!
     */
    public ProfileLine(final Map<ProfileReader.GAF_FIELDS, Integer> fieldMap, final String[] data) {
        if (fieldMap.size() < data.length) {
            this.data = new String[fieldMap.size()];
            this.fieldMap = new HashMap<>();
            int i = 0;

            for (final ProfileReader.GAF_FIELDS field : fieldMap.keySet()) {
                final int index = i++;
                this.fieldMap.put(field, index);
                this.data[index] = data[fieldMap.get(field)];
            }
        } else {
            this.data = data;
            this.fieldMap = new HashMap<ProfileReader.GAF_FIELDS, Integer>(fieldMap);
        }

        final Integer statIndex = this.fieldMap.get(ProfileReader.GAF_FIELDS.STATION);

        if (statIndex != null) {
            this.data[statIndex] = this.data[statIndex].replace("*", "");
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the bezug
     */
    public CidsLayerFeature getBezug() {
        return bezug;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bezug  the bezug to set
     */
    public void setBezug(final CidsLayerFeature bezug) {
        try {
            if ((this.bezug != bezug)
                        && !((this.bezug == null) && (bezug != null) && bezug.getProperty("l_bezug").equals(5650))) {
                final CrsTransformer transformer = new CrsTransformer("EPSG:5650");
                final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 0);
                final String rw = getField(ProfileReader.GAF_FIELDS.RW).replace(',', '.');
                final String hw = getField(ProfileReader.GAF_FIELDS.HW).replace(',', '.');

                if ((rw != null) && (hw != null)) {
                    try {
                        final Double rwD = Double.parseDouble(rw);
                        final Double hwD = Double.parseDouble(hw);

                        final Geometry point = factory.createPoint(new Coordinate(rwD, hwD));
                        final Geometry transformedPoint = transformer.fastTransformGeometry(point, getEPSG(bezug));

                        setField(ProfileReader.GAF_FIELDS.RW, transformedPoint.getCoordinate().x);
                        setField(ProfileReader.GAF_FIELDS.HW, transformedPoint.getCoordinate().y);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Exception while transforming coordinates", e);
        }
        this.bezug = bezug;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bezug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getEPSG(final CidsLayerFeature bezug) {
        if (bezug == null) {
            return "EPSG:5650";
        } else {
            return "EPSG:" + bezug.getProperty("l_bezug");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getField(final ProfileReader.GAF_FIELDS field) {
        final Integer index = fieldMap.get(field);

        if ((index == null) || (index >= data.length)) {
            return null;
        }

        return data[index.intValue()];
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  NumberFormatException  DOCUMENT ME!
     */
    public Double getFieldAsDouble(final ProfileReader.GAF_FIELDS field) throws NumberFormatException {
        final Integer index = fieldMap.get(field);

        if (index == null) {
            return null;
        }

        if (index >= data.length) {
            return 0.0;
        }

        try {
            return Double.parseDouble(data[index].replace(',', '.'));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  field  DOCUMENT ME!
     * @param  o      DOCUMENT ME!
     */
    public void setField(final ProfileReader.GAF_FIELDS field, final Object o) {
        Integer index = fieldMap.get(field);

        if (index == null) {
            fieldMap.put(field, fieldMap.size());

            final String[] tmp = data;
            index = tmp.length;
            data = new String[data.length + 1];

            System.arraycopy(tmp, 0, data, 0, tmp.length);
        } else if (index >= data.length) {
            final String[] tmp = data;
            data = new String[index + 1];

            System.arraycopy(tmp, 0, data, 0, tmp.length);
        }

        if (o == null) {
            // data[index] throws an exception. data[index.intValue()] works.
            data[index.intValue()] = null;
        } else {
            data[index.intValue()] = o.toString();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasCorrectLength() {
        return fieldMap.size() <= data.length;
    }
}
