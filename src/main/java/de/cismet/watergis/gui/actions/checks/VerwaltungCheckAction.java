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
import de.cismet.cids.custom.watergis.server.search.BakWithIncompleteGbCoverage;
import de.cismet.cids.custom.watergis.server.search.BakWithIncompleteGmdCoverage;
import de.cismet.cids.custom.watergis.server.search.BakWithIncompleteSbCoverage;
import de.cismet.cids.custom.watergis.server.search.DeleteInvalidFgBaExp;
import de.cismet.cids.custom.watergis.server.search.FgBakCount;
import de.cismet.cids.custom.watergis.server.search.MergeBaExp;
import de.cismet.cids.custom.watergis.server.search.MergeBaSb;
import de.cismet.cids.custom.watergis.server.search.OverlappedGb;
import de.cismet.cids.custom.watergis.server.search.OverlappedGmd;
import de.cismet.cids.custom.watergis.server.search.OverlappedSb;

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
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class VerwaltungCheckAction extends AbstractCheckAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final MetaClass FG_BAK = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak");
    private static final MetaClass FG_BA_SB = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_sb");
    private static final MetaClass FG_BA_GB = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_gb");
    private static final MetaClass FG_BA_GMD = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_gmd");
    private static final MetaClass FG_BA_EXP = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_exp");
    private static String QUERY_GB_CATALOGUE;
    private static String QUERY_GMD_CATALOGUE;
    private static String QUERY_GB_STATUS;
    private static String QUERY_GMD_STATUS;
    private static String QUERY_SB_CATALOGUE;
    private static String QUERY_EXP;
    private static final String CHECK_VERWALTUNG_SB_UEBERLAPPUNG = "Prüfungen->Verwaltung->SB->SB: Überlappung";
    private static final String CHECK_VERWALTUNG_GB_UEBERLAPPUNG = "Prüfungen->Verwaltung->GB->GB: Überlappung";
    private static final String CHECK_VERWALTUNG_GMD_UEBERLAPPUNG = "Prüfungen->Verwaltung->GMD->GMD: Überlappung";
    private static final String CHECK_VERWALTUNG_EXP_BELEGUNG = "Prüfungen->Verwaltung->EXP->EXP: Belegung";
    private static final String CHECK_VERWALTUNG_SB_KATALOG = "Prüfungen->Verwaltung->SB->SB: Katalog";
    private static final String CHECK_VERWALTUNG_GMD_KATALOG = "Prüfungen->Verwaltung->GMD->GMD: Referenz GMD";
    private static final String CHECK_VERWALTUNG_GB_KATALOG = "Prüfungen->Verwaltung->GB->GB: Referenz KREIS";
    private static final String CHECK_VERWALTUNG_SB_LUECKE = "Prüfungen->Verwaltung->SB->SB: Lücke";
    private static final String CHECK_VERWALTUNG_GB_LUECKE = "Prüfungen->Verwaltung->GB->GB: Lücke";
    private static final String CHECK_VERWALTUNG_GMD_LUECKE = "Prüfungen->Verwaltung->GMD->GMD: Lücke";
    private static final String[] ALL_CHECKS = new String[] {
            CHECK_VERWALTUNG_SB_UEBERLAPPUNG,
            CHECK_VERWALTUNG_GB_UEBERLAPPUNG,
            CHECK_VERWALTUNG_GMD_UEBERLAPPUNG,
            CHECK_VERWALTUNG_EXP_BELEGUNG,
            CHECK_VERWALTUNG_SB_KATALOG,
            CHECK_VERWALTUNG_GMD_KATALOG,
            CHECK_VERWALTUNG_GB_KATALOG,
            CHECK_VERWALTUNG_SB_LUECKE,
            CHECK_VERWALTUNG_GB_LUECKE,
            CHECK_VERWALTUNG_GMD_LUECKE
        };
    private static int[] USED_CLASS_IDS = new int[] {
            ((FG_BA_SB != null) ? FG_BA_SB.getId() : -1),
            ((FG_BA_GB != null) ? FG_BA_GB.getId() : -1),
            ((FG_BA_GMD != null) ? FG_BA_GMD.getId() : -1),
        };

    static {
        if (FG_BA_EXP != null) {
            USED_CLASS_IDS = new int[] {
                    ((FG_BA_SB != null) ? FG_BA_SB.getId() : -1),
                    ((FG_BA_GB != null) ? FG_BA_GB.getId() : -1),
                    ((FG_BA_GMD != null) ? FG_BA_GMD.getId() : -1),
                    FG_BA_EXP.getId()
                };
        }

        if ((FG_BA_SB != null) && (FG_BA_GB != null) && (FG_BA_GMD != null) && (FG_BAK != null)
                    && (FG_BA_EXP != null)) {
            final User user = SessionManager.getSession().getUser();

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_GMD_CATALOGUE = "select distinct " + FG_BA_GMD.getID() + ", t." + FG_BA_GMD.getPrimaryKey()
                            + " from " + FG_BA_GMD.getTableName() + " t\n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "where (%1$s is null or ba.id = any(%1$s)) and "
                            + "(((nr_li is not null or name_li is not null) and "
                            + "not exists (select 1 from dlm25w.vw_alk_gmd where gmd_nr = nr_li and "
                            + "gmd_name = name_li limit 1)) or ((nr_re is not null or name_re is not "
                            + "null) and not exists (select 1 from dlm25w.vw_alk_gmd where gmd_nr = "
                            + "nr_re and gmd_name = name_re limit 1)));";
            } else {
                QUERY_GMD_CATALOGUE = "select distinct " + FG_BA_GMD.getID() + ", t." + FG_BA_GMD.getPrimaryKey()
                            + " from " + FG_BA_GMD.getTableName() + " t \n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id) "
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or ba.id = any(%1$s)) and (((nr_li is not null or name_li is not null) and not exists (select 1 from dlm25w.vw_alk_gmd where gmd_nr = nr_li and gmd_name = name_li limit 1)) or ((nr_re is not null or name_re is not null) and not exists (select 1 from dlm25w.vw_alk_gmd where gmd_nr = nr_re and gmd_name = name_re limit 1))) and (gr.owner = '"
                            + user.getUserGroup().getName()
                            + "' or %2$s)";
            }

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_GB_CATALOGUE = "select distinct " + FG_BA_GB.getID() + ", t." + FG_BA_GB.getPrimaryKey()
                            + " from " + FG_BA_GB.getTableName() + " t\n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "where (%1$s is null or ba.id = any(%1$s)) and (((nr_li is not null) and not exists (select 1 from dlm25w.vw_alk_kreis where kreis_nr = nr_li limit 1)) or ((nr_re is not null) and not exists (select 1 from dlm25w.vw_alk_kreis where kreis_nr = nr_re limit 1)));";
            } else {
                QUERY_GB_CATALOGUE = "select distinct " + FG_BA_GB.getID() + ", t." + FG_BA_GB.getPrimaryKey()
                            + " from " + FG_BA_GB.getTableName() + " t \n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id) "
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or ba.id = any(%1$s)) and (((nr_li is not null) and not exists (select 1 from dlm25w.vw_alk_kreis where kreis_nr = nr_li limit 1)) or ((nr_re is not null) and not exists (select 1 from dlm25w.vw_alk_kreis where kreis_nr = nr_re limit 1))) and (gr.owner = '"
                            + user.getUserGroup().getName()
                            + "' or %2$s)";
            }

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_GMD_STATUS = "select distinct " + FG_BA_GMD.getID() + ", t." + FG_BA_GMD.getPrimaryKey()
                            + " from " + FG_BA_GMD.getTableName() + " t\n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "where (%1$s is null or ba.id = any(%1$s)) and (((nr_li is not null) and not exists (select 1 from dlm25w.vw_alk_gmd where kreis_nr = nr_li limit 1)) or ((nr_re is not null) and not exists (select 1 from dlm25w.vw_alk_kreis where kreis_nr = nr_re limit 1)));";
            } else {
                QUERY_GMD_STATUS = "select distinct " + FG_BA_GMD.getID() + ", t." + FG_BA_GMD.getPrimaryKey()
                            + " from " + FG_BA_GMD.getTableName() + " t \n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id) "
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or ba.id = any(%1$s)) and (((nr_li is not null) and not exists (select 1 from dlm25w.vw_alk_gmd where kreis_nr = nr_li limit 1)) or ((nr_re is not null) and not exists (select 1 from dlm25w.vw_alk_kreis where kreis_nr = nr_re limit 1))) and (gr.owner = '"
                            + user.getUserGroup().getName()
                            + "' or %2$s)";
            }

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_GB_STATUS = "select distinct " + FG_BA_GB.getID() + ", t." + FG_BA_GB.getPrimaryKey()
                            + " from " + FG_BA_GB.getTableName() + " t\n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "where (%1$s is null or ba.id = any(%1$s)) ;";
            } else {
                QUERY_GB_STATUS = "select distinct " + FG_BA_GB.getID() + ", t." + FG_BA_GB.getPrimaryKey()
                            + " from " + FG_BA_GB.getTableName() + " t \n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id) "
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (%1$s is null or ba.id = any(%1$s)) and (gr.owner = '"
                            + user.getUserGroup().getName()
                            + "' or %2$s)";
            }

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_SB_CATALOGUE = "select distinct " + FG_BA_SB.getID() + ", t." + FG_BA_SB.getPrimaryKey()
                            + " from " + FG_BA_SB.getTableName() + " t\n"
                            + "left join dlm25w.k_sb k on (k.id = t.sb) \n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "join dlm25w.k_ww_gr sbgr on (k.ww_gr = sbgr.id) "
                            + "join dlm25w.k_ww_gr bagr on (ba.ww_gr = bagr.id)"
                            + "where (%1$s is null or ba.id = any(%1$s)) and ((k.id is null and t.sb is not null) or sbgr.owner <> bagr.owner);";
            } else {
                QUERY_SB_CATALOGUE = "select distinct " + FG_BA_SB.getID() + ", t." + FG_BA_SB.getPrimaryKey()
                            + " from " + FG_BA_SB.getTableName() + " t \n"
                            + "left join dlm25w.k_sb k on (k.id = t.sb) \n"
                            + "join dlm25w.fg_ba_linie linie on (ba_st = linie.id) "
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_ba ba on (von.route = ba.id) "
                            + "join dlm25w.fg_bak bak on (ba.bak_id = bak.id) "
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "join dlm25w.k_ww_gr sbgr on (k.ww_gr = sbgr.id) "
                            + "join dlm25w.k_ww_gr bagr on (ba.ww_gr = bagr.id)"
                            + "where (%1$s is null or ba.id = any(%1$s)) and ((k.id is null and t.sb is not null) or sbgr.owner <> bagr.owner) and (gr.owner = '"
                            + user.getUserGroup().getName()
                            + "' or %2$s)";
            }

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_EXP = "select distinct " + FG_BA_EXP.getID() + ", unnest(array_agg(exp.id)) as id "
                            + " from " + FG_BA_EXP.getTableName() + " exp\n"
                            + "join dlm25w.fg_ba_linie linie on (exp.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba ba on (von.route =  ba.id)\n"
                            + "where  (%1$s is null or ba.id = any(%1$s)) \n"
                            + "group by ba.id, exp.ww_gr\n"
                            + "having count(*) > 1";
            } else {
                QUERY_EXP = "select distinct " + FG_BA_EXP.getID() + ", unnest(array_agg(exp.id)) as id "
                            + " from " + FG_BA_EXP.getTableName() + " exp \n"
                            + "join dlm25w.fg_ba_linie linie on (exp.ba_st = linie.id)\n"
                            + "join dlm25w.fg_ba_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_ba ba on (von.route =  ba.id)\n"
                            + "join dlm25w.k_ww_gr gr on (exp.ww_gr = gr.id)\n"
                            + "where (%1$s is null or ba.id = any(%1$s)) and (gr.owner = '"
                            + user.getUserGroup().getName()
                            + "' or %2$s) \n"
                            + "group by ba.id, exp.ww_gr\n"
                            + "having count(*) > 1";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    // dlm25w.merge_fg_bak_gwk()
    private boolean successful = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new VerwaltungCheckAction object.
     */
    public VerwaltungCheckAction() {
        this(false);
    }

    /**
     * Creates a new VerwaltungCheckAction object.
     *
     * @param  isBackgroundCheck  DOCUMENT ME!
     */
    public VerwaltungCheckAction(final boolean isBackgroundCheck) {
        super(isBackgroundCheck);
        final String tooltip = org.openide.util.NbBundle.getMessage(
                VerwaltungCheckAction.class,
                "VerwaltungCheckAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                VerwaltungCheckAction.class,
                "VerwaltungCheckAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-zoom.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getProgressSteps() {
        return 15;
    }

    @Override
    public boolean startCheckInternal(final boolean isExport,
            final WaitDialog wd,
            final List<H2FeatureService> result) {
        if (isExport) {
            try {
                final CheckResult cr = check(isExport, wd);

                if (result != null) {
                    addService(result, cr.getExp());
                    addService(result, cr.getIncompleteGb());
                    addService(result, cr.getIncompleteGmd());
                    addService(result, cr.getIncompleteSb());
                    addService(result, cr.getInvalidAttributeGb());
                    addService(result, cr.getInvalidAttributeGmd());
                    addService(result, cr.getInvalidAttributeSb());
                    addService(result, cr.getOverlappedGb());
                    addService(result, cr.getOverlappedGmd());
                    addService(result, cr.getOverlappedSb());
                    addService(result, cr.getStGb());
                    addService(result, cr.getStGmd());
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
                    NbBundle.getMessage(VerwaltungCheckAction.class,
                        "VerwaltungCheckAction.actionPerformed().dialog"),
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
                                        VerwaltungCheckAction.class,
                                        "VerwaltungCheckAction.actionPerformed().result.text.withoutProblems",
                                        new Object[] {
                                            result.getBakCount(),
                                            result.getIncompleteGmdErrors(),
                                            result.getIncompleteGbErrors(),
                                            result.getIncompleteSbErrors(),
                                            result.getInvalidAttributeGmdErrors(),
                                            result.getInvalidAttributeGbErrors(),
                                            result.getInvalidAttributeSbErrors(),
                                            result.getStGmdErrors(),
                                            result.getStGbErrors(),
                                            result.getOverlappedGmdErrors(),
                                            result.getOverlappedGbErrors(),
                                            result.getOverlappedSbErrors(),
                                            result.getExpErrors(),
                                            0
                                        }),
                                    NbBundle.getMessage(
                                        VerwaltungCheckAction.class,
                                        "VerwaltungCheckAction.actionPerformed().result.title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                    NbBundle.getMessage(
                                        VerwaltungCheckAction.class,
                                        "VerwaltungCheckAction.actionPerformed().result.text",
                                        new Object[] {
                                            result.getBakCount(),
                                            result.getIncompleteGmdErrors(),
                                            result.getIncompleteGbErrors(),
                                            result.getIncompleteSbErrors(),
                                            result.getInvalidAttributeGmdErrors(),
                                            result.getInvalidAttributeGbErrors(),
                                            result.getInvalidAttributeSbErrors(),
                                            result.getStGmdErrors(),
                                            result.getStGbErrors(),
                                            result.getOverlappedGmdErrors(),
                                            result.getOverlappedGbErrors(),
                                            result.getOverlappedSbErrors(),
                                            result.getExpErrors(),
                                            result.getProblemTreeObjectCount().getCount(),
                                            result.getProblemTreeObjectCount().getClasses()
                                        }),
                                    NbBundle.getMessage(
                                        VerwaltungCheckAction.class,
                                        "VerwaltungCheckAction.actionPerformed().result.title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                            if (result.getExp() != null) {
                                showService(result.getExp(),
                                    "Prüfungen->Verwaltung->EXP");
                            }
                            if (result.getOverlappedSb() != null) {
                                showService(result.getOverlappedSb(),
                                    "Prüfungen->Verwaltung->SB");
                            }
                            if (result.getInvalidAttributeSb() != null) {
                                showService(result.getInvalidAttributeSb(),
                                    "Prüfungen->Verwaltung->SB");
                            }
                            if (result.getIncompleteSb() != null) {
                                showService(result.getIncompleteSb(),
                                    "Prüfungen->Verwaltung->SB");
                            }
                            if (result.getOverlappedGb() != null) {
                                showService(result.getOverlappedGb(),
                                    "Prüfungen->Verwaltung->GB");
                            }
                            if (result.getInvalidAttributeGb() != null) {
                                showService(result.getInvalidAttributeGb(),
                                    "Prüfungen->Verwaltung->GB");
                            }
                            if (result.getIncompleteGb() != null) {
                                showService(result.getIncompleteGb(),
                                    "Prüfungen->Verwaltung->GB");
                            }
                            if (result.getOverlappedGmd() != null) {
                                showService(result.getOverlappedGmd(),
                                    "Prüfungen->Verwaltung->GMD");
                            }
                            if (result.getInvalidAttributeGmd() != null) {
                                showService(result.getInvalidAttributeGmd(),
                                    "Prüfungen->Verwaltung->GMD");
                            }
                            if (result.getIncompleteGmd() != null) {
                                showService(result.getIncompleteGmd(),
                                    "Prüfungen->Verwaltung->GMD");
                            }
                            refreshTree();
                            refreshMap();
                        } catch (Exception e) {
                            LOG.error("Error while performing the verwaltung analyse.", e);
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
    @Override
    protected CheckResult check(final boolean isExport, final WaitDialog wd) throws Exception {
        final CheckResult result = new CheckResult();
        String user = AppBroker.getInstance().getOwner();
        final int[] selectedIds = getSelectedIds(isExport);

        if (user.equalsIgnoreCase("Administratoren")) {
            user = null;
        }

        removeServicesFromDb(ALL_CHECKS);

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
        final CidsServerSearch mergeSb = new MergeBaSb(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeSb);
        increaseProgress(wd, 1);

        final CidsServerSearch mergeExp = new MergeBaExp(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeExp);
        increaseProgress(wd, 1);

        final CidsServerSearch deletBaExp = new DeleteInvalidFgBaExp(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), deletBaExp);
        increaseProgress(wd, 1);

        // start checks
        final boolean useExpCond = user != null;
        final boolean export = isExport && useExpCond;
        final String expCondition = ((isExport && useExpCond)
                ? (" exists(select id from dlm25w.fg_ba_exp_complete where owner = '" + user + "' and bak_id = bak.id)")
                : "false");
        final List<FeatureServiceAttribute> baGmdServiceAttributeDefinition = new ArrayList<FeatureServiceAttribute>();

        FeatureServiceAttribute serviceAttribute = new FeatureServiceAttribute(
                "id",
                String.valueOf(Types.INTEGER),
                true);
        baGmdServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true);
        baGmdServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ww_gr", String.valueOf(Types.INTEGER), true);
        baGmdServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_cd", String.valueOf(Types.VARCHAR), true);
        baGmdServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_von", String.valueOf(Types.DOUBLE), true);
        baGmdServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_bis", String.valueOf(Types.DOUBLE), true);
        baGmdServiceAttributeDefinition.add(serviceAttribute);
//        serviceAttribute = new FeatureServiceAttribute("gmd_nr_re", String.valueOf(Types.VARCHAR), true);
//        baGmdServiceAttributeDefinition.add(serviceAttribute);
//        serviceAttribute = new FeatureServiceAttribute("gmd_nr_li", String.valueOf(Types.VARCHAR), true);
//        baGmdServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("laenge", String.valueOf(Types.DOUBLE), true);
        baGmdServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_date", String.valueOf(Types.TIMESTAMP), true);
        baGmdServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_user", String.valueOf(Types.VARCHAR), true);
        baGmdServiceAttributeDefinition.add(serviceAttribute);

        result.setIncompleteGmd(analyseByCustomSearch(
                new BakWithIncompleteGmdCoverage(user, selectedIds, export),
                CHECK_VERWALTUNG_GMD_LUECKE,
                baGmdServiceAttributeDefinition));
        increaseProgress(wd, 1);

        final List<FeatureServiceAttribute> baGbServiceAttributeDefinition = new ArrayList<FeatureServiceAttribute>();

        serviceAttribute = new FeatureServiceAttribute(
                "id",
                String.valueOf(Types.INTEGER),
                true);
        baGbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true);
        baGbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ww_gr", String.valueOf(Types.INTEGER), true);
        baGbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_cd", String.valueOf(Types.VARCHAR), true);
        baGbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_von", String.valueOf(Types.DOUBLE), true);
        baGbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_bis", String.valueOf(Types.DOUBLE), true);
        baGbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("laenge", String.valueOf(Types.DOUBLE), true);
        baGbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_date", String.valueOf(Types.TIMESTAMP), true);
        baGbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_user", String.valueOf(Types.VARCHAR), true);
        baGbServiceAttributeDefinition.add(serviceAttribute);

        result.setIncompleteGb(analyseByCustomSearch(
                new BakWithIncompleteGbCoverage(user, selectedIds, export),
                CHECK_VERWALTUNG_GB_LUECKE,
                baGbServiceAttributeDefinition));
        increaseProgress(wd, 1);

        final List<FeatureServiceAttribute> baSbServiceAttributeDefinition = new ArrayList<FeatureServiceAttribute>();

        serviceAttribute = new FeatureServiceAttribute(
                "id",
                String.valueOf(Types.INTEGER),
                true);
        baSbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true);
        baSbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ww_gr", String.valueOf(Types.INTEGER), true);
        baSbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_cd", String.valueOf(Types.VARCHAR), true);
        baSbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_von", String.valueOf(Types.DOUBLE), true);
        baSbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_st_bis", String.valueOf(Types.DOUBLE), true);
        baSbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("laenge", String.valueOf(Types.DOUBLE), true);
        baSbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_date", String.valueOf(Types.TIMESTAMP), true);
        baSbServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_user", String.valueOf(Types.VARCHAR), true);
        baSbServiceAttributeDefinition.add(serviceAttribute);

        result.setIncompleteSb(analyseByCustomSearch(
                new BakWithIncompleteSbCoverage(user, selectedIds, export),
                CHECK_VERWALTUNG_SB_LUECKE,
                baSbServiceAttributeDefinition));
        increaseProgress(wd, 1);

        String query = (useExpCond
                ? String.format(QUERY_GB_CATALOGUE, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_GB_CATALOGUE, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setInvalidAttributeGb(analyseByQuery(
                FG_BA_GB,
                query,
                CHECK_VERWALTUNG_GB_KATALOG));
        increaseProgress(wd, 1);

//                    result.setInvalidAttributeGb(analyseByQuery(
//                            String.format(QUERY_GB_STATUS, SQLFormatter.createSqlArrayString(selectedIds)),
//                            "Prüfungen->Verwaltung->GB->Referenz Status"));
//        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_GMD_CATALOGUE, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_GMD_CATALOGUE, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setInvalidAttributeGmd(analyseByQuery(
                FG_BA_GMD,
                query,
                CHECK_VERWALTUNG_GMD_KATALOG));
        increaseProgress(wd, 1);

//                    result.setInvalidAttributeGmd(analyseByQuery(
//                            String.format(QUERY_GMD_STATUS, SQLFormatter.createSqlArrayString(selectedIds)),
//                            "Prüfungen->Verwaltung->GMD->Referenz Status"));
//        increaseProgress(wd, 1);

        query = (useExpCond
                ? String.format(QUERY_SB_CATALOGUE, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                : String.format(QUERY_SB_CATALOGUE, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setInvalidAttributeSb(analyseByQuery(
                FG_BA_SB,
                query,
                CHECK_VERWALTUNG_SB_KATALOG));
        increaseProgress(wd, 1);

        query = (useExpCond ? String.format(QUERY_EXP, SQLFormatter.createSqlArrayString(selectedIds), expCondition)
                            : String.format(QUERY_EXP, SQLFormatter.createSqlArrayString(selectedIds)));
        result.setExp(analyseByQuery(
                FG_BA_EXP,
                query,
                CHECK_VERWALTUNG_EXP_BELEGUNG));
        increaseProgress(wd, 1);

        result.setOverlappedGmd(analyseByCustomSearch(
                new OverlappedGmd(user, selectedIds, export),
                CHECK_VERWALTUNG_GMD_UEBERLAPPUNG,
                baGmdServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setOverlappedGb(analyseByCustomSearch(
                new OverlappedGb(user, selectedIds, export),
                CHECK_VERWALTUNG_GB_UEBERLAPPUNG,
                baGbServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setOverlappedSb(analyseByCustomSearch(
                new OverlappedSb(user, selectedIds, export),
                CHECK_VERWALTUNG_SB_UEBERLAPPUNG,
                baSbServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setProblemTreeObjectCount(getErrorObjectsFromTree(user, selectedIds, USED_CLASS_IDS, isExport));

        if (result.getIncompleteGb() != null) {
            result.setIncompleteGbErrors(result.getIncompleteGb().getFeatureCount(null));
            successful = false;
        }

        if (result.getIncompleteGmd() != null) {
            result.setIncompleteGmdErrors(result.getIncompleteGmd().getFeatureCount(null));
            successful = false;
        }

        if (result.getIncompleteSb() != null) {
            result.setIncompleteSbErrors(result.getIncompleteSb().getFeatureCount(null));
            successful = false;
        }

        if (result.getInvalidAttributeGb() != null) {
            result.setInvalidAttributeGbErrors(result.getInvalidAttributeGb().getFeatureCount(null));
            successful = false;
        }

        if (result.getInvalidAttributeGmd() != null) {
            result.setInvalidAttributeGmdErrors(result.getInvalidAttributeGmd().getFeatureCount(null));
            successful = false;
        }

        if (result.getInvalidAttributeSb() != null) {
            result.setInvalidAttributeSbErrors(result.getInvalidAttributeSb().getFeatureCount(null));
            successful = false;
        }

        if (result.getExp() != null) {
            result.setExpErrors(result.getExp().getFeatureCount(null));
            successful = false;
        }

        if (result.getOverlappedGb() != null) {
            result.setOverlappedGbErrors(result.getOverlappedGb().getFeatureCount(null));
            successful = false;
        }

        if (result.getOverlappedGmd() != null) {
            result.setOverlappedGmdErrors(result.getOverlappedGmd().getFeatureCount(null));
            successful = false;
        }

        if (result.getOverlappedSb() != null) {
            result.setOverlappedSbErrors(result.getOverlappedSb().getFeatureCount(null));
            successful = false;
        }

        if (result.getStGmd() != null) {
            result.setStGmdErrors(result.getStGmd().getFeatureCount(null));
            successful = false;
        }

        if (result.getStGb() != null) {
            result.setStGbErrors(result.getStGb().getFeatureCount(null));
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
    protected static class CheckResult extends AbstractCheckResult {

        //~ Static fields/initializers -----------------------------------------

        private static final String[] CHECK_NAMES = {
                "INCOMPLETE_GB",
                "INCOMPLETE_SB",
                "INCOMPLETE_GMD",
                "INVALID_ATTRIBUTE_GMD",
                "INVALID_ATTRIBUTE_GB",
                "INVALID_ATTRIBUTE_SB",
                "exp",
                "OVERLAPPED_GMD",
                "OVERLAPPED_GB",
                "OVERLAPPEDSB",
                "ST_GB",
                "ST_GMD"
            };

        //~ Instance fields ----------------------------------------------------

        private int incompleteGbErrors;
        private int incompleteSbErrors;
        private int incompleteGmdErrors;
        private int invalidAttributeGmdErrors;
        private int invalidAttributeGbErrors;
        private int invalidAttributeSbErrors;
        private int expErrors;
        private int bakCount;
        private int overlappedGmdErrors;
        private int overlappedGbErrors;
        private int overlappedSbErrors;
        private int stGbErrors;
        private int stGmdErrors;
        private ProblemCountAndClasses problemTreeObjectCount;
        private H2FeatureService incompleteGb;
        private H2FeatureService incompleteSb;
        private H2FeatureService incompleteGmd;
        private H2FeatureService invalidAttributeGmd;
        private H2FeatureService invalidAttributeGb;
        private H2FeatureService invalidAttributeSb;
        private H2FeatureService exp;
        private H2FeatureService overlappedGmd;
        private H2FeatureService overlappedGb;
        private H2FeatureService overlappedSb;
        private H2FeatureService stGb;
        private H2FeatureService stGmd;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the problemTreeObjectCount
         */
        @Override
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
         * @return  the incompleteGbErrors
         */
        public int getIncompleteGbErrors() {
            return incompleteGbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  incompleteGbErrors  the incompleteGbErrors to set
         */
        public void setIncompleteGbErrors(final int incompleteGbErrors) {
            this.incompleteGbErrors = incompleteGbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the incompleteSbErrors
         */
        public int getIncompleteSbErrors() {
            return incompleteSbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  incompleteSbErrors  the incompleteSbErrors to set
         */
        public void setIncompleteSbErrors(final int incompleteSbErrors) {
            this.incompleteSbErrors = incompleteSbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the incompleteGmdErrors
         */
        public int getIncompleteGmdErrors() {
            return incompleteGmdErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  incompleteGmdErrors  the incompleteGmdErrors to set
         */
        public void setIncompleteGmdErrors(final int incompleteGmdErrors) {
            this.incompleteGmdErrors = incompleteGmdErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the invalidAttributeGmdErrors
         */
        public int getInvalidAttributeGmdErrors() {
            return invalidAttributeGmdErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  invalidAttributeGmdErrors  the invalidAttributeGmdErrors to set
         */
        public void setInvalidAttributeGmdErrors(final int invalidAttributeGmdErrors) {
            this.invalidAttributeGmdErrors = invalidAttributeGmdErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the invalidAttributeGbErrors
         */
        public int getInvalidAttributeGbErrors() {
            return invalidAttributeGbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  invalidAttributeGbErrors  the invalidAttributeGbErrors to set
         */
        public void setInvalidAttributeGbErrors(final int invalidAttributeGbErrors) {
            this.invalidAttributeGbErrors = invalidAttributeGbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the invalidAttributeSbErrors
         */
        public int getInvalidAttributeSbErrors() {
            return invalidAttributeSbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  invalidAttributeSbErrors  the invalidAttributeSbErrors to set
         */
        public void setInvalidAttributeSbErrors(final int invalidAttributeSbErrors) {
            this.invalidAttributeSbErrors = invalidAttributeSbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the incompleteGb
         */
        public H2FeatureService getIncompleteGb() {
            return incompleteGb;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  incompleteGb  the incompleteGb to set
         */
        public void setIncompleteGb(final H2FeatureService incompleteGb) {
            this.incompleteGb = incompleteGb;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the incompleteSb
         */
        public H2FeatureService getIncompleteSb() {
            return incompleteSb;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  incompleteSb  the incompleteSb to set
         */
        public void setIncompleteSb(final H2FeatureService incompleteSb) {
            this.incompleteSb = incompleteSb;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the incompleteGmd
         */
        public H2FeatureService getIncompleteGmd() {
            return incompleteGmd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  incompleteGmd  the incompleteGmd to set
         */
        public void setIncompleteGmd(final H2FeatureService incompleteGmd) {
            this.incompleteGmd = incompleteGmd;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the invalidAttributeGmd
         */
        public H2FeatureService getInvalidAttributeGmd() {
            return invalidAttributeGmd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  invalidAttributeGmd  the invalidAttributeGmd to set
         */
        public void setInvalidAttributeGmd(final H2FeatureService invalidAttributeGmd) {
            this.invalidAttributeGmd = invalidAttributeGmd;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the invalidAttributeGb
         */
        public H2FeatureService getInvalidAttributeGb() {
            return invalidAttributeGb;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  invalidAttributeGb  the invalidAttributeGb to set
         */
        public void setInvalidAttributeGb(final H2FeatureService invalidAttributeGb) {
            this.invalidAttributeGb = invalidAttributeGb;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the invalidAttributeSb
         */
        public H2FeatureService getInvalidAttributeSb() {
            return invalidAttributeSb;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  invalidAttributeSb  the invalidAttributeSb to set
         */
        public void setInvalidAttributeSb(final H2FeatureService invalidAttributeSb) {
            this.invalidAttributeSb = invalidAttributeSb;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the expErrors
         */
        public int getExpErrors() {
            return expErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  expErrors  the expErrors to set
         */
        public void setExpErrors(final int expErrors) {
            this.expErrors = expErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the exp
         */
        public H2FeatureService getExp() {
            return exp;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  exp  the exp to set
         */
        public void setExp(final H2FeatureService exp) {
            this.exp = exp;
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
         * @return  the overlappedGmdErrors
         */
        public int getOverlappedGmdErrors() {
            return overlappedGmdErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  overlappedGmdErrors  the overlappedGmdErrors to set
         */
        public void setOverlappedGmdErrors(final int overlappedGmdErrors) {
            this.overlappedGmdErrors = overlappedGmdErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the overlappedGbErrors
         */
        public int getOverlappedGbErrors() {
            return overlappedGbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  overlappedGbErrors  the overlappedGbErrors to set
         */
        public void setOverlappedGbErrors(final int overlappedGbErrors) {
            this.overlappedGbErrors = overlappedGbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the overlappedSbErrors
         */
        public int getOverlappedSbErrors() {
            return overlappedSbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  overlappedSbErrors  the overlappedSbErrors to set
         */
        public void setOverlappedSbErrors(final int overlappedSbErrors) {
            this.overlappedSbErrors = overlappedSbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the overlappedGmd
         */
        public H2FeatureService getOverlappedGmd() {
            return overlappedGmd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  overlappedGmd  the overlappedGmd to set
         */
        public void setOverlappedGmd(final H2FeatureService overlappedGmd) {
            this.overlappedGmd = overlappedGmd;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the overlappedGb
         */
        public H2FeatureService getOverlappedGb() {
            return overlappedGb;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  overlappedGb  the overlappedGb to set
         */
        public void setOverlappedGb(final H2FeatureService overlappedGb) {
            this.overlappedGb = overlappedGb;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the overlappedSb
         */
        public H2FeatureService getOverlappedSb() {
            return overlappedSb;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  overlappedSb  the overlappedSb to set
         */
        public void setOverlappedSb(final H2FeatureService overlappedSb) {
            this.overlappedSb = overlappedSb;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the stGb
         */
        public H2FeatureService getStGb() {
            return stGb;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  stGb  the stGb to set
         */
        public void setStGb(final H2FeatureService stGb) {
            this.stGb = stGb;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the stGmd
         */
        public H2FeatureService getStGmd() {
            return stGmd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  stGmd  the stGmd to set
         */
        public void setStGmd(final H2FeatureService stGmd) {
            this.stGmd = stGmd;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the stGbErrors
         */
        public int getStGbErrors() {
            return stGbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  stGbErrors  the stGbErrors to set
         */
        public void setStGbErrors(final int stGbErrors) {
            this.stGbErrors = stGbErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the stGmdErrors
         */
        public int getStGmdErrors() {
            return stGmdErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  stGmdErrors  the stGmdErrors to set
         */
        public void setStGmdErrors(final int stGmdErrors) {
            this.stGmdErrors = stGmdErrors;
        }

        @Override
        public String[] getCheckNames() {
            return CHECK_NAMES;
        }

        @Override
        public int getErrorsPerCheck(final String checkName) {
            if (checkName.equals(CHECK_NAMES[0])) {
                return incompleteGbErrors;
            } else if (checkName.equals(CHECK_NAMES[1])) {
                return incompleteSbErrors;
            } else if (checkName.equals(CHECK_NAMES[2])) {
                return incompleteGmdErrors;
            } else if (checkName.equals(CHECK_NAMES[3])) {
                return invalidAttributeGmdErrors;
            } else if (checkName.equals(CHECK_NAMES[4])) {
                return invalidAttributeGbErrors;
            } else if (checkName.equals(CHECK_NAMES[5])) {
                return invalidAttributeSbErrors;
            } else if (checkName.equals(CHECK_NAMES[6])) {
                return expErrors;
            } else if (checkName.equals(CHECK_NAMES[7])) {
                return overlappedGmdErrors;
            } else if (checkName.equals(CHECK_NAMES[8])) {
                return overlappedGbErrors;
            } else if (checkName.equals(CHECK_NAMES[9])) {
                return overlappedSbErrors;
            } else if (checkName.equals(CHECK_NAMES[10])) {
                return stGbErrors;
            } else if (checkName.equals(CHECK_NAMES[11])) {
                return stGmdErrors;
            } else {
                return 0;
            }
        }

        @Override
        public H2FeatureService getErrorTablePerCheck(final String checkName) {
            if (checkName.equals(CHECK_NAMES[0])) {
                return incompleteGb;
            } else if (checkName.equals(CHECK_NAMES[1])) {
                return incompleteSb;
            } else if (checkName.equals(CHECK_NAMES[2])) {
                return incompleteGmd;
            } else if (checkName.equals(CHECK_NAMES[3])) {
                return invalidAttributeGmd;
            } else if (checkName.equals(CHECK_NAMES[4])) {
                return invalidAttributeGb;
            } else if (checkName.equals(CHECK_NAMES[5])) {
                return invalidAttributeSb;
            } else if (checkName.equals(CHECK_NAMES[6])) {
                return exp;
            } else if (checkName.equals(CHECK_NAMES[7])) {
                return overlappedGmd;
            } else if (checkName.equals(CHECK_NAMES[8])) {
                return overlappedGb;
            } else if (checkName.equals(CHECK_NAMES[9])) {
                return overlappedSb;
            } else if (checkName.equals(CHECK_NAMES[10])) {
                return stGb;
            } else if (checkName.equals(CHECK_NAMES[11])) {
                return stGmd;
            } else {
                return null;
            }
        }
    }
}
