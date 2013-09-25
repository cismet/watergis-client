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

import org.openide.util.Exceptions;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.HeadlessMapProvider;
import de.cismet.cismap.commons.RestrictedFileSystemView;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;
import de.cismet.watergis.gui.actions.SaveProjectAction;
import de.cismet.watergis.gui.components.ValidationJTextField;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ExportMapToFileDialog extends javax.swing.JDialog implements WindowListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ExportMapToFileDialog.class);

    //~ Instance fields --------------------------------------------------------

    private HeadlessMapProvider headlessMapProvider;

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
        this.addWindowListener(this);
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
        ((ValidationJTextField)txtWidth).setPattern("\\d+");

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
        ((ValidationJTextField)txtHeight).setPattern("\\d+");

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
    private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnSaveActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // final Image image = AppBroker.getInstance().getMappingComponent().getImage();
        final int resolution = (Integer)spnDPI.getValue();
        final int width = Integer.parseInt(txtWidth.getText());
        final int height = Integer.parseInt(txtHeight.getText());
        final HeadlessMapProvider headlessMapProvider = createHeadlessMapProvider();

        final Future<Image> futureImage = headlessMapProvider.getImage(72, resolution, width, height);
        final File file = chooseFile();

        if (file != null) {
            final Image image;
            try {
                //TODO Dauert unter Umständen lange. Siehe clipboarder
                image = futureImage.get();
                if (save(file, toBufferedImage(image))) {
                    this.dispose();
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_btnSaveActionPerformed
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

        //TODO was ist die richtige Reihenfolge? TreeMap der Key ist eine Zahl der die Reihenfolge angibt
        //TreeMap.getKeys() darauf sort und dann Treemap.get(key) 
        final Object[] rasterServices = mappingComponent.getMappingModel().getRasterServices().values().toArray();
        for (int i = rasterServices.length - 1; i >= 0; i--) {
            headlessMapProvider.addLayer((RetrievalServiceLayer)rasterServices[i]);
        }

//        for (final Object layer : mappingComponent.getMappingModel().getFeatureServices().values()) {
//            headlessMapProvider.addLayer((RetrievalServiceLayer)layer);
//        }

//        for (final Object layer : mappingComponent.getMappingModel().getRasterServices().values()) {
//            headlessMapProvider.addLayer((RetrievalServiceLayer)layer);
//        }

        return headlessMapProvider;
    }

    /**
     * Returns true, if saving was successful. Saves the buffered image and if wished by the user also initializes the
     * writing of the world file
     *
     * @param   file   DOCUMENT ME!
     * @param   image  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean save(final File file, final BufferedImage image) {
        try {
            ImageIO.write(image, "jpg", file);
            if (chbWorldFile.isSelected()) {
                writeWorldFile(image, file.getAbsolutePath());
            }
            return true;
        } catch (IOException e) {
            LOG.error("Write error for " + file.getPath(), e);
            showErrorWhileSavingDialog();
            return false;
        }
    }

    /**
     * Has also to be used if <code>src</code> is already an instance of BufferedImage, as it might be that the type is
     * not correct.
     *
     * @param   src  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage toBufferedImage(final Image src) {
        //TODO Bilder werden weiß, wie besser machen?
        
        final int w = src.getWidth(null);
        final int h = src.getHeight(null);
        final BufferedImage image2 = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = image2.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return image2;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private File chooseFile() {
        JFileChooser fc;

        try {
            fc = new JFileChooser(WatergisApp.getDIRECTORYPATH_WATERGIS());
        } catch (Exception bug) {
            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
            fc = new JFileChooser(WatergisApp.getDIRECTORYPATH_WATERGIS(), new RestrictedFileSystemView());
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

    @Override
    public void windowOpened(final WindowEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowClosing(final WindowEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowClosed(final WindowEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowIconified(final WindowEvent e) {
        // new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowDeiconified(final WindowEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowActivated(final WindowEvent e) {
        final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
        txtHeight.setText(Integer.toString(mappingComponent.getHeight()));
        txtWidth.setText(Integer.toString(mappingComponent.getWidth()));
        spnDPI.setValue(72);
    }

    @Override
    public void windowDeactivated(final WindowEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * DOCUMENT ME!
     */
    private void showErrorWhileSavingDialog() {
        final String title = org.openide.util.NbBundle.getMessage(
                ExportMapToFileDialog.class,
                "ExportMapToFileDialog.showErrorWhileSavingDialog().title");
        final String message = org.openide.util.NbBundle.getMessage(
                ExportMapToFileDialog.class,
                "ExportMapToFileDialog.showErrorWhileSavingDialog().message");
        JOptionPane.showMessageDialog(this,
            message,
            title,
            JOptionPane.ERROR_MESSAGE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  image          DOCUMENT ME!
     * @param  imageFilePath  DOCUMENT ME!
     */
    private void writeWorldFile(final Image image, final String imageFilePath) {
        final String worldFileContent = getWorldFileContent(image);
        final String worldFileName = FilenameUtils.getFullPath(imageFilePath) + FilenameUtils.getBaseName(imageFilePath)
                    + ".jgw";
        try {
            final PrintWriter out = new PrintWriter(worldFileName);
            out.println(worldFileContent);
            out.close();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   image  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getWorldFileContent(final Image image) {
        final BoundingBox boundingBox = headlessMapProvider.getCurrentBoundingBoxFromMap();
        final DecimalFormat df = new DecimalFormat("#.");
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        df.setMaximumFractionDigits(32);
        final double widthPixel = image.getWidth(null);
        final double heightPixel = image.getWidth(null);

        // pixel size in the x-direction in map units/pixel
        final double xPixelSize = (boundingBox.getX2() - boundingBox.getX1()) / widthPixel;

        // rotation about y-axis
        final int yRotation = 0;
        // rotation about x-axis
        final int xRotation = 0;

        // pixel size in the y-direction in map units, almost always negative
        final double yPixelSize = (boundingBox.getY1() - boundingBox.getY2()) / heightPixel;

        // x-coordinate of the center of the upper left pixel
        final double xPixelCenter = boundingBox.getX1() + (xPixelSize / 2);

        // y-coordinate of the center of the upper left pixel
        final double yPixelCenter = boundingBox.getY1() + (yPixelSize / 2);

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(df.format(xPixelSize));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(yRotation);
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(xRotation);
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(df.format(yPixelSize));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(df.format(xPixelCenter));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(df.format(yPixelCenter));

        return stringBuilder.toString();
    }
}
