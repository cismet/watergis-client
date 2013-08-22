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

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class MapStatusBar extends javax.swing.JPanel {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pnlCoordinateSystem;
    private javax.swing.JPanel pnlDecimalDegree;
    private javax.swing.JPanel pnlScale;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form MapStatusBar.
     */
    public MapStatusBar() {
        initComponents();
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

        pnlDecimalDegree = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        pnlCoordinateSystem = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        pnlScale = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(32769, 20));
        setMinimumSize(new java.awt.Dimension(200, 20));
        setPreferredSize(new java.awt.Dimension(500, 20));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(MapStatusBar.class, "MapStatusBar.jLabel1.text")); // NOI18N

        final javax.swing.GroupLayout pnlDecimalDegreeLayout = new javax.swing.GroupLayout(pnlDecimalDegree);
        pnlDecimalDegree.setLayout(pnlDecimalDegreeLayout);
        pnlDecimalDegreeLayout.setHorizontalGroup(
            pnlDecimalDegreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                90,
                Short.MAX_VALUE).addGroup(
                pnlDecimalDegreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    pnlDecimalDegreeLayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(
                        jLabel1).addGap(0, 0, Short.MAX_VALUE))));
        pnlDecimalDegreeLayout.setVerticalGroup(
            pnlDecimalDegreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                15,
                Short.MAX_VALUE).addGroup(
                pnlDecimalDegreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    pnlDecimalDegreeLayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(
                        jLabel1).addGap(0, 0, Short.MAX_VALUE))));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(pnlDecimalDegree, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(MapStatusBar.class, "MapStatusBar.jLabel2.text")); // NOI18N

        final javax.swing.GroupLayout pnlCoordinateSystemLayout = new javax.swing.GroupLayout(pnlCoordinateSystem);
        pnlCoordinateSystem.setLayout(pnlCoordinateSystemLayout);
        pnlCoordinateSystemLayout.setHorizontalGroup(
            pnlCoordinateSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                140,
                Short.MAX_VALUE).addGroup(
                pnlCoordinateSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    pnlCoordinateSystemLayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(
                        jLabel2).addGap(0, 0, Short.MAX_VALUE))));
        pnlCoordinateSystemLayout.setVerticalGroup(
            pnlCoordinateSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                15,
                Short.MAX_VALUE).addGroup(
                pnlCoordinateSystemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    pnlCoordinateSystemLayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(
                        jLabel2).addGap(0, 0, Short.MAX_VALUE))));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(pnlCoordinateSystem, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(MapStatusBar.class, "MapStatusBar.jLabel3.text")); // NOI18N

        final javax.swing.GroupLayout pnlScaleLayout = new javax.swing.GroupLayout(pnlScale);
        pnlScale.setLayout(pnlScaleLayout);
        pnlScaleLayout.setHorizontalGroup(
            pnlScaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                62,
                Short.MAX_VALUE).addGroup(
                pnlScaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    pnlScaleLayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(jLabel3).addGap(
                        0,
                        0,
                        Short.MAX_VALUE))));
        pnlScaleLayout.setVerticalGroup(
            pnlScaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                15,
                Short.MAX_VALUE).addGroup(
                pnlScaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    pnlScaleLayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(jLabel3).addGap(
                        0,
                        0,
                        Short.MAX_VALUE))));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        add(pnlScale, gridBagConstraints);

        final javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 9, Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                15,
                Short.MAX_VALUE));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents
}
