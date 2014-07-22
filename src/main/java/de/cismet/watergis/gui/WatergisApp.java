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
package de.cismet.watergis.gui;

import Sirius.navigator.DefaultNavigatorExceptionHandler;
import Sirius.navigator.connection.Connection;
import Sirius.navigator.connection.ConnectionFactory;
import Sirius.navigator.connection.ConnectionInfo;
import Sirius.navigator.connection.ConnectionSession;
import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.connection.proxy.ConnectionProxy;
import Sirius.navigator.exception.ConnectionException;
import Sirius.navigator.exception.ExceptionManager;
import Sirius.navigator.resource.PropertyManager;
import Sirius.navigator.ui.dialog.LoginDialog;

import Sirius.server.newuser.UserException;

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.mouse.DockingWindowActionMouseButtonListener;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.PropertiesUtil;
import net.infonode.docking.util.StringViewMap;
import net.infonode.gui.componentpainter.AlphaGradientComponentPainter;
import net.infonode.util.Direction;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import org.jdom.Element;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableListener;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;
import de.cismet.cismap.commons.gui.options.CapabilityWidgetOptionsPanel;
import de.cismet.cismap.commons.gui.overviewwidget.OverviewComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.memento.MementoInterface;

import de.cismet.cismap.linearreferencing.CreateLinearReferencedLineListener;

import de.cismet.lookupoptions.gui.OptionsClient;

import de.cismet.netutil.Proxy;

import de.cismet.tools.StaticDebuggingTools;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.historybutton.JHistoryButton;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
import de.cismet.tools.gui.startup.StaticStartupTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.components.MeasureButton;
import de.cismet.watergis.gui.components.ScaleJComboBox;
import de.cismet.watergis.gui.components.SelectionButton;
import de.cismet.watergis.gui.panels.InfoPanel;
import de.cismet.watergis.gui.panels.MapPanel;
import de.cismet.watergis.gui.panels.SelectionPanel;
import de.cismet.watergis.gui.panels.TablePanel;
import de.cismet.watergis.gui.recently_opened_files.FileMenu;
import de.cismet.watergis.gui.recently_opened_files.RecentlyOpenedFilesList;

import de.cismet.watergis.server.GeoLinkServer;

import de.cismet.watergis.utils.BookmarkManager;

