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

import java.math.RoundingMode;

import java.text.DecimalFormat;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WsgRechtDialog extends javax.swing.JDialog {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butOk;
    private javax.swing.JLabel labAnteil;
    private javax.swing.JLabel labAnz;
    private javax.swing.JLabel labBrd;
    private javax.swing.JLabel labBrdA;
    private javax.swing.JLabel labBrdAn;
    private javax.swing.JLabel labBrdFl;
    private javax.swing.JLabel labDdr;
    private javax.swing.JLabel labDdrA;
    private javax.swing.JLabel labDdrAn;
    private javax.swing.JLabel labDdrFl;
    private javax.swing.JLabel labFl;
    private javax.swing.JLabel labFlNuWsg;
    private javax.swing.JLabel labFoot;
    private javax.swing.JLabel labFoot1;
    private javax.swing.JLabel labWsg;
    private javax.swing.JLabel labWsgA;
    private javax.swing.JLabel labWsgAnzVal;
    private javax.swing.JLabel labWsgFl;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form WsgDialog.
     *
     * @param  parent    DOCUMENT ME!
     * @param  modal     DOCUMENT ME!
     * @param  wsgCount  DOCUMENT ME!
     * @param  wsgTotal  DOCUMENT ME!
     * @param  brdCount  DOCUMENT ME!
     * @param  ddrCount  DOCUMENT ME!
     * @param  brdTotal  DOCUMENT ME!
     * @param  ddrTotal  DOCUMENT ME!
     */
    public WsgRechtDialog(final java.awt.Frame parent,
            final boolean modal,
            final int wsgCount,
            final double wsgTotal,
            final int brdCount,
            final int ddrCount,
            final double brdTotal,
            final double ddrTotal) {
        super(parent, modal);
        initComponents();

        final DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        formatter.setGroupingSize(3);
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        labWsgA.setText(String.valueOf(wsgCount));
        labWsgFl.setText(formatter.format(wsgTotal / 10000) + "*");
        labDdrA.setText(String.valueOf(ddrCount));
        labDdrFl.setText(formatter.format(ddrTotal / 10000) + "**");
        labDdrAn.setText(formatter.format(ddrTotal / (ddrTotal + brdTotal) * 100));
        labBrdA.setText(String.valueOf(brdCount));
        labBrdFl.setText(formatter.format(brdTotal / 10000));
        labBrdAn.setText(formatter.format(brdTotal / (ddrTotal + brdTotal) * 100));
        setSize(660, 350);
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

        labFlNuWsg = new javax.swing.JLabel();
        labWsgAnzVal = new javax.swing.JLabel();
        labAnz = new javax.swing.JLabel();
        labFl = new javax.swing.JLabel();
        labAnteil = new javax.swing.JLabel();
        labWsg = new javax.swing.JLabel();
        labDdr = new javax.swing.JLabel();
        labBrd = new javax.swing.JLabel();
        labWsgFl = new javax.swing.JLabel();
        labWsgA = new javax.swing.JLabel();
        labDdrFl = new javax.swing.JLabel();
        labBrdFl = new javax.swing.JLabel();
        labDdrAn = new javax.swing.JLabel();
        labBrdAn = new javax.swing.JLabel();
        labFoot = new javax.swing.JLabel();
        labFoot1 = new javax.swing.JLabel();
        butOk = new javax.swing.JButton();
        labDdrA = new javax.swing.JLabel();
        labBrdA = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(WsgRechtDialog.class, "WsgRechtDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labFlNuWsg,
            org.openide.util.NbBundle.getMessage(
                WsgRechtDialog.class,
                "WsgRechtDialog.labFlNuWsg.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        getContentPane().add(labFlNuWsg, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labWsgAnzVal, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labAnz,
            org.openide.util.NbBundle.getMessage(
                WsgRechtDialog.class,
                "WsgRechtDialog.labAnz.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labAnz, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labFl,
            org.openide.util.NbBundle.getMessage(WsgRechtDialog.class, "WsgRechtDialog.labFl.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labFl, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labAnteil,
            org.openide.util.NbBundle.getMessage(
                WsgRechtDialog.class,
                "WsgRechtDialog.labAnteil.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labAnteil, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labWsg,
            org.openide.util.NbBundle.getMessage(
                WsgRechtDialog.class,
                "WsgRechtDialog.labWsg.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labWsg, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labDdr,
            org.openide.util.NbBundle.getMessage(
                WsgRechtDialog.class,
                "WsgRechtDialog.labDdr.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labDdr, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labBrd,
            org.openide.util.NbBundle.getMessage(
                WsgRechtDialog.class,
                "WsgRechtDialog.labBrd.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labBrd, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labWsgFl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labWsgA, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labDdrFl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labBrdFl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labDdrAn, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labBrdAn, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labFoot,
            org.openide.util.NbBundle.getMessage(
                WsgRechtDialog.class,
                "WsgRechtDialog.labFoot.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 5, 5, 5);
        getContentPane().add(labFoot, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labFoot1,
            org.openide.util.NbBundle.getMessage(
                WsgRechtDialog.class,
                "WsgRechtDialog.labFoot1.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labFoot1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(WsgRechtDialog.class, "WsgRechtDialog.butOk.text", new Object[] {})); // NOI18N
        butOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        getContentPane().add(butOk, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labDdrA, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labBrdA, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        setVisible(false);
    }                                                                         //GEN-LAST:event_butOkActionPerformed
}