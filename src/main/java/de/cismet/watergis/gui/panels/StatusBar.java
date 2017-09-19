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
package de.cismet.watergis.gui.panels;

import Sirius.navigator.DefaultNavigatorExceptionHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.JPanel;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.statusbar.ServicesBusyPanel;
import de.cismet.cismap.commons.gui.statusbar.ServicesErrorPanel;
import de.cismet.cismap.commons.gui.statusbar.ServicesRetrievedPanel;
import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.StatusListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.StatusEvent;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.RepaintEvent;
import de.cismet.cismap.commons.retrieval.RepaintListener;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class StatusBar extends javax.swing.JPanel implements StatusListener,
    FeatureCollectionListener,
    ActiveLayerListener,
    RepaintListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(StatusBar.class);
    private static final int K_DIVISOR = 1000;
    private static final int K_SQUARE_DIVISOR = K_DIVISOR * K_DIVISOR;

    //~ Instance fields --------------------------------------------------------

    private Map<ServiceLayer, Integer> services = new HashMap<ServiceLayer, Integer>();
    private Collection<ServiceLayer> erroneousServices = new HashSet<ServiceLayer>();
    private JPanel servicesBusyPanel = new ServicesBusyPanel();
    private JPanel servicesRetrievedPanel = new ServicesRetrievedPanel();
    private JPanel servicesErrorPanel = new ServicesErrorPanel();

    private Timer timer;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cismet.tools.gui.exceptionnotification.ExceptionNotificationStatusPanel exceptionNotificationStatusPanel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblCoordinates;
    private javax.swing.JLabel lblMeasuring;
    private javax.swing.JLabel lblNotification;
    private javax.swing.JPanel pnlCoordinates;
    private javax.swing.JPanel pnlMeasuring;
    private javax.swing.JPanel pnlNotification;
    private javax.swing.JPanel pnlServicesStatus;
    private de.cismet.cismap.commons.gui.statusbar.ServicesRetrievedPanel servicesRetrievedPanel1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StatusBar object.
     */
    public StatusBar() {
        initComponents();
        lblCoordinates.setText(""); // NOI18N
        try {
            AppBroker.getInstance().getMappingComponent().getFeatureCollection().addFeatureCollectionListener(this);
            AppBroker.getInstance().getMappingComponent().addRepaintListener(this);
        } catch (NullPointerException e) {
        }
        DefaultNavigatorExceptionHandler.getInstance().addListener(exceptionNotificationStatusPanel);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0),
                new java.awt.Dimension(32767, 0));
        pnlServicesStatus = new javax.swing.JPanel();
        servicesRetrievedPanel1 = new de.cismet.cismap.commons.gui.statusbar.ServicesRetrievedPanel();
        jPanel2 = new javax.swing.JPanel();
        pnlNotification = new javax.swing.JPanel();
        lblNotification = new javax.swing.JLabel();
        pnlMeasuring = new javax.swing.JPanel();
        lblMeasuring = new javax.swing.JLabel();
        pnlCoordinates = new javax.swing.JPanel();
        lblCoordinates = new javax.swing.JLabel();
        exceptionNotificationStatusPanel =
            new de.cismet.tools.gui.exceptionnotification.ExceptionNotificationStatusPanel();
        jPanel1 = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(32769, 20));
        setMinimumSize(new java.awt.Dimension(200, 20));
        setPreferredSize(new java.awt.Dimension(500, 20));
        addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(final java.awt.event.MouseEvent evt) {
                    formMouseClicked(evt);
                }
            });
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.weightx = 1.0;
        add(filler1, gridBagConstraints);

        pnlServicesStatus.setLayout(new java.awt.BorderLayout());
        pnlServicesStatus.add(servicesRetrievedPanel1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        add(pnlServicesStatus, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(jPanel2, gridBagConstraints);

        pnlNotification.setLayout(new java.awt.BorderLayout());
        pnlNotification.add(lblNotification, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 10);
        add(pnlNotification, gridBagConstraints);

        pnlMeasuring.setLayout(new java.awt.BorderLayout());
        pnlMeasuring.add(lblMeasuring, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 10);
        add(pnlMeasuring, gridBagConstraints);

        pnlCoordinates.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            lblCoordinates,
            org.openide.util.NbBundle.getMessage(StatusBar.class, "StatusBar.lblCoordinates.text")); // NOI18N
        pnlCoordinates.add(lblCoordinates, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(pnlCoordinates, gridBagConstraints);
        add(exceptionNotificationStatusPanel, new java.awt.GridBagConstraints());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        add(jPanel1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void formMouseClicked(final java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        if (evt.getClickCount() > 1) {
            if (!CismapBroker.getInstance().getMappingComponent().isInternalLayerWidgetVisible()) {
                CismapBroker.getInstance().getMappingComponent().showInternalLayerWidget(true, 300);
            } else {
                CismapBroker.getInstance().getMappingComponent().showInternalLayerWidget(false, 150);
            }
        }
    }//GEN-LAST:event_formMouseClicked

    /**
     * Shows the given text in the status bar. After 5 seconds it will be removed.
     *
     * @param  text  text to show in the status bar
     */
    public synchronized void showNotification(final String text) {
        if (timer != null) {
            timer.cancel();
        }
        lblNotification.setText(text);

        timer = new Timer();

        timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    lblNotification.setText("");
                }
            }, 5000);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void statusValueChanged(final StatusEvent e) {
        if (!EventQueue.isDispatchThread()) {
            LOG.warn("status bar event invocation not in edt. This can lead to an error. Event = " + e.getName(),
                new Exception());

            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        statusValueChanged(e);
                    }
                });

            return;
        }

        if (e.getName().equals(StatusEvent.MAPPING_MODE)) {
            // do nothing
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_STARTED)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entered RETRIEVAL_STARTED: " + e.getValue() + " (" + System.currentTimeMillis() + ")");
            }

            if (e.getValue() instanceof ServiceLayer) {
                final ServiceLayer service = (ServiceLayer)e.getValue();
                if (erroneousServices.contains(service)) {
                    erroneousServices.remove(service);
                }
                addService(service);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("RETRIEVAL_STARTED (" + e.getValue() + ", " + System.currentTimeMillis()
                            + ") - services started: " + services.size() + ", erroneous services: "
                            + erroneousServices.size());
            }
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_COMPLETED)) {
            // use repaintComplete instead
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_ABORTED)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entered RETRIEVAL_ABORTED: " + e.getValue() + " (" + System.currentTimeMillis() + ")");
            }

            if (e.getValue() instanceof ServiceLayer) {
                final ServiceLayer service = (ServiceLayer)e.getValue();
                removeService(service);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("RETRIEVAL_ABORTED (" + e.getValue() + ", " + System.currentTimeMillis()
                            + ") - services started: " + services.size() + ", erroneous services: "
                            + erroneousServices.size());
            }
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_ERROR)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entered RETRIEVAL_ERROR: " + e.getValue() + " (" + System.currentTimeMillis() + ")");
            }

            if (e.getValue() instanceof ServiceLayer) {
                final ServiceLayer service = (ServiceLayer)e.getValue();
                removeService(service);
                erroneousServices.add(service);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("RETRIEVAL_ERROR (" + e.getValue() + ", " + System.currentTimeMillis()
                            + ") - services started: " + services.size() + ", erroneous services: "
                            + erroneousServices.size());
            }
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_REMOVED)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entered RETRIEVAL_REMOVED: " + e.getValue() + " (" + System.currentTimeMillis() + ")");
            }

            if (e.getValue() instanceof ServiceLayer) {
                final ServiceLayer service = (ServiceLayer)e.getValue();
                removeService(service);
                if (erroneousServices.contains(service)) {
                    erroneousServices.remove(service);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("RETRIEVAL_REMOVED (" + e.getValue() + ", " + System.currentTimeMillis()
                            + ") - services started: " + services.size() + ", erroneous services: "
                            + erroneousServices.size());
            }
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_RESET)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entered RETRIEVAL_RESET: " + e.getValue() + " (" + System.currentTimeMillis() + ")");
            }

            services.clear();
            erroneousServices.clear();

            if (LOG.isDebugEnabled()) {
                LOG.debug("RETRIEVAL_RESET (" + e.getValue() + ", " + System.currentTimeMillis()
                            + ") - services started: " + services.size() + ", erroneous services: "
                            + erroneousServices.size());
            }
        }

        refreshControls(e);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    private void addService(final ServiceLayer service) {
        Integer count = services.get(service);

        if (count == null) {
            services.put(service, 1);
        } else {
            services.put(service, ++count);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    private void removeService(final ServiceLayer service) {
        Integer count = services.get(service);

        if (count != null) {
            if (count < 2) {
                services.remove(service);
            } else {
                services.put(service, --count);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    private void refreshControls(final StatusEvent e) {
        final Runnable modifyControls = new Runnable() {

                @Override
                public void run() {
                    final TreeMap<Integer, MapService> services = CismapBroker.getInstance()
                                .getMappingComponent()
                                .getMappingModel()
                                .getRasterServices();
                    int stat = 0;

                    for (final Integer key : services.keySet()) {
                        final MapService service = services.get(key);

                        if (!service.isVisible() || (service.getPNode() == null)
                                    || (service.getPNode().getTransparency() == 0.0)) {
                            continue;
                        }

                        if ((service instanceof RetrievalServiceLayer)
                                    && !((RetrievalServiceLayer)service).isEnabled()) {
                            continue;
                        }

                        final int progress = service.getProgress();

                        if (service.isRefreshNeeded()) {
                            if (stat == 0) {
                                stat = 1;
                            }
                        } else if ((progress == -1) || ((progress > 0) && (progress < 100))) {
                            if (stat == 0) {
                                stat = 1;
                            }
                        } else if ((service instanceof RetrievalServiceLayer)
                                    && ((RetrievalServiceLayer)service).hasErrors()) {
                            stat = 2;
                        }
                    }

                    pnlServicesStatus.removeAll();

                    if (stat == 0) {
                        pnlServicesStatus.add(servicesRetrievedPanel, BorderLayout.CENTER);
                    } else if (stat == 1) {
                        pnlServicesStatus.add(servicesBusyPanel, BorderLayout.CENTER);
                    } else if (stat == 2) {
                        pnlServicesStatus.add(servicesErrorPanel, BorderLayout.CENTER);
                    }
                    pnlServicesStatus.revalidate();
                    pnlServicesStatus.repaint();

                    if (e == null) {
                    } else if (e.getName().equals(StatusEvent.COORDINATE_STRING)) {
                        final Coordinate c = (Coordinate)e.getValue();
                        lblCoordinates.setText(MappingComponent.getCoordinateString(c.x, c.y));
                    } else if (e.getName().equals(StatusEvent.MEASUREMENT_INFOS)) {
                        // do nothing
                    } else if (e.getName().equals(StatusEvent.MAPPING_MODE)) {
                        // do nothing                                                          // NOI18N
                    } else if (e.getName().equals(StatusEvent.OBJECT_INFOS)) {
                        // do nothing
                    } else if (e.getName().equals(StatusEvent.SCALE)) {
                        // do nothing
                    } else if (e.getName().equals(StatusEvent.CRS)) {
                        lblCoordinates.setToolTipText(((Crs)e.getValue()).getShortname());
                    } else if (e.getName().equals(StatusEvent.RETRIEVAL_STARTED)) {
                    } else if (e.getName().equals(StatusEvent.MAP_EXTEND_FIXED)) {
                        // do nothing
                    } else if (e.getName().equals(StatusEvent.MAP_SCALE_FIXED)) {
                        // do nothing
                    }
                }
            };

        if (EventQueue.isDispatchThread()) {
            modifyControls.run();
        } else {
            EventQueue.invokeLater(modifyControls);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   d  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double roundTo2Decimals(final double d) {
        return (((int)(d * 100)) / 100.0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    private void showMeasuring(final Feature f) {
        final Geometry geom = f.getGeometry();
        double area = geom.getArea();
        double length = geom.getLength();

        if (geom.getArea() == 0) {
            final int segments = geom.getNumGeometries() * (geom.getNumPoints() - 1);
            double lastSegmentLength = 0;

            if (geom.getNumPoints() > 1) {
                final Coordinate start = geom.getCoordinates()[geom.getNumPoints() - 2];
                final Coordinate end = geom.getCoordinates()[geom.getNumPoints() - 1];
                final GeometryFactory fg = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));
                final Geometry lastSegment = fg.createLineString(new Coordinate[] { start, end });
                lastSegmentLength = lastSegment.getLength();
            }

            if (length < 10000) {
                lblMeasuring.setText(NbBundle.getMessage(
                        StatusBar.class,
                        "StatusBar.lblMeasuring.text.length.m",
                        segments,
                        roundTo2Decimals(lastSegmentLength),
                        roundTo2Decimals(length)));
            } else {
                length /= K_DIVISOR;
                lastSegmentLength /= K_DIVISOR;
                lblMeasuring.setText(NbBundle.getMessage(
                        StatusBar.class,
                        "StatusBar.lblMeasuring.text.length.km",
                        segments,
                        roundTo2Decimals(lastSegmentLength),
                        roundTo2Decimals(length)));
            }
        } else {
            if (length < 10000) {
                lblMeasuring.setText(NbBundle.getMessage(
                        StatusBar.class,
                        "StatusBar.lblMeasuring.text.m",
                        roundTo2Decimals(length),
                        roundTo2Decimals(area)));
            } else {
                area /= K_SQUARE_DIVISOR;
                length /= K_DIVISOR;
                lblMeasuring.setText(NbBundle.getMessage(
                        StatusBar.class,
                        "StatusBar.lblMeasuring.text.km",
                        roundTo2Decimals(length),
                        roundTo2Decimals(area)));
            }
        }
    }

    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
        final Collection<Feature> features = fce.getEventFeatures();

        if (AppBroker.getInstance().getMappingComponent().getInteractionMode().equals(AppBroker.MEASURE_MODE)) {
            if ((features != null) && (features.size() == 1)) {
                final Feature f = features.toArray(new Feature[1])[0];
                showMeasuring(f);
            }
        }
    }

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
        lblMeasuring.setText("");
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
        final Collection<Feature> features = fce.getEventFeatures();

        if (AppBroker.getInstance().getMappingComponent().getInteractionMode().equals(AppBroker.MEASURE_MODE)) {
            if ((features != null) && (features.size() == 1)) {
                final Feature f = features.toArray(new Feature[1])[0];
                showMeasuring(f);
            }
        }
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureCollectionChanged() {
    }
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void layerAdded(final ActiveLayerEvent e) {
        // TODO: Use this for counting starting retrievals?
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void layerRemoved(final ActiveLayerEvent e) {
        if (!EventQueue.isDispatchThread()) {
            LOG.warn("status bar event invocation not in edt. This can lead to an error.", new Exception());
        }
        if (e.getLayer() instanceof ServiceLayer) {
            statusValueChanged(new StatusEvent(StatusEvent.RETRIEVAL_REMOVED, (ServiceLayer)e.getLayer()));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void layerPositionChanged(final ActiveLayerEvent e) {
        // NOP
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void layerVisibilityChanged(final ActiveLayerEvent e) {
        // NOP
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void layerAvailabilityChanged(final ActiveLayerEvent e) {
        if (e.getLayer() instanceof ServiceLayer) {
            final ServiceLayer layer = (ServiceLayer)e.getLayer();
            if (!layer.isEnabled()) {
                statusValueChanged(new StatusEvent(StatusEvent.RETRIEVAL_REMOVED, layer));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void layerInformationStatusChanged(final ActiveLayerEvent e) {
        // NOP
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void layerSelectionChanged(final ActiveLayerEvent e) {
        // NOP
    }

    @Override
    public void repaintStart(final RepaintEvent e) {
    }

    @Override
    public void repaintComplete(final RepaintEvent e) {
        if (!EventQueue.isDispatchThread()) {
            LOG.warn("status bar event invocation not in edt. This can lead to an error.", new Exception());
        }
        // the reapintComplete event should be used instead of the RETRIEVAL_COMPLETED event
        if (LOG.isDebugEnabled()) {
            LOG.debug("Entered repaint complete: " + e.getRetrievalEvent().getRetrievalService() + " ("
                        + System.currentTimeMillis() + ")");
        }

        if (e.getRetrievalEvent().getRetrievalService() instanceof ServiceLayer) {
            final ServiceLayer service = (ServiceLayer)e.getRetrievalEvent().getRetrievalService();
            removeService(service);
            erroneousServices.remove(service);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("RETRIEVAL_COMPLETED (" + e.getRetrievalEvent().getRetrievalService() + ", "
                        + System.currentTimeMillis()
                        + ") - services started: " + services.size() + ", erroneous services: "
                        + erroneousServices.size());
        }

        refreshControls(null);
    }

    @Override
    public void repaintError(final RepaintEvent e) {
        if (!EventQueue.isDispatchThread()) {
            LOG.warn("status bar event invocation not in edt. This can lead to an error.", new Exception());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Entered RETRIEVAL_ERROR: " + e.getRetrievalEvent().getRetrievalService() + " ("
                        + System.currentTimeMillis() + ")");
        }

//        if (e.getRetrievalEvent().isInitialisationEvent()) {
//            return;
//        }
        if (e.getRetrievalEvent().getRetrievalService() instanceof ServiceLayer) {
            final ServiceLayer service = (ServiceLayer)e.getRetrievalEvent().getRetrievalService();
            removeService(service);
            erroneousServices.add(service);
        }

        refreshControls(null);
    }
}
