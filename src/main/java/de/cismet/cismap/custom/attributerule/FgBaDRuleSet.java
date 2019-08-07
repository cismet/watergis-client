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
public class FgBaDRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("profil", new Catalogue("k_profil", true, true, new Varchar(2, false, false)));
        typeMap.put("material", new Catalogue("k_material", false, true, new Varchar(10, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("obj_nr_gu", new Varchar(50, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true, new Varchar(10, false, false)));
        typeMap.put("traeger_gu", new Varchar(50, false, false));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true, new Numeric(1, 0, false, false)));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br_dm_li", new Numeric(8, 3, false, true));
        typeMap.put("ho_li", new Numeric(8, 3, false, true));
        typeMap.put("br_tr_o_li", new Numeric(5, 3, false, true));
        typeMap.put("ho_e", new Numeric(6, 2, false, true));
        typeMap.put("ho_a", new Numeric(6, 2, false, true));
        typeMap.put("gefaelle", new Numeric(6, 2, false, true));
        typeMap.put("ho_d_e", new Numeric(4, 2, false, true));
        typeMap.put("ho_d_a", new Numeric(4, 2, false, true));
        typeMap.put("ho_d_m", new Numeric(4, 2, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
        minBaLength = 0.5;
        maxBaLength = 100.0;
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
            Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();

        if (isValueEmpty(newValue)) {
            if (column.equals("profil")) {
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

        if (column.equals("ho_e") && !checkRangeBetweenOrEqual(column, newValue, -6, 179, true)) {
            return oldValue;
        }
        if (column.equals("ho_a") && !checkRangeBetweenOrEqual(column, newValue, -6, 179, true)) {
            return oldValue;
        }
        if (column.equals("gefaelle") && !checkRangeBetweenOrEqual(column, newValue, 0, 50, -10, 100, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_e") && !checkRangeBetweenOrEqual(column, newValue, 0, 5, 0, 10, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_a") && !checkRangeBetweenOrEqual(column, newValue, 0, 5, 0, 10, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_m") && !checkRange(column, newValue, 0, 10, 0, 30, true, false, true)) {
            return oldValue;
        }
        if (column.equals("br_tr_o_li") && !checkRangeBetweenOrEqual(column, newValue, 0.05, 4, 0.025, 6, true)) {
            return oldValue;
        }

        if (column.equals("br_dm_li") && (feature.getProperty("profil") != null)) {
            if (feature.getProperty("profil").equals("kr") || feature.getProperty("profil").equals("ei")) {
                if (isNoIntegerTempMessage(column, newValue, true)) {
                    if (newValue instanceof Number) {
                        newValue = Math.round(((Number)newValue).doubleValue());
                    }
                }
                if (!checkRangeBetweenOrEqual("br_dm_li", newValue, 50, 4000, 25, 6000, true)) {
                    return oldValue;
                }
            }
            if (feature.getProperty("profil").equals("re") || feature.getProperty("profil").equals("tr")) {
                if (!checkRangeBetweenOrEqual("br_dm_li", newValue, 0.05, 4, 0.025, 6, true)) {
                    return oldValue;
                }
            }
        }

        if (column.equals("ho_li") && (feature.getProperty("profil") != null)) {
            if (feature.getProperty("profil").equals("ei")) {
                if (isNoIntegerTempMessage(column, newValue, true)) {
                    if (newValue instanceof Number) {
                        newValue = Math.round(((Number)newValue).doubleValue());
                    }
                }
                if (!checkRangeBetweenOrEqual("ho_li", newValue, 50, 4000, 25, 6000, true)) {
                    return oldValue;
                }
            }
            if (feature.getProperty("profil").equals("re") || feature.getProperty("profil").equals("tr")) {
                if (!checkRangeBetweenOrEqual("ho_li", newValue, 0.05, 4, 0.025, 6, true)) {
                    return oldValue;
                }
            }
        }

//        if (column.equals("profil") && (newValue != null) && isValueIn(newValue, new String[] { "kr" }, false)) {
//            feature.setProperty("ho_li", null);
//            feature.setProperty("br_tr_o_li", null);
//        }
//
//        if (column.equals("profil") && (newValue != null) && isValueIn(newValue, new String[] { "ei" }, false)) {
//            feature.setProperty("br_tr_o_li", null);
//        }
//
//        if (column.equals("profil") && (newValue != null) && isValueIn(newValue, new String[] { "re" }, false)) {
//            feature.setProperty("br_tr_o_li", null);
//        }

        if (column.equals("ho_li") || column.equals("br_tr_o_li")) {
            if (!isValueEmpty(newValue) && (feature.getProperty("profil") != null)
                        && feature.getProperty("profil").equals("kr")) {
                showMessage("Bei Profil = kr ist kein Wert für " + column + " zulässig");
                return null;
            }
        }

        if (column.equals("br_tr_o_li")) {
            if (!isValueEmpty(newValue) && (feature.getProperty("profil") != null)
                        && isValueIn(feature.getProperty("profil"), new String[] { "ei", "kr", "re" }, false)) {
                showMessage("Bei Profil = " + feature.getProperty("profil") + " ist kein Wert für " + column
                            + " zulässig");
                return null;
            }
        }

        if (column.equals("br_dm")) {
            if (!isValueEmpty(newValue) && (feature.getProperty("profil") != null)
                        && feature.getProperty("profil").equals("tr")) {
                if ((newValue != null) && (feature.getProperty("br_tr_o_li") != null)
                            && newValue.equals(feature.getProperty("br_tr_o_li"))) {
                    showMessage("Bei Profil = tr dürfen br_dm und br_tr_o_li nicht gleich sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("br_tr_o_li")) {
            if (!isValueEmpty(newValue) && (feature.getProperty("profil") != null)
                        && feature.getProperty("profil").equals("tr")) {
                if ((newValue != null) && (feature.getProperty("br_dm") != null)
                            && newValue.equals(feature.getProperty("br_dm"))) {
                    showMessage("Bei Profil = tr dürfen br_dm und br_tr_o_li nicht gleich sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("ho_a") || column.equals("ho_e")) {
            if (column.equals("ho_a") && (feature.getProperty("ho_e") != null)) {
                final double hoe = toNumber(feature.getProperty("ho_e")).doubleValue();
                final double hoa = toNumber(newValue).doubleValue();

                if (hoe < hoa) {
                    if (!showSecurityQuestion("ho_e >= ho_a nicht eingehalten. Fortsetzen?", column, newValue)) {
                        return oldValue;
                    }
                }
            } else if (column.equals("ho_e") && (feature.getProperty("ho_a") != null)) {
                final double hoa = toNumber(feature.getProperty("ho_a")).doubleValue();
                final double hoe = toNumber(newValue).doubleValue();

                if (hoe < hoa) {
                    if (!showSecurityQuestion("ho_e >= ho_a nicht eingehalten. Fortsetzen?", column, newValue)) {
                        return oldValue;
                    }
                }
            }
        }

        // Gefaelle berechnen
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
        } else if (columnName.equals("material")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("dl");

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
        } else if (columnName.equals("profil")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("d");

            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("profil") + " - " + bean.getProperty("name");
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
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();
            if (isValueEmpty(feature.getProperty("profil"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut Profil darf nicht leer sein");
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
            if (!checkRangeBetweenOrEqual("ho_e", feature.getProperty("ho_e"), -6, 179, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_a", feature.getProperty("ho_a"), -6, 179, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("gefaelle", feature.getProperty("gefaelle"), 0, 50, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_d_e", feature.getProperty("ho_d_e"), 0, 10, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_d_a", feature.getProperty("ho_d_a"), 0, 10, true)) {
                return false;
            }

            if (!checkRange("ho_d_m", feature.getProperty("ho_d_a"), 0, 30, true, false, true)) {
                return false;
            }

            if (!checkRangeBetweenOrEqual("br_tr_o_li", feature.getProperty("ho_d_a"), 0.025, 6, true)) {
                return false;
            }

            if (isValueIn(feature.getProperty("profil"), new Object[] { "kr", "ei" }, false)) {
                if (!checkRangeBetweenOrEqual("br_dm_li", feature.getProperty("br_dm_li"), 25, 6000, true)) {
                    return false;
                }

                if (isNoInteger("br_dm_li", feature.getProperty("br_dm_li"), false)) {
                    return false;
                }
                if (isNoInteger("ho_li", feature.getProperty("ho_li"), false)) {
                    return false;
                }
            }
            if (isValueIn(feature.getProperty("profil"), new Object[] { "re", "tr" }, false)) {
                if (!checkRangeBetweenOrEqual("br_dm_li", feature.getProperty("br_dm_li"), 0.025, 6, true)) {
                    return false;
                }
                if (!checkRangeBetweenOrEqual("ho_li", feature.getProperty("ho_li"), 0.025, 6, true)) {
                    return false;
                }
            }

            if (feature.getProperty("profil").equals("ei")) {
                if (!checkRangeBetweenOrEqual("ho_li", feature.getProperty("ho_li"), 25, 6000, true)) {
                    return false;
                }
            }

            if (feature.getProperty("profil").equals("kr")
                        && ((feature.getProperty("ho_li") != null) || (feature.getProperty("br_tr_o_li") != null))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Die Attribute ho_li und br_tr_o_li dürfen nicht belegt sein, wenn profil = kr.");
                return false;
            }
            if ((feature.getProperty("profil").equals("ei") || feature.getProperty("profil").equals("re"))
                        && (feature.getProperty("br_tr_o_li") != null)) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribute br_tr_o_li darf nicht belegt sein, wenn profil = ei oder re.");
                return false;
            }
            if (feature.getProperty("profil").equals("tr")
                        && ((feature.getProperty("br_dm_li") != null) && (feature.getProperty("br_tr_o_li") != null))) {
                if (feature.getProperty("br_dm_li") == feature.getProperty("br_tr_o_li")) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Die Attribute br_dm_li und br_tr_o_li dürfen nicht gleich sein, wenn profil = tr.");
                    return false;
                }
            }
            if ((feature.getProperty("ho_e") != null) && (feature.getProperty("ho_a") != null)) {
                if (((Number)feature.getProperty("ho_e")).doubleValue()
                            < ((Number)feature.getProperty("ho_a")).doubleValue()) {
                    final int answ = JOptionPane.showConfirmDialog(AppBroker.getInstance().getWatergisApp(),
                            "Sind Sie sicher, dass das Attribut ho_e ("
                                    + feature.getProperty("ho_e")
                                    + ") kleiner als das Attribut ho_a("
                                    + feature.getProperty("ho_a")
                                    + ") sein soll?",
                            "Bestätigung",
                            JOptionPane.YES_NO_OPTION);
                    return answ == JOptionPane.OK_OPTION;
                }
            }
        }

        return super.prepareForSave(features);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());

        if ((feature.getProperty("ho_a") != null) && isNumberOrNull(feature.getProperty("ho_a"))
                    && (feature.getProperty("ho_e") != null) && isNumberOrNull(feature.getProperty("ho_e"))) {
            final double laenge = toNumber(feature.getProperty("ba_st_bis")).doubleValue()
                        - toNumber(feature.getProperty("ba_st_von")).doubleValue();
            final double gefaelle = (toNumber(feature.getProperty("ho_e")).doubleValue()
                            - toNumber(feature.getProperty("ho_a")).doubleValue()) / laenge * 1000;
            feature.setProperty("gefaelle", gefaelle);
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
//      todo:  0.5 .. 100
        final OnOwnRouteStationCheck check = new OnOwnRouteStationCheck();
        final StationLineCreator creator = new StationLineCreator(
                "ba_st",
                routeMc,
                "Basisgewässer (FG)",
                new LinearReferencingWatergisHelper(),
                0.5f,
                250);
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
