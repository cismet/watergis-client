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

import javax.swing.JTextField;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WsgForstDialog extends javax.swing.JDialog {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butOk;
    private javax.swing.JLabel labAnteil;
    private javax.swing.JLabel labFl;
    private javax.swing.JLabel labFlNuWsg;
    private javax.swing.JLabel labFoot;
    private javax.swing.JLabel labGes;
    private javax.swing.JTextField labGesWsgFl;
    private javax.swing.JLabel labHolz;
    private javax.swing.JTextField labHolzFl;
    private javax.swing.JTextField labHolzPr;
    private javax.swing.JLabel labLand;
    private javax.swing.JTextField labLandFl;
    private javax.swing.JTextField labLandPr;
    private javax.swing.JLabel labNichtEingerichtet;
    private javax.swing.JLabel labNichtHolz;
    private javax.swing.JTextField labNichtHolzFl;
    private javax.swing.JTextField labNichtHolzFl1;
    private javax.swing.JTextField labNichtHolzPr;
    private javax.swing.JTextField labNichtHolzPr1;
    private javax.swing.JLabel labWsgAnz;
    private javax.swing.JTextField labWsgAnzVal;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form WsgDialog.
     *
     * @param  parent             DOCUMENT ME!
     * @param  modal              DOCUMENT ME!
     * @param  wsgCount           DOCUMENT ME!
     * @param  wsgTotal           DOCUMENT ME!
     * @param  holzTotal          ackerTotal DOCUMENT ME!
     * @param  nichtHolzTotal     DOCUMENT ME!
     * @param  nichtEingerichtet  DOCUMENT ME!
     */
    public WsgForstDialog(final java.awt.Frame parent,
            final boolean modal,
            final int wsgCount,
            final double wsgTotal,
            final double holzTotal,
            final double nichtHolzTotal,
            final double nichtEingerichtet) {
        super(parent, modal);
        initComponents();

        makeReadOnly(labWsgAnzVal);
        makeReadOnly(labGesWsgFl);
        makeReadOnly(labLandFl);
        makeReadOnly(labHolzFl);
        makeReadOnly(labNichtHolzFl);
        makeReadOnly(labLandPr);
        makeReadOnly(labHolzPr);
        makeReadOnly(labNichtHolzPr);
        makeReadOnly(labNichtHolzFl1);
        makeReadOnly(labNichtHolzPr1);

        final DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        formatter.setGroupingSize(3);
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        labWsgAnzVal.setText(String.valueOf(wsgCount));
        labGesWsgFl.setText(formatter.format(wsgTotal / 10000));
        labLandFl.setText(formatter.format((holzTotal + nichtHolzTotal + nichtEingerichtet) / 10000));
        labHolzFl.setText(formatter.format(holzTotal / 10000));
        labNichtHolzFl.setText(formatter.format(nichtHolzTotal / 10000));
        labLandPr.setText(formatter.format((holzTotal + nichtHolzTotal + nichtEingerichtet) / wsgTotal * 100));
        labHolzPr.setText(formatter.format(holzTotal / (holzTotal + nichtHolzTotal + nichtEingerichtet) * 100));
        labNichtHolzPr.setText(formatter.format(
                nichtHolzTotal
                        / (holzTotal + nichtHolzTotal + nichtEingerichtet)
                        * 100));
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
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        return df.format(new Date());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tf  DOCUMENT ME!
     */
    public static void makeReadOnly(final JTextField tf) {
        tf.setBorder(null);
        tf.setOpaque(false);
        tf.setEditable(false);
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
        labFl = new javax.swing.JLabel();
        labAnteil = new javax.swing.JLabel();
        labGes = new javax.swing.JLabel();
        labLand = new javax.swing.JLabel();
        labHolz = new javax.swing.JLabel();
        labNichtHolz = new javax.swing.JLabel();
        labFoot = new javax.swing.JLabel();
        butOk = new javax.swing.JButton();
        labWsgAnzVal = new javax.swing.JTextField();
        labGesWsgFl = new javax.swing.JTextField();
        labLandFl = new javax.swing.JTextField();
        labLandPr = new javax.swing.JTextField();
        labHolzFl = new javax.swing.JTextField();
        labNichtHolzFl = new javax.swing.JTextField();
        labHolzPr = new javax.swing.JTextField();
        labNichtHolzPr = new javax.swing.JTextField();
        labNichtHolzFl1 = new javax.swing.JTextField();
        labNichtHolzPr1 = new javax.swing.JTextField();
        labNichtEingerichtet = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(WsgForstDialog.class, "WsgForstDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labFlNuWsg,
            org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labFlNuWsg.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        getContentPane().add(labFlNuWsg, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labWsgAnz,
            org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labWsgAnz.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        getContentPane().add(labWsgAnz, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labFl,
            org.openide.util.NbBundle.getMessage(WsgForstDialog.class, "WsgForstDialog.labFl.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labFl, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labAnteil,
            org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labAnteil.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labAnteil, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labGes,
            org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labGes.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labGes, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labLand,
            org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labLand.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 10);
        getContentPane().add(labLand, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labHolz,
            org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labHolz.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 5, 5);
        getContentPane().add(labHolz, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labNichtHolz,
            org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labNichtHolz.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 5, 5);
        getContentPane().add(labNichtHolz, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labFoot,
            org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labFoot.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 5, 5, 5);
        getContentPane().add(labFoot, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(WsgForstDialog.class, "WsgForstDialog.butOk.text", new Object[] {})); // NOI18N
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

        labWsgAnzVal.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labWsgAnzVal.text",
                new Object[] {})); // NOI18N
        labWsgAnzVal.setMaximumSize(new java.awt.Dimension(100, 17));
        labWsgAnzVal.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        getContentPane().add(labWsgAnzVal, gridBagConstraints);

        labGesWsgFl.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labGesWsgFl.text",
                new Object[] {})); // NOI18N
        labGesWsgFl.setMaximumSize(new java.awt.Dimension(100, 17));
        labGesWsgFl.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labGesWsgFl, gridBagConstraints);

        labLandFl.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labLandFl.text",
                new Object[] {})); // NOI18N
        labLandFl.setMaximumSize(new java.awt.Dimension(100, 17));
        labLandFl.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 10);
        getContentPane().add(labLandFl, gridBagConstraints);

        labLandPr.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labLandPr.text",
                new Object[] {})); // NOI18N
        labLandPr.setMaximumSize(new java.awt.Dimension(100, 17));
        labLandPr.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        getContentPane().add(labLandPr, gridBagConstraints);

        labHolzFl.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labHolzFl.text",
                new Object[] {})); // NOI18N
        labHolzFl.setMaximumSize(new java.awt.Dimension(100, 17));
        labHolzFl.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labHolzFl, gridBagConstraints);

        labNichtHolzFl.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labNichtHolzFl.text",
                new Object[] {})); // NOI18N
        labNichtHolzFl.setMaximumSize(new java.awt.Dimension(100, 17));
        labNichtHolzFl.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labNichtHolzFl, gridBagConstraints);

        labHolzPr.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labHolzPr.text",
                new Object[] {})); // NOI18N
        labHolzPr.setMaximumSize(new java.awt.Dimension(100, 17));
        labHolzPr.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labHolzPr, gridBagConstraints);

        labNichtHolzPr.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labNichtHolzPr.text",
                new Object[] {})); // NOI18N
        labNichtHolzPr.setMaximumSize(new java.awt.Dimension(100, 17));
        labNichtHolzPr.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labNichtHolzPr, gridBagConstraints);

        labNichtHolzFl1.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labNichtHolzFl1.text",
                new Object[] {})); // NOI18N
        labNichtHolzFl1.setMaximumSize(new java.awt.Dimension(100, 17));
        labNichtHolzFl1.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        getContentPane().add(labNichtHolzFl1, gridBagConstraints);

        labNichtHolzPr1.setText(org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labNichtHolzPr1.text",
                new Object[] {})); // NOI18N
        labNichtHolzPr1.setMaximumSize(new java.awt.Dimension(100, 17));
        labNichtHolzPr1.setPreferredSize(new java.awt.Dimension(75, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labNichtHolzPr1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labNichtEingerichtet,
            org.openide.util.NbBundle.getMessage(
                WsgForstDialog.class,
                "WsgForstDialog.labNichtEingerichtet.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 5, 5);
        getContentPane().add(labNichtEingerichtet, gridBagConstraints);

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
