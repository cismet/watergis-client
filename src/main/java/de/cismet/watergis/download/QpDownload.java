/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.download;

import org.apache.log4j.Logger;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.downloadmanager.AbstractDownload;
import de.cismet.tools.gui.downloadmanager.Download;

import de.cismet.watergis.gui.actions.gaf.ReportAction;

/**
 * Creates a qp report.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpDownload extends AbstractDownload {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(QpDownload.class);

    //~ Instance fields --------------------------------------------------------

    private final String extension;
    private final CidsLayerFeature qpFeature;
    private final Integer qpNr;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QpDownload object. Either the qpNr or the qpFeature parameter can be null.
     *
     * @param  filename   DOCUMENT ME!
     * @param  extension  DOCUMENT ME!
     * @param  directory  fileToSaveTo DOCUMENT ME!
     * @param  qpNr       the qp nr of the qp object, the report should created of
     * @param  qpFeature  the qpFeature nr of the qp object, the report should created of
     */
    public QpDownload(
            final String filename,
            final String extension,
            final String directory,
            final Integer qpNr,
            final CidsLayerFeature qpFeature) {
        this.extension = extension;
        this.qpFeature = qpFeature;
        this.directory = directory;
        this.qpNr = qpNr;

        title = org.openide.util.NbBundle.getMessage(
                QpDownload.class,
                "QpDownload.title");

        status = Download.State.WAITING;
        determineDestinationFile(filename, extension);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != Download.State.WAITING) {
            return;
        }
        status = Download.State.RUNNING;

        stateChanged();
        // without the deleteFile invocation, the createReport method will ask, if the user
        // want to override the file
        deleteFile();

        if ((qpFeature != null) || (qpNr != null)) {
            try {
                if (qpFeature == null) {
                    ReportAction.createReport(qpNr, fileToSaveTo);
                } else {
                    ReportAction.createReport(qpFeature, fileToSaveTo);
                }
            } catch (Exception ex) {
                LOG.error("Error while creating qp report.", ex);
                status = Download.State.COMPLETED_WITH_ERROR;
                stateChanged();
                deleteFile();
                return;
            }
        } else {
            LOG.error("Error while creating qp report: No CidsLayerFeature");
            status = Download.State.COMPLETED_WITH_ERROR;
            stateChanged();
        }

        if (status == Download.State.RUNNING) {
            status = Download.State.COMPLETED;
            stateChanged();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean cancel() {
        boolean cancelled = true;
        if (downloadFuture != null) {
            cancelled = downloadFuture.cancel(true);
        }
        if (cancelled) {
            status = Download.State.ABORTED;
            stateChanged();
        }
        return cancelled;
    }

    /**
     * DOCUMENT ME!
     */
    private void deleteFile() {
        if (fileToSaveTo.exists() && fileToSaveTo.isFile()) {
            fileToSaveTo.delete();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = (37 * hash) + ((this.extension != null) ? this.extension.hashCode() : 0);
        hash = (37 * hash) + ((this.qpFeature != null) ? this.qpFeature.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QpDownload other = (QpDownload)obj;
        if ((this.extension == null) ? (other.extension != null) : (!this.extension.equals(other.extension))) {
            return false;
        }
        if ((this.qpFeature != other.qpFeature)
                    && ((this.qpFeature == null) || !this.qpFeature.equals(other.qpFeature))) {
            return false;
        }
        return true;
    }
}
