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

import org.apache.log4j.Logger;

import java.lang.reflect.Field;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SbPartObjOffen {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SbPartObjOffen.class);

    //~ Instance fields --------------------------------------------------------

    private Integer id;
    private String owner;
    private String gu;
    private Integer wdm;
    private String gewName;
    private String baCd;
    private Double baStVon;
    private Double baStBis;
    private String nrRe;
    private String nrLi;
    private String profil;
    private Double wkFgLength;
    private Double bvRe;
    private Double blNLi;
    private Double flBRe;
    private Double blNRe;
    private Double flB;
    private Double flBtRe;
    private Double flN;
    private Double flGer;
    private Double brGewRe;
    private Double flBnRe;
    private Double bhRe;
    private String wbbl;
    private Double flQsGer;
    private Double flBn;
    private String typ;
    private Double blLi;
    private Double flBnLi;
    private Double bhLi;
    private Double blTLi;
    private String bemerkung;
    private Double brGew;
    private Integer ausbaujahr;
    private Double flSo;
    private Double brGewLi;
    private Double hoA;
    private Double flGew;
    private Double flQsGew;
    private Double brSo;
    private Double hoE;
    private Double flBt;
    private Double mw;
    private Double flBtLi;
    private Double gefaelle;
    private Double blTRe;
    private Double bvLi;
    private Double flBLi;
    private Double blRe;
    private String objNr;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GmdPartObjOffen object.
     *
     * @param  id          DOCUMENT ME!
     * @param  owner       DOCUMENT ME!
     * @param  gu          DOCUMENT ME!
     * @param  wdm         DOCUMENT ME!
     * @param  gewName     DOCUMENT ME!
     * @param  baCd        DOCUMENT ME!
     * @param  baStVon     DOCUMENT ME!
     * @param  baStBis     DOCUMENT ME!
     * @param  nrRe        DOCUMENT ME!
     * @param  nrLi        DOCUMENT ME!
     * @param  profil      DOCUMENT ME!
     * @param  wkFgLength  DOCUMENT ME!
     * @param  bvRe        DOCUMENT ME!
     * @param  blNLi       DOCUMENT ME!
     * @param  flBRe       DOCUMENT ME!
     * @param  blNRe       DOCUMENT ME!
     * @param  flB         DOCUMENT ME!
     * @param  flBtRe      DOCUMENT ME!
     * @param  flN         DOCUMENT ME!
     * @param  flGer       DOCUMENT ME!
     * @param  brGewRe     DOCUMENT ME!
     * @param  flBnRe      DOCUMENT ME!
     * @param  bhRe        DOCUMENT ME!
     * @param  wbbl        DOCUMENT ME!
     * @param  flQsGer     DOCUMENT ME!
     * @param  flBn        DOCUMENT ME!
     * @param  typ         DOCUMENT ME!
     * @param  blLi        DOCUMENT ME!
     * @param  flBnLi      DOCUMENT ME!
     * @param  bhLi        DOCUMENT ME!
     * @param  blTLi       DOCUMENT ME!
     * @param  bemerkung   DOCUMENT ME!
     * @param  brGew       DOCUMENT ME!
     * @param  ausbaujahr  DOCUMENT ME!
     * @param  flSo        DOCUMENT ME!
     * @param  brGewLi     DOCUMENT ME!
     * @param  hoA         DOCUMENT ME!
     * @param  flGew       DOCUMENT ME!
     * @param  flQsGew     DOCUMENT ME!
     * @param  brSo        DOCUMENT ME!
     * @param  hoE         DOCUMENT ME!
     * @param  flBt        DOCUMENT ME!
     * @param  mw          DOCUMENT ME!
     * @param  flBtLi      DOCUMENT ME!
     * @param  gefaelle    DOCUMENT ME!
     * @param  blTRe       DOCUMENT ME!
     * @param  bvLi        DOCUMENT ME!
     * @param  flBLi       DOCUMENT ME!
     * @param  blRe        DOCUMENT ME!
     * @param  objNr       DOCUMENT ME!
     */
    public SbPartObjOffen(final Integer id,
            final String owner,
            final String gu,
            final Integer wdm,
            final String gewName,
            final String baCd,
            final Double baStVon,
            final Double baStBis,
            final String nrRe,
            final String nrLi,
            final String profil,
            final Double wkFgLength,
            final Double bvRe,
            final Double blNLi,
            final Double flBRe,
            final Double blNRe,
            final Double flB,
            final Double flBtRe,
            final Double flN,
            final Double flGer,
            final Double brGewRe,
            final Double flBnRe,
            final Double bhRe,
            final String wbbl,
            final Double flQsGer,
            final Double flBn,
            final String typ,
            final Double blLi,
            final Double flBnLi,
            final Double bhLi,
            final Double blTLi,
            final String bemerkung,
            final Double brGew,
            final Integer ausbaujahr,
            final Double flSo,
            final Double brGewLi,
            final Double hoA,
            final Double flGew,
            final Double flQsGew,
            final Double brSo,
            final Double hoE,
            final Double flBt,
            final Double mw,
            final Double flBtLi,
            final Double gefaelle,
            final Double blTRe,
            final Double bvLi,
            final Double flBLi,
            final Double blRe,
            final String objNr) {
        this.id = id;
        this.owner = owner;
        this.gu = gu;
        this.wdm = wdm;
        this.gewName = gewName;
        this.baCd = baCd;
        this.baStVon = baStVon;
        this.baStBis = baStBis;
        this.nrRe = nrRe;
        this.nrLi = nrLi;
        this.profil = profil;
        this.wkFgLength = wkFgLength;
        this.bvRe = bvRe;
        this.blNLi = blNLi;
        this.flBRe = flBRe;
        this.blNRe = blNRe;
        this.flB = flB;
        this.flBtRe = flBtRe;
        this.flN = flN;
        this.flGer = flGer;
        this.brGewRe = brGewRe;
        this.flBnRe = flBnRe;
        this.bhRe = bhRe;
        this.wbbl = wbbl;
        this.flQsGer = flQsGer;
        this.flBn = flBn;
        this.typ = typ;
        this.blLi = blLi;
        this.flBnLi = flBnLi;
        this.bhLi = bhLi;
        this.blTLi = blTLi;
        this.bemerkung = bemerkung;
        this.brGew = brGew;
        this.ausbaujahr = ausbaujahr;
        this.flSo = flSo;
        this.brGewLi = brGewLi;
        this.hoA = hoA;
        this.flGew = flGew;
        this.flQsGew = flQsGew;
        this.brSo = brSo;
        this.hoE = hoE;
        this.flBt = flBt;
        this.mw = mw;
        this.flBtLi = flBtLi;
        this.gefaelle = gefaelle;
        this.blTRe = blTRe;
        this.bvLi = bvLi;
        this.flBLi = flBLi;
        this.blRe = blRe;
        this.objNr = objNr;
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
    public boolean isInGewPart(final Integer gew, final Double from, final Double till) {
        return ((gew != null) && gew.equals(id))
                    && ((Math.min(Math.max(baStVon, baStBis), Math.max(from, till))
                            - Math.max(Math.min(baStVon, baStBis), Math.min(from, till))) > 0.1);
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
    public Double getLengthInGewPart(final Integer gew, final Double from, final Double till) {
        if ((gew != null) && gew.equals(id)) {
            return Math.max(
                    0.0,
                    Math.min(Math.max(baStVon, baStBis), Math.max(from, till))
                            - Math.max(Math.min(baStVon, baStBis), Math.min(from, till)));
        } else {
            return 0.0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Double getLength() {
        return Math.abs(baStBis - baStVon);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id  the id to set
     */
    public void setId(final Integer id) {
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
     * @return  the wdm
     */
    public Integer getWdm() {
        return wdm;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wdm  the wdm to set
     */
    public void setWdm(final Integer wdm) {
        this.wdm = wdm;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the gew_name
     */
    public String getGewName() {
        return gewName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gewName  the gew_name to set
     */
    public void setGewName(final String gewName) {
        this.gewName = gewName;
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
     * @return  the baStVon
     */
    public Double getBaStVon() {
        return baStVon;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  baStVon  the baStVon to set
     */
    public void setBaStVon(final Double baStVon) {
        this.baStVon = baStVon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the baStBis
     */
    public Double getBaStBis() {
        return baStBis;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  baStBis  the baStBis to set
     */
    public void setBaStBis(final Double baStBis) {
        this.baStBis = baStBis;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the nrRe
     */
    public String getNrRe() {
        return nrRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nrRe  the nrRe to set
     */
    public void setNrRe(final String nrRe) {
        this.nrRe = nrRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the nrLi
     */
    public String getNrLi() {
        return nrLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nrLi  the nrLi to set
     */
    public void setNrLi(final String nrLi) {
        this.nrLi = nrLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the profil
     */
    public String getProfil() {
        return profil;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  profil  the profil to set
     */
    public void setProfil(final String profil) {
        this.profil = profil;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the wkFgLength
     */
    public Double getWkFgLength() {
        return wkFgLength;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wkFgLength  the wkFgLength to set
     */
    public void setWkFgLength(final Double wkFgLength) {
        this.wkFgLength = wkFgLength;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the bvRe
     */
    public Double getBvRe() {
        return bvRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bvRe  the bvRe to set
     */
    public void setBvRe(final Double bvRe) {
        this.bvRe = bvRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the blNLi
     */
    public Double getBlNLi() {
        return blNLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  blNLi  the blNLi to set
     */
    public void setBlNLi(final Double blNLi) {
        this.blNLi = blNLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flBRe
     */
    public Double getFlBRe() {
        return flBRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flBRe  the flBRe to set
     */
    public void setFlBRe(final Double flBRe) {
        this.flBRe = flBRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the blNRe
     */
    public Double getBlNRe() {
        return blNRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  blNRe  the blNRe to set
     */
    public void setBlNRe(final Double blNRe) {
        this.blNRe = blNRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flB
     */
    public Double getFlB() {
        return flB;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flB  the flB to set
     */
    public void setFlB(final Double flB) {
        this.flB = flB;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flBtRe
     */
    public Double getFlBtRe() {
        return flBtRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flBtRe  the flBtRe to set
     */
    public void setFlBtRe(final Double flBtRe) {
        this.flBtRe = flBtRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flN
     */
    public Double getFlN() {
        return flN;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flN  the flN to set
     */
    public void setFlN(final Double flN) {
        this.flN = flN;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flGer
     */
    public Double getFlGer() {
        return flGer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flGer  the flGer to set
     */
    public void setFlGer(final Double flGer) {
        this.flGer = flGer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the brGewRe
     */
    public Double getBrGewRe() {
        return brGewRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  brGewRe  the brGewRe to set
     */
    public void setBrGewRe(final Double brGewRe) {
        this.brGewRe = brGewRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flBnRe
     */
    public Double getFlBnRe() {
        return flBnRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flBnRe  the flBnRe to set
     */
    public void setFlBnRe(final Double flBnRe) {
        this.flBnRe = flBnRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the bhRe
     */
    public Double getBhRe() {
        return bhRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bhRe  the bhRe to set
     */
    public void setBhRe(final Double bhRe) {
        this.bhRe = bhRe;
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
     * @param  wbbl  the wbbl to set
     */
    public void setWbbl(final String wbbl) {
        this.wbbl = wbbl;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flQsGer
     */
    public Double getFlQsGer() {
        return flQsGer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flQsGer  the flQsGer to set
     */
    public void setFlQsGer(final Double flQsGer) {
        this.flQsGer = flQsGer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flBn
     */
    public Double getFlBn() {
        return flBn;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flBn  the flBn to set
     */
    public void setFlBn(final Double flBn) {
        this.flBn = flBn;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the typ
     */
    public String getTyp() {
        return typ;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  typ  the typ to set
     */
    public void setTyp(final String typ) {
        this.typ = typ;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the blLi
     */
    public Double getBlLi() {
        return blLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  blLi  the blLi to set
     */
    public void setBlLi(final Double blLi) {
        this.blLi = blLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flBnLi
     */
    public Double getFlBnLi() {
        return flBnLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flBnLi  the flBnLi to set
     */
    public void setFlBnLi(final Double flBnLi) {
        this.flBnLi = flBnLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the bhLi
     */
    public Double getBhLi() {
        return bhLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bhLi  the bhLi to set
     */
    public void setBhLi(final Double bhLi) {
        this.bhLi = bhLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the blTLi
     */
    public Double getBlTLi() {
        return blTLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  blTLi  the blTLi to set
     */
    public void setBlTLi(final Double blTLi) {
        this.blTLi = blTLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the bemerkung
     */
    public String getBemerkung() {
        return bemerkung;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bemerkung  the bemerkung to set
     */
    public void setBemerkung(final String bemerkung) {
        this.bemerkung = bemerkung;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the brGew
     */
    public Double getBrGew() {
        return brGew;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  brGew  the brGew to set
     */
    public void setBrGew(final Double brGew) {
        this.brGew = brGew;
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
     * @param  ausbaujahr  the ausbaujahr to set
     */
    public void setAusbaujahr(final Integer ausbaujahr) {
        this.ausbaujahr = ausbaujahr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flSo
     */
    public Double getFlSo() {
        return flSo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flSo  the flSo to set
     */
    public void setFlSo(final Double flSo) {
        this.flSo = flSo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the brGewLi
     */
    public Double getBrGewLi() {
        return brGewLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  brGewLi  the brGewLi to set
     */
    public void setBrGewLi(final Double brGewLi) {
        this.brGewLi = brGewLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the hoA
     */
    public Double getHoA() {
        return hoA;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hoA  the hoA to set
     */
    public void setHoA(final Double hoA) {
        this.hoA = hoA;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flGew
     */
    public Double getFlGew() {
        return flGew;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flGew  the flGew to set
     */
    public void setFlGew(final Double flGew) {
        this.flGew = flGew;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flQsGew
     */
    public Double getFlQsGew() {
        return flQsGew;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flQsGew  the flQsGew to set
     */
    public void setFlQsGew(final Double flQsGew) {
        this.flQsGew = flQsGew;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the brSo
     */
    public Double getBrSo() {
        return brSo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  brSo  the brSo to set
     */
    public void setBrSo(final Double brSo) {
        this.brSo = brSo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the hoE
     */
    public Double getHoE() {
        return hoE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hoE  the hoE to set
     */
    public void setHoE(final Double hoE) {
        this.hoE = hoE;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flBt
     */
    public Double getFlBt() {
        return flBt;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flBt  the flBt to set
     */
    public void setFlBt(final Double flBt) {
        this.flBt = flBt;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the mw
     */
    public Double getMw() {
        return mw;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mw  the mw to set
     */
    public void setMw(final Double mw) {
        this.mw = mw;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flBtLi
     */
    public Double getFlBtLi() {
        return flBtLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flBtLi  the flBtLi to set
     */
    public void setFlBtLi(final Double flBtLi) {
        this.flBtLi = flBtLi;
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
     * @param  gefaelle  the gefaelle to set
     */
    public void setGefaelle(final Double gefaelle) {
        this.gefaelle = gefaelle;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the blTRe
     */
    public Double getBlTRe() {
        return blTRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  blTRe  the blTRe to set
     */
    public void setBlTRe(final Double blTRe) {
        this.blTRe = blTRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the bvLi
     */
    public Double getBvLi() {
        return bvLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bvLi  the bvLi to set
     */
    public void setBvLi(final Double bvLi) {
        this.bvLi = bvLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the flBLi
     */
    public Double getFlBLi() {
        return flBLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  flBLi  the flBLi to set
     */
    public void setFlBLi(final Double flBLi) {
        this.flBLi = flBLi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the blRe
     */
    public Double getBlRe() {
        return blRe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  blRe  the blRe to set
     */
    public void setBlRe(final Double blRe) {
        this.blRe = blRe;
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
     * @param  objNr  the objNr to set
     */
    public void setObjNr(final String objNr) {
        this.objNr = objNr;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double get(final String name) {
        try {
            final Field f = this.getClass().getDeclaredField(name);
            final Double d = (Double)f.get(this);

            return ((d == null) ? 0.0 : d.doubleValue());
        } catch (Exception e) {
            LOG.error("Unknown field " + name, e);
            return 0.0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public KatasterGewObj toKatasterGewObj() {
        return new KatasterGewObj(id, "p", owner, gewName, gu, wdm, baCd, baStVon, baStBis);
    }
}
