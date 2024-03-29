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

import java.awt.BorderLayout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class MapPanel extends javax.swing.JPanel implements PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MapPanel.class);

    //~ Instance fields --------------------------------------------------------

    private MappingComponent mappingComponent;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form KartenPanel.
     */
    public MapPanel() {
        initComponents();
        initMap();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(MapPanel.class, "MapPanel.jLabel1.text")); // NOI18N
        add(jLabel1, java.awt.BorderLayout.CENTER);
    }                                                                                       // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     */
    private void initMap() {
        mappingComponent = CismapBroker.getInstance().getMappingComponent();
        mappingComponent.setBackgroundEnabled(true);
        mappingComponent.addPropertyChangeListener(this);
        this.add(BorderLayout.CENTER, mappingComponent);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final Object source = evt.getSource();
        final String propName = evt.getPropertyName();
        final Object newValue = evt.getNewValue();

        if (source == null) {
            return;
        }
        if (source.equals(mappingComponent)) {
            if (mappingComponent.PROPERTY_MAP_INTERACTION_MODE.equals(propName)) {
                if (newValue instanceof String) {
                    AppBroker.getInstance().switchMapMode((String)newValue);
                } else {
                    LOG.error("MapingComponent changed to a mode, which is not mapped to a String");
                }
            }
        }
    }
}
