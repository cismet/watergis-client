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

import java.sql.Date;


//import java.util.Date;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBakRuleSet extends DefaultAttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("id") && !columnName.equals("geom") && !columnName.equals("ww_gr");
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
        return null;
    }

    @Override
    public boolean prepareForSave(final TableModel model) {
        return true;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
//        final Object o = feature.getProperties().put("fis_g_date", new Date(System.currentTimeMillis()));
//        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[0];
    }

    @Override
    public Object getAdditionalFieldValue(final int index, final FeatureServiceFeature feature) {
        return null;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return null;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.LINESTRING);
    }
}