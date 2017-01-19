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

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Timestamp;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.PointAndStationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.linearreferencing.RouteTableCellEditor;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinearReferencingWatergisHelper;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class MnOwPegelRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MnOwPegelRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, true));
        typeMap.put("la_cd", new Numeric(15, 0, false, false));
        typeMap.put("la_st", new Numeric(10, 2, false, false));
        typeMap.put("gwk_lawa", new Numeric(15, 0, false, false));
        typeMap.put("station", new Numeric(10, 2, false, false));
        typeMap.put("ms_nr", new Varchar(20, false, true));
        typeMap.put("ms_nr_wsa", new Varchar(20, false, true));
        typeMap.put("ms_name", new Varchar(50, true, true));
        typeMap.put("re", new Numeric(11, 2, true, true));
        typeMap.put("ho", new Numeric(10, 2, true, true));
        typeMap.put("typ", new Varchar(2, true, true));
        typeMap.put("gbk_lawa", new Numeric(15, 0, false, true));
        typeMap.put("gwk_gn", new Varchar(60, false, true));
        typeMap.put("see_gn", new Varchar(50, false, true));
        typeMap.put("ezg_fl", new Numeric(12, 0, false, true));
        typeMap.put("ezg_fl_d", new Numeric(12, 0, false, true));
        typeMap.put("ezg_fl_dp", new Numeric(6, 2, false, true));
        typeMap.put("steckbrief", new Link(250, false, true));
        typeMap.put("ganglin_w", new Link(250, false, true));
        typeMap.put("ganglin_q", new Link(250, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("geom") && !columnName.equals("la_cd") && !columnName.equals("la_st");
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
        if (columnName.equals("steckbrief") || columnName.equals("ganglin_w") || columnName.equals("ganglin_q")) {
            return new LinkTableCellRenderer();
        } else {
            return super.getCellRenderer(columnName);
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ba_cd")) {
            return new RouteTableCellEditor("dlm25w.fg_ba", "ba_st", false);
        } else if (columnName.equals("ba_st")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("station")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ww_gr")) {
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
            if ((isValueEmpty(feature.getProperty("ms_nr"))) && (isValueEmpty(feature.getProperty("ms_nr_wsa")))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Die Attribute ms_nr und ms_nr_wsa dürfen nicht beide leer sein.");
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
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");

        return new PointAndStationCreator("ba_st", null, routeMc, new LinearReferencingWatergisHelper());
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("steckbrief") || columnName.equals("ganglin_w") || columnName.equals("ganglin_q")) {
            if ((value instanceof String) && (clickCount == 1)) {
                try {
                    final URL u = new URL(value.toString());

                    try {
                        de.cismet.tools.BrowserLauncher.openURL(u.toString());
                    } catch (Exception ex) {
                        LOG.error("Cannot open the url:" + u, ex);
                    }
                } catch (MalformedURLException ex) {
                    // nothing to do
                }
            }
        }
    }

    @Override
    public boolean isCatThree() {
        return true;
    }
}
