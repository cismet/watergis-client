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

import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.tools.CidsBeanFilter;

import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractBeanListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;
import de.cismet.watergis.utils.LinkTableCellRenderer;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.checkRangeBetweenOrEqual;
import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.getCurrentYear;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaDueRuleSet extends WatergisDefaultRuleSet {

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
        if (newValue == null) {
            if (column.equals("profil")) {
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

        if (column.equals("ho_e") && !checkRangeBetweenOrEqual(column, newValue, -6, 179, true)) {
            return oldValue;
        }
        if (column.equals("ho_a") && !checkRangeBetweenOrEqual(column, newValue, -6, 179, true)) {
            return oldValue;
        }
        if (column.equals("gefaelle") && !checkRangeBetweenOrEqual(column, newValue, -10, 100, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_e") && !checkRangeBetweenOrEqual(column, newValue, 0, 10, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_a") && !checkRangeBetweenOrEqual(column, newValue, 0, 10, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_m") && !checkRange(column, newValue, 0, 30, true, false, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_iab") && !checkRange(column, newValue, 0, 30, true, false, true)) {
            return oldValue;
        }
        if (column.equals("ho_d_iauf") && !checkRange(column, newValue, 0, 30, true, false, true)) {
            return oldValue;
        }
        if (column.equals("br_tr_o_li") && !checkRangeBetweenOrEqual(column, newValue, 0.025, 6, true)) {
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
        } else if (columnName.equals("material")) {
            final CidsBeanFilter filter;

            filter = createCidsBeanFilter("due");

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
        } else if (columnName.equals("profil")) {
            final CidsBeanFilter filter;

            filter = createCidsBeanFilter("due");

            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("profil") + " - " + bean.getProperty("name");
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
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
        for (final FeatureServiceFeature feature : features) {
            if (feature.getProperty("profil") == null) {
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
            if (!checkRangeBetweenOrEqual("gefaelle", feature.getProperty("gefaelle"), -10, 100, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_d_e", feature.getProperty("ho_d_e"), 0, 10, true)) {
                return false;
            }
            if (!checkRangeBetweenOrEqual("ho_d_a", feature.getProperty("ho_d_a"), 0, 10, true)) {
                return false;
            }
            if (!checkRange("ho_d_iab", feature.getProperty("ho_d_iab"), 0, 30, true, false, true)) {
                return false;
            }
            if (!checkRange("ho_d_iauf", feature.getProperty("ho_d_iauf"), 0, 30, true, false, true)) {
                return false;
            }

            if (!checkRange("ho_d_m", feature.getProperty("ho_d_m"), 0, 30, true, false, true)) {
                return false;
            }

            if (feature.getProperty("profil").equals("kr") || feature.getProperty("profil").equals("ei")) {
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
            }
            if (feature.getProperty("profil").equals("ei")) {
                if (!checkRangeBetweenOrEqual("ho_li", feature.getProperty("ho_li"), 25, 6000, true)) {
                    return false;
                }
            }
            if (feature.getProperty("profil").equals("re") || feature.getProperty("profil").equals("tr")) {
                if (!checkRangeBetweenOrEqual("ho_li", feature.getProperty("ho_li"), 0.025, 6, true)) {
                    return false;
                }
            }
            if (feature.getProperty("profil").equals("kr")
                        && ((feature.getProperty("ho_li") != null) || (feature.getProperty("br_tr_o_li") != null))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Die Attribute ho_li und br_tr_o_li dürfen nicht belegt sein, wenn profil = kr.");
                return false;
            }
            if ((isValueIn(feature.getProperty("profil"), new Object[] { "ei", "re" }, false))
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

        return true;
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

        return new StationLineCreator("ba_st", routeMc, new LinearReferencingWatergisHelper(), 0.5f, 250);
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
