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

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
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
public class FgBaKrRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        Numeric esw = new Numeric(1, 0, false, true);
        esw.setRange(0.0, 1.0);
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true));
        typeMap.put("l_oiu", new Catalogue("k_l_oiu", false, true));
        typeMap.put("kr", new Catalogue("k_kr", true, true));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true));
        typeMap.put("esw", esw);
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br", new Numeric(4, 2, false, true));
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
        if (isValueEmpty(newValue)) {
            if (column.equals("kr")) {
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

        if (column.equals("br") && !checkRange(column, newValue, 0, 50, 0, 100, true, false, true)) {
            return oldValue;
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("wbbl")) {
            return new LinkTableCellRenderer();
        } else {
            return super.getCellRenderer(columnName);
        }
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
        } else if (columnName.equals("kr")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("kr") + " - " + bean.getProperty("name");
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
        } else if (columnName.equals("l_oiu")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("l_oiu") + " - " + bean.getProperty("name");
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
            if (isValueEmpty(feature.getProperty("kr"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut kr darf nicht leer sein");
                return false;
            }
            if (!checkRange("br", feature.getProperty("br"), 0, 100, true, false, true)) {
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

            if ((feature.getProperty("kr") != null) && feature.getProperty("kr").toString().equals("Br")) {
                if ((feature.getProperty("l_oiu") != null) && !feature.getProperty("l_oiu").toString().equals("o")) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut kr = Br, dann muss das Attribut l_oiu = o.");
                    return false;
                }
            }

            if ((feature.getProperty("kr") != null) && feature.getProperty("kr").toString().equals("U")) {
                if ((feature.getProperty("l_oiu") != null) && !feature.getProperty("l_oiu").toString().equals("u")) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut kr = U, dann muss das Attribut l_oiu = u.");
                    return false;
                }
            }
        }

        return super.prepareForSave(features);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());

        if (isValueIn(feature.getProperty("kr"), new Object[] { "Br" }, false)) {
            final Object rl = feature.getProperty("l_oiu");

            if (rl == null) {
                feature.setProperty("l_oiu", getCatalogueElement("dlm25w.k_l_oiu", "l_loiu", "o"));
            }
        }

        if (isValueIn(feature.getProperty("kr"), new Object[] { "U" }, false)) {
            final Object rl = feature.getProperty("l_oiu");

            if (rl == null) {
                feature.setProperty("l_oiu", getCatalogueElement("dlm25w.k_l_oiu", "l_loiu", "u"));
            }
        }
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");

        return new StationCreator("ba_st", routeMc, new LinearReferencingWatergisHelper());
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
