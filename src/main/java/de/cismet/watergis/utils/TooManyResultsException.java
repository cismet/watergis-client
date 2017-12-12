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

import org.openide.util.NbBundle;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TooManyResultsException extends Exception {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TooManyResultsException object.
     */
    public TooManyResultsException() {
        super(NbBundle.getMessage(TooManyResultsException.class, "TooManyResultsException.message.default"));
    }
}
