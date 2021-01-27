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
public class DrainbasinRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("template", new Varchar(24, false, false));
        typeMap.put("drain_cd", new Varchar(89, false, false));
        typeMap.put("b_name", new Varchar(255, false, false));
        typeMap.put("geb_kz", new Varchar(24, false, false));
        typeMap.put("drain_na", new Varchar(70, false, false));
        typeMap.put("gew_kz", new Varchar(24, false, false));
        typeMap.put("descr_from", new Varchar(255, false, false));
        typeMap.put("descr_to", new Varchar(255, false, false));
        typeMap.put("comments", new Varchar(255, false, false));
        typeMap.put("area_nom", new Numeric(12, 6, false, false));
        typeMap.put("pegel_cd", new Varchar(100, false, false));
        typeMap.put("service", new Varchar(100, false, false));
        typeMap.put("eu_cd_wb", new Varchar(42, false, false));
        typeMap.put("planu_cd", new Varchar(24, false, false));
        typeMap.put("uom_cd", new Varchar(24, false, false));
        typeMap.put("wa_cd", new Varchar(10, false, false));
        typeMap.put("rbd_cd", new Varchar(4, false, false));
        typeMap.put("ch_date", new DateType(false, false));
        typeMap.put("scale", new Varchar(1, false, false));
        typeMap.put("conf_cd", new Varchar(1, false, false));
        typeMap.put("snap_dlm", new Varchar(1, false, false));
        typeMap.put("land_cd", new Varchar(4, false, false));
        typeMap.put("metadata", new Varchar(255, false, false));
        typeMap.put("url", new Varchar(255, false, false));
        typeMap.put("wbusername", new Varchar(254, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }
}
