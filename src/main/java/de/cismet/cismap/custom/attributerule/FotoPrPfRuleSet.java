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

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("foto")) {
            return new LinkTableCellRenderer();
        }
        return null;
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
        return true;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
    }

    @Override
    public void afterSave(final TableModel model) {
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
                            Photo.WEB_DAV_PASSWORD);

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
        }
    }

    @Override
    public boolean hasCustomPrintFeaturesMethod() {
        return true;
    }

    @Override
    public void printFeatures() {
        AppBroker.getInstance().getPhotoPrint().actionPerformed(null);
    }
}
