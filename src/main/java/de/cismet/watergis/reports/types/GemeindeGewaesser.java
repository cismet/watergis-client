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

import de.cismet.watergis.reports.KatasterflaechenReport;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GemeindeGewaesser implements Comparable<GemeindeGewaesser> {

    //~ Instance fields --------------------------------------------------------

    private int gemeinde;
    private int gewId;
    private String ba_cd;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GemeindeGewaesser object.
     *
     * @param  gemeinde  DOCUMENT ME!
     * @param  gewId     DOCUMENT ME!
     * @param  ba_cd     DOCUMENT ME!
     */
    public GemeindeGewaesser(final int gemeinde, final int gewId, final String ba_cd) {
        this.gemeinde = gemeinde;
        this.gewId = gewId;
        this.ba_cd = ba_cd;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int compareTo(final GemeindeGewaesser o) {
        if (gemeinde == o.gemeinde) {
            return (int)Math.signum(gewId - o.gewId);
        } else {
            return (int)Math.signum(gemeinde - o.gemeinde);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof GemeindeGewaesser) {
            final GemeindeGewaesser other = (GemeindeGewaesser)obj;
            return (gemeinde == other.gemeinde) && (gewId == other.gewId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (79 * hash) + this.gemeinde;
        hash = (79 * hash) + this.gewId;
        return hash;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the gemeinde
     */
    public int getGemeinde() {
        return gemeinde;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gemeinde  the gemeinde to set
     */
    public void setGemeinde(final int gemeinde) {
        this.gemeinde = gemeinde;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the gewId
     */
    public int getGewId() {
        return gewId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gewId  the gewId to set
     */
    public void setGewId(final int gewId) {
        this.gewId = gewId;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ba_cd
     */
    public String getBa_cd() {
        return ba_cd;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ba_cd  the ba_cd to set
     */
    public void setBa_cd(final String ba_cd) {
        this.ba_cd = ba_cd;
    }
}
