/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.custom.tostringconverter.dlm25w;

import de.cismet.cids.tools.CustomToStringConverter;
import de.cismet.cids.tools.tostring.CidsLayerFeatureToStringConverter;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Dlm25wKSbToStringConverter extends CustomToStringConverter implements CidsLayerFeatureToStringConverter {

    //~ Methods ----------------------------------------------------------------

// private static final Logger LOG = Logger.getLogger(BewirtschaftungsendeToStringConverter.class);
// private static Map<String, String> map = new Hashtable<String, String>();

    @Override
    public String createString() {
        final String name = String.valueOf(cidsBean.getProperty("sb"));

        return name;
    }

    @Override
    public String featureToString(final Object feature) {
        if (feature instanceof CidsLayerFeature) {
            final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
            final String name = String.valueOf(cidsFeature.getProperty("sb"));

            return name;
        }

        return "unbenannt";
    }
}
