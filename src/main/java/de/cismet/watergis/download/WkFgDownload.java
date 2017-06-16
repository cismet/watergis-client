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
package de.cismet.watergis.download;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import org.openide.util.NbBundle;

import java.io.File;

import de.cismet.cids.custom.reports.WkFgReport;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;
import de.cismet.tools.gui.downloadmanager.Download;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WkFgDownload extends AbstractCancellableDownload {

    //~ Static fields/initializers ---------------------------------------------

    private static final String PDF_FILE_EXTENSION = ".pdf";

    //~ Instance fields --------------------------------------------------------

// private final String targetDirectory;
    private final String wkFgWkk;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportShapeDownload object.
     *
     * @param  directory  file DOCUMENT ME!
     * @param  wkFgWkk    DOCUMENT ME!
     */
    public WkFgDownload(final String directory, final String wkFgWkk) {
        status = Download.State.WAITING;
        File file = new File(directory, wkFgWkk + PDF_FILE_EXTENSION);
        int index = 0;
        this.wkFgWkk = wkFgWkk;
        this.directory = directory;
        this.title = wkFgWkk;

        while (file.exists()) {
            file = new File(directory, wkFgWkk + (++index) + PDF_FILE_EXTENSION);
        }

        fileToSaveTo = file;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != Download.State.WAITING) {
            return;
        }

        status = Download.State.RUNNING;
        stateChanged();

        final MetaClass wkFgMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME_WRRL, "wk_fg");

        if (wkFgMc == null) {
            error(new Exception(NbBundle.getMessage(WkFgDownload.class, "WkFgDownload.run.noMc")));
            return;
        }
        final String query = "select " + wkFgMc.getID() + ", " + wkFgMc.getTableName() + "."
                    + wkFgMc.getPrimaryKey() + " from "
                    + wkFgMc.getTableName() + " WHERE wk_k = '" + wkFgWkk + "'"; // NOI18N
        try {
            final MetaObject[] mos = SessionManager.getProxy()
                        .getMetaObjectByQuery(SessionManager.getSession().getUser(), query, AppBroker.DOMAIN_NAME_WRRL);

            if ((mos != null) && (mos.length > 0)) {
                WkFgReport.createReport(fileToSaveTo.getAbsolutePath(), mos[0].getBean());
            } else {
                error(new Exception(NbBundle.getMessage(WkFgDownload.class, "WkFgDownload.run.objectNotFound")));
            }
        } catch (Exception ex) {
            error(ex);
            return;
        }

        if (status == Download.State.RUNNING) {
            status = Download.State.COMPLETED;
            stateChanged();
        }
    }
}
