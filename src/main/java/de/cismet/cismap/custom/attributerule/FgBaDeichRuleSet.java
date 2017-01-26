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
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.LineAndStationCreator;

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
public class FgBaDeichRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        final Numeric bermeW = new Numeric(1, 0, false, true);
        final Numeric bermeB = new Numeric(1, 0, false, true);
        final Numeric esw = new Numeric(1, 0, false, true);
        esw.setRange(0.0, 1.0);

        bermeW.setRange(0.0, 1.0);
        bermeB.setRange(0.0, 1.0);

        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("km_von", new Numeric(10, 2, false, true));
        typeMap.put("km_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true));
        typeMap.put("l_rl", new Catalogue("k_l_rl", false, true));
        typeMap.put("nr", new Varchar(50, false, true));
        typeMap.put("name", new Varchar(50, false, true));
        typeMap.put("deich", new Catalogue("k_deich", true, true));
        typeMap.put("ord", new Catalogue("k_deich_ord", true, true));
        typeMap.put("l_fk", new Catalogue("k_deich_l_fk", true, true));
        typeMap.put("schgr", new Catalogue("k_deich_schgr", false, true));
        typeMap.put("material_f", new Catalogue("k_material", false, true));
        typeMap.put("material_w", new Catalogue("k_material", false, true));
        typeMap.put("material_k", new Catalogue("k_material", false, true));
        typeMap.put("material_i", new Catalogue("k_material", false, true));
        typeMap.put("material_b", new Catalogue("k_material", false, true));
        typeMap.put("berme_w", bermeW);
        typeMap.put("berme_b", bermeB);
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true));
        typeMap.put("esw", esw);
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br_f", new Numeric(4, 2, false, true));
        typeMap.put("br_k", new Numeric(4, 2, false, true));
        typeMap.put("ho_k_f", new Numeric(4, 2, false, true));
        typeMap.put("ho_k_pn", new Numeric(4, 2, false, true));
        typeMap.put("ho_bhw_pn", new Numeric(4, 2, false, true));
        typeMap.put("ho_mw_pn", new Numeric(4, 2, false, true));
        typeMap.put("bv_w", new Numeric(4, 2, false, true));
        typeMap.put("bv_b", new Numeric(4, 2, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("geom") && !columnName.equals("laenge") && !columnName.equals("obj_nr")
                    && !columnName.equals("id");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (isValueEmpty(newValue)) {
            if (column.equalsIgnoreCase("deich") || column.equalsIgnoreCase("ord") || column.equalsIgnoreCase("l_fk")) {
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

        if (column.equals("bv_w") && !checkRangeBetweenOrEqual(column, newValue, 3, 6, 1, 15, true)) {
            return oldValue;
        }

        if (column.equals("bv_b") && !checkRangeBetweenOrEqual(column, newValue, 3, 6, 1, 15, true)) {
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
        if (columnName.equals("ba_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ba_st_bis")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ww_gr")) {
            CidsLayerFeatureFilter filter = null;

            if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren")) {
                final String userName = AppBroker.getInstance().getOwner();
                filter = new CidsLayerFeatureFilter() {

                        @Override
                        public boolean accept(final CidsLayerFeature bean) {
                            if (bean == null) {
                                return false;
                            }
                            return bean.getProperty("owner").equals(userName);
                        }
                    };
            } else {
                filter = new CidsLayerFeatureFilter() {

                        @Override
                        public boolean accept(final CidsLayerFeature bean) {
                            return bean != null;
                        }
                    };
            }
            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true),
                    filter);
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
        } else if (columnName.equals("l_rl")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("deich");
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
                        return bean.getProperty("l_rl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("deich")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("deich") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("ord")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("ord") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("l_fk")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("l_fk") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("schgr")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("schgr") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.startsWith("material")) {
            final AbstractCidsLayerListCellRenderer renderer = new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("material") + " - " + bean.getProperty("name");
                    }
                };

            final CidsLayerFeatureFilter filter;

            if (columnName.equals("material_f")) {
                filter = createCidsLayerFeatureFilter("deich_f");
            } else if (columnName.equals("material_w")) {
                filter = createCidsLayerFeatureFilter("deich_w");
            } else if (columnName.equals("material_k")) {
                filter = createCidsLayerFeatureFilter("deich_k");
            } else if (columnName.equals("material_i")) {
                filter = createCidsLayerFeatureFilter("deich_i");
            } else if (columnName.equals("material_b")) {
                filter = createCidsLayerFeatureFilter("deich_b");
            } else {
                return null;
            }

            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);

            editor.setListRenderer(renderer);

            return editor;
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            if (isValueEmpty(feature.getProperty("deich"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut deich darf nicht leer sein");
                return false;
            }
            if (isValueEmpty(feature.getProperty("ord"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut ord darf nicht leer sein");
                return false;
            }
            if (isValueEmpty(feature.getProperty("l_fk"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut l_fk darf nicht leer sein");
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

            if (!checkRangeBetweenOrEqual("bv_w", feature.getProperty("bv_w"), 1, 15, true)) {
                return false;
            }

            if (!checkRangeBetweenOrEqual("bv_b", feature.getProperty("bv_b"), 1, 15, true)) {
                return false;
            }

            if (isValueIn(feature.getProperty("l_fk"), new Object[] { "fd" }, false)) {
                if (!checkRangeBetweenOrEqual("br_f", feature.getProperty("br_f"), 2, 50, true)) {
                    return false;
                }
                if (!checkRangeBetweenOrEqual("br_k", feature.getProperty("br_k"), 0.5, 10, true)) {
                    return false;
                }
                if (!checkRangeBetweenOrEqual("ho_k_f", feature.getProperty("ho_k_f"), 0.5, 15, true)) {
                    return false;
                }
                if (!checkRangeBetweenOrEqual("ho_k_pn", feature.getProperty("ho_k_pn"), 2, 25, true)) {
                    return false;
                }
                if (!checkRangeBetweenOrEqual("ho_bhw_pn", feature.getProperty("ho_bhw_pn"), 2, 25, true)) {
                    return false;
                }

                if (!isValueIn(feature.getProperty("l_rl"), new Object[] { "re", "li" }, false)) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn l_fk = fd, dann muss l_rl = re oder li");
                    return false;
                }
            } else if (isValueIn(feature.getProperty("l_fk"), new Object[] { "bd", "kd" }, false)) {
                if (!checkRangeBetweenOrEqual("br_f", feature.getProperty("br_f"), 2, 100, true)) {
                    return false;
                }
                if (!checkRangeBetweenOrEqual("br_k", feature.getProperty("br_k"), 0.5, 20, true)) {
                    return false;
                }
                if (!checkRangeBetweenOrEqual("ho_k_f", feature.getProperty("ho_k_f"), 1, 15, true)) {
                    return false;
                }
                if (!checkRangeBetweenOrEqual("ho_k_pn", feature.getProperty("ho_k_pn"), 1, 20, true)) {
                    return false;
                }
                if (!checkRange("ho_bhw_pn", feature.getProperty("ho_bhw_pn"), 0, 20, true, false, true)) {
                    return false;
                }
                if (feature.getProperty("l_rl") != null) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut l_rl muss leer sein, wenn l_fk = bd oder kd");
                    return false;
                }
                if (feature.getProperty("ba_st") != null) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Es darf keine Stationierung angegeben sein, wenn l_fk = bd oder kd");
                    return false;
                }
            }

            if ((feature.getProperty("br_f") != null) && (feature.getProperty("br_k") != null)) {
                if (((Number)feature.getProperty("br_f")).doubleValue()
                            <= ((Number)feature.getProperty("br_k")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribute br_f muss größer br_k sein.");
                    return false;
                }
            }
            if ((feature.getProperty("ho_k_f") != null) && (feature.getProperty("ho_k_pn") != null)) {
                if (((Number)feature.getProperty("ho_k_pn")).doubleValue()
                            <= ((Number)feature.getProperty("ho_k_f")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribute ho_k_pn muss größer ho_k_f sein.");
                    return false;
                }
            }
            if ((feature.getProperty("ho_bhw_pn") != null) && (feature.getProperty("ho_k_pn") != null)) {
                if (((Number)feature.getProperty("ho_k_pn")).doubleValue()
                            <= ((Number)feature.getProperty("ho_bhw_pn")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribute ho_k_pn muss größer ho_bhw_pn sein.");
                    return false;
                }
            }
            if ((feature.getProperty("ho_bhw_pn") != null) && (feature.getProperty("ho_mw_pn") != null)) {
                if (((Number)feature.getProperty("ho_bhw_pn")).doubleValue()
                            <= ((Number)feature.getProperty("ho_mw_pn")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribute ho_bhw_pn muss größer ho_k_f sein.");
                    return false;
                }
            }
            if ((feature.getProperty("ho_k_pn") != null) && (feature.getProperty("ho_mw_pn") != null)) {
                if (((Number)feature.getProperty("ho_k_pn")).doubleValue()
                            <= ((Number)feature.getProperty("ho_mw_pn")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribute ho_bhw_pn muss größer ho_mw_pn sein.");
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
    public FeatureCreator getFeatureCreator() {
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");

        return new LineAndStationCreator("ba_st", getDefaultValues(), routeMc, new LinearReferencingWatergisHelper());
    }

    @Override
    public Map<String, Object> getDefaultValues() {
        final Map properties = new HashMap();
        properties.put("ww_gr", AppBroker.getInstance().getOwnWwGr());

        return properties;
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
    public boolean isCatThree() {
        return true;
    }
}
