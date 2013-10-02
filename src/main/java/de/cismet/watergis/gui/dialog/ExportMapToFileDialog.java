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
package de.cismet.watergis.gui.dialog;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import org.deegree.model.coverage.grid.WorldFile;

import org.openide.util.Exceptions;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.HeadlessMapProvider;
import de.cismet.cismap.commons.RestrictedFileSystemView;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;
import de.cismet.watergis.gui.actions.SaveProjectAction;
import de.cismet.watergis.gui.components.ValidationJTextField;

import de.cismet.watergis.utils.ImageDownload;
import de.cismet.watergis.utils.WorldFileDownload;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ExportMapToFileDialog extends javax.swing.JDialog implements ComponentListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ExportMapToFileDialog.class);

    //~ Instance fields --------------------------------------------------------

    private HeadlessMapProvider headlessMapProvider;

    private HeightChangedDocumentListener heightChangedDocumentListener = new HeightChangedDocumentListener();

    private WidthChangedDocumentListener widthChangedDocumentListener = new WidthChangedDocumentListener();

    private PixelDPICalculator pixelDPICalculator;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JCheckBox chbWorldFile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblName;
    private javax.swing.JSpinner spnDPI;
    private javax.swing.JTextField txtHeight;
    private javax.swing.JTextField txtWidth;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form CreateBookmark.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public ExportMapToFileDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();
        this.addComponentListener(this);
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

        jPanel1 = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        spnDPI = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtWidth = new ValidationJTextField();
        txtHeight = new ValidationJTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        chbWorldFile = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(ExportMapToFileDialog.class, "ExportMapToFileDialog.title")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            lblName,
            org.openide.util.NbBundle.getMessage(ExportMapToFileDialog.class, "ExportMapToFileDialog.lblName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 2);
        jPanel1.add(lblName, gridBagConstraints);

        spnDPI.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(72), null, null, Integer.valueOf(1)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 6, 0);
        jPanel1.add(spnDPI, gridBagConstraints);
        final JFormattedTextField tf = ((JSpinner.DefaultEditor)spnDPI.getEditor()).getTextField();
        tf.setHorizontalAlignment(JFormattedTextField.LEFT);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(ExportMapToFileDialog.class, "ExportMapToFileDialog.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(ExportMapToFileDialog.class, "ExportMapToFileDialog.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 2);
        jPanel1.add(jLabel2, gridBagConstraints);

        txtWidth.setText(org.openide.util.NbBundle.getMessage(
                ExportMapToFileDialog.class,
                "ExportMapToFileDialog.txtWidth.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 4, 0);
        jPanel1.add(txtWidth, gridBagConstraints);
        ((ValidationJTextField)txtWidth).setPattern("\\d{1,9}");

        txtHeight.setText(org.openide.util.NbBundle.getMessage(
                ExportMapToFileDialog.class,
                "ExportMapToFileDialog.txtHeight.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 4, 0);
        jPanel1.add(txtHeight, gridBagConstraints);
        ((ValidationJTextField)txtHeight).setPattern("\\d{1,9}");

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(ExportMapToFileDialog.class, "ExportMapToFileDialog.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 2);
        jPanel1.add(jLabel3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel4,
            org.openide.util.NbBundle.getMessage(ExportMapToFileDialog.class, "ExportMapToFileDialog.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(jLabel4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel5,
            org.openide.util.NbBundle.getMessage(ExportMapToFileDialog.class, "ExportMapToFileDialog.jLabel5.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(jLabel5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            chbWorldFile,
            org.openide.util.NbBundle.getMessage(
                ExportMapToFileDialog.class,
                "ExportMapToFileDialog.chbWorldFile.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel1.add(chbWorldFile, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        btnSave.setMnemonic('S');
        org.openide.awt.Mnemonics.setLocalizedText(
            btnSave,
            org.openide.util.NbBundle.getMessage(ExportMapToFileDialog.class, "ExportMapToFileDialog.btnSave.text")); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnSaveActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 4, 0, 0);
        jPanel2.add(btnSave, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            btnCancel,
            org.openide.util.NbBundle.getMessage(ExportMapToFileDialog.class, "ExportMapToFileDialog.btnCancel.text")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 3);
        jPanel2.add(btnCancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanel1.add(jPanel2, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }                                                                             //GEN-LAST:event_btnCancelActionPerformed
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnSaveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed
        // final Image image = AppBroker.getInstance().getMappingComponent().getImage();
        final int resolution = (Integer)spnDPI.getValue();
        final int width = Integer.parseInt(txtWidth.getText());
        final int height = Integer.parseInt(txtHeight.getText());
        headlessMapProvider = createHeadlessMapProvider();

        final Future<Image> futureImage = headlessMapProvider.getImage(72, resolution, width, height);
        final File file = chooseFile();

        if (file != null) {
            final String imageFilePath = file.getAbsolutePath();
            final ImageDownload imageDownload = new ImageDownload(
                    FilenameUtils.getBaseName(imageFilePath),
                    FilenameUtils.getExtension(imageFilePath),
                    file,
                    futureImage);
            DownloadManager.instance().add(imageDownload);
            if (chbWorldFile.isSelected()) {
                final String worldFileName = FilenameUtils.getFullPath(imageFilePath)
                            + FilenameUtils.getBaseName(imageFilePath)
                            + ".jgw";
                final WorldFileDownload worldFileDownload = new WorldFileDownload(
                        futureImage,
                        headlessMapProvider.getCurrentBoundingBoxFromMap(),
                        worldFileName);
                DownloadManager.instance().add(worldFileDownload);
            }

            this.dispose();
        }
    } //GEN-LAST:event_btnSaveActionPerformed
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private HeadlessMapProvider createHeadlessMapProvider() {
        headlessMapProvider = new HeadlessMapProvider();
        final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
        headlessMapProvider.setDominatingDimension(HeadlessMapProvider.DominatingDimension.SIZE);
        headlessMapProvider.setBoundingBox((XBoundingBox)mappingComponent.getCurrentBoundingBoxFromCamera());

        // Raster Services
        final TreeMap rasterServices = mappingComponent.getMappingModel().getRasterServices();
        final List positionsRaster = new ArrayList(rasterServices.keySet());
        Collections.sort(positionsRaster);

        for (final Object position : positionsRaster) {
            final Object rasterService = rasterServices.get(position);
            if (rasterService instanceof RetrievalServiceLayer) {
                headlessMapProvider.addLayer((RetrievalServiceLayer)rasterService);
            } else {
                LOG.warn(
                    "Layer can not be added to the headlessMapProvider as it is not an instance of RetrievalServiceLayer");
            }
        }

        // Feature Services
        final TreeMap featureServices = mappingComponent.getMappingModel().getFeatureServices();
        final List positionsFeatures = new ArrayList(featureServices.keySet());
        Collections.sort(positionsFeatures);

        for (final Object position : positionsFeatures) {
            final Object featureService = featureServices.get(position);
            if (featureService instanceof RetrievalServiceLayer) {
                headlessMapProvider.addLayer((RetrievalServiceLayer)featureService);
            } else {
                LOG.warn(
                    "Feature can not be added to the headlessMapProvider as it is not an instance of RetrievalServiceLayer");
            }
        }

        return headlessMapProvider;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private File chooseFile() {
        JFileChooser fc;
        try {
            fc = new JFileChooser(DownloadManager.instance().getDestinationDirectory());
        } catch (Exception bug) {
            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
            fc = new JFileChooser(DownloadManager.instance().getDestinationDirectory(), new RestrictedFileSystemView());
        }

        fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    return f.isDirectory()
                                || f.getName().toLowerCase().endsWith(".jpg")
                                || f.getName().toLowerCase().endsWith(".jpeg"); // NOI18N
                }

                @Override
                public String getDescription() {
                    return org.openide.util.NbBundle.getMessage(
                            SaveProjectAction.class,
                            "SaveProjectAction.save.FileFilter.getDescription.return"); // NOI18N
                }
            });

        final int state = fc.showSaveDialog(AppBroker.getInstance().getComponent(ComponentName.MAIN));
        if (LOG.isDebugEnabled()) {
            LOG.debug("state:" + state); // NOI18N
        }

        if (state == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            final String name = file.getAbsolutePath();

            if (!(name.endsWith(".jpg") || name.endsWith(".jpeg"))) { // NOI18N
                file = new File(file.getAbsolutePath() + ".jpg");
            }
            return file;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void addListener() {
        txtHeight.getDocument().addDocumentListener(heightChangedDocumentListener);
        txtWidth.getDocument().addDocumentListener(widthChangedDocumentListener);
    }

    /**
     * DOCUMENT ME!
     */
    private void removeListener() {
        txtHeight.getDocument().removeDocumentListener(heightChangedDocumentListener);
        txtWidth.getDocument().removeDocumentListener(widthChangedDocumentListener);
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void componentMoved(final ComponentEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void componentShown(final ComponentEvent e) {
        removeListener();

        final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
        final int width = mappingComponent.getWidth();
        final int height = mappingComponent.getHeight();
        final int dpi = 72;

        pixelDPICalculator = new PixelDPICalculator(width, height, dpi);

        txtHeight.setText(Integer.toString(height));
        txtWidth.setText(Integer.toString(width));
        spnDPI.setValue(dpi);

        addListener();
    }

    @Override
    public void componentHidden(final ComponentEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class PixelDPICalculator {

        //~ Instance fields ----------------------------------------------------

        private int widthPixel;
        private int heightPixel;
        private int dpi;
        private double aspectRatio; // width / height

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PixelDPICalculator object.
         *
         * @param  widthPixel   DOCUMENT ME!
         * @param  heightPixel  DOCUMENT ME!
         * @param  dpi          DOCUMENT ME!
         */
        public PixelDPICalculator(final int widthPixel, final int heightPixel, final int dpi) {
            this.widthPixel = widthPixel;
            this.heightPixel = heightPixel;
            this.dpi = dpi;

            this.aspectRatio = widthPixel * 1d / heightPixel;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getHeightPixel() {
            return heightPixel;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  newHeightPixel  DOCUMENT ME!
         */
        public void setHeightPixel(final int newHeightPixel) {
            this.widthPixel = (int)Math.round(newHeightPixel * aspectRatio);
            final double newDpi = (newHeightPixel * 1d * dpi) / this.heightPixel;

            this.dpi = (int)Math.round(newDpi);
            this.heightPixel = newHeightPixel;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getDPI() {
            return dpi;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dpi  DPI DOCUMENT ME!
         */
        public void setDPI(final int dpi) {
            this.dpi = dpi;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getWidthPixel() {
            return widthPixel;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  newWidthPixel  DOCUMENT ME!
         */
        public void setWidthPixel(final int newWidthPixel) {
            this.heightPixel = (int)Math.round(newWidthPixel * 1d / aspectRatio);
            final double newDpi = (newWidthPixel * 1d * dpi) / this.widthPixel;

            this.dpi = (int)Math.round(newDpi);
            this.widthPixel = newWidthPixel;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class HeightChangedDocumentListener implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void insertUpdate(final DocumentEvent e) {
            heightChanged();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            heightChanged();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            heightChanged();
        }

        /**
         * DOCUMENT ME!
         */
        private void heightChanged() {
            if (((ValidationJTextField)txtHeight).isContentValid()) {
                ExportMapToFileDialog.this.removeListener();

                final int newHeigth = Integer.parseInt(txtHeight.getText());
                pixelDPICalculator.setHeightPixel(newHeigth);
                txtWidth.setText(Integer.toString(pixelDPICalculator.getWidthPixel()));
                spnDPI.setValue(pixelDPICalculator.getDPI());

                ExportMapToFileDialog.this.addListener();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class WidthChangedDocumentListener implements DocumentListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void insertUpdate(final DocumentEvent e) {
            widthChanged();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            widthChanged();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            widthChanged();
        }

        /**
         * DOCUMENT ME!
         */
        private void widthChanged() {
            if (((ValidationJTextField)txtWidth).isContentValid()) {
                ExportMapToFileDialog.this.removeListener();

                final int newWidth = Integer.parseInt(txtWidth.getText());
                pixelDPICalculator.setWidthPixel(newWidth);
                txtHeight.setText(Integer.toString(pixelDPICalculator.getHeightPixel()));
                spnDPI.setValue(pixelDPICalculator.getDPI());

                ExportMapToFileDialog.this.addListener();
            }
        }
    }
}
