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

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaGerogRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("l_gew", new Numeric(10, 2, false, false));
        typeMap.put("l_gew_o", new Numeric(10, 2, false, false));
        typeMap.put("l_gew_gero", new Numeric(10, 2, false, false));
        typeMap.put("typ", new Varchar(10, false, false));
        typeMap.put("fl_so", new Numeric(10, 2, false, false));
        typeMap.put("fl_b", new Numeric(10, 2, false, false));
        typeMap.put("fl_ger", new Numeric(10, 2, false, false));
        typeMap.put("fl_gew", new Numeric(10, 2, false, false));
        typeMap.put("fl_bn", new Numeric(10, 2, false, false));
        typeMap.put("fl_bt", new Numeric(10, 2, false, false));
        typeMap.put("fl_so_geo", new Numeric(10, 2, false, false));
        typeMap.put("fl_ger_geo", new Numeric(10, 2, false, false));
        typeMap.put("fl_gew_geo", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return super.getCellEditor(columnName);
    }
}
