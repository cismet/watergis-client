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
package de.cismet.watergis.gui.actions.reports;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.math.BigDecimal;

import java.net.URL;

import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.watergis.server.search.WsgStatSearch;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.WsgForstDialog;

import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WsgWaldAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneGGewaesserReportAction.class);
    private static final ConnectionContext CC = ConnectionContext.create(
            AbstractConnectionContext.Category.ACTION,
            "WsgAction");

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DissolveGeoprocessingAction object.
     */
    public WsgWaldAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-spiderweb.png");
        String text = "Forstnutzung";
        String tooltiptext = "Forstnutzung";
        String mnemonic = "O";

        try {
            text = NbBundle.getMessage(WsgWaldAction.class,
                    "WsgWaldAction.text");
            tooltiptext = NbBundle.getMessage(WsgWaldAction.class,
                    "WsgWaldAction.toolTipText");
            mnemonic = NbBundle.getMessage(WsgWaldAction.class,
                    "WsgWaldAction.mnemonic");
        } catch (MissingResourceException e) {
            LOG.error("Couldn't find resources. Using fallback settings.", e);
        }

        if (icon != null) {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(icon));
        }

        putValue(SHORT_DESCRIPTION, tooltiptext);
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        putValue(NAME, text);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final WaitingDialogThread<WsgForstDialog> wdt = new WaitingDialogThread<WsgForstDialog>(
                StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                true,
                "erstelle Auswertung",
                null,
                100,
                true) {

                @Override
                protected WsgForstDialog doInBackground() throws Exception {
                    final CidsServerSearch search = new WsgStatSearch(false);
                    final User user = SessionManager.getSession().getUser();
                    final ArrayList<Object> attributes = (ArrayList<Object>)SessionManager.getProxy()
                                .customServerSearch(user, search, CC);

                    final int wsgCount = ((Long)attributes.get(0)).intValue();
                    final double wsgTotal = ((BigDecimal)attributes.get(1)).doubleValue();
                    final double holzTotal = (Double)attributes.get(2);
                    final double nichtHolzTotal = (Double)attributes.get(3);
                    final double nichtEingerichtetTotal = (Double)attributes.get(4);
                    final WsgForstDialog dialog = new WsgForstDialog(AppBroker.getInstance().getWatergisApp(),
                            true,
                            wsgCount,
                            wsgTotal,
                            holzTotal,
                            nichtHolzTotal,
                            nichtEingerichtetTotal);

                    return dialog;
                }

                @Override
                protected void done() {
                    try {
                        final WsgForstDialog dialog = get();

                        if (dialog != null) {
                            StaticSwingTools.centerWindowOnScreen(dialog);

                            final ActiveLayerModel model = (ActiveLayerModel)AppBroker.getInstance()
                                        .getMappingComponent()
                                        .getMappingModel();
                            final MetaClass wsgMc = ClassCacheMultiple.getMetaClass(
                                    AppBroker.DOMAIN_NAME,
                                    "dlm25w.wr_sg_wsg_fn");
                            final CidsLayer layer = new CidsLayer(wsgMc);

                            model.removeLayer(layer);
                            model.addLayer(layer);
                        }
                    } catch (Exception e) {
                        final ErrorInfo info = new ErrorInfo(
                                "Fehler bei der Auswertung",
                                e.getMessage(),
                                null,
                                null,
                                e,
                                Level.SEVERE,
                                null);
                        JXErrorPane.showDialog(StaticSwingTools.getParentFrame(
                                AppBroker.getInstance().getWatergisApp()),
                            info);
                    }
                }
            };

        wdt.start();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
