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

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Timestamp;

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
import de.cismet.cismap.cidslayer.LineAndStationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractBeanListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;
import de.cismet.watergis.utils.LinkTableCellRenderer;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.checkRangeBetweenOrEqual;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaDeichRuleSet extends WatergisDefaultRuleSet {

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
        if (newValue == null) {
            if (column.equalsIgnoreCase("deich") || column.equalsIgnoreCase("ord") || column.equalsIgnoreCase("l_fk")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
                return oldValue;
            }
        }

        if (column.equals("ausbaujahr")
                    && !checkRangeBetweenOrEqual(column, newValue, 1800, getCurrentYear() + 2, true)) {
            return oldValue;
        }

        if (column.equals("bv_w") && !checkRangeBetweenOrEqual(column, newValue, 1, 15, true)) {
            return oldValue;
        }

        if (column.equals("bv_b") && !checkRangeBetweenOrEqual(column, newValue, 1, 15, true)) {
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
        if (columnName.equals("ba_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ba_st_bis")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ww_gr")) {
            CidsBeanFilter filter = null;

            if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren")) {
                final String userName = AppBroker.getInstance().getOwner();
                filter = new CidsBeanFilter() {

                        @Override
                        public boolean accept(final CidsBean bean) {
                            if (bean == null) {
                                return false;
                            }
                            return bean.getProperty("owner").equals(userName);
                        }
                    };
            } else {
                filter = new CidsBeanFilter() {

                        @Override
                        public boolean accept(final CidsBean bean) {
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
        } else if (columnName.equals("l_rl")) {
            final CidsBeanFilter filter = createCidsBeanFilter("deich");
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

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
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

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
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

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
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

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("schgr") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.startsWith("material")) {
            final AbstractBeanListCellRenderer renderer = new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("material") + " - " + bean.getProperty("name");
                    }
                };

            final CidsBeanFilter filter;

            if (columnName.equals("material_f")) {
                filter = createCidsBeanFilter("deich_f");
            } else if (columnName.equals("material_w")) {
                filter = createCidsBeanFilter("deich_w");
            } else if (columnName.equals("material_k")) {
                filter = createCidsBeanFilter("deich_k");
            } else if (columnName.equals("material_i")) {
                filter = createCidsBeanFilter("deich_i");
            } else if (columnName.equals("material_b")) {
                filter = createCidsBeanFilter("deich_b");
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
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
        for (final FeatureServiceFeature feature : features) {
            if (feature.getProperty("deich") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut deich darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("ord") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut ord darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("l_fk") == null) {
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
            value = geom.getLength();
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
