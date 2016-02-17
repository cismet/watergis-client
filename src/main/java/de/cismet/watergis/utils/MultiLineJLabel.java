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

import javax.swing.JLabel;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class MultiLineJLabel extends JLabel {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setText(final String text) {
        super.setText("<html><pre>" + text + "</pre></html>");
    }
}
