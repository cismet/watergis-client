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

import de.cismet.cids.custom.watergis.server.search.BakWithIncompleteGbkCoverage;
import de.cismet.cids.custom.watergis.server.search.FgBakCount;
import de.cismet.cids.custom.watergis.server.search.GbkInIncorrectEzg;
import de.cismet.cids.custom.watergis.server.search.GwkLaCdFailure;
import de.cismet.cids.custom.watergis.server.search.MergeBaGbkDelta;
import de.cismet.cids.custom.watergis.server.search.MergeBakGbk;
import de.cismet.cids.custom.watergis.server.search.MergeBakGn;
import de.cismet.cids.custom.watergis.server.search.MergeBakObjart;
import de.cismet.cids.custom.watergis.server.search.MergeFgBakGwk;

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
 * Diese Prüfung bezieht sich auf Issue 240. Wird beim Export nicht ausgefuehrt und deshalb wird exp auchnicht beachtet.
 *
 * <p>Anmerkung: Bei komplexen Prüfungen werden immer alle Objekte geprüft.</p>
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LawaCheckAction extends AbstractCheckAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final MetaClass FG_BAK = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak");
    private static final MetaClass FG_BAK_GBK = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_gbk");
    private static final MetaClass FG_BA_GBK_DELTA = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_gbk_delta");
    private static final MetaClass FG_BAK_GWK = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_gwk");
    private static final MetaClass FG_BAK_GN1 = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_gn1");
    private static final MetaClass FG_BAK_GN2 = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_gn2");
    private static final MetaClass FG_BAK_GN3 = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_gn3");
    private static String QUERY_GBK_CATALOGUE;
    private static String QUERY_GWK_CATALOGUE;
    private static final String CHECKEZG_DELTA_GBK = "Prüfungen->LAWA-Schlüssel->fg_bak_gbk vs EZG";
    private static final String CHECK_GWK_DELTA_GWK = "Prüfungen->LAWA-Schlüssel->fg_bak_gwk mit Delta GWK/GBK";
    private static final String CHECK_GBK_CAT_GWK =
        "Prüfungen->LAWA-Schlüssel->fg_bak_gwk mit Katalogfehler k_gwk_lawa";
    private static final String CHECK_GBK_CAT_LAWA =
        "Prüfungen->LAWA-Schlüssel->fg_bak_gbk mit Katalogfehler k_gbk_lawa";
    private static final String CHECK_BAK_OHNE_GBK = "Prüfungen->LAWA-Schlüssel->fg_bak ohne fg_bak_gbk";
    private static final String[] ALL_CHECKS = new String[] {
            CHECKEZG_DELTA_GBK,
            CHECK_GWK_DELTA_GWK,
            CHECK_GBK_CAT_GWK,
            CHECK_GBK_CAT_LAWA,
            CHECK_BAK_OHNE_GBK
        };
    private static final int[] USED_CLASS_IDS = new int[] {
            ((FG_BAK_GBK != null) ? FG_BAK_GBK.getId() : -1),
            ((FG_BAK_GWK != null) ? FG_BAK_GWK.getId() : -1),
            ((FG_BA_GBK_DELTA != null) ? FG_BA_GBK_DELTA.getId() : -1),
            ((FG_BAK_GN1 != null) ? FG_BAK_GN1.getId() : -1),
            ((FG_BAK_GN2 != null) ? FG_BAK_GN2.getId() : -1),
            ((FG_BAK_GN3 != null) ? FG_BAK_GN3.getId() : -1)
        };

    static {
        if ((FG_BAK_GBK != null) && (FG_BAK_GWK != null) && (FG_BAK != null)) {
            final User user = SessionManager.getSession().getUser();

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_GBK_CATALOGUE = "select " + FG_BAK_GBK.getID() + ", t." + FG_BAK_GBK.getPrimaryKey()
                            + " from " + FG_BAK_GBK.getTableName() + " t\n"
                            + "join dlm25w.fg_bak_linie linie on (bak_st = linie.id) "
                            + "join dlm25w.fg_bak_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_bak bak on (von.route = bak.id) "
                            + "where not exists (select 1 from dlm25w.k_gbk_lawa where id = t.gbk_lawa)";
            } else {
                QUERY_GBK_CATALOGUE = "select " + FG_BAK_GBK.getID() + ", t." + FG_BAK_GBK.getPrimaryKey()
                            + " from " + FG_BAK_GBK.getTableName() + " t \n"
                            + "join dlm25w.fg_bak_linie linie on (bak_st = linie.id) "
                            + "join dlm25w.fg_bak_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_bak bak on (von.route = bak.id) "
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where not exists (select 1 from dlm25w.k_gbk_lawa where id = t.gbk_lawa) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }

            if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                QUERY_GWK_CATALOGUE = "select " + FG_BAK_GWK.getID() + ", t." + FG_BAK_GWK.getPrimaryKey()
                            + " from " + FG_BAK_GWK.getTableName() + " t\n"
                            + "join dlm25w.fg_bak_linie linie on (bak_st = linie.id) "
                            + "join dlm25w.fg_bak_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_bak bak on (von.route = bak.id) "
                            + "where not exists (select 1 from dlm25w.k_gwk_lawa where id = t.la_cd)";
            } else {
                QUERY_GWK_CATALOGUE = "select " + FG_BAK_GWK.getID() + ", t." + FG_BAK_GWK.getPrimaryKey()
                            + " from " + FG_BAK_GWK.getTableName() + " t \n"
                            + "join dlm25w.fg_bak_linie linie on (bak_st = linie.id) "
                            + "join dlm25w.fg_bak_punkt von on (linie.von = von.id) "
                            + "join dlm25w.fg_bak bak on (von.route = bak.id) "
                            + "join dlm25w.k_ww_gr gr on (bak.ww_gr = gr.id)\n"
                            + "where not exists (select 1 from dlm25w.k_gwk_lawa where id = t.la_cd) and gr.owner = '"
                            + user.getUserGroup().getName()
                            + "'";
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    // dlm25w.merge_fg_bak_gwk()
    private boolean successful = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LawaCheckAction object.
     */
    public LawaCheckAction() {
        this(false);
    }

    /**
     * Creates a new LawaCheckAction object.
     *
     * @param  isBackgroundCheck  DOCUMENT ME!
     */
    public LawaCheckAction(final boolean isBackgroundCheck) {
        super(isBackgroundCheck);

        final String tooltip = org.openide.util.NbBundle.getMessage(
                LawaCheckAction.class,
                "LawaCheckAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                LawaCheckAction.class,
                "LawaCheckAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-forkcms.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getProgressSteps() {
        return 10;
    }

    @Override
    public boolean startCheckInternal(final boolean isExport,
            final WaitDialog wd,
            final List<H2FeatureService> result) {
        final WaitingDialogThread<CheckResult> wdt = new WaitingDialogThread<CheckResult>(
                StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                true,
                NbBundle.getMessage(LawaCheckAction.class,
                    "LawaCheckAction.actionPerformed().dialog"),
                null,
                100) {

                @Override
                protected CheckResult doInBackground() throws Exception {
                    this.wd.setMax(getProgressSteps());
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
                                    LawaCheckAction.class,
                                    "LawaCheckAction.actionPerformed().result.text.withoutProblems",
                                    new Object[] {
                                        result.getBakCount(),
                                        result.getFgBakWithoutGbkErrors(),
                                        result.getGbkCatErrors(),
                                        result.getGwkCatErrors(),
                                        result.getGwkGbkErrors(),
                                        result.getGbkInIncorrectEzgErrors(),
                                        0
                                    }),
                                NbBundle.getMessage(
                                    LawaCheckAction.class,
                                    "LawaCheckAction.actionPerformed().result.title"),
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                NbBundle.getMessage(
                                    LawaCheckAction.class,
                                    "LawaCheckAction.actionPerformed().result.text",
                                    new Object[] {
                                        result.getBakCount(),
                                        result.getFgBakWithoutGbkErrors(),
                                        result.getGbkCatErrors(),
                                        result.getGwkCatErrors(),
                                        result.getGwkGbkErrors(),
                                        result.getGbkInIncorrectEzgErrors(),
                                        result.getProblemTreeObjectCount().getCount(),
                                        result.getProblemTreeObjectCount().getClasses()
                                    }),
                                NbBundle.getMessage(
                                    LawaCheckAction.class,
                                    "LawaCheckAction.actionPerformed().result.title"),
                                JOptionPane.INFORMATION_MESSAGE);
                        }

                        if (result.getGbkInIncorrectEzg() != null) {
                            showService(result.getGbkInIncorrectEzg(),
                                "Prüfungen->LAWA-Schlüssel");
                        }
                        if (result.getGwkGbk() != null) {
                            showService(result.getGwkGbk(),
                                "Prüfungen->LAWA-Schlüssel");
                        }
                        if (result.getGwkCat() != null) {
                            showService(result.getGwkCat(),
                                "Prüfungen->LAWA-Schlüssel");
                        }
                        if (result.getGbkCat() != null) {
                            showService(result.getGbkCat(),
                                "Prüfungen->LAWA-Schlüssel");
                        }
                        if (result.getFgBakWithoutGbk() != null) {
                            showService(result.getFgBakWithoutGbk(),
                                "Prüfungen->LAWA-Schlüssel");
                        }
                        refreshTree();
                        refreshMap();
                    } catch (Exception e) {
                        LOG.error("Error while performing the lawa analyse.", e);
                        successful = false;
                    }
                }
            };

        wdt.start();

        return successful;
    }

    @Override
    protected CheckResult check(final boolean isExport, final WaitDialog wd) throws Exception {
        final CheckResult result = new CheckResult();
        String user = AppBroker.getInstance().getOwner();

        if (user.equalsIgnoreCase("Administratoren")) {
            user = null;
        }

        removeServicesFromDb(ALL_CHECKS);

        final ArrayList<ArrayList> countList = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(),
                            new FgBakCount(user, null, null));

        if ((countList != null) && !countList.isEmpty()) {
            final ArrayList innerList = countList.get(0);

            if ((innerList != null) && !innerList.isEmpty() && (innerList.get(0) instanceof Number)) {
                result.setBakCount(((Number)innerList.get(0)).intValue());
            }
        }
        increaseProgress(wd, 1);

        // start auto correction
        final CidsServerSearch mergeGwk = new MergeFgBakGwk(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeGwk);
        increaseProgress(wd, 1);
        final CidsServerSearch mergeGbk = new MergeBakGbk(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeGbk);
        increaseProgress(wd, 1);
        final CidsServerSearch mergeGn = new MergeBakGn(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeGn);
        increaseProgress(wd, 1);
