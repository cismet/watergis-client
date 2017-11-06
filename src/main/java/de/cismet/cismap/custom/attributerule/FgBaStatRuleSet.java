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
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaStatRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new WatergisDefaultRuleSet.Geom(true, false));
        typeMap.put("ww_gr", new WatergisDefaultRuleSet.Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new WatergisDefaultRuleSet.Varchar(50, false, false));
        typeMap.put("ba_st", new WatergisDefaultRuleSet.Numeric(10, 0, false, false));
        typeMap.put("ba_st_km", new WatergisDefaultRuleSet.Numeric(7, 1, false, false, true));
        typeMap.put("ba_st_c", new WatergisDefaultRuleSet.Varchar(9, false, false));
        typeMap.put("fis_g_date", new WatergisDefaultRuleSet.DateTime(false, false));
        typeMap.put("fis_g_user", new WatergisDefaultRuleSet.Varchar(50, false, false));
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
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        return true;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
//        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
//        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return null;
    }
}
