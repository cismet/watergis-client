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
package de.cismet.watergis.reports.types;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.util.List;
import java.util.Map;

import de.cismet.watergis.reports.GerinneGGemeindeReport;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureDataSource implements JRDataSource {

    //~ Instance fields --------------------------------------------------------

    private int index = -1;
    private final List<Map<String, Object>> features;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureDataSource object.
     *
     * @param  copy  features DOCUMENT ME!
     */
    public FeatureDataSource(final FeatureDataSource copy) {
        this.features = copy.features;
    }

    /**
     * Creates a new FeatureDataSource object.
     *
     * @param  features  DOCUMENT ME!
     */
    public FeatureDataSource(final List<Map<String, Object>> features) {
        this.features = features;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean next() throws JRException {
        ++index;

        if (features.isEmpty() && (index == 0)) {
            return true;
        } else {
            return (index) < features.size();
        }
    }

    @Override
    public Object getFieldValue(final JRField jrf) throws JRException {
        if (features.isEmpty()) {
            return null;
        } else {
            final Object o = features.get(index).get(jrf.getName());

            if (o instanceof Double) {
                return round((Double)o);
            } else {
                return o;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double round(final double value) {
        return ((int)((value * 100.0) + 0.5)) / 100.0;
    }
}