import static java.awt.Frame.MAXIMIZED_BOTH;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class WatergisApp extends javax.swing.JFrame implements Configurable, WindowListener, Observer {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(WatergisApp.class);
    private static JFrame SPLASH;
    private static final ConfigurationManager configManager = new ConfigurationManager();
    private static final String FILENAME_WATERGIS_CONFIGURATION = "defaultWatergisProperties.xml";
    private static final String FILENAME_LOCAL_WATERGIS_CONFIGURATION = "watergisProperties.xml";
    private static final String CLASSPATH_WATERGIS_CONFIGURATION = "/de/cismet/watergis/configuration/";
    private static final String DIRECTORYPATH_HOME = System.getProperty("user.home");
    private static final String DIRECTORYEXTENSION = System.getProperty("directory.extension");
    private static final String FILESEPARATOR = System.getProperty("file.separator");
    private static final String DIRECTORYNAME_WATERGISHOME = ".watergis"
                + ((DIRECTORYEXTENSION != null) ? DIRECTORYEXTENSION : "");
    private static final String DIRECTORYPATH_WATERGIS = DIRECTORYPATH_HOME + FILESEPARATOR
                + DIRECTORYNAME_WATERGISHOME;
    private static final String FILEPATH_DEFAULT_LAYOUT = DIRECTORYPATH_WATERGIS + FILESEPARATOR + "watergis.layout";
    private static final String FILEPATH_PLUGIN_LAYOUT = DIRECTORYPATH_WATERGIS + FILESEPARATOR
                + "pluginWatergis.layout";
    private static final String FILEPATH_DEFAULT_APP_DATA = DIRECTORYPATH_WATERGIS + FILESEPARATOR + "watergis.data";
    private static final String FILEPATH_SCREEN = DIRECTORYPATH_WATERGIS + FILESEPARATOR + "watergis.screen";

    static {
        configManager.setDefaultFileName(FILENAME_WATERGIS_CONFIGURATION);
        configManager.setFileName(FILENAME_LOCAL_WATERGIS_CONFIGURATION);
        configManager.setClassPathFolder(CLASSPATH_WATERGIS_CONFIGURATION);
        configManager.setFolder(DIRECTORYNAME_WATERGISHOME);
    }

    //~ Instance fields --------------------------------------------------------

    private Integer httpInterfacePort = 9098;

    private RootWindow rootWindow;
    private TabWindow tabWindow;
    private StringViewMap viewMap = new StringViewMap();
    private HashMap<String, View> attributeTableMap = new HashMap<String, View>();
    // Configurable
    private Dimension windowSize = null;
    private Point windowLocation = null;
    // Panels
    private MapPanel pMap;
    private ThemeLayerWidget pTopicTree;
    private InfoPanel pInfo;
    private SelectionPanel pSelection;
    private TablePanel pTable;
    private OverviewComponent pOverview;
    private CapabilityWidget pCapabilities;
    // Views
    private View vMap;
    private View vTopicTree;
    private View vTable;
    private View vOverview;
    private View vCapability;
    private MappingComponent mappingComponent;
    private ActiveLayerModel mappingModel = new ActiveLayerModel();

    private String helpURL;
    private String infoURL;
    private boolean isInit = true;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btnGroupMapMode;
    private javax.swing.JComboBox cboScale;
    private de.cismet.watergis.gui.actions.CentralConfigAction centralConfigAction;
    private de.cismet.watergis.gui.actions.CloseAction closeAction;
    private javax.swing.JButton cmdAddBookmark;
    private javax.swing.JButton cmdDownloadManager;
    private javax.swing.JButton cmdExportMap1;
    private javax.swing.JButton cmdExportMap2;
    private javax.swing.JButton cmdFullExtend;
    private javax.swing.JButton cmdGoTo;
    private javax.swing.JButton cmdInvertSelection;
    private javax.swing.JButton cmdManageBookmarks;
    private javax.swing.JButton cmdNextExtend;
    private javax.swing.JToggleButton cmdNodeAdd;
    private javax.swing.JToggleButton cmdNodeMove;
    private javax.swing.JToggleButton cmdNodeReflectGeometry;
    private javax.swing.JToggleButton cmdNodeRemove;
    private javax.swing.JToggleButton cmdNodeRotateGeometry;
    private javax.swing.JButton cmdOpenProject;
    private javax.swing.JButton cmdPresentation;
    private javax.swing.JButton cmdPreviousExtend;
    private javax.swing.JButton cmdPrint;
    private javax.swing.JButton cmdRedo;
    private javax.swing.JButton cmdRemoveSelectionAktiveTheme;
    private javax.swing.JButton cmdRemoveSelectionAllThemes;
    private javax.swing.JButton cmdSaveProject;
    private javax.swing.JButton cmdSelectAll;
    private javax.swing.JButton cmdSelectionAttribute;
    private javax.swing.JButton cmdSelectionLocation;
    private javax.swing.JButton cmdSelectionMode;
    private javax.swing.JButton cmdTable;
    private javax.swing.JButton cmdUndo;
    private javax.swing.JButton cmdZoomIn;
    private javax.swing.JButton cmdZoomOut;
    private javax.swing.JButton cmdZoomSelectedObjects;
    private de.cismet.watergis.gui.actions.map.CreateGeoLinkAction createGeoLinkAction;
    private de.cismet.watergis.gui.actions.DownloadManagerAction downloadManagerAction;
    private javax.swing.ButtonGroup editGroup;
    private de.cismet.watergis.gui.actions.map.ExportMapAction exportMapAction;
    private de.cismet.watergis.gui.actions.map.ExportMapToFileAction exportMapToFileAction;
    private de.cismet.watergis.gui.actions.map.FullExtendAction fullExtendAction;
    private de.cismet.watergis.gui.actions.map.GoToAction goToAction;
    private de.cismet.watergis.gui.actions.InfoAction infoAction;
    private de.cismet.watergis.gui.actions.InfoWindowAction infoWindowAction;
    private de.cismet.watergis.gui.actions.selection.InvertSelectionAction invertSelectionAction;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private de.cismet.watergis.gui.actions.LocalConfigAction localConfigAction;
    private de.cismet.watergis.gui.actions.map.MeasureAction measureAction;
    private javax.swing.JMenu menBookmark;
    private javax.swing.JMenu menFile;
    private javax.swing.JMenu menHelp;
    private javax.swing.JMenu menSelection;
    private javax.swing.JMenuItem mniClose;
    private javax.swing.JMenuItem mniCreateBookmark;
    private javax.swing.JMenuItem mniCreateGeoLink;
    private javax.swing.JMenuItem mniExportMap;
    private javax.swing.JMenuItem mniFileOptions;
    private javax.swing.JMenuItem mniHelp;
    private javax.swing.JMenuItem mniInfo;
    private javax.swing.JMenuItem mniManageBookmarks;
    private javax.swing.JMenuItem mniOpenProject;
    private javax.swing.JMenuItem mniOverview;
    private javax.swing.JMenuItem mniPrint;
    private javax.swing.JMenuItem mniRemoveSelection;
    private javax.swing.JMenuItem mniSaveMapToFile;
    private javax.swing.JMenuItem mniSaveProject;
    private javax.swing.JMenuItem mniSelectAttribute;
    private javax.swing.JMenuItem mniSelectEllipse;
    private javax.swing.JMenuItem mniSelectLocation;
    private javax.swing.JMenuItem mniSelectPolygon;
    private javax.swing.JMenuItem mniSelectRectangle;
    private javax.swing.JMenuItem mniZoomSelectedObjects;
    private de.cismet.watergis.gui.actions.NewObjectAction newObjectAction;
    private de.cismet.watergis.gui.actions.map.NextExtendAction nextExtendAction;
    private de.cismet.watergis.gui.actions.OnlineHelpAction onlineHelpAction;
    private de.cismet.watergis.gui.actions.OpenProjectAction openProjectAction;
    private de.cismet.watergis.gui.actions.OptionsAction optionsAction;
    private de.cismet.watergis.gui.actions.map.PanModeAction panAction;
    private javax.swing.JPanel panMain;
    private de.cismet.watergis.gui.actions.PresentationAction presentationAction;
    private de.cismet.watergis.gui.actions.map.PreviousExtendAction previousExtendAction;
    private de.cismet.watergis.gui.actions.PrintAction printAction;
    private de.cismet.watergis.gui.actions.selection.RemoveSelectionAllTopicsAction removeSelectionAllTopicsAction;
    private de.cismet.watergis.gui.actions.selection.RemoveSelectionCurrentTopicAction
        removeSelectionCurrentTopicAction;
    private de.cismet.watergis.gui.actions.SaveProjectAction saveProjectAction;
    private de.cismet.watergis.gui.actions.selection.SelectAllAction selectAllAction;
    private de.cismet.watergis.gui.actions.selection.SelectionAttributeAction selectionAttributeAction;
    private de.cismet.watergis.gui.actions.selection.SelectionEllipseAction selectionEllipseAction;
    private de.cismet.watergis.gui.actions.selection.SelectionLocationAction selectionLocationAction;
    private de.cismet.watergis.gui.actions.map.SelectionModeAction selectionModeAction;
    private de.cismet.watergis.gui.actions.selection.SelectionPolygonAction selectionPolygonAction;
    private de.cismet.watergis.gui.actions.selection.SelectionRectangleAction selectionRectangleAction;
    private javax.swing.JPopupMenu.Separator sepCentralFilesEnd;
    private javax.swing.JPopupMenu.Separator sepCentralFilesStart;
    private javax.swing.JPopupMenu.Separator sepLocalFilesEnd;
    private de.cismet.watergis.gui.actions.bookmarks.ShowCreateBookmarkDialogAction showCreateBookmarkDialogAction;
    private de.cismet.watergis.gui.actions.window.ShowHideOverviewWindowAction showHideOverviewWindowAction;
    private de.cismet.watergis.gui.actions.bookmarks.ShowManageBookmarksDialogAction showManageBookmarksDialogAction;
    private de.cismet.watergis.gui.panels.StatusBar statusBar1;
    private de.cismet.watergis.gui.actions.TableAction tableAction;
    private javax.swing.JToggleButton tbtNewObject;
    private javax.swing.JToggleButton tbtnInfo;
    private javax.swing.JButton tbtnMeasure;
    private javax.swing.JToggleButton tbtnPanMode;
    private javax.swing.JToggleButton tbtnZoomMode;
    private javax.swing.JToolBar tobDLM25W;
    private de.cismet.watergis.gui.actions.WindowAction windowAction;
    private de.cismet.watergis.gui.actions.map.ZoomInAction zoomInAction;
    private de.cismet.watergis.gui.actions.map.ZoomModeAction zoomModeAction;
    private de.cismet.watergis.gui.actions.map.ZoomOutAction zoomOutAction;
    private de.cismet.watergis.gui.actions.selection.ZoomSelectedObjectsAction zoomSelectedObjectsAction;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form WatergisApp.
     */
    public WatergisApp() {
        addWindowListener(this);
        CismapBroker.getInstance().setUseInternalDb(true);
        try {
            initConnection(Proxy.fromPreferences());
        } catch (Exception e) {
            LOG.error("Connection exception", e);
            final List<String> messages = new ArrayList<String>();
            final StackTraceElement[] elements = e.getStackTrace();
            messages.add("call server: " + AppBroker.getInstance().getCallserverUrl());
            messages.add("Connection class: " + AppBroker.getInstance().getConnectionClass());
            messages.add("Domain: " + AppBroker.getInstance().getDomain());
            if (Proxy.fromPreferences() != null) {
                messages.add("proxy: " + Proxy.fromPreferences().isEnabled());
            }
            messages.add(e.getMessage());

            for (int i = 0; i < elements.length; i++) {
                if (elements[i] != null) {
                    messages.add(elements[i].toString());
                }
            }

            ExceptionManager.getManager()
                    .showExceptionDialog(this, ExceptionManager.ERROR, "Exception", e.getMessage(), messages);
        }
        configManager.addConfigurable(this);
        configManager.configure(this);
        configManager.addConfigurable(OptionsClient.getInstance());
        configManager.configure(OptionsClient.getInstance());
        AppBroker.setConfigManager(configManager);
        UIManager.put("Table.selectionBackground", new Color(195, 212, 232));
        UIManager.put("Tree.selectionBackground", new Color(195, 212, 232));
        initCismap();
        initComponents();
        cmdTable.setVisible(false);
        ((MeasureButton)tbtnMeasure).setButtonGroup(btnGroupMapMode);
        ((SelectionButton)cmdSelectionMode).setButtonGroup(btnGroupMapMode);
        initMapModes();
        initHistoryButtonsAndRecentlyOpenedFiles();

        initDefaultPanels();
        initDocking();
        initInfoNode();
        initAttributeTable();
        configureFileMenu();
        configureButtons();
        initLog4JQuickConfig();
        initBookmarkManager();
        if (!EventQueue.isDispatchThread()) {
            try {
                EventQueue.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            loadLayout(FILEPATH_DEFAULT_LAYOUT);
                            isInit = false;
                        }
                    });
            } catch (InterruptedException ex) {
                LOG.fatal("Problem during loading layout.", ex);
            } catch (InvocationTargetException ex) {
                LOG.fatal("Problem during loading layout.", ex);
            }
        } else {
            loadLayout(FILEPATH_DEFAULT_LAYOUT);
            isInit = false;
        }
        if (!StaticDebuggingTools.checkHomeForFile("cismetTurnOffInternalWebserver")) { // NOI18N
            initHttpServer();
        }
        panMain.add(rootWindow, BorderLayout.CENTER);
        setWindowSize();
        mappingComponent.unlock();
        pOverview.getOverviewMap().unlock();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void initCismap() {
        mappingComponent = new MappingComponent();
        AppBroker.getInstance().setMappingComponent(mappingComponent);
        final SelectionListener sl = (SelectionListener)mappingComponent.getInputEventListener()
                    .get(MappingComponent.SELECT);
        sl.setFeaturesFromServicesSelectable(true);
        mappingModel.setInitalLayerConfigurationFromServer(false);
        configManager.addConfigurable((ActiveLayerModel)mappingModel);
        configManager.addConfigurable(mappingComponent);

        // First local configuration then serverconfiguration
        configManager.configure(mappingModel);
        mappingComponent.preparationSetMappingModel(mappingModel);
        configManager.configure(mappingComponent);

        mappingComponent.setMappingModel(mappingModel);

        mappingComponent.setInternalLayerWidgetAvailable(true);
        ((Observable)mappingComponent.getMemUndo()).addObserver(this);
        ((Observable)mappingComponent.getMemRedo()).addObserver(this);

        CismapBroker.getInstance().setMappingComponent(mappingComponent);
    }

    /**
     * DOCUMENT ME!
     */
    private void initLog4JQuickConfig() {
        final KeyStroke configLoggerKeyStroke = KeyStroke.getKeyStroke(
                'L',
                InputEvent.CTRL_DOWN_MASK
                        + InputEvent.SHIFT_DOWN_MASK);
        final Action configAction = new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    java.awt.EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                Log4JQuickConfig.getSingletonInstance().setVisible(true);
                            }
                        });
                }
            };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(configLoggerKeyStroke, "CONFIGLOGGING"); // NOI18N
        getRootPane().getActionMap().put("CONFIGLOGGING", configAction);
    }

    /**
     * DOCUMENT ME!
     */
    private void initHistoryButtonsAndRecentlyOpenedFiles() {
        ((JHistoryButton)cmdNextExtend).setDirection(JHistoryButton.DIRECTION_FORWARD);
        ((JHistoryButton)cmdPreviousExtend).setDirection(JHistoryButton.DIRECTION_BACKWARD);
        ((JHistoryButton)cmdNextExtend).setHistoryModel(mappingComponent);
        ((JHistoryButton)cmdPreviousExtend).setHistoryModel(mappingComponent);

        final RecentlyOpenedFilesList recentlyOpenedFilesList = new RecentlyOpenedFilesList();
        configManager.addConfigurable(recentlyOpenedFilesList);
        configManager.configure(recentlyOpenedFilesList);
        AppBroker.getInstance().setRecentlyOpenedFilesList(recentlyOpenedFilesList);
    }

    /**
     * DOCUMENT ME!
     */
    private void initDocking() {
        rootWindow = DockingUtil.createRootWindow(viewMap, true);
        AppBroker.getInstance().setRootWindow(rootWindow);
    }

    /**
     * DOCUMENT ME!
     */
    private void initHttpServer() {
        GeoLinkServer.startServer();
    }

    /**
     * DOCUMENT ME!
     */
    private static void initLog4J() {
//        try {
//            PropertyConfigurator.configure(WatergisApp.class.getResource(
//                    "/de/cismet/watergis/configuration/log4j.properties"));
//            LOG.info("Log4J System was configured successfully");
//        } catch (Exception ex) {
//            System.err.println("Error during the initialisation");
//            ex.printStackTrace();
//        }
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender"); // NOI18N
        p.put("log4j.appender.Remote.remoteHost", "localhost");                // NOI18N
        p.put("log4j.appender.Remote.port", "4445");                           // NOI18N
        p.put("log4j.appender.Remote.locationInfo", "true");                   // NOI18N
        p.put("log4j.rootLogger", "WARN,Remote");                              // NOI18N
        org.apache.log4j.PropertyConfigurator.configure(p);
//        Log4JQuickConfig.configure4LumbermillOnLocalhost();
    }

    /**
     * DOCUMENT ME!
     */
    private void initDefaultPanels() {
        pMap = new MapPanel();
        pTopicTree = new ThemeLayerWidget();
        pInfo = new InfoPanel();
        pSelection = new SelectionPanel();
        pTopicTree.setMappingModel(mappingModel);
        pTopicTree.addAddThemeMenuItemListener(new AddThemeMenuItemListener());
        pTopicTree.addTreeSelectionListener(new TreeSelectionListener() {

                @Override
                public void valueChanged(final TreeSelectionEvent e) {
                    topicTreeSelectionChanged(e);
                }
            });

        pOverview = new OverviewComponent();
        pOverview.setMasterMap(mappingComponent);
        configManager.addConfigurable(pOverview);
        configManager.configure(pOverview);

        pCapabilities = new CapabilityWidget();
        CapabilityWidgetOptionsPanel.setCapabilityWidget(pCapabilities);
        CismapBroker.getInstance().addMapBoundsListener(pCapabilities);
        configManager.addConfigurable(pCapabilities);
        configManager.configure(pCapabilities);

        AppBroker.getInstance().addComponent(ComponentName.MAP, pMap);
        AppBroker.getInstance().addComponent(ComponentName.TREE, pTopicTree);
        AppBroker.getInstance().addComponent(ComponentName.INFO, pInfo);
        AppBroker.getInstance().addComponent(ComponentName.SELECTION, pSelection);
        AppBroker.getInstance().addComponent(ComponentName.TABLE, pTable);
        AppBroker.getInstance().addComponent(ComponentName.OVERVIEW, pOverview);
        AppBroker.getInstance().addComponent(ComponentName.CAPABILITIES, pCapabilities);

        AppBroker.getInstance().addComponent(ComponentName.MENU_BOOKMARK, menBookmark);

        AppBroker.getInstance().addComponent(ComponentName.STATUSBAR, statusBar1);
        // mappingComponent.getFeatureCollection().addFeatureCollectionListener(statusBar1);
        CismapBroker.getInstance().addStatusListener(statusBar1);

        LOG.info("set refernence for the main application in Broker: " + this);
        AppBroker.getInstance().addComponent(ComponentName.MAIN, this);
    }

    /**
     * DOCUMENT ME!
     */
    private void initMapModes() {
        AppBroker.getInstance().addMapMode(MappingComponent.PAN, panAction);
        AppBroker.getInstance().addMapMode(MappingComponent.ZOOM, zoomModeAction);
        AppBroker.getInstance().addMapMode(MappingComponent.SELECT, selectionModeAction);
        AppBroker.getInstance().addMapMode(MappingComponent.FEATURE_INFO_MULTI_GEOM, infoWindowAction);
        AppBroker.getInstance().addMapMode(AppBroker.MEASURE_MODE, measureAction);
        AppBroker.getInstance().addMapMode(FeatureCreator.SIMPLE_GEOMETRY_LISTENER_KEY, newObjectAction);
        AppBroker.getInstance().addMapMode(MappingComponent.LINEAR_REFERENCING, newObjectAction);
        AppBroker.getInstance()
                .addMapMode(CreateLinearReferencedLineListener.CREATE_LINEAR_REFERENCED_LINE_MODE, newObjectAction);

        // set the initial interaction mode
        AppBroker.getInstance().switchMapMode(mappingComponent.getInteractionMode());
    }

    /**
     * DOCUMENT ME!
     */
    private void initInfoNode() {
        String title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().TopicTree");
        vTopicTree = new View(title, null, pTopicTree);
        viewMap.addView(title, vTopicTree);

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Map");
        vMap = new View(title, null, pMap);
        viewMap.addView(title, vMap);

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Table");
        vTable = new View(title, null, pTable);
        viewMap.addView(title, vTable);

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Overview");
        vOverview = new View(title, null, pOverview);
        showHideOverviewWindowAction.setView(vOverview);
        viewMap.addView(title, vOverview);

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Capabilities");
        vCapability = new View(title, null, pCapabilities);
        viewMap.addView(title, vCapability);

        rootWindow = DockingUtil.createRootWindow(viewMap, true);
        AppBroker.getInstance().setRootWindow(rootWindow);

        rootWindow.addTabMouseButtonListener(DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);
        final DockingWindowsTheme theme = new ShapedGradientDockingTheme();
        rootWindow.getRootWindowProperties().addSuperObject(
            theme.getRootWindowProperties());

        final RootWindowProperties titleBarStyleProperties = PropertiesUtil.createTitleBarStyleRootWindowProperties();

        rootWindow.getRootWindowProperties().addSuperObject(
            titleBarStyleProperties);

        rootWindow.getRootWindowProperties().getDockingWindowProperties().setUndockEnabled(true);
        final AlphaGradientComponentPainter x = new AlphaGradientComponentPainter(
                java.awt.SystemColor.inactiveCaptionText,
                java.awt.SystemColor.activeCaptionText,
                java.awt.SystemColor.activeCaptionText,
                java.awt.SystemColor.inactiveCaptionText);
        rootWindow.getRootWindowProperties().getDragRectangleShapedPanelProperties().setComponentPainter(x);

        AppBroker.getInstance().setTitleBarComponentpainter(AppBroker.DEFAULT_MODE_COLOR);
        rootWindow.getRootWindowProperties()
                .getTabWindowProperties()
                .getTabbedPanelProperties()
                .setPaintTabAreaShadow(true);
        rootWindow.getRootWindowProperties().getTabWindowProperties().getTabbedPanelProperties().setShadowSize(10);
        rootWindow.getRootWindowProperties()
                .getTabWindowProperties()
                .getTabbedPanelProperties()
                .setShadowStrength(0.8f);
    }

    /**
     * DOCUMENT ME!
     */
    private void configureFileMenu() {
        configManager.addConfigurable((FileMenu)menFile);
        configManager.configure((FileMenu)menFile);
    }

    /**
     * DOCUMENT ME!
     */
    private void configureButtons() {
        configManager.addConfigurable((SelectionButton)cmdSelectionMode);
        configManager.configure((SelectionButton)cmdSelectionMode);
        configManager.addConfigurable((MeasureButton)tbtnMeasure);
        configManager.configure((MeasureButton)tbtnMeasure);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    private void topicTreeSelectionChanged(final TreeSelectionEvent e) {
        final TreePath tp = e.getNewLeadSelectionPath();
        tbtNewObject.setEnabled(false);
        newObjectAction.setSelectedService(null);

        if (tp != null) {
            final Object o = tp.getLastPathComponent();

            if (o instanceof AbstractFeatureService) {
                final AbstractFeatureService service = (AbstractFeatureService)o;

                if (service.isEditable()) {
                    tbtNewObject.setEnabled(true);
                    newObjectAction.setSelectedService(service);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void initAttributeTable() {
        AttributeTableFactory.getInstance().setMappingComponent(mappingComponent);
        AttributeTableFactory.getInstance().setAttributeTableListener(new AttributeTableListener() {

                @Override
                public void showPanel(final JPanel panel, final String id, final String name, final String tooltip) {
                    View view = attributeTableMap.get(id);

                    if ((vMap != null) && (vMap.getWindowParent() != null)) {
                        if (vMap.getWindowParent() instanceof TabWindow) {
                            tabWindow = (TabWindow)vMap.getWindowParent();
                        }
                    }

                    if (tabWindow == null) {
                        tabWindow = (TabWindow)((SplitWindow)rootWindow.getWindow()).getRightWindow();
                    }

                    if (view != null) {
                        final int viewIndex = tabWindow.getChildWindowIndex(view);

                        if (viewIndex != -1) {
                            tabWindow.setSelectedTab(viewIndex);
                        } else {
                            view = new View(name, null, panel);
                            viewMap.addView(name, view);
                            attributeTableMap.put(id, view);
                            tabWindow.addTab(view);
                        }
                    } else {
                        view = new View(name, null, panel);
                        viewMap.addView(name, view);
                        attributeTableMap.put(id, view);
                        tabWindow.addTab(view);
                    }
                }

                @Override
                public void changeName(final String id, final String name) {
                    final View view = attributeTableMap.get(id);

                    if (view != null) {
                        view.getViewProperties().setTitle(name);
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    private void initBookmarkManager() {
        final BookmarkManager manager = new BookmarkManager();
        configManager.addConfigurable(manager);
        configManager.configure(manager);
        AppBroker.getInstance().setBookmarkManager(manager);
    }

    /**
     * DOCUMENT ME!
     */
    public void doLayoutInfoNode() {
        tabWindow = new TabWindow(new DockingWindow[] { vMap });
        final SplitWindow treeWindow = new SplitWindow(false, 0.6f, vTopicTree, vOverview);
        rootWindow.setWindow(new SplitWindow(true, 0.22901994f, treeWindow, tabWindow));

        vMap.restoreFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        exportMapAction = new de.cismet.watergis.gui.actions.map.ExportMapAction();
        closeAction = new de.cismet.watergis.gui.actions.CloseAction();
        openProjectAction = new de.cismet.watergis.gui.actions.OpenProjectAction();
        optionsAction = new de.cismet.watergis.gui.actions.OptionsAction();
        printAction = new de.cismet.watergis.gui.actions.PrintAction();
        saveProjectAction = new de.cismet.watergis.gui.actions.SaveProjectAction();
        windowAction = new de.cismet.watergis.gui.actions.WindowAction();
        createGeoLinkAction = new de.cismet.watergis.gui.actions.map.CreateGeoLinkAction();
        selectionAttributeAction = new de.cismet.watergis.gui.actions.selection.SelectionAttributeAction();
        selectionLocationAction = new de.cismet.watergis.gui.actions.selection.SelectionLocationAction();
        removeSelectionAllTopicsAction = new de.cismet.watergis.gui.actions.selection.RemoveSelectionAllTopicsAction();
        zoomSelectedObjectsAction = new de.cismet.watergis.gui.actions.selection.ZoomSelectedObjectsAction();
        selectAllAction = new de.cismet.watergis.gui.actions.selection.SelectAllAction();
        invertSelectionAction = new de.cismet.watergis.gui.actions.selection.InvertSelectionAction();
        removeSelectionCurrentTopicAction =
            new de.cismet.watergis.gui.actions.selection.RemoveSelectionCurrentTopicAction();
        fullExtendAction = new de.cismet.watergis.gui.actions.map.FullExtendAction();
        goToAction = new de.cismet.watergis.gui.actions.map.GoToAction();
        measureAction = new de.cismet.watergis.gui.actions.map.MeasureAction();
        nextExtendAction = new de.cismet.watergis.gui.actions.map.NextExtendAction();
        panAction = new de.cismet.watergis.gui.actions.map.PanModeAction();
        previousExtendAction = new de.cismet.watergis.gui.actions.map.PreviousExtendAction();
        zoomInAction = new de.cismet.watergis.gui.actions.map.ZoomInAction();
        zoomOutAction = new de.cismet.watergis.gui.actions.map.ZoomOutAction();
        centralConfigAction = new de.cismet.watergis.gui.actions.CentralConfigAction();
        infoAction = new de.cismet.watergis.gui.actions.InfoAction();
        infoWindowAction = new de.cismet.watergis.gui.actions.InfoWindowAction();
        localConfigAction = new de.cismet.watergis.gui.actions.LocalConfigAction();
        onlineHelpAction = new de.cismet.watergis.gui.actions.OnlineHelpAction();
        tableAction = new de.cismet.watergis.gui.actions.TableAction();
        presentationAction = new de.cismet.watergis.gui.actions.PresentationAction();
        zoomModeAction = new de.cismet.watergis.gui.actions.map.ZoomModeAction();
        selectionModeAction = new de.cismet.watergis.gui.actions.map.SelectionModeAction();
        btnGroupMapMode = new javax.swing.ButtonGroup();
        downloadManagerAction = new de.cismet.watergis.gui.actions.DownloadManagerAction();
        showManageBookmarksDialogAction =
            new de.cismet.watergis.gui.actions.bookmarks.ShowManageBookmarksDialogAction();
        showCreateBookmarkDialogAction = new de.cismet.watergis.gui.actions.bookmarks.ShowCreateBookmarkDialogAction();
        exportMapToFileAction = new de.cismet.watergis.gui.actions.map.ExportMapToFileAction();
        showHideOverviewWindowAction = new de.cismet.watergis.gui.actions.window.ShowHideOverviewWindowAction();
        selectionPolygonAction = new de.cismet.watergis.gui.actions.selection.SelectionPolygonAction();
        selectionEllipseAction = new de.cismet.watergis.gui.actions.selection.SelectionEllipseAction();
        selectionRectangleAction = new de.cismet.watergis.gui.actions.selection.SelectionRectangleAction();
        editGroup = new javax.swing.ButtonGroup();
        newObjectAction = new de.cismet.watergis.gui.actions.NewObjectAction();
        tobDLM25W = new javax.swing.JToolBar();
        cmdOpenProject = new javax.swing.JButton();
        cmdSaveProject = new javax.swing.JButton();
        cmdPrint = new javax.swing.JButton();
        cmdExportMap2 = new javax.swing.JButton();
        cmdExportMap1 = new javax.swing.JButton();
        cmdDownloadManager = new javax.swing.JButton();
        cmdAddBookmark = new javax.swing.JButton();
        cmdManageBookmarks = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        cboScale = new ScaleJComboBox();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        tbtnZoomMode = new javax.swing.JToggleButton();
        cmdZoomIn = new javax.swing.JButton();
        cmdZoomOut = new javax.swing.JButton();
        tbtnPanMode = new javax.swing.JToggleButton();
        cmdGoTo = new javax.swing.JButton();
        cmdFullExtend = new javax.swing.JButton();
        cmdPreviousExtend = new JHistoryButton(false);
        cmdNextExtend = new JHistoryButton(false);
        jSeparator5 = new javax.swing.JToolBar.Separator();
        cmdSelectionMode = new SelectionButton();
        cmdSelectionAttribute = new javax.swing.JButton();
        cmdSelectionLocation = new javax.swing.JButton();
        cmdZoomSelectedObjects = new javax.swing.JButton();
        cmdSelectAll = new javax.swing.JButton();
        cmdInvertSelection = new javax.swing.JButton();
        cmdRemoveSelectionAktiveTheme = new javax.swing.JButton();
        cmdRemoveSelectionAllThemes = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        cmdTable = new javax.swing.JButton();
        tbtnInfo = new javax.swing.JToggleButton();
        tbtnMeasure = new MeasureButton();
        cmdPresentation = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        cmdNodeMove = new javax.swing.JToggleButton();
        cmdNodeAdd = new javax.swing.JToggleButton();
        cmdNodeRemove = new javax.swing.JToggleButton();
        cmdNodeRotateGeometry = new javax.swing.JToggleButton();
        cmdNodeReflectGeometry = new javax.swing.JToggleButton();
        cmdUndo = new javax.swing.JButton();
        cmdRedo = new javax.swing.JButton();
        tbtNewObject = new javax.swing.JToggleButton();
        panMain = new javax.swing.JPanel();
        statusBar1 = new de.cismet.watergis.gui.panels.StatusBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        menFile = new FileMenu();
        mniOpenProject = new javax.swing.JMenuItem();
        mniSaveProject = new javax.swing.JMenuItem();
        mniOverview = new javax.swing.JMenuItem();
        mniPrint = new javax.swing.JMenuItem();
        mniSaveMapToFile = new javax.swing.JMenuItem();
        mniExportMap = new javax.swing.JMenuItem();
        mniCreateGeoLink = new javax.swing.JMenuItem();
        mniFileOptions = new javax.swing.JMenuItem();
        sepCentralFilesStart = new javax.swing.JPopupMenu.Separator();
        sepCentralFilesEnd = new javax.swing.JPopupMenu.Separator();
        sepLocalFilesEnd = new javax.swing.JPopupMenu.Separator();
        mniClose = new javax.swing.JMenuItem();
        menBookmark = new javax.swing.JMenu();
        mniCreateBookmark = new javax.swing.JMenuItem();
        mniManageBookmarks = new javax.swing.JMenuItem();
        menSelection = new javax.swing.JMenu();
        mniSelectRectangle = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        mniSelectPolygon = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        mniSelectEllipse = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                    "ProgressBar.foreground"),
                Color.WHITE);
        mniSelectAttribute = new javax.swing.JMenuItem();
        mniSelectLocation = new javax.swing.JMenuItem();
        mniZoomSelectedObjects = new javax.swing.JMenuItem();
        mniRemoveSelection = new javax.swing.JMenuItem();
        menHelp = new javax.swing.JMenu();
        mniHelp = new javax.swing.JMenuItem();
        mniInfo = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.title")); // NOI18N

        tobDLM25W.setFloatable(false);
        tobDLM25W.setRollover(true);
        tobDLM25W.setMaximumSize(new java.awt.Dimension(679, 32769));
        tobDLM25W.setMinimumSize(new java.awt.Dimension(667, 26));
        tobDLM25W.setPreferredSize(new java.awt.Dimension(691, 28));

        cmdOpenProject.setAction(openProjectAction);
        cmdOpenProject.setFocusable(false);
        cmdOpenProject.setHideActionText(true);
        cmdOpenProject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdOpenProject.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdOpenProject.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdOpenProject.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdOpenProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdOpenProject);

        cmdSaveProject.setAction(saveProjectAction);
        cmdSaveProject.setFocusable(false);
        cmdSaveProject.setHideActionText(true);
        cmdSaveProject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSaveProject.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSaveProject.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSaveProject.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSaveProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSaveProject);

        cmdPrint.setAction(printAction);
        cmdPrint.setFocusable(false);
        cmdPrint.setHideActionText(true);
        cmdPrint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPrint.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdPrint.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdPrint.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdPrint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdPrint);

        cmdExportMap2.setAction(exportMapAction);
        cmdExportMap2.setFocusable(false);
        cmdExportMap2.setHideActionText(true);
        cmdExportMap2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdExportMap2.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdExportMap2.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdExportMap2.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdExportMap2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdExportMap2);

        cmdExportMap1.setAction(exportMapToFileAction);
        cmdExportMap1.setFocusable(false);
        cmdExportMap1.setHideActionText(true);
        cmdExportMap1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdExportMap1.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdExportMap1.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdExportMap1.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdExportMap1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdExportMap1);

        cmdDownloadManager.setAction(downloadManagerAction);
        cmdDownloadManager.setFocusable(false);
        cmdDownloadManager.setHideActionText(true);
        cmdDownloadManager.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdDownloadManager.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdDownloadManager);

        cmdAddBookmark.setAction(showCreateBookmarkDialogAction);
        cmdAddBookmark.setFocusable(false);
        cmdAddBookmark.setHideActionText(true);
        cmdAddBookmark.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdAddBookmark.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdAddBookmark.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdAddBookmark.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdAddBookmark.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdAddBookmark);

        cmdManageBookmarks.setAction(showManageBookmarksDialogAction);
        cmdManageBookmarks.setFocusable(false);
        cmdManageBookmarks.setHideActionText(true);
        cmdManageBookmarks.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdManageBookmarks.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdManageBookmarks.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdManageBookmarks.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdManageBookmarks.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdManageBookmarks);

        jSeparator8.setSeparatorSize(new java.awt.Dimension(1, 32));
        tobDLM25W.add(jSeparator8);

        cboScale.setEditable(true);
        cboScale.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cboScale.toolTipText")); // NOI18N
        cboScale.setMaximumSize(new java.awt.Dimension(100, 24));
        cboScale.setMinimumSize(new java.awt.Dimension(100, 24));
        cboScale.setPreferredSize(new java.awt.Dimension(100, 24));
        tobDLM25W.add(cboScale);

        jSeparator7.setSeparatorSize(new java.awt.Dimension(2, 32));
        tobDLM25W.add(jSeparator7);

        tbtnZoomMode.setAction(zoomModeAction);
        btnGroupMapMode.add(tbtnZoomMode);
        tbtnZoomMode.setFocusable(false);
        tbtnZoomMode.setHideActionText(true);
        tbtnZoomMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnZoomMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(tbtnZoomMode);

        cmdZoomIn.setAction(zoomInAction);
        cmdZoomIn.setFocusable(false);
        cmdZoomIn.setHideActionText(true);
        cmdZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomIn.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomIn.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomIn.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdZoomIn);

        cmdZoomOut.setAction(zoomOutAction);
        cmdZoomOut.setFocusable(false);
        cmdZoomOut.setHideActionText(true);
        cmdZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomOut.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomOut.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomOut.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdZoomOut);

        tbtnPanMode.setAction(panAction);
        btnGroupMapMode.add(tbtnPanMode);
        tbtnPanMode.setFocusable(false);
        tbtnPanMode.setHideActionText(true);
        tbtnPanMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnPanMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(tbtnPanMode);

        cmdGoTo.setAction(goToAction);
        cmdGoTo.setFocusable(false);
        cmdGoTo.setHideActionText(true);
        cmdGoTo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdGoTo.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdGoTo.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdGoTo.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdGoTo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdGoTo);

        cmdFullExtend.setAction(fullExtendAction);
        cmdFullExtend.setFocusable(false);
        cmdFullExtend.setHideActionText(true);
        cmdFullExtend.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdFullExtend.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdFullExtend.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdFullExtend.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdFullExtend.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdFullExtend);

        cmdPreviousExtend.setAction(previousExtendAction);
        cmdPreviousExtend.setFocusable(false);
        cmdPreviousExtend.setHideActionText(true);
        cmdPreviousExtend.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPreviousExtend.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdPreviousExtend.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdPreviousExtend.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdPreviousExtend.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdPreviousExtend);

        cmdNextExtend.setAction(nextExtendAction);
        cmdNextExtend.setFocusable(false);
        cmdNextExtend.setHideActionText(true);
        cmdNextExtend.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNextExtend.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdNextExtend.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdNextExtend.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdNextExtend.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdNextExtend);

        jSeparator5.setSeparatorSize(new java.awt.Dimension(2, 0));
        tobDLM25W.add(jSeparator5);

        cmdSelectionMode.setAction(selectionModeAction);
        cmdSelectionMode.setFocusable(false);
        cmdSelectionMode.setHideActionText(true);
        cmdSelectionMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectionMode.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectionMode.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectionMode.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectionMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSelectionMode);

        cmdSelectionAttribute.setAction(selectionAttributeAction);
        cmdSelectionAttribute.setFocusable(false);
        cmdSelectionAttribute.setHideActionText(true);
        cmdSelectionAttribute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectionAttribute.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectionAttribute.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectionAttribute.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectionAttribute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSelectionAttribute);

        cmdSelectionLocation.setAction(selectionLocationAction);
        cmdSelectionLocation.setFocusable(false);
        cmdSelectionLocation.setHideActionText(true);
        cmdSelectionLocation.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectionLocation.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectionLocation.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectionLocation.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectionLocation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSelectionLocation);

        cmdZoomSelectedObjects.setAction(zoomSelectedObjectsAction);
        cmdZoomSelectedObjects.setFocusable(false);
        cmdZoomSelectedObjects.setHideActionText(true);
        cmdZoomSelectedObjects.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomSelectedObjects.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedObjects.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedObjects.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedObjects.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdZoomSelectedObjects);

        cmdSelectAll.setAction(selectAllAction);
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdSelectAll,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdSelectAll.text")); // NOI18N
        cmdSelectAll.setFocusable(false);
        cmdSelectAll.setHideActionText(true);
        cmdSelectAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectAll.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectAll.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectAll.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSelectAll);

        cmdInvertSelection.setAction(invertSelectionAction);
        cmdInvertSelection.setFocusable(false);
        cmdInvertSelection.setHideActionText(true);
        cmdInvertSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdInvertSelection.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdInvertSelection.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdInvertSelection.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdInvertSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdInvertSelection);

        cmdRemoveSelectionAktiveTheme.setAction(removeSelectionCurrentTopicAction);
        cmdRemoveSelectionAktiveTheme.setFocusable(false);
        cmdRemoveSelectionAktiveTheme.setHideActionText(true);
        cmdRemoveSelectionAktiveTheme.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRemoveSelectionAktiveTheme.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAktiveTheme.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAktiveTheme.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAktiveTheme.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdRemoveSelectionAktiveTheme);

        cmdRemoveSelectionAllThemes.setAction(removeSelectionAllTopicsAction);
        cmdRemoveSelectionAllThemes.setFocusable(false);
        cmdRemoveSelectionAllThemes.setHideActionText(true);
        cmdRemoveSelectionAllThemes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRemoveSelectionAllThemes.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAllThemes.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAllThemes.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAllThemes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdRemoveSelectionAllThemes);

        jSeparator6.setSeparatorSize(new java.awt.Dimension(2, 0));
        tobDLM25W.add(jSeparator6);

        cmdTable.setAction(tableAction);
        cmdTable.setFocusable(false);
        cmdTable.setHideActionText(true);
        cmdTable.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdTable.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdTable.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdTable.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdTable.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdTable);

        tbtnInfo.setAction(infoWindowAction);
        btnGroupMapMode.add(tbtnInfo);
        tbtnInfo.setFocusable(false);
        tbtnInfo.setHideActionText(true);
        tbtnInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnInfo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(tbtnInfo);

        tbtnMeasure.setAction(measureAction);
        tbtnMeasure.setFocusable(false);
        tbtnMeasure.setHideActionText(true);
        tbtnMeasure.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnMeasure.setMaximumSize(new java.awt.Dimension(26, 26));
        tbtnMeasure.setMinimumSize(new java.awt.Dimension(26, 26));
        tbtnMeasure.setPreferredSize(new java.awt.Dimension(26, 26));
        tbtnMeasure.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(tbtnMeasure);

        cmdPresentation.setAction(presentationAction);
        cmdPresentation.setFocusable(false);
        cmdPresentation.setHideActionText(true);
        cmdPresentation.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPresentation.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdPresentation);
        tobDLM25W.add(jSeparator1);

        editGroup.add(cmdNodeMove);
        cmdNodeMove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/moveNodes.png"))); // NOI18N
        cmdNodeMove.setSelected(true);
        cmdNodeMove.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdNodeMove.toolTipText"));                                                 // NOI18N
        cmdNodeMove.setBorderPainted(false);
        cmdNodeMove.setFocusPainted(false);
        cmdNodeMove.setFocusable(false);
        cmdNodeMove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNodeMove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdNodeMove.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdNodeMoveActionPerformed(evt);
                }
            });
        tobDLM25W.add(cmdNodeMove);

        editGroup.add(cmdNodeAdd);
        cmdNodeAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/insertNodes.png"))); // NOI18N
        cmdNodeAdd.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdNodeAdd.toolTipText"));                                                   // NOI18N
        cmdNodeAdd.setBorderPainted(false);
        cmdNodeAdd.setFocusPainted(false);
        cmdNodeAdd.setFocusable(false);
        cmdNodeAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNodeAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdNodeAdd.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdNodeAddActionPerformed(evt);
                }
            });
        tobDLM25W.add(cmdNodeAdd);

        editGroup.add(cmdNodeRemove);
        cmdNodeRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/removeNodes.png"))); // NOI18N
        cmdNodeRemove.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdNodeRemove.toolTipText"));                                                   // NOI18N
        cmdNodeRemove.setBorderPainted(false);
        cmdNodeRemove.setFocusPainted(false);
        cmdNodeRemove.setFocusable(false);
        cmdNodeRemove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNodeRemove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdNodeRemove.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdNodeRemoveActionPerformed(evt);
                }
            });
        tobDLM25W.add(cmdNodeRemove);

        editGroup.add(cmdNodeRotateGeometry);
        cmdNodeRotateGeometry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rotate.png"))); // NOI18N
        cmdNodeRotateGeometry.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdNodeRotateGeometry.toolTipText"));                                              // NOI18N
        cmdNodeRotateGeometry.setBorderPainted(false);
        cmdNodeRotateGeometry.setFocusPainted(false);
        cmdNodeRotateGeometry.setFocusable(false);
        cmdNodeRotateGeometry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNodeRotateGeometry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdNodeRotateGeometry.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdNodeRotateGeometryActionPerformed(evt);
                }
            });
        tobDLM25W.add(cmdNodeRotateGeometry);

        editGroup.add(cmdNodeReflectGeometry);
        cmdNodeReflectGeometry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/mirror.png"))); // NOI18N
        cmdNodeReflectGeometry.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdNodeReflectGeometry.toolTipText"));                                              // NOI18N
        cmdNodeReflectGeometry.setBorderPainted(false);
        cmdNodeReflectGeometry.setFocusPainted(false);
        cmdNodeReflectGeometry.setFocusable(false);
        cmdNodeReflectGeometry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNodeReflectGeometry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdNodeReflectGeometry.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdNodeReflectGeometryActionPerformed(evt);
                }
            });
        tobDLM25W.add(cmdNodeReflectGeometry);

        cmdUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/watergis/res/icons22/undo.png"))); // NOI18N
        cmdUndo.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdUndo.toolTipText"));                                                                    // NOI18N
        cmdUndo.setBorderPainted(false);
        cmdUndo.setEnabled(false);
        cmdUndo.setFocusPainted(false);
        cmdUndo.setFocusable(false);
        cmdUndo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdUndo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdUndo.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdUndomniUndoPerformed(evt);
                }
            });
        tobDLM25W.add(cmdUndo);

        cmdRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/watergis/res/icons22/redo.png"))); // NOI18N
        cmdRedo.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdRedo.toolTipText"));                                                                    // NOI18N
        cmdRedo.setBorderPainted(false);
        cmdRedo.setEnabled(false);
        cmdRedo.setFocusPainted(false);
        cmdRedo.setFocusable(false);
        cmdRedo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRedo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdRedo.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRedomniRedoPerformed(evt);
                }
            });
        tobDLM25W.add(cmdRedo);

        getContentPane().add(tobDLM25W, java.awt.BorderLayout.NORTH);

        panMain.setLayout(new java.awt.BorderLayout());
        getContentPane().add(panMain, java.awt.BorderLayout.CENTER);
        getContentPane().add(statusBar1, java.awt.BorderLayout.PAGE_END);

        org.openide.awt.Mnemonics.setLocalizedText(
            menFile,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menFile.text")); // NOI18N

        mniOpenProject.setAction(openProjectAction);
        menFile.add(mniOpenProject);

        mniSaveProject.setAction(saveProjectAction);
        menFile.add(mniSaveProject);

        mniOverview.setAction(showHideOverviewWindowAction);
        menFile.add(mniOverview);

        mniPrint.setAction(printAction);
        menFile.add(mniPrint);

        mniSaveMapToFile.setAction(exportMapToFileAction);
        menFile.add(mniSaveMapToFile);

        mniExportMap.setAction(exportMapAction);
        menFile.add(mniExportMap);

        mniCreateGeoLink.setAction(createGeoLinkAction);
        menFile.add(mniCreateGeoLink);

        mniFileOptions.setAction(optionsAction);
        menFile.add(mniFileOptions);

        sepCentralFilesStart.setName("sepCentralFilesStart"); // NOI18N
        menFile.add(sepCentralFilesStart);

        sepCentralFilesEnd.setName("sepCentralFilesEnd"); // NOI18N
        menFile.add(sepCentralFilesEnd);

        sepLocalFilesEnd.setName("sepLocalFilesEnd"); // NOI18N
        menFile.add(sepLocalFilesEnd);

        mniClose.setAction(closeAction);
        menFile.add(mniClose);

        jMenuBar1.add(menFile);
        ((FileMenu)menFile).saveComponentsAfterInitialisation();

        org.openide.awt.Mnemonics.setLocalizedText(
            menBookmark,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menBookmark.text")); // NOI18N

        mniCreateBookmark.setAction(showCreateBookmarkDialogAction);
        menBookmark.add(mniCreateBookmark);

        mniManageBookmarks.setAction(showManageBookmarksDialogAction);
        menBookmark.add(mniManageBookmarks);

        jMenuBar1.add(menBookmark);

        org.openide.awt.Mnemonics.setLocalizedText(
            menSelection,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menSelection.text")); // NOI18N

        mniSelectRectangle.setAction(selectionRectangleAction);
        mniSelectRectangle.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSelectRectangle.toolTipText")); // NOI18N
        menSelection.add(mniSelectRectangle);

        mniSelectPolygon.setAction(selectionPolygonAction);
        mniSelectPolygon.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSelectPolygon.toolTipText")); // NOI18N
        mniSelectPolygon.setLabel(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSelectPolygon.label"));       // NOI18N
        menSelection.add(mniSelectPolygon);

        mniSelectEllipse.setAction(selectionEllipseAction);
        mniSelectEllipse.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSelectEllipse.toolTipText")); // NOI18N
        mniSelectEllipse.setLabel(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSelectEllipse.label"));       // NOI18N
        menSelection.add(mniSelectEllipse);

        mniSelectAttribute.setAction(selectionAttributeAction);
        menSelection.add(mniSelectAttribute);

        mniSelectLocation.setAction(selectionLocationAction);
        menSelection.add(mniSelectLocation);

        mniZoomSelectedObjects.setAction(zoomSelectedObjectsAction);
        menSelection.add(mniZoomSelectedObjects);

        mniRemoveSelection.setAction(removeSelectionAllTopicsAction);
        mniRemoveSelection.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionremove.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniRemoveSelection,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniRemoveSelection.text")); // NOI18N
        menSelection.add(mniRemoveSelection);

        jMenuBar1.add(menSelection);

        org.openide.awt.Mnemonics.setLocalizedText(
            menHelp,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menHelp.text")); // NOI18N

        mniHelp.setAction(onlineHelpAction);
        menHelp.add(mniHelp);

        mniInfo.setAction(infoAction);
        menHelp.add(mniInfo);

        jMenuBar1.add(menHelp);

        setJMenuBar(jMenuBar1);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdUndomniUndoPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdUndomniUndoPerformed
        final CustomAction a = CismapBroker.getInstance().getMappingComponent().getMemUndo().getLastAction();
        if (LOG.isDebugEnabled()) {
            LOG.debug("... execute action: " + a.info());                        // NOI18N
        }

        try {
            a.doAction();
        } catch (Exception e) {
            LOG.error("Error while executing action", e); // NOI18N
        }

        final CustomAction inverse = a.getInverse();
        CismapBroker.getInstance().getMappingComponent().getMemRedo().addAction(inverse);
        if (LOG.isDebugEnabled()) {
            LOG.debug("... new action on REDO stack: " + inverse); // NOI18N
            LOG.debug("... completed");                            // NOI18N
        }
    }                                                              //GEN-LAST:event_cmdUndomniUndoPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRedomniRedoPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRedomniRedoPerformed
        final CustomAction a = CismapBroker.getInstance().getMappingComponent().getMemRedo().getLastAction();
        if (LOG.isDebugEnabled()) {
            LOG.debug("... execute action: " + a.info());                        // NOI18N
        }

        try {
            a.doAction();
        } catch (Exception e) {
            LOG.error("Error while executing action", e); // NOI18N
        }

        final CustomAction inverse = a.getInverse();
        CismapBroker.getInstance().getMappingComponent().getMemUndo().addAction(inverse);
        if (LOG.isDebugEnabled()) {
            LOG.debug("... new action on REDO stack: " + inverse); // NOI18N
            LOG.debug("... completed");                            // NOI18N
        }
    }                                                              //GEN-LAST:event_cmdRedomniRedoPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdNodeMoveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdNodeMoveActionPerformed
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    CismapBroker.getInstance()
                            .getMappingComponent()
                            .setHandleInteractionMode(MappingComponent.MOVE_HANDLE);
                    CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
                }
            });
    } //GEN-LAST:event_cmdNodeMoveActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdNodeAddActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdNodeAddActionPerformed
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    CismapBroker.getInstance()
                            .getMappingComponent()
                            .setHandleInteractionMode(MappingComponent.ADD_HANDLE);
                    CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
                }
            });
    } //GEN-LAST:event_cmdNodeAddActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdNodeRemoveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdNodeRemoveActionPerformed
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    CismapBroker.getInstance()
                            .getMappingComponent()
                            .setHandleInteractionMode(MappingComponent.REMOVE_HANDLE);
                    CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
                }
            });
    } //GEN-LAST:event_cmdNodeRemoveActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdNodeRotateGeometryActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdNodeRotateGeometryActionPerformed
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    CismapBroker.getInstance()
                            .getMappingComponent()
                            .setHandleInteractionMode(MappingComponent.ROTATE_POLYGON);
                    CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
                }
            });
    } //GEN-LAST:event_cmdNodeRotateGeometryActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdNodeReflectGeometryActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdNodeReflectGeometryActionPerformed
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    CismapBroker.getInstance()
                            .getMappingComponent()
                            .setHandleInteractionMode(MappingComponent.REFLECT_POLYGON);
                    CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
                }
            });
    } //GEN-LAST:event_cmdNodeReflectGeometryActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param   proxyConfig  DOCUMENT ME!
     *
     * @throws  ConnectionException   DOCUMENT ME!
     * @throws  InterruptedException  DOCUMENT ME!
     */
    private void initConnection(final Proxy proxyConfig) throws ConnectionException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initialising connection using proxy: " + proxyConfig);
        }

        final PropertyManager propertyManager = PropertyManager.getManager();
        final Connection connection = ConnectionFactory.getFactory()
                    .createConnection(AppBroker.getInstance().getConnectionClass(),
                        AppBroker.getInstance().getCallserverUrl(),
                        proxyConfig);
        ConnectionSession session = null;
        ConnectionProxy proxy = null;

        if (session == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("performing login"); // NOI18N
            }
            try {
                final ConnectionInfo connInfo = new ConnectionInfo();
                connInfo.setCallserverURL(AppBroker.getInstance().getCallserverUrl());
                session = ConnectionFactory.getFactory().createSession(connection, connInfo, false);
            } catch (UserException uexp) {
            }                                 // should never happen
            proxy = ConnectionFactory.getFactory()
                        .createProxy("Sirius.navigator.connection.proxy.DefaultConnectionProxyHandler", session);
            SessionManager.init(proxy);

            final LoginDialog loginDialog = new LoginDialog(this);
            StaticSwingTools.showDialog(loginDialog);
        }

