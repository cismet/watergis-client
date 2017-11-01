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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

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
public class FgBaWehrRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Map<String, String[]> ALLOWED_WEHR_WEHR_V = new HashMap<String, String[]>();
    private static final Map<String, String[]> ALLOWED_WEHR_WEHR_AV = new HashMap<String, String[]>();
    private static final Map<String, String[]> WEHR_V_MATERIAL = new HashMap<String, String[]>();

    static {
        ALLOWED_WEHR_WEHR_V.put("s-kbw", new String[] { "bo", "bo-j", "schü" });
        ALLOWED_WEHR_WEHR_V.put("s-sbw", new String[] { "bo", "bo-j", "schü" });
        ALLOWED_WEHR_WEHR_V.put("s-stw", new String[] { "bo", "bo-j", "schü" });
        ALLOWED_WEHR_WEHR_V.put("s-moe", new String[] { "bo", "bo-j", "schü" });
        ALLOWED_WEHR_WEHR_V.put("w-strei", new String[] { "schw" });
        ALLOWED_WEHR_WEHR_V.put("w-üfa", new String[] { "schw" });
        ALLOWED_WEHR_WEHR_V.put("kl", new String[] { "ki", "ki-fb", "ki-fb-schü", "ki-schü" });
        ALLOWED_WEHR_WEHR_V.put("na", new String[] { "na" });
        ALLOWED_WEHR_WEHR_V.put("seg", new String[] { "seg", "seg-fb" });
        ALLOWED_WEHR_WEHR_V.put("sek", new String[] { "sek" });
        ALLOWED_WEHR_WEHR_V.put("schl", new String[] { "schl" });
        ALLOWED_WEHR_WEHR_V.put("w-schü", new String[] { "schü", "schü-dop", "schü-dreh", "schü-haken", "schü-seg" });
        ALLOWED_WEHR_WEHR_V.put("tro", new String[] { "tro" });
        ALLOWED_WEHR_WEHR_V.put("wz", new String[] { "wz" });
        ALLOWED_WEHR_WEHR_AV.put("w-strei", new String[] { "ohne" });
        ALLOWED_WEHR_WEHR_AV.put("w-üfa", new String[] { "ohne" });
        WEHR_V_MATERIAL.put("bo", new String[] { "h", "k", "st" });
        WEHR_V_MATERIAL.put("bo-j", new String[] { "h", "k", "st" });
        WEHR_V_MATERIAL.put("schw", new String[] { "b", "k" });
    }

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true));
        typeMap.put("wehr", new Catalogue("k_wehr", true, true));
        typeMap.put("wehr_v", new Catalogue("k_wehr_v", true, true));
        typeMap.put("material_v", new Catalogue("k_material_v", false, true));
        typeMap.put("wehr_av", new Catalogue("k_wehr_av", true, true));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true));
        typeMap.put("esw", new BooleanAsInteger(false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br", new Numeric(4, 2, false, true));
        typeMap.put("br_li", new Numeric(4, 2, false, true));
        typeMap.put("ho_so", new Numeric(6, 2, false, true));
        typeMap.put("sz", new Numeric(6, 2, false, true));
        typeMap.put("az", new Numeric(6, 2, false, true));
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
            if (column.equals("wehr") || column.equals("wehr_v") || column.equals("wehr_av")) {
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

        if (column.equals("br") && !checkRange(column, newValue, 0, 10, 0, 30, true, false, true)) {
            return oldValue;
        }

        if (column.equals("br_li") && !checkRange(column, newValue, 0, 30, 0, 100, true, false, true)) {
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

        if (column.equals("sz")) {
            if ((newValue != null) && (feature.getProperty("az") != null)) {
                if (((Number)newValue).doubleValue()
                            <= ((Number)feature.getProperty("az")).doubleValue()) {
                    showMessage("Das Attribut sz muss größer als das Attribut az sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("az")) {
            if (((feature.getProperty("sz") != null) && (newValue != null))) {
                if (((Number)feature.getProperty("sz")).doubleValue()
                            <= ((Number)newValue).doubleValue()) {
                    showMessage("Das Attribut sz muss größer als das Attribut az sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("sz")) {
            if ((newValue != null) && (feature.getProperty("ho_so") != null)) {
                if (((Number)newValue).doubleValue()
                            <= ((Number)feature.getProperty("ho_so")).doubleValue()) {
                    showMessage("Das Attribut sz muss größer als das Attribut ho_so sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("ho_so")) {
            if (((feature.getProperty("sz") != null) && (newValue != null))) {
                if (((Number)feature.getProperty("sz")).doubleValue()
                            <= ((Number)newValue).doubleValue()) {
                    showMessage("Das Attribut sz muss größer als das Attribut ho_so sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("az")) {
            if ((newValue != null) && (feature.getProperty("ho_so") != null)) {
                if (((Number)newValue).doubleValue()
                            <= ((Number)feature.getProperty("ho_so")).doubleValue()) {
                    showMessage("Das Attribut az muss größer als das Attribut ho_so sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("ho_so")) {
            if (((feature.getProperty("az") != null) && (newValue != null))) {
                if (((Number)feature.getProperty("az")).doubleValue()
                            <= ((Number)newValue).doubleValue()) {
                    showMessage("Das Attribut az muss größer als das Attribut ho_so sein.");
                    return oldValue;
                }
            }
        }

//        if (column.equals("wehr") && (newValue != null)) {
//            final String[] allowedWehrVArray = ALLOWED_WEHR_WEHR_V.get(newValue.toString().toLowerCase());
//            final String[] allowedWehrAVArray = ALLOWED_WEHR_WEHR_AV.get(newValue.toString().toLowerCase());
//
//            if (allowedWehrVArray != null) {
//                if ((isValueEmpty(feature.getProperty("wehr_v")))
//                            || !arrayContains(
//                                allowedWehrVArray,
//                                feature.getProperty("wehr_v").toString().toLowerCase())) {
//                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                        "Wenn das Attribut wehr = "
//                                + newValue.toString()
//                                + ", dann muss das Attribut wehr_v "
//                                + arrayToString(allowedWehrVArray)
//                                + " sein.");
//                    return oldValue;
//                }
//            }
//
//            if (allowedWehrAVArray != null) {
//                if ((isValueEmpty(feature.getProperty("wehr_av")))
//                            || !arrayContains(
//                                allowedWehrAVArray,
//                                feature.getProperty("wehr_av").toString().toLowerCase())) {
//                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                        "Wenn das Attribut wehr = "
//                                + newValue.toString()
//                                + ", dann muss das Attribut wehr_av "
//                                + arrayToString(allowedWehrAVArray)
//                                + " sein.");
//                    return oldValue;
//                }
//            }
//        }

        if (column.equals("wehr_v") && (newValue != null) && (feature.getProperty("wehr") != null)) {
            final String[] allowedWehrVArray = ALLOWED_WEHR_WEHR_V.get(feature.getProperty("wehr").toString()
                            .toLowerCase());

            if (allowedWehrVArray != null) {
                if ((isValueEmpty(newValue))
                            || !arrayContains(
                                allowedWehrVArray,
                                newValue.toString().toLowerCase())) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut wehr = "
                                + feature.getProperty("wehr").toString()
                                + ", dann muss das Attribut wehr_v "
                                + arrayToString(allowedWehrVArray)
                                + " sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("wehr_av") && (newValue != null) && (feature.getProperty("wehr") != null)) {
            final String[] allowedWehrAVArray = ALLOWED_WEHR_WEHR_AV.get(feature.getProperty("wehr").toString()
                            .toLowerCase());

            if (allowedWehrAVArray != null) {
                if ((isValueEmpty(newValue))
                            || !arrayContains(
                                allowedWehrAVArray,
                                newValue.toString().toLowerCase())) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut wehr = "
                                + newValue.toString()
                                + ", dann muss das Attribut wehr_av "
                                + arrayToString(allowedWehrAVArray)
                                + " sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("wehr_v") && (newValue != null) && (feature.getProperty("material_v") != null)) {
            final String[] allowedMaterialVArray = WEHR_V_MATERIAL.get(newValue.toString().toLowerCase());

            if (allowedMaterialVArray != null) {
                if ((feature.getProperty("material_v") == null)
                            || !arrayContains(
                                allowedMaterialVArray,
                                feature.getProperty("material_v").toString().toLowerCase())) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut wehr_v = "
                                + newValue.toString()
                                + ", dann muss das Attribut material_v "
                                + arrayToString(allowedMaterialVArray)
                                + " sein.");
                    return oldValue;
                }
            } else {
                if (feature.getProperty("material_v") != null) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut wehr_v = "
                                + newValue.toString()
                                + ", dann muss das Attribut material_v null sein");
                }
            }
        }

        if (column.equals("material_v") && (newValue != null) && (feature.getProperty("material_v") != null)) {
            final String[] allowedMaterialVArray = WEHR_V_MATERIAL.get(feature.getProperty("wehr_v").toString()
                            .toLowerCase());

            if (allowedMaterialVArray != null) {
                if ((newValue == null)
                            || !arrayContains(
                                allowedMaterialVArray,
                                newValue.toString().toLowerCase())) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut wehr_v = "
                                + feature.getProperty("wehr_v").toString()
                                + ", dann muss das Attribut material_v "
                                + arrayToString(allowedMaterialVArray)
                                + " sein.");
                    return oldValue;
                }
            } else {
                if (newValue != null) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut wehr_v = "
                                + feature.getProperty("wehr_v").toString()
                                + ", dann muss das Attribut material_v null sein");
                }
            }
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
        } else if (columnName.equals("wehr")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
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

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
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

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("wehr_av") + " - " + bean.getProperty("name");
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
        } else if (columnName.equals("material_v")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("wehr_v");

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
            if (isValueEmpty(feature.getProperty("wehr"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wehr darf nicht leer sein");
                return false;
            }
            if (isValueEmpty(feature.getProperty("wehr_v"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut wehr_v darf nicht leer sein");
                return false;
            }
            if (isValueEmpty(feature.getProperty("wehr_av"))) {
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
                final String[] allowedWehrVArray = ALLOWED_WEHR_WEHR_V.get(feature.getProperty("wehr").toString()
                                .toLowerCase());
                final String[] allowedWehrAVArray = ALLOWED_WEHR_WEHR_AV.get(feature.getProperty("wehr").toString()
                                .toLowerCase());

                if (allowedWehrVArray != null) {
                    if ((isValueEmpty(feature.getProperty("wehr_v")))
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
                    if ((isValueEmpty(feature.getProperty("wehr_av")))
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
                final String[] allowedMaterialVArray = WEHR_V_MATERIAL.get(feature.getProperty("wehr_v").toString()
                                .toLowerCase());

                if (allowedMaterialVArray != null) {
                    if ((feature.getProperty("material_v") == null)
                                || !arrayContains(
                                    allowedMaterialVArray,
                                    feature.getProperty("material_v").toString().toLowerCase())) {
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
