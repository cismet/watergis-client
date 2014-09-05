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

import Sirius.navigator.connection.ConnectionInfo;
import Sirius.navigator.connection.ConnectionSession;
import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.event.CatalogueActivationListener;
import Sirius.navigator.event.CatalogueSelectionListener;
import Sirius.navigator.resource.PropertyManager;
import Sirius.navigator.search.dynamic.SearchDialog;
import Sirius.navigator.types.treenode.RootTreeNode;
import Sirius.navigator.ui.ComponentRegistry;
import Sirius.navigator.ui.DescriptionPane;
import Sirius.navigator.ui.DescriptionPaneFS;
import Sirius.navigator.ui.LayoutedContainer;
import Sirius.navigator.ui.MutableMenuBar;
import Sirius.navigator.ui.MutablePopupMenu;
import Sirius.navigator.ui.MutableToolBar;
import Sirius.navigator.ui.attributes.AttributeViewer;
import Sirius.navigator.ui.attributes.editor.AttributeEditor;
import Sirius.navigator.ui.tree.MetaCatalogueTree;
import Sirius.navigator.ui.tree.SearchResultsTree;

import Sirius.server.middleware.types.Node;

import net.infonode.docking.RootWindow;
import net.infonode.gui.componentpainter.GradientComponentPainter;

import org.jdom.Element;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuListener;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MessenGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RubberBandZoomListener;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.tools.gui.DefaultPopupMenuListener;

import de.cismet.watergis.broker.listener.SelectionModeChangedEvent;
import de.cismet.watergis.broker.listener.SelectionModeListener;

import de.cismet.watergis.gui.WatergisApp;
import de.cismet.watergis.gui.actions.InfoWindowAction;
import de.cismet.watergis.gui.recently_opened_files.RecentlyOpenedFilesList;

import de.cismet.watergis.utils.BookmarkManager;

/**
 * DOCUMENT ME!
 *
 * @author   Puhl
 * @version  $Revision$, $Date$
 */
