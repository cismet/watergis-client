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

import org.deegree.datatypes.Types;

import java.sql.Timestamp;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.StationCreationCheck;
import de.cismet.cismap.cidslayer.StationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaSchaRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true));
        typeMap.put("name", new Varchar(250, false, false));
        typeMap.put("scha", new Catalogue("k_scha", true, true));
        typeMap.put("material", new Catalogue("k_material", false, true));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("obj_nr_gu", new Varchar(50, false, true));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true));
        typeMap.put("traeger_gu", new Varchar(50, false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("ho_so", new Numeric(6, 2, false, true));
        typeMap.put("ho_d_so_ok", new Numeric(4, 2, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("ww_gr") && !columnName.equals("ba_cd") && !columnName.equals("geom")
                    && !columnName.equals("obj_nr");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (isValueEmpty(newValue)) {
            if (column.equals("scha")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
                return oldValue;
            }
        }

        if (column.equals("ausbaujahr")
                    && !checkRange(
                        column,
                        newValue,
                        1950,
                        getCurrentYear(),
                        1800,
                        getCurrentYear()
                        + 2,
                        true,
                        true,
                        true)) {
            return oldValue;
        }

        if (column.equals("ho_so") && !checkRange(column, newValue, -6, 179, true, true, true)) {
            return oldValue;
        }

        if (column.equals("ho_d_so_ok") && !checkRange(column, newValue, 0, 10, true, false, true)) {
            return oldValue;
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ba_st")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("traeger")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("traeger") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("zust_kl")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("zust_kl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("scha")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("scha") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("l_st")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("nicht_qp");
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("l_st") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("material")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("scha");

            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("material") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();
            if (isValueEmpty(feature.getProperty("scha"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut scha darf nicht leer sein");
                return false;
            }

            if (
                !checkRange(
                            "ausbaujahr",
                            feature.getProperty("ausbaujahr"),
                            1800,
                            getCurrentYear()
                            + 2,
                            true,
                            true,
                            true)) {
                return false;
            }
            if (!checkRange("ho_so", feature.getProperty("ho_so"), -6, 179, true, true, true)) {
                return false;
            }
            if (!checkRange("ho_d_so_ok", feature.getProperty("ho_d_so_ok"), 0, 10, true, false, true)) {
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
        final StationCreationCheck check = new OnOwnRouteStationCheck();

        final StationCreator creator = new StationCreator(
                "ba_st",
                routeMc,
                "Basisgewässer (FG)",
                new LinearReferencingWatergisHelper());
        creator.setCheck(check);

        return creator;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("wbbl")) {
            if ((value instanceof String) && (clickCount == 1)) {
                downloadDocumentFromWebDav(getWbblPath(), addExtension(value.toString(), "pdf"));
            }
        }
    }
}
