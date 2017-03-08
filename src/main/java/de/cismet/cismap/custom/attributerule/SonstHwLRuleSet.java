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
public class SonstHwLRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new WatergisDefaultRuleSet.Geom(true, false));
        typeMap.put("ww_gr", new WatergisDefaultRuleSet.Catalogue("k_ww_gr", false, true));
        typeMap.put("nr", new Numeric(16, 0, false, true));
        typeMap.put("wann", new Varchar(16, false, true));
        typeMap.put("wer", new Varchar(250, true, true));
        typeMap.put("firma", new Varchar(250, false, true));
        typeMap.put("vorwahl", new Varchar(10, false, true));
        typeMap.put("nummer", new Varchar(20, false, true));
        typeMap.put("mail", new Varchar(50, false, true));
        typeMap.put("text", new Varchar(250, true, true));
        typeMap.put("gewaesser", new Varchar(250, false, true));
        typeMap.put("station", new Numeric(10, 2, false, true));
        typeMap.put("koord_rw", new Numeric(11, 2, false, true));
        typeMap.put("koord_hw", new Numeric(10, 2, false, true));
        typeMap.put("bearb_wann", new Varchar(16, false, true));
        typeMap.put("bearb_wer", new Varchar(250, false, true));
        typeMap.put("bearb_komm", new Varchar(250, false, true));
        typeMap.put("fis_g_date", new WatergisDefaultRuleSet.DateTime(false, false));
        typeMap.put("fis_g_user", new WatergisDefaultRuleSet.Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        if (columnName.equals("bearb_wann") || columnName.equals("bearb_wer") || columnName.equals("bearb_komm")) {
            return SessionManager.getSession().getUser().getUserGroup().getName().equals("lung_edit1")
                        || SessionManager.getSession()
                        .getUser()
                        .getUserGroup()
                        .getName()
                        .equalsIgnoreCase("Administratoren");
        } else {
            return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                        && !columnName.equals("nr") && !columnName.equals("wann") && !columnName.equals("geom")
                        && !columnName.equals("id");
        }
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
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
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            if ((isValueEmpty(feature.getProperty("mail")))
                        && ((isValueEmpty(feature.getProperty("vorwahl")))
                            || (isValueEmpty(feature.getProperty("nummer"))))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Es muss immer entweder das Mail-Feld oder das Vorwahl- und Nummer-Feld gesetzt sein.");

                return false;
            }
        }

        return super.prepareForSave(features);
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
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.LINESTRING, properties);
    }
}
