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
import net.sf.jasperreports.engine.fill.JRFillParameter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.imageio.ImageIO;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.HeadlessMapProvider;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.GafReader;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GafScriptlet extends JRDefaultScriptlet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GafScriptlet.class);

    //~ Instance fields --------------------------------------------------------

    private final CidsLayer ppLayer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                AppBroker.DOMAIN_NAME,
                "dlm25w.qp_gaf_pp"));

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FotoScriptlet object.
     */
    public GafScriptlet() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Image loadPreview() {
        Image result = null;

        try {
            final String qpNr = (String)((JRFillParameter)parametersMap.get("qp_nr")).getValue();

            ppLayer.initAndWait();
            final List<DefaultFeatureServiceFeature> features = ppLayer.getFeatureFactory()
                        .createFeatures("qp_nr = " + qpNr,
                            null,
                            null,
                            0,
                            0,
                            null);
            if (features != null) {
                final GafReader reader = new GafReader(features);

                result = reader.createImage(reader.getProfiles().toArray(new Double[1])[0], 311, 226);
            } else {
                if (result == null) {
                    try {
                        result = ImageIO.read(getClass().getResource(
                                    "/de/cismet/watergis/res/icon128/file-broken.png"));
                    } catch (IOException ex1) {
                        LOG.error("Couldn't load fallback photo", ex1);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot create gaf preview image", e);

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
        private final Condition condition;

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