public class AppBroker implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AppBroker.class);
    // COLORS
    private static final Color blue = new Color(124, 160, 221);
    public static final Color DEFAULT_MODE_COLOR = blue;
    public static final String MEASURE_MODE = "MEASURE_MODE";
    private static ConfigurationManager configManager;
    public static final String DOMAIN_NAME = "DLM25W";

    //~ Instance fields --------------------------------------------------------

    private RecentlyOpenedFilesList recentlyOpenedFilesList;

    private transient ConnectionSession session;
    private MappingComponent mappingComponent;
    private boolean loggedIn;
    private String domain;
    private String callserverUrl;
    private String connectionClass;
    private RootWindow rootWindow;
    private BookmarkManager bookmarkManager;
    private EnumMap<ComponentName, Component> components = new EnumMap<ComponentName, Component>(ComponentName.class);
    private HashMap<String, Action> mapModeSelectionActions = new HashMap<String, Action>();
    private MessenGeometryListener measureListener;
    private List<SelectionModeListener> selecionModeListener = new ArrayList<SelectionModeListener>();
    private ComponentRegistry componentRegistry;
    private ConnectionInfo connectionInfo;
    private InfoWindowAction infoWindowAction;

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
        mappingComponent.setReadOnly(false);
        setMeasureListener(new MessenGeometryListener(mappingComponent));
        mappingComponent.addInputListener(MEASURE_MODE, getMeasureListener());
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
        this.connectionClass = this.connectionClass.replace('"', ' ');
        this.connectionClass = this.connectionClass.trim();
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
        this.callserverUrl = this.callserverUrl.replace('"', ' ');
        this.callserverUrl = this.callserverUrl.trim();
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
        this.domain = this.domain.replace('"', ' ');
        this.domain = this.domain.trim();
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

    /**
     * DOCUMENT ME!
     *
     * @param  color  DOCUMENT ME!
     */
    public void setTitleBarComponentpainter(final Color color) {
        getRootWindow().getRootWindowProperties()
                .getViewProperties()
                .getViewTitleBarProperties()
                .getNormalProperties()
                .getShapedPanelProperties()
                .setComponentPainter(new GradientComponentPainter(
                        color,
                        new Color(236, 233, 216),
                        color,
                        new Color(236, 233, 216)));
    }
    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Component getComponent(final ComponentName name) {
        return components.get(name);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WatergisApp getWatergisApp() {
        return (WatergisApp)components.get(ComponentName.MAIN);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name       DOCUMENT ME!
     * @param  component  DOCUMENT ME!
     */
    public void addComponent(final ComponentName name, final Component component) {
        components.put(name, component);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RecentlyOpenedFilesList getRecentlyOpenedFilesList() {
        return recentlyOpenedFilesList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  recentlyOpenedFilesList  DOCUMENT ME!
     */
    public void setRecentlyOpenedFilesList(final RecentlyOpenedFilesList recentlyOpenedFilesList) {
        this.recentlyOpenedFilesList = recentlyOpenedFilesList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isActionsAlwaysEnabled() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name    DOCUMENT ME!
     * @param  action  DOCUMENT ME!
     */
    public void addMapMode(final String name, final Action action) {
        mapModeSelectionActions.put(name, action);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    public void switchMapMode(final String mode) {
        final Action action = mapModeSelectionActions.get(mode);
        if (action != null) {
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, mode));
        } else {
            LOG.warn("Can not switch to mode " + mode + ". It does not exist.");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  factor  DOCUMENT ME!
     */
    public void simpleZoom(final float factor) {
        final RubberBandZoomListener r = (RubberBandZoomListener)AppBroker.getInstance().getMappingComponent()
                    .getInputListener(MappingComponent.ZOOM);
        r.zoom(factor, mappingComponent.getCamera(), mappingComponent.getAnimationDuration(), 500);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConfigurationManager getConfigManager() {
        return configManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  configManager  DOCUMENT ME!
     */
    public static void setConfigManager(final ConfigurationManager configManager) {
        AppBroker.configManager = configManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public BookmarkManager getBookmarkManager() {
        return bookmarkManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bookmarkManager  DOCUMENT ME!
     */
    public void setBookmarkManager(final BookmarkManager bookmarkManager) {
        this.bookmarkManager = bookmarkManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the measureListener
     */
    public MessenGeometryListener getMeasureListener() {
        return measureListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  measureListener  the measureListener to set
     */
    public void setMeasureListener(final MessenGeometryListener measureListener) {
        this.measureListener = measureListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addSelecionModeListener(final SelectionModeListener listener) {
        selecionModeListener.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removeSelecionModeListener(final SelectionModeListener listener) {
        selecionModeListener.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  source   DOCUMENT ME!
     * @param  oldMode  DOCUMENT ME!
     * @param  newMode  DOCUMENT ME!
     */
    public void fireSelectionModeChanged(final Object source, final String oldMode, final String newMode) {
        final SelectionModeChangedEvent e = new SelectionModeChangedEvent(source, oldMode, newMode);
        for (final SelectionModeListener tmp : selecionModeListener) {
            tmp.selectionModeChanged(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   frame  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void initComponentRegistry(final JFrame frame) throws Exception {
        PropertyManager.getManager().setEditable(true);

        final SearchResultsTree searchResultsTree = new SearchResultsTree();
        final MutableToolBar toolBar = new MutableToolBar();
        final MutableMenuBar menuBar = new MutableMenuBar();
        final LayoutedContainer container = new LayoutedContainer(toolBar, menuBar, true);
        final AttributeViewer attributeViewer = new AttributeViewer();
        final AttributeEditor attributeEditor = new AttributeEditor();
        final SearchDialog searchDialog = null;

        final DescriptionPane descriptionPane = new DescriptionPaneFS();
        final MutablePopupMenu popupMenu = new MutablePopupMenu();
        for (final PopupMenuListener l : popupMenu.getPopupMenuListeners()) {
            popupMenu.removePopupMenuListener(l);
        }

        final Collection<Component> toRemoveComponents = new ArrayList<Component>();
        for (final Component component : popupMenu.getComponents()) {
            if ((component instanceof JSeparator)
                        || !((component instanceof JMenuItem)
                            && ((((JMenuItem)component).getAccelerator() != null)
                                && ((JMenuItem)component).getAccelerator().equals(
                                    KeyStroke.getKeyStroke("F5"))))) {
                toRemoveComponents.add(component);
            }
        }
        for (final Component toRemoveComponent : toRemoveComponents) {
            popupMenu.remove(toRemoveComponent);
        }

        final DefaultPopupMenuListener cataloguePopupMenuListener = new DefaultPopupMenuListener(popupMenu);
        final Node[] roots = SessionManager.getProxy().getRoots();
        final RootTreeNode rootTreeNode = new RootTreeNode(roots);
        while (roots.length != rootTreeNode.getChildCount()) {
            Thread.sleep(100);
        }
        final MetaCatalogueTree metaCatalogueTree = new MetaCatalogueTree(
                rootTreeNode,
                PropertyManager.getManager().isEditable(),
                true,
                PropertyManager.getManager().getMaxConnections());
        final CatalogueSelectionListener catalogueSelectionListener = new CatalogueSelectionListener(
                attributeViewer,
                descriptionPane);
        final CatalogueActivationListener catalogueActivationListener = new CatalogueActivationListener(
                metaCatalogueTree,
                attributeViewer,
                descriptionPane);

        metaCatalogueTree.addMouseListener(cataloguePopupMenuListener);
        metaCatalogueTree.addTreeSelectionListener(catalogueSelectionListener);
        metaCatalogueTree.addComponentListener(catalogueActivationListener);

        ComponentRegistry.registerComponents(
            frame,
            container,
            menuBar,
            toolBar,
            popupMenu,
            metaCatalogueTree,
            searchResultsTree,
            attributeViewer,
            attributeEditor,
            searchDialog,
            descriptionPane);

        setComponentRegistry(ComponentRegistry.getRegistry());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ComponentRegistry getComponentRegistry() {
        return componentRegistry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  componentRegistry  DOCUMENT ME!
     */
    public void setComponentRegistry(final ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the connectionInfo
     */
    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  connectionInfo  the connectionInfo to set
     */
    public void setConnectionInfo(final ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the infoWindowAction
     */
    public InfoWindowAction getInfoWindowAction() {
        return infoWindowAction;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  infoWindowAction  the infoWindowAction to set
     */
    public void setInfoWindowAction(final InfoWindowAction infoWindowAction) {
        this.infoWindowAction = infoWindowAction;
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
