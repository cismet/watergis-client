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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

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
import de.cismet.watergis.utils.LinkTableCellRenderer;

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
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true));
        typeMap.put("profil", new Catalogue("k_profil", true, true));
        typeMap.put("obj_nr", new Numeric(20, 0, false, true));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true));
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
            if (column.equals("profil")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
                return oldValue;
            }
        }
        if (column.equals("mw") && !checkRange(column, newValue, 0, 10, 0, 30, true, false, true)) {
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

        if (column.equals("br") && !checkRangeBetweenOrEqual(column, newValue, 0, 10, 0, 30, true)) {
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

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("wbbl")) {
            return new LinkTableCellRenderer();
        }
        return super.getCellRenderer(columnName);
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
        for (final FeatureServiceFeature feature : features) {
            if (isValueEmpty(feature.getProperty("profil"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut profil darf nicht leer sein");
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
            if (!checkRangeBetweenOrEqual("ho_e", feature.getProperty("ho_e"), -6, 179, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_a", feature.getProperty("ho_a"), -6, 179, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("gefaelle", feature.getProperty("gefaelle"), -10, 100, true)) {
                return false;
            }

            if (!hasValue("bv_re", feature.getProperty("bv_re"), 0.0, true)
                        && !checkRangeBetweenOrEqual("bv_re", feature.getProperty("bv_re"), 0.25, 5, true)) {
                return false;
            }
            if (!checkRange("bh_re", feature.getProperty("bh_re"), 0, 15, true, false, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("bl_re", feature.getProperty("bl_re"), 0, 30, true)) {
                return false;
            }

            if (!hasValue("bv_li", feature.getProperty("bv_li"), 0.0, true)
                        && !checkRangeBetweenOrEqual("bv_li", feature.getProperty("bv_li"), 0.25, 5, true)) {
                return false;
            }
            if (!checkRange("bh_li", feature.getProperty("bh_li"), 0, 15, true, false, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("bl_li", feature.getProperty("bl_li"), 0, 30, true)) {
                return false;
            }

            if ((feature.getProperty("profil") != null) && feature.getProperty("profil").toString().equals("re")) {
                if (!hasValue("bv_re", feature.getProperty("bv_re"), 0, true)
                            || !hasValue("bv_li", feature.getProperty("bv_li"), 0, true)) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Die Attribute bv_re und bv_li müssen beide 0 sein, wenn das Attribut profil den Wert re hat.");
                    return false;
                }
            }

            if ((feature.getProperty("profil") != null) && feature.getProperty("profil").toString().equals("tr")) {
                if (hasValue("bv_re", feature.getProperty("bv_re"), 0, true)
                            && hasValue("bv_li", feature.getProperty("bv_li"), 0, true)) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Die Attribute bv_re und bv_li dürfen nicht beide 0 sein, wenn das Attribut profil den Wert tr hat.");
                    return false;
                }
            }

            if ((feature.getProperty("ho_e") != null) && (feature.getProperty("ho_a") != null)) {
                if (toNumber(feature.getProperty("ho_e")).doubleValue()
                            < toNumber(feature.getProperty("ho_a")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribute ho_e muss größer oder gleich dem Attribut ho_a sein.");
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

        if ((feature.getProperty("ho_a") != null) && isNumberOrNull(feature.getProperty("ho_a"))
                    && (feature.getProperty("ho_e") != null) && isNumberOrNull(feature.getProperty("ho_e"))) {
            final double laenge = toNumber(feature.getProperty("ba_st_bis")).doubleValue()
                        - toNumber(feature.getProperty("ba_st_von")).doubleValue();
            final double gefaelle = (toNumber(feature.getProperty("ho_e")).doubleValue()
                            - toNumber(feature.getProperty("ho_a")).doubleValue()) / laenge * 1000;
            feature.setProperty("gefaelle", gefaelle);
        }

        if ((feature.getProperty("profil") != null) && feature.getProperty("profil").toString().equals("re")) {
            if (feature.getProperty("bv_re") == null) {
                feature.setProperty("bv_re", 0);
            }

            if (feature.getProperty("bv_li") == null) {
                feature.setProperty("bv_li", 0);
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
                    final double bh = toNumber(feature.getProperty("bh_re")).doubleValue();
                    final double bv = toNumber(feature.getProperty("bv_re")).doubleValue();
                    final double bl = Math.sqrt(Math.pow(bv * bh, 2) + Math.pow(bh, 2));

                    feature.setProperty("bl_re", Math.round(bl * 100) / 100.0);
                }
            }

            if ((feature.getProperty("bv_li") != null) && isNumberOrNull(feature.getProperty("bv_li"))
                        && (feature.getProperty("bh_li") != null) && isNumberOrNull(feature.getProperty("bh_li"))) {
                if ((toNumber(feature.getProperty("bv_li")).doubleValue() != 0.0)
                            && (toNumber(feature.getProperty("bh_li")).doubleValue() != 0.0)) {
                    // we assume, that bv = ba / bh and bl^2 = ba^2 + bh^2
                    // so bl = sqrt((bv*bh)^2 + bh^2)
                    final double bh = toNumber(feature.getProperty("bh_li")).doubleValue();
                    final double bv = toNumber(feature.getProperty("bv_li")).doubleValue();
                    final double bl = Math.sqrt(Math.pow(bv * bh, 2) + Math.pow(bh, 2));

                    feature.setProperty("bl_li", Math.round(bl * 100) / 100.0);
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

        return new StationLineCreator("ba_st", routeMc, new LinearReferencingWatergisHelper(), 0.5f);
    }
}
