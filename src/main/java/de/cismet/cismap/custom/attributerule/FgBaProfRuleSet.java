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

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import org.deegree.datatypes.Types;

import java.util.List;

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
public class FgBaProfRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("profil", new Catalogue("k_profil", true, true, new Varchar(2, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, true));
        typeMap.put("obj_nr_gu", new Varchar(50, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true, new Varchar(10, false, false)));
        typeMap.put("traeger_gu", new Varchar(50, false, false));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true, new Numeric(1, 0, false, false)));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br", new Numeric(6, 2, false, true));
        typeMap.put("ho_e", new Numeric(6, 2, false, true));
        typeMap.put("ho_a", new Numeric(6, 2, false, true));
        typeMap.put("gefaelle", new Numeric(6, 2, false, true));
        typeMap.put("bv_re", new Numeric(4, 2, false, true));
        typeMap.put("bh_re", new Numeric(4, 2, false, true));
        typeMap.put("bl_re", new Numeric(4, 2, false, true));
        typeMap.put("bv_li", new Numeric(4, 2, false, true));
        typeMap.put("bh_li", new Numeric(4, 2, false, true));
        typeMap.put("bl_li", new Numeric(4, 2, false, true));
        typeMap.put("mw", new Numeric(4, 2, false, true));
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
        idOfCurrentlyCheckedFeature = feature.getId();
        if (isValueEmpty(newValue)) {
            if (column.equals("profil")) {
                showMessage("Das Attribut "
                            + column
                            + " darf nicht leer sein", column);
                return oldValue;
            }
        }
        if (column.equals("mw") && !checkRange(column, newValue, 0, 10, 0, 15, true, false, true)) {
            return oldValue;
        } else if (column.equals("mw")) {
            if (!isValueEmpty(newValue)) {
                final double mw = ((Number)newValue).doubleValue();

                if (!isValueEmpty(feature.getProperty("bh_li")) && !isValueEmpty(feature.getProperty("bh_re"))) {
                    if (mw
                                > Math.min(
                                    ((Number)feature.getProperty("bh_li")).doubleValue(),
                                    ((Number)feature.getProperty("bh_re")).doubleValue())) {
                        showMessage("Das Attribut mw muss kleiner als das Minimum von bh_re, bh_li sein.", "mw");
                        return oldValue;
                    }
                } else if (!isValueEmpty(feature.getProperty("bh_li"))) {
                    if (mw > ((Number)feature.getProperty("bh_li")).doubleValue()) {
                        showMessage("Das Attribut mw muss kleiner als bh_li sein.", "mw");
                        return oldValue;
                    }
                } else if (!isValueEmpty(feature.getProperty("bh_re"))) {
                    if (mw > ((Number)feature.getProperty("bh_re")).doubleValue()) {
                        showMessage("Das Attribut mw muss kleiner als bh_re sein.", "mw");
                        return oldValue;
                    }
                }
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

        if (column.equals("br") && !checkRange(column, newValue, 0, 10, 0, 100, true, false, true)) {
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
        if (column.equals("bv_re") && !hasValue(column, newValue, 0.0, true)
                    && !checkRangeBetweenOrEqual(column, newValue, 0.25, 10, 0.1, 15, true)) {
            return oldValue;
        }
        if (column.equals("bh_re") && !checkRange(column, newValue, 0, 10, 0, 15, true, false, true)) {
            return oldValue;
        }
        if (column.equals("bl_re") && !checkRangeBetweenOrEqual(column, newValue, 0, 15, 0, 30, true)) {
            return oldValue;
        }

        if (column.equals("bv_li") && !hasValue(column, newValue, 0.0, true)
                    && !checkRangeBetweenOrEqual(column, newValue, 0.25, 10, 0.1, 15, true)) {
            return oldValue;
        }
        if (column.equals("bh_li") && !checkRange(column, newValue, 0, 10, 0, 15, true, false, true)) {
            return oldValue;
        }
        if (column.equals("bl_li") && !checkRangeBetweenOrEqual(column, newValue, 0, 15, 0, 30, true)) {
            return oldValue;
        }

        if (column.equals("ho_a") || column.equals("ho_e")) {
            if (column.equals("ho_a") && (feature.getProperty("ho_e") != null)) {
                final double hoe = toNumber(feature.getProperty("ho_e")).doubleValue();
                final double hoa = toNumber(newValue).doubleValue();

                if (hoe < hoa) {
                    showMessage("Wert nicht zulässig, weil ho_e >= ho_a nicht eingehalten", "ho_e");
                    return oldValue;
                }
            } else if (column.equals("ho_e") && (feature.getProperty("ho_a") != null)) {
                final double hoa = toNumber(feature.getProperty("ho_a")).doubleValue();
                final double hoe = toNumber(newValue).doubleValue();

                if (hoe < hoa) {
                    showMessage("Wert nicht zulässig, weil ho_e >= ho_a nicht eingehalten", "ho_e");
                    return oldValue;
                }
            }
        }

        if (column.equals("bv_li") || column.equals("bh_li") || column.equals("bv_re") || column.equals("bh_re")
                    || column.equals("profil")) {
            // The value should be set to allow the refillFields access to the new value.
            // The checks for bv_li, bh_li, bv_re, bh_re were done before.
            feature.setProperty(column, newValue);
        }
        refillFields(feature, false);
        checkRangeBetweenOrEqual("bl_re", feature.getProperty("bl_re"), 0, 30, true);
        checkRangeBetweenOrEqual("bl_li", feature.getProperty("bl_li"), 0, 30, true);

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
                if (!checkRangeBetweenOrEqual("gefaelle", gefaelle, 0, 50, -10, 100, true)) {
                    return oldValue;
                }
                feature.setProperty("gefaelle", gefaelle);
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
        } else if (columnName.equals("profil")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    createCidsLayerFeatureFilter("prof"));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("profil") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        return prepareForSaveWithDetails(features) == null;
    }

    @Override
    public ErrorDetails prepareForSaveWithDetails(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();
            if (isValueEmpty(feature.getProperty("profil"))) {
                showMessage("Das Attribut profil darf nicht leer sein", "profil");
                return new ErrorDetails(feature, "profil");
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
                return new ErrorDetails(feature, "ausbaujahr");
            }
            if (!checkRange("br", feature.getProperty("br"), 0, 10, 0, 100, true, false, true)) {
                return new ErrorDetails(feature, "br");
            }
            if (!checkRange("mw", feature.getProperty("mw"), 0, 10, 0, 15, true, false, true)) {
                return new ErrorDetails(feature, "mw");
            }
            if (!checkRangeBetweenOrEqual("ho_e", feature.getProperty("ho_e"), -6, 179, true)) {
                return new ErrorDetails(feature, "ho_e");
            }
            if (!checkRangeBetweenOrEqual("ho_a", feature.getProperty("ho_a"), -6, 179, true)) {
                return new ErrorDetails(feature, "ho_a");
            }

            if (!hasValue("bv_re", feature.getProperty("bv_re"), 0.0, true)
                        && !checkRangeBetweenOrEqual("bv_re", feature.getProperty("bv_re"), 0.25, 10, 0.1, 15, true)) {
                return new ErrorDetails(feature, "bv_re");
            }
            if (!checkRange("bh_re", feature.getProperty("bh_re"), 0, 10, 0, 15, true, false, true)) {
                return new ErrorDetails(feature, "bh_re");
            }

            if (!hasValue("bv_li", feature.getProperty("bv_li"), 0.0, true)
                        && !checkRangeBetweenOrEqual("bv_li", feature.getProperty("bv_li"), 0.25, 10, 0.1, 15, true)) {
                return new ErrorDetails(feature, "ho_a");
            }
            if (!checkRange("bh_li", feature.getProperty("bh_li"), 0, 10, 0, 15, true, false, true)) {
                return new ErrorDetails(feature, "bh_li");
            }

            if (!isValueEmpty(feature.getProperty("mw"))) {
                final double mw = ((Number)feature.getProperty("mw")).doubleValue();

                if (!isValueEmpty(feature.getProperty("bh_li")) && !isValueEmpty(feature.getProperty("bh_re"))) {
                    if (mw
                                > Math.min(
                                    ((Number)feature.getProperty("bh_li")).doubleValue(),
                                    ((Number)feature.getProperty("bh_re")).doubleValue())) {
                        showMessage("Das Attribut mw muss kleiner als das Minimum von bh_re, bh_li sein.", "mw");
                        return new ErrorDetails(feature, "mw");
                    }
                } else if (!isValueEmpty(feature.getProperty("bh_li"))) {
                    if (mw > ((Number)feature.getProperty("bh_li")).doubleValue()) {
                        showMessage("Das Attribut mw muss kleiner als bh_li sein.", "mw");
                        return new ErrorDetails(feature, "mw");
                    }
                } else if (!isValueEmpty(feature.getProperty("bh_re"))) {
                    if (mw > ((Number)feature.getProperty("bh_re")).doubleValue()) {
                        showMessage("Das Attribut mw muss kleiner als bh_re sein.", "mw");
                        return new ErrorDetails(feature, "mw");
                    }
                }
            }

            if ((feature.getProperty("profil") != null) && feature.getProperty("profil").toString().equals("re")) {
                if (!hasValue("bv_re", feature.getProperty("bv_re"), 0, true)
                            || !hasValue("bv_li", feature.getProperty("bv_li"), 0, true)) {
                    showMessage(
                        "Die Attribute bv_re und bv_li müssen beide 0 sein, wenn das Attribut profil den Wert re hat.",
                        "bv_re");
                    return new ErrorDetails(feature, "bv_re");
                }
            }

            if ((feature.getProperty("profil") != null) && feature.getProperty("profil").toString().equals("tr")) {
                if ((feature.getProperty("bv_re") != null) && (feature.getProperty("bv_li") != null)
                            && hasValue("bv_re", feature.getProperty("bv_re"), 0, true)
                            && hasValue("bv_li", feature.getProperty("bv_li"), 0, true)) {
                    showMessage(
                        "Die Attribute bv_re und bv_li dürfen nicht beide 0 sein, wenn das Attribut profil den Wert tr hat.",
                        "bv_re");
                    return new ErrorDetails(feature, "bv_re");
                }
            }

            if ((feature.getProperty("ho_e") != null) && (feature.getProperty("ho_a") != null)) {
                if (toNumber(feature.getProperty("ho_e")).doubleValue()
                            < toNumber(feature.getProperty("ho_a")).doubleValue()) {
                    showMessage("Das Attribute ho_e muss größer oder gleich dem Attribut ho_a sein.", "ho_e");
                    return new ErrorDetails(feature, "ho_e");
                }
            }

            refillFields(feature, true);

            if (!checkRangeBetweenOrEqual("gefaelle", feature.getProperty("gefaelle"), 0, 50, -10, 100, true)) {
                return new ErrorDetails(feature, "gefaelle");
            }

            if (!checkRange("bl_re", feature.getProperty("bl_re"), 0, 15, 0, 30, true, false, true)) {
                return new ErrorDetails(feature, "bl_re");
            }

            if (!checkRange("bl_li", feature.getProperty("bl_li"), 0, 15, 0, 30, true, false, true)) {
                return new ErrorDetails(feature, "bl_li");
            }
        }

        return super.prepareForSaveWithDetails(features);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature       DOCUMENT ME!
     * @param  withGefaelle  DOCUMENT ME!
     */
    private void refillFields(final FeatureServiceFeature feature, final boolean withGefaelle) {
        if (withGefaelle) {
            if ((feature.getProperty("ho_a") != null) && isNumberOrNull(feature.getProperty("ho_a"))
                        && (feature.getProperty("ho_e") != null) && isNumberOrNull(feature.getProperty("ho_e"))) {
                final double laenge = toNumber(feature.getProperty("ba_st_bis")).doubleValue()
                            - toNumber(feature.getProperty("ba_st_von")).doubleValue();
                final double gefaelle = (toNumber(feature.getProperty("ho_e")).doubleValue()
                                - toNumber(feature.getProperty("ho_a")).doubleValue()) / laenge * 1000;
                feature.setProperty("gefaelle", gefaelle);
            }
        }
        if ((feature.getProperty("profil") != null) && feature.getProperty("profil").toString().equals("re")) {
            if (feature.getProperty("bv_re") == null) {
                feature.setProperty("bv_re", 0.0);
            }

            if (feature.getProperty("bv_li") == null) {
                feature.setProperty("bv_li", 0.0);
            }

            if (toNumber(feature.getProperty("bh_re")).doubleValue() != 0.0) {
                feature.setProperty("bl_re", feature.getProperty("bh_re"));
            }

            if (toNumber(feature.getProperty("bh_li")).doubleValue() != 0.0) {
                feature.setProperty("bl_li", feature.getProperty("bh_li"));
            }
        }

        if ((feature.getProperty("profil") != null) && feature.getProperty("profil").toString().equals("tr")) {
            if ((feature.getProperty("bv_re") != null) && isNumberOrNull(feature.getProperty("bv_re"))
                        && (feature.getProperty("bh_re") != null) && isNumberOrNull(feature.getProperty("bh_re"))) {
                if ((toNumber(feature.getProperty("bv_re")).doubleValue() != 0.0)
                            && (toNumber(feature.getProperty("bh_re")).doubleValue() != 0.0)) {
                    // we assume, that bv = ba / bh and bl^2 = ba^2 + bh^2
                    // so bl = sqrt((bv*bh)^2 + bh^2)
                    // bv = sqrt(bl^2 - bh^2) / bh
                    // bh = sqrt(bl^2 /(bv^2+1))
                    final double bh = toNumber(feature.getProperty("bh_re")).doubleValue();
                    final double bv = toNumber(feature.getProperty("bv_re")).doubleValue();
                    final double bl = Math.sqrt(Math.pow(bv * bh, 2) + Math.pow(bh, 2));

                    feature.setProperty("bl_re", Math.round(bl * 100) / 100.0);
                } else if ((toNumber(feature.getProperty("bv_re")).doubleValue() == 0.0)) {
                    feature.setProperty("bl_re", toNumber(feature.getProperty("bh_re")).doubleValue());
                }
            }

            if ((feature.getProperty("bv_li") != null) && isNumberOrNull(feature.getProperty("bv_li"))
                        && (feature.getProperty("bh_li") != null) && isNumberOrNull(feature.getProperty("bh_li"))) {
                if ((toNumber(feature.getProperty("bv_li")).doubleValue() != 0.0)
                            && (toNumber(feature.getProperty("bh_li")).doubleValue() != 0.0)) {
                    // we assume, that bv = ba / bh and bl^2 = ba^2 + bh^2
                    // so bl = sqrt((bv*bh)^2 + bh^2)
                    // bv = sqrt(bl^2 - bh^2) / bh
                    // bh = sqrt(bl^2 /(bv^2+1))
                    final double bh = toNumber(feature.getProperty("bh_li")).doubleValue();
                    final double bv = toNumber(feature.getProperty("bv_li")).doubleValue();
                    final double bl = Math.sqrt(Math.pow(bv * bh, 2) + Math.pow(bh, 2));

                    feature.setProperty("bl_li", Math.round(bl * 100) / 100.0);
                } else if ((toNumber(feature.getProperty("bv_li")).doubleValue() == 0.0)) {
                    feature.setProperty("bl_li", toNumber(feature.getProperty("bh_li")).doubleValue());
                }
            }
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
}
