/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.profile;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.commons.security.WebDavClient;
import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;
import de.cismet.watergis.gui.panels.Photo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author therter
 */
public class QpNplDownload extends AbstractQpDownload {
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
    public QpNplDownload(final WebDavClient client,
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
