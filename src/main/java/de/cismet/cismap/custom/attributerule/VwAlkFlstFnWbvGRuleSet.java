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

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
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
public class VwAlkFlstFnWbvGRuleSet extends DefaultAttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("fn_flst_an") && !columnName.equals("fn_g_fl")
                    && !columnName.equals("geom") && !columnName.equals("id");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (newValue == null) {
            if (column.equals("flst") || column.equals("fl_nr") || column.equals("gmk_nr") || column.equals("gmk_name")
                        || column.equals("gmd_nr")
                        || column.equals("gmd_name") || column.equals("wbv") || column.equals("fn_gr")
                        || column.equals("fn") || column.equals("flst_g_an") || column.equals("flst_g_fl")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
                return oldValue;
            }
        }
        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return null;
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
        for (final FeatureServiceFeature feature : features) {
            if (feature.getProperty("flst") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut flst darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("fl_nr") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut fl_nr darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("gmk_nr") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut gmk_nr darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("gmk_name") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut gmk_name darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("gmd_nr") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut gmd_nr darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("gmd_name") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut gmd_name darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("wbv") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wbv darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("fn_gr") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wbv darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("fn") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wbv darf nicht leer sein");
                return false;
            }
        }
        return true;
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
        return new String[] { "fn_flst_an", "fn_g_fl" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("fn_flst_an")) {
            return -4;
        } else if (name.equals("fn_g_fl")) {
            return -3;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final String propertyName, final FeatureServiceFeature feature) {
        Double fn_g_fl = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            fn_g_fl = geom.getArea();
        }

        if (propertyName.equals("fn_g_fl")) {
            return fn_g_fl;
        } else if (propertyName.equals("fn_flst_an")) {
            Double value = null;

            final Double flst_fl = ((Double)feature.getProperty("flst_fl"));
            final Double fn_fl = ((Double)feature.getProperty("fn_fl"));

            if (flst_fl != null) {
                value = fn_fl * 100 / flst_fl;
            }

            return value;
        }

        return null;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON);
    }
}
