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
public class VwAlkFlstRuleSet extends WatergisDefaultRuleSet {

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
        typeMap.put("flst_fl", new Numeric(16, 4, false, false));
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
            // the geometry has changed
            final Geometry geom = ((Geometry)feature.getProperty("geom"));

            if (geom != null) {
                final Double flst_fl = Math.round(geom.getArea() * 10000) / 10000.0;
                feature.getProperties().put("flst_fl", flst_fl);
            }
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("flst_fl") && !columnName.equals("geom") && !columnName.equals("id");
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

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            final Double flst_fl = Math.round(geom.getArea() * 10000) / 10000.0;
            feature.getProperties().put("flst_fl", flst_fl);
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
