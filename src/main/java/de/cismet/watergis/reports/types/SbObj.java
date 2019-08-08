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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SbObj {

    //~ Instance fields --------------------------------------------------------

    private String art;
    private String owner;
    private String gewName;
    private String gu;
    private int widmung;
    private int id;
    private String baCd;
    private double from;
    private double till;
    private Integer sb;
    private String sbName;
    private String guName;
    private Double baLen;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GmdPartObj object.
     *
     * @param  id       DOCUMENT ME!
     * @param  art      DOCUMENT ME!
     * @param  owner    DOCUMENT ME!
     * @param  gewName  DOCUMENT ME!
     * @param  gu       DOCUMENT ME!
     * @param  widmung  DOCUMENT ME!
     * @param  baCd     DOCUMENT ME!
     * @param  from     DOCUMENT ME!
     * @param  till     DOCUMENT ME!
     * @param  sb       nr_re DOCUMENT ME!
     * @param  sbName   nr_li DOCUMENT ME!
     * @param  baLen    DOCUMENT ME!
     * @param  guName   DOCUMENT ME!
     */
    public SbObj(final int id,
            final String art,
            final String owner,
            final String gewName,
            final String gu,
            final int widmung,
            final String baCd,
            final double from,
            final double till,
            final Integer sb,
            final String sbName,
            final Double baLen,
            final String guName) {
        this.id = id;
        this.art = art;
        this.owner = owner;
        this.gewName = gewName;
        this.gu = gu;
        this.widmung = widmung;
        this.baCd = baCd;
        this.from = from;
        this.till = till;
        this.sb = sb;
        this.sbName = sbName;
        this.guName = guName;
        this.baLen = baLen;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   gew   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isInGewPart(final int gew, final double from, final double till) {
        return (gew == id)
                    && ((Math.min(Math.max(this.from, this.till), Math.max(from, till))
                            - Math.max(Math.min(this.from, this.till), Math.min(from, till))) > 0.1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthInGewPart(final int gew, final double from, final double till) {
        if (gew == id) {
            return Math.max(
                    0.0,
                    Math.min(Math.max(this.from, this.till), Math.max(from, till))
                            - Math.max(Math.min(this.from, this.till), Math.min(from, till)));
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLength() {
        return Math.abs(till - from);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the art
     */
    public String getArt() {
        return art;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  art  the art to set
     */
    public void setArt(final String art) {
        this.art = art;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the baCd
     */
    public String getBaCd() {
        return baCd;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  baCd  the baCd to set
     */
    public void setBaCd(final String baCd) {
        this.baCd = baCd;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the from
     */
    public double getFrom() {
        return Math.min(from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  from  the from to set
     */
    public void setFrom(final double from) {
        this.from = from;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the till
     */
    public double getTill() {
        return Math.max(from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  till  the till to set
     */
    public void setTill(final double till) {
        this.till = till;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the id
     */
    public int getId() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id  the id to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the owner
     */
    public String getGuName() {
        return guName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  owner  the owner to set
     */
    public void setOwner(final String owner) {
        this.owner = owner;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the gu
     */
    public String getGu() {
        return gu;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gu  the gu to set
     */
    public void setGu(final String gu) {
        this.gu = gu;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  guName  gu the gu to set
     */
    public void setGuName(final String guName) {
        this.guName = guName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the gewName
     */
    public String getGewName() {
        return gewName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gewName  the gewName to set
     */
    public void setGewName(final String gewName) {
        this.gewName = gewName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the widmung
     */
    public int getWidmung() {
        return widmung;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  widmung  the widmung to set
     */
    public void setWidmung(final int widmung) {
        this.widmung = widmung;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the sb
     */
    public Integer getSb() {
        return sb;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sb  the sb to set
     */
    public void setSb(final Integer sb) {
        this.sb = sb;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the sbName
     */
    public String getSbName() {
        return sbName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sbName  the sbName to set
     */
    public void setSbName(final String sbName) {
        this.sbName = sbName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the baLen
     */
    public Double getBaLen() {
        return baLen;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  baLen  the baLen to set
     */
    public void setBaLen(final Double baLen) {
        this.baLen = baLen;
    }
}
