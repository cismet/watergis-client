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
public class CwbodygeomRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("template", new Varchar(24, false, false));
        typeMap.put("eu_cd_cw", new Varchar(42, false, false));
        typeMap.put("apsfr_cd", new Varchar(24, false, false));
        typeMap.put("rbd_cd", new Varchar(4, false, false));
        typeMap.put("wa_cd", new Varchar(10, false, false));
        typeMap.put("planu_cd", new Varchar(10, false, false));
        typeMap.put("land_cd", new Varchar(4, false, false));
        typeMap.put("ins_when", new Varchar(8, false, false));
        typeMap.put("ins_by", new Varchar(15, false, false));
        typeMap.put("scale", new Varchar(1, false, false));
        typeMap.put("inv_land", new Varchar(49, false, false));
        typeMap.put("metadata", new Varchar(255, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }
}
