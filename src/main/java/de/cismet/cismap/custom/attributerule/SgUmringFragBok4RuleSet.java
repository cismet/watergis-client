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

import com.vividsolutions.jts.geom.Geometry;

import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SgUmringFragBok4RuleSet extends BasicBufferRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("bok_id", new Numeric(12, 0, false, true));
        typeMap.put("see_id", new Numeric(12, 0, false, true));
    }
}
