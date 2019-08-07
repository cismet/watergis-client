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
public class KTechRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("tech", new Varchar(10, false));
        typeMap.put("name", new Varchar(50, false));
        typeMap.put("art", new Varchar(1, false));
        typeMap.put("url", new Varchar(1, false));
        typeMap.put("ubl", new Varchar(1, false));
        typeMap.put("us", new Varchar(1, false));
        typeMap.put("ubr", new Varchar(1, false));
        typeMap.put("urr", new Varchar(1, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }
}
