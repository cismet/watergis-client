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

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.commons.security.WebDavClient;

import de.cismet.netutil.ProxyHandler;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.profile.QpNplDownload;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.FeatureServiceHelper;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpNplRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(QpNplRuleSet.class);
    private static final String[] ALLOWED_CALC_VALUES = { "calc" };

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, false));
        typeMap.put("la_cd", new Numeric(15, 0, true, false));
        typeMap.put("la_st", new Numeric(10, 2, false, false));
        typeMap.put("stat", new Numeric(10, 2, false, false));
        typeMap.put("re", new Numeric(11, 2, false, false));
        typeMap.put("ho", new Numeric(10, 2, false, false));
        typeMap.put("qp_nr", new Numeric(20, 0, false, false));
        typeMap.put("qp_hist", new Catalogue("k_hist", false, true, new Varchar(1, false, false)));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("l_bezug", new Catalogue("k_l_bezug", false, true, new Numeric(5, 0, false, false)));
        typeMap.put("h_bezug", new Catalogue("k_h_bezug", false, true, new Numeric(5, 0, false, false)));
        typeMap.put("l_calc", new Varchar(4, false, false, ALLOWED_CALC_VALUES));
        typeMap.put("upl_nr", new Numeric(20, 0, false, false));
        typeMap.put("upl_name", new Varchar(50, true, false));
        typeMap.put("upl_datum", new Varchar(10, true, false));
        typeMap.put("upl_zeit", new Varchar(8, true, false));
        typeMap.put("aufn_name", new Varchar(50, false, false));
        typeMap.put("aufn_datum", new Varchar(10, false, false));
        typeMap.put("aufn_zeit", new Varchar(8, false, false));
        typeMap.put("freigabe", new Catalogue("k_freigabe", false, true, new Varchar(10, false, false)));
        typeMap.put("titel", new Varchar(250, false, false));
        typeMap.put("beschreib", new Varchar(250, false, false));
        typeMap.put("bemerkung", new Varchar(250, false, false));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return columnName.equalsIgnoreCase("bemerkung") || columnName.equalsIgnoreCase("qp_hist");
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
        if (columnName.equals("qp_hist")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        "qp_hist",
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("hist");
                    }
                });

            return editor;
        } else {
            return super.getCellEditor(columnName);
        }
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
        reloadService("qp_pkte");
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return null;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("qp_nr")) {
            if ((value instanceof Integer) && (clickCount == 1) && (feature instanceof CidsLayerFeature)) {
                if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
                    final String jobname = DownloadManagerDialog.getInstance().getJobName();
                    final List<FeatureServiceFeature> features = new ArrayList<>();
                    features.add(feature);

                    final WebDavClient webDavClient = new WebDavClient(ProxyHandler.getInstance().getProxy(),
                            Photo.WEB_DAV_USER,
                            Photo.WEB_DAV_PASSWORD,
                            true);
                    final File f = new File(DownloadManager.instance().getDestinationDirectory(), jobname);

                    DownloadManager.instance()
                            .add(new QpNplDownload(webDavClient, f.getAbsolutePath(), "Download Profil", features));
                }
            }
        }
    }

    @Override
    public boolean hasCustomExportFeaturesMethod() {
        return true;
    }

    @Override
    public void exportFeatures() {
        try {
            List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                    AppBroker.QP_NPL_MC_NAME);

            if ((features == null) || features.isEmpty()) {
                final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                        AppBroker.QP_NPL_MC_NAME);

                if ((services != null) && !services.isEmpty()) {
                    features = FeatureServiceHelper.getFeatures(services.get(0), false);
                }
            }

            if ((features == null) || features.isEmpty()) {
                LOG.warn("No qp_npl objects found to create qp_npl export");
            }

            if (!features.isEmpty()) {
                if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
                    final String jobname = DownloadManagerDialog.getInstance().getJobName();

                    final WebDavClient webDavClient = new WebDavClient(ProxyHandler.getInstance().getProxy(),
                            Photo.WEB_DAV_USER,
                            Photo.WEB_DAV_PASSWORD,
                            true);
                    final File f = new File(DownloadManager.instance().getDestinationDirectory(), jobname);

                    DownloadManager.instance()
                            .add(new QpNplDownload(webDavClient, f.getAbsolutePath(), "Download NPL", features));
                }
            }
        } catch (Exception e) {
            LOG.error("Error while creating qp_npl export", e);
        }
    }
}