//        PropertyManager.getManager()
//                .setEditable(this.hasPermission(
//                        SessionManager.getProxy().getClasses(),
//                        PermissionHolder.WRITEPERMISSION));
        // PropertyManager.getManager().setEditable(true);
//        if (LOG.isInfoEnabled()) {
//            LOG.info("initConnection(): navigator editor enabled: " + PropertyManager.getManager().isEditable()); // NOI18N
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(DefaultNavigatorExceptionHandler.getInstance());
        initLog4J();
        try {
            LOG.warn("args: " + args.length);
            final Options options = new Options();
            options.addOption("u", true, "CallserverUrl");
            options.addOption("c", true, "ConnectionClass");
            options.addOption("d", true, "Domain");
            final PosixParser parser = new PosixParser();
            final CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("u")) {
                AppBroker.getInstance().setCallserverUrl(cmd.getOptionValue("u"));
            } else {
                LOG.warn("No Callserverhost specified, please specify it with the option -u.");
                System.exit(1);
            }
            if (cmd.hasOption("c")) {
                AppBroker.getInstance().setConnectionClass(cmd.getOptionValue("c"));
            } else {
                LOG.warn("No ConnectionClass specified, please specify it with the option -c.");
                System.exit(1);
            }
            if (cmd.hasOption("d")) {
                AppBroker.getInstance().setDomain(cmd.getOptionValue("d"));
            } else {
                LOG.error("No Domain specified, please specify it with the option -d.");
                System.exit(1);
            }
        } catch (Exception ex) {
            LOG.error("Error while reading the command-line parameters.", ex);
            System.exit(1);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        final PlasticXPLookAndFeel lf = new PlasticXPLookAndFeel();
                        javax.swing.UIManager.setLookAndFeel(lf);
                    } catch (Exception ex) {
                        LOG.error("Error while setting the Look & Feel", ex);
                    }
                    try {
                        // SPLASH = StaticStartupTools.showGhostFrame(FILEPATH_SCREEN, "FIS Gewässer [Startup]");
                    } catch (Exception e) {
                        LOG.warn("Problem with displaying the Pre-Loading-Frame", e);
                    }
                    try {
//                        handleLogin();
                        AppBroker.getInstance().setLoggedIn(true);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Login successful");
                        }
                        final WatergisApp app = new WatergisApp();
                        app.setVisible(true);
