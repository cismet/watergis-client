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
public class LinkTableCellRenderer extends DefaultTableCellRenderer {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getTableCellRendererComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {
        final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (c instanceof JLabel) {
            ((JLabel)c).setForeground(Color.BLUE);
            final Font underlinedFont = ((JLabel)c).getFont();
            final Map attributes = underlinedFont.getAttributes();
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            ((JLabel)c).setFont(underlinedFont.deriveFont(attributes));
        }

        return c;
    }
}
