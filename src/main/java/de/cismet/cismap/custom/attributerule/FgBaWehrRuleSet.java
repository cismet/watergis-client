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

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.tools.CidsBeanFilter;

import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.StationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractBeanListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaWehrRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Map<String, String[]> allowedWehrV = new HashMap<String, String[]>();
    private static final Map<String, String[]> allowedWehrAV = new HashMap<String, String[]>();
    private static final Map<String, String[]> materialV = new HashMap<String, String[]>();

    static {
        allowedWehrV.put("s-kbw", new String[] { "bo", "bo-j", "schü" });
        allowedWehrV.put("s-sbw", new String[] { "bo", "bo-j", "schü" });
        allowedWehrV.put("s-stw", new String[] { "bo", "bo-j", "schü" });
        allowedWehrV.put("s-moe", new String[] { "bo", "bo-j", "schü" });
        allowedWehrV.put("w-strei", new String[] { "schw" });
        allowedWehrV.put("w-üfa", new String[] { "schw" });
        allowedWehrV.put("kl", new String[] { "ki", "ki-fb", "ki-fb-schü", "ki-schü" });
        allowedWehrV.put("na", new String[] { "na" });
        allowedWehrV.put("seg", new String[] { "seg", "seg-fb" });
        allowedWehrV.put("sek", new String[] { "sek" });
        allowedWehrV.put("schl", new String[] { "schl" });
        allowedWehrV.put("w-schü", new String[] { "schü", "schü-dop", "schü-dreh", "schü-haken", "schü-seg" });
        allowedWehrV.put("tro", new String[] { "tro" });
        allowedWehrV.put("wz", new String[] { "wz" });
        allowedWehrAV.put("w-strei", new String[] { "ohne" });
        allowedWehrAV.put("w-üfa", new String[] { "ohne" });
        materialV.put("bo", new String[] { "h", "k", "st" });
        materialV.put("bo-j", new String[] { "h", "k", "st" });
        materialV.put("schw", new String[] { "b", "k" });
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
        if (newValue == null) {
            if (column.equals("wehr") || column.equals("wehr_v") || column.equals("wehr_av")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
                return oldValue;
            }
        }

        if (column.equals("ausbaujahr")
                    && !checkRange(column, newValue, 1800, getCurrentYear() + 2, true, true, true)) {
            return oldValue;
        }

        if (column.equals("br") && !checkRange(column, newValue, 0, 10, true, false, true)) {
            return oldValue;
        }

        if (column.equals("br_li") && !checkRange(column, newValue, 0, 100, true, false, true)) {
            return oldValue;
        }

        if (column.equals("ho_so") && !checkRange(column, newValue, -6, 179, true, true, true)) {
            return oldValue;
        }
        if (column.equals("sz") && !checkRange(column, newValue, -6, 179, true, true, true)) {
            return oldValue;
        }
        if (column.equals("az") && !checkRange(column, newValue, -6, 179, true, true, true)) {
            return oldValue;
        }

        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("wbbl")) {
            return new LinkTableCellRenderer();
        } else {
            return null;
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

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
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

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("zust_kl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("wehr")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("wehr") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("wehr_v")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("wehr_v") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("wehr_av")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("wehr_av") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("l_st")) {
            final CidsBeanFilter filter = createCidsBeanFilter("nicht_qp");
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);
            editor.setNullable(true);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("l_st") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("material_v")) {
            final CidsBeanFilter filter = createCidsBeanFilter("wehr_v");

            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("material") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
        for (final FeatureServiceFeature feature : features) {
            if (feature.getProperty("wehr") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wehr darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("wehr_v") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wehr_v darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("wehr_av") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wehr_av darf nicht leer sein");
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
            if (!checkRange("br", feature.getProperty("br"), 0, 30, true, false, true)) {
                return false;
            }
            if (!checkRange("br_li", feature.getProperty("br_li"), 0, 100, true, false, true)) {
                return false;
            }
            if (!checkRange("ho_so", feature.getProperty("ho_so"), -6, 179, true, true, true)) {
                return false;
            }
            if (!checkRange("sz", feature.getProperty("sz"), -6, 179, true, true, true)) {
                return false;
            }
            if (!checkRange("az", feature.getProperty("az"), -6, 179, true, true, true)) {
                return false;
            }

            if ((feature.getProperty("sz") != null) && (feature.getProperty("az") != null)) {
                if (((Number)feature.getProperty("sz")).doubleValue()
                            <= ((Number)feature.getProperty("az")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut sz muss größer als das Attribut az sein.");
                    return false;
                }
            }

            if ((feature.getProperty("sz") != null) && (feature.getProperty("ho_so") != null)) {
                if (((Number)feature.getProperty("sz")).doubleValue()
                            <= ((Number)feature.getProperty("ho_so")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut sz muss größer als das Attribut ho_so sein.");
                    return false;
                }
            }

            if ((feature.getProperty("az") != null) && (feature.getProperty("ho_so") != null)) {
                if (((Number)feature.getProperty("az")).doubleValue()
                            <= ((Number)feature.getProperty("ho_so")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut az muss größer als das Attribut ho_so sein.");
                    return false;
                }
            }

            if (feature.getProperty("wehr") != null) {
                final String[] allowedWehrVArray = allowedWehrV.get(feature.getProperty("wehr").toString()
                                .toLowerCase());
                final String[] allowedWehrAVArray = allowedWehrAV.get(feature.getProperty("wehr").toString()
                                .toLowerCase());

                if (allowedWehrVArray != null) {
                    if ((feature.getProperty("wehr_v") == null)
                                || !arrayContains(
                                    allowedWehrVArray,
                                    feature.getProperty("wehr_v").toString().toLowerCase())) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Wenn das Attribut wehr = "
                                    + feature.getProperty("wehr").toString()
                                    + ", dann muss das Attribut wehr_v "
                                    + arrayToString(allowedWehrVArray)
                                    + " sein.");
                        return false;
                    }
                }

                if (allowedWehrAVArray != null) {
                    if ((feature.getProperty("wehr_av") == null)
                                || !arrayContains(
                                    allowedWehrAVArray,
                                    feature.getProperty("wehr_av").toString().toLowerCase())) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Wenn das Attribut wehr = "
                                    + feature.getProperty("wehr").toString()
                                    + ", dann muss das Attribut wehr_av "
                                    + arrayToString(allowedWehrAVArray)
                                    + " sein.");
                        return false;
                    }
                }
            }

            if (feature.getProperty("wehr_v") != null) {
                final String[] allowedMaterialVArray = allowedWehrV.get(feature.getProperty("wehr_v").toString()
                                .toLowerCase());

                if (allowedMaterialVArray != null) {
                    if ((feature.getProperty("material_v") == null)
                                || !arrayContains(
                                    allowedMaterialVArray,
                                    feature.getProperty("wehr_v").toString().toLowerCase())) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Wenn das Attribut wehr_v = "
                                    + feature.getProperty("wehr_v").toString()
                                    + ", dann muss das Attribut material_v "
                                    + arrayToString(allowedMaterialVArray)
                                    + " sein.");
                        return false;
                    }
                } else {
                    if (feature.getProperty("material_v") != null) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Wenn das Attribut wehr_v = "
                                    + feature.getProperty("wehr_v").toString()
                                    + ", dann muss das Attribut material_v null sein");
                    }
                }
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
