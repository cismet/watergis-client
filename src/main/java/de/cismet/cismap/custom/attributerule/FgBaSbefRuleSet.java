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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaSbefRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final HashMap<String, String[]> allowedMaterial = new HashMap<String, String[]>();

    static {
        allowedMaterial.put("Buh", new String[] { "H", "K", "Ste", "Ste-Fs", "Ste-Gab", "Ste-Wab" });
        allowedMaterial.put("Pf", new String[] { "B", "H", "K" });
        allowedMaterial.put("Pfl", new String[] { "B", "Ste", "Ste-Fs", "Ste-Gab", "Ste-Wb" });
        allowedMaterial.put("Pfr", new String[] { "B", "H", "K" });
        allowedMaterial.put("Pl", new String[] { "B" });
        allowedMaterial.put("Rgl", new String[] { "Ste", "Ste-Fs", "Ste-Gab", "Ste-Wb" });
        allowedMaterial.put("So-Ab", new String[] { "B" });
        allowedMaterial.put("So-Abt", new String[] { "B" });
        allowedMaterial.put("So-Gl", new String[] { "B" });
        allowedMaterial.put("So-Ra", new String[] { "B" });
        allowedMaterial.put("SP", new String[] { "Ste", "Ste-Fs", "Ste-Gab", "Ste-Wb" });
        allowedMaterial.put("Stöste", new String[] { "Ste", "Ste-Fs", "Ste-Wb" });
        allowedMaterial.put("Sw-Gru", new String[] { "B" });
        allowedMaterial.put("Sw-So", new String[] { "B" });
        allowedMaterial.put("Sw-Stü", new String[] { "B" });
        allowedMaterial.put("Wu", new String[] { "H" });
    }

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true));
        typeMap.put("sbef", new Catalogue("k_sbef", true, true));
        typeMap.put("material", new Catalogue("k_material", false, true));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true));
        typeMap.put("esw", new BooleanAsInteger(false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br", new Numeric(4, 2, false, true));
        typeMap.put("ho_e", new Numeric(6, 2, false, true));
        typeMap.put("ho_a", new Numeric(6, 2, false, true));
        typeMap.put("gefaelle", new Numeric(6, 2, false, true));
        typeMap.put("ho_d_e", new Numeric(4, 2, false, true));
        typeMap.put("ho_d_a", new Numeric(4, 2, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
        minBaLength = 0.5;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("laenge") && !columnName.equals("ww_gr") && !columnName.equals("ba_cd")
                    && !columnName.equals("geom") && !columnName.equals("obj_nr");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (isValueEmpty(newValue)) {
            if (column.equals("sbef")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
                return oldValue;
            }
        }

        if (column.equals("br") && !checkRange(column, newValue, 0, 10, 0, 30, true, false, true)) {
            return oldValue;
        }

        if (column.equals("ho_e") && !checkRangeBetweenOrEqual(column, newValue, -6, 179, true)) {
            return oldValue;
        }
        if (column.equals("ho_a") && !checkRangeBetweenOrEqual(column, newValue, -6, 179, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_e") && !checkRangeBetweenOrEqual(column, newValue, 0, 1, 0, 5, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_a") && !checkRangeBetweenOrEqual(column, newValue, 0, 1, 0, 5, true)) {
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

        if (column.equals("ho_a") || column.equals("ho_e")) {
            if (column.equals("ho_a") && (feature.getProperty("ho_e") != null)) {
                final double hoe = toNumber(feature.getProperty("ho_e")).doubleValue();
                final double hoa = toNumber(newValue).doubleValue();

                if (hoe < hoa) {
                    showMessage("Wert nicht zulässig, weil ho_e >= ho_a nicht eingehalten");
                    return oldValue;
                }
            } else if (column.equals("ho_e") && (feature.getProperty("ho_a") != null)) {
                final double hoa = toNumber(feature.getProperty("ho_a")).doubleValue();
                final double hoe = toNumber(newValue).doubleValue();

                if (hoe < hoa) {
                    showMessage("Wert nicht zulässig, weil ho_e >= ho_a nicht eingehalten");
                    return oldValue;
                }
            }
        }

        if (column.equals("ho_a") || column.equals("ho_e") || column.equals("ba_st_bis")
                    || column.equals("ba_st_von")) {
            final Object hoA = (column.equals("ho_a") ? newValue : feature.getProperty("ho_a"));
            final Object hoE = (column.equals("ho_e") ? newValue : feature.getProperty("ho_e"));
            final Object von = (column.equals("ba_st_von") ? newValue : feature.getProperty("ba_st_von"));
            final Object bis = (column.equals("ba_st_bis") ? newValue : feature.getProperty("ba_st_bis"));

            if ((hoA != null) && isNumberOrNull(hoA) && (hoE != null) && isNumberOrNull(hoE)
                        && (von != null) && isNumberOrNull(von)
                        && (bis != null) && isNumberOrNull(bis)) {
                final double laenge = toNumber(bis).doubleValue()
                            - toNumber(von).doubleValue();
                final double gefaelle = (toNumber(hoE).doubleValue()
                                - toNumber(hoA).doubleValue()) / Math.abs(laenge) * 1000;
                feature.setProperty("gefaelle", gefaelle);

                if (!checkRangeBetweenOrEqual("gefaelle", feature.getProperty("gefaelle"), 0, 50, -10, 100, true)) {
                    return oldValue;
                }
            } else if (feature.getProperty("gefaelle") != null) {
                feature.setProperty("gefaelle", null);
            }
        }

        if (column.equals("gefaelle") && !checkRangeBetweenOrEqual(column, newValue, 0, 50, -10, 100, true)) {
            return oldValue;
        }

//        if (column.equals("sbef") && !isValueEmpty(newValue)) {
//            final String[] allowedMaterialVArray = allowedMaterial.get(newValue.toString());
//
//            if (allowedMaterialVArray != null) {
//                if ((!isValueEmpty(feature.getProperty("material")))
//                            && !arrayContains(
//                                allowedMaterialVArray,
//                                feature.getProperty("material").toString())) {
//                    showMessage("Wenn das Attribut sbef = "
//                                + newValue
//                                + ", dann muss das Attribut material "
//                                + arrayToString(allowedMaterialVArray)
//                                + " sein.");
//                    return oldValue;
//                }
//            }
//        }

        if (column.equals("material") && !isValueEmpty(newValue)) {
            final String[] allowedMaterialVArray = allowedMaterial.get(feature.getProperty("sbef").toString());

            if (allowedMaterialVArray != null) {
                if (!arrayContains(allowedMaterialVArray, newValue.toString())) {
                    showMessage("Wenn das Attribut sbef = "
                                + feature.getProperty("sbef").toString()
                                + ", dann muss das Attribut material "
                                + arrayToString(allowedMaterialVArray)
                                + " sein.");
                    return oldValue;
                }
            }
        }

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
        } else if (columnName.equals("sbef")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("sbef") + " - " + bean.getProperty("name");
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
                    createCidsLayerFeatureFilter("sbef"));
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
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            if (isValueEmpty(feature.getProperty("sbef"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut sbef darf nicht leer sein");
                return false;
            }
            if (!checkRange("br", feature.getProperty("br"), 0, 30, true, false, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_e", feature.getProperty("ho_e"), -6, 179, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_a", feature.getProperty("ho_a"), -6, 179, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_d_e", feature.getProperty("ho_d_e"), 0, 5, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_d_a", feature.getProperty("ho_d_a"), 0, 5, true)) {
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

            if ((feature.getProperty("ho_e") != null) && (feature.getProperty("ho_a") != null)) {
                if (((Number)feature.getProperty("ho_e")).doubleValue()
                            < ((Number)feature.getProperty("ho_a")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribute ho_e muss größer oder gleich dem Attribut ho_a sein.");
                    return false;
                }
            }

            if (feature.getProperty("sbef") != null) {
                final String[] allowedMaterialVArray = allowedMaterial.get(feature.getProperty("sbef").toString());

                if (allowedMaterialVArray != null) {
                    if ((!isValueEmpty(feature.getProperty("material")))
                                && !arrayContains(
                                    allowedMaterialVArray,
                                    feature.getProperty("material").toString())) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Wenn das Attribut sbef = "
                                    + feature.getProperty("sbef").toString()
                                    + ", dann muss das Attribut material "
                                    + arrayToString(allowedMaterialVArray)
                                    + " sein.");
                        return false;
                    }
                }
            }

            refillGefaelle(feature);

            if (!checkRangeBetweenOrEqual("gefaelle", feature.getProperty("gefaelle"), -10, 100, true)) {
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

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void refillGefaelle(final FeatureServiceFeature feature) {
        if ((feature.getProperty("ho_a") != null) && isNumberOrNull(feature.getProperty("ho_a"))
                    && (feature.getProperty("ho_e") != null) && isNumberOrNull(feature.getProperty("ho_e"))) {
            final double laenge = toNumber(feature.getProperty("ba_st_bis")).doubleValue()
                        - toNumber(feature.getProperty("ba_st_von")).doubleValue();
            final double gefaelle = (toNumber(feature.getProperty("ho_e")).doubleValue()
                            - toNumber(feature.getProperty("ho_a")).doubleValue()) / laenge * 1000;
            feature.setProperty("gefaelle", gefaelle);
        } else if (feature.getProperty("gefaelle") != null) {
            feature.setProperty("gefaelle", null);
        }
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
                0.5f,
                100);
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
