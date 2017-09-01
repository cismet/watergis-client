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
public class VwAlkFlstWbvGRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new WatergisDefaultRuleSet.Geom(true, false));
        typeMap.put("flst", new WatergisDefaultRuleSet.Varchar(10, true));
        typeMap.put("fl_nr", new WatergisDefaultRuleSet.Numeric(3, 0, true));
        typeMap.put("gmk_nr", new WatergisDefaultRuleSet.Numeric(6, 0, true));
        typeMap.put("gmk_name", new WatergisDefaultRuleSet.Varchar(50, true));
        typeMap.put("gmd_nr", new WatergisDefaultRuleSet.Numeric(8, 0, true));
        typeMap.put("gmd_name", new WatergisDefaultRuleSet.Varchar(50, true));
        typeMap.put("wbv", new Numeric(2, 0, true));
        typeMap.put("flst_fl", new WatergisDefaultRuleSet.Numeric(16, 4, true));
        typeMap.put("flst_g_an", new Numeric(5, 2, false, false));
        typeMap.put("flst_g_fl", new Numeric(16, 4, false, false));
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
        if ((column.equals("fl_nr") || column.equals("gmk_nr") || column.equals("gmd_nr") || column.equals("flst"))
                    && (newValue instanceof String)) {
            try {
                newValue = Integer.parseInt((String)newValue);
            } catch (NumberFormatException e) {
                // nothing to do
            }
        }

        if (newValue instanceof Geometry) {
            Double flst_g_fl = null;

            final Geometry geom = ((Geometry)newValue);

            if (geom != null) {
                flst_g_fl = geom.getArea();
                feature.getProperties().put("flst_g_fl", Math.round(flst_g_fl * 10000) / 10000.0);
                Double value = null;

                final Double flst_fl = ((Double)feature.getProperty("flst_fl"));

                if (flst_fl != null) {
                    value = flst_g_fl * 100 / flst_fl;
                    feature.getProperties().put("flst_g_an", round(value));
                } else {
                    feature.getProperties().put("flst_g_an", null);
                }
            } else {
                feature.getProperties().put("flst_g_fl", null);
                feature.getProperties().put("flst_g_an", null);
            }
        }

        if (column.equals("flst_fl")) {
            Double flst_g_fl = null;

            final Geometry geom = ((Geometry)feature.getProperty("geom"));

            if (geom != null) {
                flst_g_fl = geom.getArea();
                feature.getProperties().put("flst_g_fl", Math.round(flst_g_fl * 10000) / 10000.0);
                Double value = null;

                final Double flst_fl = ((Double)newValue);

                if (flst_fl != null) {
                    value = flst_g_fl * 100 / flst_fl;
                    feature.getProperties().put("flst_g_an", round(value));
                } else {
                    feature.getProperties().put("flst_g_an", null);
                }
            } else {
                feature.getProperties().put("flst_g_fl", null);
                feature.getProperties().put("flst_g_an", null);
            }
        }
        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("flst_g_an") && !columnName.equals("flst_g_fl") && !columnName.equals("geom");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("fl_nr")) {
            return new DefaultCellEditor(new JTextField());
        }
        if (columnName.equals("gmk_nr")) {
            return new DefaultCellEditor(new JTextField());
        }
        if (columnName.equals("gmd_nr")) {
            return new DefaultCellEditor(new JTextField());
        }
        if (columnName.equals("flst")) {
            return new DefaultCellEditor(new JTextField());
        }
        return null;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());

        Double flst_g_fl = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            flst_g_fl = geom.getArea();
            feature.getProperties().put("flst_g_fl", Math.round(flst_g_fl * 10000) / 10000.0);
            Double value = null;

            final Double flst_fl = ((Double)feature.getProperty("flst_fl"));

            if (flst_fl != null) {
                value = flst_g_fl * 100 / flst_fl;
                feature.getProperties().put("flst_g_an", round(value));
            } else {
                feature.getProperties().put("flst_g_an", null);
            }
        } else {
            feature.getProperties().put("flst_g_fl", null);
            feature.getProperties().put("flst_g_an", null);
        }
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
    }
}
