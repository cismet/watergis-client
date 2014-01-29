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

import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.cismet.cids.custom.beans.watergis.Bookmark;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.bookmarks.DeleteBookmarkAction;
import de.cismet.watergis.gui.actions.bookmarks.LoadBookmarksAction;
import de.cismet.watergis.gui.actions.bookmarks.ZoomBookmarkInMapAction;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ManageBookmarksDialog extends javax.swing.JDialog {

    //~ Instance fields --------------------------------------------------------

    private DocumentListener txtNameDocumentListener;
    private DocumentListener txtaDescriptionDocumentListener;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnLoad;
    private javax.swing.JButton btnPan;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnZoom;
    private de.cismet.watergis.gui.actions.bookmarks.DeleteBookmarkAction deleteBookmarkAction;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblName;
    private de.cismet.watergis.gui.actions.bookmarks.LoadBookmarksAction loadBookmarksAction;
    private javax.swing.JList lstBookmarks;
    private de.cismet.watergis.gui.actions.bookmarks.SaveBookmarksAction saveBookmarksAction;
    private de.cismet.watergis.gui.actions.bookmarks.PanBookmarkInMapAction showBookmarkInMapAction;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextArea txtaDescription;
    private de.cismet.watergis.gui.actions.bookmarks.ZoomBookmarkInMapAction zoomBookmarkInMapAction;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form ManageBookmarksDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public ManageBookmarksDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();
        initDocumentListener();
        addDocumentListeners();
        enableComponents(false);
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

        showBookmarkInMapAction = new de.cismet.watergis.gui.actions.bookmarks.PanBookmarkInMapAction(this);
        deleteBookmarkAction = new DeleteBookmarkAction(this);
        zoomBookmarkInMapAction = new ZoomBookmarkInMapAction(this);
        saveBookmarksAction = new de.cismet.watergis.gui.actions.bookmarks.SaveBookmarksAction();
        loadBookmarksAction = new LoadBookmarksAction(this);
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstBookmarks = new javax.swing.JList();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblDescription = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtaDescription = new javax.swing.JTextArea();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0),
                new java.awt.Dimension(32767, 0));
        jPanel2 = new javax.swing.JPanel();
        btnLoad = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnZoom = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnPan = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(ManageBookmarksDialog.class, "ManageBookmarksDialog.title")); // NOI18N
        setPreferredSize(new java.awt.Dimension(471, 441));
        addWindowListener(new java.awt.event.WindowAdapter() {

                @Override
                public void windowOpened(final java.awt.event.WindowEvent evt) {
                    formWindowOpened(evt);
                }
            });

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 7, 7, 7));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        lstBookmarks.setMaximumSize(new java.awt.Dimension(32767, 32767));
        lstBookmarks.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

                @Override
                public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                    lstBookmarksValueChanged(evt);
                }
            });
        jScrollPane1.setViewportView(lstBookmarks);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 3, 0);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            lblName,
            org.openide.util.NbBundle.getMessage(ManageBookmarksDialog.class, "ManageBookmarksDialog.lblName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 2);
        jPanel1.add(lblName, gridBagConstraints);

        txtName.setText(org.openide.util.NbBundle.getMessage(
                ManageBookmarksDialog.class,
                "ManageBookmarksDialog.txtName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 4, 0);
        jPanel1.add(txtName, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            lblDescription,
            org.openide.util.NbBundle.getMessage(
                ManageBookmarksDialog.class,
                "ManageBookmarksDialog.lblDescription.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 2);
        jPanel1.add(lblDescription, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(258, 125));

        txtaDescription.setColumns(20);
        txtaDescription.setRows(5);
        txtaDescription.setPreferredSize(new java.awt.Dimension(250, 120));
        jScrollPane2.setViewportView(txtaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 4, 0);
        jPanel1.add(jScrollPane2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(filler1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        btnLoad.setAction(loadBookmarksAction);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 4);
        jPanel2.add(btnLoad, gridBagConstraints);

        btnSave.setAction(saveBookmarksAction);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 4);
        jPanel2.add(btnSave, gridBagConstraints);

        btnZoom.setAction(zoomBookmarkInMapAction);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 4);
        jPanel2.add(btnZoom, gridBagConstraints);

        btnDelete.setAction(deleteBookmarkAction);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 0);
        jPanel2.add(btnDelete, gridBagConstraints);

        btnPan.setAction(showBookmarkInMapAction);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 4);
        jPanel2.add(btnPan, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jPanel2, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void formWindowOpened(final java.awt.event.WindowEvent evt) { //GEN-FIRST:event_formWindowOpened
        updateModel();
    }                                                                     //GEN-LAST:event_formWindowOpened

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void lstBookmarksValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstBookmarksValueChanged
        final Bookmark bookmark = (Bookmark)lstBookmarks.getSelectedValue();
        if (bookmark != null) {
            txtName.setText(bookmark.getName());
            txtaDescription.setText(bookmark.getDescription());
            enableComponents(true);
        } else {
            removeDocumentListeners();
            txtName.setText("");
            txtaDescription.setText("");
            enableComponents(false);
            addDocumentListeners();
        }
    }                                                                                       //GEN-LAST:event_lstBookmarksValueChanged

    /**
     * DOCUMENT ME!
     */
    public void updateModel() {
        final DefaultListModel<Bookmark> model = new DefaultListModel<Bookmark>();
        for (final Bookmark b : AppBroker.getInstance().getBookmarkManager().getBookmarks()) {
            model.addElement(b);
        }
        lstBookmarks.setModel(model);
    }

    /**
     * DOCUMENT ME!
     */
    private void initDocumentListener() {
        txtNameDocumentListener = new DocumentListener() {

                @Override
                public void insertUpdate(final DocumentEvent e) {
                    updateBookMarkName();
                }

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    updateBookMarkName();
                }

                @Override
                public void changedUpdate(final DocumentEvent e) {
                    updateBookMarkName();
                }
            };

        txtaDescriptionDocumentListener = new DocumentListener() {

                @Override
                public void insertUpdate(final DocumentEvent e) {
                    updateBookMarkDescription();
                }

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    updateBookMarkDescription();
                }

                @Override
                public void changedUpdate(final DocumentEvent e) {
                    updateBookMarkDescription();
                }
            };
    }

    /**
     * DOCUMENT ME!
     */
    private void updateBookMarkName() {
        final Bookmark bookmark = (Bookmark)lstBookmarks.getSelectedValue();
        final String bookmarkName = txtName.getText().trim();
        if ((bookmarkName == null) || bookmarkName.equals("")) {
            txtName.setBackground(Color.red);
        } else {
            bookmark.setName(txtName.getText());
            txtName.setBackground(Color.white);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void updateBookMarkDescription() {
        final Bookmark bookmark = (Bookmark)lstBookmarks.getSelectedValue();
        bookmark.setDescription(txtaDescription.getText());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ManageBookmarksDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ManageBookmarksDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ManageBookmarksDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ManageBookmarksDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final ManageBookmarksDialog dialog = new ManageBookmarksDialog(new javax.swing.JFrame(), true);
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
     * @return  DOCUMENT ME!
     */
    public Bookmark getSelectedBookmark() {
        return (Bookmark)lstBookmarks.getSelectedValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bookmark  DOCUMENT ME!
     */
    public void removeBookmarkFromList(final Bookmark bookmark) {
        ((DefaultListModel)lstBookmarks.getModel()).removeElement(bookmark);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  b  DOCUMENT ME!
     */
    private void enableComponents(final boolean b) {
        btnDelete.setEnabled(b);
        btnPan.setEnabled(b);
        btnZoom.setEnabled(b);
        txtaDescription.setEnabled(b);
        txtName.setEnabled(b);
    }

    /**
     * DOCUMENT ME!
     */
    private void addDocumentListeners() {
        txtName.getDocument().addDocumentListener(txtNameDocumentListener);
        txtaDescription.getDocument().addDocumentListener(txtaDescriptionDocumentListener);
    }

    /**
     * DOCUMENT ME!
     */
    private void removeDocumentListeners() {
        txtName.getDocument().removeDocumentListener(txtNameDocumentListener);
        txtaDescription.getDocument().removeDocumentListener(txtaDescriptionDocumentListener);
    }
}
