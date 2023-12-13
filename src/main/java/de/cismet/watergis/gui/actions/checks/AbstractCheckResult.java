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
package de.cismet.watergis.gui.actions.checks;

import de.cismet.cismap.commons.featureservice.H2FeatureService;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCheckResult {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String[] getCheckNames();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract AbstractCheckAction.ProblemCountAndClasses getProblemTreeObjectCount();
    /**
     * DOCUMENT ME!
     *
     * @param   checkName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract int getErrorsPerCheck(String checkName);
    /**
     * DOCUMENT ME!
     *
     * @param   checkName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract H2FeatureService getErrorTablePerCheck(String checkName);
}
