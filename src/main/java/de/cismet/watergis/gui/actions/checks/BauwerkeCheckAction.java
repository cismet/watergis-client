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
package de.cismet.watergis.gui.actions.checks;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.custom.helper.SQLFormatter;
import de.cismet.cids.custom.watergis.server.search.FgBakCount;
import de.cismet.cids.custom.watergis.server.search.MergeBaAnll;
import de.cismet.cids.custom.watergis.server.search.MergeBaAnlp;
import de.cismet.cids.custom.watergis.server.search.MergeBaD;
import de.cismet.cids.custom.watergis.server.search.MergeBaDue;
import de.cismet.cids.custom.watergis.server.search.MergeBaEa;
import de.cismet.cids.custom.watergis.server.search.MergeBaKr;
import de.cismet.cids.custom.watergis.server.search.MergeBaRl;
import de.cismet.cids.custom.watergis.server.search.MergeBaScha;
import de.cismet.cids.custom.watergis.server.search.MergeBaSchw;
import de.cismet.cids.custom.watergis.server.search.MergeBaWehr;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.featureservice.H2FeatureService;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitDialog;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

import static de.cismet.watergis.gui.actions.checks.AbstractCheckAction.LOG;

/**
 * Issue 241.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class BauwerkeCheckAction extends AbstractCheckAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final MetaClass FG_BAK = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak");
    private static final MetaClass FG_BA_RL = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_rl");
    private static final MetaClass FG_BA_D = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_d");
    private static final MetaClass FG_BA_DUE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_due");
    private static final MetaClass FG_BA_ANLL = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_anll");
    private static final MetaClass FG_BA_ANLP = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_anlp");
    private static final MetaClass FG_BA_KR = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_kr");
    private static final MetaClass FG_BA_EA = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_ea");
    private static final MetaClass FG_BA_SCHA = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_scha");
    private static final MetaClass FG_BA_WEHR = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_wehr");
    private static final MetaClass FG_BA_SCHW = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_schw");
    private static String QUERY_RL_HOLE;
    private static String QUERY_D_HOLE;
    private static String QUERY_DUE_HOLE;
    private static String QUERY_RL_OVERLAPS;
    private static String QUERY_D_OVERLAPS;
    private static String QUERY_DUE_OVERLAPS;
    private static String QUERY_DUE_ATTR;
    private static String QUERY_D_ATTR;
    private static String QUERY_RL_ATTR;
    private static String QUERY_ANLL_ATTR;
    private static String QUERY_ANLP_ATTR;
    private static String QUERY_KR_ATTR;
    private static String QUERY_EA_ATTR;
    private static String QUERY_SCHA_ATTR;
    private static String QUERY_WEHR_ATTR;
    private static String QUERY_SCHW_ATTR;
    private static String QUERY_SCHA_DISTANCE;
    private static String QUERY_SCHW_DISTANCE;
    private static String QUERY_WEHR_DISTANCE;
    private static String QUERY_KR_DISTANCE;
    private static String QUERY_EA_DISTANCE;
    private static String QUERY_SCHA_OFFEN;
//    private static String QUERY_WEHR_GESCHL;
    private static String QUERY_ANLP_OFFEN;
    private static String QUERY_ANLP_GESCHL;
    private static String QUERY_ANLP_ESW;
    private static String QUERY_KR_ESW;
//    private static String QUERY_EA_GESCHL;
    private static String QUERY_EA_ESW;
    private static String QUERY_ANLL_GESCHL;
    private static String QUERY_KR_MARKED_TWICE;
    private static final String CHECKS_BAUWERKE_KR_KR_DOPPELTE__MARKIERUNG =
        "Prüfungen->Bauwerke->Kr->Kr: doppelte Markierung";
    private static final String CHECKS_BAUWERKE_ANLL_ANLL_AUF_GESCHLOSSEN =
        "Prüfungen->Bauwerke->Anll->Anll: auf geschlossenem Gerinne";
    private static final String CHECKS_BAUWERKE_EA_EA__ESW_FUER_GESCHLOSSEN =
        "Prüfungen->Bauwerke->Ea->Ea: Esw für geschlossenes Gerinne";
    private static final String CHECKS_BAUWERKE_EA_EA_AUF_GESCHLOSSENEM_G =
        "Prüfungen->Bauwerke->Ea->Ea: auf geschlossenem Gerinne";
    private static final String CHECKS_BAUWERKE_KR_KR__ESW_FUER_GESCHLOSSEN =
        "Prüfungen->Bauwerke->Kr->Kr: Esw für geschlossenes Gerinne";
    private static final String CHECKS_BAUWERKE_ANLP_ANLP__ESW_FUER_GESCHLO =
        "Prüfungen->Bauwerke->Anlp->Anlp: Esw für geschlossenes Gerinne";
    private static final String CHECKS_BAUWERKE_ANLP_ANLP_AUF_GESCHLOSSEN =
        "Prüfungen->Bauwerke->Anlp->Anlp: auf geschlossenem Gerinne";
    private static final String CHECKS_BAUWERKE_ANLP_ANLP_AUF_OFFENEM__GER =
        "Prüfungen->Bauwerke->Anlp->Anlp: auf offenem Gerinne";
    private static final String CHECKS_BAUWERKE_WEHR_WEHR_AUF_GESCHLOSSEN =
        "Prüfungen->Bauwerke->Wehr->Wehr: auf geschlossenem Gerinne";
    private static final String CHECKS_BAUWERKE_SCHA_SCHA_AUF_OFFENEM__GER =
        "Prüfungen->Bauwerke->Scha->Scha: auf offenem Gerinne";
    private static final String CHECKS_BAUWERKE_EA_EA_DOPPELTZU_NAH = "Prüfungen->Bauwerke->Ea->Ea: doppelt/zu nah";
    private static final String CHECKS_BAUWERKE_KR_KR_DOPPELTZU_NAH = "Prüfungen->Bauwerke->Kr->Kr: doppelt/zu nah";
    private static final String CHECKS_BAUWERKE_SCHW_SCHW_DOPPELTZU_NAH =
        "Prüfungen->Bauwerke->Schw->Schw: doppelt/zu nah";
    private static final String CHECKS_BAUWERKE_WEHR_WEHR_DOPPELTZU_NAH =
        "Prüfungen->Bauwerke->Wehr->Wehr: doppelt/zu nah";
    private static final String CHECKS_BAUWERKE_SCHA_SCHA_DOPPELTZU_NAH =
        "Prüfungen->Bauwerke->Scha->Scha: doppelt/zu nah";
    private static final String CHECKS_BAUWERKE_DUE_DUE__UEBERLAPPUNG = "Prüfungen->Bauwerke->Due->Due: Überlappung";
    private static final String CHECKS_BAUWERKE_DD__UEBERLAPPUNG = "Prüfungen->Bauwerke->D->D: Überlappung";
    private static final String CHECKS_BAUWERKE_RL_RL__UEBERLAPPUNG = "Prüfungen->Bauwerke->Rl->Rl: Überlappung";
    private static final String CHECKS_BAUWERKE_DUE_DUE__LUECKE = "Prüfungen->Bauwerke->Due->Due: Lücke";
    private static final String CHECKS_BAUWERKE_DD__LUECKE = "Prüfungen->Bauwerke->D->D: Lücke";
    private static final String CHECKS_BAUWERKE_RL_RL__LUECKE = "Prüfungen->Bauwerke->Rl->Rl: Lücke";
    private static final String CHECKS_BAUWERKE_DD__ATTRIBUTE = "Prüfungen->Bauwerke->D->D: Attribute";
    private static final String CHECKS_BAUWERKE_WEHR_WEHR__ATTRIBUTE = "Prüfungen->Bauwerke->Wehr->Wehr: Attribute";
    private static final String CHECKS_BAUWERKE_SCHW_SCHW__ATTRIBUTE = "Prüfungen->Bauwerke->Schw->Schw: Attribute";
    private static final String CHECKS_BAUWERKE_SCHA_SCHA__ATTRIBUTE = "Prüfungen->Bauwerke->Scha->Scha: Attribute";
    private static final String CHECKS_BAUWERKE_RL_RL__ATTRIBUTE = "Prüfungen->Bauwerke->Rl->Rl: Attribute";
    private static final String CHECKS_BAUWERKE_KR_KR__ATTRIBUTE = "Prüfungen->Bauwerke->Kr->Kr: Attribute";
    private static final String CHECKS_BAUWERKE_EA_EA__ATTRIBUTE = "Prüfungen->Bauwerke->Ea->Ea: Attribute";
    private static final String CHECKS_BAUWERKE_DUE_DUE__ATTRIBUTE = "Prüfungen->Bauwerke->Dü->Dü: Attribute";
    private static final String CHECKS_BAUWERKE_ANLP_ANLP__ATTRIBUTE = "Prüfungen->Bauwerke->Anlp->Anlp: Attribute";
    private static final String CHECKS_BAUWERKE_ANLL_ANLL__ATTRIBUTE = "Prüfungen->Bauwerke->Anll->Anll: Attribute";
    private static final String[] ALL_CHECKS = new String[] {
            CHECKS_BAUWERKE_ANLL_ANLL_AUF_GESCHLOSSEN,
            CHECKS_BAUWERKE_ANLL_ANLL__ATTRIBUTE,
            CHECKS_BAUWERKE_ANLP_ANLP_AUF_GESCHLOSSEN,
            CHECKS_BAUWERKE_ANLP_ANLP_AUF_OFFENEM__GER,
            CHECKS_BAUWERKE_ANLP_ANLP__ATTRIBUTE,
            CHECKS_BAUWERKE_ANLP_ANLP__ESW_FUER_GESCHLO,
            CHECKS_BAUWERKE_DD__ATTRIBUTE,
            CHECKS_BAUWERKE_DD__LUECKE,
            CHECKS_BAUWERKE_DD__UEBERLAPPUNG,
            CHECKS_BAUWERKE_DUE_DUE__LUECKE,
            CHECKS_BAUWERKE_DUE_DUE__UEBERLAPPUNG,
            CHECKS_BAUWERKE_DUE_DUE__ATTRIBUTE,
            CHECKS_BAUWERKE_EA_EA_AUF_GESCHLOSSENEM_G,
            CHECKS_BAUWERKE_EA_EA_DOPPELTZU_NAH,
            CHECKS_BAUWERKE_EA_EA__ATTRIBUTE,
            CHECKS_BAUWERKE_EA_EA__ESW_FUER_GESCHLOSSEN,
            CHECKS_BAUWERKE_KR_KR_DOPPELTE__MARKIERUNG,
            CHECKS_BAUWERKE_KR_KR_DOPPELTZU_NAH,
            CHECKS_BAUWERKE_KR_KR__ATTRIBUTE,
            CHECKS_BAUWERKE_KR_KR__ESW_FUER_GESCHLOSSEN,
            CHECKS_BAUWERKE_RL_RL__ATTRIBUTE,
            CHECKS_BAUWERKE_RL_RL__LUECKE,
            CHECKS_BAUWERKE_RL_RL__UEBERLAPPUNG,
            CHECKS_BAUWERKE_SCHA_SCHA_AUF_OFFENEM__GER,
            CHECKS_BAUWERKE_SCHA_SCHA_DOPPELTZU_NAH,
            CHECKS_BAUWERKE_SCHA_SCHA__ATTRIBUTE,
            CHECKS_BAUWERKE_SCHW_SCHW_DOPPELTZU_NAH,
            CHECKS_BAUWERKE_SCHW_SCHW__ATTRIBUTE,
            CHECKS_BAUWERKE_WEHR_WEHR_AUF_GESCHLOSSEN,
            CHECKS_BAUWERKE_WEHR_WEHR_DOPPELTZU_NAH,
            CHECKS_BAUWERKE_WEHR_WEHR__ATTRIBUTE
        };

    static {
        if ((FG_BAK != null) && (FG_BA_RL != null) && (FG_BA_D != null) && (FG_BA_DUE != null)
                    && (FG_BA_ANLL != null)
                    && (FG_BA_ANLP != null)
                    && (FG_BA_KR != null)
                    && (FG_BA_EA != null)
                    && (FG_BA_WEHR != null)
                    && (FG_BA_SCHA != null)
                    && (FG_BA_SCHW != null)) {
            final User user = SessionManager.getSession().getUser();

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_RL_HOLE = "select " + FG_BA_RL.getID() + ", d1." + FG_BA_RL.getPrimaryKey()
                            + "	from (select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_rl d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	) as d1,\n"
                            + "	(select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_rl d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and d1.route = d2.route and abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) > 0 and  abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) < 0.5;";
            } else {
                QUERY_RL_HOLE = "select " + FG_BA_RL.getID() + ", d1." + FG_BA_RL.getPrimaryKey()
                            + "	from (select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_rl d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + " join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + " left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " where (%1$s is null or von.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' \n"
                            + "	) as d1,\n"
                            + "	(select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_rl d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + " join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + " left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " where (%1$s is null or von.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' \n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and d1.route = d2.route and abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) > 0 and  abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) < 0.5;";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_RL_ATTR = "select " + FG_BA_RL.getID() + ", rl." + FG_BA_RL.getPrimaryKey()
                            + " from dlm25w.fg_ba_rl rl\n"
                            + "join dlm25w.fg_ba_linie linie on (rl.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_profil p on (p.id = rl.profil)\n"
                            + "where\n"
                            + "(rl.profil is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br_dm_li is not null and ((p.profil in ('kr', 'ei') and (br_dm_li <25 or br_dm_li > 4000)) or ( p.profil in ('re', 'tr') and (br_dm_li < 0.025 or br_dm_li > 4) )))\n"
                            + "or (ho_li is not null and ((p.profil in ('ei') and (ho_li <25 or ho_li > 4000)) or ( p.profil in ('re', 'tr') and (ho_li < 0.025 or ho_li > 4) )))\n"
                            + "or (br_tr_o_li is not null and (br_tr_o_li <0.025 or br_tr_o_li > 4))\n"
                            + "or (ho_e is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (ho_d_e is not null and (ho_d_e < 0 or ho_d_e > 10))\n"
                            + "or (ho_d_a is not null and (ho_d_a < 0 or ho_d_a > 10))\n"
                            + "or (ho_d_m is not null and (ho_d_m <= 0 or ho_d_m > 30))\n"
                            + "or abs(von.wert - bis.wert) < 0.5\n"
                            + "or (p.profil = 'kr' and (ho_li is not null or br_tr_o_li is not null))\n"
                            + "or (p.profil in ('ei', 're') and br_tr_o_li is not null)\n"
                            + "or (p.profil = 'tr' and (br_dm_li = br_tr_o_li))) and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_RL_ATTR = "select " + FG_BA_RL.getID() + ", rl." + FG_BA_RL.getPrimaryKey()
                            + " from dlm25w.fg_ba_rl rl\n"
                            + "join dlm25w.fg_ba_linie linie on (rl.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_profil p on (p.id = rl.profil)\n"
                            + "where\n"
                            + "(rl.profil is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br_dm_li is not null and ((p.profil in ('kr', 'ei') and (br_dm_li <25 or br_dm_li > 4000)) or ( p.profil in ('re', 'tr') and (br_dm_li < 0.025 or br_dm_li > 4) )))\n"
                            + "or (ho_li is not null and ((p.profil in ('ei') and (ho_li <25 or ho_li > 4000)) or ( p.profil in ('re', 'tr') and (ho_li < 0.025 or ho_li > 4) )))\n"
                            + "or (br_tr_o_li is not null and (br_tr_o_li <0.025 or br_tr_o_li > 4))\n"
                            + "or (ho_e is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (ho_d_e is not null and (ho_d_e < 0 or ho_d_e > 10))\n"
                            + "or (ho_d_a is not null and (ho_d_a < 0 or ho_d_a > 10))\n"
                            + "or (ho_d_m is not null and (ho_d_m <= 0 or ho_d_m > 30))\n"
                            + "or abs(von.wert - bis.wert) < 0.5\n"
                            + "or (p.profil = 'kr' and (ho_li is not null or br_tr_o_li is not null))\n"
                            + "or (p.profil in ('ei', 're') and br_tr_o_li is not null)\n"
                            + "or (p.profil = 'tr' and (br_dm_li = br_tr_o_li))) and (%1$s is null or ba.id = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_D_HOLE = "select " + FG_BA_D.getID() + ", d1." + FG_BA_D.getPrimaryKey()
                            + "	from (select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_d d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	) as d1,\n"
                            + "	(select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_d d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and d1.route = d2.route and abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) > 0 and  abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) < 0.5;";
            } else {
                QUERY_D_HOLE = "select " + FG_BA_D.getID() + ", d1." + FG_BA_D.getPrimaryKey()
                            + "	from (select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_d d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + " join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + " left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " where (%1$s is null or von.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' \n"
                            + "	) as d1,\n"
                            + "	(select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_d d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + " join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + " left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " where (%1$s is null or von.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' \n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and d1.route = d2.route and abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) > 0 and  abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) < 0.5;";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_D_ATTR = "select " + FG_BA_D.getID() + ", d." + FG_BA_D.getPrimaryKey()
                            + " from dlm25w.fg_ba_d d\n"
                            + "join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_profil p on (p.id = d.profil)\n"
                            + "where\n"
                            + "(d.profil is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br_dm_li is not null and ((p.profil in ('kr', 'ei') and (br_dm_li <25 or br_dm_li > 6000)) or ( p.profil in ('re', 'tr') and (br_dm_li < 0.025 or br_dm_li > 6) )))\n"
                            + "or (ho_li is not null and ((p.profil in ('ei') and (ho_li <25 or ho_li > 6000)) or ( p.profil in ('re', 'tr') and (ho_li < 0.025 or ho_li > 6) )))\n"
                            + "or (br_tr_o_li is not null and (br_tr_o_li <0.025 or br_tr_o_li > 6))\n"
                            + "or (ho_e is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (ho_d_e is not null and (ho_d_e < 0 or ho_d_e > 10))\n"
                            + "or (ho_d_a is not null and (ho_d_a < 0 or ho_d_a > 10))\n"
                            + "or (ho_d_m is not null and (ho_d_m <= 0 or ho_d_m > 30))\n"
                            + "or abs(von.wert - bis.wert) < 0.5\n"
                            + "or abs(von.wert - bis.wert) > 250\n"
                            + "or (p.profil = 'kr' and (ho_li is not null or br_tr_o_li is not null))\n"
                            + "or (p.profil in ('ei', 're') and br_tr_o_li is not null)\n"
                            + "or (p.profil = 'tr' and (br_dm_li = br_tr_o_li))) and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_D_ATTR = "select " + FG_BA_D.getID() + ", d." + FG_BA_D.getPrimaryKey()
                            + " from dlm25w.fg_ba_d d\n"
                            + "join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_profil p on (p.id = d.profil)\n"
                            + "where\n"
                            + "(d.profil is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br_dm_li is not null and ((p.profil in ('kr', 'ei') and (br_dm_li <25 or br_dm_li > 6000)) or ( p.profil in ('re', 'tr') and (br_dm_li < 0.025 or br_dm_li > 6) )))\n"
                            + "or (ho_li is not null and ((p.profil in ('ei') and (ho_li <25 or ho_li > 6000)) or ( p.profil in ('re', 'tr') and (ho_li < 0.025 or ho_li > 6) )))\n"
                            + "or (br_tr_o_li is not null and (br_tr_o_li <0.025 or br_tr_o_li > 6))\n"
                            + "or (ho_e is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (ho_d_e is not null and (ho_d_e < 0 or ho_d_e > 10))\n"
                            + "or (ho_d_a is not null and (ho_d_a < 0 or ho_d_a > 10))\n"
                            + "or (ho_d_m is not null and (ho_d_m <= 0 or ho_d_m > 30))\n"
                            + "or abs(von.wert - bis.wert) < 0.5\n"
                            + "or abs(von.wert - bis.wert) > 250\n"
                            + "or (p.profil = 'kr' and (ho_li is not null or br_tr_o_li is not null))\n"
                            + "or (p.profil in ('ei', 're') and br_tr_o_li is not null)\n"
                            + "or (p.profil = 'tr' and (br_dm_li = br_tr_o_li))) and (%1$s is null or ba.id = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_DUE_HOLE = "select " + FG_BA_DUE.getID() + ", d1." + FG_BA_DUE.getPrimaryKey()
                            + "	from (select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_due d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	) as d1,\n"
                            + "	(select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_due d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and d1.route = d2.route and abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) > 0 and  abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) < 0.5;";
            } else {
                QUERY_DUE_HOLE = "select " + FG_BA_DUE.getID() + ", d1." + FG_BA_DUE.getPrimaryKey()
                            + "	from (select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_due d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + " join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + " left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " where (%1$s is null or von.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' \n"
                            + "	) as d1,\n"
                            + "	(select von.wert as von, bis.wert as bis, von.route, d.id from \n"
                            + "	dlm25w.fg_ba_due d\n"
                            + "	join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + " join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + " left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " where (%1$s is null or von.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' \n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and d1.route = d2.route and abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) > 0 and  abs(least(d2.von, d2.bis) - greatest(d1.von, d1.bis)) < 0.5;";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_DUE_ATTR = "select " + FG_BA_DUE.getID() + ", d." + FG_BA_DUE.getPrimaryKey()
                            + " from dlm25w.fg_ba_due d\n"
                            + "join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_profil p on (p.id = d.profil)\n"
                            + "where\n"
                            + "(d.profil is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br_dm_li is not null and ((p.profil in ('kr', 'ei') and (br_dm_li <25 or br_dm_li > 6000)) or ( p.profil in ('re', 'tr') and (br_dm_li < 0.025 or br_dm_li > 6) )))\n"
                            + "or (ho_li is not null and ((p.profil in ('ei') and (ho_li <25 or ho_li > 6000)) or ( p.profil in ('re', 'tr') and (ho_li < 0.025 or ho_li > 6) )))\n"
                            + "or (br_tr_o_li is not null and (br_tr_o_li <0.025 or br_tr_o_li > 6))\n"
                            + "or (ho_e is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (ho_d_e is not null and (ho_d_e < 0 or ho_d_e > 10))\n"
                            + "or (ho_d_a is not null and (ho_d_a < 0 or ho_d_a > 10))\n"
                            + "or (ho_d_iab is not null and (ho_d_iab <= 0 or ho_d_iab > 30))\n"
                            + "or (ho_d_iauf is not null and (ho_d_iauf <= 0 or ho_d_iauf > 30))\n"
                            + "or (ho_d_m is not null and (ho_d_m <= 0 or ho_d_m > 30))\n"
                            + "or abs(von.wert - bis.wert) < 0.5\n"
                            + "or abs(von.wert - bis.wert) > 250\n"
                            + "or (p.profil = 'kr' and (ho_li is not null or br_tr_o_li is not null))\n"
                            + "or (p.profil in ('ei', 're') and br_tr_o_li is not null)\n"
                            + "or (p.profil = 'tr' and (br_dm_li = br_tr_o_li))) and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_DUE_ATTR = "select " + FG_BA_DUE.getID() + ", d." + FG_BA_DUE.getPrimaryKey()
                            + " from dlm25w.fg_ba_due d\n"
                            + "join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_profil p on (p.id = d.profil)\n"
                            + "where\n"
                            + "(d.profil is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br_dm_li is not null and ((p.profil in ('kr', 'ei') and (br_dm_li <25 or br_dm_li > 6000)) or ( p.profil in ('re', 'tr') and (br_dm_li < 0.025 or br_dm_li > 6) )))\n"
                            + "or (ho_li is not null and ((p.profil in ('ei') and (ho_li <25 or ho_li > 6000)) or ( p.profil in ('re', 'tr') and (ho_li < 0.025 or ho_li > 6) )))\n"
                            + "or (br_tr_o_li is not null and (br_tr_o_li <0.025 or br_tr_o_li > 6))\n"
                            + "or (ho_e is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (ho_d_e is not null and (ho_d_e < 0 or ho_d_e > 10))\n"
                            + "or (ho_d_a is not null and (ho_d_a < 0 or ho_d_a > 10))\n"
                            + "or (ho_d_iab is not null and (ho_d_iab <= 0 or ho_d_iab > 30))\n"
                            + "or (ho_d_iauf is not null and (ho_d_iauf <= 0 or ho_d_iauf > 30))\n"
                            + "or (ho_d_m is not null and (ho_d_m <= 0 or ho_d_m > 30))\n"
                            + "or abs(von.wert - bis.wert) < 0.5\n"
                            + "or abs(von.wert - bis.wert) > 250\n"
                            + "or (p.profil = 'kr' and (ho_li is not null or br_tr_o_li is not null))\n"
                            + "or (p.profil in ('ei', 're') and br_tr_o_li is not null)\n"
                            + "or (p.profil = 'tr' and (br_dm_li = br_tr_o_li))) and (%1$s is null or ba.id = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_ANLL_ATTR = "select " + FG_BA_ANLL.getID() + ", a." + FG_BA_ANLL.getPrimaryKey()
                            + " from dlm25w.fg_ba_anll a\n"
                            + "join dlm25w.fg_ba_linie linie on (a.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_anll an on (an.id = a.anll)\n"
                            + "where\n"
                            + "(a.anll is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (an.anll in ('See', 'Spei') and (abs(von.wert - bis.wert) < 5 or abs(von.wert - bis.wert) > 50000 ))\n"
                            + "or (an.anll in ('Drte', 'Faa', 'Rb') and (abs(von.wert - bis.wert) < 5 or abs(von.wert - bis.wert) > 200))\n"
                            + "or (an.anll in ('Ds', 'Sf', 'Si', 'Sleu','Tosb','WKA') and (abs(von.wert - bis.wert) < 1 or abs(von.wert - bis.wert) > 200))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_ANLL_ATTR = "select " + FG_BA_ANLL.getID() + ", a." + FG_BA_ANLL.getPrimaryKey()
                            + " from dlm25w.fg_ba_anll a\n"
                            + "join dlm25w.fg_ba_linie linie on (a.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_anll an on (an.id = a.anll)\n"
                            + "where\n"
                            + "(a.anll is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (an.anll in ('See', 'Spei') and (abs(von.wert - bis.wert) < 5 or abs(von.wert - bis.wert) > 50000 ))\n"
                            + "or (an.anll in ('Drte', 'Faa', 'Rb') and (abs(von.wert - bis.wert) < 5 or abs(von.wert - bis.wert) > 200))\n"
                            + "or (an.anll in ('Ds', 'Sf', 'Si', 'Sleu','Tosb','WKA') and (abs(von.wert - bis.wert) < 1 or abs(von.wert - bis.wert) > 200))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_ANLP_ATTR = "select " + FG_BA_ANLP.getID() + ", a." + FG_BA_ANLP.getPrimaryKey()
                            + " from dlm25w.fg_ba_anlp a\n"
                            + "join dlm25w.fg_ba_punkt von on (a.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_anlp an on (an.id = a.anlp)\n"
                            + "left join dlm25w.k_l_rl rl on (rl.id = a.l_rl)\n"
                            + "where\n"
                            + "(a.anlp is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (an.anlp not in ('Steg', 'Vt', 'Wee', 'Z') and (br < 0 or br > 10 ))\n"
                            + "or (an.anlp in ('Steg', 'Vt', 'Wee') and (br < 1 or br > 200))\n"
                            + "or (an.anlp in ('Albw', 'Elbw', 'Fu', 'Rsk', 'Schi', 'Slu', 'Stt') and (rl.l_rl <> 'mi'))\n"
                            + "or (an.anlp in ('P', 'P-Gr', 'P-Steg', 'P-Gr-Steg', 'P-Lat', 'Sta') and (rl.l_rl is not null and rl.l_rl not in ('re', 'li')))\n"
                            + "or (an.anlp in ('Steg', 'Vt', 'Wes') and (rl.l_rl is not null and rl.l_rl not in ('re', 'li', 'bs')))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_ANLP_ATTR = "select " + FG_BA_ANLP.getID() + ", a." + FG_BA_ANLP.getPrimaryKey()
                            + " from dlm25w.fg_ba_anlp a\n"
                            + "join dlm25w.fg_ba_punkt von on (a.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_anlp an on (an.id = a.anlp)\n"
                            + "left join dlm25w.k_l_rl rl on (rl.id = a.l_rl)\n"
                            + "where\n"
                            + "(a.anlp is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (an.anlp not in ('Steg', 'Vt', 'Wee', 'Z') and (br < 0 or br > 10 ))\n"
                            + "or (an.anlp in ('Steg', 'Vt', 'Wee') and (br < 1 or br > 200))\n"
                            + "or (an.anlp in ('Albw', 'Elbw', 'Fu', 'Rsk', 'Schi', 'Slu', 'Stt') and (rl.l_rl <> 'mi'))\n"
                            + "or (an.anlp in ('P', 'P-Gr', 'P-Steg', 'P-Gr-Steg', 'P-Lat', 'Sta') and (rl.l_rl is not null and rl.l_rl not in ('re', 'li')))\n"
                            + "or (an.anlp in ('Steg', 'Vt', 'Wes') and (rl.l_rl is not null and rl.l_rl not in ('re', 'li', 'bs')))\n"
                            + ") and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_KR_ATTR = "select " + FG_BA_KR.getID() + ", kr." + FG_BA_KR.getPrimaryKey()
                            + " from dlm25w.fg_ba_kr kr\n"
                            + "join dlm25w.fg_ba_punkt von on (kr.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_kr k on (k.id = kr.kr)\n"
                            + "left join dlm25w.k_l_oiu oi on (oi.id = kr.l_oiu)\n"
                            + "where\n"
                            + "(kr.kr is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br is not null and (br <= 0 or br > 100))\n"
                            + "or (k.kr in ('Br') and (oi.l_oiu <> 'o' ))\n"
                            + "or (k.kr in ('U') and (oi.l_oiu <> 'u' ))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_KR_ATTR = "select " + FG_BA_KR.getID() + ", kr." + FG_BA_KR.getPrimaryKey()
                            + " from dlm25w.fg_ba_kr kr\n"
                            + "join dlm25w.fg_ba_punkt von on (kr.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_kr k on (k.id = kr.kr)\n"
                            + "left join dlm25w.k_l_oiu oi on (oi.id = kr.l_oiu)\n"
                            + "where\n"
                            + "(kr.kr is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br is not null and (br <= 0 or br > 100))\n"
                            + "or (k.kr in ('Br') and (oi.l_oiu <> 'o' ))\n"
                            + "or (k.kr in ('U') and (oi.l_oiu <> 'u' ))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_EA_ATTR = "select " + FG_BA_EA.getID() + ", ea." + FG_BA_EA.getPrimaryKey()
                            + " from dlm25w.fg_ba_ea ea\n"
                            + "join dlm25w.fg_ba_punkt von on (ea.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where\n"
                            + "(ea.l_rl is null or ea.ea is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br is not null and (br <= 0 or br > 30))\n"
                            + "or (ho_ea  is not null and (ho_ea < -6 or ho_ea > 179))\n"
                            + "or (ho_d_ea is not null and (ho_d_ea < 0 or ho_d_ea > 15))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_EA_ATTR = "select " + FG_BA_EA.getID() + ", ea." + FG_BA_EA.getPrimaryKey()
                            + " from dlm25w.fg_ba_ea ea\n"
                            + "join dlm25w.fg_ba_punkt von on (ea.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where\n"
                            + "(ea.l_rl is null or ea.ea is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br is not null and (br <= 0 or br > 30))\n"
                            + "or (ho_ea  is not null and (ho_ea < -6 or ho_ea > 179))\n"
                            + "or (ho_d_ea is not null and (ho_d_ea < 0 or ho_d_ea > 15))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_SCHA_ATTR = "select " + FG_BA_SCHA.getID() + ", scha." + FG_BA_SCHA.getPrimaryKey()
                            + " from dlm25w.fg_ba_scha scha\n"
                            + "join dlm25w.fg_ba_punkt von on (scha.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where\n"
                            + "(scha.scha is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (ho_so  is not null and (ho_so < -6 or ho_so > 179))\n"
                            + "or (ho_d_so_ok is not null and (ho_d_so_ok <= 0 or ho_d_so_ok > 10))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_SCHA_ATTR = "select " + FG_BA_SCHA.getID() + ", scha." + FG_BA_SCHA.getPrimaryKey()
                            + " from dlm25w.fg_ba_scha scha\n"
                            + "join dlm25w.fg_ba_punkt von on (scha.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where\n"
                            + "(scha.scha is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (ho_so  is not null and (ho_so < -6 or ho_so > 179))\n"
                            + "or (ho_d_so_ok is not null and (ho_d_so_ok <= 0 or ho_d_so_ok > 10))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_WEHR_ATTR = "select " + FG_BA_WEHR.getID() + ", wehr." + FG_BA_WEHR.getPrimaryKey()
                            + " from dlm25w.fg_ba_wehr wehr\n"
                            + "join dlm25w.fg_ba_punkt von on (wehr.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_wehr w on (w.id = wehr.wehr)\n"
                            + "left join dlm25w.k_wehr_v wv on (wv.id = wehr.wehr_v)\n"
                            + "left join dlm25w.k_wehr_av wav on (wav.id = wehr.wehr_av)\n"
                            + "left join dlm25w.k_material m on (m.id = wehr.material_v)\n"
                            + "where\n"
                            + "(wehr.wehr is null or wehr.wehr_v is null or wehr.wehr_av is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 30))\n"
                            + "or (br_li  is not null and (br_li <= 0 or br_li > 100))\n"
                            + "or (ho_so  is not null and (ho_so < -6 or ho_so > 179))\n"
                            + "or (sz  is not null and (sz < -6 or sz > 179))\n"
                            + "or (az  is not null and (az < -6 or az > 179))\n"
                            + "or (sz is not null and az is not null and (sz <= az))\n"
                            + "or (sz is not null and ho_so is not null and (sz <= ho_so))\n"
                            + "or (az is not null and ho_so is not null and (az <= ho_so))\n"
                            + "or (w.wehr in ('S-Kbw', 'S-Sbw', 'S-Stw', 'S-Moe') and wv.wehr_v not in ('Bo', 'Bo-J', 'Schü'))\n"
                            + "or (w.wehr in ('W-Strei', 'W-Üfa') and wv.wehr_v <> 'Schw')\n"
                            + "or (w.wehr in ('Ki') and wv.wehr_v not in ('Kl', 'Kl-Fb', 'Kl-Fb-Schü', 'Kl-Schü'))\n"
                            + "or (w.wehr in ('Na') and wv.wehr_v not in ('na'))\n"
                            + "or (w.wehr in ('Seg') and wv.wehr_v not in ('Seg', 'Seg-Fb'))\n"
                            + "or (w.wehr in ('Sek') and wv.wehr_v not in ('Sek'))\n"
                            + "or (w.wehr in ('Schl') and wv.wehr_v not in ('Schl'))\n"
                            + "or (w.wehr in ('Schü') and wv.wehr_v not in ('Schü', 'Schü-Dop', 'Schü-Dreh', 'Schü-Haken', 'Schü-Seg'))\n"
                            + "or (w.wehr in ('Tro') and wv.wehr_v not in ('Tro'))\n"
                            + "or (w.wehr in ('Wz') and wv.wehr_v not in ('Wz'))\n"
                            + "or (w.wehr in ('W-Strei', 'W-Üfa') and wav.wehr_av not in ('ohne'))\n"
                            + "or (wv.wehr_v in ('Bo','Bo-J') and m.material not in ('H','K','St'))\n"
                            + "or (wv.wehr_v in ('Schw') and m.material not in ('B','K'))\n"
                            + "or (wv.wehr_v not in ('Bo', 'Bo-J', 'Schw') and m.material is not null)\n"
                            + ") and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_WEHR_ATTR = "select " + FG_BA_WEHR.getID() + ", wehr." + FG_BA_WEHR.getPrimaryKey()
                            + " from dlm25w.fg_ba_wehr wehr\n"
                            + "join dlm25w.fg_ba_punkt von on (wehr.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_wehr w on (w.id = wehr.wehr)\n"
                            + "left join dlm25w.k_wehr_v wv on (wv.id = wehr.wehr_v)\n"
                            + "left join dlm25w.k_wehr_av wav on (wav.id = wehr.wehr_av)\n"
                            + "left join dlm25w.k_material m on (m.id = wehr.material_v)\n"
                            + "where\n"
                            + "(wehr.wehr is null or wehr.wehr_v is null or wehr.wehr_av is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 30))\n"
                            + "or (br_li  is not null and (br_li <= 0 or br_li > 100))\n"
                            + "or (ho_so  is not null and (ho_so < -6 or ho_so > 179))\n"
                            + "or (sz  is not null and (sz < -6 or sz > 179))\n"
                            + "or (az  is not null and (az < -6 or az > 179))\n"
                            + "or (sz is not null and az is not null and (sz <= az))\n"
                            + "or (sz is not null and ho_so is not null and (sz <= ho_so))\n"
                            + "or (az is not null and ho_so is not null and (az <= ho_so))\n"
                            + "or (w.wehr in ('S-Kbw', 'S-Sbw', 'S-Stw', 'S-Moe') and wv.wehr_v not in ('Bo', 'Bo-J', 'Schü'))\n"
                            + "or (w.wehr in ('W-Strei', 'W-Üfa') and wv.wehr_v <> 'Schw')\n"
                            + "or (w.wehr in ('Ki') and wv.wehr_v not in ('Kl', 'Kl-Fb', 'Kl-Fb-Schü', 'Kl-Schü'))\n"
                            + "or (w.wehr in ('Na') and wv.wehr_v not in ('na'))\n"
                            + "or (w.wehr in ('Seg') and wv.wehr_v not in ('Seg', 'Seg-Fb'))\n"
                            + "or (w.wehr in ('Sek') and wv.wehr_v not in ('Sek'))\n"
                            + "or (w.wehr in ('Schl') and wv.wehr_v not in ('Schl'))\n"
                            + "or (w.wehr in ('Schü') and wv.wehr_v not in ('Schü', 'Schü-Dop', 'Schü-Dreh', 'Schü-Haken', 'Schü-Seg'))\n"
                            + "or (w.wehr in ('Tro') and wv.wehr_v not in ('Tro'))\n"
                            + "or (w.wehr in ('Wz') and wv.wehr_v not in ('Wz'))\n"
                            + "or (w.wehr in ('W-Strei', 'W-Üfa') and wav.wehr_av not in ('ohne'))\n"
                            + "or (wv.wehr_v in ('Bo','Bo-J') and m.material not in ('H','K','St'))\n"
                            + "or (wv.wehr_v in ('Schw') and m.material not in ('B','K'))\n"
                            + "or (wv.wehr_v not in ('Bo', 'Bo-J', 'Schw') and m.material is not null)\n"
                            + ") and (%1$s is null or ba.id = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_SCHW_ATTR = "select " + FG_BA_SCHW.getID() + ", schw." + FG_BA_SCHW.getPrimaryKey()
                            + " from dlm25w.fg_ba_schw schw\n"
                            + "join dlm25w.fg_ba_punkt von on (schw.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where\n"
                            + "(schw.schw is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 30))\n"
                            + "or (sz  is not null and (sz < -6 or sz > 179))\n"
                            + "or (az  is not null and (az < -6 or az > 179))\n"
                            + "or (ezg_fl  is not null and (ezg_fl <= 0 or ezg_fl > 100))\n"
                            + "or (v_fl  is not null and (v_fl <= 0 or v_fl > 100))\n"
                            + "or (pu  is not null and (pu < 1 or pu > 9))\n"
                            + "or (pu_foel  is not null and (pu_foel <= 0 or pu_foel > 100))\n"
                            + "or (sz is not null and az is not null and (sz <= az))\n"
                            + "or (ezg_fl is not null and v_fl is not null and (ezg_fl < v_fl))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s));";
            } else {
                QUERY_SCHW_ATTR = "select " + FG_BA_SCHW.getID() + ", schw." + FG_BA_SCHW.getPrimaryKey()
                            + " from dlm25w.fg_ba_schw schw\n"
                            + "join dlm25w.fg_ba_punkt von on (schw.ba_st = von.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where\n"
                            + "(schw.schw is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 30))\n"
                            + "or (sz  is not null and (sz < -6 or sz > 179))\n"
                            + "or (az  is not null and (az < -6 or az > 179))\n"
                            + "or (ezg_fl  is not null and (ezg_fl <= 0 or ezg_fl > 100))\n"
                            + "or (v_fl  is not null and (v_fl <= 0 or v_fl > 100))\n"
                            + "or (pu  is not null and (pu < 1 or pu > 9))\n"
                            + "or (pu_foel  is not null and (pu_foel <= 0 or pu_foel > 100))\n"
                            + "or (sz is not null and az is not null and (sz <= az))\n"
                            + "or (ezg_fl is not null and v_fl is not null and (ezg_fl < v_fl))\n"
                            + ") and (%1$s is null or ba.id = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_RL_OVERLAPS = "select distinct " + FG_BA_RL.getID() + ", t1." + FG_BA_RL.getPrimaryKey()
                            + " from (select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_rl t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "                            	) as t1,\n"
                            + "                            	(select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_rl t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "                            	) as t2\n"
                            + "                            where (%1$s is null or t1.route = any(%1$s)) and t1.id <> t2.id and t1.route = t2.route and (least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) > 0.5";
            } else {
                QUERY_RL_OVERLAPS = "select distinct " + FG_BA_RL.getID() + ", t1." + FG_BA_RL.getPrimaryKey()
                            + " from (select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_rl t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + "                            	) as t1,\n"
                            + "                            	(select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_rl t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + "                            	) as t2\n"
                            + "                            where (%1$s is null or t1.route = any(%1$s)) and t1.id <> t2.id and t1.route = t2.route and (least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) > 0.5";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_D_OVERLAPS = "select distinct " + FG_BA_D.getID() + ", t1." + FG_BA_D.getPrimaryKey()
                            + " from (select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_d t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "                            	) as t1,\n"
                            + "                            	(select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_d t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "                            	) as t2\n"
                            + "                            where (%1$s is null or t1.route = any(%1$s)) and t1.id <> t2.id and t1.route = t2.route and (least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) > 0.5";
            } else {
                QUERY_D_OVERLAPS = "select distinct " + FG_BA_D.getID() + ", t1." + FG_BA_D.getPrimaryKey()
                            + " from (select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_d t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + "                            	) as t1,\n"
                            + "                            	(select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_d t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + "                            	) as t2\n"
                            + "                            where (%1$s is null or t1.route = any(%1$s)) and t1.id <> t2.id and t1.route = t2.route and (least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) > 0.5";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_DUE_OVERLAPS = "select distinct " + FG_BA_DUE.getID() + ", t1." + FG_BA_DUE.getPrimaryKey()
                            + " from (select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_due t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "                            	) as t1,\n"
                            + "                            	(select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_due t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "                            	) as t2\n"
                            + "                            where (%1$s is null or t1.route = any(%1$s)) and t1.id <> t2.id and t1.route = t2.route and (least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) > 0.5";
            } else {
                QUERY_DUE_OVERLAPS = "select distinct " + FG_BA_DUE.getID() + ", t1." + FG_BA_DUE.getPrimaryKey()
                            + " from (select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_due t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + "                            	) as t1,\n"
                            + "                            	(select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_due t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "                                 join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "                                 left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + " WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + "                            	) as t2\n"
                            + "                            where (%1$s is null or t1.route = any(%1$s)) and t1.id <> t2.id and t1.route = t2.route and (least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) > 0.5";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_SCHA_DISTANCE = "select distinct " + FG_BA_SCHA.getID() + ", s1." + FG_BA_SCHA.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_scha scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_scha scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 0.5";
            } else {
                QUERY_SCHA_DISTANCE = "select distinct " + FG_BA_SCHA.getID() + ", s1." + FG_BA_SCHA.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_scha scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_scha scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 0.5";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_WEHR_DISTANCE = "select distinct " + FG_BA_WEHR.getID() + ", s1." + FG_BA_WEHR.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_wehr scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_wehr scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 0.5";
            } else {
                QUERY_WEHR_DISTANCE = "select distinct " + FG_BA_WEHR.getID() + ", s1." + FG_BA_WEHR.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_wehr scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_wehr scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 0.5";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_SCHW_DISTANCE = "select distinct " + FG_BA_SCHW.getID() + ", s1." + FG_BA_SCHW.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_schw scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_schw scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 20";
            } else {
                QUERY_SCHW_DISTANCE = "select distinct " + FG_BA_SCHW.getID() + ", s1." + FG_BA_SCHW.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_schw scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_schw scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 20";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_KR_DISTANCE = "select distinct " + FG_BA_KR.getID() + ", s1." + FG_BA_KR.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_kr scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + " left join dlm25w.k_kr k on (k.id = scha.kr)"
                            + " where k.kr in ('Br', 'Gew', 'U')"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_kr scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + " left join dlm25w.k_kr k on (k.id = scha.kr)"
                            + " where k.kr in ('Br', 'Gew', 'U')"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 3";
            } else {
                QUERY_KR_DISTANCE = "select distinct " + FG_BA_KR.getID() + ", s1." + FG_BA_KR.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_kr scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + " left join dlm25w.k_kr k on (k.id = scha.kr)"
                            + "WHERE k.kr in ('Br', 'Gew', 'U') and gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_kr scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + " left join dlm25w.k_kr k on (k.id = scha.kr)"
                            + "WHERE k.kr in ('Br', 'Gew', 'U') and gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 3";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_EA_DISTANCE = "select distinct " + FG_BA_EA.getID() + ", s1." + FG_BA_EA.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_ea scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_ea scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 0.5";
            } else {
                QUERY_EA_DISTANCE = "select distinct " + FG_BA_EA.getID() + ", s1." + FG_BA_EA.getPrimaryKey()
                            + " from (\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_ea scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s1,\n"
                            + "(\n"
                            + "select scha.id, st.route, st.wert\n"
                            + "from dlm25w.fg_ba_ea scha\n"
                            + "join dlm25w.fg_ba_punkt st on (scha.ba_st = st.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = st.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "WHERE gr.owner = '"
                            + user.getUserGroup().getName() + "'"
                            + ") s2\n"
                            + "where (%1$s is null or s1.route = any(%1$s)) and s1.id <> s2.id and s1.route = s2.route and abs(s1.wert - s2.wert) < 0.5";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_SCHA_OFFEN = "select distinct " + FG_BA_SCHA.getID() + ", scha." + FG_BA_SCHA.getPrimaryKey()
                            + " from dlm25w.fg_ba_scha scha\n"
                            + "join dlm25w.fg_ba_punkt s on (scha.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and not\n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert))\n";
            } else {
                QUERY_SCHA_OFFEN = "select distinct " + FG_BA_SCHA.getID() + ", scha." + FG_BA_SCHA.getPrimaryKey()
                            + " from dlm25w.fg_ba_scha scha\n"
                            + "join dlm25w.fg_ba_punkt s on (scha.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' and not\n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert))\n";
            }

//            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
//                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
//                QUERY_WEHR_GESCHL = "select distinct " + FG_BA_WEHR.getID() + ", wehr." + FG_BA_WEHR.getPrimaryKey()
//                            + " from dlm25w.fg_ba_wehr wehr\n"
//                            + "join dlm25w.fg_ba_punkt s on (wehr.ba_st = s.id)\n"
//                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
//                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
//                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
//                            + "where (%1$s is null or s.route = any(%1$s)) and \n"
//                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + "or\n"
//                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + "or \n"
//                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + ")";
//            } else {
//                QUERY_WEHR_GESCHL = "select distinct " + FG_BA_WEHR.getID() + ", wehr." + FG_BA_WEHR.getPrimaryKey()
//                            + " from dlm25w.fg_ba_wehr wehr\n"
//                            + "join dlm25w.fg_ba_punkt s on (wehr.ba_st = s.id)\n"
//                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
//                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
//                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
//                            + "where (%1$s is null or s.route = any(%1$s)) and gr.owner = '"
//                            + user.getUserGroup().getName() + "' and  \n"
//                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + "or\n"
//                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + "or \n"
//                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + ")";
//            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_ANLP_OFFEN = "select distinct " + FG_BA_ANLP.getID() + ", a." + FG_BA_ANLP.getPrimaryKey()
                            + " from dlm25w.fg_ba_anlp a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anlp ka on (ka.id = a.anlp)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and ka.anlp in ('Schi', 'Slu') and \n"
                            + "not (exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            } else {
                QUERY_ANLP_OFFEN = "select distinct " + FG_BA_ANLP.getID() + ", a." + FG_BA_ANLP.getPrimaryKey()
                            + " from dlm25w.fg_ba_anlp a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anlp ka on (ka.id = a.anlp)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' and ka.anlp in ('Schi', 'Slu') and \n"
                            + "not (exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_ANLP_GESCHL = "select distinct " + FG_BA_ANLP.getID() + ", a." + FG_BA_ANLP.getPrimaryKey()
                            + " from dlm25w.fg_ba_anlp a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anlp ka on (ka.id = a.anlp)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and ka.anlp in ('Fu', 'P', 'P-Grr', 'P-Steg', 'P-Grr-Steg', 'P-Lat', 'Steg', 'Stt', 'Vt') and \n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            } else {
                QUERY_ANLP_GESCHL = "select distinct " + FG_BA_ANLP.getID() + ", a." + FG_BA_ANLP.getPrimaryKey()
                            + " from dlm25w.fg_ba_anlp a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anlp ka on (ka.id = a.anlp)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "' and ka.anlp in ('Fu', 'P', 'P-Grr', 'P-Steg', 'P-Grr-Steg', 'P-Lat', 'Steg', 'Stt', 'Vt') and \n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_ANLP_ESW = "select distinct " + FG_BA_ANLP.getID() + ", a." + FG_BA_ANLP.getPrimaryKey()
                            + " from dlm25w.fg_ba_anlp a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anlp ka on (ka.id = a.anlp)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and esw = 1 and \n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            } else {
                QUERY_ANLP_ESW = "select distinct " + FG_BA_ANLP.getID() + ", a." + FG_BA_ANLP.getPrimaryKey()
                            + " from dlm25w.fg_ba_anlp a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anlp ka on (ka.id = a.anlp)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' and esw = 1 and \n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_KR_ESW = "select distinct " + FG_BA_KR.getID() + ", a." + FG_BA_KR.getPrimaryKey()
                            + " from dlm25w.fg_ba_kr a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and esw = 1 and \n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            } else {
                QUERY_KR_ESW = "select distinct " + FG_BA_KR.getID() + ", a." + FG_BA_KR.getPrimaryKey()
                            + " from dlm25w.fg_ba_kr a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' and esw = 1 and \n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            }

//            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
//                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
//                QUERY_EA_GESCHL = "select distinct " + FG_BA_EA.getID() + ", a." + FG_BA_EA.getPrimaryKey()
//                            + " from dlm25w.fg_ba_ea a\n"
//                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
//                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
//                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
//                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
//                            + "where (%1$s is null or s.route = any(%1$s)) and \n"
//                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + "or\n"
//                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + "or \n"
//                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + ")";
//            } else {
//                QUERY_EA_GESCHL = "select distinct " + FG_BA_EA.getID() + ", a." + FG_BA_EA.getPrimaryKey()
//                            + " from dlm25w.fg_ba_ea a\n"
//                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
//                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
//                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
//                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
//                            + "where (%1$s is null or s.route = any(%1$s)) and gr.owner = '"
//                            + user.getUserGroup().getName()
//                            + "' and \n"
//                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + "or\n"
//                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + "or \n"
//                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
//                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
//                            + ")";
//            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_EA_ESW = "select distinct " + FG_BA_EA.getID() + ", a." + FG_BA_EA.getPrimaryKey()
                            + " from dlm25w.fg_ba_ea a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and esw = 1 and \n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            } else {
                QUERY_EA_ESW = "select distinct " + FG_BA_EA.getID() + ", a." + FG_BA_EA.getPrimaryKey()
                            + " from dlm25w.fg_ba_ea a\n"
                            + "join dlm25w.fg_ba_punkt s on (a.ba_st = s.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' and esw = 1 and \n"
                            + "(exists(select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or\n"
                            + "exists(select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + "or \n"
                            + "exists(select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = s.route and least(v.wert, b.wert) <= s.wert and greatest(v.wert, b.wert) >= s.wert)\n"
                            + ")";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_ANLL_GESCHL = "select distinct " + FG_BA_ANLL.getID() + ", a." + FG_BA_ANLL.getPrimaryKey()
                            + " from dlm25w.fg_ba_anll a\n"
                            + "join dlm25w.fg_ba_linie linie on (a.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anll ka on (ka.id = a.anll)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and ka.anll <> 'WKA' and \n"
                            + "((\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") + \n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") +\n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ")) > 0.1";
            } else {
                QUERY_ANLL_GESCHL = "select distinct " + FG_BA_ANLL.getID() + ", a." + FG_BA_ANLL.getPrimaryKey()
                            + " from dlm25w.fg_ba_anll a\n"
                            + "join dlm25w.fg_ba_linie linie on (a.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anll ka on (ka.id = a.anll)\n"
                            + "where (%1$s is null or s.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' and ka.anll <> 'WKA' and \n"
                            + "((\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") + \n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") +\n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ")) > 0.1";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_ANLL_GESCHL = "select distinct " + FG_BA_ANLL.getID() + ", a." + FG_BA_ANLL.getPrimaryKey()
                            + " from dlm25w.fg_ba_anll a\n"
                            + "join dlm25w.fg_ba_linie linie on (a.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anll ka on (ka.id = a.anll)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and ka.anll <> 'WKA' and \n"
                            + "((\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") + \n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") +\n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ")) > 0.1";
            } else {
                QUERY_ANLL_GESCHL = "select distinct " + FG_BA_ANLL.getID() + ", a." + FG_BA_ANLL.getPrimaryKey()
                            + " from dlm25w.fg_ba_anll a\n"
                            + "join dlm25w.fg_ba_linie linie on (a.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)\n"
                            + "left join dlm25w.k_anll ka on (ka.id = a.anll)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' and ka.anll <> 'WKA' and \n"
                            + "((\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") + \n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") +\n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ")) > 0.1";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_KR_MARKED_TWICE = "select distinct " + FG_BA_KR.getID() + ", k1." + FG_BA_KR.getPrimaryKey()
                            + " from \n"
                            + "(select kr.id, g.geo_field, s.route\n"
                            + "from dlm25w.fg_ba_kr kr\n"
                            + "join dlm25w.fg_ba_punkt s on (kr.ba_st = s.id)\n"
                            + "join geom g on (s.real_point = g.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)) as k1,\n"
                            + "(select kr.id, g.geo_field, s.route \n"
                            + "from dlm25w.fg_ba_kr kr\n"
                            + "join dlm25w.fg_ba_punkt s on (kr.ba_st = s.id)\n"
                            + "join geom g on (s.real_point = g.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)) as k2\n"
                            + " where (%1$s is null or k1.route = any(%1$s)) and k1.id <> k2.id and st_intersects(k1.geo_field, st_buffer(k2.geo_field, 1));";
            } else {
                QUERY_KR_MARKED_TWICE = "select distinct " + FG_BA_KR.getID() + ", k1." + FG_BA_KR.getPrimaryKey()
                            + " from \n"
                            + "(select kr.id, g.geo_field, s.route\n"
                            + "from dlm25w.fg_ba_kr kr\n"
                            + "join dlm25w.fg_ba_punkt s on (kr.ba_st = s.id)\n"
                            + "join geom g on (s.real_point = g.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)"
                            + " where gr.owner = '"
                            + user.getUserGroup().getName() + "') as k1,\n"
                            + "(select kr.id, g.geo_field, s.route \n"
                            + "from dlm25w.fg_ba_kr kr\n"
                            + "join dlm25w.fg_ba_punkt s on (kr.ba_st = s.id)\n"
                            + "join geom g on (s.real_point = g.id)\n"
                            + "join dlm25w.fg_ba ba on (s.route = ba.id)\n"
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = bak.ww_gr)"
                            + " where gr.owner = '"
                            + user.getUserGroup().getName() + "') as k2\n"
                            + "where (%1$s is null or k1.route = any(%1$s)) and k1.id <> k2.id and not st_isempty(st_intersection(k1.geo_field, st_buffer(k2.geo_field, 1)));";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    // dlm25w.merge_fg_bak_gwk()
    private boolean successful = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BauwerkeCheckAction object.
     */
    public BauwerkeCheckAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                BauwerkeCheckAction.class,
                "BauwerkeCheckAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                BauwerkeCheckAction.class,
                "BauwerkeCheckAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-zoom.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getProgressSteps() {
        return 36;
    }

    @Override
    public boolean startCheckInternal(final boolean isExport,
            final WaitDialog wd,
            final List<H2FeatureService> result) {
        if (isExport) {
            try {
                final CheckResult cr = check(isExport, wd);

                if (result != null) {
                    addService(result, cr.getAnllAttr());
                    addService(result, cr.getAnllGeschl());
                    addService(result, cr.getAnlpAttr());
                    addService(result, cr.getAnlpEsw());
                    addService(result, cr.getAnlpGeschlossen());
                    addService(result, cr.getAnlpOffen());
                    addService(result, cr.getDueAttr());
                    addService(result, cr.getDueHole());
                    addService(result, cr.getDueOverlapps());
                    addService(result, cr.getEaAttr());
                    addService(result, cr.getEaDistance());
                    addService(result, cr.getEaEsw());
//                    addService(result, cr.getEaGeschl());
                    addService(result, cr.getKrAttr());
                    addService(result, cr.getKrDistance());
                    addService(result, cr.getKrEsw());
                    addService(result, cr.getKrMarkedTwice());
                    addService(result, cr.getRlAttr());
                    addService(result, cr.getRlHole());
                    addService(result, cr.getRlOverlapps());
                    addService(result, cr.getSchaAttr());
                    addService(result, cr.getSchaDistance());
                    addService(result, cr.getSchaOffen());
                    addService(result, cr.getSchwAttr());
                    addService(result, cr.getSchwDistance());
                    addService(result, cr.getWehrAttr());
                    addService(result, cr.getWehrDistance());
//                    addService(result, cr.getWehrGeschlossen());
                    addService(result, cr.getdAttr());
                    addService(result, cr.getdHole());
                    addService(result, cr.getdOverlapps());
                }
            } catch (Exception e) {
                LOG.error("Error while performing check", e);

                return false;
            }

            return true;
        } else {
            final WaitingDialogThread<CheckResult> wdt = new WaitingDialogThread<CheckResult>(
                    StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                    true,
                    NbBundle.getMessage(BauwerkeCheckAction.class,
                        "BauwerkeCheckAction.actionPerformed().dialog"),
                    null,
                    100) {

                    @Override
                    protected CheckResult doInBackground() throws Exception {
                        wd.setMax(getProgressSteps());
                        return check(isExport, wd);
                    }

                    @Override
                    protected void done() {
                        try {
                            final CheckResult result = get();

                            removeServicesFromLayerModel(ALL_CHECKS);

                            if (isExport) {
                                return;
                            }

                            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                NbBundle.getMessage(
                                    BauwerkeCheckAction.class,
                                    "BauwerkeCheckAction.actionPerformed().result.text",
                                    new Object[] {
                                        result.getBakCount(),
                                        result.getRlAttrErrors(),
                                        result.getdAttrErrors(),
                                        result.getDueAttrErrors(),
                                        result.getSchaAttrErrors(),
                                        result.getWehrAttrErrors(),
                                        result.getSchwAttrErrors(),
                                        result.getAnlpAttrErrors(),
                                        result.getAnllAttrErrors(),
                                        result.getKrAttrErrors(),
                                        result.getEaAttrErrors(),
                                        result.getRlHoleErrors(),
                                        result.getdHoleErrors(),
                                        result.getDueHoleErrors(),
                                        result.getRlOverlappsErrors(),
                                        result.getdOverlappsErrors(),
                                        result.getDueOverlappsErrors(),
                                        result.getSchaDistanceError()
                                                + result.getSchaOffenError(),
                                        result.getWehrDistanceError(),
//                                                + result.getWehrGeschlossenError(),
                                        result.getSchwDistanceError(),
                                        result.getAnlpEswError()
                                                + result.getAnlpGeschlossenError()
                                                + result.getAnlpOffenError(),
                                        result.getAnllGeschlError(),
                                        result.getKrDistanceError()
                                                + result.getKrEswError()
                                                + result.getKrMarkedTwiceError(),
                                        result.getEaDistanceError()
                                                + result.getEaEswError()
                                                // + result.getEaGeschlError()
                                    }),
                                NbBundle.getMessage(
                                    BauwerkeCheckAction.class,
                                    "BauwerkeCheckAction.actionPerformed().result.title"),
                                JOptionPane.INFORMATION_MESSAGE);

                            if (result.getEaEsw() != null) {
                                showService(result.getEaEsw());
                            }
                            if (result.getEaDistance() != null) {
                                showService(result.getEaDistance());
                            }
//                            if (result.getEaGeschl() != null) {
//                                showService(result.getEaGeschl());
//                            }
                            if (result.getEaAttr() != null) {
                                showService(result.getEaAttr());
                            }
                            if (result.getKrEsw() != null) {
                                showService(result.getKrEsw());
                            }
                            if (result.getKrMarkedTwice() != null) {
                                showService(result.getKrMarkedTwice());
                            }
                            if (result.getKrDistance() != null) {
                                showService(result.getKrDistance());
                            }
                            if (result.getKrAttr() != null) {
                                showService(result.getKrAttr());
                            }
                            if (result.getAnllGeschl() != null) {
                                showService(result.getAnllGeschl());
                            }
                            if (result.getAnllAttr() != null) {
                                showService(result.getAnllAttr());
                            }
                            if (result.getAnlpEsw() != null) {
                                showService(result.getAnlpEsw());
                            }
                            if (result.getAnlpGeschlossen() != null) {
                                showService(result.getAnlpGeschlossen());
                            }
                            if (result.getAnlpOffen() != null) {
                                showService(result.getAnlpOffen());
                            }
                            if (result.getAnlpAttr() != null) {
                                showService(result.getAnlpAttr());
                            }
                            if (result.getSchwDistance() != null) {
                                showService(result.getSchwDistance());
                            }
                            if (result.getSchwAttr() != null) {
                                showService(result.getSchwAttr());
                            }
                            if (result.getWehrDistance() != null) {
                                showService(result.getWehrDistance());
                            }
                            if (result.getWehrAttr() != null) {
                                showService(result.getWehrAttr());
                            }
                            if (result.getSchaOffen() != null) {
                                showService(result.getSchaOffen());
                            }
                            if (result.getSchaDistance() != null) {
                                showService(result.getSchaDistance());
                            }
                            if (result.getSchaAttr() != null) {
                                showService(result.getSchaAttr());
                            }
                            if (result.getDueOverlapps() != null) {
                                showService(result.getDueOverlapps());
                            }
                            if (result.getDueHole() != null) {
                                showService(result.getDueHole());
                            }
                            if (result.getDueAttr() != null) {
                                showService(result.getDueAttr());
                            }
                            if (result.getdOverlapps() != null) {
                                showService(result.getdOverlapps());
                            }
                            if (result.getdHole() != null) {
                                showService(result.getdHole());
                            }
                            if (result.getdAttr() != null) {
                                showService(result.getdAttr());
                            }
                            if (result.getRlOverlapps() != null) {
                                showService(result.getRlOverlapps());
                            }
                            if (result.getRlHole() != null) {
                                showService(result.getRlHole());
                            }
                            if (result.getRlAttr() != null) {
                                showService(result.getRlAttr());
                            }
//                            if (result.getWehrGeschlossen() != null) {
//                                showService(result.getWehrGeschlossen());
//                            }
                        } catch (Exception e) {
                            LOG.error("Error while performing the bauwerke analyse.", e);
                            successful = false;
                        }
                    }
                };

            wdt.start();

            return successful;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isExport  DOCUMENT ME!
     * @param   wd        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private CheckResult check(final boolean isExport, final WaitDialog wd) throws Exception {
        final CheckResult result = new CheckResult();
        String user = AppBroker.getInstance().getOwner();
        int[] selectedIds = null;

        if (user.equalsIgnoreCase("Administratoren") || user.equalsIgnoreCase("lung_edit1")) {
            user = null;
        }

        removeServicesFromDb(ALL_CHECKS);

        if (isExport) {
            if (user == null) {
                selectedIds = getIdsOfSelectedObjects("fg_ba");
            }
        } else {
            selectedIds = getIdsOfSelectedObjects("fg_ba");
        }

        final ArrayList<ArrayList> countList = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(),
                            new FgBakCount(user, selectedIds, null));

        if ((countList != null) && !countList.isEmpty()) {
            final ArrayList innerList = countList.get(0);

            if ((innerList != null) && !innerList.isEmpty() && (innerList.get(0) instanceof Number)) {
                result.setBakCount(((Number)innerList.get(0)).intValue());
            }
        }

        // start auto correction
        final CidsServerSearch mergeRl = new MergeBaRl(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeRl);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeD = new MergeBaD(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeD);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeDue = new MergeBaDue(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeDue);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeScha = new MergeBaScha(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeScha);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeWehr = new MergeBaWehr(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeWehr);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeSchw = new MergeBaSchw(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeSchw);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeAnlp = new MergeBaAnlp(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeAnlp);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeAnll = new MergeBaAnll(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeAnll);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeKr = new MergeBaKr(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeKr);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeEa = new MergeBaEa(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeEa);
        increaseProgress(wd, 1);

        // todo korrigiere berechnete Attribute

        // start checks
        result.setAnllAttr(analyseByQuery(
                FG_BA_ANLL,
                String.format(QUERY_ANLL_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_ANLL_ANLL__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setAnlpAttr(analyseByQuery(
                FG_BA_ANLP,
                String.format(QUERY_ANLP_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_ANLP_ANLP__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setDueAttr(analyseByQuery(
                FG_BA_DUE,
                String.format(QUERY_DUE_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_DUE_DUE__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setEaAttr(analyseByQuery(
                FG_BA_EA,
                String.format(QUERY_EA_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_EA_EA__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setKrAttr(analyseByQuery(
                FG_BA_KR,
                String.format(QUERY_KR_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_KR_KR__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setRlAttr(analyseByQuery(
                FG_BA_RL,
                String.format(QUERY_RL_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_RL_RL__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setSchaAttr(analyseByQuery(
                FG_BA_SCHA,
                String.format(QUERY_SCHA_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_SCHA_SCHA__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setSchwAttr(analyseByQuery(
                FG_BA_SCHW,
                String.format(QUERY_SCHW_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_SCHW_SCHW__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setWehrAttr(analyseByQuery(
                FG_BA_WEHR,
                String.format(QUERY_WEHR_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_WEHR_WEHR__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setdAttr(analyseByQuery(
                FG_BA_D,
                String.format(QUERY_D_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_DD__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setRlHole(analyseByQuery(
                FG_BA_RL,
                String.format(QUERY_RL_HOLE, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_RL_RL__LUECKE));
        increaseProgress(wd, 1);

        result.setdHole(analyseByQuery(
                FG_BA_D,
                String.format(QUERY_D_HOLE, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_DD__LUECKE));
        increaseProgress(wd, 1);

        result.setDueHole(analyseByQuery(
                FG_BA_DUE,
                String.format(QUERY_DUE_HOLE, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_DUE_DUE__LUECKE));
        increaseProgress(wd, 1);

        result.setRlOverlapps(analyseByQuery(
                FG_BA_RL,
                String.format(QUERY_RL_OVERLAPS, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_RL_RL__UEBERLAPPUNG));
        increaseProgress(wd, 1);

        result.setdOverlapps(analyseByQuery(
                FG_BA_D,
                String.format(QUERY_D_OVERLAPS, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_DD__UEBERLAPPUNG));

        result.setDueOverlapps(analyseByQuery(
                FG_BA_DUE,
                String.format(QUERY_DUE_OVERLAPS, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_DUE_DUE__UEBERLAPPUNG));
        increaseProgress(wd, 1);

        result.setSchaDistance(analyseByQuery(
                FG_BA_SCHA,
                String.format(QUERY_SCHA_DISTANCE, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_SCHA_SCHA_DOPPELTZU_NAH));
        increaseProgress(wd, 1);

        result.setWehrDistance(analyseByQuery(
                FG_BA_WEHR,
                String.format(QUERY_WEHR_DISTANCE, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_WEHR_WEHR_DOPPELTZU_NAH));
        increaseProgress(wd, 1);

        result.setSchwDistance(analyseByQuery(
                FG_BA_SCHW,
                String.format(QUERY_SCHW_DISTANCE, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_SCHW_SCHW_DOPPELTZU_NAH));
        increaseProgress(wd, 1);

        result.setKrDistance(analyseByQuery(
                FG_BA_KR,
                String.format(QUERY_KR_DISTANCE, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_KR_KR_DOPPELTZU_NAH));
        increaseProgress(wd, 1);

        result.setEaDistance(analyseByQuery(
                FG_BA_EA,
                String.format(QUERY_EA_DISTANCE, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_EA_EA_DOPPELTZU_NAH));
        increaseProgress(wd, 1);

        result.setSchaOffen(analyseByQuery(
                FG_BA_SCHA,
                String.format(QUERY_SCHA_OFFEN, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_SCHA_SCHA_AUF_OFFENEM__GER));
        increaseProgress(wd, 1);

//        result.setWehrGeschlossen(analyseByQuery(FG_BA_WEHR,
//                String.format(QUERY_WEHR_GESCHL, SQLFormatter.createSqlArrayString(selectedIds)), CHECKS_BAUWERKE_WEHR_WEHR_AUF_GESCHLOSSEN));
//        increaseProgress(wd, 1);

        result.setAnlpOffen(analyseByQuery(
                FG_BA_ANLP,
                String.format(QUERY_ANLP_OFFEN, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_ANLP_ANLP_AUF_OFFENEM__GER));
        increaseProgress(wd, 1);

        result.setAnlpGeschlossen(analyseByQuery(
                FG_BA_ANLP,
                String.format(QUERY_ANLP_GESCHL, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_ANLP_ANLP_AUF_GESCHLOSSEN));
        increaseProgress(wd, 1);

        result.setAnlpEsw(analyseByQuery(
                FG_BA_ANLP,
                String.format(QUERY_ANLP_ESW, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_ANLP_ANLP__ESW_FUER_GESCHLO));

        result.setKrEsw(analyseByQuery(
                FG_BA_KR,
                String.format(QUERY_KR_ESW, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_KR_KR__ESW_FUER_GESCHLOSSEN));
        increaseProgress(wd, 1);

//        result.setEaGeschl(analyseByQuery(FG_BA_EA,
//                String.format(QUERY_EA_GESCHL, SQLFormatter.createSqlArrayString(selectedIds)), CHECKS_BAUWERKE_EA_EA_AUF_GESCHLOSSENEM_G));

        result.setEaEsw(analyseByQuery(
                FG_BA_EA,
                String.format(QUERY_EA_ESW, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_EA_EA__ESW_FUER_GESCHLOSSEN));
        increaseProgress(wd, 1);

        result.setAnllGeschl(analyseByQuery(
                FG_BA_ANLL,
                String.format(QUERY_ANLL_GESCHL, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_ANLL_ANLL_AUF_GESCHLOSSEN));
        increaseProgress(wd, 1);

        result.setKrMarkedTwice(analyseByQuery(
                FG_BA_KR,
                String.format(QUERY_KR_MARKED_TWICE, SQLFormatter.createSqlArrayString(selectedIds)),
                CHECKS_BAUWERKE_KR_KR_DOPPELTE__MARKIERUNG));
        increaseProgress(wd, 1);

        if (result.getAnllAttr() != null) {
            result.setAnllAttrErrors(result.getAnllAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getAnlpAttr() != null) {
            result.setAnlpAttrErrors(result.getAnlpAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getDueAttr() != null) {
            result.setDueAttrErrors(result.getDueAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getEaAttr() != null) {
            result.setEaAttrErrors(result.getEaAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getKrAttr() != null) {
            result.setKrAttrErrors(result.getKrAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getRlAttr() != null) {
            result.setRlAttrErrors(result.getRlAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getSchaAttr() != null) {
            result.setSchaAttrErrors(result.getSchaAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getSchwAttr() != null) {
            result.setSchwAttrErrors(result.getSchwAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getWehrAttr() != null) {
            result.setWehrAttrErrors(result.getWehrAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getdAttr() != null) {
            result.setdAttrErrors(result.getdAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getRlHole() != null) {
            result.setRlHoleErrors(result.getRlHole().getFeatureCount(null));
            successful = false;
        }

        if (result.getdHole() != null) {
            result.setdHoleErrors(result.getdHole().getFeatureCount(null));
            successful = false;
        }

        if (result.getDueHole() != null) {
            result.setDueHoleErrors(result.getDueHole().getFeatureCount(null));
            successful = false;
        }

        if (result.getRlOverlapps() != null) {
            result.setRlOverlappsErrors(result.getRlOverlapps().getFeatureCount(null));
            successful = false;
        }

        if (result.getdOverlapps() != null) {
            result.setdOverlappsErrors(result.getdOverlapps().getFeatureCount(null));
            successful = false;
        }

        if (result.getDueOverlapps() != null) {
            result.setDueOverlappsErrors(result.getDueOverlapps().getFeatureCount(null));
            successful = false;
        }

        if (result.getSchaDistance() != null) {
            result.setSchaDistanceError(result.getSchaDistance().getFeatureCount(null));
            successful = false;
        }

        if (result.getWehrDistance() != null) {
            result.setWehrDistanceError(result.getWehrDistance().getFeatureCount(null));
            successful = false;
        }

        if (result.getSchwDistance() != null) {
            result.setSchwDistanceError(result.getSchwDistance().getFeatureCount(null));
            successful = false;
        }

        if (result.getKrDistance() != null) {
            result.setKrDistanceError(result.getKrDistance().getFeatureCount(null));
            successful = false;
        }

        if (result.getEaDistance() != null) {
            result.setEaDistanceError(result.getEaDistance().getFeatureCount(null));
            successful = false;
        }

        if (result.getSchaOffen() != null) {
            result.setSchaOffenError(result.getSchaOffen().getFeatureCount(null));
            successful = false;
        }

//        if (result.getWehrGeschlossen() != null) {
//            result.setWehrGeschlossenError(result.getWehrGeschlossen().getFeatureCount(null));
//            successful = false;
//        }

        if (result.getAnlpGeschlossen() != null) {
            result.setAnlpGeschlossenError(result.getAnlpGeschlossen().getFeatureCount(null));
            successful = false;
        }

        if (result.getAnlpOffen() != null) {
            result.setAnlpOffenError(result.getAnlpOffen().getFeatureCount(null));
            successful = false;
        }

        if (result.getKrEsw() != null) {
            result.setKrEswError(result.getKrEsw().getFeatureCount(null));
            successful = false;
        }

//        if (result.getEaGeschl() != null) {
//            result.setEaGeschlError(result.getEaGeschl().getFeatureCount(null));
//            successful = false;
//        }

        if (result.getEaEsw() != null) {
            result.setEaEswError(result.getEaEsw().getFeatureCount(null));
            successful = false;
        }

        if (result.getAnllGeschl() != null) {
            result.setAnllGeschlError(result.getAnllGeschl().getFeatureCount(null));
            successful = false;
        }

        if (result.getKrMarkedTwice() != null) {
            result.setKrMarkedTwiceError(result.getKrMarkedTwice().getFeatureCount(null));
            successful = false;
        }

        if (result.getAnlpEsw() != null) {
            result.setAnlpEswError(result.getAnlpEsw().getFeatureCount(null));
            successful = false;
        }

        return result;
    }

    @Override
    public boolean isEnabled() {
        return true
                    || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CheckResult {

        //~ Instance fields ----------------------------------------------------

        private int bakCount;
        private int rlAttrErrors;
        private int dAttrErrors;
        private int dueAttrErrors;
        private int schaAttrErrors;
        private int wehrAttrErrors;
        private int schwAttrErrors;
        private int anlpAttrErrors;
        private int anllAttrErrors;
        private int krAttrErrors;
        private int eaAttrErrors;
        private int rlHoleErrors;
        private int dHoleErrors;
        private int dueHoleErrors;
        private int rlOverlappsErrors;
        private int dOverlappsErrors;
        private int dueOverlappsErrors;
        private int schaDistanceError;
        private int wehrDistanceError;
        private int schwDistanceError;
        private int krDistanceError;
        private int eaDistanceError;
        private int schaOffenError;
//        private int wehrGeschlossenError;
        private int anlpOffenError;
        private int anlpGeschlossenError;
        private int anlpEswError;
        private int krEswError;
        private int eaEswError;
//        private int eaGeschlError;
        private int anllGeschlError;
        private int krMarkedTwiceError;
        private H2FeatureService rlAttr;
        private H2FeatureService dAttr;
        private H2FeatureService dueAttr;
        private H2FeatureService schaAttr;
        private H2FeatureService wehrAttr;
        private H2FeatureService schwAttr;
        private H2FeatureService anlpAttr;
        private H2FeatureService anllAttr;
        private H2FeatureService krAttr;
        private H2FeatureService eaAttr;
        private H2FeatureService rlHole;
        private H2FeatureService dHole;
        private H2FeatureService dueHole;
        private H2FeatureService rlOverlapps;
        private H2FeatureService dOverlapps;
        private H2FeatureService dueOverlapps;
        private H2FeatureService schaDistance;
        private H2FeatureService wehrDistance;
        private H2FeatureService schwDistance;
        private H2FeatureService krDistance;
        private H2FeatureService eaDistance;
        private H2FeatureService schaOffen;
//        private H2FeatureService wehrGeschlossen;
        private H2FeatureService anlpOffen;
        private H2FeatureService anlpGeschlossen;
        private H2FeatureService anlpEsw;
        private H2FeatureService krEsw;
        private H2FeatureService eaEsw;
//        private H2FeatureService eaGeschl;
        private H2FeatureService anllGeschl;
        private H2FeatureService krMarkedTwice;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the rlAttrErrors
         */
        public int getRlAttrErrors() {
            return rlAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rlAttrErrors  the rlAttrErrors to set
         */
        public void setRlAttrErrors(final int rlAttrErrors) {
            this.rlAttrErrors = rlAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dAttrErrors
         */
        public int getdAttrErrors() {
            return dAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dAttrErrors  the dAttrErrors to set
         */
        public void setdAttrErrors(final int dAttrErrors) {
            this.dAttrErrors = dAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dueAttrErrors
         */
        public int getDueAttrErrors() {
            return dueAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dueAttrErrors  the dueAttrErrors to set
         */
        public void setDueAttrErrors(final int dueAttrErrors) {
            this.dueAttrErrors = dueAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schaAttrErrors
         */
        public int getSchaAttrErrors() {
            return schaAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schaAttrErrors  the schaAttrErrors to set
         */
        public void setSchaAttrErrors(final int schaAttrErrors) {
            this.schaAttrErrors = schaAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the wehrAttrErrors
         */
        public int getWehrAttrErrors() {
            return wehrAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  wehrAttrErrors  the wehrAttrErrors to set
         */
        public void setWehrAttrErrors(final int wehrAttrErrors) {
            this.wehrAttrErrors = wehrAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schwAttrErrors
         */
        public int getSchwAttrErrors() {
            return schwAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schwAttrErrors  the schwAttrErrors to set
         */
        public void setSchwAttrErrors(final int schwAttrErrors) {
            this.schwAttrErrors = schwAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anlpAttrErrors
         */
        public int getAnlpAttrErrors() {
            return anlpAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anlpAttrErrors  the anlpAttrErrors to set
         */
        public void setAnlpAttrErrors(final int anlpAttrErrors) {
            this.anlpAttrErrors = anlpAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anllAttrErrors
         */
        public int getAnllAttrErrors() {
            return anllAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anllAttrErrors  the anllAttrErrors to set
         */
        public void setAnllAttrErrors(final int anllAttrErrors) {
            this.anllAttrErrors = anllAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the krAttrErrors
         */
        public int getKrAttrErrors() {
            return krAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  krAttrErrors  the krAttrErrors to set
         */
        public void setKrAttrErrors(final int krAttrErrors) {
            this.krAttrErrors = krAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the eaAttrErrors
         */
        public int getEaAttrErrors() {
            return eaAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  eaAttrErrors  the eaAttrErrors to set
         */
        public void setEaAttrErrors(final int eaAttrErrors) {
            this.eaAttrErrors = eaAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the rlAttr
         */
        public H2FeatureService getRlAttr() {
            return rlAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rlAttr  the rlAttr to set
         */
        public void setRlAttr(final H2FeatureService rlAttr) {
            this.rlAttr = rlAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dAttr
         */
        public H2FeatureService getdAttr() {
            return dAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dAttr  the dAttr to set
         */
        public void setdAttr(final H2FeatureService dAttr) {
            this.dAttr = dAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dueAttr
         */
        public H2FeatureService getDueAttr() {
            return dueAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dueAttr  the dueAttr to set
         */
        public void setDueAttr(final H2FeatureService dueAttr) {
            this.dueAttr = dueAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schaAttr
         */
        public H2FeatureService getSchaAttr() {
            return schaAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schaAttr  the schaAttr to set
         */
        public void setSchaAttr(final H2FeatureService schaAttr) {
            this.schaAttr = schaAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the wehrAttr
         */
        public H2FeatureService getWehrAttr() {
            return wehrAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  wehrAttr  the wehrAttr to set
         */
        public void setWehrAttr(final H2FeatureService wehrAttr) {
            this.wehrAttr = wehrAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schwAttr
         */
        public H2FeatureService getSchwAttr() {
            return schwAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schwAttr  the schwAttr to set
         */
        public void setSchwAttr(final H2FeatureService schwAttr) {
            this.schwAttr = schwAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anlpAttr
         */
        public H2FeatureService getAnlpAttr() {
            return anlpAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anlpAttr  the anlpAttr to set
         */
        public void setAnlpAttr(final H2FeatureService anlpAttr) {
            this.anlpAttr = anlpAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anllAttr
         */
        public H2FeatureService getAnllAttr() {
            return anllAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anllAttr  the anllAttr to set
         */
        public void setAnllAttr(final H2FeatureService anllAttr) {
            this.anllAttr = anllAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the krAttr
         */
        public H2FeatureService getKrAttr() {
            return krAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  krAttr  the krAttr to set
         */
        public void setKrAttr(final H2FeatureService krAttr) {
            this.krAttr = krAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the eaAttr
         */
        public H2FeatureService getEaAttr() {
            return eaAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  eaAttr  the eaAttr to set
         */
        public void setEaAttr(final H2FeatureService eaAttr) {
            this.eaAttr = eaAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the bakCount
         */
        public int getBakCount() {
            return bakCount;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  bakCount  the bakCount to set
         */
        public void setBakCount(final int bakCount) {
            this.bakCount = bakCount;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the rlHoleErrors
         */
        public int getRlHoleErrors() {
            return rlHoleErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rlHoleErrors  the rlHoleErrors to set
         */
        public void setRlHoleErrors(final int rlHoleErrors) {
            this.rlHoleErrors = rlHoleErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dHoleErrors
         */
        public int getdHoleErrors() {
            return dHoleErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dHoleErrors  the dHoleErrors to set
         */
        public void setdHoleErrors(final int dHoleErrors) {
            this.dHoleErrors = dHoleErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dueHoleErrors
         */
        public int getDueHoleErrors() {
            return dueHoleErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dueHoleErrors  the dueHoleErrors to set
         */
        public void setDueHoleErrors(final int dueHoleErrors) {
            this.dueHoleErrors = dueHoleErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the rlHole
         */
        public H2FeatureService getRlHole() {
            return rlHole;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rlHole  the rlHole to set
         */
        public void setRlHole(final H2FeatureService rlHole) {
            this.rlHole = rlHole;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dHole
         */
        public H2FeatureService getdHole() {
            return dHole;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dHole  the dHole to set
         */
        public void setdHole(final H2FeatureService dHole) {
            this.dHole = dHole;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dueHole
         */
        public H2FeatureService getDueHole() {
            return dueHole;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dueHole  the dueHole to set
         */
        public void setDueHole(final H2FeatureService dueHole) {
            this.dueHole = dueHole;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the rlOverlapps
         */
        public H2FeatureService getRlOverlapps() {
            return rlOverlapps;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rlOverlapps  the rlOverlapps to set
         */
        public void setRlOverlapps(final H2FeatureService rlOverlapps) {
            this.rlOverlapps = rlOverlapps;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dOverlapps
         */
        public H2FeatureService getdOverlapps() {
            return dOverlapps;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dOverlapps  the dOverlapps to set
         */
        public void setdOverlapps(final H2FeatureService dOverlapps) {
            this.dOverlapps = dOverlapps;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dueOverlapps
         */
        public H2FeatureService getDueOverlapps() {
            return dueOverlapps;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dueOverlapps  the dueOverlapps to set
         */
        public void setDueOverlapps(final H2FeatureService dueOverlapps) {
            this.dueOverlapps = dueOverlapps;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the rlOverlappsErrors
         */
        public int getRlOverlappsErrors() {
            return rlOverlappsErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rlOverlappsErrors  the rlOverlappsErrors to set
         */
        public void setRlOverlappsErrors(final int rlOverlappsErrors) {
            this.rlOverlappsErrors = rlOverlappsErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dOverlappsErrors
         */
        public int getdOverlappsErrors() {
            return dOverlappsErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dOverlappsErrors  the dOverlappsErrors to set
         */
        public void setdOverlappsErrors(final int dOverlappsErrors) {
            this.dOverlappsErrors = dOverlappsErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dueOverlappsErrors
         */
        public int getDueOverlappsErrors() {
            return dueOverlappsErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dueOverlappsErrors  the dueOverlappsErrors to set
         */
        public void setDueOverlappsErrors(final int dueOverlappsErrors) {
            this.dueOverlappsErrors = dueOverlappsErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schaDistance
         */
        public H2FeatureService getSchaDistance() {
            return schaDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schaDistance  the schaDistance to set
         */
        public void setSchaDistance(final H2FeatureService schaDistance) {
            this.schaDistance = schaDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the wehrDistance
         */
        public H2FeatureService getWehrDistance() {
            return wehrDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  wehrDistance  the wehrDistance to set
         */
        public void setWehrDistance(final H2FeatureService wehrDistance) {
            this.wehrDistance = wehrDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schwDistance
         */
        public H2FeatureService getSchwDistance() {
            return schwDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schwDistance  the schwDistance to set
         */
        public void setSchwDistance(final H2FeatureService schwDistance) {
            this.schwDistance = schwDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the krDistance
         */
        public H2FeatureService getKrDistance() {
            return krDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  krDistance  the krDistance to set
         */
        public void setKrDistance(final H2FeatureService krDistance) {
            this.krDistance = krDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the eaDistance
         */
        public H2FeatureService getEaDistance() {
            return eaDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  eaDistance  the eaDistance to set
         */
        public void setEaDistance(final H2FeatureService eaDistance) {
            this.eaDistance = eaDistance;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schaDistanceError
         */
        public int getSchaDistanceError() {
            return schaDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schaDistanceError  the schaDistanceError to set
         */
        public void setSchaDistanceError(final int schaDistanceError) {
            this.schaDistanceError = schaDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the wehrDistanceError
         */
        public int getWehrDistanceError() {
            return wehrDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  wehrDistanceError  the wehrDistanceError to set
         */
        public void setWehrDistanceError(final int wehrDistanceError) {
            this.wehrDistanceError = wehrDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schwDistanceError
         */
        public int getSchwDistanceError() {
            return schwDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schwDistanceError  the schwDistanceError to set
         */
        public void setSchwDistanceError(final int schwDistanceError) {
            this.schwDistanceError = schwDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the krDistanceError
         */
        public int getKrDistanceError() {
            return krDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  krDistanceError  the krDistanceError to set
         */
        public void setKrDistanceError(final int krDistanceError) {
            this.krDistanceError = krDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the eaDistanceError
         */
        public int getEaDistanceError() {
            return eaDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  eaDistanceError  the eaDistanceError to set
         */
        public void setEaDistanceError(final int eaDistanceError) {
            this.eaDistanceError = eaDistanceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schaOffen
         */
        public H2FeatureService getSchaOffen() {
            return schaOffen;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schaOffen  the schaOffen to set
         */
        public void setSchaOffen(final H2FeatureService schaOffen) {
            this.schaOffen = schaOffen;
        }

//        /**
//         * DOCUMENT ME!
//         *
//         * @return  the wehrGeschlossen
//         */
//        public H2FeatureService getWehrGeschlossen() {
//            return wehrGeschlossen;
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param  wehrGeschlossen  the wehrGeschlossen to set
//         */
//        public void setWehrGeschlossen(final H2FeatureService wehrGeschlossen) {
//            this.wehrGeschlossen = wehrGeschlossen;
//        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anlpOffen
         */
        public H2FeatureService getAnlpOffen() {
            return anlpOffen;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anlpOffen  the anlpOffen to set
         */
        public void setAnlpOffen(final H2FeatureService anlpOffen) {
            this.anlpOffen = anlpOffen;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anlpGeschlossen
         */
        public H2FeatureService getAnlpGeschlossen() {
            return anlpGeschlossen;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anlpGeschlossen  the anlpGeschlossen to set
         */
        public void setAnlpGeschlossen(final H2FeatureService anlpGeschlossen) {
            this.anlpGeschlossen = anlpGeschlossen;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anlpEsw
         */
        public H2FeatureService getAnlpEsw() {
            return anlpEsw;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anlpEsw  the anlpEsw to set
         */
        public void setAnlpEsw(final H2FeatureService anlpEsw) {
            this.anlpEsw = anlpEsw;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the schaOffenError
         */
        public int getSchaOffenError() {
            return schaOffenError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  schaOffenError  the schaOffenError to set
         */
        public void setSchaOffenError(final int schaOffenError) {
            this.schaOffenError = schaOffenError;
        }

//        /**
//         * DOCUMENT ME!
//         *
//         * @return  the wehrGeschlossenError
//         */
//        public int getWehrGeschlossenError() {
//            return wehrGeschlossenError;
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param  wehrGeschlossenError  the wehrGeschlossenError to set
//         */
//        public void setWehrGeschlossenError(final int wehrGeschlossenError) {
//            this.wehrGeschlossenError = wehrGeschlossenError;
//        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anlpOffenError
         */
        public int getAnlpOffenError() {
            return anlpOffenError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anlpOffenError  the anlpOffenError to set
         */
        public void setAnlpOffenError(final int anlpOffenError) {
            this.anlpOffenError = anlpOffenError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anlpGeschlossenError
         */
        public int getAnlpGeschlossenError() {
            return anlpGeschlossenError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anlpGeschlossenError  the anlpGeschlossenError to set
         */
        public void setAnlpGeschlossenError(final int anlpGeschlossenError) {
            this.anlpGeschlossenError = anlpGeschlossenError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anlpEswError
         */
        public int getAnlpEswError() {
            return anlpEswError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anlpEswError  the anlpEswError to set
         */
        public void setAnlpEswError(final int anlpEswError) {
            this.anlpEswError = anlpEswError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the krEsw
         */
        public H2FeatureService getKrEsw() {
            return krEsw;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  krEsw  the krEsw to set
         */
        public void setKrEsw(final H2FeatureService krEsw) {
            this.krEsw = krEsw;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the eaEsw
         */
        public H2FeatureService getEaEsw() {
            return eaEsw;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  eaEsw  the eaEsw to set
         */
        public void setEaEsw(final H2FeatureService eaEsw) {
            this.eaEsw = eaEsw;
        }

//        /**
//         * DOCUMENT ME!
//         *
//         * @return  the eaGeschl
//         */
//        public H2FeatureService getEaGeschl() {
//            return eaGeschl;
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param  eaGeschl  the eaGeschl to set
//         */
//        public void setEaGeschl(final H2FeatureService eaGeschl) {
//            this.eaGeschl = eaGeschl;
//        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anllGeschl
         */
        public H2FeatureService getAnllGeschl() {
            return anllGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anllGeschl  the anllGeschl to set
         */
        public void setAnllGeschl(final H2FeatureService anllGeschl) {
            this.anllGeschl = anllGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the krMarkedTwice
         */
        public H2FeatureService getKrMarkedTwice() {
            return krMarkedTwice;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  krMarkedTwice  the krMarkedTwice to set
         */
        public void setKrMarkedTwice(final H2FeatureService krMarkedTwice) {
            this.krMarkedTwice = krMarkedTwice;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the krEswError
         */
        public int getKrEswError() {
            return krEswError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  krEswError  the krEswError to set
         */
        public void setKrEswError(final int krEswError) {
            this.krEswError = krEswError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the eaEswError
         */
        public int getEaEswError() {
            return eaEswError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  eaEswError  the eaEswError to set
         */
        public void setEaEswError(final int eaEswError) {
            this.eaEswError = eaEswError;
        }

//        /**
//         * DOCUMENT ME!
//         *
//         * @return  the eaGeschlError
//         */
//        public int getEaGeschlError() {
//            return eaGeschlError;
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param  eaGeschlError  the eaGeschlError to set
//         */
//        public void setEaGeschlError(final int eaGeschlError) {
//            this.eaGeschlError = eaGeschlError;
//        }

        /**
         * DOCUMENT ME!
         *
         * @return  the anllGeschlError
         */
        public int getAnllGeschlError() {
            return anllGeschlError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  anllGeschlError  the anllGeschlError to set
         */
        public void setAnllGeschlError(final int anllGeschlError) {
            this.anllGeschlError = anllGeschlError;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the krMarkedTwiceError
         */
        public int getKrMarkedTwiceError() {
            return krMarkedTwiceError;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  krMarkedTwiceError  the krMarkedTwiceError to set
         */
        public void setKrMarkedTwiceError(final int krMarkedTwiceError) {
            this.krMarkedTwiceError = krMarkedTwiceError;
        }
    }
}
