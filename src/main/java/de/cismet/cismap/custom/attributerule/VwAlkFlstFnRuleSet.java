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
public class VwAlkFlstFnRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("flst", new Varchar(10, true));
        typeMap.put("fl_nr", new Numeric(3, 0, true));
        typeMap.put("gmk_nr", new Numeric(6, 0, true));
        typeMap.put("gmk_name", new Varchar(50, true));
        typeMap.put("gmd_nr", new Numeric(8, 0, true));
        typeMap.put("gmd_name", new Varchar(50, true));
        typeMap.put("wbv", new Numeric(2, 0, true));
        typeMap.put("flst_fl", new Numeric(15, 4, true));
        typeMap.put("fn_gr", new Numeric(1, 0, true));
        typeMap.put("fn", new Numeric(4, 0, true));
        typeMap.put("fn_fl", new Numeric(16, 4, false, false));
        typeMap.put("fn_flst_an", new Numeric(5, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("fn_fl") && !columnName.equals("fn_flst_an") && !columnName.equals("geom");
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
        if (columnName.equals("fn")) {
            return new DefaultCellEditor(new JTextField());
        }
        return null;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[] { "fn_fl", "fn_flst_an" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("fn_fl")) {
            return -3;
        } else if (name.equals("fn_flst_an")) {
            return -4;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final String propertyName, final FeatureServiceFeature feature) {
        Double fn_fl = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            fn_fl = geom.getArea();
        }

        if (propertyName.equals("fn_fl")) {
            return Math.round(fn_fl * 10000) / 10000.0;
        } else if (propertyName.equals("fn_flst_an")) {
            Double value = null;

            final Double flst_fl = ((Double)feature.getProperty("flst_fl"));

            if (flst_fl != null) {
                value = fn_fl * 100 / flst_fl;
            }

            return round(value);
        }

        return null;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
    }
}
