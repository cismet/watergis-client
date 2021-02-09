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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SWatercsRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new WatergisDefaultRuleSet.Geom(true, true));
        typeMap.put("template", new WatergisDefaultRuleSet.Varchar(24, false));
        typeMap.put("s_name", new WatergisDefaultRuleSet.Varchar(100, false));
        typeMap.put("eu_seg_cd", new WatergisDefaultRuleSet.Varchar(42, false));
        typeMap.put("continua", new WatergisDefaultRuleSet.Varchar(1, false));
        typeMap.put("flowdir", new WatergisDefaultRuleSet.Varchar(19, false));
        typeMap.put("river_cd", new WatergisDefaultRuleSet.Varchar(20, false));
        typeMap.put("rbd_cd", new WatergisDefaultRuleSet.Varchar(4, false));
        typeMap.put("wa_cd", new WatergisDefaultRuleSet.Varchar(10, false));
        typeMap.put("land_cd", new WatergisDefaultRuleSet.Varchar(4, false));
        typeMap.put("ins_when", new WatergisDefaultRuleSet.Varchar(8, false));
        typeMap.put("ins_by", new WatergisDefaultRuleSet.Varchar(15, false));
        typeMap.put("scale", new WatergisDefaultRuleSet.Varchar(1, false));
        typeMap.put("inv_land", new WatergisDefaultRuleSet.Varchar(49, false));
        typeMap.put("metadata", new WatergisDefaultRuleSet.Varchar(255, false));
        typeMap.put("wbusername", new WatergisDefaultRuleSet.Varchar(254, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }
}
