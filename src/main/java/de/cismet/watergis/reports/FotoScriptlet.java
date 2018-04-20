/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.reports;

import com.vividsolutions.jts.geom.Geometry;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.fill.JRFillField;
import net.sf.jasperreports.engine.fill.JRFillParameter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.InputStream;

import java.util.ResourceBundle;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.imageio.ImageIO;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.HeadlessMapProvider;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.Proxy;

import de.cismet.tools.PasswordEncrypter;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FotoScriptlet extends JRDefaultScriptlet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(FotoScriptlet.class);

    private static final String WEB_DAV_USER;
    private static final String WEB_DAV_PASSWORD;
    private static final String WEB_DAV_DIRECTORY;

    private static final WebDavClient webDavClient;

    static {
        final ResourceBundle bundle = ResourceBundle.getBundle("WebDav");
        String pass = bundle.getString("password");

        if ((pass != null)) {
            pass = new String(PasswordEncrypter.decrypt(pass.toCharArray(), true));
        }

        WEB_DAV_PASSWORD = pass;
        WEB_DAV_USER = bundle.getString("username");
        WEB_DAV_DIRECTORY = bundle.getString("url");

        webDavClient = new WebDavClient(
                Proxy.fromPreferences(),
                WEB_DAV_USER,
                WEB_DAV_PASSWORD,
                true);
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FotoScriptlet object.
     */
    public FotoScriptlet() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Image loadFoto() {
        Image result = null;

        final String path = (String)((JRFillField)fieldsMap.get("dateipfad")).getValue();
        final String filename = (String)((JRFillField)fieldsMap.get("foto")).getValue();
        final String file = path + filename;
        final String encodedFilename = WebDavHelper.encodeURL(file);

        InputStream inputStream = null;
        try {
            inputStream = webDavClient.getInputStream(WEB_DAV_DIRECTORY + encodedFilename);
            result = ImageIO.read(inputStream);
        } catch (IOException ex) {
            LOG.error("Couldn't load photo", ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    LOG.error("Couldn't close stream for " + encodedFilename, ex);
                }
            }
        }

        if (result == null) {
            try {
                result = ImageIO.read(getClass().getResource(
                            "/de/cismet/watergis/res/icon128/file-broken.png"));
            } catch (IOException ex1) {
                LOG.error("Couldn't load fallback photo", ex1);
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Image generateMap() {
        try {
            final Geometry point = (Geometry)((JRFillParameter)parametersMap.get("punkt")).getValue();
            final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
            final HeadlessMapProvider mapProvider = HeadlessMapProvider.createHeadlessMapProviderAndAddLayers(mc);
            final XBoundingBox boundingBox = new XBoundingBox(point);

            boundingBox.setX1(boundingBox.getX1() - 50);
            boundingBox.setY1(boundingBox.getY1() - 50);
            boundingBox.setX2(boundingBox.getX2() + 50);
            boundingBox.setY2(boundingBox.getY2() + 50);

            final BoundingBox scaledBBox = mc.getScaledBoundingBox(500, boundingBox);
            final int srid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());
            final XBoundingBox bbox = new XBoundingBox(scaledBBox.getGeometry(srid));

            mapProvider.setBoundingBox(bbox);
            return mapProvider.getImageAndWait(311, 236);
        } catch (Exception e) {
            LOG.error("Error while retrieving map.", e);
            return null;
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public class SignallingRetrievalListener implements RetrievalListener {

        //~ Instance fields ----------------------------------------------------

        private BufferedImage image = null;
        private final Lock lock;
        private Condition condition;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SignallingRetrievalListener object.
         *
         * @param  lock       DOCUMENT ME!
         * @param  condition  DOCUMENT ME!
         */
        public SignallingRetrievalListener(final Lock lock, final Condition condition) {
            this.lock = lock;
            this.condition = condition;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void retrievalStarted(final RetrievalEvent e) {
        }

        @Override
        public void retrievalProgress(final RetrievalEvent e) {
        }

        @Override
        public void retrievalComplete(final RetrievalEvent e) {
            if (e.getRetrievedObject() instanceof Image) {
                final Image retrievedImage = (Image)e.getRetrievedObject();
                image = new BufferedImage(
                        retrievedImage.getWidth(null),
                        retrievedImage.getHeight(null),
                        BufferedImage.TYPE_INT_RGB);
                final Graphics2D g = (Graphics2D)image.getGraphics();
                g.drawImage(retrievedImage, 0, 0, null);
                g.dispose();
            }
            signalAll();
        }

        @Override
        public void retrievalAborted(final RetrievalEvent e) {
            signalAll();
        }

        @Override
        public void retrievalError(final RetrievalEvent e) {
            signalAll();
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public BufferedImage getRetrievedImage() {
            return image;
        }

        /**
         * DOCUMENT ME!
         */
        private void signalAll() {
            lock.lock();
            try {
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}
