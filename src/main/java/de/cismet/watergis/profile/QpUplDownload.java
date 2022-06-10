/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.profile;

import org.openide.util.Exceptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.commons.security.WebDavClient;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.utils.JumpShapeWriter;

import static de.cismet.watergis.gui.actions.gaf.ExportAction.downloadMetaDocument;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpUplDownload extends AbstractQpDownload {

    //~ Static fields/initializers ---------------------------------------------

    private static final int MAX_BUFFER_SIZE = 1024;

    //~ Instance fields --------------------------------------------------------

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WebDavDownload object.
     *
     * @param  client     DOCUMENT ME!
     * @param  directory  DOCUMENT ME!
     * @param  title      DOCUMENT ME!
     * @param  features   path DOCUMENT ME!
     */
    public QpUplDownload(final WebDavClient client,
            final String directory,
            final String title,
            final List<FeatureServiceFeature> features) {
        super(client, directory, title, features);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != State.WAITING) {
            return;
        }

        status = State.RUNNING;

        FileOutputStream out = null;
        InputStream resp = null;

        stateChanged();

        for (final FeatureServiceFeature feature : this.features) {
            try {
                final String fileName = "qp-" + String.valueOf(feature.getProperty("upl_nr")) + ".zip";
                final String filePrefix = "qp/";
                resp = client.getInputStream(Photo.WEB_DAV_DIRECTORY + filePrefix + fileName);

                out = new FileOutputStream(this.directory + fileName);
                final byte[] buffer = new byte[MAX_BUFFER_SIZE];
                int read;

                while ((read = resp.read(buffer)) != -1) {
                    if (Thread.interrupted()) {
                        log.info("Download was interuppted");
                        out.close();
                        resp.close();
                        deleteFile();
                        return;
                    }

                    out.write(buffer, 0, read);
                }
            } catch (Exception ex) {
                error(ex);
            } finally {
                // Close file.
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e) {
                        log.warn("Exception occured while closing file.", e);
                    }
                }

                // Close connection to server.
                if (resp != null) {
                    try {
                        resp.close();
                    } catch (Exception e) {
                        log.warn("Exception occured while closing response stream.", e);
                    }
                }
            }
        }

        try {
            final File shapeFile = new File(this.directory, "qp_upl");
            createShapeAndMetaDoc(features, this.directory + "qp_upl", true);
        } catch (Exception ex) {
            error(ex);
        }

        if (status == State.RUNNING) {
            status = State.COMPLETED;
            stateChanged();
        }
    }
}
