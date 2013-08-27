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
package de.cismet.watergis.server;

import org.apache.log4j.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.WatergisApp;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class GeoLinkServer {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GeoLinkServer.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public static void startServer() {
        try {
            final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
            final Thread http = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1500);                             // Bugfix Try Deadlock
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Http Interface initialisieren"); // NOI18N
                                }

                                final Server server = new Server();
                                final Connector connector = new SelectChannelConnector();
                                connector.setPort(AppBroker.getInstance().getWatergisApp().getHttpInterfacePort());
                                server.setConnectors(new Connector[] { connector });

                                final Handler param = new AbstractHandler() {

                                        @Override
                                        public void handle(final String target,
                                                final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                final int dispatch) throws IOException, ServletException {
                                            final Request base_request = (request instanceof Request)
                                                ? (Request)request : HttpConnection.getCurrentConnection().getRequest();
                                            base_request.setHandled(true);
                                            response.setContentType("text/html");                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 // NOI18N
                                            response.setStatus(HttpServletResponse.SC_ACCEPTED);
                                            response.getWriter()
                                                    .println(
                                                        "<html><head><title>HTTP interface</title></head><body><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"80%\"><tr><td width=\"30%\" align=\"center\" valign=\"middle\"><img border=\"0\" src=\"http://www.cismet.de/images/cismetLogo250M.png\" ><br></td><td width=\"%\">&nbsp;</td><td width=\"50%\" align=\"left\" valign=\"middle\"><font face=\"Arial\" size=\"3\" color=\"#1c449c\">... and <b><font face=\"Arial\" size=\"3\" color=\"#1c449c\">http://</font></b> just works</font><br><br><br></td></tr></table></body></html>"); // NOI18N
                                        }
                                    };

                                final Handler hello = new AbstractHandler() {

                                        @Override
                                        public void handle(final String target,
                                                final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                final int dispatch) throws IOException, ServletException {
                                            try {
                                                if (request.getLocalAddr().equals(request.getRemoteAddr())) {
                                                    LOG.info("HttpInterface connected"); // NOI18N

                                                    if (target.equalsIgnoreCase("/gotoBoundingBox")) { // NOI18N

                                                        final String x1 = request.getParameter("x1"); // NOI18N
                                                        final String y1 = request.getParameter("y1"); // NOI18N
                                                        final String x2 = request.getParameter("x2"); // NOI18N
                                                        final String y2 = request.getParameter("y2"); // NOI18N

                                                        try {
                                                            final BoundingBox bb = new BoundingBox(
                                                                    new Double(x1),
                                                                    new Double(y1),
                                                                    new Double(x2),
                                                                    new Double(y2));
                                                            mappingComponent.gotoBoundingBoxWithHistory(bb);
                                                        } catch (Exception e) {
                                                            LOG.warn("gotoBoundingBox failed", e); // NOI18N
                                                        }
                                                    }

                                                    if (target.equalsIgnoreCase("/gotoScale")) { // NOI18N

                                                        final String x1 = request.getParameter("x1"); // NOI18N
                                                        final String y1 = request.getParameter("y1"); // NOI18N
                                                        final String scaleDenominator = request.getParameter(
                                                                "scaleDenominator");                  // NOI18N

                                                        try {
                                                            final BoundingBox bb = new BoundingBox(
                                                                    new Double(x1),
                                                                    new Double(y1),
                                                                    new Double(x1),
                                                                    new Double(y1));

                                                            mappingComponent.gotoBoundingBoxWithHistory(
                                                                mappingComponent.getScaledBoundingBox(
                                                                    new Double(scaleDenominator).doubleValue(),
                                                                    bb));
                                                        } catch (Exception e) {
                                                            LOG.warn("gotoBoundingBox failed", e); // NOI18N
                                                        }
                                                    }

                                                    if (target.equalsIgnoreCase("/centerOnPoint")) { // NOI18N

                                                        final String x1 = request.getParameter("x1"); // NOI18N
                                                        final String y1 = request.getParameter("y1"); // NOI18N

                                                        try {
                                                            final BoundingBox bb = new BoundingBox(
                                                                    new Double(x1),
                                                                    new Double(y1),
                                                                    new Double(x1),
                                                                    new Double(y1));
                                                            mappingComponent.gotoBoundingBoxWithHistory(bb);
                                                        } catch (Exception e) {
                                                            LOG.warn("centerOnPoint failed", e); // NOI18N
                                                        }
                                                    } else {
                                                        LOG.warn("Unknown target: " + target);   // NOI18N
                                                    }
                                                } else {
                                                    LOG.warn(
                                                        "Someone tries to access the http interface from an other computer. Access denied."); // NOI18N
                                                }
                                            } catch (Throwable t) {
                                                LOG.error("Error while handle http requests", t); // NOI18N
                                            }
                                        }
                                    };

                                final HandlerCollection handlers = new HandlerCollection();
                                handlers.setHandlers(new Handler[] { param, hello });
                                server.setHandler(handlers);

                                server.start();
                                server.join();
                            } catch (Throwable t) {
                                LOG.error("Error in the HttpInterface of cismap", t); // NOI18N
                            }
                        }
                    });
            http.start();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Initialise HTTP interface");                               // NOI18N
            }
        } catch (Throwable t) {
            LOG.fatal("Nothing at all", t);                                           // NOI18N
        }
    }
}
