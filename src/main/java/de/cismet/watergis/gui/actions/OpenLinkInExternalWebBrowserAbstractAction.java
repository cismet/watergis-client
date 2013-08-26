/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.gui.actions;

import javax.swing.AbstractAction;
import org.apache.log4j.Logger;

/**
 *
 * @author Gilles Baatz
 */
public abstract class OpenLinkInExternalWebBrowserAbstractAction extends AbstractAction{
    private static final Logger LOG = Logger.getLogger(OpenLinkInExternalWebBrowserAbstractAction.class);
    
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
