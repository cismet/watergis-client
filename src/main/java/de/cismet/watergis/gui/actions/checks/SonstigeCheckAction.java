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
import de.cismet.cids.custom.watergis.server.search.FgBakCount;
import de.cismet.cids.custom.watergis.server.search.MergeBaFoto;
import de.cismet.cids.custom.watergis.server.search.MergeBaLeis;
import de.cismet.cids.custom.watergis.server.search.MergeBaTech;
import de.cismet.cids.custom.watergis.server.search.MergeBaUghz;
import de.cismet.cids.custom.watergis.server.search.OverlappedGmd;
import de.cismet.cids.custom.watergis.server.search.OverlappedTech;

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

import static de.cismet.watergis.gui.actions.checks.AbstractCheckAction.LOG;

/**
 * Issue 243.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SonstigeCheckAction extends AbstractCheckAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final MetaClass FG_BA_DEICH = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_deich");
    private static final MetaClass GU_WIWE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.gu_wiwe");
    private static final MetaClass FG_BA_UGHZ = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_ughz");
    private static final MetaClass FG_BA_LEIS = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_leis");
    private static final MetaClass FG_BA_TECH = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_tech");
    private static final MetaClass FOTO = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.foto");
    private static final MetaClass FG_BA_RL = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_rl");
    private static final MetaClass FG_BA_D = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_d");
    private static final MetaClass FG_BA_DUE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_due");
    private static String QUERY_DEICH_HOLE;
    private static String QUERY_TECH_HOLE;
    private static String QUERY_DEICH_ATTR;
    private static String QUERY_WIWE_HOLE;
    private static String QUERY_WIWE_ATTR;
    private static String QUERY_LEIS_ATTR;
    private static String QUERY_TECH_ATTR;
    private static String QUERY_FOTO_ATTR;
    private static String QUERY_UGHZ_ATTR;
    private static String QUERY_LEIS_GESCHL;
    private static String QUERY_DEICH_GESCHL;
    private static String QUERY_TECH_D;
    private static String QUERY_TECH_V;
    private static String QUERY_TECH_OFF;
    private static String QUERY_TECH_GESCH;
    private static final String CHECK_SONSTIGES_TECH_TECH__LUECKE = "Prüfungen->Sonstiges->Tech->Tech: Lücke";
    private static final String CHECK_SONSTIGES_TECH_TECH__UEBERLAPPUNG =
        "Prüfungen->Sonstiges->Tech->Tech: Überlappung";
    private static final String CHECK_SONSTIGES_UGHZ_UGHZ__ATTRIBUTE = "Prüfungen->Sonstiges->Ughz->Ughz: Attribute";
    private static final String CHECK_SONSTIGES_FOTO_FOTO__ATTRIBUTE = "Prüfungen->Sonstiges->Foto->Foto: Attribute";
    private static final String CHECK_SONSTIGES_TECH_TECH_FALSCH_AUF_GES =
        "Prüfungen->Sonstiges->Tech->Tech: falsch auf geschlossenem Gerinne";
    private static final String CHECK_SONSTIGES_TECH_TECH_FALSCH_AUF_OFF =
        "Prüfungen->Sonstiges->Tech->Tech: falsch auf offenem Gerinne";
    private static final String CHECK_SONSTIGES_TECH_TECH_D_NICHT_AUF_DD =
        "Prüfungen->Sonstiges->Tech->Tech: d nicht auf d/due";
    private static final String CHECK_SONSTIGES_TECH_TECH_V_NICHT_AUF_RL =
        "Prüfungen->Sonstiges->Tech->Tech: v nicht auf rl/due";
    private static final String CHECK_SONSTIGES_TECH_TECH__ATTRIBUTE = "Prüfungen->Sonstiges->Tech->Tech: Attribute";
    private static final String CHECK_SONSTIGES_LEIS_LEIS__ESW_FUER_GESCHL =
        "Prüfungen->Sonstiges->Leis->Leis: Esw für geschlossenes Gerinne";
    private static final String CHECK_SONSTIGES_LEIS_LEIS__ATTRIBUTE = "Prüfungen->Sonstiges->Leis->Leis: Attribute";
    private static final String CHECK_SONSTIGES_WIWE_WIWE__LUECKE = "Prüfungen->Sonstiges->Wiwe->Wiwe: Lücke";
    private static final String CHECK_SONSTIGES_WIWE_WIWE__ATTRIBUTE = "Prüfungen->Sonstiges->Wiwe->Wiwe: Attribute";
    private static final String CHECK_SONSTIGES_DEICH_DEICH_KREUZT_OFFEN =
        "Prüfungen->Sonstiges->Deich->Deich: kreuzt offenes Gerinne";
    private static final String CHECK_SONSTIGES_DEICH_DEICH__ATTRIBUTE =
        "Prüfungen->Sonstiges->Deich->Deich: Attribute";
    private static final String CHECK_SONSTIGES_DEICH_DEICH__LUECKE = "Prüfungen->Sonstiges->Deich->Deich: Lücke";
    private static final int[] USED_CLASS_IDS = new int[] {
            ((FG_BA_DEICH != null) ? FG_BA_DEICH.getId() : -1),
            ((GU_WIWE != null) ? GU_WIWE.getId() : -1),
            ((FG_BA_UGHZ != null) ? FG_BA_UGHZ.getId() : -1),
            ((FG_BA_LEIS != null) ? FG_BA_LEIS.getId() : -1),
            ((FG_BA_TECH != null) ? FG_BA_TECH.getId() : -1),
            ((FOTO != null) ? FOTO.getId() : -1),
            ((FG_BA_RL != null) ? FG_BA_RL.getId() : -1),
            ((FG_BA_D != null) ? FG_BA_D.getId() : -1),
            ((FG_BA_DUE != null) ? FG_BA_DUE.getId() : -1)
        };
//    private static final String CHECK_SONSTIGES_DEICH_DEICH__UEBERLAPPUNG =
//        "Prüfungen->Sonstiges->Deich->Deich: Überlappung";
    private static final String[] ALL_CHECKS = new String[] {
            CHECK_SONSTIGES_TECH_TECH__LUECKE,
            CHECK_SONSTIGES_TECH_TECH__UEBERLAPPUNG,
            CHECK_SONSTIGES_UGHZ_UGHZ__ATTRIBUTE,
            CHECK_SONSTIGES_FOTO_FOTO__ATTRIBUTE,
            CHECK_SONSTIGES_TECH_TECH_FALSCH_AUF_GES,
            CHECK_SONSTIGES_TECH_TECH_FALSCH_AUF_OFF,
            CHECK_SONSTIGES_TECH_TECH_D_NICHT_AUF_DD,
            CHECK_SONSTIGES_TECH_TECH_V_NICHT_AUF_RL,
            CHECK_SONSTIGES_TECH_TECH__ATTRIBUTE,
            CHECK_SONSTIGES_LEIS_LEIS__ESW_FUER_GESCHL,
            CHECK_SONSTIGES_LEIS_LEIS__ATTRIBUTE,
            CHECK_SONSTIGES_WIWE_WIWE__LUECKE,
            CHECK_SONSTIGES_WIWE_WIWE__ATTRIBUTE,
            CHECK_SONSTIGES_DEICH_DEICH_KREUZT_OFFEN,
            CHECK_SONSTIGES_DEICH_DEICH__ATTRIBUTE,
            CHECK_SONSTIGES_DEICH_DEICH__LUECKE
//            ,CHECK_SONSTIGES_DEICH_DEICH__UEBERLAPPUNG
        };

    static {
        if ((GU_WIWE != null) && (FG_BA_UGHZ != null) && (FG_BA_LEIS != null) && (FG_BA_DEICH != null)
                    && (FG_BA_TECH != null)) {
            final User user = SessionManager.getSession().getUser();

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_DEICH_HOLE = "select " + FG_BA_DEICH.getID() + ", d1." + FG_BA_DEICH.getPrimaryKey()
                            + "	from (select d.id, g.geo_field geo from \n"
                            + "	dlm25w.fg_ba_deich d\n"
                            + "	join geom g on (d.geom = g.id)\n"
                            + "	) as d1,\n"
                            + "	(select d.id, g.geo_field geo from \n"
                            + "	dlm25w.fg_ba_deich d\n"
                            + "	join geom g on (d.geom = g.id)\n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and st_distance(dlm25w.endpunkte(d1.geo), dlm25w.endpunkte(d2.geo)) > 0.001 and st_distance(dlm25w.endpunkte(d1.geo), dlm25w.endpunkte(d2.geo)) < 0.5;";
            } else {
                QUERY_DEICH_HOLE = "select " + FG_BA_DEICH.getID() + ", d1." + FG_BA_DEICH.getPrimaryKey()
                            + "	from (select d.id, g.geo_field geo from \n"
                            + "	dlm25w.fg_ba_deich d\n"
                            + "	join geom g on (d.geom = g.id)\n"
                            + " join dlm25w.k_ww_gr gr on (d.ww_gr = gr.id)"
                            + " where (%1$s is null or von.route = any(%1$s)) and gr.owner = '"
                            + user.getUserGroup().getName() + "' \n"
                            + "	) as d1,\n"
                            + "	(select d.id, g.geo_field geo from \n"
                            + "	dlm25w.fg_ba_deich d\n"
                            + "	join geom g on (d.geom = g.id)\n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and st_distance(dlm25w.endpunkte(d1.geo), dlm25w.endpunkte(d2.geo)) > 0.001 and st_distance(dlm25w.endpunkte(d1.geo), dlm25w.endpunkte(d2.geo)) < 0.5;";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_DEICH_ATTR = "select " + FG_BA_DEICH.getID() + ", d." + FG_BA_DEICH.getPrimaryKey()
                            + "	from dlm25w.fg_ba_deich d\n"
                            + "	left join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	left join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	left join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	left join dlm25w.k_ww_gr gr on (d.ww_gr = gr.id)\n"
                            + "	left join dlm25w.k_deich_l_fk fk on (d.l_fk = fk.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (deich is null or gr.ww_gr is null or ord is null or d.l_fk is null or obj_nr is null or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br_f is not null and (fk.l_fk = 'fd' and (br_f < 2 or br_f > 50)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (br_f < 2 or br_f > 100)))\n"
                            + "or (br_k is not null and (fk.l_fk = 'fd' and (br_k < 0.5 or br_k > 10)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (br_k < 0.5 or br_k > 20)))\n"
                            + "or (ho_k_f is not null and (fk.l_fk = 'fd' and (ho_k_f < 0.5 or ho_k_f > 15)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (ho_k_f < 1 or ho_k_f > 15)))\n"
                            + "or (ho_k_pn is not null and (fk.l_fk = 'fd' and (ho_k_pn < 2 or ho_k_pn > 25)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (ho_k_pn < 1 or ho_k_pn > 20)))\n"
                            + "or (ho_bhw_pn is not null and (fk.l_fk = 'fd' and (ho_bhw_pn < 2 or ho_bhw_pn > 25)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (ho_bhw_pn < 0 or ho_bhw_pn > 20)))\n"
                            + "or (bv_w is not null and (bv_w < 1 or bv_w > 15))\n"
                            + "or (bv_b is not null and (bv_b < 1 or bv_b > 15))\n"
                            + "or ((fk.l_fk = 'fd') and (l_rl is null))\n"
                            + "or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (linie is not null))\n"
                            + "or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (l_rl is not null))\n"
                            + "or ((br_f is not null and br_k is not null) and br_f <= br_k)\n"
                            + "or ((ho_k_f is not null and ho_k_pn is not null) and ho_k_pn <= ho_k_f)\n"
                            + "or ((ho_k_pn is not null and ho_bhw_pn is not null) and ho_k_pn <= ho_bhw_pn)\n"
                            + "or ((ho_bhw_pn is not null and ho_mw_pn is not null) and ho_bhw_pn <= ho_mw_pn)\n"
                            // + "or (d.esw is not null and (d.esw < 0 or d.esw > 1)) "
                            + "or ((ho_k_pn is not null and ho_mw_pn is not null) and ho_k_pn <= ho_mw_pn))";
            } else {
                QUERY_DEICH_ATTR = "select " + FG_BA_DEICH.getID() + ", d." + FG_BA_DEICH.getPrimaryKey()
                            + "	from dlm25w.fg_ba_deich d\n"
                            + "	left join dlm25w.fg_ba_linie linie on (d.ba_st = linie.id)\n"
                            + "	left join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	left join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	left join dlm25w.k_ww_gr gr on (d.ww_gr = gr.id)\n"
                            + "	left join dlm25w.k_deich_l_fk fk on (d.l_fk = fk.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (deich is null or gr.ww_gr is null or ord is null or d.l_fk is null or obj_nr is null or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br_f is not null and (fk.l_fk = 'fd' and (br_f < 2 or br_f > 50)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (br_f < 2 or br_f > 100)))\n"
                            + "or (br_k is not null and (fk.l_fk = 'fd' and (br_k < 0.5 or br_k > 10)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (br_k < 0.5 or br_k > 20)))\n"
                            + "or (ho_k_f is not null and (fk.l_fk = 'fd' and (ho_k_f < 0.5 or ho_k_f > 15)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (ho_k_f < 1 or ho_k_f > 15)))\n"
                            + "or (ho_k_pn is not null and (fk.l_fk = 'fd' and (ho_k_pn < 2 or ho_k_pn > 25)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (ho_k_pn < 1 or ho_k_pn > 20)))\n"
                            + "or (ho_bhw_pn is not null and (fk.l_fk = 'fd' and (ho_bhw_pn < 2 or ho_bhw_pn > 25)) or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (ho_bhw_pn < 0 or ho_bhw_pn > 20)))\n"
                            + "or (bv_w is not null and (bv_w < 1 or bv_w > 15))\n"
                            + "or (bv_b is not null and (bv_b < 1 or bv_b > 15))\n"
                            + "or ((fk.l_fk = 'fd') and (l_rl is null))\n"
                            + "or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (linie is not null))\n"
                            + "or ((fk.l_fk = 'bd' or fk.l_fk = 'kd') and (l_rl is not null))\n"
                            + "or ((br_f is not null and br_k is not null) and br_f <= br_k)\n"
                            + "or ((ho_k_f is not null and ho_k_pn is not null) and ho_k_pn <= ho_k_f)\n"
                            + "or ((ho_k_pn is not null and ho_bhw_pn is not null) and ho_k_pn <= ho_bhw_pn)\n"
                            + "or ((ho_bhw_pn is not null and ho_mw_pn is not null) and ho_bhw_pn <= ho_mw_pn)\n"
                            // + "or (d.esw is not null and (d.esw < 0 or d.esw > 1)) "
                            + "or ((ho_k_pn is not null and ho_mw_pn is not null) and ho_k_pn <= ho_mw_pn)) and gr.owner = '"
                            + user.getUserGroup().getName() + "'";
            }
            // the geo index should not be used to improve the performance if ((user == null) ||
            // user.getUserGroup().getName().startsWith("lung") ||
            // user.getUserGroup().getName().equalsIgnoreCase("administratoren")) { QUERY_DEICH_GESCHL = "select " +
            // FG_BA_DEICH.getID() + ", d." + FG_BA_DEICH.getPrimaryKey() + "       from dlm25w.fg_ba_deich d\n" + "join
            // geom g on (d.geom = g.id),\n" + "(select (dlm25w.fast_union(\n" + "'select geo_field from dlm25w.fg_ba_rl
            // r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join geom g on (l.geom = g.id)\n" + "union\n" + "select
            // geo_field from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join geom g on (l.geom =
            // g.id)\n" + "union\n" + "select geo_field from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st =
            // l.id) join geom g on (l.geom = g.id)'\n" + ")) as geo_field) as geschl_gerinne\n" + "where
            // _st_intersects(g.geo_field, geschl_gerinne.geo_field)"; } else { QUERY_DEICH_GESCHL = "select " +
            // FG_BA_DEICH.getID() + ", d." + FG_BA_DEICH.getPrimaryKey() + "       from dlm25w.fg_ba_deich d\n" + "join
            // geom g on (d.geom = g.id)\n" + "join dlm25w.k_ww_gr gr on (d.ww_gr = gr.id)," + "(select
            // (dlm25w.fast_union(\n" + "'select geo_field from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st
            // = l.id) join geom g on (l.geom = g.id)\n" + "union\n" + "select geo_field from dlm25w.fg_ba_d r join
            // dlm25w.fg_ba_linie l on (r.ba_st = l.id) join geom g on (l.geom = g.id)\n" + "union\n" + "select
            // geo_field from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join geom g on (l.geom =
            // g.id)'\n" + ")) as geo_field) as geschl_gerinne\n" + "where _st_intersects(g.geo_field,
            // geschl_gerinne.geo_field) and gr.owner = '" + user.getUserGroup().getName() + "'"; }
            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_DEICH_GESCHL = "select distinct " + FG_BA_DEICH.getID() + ", "
                            + " unnest(dlm25w.determine_crossed_deich(null, %1$s)) as id";
            } else {
                QUERY_DEICH_GESCHL = "select distinct " + FG_BA_DEICH.getID() + ", "
                            + " unnest(dlm25w.determine_crossed_deich('" + user.getUserGroup().getName()
                            + "', %1$s)) as id";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_WIWE_ATTR = "select " + GU_WIWE.getID() + ", w." + GU_WIWE.getPrimaryKey()
                            + "	from dlm25w.gu_wiwe w\n"
                            + "	left join geom g on (w.geom = g.id)\n"
                            + "	left join dlm25w.k_wiwe kw on (w.wiwe = kw.id)\n"
                            + "	left join dlm25w.k_material m on (w.material = m.id)\n"
                            + "where w.wiwe is null or obj_nr is null or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br is not null and (br < 0 or br > 30)) or st_length(g.geo_field) < 1\n"
                            + "or (kw.wiwe = 'Decke' and m.material not in ('As', 'B', 'Kies','Kies-wgRec','Rec-wg','Sand','Sand-wg','Scho','Scho-wg'))\n"
                            + "or (kw.wiwe in ('Decke-Pfl','Decke-Pl','Decke-Rg') and m.material <> 'B')\n"
                            + "or (kw.wiwe = 'Spur' and m.material not in ('As','B'))\n"
                            + "or (kw.wiwe in ('Spur-Pfl','Spur-Pl','Spur-Rg') and m.material <> 'B')";
            } else {
                QUERY_WIWE_ATTR = "select " + GU_WIWE.getID() + ", w." + GU_WIWE.getPrimaryKey()
                            + "	from dlm25w.gu_wiwe w\n"
                            + "	left join geom g on (w.geom = g.id)\n"
                            + "	left join dlm25w.k_wiwe kw on (w.wiwe = kw.id)\n"
                            + "	left join dlm25w.k_material m on (w.material = m.id)\n"
                            + "	left join dlm25w.k_ww_gr gr on (w.ww_gr = gr.id)\n"
                            + "where (w.wiwe is null or obj_nr is null or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br is not null and (br < 0 or br > 30)) or st_length(g.geo_field) < 1\n"
                            + "or (kw.wiwe = 'Decke' and m.material not in ('As', 'B', 'Kies','Kies-wgRec','Rec-wg','Sand','Sand-wg','Scho','Scho-wg'))\n"
                            + "or (kw.wiwe in ('Decke-Pfl','Decke-Pl','Decke-Rg') and m.material <> 'B')\n"
                            + "or (kw.wiwe = 'Spur' and m.material not in ('As','B'))\n"
                            + "or (kw.wiwe in ('Spur-Pfl','Spur-Pl','Spur-Rg') and m.material <> 'B')) and gr.owner = '"
                            + user.getUserGroup().getName() + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_WIWE_HOLE = "select distinct " + GU_WIWE.getID() + ", d1." + GU_WIWE.getPrimaryKey()
                            + "	from (select d.id, g.geo_field geo from \n"
                            + "	dlm25w.gu_wiwe d\n"
                            + "	join geom g on (d.geom = g.id)\n"
                            + "	) as d1,\n"
                            + "	(select d.id, g.geo_field geo from \n"
                            + "	dlm25w.gu_wiwe d\n"
                            + "	join geom g on (d.geom = g.id)\n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and st_distance(dlm25w.endpunkte(d1.geo), dlm25w.endpunkte(d2.geo)) > 0.001 and st_distance(dlm25w.endpunkte(d1.geo), dlm25w.endpunkte(d2.geo)) < 0.5;";
            } else {
                QUERY_WIWE_HOLE = "select distinct " + GU_WIWE.getID() + ", d1." + GU_WIWE.getPrimaryKey()
                            + "	from (select d.id, g.geo_field geo from \n"
                            + "	dlm25w.gu_wiwe d\n"
                            + "	join geom g on (d.geom = g.id)\n"
                            + "join dlm25w.k_ww_gr gr on (d.ww_gr = gr.id)"
                            + " where gr.owner = '" + user.getUserGroup().getName() + "'\n"
                            + "	) as d1,\n"
                            + "	(select d.id, g.geo_field geo from \n"
                            + "	dlm25w.gu_wiwe d\n"
                            + "	join geom g on (d.geom = g.id)\n"
                            + "	) as d2\n"
                            + "where d1.id <> d2.id and st_distance(dlm25w.endpunkte(d1.geo), dlm25w.endpunkte(d2.geo)) > 0.001 and st_distance(dlm25w.endpunkte(d1.geo), dlm25w.endpunkte(d2.geo)) < 0.5;";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_LEIS_ATTR = "select " + FG_BA_LEIS.getID() + ", l." + FG_BA_LEIS.getPrimaryKey()
                            + " from dlm25w.fg_ba_leis l\n"
                            + "	join dlm25w.fg_ba_linie linie on (l.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "	join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and ("
                            + "(leis is null or obj_nr is null or l_rl is null or abs(von.wert - bis.wert) < 0.5)"
                            + " or (l.esw is not null and (l.esw < 0 or l.esw > 1)) "
                            + ")";
            } else {
                QUERY_LEIS_ATTR = "select " + FG_BA_LEIS.getID() + ", l." + FG_BA_LEIS.getPrimaryKey()
                            + " from dlm25w.fg_ba_leis l\n"
                            + "	join dlm25w.fg_ba_linie linie on (l.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "	join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "	join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and ("
                            + "(leis is null or obj_nr is null or l_rl is null or abs(von.wert - bis.wert) < 0.5) "
                            + " or (l.esw is not null and (l.esw < 0 or l.esw > 1)) "
                            + ") and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)";
            }
            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_LEIS_GESCHL = "select " + FG_BA_LEIS.getID() + ", le." + FG_BA_LEIS.getPrimaryKey()
                            + " from dlm25w.fg_ba_leis le \n"
                            + " join dlm25w.fg_ba_linie linie on (le.ba_st = linie.id)\n"
                            + " join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + " join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (von.route = ba.id)"
                            + " left join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and esw = 1 and \n"
                            + "(exists (select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + "or\n"
                            + "exists (select 1 from dlm25w.fg_ba_d d join dlm25w.fg_ba_linie l on (d.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + "or\n"
                            + "exists (select 1 from dlm25w.fg_ba_due due join dlm25w.fg_ba_linie l on (due.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + "))";
            } else {
                QUERY_LEIS_GESCHL = "select " + FG_BA_LEIS.getID() + ", le." + FG_BA_LEIS.getPrimaryKey()
                            + " from dlm25w.fg_ba_leis le \n"
                            + " join dlm25w.fg_ba_linie linie on (le.ba_st = linie.id)\n"
                            + " join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + " join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (von.route = ba.id)"
                            + " join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and esw = 1 and \n"
                            + "(exists (select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + "or\n"
                            + "exists (select 1 from dlm25w.fg_ba_d d join dlm25w.fg_ba_linie l on (d.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + "or\n"
                            + "exists (select 1 from dlm25w.fg_ba_due due join dlm25w.fg_ba_linie l on (due.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")) and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)";
            }
            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_TECH_ATTR = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "	join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (obj_nr is null or abs(von.wert - bis.wert) < 0.5)";
            } else {
                QUERY_TECH_ATTR = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "	join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "	join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (obj_nr is null or abs(von.wert - bis.wert) < 0.5) and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)";
            }
            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_TECH_V = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "left join dlm25w.k_ww_gr gr on (gr.id = t.ww_gr)\n"
                            + "left join dlm25w.k_tech te on (te.id = t.tech)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and te.tech = 'v' and \n"
                            + "((\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") + \n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ")) <> (abs(von.wert - bis.wert))";
            } else {
                QUERY_TECH_V = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "left join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "left join dlm25w.k_tech te on (te.id = t.tech)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and te.tech = 'v' and \n"
                            + "((\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") + \n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ")) <> (abs(von.wert - bis.wert)) and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)";
            }
            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_TECH_D = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "left join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "left join dlm25w.k_tech te on (te.id = t.tech)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and te.tech = 'd' and \n"
                            + "((\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") + \n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ")) <> (abs(von.wert - bis.wert))";
            } else {
                QUERY_TECH_D = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "left join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "left join dlm25w.k_tech te on (te.id = t.tech)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and te.tech = 'd' and \n"
                            + "((\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ") + \n"
                            + "(\n"
                            + "select coalesce(sum(least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert))), 0) from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0\n"
                            + ")) <> (abs(von.wert - bis.wert)) and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)";
            }
            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_TECH_OFF = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "left join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "left join dlm25w.k_tech te on (te.id = t.tech)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (te.tech = 'd' or te.tech = 'v') and \n"
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
                            + ")) < (abs(von.wert - bis.wert))";
            } else {
                QUERY_TECH_OFF = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "left join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "left join dlm25w.k_tech te on (te.id = t.tech)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (te.tech = 'd' or te.tech = 'v') and \n"
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
                            + ")) < (abs(von.wert - bis.wert)) and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)";
            }
            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_TECH_GESCH = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "left join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "left join dlm25w.k_tech te on (te.id = t.tech)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (te.tech <> 'd' and te.tech <> 'v') and \n"
                            + "(exists (select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + "or exists (select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + "or exists (select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + ")";
            } else {
                QUERY_TECH_GESCH = "select " + FG_BA_TECH.getID() + ", t." + FG_BA_TECH.getPrimaryKey()
                            + " from dlm25w.fg_ba_tech t\n"
                            + "join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + " join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "left join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "left join dlm25w.k_tech te on (te.id = t.tech)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (te.tech <> 'd' and te.tech <> 'v') and \n"
                            + "(exists (select 1 from dlm25w.fg_ba_d r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + "or exists (select 1 from dlm25w.fg_ba_due r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + "or exists (select 1 from dlm25w.fg_ba_rl r join dlm25w.fg_ba_linie l on (r.ba_st = l.id) join dlm25w.fg_ba_punkt v on (l.von = v.id) join dlm25w.fg_ba_punkt b on (l.bis = b.id)\n"
                            + "where v.route = von.route and least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.01\n"
                            + ")\n"
                            + ") and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)";
            }
            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_FOTO_ATTR = "select " + FOTO.getID() + ", f." + FOTO.getPrimaryKey()
                            + " from dlm25w.foto f\n"
                            + "	left join dlm25w.fg_ba_punkt von on (f.ba_st = von.id)\n"
                            + "	left join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "	left join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "	left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (foto_nr is null or winkel < 0 or winkel > 360 or foto is null \n"
                            + "or freigabe is null or upl_name is null or upl_datum is null or upl_zeit is null or (aufn_datum is not null and ( date_part('year', aufn_datum) < 1900 or date_part('year', aufn_datum) > date_part('year', now()) ) ))";
            } else {
                QUERY_FOTO_ATTR = "select " + FOTO.getID() + ", f." + FOTO.getPrimaryKey()
                            + " from dlm25w.foto f\n"
                            + "	left join dlm25w.fg_ba_punkt von on (f.ba_st = von.id)\n"
                            + "	left join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "	left join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "	left join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (foto_nr is null or winkel < 0 or winkel > 360 or foto is null \n"
                            + "or freigabe is null or upl_name is null or upl_datum is null or upl_zeit is null or (aufn_datum is not null and ( date_part('year', aufn_datum) < 1900 or date_part('year', aufn_datum) > date_part('year', now()) ) ))  and gr.owner = '"
                            + user.getUserGroup().getName() + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_UGHZ_ATTR = "select " + FG_BA_UGHZ.getID() + ", u." + FG_BA_UGHZ.getPrimaryKey()
                            + " from dlm25w.fg_ba_ughz u\n"
                            + "	join dlm25w.fg_ba_linie linie on (u.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "	join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "	join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (l_rl is null or ughz is null or obj_nr is null \n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br is not null and (br < 0 or br > 30))\n"
                            + "or (ho_d_o is not null and (ho_d_o < 0 or ho_d_o > 15))\n"
                            + "or (ho_d_u is not null and (ho_d_u < 0 or ho_d_u > 15))\n"
                            + "or abs(von.wert - bis.wert) < 0.5\n"
                            + " or (u.esw is not null and (u.esw < 0 or u.esw > 1)) "
                            + "or (ho_d_o is not null and ho_d_u is not null and ho_d_o <= ho_d_u))";
            } else {
                QUERY_UGHZ_ATTR = "select " + FG_BA_UGHZ.getID() + ", u." + FG_BA_UGHZ.getPrimaryKey()
                            + " from dlm25w.fg_ba_ughz u\n"
                            + "	join dlm25w.fg_ba_linie linie on (u.ba_st = linie.id)\n"
                            + "	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "	join dlm25w.fg_ba ba on (von.route = ba.id)\n"
                            + "	join dlm25w.fg_bak bak on (ba.bak_id = bak.id)\n"
                            + "	join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or von.route = any(%1$s)) and (l_rl is null or ughz is null or obj_nr is null \n"
                            + "or (ausbaujahr is not null and (ausbaujahr < 1800 or ausbaujahr > date_part('year', now()) + 2))\n"
                            + "or (br is not null and (br < 0 or br > 30))\n"
                            + "or (ho_d_o is not null and (ho_d_o < 0 or ho_d_o > 15))\n"
                            + "or (ho_d_u is not null and (ho_d_u < 0 or ho_d_u > 15))\n"
                            + "or abs(von.wert - bis.wert) < 0.5\n"
                            + " or (u.esw is not null and (u.esw < 0 or u.esw > 1)) "
                            + "or (ho_d_o is not null and ho_d_u is not null and ho_d_o <= ho_d_u)) and (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_TECH_HOLE = "select distinct " + FG_BA_TECH.getID() + ", t1." + FG_BA_TECH.getPrimaryKey()
                            + " from (select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_tech t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "                            	join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "                            	) as t1,\n"
                            + "                            	(select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_tech t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "                            	join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + "                            	) as t2\n"
                            + "                            where (%1$s is null or t1.route = any(%1$s)) and t1.id <> t2.id and t1.route = t2.route and ((least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) > -0.5 and (least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) < 0)";
            } else {
                QUERY_TECH_HOLE = "select distinct " + FG_BA_TECH.getID() + ", t1." + FG_BA_TECH.getPrimaryKey()
                            + " from (select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_tech t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "                            	join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + " WHERE (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)"
                            + "                            	) as t1,\n"
                            + "                            	(select von.wert as von, bis.wert as bis, von.route, t.id from \n"
                            + "                            	dlm25w.fg_ba_tech t\n"
                            + "                            	join dlm25w.fg_ba_linie linie on (t.ba_st = linie.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "                            	join dlm25w.fg_ba_punkt bis on (linie.bis = bis.id)\n"
                            + "                                 join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "                            	join dlm25w.k_ww_gr gr on (gr.id = ba.ww_gr)\n"
                            + " WHERE (gr.owner = '"
                            + user.getUserGroup().getName() + "' or %2$s)"
                            + "                            	) as t2\n"
                            + "                            where (%1$s is null or t1.route = any(%1$s)) and t1.id <> t2.id and t1.route = t2.route and ((least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) > -0.5 and (least(greatest(t2.von, t2.bis), greatest(t1.von, t1.bis)) - greatest(least(t2.von, t2.bis), least(t1.von, t1.bis))) < 0)";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    // dlm25w.merge_fg_bak_gwk()
    private boolean successful = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SonstigeCheckAction object.
     */
    public SonstigeCheckAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SonstigeCheckAction.class,
                "SonstigeCheckAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                SonstigeCheckAction.class,
                "SonstigeCheckAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-zoom.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getProgressSteps() {
        return 20;
    }

    @Override
    public boolean startCheckInternal(final boolean isExport,
            final WaitDialog wd,
            final List<H2FeatureService> result) {
        if (isExport) {
            try {
                final CheckResult cr = check(isExport, wd);

                if (result != null) {
                    addService(result, cr.getAttributesUghz());
                    addService(result, cr.getAttributesLeis());
                    addService(result, cr.getAttributesTech());
                    addService(result, cr.getGapTech());
                    addService(result, cr.getGerinneLeis());
                    addService(result, cr.getOverlappedTech());
                    addService(result, cr.getdTech());
                    addService(result, cr.getvTech());
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
                    NbBundle.getMessage(SonstigeCheckAction.class,
                        "SonstigeCheckAction.actionPerformed().dialog"),
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
                                        SonstigeCheckAction.class,
                                        "SonstigeCheckAction.actionPerformed().result.text.withoutProblems",
                                        new Object[] {
                                            result.getBakCount(),
                                            result.getAttributesDeichErrors(),
                                            result.getAttributesWiweErrors(),
                                            result.getAttributesUghzErrors(),
                                            result.getAttributesFotoErrors(),
                                            result.getAttributesLeisErrors(),
                                            result.getAttributesTechErrors(),
                                            result.getGapDeichErrors(),
                                            result.getGapWiweErrors(),
                                            result.getGapTechErrors(),
                                            result.getOverlappedTechErrors(),
                                            result.getGapDeichErrors()
                                                    + result.getGerinneDeichErrors(),
                                            result.getGapWiweErrors()
                                                    + result.getGerinneWiweErrors(),
                                            result.getGerinneLeisErrors(),
                                            result.getGapTechErrors()
                                                    + result.getOverlappedTechErrors()
                                                    + result.getOffGerinneTechErrors()
                                                    + result.getGeschGerinneTechErrors()
                                                    + result.getdTechErrors()
                                                    + result.getvTechErrors(),
                                            0
                                        }),
                                    NbBundle.getMessage(
                                        SonstigeCheckAction.class,
                                        "SonstigeCheckAction.actionPerformed().result.title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                    NbBundle.getMessage(
                                        SonstigeCheckAction.class,
                                        "SonstigeCheckAction.actionPerformed().result.text",
                                        new Object[] {
                                            result.getBakCount(),
                                            result.getAttributesDeichErrors(),
                                            result.getAttributesWiweErrors(),
                                            result.getAttributesUghzErrors(),
                                            result.getAttributesFotoErrors(),
                                            result.getAttributesLeisErrors(),
                                            result.getAttributesTechErrors(),
                                            result.getGapDeichErrors(),
                                            result.getGapWiweErrors(),
                                            result.getGapTechErrors(),
                                            result.getOverlappedTechErrors(),
                                            result.getGapDeichErrors()
                                                    + result.getGerinneDeichErrors(),
                                            result.getGapWiweErrors()
                                                    + result.getGerinneWiweErrors(),
                                            result.getGerinneLeisErrors(),
                                            result.getGapTechErrors()
                                                    + result.getOverlappedTechErrors()
                                                    + result.getOffGerinneTechErrors()
                                                    + result.getGeschGerinneTechErrors()
                                                    + result.getdTechErrors()
                                                    + result.getvTechErrors(),
                                            result.getProblemTreeObjectCount().getCount(),
                                            result.getProblemTreeObjectCount().getClasses()
                                        }),
                                    NbBundle.getMessage(
                                        SonstigeCheckAction.class,
                                        "SonstigeCheckAction.actionPerformed().result.title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                            if (result.getvTech() != null) {
                                showService(result.getvTech());
                            }
                            if (result.getdTech() != null) {
                                showService(result.getdTech());
                            }
                            if (result.getGeschGerinneTech() != null) {
                                showService(result.getGeschGerinneTech());
                            }
                            if (result.getOffGerinneTech() != null) {
                                showService(result.getOffGerinneTech());
                            }
                            if (result.getOverlappedTech() != null) {
                                showService(result.getOverlappedTech());
                            }
                            if (result.getGapTech() != null) {
                                showService(result.getGapTech());
                            }
                            if (result.getAttributesTech() != null) {
                                showService(result.getAttributesTech());
                            }
                            if (result.getGerinneLeis() != null) {
                                showService(result.getGerinneLeis());
                            }
                            if (result.getAttributesLeis() != null) {
                                showService(result.getAttributesLeis());
                            }
                            if (result.getAttributesFoto() != null) {
                                showService(result.getAttributesFoto());
                            }
                            if (result.getAttributesUghz() != null) {
                                showService(result.getAttributesUghz());
                            }
                            if (result.getGerinneWiwe() != null) {
                                showService(result.getGerinneWiwe());
                            }
                            if (result.getGapWiwe() != null) {
                                showService(result.getGapWiwe());
                            }
                            if (result.getAttributesWiwe() != null) {
                                showService(result.getAttributesWiwe());
                            }
                            if (result.getGerinneDeich() != null) {
                                showService(result.getGerinneDeich());
                            }
                            if (result.getGapDeich() != null) {
                                showService(result.getGapDeich());
                            }
                            if (result.getAttributesDeich() != null) {
                                showService(result.getAttributesDeich());
                            }
                            refreshTree();
                            refreshMap();
                        } catch (Exception e) {
                            LOG.error("Error while performing the sonstige analyse.", e);
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
        if (!isExport) {
            final CidsServerSearch mergeFoto = new MergeBaFoto(user);
            SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeFoto);
        }
        increaseProgress(wd, 1);
        final CidsServerSearch mergeUghz = new MergeBaUghz(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeUghz);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeTech = new MergeBaTech(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeTech);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeLeis = new MergeBaLeis(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeLeis);
        increaseProgress(wd, 1);

        final List<FeatureServiceAttribute> serviceAttributeDefinition = new ArrayList<FeatureServiceAttribute>();

        FeatureServiceAttribute serviceAttribute = new FeatureServiceAttribute(
                "id",
                String.valueOf(Types.INTEGER),
                true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ww_gr", String.valueOf(Types.INTEGER), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_cd", String.valueOf(Types.VARCHAR), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_von", String.valueOf(Types.DOUBLE), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_bis", String.valueOf(Types.DOUBLE), true);
        serviceAttributeDefinition.add(serviceAttribute);
//        serviceAttribute = new FeatureServiceAttribute("gmd_nr_re", String.valueOf(Types.VARCHAR), true);
//        baGmdServiceAttributeDefinition.add(serviceAttribute);
//        serviceAttribute = new FeatureServiceAttribute("gmd_nr_li", String.valueOf(Types.VARCHAR), true);
//        baGmdServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("laenge", String.valueOf(Types.DOUBLE), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_date", String.valueOf(Types.TIMESTAMP), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_user", String.valueOf(Types.VARCHAR), true);
        serviceAttributeDefinition.add(serviceAttribute);

        // start checks
        final boolean useExpCond = user != null;
        final boolean export = isExport && useExpCond;
        final String expCondition = ((isExport && useExpCond)
                ? (" exists(select id from dlm25w.fg_ba_exp_complete where owner = '" + user + "' and bak_id = bak.id)")
                : "false");

        if (!isExport) {
            result.setGapDeich(analyseByQuery(
                    FG_BA_DEICH,
                    String.format(QUERY_DEICH_HOLE, SQLFormatter.createSqlArrayString(selectedIds)),
                    CHECK_SONSTIGES_DEICH_DEICH__LUECKE));
            increaseProgress(wd, 1);

            result.setAttributesDeich(analyseByQuery(
                    FG_BA_DEICH,
                    String.format(QUERY_DEICH_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                    CHECK_SONSTIGES_DEICH_DEICH__ATTRIBUTE));
            increaseProgress(wd, 1);

            result.setGerinneDeich(analyseByQuery(
                    FG_BA_DEICH,
                    String.format(QUERY_DEICH_GESCHL, SQLFormatter.createSqlArrayString(selectedIds)),
                    CHECK_SONSTIGES_DEICH_DEICH_KREUZT_OFFEN));
            increaseProgress(wd, 1);

            result.setAttributesWiwe(analyseByQuery(
                    GU_WIWE,
                    String.format(QUERY_WIWE_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                    CHECK_SONSTIGES_WIWE_WIWE__ATTRIBUTE));
            increaseProgress(wd, 1);

            result.setGapWiwe(analyseByQuery(
                    GU_WIWE,
                    String.format(QUERY_WIWE_HOLE, SQLFormatter.createSqlArrayString(selectedIds)),
                    CHECK_SONSTIGES_WIWE_WIWE__LUECKE));
            increaseProgress(wd, 1);
        } else {
            increaseProgress(wd, 6);
        }

        result.setProblemTreeObjectCount(getErrorObjectsFromTree(user, selectedIds, USED_CLASS_IDS, isExport));

        String query = (useExpCond
                ? String.format(QUERY_LEIS_ATTR, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_LEIS_ATTR, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setAttributesLeis(analyseByQuery(
                FG_BA_LEIS,
                query,
                CHECK_SONSTIGES_LEIS_LEIS__ATTRIBUTE));
        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_LEIS_GESCHL, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_LEIS_GESCHL, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setGerinneLeis(analyseByQuery(
                FG_BA_LEIS,
                query,
                CHECK_SONSTIGES_LEIS_LEIS__ESW_FUER_GESCHL));
        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_TECH_ATTR, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_TECH_ATTR, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setAttributesTech(analyseByQuery(
                FG_BA_TECH,
                query,
                CHECK_SONSTIGES_TECH_TECH__ATTRIBUTE));
        increaseProgress(wd, 1);

        query = (useExpCond ? String.format(QUERY_TECH_V, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                            : String.format(QUERY_TECH_V, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setvTech(analyseByQuery(
                FG_BA_TECH,
                query,
                CHECK_SONSTIGES_TECH_TECH_V_NICHT_AUF_RL));
        increaseProgress(wd, 1);

        query = (useExpCond ? String.format(QUERY_TECH_D, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                            : String.format(QUERY_TECH_D, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setdTech(analyseByQuery(
                FG_BA_TECH,
                query,
                CHECK_SONSTIGES_TECH_TECH_D_NICHT_AUF_DD));
        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_TECH_OFF, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_TECH_OFF, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setOffGerinneTech(analyseByQuery(
                FG_BA_TECH,
                query,
                CHECK_SONSTIGES_TECH_TECH_FALSCH_AUF_OFF));
        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_TECH_GESCH, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_TECH_GESCH, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setGeschGerinneTech(analyseByQuery(
                FG_BA_TECH,
                query,
                CHECK_SONSTIGES_TECH_TECH_FALSCH_AUF_GES));
        increaseProgress(wd, 1);

        if (!isExport) {
            result.setAttributesFoto(analyseByQuery(
                    FOTO,
                    String.format(QUERY_FOTO_ATTR, SQLFormatter.createSqlArrayString(selectedIds)),
                    CHECK_SONSTIGES_FOTO_FOTO__ATTRIBUTE));
        }
        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_UGHZ_ATTR, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_UGHZ_ATTR, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setAttributesUghz(analyseByQuery(
                FG_BA_UGHZ,
                query,
                CHECK_SONSTIGES_UGHZ_UGHZ__ATTRIBUTE));
        increaseProgress(wd, 1);

        result.setOverlappedTech(analyseByCustomSearch(
                new OverlappedTech(user, selectedIds, export),
                CHECK_SONSTIGES_TECH_TECH__UEBERLAPPUNG,
                serviceAttributeDefinition));
        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_TECH_HOLE, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_TECH_HOLE, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setGapTech(analyseByQuery(
                FG_BA_TECH,
                query,
                CHECK_SONSTIGES_TECH_TECH__LUECKE));
        increaseProgress(wd, 1);

        if (result.getAttributesDeich() != null) {
            result.setAttributesDeichErrors(result.getAttributesDeich().getFeatureCount(null));
            successful = false;
        }

        if (result.getAttributesFoto() != null) {
            result.setAttributesFotoErrors(result.getAttributesFoto().getFeatureCount(null));
            successful = false;
        }

        if (result.getAttributesLeis() != null) {
            result.setAttributesLeisErrors(result.getAttributesLeis().getFeatureCount(null));
            successful = false;
        }

        if (result.getAttributesTech() != null) {
            result.setAttributesTechErrors(result.getAttributesTech().getFeatureCount(null));
            successful = false;
        }

        if (result.getAttributesUghz() != null) {
            result.setAttributesUghzErrors(result.getAttributesUghz().getFeatureCount(null));
            successful = false;
        }

        if (result.getAttributesWiwe() != null) {
            result.setAttributesWiweErrors(result.getAttributesWiwe().getFeatureCount(null));
            successful = false;
        }

        if (result.getGapDeich() != null) {
            result.setGapDeichErrors(result.getGapDeich().getFeatureCount(null));
            successful = false;
        }

        if (result.getGapTech() != null) {
            result.setGapTechErrors(result.getGapTech().getFeatureCount(null));
            successful = false;
        }

        if (result.getGapWiwe() != null) {
            result.setGapWiweErrors(result.getGapWiwe().getFeatureCount(null));
            successful = false;
        }

        if (result.getGerinneDeich() != null) {
            result.setGerinneDeichErrors(result.getGerinneDeich().getFeatureCount(null));
            successful = false;
        }

        if (result.getGerinneLeis() != null) {
            result.setGerinneLeisErrors(result.getGerinneLeis().getFeatureCount(null));
            successful = false;
        }

        if (result.getGerinneWiwe() != null) {
            result.setGerinneWiweErrors(result.getGerinneWiwe().getFeatureCount(null));
            successful = false;
        }

        if (result.getGeschGerinneTech() != null) {
            result.setGeschGerinneTechErrors(result.getGeschGerinneTech().getFeatureCount(null));
            successful = false;
        }

        if (result.getOffGerinneTech() != null) {
            result.setOffGerinneTechErrors(result.getOffGerinneTech().getFeatureCount(null));
            successful = false;
        }

        if (result.getOverlappedTech() != null) {
            result.setOverlappedTechErrors(result.getOverlappedTech().getFeatureCount(null));
            successful = false;
        }

        if (result.getdTech() != null) {
            result.setdTechErrors(result.getdTech().getFeatureCount(null));
            successful = false;
        }

        if (result.getvTech() != null) {
            result.setvTechErrors(result.getvTech().getFeatureCount(null));
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
        private int attributesDeichErrors;
        private int gapDeichErrors;
//        private int overlappedDeichErrors;
        private int gerinneDeichErrors;
        private int attributesWiweErrors;
        private int gapWiweErrors;
        private int gerinneWiweErrors;
        private int attributesUghzErrors;
        private int attributesFotoErrors;
        private int attributesLeisErrors;
        private int gerinneLeisErrors;
        private int attributesTechErrors;
        private int gapTechErrors;
        private int overlappedTechErrors;
        private int geschGerinneTechErrors;
        private int offGerinneTechErrors;
        private int dTechErrors;
        private int vTechErrors;
        private ProblemCountAndClasses problemTreeObjectCount;

        private H2FeatureService attributesDeich;
        private H2FeatureService gapDeich;
//        private H2FeatureService overlappedDeich;
        private H2FeatureService gerinneDeich;
        private H2FeatureService attributesWiwe;
        private H2FeatureService gapWiwe;
        private H2FeatureService gerinneWiwe;
        private H2FeatureService attributesUghz;
        private H2FeatureService attributesFoto;
        private H2FeatureService attributesLeis;
        private H2FeatureService gerinneLeis;
        private H2FeatureService attributesTech;
        private H2FeatureService gapTech;
        private H2FeatureService overlappedTech;
        private H2FeatureService geschGerinneTech;
        private H2FeatureService offGerinneTech;
        private H2FeatureService dTech;
        private H2FeatureService vTech;

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
         * @return  the attributesDeichErrors
         */
        public int getAttributesDeichErrors() {
            return attributesDeichErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesDeichErrors  the attributesDeichErrors to set
         */
        public void setAttributesDeichErrors(final int attributesDeichErrors) {
            this.attributesDeichErrors = attributesDeichErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gapDeichErrors
         */
        public int getGapDeichErrors() {
            return gapDeichErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gapDeichErrors  the gapDeichErrors to set
         */
        public void setGapDeichErrors(final int gapDeichErrors) {
            this.gapDeichErrors = gapDeichErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gerinneDeichErrors
         */
        public int getGerinneDeichErrors() {
            return gerinneDeichErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gerinneDeichErrors  the gerinneDeichErrors to set
         */
        public void setGerinneDeichErrors(final int gerinneDeichErrors) {
            this.gerinneDeichErrors = gerinneDeichErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesWiweErrors
         */
        public int getAttributesWiweErrors() {
            return attributesWiweErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesWiweErrors  the attributesWiweErrors to set
         */
        public void setAttributesWiweErrors(final int attributesWiweErrors) {
            this.attributesWiweErrors = attributesWiweErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gapWiweErrors
         */
        public int getGapWiweErrors() {
            return gapWiweErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gapWiweErrors  the gapWiweErrors to set
         */
        public void setGapWiweErrors(final int gapWiweErrors) {
            this.gapWiweErrors = gapWiweErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gerinneWiweErrors
         */
        public int getGerinneWiweErrors() {
            return gerinneWiweErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gerinneWiweErrors  the gerinneWiweErrors to set
         */
        public void setGerinneWiweErrors(final int gerinneWiweErrors) {
            this.gerinneWiweErrors = gerinneWiweErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesUghzErrors
         */
        public int getAttributesUghzErrors() {
            return attributesUghzErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesUghzErrors  the attributesUghzErrors to set
         */
        public void setAttributesUghzErrors(final int attributesUghzErrors) {
            this.attributesUghzErrors = attributesUghzErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesFotoErrors
         */
        public int getAttributesFotoErrors() {
            return attributesFotoErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesFotoErrors  the attributesFotoErrors to set
         */
        public void setAttributesFotoErrors(final int attributesFotoErrors) {
            this.attributesFotoErrors = attributesFotoErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesLeisErrors
         */
        public int getAttributesLeisErrors() {
            return attributesLeisErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesLeisErrors  the attributesLeisErrors to set
         */
        public void setAttributesLeisErrors(final int attributesLeisErrors) {
            this.attributesLeisErrors = attributesLeisErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gerinneLeisErrors
         */
        public int getGerinneLeisErrors() {
            return gerinneLeisErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gerinneLeisErrors  the gerinneLeisErrors to set
         */
        public void setGerinneLeisErrors(final int gerinneLeisErrors) {
            this.gerinneLeisErrors = gerinneLeisErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesTechErrors
         */
        public int getAttributesTechErrors() {
            return attributesTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesTechErrors  the attributesTechErrors to set
         */
        public void setAttributesTechErrors(final int attributesTechErrors) {
            this.attributesTechErrors = attributesTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gapTechErrors
         */
        public int getGapTechErrors() {
            return gapTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gapTechErrors  the gapTechErrors to set
         */
        public void setGapTechErrors(final int gapTechErrors) {
            this.gapTechErrors = gapTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the overlappedTechErrors
         */
        public int getOverlappedTechErrors() {
            return overlappedTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  overlappedTechErrors  the overlappedTechErrors to set
         */
        public void setOverlappedTechErrors(final int overlappedTechErrors) {
            this.overlappedTechErrors = overlappedTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the geschGerinneTechErrors
         */
        public int getGeschGerinneTechErrors() {
            return geschGerinneTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  geschGerinneTechErrors  the geschGerinneTechErrors to set
         */
        public void setGeschGerinneTechErrors(final int geschGerinneTechErrors) {
            this.geschGerinneTechErrors = geschGerinneTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the offGerinneTechErrors
         */
        public int getOffGerinneTechErrors() {
            return offGerinneTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  offGerinneTechErrors  the offGerinneTechErrors to set
         */
        public void setOffGerinneTechErrors(final int offGerinneTechErrors) {
            this.offGerinneTechErrors = offGerinneTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dTechErrors
         */
        public int getdTechErrors() {
            return dTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dTechErrors  the dTechErrors to set
         */
        public void setdTechErrors(final int dTechErrors) {
            this.dTechErrors = dTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the vTechErrors
         */
        public int getvTechErrors() {
            return vTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  vTechErrors  the vTechErrors to set
         */
        public void setvTechErrors(final int vTechErrors) {
            this.vTechErrors = vTechErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesDeich
         */
        public H2FeatureService getAttributesDeich() {
            return attributesDeich;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesDeich  the attributesDeich to set
         */
        public void setAttributesDeich(final H2FeatureService attributesDeich) {
            this.attributesDeich = attributesDeich;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gapDeich
         */
        public H2FeatureService getGapDeich() {
            return gapDeich;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gapDeich  the gapDeich to set
         */
        public void setGapDeich(final H2FeatureService gapDeich) {
            this.gapDeich = gapDeich;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gerinneDeich
         */
        public H2FeatureService getGerinneDeich() {
            return gerinneDeich;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gerinneDeich  the gerinneDeich to set
         */
        public void setGerinneDeich(final H2FeatureService gerinneDeich) {
            this.gerinneDeich = gerinneDeich;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesWiwe
         */
        public H2FeatureService getAttributesWiwe() {
            return attributesWiwe;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesWiwe  the attributesWiwe to set
         */
        public void setAttributesWiwe(final H2FeatureService attributesWiwe) {
            this.attributesWiwe = attributesWiwe;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gapWiwe
         */
        public H2FeatureService getGapWiwe() {
            return gapWiwe;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gapWiwe  the gapWiwe to set
         */
        public void setGapWiwe(final H2FeatureService gapWiwe) {
            this.gapWiwe = gapWiwe;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gerinneWiwe
         */
        public H2FeatureService getGerinneWiwe() {
            return gerinneWiwe;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gerinneWiwe  the gerinneWiwe to set
         */
        public void setGerinneWiwe(final H2FeatureService gerinneWiwe) {
            this.gerinneWiwe = gerinneWiwe;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesUghz
         */
        public H2FeatureService getAttributesUghz() {
            return attributesUghz;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesUghz  the attributesUghz to set
         */
        public void setAttributesUghz(final H2FeatureService attributesUghz) {
            this.attributesUghz = attributesUghz;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesFoto
         */
        public H2FeatureService getAttributesFoto() {
            return attributesFoto;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesFoto  the attributesFoto to set
         */
        public void setAttributesFoto(final H2FeatureService attributesFoto) {
            this.attributesFoto = attributesFoto;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesLeis
         */
        public H2FeatureService getAttributesLeis() {
            return attributesLeis;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesLeis  the attributesLeis to set
         */
        public void setAttributesLeis(final H2FeatureService attributesLeis) {
            this.attributesLeis = attributesLeis;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gerinneLeis
         */
        public H2FeatureService getGerinneLeis() {
            return gerinneLeis;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gerinneLeis  the gerinneLeis to set
         */
        public void setGerinneLeis(final H2FeatureService gerinneLeis) {
            this.gerinneLeis = gerinneLeis;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributesTech
         */
        public H2FeatureService getAttributesTech() {
            return attributesTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributesTech  the attributesTech to set
         */
        public void setAttributesTech(final H2FeatureService attributesTech) {
            this.attributesTech = attributesTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gapTech
         */
        public H2FeatureService getGapTech() {
            return gapTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gapTech  the gapTech to set
         */
        public void setGapTech(final H2FeatureService gapTech) {
            this.gapTech = gapTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the overlappedTech
         */
        public H2FeatureService getOverlappedTech() {
            return overlappedTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  overlappedTech  the overlappedTech to set
         */
        public void setOverlappedTech(final H2FeatureService overlappedTech) {
            this.overlappedTech = overlappedTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the geschGerinneTech
         */
        public H2FeatureService getGeschGerinneTech() {
            return geschGerinneTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  geschGerinneTech  the geschGerinneTech to set
         */
        public void setGeschGerinneTech(final H2FeatureService geschGerinneTech) {
            this.geschGerinneTech = geschGerinneTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the offGerinneTech
         */
        public H2FeatureService getOffGerinneTech() {
            return offGerinneTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  offGerinneTech  the offGerinneTech to set
         */
        public void setOffGerinneTech(final H2FeatureService offGerinneTech) {
            this.offGerinneTech = offGerinneTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dTech
         */
        public H2FeatureService getdTech() {
            return dTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dTech  the dTech to set
         */
        public void setdTech(final H2FeatureService dTech) {
            this.dTech = dTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the vTech
         */
        public H2FeatureService getvTech() {
            return vTech;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  vTech  the vTech to set
         */
        public void setvTech(final H2FeatureService vTech) {
            this.vTech = vTech;
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
