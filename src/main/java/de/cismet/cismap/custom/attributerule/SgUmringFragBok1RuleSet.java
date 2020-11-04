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
package de.cismet.cismap.custom.attributerule;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SgUmringFragBok1RuleSet extends BasicBufferRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("bok_id", new Numeric(12, 0, false, true));
        typeMap.put("see_id", new Numeric(12, 0, false, true));
    }
}
