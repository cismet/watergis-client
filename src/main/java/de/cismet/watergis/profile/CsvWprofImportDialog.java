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
package de.cismet.watergis.profile;

import Sirius.server.middleware.types.MetaClass;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CsvWprofImportDialog extends AbstractImportDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final String[][] ALL_ALLOWED_TYPE_COMBINATIONS;

    static {
        ALL_ALLOWED_TYPE_COMBINATIONS = new String[2][];
        ALL_ALLOWED_TYPE_COMBINATIONS[0] = new String[] {
                ProfileReader.GAF_FIELDS.STATION.name(),
                ProfileReader.GAF_FIELDS.Y.name(),
                ProfileReader.GAF_FIELDS.Z.name()
            };
        ALL_ALLOWED_TYPE_COMBINATIONS[1] = new String[] {
                ProfileReader.GAF_FIELDS.Z.name(),
                ProfileReader.GAF_FIELDS.HW.name(),
                ProfileReader.GAF_FIELDS.RW.name()
            };
    }

    //~ Instance fields --------------------------------------------------------

    private MetaClass LAWA_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_la");

    private DefaultComboBoxModel<String>[] models;
//    private String[][] exampleData;
    private boolean cancelled = false;
    private boolean routeRequired = false;
    private final WPROFReader parentReader;
    private String[] columnProposal = null;
    private boolean initInProgress = false;
    private List<JPanel> previewPanels = new ArrayList<>();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JButton butOk1;
    private javax.swing.JComboBox cbRoute;
    private javax.swing.JCheckBox chkHeader;
    private javax.swing.JCheckBox chkSeparator;
    private javax.swing.JDialog diaRoute;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panColumns;
    private javax.swing.JPanel panControl;
    private javax.swing.JPanel panData;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form CsvWprofImportDialog.
     *
     * @param  parent          DOCUMENT ME!
     * @param  modal           DOCUMENT ME!
     * @param  columnProposal  DOCUMENT ME!
     * @param  parentReader    DOCUMENT ME!
     */
    public CsvWprofImportDialog(final java.awt.Frame parent,
            final boolean modal,
            final String[] columnProposal,
            final WPROFReader parentReader) {
        super(parent, modal);
        setTitle("Importiere Profildaten");
        initComponents();
        this.parentReader = parentReader;
        init(columnProposal, true);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  columnProposal  DOCUMENT ME!
     * @param  init            DOCUMENT ME!
     */
    private void init(final String[] columnProposal, final boolean init) {
        models = new DefaultComboBoxModel[columnProposal.length];
        this.columnProposal = columnProposal;
        final String[][] exampleData = parentReader.readExampleData();

        if (init) {
            for (int i = 0; i < columnProposal.length; ++i) {
                if (columnProposal[i] != null) {
                    initInProgress = true;
                    chkHeader.setSelected(true);
                    initInProgress = false;
                    break;
                }
            }
        }

        for (int i = 0; i < columnProposal.length; ++i) {
            final JComboBox cbColumn = new JComboBox();
            final boolean isDouble = hasOnlyDouble(exampleData, i);

            models[i] = createModel(isDouble);
            if (columnProposal[i] != null) {
                models[i].setSelectedItem(columnProposal[i]);
            }
            cbColumn.setModel(models[i]);
            cbColumn.addItemListener(new ColumnItemListener(i));
            cbColumn.setPreferredSize(new Dimension(120, 34));
            cbColumn.setMinimumSize(new Dimension(120, 34));
            cbColumn.setMaximumSize(new Dimension(120, 34));
            panColumns.add(cbColumn);
        }

        for (int row = 0; (row < 15) && (row < exampleData.length); ++row) {
            final JPanel panRow = new JPanel();

            for (int i = 0; (i < columnProposal.length) && (i < exampleData[row].length); ++i) {
                final JLabel labData = new JLabel(exampleData[row][i].substring(
                            0,
                            Math.min(exampleData[row][i].length(), 12)));

                labData.setToolTipText(exampleData[row][i]);
                labData.setPreferredSize(new Dimension(120, 22));
                labData.setMinimumSize(new Dimension(120, 22));
                labData.setMaximumSize(new Dimension(120, 22));
                panRow.add(labData);
            }
            int emptyCols = 0;

            while ((exampleData[row].length + emptyCols) < columnProposal.length) {
                final JLabel labData = new JLabel("<html>&nbsp;<html>");

                labData.setPreferredSize(new Dimension(120, 22));
                labData.setMinimumSize(new Dimension(120, 22));
                labData.setMaximumSize(new Dimension(120, 22));
                panRow.add(labData);
                ++emptyCols;
            }

            panRow.setOpaque(false);
            final GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = row + 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
            previewPanels.add(panRow);
            panData.add(panRow, gridBagConstraints);
//            getContentPane().add(panRow, gridBagConstraints);
        }

        adjustComboboxes(0);

        final int width = columnProposal.length * 130;

        setSize(((width < 720) ? 720 : width), 351);
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setSize(((width < 720) ? 720 : width), 350);
                    getContentPane().repaint();
                    getContentPane().doLayout();
                }
            });

        if (models.length < 3) {
            // todo: Meldung an Nutzer
            cancelled = true;
            setVisible(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   exampleData  DOCUMENT ME!
     * @param   index        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean hasOnlyDouble(final String[][] exampleData, final int index) {
        for (int i = (chkHeader.isSelected() ? 1 : 0); i < exampleData.length; ++i) {
            final String[] row = exampleData[i];

            try {
                if (row.length > index) {
                    Double.parseDouble(row[index].replace(",", ".").replace("*", ""));
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isDouble  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private DefaultComboBoxModel<String> createModel(final boolean isDouble) {
        final TreeSet<String> allowedTypes = new TreeSet<>();

        allowedTypes.add("");

        if (isDouble) {
            allowedTypes.add(ProfileReader.GAF_FIELDS.Z.name());
            allowedTypes.add(ProfileReader.GAF_FIELDS.Y.name());
            allowedTypes.add(ProfileReader.GAF_FIELDS.STATION.name());
        }

        allowedTypes.add(ProfileReader.GAF_FIELDS.HYK.name());
        allowedTypes.add(ProfileReader.GAF_FIELDS.ID.name());
        allowedTypes.add(ProfileReader.GAF_FIELDS.RK.name());
        allowedTypes.add(ProfileReader.GAF_FIELDS.BK.name());
        allowedTypes.add(ProfileReader.GAF_FIELDS.KZ.name());
        allowedTypes.add(ProfileReader.GAF_FIELDS.HW.name());
        allowedTypes.add(ProfileReader.GAF_FIELDS.RW.name());

        final List<String> list = new ArrayList<>(allowedTypes.descendingSet());
        Collections.reverse(list);

        return new DefaultComboBoxModel<>(list.toArray(new String[allowedTypes.size()]));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  index  DOCUMENT ME!
     */
    private void adjustComboboxes(final int index) {
        final TreeSet<String> usedValues = new TreeSet<>();
        final boolean allBoxesFilled = true;
        final String selectedValue = (String)models[index].getSelectedItem();

        usedValues.add(selectedValue);

        for (int i = 0; i < models.length; ++i) {
            if (i != index) {
                final String value = (String)models[i].getSelectedItem();

                if (!value.equals("") && usedValues.contains(value)) {
                    models[i].setSelectedItem("");
                } else if (!value.equals("") && !usedValues.contains(value)) {
                    usedValues.add(value);
                }
            }
        }

        butOk.setEnabled(hasAllowedCombination(usedValues));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   usedValues  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean hasAllowedCombination(final TreeSet<String> usedValues) {
        for (final String[] combination : ALL_ALLOWED_TYPE_COMBINATIONS) {
            if (usedValues.size() >= combination.length) {
                boolean combFullfilled = true;

                for (final String val : combination) {
                    if (!usedValues.contains(val)) {
                        combFullfilled = false;
                        break;
                    }
                }

                if (combFullfilled) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        diaRoute = new javax.swing.JDialog();
        butOk1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        cbRoute = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(LAWA_MC, true);
        panColumns = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        panData = new javax.swing.JPanel();
        panControl = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        chkHeader = new javax.swing.JCheckBox();
        chkSeparator = new javax.swing.JCheckBox();

        diaRoute.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        diaRoute.setTitle(org.openide.util.NbBundle.getMessage(
                CsvWprofImportDialog.class,
                "CsvWprofImportDialog.diaRoute.title",
                new Object[] {})); // NOI18N
        diaRoute.setAlwaysOnTop(true);
        diaRoute.setModal(true);
        diaRoute.getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk1,
            org.openide.util.NbBundle.getMessage(
                CsvWprofImportDialog.class,
                "CsvWprofImportDialog.butOk1.text",
                new Object[] {})); // NOI18N
        butOk1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOk1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        diaRoute.getContentPane().add(butOk1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(
                CsvWprofImportDialog.class,
                "CsvWprofImportDialog.jLabel2.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        diaRoute.getContentPane().add(jLabel2, gridBagConstraints);

        cbRoute.setMinimumSize(new java.awt.Dimension(200, 27));
        cbRoute.setPreferredSize(new java.awt.Dimension(200, 27));
        cbRoute.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbRouteActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        diaRoute.getContentPane().add(cbRoute, gridBagConstraints);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        panColumns.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
        getContentPane().add(panColumns, gridBagConstraints);

        panData.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(panData);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        panControl.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                CsvWprofImportDialog.class,
                "CsvWprofImportDialog.butOk.text",
                new Object[] {})); // NOI18N
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
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        panControl.add(butOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butCancel,
            org.openide.util.NbBundle.getMessage(
                CsvWprofImportDialog.class,
                "CsvWprofImportDialog.butCancel.text",
                new Object[] {})); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 10);
        panControl.add(butCancel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            chkHeader,
            org.openide.util.NbBundle.getMessage(
                CsvWprofImportDialog.class,
                "CsvWprofImportDialog.chkHeader.text",
                new Object[] {})); // NOI18N
        chkHeader.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkHeaderItemStateChanged(evt);
                }
            });
        chkHeader.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(final javax.swing.event.ChangeEvent evt) {
                    chkHeaderStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 12, 10);
        panControl.add(chkHeader, gridBagConstraints);

        chkSeparator.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            chkSeparator,
            org.openide.util.NbBundle.getMessage(
                CsvWprofImportDialog.class,
                "CsvWprofImportDialog.chkSeparator.text",
                new Object[] {})); // NOI18N
        chkSeparator.setToolTipText(org.openide.util.NbBundle.getMessage(
                CsvWprofImportDialog.class,
                "CsvWprofImportDialog.chkSeparator.toolTipText",
                new Object[] {})); // NOI18N
        chkSeparator.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkSeparatorItemStateChanged(evt);
                }
            });
        chkSeparator.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(final javax.swing.event.ChangeEvent evt) {
                    chkSeparatorStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 12, 10);
        panControl.add(chkSeparator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        getContentPane().add(panControl, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        cancelled = false;
        routeRequired = false;

        final Map<ProfileReader.GAF_FIELDS, Integer> fieldMap = new HashMap<>();

        for (int i = 0; i < models.length; ++i) {
            final String selectedValue = (String)models[i].getSelectedItem();

            if (!selectedValue.equals("")) {
                fieldMap.put(ProfileReader.GAF_FIELDS.valueOf(selectedValue), i);
            }
        }

        parentReader.setHeader(fieldMap, chkHeader.isSelected());

        if (!fieldMap.containsKey(ProfileReader.GAF_FIELDS.RW) || !fieldMap.containsKey(ProfileReader.GAF_FIELDS.HW)) {
            routeRequired = true;
        }

        setVisible(false);
    } //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        cancelled = true;
        routeRequired = false;
        setVisible(false);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOk1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOk1ActionPerformed
        if (cbRoute.getSelectedItem() != null) {
            parentReader.setRoute((CidsLayerFeature)cbRoute.getSelectedItem());
            diaRoute.setVisible(false);
        }
    }                                                                          //GEN-LAST:event_butOk1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbRouteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbRouteActionPerformed
    }                                                                           //GEN-LAST:event_cbRouteActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkHeaderStateChanged(final javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_chkHeaderStateChanged
    }                                                                             //GEN-LAST:event_chkHeaderStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkHeaderItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkHeaderItemStateChanged
        if (!initInProgress) {
            panColumns.removeAll();

            for (final JPanel p : previewPanels) {
                panData.remove(p);
//                getContentPane().remove(p);
            }
            previewPanels.clear();
            init(columnProposal, false);
        }
    } //GEN-LAST:event_chkHeaderItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkSeparatorItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkSeparatorItemStateChanged
        parentReader.setDuplicateSepAllowed(chkSeparator.isSelected());

        if (!initInProgress) {
            panColumns.removeAll();

            for (final JPanel p : previewPanels) {
                panData.remove(p);
                getContentPane().remove(p);
            }
            previewPanels.clear();
            init(columnProposal, false);
        }
    } //GEN-LAST:event_chkSeparatorItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkSeparatorStateChanged(final javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_chkSeparatorStateChanged
        // TODO add your handling code here:
    } //GEN-LAST:event_chkSeparatorStateChanged

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isRouteRequired() {
        return routeRequired;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ColumnItemListener implements ItemListener {

        //~ Instance fields ----------------------------------------------------

        private final int index;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ColumnItemListener object.
         *
         * @param  index  DOCUMENT ME!
         */
        public ColumnItemListener(final int index) {
            this.index = index;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void itemStateChanged(final ItemEvent e) {
            adjustComboboxes(index);
        }
    }
}
