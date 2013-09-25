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

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

import net.infonode.docking.DockingWindow;
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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
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

import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableListener;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.lookupoptions.gui.OptionsClient;

import de.cismet.tools.StaticDebuggingTools;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.tools.gui.historybutton.JHistoryButton;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
import de.cismet.tools.gui.startup.StaticStartupTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.components.ScaleJComboBox;
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
public class WatergisApp extends javax.swing.JFrame implements Configurable, WindowListener {

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
    // Views
    private View vMap;
    private View vTopicTree;
    private View vInfo;
    private View vSelection;
    private View vTable;
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
    private javax.swing.JButton cmdExportMap;
    private javax.swing.JButton cmdExportMap1;
    private javax.swing.JButton cmdFullExtend;
    private javax.swing.JButton cmdGoTo;
    private javax.swing.JButton cmdInfo;
    private javax.swing.JButton cmdInvertSelection;
    private javax.swing.JButton cmdManageBookmarks;
    private javax.swing.JButton cmdMeasure;
    private javax.swing.JButton cmdNextExtend;
    private javax.swing.JButton cmdOpenProject;
    private javax.swing.JButton cmdPresentation;
    private javax.swing.JButton cmdPreviousExtend;
    private javax.swing.JButton cmdPrint;
    private javax.swing.JButton cmdRemoveSelectionAktiveTheme;
    private javax.swing.JButton cmdRemoveSelectionAllThemes;
    private javax.swing.JButton cmdSaveProject;
    private javax.swing.JButton cmdSelectAll;
    private javax.swing.JButton cmdSelectionAttribute;
    private javax.swing.JButton cmdSelectionForm;
    private javax.swing.JButton cmdSelectionLocation;
    private javax.swing.JButton cmdTable;
    private javax.swing.JButton cmdZoomIn;
    private javax.swing.JButton cmdZoomOut;
    private javax.swing.JButton cmdZoomSelectedObjects;
    private de.cismet.watergis.gui.actions.map.CreateGeoLinkAction createGeoLinkAction;
    private de.cismet.watergis.gui.actions.DownloadManagerAction downloadManagerAction;
    private de.cismet.watergis.gui.actions.map.ExportMapAction exportMapAction;
    private de.cismet.watergis.gui.actions.map.ExportMapToFileAction exportMapToFileAction;
    private de.cismet.watergis.gui.actions.map.FullExtendAction fullExtendAction;
    private de.cismet.watergis.gui.actions.map.GoToAction goToAction;
    private de.cismet.watergis.gui.actions.InfoAction infoAction;
    private de.cismet.watergis.gui.actions.InfoWindowAction infoWindowAction;
    private de.cismet.watergis.gui.actions.selection.InvertSelectionAction invertSelectionAction;
    private javax.swing.JMenuBar jMenuBar1;
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
    private javax.swing.JMenuItem mniPrint;
    private javax.swing.JMenuItem mniRemoveSelection;
    private javax.swing.JMenuItem mniSaveProject;
    private javax.swing.JMenuItem mniSelectAttribute;
    private javax.swing.JMenuItem mniSelectForm;
    private javax.swing.JMenuItem mniSelectLocation;
    private javax.swing.JMenuItem mniSelectionOptions;
    private javax.swing.JMenuItem mniWindow;
    private javax.swing.JMenuItem mniZoomSelectedObjects;
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
    private de.cismet.watergis.gui.actions.selection.SelectionFormAction selectionFormAction;
    private de.cismet.watergis.gui.actions.selection.SelectionLocationAction selectionLocationAction;
    private de.cismet.watergis.gui.actions.map.SelectionModeAction selectionModeAction;
    private de.cismet.watergis.gui.actions.selection.SelectionOptionsAction selectionOptionsAction;
    private javax.swing.JPopupMenu.Separator sepCentralFilesEnd;
    private javax.swing.JPopupMenu.Separator sepCentralFilesStart;
    private javax.swing.JPopupMenu.Separator sepLocalFilesEnd;
    private de.cismet.watergis.gui.actions.bookmarks.ShowCreateBookmarkDialogAction showCreateBookmarkDialogAction;
    private de.cismet.watergis.gui.actions.bookmarks.ShowManageBookmarksDialogAction showManageBookmarksDialogAction;
    private de.cismet.watergis.gui.panels.StatusBar statusBar1;
    private de.cismet.watergis.gui.actions.TableAction tableAction;
    private javax.swing.JToggleButton tbtnPanMode;
    private javax.swing.JToggleButton tbtnSelectionMode;
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
        configManager.addConfigurable(this);
        configManager.configure(this);
        configManager.addConfigurable(OptionsClient.getInstance());
        configManager.configure(OptionsClient.getInstance());
        AppBroker.setConfigManager(configManager);
        initCismap();
        initComponents();
        initMapModes();
        initHistoryButtonsAndRecentlyOpenedFiles();

