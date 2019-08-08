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
public class GmdPartObjGeschl implements Comparable<GmdPartObjGeschl> {

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
    private Integer nr_re;
    private Integer nr_li;
    private Double tf;
    private Double dim;

    private final String ls;
    private final String prof;
    private final String ma;
    private final String objNr;
    private final String tr;
    private final String trGu;
    private final String objNrGu;
    private final Integer ausbaujahr;
    private final String wbbl;
    private final String code;
    private final String zustKl;
    private final Double br;
    private final Double brOben;
    private final Double hoehe;
    private final Double hEin;
    private final Double hAus;
    private final Double gefaelle;
    private final Double dhAus;
    private final Double dhEin;
    private final Double hAb;
    private final Double hAuf;
    private final Double aufstieg;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GmdPartObj object.
     *
     * @param  id          DOCUMENT ME!
     * @param  art         DOCUMENT ME!
     * @param  owner       DOCUMENT ME!
     * @param  gewName     DOCUMENT ME!
     * @param  gu          DOCUMENT ME!
     * @param  widmung     DOCUMENT ME!
     * @param  baCd        DOCUMENT ME!
     * @param  from        DOCUMENT ME!
     * @param  till        DOCUMENT ME!
     * @param  nr_re       DOCUMENT ME!
     * @param  nr_li       DOCUMENT ME!
     * @param  tf          DOCUMENT ME!
     * @param  dim         DOCUMENT ME!
     * @param  ls          DOCUMENT ME!
     * @param  prof        DOCUMENT ME!
     * @param  ma          DOCUMENT ME!
     * @param  objNr       DOCUMENT ME!
     * @param  tr          DOCUMENT ME!
     * @param  ausbaujahr  DOCUMENT ME!
     * @param  wbbl        DOCUMENT ME!
     * @param  code        DOCUMENT ME!
     * @param  zustKl      DOCUMENT ME!
     * @param  br          DOCUMENT ME!
     * @param  brOben      DOCUMENT ME!
     * @param  hoehe       DOCUMENT ME!
     * @param  hEin        DOCUMENT ME!
     * @param  hAus        DOCUMENT ME!
     * @param  gefaelle    DOCUMENT ME!
     * @param  dhAus       DOCUMENT ME!
     * @param  dhEin       DOCUMENT ME!
     * @param  hAb         DOCUMENT ME!
     * @param  hAuf        DOCUMENT ME!
     * @param  aufstieg    DOCUMENT ME!
     * @param  trGu        DOCUMENT ME!
     * @param  objNrGu     DOCUMENT ME!
     */
    public GmdPartObjGeschl(final int id,
            final String art,
            final String owner,
            final String gewName,
            final String gu,
            final int widmung,
            final String baCd,
            final double from,
            final double till,
            final Integer nr_re,
            final Integer nr_li,
            final Double tf,
            final Double dim,
            final String ls,
            final String prof,
            final String ma,
            final String objNr,
            final String tr,
            final Integer ausbaujahr,
            final String wbbl,
            final String code,
            final String zustKl,
            final Double br,
            final Double brOben,
            final Double hoehe,
            final Double hEin,
            final Double hAus,
            final Double gefaelle,
            final Double dhAus,
            final Double dhEin,
            final Double hAb,
            final Double hAuf,
            final Double aufstieg,
            final String trGu,
            final String objNrGu) {
        this.id = id;
        this.art = art;
        this.owner = owner;
        this.gewName = gewName;
        this.gu = gu;
        this.widmung = widmung;
        this.baCd = baCd;
        this.from = from;
        this.till = till;
        this.nr_re = nr_re;
        this.nr_li = nr_li;
        this.tf = tf;
        this.dim = dim;
        this.ls = ls;
        this.prof = prof;
        this.ma = ma;
        this.objNr = objNr;
        this.tr = tr;
        this.ausbaujahr = ausbaujahr;
        this.wbbl = wbbl;
        this.code = code;
        this.zustKl = zustKl;
        this.br = br;
        this.brOben = brOben;
        this.hoehe = hoehe;
        this.hEin = hEin;
        this.hAus = hAus;
        this.gefaelle = gefaelle;
        this.dhAus = dhAus;
        this.dhEin = dhEin;
        this.hAb = hAb;
        this.hAuf = hAuf;
        this.aufstieg = aufstieg;
        this.trGu = trGu;
        this.objNrGu = objNrGu;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the trGu
     */
    public String getTrGu() {
        return trGu;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the objNrGu
     */
    public String getObjNrGu() {
        return objNrGu;
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
     * @return  the nr_re
     */
    public Integer getNr_re() {
        return nr_re;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nr_re  the nr_re to set
     */
    public void setNr_re(final Integer nr_re) {
        this.nr_re = nr_re;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the nr_li
     */
    public Integer getNr_li() {
        return nr_li;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nr_li  the nr_li to set
     */
    public void setNr_li(final Integer nr_li) {
        this.nr_li = nr_li;
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
    public String getOwner() {
        return owner;
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
    public Integer getWidmung() {
        return widmung;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  widmung  the widmung to set
     */
    public void setWidmung(final Integer widmung) {
        this.widmung = widmung;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the tf
     */
    public Double getTf() {
        return tf;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tf  the tf to set
     */
    public void setTf(final Double tf) {
        this.tf = tf;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the dim
     */
    public Double getDim() {
        return dim;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dim  the dim to set
     */
    public void setDim(final Double dim) {
        this.dim = dim;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ls
     */
    public String getLs() {
        return ls;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the prof
     */
    public String getProf() {
        return prof;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ma
     */
    public String getMa() {
        return ma;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the objNr
     */
    public String getObjNr() {
        return objNr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the tr
     */
    public String getTr() {
        return tr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ausbaujahr
     */
    public Integer getAusbaujahr() {
        return ausbaujahr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the wbbl
     */
    public String getWbbl() {
        return wbbl;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the code
     */
    public String getCode() {
        return code;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the zustKl
     */
    public String getZustKl() {
        return zustKl;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the br
     */
    public Double getBr() {
        return br;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the brOben
     */
    public Double getBrOben() {
        return brOben;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the hoehe
     */
    public Double getHoehe() {
        return hoehe;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the hEin
     */
    public Double gethEin() {
        return hEin;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the hAus
     */
    public Double gethAus() {
        return hAus;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the gefaelle
     */
    public Double getGefaelle() {
        return gefaelle;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the dhAus
     */
    public Double getDhAus() {
        return dhAus;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the dhEin
     */
    public Double getDhEin() {
        return dhEin;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the hAb
     */
    public Double gethAb() {
        return hAb;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the hAuf
     */
    public Double gethAuf() {
        return hAuf;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the aufstieg
     */
    public Double getAufstieg() {
        return aufstieg;
    }

    @Override
    public int compareTo(final GmdPartObjGeschl o) {
        return ((Integer)id).compareTo(o.getId());
    }
}
