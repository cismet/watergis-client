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

import org.deegree.datatypes.Types;

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.custom.watergis.server.search.LawaConnected;
import de.cismet.cids.custom.watergis.server.search.LawaCount;
import de.cismet.cids.custom.watergis.server.search.LawaDirection;
import de.cismet.cids.custom.watergis.server.search.MergeLawa;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitDialog;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * Anmerkung: Bei komplexen Prüfungen werden immer alle Objekte geprüft. Issue 237
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GWKConnectionCheckAction extends AbstractCheckAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final MetaClass LAK_AE_MC = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_lak_ae");
    private static String QUERY_AE = null;
    private static final String CHECK_LAWA_ROUTEN_AUS_EINLEITUNG = "Prüfungen->LAWA-Routen->Aus-/Einleitung";
    private static final String CHECK_LAWA_ROUTEN_GERICHTETHEIT = "Prüfungen->LAWA-Routen->Gerichtetheit";
    private static final String CHECK_LAWA_ROUTEN_KONNEKTIVITAET = "Prüfungen->LAWA-Routen->Konnektivität";
    private static final String[] ALL_CHECKS = new String[] {
            CHECK_LAWA_ROUTEN_AUS_EINLEITUNG,
            CHECK_LAWA_ROUTEN_GERICHTETHEIT,
            CHECK_LAWA_ROUTEN_KONNEKTIVITAET
        };

    static {
        if (LAK_AE_MC != null) {
            QUERY_AE = "select " + LAK_AE_MC.getID() + ", ae." + LAK_AE_MC.getPrimaryKey()
                        + " from dlm25w.fg_lak_ae ae \n"
                        + "join dlm25w.fg_lak_linie linie on (ae.lak_st = linie.id) \n"
                        + "join dlm25w.fg_lak_punkt von on (linie.von = von.id)\n"
                        + "join dlm25w.fg_lak_punkt bis on (linie.bis = bis.id)\n"
                        + "join dlm25w.fg_lak lak on (von.route = lak.id) \n"
                        + "join geom on (lak.geom = geom.id) \n"
                        + "where (von.wert = 0 and abs(bis.wert - st_length(geo_field)) < 1) or \n"
                        + "(von.wert > 0 and abs(bis.wert - st_length(geo_field)) >= 1);";
        }
    }

    //~ Instance fields --------------------------------------------------------

    // dlm25w.merge_fg_bak_gwk()
    private boolean successful = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GWKConnectionCheckAction object.
     */
    public GWKConnectionCheckAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                GWKConnectionCheckAction.class,
                "GWKConnectionCheckAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                GWKConnectionCheckAction.class,
                "GWKConnectionCheckAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-zoom.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getProgressSteps() {
        return 7;
    }

    @Override
    public boolean startCheckInternal(final boolean isExport,
            final WaitDialog wd,
            final List<H2FeatureService> result) {
        final WaitingDialogThread<CheckResult> wdt = new WaitingDialogThread<CheckResult>(
                StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                true,
                NbBundle.getMessage(
                    GWKConnectionCheckAction.class,
                    "GWKConnectionCheckAction.actionPerformed().dialog"),
                null,
                100) {

                @Override
                protected CheckResult doInBackground() throws Exception {
                    final CheckResult result = new CheckResult();
                    String user = AppBroker.getInstance().getOwner();
                    this.wd.setMax(getProgressSteps());

                    if (user.equalsIgnoreCase("Administratoren") || user.equalsIgnoreCase("lung_edit1")) {
                        user = null;
                    }

                    removeServicesFromDb(ALL_CHECKS);

                    // start auto correction
                    final CidsServerSearch search = new MergeLawa(user);
                    SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), search);
                    increaseProgress(wd, 1);

                    final List<FeatureServiceAttribute> serviceAttributeDefinition =
                        new ArrayList<FeatureServiceAttribute>();

                    FeatureServiceAttribute serviceAttribute = new FeatureServiceAttribute(
                            "la_cd",
                            String.valueOf(Types.VARCHAR),
                            true);
                    serviceAttributeDefinition.add(serviceAttribute);
                    serviceAttribute = new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true);
                    serviceAttributeDefinition.add(serviceAttribute);

                    // start checks
                    result.setConnectionService(analyseByCustomSearch(
                            new LawaConnected(user),
                            CHECK_LAWA_ROUTEN_KONNEKTIVITAET,
                            serviceAttributeDefinition));
                    increaseProgress(wd, 1);

                    result.setDirectionService(analyseByCustomSearch(
                            new LawaDirection(user),
                            CHECK_LAWA_ROUTEN_GERICHTETHEIT,
                            serviceAttributeDefinition));
                    increaseProgress(wd, 1);

                    result.setLakAeService(analyseByQuery(LAK_AE_MC, QUERY_AE, CHECK_LAWA_ROUTEN_AUS_EINLEITUNG));
                    increaseProgress(wd, 1);

                    final ArrayList<ArrayList> lawaCountList = (ArrayList<ArrayList>)SessionManager.getProxy()
                                .customServerSearch(SessionManager.getSession().getUser(), new LawaCount(user));

                    if ((lawaCountList != null) && !lawaCountList.isEmpty()) {
                        final ArrayList innerList = lawaCountList.get(0);

                        if ((innerList != null) && !innerList.isEmpty() && (innerList.get(0) instanceof Number)) {
                            result.setLawaCount(((Number)innerList.get(0)).intValue());
                        }
                    }
                    increaseProgress(wd, 1);

                    if (result.getConnectionService() != null) {
                        final H2FeatureServiceFactory fac = (H2FeatureServiceFactory)result.getConnectionService()
                                    .getFeatureFactory();
                        final XBoundingBox boundingBox = new XBoundingBox(fac.getEnvelope());
                        final List<FeatureServiceFeature> features = fac.createFeatures(
                                null,
                                boundingBox,
                                null,
                                0,
                                0,
                                null);
                        final TreeSet<Object> laCdSet = new TreeSet<Object>();

                        for (final FeatureServiceFeature fsf : features) {
                            Object laCdCode = fsf.getProperty("la_cd");
                            if (laCdCode == null) {
                                laCdCode = "";
                            }
                            laCdSet.add(laCdCode);
                        }

                        result.setConnectionErrors(laCdSet.size());
                        successful = false;
                    }
                    increaseProgress(wd, 1);

                    if (result.getDirectionService() != null) {
                        final H2FeatureServiceFactory fac = (H2FeatureServiceFactory)result.getDirectionService()
                                    .getFeatureFactory();
                        final XBoundingBox boundingBox = new XBoundingBox(fac.getEnvelope());
                        final List<FeatureServiceFeature> features = fac.createFeatures(
                                null,
                                boundingBox,
                                null,
                                0,
                                0,
                                null);
                        final TreeSet<Object> laCdSet = new TreeSet<Object>();

                        for (final FeatureServiceFeature fsf : features) {
                            Object laCdCode = fsf.getProperty("la_cd");
                            if (laCdCode == null) {
                                laCdCode = "";
                            }
                            laCdSet.add(laCdCode);
                        }
                        result.setDirectionErrors(laCdSet.size());
                        successful = false;
                    }
                    increaseProgress(wd, 1);

                    if (result.getLakAeService() != null) {
                        result.setLakAeErrors(result.getLakAeService().getFeatureCount(null));
                        successful = false;
                    }

                    return result;
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
                                GWKConnectionCheckAction.class,
                                "GWKConnectionCheckAction.actionPerformed().result.text",
                                new Object[] {
                                    result.getLawaCount(),
                                    result.getConnectionErrors(),
                                    result.getDirectionErrors(),
                                    result.getLakAeErrors()
                                }),
                            NbBundle.getMessage(
                                GWKConnectionCheckAction.class,
                                "GWKConnectionCheckAction.actionPerformed().result.title"),
                            JOptionPane.INFORMATION_MESSAGE);

                        if (result.getLakAeService() != null) {
                            showService(result.getLakAeService(), "Prüfungen->LAWA-Routen");
                        }
                        if (result.getDirectionService() != null) {
                            showService(result.getDirectionService(), "Prüfungen->LAWA-Routen");
                        }
                        if (result.getConnectionService() != null) {
                            showService(result.getConnectionService(), "Prüfungen->LAWA-Routen");
                        }
                    } catch (Exception e) {
                        LOG.error("Error while performing the lawa connection analyse.", e);
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

        private int connectionErrors;
        private int directionErrors;
        private int lakAeErrors;
        private int lawaCount;
        private H2FeatureService directionService;
        private H2FeatureService connectionService;
        private H2FeatureService lakAeService;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the connectionErrors
         */
        public int getConnectionErrors() {
            return connectionErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  connectionErrors  the connectionErrors to set
         */
        public void setConnectionErrors(final int connectionErrors) {
            this.connectionErrors = connectionErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the directionErrors
         */
        public int getDirectionErrors() {
            return directionErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  directionErrors  the directionErrors to set
         */
        public void setDirectionErrors(final int directionErrors) {
            this.directionErrors = directionErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the lakAeErrors
         */
        public int getLakAeErrors() {
            return lakAeErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  lakAeErrors  the lakAeErrors to set
         */
        public void setLakAeErrors(final int lakAeErrors) {
            this.lakAeErrors = lakAeErrors;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the directionService
         */
        public H2FeatureService getDirectionService() {
            return directionService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  directionService  the directionService to set
         */
        public void setDirectionService(final H2FeatureService directionService) {
            this.directionService = directionService;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the connectionService
         */
        public H2FeatureService getConnectionService() {
            return connectionService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  connectionService  the connectionService to set
         */
        public void setConnectionService(final H2FeatureService connectionService) {
            this.connectionService = connectionService;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the lakAeService
         */
        public H2FeatureService getLakAeService() {
            return lakAeService;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  lakAeService  the lakAeService to set
         */
        public void setLakAeService(final H2FeatureService lakAeService) {
            this.lakAeService = lakAeService;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the lawaCount
         */
        public int getLawaCount() {
            return lawaCount;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  lawaCount  the lawaCount to set
         */
        public void setLawaCount(final int lawaCount) {
            this.lawaCount = lawaCount;
        }
    }
}
