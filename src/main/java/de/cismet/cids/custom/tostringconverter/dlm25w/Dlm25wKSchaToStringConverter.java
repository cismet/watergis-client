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
public class Dlm25wKSchaToStringConverter extends CustomToStringConverter implements CidsLayerFeatureToStringConverter {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String createString() {
        String name = String.valueOf(cidsBean.getProperty("scha"));

        if (name == null) {
            name = "unbenannt";
        }

        return name;
    }

    @Override
    public String featureToString(final Object feature) {
        if (feature instanceof CidsLayerFeature) {
            final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
            String name = String.valueOf(cidsFeature.getProperty("scha"));

            if (name == null) {
                name = "unbenannt";
            }

            return name;
        }

        return "unbenannt";
    }
}
