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

import Sirius.navigator.connection.SessionManager;

import com.vividsolutions.jts.geom.Geometry;

import java.sql.Timestamp;

import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class VwAlkGmdRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new WatergisDefaultRuleSet.Geom(true, false));
        typeMap.put("gmd_nr", new WatergisDefaultRuleSet.Numeric(8, 0, true, true, "gmd_nr", "vw_alk_gmd"));
        typeMap.put("gmd_name", new WatergisDefaultRuleSet.Varchar(50, true));
        typeMap.put("amt_nr", new WatergisDefaultRuleSet.Numeric(4, 0, true));
        typeMap.put("amt_name", new WatergisDefaultRuleSet.Varchar(50, true));
        typeMap.put("kreis_nr", new WatergisDefaultRuleSet.Numeric(5, 0, true));
        typeMap.put("kreis_name", new WatergisDefaultRuleSet.Varchar(50, true));
        typeMap.put("gmd_fl", new WatergisDefaultRuleSet.Numeric(12, 0, false, false));
        typeMap.put("gmd_wbv_01", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_02", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_04", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_05", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_06", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_07", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_08", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_09", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_10", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_11", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_12", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_13", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_14", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_15", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_16", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_17", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_18", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_19", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_20", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_22", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_23", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_24", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_25", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_27", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_28", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_30", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("gmd_wbv_31", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("fis_g_date", new WatergisDefaultRuleSet.DateTime(false, false));
        typeMap.put("fis_g_user", new WatergisDefaultRuleSet.Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();

        if ((column.equals("gmd_nr") || column.equals("kreis_nr") || column.equals("amt_nr"))
                    && (newValue instanceof String)) {
            try {
                newValue = Integer.parseInt((String)newValue);
            } catch (NumberFormatException e) {
                // nothing to do
            }
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("gmd_fl") && !columnName.equals("geom")
                    && !columnName.equals("id");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("kreis_nr")) {
            return new DefaultCellEditor(new JTextField());
        }
        if (columnName.equals("amt_nr")) {
            return new DefaultCellEditor(new JTextField());
        }
        if (columnName.equals("gmd_nr")) {
            return new DefaultCellEditor(new JTextField());
        }
        return null;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[] { "gmd_fl" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("gmd_fl")) {
            return -3;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Long value = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            value = Math.round(geom.getArea());
        }

        return value;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("gmd_fl")) {
            return "round(st_area(geom))";
        } else {
            return null;
        }
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Long.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
        c.setMinArea(MIN_AREA_SIZE);

        return c;
    }
}
