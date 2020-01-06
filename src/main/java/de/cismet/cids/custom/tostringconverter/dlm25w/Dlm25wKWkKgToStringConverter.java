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
public class Dlm25wKWkKgToStringConverter extends CustomToStringConverter implements CidsLayerFeatureToStringConverter {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String createString() {
        String name = (String)cidsBean.getProperty("wk_nr");

        if (name == null) {
            name = "unbenannt";
        }

        return name;
    }

    @Override
    public String featureToString(final Object feature) {
        if (feature instanceof CidsLayerFeature) {
            final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
            String name = (String)cidsFeature.getProperty("wk_nr");

            if (name == null) {
                name = "unbenannt";
            }

            return name;
        }

        return "unbenannt";
    }
}
