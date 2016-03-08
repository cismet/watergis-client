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


import org.apache.log4j.Logger;


import java.sql.Timestamp;

import java.util.List;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;


import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.download.QpDownload;

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
        if (columnName.equals("qp_nr")) {
            return new LinkTableCellRenderer();
        } else {
            return null;
        }
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

    @Override
    public boolean hasCustomExportFeaturesMethod() {
        return true;
    }

    @Override
    public void exportFeatures() {
        AppBroker.getInstance().getGafExport().actionPerformed(null);
    }

    @Override
    public boolean hasCustomPrintFeaturesMethod() {
        return true;
    }

    @Override
    public void printFeatures() {
        AppBroker.getInstance().getGafPrint().actionPerformed(null);
    }
}
