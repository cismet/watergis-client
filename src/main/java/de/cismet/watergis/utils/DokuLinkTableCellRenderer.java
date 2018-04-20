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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;

import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DokuLinkTableCellRenderer extends DefaultTableCellRenderer {

    //~ Instance fields --------------------------------------------------------

    private int alignment = JLabel.LEFT;
    private final LinkTableCellRenderer linkRenderer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LinkTableCellRenderer object.
     */
    public DokuLinkTableCellRenderer() {
        linkRenderer = new LinkTableCellRenderer();
    }

    /**
     * Creates a new LinkTableCellRenderer object.
     *
     * @param  alignment  DOCUMENT ME!
     */
    public DokuLinkTableCellRenderer(final int alignment) {
        this.alignment = alignment;
        linkRenderer = new LinkTableCellRenderer(alignment);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getTableCellRendererComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {
        if (isLink(value)) {
            return linkRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isLink(final Object value) {
        if (value instanceof String) {
            final String stringVal = (String)value;
            if (stringVal.length() > 2) {
                final boolean firstCharLetter = Character.isUpperCase(stringVal.charAt(0))
                            || Character.isLowerCase(stringVal.charAt(0));

                return firstCharLetter && stringVal.substring(1, 2).equals(":");
            }
        }

        return false;
    }
}
