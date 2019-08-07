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
package de.cismet.cismap.custom.attributerule;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaPrAblRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, false));
        typeMap.put("ba_gn", new Varchar(50, false, false));
        typeMap.put("thema", new Varchar(10, false, false));
        typeMap.put("l_st", new Catalogue("k_l_st", false, false, new Varchar(10, false, false)));
        typeMap.put("l_rl", new Catalogue("k_l_rl", false, false, new Varchar(2, false, false)));
        typeMap.put("art", new Varchar(10, false, false));
        typeMap.put("material", new Varchar(10, false, false));
        typeMap.put("th_art_mat", new Varchar(30, false, false));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("obj_nr_gu", new Varchar(50, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, false, new Varchar(10, false, false)));
        typeMap.put("traeger_gu", new Varchar(50, false, false));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, false));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, false));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, false, true, new Numeric(1, 0, false, false)));
        typeMap.put("esw", new BooleanAsInteger(false, false));
        typeMap.put("bemerkung", new Varchar(250, false, false));
        typeMap.put("br", new Numeric(8, 3, false, false));
        typeMap.put("gefaelle", new Numeric(6, 2, false, false));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("wbbl")) {
            if ((value instanceof String) && (clickCount == 1)) {
                downloadDocumentFromWebDav(getWbblPath(), addExtension(value.toString(), "pdf"));
            }
        }
    }
}
