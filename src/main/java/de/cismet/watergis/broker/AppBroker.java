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

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.java2xml.Java2XML;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;

import de.latlon.deejump.plugin.style.LayerStyle2SLDPlugIn;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.gui.componentpainter.GradientComponentPainter;

import org.deegree.style.persistence.sld.SLDParser;

import org.jdom.Element;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuListener;

import javax.xml.stream.XMLInputFactory;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MessenGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RubberBandZoomListener;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.tools.gui.DefaultPopupMenuListener;

import de.cismet.watergis.broker.listener.SelectionModeChangedEvent;
import de.cismet.watergis.broker.listener.SelectionModeListener;

import de.cismet.watergis.gui.WatergisApp;
import de.cismet.watergis.gui.actions.CleanUpAction;
import de.cismet.watergis.gui.actions.InfoWindowAction;
import de.cismet.watergis.gui.actions.foto.ExportAction;
import de.cismet.watergis.gui.actions.foto.ReportAction;
import de.cismet.watergis.gui.components.RefreshMenuItem;
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
    public static final String FOTO_MC_NAME = "foto";
    public static final String GAF_PROF_MC_NAME = "qp";
    private static ConfigurationManager configManager;
    public static final String DOMAIN_NAME = "DLM25W";
    public static final String DOMAIN_NAME_WRRL = "WRRL_DB_MV";

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
    private final EnumMap<ComponentName, Component> components = new EnumMap<ComponentName, Component>(
            ComponentName.class);
    private final HashMap<String, Action> mapModeSelectionActions = new HashMap<String, Action>();
    private MessenGeometryListener measureListener;
    private final List<SelectionModeListener> selecionModeListener = new ArrayList<SelectionModeListener>();
    private ComponentRegistry componentRegistry;
    private ConnectionInfo connectionInfo;
    private InfoWindowAction infoWindowAction;
    private Action lastActionMode;
    private Layer drawingStyleLayer;
    private List<CidsBean> ownWwGr = new ArrayList<CidsBean>();
    private String[] validLawaCodes;
    private CidsBean niemandWwGr = null;
    private View photoView;
    private View gafView;
    private ExportAction photoExport;
    private de.cismet.watergis.gui.actions.gaf.ExportAction gafExport;
    private ReportAction photoPrint;
    private de.cismet.watergis.gui.actions.gaf.ReportAction gafPrint;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AppBroker object.
     */
    private AppBroker() {
        final LayerManager layerManager = new LayerManager();
        final FeatureSchema featureSchema = new FeatureSchema();
        final FeatureCollection features = new FeatureDataset(featureSchema);

        layerManager.setFiringEvents(false);
        drawingStyleLayer = new Layer("default", Color.RED, features, layerManager);
//        BasicStyle basicStyle = new BasicStyle(Color.RED);
////        basicStyle.set;
//        String file = "/home/therter/c.png";
////        String file = PFeature.class.getResource(
////                "/de/cismet/cismap/commons/gui/res/pushpinSelected.png").toString();
////        BitmapVertexStyle vertexStyle = new BitmapVertexStyle(file);
//////        BitmapVertexStyle vertexStyle = new BitmapVertexStyle("/de/cismet/cismap/commons/gui/res/pushpin.png");
////        vertexStyle.setEnabled(true);
//        PushpinVertexStyle style = new PushpinVertexStyle();
//        style.setEnabled(true);
////        vertexStyle.setFileName(domain);
//        drawingStyleLayer.addStyle(style);
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
            if (lastActionMode instanceof CleanUpAction) {
                ((CleanUpAction)lastActionMode).cleanUp();
            }

            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, mode));

            lastActionMode = action;
        } else {
            LOG.warn("Can not switch to mode " + mode + ". It does not exist.");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Action getSelectionMode() {
        return mapModeSelectionActions.get(MappingComponent.SELECT);
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

        final DescriptionPane descriptionPane = new DescriptionPaneFS();
        final MutablePopupMenu popupMenu = new MutablePopupMenu();
        for (final PopupMenuListener l : popupMenu.getPopupMenuListeners()) {
            popupMenu.removePopupMenuListener(l);
        }

        popupMenu.removeAll();
        popupMenu.add(new RefreshMenuItem());

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
            null,
            attributeViewer,
            attributeEditor,
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

    /**
     * DOCUMENT ME!
     *
     * @return  the drawingStyleLayer
     */
    public Layer getDrawingStyleLayer() {
        return drawingStyleLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  drawingStyleLayer  the drawingStyleLayer to set
     */
    public void setDrawingStyleLayer(final Layer drawingStyleLayer) {
        this.drawingStyleLayer = drawingStyleLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layer         DOCUMENT ME!
     * @param   geometryType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String exportSLD(final Layer layer, final String geometryType) {
        String sld = null;
        try {
            final Java2XML java2Xml = new Java2XML();
            final StringWriter xmlWriter = new StringWriter();
//            final Geometry geom = firstFeature.getGeometry();
            final String name = "default";

            java2Xml.write(layer, "layer", xmlWriter);
            final HashMap<String, String> params = new HashMap<String, String>();
            params.put("wmsLayerName", name);
            params.put("featureTypeStyle", name);
            params.put("styleName", name);
            params.put("styleTitle", name);
            params.put("Namespace", "http://cismet.de");
            params.put("NamespacePrefix", "");
            params.put("geoType", geometryType);
            params.put("geomProperty", "geom");
            if (layer.getMinScale() != null) {
                params.put("maxScale", "" + layer.getMinScale());
            }
            if (layer.getMaxScale() != null) {
                params.put("minScale", "" + layer.getMaxScale());
            }

            sld = LayerStyle2SLDPlugIn.transformContext(new StringReader(xmlWriter.toString()), params);
        } catch (Exception e) {
            LOG.info("could not save sld definition", e);
        }
        return sld;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geometryType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> getDrawingStyles(final String geometryType) {
        final Reader input = new StringReader(exportSLD(drawingStyleLayer, geometryType));
        Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles = null;
        final XMLInputFactory factory = XMLInputFactory.newInstance();

        try {
            styles = SLDParser.getStyles(factory.createXMLStreamReader(input));
        } catch (Exception ex) {
            LOG.error("Fehler in der SLD", ex);
        }
        if (styles == null) {
            LOG.info("SLD Parser funtkioniert nicht");
        }
        return styles;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sld  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> getDrawingStylesBySld(final String sld) {
        final Reader input = new StringReader(sld);
        Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles = null;
        final XMLInputFactory factory = XMLInputFactory.newInstance();

        try {
            styles = SLDParser.getStyles(factory.createXMLStreamReader(input));
        } catch (Exception ex) {
            LOG.error("Fehler in der SLD", ex);
        }
        if (styles == null) {
            LOG.info("SLD Parser funtkioniert nicht");
        }
        return styles;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ownWwGr
     */
    public List<CidsBean> getOwnWwGrList() {
        return ownWwGr;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ownWwGr  the ownWwGr to set
     */
    public void setOwnWwGr(final List<CidsBean> ownWwGr) {
        this.ownWwGr = ownWwGr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the validLawaCodes
     */
    public String[] getValidLawaCodes() {
        return validLawaCodes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  validLawaCodes  the validLawaCodes to set
     */
    public void setValidLawaCodes(final String[] validLawaCodes) {
        this.validLawaCodes = validLawaCodes;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the niemandWwGr
     */
    public CidsBean getNiemandWwGr() {
        return niemandWwGr;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  niemandWwGr  the niemandWwGr to set
     */
    public void setNiemandWwGr(final CidsBean niemandWwGr) {
        this.niemandWwGr = niemandWwGr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getOwner() {
        return SessionManager.getSession().getUser().getUserGroup().getName();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsBean getOwnWwGr() {
        if ((getOwnWwGrList() != null) && !getOwnWwGrList().isEmpty()) {
            CidsBean min = null;

            for (int i = 0; i < getOwnWwGrList().size(); ++i) {
                if ((min == null)
                            || ((Integer)min.getProperty("ww_gr")
                                > (Integer)getOwnWwGrList().get(i).getProperty("ww_gr"))) {
                    min = getOwnWwGrList().get(i);
                }
            }

            return min;
        } else {
            return getNiemandWwGr();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wwGr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isOwnerWwGr(final Integer wwGr) {
        if (wwGr == null) {
            return false;
        }

        if ((getOwnWwGrList() != null) && !getOwnWwGrList().isEmpty()) {
            for (int i = 0; i < getOwnWwGrList().size(); ++i) {
                if (wwGr.equals(getOwnWwGrList().get(i).getProperty("ww_gr"))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the photoView
     */
    public View getPhotoView() {
        return photoView;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the photoView
     */
    public View getGafView() {
        return gafView;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  photoView  the photoView to set
     */
    public void setPhotoView(final View photoView) {
        this.photoView = photoView;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gafView  the photoView to set
     */
    public void setGafView(final View gafView) {
        this.gafView = gafView;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the photoExport
     */
    public ExportAction getPhotoExport() {
        return photoExport;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  photoExport  the photoExport to set
     */
    public void setPhotoExport(final ExportAction photoExport) {
        this.photoExport = photoExport;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the gafExport
     */
    public de.cismet.watergis.gui.actions.gaf.ExportAction getGafExport() {
        return gafExport;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gafExport  the gafExport to set
     */
    public void setGafExport(final de.cismet.watergis.gui.actions.gaf.ExportAction gafExport) {
        this.gafExport = gafExport;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the photoPrint
     */
    public ReportAction getPhotoPrint() {
        return photoPrint;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  photoPrint  the photoPrint to set
     */
    public void setPhotoPrint(final ReportAction photoPrint) {
        this.photoPrint = photoPrint;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the gafPrint
     */
    public de.cismet.watergis.gui.actions.gaf.ReportAction getGafPrint() {
        return gafPrint;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gafPrint  the gafPrint to set
     */
    public void setGafPrint(final de.cismet.watergis.gui.actions.gaf.ReportAction gafPrint) {
        this.gafPrint = gafPrint;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the lastActionMode
     */
    public Action getLastActionMode() {
        return lastActionMode;
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
