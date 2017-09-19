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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.sql.Timestamp;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.download.QpDownload;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpGafPpRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(QpGafPpRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        final Numeric rw = new Numeric(11, 2, false, false);
        final Numeric hw = new Numeric(10, 2, false, false);
        final Numeric y = new Numeric(6, 2, false);
        final Numeric z = new Numeric(6, 2, false);
        hw.setRange(5600000d, 6399999.99);
        rw.setRange(33000000d, 33999999.99);
        y.setRange(-999.99, 999.99);
        z.setRange(-19.99, 199.99);

        typeMap.put("geom", new Geom(true, false));
        typeMap.put("p_nr", new Numeric(20, 0, true, false));
        typeMap.put("qp_nr", new Numeric(20, 0, true, false));
        typeMap.put("id_gaf", new Varchar(50, true));
        typeMap.put("y", y);
        typeMap.put("z", z);
        typeMap.put("kz", new Catalogue("k_qp_gaf_kz", true, true));
        typeMap.put("rk", new Catalogue("k_qp_gaf_rk", false, true));
        typeMap.put("rk_name", new Varchar(75, false));
        typeMap.put("rk_k", new Numeric(6, 2, false));
        typeMap.put("rk_kst", new Numeric(6, 2, false));
        typeMap.put("bk", new Catalogue("k_qp_gaf_bk", false, true));
        typeMap.put("bk_name", new Varchar(75, false));
        typeMap.put("bk_ax", new Numeric(6, 2, false));
        typeMap.put("bk_ay", new Numeric(6, 2, false));
        typeMap.put("bk_dp", new Numeric(6, 3, false));
        typeMap.put("hw", new Numeric(10, 2, false, false));
        typeMap.put("rw", new Numeric(11, 2, false, false));
        typeMap.put("hw", hw);
        typeMap.put("rw", rw);
        typeMap.put("hyk", new Varchar(10, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("p_nr") && !columnName.equals("qp_nr")
                    && !columnName.equals("hyk")
                    && !columnName.equals("geom") && !columnName.equals("id");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        final Object result = super.afterEdit(feature, column, row, oldValue, newValue);
        if (column.equals("kz")) {
            Object catObject = newValue;

            if (catObject instanceof String) {
                catObject = getCatalogueElement("dlm25w.k_qp_gaf_kz", "kz", (String)catObject);
            }

            if (catObject instanceof CidsLayerFeature) {
                final Object hyk = ((CidsLayerFeature)catObject).getProperty("hyk");
                feature.setProperty("hyk", ((hyk == null) ? "x" : hyk));
            } else if (catObject instanceof CidsBean) {
                final Object hyk = ((CidsBean)catObject).getProperty("hyk");
                feature.setProperty("hyk", ((hyk == null) ? "x" : hyk));
            }
        }

        return result;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("qp_nr")) {
            return new LinkTableCellRenderer(JLabel.RIGHT);
        } else {
            return super.getCellRenderer(columnName);
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("kz")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("kz") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("rk")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("rk") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("bk")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("bk") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        }
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            final Object kz = feature.getProperty("kz");

            if (kz instanceof FeatureServiceFeature) {
                Object hyk = ((FeatureServiceFeature)kz).getProperty("hyk");

                if (hyk == null) {
                    hyk = "x";
                }

                feature.setProperty("hyk", hyk);
            }

            final Object rk = feature.getProperty("rk");
            final Object bk = feature.getProperty("bk");

            if (isValueEmpty(rk)
                        && (isValueEmpty(feature.getProperty("rk_name")) || isValueEmpty(feature.getProperty("rk_k"))
                            || isValueEmpty(feature.getProperty("rk_kst")))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Wenn das Attribut rk leer ist, dann müssen rk_name, rk_k und rk_kst gesetzt sein.",
                    "Fehlerhaft Attributbelegung",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (isValueEmpty(bk)
                        && (isValueEmpty(feature.getProperty("bk_name")) || isValueEmpty(feature.getProperty("bk_ax"))
                            || isValueEmpty(feature.getProperty("bk_ay"))
                            || isValueEmpty(feature.getProperty("bk_dp")))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Wenn das Attribut rk leer ist, dann müssen bk_name, bk_ax, bk_ay und bk_dp gesetzt sein.",
                    "Fehlerhaft Attributbelegung",
                    JOptionPane.ERROR_MESSAGE);
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

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return null;
    }

    @Override
    public boolean isCatThree() {
        return false;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("qp_nr")) {
            if ((value instanceof Integer) && (clickCount == 1) && (feature instanceof CidsLayerFeature)) {
                if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
                    final String jobname = DownloadManagerDialog.getJobname();
                    final String filename = value.toString();
                    final String extension = ".pdf";

                    DownloadManager.instance().add(new QpDownload(
                            filename,
                            extension,
                            jobname,
                            (Integer)value,
                            null));
                }
            }
        }
    }

//    @Override
//    public boolean hasCustomExportFeaturesMethod() {
//        return true;
//    }
//
//    @Override
//    public void exportFeatures() {
//        AppBroker.getInstance().getGafExport().actionPerformed(null);
//    }

//    @Override
//    public boolean hasCustomPrintFeaturesMethod() {
//        return true;
//    }
//
//    @Override
//    public void printFeatures() {
//        AppBroker.getInstance().getGafPrint().actionPerformed(null);
//    }
}
