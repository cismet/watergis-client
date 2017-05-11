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

import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.watergis.gui.actions.reports.GewaesserReportAction;

import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(false, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, true));
        // wenn ba_cd == null, dann wird es automatisch gefuellt (in prepareforSave)
        typeMap.put("ba_cd", new Varchar(50, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, false));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, false));
        typeMap.put("ba_gn", new Varchar(50, false));
        typeMap.put("wdm", new Numeric(4, 0, false));
        typeMap.put("gu_zust", new Varchar(2, false));
        typeMap.put("gu_cd", new Varchar(50, false));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
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
        if (columnName.equals("ba_cd")) {
            return new LinkTableCellRenderer();
        } else {
            return super.getCellRenderer(columnName);
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[] { "laenge" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("laenge")) {
            return -3;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Double value = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            value = round(geom.getLength());
        }

        return value;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("ba_cd")) {
            if ((value instanceof String) && (clickCount == 1)) {
                createGuReport(value.toString());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bacd  DOCUMENT ME!
     */
    private void createGuReport(final String bacd) {
        final GewaesserReportAction action = new GewaesserReportAction();
        action.actionPerformed(null);
    }
}
