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

import java.awt.event.ActionEvent;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.custom.watergis.server.search.BakCount;
import de.cismet.cids.custom.watergis.server.search.MergeBakAe;
import de.cismet.cids.custom.watergis.server.search.RemoveDuplicatedNodesFromFgBak;
import de.cismet.cids.custom.watergis.server.search.RouteProblemsCount;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.featureservice.H2FeatureService;

import de.cismet.tools.gui.StaticSwingTools;
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
public class BasicRoutesCheckAction extends AbstractCheckAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final MetaClass BAK_AE_MC = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_ae");
    private static final MetaClass BAK_MC = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak");
    private static String QUERY_AE = null;
    private static String QUERY_FG_BAK_LENGTH;
    private static String QUERY_CROSSING_LINES;
    private static String QUERY_WITHOUT_BA_CD;
    private static String QUERY_DUPLICATED_BA_CD;
    private static String QUERY_PREFIX;
    private static String QUERY_PREFIX_ME_WW_GR_OTHER;

    static {
        if ((BAK_AE_MC != null) && (BAK_MC != null)) {
            final User user = SessionManager.getSession().getUser();

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_FG_BAK_LENGTH = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "join geom g on (bak.geom = g.id) \n"
                            + "where st_length(geo_field) < 0.5";
            } else {
                QUERY_FG_BAK_LENGTH = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "join geom g on (bak.geom = g.id) \n"
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where st_length(geo_field) < 0.5 and gr.owner = '" + user.getUserGroup().getName() + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_CROSSING_LINES = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "join geom g on (bak.geom = g.id) \n"
                            + "where not dlm25w.check_for_crossed_lines(geo_field)";
            } else {
                QUERY_CROSSING_LINES = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "join geom g on (bak.geom = g.id) \n"
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where not dlm25w.check_for_crossed_lines(geo_field) "
                            + "and gr.owner = '" + user.getUserGroup().getName() + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_WITHOUT_BA_CD = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "where ba_cd is null OR ba_cd = ''";
            } else {
                QUERY_WITHOUT_BA_CD = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where (ba_cd is null OR ba_cd = '') and gr.owner = '" + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_DUPLICATED_BA_CD = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "where ba_cd in (select ba_cd from dlm25w.fg_bak group by ba_cd having count(ba_cd) > 1)";
            } else {
                QUERY_DUPLICATED_BA_CD = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where ba_cd in (select ba_cd from dlm25w.fg_bak group by ba_cd having count(ba_cd) > 1) "
                            + "and gr.owner = '" + user.getUserGroup().getName() + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_PREFIX = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where substr(ba_cd, 1, length(gr.praefix) + 1) <>  (gr.praefix || ':')";
            } else {
                QUERY_PREFIX = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak \n"
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where substr(ba_cd, 1, length(gr.praefix) + 1) <>  (gr.praefix || ':') "
                            + "and gr.owner = '" + user.getUserGroup().getName() + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_PREFIX_ME_WW_GR_OTHER = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak "
                            + "join dlm25w.k_ww_gr gr on (substr(ba_cd, 1, length(gr.praefix) + 1) = (gr.praefix || ':'))\n"
                            + "join dlm25w.k_ww_gr gr1 on (bak.ww_gr = gr1.id)\n"
                            + "where substr(ba_cd, 1, length(gr1.praefix) + 1) <>  (gr1.praefix || ':')";
            } else {
                QUERY_PREFIX_ME_WW_GR_OTHER = "select " + BAK_MC.getID() + ", bak." + BAK_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak bak "
                            + "join dlm25w.k_ww_gr gr on (substr(ba_cd, 1, length(gr.praefix) + 1) = (gr.praefix || ':'))\n"
                            + "join dlm25w.k_ww_gr gr1 on (bak.ww_gr = gr1.id)\n"
                            + "where substr(ba_cd, 1, length(gr1.praefix) + 1) <>  (gr1.praefix || ':')"
                            + "and gr.owner = '" + user.getUserGroup().getName() + "'";
            }

            if ((user == null) || user.getUserGroup().getName().startsWith("lung")
                        || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_AE = "select " + BAK_AE_MC.getID() + ", ae." + BAK_AE_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak_ae ae \n"
                            + "join dlm25w.fg_bak_linie linie on (ae.bak_st = linie.id) \n"
                            + "join dlm25w.fg_bak_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_bak_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_bak bak on (von.route = bak.id) \n"
                            + "join geom on (bak.geom = geom.id) \n"
                            + "where (von.wert = 0 and abs(bis.wert - st_length(geo_field)) < 1) or \n"
                            + "(von.wert > 0 and abs(bis.wert - st_length(geo_field)) >= 1);";
            } else {
                QUERY_AE = "select " + BAK_AE_MC.getID() + ", ae." + BAK_AE_MC.getPrimaryKey()
                            + " from dlm25w.fg_bak_ae ae \n"
                            + "join dlm25w.fg_bak_linie linie on (ae.bak_st = linie.id) \n"
                            + "join dlm25w.fg_bak_punkt von on (linie.von = von.id)\n"
                            + "join dlm25w.fg_bak_punkt bis on (linie.bis = bis.id)\n"
                            + "join dlm25w.fg_bak bak on (von.route = bak.id) \n"
                            + "join geom on (bak.geom = geom.id) \n"
                            + "where (von.wert = 0 and abs(bis.wert - st_length(geo_field)) < 1) or \n"
                            + "(von.wert > 0 and abs(bis.wert - st_length(geo_field)) >= 1);";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    private boolean successful = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BasicRoutesCheckAction object.
     */
    public BasicRoutesCheckAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                BasicRoutesCheckAction.class,
                "BasicRoutesCheckAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                BasicRoutesCheckAction.class,
                "BasicRoutesCheckAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-zoom.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean startCheck(final boolean isExport) {
        final WaitingDialogThread<CheckResult> wdt = new WaitingDialogThread<CheckResult>(
                StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                true,
                NbBundle.getMessage(BasicRoutesCheckAction.class,
                    "BasicRoutesCheckAction.actionPerformed().dialog"),
                null,
                100) {

                @Override
                protected CheckResult doInBackground() throws Exception {
                    final CheckResult result = new CheckResult();
                    String user = AppBroker.getInstance().getOwner();

                    if (user.equalsIgnoreCase("Administratoren") || user.equalsIgnoreCase("lung_edit1")) {
                        user = null;
                    }

                    // start auto correction
                    final CidsServerSearch search = new MergeBakAe(user);
                    SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), search);

                    final CidsServerSearch nodesSearch = new RemoveDuplicatedNodesFromFgBak(user);
                    SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), nodesSearch);

                    // start checks
                    result.setShortService(analyseByQuery(
                            QUERY_FG_BAK_LENGTH,
                            "Prüfungen->Basisrouten->Gewässer_zu_kurz"));
                    result.setCrossedService(analyseByQuery(
                            QUERY_CROSSING_LINES,
                            "Prüfungen->Basisrouten->Gewässer_schneidend"));
                    result.setMissingCodeService(analyseByQuery(
                            QUERY_WITHOUT_BA_CD,
                            "Prüfungen->Basisrouten->Gewässercode_fehlt"));
                    result.setNotUniqueCodeService(analyseByQuery(
                            QUERY_DUPLICATED_BA_CD,
                            "Prüfungen->Basisrouten->Gewässercode_mehrfach"));
                    result.setMissingCodeService(analyseByQuery(
                            QUERY_AE,
                            "Prüfungen->Basisrouten->Aus-/Einleitung"));
                    result.setMissingCodeService(analyseByQuery(
                            QUERY_PREFIX,
                            "Prüfungen->Basisrouten->Präfix"));
                    result.setMissingCodeService(analyseByQuery(
                            QUERY_PREFIX_ME_WW_GR_OTHER,
                            "Prüfungen->Basisrouten->ww_gr"));

                    final ArrayList<ArrayList> countList = (ArrayList<ArrayList>)SessionManager.getProxy()
                                .customServerSearch(SessionManager.getSession().getUser(), new BakCount(user));

                    if ((countList != null) && !countList.isEmpty()) {
                        final ArrayList innerList = countList.get(0);

                        if ((innerList != null) && !innerList.isEmpty() && (innerList.get(0) instanceof Number)) {
                            result.setBakCount(((Number)innerList.get(0)).intValue());
                        }
                    }

                    String owner = "null";

                    if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("administratoren")) {
                        owner = "'" + AppBroker.getInstance().getOwner() + "'";
                    }
                    final ArrayList<ArrayList> problemCountList = (ArrayList<ArrayList>)SessionManager
                                .getProxy()
                                .customServerSearch(SessionManager.getSession().getUser(),
                                        new RouteProblemsCount(owner));

                    if ((problemCountList != null) && !problemCountList.isEmpty()) {
                        final ArrayList innerList = problemCountList.get(0);

                        if ((innerList != null) && !innerList.isEmpty() && (innerList.get(0) instanceof Number)) {
                            result.setErrorTree(((Number)innerList.get(0)).intValue());
                        }
                    }

                    if (result.getAusEinleitungService() != null) {
                        result.setAusEinleitungErrors(result.getAusEinleitungService().getFeatureCount(null));
                        successful = false;
                    }
                    if (result.getCrossedService() != null) {
                        result.setCrossedErrors(result.getCrossedService().getFeatureCount(null));
                        successful = false;
                    }
                    if (result.getMissingCodeService() != null) {
                        result.setMissingCodeErrors(result.getMissingCodeService().getFeatureCount(null));
                        successful = false;
                    }
                    if (result.getNotUniqueCodeService() != null) {
                        result.setNotUniqueCodeErrors(result.getNotUniqueCodeService().getFeatureCount(null));
                        successful = false;
                    }
                    if (result.getPrefixService() != null) {
                        result.setPrefixErrors(result.getPrefixService().getFeatureCount(null));
                        successful = false;
                    }
                    if (result.getShortService() != null) {
                        result.setShortErrors(result.getShortService().getFeatureCount(null));
                        successful = false;
                    }
                    if (result.getWwGrService() != null) {
                        result.setWwGrErrors(result.getWwGrService().getFeatureCount(null));
                        successful = false;
                    }

                    return result;
                }

                @Override
                protected void done() {
                    try {
                        final CheckResult result = get();

                        if (isExport) {
                            return;
                        }

                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            NbBundle.getMessage(
                                BasicRoutesCheckAction.class,
                                "BasicRoutesCheckAction.actionPerformed().result.text",
                                new Object[] {
                                    result.getBakCount(),
                                    result.getShortErrors(),
                                    result.getCrossedErrors(),
                                    result.getMissingCodeErrors(),
                                    result.getNotUniqueCodeErrors(),
                                    result.getAusEinleitungErrors(),
                                    result.getPrefixErrors(),
                                    result.getWwGrErrors(),
                                    result.getErrorTree()
                                }),
                            NbBundle.getMessage(
                                BasicRoutesCheckAction.class,
                                "BasicRoutesCheckAction.actionPerformed().result.title"),
                            JOptionPane.INFORMATION_MESSAGE);

                        if (result.getShortService() != null) {
                            showService(result.getShortService(), "Prüfungen->Basisrouten");
                        }
                        if (result.getCrossedService() != null) {
                            showService(result.getCrossedService(), "Prüfungen->Basisrouten");
                        }
                        if (result.getMissingCodeService() != null) {
                            showService(result.getMissingCodeService(), "Prüfungen->Basisrouten");
                        }
                        if (result.getNotUniqueCodeService() != null) {
                            showService(result.getNotUniqueCodeService(), "Prüfungen->Basisrouten");
                        }
                        if (result.getAusEinleitungService() != null) {
                            showService(result.getAusEinleitungService(), "Prüfungen->Basisrouten");
                        }
                        if (result.getPrefixService() != null) {
                            showService(result.getPrefixService(), "Prüfungen->Basisrouten");
                        }
                        if (result.getWwGrService() != null) {
                            showService(result.getWwGrService(), "Prüfungen->Basisrouten");
                        }
                    } catch (Exception e) {
                        LOG.error("Error while performing the route analyse.", e);
                        successful = false;
                    }
                }
            };

        wdt.start();

        return successful;
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

        private int shortErrors;
        private int crossedErrors;
        private int missingCodeErrors;
        private int notUniqueCodeErrors;
        private int ausEinleitungErrors;
        private int prefixErrors;
        private int wwGrErrors;
        private int errorTree;
        private int bakCount;
        private H2FeatureService shortService;
        private H2FeatureService crossedService;
        private H2FeatureService missingCodeService;
        private H2FeatureService notUniqueCodeService;
        private H2FeatureService ausEinleitungService;
        private H2FeatureService prefixService;
        private H2FeatureService wwGrService;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the shortErrors
         */
        public int getShortErrors() {
            return shortErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  shortErrors  the shortErrors to set
         */
        public void setShortErrors(final int shortErrors) {
            this.shortErrors = shortErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the crossedErrors
         */
        public int getCrossedErrors() {
            return crossedErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  crossedErrors  the crossedErrors to set
         */
        public void setCrossedErrors(final int crossedErrors) {
            this.crossedErrors = crossedErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the missingCodeErrors
         */
        public int getMissingCodeErrors() {
            return missingCodeErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  missingCodeErrors  the missingCodeErrors to set
         */
        public void setMissingCodeErrors(final int missingCodeErrors) {
            this.missingCodeErrors = missingCodeErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the notUniqueCodeErrors
         */
        public int getNotUniqueCodeErrors() {
            return notUniqueCodeErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  notUniqueCodeErrors  the notUniqueCodeErrors to set
         */
        public void setNotUniqueCodeErrors(final int notUniqueCodeErrors) {
            this.notUniqueCodeErrors = notUniqueCodeErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the ausEinleitungErrors
         */
        public int getAusEinleitungErrors() {
            return ausEinleitungErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  ausEinleitungErrors  the ausEinleitungErrors to set
         */
        public void setAusEinleitungErrors(final int ausEinleitungErrors) {
            this.ausEinleitungErrors = ausEinleitungErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the prefixErrors
         */
        public int getPrefixErrors() {
            return prefixErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  prefixErrors  the prefixErrors to set
         */
        public void setPrefixErrors(final int prefixErrors) {
            this.prefixErrors = prefixErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the wwGrErrors
         */
        public int getWwGrErrors() {
            return wwGrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  wwGrErrors  the wwGrErrors to set
         */
        public void setWwGrErrors(final int wwGrErrors) {
            this.wwGrErrors = wwGrErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the errorTree
         */
        public int getErrorTree() {
            return errorTree;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  errorTree  the errorTree to set
         */
        public void setErrorTree(final int errorTree) {
            this.errorTree = errorTree;
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
         * @return  the shortService
         */
        public H2FeatureService getShortService() {
            return shortService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  shortService  the shortService to set
         */
        public void setShortService(final H2FeatureService shortService) {
            this.shortService = shortService;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the crossedService
         */
        public H2FeatureService getCrossedService() {
            return crossedService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  crossedService  the crossedService to set
         */
        public void setCrossedService(final H2FeatureService crossedService) {
            this.crossedService = crossedService;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the missingCodeService
         */
        public H2FeatureService getMissingCodeService() {
            return missingCodeService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  missingCodeService  the missingCodeService to set
         */
        public void setMissingCodeService(final H2FeatureService missingCodeService) {
            this.missingCodeService = missingCodeService;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the notUniqueCodeService
         */
        public H2FeatureService getNotUniqueCodeService() {
            return notUniqueCodeService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  notUniqueCodeService  the notUniqueCodeService to set
         */
        public void setNotUniqueCodeService(final H2FeatureService notUniqueCodeService) {
            this.notUniqueCodeService = notUniqueCodeService;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the ausEinleitungService
         */
        public H2FeatureService getAusEinleitungService() {
            return ausEinleitungService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  ausEinleitungService  the ausEinleitungService to set
         */
        public void setAusEinleitungService(final H2FeatureService ausEinleitungService) {
            this.ausEinleitungService = ausEinleitungService;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the prefixService
         */
        public H2FeatureService getPrefixService() {
            return prefixService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  prefixService  the prefixService to set
         */
        public void setPrefixService(final H2FeatureService prefixService) {
            this.prefixService = prefixService;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the wwGrService
         */
        public H2FeatureService getWwGrService() {
            return wwGrService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  wwGrService  the wwGrService to set
         */
        public void setWwGrService(final H2FeatureService wwGrService) {
            this.wwGrService = wwGrService;
        }
    }
}
