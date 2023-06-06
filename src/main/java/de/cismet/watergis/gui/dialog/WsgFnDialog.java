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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WsgFnDialog extends javax.swing.JDialog {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butOk;
    private javax.swing.JLabel labAcker;
    private javax.swing.JLabel labAckerFl;
    private javax.swing.JLabel labAckerPr;
    private javax.swing.JLabel labAnteil;
    private javax.swing.JLabel labFl;
    private javax.swing.JLabel labFlNuWsg;
    private javax.swing.JLabel labFoot;
    private javax.swing.JLabel labGes;
    private javax.swing.JLabel labGesWsgFl;
    private javax.swing.JLabel labGruen;
    private javax.swing.JLabel labGruenFl;
    private javax.swing.JLabel labGruenPr;
    private javax.swing.JLabel labLand;
    private javax.swing.JLabel labLandFl;
    private javax.swing.JLabel labLandPr;
    private javax.swing.JLabel labWsgAnz;
    private javax.swing.JLabel labWsgAnzVal;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form WsgDialog.
     *
     * @param  parent      DOCUMENT ME!
     * @param  modal       DOCUMENT ME!
     * @param  wsgCount    DOCUMENT ME!
     * @param  wsgTotal    DOCUMENT ME!
     * @param  ackerTotal  DOCUMENT ME!
     * @param  grTotal     DOCUMENT ME!
     */
    public WsgFnDialog(final java.awt.Frame parent,
            final boolean modal,
            final int wsgCount,
            final double wsgTotal,
            final double ackerTotal,
            final double grTotal) {
        super(parent, modal);
        initComponents();

        final DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        formatter.setGroupingSize(3);
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        labWsgAnzVal.setText(String.valueOf(wsgCount));
        labGesWsgFl.setText(formatter.format(wsgTotal / 10000));
        labLandFl.setText(formatter.format((ackerTotal + grTotal) / 10000));
        labAckerFl.setText(formatter.format(ackerTotal / 10000));
        labGruenFl.setText(formatter.format(grTotal / 10000));
        labLandPr.setText(formatter.format(wsgTotal / (ackerTotal + grTotal) * 100));
        labAckerPr.setText(formatter.format(ackerTotal / (ackerTotal + grTotal) * 100));
        labGruenPr.setText(formatter.format(grTotal / (ackerTotal + grTotal) * 100));
        labFoot.setText(labFoot.getText() + getDate());
        setSize(550, 380);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getDate() {
        final DateFormat df = new SimpleDateFormat("dd.mm.yyyy");
        return df.format(new Date());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labFlNuWsg = new javax.swing.JLabel();
        labWsgAnz = new javax.swing.JLabel();
        labWsgAnzVal = new javax.swing.JLabel();
        labFl = new javax.swing.JLabel();
        labAnteil = new javax.swing.JLabel();
        labGes = new javax.swing.JLabel();
        labLand = new javax.swing.JLabel();
        labAcker = new javax.swing.JLabel();
        labGruen = new javax.swing.JLabel();
        labGesWsgFl = new javax.swing.JLabel();
        labLandFl = new javax.swing.JLabel();
        labAckerFl = new javax.swing.JLabel();
        labGruenFl = new javax.swing.JLabel();
        labLandPr = new javax.swing.JLabel();
        labAckerPr = new javax.swing.JLabel();
        labGruenPr = new javax.swing.JLabel();
        labFoot = new javax.swing.JLabel();
        butOk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labFlNuWsg,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.labFlNuWsg.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        getContentPane().add(labFlNuWsg, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labWsgAnz,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.labWsgAnz.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        getContentPane().add(labWsgAnz, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        getContentPane().add(labWsgAnzVal, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labFl,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.labFl.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labFl, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labAnteil,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.labAnteil.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labAnteil, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labGes,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.labGes.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labGes, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labLand,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.labLand.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 10);
        getContentPane().add(labLand, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labAcker,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.labAcker.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 5, 5);
        getContentPane().add(labAcker, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labGruen,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.labGruen.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 5, 5);
        getContentPane().add(labGruen, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labGesWsgFl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 10);
        getContentPane().add(labLandFl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labAckerFl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labGruenFl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        getContentPane().add(labLandPr, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labAckerPr, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labGruenPr, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labFoot,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.labFoot.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 5, 5, 5);
        getContentPane().add(labFoot, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(WsgFnDialog.class, "WsgFnDialog.butOk.text", new Object[] {})); // NOI18N
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
