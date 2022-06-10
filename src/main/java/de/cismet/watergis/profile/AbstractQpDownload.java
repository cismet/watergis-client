/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.profile;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.commons.security.WebDavClient;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;

import de.cismet.watergis.broker.AppBroker;


import de.cismet.watergis.utils.JumpShapeWriter;

import static de.cismet.watergis.gui.actions.gaf.ExportAction.downloadMetaDocument;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractQpDownload extends AbstractCancellableDownload {

    //~ Static fields/initializers ---------------------------------------------

    private static final int MAX_BUFFER_SIZE = 1024;

    //~ Instance fields --------------------------------------------------------

    protected final WebDavClient client;
    protected final List<FeatureServiceFeature> features;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WebDavDownload object.
     *
     * @param  client     DOCUMENT ME!
     * @param  directory  DOCUMENT ME!
     * @param  title      DOCUMENT ME!
     * @param  features   path DOCUMENT ME!
     */
    public AbstractQpDownload(final WebDavClient client,
            final String directory,
            final String title,
            final List<FeatureServiceFeature> features) {
        this.client = client;
        this.directory = directory;
        this.title = title;
        this.features = features;

        if (!this.directory.endsWith("/")) {
            this.directory += "/";
        }

        status = State.WAITING;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   features        DOCUMENT ME!
     * @param   outputFileStem  DOCUMENT ME!
     * @param   createPrj       DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected void createDbfAndMetaDoc(final List<FeatureServiceFeature> features,
            final String outputFileStem,
            final boolean createPrj) throws Exception {
        createShapeAndMetaDoc(features, outputFileStem, false);
        File file = new File(outputFileStem + ".shp");

        if (file.exists()) {
            file.delete();
        }
        file = new File(outputFileStem + ".shx");

        if (file.exists()) {
            file.delete();
        }
        file = new File(outputFileStem + ".prj");

        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   features        DOCUMENT ME!
     * @param   outputFileStem  DOCUMENT ME!
     * @param   createPrj       DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected void createShapeAndMetaDoc(final List<FeatureServiceFeature> features,
            final String outputFileStem,
            final boolean createPrj) throws Exception {
        final CidsLayer service = (CidsLayer)features.get(0).getLayerProperties().getFeatureService();
        final JumpShapeWriter shapeWriter = new JumpShapeWriter();
        final String charset = Charset.defaultCharset().name();

        shapeWriter.writeShpFile(features.toArray(new FeatureServiceFeature[features.size()]),
            new File(outputFileStem + ".shp"),
            null,
            charset);

        if (createPrj) {
            // create prj
            final BufferedWriter bw = new BufferedWriter(new FileWriter(
                        outputFileStem
                                + ".prj"));
            bw.write(AppBroker.PRJ_CONTENT);
            bw.close();
        }

        final BufferedWriter bwCpg = new BufferedWriter(new FileWriter(
                    outputFileStem
                            + ".cpg"));
        bwCpg.write(charset);
        bwCpg.close();
        // create meta document
        downloadMetaDocument(service, outputFileStem);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof AbstractQpDownload)) {
            return false;
        }

        final AbstractQpDownload other = (AbstractQpDownload)obj;

        boolean result = true;

        if ((this.features != other.features) && ((this.features == null) || !this.features.equals(other.features))) {
            result &= false;
        }

        if ((this.directory == null) ? (other.directory != null) : (!this.directory.equals(other.directory))) {
            result &= false;
        }

        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = (43 * hash) + ((this.features != null) ? this.features.hashCode() : 0);
        hash = (43 * hash) + ((this.fileToSaveTo != null) ? this.fileToSaveTo.hashCode() : 0);

        return hash;
    }

    /**
     * DOCUMENT ME!
     */
    protected void deleteFile() {
        if (fileToSaveTo.exists() && fileToSaveTo.isFile()) {
            fileToSaveTo.delete();
        }
    }
}
