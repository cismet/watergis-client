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
package de.cismet.watergis.gui.actions;

/**
 * After every change of the MappingComponent modus, the cleanUp method will be invoked
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface CleanUpAction {

    //~ Methods ----------------------------------------------------------------

    /**
     * After every change of the MappingComponent modus, the cleanUp method will be invoked
     */
    void cleanUp();
}
