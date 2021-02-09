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

import Sirius.navigator.connection.SessionManager;

import java.util.GregorianCalendar;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SStandwtRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("template", new Varchar(24, false, false));
        typeMap.put("s_name", new Varchar(100, false, false));
        typeMap.put("eu_cd_ls", new Varchar(42, false, false));
        typeMap.put("lake_cat", new Varchar(6, false, false));
        typeMap.put("rbd_cd", new Varchar(4, false, false));
        typeMap.put("wa_cd", new Varchar(10, false, false));
        typeMap.put("land_cd", new Varchar(4, false, false));
        typeMap.put("ins_when", new Varchar(8, false, false));
        typeMap.put("ins_by", new Varchar(15, false, false));
        typeMap.put("scale", new Varchar(1, false, false));
        typeMap.put("inv_land", new Varchar(49, false, false));
        typeMap.put("metadata", new Varchar(255, false, false));
        typeMap.put("wbusername", new Varchar(255, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }

    @Override
    public FeatureServiceFeature[] prepareFeaturesForExport(final FeatureServiceFeature[] features) {
        final String username = SessionManager.getSession().getUser().getName();
        final GregorianCalendar date = new GregorianCalendar();

        final String dateString = date.get(GregorianCalendar.YEAR) + to2Digits((date.get(GregorianCalendar.MONTH) + 1))
                    + to2Digits(date.get(GregorianCalendar.DATE));

        for (final FeatureServiceFeature f : features) {
            f.setProperty("ins_when", dateString);
            f.setProperty("ins_by", username);
        }

        return features;
    }
}
