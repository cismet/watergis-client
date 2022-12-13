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

import org.apache.log4j.Logger;

import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpPkteRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(QpPkteRuleSet.class);
    private static final String[] ALLOWED_CALC_VALUES = { "calc" };

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("p_nr", new Numeric(20, 0, false, false));
        typeMap.put("qp_nr", new Numeric(20, 0, false, false));
        typeMap.put("qp_hist", new Varchar(1, true, false));
        typeMap.put("upl_nr", new Numeric(20, 0, false, false));
        typeMap.put("l_calc", new Varchar(4, false, false, ALLOWED_CALC_VALUES));
        typeMap.put("gaf_id", new Varchar(50, true, false));
        typeMap.put("stat", new Numeric(10, 2, false, false));
        typeMap.put("y", new Numeric(6, 2, false, false));
        typeMap.put("z", new Numeric(6, 2, false, false));
        typeMap.put("kz", new Varchar(10, true, false));
        typeMap.put("rk", new Numeric(10, 0, false, false));
        typeMap.put("bk", new Numeric(10, 0, false, false));
        typeMap.put("hw", new Numeric(10, 2, false, false));
        typeMap.put("rw", new Numeric(11, 2, false, false));
        typeMap.put("hyk", new Varchar(10, true, false));
        typeMap.put("bemerkung", new Varchar(250, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return columnName.equalsIgnoreCase("bemerkung");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return null;
    }
}
