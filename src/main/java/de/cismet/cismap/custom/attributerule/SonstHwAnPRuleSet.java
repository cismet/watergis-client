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

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
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
public class SonstHwAnPRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, true));
        typeMap.put("nr", new Numeric(16, 0, false, true));
        typeMap.put("wann", new Varchar(16, false, false));
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
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        if (columnName.equals("ww_gr")) {
            return AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren");
        } else {
            return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                        && !columnName.equals("nr") && !columnName.equals("wann") && !columnName.equals("geom");
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ww_gr")) {
            CidsLayerFeatureFilter filter = null;

            if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren")) {
                final String userName = AppBroker.getInstance().getOwner();
                filter = new CidsLayerFeatureFilter() {

                        @Override
                        public boolean accept(final CidsLayerFeature bean) {
                            if (bean == null) {
                                return false;
                            }
                            return bean.getProperty("owner").equals(userName);
                        }
                    };
            } else {
                filter = new CidsLayerFeatureFilter() {

                        @Override
                        public boolean accept(final CidsLayerFeature feature) {
                            return feature != null;
                        }
                    };
            }
            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true),
                    filter);
        }
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();
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
        final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        properties.put("wann", formatter.format(new Date()));
        if ((AppBroker.getInstance().getOwnWwGr() != null)) {
            properties.put("ww_gr", AppBroker.getInstance().getOwnWwGr());
        } else {
            properties.put("ww_gr", AppBroker.getInstance().getNiemandWwGr());
        }
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT, properties);
    }
}
