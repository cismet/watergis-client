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

import Sirius.server.middleware.types.MetaClass;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

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
public class GWKConnectionCheckAction extends AbstractCheckAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final MetaClass GWK_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak_gwk");
    private static String QUERY = null;

    static {
        if (GWK_MC != null) {
            QUERY = "select " + GWK_MC.getID() + ", " + GWK_MC.getPrimaryKey()
                        + "  from dlm25w.fg_bak_gwk where gwk in (select gwk.gwk from dlm25w.fg_bak_gwk gwk join "
                        + "dlm25w.fg_bak_linie linie on (gwk.bak_st = linie.id) join dlm25w.fg_bak_punkt"
                        + " von on (linie.von = von.id) join dlm25w.fg_bak_punkt bis on (linie.bis = bis.id)\n"
                        + "join dlm25w.fg_bak bak on (von.route = bak.id) join geom on (bak.geom = geom.id) \n"
                        + "group by gwk.gwk\n"
                        + "having st_geometrytype(st_union(st_line_substring(\n"
                        + "geo_field, \n"
                        + "von.wert / st_length(geo_field), \n"
                        + "case when (bis.wert / st_length(geo_field)) <= 1.0 then (bis.wert / st_length(geo_field)) else 1.0 end\n"
                        + ") )) <> 'ST_LineString'\n"
                        + "order by gwk)";
        }
    }

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
    public void actionPerformed(final ActionEvent e) {
        final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(StaticSwingTools
                        .getParentFrame(AppBroker.getInstance().getWatergisApp()),
                true,
                NbBundle.getMessage(
                    GWKConnectionCheckAction.class,
                    "GWKConnectionCheckAction.actionPerformed().dialog"),
                null,
                100) {

                @Override
                protected H2FeatureService doInBackground() throws Exception {
                    return analyseByQuery(QUERY, "Nicht_geschlossene_LAWA_Routen");
                }

                @Override
                protected void done() {
                    try {
                        final H2FeatureService service = get();

                        if (service == null) {
                            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                NbBundle.getMessage(
                                    GWKConnectionCheckAction.class,
                                    "GWKConnectionCheckAction.actionPerformed().noResult"),
                                NbBundle.getMessage(
                                    GWKConnectionCheckAction.class,
                                    "GWKConnectionCheckAction.actionPerformed().noResult.title"),
                                JOptionPane.ERROR_MESSAGE);
                        } else {
                            showService(service);
                        }
                    } catch (Exception e) {
                        LOG.error("Error while performing the lawa connection analyse.", e);
                    }
                }
            };

        wdt.start();
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
