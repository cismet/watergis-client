/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.gui.actions;

import org.apache.log4j.Logger;

import javax.swing.AbstractAction;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public abstract class OpenLinkInExternalWebBrowserAbstractAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(OpenLinkInExternalWebBrowserAbstractAction.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  url  DOCUMENT ME!
     */
    void openUrlInExternalBrowser(final String url) {
        try {
            de.cismet.tools.BrowserLauncher.openURL(url);
        } catch (Exception e) {
            LOG.warn("Fehler beim \u00D6ffnen von:" + url + "\\nNeuer Versuch", e);
            // Nochmal zur Sicherheit mit dem BrowserLauncher probieren
            try {
                de.cismet.tools.BrowserLauncher.openURL(url);
            } catch (Exception e2) {
                LOG.warn("Auch das 2te Mal ging schief.Fehler beim \u00D6ffnen von:" + "\\nLetzter Versuch", e2);
                try {
                    de.cismet.tools.BrowserLauncher.openURL("file://" + url);
                } catch (Exception e3) {
                    LOG.error("Auch das 3te Mal ging schief.Fehler beim \u00D6ffnen von:" + url, e3);
                }
            }
        }
    }
}
