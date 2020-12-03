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

import org.jdom.Element;

import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Export {

    //~ Instance fields --------------------------------------------------------

    private String tableName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Export object.
     *
     * @param  element  DOCUMENT ME!
     */
    public Export(final Element element) {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  element  DOCUMENT ME!
     */
    private void configure(final Element element) {
        final Element table = element.getChild("table");

        if (table != null) {
            tableName = table.getAttributeValue("name");

            final List attributes = table.getChildren("attribute");
            if (attributes != null) {
//                for (Element attribute : attributes) {
//
//                }
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class Attribute {
    }
}
