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

import org.deegree.datatypes.Types;

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.custom.helper.SQLFormatter;
import de.cismet.cids.custom.watergis.server.search.BaWithIncompleteProfCoverage;
import de.cismet.cids.custom.watergis.server.search.FgBakCount;
import de.cismet.cids.custom.watergis.server.search.MergeBaBbef;
import de.cismet.cids.custom.watergis.server.search.MergeBaProf;
import de.cismet.cids.custom.watergis.server.search.MergeBaSbef;
import de.cismet.cids.custom.watergis.server.search.MergeBaUbef;
import de.cismet.cids.custom.watergis.server.search.OverlappedBBefWithProf;
import de.cismet.cids.custom.watergis.server.search.OverlappedBBefWithR;
import de.cismet.cids.custom.watergis.server.search.OverlappedProf;
import de.cismet.cids.custom.watergis.server.search.OverlappedProfWithR;
import de.cismet.cids.custom.watergis.server.search.OverlappedSBefWithProf;
import de.cismet.cids.custom.watergis.server.search.OverlappedSBefWithR;
import de.cismet.cids.custom.watergis.server.search.OverlappedUbefWithR;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitDialog;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * Issue 242.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AusbauCheckAction extends AbstractCheckAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final MetaClass FG_BA_SBEF = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_sbef");
    private static final MetaClass FG_BA_UBEF = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_ubef");
    private static final MetaClass FG_BA_BBEF = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_bbef");
    private static final MetaClass FG_BA_PROF = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_prof");
    private static final MetaClass FG_BA_RL = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_rl");
    private static final MetaClass FG_BA_D = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_d");
    private static final MetaClass FG_BA_DUE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_due");
    private static final int[] USED_CLASS_IDS = new int[] {
            ((FG_BA_SBEF != null) ? FG_BA_SBEF.getId() : -1),
            ((FG_BA_UBEF != null) ? FG_BA_UBEF.getId() : -1),
            ((FG_BA_BBEF != null) ? FG_BA_BBEF.getId() : -1),
            ((FG_BA_PROF != null) ? FG_BA_PROF.getId() : -1),
            ((FG_BA_RL != null) ? FG_BA_RL.getId() : -1),
            ((FG_BA_D != null) ? FG_BA_D.getId() : -1),
            ((FG_BA_DUE != null) ? FG_BA_DUE.getId() : -1)
        };
    private static String QUERY_PROF_ATTR;
    private static String QUERY_SBEF_ATTR;
    private static String QUERY_UBEF_ATTR;
    private static String QUERY_BBEF_ATTR;
    private static final String CHECK_AUSBAU_BBEFPROFBBEF__UEBERLAPPUNG_AT =
        "Prüfungen->Ausbau->BBEF->PROF/BBEF: Überlappung/Attribute";
    private static final String CHECK_AUSBAU_SBEFPROFSBEF__UEBERLAPPUNG_AT =
        "Prüfungen->Ausbau->SBEF->PROF/SBEF: Überlappung/Attribute";
    private static final String CHECK_AUSBAU_BBEFBBEF__UEBERLAPPUNG_MIT_R =
        "Prüfungen->Ausbau->BBEF->BBEF: Überlappung mit RL/D/Dü";
    private static final String CHECK_AUSBAU_UBEFUBEF__UEBERLAPPUNG_MIT_R =
        "Prüfungen->Ausbau->UBEF->UBEF: Überlappung mit RL/D/Dü";
    private static final String CHECK_AUSBAU_SBEFSBEF__UEBERLAPPUNG_MIT_R =
        "Prüfungen->Ausbau->SBEF->SBEF: Überlappung mit RL/D/Dü";
    private static final String CHECK_AUSBAU_PROFPROF__UEBERLAPPUNG_MIT_R =
        "Prüfungen->Ausbau->PROF->PROF: Überlappung mit RL/D/Dü";
    private static final String CHECK_AUSBAU_PROFPROF__UEBERLAPPUNG__THEMA =
        "Prüfungen->Ausbau->PROF->PROF: Überlappung im Thema";
    private static final String CHECK_AUSBAU_PROFPROF__LUECKE_IM__THEMA =
        "Prüfungen->Ausbau->PROF->PROF: Lücke im Thema";
    private static final String CHECK_AUSBAU_UBEFUBEF__ATTRIBUTE = "Prüfungen->Ausbau->UBEF->UBEF: Attribute";
    private static final String CHECK_AUSBAU_BBEFBBEF__ATTRIBUTE = "Prüfungen->Ausbau->BBEF->BBEF: Attribute";
    private static final String CHECK_AUSBAU_SBEFSBEF__ATTRIBUTE = "Prüfungen->Ausbau->SBEF->SBEF: Attribute";
    private static final String CHECK_AUSBAU_PROFPROF__ATTRIBUTE = "Prüfungen->Ausbau->PROF->PROF: Attribute";
    private static String[] ALL_CHECKS = new String[] {
            CHECK_AUSBAU_BBEFBBEF__ATTRIBUTE,
            CHECK_AUSBAU_BBEFBBEF__UEBERLAPPUNG_MIT_R,
            CHECK_AUSBAU_BBEFPROFBBEF__UEBERLAPPUNG_AT,
            CHECK_AUSBAU_PROFPROF__ATTRIBUTE,
            CHECK_AUSBAU_PROFPROF__LUECKE_IM__THEMA,
            CHECK_AUSBAU_PROFPROF__UEBERLAPPUNG_MIT_R,
            CHECK_AUSBAU_PROFPROF__UEBERLAPPUNG__THEMA,
            CHECK_AUSBAU_SBEFPROFSBEF__UEBERLAPPUNG_AT,
            CHECK_AUSBAU_SBEFSBEF__ATTRIBUTE,
            CHECK_AUSBAU_SBEFSBEF__UEBERLAPPUNG_MIT_R,
            CHECK_AUSBAU_UBEFUBEF__ATTRIBUTE,
            CHECK_AUSBAU_UBEFUBEF__UEBERLAPPUNG_MIT_R
        };

    static {
        if ((FG_BA_SBEF != null) && (FG_BA_UBEF != null) && (FG_BA_BBEF != null) && (FG_BA_PROF != null)) {
            final User user = SessionManager.getSession().getUser();

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_PROF_ATTR = "select distinct " + FG_BA_PROF.getID() + ", bef." + FG_BA_PROF.getPrimaryKey()
                            + " from dlm25w.fg_ba_prof bef\n"
                            + "join dlm25w.fg_ba_linie linie on (bef.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_profil kpr on (kpr.id = bef.profil)\n"
                            + "left join dlm25w.k_l_st kst on (kst.id = bef.l_st)\n"
                            + "left join dlm25w.k_traeger kt on (kt.id = bef.traeger)\n"
                            + "left join dlm25w.k_zust_kl kz on (kz.id = bef.zust_kl)\n"
                            + "where\n"
                            + " (%1$s is null or von.route = any(%1$s)) and (kpr.profil is null or (bef.l_st is not null and kst.l_st is null) or obj_nr is null\n"
                            + "or (bef.traeger is not null and kt.traeger is null)\n"
                            + "or (bef.zust_kl is not null and kz.zust_kl is null)\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 100))\n"
                            + "or (ho_e  is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a  is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle  is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (bv_re  is not null and bv_re <> 0 and (bv_re < 0.1 or bv_re > 15))\n"
                            + "or (bh_re  is not null and (bh_re <= 0 or bh_re > 15))\n"
                            + "or (bl_re  is not null and (bl_re <= 0 or bl_re > 30))\n"
                            + "or (bv_li  is not null and bv_li <> 0 and (bv_li < 0.1 or bv_li > 15))\n"
                            + "or (bh_li  is not null and (bh_li <= 0 or bh_li > 15))\n"
                            + "or (bl_li  is not null and (bl_li <= 0 or bl_li > 30))\n"
                            + "or (mw  is not null and (mw <= 0 or mw > 30))\n"
                            + "or abs(bis.wert - von.wert) < 0.5\n"
                            + "or (ho_e is not null and ho_a is not null and (ho_e < ho_a))\n"
                            + "or (kpr.profil = 're' and (bv_re is not null and bv_re <> 0))\n"
                            + "or (kpr.profil = 're' and ((bv_re is not null and bv_re <> 0) or (bv_li is not null and bv_li <> 0) ) )\n"
                            + "or (kpr.profil = 'tr' and ((bv_re is not null and bv_re = 0) and (bv_li is not null and bv_li = 0) ) )\n"
                            + ");";
            } else {
                QUERY_PROF_ATTR = "select distinct " + FG_BA_PROF.getID() + ", bef." + FG_BA_PROF.getPrimaryKey()
                            + " from dlm25w.fg_ba_prof bef\n"
                            + "join dlm25w.fg_ba_linie linie on (bef.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_profil kpr on (kpr.id = bef.profil)\n"
                            + "left join dlm25w.k_l_st kst on (kst.id = bef.l_st)\n"
                            + "left join dlm25w.k_traeger kt on (kt.id = bef.traeger)\n"
                            + "left join dlm25w.k_zust_kl kz on (kz.id = bef.zust_kl)\n"
                            + "where\n"
                            + " (%1$s is null or von.route = any(%1$s)) and (kpr.profil is null or (bef.l_st is not null and kst.l_st is null) or obj_nr is null\n"
                            + "or (bef.traeger is not null and kt.traeger is null)\n"
                            + "or (bef.zust_kl is not null and kz.zust_kl is null)\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 100))\n"
                            + "or (ho_e  is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a  is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle  is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (bv_re  is not null and bv_re <> 0 and (bv_re < 0.1 or bv_re > 15))\n"
                            + "or (bh_re  is not null and (bh_re <= 0 or bh_re > 15))\n"
                            + "or (bl_re  is not null and (bl_re <= 0 or bl_re > 30))\n"
                            + "or (bv_li  is not null and bv_li <> 0 and (bv_li < 0.1 or bv_li > 15))\n"
                            + "or (bh_li  is not null and (bh_li <= 0 or bh_li > 15))\n"
                            + "or (bl_li  is not null and (bl_li <= 0 or bl_li > 30))\n"
                            + "or (mw  is not null and (mw <= 0 or mw > 30))\n"
                            + "or abs(bis.wert - von.wert) < 0.5\n"
                            + "or (ho_e is not null and ho_a is not null and (ho_e < ho_a))\n"
                            + "or (kpr.profil = 're' and (bv_re is not null and bv_re <> 0))\n"
                            + "or (kpr.profil = 're' and ((bv_re is not null and bv_re <> 0) or (bv_li is not null and bv_li <> 0) ) )\n"
                            + "or (kpr.profil = 'tr' and ((bv_re is not null and bv_re = 0) and (bv_li is not null and bv_li = 0) ) )\n"
                            + ") and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s);";
            }

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_UBEF_ATTR = "select distinct " + FG_BA_UBEF.getID() + ", bef." + FG_BA_UBEF.getPrimaryKey()
                            + " from dlm25w.fg_ba_ubef bef\n"
                            + "join dlm25w.fg_ba_linie linie on (bef.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_ubef kbef on (kbef.id = bef.ubef)\n"
                            + "left join dlm25w.k_l_rl krl on (krl.id = bef.l_rl)\n"
                            + "left join dlm25w.k_l_st kst on (kst.id = bef.l_st)\n"
                            + "left join dlm25w.k_material km on (km.id = bef.material)\n"
                            + "left join dlm25w.k_traeger kt on (kt.id = bef.traeger)\n"
                            + "left join dlm25w.k_zust_kl kz on (kz.id = bef.zust_kl)\n"
                            + "where\n"
                            + "(%1$s is null or von.route = any(%1$s)) and (kbef.ubef is null or (bef.l_st is not null and kst.l_st is null) or krl.l_rl is null or obj_nr is null\n"
                            + "or (bef.material is not null and km.material is null) or (bef.traeger is not null and kt.traeger is null)\n"
                            + "or (bef.zust_kl is not null and kz.zust_kl is null)\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 10))\n"
                            + "or (ho_d_o  is not null and (ho_d_o <= 0 or ho_d_o > 15))\n"
                            + "or abs(bis.wert - von.wert) < 0.5\n"
                            + "or (kbef.ubef = 'Fa' and km.material not in ('H-Rsg', 'Kok'))\n"
                            + "or (kbef.ubef = 'Gtr' and km.material not in ('B', 'K'))\n"
                            + "or (kbef.ubef = 'Mte' and km.material <> 'Vl')\n"
                            + "or (kbef.ubef = 'Pfr' and km.material not in ('B', 'H', 'K', 'St'))\n"
                            + "or (kbef.ubef = 'Pl' and km.material not in ('B'))\n"
                            + "or (kbef.ubef = 'SP' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Wb'))\n"
                            + "or (kbef.ubef = 'Spw' and km.material not in ('H', 'K', 'St', 'St-B', 'Ste-Gab'))\n"
                            + "or (kbef.ubef = 'Wistü' and km.material not in ('B', 'K', 'St', 'St-B'))\n"
                            + "or (bef.esw is not null and (bef.esw < 0 or bef.esw > 1)) "
                            + ")";
            } else {
                QUERY_UBEF_ATTR = "select distinct " + FG_BA_UBEF.getID() + ", bef." + FG_BA_UBEF.getPrimaryKey()
                            + " from dlm25w.fg_ba_ubef bef\n"
                            + "join dlm25w.fg_ba_linie linie on (bef.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_ubef kbef on (kbef.id = bef.ubef)\n"
                            + "left join dlm25w.k_l_rl krl on (krl.id = bef.l_rl)\n"
                            + "left join dlm25w.k_l_st kst on (kst.id = bef.l_st)\n"
                            + "left join dlm25w.k_material km on (km.id = bef.material)\n"
                            + "left join dlm25w.k_traeger kt on (kt.id = bef.traeger)\n"
                            + "left join dlm25w.k_zust_kl kz on (kz.id = bef.zust_kl)\n"
                            + "where\n"
                            + "(%1$s is null or von.route = any(%1$s)) and (kbef.ubef is null or (bef.l_st is not null and kst.l_st is null) or krl.l_rl is null or obj_nr is null\n"
                            + "or (bef.material is not null and km.material is null) or (bef.traeger is not null and kt.traeger is null)\n"
                            + "or (bef.zust_kl is not null and kz.zust_kl is null)\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 10))\n"
                            + "or (ho_d_o  is not null and (ho_d_o <= 0 or ho_d_o > 15))\n"
                            + "or abs(bis.wert - von.wert) < 0.5\n"
                            + "or (kbef.ubef = 'Fa' and km.material not in ('H-Rsg', 'Kok'))\n"
                            + "or (kbef.ubef = 'Gtr' and km.material not in ('B', 'K'))\n"
                            + "or (kbef.ubef = 'Mte' and km.material <> 'Vl')\n"
                            + "or (kbef.ubef = 'Pfr' and km.material not in ('B', 'H', 'K', 'St'))\n"
                            + "or (kbef.ubef = 'Pl' and km.material not in ('B'))\n"
                            + "or (kbef.ubef = 'SP' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Wb'))\n"
                            + "or (kbef.ubef = 'Spw' and km.material not in ('H', 'K', 'St', 'St-B', 'Ste-Gab'))\n"
                            + "or (kbef.ubef = 'Wistü' and km.material not in ('B', 'K', 'St', 'St-B'))\n"
                            + "or (bef.esw is not null and (bef.esw < 0 or bef.esw > 1)) "
                            + ") and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s);";
            }

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_SBEF_ATTR = "select distinct " + FG_BA_SBEF.getID() + ", bef." + FG_BA_SBEF.getPrimaryKey()
                            + " from dlm25w.fg_ba_sbef bef\n"
                            + "join dlm25w.fg_ba_linie linie on (bef.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_sbef kbef on (kbef.id = bef.sbef)\n"
                            + "left join dlm25w.k_material km on (km.id = bef.material)\n"
                            + "where\n"
                            + "(%1$s is null or von.route = any(%1$s)) and (kbef.sbef is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 100))\n"
                            + "or (ho_e  is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a  is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle  is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (ho_d_e  is not null and (ho_d_e < 0 or ho_d_e > 5))\n"
                            + "or (ho_d_a  is not null and (ho_d_a < 0 or ho_d_a > 5))\n"
                            + "or abs(bis.wert - von.wert) < 0.1\n"
                            + "or (ho_e is not null and ho_a is not null and (ho_e < ho_a))\n"
                            + "or (kbef.sbef = 'Buh' and km.material not in ('H', 'K', 'Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wab'))\n"
                            + "or (kbef.sbef = 'Pf' and km.material not in ('B', 'H', 'K'))\n"
                            + "or (kbef.sbef = 'Pfl' and km.material not in ('B', 'Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wb'))\n"
                            + "or (kbef.sbef = 'Pfr' and km.material not in ('B', 'H', 'K'))\n"
                            + "or (kbef.sbef = 'Pl' and km.material not in ('B'))\n"
                            + "or (kbef.sbef = 'Rgl' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wb'))\n"
                            + "or (kbef.sbef in ('So-Ab', 'So-Abt', 'So-Gl', 'So-Ra') and km.material <> 'B')\n"
                            + "or (kbef.sbef = 'SP' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wb'))\n"
                            + "or (kbef.sbef = 'Stöste' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Wb'))\n"
                            + "or (kbef.sbef in ('Sw-Gru', 'Sw-So', 'Sw-Stü') and km.material <> 'B')\n"
                            + "or (kbef.sbef = 'Wu' and km.material <> 'H')\n"
                            + "or (bef.esw is not null and (bef.esw < 0 or bef.esw > 1)) "
                            + ")";
            } else {
                QUERY_SBEF_ATTR = "select distinct " + FG_BA_SBEF.getID() + ", bef." + FG_BA_SBEF.getPrimaryKey()
                            + " from dlm25w.fg_ba_sbef bef\n"
                            + "join dlm25w.fg_ba_linie linie on (bef.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_sbef kbef on (kbef.id = bef.sbef)\n"
                            + "left join dlm25w.k_material km on (km.id = bef.material)\n"
                            + "where\n"
                            + "(%1$s is null or von.route = any(%1$s)) and (kbef.sbef is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 100))\n"
                            + "or (ho_e  is not null and (ho_e < -6 or ho_e > 179))\n"
                            + "or (ho_a  is not null and (ho_a < -6 or ho_a > 179))\n"
                            + "or (gefaelle  is not null and (gefaelle < -10 or gefaelle > 100))\n"
                            + "or (ho_d_e  is not null and (ho_d_e < 0 or ho_d_e > 5))\n"
                            + "or (ho_d_a  is not null and (ho_d_a < 0 or ho_d_a > 5))\n"
                            + "or abs(bis.wert - von.wert) < 0.1\n"
                            + "or (ho_e is not null and ho_a is not null and (ho_e < ho_a))\n"
                            + "or (kbef.sbef = 'Buh' and km.material not in ('H', 'K', 'Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wab'))\n"
                            + "or (kbef.sbef = 'Gtr' and km.material not in ('B', 'Ste-Fs', 'K'))\n"
                            + "or (kbef.sbef = 'Pf' and km.material not in ('B', 'H', 'K'))\n"
                            + "or (kbef.sbef = 'Pfl' and km.material not in ('B', 'Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wb'))\n"
                            + "or (kbef.sbef = 'Pfr' and km.material not in ('B', 'H', 'K'))\n"
                            + "or (kbef.sbef = 'Pl' and km.material not in ('B'))\n"
                            + "or (kbef.sbef = 'Rgl' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wb'))\n"
                            + "or (kbef.sbef in ('So-Ab', 'So-Abt', 'So-Gl', 'So-Ra') and km.material <> 'B')\n"
                            + "or (kbef.sbef = 'SP' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wb'))\n"
                            + "or (kbef.sbef = 'Stöste' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Wb'))\n"
                            + "or (kbef.sbef in ('Sw-Gru', 'Sw-So', 'Sw-Stü') and km.material <> 'B')\n"
                            + "or (kbef.sbef = 'Wu' and km.material <> 'H')\n"
                            + "or (bef.esw is not null and (bef.esw < 0 or bef.esw > 1)) "
                            + ") and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s);";
            }

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_BBEF_ATTR = "select distinct " + FG_BA_BBEF.getID() + ", bef." + FG_BA_BBEF.getPrimaryKey()
                            + " from dlm25w.fg_ba_bbef bef\n"
                            + "join dlm25w.fg_ba_linie linie on (bef.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_bbef kbef on (kbef.id = bef.bbef)\n"
                            + "left join dlm25w.k_l_rl krl on (krl.id = bef.l_rl)\n"
                            + "left join dlm25w.k_material km on (km.id = bef.material)\n"
                            + "where\n"
                            + "(%1$s is null or von.route = any(%1$s)) and (kbef.bbef is null or krl.l_rl is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 30))\n"
                            + "or (ho_d_o  is not null and (ho_d_o <= 0 or ho_d_o > 15))\n"
                            + "or (ho_d_u  is not null and (ho_d_u < 0 or ho_d_u > 15))\n"
                            + "or (kbef.bbef = 'Rin' and (abs(bis.wert - von.wert) <= 0 or abs(bis.wert - von.wert) > 10))\n"
                            + "or (kbef.bbef <> 'Rin' and (abs(bis.wert - von.wert) <= 0.5))\n"
                            + "or (ho_d_o is not null and ho_d_u is not null and (ho_d_o <= ho_d_u))\n"
                            + "or (kbef.bbef = 'Berme' and km.material is not null)\n"
                            + "or (kbef.bbef = 'Fa' and km.material not in ('H-Rsg', 'Kok', 'H'))\n"
                            + "or (kbef.bbef = 'SP' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wb'))\n"
                            + "or (kbef.bbef = 'Gtr' and km.material not in ('B', 'K', 'Ste-Fs'))\n"
                            + "or (kbef.bbef = 'Mte' and km.material not in ('Ste-Gab', 'Vl'))\n"
                            + "or (kbef.bbef = 'Pfl' and km.material not in ('B'))\n"
                            + "or (kbef.bbef = 'Pfr' and km.material not in ('B', 'H', 'K', 'St'))\n"
                            + "or (kbef.bbef = 'Pl' and km.material <> 'B')\n"
                            + "or (kbef.bbef = 'Rin' and km.material not in ('B', 'St-B', 'Ste', 'Ste-Fs', 'Ste-Mw', 'Ste-Wb'))\n"
                            + "or (kbef.bbef = 'Spreit' and km.material <> 'H')\n"
                            + "or (bef.esw is not null and (bef.esw < 0 or bef.esw > 1)) "
                            + ") ";
            } else {
                QUERY_BBEF_ATTR = "select distinct " + FG_BA_BBEF.getID() + ", bef." + FG_BA_BBEF.getPrimaryKey()
                            + " from dlm25w.fg_ba_bbef bef\n"
                            + "join dlm25w.fg_ba_linie linie on (bef.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_ba ba on (ba.id = von.route)\n"
                            + "join dlm25w.fg_bak bak on (bak.id = ba.bak_id)\n"
                            + "left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "left join dlm25w.k_bbef kbef on (kbef.id = bef.bbef)\n"
                            + "left join dlm25w.k_l_rl krl on (krl.id = bef.l_rl)\n"
                            + "left join dlm25w.k_material km on (km.id = bef.material)\n"
                            + "where\n"
                            + "(%1$s is null or von.route = any(%1$s)) and (kbef.bbef is null or krl.l_rl is null or obj_nr is null\n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br  is not null and (br <= 0 or br > 30))\n"
                            + "or (ho_d_o  is not null and (ho_d_o <= 0 or ho_d_o > 15))\n"
                            + "or (ho_d_u  is not null and (ho_d_u < 0 or ho_d_u > 15))\n"
                            + "or (kbef.bbef = 'Rin' and (abs(bis.wert - von.wert) <= 0 or abs(bis.wert - von.wert) > 10))\n"
                            + "or (kbef.bbef <> 'Rin' and (abs(bis.wert - von.wert) <= 0.5))\n"
                            + "or (ho_d_o is not null and ho_d_u is not null and (ho_d_o <= ho_d_u))\n"
                            + "or (kbef.bbef = 'Berme' and km.material is not null)\n"
                            + "or (kbef.bbef = 'Fa' and km.material not in ('H-Rsg', 'Kok', 'H'))\n"
                            + "or (kbef.bbef = 'SP' and km.material not in ('Ste', 'Ste-Fs', 'Ste-Gab', 'Ste-Wb'))\n"
                            + "or (kbef.bbef = 'Gtr' and km.material not in ('B', 'K', 'Ste-Fs'))\n"
                            + "or (kbef.bbef = 'Mte' and km.material not in ('Ste-Gab', 'Vl'))\n"
                            + "or (kbef.bbef = 'Pfl' and km.material not in ('B'))\n"
                            + "or (kbef.bbef = 'Pfr' and km.material not in ('B', 'H', 'K', 'St'))\n"
                            + "or (kbef.bbef = 'Pl' and km.material <> 'B')\n"
                            + "or (kbef.bbef = 'Rin' and km.material not in ('B', 'St-B', 'Ste', 'Ste-Fs', 'Ste-Mw', 'Ste-Wb'))\n"
                            + "or (kbef.bbef = 'Spreit' and km.material <> 'H')\n"
                            + "or (bef.esw is not null and (bef.esw < 0 or bef.esw > 1)) "
                            + ") and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s);";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    // dlm25w.merge_fg_bak_gwk()
    private boolean successful = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AusbauCheckAction object.
     */
    public AusbauCheckAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                AusbauCheckAction.class,
                "AusbauCheckAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(AusbauCheckAction.class,
                "AusbauCheckAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-zoom.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getProgressSteps() {
        return 17;
    }

    @Override
    public boolean startCheckInternal(final boolean isExport,
            final WaitDialog wd,
            final List<H2FeatureService> result) {
        if (isExport) {
            try {
                final CheckResult cr = check(isExport, wd);

                if (result != null) {
                    addService(result, cr.getBbefAttr());
                    addService(result, cr.getBbefGeschl());
                    addService(result, cr.getBbefOverlapsAttr());
                    addService(result, cr.getProfAttr());
                    addService(result, cr.getProfGeschl());
                    addService(result, cr.getProfHole());
                    addService(result, cr.getProfOverlaps());
                    addService(result, cr.getSbefAttr());
                    addService(result, cr.getSbefGeschl());
                    addService(result, cr.getSbefOverlapsAttr());
                    addService(result, cr.getUbefAttr());
                    addService(result, cr.getUbefGeschl());
                }
            } catch (Exception e) {
                LOG.error("Error while performing check", e);

                return false;
            }

            return true;
        } else {
            final int[] selectedIds = getIdsOfSelectedObjects("fg_ba");
            final String message = ((selectedIds == null)
                    ? "Es werden alle Routen geprüft"
                    : ("Es werden die " + selectedIds.length + " selektierten Routen geprüft"));

            final int ans = JOptionPane.showConfirmDialog(StaticSwingTools.getParentFrame(
                        AppBroker.getInstance().getWatergisApp()),
                    message,
                    "Prüfung starten",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            if (ans == JOptionPane.CANCEL_OPTION) {
                return false;
            }

            final WaitingDialogThread<CheckResult> wdt = new WaitingDialogThread<CheckResult>(
                    StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                    true,
                    NbBundle.getMessage(AusbauCheckAction.class,
                        "AusbauCheckAction.actionPerformed().dialog"),
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

                            if ((result.getProblemTreeObjectCount() == null)
                                        || (result.getProblemTreeObjectCount().getCount() == 0)) {
                                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                    NbBundle.getMessage(
                                        AusbauCheckAction.class,
                                        "AusbauCheckAction.actionPerformed().result.text.withoutProblems",
                                        new Object[] {
                                            result.getBakCount(),
                                            result.getProfAttrErrors(),
                                            result.getSbefAttrErrors(),
                                            result.getUbefAttrErrors(),
                                            result.getBbefAttrErrors(),
                                            result.getProfGeschlErrors(),
                                            result.getSbefGeschlErrors(),
                                            result.getUbefGeschlErrors(),
                                            result.getBbefGeschlErrors(),
                                            result.getProfOverlapsErrors(),
                                            result.getProfHoleErrors(),
                                            result.getSbefOverlapsAttrErrors(),
                                            result.getBbefOverlapsAttrErrors(),
                                            0
                                        }),
                                    NbBundle.getMessage(
                                        AusbauCheckAction.class,
                                        "AusbauCheckAction.actionPerformed().result.title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                    NbBundle.getMessage(
                                        AusbauCheckAction.class,
                                        "AusbauCheckAction.actionPerformed().result.text",
                                        new Object[] {
                                            result.getBakCount(),
                                            result.getProfAttrErrors(),
                                            result.getSbefAttrErrors(),
                                            result.getUbefAttrErrors(),
                                            result.getBbefAttrErrors(),
                                            result.getProfGeschlErrors(),
                                            result.getSbefGeschlErrors(),
                                            result.getUbefGeschlErrors(),
                                            result.getBbefGeschlErrors(),
                                            result.getProfOverlapsErrors(),
                                            result.getProfHoleErrors(),
                                            result.getSbefOverlapsAttrErrors(),
                                            result.getBbefOverlapsAttrErrors(),
                                            result.getProblemTreeObjectCount().getCount(),
                                            result.getProblemTreeObjectCount().getClasses()
                                        }),
                                    NbBundle.getMessage(
                                        AusbauCheckAction.class,
                                        "AusbauCheckAction.actionPerformed().result.title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            }

                            if (result.getBbefOverlapsAttr() != null) {
                                showService(result.getBbefOverlapsAttr());
                            }
                            if (result.getBbefGeschl() != null) {
                                showService(result.getBbefGeschl());
                            }
                            if (result.getBbefAttr() != null) {
                                showService(result.getBbefAttr());
                            }
                            if (result.getUbefGeschl() != null) {
                                showService(result.getUbefGeschl());
                            }
                            if (result.getUbefAttr() != null) {
                                showService(result.getUbefAttr());
                            }
                            if (result.getSbefOverlapsAttr() != null) {
                                showService(result.getSbefOverlapsAttr());
                            }
                            if (result.getSbefGeschl() != null) {
                                showService(result.getSbefGeschl());
                            }
                            if (result.getSbefAttr() != null) {
                                showService(result.getSbefAttr());
                            }
                            if (result.getProfHole() != null) {
                                showService(result.getProfHole());
                            }
                            if (result.getProfOverlaps() != null) {
                                showService(result.getProfOverlaps());
                            }
                            if (result.getProfGeschl() != null) {
                                showService(result.getProfGeschl());
                            }
                            if (result.getProfAttr() != null) {
                                showService(result.getProfAttr());
                            }

                            refreshTree();
                            refreshMap();
                        } catch (Exception e) {
                            LOG.error("Error while performing the ausbau analyse.", e);
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

        if (user.equalsIgnoreCase("Administratoren")) {
            user = null;
        }

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

        removeServicesFromDb(ALL_CHECKS);

        // start auto correction
        final CidsServerSearch mergeProf = new MergeBaProf(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeProf);
        increaseProgress(wd, 1);
        final CidsServerSearch mergeSbef = new MergeBaSbef(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeSbef);
        increaseProgress(wd, 1);
        final CidsServerSearch mergeUbef = new MergeBaUbef(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeUbef);
        increaseProgress(wd, 1);
        final CidsServerSearch mergeBBef = new MergeBaBbef(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeBBef);
        increaseProgress(wd, 1);

        final List<FeatureServiceAttribute> baProfServiceAttributeDefinition = new ArrayList<FeatureServiceAttribute>();
        FeatureServiceAttribute serviceAttribute = new FeatureServiceAttribute(
                "id",
                String.valueOf(Types.INTEGER),
                true);
        baProfServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true);
        baProfServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ww_gr", String.valueOf(Types.INTEGER), true);
        baProfServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_cd", String.valueOf(Types.VARCHAR), true);
        baProfServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_von", String.valueOf(Types.DOUBLE), true);
        baProfServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_bis", String.valueOf(Types.DOUBLE), true);
        baProfServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("laenge", String.valueOf(Types.DOUBLE), true);
        baProfServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_date", String.valueOf(Types.TIMESTAMP), true);
        baProfServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_user", String.valueOf(Types.VARCHAR), true);
        baProfServiceAttributeDefinition.add(serviceAttribute);

        // start checks
        final boolean useExpCond = user != null;
        final boolean export = isExport && useExpCond;
        final String expCondition = ((isExport && useExpCond)
                ? (" exists(select id from dlm25w.fg_ba_exp_complete where owner = '" + user + "' and bak_id = bak.id)")
                : "false");
        String query = (useExpCond
                ? String.format(QUERY_BBEF_ATTR, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_BBEF_ATTR, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setBbefAttr(analyseByQuery(
                FG_BA_BBEF,
                query,
                CHECK_AUSBAU_BBEFBBEF__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setUbefGeschl(analyseByCustomSearch(
                new OverlappedUbefWithR(user, selectedIds, export),
                CHECK_AUSBAU_UBEFUBEF__UEBERLAPPUNG_MIT_R,
                baProfServiceAttributeDefinition));
        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_PROF_ATTR, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_PROF_ATTR, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setProfAttr(analyseByQuery(
                FG_BA_PROF,
                query,
                CHECK_AUSBAU_PROFPROF__ATTRIBUTE));
        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_UBEF_ATTR, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_UBEF_ATTR, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setUbefAttr(analyseByQuery(
                FG_BA_UBEF,
                query,
                CHECK_AUSBAU_UBEFUBEF__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setProfHole(analyseByCustomSearch(
                new BaWithIncompleteProfCoverage(user, selectedIds, export),
                CHECK_AUSBAU_PROFPROF__LUECKE_IM__THEMA,
                baProfServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setProfOverlaps(analyseByCustomSearch(
                new OverlappedProf(user, selectedIds, export),
                CHECK_AUSBAU_PROFPROF__UEBERLAPPUNG__THEMA,
                baProfServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setProfGeschl(analyseByCustomSearch(
                new OverlappedProfWithR(user, selectedIds, export),
                CHECK_AUSBAU_PROFPROF__UEBERLAPPUNG_MIT_R,
                baProfServiceAttributeDefinition));
        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_SBEF_ATTR, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_SBEF_ATTR, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setSbefAttr(analyseByQuery(
                FG_BA_SBEF,
                query,
                CHECK_AUSBAU_SBEFSBEF__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setSbefGeschl(analyseByCustomSearch(
                new OverlappedSBefWithR(user, selectedIds, export),
                CHECK_AUSBAU_SBEFSBEF__UEBERLAPPUNG_MIT_R,
                baProfServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setBbefGeschl(analyseByCustomSearch(
                new OverlappedBBefWithR(user, selectedIds, export),
                CHECK_AUSBAU_BBEFBBEF__UEBERLAPPUNG_MIT_R,
                baProfServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setSbefOverlapsAttr(analyseByCustomSearch(
                new OverlappedSBefWithProf(user, selectedIds, export),
                CHECK_AUSBAU_SBEFPROFSBEF__UEBERLAPPUNG_AT,
                baProfServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setBbefOverlapsAttr(analyseByCustomSearch(
                new OverlappedBBefWithProf(user, selectedIds, export),
                CHECK_AUSBAU_BBEFPROFBBEF__UEBERLAPPUNG_AT,
                baProfServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setProblemTreeObjectCount(getErrorObjectsFromTree(user, selectedIds, USED_CLASS_IDS, isExport));

        if (result.getBbefAttr() != null) {
            result.setBbefAttrErrors(result.getBbefAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getBbefGeschl() != null) {
            result.setBbefGeschlErrors(result.getBbefGeschl().getFeatureCount(null));
            successful = false;
        }

        if (result.getBbefOverlapsAttr() != null) {
            result.setBbefOverlapsAttrErrors(result.getBbefOverlapsAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getProfAttr() != null) {
            result.setProfAttrErrors(result.getProfAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getProfGeschl() != null) {
            result.setProfGeschlErrors(result.getProfGeschl().getFeatureCount(null));
            successful = false;
        }

        if (result.getProfHole() != null) {
            result.setProfHoleErrors(result.getProfHole().getFeatureCount(null));
            successful = false;
        }

        if (result.getProfOverlaps() != null) {
            result.setProfOverlapsErrors(result.getProfOverlaps().getFeatureCount(null));
            successful = false;
        }

        if (result.getSbefAttr() != null) {
            result.setSbefAttrErrors(result.getSbefAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getSbefGeschl() != null) {
            result.setSbefGeschlErrors(result.getSbefGeschl().getFeatureCount(null));
            successful = false;
        }

        if (result.getSbefOverlapsAttr() != null) {
            result.setSbefOverlapsAttrErrors(result.getSbefOverlapsAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getUbefAttr() != null) {
            result.setUbefAttrErrors(result.getUbefAttr().getFeatureCount(null));
            successful = false;
        }

        if (result.getUbefGeschl() != null) {
            result.setUbefGeschlErrors(result.getUbefGeschl().getFeatureCount(null));
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
        private int profAttrErrors;
        private int sbefAttrErrors;
        private int bbefAttrErrors;
        private int ubefAttrErrors;
        private int profGeschlErrors;
        private int sbefGeschlErrors;
        private int ubefGeschlErrors;
        private int bbefGeschlErrors;
        private int profOverlapsErrors;
        private int profHoleErrors;
        private int sbefOverlapsAttrErrors;
        private int bbefOverlapsAttrErrors;
        private ProblemCountAndClasses problemTreeObjectCount;
        private H2FeatureService profAttr;
        private H2FeatureService sbefAttr;
        private H2FeatureService bbefAttr;
        private H2FeatureService ubefAttr;
        private H2FeatureService profGeschl;
        private H2FeatureService sbefGeschl;
        private H2FeatureService ubefGeschl;
        private H2FeatureService bbefGeschl;
        private H2FeatureService profOverlaps;
        private H2FeatureService profHole;
        private H2FeatureService sbefOverlapsAttr;
        private H2FeatureService bbefOverlapsAttr;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the problemTreeObjectCount
         */
        public ProblemCountAndClasses getProblemTreeObjectCount() {
            return problemTreeObjectCount;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  problemTreeObjectCount  the problemTreeObjectCount to set
         */
        public void setProblemTreeObjectCount(final ProblemCountAndClasses problemTreeObjectCount) {
            this.problemTreeObjectCount = problemTreeObjectCount;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the profAttrErrors
         */
        public int getProfAttrErrors() {
            return profAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  profAttrErrors  the profAttrErrors to set
         */
        public void setProfAttrErrors(final int profAttrErrors) {
            this.profAttrErrors = profAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the sbefAttrErrors
         */
        public int getSbefAttrErrors() {
            return sbefAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sbefAttrErrors  the sbefAttrErrors to set
         */
        public void setSbefAttrErrors(final int sbefAttrErrors) {
            this.sbefAttrErrors = sbefAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the bbefAttrErrors
         */
        public int getBbefAttrErrors() {
            return bbefAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  bbefAttrErrors  the bbefAttrErrors to set
         */
        public void setBbefAttrErrors(final int bbefAttrErrors) {
            this.bbefAttrErrors = bbefAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the ubefAttrErrors
         */
        public int getUbefAttrErrors() {
            return ubefAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  ubefAttrErrors  the ubefAttrErrors to set
         */
        public void setUbefAttrErrors(final int ubefAttrErrors) {
            this.ubefAttrErrors = ubefAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the profGeschlErrors
         */
        public int getProfGeschlErrors() {
            return profGeschlErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  profGeschlErrors  the profGeschlErrors to set
         */
        public void setProfGeschlErrors(final int profGeschlErrors) {
            this.profGeschlErrors = profGeschlErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the sbefGeschlErrors
         */
        public int getSbefGeschlErrors() {
            return sbefGeschlErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sbefGeschlErrors  the sbefGeschlErrors to set
         */
        public void setSbefGeschlErrors(final int sbefGeschlErrors) {
            this.sbefGeschlErrors = sbefGeschlErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the ubefGeschlErrors
         */
        public int getUbefGeschlErrors() {
            return ubefGeschlErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  ubefGeschlErrors  the ubefGeschlErrors to set
         */
        public void setUbefGeschlErrors(final int ubefGeschlErrors) {
            this.ubefGeschlErrors = ubefGeschlErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the bbefGeschlErrors
         */
        public int getBbefGeschlErrors() {
            return bbefGeschlErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  bbefGeschlErrors  the bbefGeschlErrors to set
         */
        public void setBbefGeschlErrors(final int bbefGeschlErrors) {
            this.bbefGeschlErrors = bbefGeschlErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the profOverlapsErrors
         */
        public int getProfOverlapsErrors() {
            return profOverlapsErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  profOverlapsErrors  the profOverlapsErrors to set
         */
        public void setProfOverlapsErrors(final int profOverlapsErrors) {
            this.profOverlapsErrors = profOverlapsErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the profHoleErrors
         */
        public int getProfHoleErrors() {
            return profHoleErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  profHoleErrors  the profHoleErrors to set
         */
        public void setProfHoleErrors(final int profHoleErrors) {
            this.profHoleErrors = profHoleErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the sbefOverlapsAttrErrors
         */
        public int getSbefOverlapsAttrErrors() {
            return sbefOverlapsAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sbefOverlapsAttrErrors  the sbefOverlapsAttrErrors to set
         */
        public void setSbefOverlapsAttrErrors(final int sbefOverlapsAttrErrors) {
            this.sbefOverlapsAttrErrors = sbefOverlapsAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the bbefOverlapsAttrErrors
         */
        public int getBbefOverlapsAttrErrors() {
            return bbefOverlapsAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  bbefOverlapsAttrErrors  the bbefOverlapsAttrErrors to set
         */
        public void setBbefOverlapsAttrErrors(final int bbefOverlapsAttrErrors) {
            this.bbefOverlapsAttrErrors = bbefOverlapsAttrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the profAttr
         */
        public H2FeatureService getProfAttr() {
            return profAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  profAttr  the profAttr to set
         */
        public void setProfAttr(final H2FeatureService profAttr) {
            this.profAttr = profAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the sbefAttr
         */
        public H2FeatureService getSbefAttr() {
            return sbefAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sbefAttr  the sbefAttr to set
         */
        public void setSbefAttr(final H2FeatureService sbefAttr) {
            this.sbefAttr = sbefAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the bbefAttr
         */
        public H2FeatureService getBbefAttr() {
            return bbefAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  bbefAttr  the bbefAttr to set
         */
        public void setBbefAttr(final H2FeatureService bbefAttr) {
            this.bbefAttr = bbefAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the ubefAttr
         */
        public H2FeatureService getUbefAttr() {
            return ubefAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  ubefAttr  the ubefAttr to set
         */
        public void setUbefAttr(final H2FeatureService ubefAttr) {
            this.ubefAttr = ubefAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the profGeschl
         */
        public H2FeatureService getProfGeschl() {
            return profGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  profGeschl  the profGeschl to set
         */
        public void setProfGeschl(final H2FeatureService profGeschl) {
            this.profGeschl = profGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the sbefGeschl
         */
        public H2FeatureService getSbefGeschl() {
            return sbefGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sbefGeschl  the sbefGeschl to set
         */
        public void setSbefGeschl(final H2FeatureService sbefGeschl) {
            this.sbefGeschl = sbefGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the ubefGeschl
         */
        public H2FeatureService getUbefGeschl() {
            return ubefGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  ubefGeschl  the ubefGeschl to set
         */
        public void setUbefGeschl(final H2FeatureService ubefGeschl) {
            this.ubefGeschl = ubefGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the bbefGeschl
         */
        public H2FeatureService getBbefGeschl() {
            return bbefGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  bbefGeschl  the bbefGeschl to set
         */
        public void setBbefGeschl(final H2FeatureService bbefGeschl) {
            this.bbefGeschl = bbefGeschl;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the profOverlaps
         */
        public H2FeatureService getProfOverlaps() {
            return profOverlaps;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  profOverlaps  the profOverlaps to set
         */
        public void setProfOverlaps(final H2FeatureService profOverlaps) {
            this.profOverlaps = profOverlaps;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the profHole
         */
        public H2FeatureService getProfHole() {
            return profHole;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  profHole  the profHole to set
         */
        public void setProfHole(final H2FeatureService profHole) {
            this.profHole = profHole;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the sbefOverlapsAttr
         */
        public H2FeatureService getSbefOverlapsAttr() {
            return sbefOverlapsAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sbefOverlapsAttr  the sbefOverlapsAttr to set
         */
        public void setSbefOverlapsAttr(final H2FeatureService sbefOverlapsAttr) {
            this.sbefOverlapsAttr = sbefOverlapsAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the bbefOverlapsAttr
         */
        public H2FeatureService getBbefOverlapsAttr() {
            return bbefOverlapsAttr;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  bbefOverlapsAttr  the bbefOverlapsAttr to set
         */
        public void setBbefOverlapsAttr(final H2FeatureService bbefOverlapsAttr) {
            this.bbefOverlapsAttr = bbefOverlapsAttr;
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
    }
}
