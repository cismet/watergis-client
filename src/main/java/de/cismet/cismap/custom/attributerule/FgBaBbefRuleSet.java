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

import com.vividsolutions.jts.geom.Geometry;

import org.deegree.datatypes.Types;

import java.sql.Timestamp;

import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.arrayContains;
import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.checkRange;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaBbefRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final HashMap<String, String[]> allowedMaterial = new HashMap<String, String[]>();

    static {
        allowedMaterial.put("Berme", new String[] { null });
        allowedMaterial.put("Fa", new String[] { "H-Rsg", "Kok", "H" });
        allowedMaterial.put("SP", new String[] { "Ste", "Ste-Fs", "Ste-Gab", "Ste-Wb" });
        allowedMaterial.put("Gtr", new String[] { "B", "K", "Ste-Fs" });
        allowedMaterial.put("Mte", new String[] { "Ste-Gab", "Vl" });
        allowedMaterial.put("Pfl", new String[] { "B" });
        allowedMaterial.put("Pfr", new String[] { "B", "H", "K", "St" });
        allowedMaterial.put("Pl", new String[] { "B" });
        allowedMaterial.put("Rin", new String[] { "B", "St-B", "Ste", "Ste-Fs", "Ste-Mw", "Ste-Wb" });
        allowedMaterial.put("Spreit", new String[] { "H" });
        minBaLength = 0.5;
    }

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("l_rl", new Catalogue("k_l_rl", true, true, new Varchar(2, false, false)));
        typeMap.put("bbef", new Catalogue("k_bbef", true, true, true, new Varchar(10, false, false)));
        typeMap.put("material", new Catalogue("k_material", false, true, new Varchar(10, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("obj_nr_gu", new Varchar(50, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true, new Varchar(10, false, false)));
        typeMap.put("traeger_gu", new Varchar(50, false, false));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true, new Numeric(1, 0, false, false)));
        typeMap.put("esw", new BooleanAsInteger(false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br", new Numeric(4, 2, false, true));
        typeMap.put("ho_d_o", new Numeric(4, 2, false, true));
        typeMap.put("ho_d_u", new Numeric(4, 2, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("laenge") && !columnName.equals("ww_gr") && !columnName.equals("ba_cd")
                    && !columnName.equals("geom") && !columnName.equals("obj_nr") && !columnName.equals("id");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (isValueEmpty(newValue)) {
            if (column.equals("l_rl") || column.equals("bbef")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
                return oldValue;
            }
        }

        if (column.equals("br") && !checkRange(column, newValue, 0, 15, 0, 30, true, false, true)) {
            return oldValue;
        }

        if (column.equals("ho_d_o") && !checkRange(column, newValue, 0, 10, 0, 15, true, false, true)) {
            return oldValue;
        }

        if (column.equals("ho_d_u") && !checkRange(column, newValue, 0, 10, 0, 15, true, true, false)) {
            return oldValue;
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

        if (column.equals("bbef") && !isValueEmpty(newValue)) {
            final String[] allowedMaterialVArray = allowedMaterial.get(newValue.toString());

            if (allowedMaterialVArray != null) {
                if (!isValueEmpty(feature.getProperty("material"))
                            && !arrayContains(
                                allowedMaterialVArray,
                                ((feature.getProperty("material") != null) ? feature.getProperty("material")
                                        .toString() : null))) {
                    showMessage("Wenn das Attribut bbef = "
                                + newValue.toString()
                                + ", dann muss das Attribut material "
                                + arrayToString(allowedMaterialVArray)
                                + " sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("material") && !isValueEmpty(newValue)) {
            final String[] allowedMaterialVArray = allowedMaterial.get(feature.getProperty("bbef").toString());

            if (allowedMaterialVArray != null) {
                if (!arrayContains(allowedMaterialVArray, newValue.toString())) {
                    showMessage("Wenn das Attribut bbef = "
                                + newValue
                                + ", dann muss das Attribut material "
                                + arrayToString(allowedMaterialVArray)
                                + " sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("ho_d_o") && (newValue != null) && isNumberOrNull(newValue)
                    && (feature.getProperty("ho_d_u") != null)) {
            if (((Number)newValue).doubleValue()
                        <= ((Number)feature.getProperty("ho_d_u")).doubleValue()) {
                showMessage("Das Attribut ho_d_o muss größer als das Attribut ho_d_u sein.");
                return oldValue;
            }
        }

        if (column.equals("ho_d_u") && (newValue != null) && isNumberOrNull(newValue)
                    && (feature.getProperty("ho_d_o") != null)) {
            if (((Number)feature.getProperty("ho_d_o")).doubleValue()
                        <= ((Number)newValue).doubleValue()) {
                showMessage("Das Attribut ho_d_o muss größer als das Attribut ho_d_u sein.");
                return oldValue;
            }
        }

//        if (column.equals("bbef") && !isValueEmpty(newValue)) {
//            final Double from = (Double)feature.getProperty("ba_st_von");
//            final Double till = (Double)feature.getProperty("ba_st_bis");
//
//            if ((from != null) && (till != null)) {
//                if (isValueIn(feature.getProperty("bbef"), new String[] { "Rin" }, false)) {
//                    if ((Math.abs(till - from) > 5) && (Math.abs(till - from) <= 10)) {
//                        if (
//                            !showSecurityQuestion(
//                                        "Die Länge des Objektes mit der id "
//                                        + feature.getId()
//                                        + "liegt außerhalb des Standardbereichs (0 .. 5) --> verwenden ?")) {
//                            return false;
//                        }
//                    } else if (Math.abs(till - from) > 10) {
//                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                            "Die Länge des Objektes mit der id "
//                                    + feature.getId()
//                                    + " darf nicht größer "
//                                    + 10
//                                    + " sein",
//                            "Ungültiger Wert",
//                            JOptionPane.ERROR_MESSAGE);
//
//                        return false;
//                    }
//                } else {
//                    if (Math.abs(till - from) < 0.5) {
//                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                            "Die Länge des Objektes mit der id "
//                                    + feature.getId()
//                                    + " darf nicht kleiner "
//                                    + 0.5
//                                    + " sein",
//                            "Ungültiger Wert",
//                            JOptionPane.ERROR_MESSAGE);
//
//                        return false;
//                    }
//                }
//            }
//        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ba_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ba_st_bis")) {
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
        } else if (columnName.equals("l_rl")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    createCidsLayerFeatureFilter("bbef"));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("l_rl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("bbef")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("bbef") + " - " + bean.getProperty("name");
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
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    createCidsLayerFeatureFilter("bbef"));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("material") + " - " + bean.getProperty("name");
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
        } else {
            return super.getCellEditor(columnName);
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();
            if (isValueEmpty(feature.getProperty("l_rl"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut l_rl darf nicht leer sein");
                return false;
            }
            if (isValueEmpty(feature.getProperty("bbef"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut bbef darf nicht leer sein");
                return false;
            }
            if (!checkRange("br", feature.getProperty("br"), 0, 15, 0, 30, true, false, true)) {
                return false;
            }

            if (!checkRange("ho_d_o", feature.getProperty("ho_d_o"), 0, 10, 0, 15, true, false, true)) {
                return false;
            }

            if (!checkRange("ho_d_u", feature.getProperty("ho_d_u"), 0, 10, 0, 15, true, true, false)) {
                return false;
            }

            if (
                !checkRangeBetweenOrEqual(
                            "ausbaujahr",
                            feature.getProperty("ausbaujahr"),
                            1800,
                            getCurrentYear()
                            + 2,
                            true)) {
                return false;
            }

            if ((feature.getProperty("bbef") != null) && feature.getProperty("bbef").toString().equals("Rin")) {
                if (
                    Math.abs(
                                toNumber(feature.getProperty("ba_st_von")).doubleValue()
                                - toNumber(feature.getProperty("ba_st_bis")).doubleValue())
                            > 10) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Bei Rin darf die Geometrie nicht länger als 10 m sein.");
                    return false;
                }
            }

            if ((feature.getProperty("ho_d_o") != null) && (feature.getProperty("ho_d_u") != null)) {
                if (((Number)feature.getProperty("ho_d_o")).doubleValue()
                            <= ((Number)feature.getProperty("ho_d_u")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut ho_d_o muss größer als das Attribut ho_d_u sein.");
                    return false;
                }
            }

            if (feature.getProperty("bbef") != null) {
                final String[] allowedMaterialVArray = allowedMaterial.get(feature.getProperty("bbef").toString());

                if (allowedMaterialVArray != null) {
                    if (!isValueEmpty(feature.getProperty("material"))
                                && !arrayContains(
                                    allowedMaterialVArray,
                                    ((feature.getProperty("material") != null)
                                        ? feature.getProperty("material").toString() : null))) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Wenn das Attribut bbef = "
                                    + feature.getProperty("bbef").toString()
                                    + ", dann muss das Attribut material "
                                    + arrayToString(allowedMaterialVArray)
                                    + " sein.");
                        return false;
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
    public String[] getAdditionalFieldNames() {
        return new String[] { "laenge" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("laenge")) {
            return -3;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Double value = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            value = round(geom.getLength());
        }

        return value;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("laenge")) {
            return "round(st_length(geom)::numeric, 2)";
        } else {
            return null;
        }
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");

        final OnOwnRouteStationCheck check = new OnOwnRouteStationCheck();

        final StationLineCreator creator = new StationLineCreator(
                "ba_st",
                routeMc,
                "Basisgewässer (FG)",
                new LinearReferencingWatergisHelper(),
                0.5f);
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
