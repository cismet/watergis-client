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
package de.cismet.watergis.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class GeoLinkUrl implements Transferable {

    //~ Instance fields --------------------------------------------------------

    String url;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeoLinkUrl object.
     *
     * @param  url  DOCUMENT ME!
     */
    public GeoLinkUrl(final String url) {
        this.url = url;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataFlavor.stringFlavor };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flavor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return DataFlavor.stringFlavor.equals(flavor);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flavor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedFlavorException  DOCUMENT ME!
     * @throws  IOException                 DOCUMENT ME!
     */
    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!DataFlavor.stringFlavor.equals(flavor)) {
            throw (new UnsupportedFlavorException(flavor));
        }

        return url;
    }
}
