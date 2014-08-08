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

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import java.sql.Date;
import java.sql.Timestamp;


//import java.util.Date;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBakAeRuleSet extends DefaultAttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("id") && !columnName.equals("geom") && !columnName.equals("fg_bak");
    }

    @Override
    public Object afterEdit(final String column, final int row, final Object oldValue, final Object newValue) {
        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return null;
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("bis")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ae_code")) {
            return new CidsLayerReferencedComboEditor("ae_code");
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final TableModel model) {
        return true;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
//        feature.getProperties().put("fis_g_date", new Date(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public String[] getAdditionalFieldNames() {
//        return new String[] { "Area" };
        return new String[0];
    }

    @Override
    public Object getAdditionalFieldValue(final int index, final FeatureServiceFeature feature) {
//        Double value = null;
//
//        final Geometry geom = ((Geometry)feature.getProperties().get("geo_field"));
//        value = geom.getArea();
//
//        return value;
        return null;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
//        return Double.class;
        return null;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak");

        return new StationLineCreator("bis", routeMc, new LinearReferencingWatergisHelper());
    }
}
