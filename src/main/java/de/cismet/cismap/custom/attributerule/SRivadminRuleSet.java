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
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SRivadminRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new WatergisDefaultRuleSet.Geom(true, true));
        typeMap.put("template", new WatergisDefaultRuleSet.Varchar(24, false));
        typeMap.put("adm_pt_cd", new WatergisDefaultRuleSet.Varchar(89, false));
        typeMap.put("pt_type", new WatergisDefaultRuleSet.Varchar(1, false));
        typeMap.put("lat", new WatergisDefaultRuleSet.Numeric(9, 6, false));
        typeMap.put("lon", new WatergisDefaultRuleSet.Numeric(9, 6, false));
        typeMap.put("coord_stat", new WatergisDefaultRuleSet.Varchar(1, false));
        typeMap.put("coord_sdat", new WatergisDefaultRuleSet.Varchar(8, false));
        typeMap.put("comments", new WatergisDefaultRuleSet.Varchar(255, false));
        typeMap.put("measure", new WatergisDefaultRuleSet.Numeric(9, 5, false));
        typeMap.put("scale", new WatergisDefaultRuleSet.Varchar(1, false));
        typeMap.put("river_cd", new WatergisDefaultRuleSet.Varchar(20, false));
        typeMap.put("inv_land", new WatergisDefaultRuleSet.Varchar(49, false));
        typeMap.put("rbd_cd", new WatergisDefaultRuleSet.Varchar(4, false));
        typeMap.put("wa_cd", new WatergisDefaultRuleSet.Varchar(10, false));
        typeMap.put("planu_cd", new WatergisDefaultRuleSet.Varchar(10, false));
        typeMap.put("land_cd", new WatergisDefaultRuleSet.Varchar(4, false));
        typeMap.put("ins_when", new WatergisDefaultRuleSet.Varchar(8, false));
        typeMap.put("ins_by", new WatergisDefaultRuleSet.Varchar(15, false));
        typeMap.put("metadata", new WatergisDefaultRuleSet.Varchar(255, false));
        typeMap.put("wbusername", new WatergisDefaultRuleSet.Varchar(254, false));
        typeMap.put("fis_g_date", new WatergisDefaultRuleSet.DateTime(false, false));
        typeMap.put("fis_g_user", new WatergisDefaultRuleSet.Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("geom") && !columnName.equals("id") && !columnName.equals("fis_g_date")
                    && !columnName.equals("fis_g_user");
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT, false);

        return c;
    }
}
