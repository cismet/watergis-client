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

import java.util.List;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.Proxy;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.downloadmanager.WebDavDownload;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.foto.ReportAction;
import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FotoPrPfRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(FotoPrPfRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, false));
        typeMap.put("la_cd", new Numeric(15, 0, true, false));
        typeMap.put("la_st", new Numeric(10, 2, false, false));
        typeMap.put("l_st", new Catalogue("k_l_st", false, false));
        typeMap.put("l_rl", new Catalogue("k_l_rl", false, false));
        typeMap.put("re", new Numeric(11, 2, false, false));
        typeMap.put("ho", new Numeric(10, 2, false, false));
        typeMap.put("winkel", new Numeric(5, 1, false, false));
        typeMap.put("foto_nr", new Numeric(15, 0, false, false));
        typeMap.put("foto_nr_gu", new Varchar(50, false, false));
        typeMap.put("foto", new Varchar(250, false, false));
        typeMap.put("upl_name", new Varchar(50, false, false));
        typeMap.put("upl_datum", new Varchar(10, false, false));
        typeMap.put("upl_zeit", new Varchar(8, false, false));
        typeMap.put("aufn_name", new Varchar(50, false, false));
        typeMap.put("aufn_datum", new Varchar(10, false, false));
        typeMap.put("aufn_zeit", new Varchar(8, false, false));
        typeMap.put("freigabe", new Catalogue("k_freigabe", true, false));
        typeMap.put("titel", new Varchar(250, false, false));
        typeMap.put("beschreib", new Varchar(250, false, false));
        typeMap.put("bemerkung", new Varchar(250, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("foto")) {
            return new LinkTableCellRenderer();
        } else if (columnName.equals("foto_nr")) {
            return new LinkTableCellRenderer(JLabel.RIGHT);
        }

        return super.getCellRenderer(columnName);
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("foto")) {
            if ((value instanceof String) && (clickCount == 1) && (feature instanceof CidsLayerFeature)) {
                if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
                    final String jobname = DownloadManagerDialog.getJobname();
                    final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                    // the attribute dateipfad is not visible. So it can also be accessed via the bean.
                    final String path = Photo.WEB_DAV_DIRECTORY
                                + String.valueOf(cidsFeature.getBean().getProperty("dateipfad"));
                    final String file = value.toString();
                    String filename;
                    String extension = null;

                    if (file.contains(".")) {
                        extension = file.substring(file.lastIndexOf("."));
                        filename = file.substring(0, file.lastIndexOf("."));
                    } else {
                        filename = file;
                    }

                    final WebDavClient webDavClient = new WebDavClient(Proxy.fromPreferences(),
                            Photo.WEB_DAV_USER,
                            Photo.WEB_DAV_PASSWORD,
                            true);

                    DownloadManager.instance()
                            .add(new WebDavDownload(
                                    webDavClient,
                                    path
                                    + WebDavHelper.encodeURL(file),
                                    jobname,
                                    filename
                                    + extension,
                                    filename,
                                    extension));
                }
            }
        } else if (columnName.equals("foto_nr")) {
            if ((value != null) && (clickCount == 1)) {
                createFotoReport();
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void createFotoReport() {
        final ReportAction action = new ReportAction();
        action.actionPerformed(null);
    }

//    @Override
//    public boolean hasCustomPrintFeaturesMethod() {
//        return true;
//    }
//
//    @Override
//    public void printFeatures() {
//        AppBroker.getInstance().getPhotoPrint().actionPerformed(null);
//    }
}
