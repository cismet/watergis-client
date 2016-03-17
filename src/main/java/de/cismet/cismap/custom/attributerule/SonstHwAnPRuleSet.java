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

import org.deegree.datatypes.Types;

import java.sql.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
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
public class SonstHwAnPRuleSet extends DefaultAttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("nr") && !columnName.equals("wann") && !columnName.equals("geom");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (column.equals("wer") && (newValue == null)) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut wer darf nicht leer sein");
            return oldValue;
        }
        if (column.equals("text") && (newValue == null)) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut text darf nicht leer sein");
            return oldValue;
        }
        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return null;
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ww_gr")) {
            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true));
        }
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
        for (final FeatureServiceFeature feature : features) {
            if (feature.getProperty("wer") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wer darf nicht leer sein");

                return false;
            }
            if ((feature.getProperty("mail") == null)
                        && ((feature.getProperty("vorwahl") == null) || (feature.getProperty("nummer") == null))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Es muss immer entweder das Mail-Feld oder das Vorwahl- und Nummer-Feld gesetzt sein.");

                return false;
            }

            if (feature.getProperty("text") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut text darf nicht leer sein.");

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
    public FeatureCreator getFeatureCreator() {
        final Map properties = new HashMap();
        properties.put("nr", "@id");
        properties.put("wann", new Timestamp(System.currentTimeMillis()));
        if ((AppBroker.getInstance().getOwnWwGrList() != null) && !AppBroker.getInstance().getOwnWwGrList().isEmpty()) {
            properties.put("ww_gr", AppBroker.getInstance().getOwnWwGrList().get(0));
        } else {
            properties.put("ww_gr", AppBroker.getInstance().getNiemandWwGr());
        }
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT, properties);
    }
}
