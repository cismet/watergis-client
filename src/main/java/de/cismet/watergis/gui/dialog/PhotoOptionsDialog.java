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

import com.vividsolutions.jts.geom.Geometry;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Comparator;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.PhotoInfoListener;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PhotoOptionsDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Dimension[] dimensions = new Dimension[4];

    static {
        dimensions[0] = new Dimension(150, 200);
        dimensions[1] = new Dimension(200, 250);
        dimensions[2] = new Dimension(300, 350);
        dimensions[3] = new Dimension(400, 450);
    }

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Order {

        //~ Enum constants -----------------------------------------------------

        XY, RIVER, LAWA
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Size {

        //~ Enum constants -----------------------------------------------------

        SMALL, MEDIUM, LARGE, EXTRA_LARGE
    }

    //~ Instance fields --------------------------------------------------------

    private Order order = Order.XY;
    private Size size = Size.SMALL;
    private boolean automatic = true;
    private double distance = 10;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgOrder;
    private javax.swing.ButtonGroup bgSize;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JCheckBox cbAutomatic;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel labM;
    private javax.swing.JLabel labOrder;
    private javax.swing.JLabel labRiver;
    private javax.swing.JLabel labSize;
    private javax.swing.JRadioButton rbExtraLarge;
    private javax.swing.JRadioButton rbGewaesser;
    private javax.swing.JRadioButton rbLarge;
    private javax.swing.JRadioButton rbLawaCode;
    private javax.swing.JRadioButton rbMedium;
    private javax.swing.JRadioButton rbSmall;
    private javax.swing.JRadioButton rbXy;
    private javax.swing.JTextField txtDistance;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private PhotoOptionsDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();
        addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(final WindowEvent e) {
                    butCancelActionPerformed(null);
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the order
     */
    public Order getOrder() {
        return order;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Comparator<FeatureServiceFeature> getSorter() {
        if (order == Order.XY) {
            return new Comparator<FeatureServiceFeature>() {

                    @Override
                    public int compare(final FeatureServiceFeature o1, final FeatureServiceFeature o2) {
                        if ((o1 == null) && (o2 != null)) {
                            return 1;
                        } else if ((o2 == null) && (o1 != null)) {
                            return -1;
                        } else if ((o2 == null) && (o1 == null)) {
                            return 0;
                        } else {
                            final Geometry g1 = o1.getGeometry();
                            final Geometry g2 = o2.getGeometry();

                            if ((g1 == null) && (g2 != null)) {
                                return 1;
                            } else if ((g2 == null) && (g1 != null)) {
                                return -1;
                            } else if ((g2 == null) && (g1 == null)) {
                                return 0;
                            } else {
                                if (g1.getCoordinate().x < g2.getCoordinate().x) {
                                    return -1;
                                } else if (g1.getCoordinate().x == g2.getCoordinate().x) {
                                    return (int)Math.signum(g1.getCoordinate().y - g2.getCoordinate().y);
                                } else {
                                    return 1;
                                }
                            }
                        }
                    }
                };
        } else if (order == Order.RIVER) {
            return new Comparator<FeatureServiceFeature>() {

                    @Override
                    public int compare(final FeatureServiceFeature o1, final FeatureServiceFeature o2) {
                        if ((o1 == null) && (o2 != null)) {
                            return 1;
                        } else if ((o2 == null) && (o1 != null)) {
                            return -1;
                        } else if ((o2 == null) && (o1 == null)) {
                            return 0;
                        } else {
                            final String baCd1 = getBaCd(o1);
                            final String baCd2 = getBaCd(o2);

                            if ((baCd1 == null) && (baCd2 != null)) {
                                return 1;
                            } else if ((baCd2 == null) && (baCd1 != null)) {
                                return -1;
                            } else if ((baCd2 == null) && (baCd1 == null)) {
                                return 0;
                            } else {
                                if (baCd1.compareTo(baCd2) == 0) {
                                    return (int)Math.signum(getStatValue(o1) - getStatValue(o2));
                                } else {
                                    return baCd1.compareTo(baCd2);
                                }
                            }
                        }
                    }
                };
        } else if (order == Order.LAWA) {
            return new Comparator<FeatureServiceFeature>() {

                    @Override
                    public int compare(final FeatureServiceFeature o1, final FeatureServiceFeature o2) {
                        if ((o1 == null) && (o2 != null)) {
                            return 1;
                        } else if ((o2 == null) && (o1 != null)) {
                            return -1;
                        } else if ((o2 == null) && (o1 == null)) {
                            return 0;
                        } else {
                            final String laCd1 = getLaCd(o1);
                            final String laCd2 = getLaCd(o2);

                            if ((laCd1 == null) && (laCd2 != null)) {
                                return 1;
                            } else if ((laCd2 == null) && (laCd1 != null)) {
                                return -1;
                            } else if ((laCd2 == null) && (laCd1 == null)) {
                                return 0;
                            } else {
                                if (laCd1.compareTo(laCd2) == 0) {
                                    return (int)Math.signum(getLaStatValue(o1) - getLaStatValue(o2));
                                } else {
                                    return laCd1.compareTo(laCd2);
                                }
                            }
                        }
                    }
                };
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getBaCd(final FeatureServiceFeature f) {
        Object baCd = f.getProperty("ba_cd");

        if (baCd == null) {
            baCd = f.getProperty("ba_st");

            if (baCd instanceof CidsBean) {
                return (String)((CidsBean)baCd).getProperty("route.ba_cd");
            } else {
                return null;
            }
        } else {
            return (String)baCd;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Double getStatValue(final FeatureServiceFeature f) {
        final Object stat = f.getProperty("ba_st");

        if (stat instanceof CidsBean) {
            return (Double)((CidsBean)stat).getProperty("wert");
        } else {
            if (stat == null) {
                return 0.0;
            } else {
                return (Double)stat;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getLaCd(final FeatureServiceFeature f) {
        return (String)f.getProperty("la_cd");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Double getLaStatValue(final FeatureServiceFeature f) {
        final Object stat = f.getProperty("la_st");

        if (stat == null) {
            return 0.0;
        } else {
            return (Double)stat;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the order
     */
    public Dimension getPhotoSize() {
        return dimensions[size.ordinal()];
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the automatic
     */
    public boolean isAutomatic() {
        return automatic;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PhotoOptionsDialog getInstance() {
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

        bgOrder = new javax.swing.ButtonGroup();
        bgSize = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 32767));
        jPanel2 = new javax.swing.JPanel();
        labOrder = new javax.swing.JLabel();
        rbXy = new javax.swing.JRadioButton();
        rbGewaesser = new javax.swing.JRadioButton();
        rbLawaCode = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        labRiver = new javax.swing.JLabel();
        cbAutomatic = new javax.swing.JCheckBox();
        txtDistance = new javax.swing.JTextField();
        labM = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        labSize = new javax.swing.JLabel();
        rbSmall = new javax.swing.JRadioButton();
        rbMedium = new javax.swing.JRadioButton();
        rbLarge = new javax.swing.JRadioButton();
        rbExtraLarge = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.title_1",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.butOk.text_1",
                new Object[] {})); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(120, 29));
        butOk.setPreferredSize(new java.awt.Dimension(150, 29));
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
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.butCancel.text_1",
                new Object[] {})); // NOI18N
        butCancel.setMinimumSize(new java.awt.Dimension(120, 29));
        butCancel.setPreferredSize(new java.awt.Dimension(150, 29));
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(filler1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labOrder,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.labOrder.text",
                new Object[] {})); // NOI18N
        labOrder.setPreferredSize(new java.awt.Dimension(150, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        jPanel2.add(labOrder, gridBagConstraints);

        bgOrder.add(rbXy);
        rbXy.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            rbXy,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.rbXy.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel2.add(rbXy, gridBagConstraints);

        bgOrder.add(rbGewaesser);
        org.openide.awt.Mnemonics.setLocalizedText(
            rbGewaesser,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.rbGewaesser.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        jPanel2.add(rbGewaesser, gridBagConstraints);

        bgOrder.add(rbLawaCode);
        org.openide.awt.Mnemonics.setLocalizedText(
            rbLawaCode,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.rbLawaCode.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        jPanel2.add(rbLawaCode, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labRiver,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.labRiver.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 0);
        jPanel3.add(labRiver, gridBagConstraints);

        cbAutomatic.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            cbAutomatic,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.cbAutomatic.text",
                new Object[] {})); // NOI18N
        cbAutomatic.setPreferredSize(new java.awt.Dimension(218, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        jPanel3.add(cbAutomatic, gridBagConstraints);

        txtDistance.setText(org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.txtDistance.text",
                new Object[] {})); // NOI18N
        txtDistance.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        jPanel3.add(txtDistance, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labM,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.labM.text",
                new Object[] {})); // NOI18N
        labM.setPreferredSize(new java.awt.Dimension(30, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        jPanel3.add(labM, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        getContentPane().add(jPanel3, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labSize,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.labSize.text",
                new Object[] {})); // NOI18N
        labSize.setPreferredSize(new java.awt.Dimension(150, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel4.add(labSize, gridBagConstraints);

        bgSize.add(rbSmall);
        org.openide.awt.Mnemonics.setLocalizedText(
            rbSmall,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.rbSmall.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        jPanel4.add(rbSmall, gridBagConstraints);

        bgSize.add(rbMedium);
        rbMedium.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            rbMedium,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.rbMedium.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 15, 0, 0);
        jPanel4.add(rbMedium, gridBagConstraints);

        bgSize.add(rbLarge);
        org.openide.awt.Mnemonics.setLocalizedText(
            rbLarge,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.rbLarge.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 15, 0, 0);
        jPanel4.add(rbLarge, gridBagConstraints);

        bgSize.add(rbExtraLarge);
        org.openide.awt.Mnemonics.setLocalizedText(
            rbExtraLarge,
            org.openide.util.NbBundle.getMessage(
                PhotoOptionsDialog.class,
                "PhotoOptionsDialog.rbExtraLarge.text",
                new Object[] {})); // NOI18N
        rbExtraLarge.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    rbExtraLargeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 15, 0, 0);
        jPanel4.add(rbExtraLarge, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        getContentPane().add(jPanel4, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        if (order == Order.XY) {
            rbXy.setSelected(true);
        } else if (order == Order.RIVER) {
            rbGewaesser.setSelected(true);
        } else if (order == Order.LAWA) {
            rbLawaCode.setSelected(true);
        }

        if (size == Size.SMALL) {
            rbSmall.setSelected(true);
        } else if (size == Size.MEDIUM) {
            rbMedium.setSelected(true);
        } else if (size == Size.LARGE) {
            rbLarge.setSelected(true);
        } else if (size == Size.EXTRA_LARGE) {
            rbExtraLarge.setSelected(true);
        }

        cbAutomatic.setSelected(automatic);
        txtDistance.setText(String.valueOf(distance).replace('.', ','));

        setVisible(false);
    } //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        automatic = cbAutomatic.isSelected();
        try {
            distance = Double.parseDouble(txtDistance.getText().replace(',', '.'));
        } catch (NumberFormatException e) {
            // nothing to do. The value in the text field will just be ignored
        }

        if (bgOrder.isSelected(rbXy.getModel())) {
            order = Order.XY;
        } else if (bgOrder.isSelected(rbGewaesser.getModel())) {
            order = Order.RIVER;
        } else if (bgOrder.isSelected(rbLawaCode.getModel())) {
            order = Order.LAWA;
        }

        if (bgSize.isSelected(rbSmall.getModel())) {
            size = Size.SMALL;
        } else if (bgSize.isSelected(rbMedium.getModel())) {
            size = Size.MEDIUM;
        } else if (bgSize.isSelected(rbLarge.getModel())) {
            size = Size.LARGE;
        } else if (bgSize.isSelected(rbExtraLarge.getModel())) {
            size = Size.EXTRA_LARGE;
        }

        final PhotoInfoListener infoListener = (PhotoInfoListener)AppBroker.getInstance().getMappingComponent()
                    .getInputEventListener()
                    .get(PhotoInfoListener.MODE);

        if (infoListener != null) {
            infoListener.setPhotoSize();
        }

        setVisible(false);
    } //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void rbExtraLargeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_rbExtraLargeActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_rbExtraLargeActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient PhotoOptionsDialog INSTANCE = new PhotoOptionsDialog(AppBroker.getInstance()
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
