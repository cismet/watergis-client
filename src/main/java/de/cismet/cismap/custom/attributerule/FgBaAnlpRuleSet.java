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
import de.cismet.cismap.cidslayer.StationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractBeanListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;
import de.cismet.watergis.utils.LinkTableCellRenderer;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.checkRange;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaAnlpRuleSet extends WatergisDefaultRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("ww_gr") && !columnName.equals("ba_cd") && !columnName.equals("geom")
                    && !columnName.equals("obj_nr") && !columnName.equals("id");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (newValue == null) {
            if (column.equals("anlp") || column.equals("l_rl")) {
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

        if (column.equals("br") && !checkRange(column, newValue, 0, 200, true, false, true)) {
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
        } else if (columnName.equals("anlp")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("anlp") + " - " + bean.getProperty("name");
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
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    createCidsBeanFilter("anlp"));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("l_rl") + " - " + bean.getProperty("name");
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
            if (feature.getProperty("anlp") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut anlp darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("l_rl") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut l_rl darf nicht leer sein");
                return false;
            }
            if (feature.getProperty("br") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut br darf nicht leer sein");
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

            if (isValueIn(feature.getProperty("anlp"), new Object[] { "Steg", "VT", "Wee" }, false)) {
                final Number br = toNumber(feature.getProperty("br"));

                if ((br.doubleValue() < 1) || (br.doubleValue() > 200)) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut br muss zwischen 1 und 200 liegen, wenn anlp = Steg/VT/Wee");
                    return false;
                }
            } else {
                final Number br = toNumber(feature.getProperty("br"));

                if ((br.doubleValue() < 0) || (br.doubleValue() > 10)) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut br muss zwischen 0 und 10 liegen, wenn anlp nicht gleich Steg/VT/Wee");
                    return false;
                }
            }

            if (isValueIn(
                            feature.getProperty("anlp"),
                            new Object[] { "Albw", "Elbw", "Fu", "Rsk", "Schi", "Slu", "Stt" },
                            false)) {
                final Object rl = feature.getProperty("l_rl");

                if ((rl != null) && !rl.toString().equals("mi")) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Bei anlp = Albw/Elbw/Fu/Rsk/Schi/Slu/Stt darf das Attribut l_rl nur den Wert mi haben.");
                    return false;
                }
            } else if (isValueIn(
                            feature.getProperty("anlp"),
                            new Object[] { "P", "P-Gr", "P-Steg", "P-Gr-Ste", "P-Lat", "Sta" },
                            false)) {
                final Object rl = feature.getProperty("l_rl");

                if ((rl == null)
                            || !(rl.toString().equals("mi") || rl.toString().equals("re")
                                || rl.toString().equals("li") || rl.toString().equals("bs"))) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Bei anlp = P/P-Gr/P-Steg/P-Gr-Ste/P-Lat/Sta darf das Attribut l_rl nur die Werte re/li/mi/bs haben.");
                    return false;
                }
            } else if (isValueIn(feature.getProperty("anlp"), new Object[] { "Steg", "Vt", "Wes" }, false)) {
                final Object rl = feature.getProperty("l_rl");

                if ((rl == null)
                            || !(rl.toString().equals("bs") || rl.toString().equals("re")
                                || rl.toString().equals("li"))) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Bei anlp = Steg/Vt/Wes darf das Attribut l_rl nur die Werte re/li/bs haben.");
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

        if (isValueIn(
                        feature.getProperty("anlp"),
                        new Object[] { "Albw", "Elbw", "Fu", "Rsk", "Schi", "Slu", "Stt" },
                        false)) {
            final Object rl = feature.getProperty("l_rl");

            if (rl == null) {
                feature.setProperty("l_rl", getCatalogueElement("dlm25w.k_l_rl", "l_rl", "mi"));
            }
        }
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        // todo: nur auf eigenen Basisrouten
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
