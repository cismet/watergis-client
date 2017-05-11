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

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaGeroRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, false));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, false));
        typeMap.put("l_st", new Varchar(10, false, false));
        typeMap.put("profil", new Varchar(2, false, false));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("traeger", new Varchar(10, false, false));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, false));
        typeMap.put("zust_kl", new Numeric(1, 0, false, false, true));
        typeMap.put("bemerkung", new Varchar(250, false, false));
        typeMap.put("br_so", new Numeric(6, 2, false, false));
        typeMap.put("ho_e", new Numeric(6, 2, false, false));
        typeMap.put("ho_a", new Numeric(6, 2, false, false));
        typeMap.put("gefaelle", new Numeric(6, 2, false, false));
        typeMap.put("bv_re", new Numeric(4, 2, false, false));
        typeMap.put("bh_re", new Numeric(4, 2, false, false));
        typeMap.put("bl_re", new Numeric(4, 2, false, false));
        typeMap.put("bv_li", new Numeric(4, 2, false, false));
        typeMap.put("bh_li", new Numeric(4, 2, false, false));
        typeMap.put("bl_li", new Numeric(4, 2, false, false));
        typeMap.put("mw", new Numeric(4, 2, false, false));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("typ", new Varchar(10, false, false));
        typeMap.put("fl_so", new Numeric(10, 2, false, false));
        typeMap.put("fl_b_re", new Numeric(10, 2, false, false));
        typeMap.put("fl_b_li", new Numeric(10, 2, false, false));
        typeMap.put("fl_b", new Numeric(10, 2, false, false));
        typeMap.put("fl_ger", new Numeric(10, 2, false, false));
        typeMap.put("fl_qs_ger", new Numeric(10, 2, false, false));
        typeMap.put("br_gew_re", new Numeric(6, 2, false, false));
        typeMap.put("br_gew_li", new Numeric(6, 2, false, false));
        typeMap.put("br_gew", new Numeric(6, 2, false, false));
        typeMap.put("fl_gew", new Numeric(10, 2, false, false));
        typeMap.put("fl_qs_gew", new Numeric(10, 2, false, false));
        typeMap.put("bl_n_re", new Numeric(4, 2, false, false));
        typeMap.put("bl_t_re", new Numeric(4, 2, false, false));
        typeMap.put("bl_n_li", new Numeric(4, 2, false, false));
        typeMap.put("bl_t_li", new Numeric(4, 2, false, false));
        typeMap.put("fl_bn_re", new Numeric(10, 2, false, false));
        typeMap.put("fl_bt_re", new Numeric(10, 2, false, false));
        typeMap.put("fl_bn_li", new Numeric(10, 2, false, false));
        typeMap.put("fl_bt_li", new Numeric(10, 2, false, false));
        typeMap.put("fl_bn", new Numeric(10, 2, false, false));
        typeMap.put("fl_bt", new Numeric(10, 2, false, false));
        typeMap.put("fl_n", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return super.getCellEditor(columnName);
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
