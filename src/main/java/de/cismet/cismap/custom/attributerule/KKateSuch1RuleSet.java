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
public class KKateSuch1RuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("index", new Numeric(2, 0, false));
        typeMap.put("thema", new Varchar(50, false));
        typeMap.put("attr", new Varchar(50, false));
        typeMap.put("name", new Varchar(50, false));
        typeMap.put("teil", new Numeric(1, 0, false));
    }
}
