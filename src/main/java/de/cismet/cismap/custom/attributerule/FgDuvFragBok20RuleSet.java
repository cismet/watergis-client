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
package de.cismet.cismap.custom.attributerule;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgDuvFragBok20RuleSet extends BasicBufferRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("ba_cd", new Varchar(50, true));
        typeMap.put("bok_id", new Numeric(15, 0, false, false));
    }
}
