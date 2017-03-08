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

import java.util.List;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgLakPrPfRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("la_cd", new Numeric(20, 0, true, false));
        typeMap.put("lak_st", new Numeric(10, 2, false, false));
        typeMap.put("la_cd_k", new Numeric(20, 0, true, false));
        typeMap.put("la_gn", new Varchar(75, true, false));
        typeMap.put("la_gn_t", new Numeric(1, 0, true, false));
        typeMap.put("la_lage", new Varchar(1, true, false));
        typeMap.put("la_ordn", new Numeric(2, 0, true, false));
        typeMap.put("la_wrrl", new Numeric(1, 0, true, false));
        typeMap.put("winkel", new Numeric(5, 1, true, false));
        typeMap.put("kn", new Numeric(2, 0, false, false));
        typeMap.put("ezg_fl", new Numeric(12, 0, false, false));
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
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        return super.prepareForSave(features);
    }
}