        initDefaultPanels();
        initDocking();
        initInfoNode();
        initAttributeTable();
        configureFileMenu();
        initLog4JQuickConfig();
        initBookmarkManager();
        if (!EventQueue.isDispatchThread()) {
            try {
                EventQueue.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            loadLayout(FILEPATH_DEFAULT_LAYOUT);
                        }
                    });
            } catch (InterruptedException ex) {
                LOG.fatal("Problem during loading layout.", ex);
            } catch (InvocationTargetException ex) {
                LOG.fatal("Problem during loading layout.", ex);
            }
        } else {
            loadLayout(FILEPATH_DEFAULT_LAYOUT);
        }
        if (!StaticDebuggingTools.checkHomeForFile("cismetTurnOffInternalWebserver")) { // NOI18N
            initHttpServer();
        }
        panMain.add(rootWindow, BorderLayout.CENTER);
        setWindowSize();
        mappingComponent.unlock();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void initCismap() {
        mappingComponent = new MappingComponent();
        AppBroker.getInstance().setMappingComponent(mappingComponent);
        mappingModel.setInitalLayerConfigurationFromServer(false);
        configManager.addConfigurable((ActiveLayerModel)mappingModel);
        configManager.addConfigurable(mappingComponent);

        // First local configuration then serverconfiguration
        configManager.configure(mappingModel);
        mappingComponent.preparationSetMappingModel(mappingModel);
        configManager.configure(mappingComponent);

        mappingComponent.setMappingModel(mappingModel);

        mappingComponent.setInternalLayerWidgetAvailable(true);

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
        Log4JQuickConfig.configure4LumbermillOnLocalhost();
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

        AppBroker.getInstance().addComponent(ComponentName.MAP, pMap);
        AppBroker.getInstance().addComponent(ComponentName.TREE, pTopicTree);
        AppBroker.getInstance().addComponent(ComponentName.INFO, pInfo);
        AppBroker.getInstance().addComponent(ComponentName.SELECTION, pSelection);
        AppBroker.getInstance().addComponent(ComponentName.TABLE, pTable);

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

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Info");
        vInfo = new View(title, null, pInfo);
        viewMap.addView(title, vInfo);

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Selection");
        vSelection = new View(title, null, pSelection);
        viewMap.addView(title, vSelection);

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Table");
        vTable = new View(title, null, pTable);
        viewMap.addView(title, vTable);

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
    private void initAttributeTable() {
        AttributeTableFactory.getInstance().setMappingComponent(mappingComponent);
        AttributeTableFactory.getInstance().setAttributeTableListener(new AttributeTableListener() {

                @Override
                public void showPanel(final JPanel panel, final String id, final String name, final String tooltip) {
                    View view = attributeTableMap.get(id);

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
        tabWindow = new TabWindow(new DockingWindow[] { vMap, vInfo, vSelection });
        rootWindow.setWindow(new SplitWindow(true, 0.22901994f, vTopicTree, tabWindow));
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
        selectionFormAction = new de.cismet.watergis.gui.actions.selection.SelectionFormAction();
        selectionLocationAction = new de.cismet.watergis.gui.actions.selection.SelectionLocationAction();
        removeSelectionAllTopicsAction = new de.cismet.watergis.gui.actions.selection.RemoveSelectionAllTopicsAction();
        selectionOptionsAction = new de.cismet.watergis.gui.actions.selection.SelectionOptionsAction();
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
        tobDLM25W = new javax.swing.JToolBar();
        cmdOpenProject = new javax.swing.JButton();
        cmdSaveProject = new javax.swing.JButton();
        cmdPrint = new javax.swing.JButton();
        cmdExportMap = new javax.swing.JButton();
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
        cmdPreviousExtend = new JHistoryButton();
        cmdNextExtend = new JHistoryButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        tbtnSelectionMode = new javax.swing.JToggleButton();
        cmdSelectionForm = new javax.swing.JButton();
        cmdSelectionAttribute = new javax.swing.JButton();
        cmdSelectionLocation = new javax.swing.JButton();
        cmdZoomSelectedObjects = new javax.swing.JButton();
        cmdSelectAll = new javax.swing.JButton();
        cmdInvertSelection = new javax.swing.JButton();
        cmdRemoveSelectionAktiveTheme = new javax.swing.JButton();
        cmdRemoveSelectionAllThemes = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        cmdTable = new javax.swing.JButton();
        cmdInfo = new javax.swing.JButton();
        cmdMeasure = new javax.swing.JButton();
        cmdPresentation = new javax.swing.JButton();
        panMain = new javax.swing.JPanel();
        statusBar1 = new de.cismet.watergis.gui.panels.StatusBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        menFile = new FileMenu();
        mniOpenProject = new javax.swing.JMenuItem();
        mniSaveProject = new javax.swing.JMenuItem();
        mniWindow = new javax.swing.JMenuItem();
        mniPrint = new javax.swing.JMenuItem();
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
        mniSelectForm = new javax.swing.JMenuItem();
        mniSelectAttribute = new javax.swing.JMenuItem();
        mniSelectLocation = new javax.swing.JMenuItem();
        mniZoomSelectedObjects = new javax.swing.JMenuItem();
        mniRemoveSelection = new javax.swing.JMenuItem();
        mniSelectionOptions = new javax.swing.JMenuItem();
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

        cmdExportMap.setAction(exportMapAction);
        cmdExportMap.setFocusable(false);
        cmdExportMap.setHideActionText(true);
        cmdExportMap.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdExportMap.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdExportMap.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdExportMap.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdExportMap.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdExportMap);

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

        tbtnSelectionMode.setAction(selectionModeAction);
        btnGroupMapMode.add(tbtnSelectionMode);
        org.openide.awt.Mnemonics.setLocalizedText(
            tbtnSelectionMode,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.tbtnSelectionMode.text_1")); // NOI18N
        tbtnSelectionMode.setFocusable(false);
        tbtnSelectionMode.setHideActionText(true);
        tbtnSelectionMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnSelectionMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(tbtnSelectionMode);

        cmdSelectionForm.setAction(selectionFormAction);
        cmdSelectionForm.setFocusable(false);
        cmdSelectionForm.setHideActionText(true);
        cmdSelectionForm.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectionForm.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectionForm.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectionForm.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectionForm.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSelectionForm);

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

        cmdInfo.setAction(infoWindowAction);
        cmdInfo.setFocusable(false);
        cmdInfo.setHideActionText(true);
        cmdInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdInfo.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdInfo.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdInfo.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdInfo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdInfo);

        cmdMeasure.setAction(measureAction);
        cmdMeasure.setFocusable(false);
        cmdMeasure.setHideActionText(true);
        cmdMeasure.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdMeasure.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdMeasure.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdMeasure.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdMeasure.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdMeasure);

        cmdPresentation.setAction(presentationAction);
        cmdPresentation.setFocusable(false);
        cmdPresentation.setHideActionText(true);
        cmdPresentation.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPresentation.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdPresentation);

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

        mniWindow.setAction(windowAction);
        menFile.add(mniWindow);

        mniPrint.setAction(printAction);
        menFile.add(mniPrint);

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

        mniSelectForm.setAction(selectionFormAction);
        menSelection.add(mniSelectForm);

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

        mniSelectionOptions.setAction(selectionOptionsAction);
        mniSelectionOptions.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-settingsandroid.png")));         // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectionOptions,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniSelectionOptions.text")); // NOI18N
        menSelection.add(mniSelectionOptions);

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
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(final Thread t, final Throwable e) {
                    LOG.error("Uncaught Exception in " + t, e);
                }
            });
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
}