//                        if (SPLASH != null) {
//                            SPLASH.dispose();
//                        }
//                        SPLASH = null;
                    } catch (Exception ex) {
                        LOG.error("Error during the Loginframe", ex);
                        System.exit(0);
                    }
                }
            });
    }

    @Override
    public Element getConfiguration() {
        final Element ret = new Element("cismapPluginUIPreferences");
        final Element window = new Element("window");
        final int windowHeight = this.getHeight();
        final int windowWidth = this.getWidth();
        final int windowX = (int)this.getLocation().getX();
        final int windowY = (int)this.getLocation().getY();
        final boolean windowMaximised = (this.getExtendedState() == MAXIMIZED_BOTH);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Windowsize: width " + windowWidth + " height " + windowHeight);
        }
        window.setAttribute("height", "" + windowHeight);
        window.setAttribute("width", "" + windowWidth);
        window.setAttribute("x", "" + windowX);
        window.setAttribute("y", "" + windowY);
        window.setAttribute("max", "" + windowMaximised);
        ret.addContent(window);
        return ret;
    }

    @Override
    public void masterConfigure(final Element parent) {
        final Element prefs = parent.getChild("urls");
        try {
            final Element help_url_element = prefs.getChild("onlineHelp"); // NOI18N
            final Element info_url_element = prefs.getChild("info");       // NOI18N

            helpURL = help_url_element.getText();
            if (LOG.isDebugEnabled()) {
                LOG.debug("helpUrl:" + helpURL); // NOI18N
            }

            infoURL = info_url_element.getText();
        } catch (Throwable t) {
            LOG.error("Error while loading the help urls (" + prefs.getChildren() + ")", t); // NOI18N
        }

        final Element ports = parent.getChild("ports");
        final Element httpInterfacePortElement = ports.getChild("httpInterfacePort"); // NOI18N

        try {
            httpInterfacePort = new Integer(httpInterfacePortElement.getText());
        } catch (Throwable t) {
            LOG.warn("httpInterface was not configured. Set default value: " + httpInterfacePort, t); // NOI18N
        }

        // try {
// // ToDo if it fails all fail better place in the single try catch
// final Element urls = parent.getChild("urls");
// final Element albConfiguration = parent.getChild("albConfiguration");
// try {
// if (LOG.isDebugEnabled()) {
// LOG.debug("OnlineHilfeUrl: " + urls.getChildText("onlineHelp"));
// }
// onlineHelpURL = urls.getChildText("onlineHelp");
// } catch (Exception ex) {
// LOG.warn("Fehler beim lesen der OnlineHilfe URL", ex);
// }
// try {
// albURL = albConfiguration.getChildText("albURL");
// if (albURL != null) {
// albURL = albURL.trim();
// }
// if (LOG.isDebugEnabled()) {
// LOG.debug("ALBURL: " + albURL.trim());
// }
// } catch (Exception ex) {
// LOG.warn("Fehler beim lesen der ALB Konfiguration", ex);
// }
// try {
// if (LOG.isDebugEnabled()) {
// LOG.debug("News Url: " + urls.getChildText("onlineHelp"));
// }
// newsURL = urls.getChildText("news");
// } catch (Exception ex) {
// LOG.warn("Fehler beim lesen der News Url", ex);
// }
//
// } catch (Exception ex) {
// LOG.error("Fehler beim konfigurieren der Watergis Applikation: ", ex);
// }
    }

    @Override
    public void dispose() {
        try {
            StaticStartupTools.saveScreenshotOfFrame(this, FILEPATH_SCREEN);
        } catch (Exception ex) {
            LOG.fatal("Error while capturing the app content", ex);
        }

        setVisible(false);
        LOG.info("Dispose(): Watergis is going to shut down");

//        this.saveAppData(FILEPATH_DEFAULT_APP_DATA);

        configManager.writeConfiguration();
        saveLayout(FILEPATH_DEFAULT_LAYOUT);

        super.dispose();
        System.exit(0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file  DOCUMENT ME!
     */
    public void saveLayout(final String file) {
        // AppBroker.getInstance().setTitleBarComponentpainter(AppBroker.DEFAULT_MODE_COLOR);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving Layout.. to " + file);
        }
        final File layoutFile = new File(file);
        try {
            if (!layoutFile.exists()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Saving Layout.. File '" + file + "' does not exit");
                }
                layoutFile.createNewFile();
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Saving Layout.. File '" + file + "' does exit");
                }
            }
            final FileOutputStream layoutOutput = new FileOutputStream(layoutFile);
            final ObjectOutputStream out = new ObjectOutputStream(layoutOutput);

            // close all attribute table views
            for (final String key : attributeTableMap.keySet()) {
                final View attrTableView = attributeTableMap.get(key);
                if (attrTableView != null) {
                    attrTableView.close();
                }
            }

            rootWindow.write(out);
            out.flush();
            out.close();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Saving Layout.. to " + file + " successfull");
            }
        } catch (IOException ex) {
            final String message = org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.saveLayout().MessageDialog.message");
            final String title = org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.saveLayout().MessageDialog.title");
            JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE);
            LOG.error("A failure occured during writing the layout file " + file, ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file  DOCUMENT ME!
     */
    public void loadLayout(final String file) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Load Layout.. from " + file);
        }
        final File layoutFile = new File(file);

        if (layoutFile.exists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Layout File exists");
            }
            try {
                final FileInputStream layoutInput = new FileInputStream(layoutFile);
                final ObjectInputStream in = new ObjectInputStream(layoutInput);
                rootWindow.read(in);
                in.close();
                rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
                rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
                if (isInit) {
                    final int count = viewMap.getViewCount();
                    for (int i = 0; i < count; i++) {
                        final View current = viewMap.getViewAtIndex(i);
                        if (current.isUndocked()) {
                            current.dock();
                        }
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Loading Layout successfull");
                }
            } catch (IOException ex) {
                LOG.error("Layout File IO Exception --> loading default Layout", ex);
                if (isInit) {
                    JOptionPane.showMessageDialog(
                        this,
                        "W\u00E4hrend dem Laden des Layouts ist ein Fehler aufgetreten.\n Das Layout wird zur\u00FCckgesetzt.",
                        "Fehler",
                        JOptionPane.INFORMATION_MESSAGE);
                    doLayoutInfoNode();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "W\u00E4hrend dem Laden des Layouts ist ein Fehler aufgetreten.\n Das Layout wird zur\u00FCckgesetzt.",
                        "Fehler",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else {
            if (isInit) {
                LOG.warn("Datei exitstiert nicht --> default layout (init)");
                SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            // UGLY WINNING --> Gefixed durch IDW Version 1.5
                            // setupDefaultLayout();
                            // DeveloperUtil.createWindowLayoutFrame("nach setup1",rootWindow).setVisible(true);
                            doLayoutInfoNode();
                            // DeveloperUtil.createWindowLayoutFrame("nach setup2",rootWindow).setVisible(true);
                        }
                    });
            } else {
                LOG.warn("Datei exitstiert nicht)");
                JOptionPane.showMessageDialog(
                    this,
                    "Das angegebene Layout konnte nicht gefunden werden.",
                    "Fehler",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Override
    public void configure(final Element parent) {
        final Element prefs = parent.getChild("cismapPluginUIPreferences");
        if (prefs == null) {
            LOG.warn("there is no local configuration 'cismapPluginUIPreferences'");
        } else {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("setting windowsize of application");
                }
                final Element window = prefs.getChild("window");
                if (window == null) {
                    LOG.warn("there is no 'window' configuration in 'cismapPluginUIPreferences'");
                } else {
                    final int windowHeight = window.getAttribute("height").getIntValue();
                    final int windowWidth = window.getAttribute("width").getIntValue();
                    final int windowX = window.getAttribute("x").getIntValue();
                    final int windowY = window.getAttribute("y").getIntValue();
                    final boolean windowMaximised = window.getAttribute("max").getBooleanValue();
                    windowSize = new Dimension(windowWidth, windowHeight);
                    windowLocation = new Point(windowX, windowY);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("windowSize: width " + windowWidth + " heigth " + windowHeight);
                    }
                    // TODO why is this not working
                    // mapComponent.formComponentResized(null);
                    if (windowMaximised) {
                        this.setExtendedState(MAXIMIZED_BOTH);
                    } else {
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("setting of window successful");
                    }
                }
            } catch (Exception t) {
                // TODO defaults
                LOG.error("Error while setting windowsize", t);
            }
        }
    }

    @Override
    public void windowOpened(final WindowEvent e) {
    }

    @Override
    public void windowClosing(final WindowEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("windowClosing():");
        }
        cleanUp();
        dispose();
    }

    @Override
    public void windowClosed(final WindowEvent e) {
    }

    @Override
    public void windowIconified(final WindowEvent e) {
    }

    @Override
    public void windowDeiconified(final WindowEvent e) {
    }

    @Override
    public void windowActivated(final WindowEvent e) {
    }

    @Override
    public void windowDeactivated(final WindowEvent e) {
    }

    /**
     * DOCUMENT ME!
     */
    private void cleanUp() {
    }

    /**
     * DOCUMENT ME!
     */
    private void setWindowSize() {
        if ((windowSize != null) && (windowLocation != null)) {
            this.setSize(windowSize);
            this.setLocation(windowLocation);
        } else {
            this.pack();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getHelpURL() {
        return helpURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getInfoURL() {
        return infoURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Integer getHttpInterfacePort() {
        return httpInterfacePort;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getDIRECTORYPATH_WATERGIS() {
        return DIRECTORYPATH_WATERGIS;
    }

    @Override
    public void update(final Observable o, final Object arg) {
        final MappingComponent mapC = CismapBroker.getInstance().getMappingComponent();

        if (o.equals(mapC.getMemUndo())) {
            if (arg.equals(MementoInterface.ACTIVATE) && !cmdUndo.isEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("activate UNDO button"); // NOI18N
                }
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            cmdUndo.setEnabled(true);
                        }
                    });
            } else if (arg.equals(MementoInterface.DEACTIVATE) && cmdUndo.isEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("deactivate UNDO button"); // NOI18N
                }
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            cmdUndo.setEnabled(false);
                        }
                    });
            }
        } else if (o.equals(mapC.getMemRedo())) {
            if (arg.equals(MementoInterface.ACTIVATE) && !cmdRedo.isEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("activate REDO button"); // NOI18N
                }
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            cmdRedo.setEnabled(true);
                        }
                    });
            } else if (arg.equals(MementoInterface.DEACTIVATE) && cmdRedo.isEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("deactivate REDO button"); // NOI18N
                }
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            cmdRedo.setEnabled(false);
                        }
                    });
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AddThemeMenuItemListener implements ActionListener {

        //~ Instance fields ----------------------------------------------------

        boolean capabilitiesDialogWasFloating;

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            showView();
        }

        /**
         * DOCUMENT ME!
         */
        private void showView() {
            final Dimension prefSize = pCapabilities.getPreferredSize();
            final Dimension size = pCapabilities.getSize();
            Dimension sizeToUse;
            if ((prefSize.height * prefSize.width) >= (size.height * size.width)) {
                sizeToUse = prefSize;
            } else {
                sizeToUse = size;
            }

            final FloatingWindow fw = rootWindow.createFloatingWindow(
                    getPointOfCenterOfScreen(sizeToUse),
                    sizeToUse,
                    vCapability);
            fw.getTopLevelAncestor().setVisible(true);
        }

        /**
         * Centers a Dimension instance on the screen on which the mouse pointer is located.
         *
         * @param   d  w window instance to be centered
         *
         * @return  DOCUMENT ME!
         *
         * @see     StaticSwingTools..centerWindowOnScreen()
         */
        public Point getPointOfCenterOfScreen(final Dimension d) {
            final PointerInfo pInfo = MouseInfo.getPointerInfo();
            final Point pointerLocation = pInfo.getLocation();

            // determine screen boundaries w.r.t. the current mouse position
            final GraphicsConfiguration[] cfgArr = pInfo.getDevice().getConfigurations();

            Rectangle bounds = null;
            for (int i = 0; i < cfgArr.length; i++) {
                bounds = cfgArr[i].getBounds();

                if (pointerLocation.x <= bounds.x) {
                    break;
                }
            }

            // determine coordinates in the center of the current mouse location
            final int x = (int)(bounds.x + ((bounds.width - d.getWidth()) / 2));
            final int y = (int)(bounds.y + ((bounds.height - d.getHeight()) / 2));

            return new Point(x, y);
        }
    }
}
