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
package de.cismet.cismap.custom.simplifier;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

import org.apache.log4j.Logger;

import de.cismet.cismap.commons.features.DefaultFeatureSimplifier;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AslFeatureSimplifier extends DefaultFeatureSimplifier {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AslFeatureSimplifier.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public Geometry simplify(final Feature feature) {
        Geometry geom = feature.getGeometry();
        final int s = geom.getCoordinates().length;
        final long start = System.currentTimeMillis();

        if ((geom.getCoordinates().length > 1000)) {
            if (CismapBroker.getInstance().getMappingComponent().getScaleDenominator() > 50000) {
                geom = simplifyGeometry(geom, 30);
            } else if (CismapBroker.getInstance().getMappingComponent().getScaleDenominator() > 25000) {
                geom = simplifyGeometry(geom, 15);
            } else {
                geom = simplifyGeometry(geom, 5);
            }
        }
//        LOG.error("simpifiziert: " + CismapBroker.getInstance().getMappingComponent().getScaleDenominator() + " z: "
//                    + (System.currentTimeMillis() - start) + " s: " + s + " a: " + geom.getCoordinates().length);

        return geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom       DOCUMENT ME!
     * @param   precision  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry simplifyGeometry(final Geometry geom, final double precision) {
//        final long start = System.currentTimeMillis();
        final Geometry simplifiedGeometry = DouglasPeuckerSimplifier.simplify(geom, precision);
//        System.out.println("preserverTopo: " + (System.currentTimeMillis() - start));

        if (LOG.isDebugEnabled()) {
            LOG.debug("length of the geometry: " + geom.getCoordinates().length + " "
                        + " Geometry will be simplified to a length of: "
                        + simplifiedGeometry.getCoordinates().length);
        }
        return simplifiedGeometry;
    }
}
