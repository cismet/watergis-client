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
package de.cismet.watergis.gui;

import org.apache.log4j.Logger;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.lang.ref.SoftReference;

import java.text.SimpleDateFormat;

import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.Proxy;

import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.PasswordEncrypter;

import de.cismet.watergis.gui.dialog.PhotoOptionsDialog;
import de.cismet.watergis.gui.panels.PhotoEditor;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PhotoInfoPanel extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(PhotoInfoPanel.class);
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
                WEB_DAV_PASSWORD);
    }

    //~ Instance fields --------------------------------------------------------

    private CidsLayerFeature feature;
    private BufferedImage image;
    private Timer timer;
    private ImageResizeWorker currentResizeWorker;
    private Dimension lastDims;
    private PhotoInfoPHandle handle;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labImage;
    private javax.swing.JLabel labTime;
    private javax.swing.JLabel labTitle;
    private org.jdesktop.swingx.JXBusyLabel lblBusy;
    private javax.swing.JPanel panData;
    private javax.swing.JPanel panImage;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form FotoInfoPanel.
     *
     * @param  handle  DOCUMENT ME!
     */
    public PhotoInfoPanel(final PhotoInfoPHandle handle) {
        this.handle = handle;
        initComponents();
        panImage.setSize(PhotoOptionsDialog.getInstance().getPhotoSize());

        timer = new javax.swing.Timer(300, new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (currentResizeWorker != null) {
                            currentResizeWorker.cancel(true);
                        }
                        currentResizeWorker = new ImageResizeWorker();
                        CismetThreadPool.execute(currentResizeWorker);
                    }
                });
        timer.setRepeats(false);
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

        panImage = new javax.swing.JPanel();
        labImage = new javax.swing.JLabel();
        lblBusy = new org.jdesktop.swingx.JXBusyLabel(new Dimension(75, 75));
        panData = new javax.swing.JPanel();
        labTitle = new de.cismet.watergis.utils.MultiLineJLabel();
        labTime = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        panImage.setMinimumSize(new java.awt.Dimension(300, 300));
        panImage.setPreferredSize(new java.awt.Dimension(300, 300));
        panImage.setLayout(new java.awt.CardLayout());

        labImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panImage.add(labImage, "image");

        lblBusy.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBusy.setMaximumSize(new java.awt.Dimension(140, 40));
        lblBusy.setMinimumSize(new java.awt.Dimension(140, 40));
        lblBusy.setPreferredSize(new java.awt.Dimension(140, 40));
        panImage.add(lblBusy, "busy");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(panImage, gridBagConstraints);

        panData.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labTitle,
            org.openide.util.NbBundle.getMessage(
                PhotoInfoPanel.class,
                "PhotoInfoPanel.labTitle.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panData.add(labTitle, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labTime,
            org.openide.util.NbBundle.getMessage(
                PhotoInfoPanel.class,
                "PhotoInfoPanel.labTime.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panData.add(labTime, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(panData, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @return  the feature
     */
    public CidsLayerFeature getFeature() {
        return feature;
    }

    /**
     * DOCUMENT ME!
     */
    public void setPhotoSize() {
        this.setSize(PhotoOptionsDialog.getInstance().getPhotoSize());
        setFeature(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  the feature to set
     */
    public void setFeature(final CidsLayerFeature feature) {
        if ((feature != null) && (this.feature != feature)) {
            this.feature = feature;
            labTitle.setText(obj2String(feature.getProperty("titel")));
            labTime.setText(obj2Time(feature.getProperty("aufn_zeit")));
            loadFoto();
        } else {
            this.feature = feature;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String obj2String(final Object o) {
        if (o == null) {
            return "";
        } else {
            return String.valueOf(o);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String obj2Time(final Object o) {
        if (o == null) {
            return "";
        } else {
            try {
                final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                return format.format(o);
            } catch (IllegalArgumentException e) {
                LOG.error("Not a date", e);
                return "";
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void loadFoto() {
        final String path = (String)feature.getBean().getProperty("dateipfad");
        final String filename = (String)feature.getProperty("foto");
        boolean cacheHit = false;

        if ((path != null) && (filename != null)) {
            final String file = path + filename;
            final SoftReference<BufferedImage> cachedImageRef = PhotoEditor.IMAGE_CACHE.get(file);
            showWait(true);

            if (cachedImageRef != null) {
                final BufferedImage cachedImage = cachedImageRef.get();
                if (cachedImage != null) {
                    cacheHit = true;
                    image = cachedImage;
                    timer.restart();
                }
            }

            if (!cacheHit) {
                CismetThreadPool.execute(new LoadImageWorker(path, filename));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  error  DOCUMENT ME!
     */
    private void indicateError(final String error) {
        labImage.setToolTipText(error);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wait  DOCUMENT ME!
     */
    private void showWait(final boolean wait) {
        if (wait) {
            if (!lblBusy.isBusy()) {
                final CardLayout cardLayout = (CardLayout)panImage.getLayout();
                cardLayout.show(panImage, "busy");
                labImage.setIcon(null);
                lblBusy.setBusy(true);
            }
        } else {
            final CardLayout cardLayout = (CardLayout)panImage.getLayout();
            cardLayout.show(panImage, "image");
            lblBusy.setBusy(false);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class LoadImageWorker extends SwingWorker<BufferedImage, Void> {

        //~ Instance fields ----------------------------------------------------

        private final String path;
        private final String file;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadSelectedImageWorker object.
         *
         * @param  path  toLoad DOCUMENT ME!
         * @param  file  DOCUMENT ME!
         */
        public LoadImageWorker(final String path, final String file) {
            this.path = path;
            this.file = file;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected BufferedImage doInBackground() throws Exception {
            if ((file != null) && (file.length() > 0)) {
                final IIOReadProgressListener listener = new IIOReadProgressListener() {

                        @Override
                        public void sequenceStarted(final ImageReader source, final int minIndex) {
                        }

                        @Override
                        public void sequenceComplete(final ImageReader source) {
                        }

                        @Override
                        public void imageStarted(final ImageReader source, final int imageIndex) {
                        }

                        @Override
                        public void imageProgress(final ImageReader source, final float percentageDone) {
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        handle.repaint();
                                    }
                                });
                        }

                        @Override
                        public void imageComplete(final ImageReader source) {
                        }

                        @Override
                        public void thumbnailStarted(final ImageReader source,
                                final int imageIndex,
                                final int thumbnailIndex) {
                        }

                        @Override
                        public void thumbnailProgress(final ImageReader source, final float percentageDone) {
                        }

                        @Override
                        public void thumbnailComplete(final ImageReader source) {
                        }

                        @Override
                        public void readAborted(final ImageReader source) {
                        }
                    };

                return WebDavHelper.downloadImageFromWebDAV(
                        file,
                        WEB_DAV_DIRECTORY
                                + path,
                        webDavClient,
                        null,
                        listener);
            }
            return null;
        }

        @Override
        protected void done() {
            try {
                image = get();
                if (image != null) {
                    PhotoEditor.IMAGE_CACHE.put(path + file, new SoftReference<BufferedImage>(image));
                    timer.restart();
                } else {
                    indicateError("Bild konnte nicht geladen werden: Unbekanntes Bildformat");
                }
                handle.repaint();
            } catch (InterruptedException ex) {
                image = null;
                LOG.warn(ex, ex);
            } catch (ExecutionException ex) {
                image = null;
                LOG.error(ex, ex);
                String causeMessage = "";
                final Throwable cause = ex.getCause();
                if (cause != null) {
                    causeMessage = cause.getMessage();
                }
                indicateError(causeMessage);
            } finally {
                if (image == null) {
                    showWait(false);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class ImageResizeWorker extends SwingWorker<ImageIcon, Void> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ImageResizeWorker object.
         */
        public ImageResizeWorker() {
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected ImageIcon doInBackground() throws Exception {
            if (image != null) {
                final ImageIcon result = new ImageIcon(PhotoEditor.adjustScale(image, panImage, 20, 20));
                return result;
            } else {
                return null;
            }
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                try {
                    final ImageIcon result = get();
                    labImage.setIcon(result);
                    labImage.setText("");
                    labImage.setToolTipText(null);
                } catch (InterruptedException ex) {
                    LOG.warn(ex, ex);
                } catch (ExecutionException ex) {
                    LOG.error(ex, ex);
                    labImage.setText("Fehler beim Skalieren!");
                } finally {
                    showWait(false);
                    if (currentResizeWorker == this) {
                        currentResizeWorker = null;
                    }
                    handle.repaint();
                }
            }
        }
    }
}
