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
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SgSeeWkRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("wk_nr", new Varchar(10, true, true));
        typeMap.put("see_gn", new Varchar(50, true, true));
        typeMap.put("see_lawa", new Varchar(20, true, true));
        typeMap.put("see_sp", new Varchar(8, true, true));
        typeMap.put("tmax", new Numeric(5, 2, false, true));
        typeMap.put("td", new Numeric(5, 2, false, true));
        typeMap.put("vol", new Numeric(10, 0, false, true));
        typeMap.put("tg", new Numeric(6, 3, false, true));
        typeMap.put("ue", new Numeric(6, 3, false, true));
        typeMap.put("ul", new Numeric(7, 3, false, true));
        typeMap.put("leff", new Numeric(6, 3, false, true));
        typeMap.put("beff", new Numeric(6, 3, false, true));
        typeMap.put("tabelle", new Varchar(50, false, true));
        typeMap.put("flaeche", new Numeric(12, 0, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("flaeche") && !columnName.equals("geom") && !columnName.equals("id");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (isValueEmpty(newValue)) {
            if (column.equalsIgnoreCase("wk_nr")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut ms_cd_lw darf nicht leer sein");
                return oldValue;
            } else if (column.equalsIgnoreCase("see_gn")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut see_gn darf nicht leer sein");
                return oldValue;
            } else if (column.equalsIgnoreCase("see_lawa")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut see_lawa darf nicht leer sein");
                return oldValue;
            } else if (column.equalsIgnoreCase("see_sp")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut see_sp darf nicht leer sein");
                return oldValue;
            }
        }
        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("tabelle")) {
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
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            if (isValueEmpty(feature.getProperty("wk_nr"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wk_nr darf nicht leer sein");
                return false;
            } else if (isValueEmpty(feature.getProperty("see_gn"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut see_gn darf nicht leer sein");
                return false;
            } else if (isValueEmpty(feature.getProperty("see_lawa"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut gbk_lawa darf nicht leer sein");
                return false;
            } else if (isValueEmpty(feature.getProperty("see_sp"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut see_sp darf nicht leer sein");
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
        return new String[] { "flaeche" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("flaeche")) {
            return -3;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Integer value = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            value = (int)geom.getArea();
        }

        return value;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Integer.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("tabelle")) {
            if ((value instanceof String) && (clickCount == 1)) {
                downloadDocumentFromWebDav(getSgLinkTablePath(), addExtension(value.toString(), "xlsx"));
            }
        }
    }
}
