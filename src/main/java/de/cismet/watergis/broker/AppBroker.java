/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AppBroker.java
 *
 * Created on 20. April 2007, 13:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.watergis.broker;

import Sirius.navigator.connection.ConnectionSession;

import net.infonode.docking.RootWindow;

import org.jdom.Element;

import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.tools.configuration.Configurable;

/**
 * DOCUMENT ME!
 *
 * @author   Puhl
 * @version  $Revision$, $Date$
 */
public class AppBroker implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppBroker.class);

    //~ Instance fields --------------------------------------------------------

    private transient ConnectionSession session;
    private MappingComponent mappingComponent;
    private boolean loggedIn;
    private String domain;
    private String callserverUrl;
    private String connectionClass;
    private RootWindow rootWindow;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AppBroker object.
     */
    private AppBroker() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ConnectionSession getSession() {
        return session;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  session  DOCUMENT ME!
     */
    public void setSession(final ConnectionSession session) {
        this.session = session;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static AppBroker getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aMappingComponent  DOCUMENT ME!
     */
    public void setMappingComponent(final MappingComponent aMappingComponent) {
        mappingComponent = aMappingComponent;
    }

    @Override
    public Element getConfiguration() {
        return null;
    }

    @Override
    public void masterConfigure(final Element parent) {
    }

    @Override
    public void configure(final Element parent) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  loggedIn  DOCUMENT ME!
     */
    public void setLoggedIn(final boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getConnectionClass() {
        return connectionClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  connectionClass  DOCUMENT ME!
     */
    public void setConnectionClass(final String connectionClass) {
        this.connectionClass = connectionClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCallserverUrl() {
        return callserverUrl;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  callserverUrl  DOCUMENT ME!
     */
    public void setCallserverUrl(final String callserverUrl) {
        this.callserverUrl = callserverUrl;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomain() {
        return domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  domain  DOCUMENT ME!
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rootWindow  DOCUMENT ME!
     */
    public void setRootWindow(final RootWindow rootWindow) {
        this.rootWindow = rootWindow;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RootWindow getRootWindow() {
        return rootWindow;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        static final AppBroker INSTANCE = new AppBroker();
    }
}
