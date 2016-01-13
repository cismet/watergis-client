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
package de.cismet.watergis.utils;

import java.io.File;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;
import de.cismet.tools.gui.downloadmanager.Download;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FakeFileDownload extends AbstractCancellableDownload {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportShapeDownload object. The init method must be invoked before the download can be started, if
     * this constructor is used.
     */
    public FakeFileDownload() {
    }

    /**
     * Creates a new ExportShapeDownload object.
     *
     * @param  file  DOCUMENT ME!
     */
    public FakeFileDownload(final File file) {
        status = Download.State.WAITING;
        fileToSaveTo = file;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != Download.State.WAITING) {
            return;
        }

        status = Download.State.RUNNING;

        if (status == Download.State.RUNNING) {
            status = Download.State.COMPLETED;
            stateChanged();
        }
    }

    @Override
    public String toString() {
        return "FakeFileDownload";
    }
}
