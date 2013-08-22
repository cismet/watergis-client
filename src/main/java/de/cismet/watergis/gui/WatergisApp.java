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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.jdom.Element;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.tools.gui.startup.StaticStartupTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.actions.CloseAction;
import de.cismet.watergis.gui.actions.OptionsAction;
import de.cismet.watergis.gui.panels.InfoPanel;
import de.cismet.watergis.gui.panels.MapPanel;
import de.cismet.watergis.gui.panels.SelectionPanel;
import de.cismet.watergis.gui.panels.TablePanel;
import de.cismet.watergis.gui.panels.TopicTreePanel;

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

    private RootWindow rootWindow;
    private StringViewMap viewMap = new StringViewMap();
    // Configurable
    private Dimension windowSize = null;
    private Point windowLocation = null;
    // Panels
    private MapPanel pMap;
    private TopicTreePanel pTopicTree;
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdAddBookmark;
    private javax.swing.JButton cmdExportMap;
    private javax.swing.JButton cmdFullExtend;
    private javax.swing.JButton cmdGoTo;
    private javax.swing.JButton cmdInfo;
    private javax.swing.JButton cmdInvertSelection;
    private javax.swing.JButton cmdManageBookmarks;
    private javax.swing.JButton cmdMeasure;
    private javax.swing.JButton cmdNextExtend;
    private javax.swing.JButton cmdOpenProject;
    private javax.swing.JButton cmdPan;
    private javax.swing.JButton cmdPresentation;
    private javax.swing.JButton cmdPreviousExtend;
    private javax.swing.JButton cmdPrint;
    private javax.swing.JButton cmdRemoveSelectionAktiveTheme;
    private javax.swing.JButton cmdRemoveSelectionAllThemes;
    private javax.swing.JButton cmdSaveProject;
    private javax.swing.JButton cmdScale;
    private javax.swing.JButton cmdSelectAll;
    private javax.swing.JButton cmdSelectionAttribute;
    private javax.swing.JButton cmdSelectionForm;
    private javax.swing.JButton cmdSelectionLocation;
    private javax.swing.JButton cmdTable;
    private javax.swing.JButton cmdZoomIn;
    private javax.swing.JButton cmdZoomOut;
    private javax.swing.JButton cmdZoomSelectedObjects;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
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
    private javax.swing.JPanel panMain;
    private javax.swing.JToolBar tobDLM25W;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form WatergisApp.
     */
    public WatergisApp() {
        this.addWindowListener(this);

        configManager.addConfigurable(this);
        configManager.configure(this);
        initComponents();
        initCismetCommonsComponents();
        initDefaultPanels();
        initDocking();
        initInfoNode();
        doLayoutInfoNode();
        panMain.add(rootWindow, BorderLayout.CENTER);
        setWindowSize();
    }

    //~ Methods ----------------------------------------------------------------

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
    private static void initLog4J() {
        try {
            PropertyConfigurator.configure(WatergisApp.class.getResource(
                    "/de/cismet/watergis/configuration/log4j.properties"));
            LOG.info("Log4J System was configured successfully");
        } catch (Exception ex) {
            System.err.println("Error during the initialisation");
            ex.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void initDefaultPanels() {
        pMap = new MapPanel();
        pTopicTree = new TopicTreePanel();
        pInfo = new InfoPanel();
        pSelection = new SelectionPanel();
        pTable = new TablePanel();

        AppBroker.getInstance().addComponent(ComponentName.MAP, pMap);
        AppBroker.getInstance().addComponent(ComponentName.TREE, pTopicTree);
        AppBroker.getInstance().addComponent(ComponentName.INFO, pInfo);
        AppBroker.getInstance().addComponent(ComponentName.SELECTION, pSelection);
        AppBroker.getInstance().addComponent(ComponentName.TABLE, pTable);
        LOG.info("set refernence for the main application in Broker: " + this);
        AppBroker.getInstance().addComponent(ComponentName.MAIN, this);
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
    private void initCismetCommonsComponents() {
        mappingComponent = new MappingComponent();
        CismapBroker.getInstance().setMappingComponent(mappingComponent);
        AppBroker.getInstance().setMappingComponent(mappingComponent);
    }

    /**
     * DOCUMENT ME!
     */
    public void doLayoutInfoNode() {
        final TabWindow tab = new TabWindow(new DockingWindow[] { vMap, vInfo, vSelection, vTable });
        rootWindow.setWindow(new SplitWindow(true, 0.22901994f, vTopicTree, tab));
        vMap.restoreFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        tobDLM25W = new javax.swing.JToolBar();
        cmdOpenProject = new javax.swing.JButton();
        cmdSaveProject = new javax.swing.JButton();
        cmdPrint = new javax.swing.JButton();
        cmdExportMap = new javax.swing.JButton();
        cmdAddBookmark = new javax.swing.JButton();
        cmdManageBookmarks = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        cmdScale = new javax.swing.JButton();
        cmdZoomIn = new javax.swing.JButton();
        cmdZoomOut = new javax.swing.JButton();
        cmdPan = new javax.swing.JButton();
        cmdGoTo = new javax.swing.JButton();
        cmdFullExtend = new javax.swing.JButton();
        cmdPreviousExtend = new javax.swing.JButton();
        cmdNextExtend = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
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
        jMenuBar1 = new javax.swing.JMenuBar();
        menFile = new javax.swing.JMenu();
        mniOpenProject = new javax.swing.JMenuItem();
        mniSaveProject = new javax.swing.JMenuItem();
        mniWindow = new javax.swing.JMenuItem();
        mniPrint = new javax.swing.JMenuItem();
        mniExportMap = new javax.swing.JMenuItem();
        mniCreateGeoLink = new javax.swing.JMenuItem();
        mniFileOptions = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
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

        tobDLM25W.setRollover(true);
        tobDLM25W.setMaximumSize(new java.awt.Dimension(679, 32769));
        tobDLM25W.setMinimumSize(new java.awt.Dimension(667, 26));
        tobDLM25W.setPreferredSize(new java.awt.Dimension(691, 28));

        cmdOpenProject.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-folder-open.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdOpenProject,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdOpenProject.text")); // NOI18N
        cmdOpenProject.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdOpenProject.toolTipText"));                                              // NOI18N
        cmdOpenProject.setFocusable(false);
        cmdOpenProject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdOpenProject.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdOpenProject.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdOpenProject.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdOpenProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdOpenProject);

        cmdSaveProject.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-save-floppy.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdSaveProject,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdSaveProject.text")); // NOI18N
        cmdSaveProject.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdSaveProject.toolTipText"));                                              // NOI18N
        cmdSaveProject.setFocusable(false);
        cmdSaveProject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSaveProject.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSaveProject.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSaveProject.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSaveProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSaveProject);

        cmdPrint.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-print.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdPrint,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdPrint.text")); // NOI18N
        cmdPrint.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdPrint.toolTipText"));                                              // NOI18N
        cmdPrint.setFocusable(false);
        cmdPrint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPrint.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdPrint.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdPrint.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdPrint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdPrint);

        cmdExportMap.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-export.png")));           // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdExportMap,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdExportMap.text")); // NOI18N
        cmdExportMap.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdExportMap.toolTipText"));                                              // NOI18N
        cmdExportMap.setFocusable(false);
        cmdExportMap.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdExportMap.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdExportMap.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdExportMap.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdExportMap.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdExportMap);

        cmdAddBookmark.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-circleadd.png")));          // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdAddBookmark,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdAddBookmark.text")); // NOI18N
        cmdAddBookmark.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdAddBookmark.toolTipText"));                                              // NOI18N
        cmdAddBookmark.setFocusable(false);
        cmdAddBookmark.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdAddBookmark.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdAddBookmark.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdAddBookmark.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdAddBookmark.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdAddBookmark);

        cmdManageBookmarks.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-bookmark.png")));               // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdManageBookmarks,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdManageBookmarks.text")); // NOI18N
        cmdManageBookmarks.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdManageBookmarks.toolTipText"));                                              // NOI18N
        cmdManageBookmarks.setFocusable(false);
        cmdManageBookmarks.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdManageBookmarks.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdManageBookmarks.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdManageBookmarks.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdManageBookmarks.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdManageBookmarks);

        jSeparator4.setSeparatorSize(new java.awt.Dimension(2, 0));
        tobDLM25W.add(jSeparator4);

        cmdScale.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-ruler.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdScale,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdScale.text")); // NOI18N
        cmdScale.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdScale.toolTipText"));                                              // NOI18N
        cmdScale.setFocusable(false);
        cmdScale.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdScale.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdScale.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdScale.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdScale.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdScale);

        cmdZoomIn.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-zoom-in.png")));       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdZoomIn,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdZoomIn.text")); // NOI18N
        cmdZoomIn.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdZoomIn.toolTipText"));                                              // NOI18N
        cmdZoomIn.setFocusable(false);
        cmdZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomIn.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomIn.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomIn.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdZoomIn);

        cmdZoomOut.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-zoom-out.png")));       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdZoomOut,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdZoomOut.text")); // NOI18N
        cmdZoomOut.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdZoomOut.toolTipText"));                                              // NOI18N
        cmdZoomOut.setFocusable(false);
        cmdZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomOut.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomOut.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomOut.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdZoomOut);

        cmdPan.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-move.png")));       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdPan,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdPan.text")); // NOI18N
        cmdPan.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdPan.toolTipText"));                                              // NOI18N
        cmdPan.setFocusable(false);
        cmdPan.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPan.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdPan.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdPan.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdPan.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdPan);

        cmdGoTo.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-map-marker.png")));  // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdGoTo,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdGoTo.text")); // NOI18N
        cmdGoTo.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdGoTo.toolTipText"));                                              // NOI18N
        cmdGoTo.setFocusable(false);
        cmdGoTo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdGoTo.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdGoTo.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdGoTo.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdGoTo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdGoTo);

        cmdFullExtend.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-fullscreen.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdFullExtend,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdFullExtend.text")); // NOI18N
        cmdFullExtend.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdFullExtend.toolTipText"));                                              // NOI18N
        cmdFullExtend.setFocusable(false);
        cmdFullExtend.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdFullExtend.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdFullExtend.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdFullExtend.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdFullExtend.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdFullExtend);

        cmdPreviousExtend.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-arrow-left.png")));            // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdPreviousExtend,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdPreviousExtend.text")); // NOI18N
        cmdPreviousExtend.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdPreviousExtend.toolTipText"));                                              // NOI18N
        cmdPreviousExtend.setFocusable(false);
        cmdPreviousExtend.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPreviousExtend.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdPreviousExtend.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdPreviousExtend.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdPreviousExtend.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdPreviousExtend);

        cmdNextExtend.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-arrow-right.png")));       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdNextExtend,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdNextExtend.text")); // NOI18N
        cmdNextExtend.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdNextExtend.toolTipText"));                                              // NOI18N
        cmdNextExtend.setFocusable(false);
        cmdNextExtend.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNextExtend.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdNextExtend.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdNextExtend.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdNextExtend.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdNextExtend);

        jSeparator5.setSeparatorSize(new java.awt.Dimension(2, 0));
        tobDLM25W.add(jSeparator5);

        cmdSelectionForm.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionadd.png")));         // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdSelectionForm,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdSelectionForm.text")); // NOI18N
        cmdSelectionForm.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdSelectionForm.toolTipText"));                                              // NOI18N
        cmdSelectionForm.setFocusable(false);
        cmdSelectionForm.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectionForm.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectionForm.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectionForm.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectionForm.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSelectionForm);

        cmdSelectionAttribute.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionadd.png")));              // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdSelectionAttribute,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdSelectionAttribute.text")); // NOI18N
        cmdSelectionAttribute.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdSelectionAttribute.toolTipText"));                                              // NOI18N
        cmdSelectionAttribute.setFocusable(false);
        cmdSelectionAttribute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectionAttribute.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectionAttribute.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectionAttribute.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectionAttribute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSelectionAttribute);

        cmdSelectionLocation.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionadd.png")));             // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdSelectionLocation,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdSelectionLocation.text")); // NOI18N
        cmdSelectionLocation.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdSelectionLocation.toolTipText"));                                              // NOI18N
        cmdSelectionLocation.setFocusable(false);
        cmdSelectionLocation.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectionLocation.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectionLocation.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectionLocation.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectionLocation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSelectionLocation);

        cmdZoomSelectedObjects.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-resize.png")));                     // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdZoomSelectedObjects,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdZoomSelectedObjects.text")); // NOI18N
        cmdZoomSelectedObjects.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdZoomSelectedObjects.toolTipText"));                                              // NOI18N
        cmdZoomSelectedObjects.setFocusable(false);
        cmdZoomSelectedObjects.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomSelectedObjects.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedObjects.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedObjects.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedObjects.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdZoomSelectedObjects);

        cmdSelectAll.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-newwindow.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdSelectAll,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdSelectAll.text")); // NOI18N
        cmdSelectAll.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdSelectAll.toolTipText"));                                              // NOI18N
        cmdSelectAll.setFocusable(false);
        cmdSelectAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectAll.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectAll.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectAll.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSelectAll);

        cmdInvertSelection.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selection-rectangleselection.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdInvertSelection,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdInvertSelection.text"));       // NOI18N
        cmdInvertSelection.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdInvertSelection.toolTipText"));                                                    // NOI18N
        cmdInvertSelection.setFocusable(false);
        cmdInvertSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdInvertSelection.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdInvertSelection.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdInvertSelection.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdInvertSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdInvertSelection);

        cmdRemoveSelectionAktiveTheme.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionremove.png")));                   // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdRemoveSelectionAktiveTheme,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdRemoveSelectionAktiveTheme.text")); // NOI18N
        cmdRemoveSelectionAktiveTheme.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdRemoveSelectionAktiveTheme.toolTipText"));                                              // NOI18N
        cmdRemoveSelectionAktiveTheme.setFocusable(false);
        cmdRemoveSelectionAktiveTheme.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRemoveSelectionAktiveTheme.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAktiveTheme.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAktiveTheme.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAktiveTheme.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdRemoveSelectionAktiveTheme);

        cmdRemoveSelectionAllThemes.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionremove.png")));                 // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdRemoveSelectionAllThemes,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdRemoveSelectionAllThemes.text")); // NOI18N
        cmdRemoveSelectionAllThemes.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdRemoveSelectionAllThemes.toolTipText"));                                              // NOI18N
        cmdRemoveSelectionAllThemes.setFocusable(false);
        cmdRemoveSelectionAllThemes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRemoveSelectionAllThemes.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAllThemes.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAllThemes.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAllThemes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdRemoveSelectionAllThemes);

        jSeparator6.setSeparatorSize(new java.awt.Dimension(2, 0));
        tobDLM25W.add(jSeparator6);

        cmdTable.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-spreadsheet.png")));  // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdTable,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdTable.text")); // NOI18N
        cmdTable.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdTable.toolTipText"));                                              // NOI18N
        cmdTable.setFocusable(false);
        cmdTable.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdTable.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdTable.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdTable.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdTable.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdTable);

        cmdInfo.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-info-sign.png")));   // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdInfo,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdInfo.text")); // NOI18N
        cmdInfo.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdInfo.toolTipText"));                                              // NOI18N
        cmdInfo.setFocusable(false);
        cmdInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdInfo.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdInfo.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdInfo.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdInfo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdInfo);

        cmdMeasure.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-ruler.png")));          // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdMeasure,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdMeasure.text")); // NOI18N
        cmdMeasure.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdMeasure.toolTipText"));                                              // NOI18N
        cmdMeasure.setFocusable(false);
        cmdMeasure.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdMeasure.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdMeasure.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdMeasure.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdMeasure.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdMeasure);

        cmdPresentation.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-eye-view.png")));            // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            cmdPresentation,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.cmdPresentation.text")); // NOI18N
        cmdPresentation.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdPresentation.toolTipText"));                                              // NOI18N
        cmdPresentation.setFocusable(false);
        cmdPresentation.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPresentation.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdPresentation);

        getContentPane().add(tobDLM25W, java.awt.BorderLayout.NORTH);

        panMain.setLayout(new java.awt.BorderLayout());
        getContentPane().add(panMain, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(
            menFile,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menFile.text")); // NOI18N

        mniOpenProject.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-folder-open.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniOpenProject,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniOpenProject.text")); // NOI18N
        menFile.add(mniOpenProject);

        mniSaveProject.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-save-floppy.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSaveProject,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniSaveProject.text")); // NOI18N
        menFile.add(mniSaveProject);

        mniWindow.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-newwindow.png")));     // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniWindow,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniWindow.text")); // NOI18N
        menFile.add(mniWindow);

        mniPrint.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-print.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniPrint,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniPrint.text")); // NOI18N
        menFile.add(mniPrint);

        mniExportMap.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-export.png")));           // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniExportMap,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniExportMap.text")); // NOI18N
        menFile.add(mniExportMap);

        mniCreateGeoLink.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-link.png")));                 // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniCreateGeoLink,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniCreateGeoLink.text")); // NOI18N
        menFile.add(mniCreateGeoLink);

        mniFileOptions.setAction(new OptionsAction());
        mniFileOptions.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-settingsandroid.png")));    // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniFileOptions,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniFileOptions.text")); // NOI18N
        menFile.add(mniFileOptions);
        menFile.add(jSeparator1);

        jMenuItem8.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-explorerwindow.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jMenuItem8,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.jMenuItem8.text")); // NOI18N
        menFile.add(jMenuItem8);

        jMenuItem11.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-explorerwindow.png")));  // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jMenuItem11,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.jMenuItem11.text")); // NOI18N
        menFile.add(jMenuItem11);
        menFile.add(jSeparator2);

        jMenuItem9.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-explorerwindow.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jMenuItem9,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.jMenuItem9.text")); // NOI18N
        menFile.add(jMenuItem9);

        jMenuItem12.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-explorerwindow.png")));  // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jMenuItem12,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.jMenuItem12.text")); // NOI18N
        menFile.add(jMenuItem12);
        menFile.add(jSeparator3);

        mniClose.setAction(new CloseAction());
        mniClose.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-programclose.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniClose,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniClose.text")); // NOI18N
        menFile.add(mniClose);

        jMenuBar1.add(menFile);

        org.openide.awt.Mnemonics.setLocalizedText(
            menBookmark,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menBookmark.text")); // NOI18N

        mniCreateBookmark.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-circleadd.png")));             // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniCreateBookmark,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniCreateBookmark.text")); // NOI18N
        menBookmark.add(mniCreateBookmark);

        mniManageBookmarks.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-bookmark.png")));               // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniManageBookmarks,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniManageBookmarks.text")); // NOI18N
        menBookmark.add(mniManageBookmarks);

        jMenuBar1.add(menBookmark);

        org.openide.awt.Mnemonics.setLocalizedText(
            menSelection,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menSelection.text")); // NOI18N

        mniSelectForm.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionadd.png")));      // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectForm,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniSelectForm.text")); // NOI18N
        menSelection.add(mniSelectForm);

        mniSelectAttribute.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionadd.png")));           // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectAttribute,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniSelectAttribute.text")); // NOI18N
        menSelection.add(mniSelectAttribute);

        mniSelectLocation.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionadd.png")));          // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniSelectLocation,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniSelectLocation.text")); // NOI18N
        menSelection.add(mniSelectLocation);

        mniZoomSelectedObjects.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-resize.png")));                     // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniZoomSelectedObjects,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniZoomSelectedObjects.text")); // NOI18N
        menSelection.add(mniZoomSelectedObjects);

        mniRemoveSelection.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionremove.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniRemoveSelection,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniRemoveSelection.text")); // NOI18N
        menSelection.add(mniRemoveSelection);

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

        mniHelp.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-question-sign.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniHelp,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniHelp.text"));   // NOI18N
        menHelp.add(mniHelp);

        mniInfo.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-info-sign.png")));   // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniInfo,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniInfo.text")); // NOI18N
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
                        SPLASH = StaticStartupTools.showGhostFrame(FILEPATH_SCREEN, "FIS Gewässer [Startup]");
                        SPLASH.setLocationRelativeTo(null);
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
                        if (SPLASH != null) {
                            SPLASH.dispose();
                        }
                        SPLASH = null;
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
//        try {
//            // ToDo if it fails all fail better place in the single try catch
//            final Element urls = parent.getChild("urls");
//            final Element albConfiguration = parent.getChild("albConfiguration");
//            try {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("OnlineHilfeUrl: " + urls.getChildText("onlineHelp"));
//                }
//                onlineHelpURL = urls.getChildText("onlineHelp");
//            } catch (Exception ex) {
//                LOG.warn("Fehler beim lesen der OnlineHilfe URL", ex);
//            }
//            try {
//                albURL = albConfiguration.getChildText("albURL");
//                if (albURL != null) {
//                    albURL = albURL.trim();
//                }
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("ALBURL: " + albURL.trim());
//                }
//            } catch (Exception ex) {
//                LOG.warn("Fehler beim lesen der ALB Konfiguration", ex);
//            }
//            try {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("News Url: " + urls.getChildText("onlineHelp"));
//                }
//                newsURL = urls.getChildText("news");
//            } catch (Exception ex) {
//                LOG.warn("Fehler beim lesen der News Url", ex);
//            }
//
//        } catch (Exception ex) {
//            LOG.error("Fehler beim konfigurieren der Watergis Applikation: ", ex);
//        }
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
}
