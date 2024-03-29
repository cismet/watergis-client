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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class VwAlkFlstWbvGRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("fsk", new Varchar(20, true));
        typeMap.put("flst", new Varchar(10, false));
        typeMap.put("fl_nr", new Numeric(3, 0, false));
        typeMap.put("gmk_nr", new Numeric(6, 0, false));
        typeMap.put("gmk_name", new Varchar(50, false));
        typeMap.put("gmd_nr", new Numeric(8, 0, false));
        typeMap.put("gmd_name", new Varchar(50, false));
        typeMap.put("wbv", new Numeric(2, 0, false));
        typeMap.put("flst_fl", new Numeric(16, 0, false));
        typeMap.put("flst_g_an", new Numeric(5, 1, false, false));
        typeMap.put("flst_g_fl", new Numeric(16, 1, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();

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
                feature.getProperties().put("flst_g_fl", round(flst_g_fl, 4));
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
                feature.getProperties().put("flst_g_fl", round(flst_g_fl, 4));
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
        adjustFisGDateAndFisGUser(feature);

        Double flst_g_fl = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            flst_g_fl = geom.getArea();
            feature.getProperties().put("flst_g_fl", round(flst_g_fl, 4));
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
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
        c.setMinArea(MIN_AREA_SIZE);

        return c;
    }
}
