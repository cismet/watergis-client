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
package de.cismet.watergis.gui.dialog;

import java.io.File;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GafCheckDialog extends javax.swing.JDialog {

    //~ Instance fields --------------------------------------------------------

    private boolean cancelled = false;
    private String lastPath = WatergisApp.getDIRECTORYPATH_WATERGIS();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butBkFile;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butGafFile;
    private javax.swing.JButton butOk;
    private javax.swing.JButton butRkFile;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JTextField txtBkFile;
    private javax.swing.JTextField txtGafFile;
    private javax.swing.JTextField txtRkFile;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private GafCheckDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();
        setSize(195, 190);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setVisible(final boolean b) {
        if (b) {
            cancelled = true;
        }
        super.setVisible(b);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static GafCheckDialog getInstance() {
        return LazyInitializer.INSTANCE;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        txtGafFile = new javax.swing.JTextField();
        butGafFile = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 32767));
        jPanel4 = new javax.swing.JPanel();
        txtRkFile = new javax.swing.JTextField();
        butRkFile = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        txtBkFile = new javax.swing.JTextField();
        butBkFile = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(GafCheckDialog.class, "GafCheckDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(GafCheckDialog.class, "GafCheckDialog.butOk.text_1", new Object[] {
                })); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(170, 29));
        butOk.setPreferredSize(new java.awt.Dimension(170, 29));
        butOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butCancel,
            org.openide.util.NbBundle.getMessage(
                GafCheckDialog.class,
                "GafCheckDialog.butCancel.text_1",
                new Object[] {})); // NOI18N
        butCancel.setMinimumSize(new java.awt.Dimension(170, 29));
        butCancel.setPreferredSize(new java.awt.Dimension(170, 29));
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butCancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        txtGafFile.setText(org.openide.util.NbBundle.getMessage(
                GafCheckDialog.class,
                "GafCheckDialog.txtGafFile.text_1",
                new Object[] {})); // NOI18N
        txtGafFile.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel3.add(txtGafFile, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butGafFile,
            org.openide.util.NbBundle.getMessage(
                GafCheckDialog.class,
                "GafCheckDialog.butGafFile.text_1",
                new Object[] {})); // NOI18N
        butGafFile.setPreferredSize(new java.awt.Dimension(145, 29));
        butGafFile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butGafFileActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel3.add(butGafFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(filler1, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        txtRkFile.setText(org.openide.util.NbBundle.getMessage(
                GafCheckDialog.class,
                "GafCheckDialog.txtRkFile.text",
                new Object[] {})); // NOI18N
        txtRkFile.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(txtRkFile, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butRkFile,
            org.openide.util.NbBundle.getMessage(
                GafCheckDialog.class,
                "GafCheckDialog.butRkFile.text",
                new Object[] {})); // NOI18N
        butRkFile.setPreferredSize(new java.awt.Dimension(145, 29));
        butRkFile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butRkFileActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(butRkFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel4, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        txtBkFile.setText(org.openide.util.NbBundle.getMessage(
                GafCheckDialog.class,
                "GafCheckDialog.txtBkFile.text",
                new Object[] {})); // NOI18N
        txtBkFile.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel5.add(txtBkFile, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butBkFile,
            org.openide.util.NbBundle.getMessage(
                GafCheckDialog.class,
                "GafCheckDialog.butBkFile.text",
                new Object[] {})); // NOI18N
        butBkFile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butBkFileActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel5.add(butBkFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel5, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        cancelled = true;
        setVisible(false);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        if (!txtGafFile.getText().equals("")) {
            cancelled = false;
            setVisible(false);
        } else {
            butGafFileActionPerformed(null);
        }
    }                                                                         //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butGafFileActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butGafFileActionPerformed
        final File file = StaticSwingTools.chooseFile(
                lastPath,
                false,
                new String[] { "gaf" },
                org.openide.util.NbBundle.getMessage(
                    GafCheckDialog.class,
                    "GafCheckReportDialog.butFileActionPerformed().getDescription()"),
                AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (file != null) {
            lastPath = file.getParent();
            txtGafFile.setText(file.getAbsolutePath());
        }
    } //GEN-LAST:event_butGafFileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butRkFileActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butRkFileActionPerformed
        final File file = StaticSwingTools.chooseFile(
                lastPath,
                false,
                null,
                org.openide.util.NbBundle.getMessage(
                    GafCheckDialog.class,
                    "GafCheckReportDialog.butFileActionPerformed().getDescription()"),
                AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (file != null) {
            lastPath = file.getParent();
            txtRkFile.setText(file.getAbsolutePath());
        } else {
            txtRkFile.setText("");
        }
    } //GEN-LAST:event_butRkFileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butBkFileActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butBkFileActionPerformed
        final File file = StaticSwingTools.chooseFile(
                lastPath,
                false,
                null,
                org.openide.util.NbBundle.getMessage(
                    GafCheckDialog.class,
                    "GafCheckReportDialog.butFileActionPerformed().getDescription()"),
                AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (file != null) {
            lastPath = file.getParent();
            txtBkFile.setText(file.getAbsolutePath());
        } else {
            txtRkFile.setText("");
        }
    } //GEN-LAST:event_butBkFileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGafFile() {
        return txtGafFile.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getBkFile() {
        if (!txtBkFile.getText().equals("")) {
            final File catFile = new File(txtBkFile.getText());

            if (catFile.exists()) {
                return txtBkFile.getText();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getRkFile() {
        if (!txtRkFile.getText().equals("")) {
            final File catFile = new File(txtRkFile.getText());

            if (catFile.exists()) {
                return txtRkFile.getText();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient GafCheckDialog INSTANCE = new GafCheckDialog(AppBroker.getInstance()
                        .getWatergisApp(),
                true);

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitializer object.
         */
        private LazyInitializer() {
        }
    }
}
