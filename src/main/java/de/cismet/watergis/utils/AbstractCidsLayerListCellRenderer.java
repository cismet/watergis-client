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
package de.cismet.watergis.utils;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCidsLayerListCellRenderer extends DefaultListCellRenderer {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   bean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract String toString(CidsLayerFeature bean);

    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value,
            final int index,
            final boolean isSelected,
            final boolean cellHasFocus) {
        final Component c = super.getListCellRendererComponent(
                list,
                value,
                index,
                isSelected,
                cellHasFocus);

        if (c instanceof JLabel) {
            if (value instanceof CidsLayerFeature) {
                final CidsLayerFeature bean = (CidsLayerFeature)value;
                final String textRepresentation = toString(bean);
                ((JLabel)c).setText(textRepresentation);
            } else if (value == null) {
                ((JLabel)c).setText(" ");
            }
        }

        return c;
    }
}