//                    final CidsServerSearch mergeObjart = new MergeBakObjart(user);
//                    SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeObjart);
//                    increaseProgress(wd, 1);
        final CidsServerSearch mergeGbkDelta = new MergeBaGbkDelta(user);
        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), mergeGbkDelta);
        increaseProgress(wd, 1);

        // start checks
        final List<FeatureServiceAttribute> gbkServiceAttributeDefinition = new ArrayList<FeatureServiceAttribute>();

        FeatureServiceAttribute serviceAttribute = new FeatureServiceAttribute(
                "id",
                String.valueOf(Types.INTEGER),
                true);
        gbkServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true);
        gbkServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_cd", String.valueOf(Types.VARCHAR), true);
        gbkServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("bak_st_von", String.valueOf(Types.DOUBLE), true);
        gbkServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("bak_st_bis", String.valueOf(Types.DOUBLE), true);
        gbkServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("gbk_lawa", String.valueOf(Types.VARCHAR), true);
        gbkServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("laenge", String.valueOf(Types.DOUBLE), true);
        gbkServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_date", String.valueOf(Types.TIMESTAMP), true);
        gbkServiceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_user", String.valueOf(Types.VARCHAR), true);
        gbkServiceAttributeDefinition.add(serviceAttribute);

        result.setFgBakWithoutGbk(analyseByCustomSearch(
                new BakWithIncompleteGbkCoverage(user, null, false),
                CHECK_BAK_OHNE_GBK,
                gbkServiceAttributeDefinition));
        increaseProgress(wd, 1);

        result.setGbkCat(analyseByQuery(FG_BAK_GBK,
                QUERY_GBK_CATALOGUE, CHECK_GBK_CAT_LAWA));
        increaseProgress(wd, 1);

        result.setGwkCat(analyseByQuery(FG_BAK_GWK,
                QUERY_GWK_CATALOGUE, CHECK_GBK_CAT_GWK));
        increaseProgress(wd, 1);

        final List<FeatureServiceAttribute> serviceAttributeDefinition = new ArrayList<FeatureServiceAttribute>();

        serviceAttribute = new FeatureServiceAttribute("id", String.valueOf(Types.INTEGER), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_cd", String.valueOf(Types.VARCHAR), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("bak_st_von", String.valueOf(Types.DOUBLE), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("bak_st_bis", String.valueOf(Types.DOUBLE), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("la_cd", String.valueOf(Types.NUMERIC), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("gbk_lawa", String.valueOf(Types.NUMERIC), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("laenge", String.valueOf(Types.NUMERIC), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_date", String.valueOf(Types.TIMESTAMP), true);
        serviceAttributeDefinition.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_user", String.valueOf(Types.VARCHAR), true);
        serviceAttributeDefinition.add(serviceAttribute);

        result.setGwkGbk(analyseByCustomSearch(
                new GwkLaCdFailure(user, null, false),
                CHECK_GWK_DELTA_GWK,
                serviceAttributeDefinition));
        increaseProgress(wd, 1);

        final List<FeatureServiceAttribute> serviceAttributeDefinitionFgBa = new ArrayList<FeatureServiceAttribute>();

        serviceAttribute = new FeatureServiceAttribute("id", String.valueOf(Types.INTEGER), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ba_cd", String.valueOf(Types.VARCHAR), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("bak_st_von", String.valueOf(Types.DOUBLE), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("bak_st_bis", String.valueOf(Types.DOUBLE), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("ezg", String.valueOf(Types.NUMERIC), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("gbk_lawa", String.valueOf(Types.VARCHAR), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("laenge", String.valueOf(Types.DOUBLE), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_date", String.valueOf(Types.TIMESTAMP), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);
        serviceAttribute = new FeatureServiceAttribute("fis_g_user", String.valueOf(Types.VARCHAR), true);
        serviceAttributeDefinitionFgBa.add(serviceAttribute);

        result.setGbkInIncorrectEzg(analyseByCustomSearch(
                new GbkInIncorrectEzg(user, null, false),
                CHECKEZG_DELTA_GBK,
                serviceAttributeDefinitionFgBa));
        increaseProgress(wd, 1);

        result.setProblemTreeObjectCount(getErrorObjectsFromTree(user, null, USED_CLASS_IDS, isExport));

        if (result.getFgBakWithoutGbk() != null) {
            result.setFgBakWithoutGbkErrors(result.getFgBakWithoutGbk().getFeatureCount(null));
            successful = false;
        }

        if (result.getGbkCat() != null) {
            result.setGbkCatErrors(result.getGbkCat().getFeatureCount(null));
            successful = false;
        }

        if (result.getGwkCat() != null) {
            result.setGwkCatErrors(result.getGwkCat().getFeatureCount(null));
            successful = false;
        }

        if (result.getGwkGbk() != null) {
            result.setGwkGbkErrors(result.getGwkGbk().getFeatureCount(null));
            successful = false;
        }

        if (result.getGbkInIncorrectEzg() != null) {
            result.setGbkInIncorrectEzgErrors(result.getGbkInIncorrectEzg().getFeatureCount(null));
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
                "FG_BAK_WITHOUT_GBK",
                "GBK_CAT",
                "GWK_CAT",
                "GWK_GBK",
                "GBK_IN_INCORRECT_EZG"
            };

        //~ Instance fields ----------------------------------------------------

        private int fgBakWithoutGbkErrors;
        private int gbkCatErrors;
        private int gwkCatErrors;
        private int gwkGbkErrors;
        private int bakCount;
        private int gbkInIncorrectEzgErrors;
        private ProblemCountAndClasses problemTreeObjectCount;
        private H2FeatureService fgBakWithoutGbk;
        private H2FeatureService gbkCat;
        private H2FeatureService gwkCat;
        private H2FeatureService gwkGbk;
        private H2FeatureService gbkInIncorrectEzg;

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
         * @return  the fgBakWithoutGbkErrors
         */
        public int getFgBakWithoutGbkErrors() {
            return fgBakWithoutGbkErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  fgBakWithoutGbkErrors  the fgBakWithoutGbkErrors to set
         */
        public void setFgBakWithoutGbkErrors(final int fgBakWithoutGbkErrors) {
            this.fgBakWithoutGbkErrors = fgBakWithoutGbkErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the fgBakWithoutGbk
         */
        public H2FeatureService getFgBakWithoutGbk() {
            return fgBakWithoutGbk;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  fgBakWithoutGbk  the fgBakWithoutGbk to set
         */
        public void setFgBakWithoutGbk(final H2FeatureService fgBakWithoutGbk) {
            this.fgBakWithoutGbk = fgBakWithoutGbk;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gbkCatErrors
         */
        public int getGbkCatErrors() {
            return gbkCatErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gbkCatErrors  the gbkCatErrors to set
         */
        public void setGbkCatErrors(final int gbkCatErrors) {
            this.gbkCatErrors = gbkCatErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gwkCatErrors
         */
        public int getGwkCatErrors() {
            return gwkCatErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gwkCatErrors  the gwkCatErrors to set
         */
        public void setGwkCatErrors(final int gwkCatErrors) {
            this.gwkCatErrors = gwkCatErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gbkCat
         */
        public H2FeatureService getGbkCat() {
            return gbkCat;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gbkCat  the gbkCat to set
         */
        public void setGbkCat(final H2FeatureService gbkCat) {
            this.gbkCat = gbkCat;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gwkCat
         */
        public H2FeatureService getGwkCat() {
            return gwkCat;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gwkCat  the gwkCat to set
         */
        public void setGwkCat(final H2FeatureService gwkCat) {
            this.gwkCat = gwkCat;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gwkGbkErrors
         */
        public int getGwkGbkErrors() {
            return gwkGbkErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gwkGbkErrors  the gwkGbkErrors to set
         */
        public void setGwkGbkErrors(final int gwkGbkErrors) {
            this.gwkGbkErrors = gwkGbkErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gwkGbk
         */
        public H2FeatureService getGwkGbk() {
            return gwkGbk;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gwkGbk  the gwkGbk to set
         */
        public void setGwkGbk(final H2FeatureService gwkGbk) {
            this.gwkGbk = gwkGbk;
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
         * @return  the gbkInIncorrectEzgErrors
         */
        public int getGbkInIncorrectEzgErrors() {
            return gbkInIncorrectEzgErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gbkInIncorrectEzgErrors  the gbkInIncorrectEzgErrors to set
         */
        public void setGbkInIncorrectEzgErrors(final int gbkInIncorrectEzgErrors) {
            this.gbkInIncorrectEzgErrors = gbkInIncorrectEzgErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the gbkInIncorrectEzg
         */
        public H2FeatureService getGbkInIncorrectEzg() {
            return gbkInIncorrectEzg;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  gbkInIncorrectEzg  the gbkInIncorrectEzg to set
         */
        public void setGbkInIncorrectEzg(final H2FeatureService gbkInIncorrectEzg) {
            this.gbkInIncorrectEzg = gbkInIncorrectEzg;
        }

        @Override
        public String[] getCheckNames() {
            return CHECK_NAMES;
        }

        @Override
        public int getErrorsPerCheck(final String checkName) {
            if (checkName.equals(CHECK_NAMES[0])) {
                return fgBakWithoutGbkErrors;
            } else if (checkName.equals(CHECK_NAMES[1])) {
                return gbkCatErrors;
            } else if (checkName.equals(CHECK_NAMES[2])) {
                return gwkCatErrors;
            } else if (checkName.equals(CHECK_NAMES[3])) {
                return gwkGbkErrors;
            } else if (checkName.equals(CHECK_NAMES[4])) {
                return gbkInIncorrectEzgErrors;
            } else {
                return 0;
            }
        }

        @Override
        public H2FeatureService getErrorTablePerCheck(final String checkName) {
            if (checkName.equals(CHECK_NAMES[0])) {
                return fgBakWithoutGbk;
            } else if (checkName.equals(CHECK_NAMES[1])) {
                return gbkCat;
            } else if (checkName.equals(CHECK_NAMES[2])) {
                return gwkCat;
            } else if (checkName.equals(CHECK_NAMES[3])) {
                return gwkGbk;
            } else if (checkName.equals(CHECK_NAMES[4])) {
                return gbkInIncorrectEzg;
            } else {
                return null;
            }
        }
    }
}
