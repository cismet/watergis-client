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
package de.cismet.watergis.gui.components;

import org.apache.log4j.Logger;

import org.jdesktop.swingx.JXTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.style.BasicStyle;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.SimpleAttributeTableModel;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AttributeTableDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AttributeTableDialog.class);

    //~ Instance fields --------------------------------------------------------

    private FeatureServiceFeature returnValue;
    private SimpleAttributeTableModel model;
    private Color[] colors = new Color[] {
            new Color(217, 215, 204),
            new Color(242, 187, 19),
            new Color(217, 159, 126),
            new Color(242, 65, 48),
            new Color(121, 132, 39),
            new Color(184, 206, 233),
            new Color(216, 120, 57)
        };
    private int colorCounter = 0;
    private Map<FeatureServiceFeature, Paint> oldPaint = new HashMap<FeatureServiceFeature, Paint>();
    private List<PFeature> allPFeature;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable attrTab;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form AttributeTableDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  title   DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public AttributeTableDialog(final java.awt.Frame parent, final String title, final boolean modal) {
        super(parent, title, modal);
        initComponents();
        attrTab.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attrTab.setDefaultRenderer(Object.class, new ColoredCellRenderer());
        attrTab.setDefaultRenderer(Number.class, new ColoredCellRenderer());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  service      DOCUMENT ME!
     * @param  featureList  DOCUMENT ME!
     */
    public void setData(final AbstractFeatureService service, final List<FeatureServiceFeature> featureList) {
        final Map<String, FeatureServiceAttribute> featureServiceAttributes = service.getFeatureServiceAttributes();
        final List<String> orderedFeatureServiceAttributes = service.getOrderedFeatureServiceAttributes();
        oldPaint.clear();

        for (final FeatureServiceFeature fsf : featureList) {
            final Paint c = fsf.getFillingPaint();
            oldPaint.put(fsf, c);
            fsf.setFillingPaint(getNextColor());
        }

        model = new SimpleAttributeTableModel(
                orderedFeatureServiceAttributes,
                featureServiceAttributes,
                (List<FeatureServiceFeature>)featureList,
                AttributeTable.getTableRuleSetForFeatureService(service));
        attrTab.setModel(model);
        removeSelectionOnAllFeatures();
    }

    /**
     * DOCUMENT ME!
     */
    private void removeSelectionOnAllFeatures() {
        final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);
        allPFeature = getPFeature();
        final List<Feature> toBeUnselected = new ArrayList<Feature>();

        for (final PFeature feature : allPFeature) {
            if (feature.isSelected()) {
                feature.setSelected(false);
                feature.refreshDesign();
                sl.removeSelectedFeature(feature);
                toBeUnselected.add(feature.getFeature());
            }
        }
        ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection()).unselect(
            toBeUnselected);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<PFeature> getPFeature() {
        final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);

        return sl.getAllSelectedPFeatures();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Color getNextColor() {
        return colors[(++colorCounter) % colors.length];
    }

    /**
     * DOCUMENT ME!
     *
     * @param  text  DOCUMENT ME!
     */
    public void setCustomText(final String text) {
        lblTitle.setText(text);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        attrTab = new JXTable();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {

                @Override
                public void windowClosed(final java.awt.event.WindowEvent evt) {
                    formWindowClosed(evt);
                }
            });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("Ubuntu", 0, 18)); // NOI18N
        lblTitle.setText(org.openide.util.NbBundle.getMessage(
                AttributeTableDialog.class,
                "AttributeTableDialog.lblTitle.text",
                new Object[] {}));                            // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 10, 0);
        getContentPane().add(lblTitle, gridBagConstraints);

        attrTab.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                    { null, null, null, null },
                    { null, null, null, null },
                    { null, null, null, null },
                    { null, null, null, null }
                },
                new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        jScrollPane1.setViewportView(attrTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        butOk.setText(org.openide.util.NbBundle.getMessage(
                AttributeTableDialog.class,
                "AttributeTableDialog.butOk.text",
                new Object[] {})); // NOI18N
        butOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        getContentPane().add(butOk, gridBagConstraints);

        butCancel.setText(org.openide.util.NbBundle.getMessage(
                AttributeTableDialog.class,
                "AttributeTableDialog.butCancel.text",
                new Object[] {})); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 10);
        getContentPane().add(butCancel, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        returnValue = null;
        resetColors();
        setVisible(false);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        returnValue = model.getFeatureServiceFeature(attrTab.convertRowIndexToModel(attrTab.getSelectedRow()));
        resetColors();
        setVisible(false);
    }                                                                         //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void formWindowClosed(final java.awt.event.WindowEvent evt) { //GEN-FIRST:event_formWindowClosed
        resetColors();
    }                                                                     //GEN-LAST:event_formWindowClosed

    /**
     * DOCUMENT ME!
     */
    private void resetColors() {
        for (final FeatureServiceFeature fsf : oldPaint.keySet()) {
            final Paint old = oldPaint.get(fsf);
            fsf.setFillingPaint(old);
        }

        for (final PFeature feature : allPFeature) {
            feature.refreshDesign();
        }

        oldPaint.clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AttributeTableDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AttributeTableDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AttributeTableDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AttributeTableDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final AttributeTableDialog dialog = new AttributeTableDialog(
                            new javax.swing.JFrame(),
                            "test",
                            true);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                            @Override
                            public void windowClosing(final java.awt.event.WindowEvent e) {
                                System.exit(0);
                            }
                        });
                    dialog.setVisible(true);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the returnValue
     */
    public FeatureServiceFeature getReturnValue() {
        return returnValue;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ColoredCellRenderer extends DefaultTableCellRenderer {

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getTableCellRendererComponent(final JTable table,
                final Object value,
                final boolean isSelected,
                final boolean hasFocus,
                final int row,
                final int column) {
            final Component oc = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (oc instanceof JLabel) {
                final JLabel origLabel = (JLabel)oc;
                final JLabel c = new JLabel(origLabel.getText(),
                        origLabel.getIcon(),
                        origLabel.getHorizontalAlignment());

                final FeatureServiceFeature fsf = model.getFeatureServiceFeature(table.convertRowIndexToModel(row));

                c.setBorder(origLabel.getBorder());
                c.setOpaque(true);

                if (isSelected) {
                    c.setBackground(BasicStyle.lighten((Color)fsf.getFillingPaint()));
                } else {
                    c.setBackground((Color)fsf.getFillingPaint());
                }
                LOG.error("Background: " + c.getBackground());

                return c;
            }

            return oc;
        }
    }
}
