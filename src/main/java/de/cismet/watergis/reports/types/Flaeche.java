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
package de.cismet.watergis.reports.types;

import com.vividsolutions.jts.geom.Geometry;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Flaeche {

    //~ Instance fields --------------------------------------------------------

    private Geometry geom;
    private Object attr1;
    private Object attr2;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the geom
     */
    public Geometry getGeom() {
        return geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geom  the geom to set
     */
    public void setGeom(final Geometry geom) {
        this.geom = geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the attr1
     */
    public Object getAttr1() {
        return attr1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  attr1  the attr1 to set
     */
    public void setAttr1(final Object attr1) {
        this.attr1 = attr1;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the attr2
     */
    public Object getAttr2() {
        return attr2;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  attr2  the attr2 to set
     */
    public void setAttr2(final Object attr2) {
        this.attr2 = attr2;
    }
}
