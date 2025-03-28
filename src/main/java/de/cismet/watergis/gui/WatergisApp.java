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
import Sirius.navigator.types.treenode.PureTreeNode;
import Sirius.navigator.types.treenode.RootTreeNode;
import Sirius.navigator.ui.dialog.LoginDialog;
import Sirius.navigator.ui.tree.MetaCatalogueTree;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
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
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.util.Direction;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import org.jdom.Element;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.reflect.InvocationTargetException;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.cismet.cids.custom.watergis.server.search.CheckForCreatedObjectTable;
import de.cismet.cids.custom.watergis.server.search.CleanupCreatedObjectTable;
import de.cismet.cids.custom.watergis.server.search.RemoveOldLocks;
import de.cismet.cids.custom.watergis.server.search.RemoveUnnusedRoute;
import de.cismet.cids.custom.watergis.server.search.ValidLawaCodes;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;
import de.cismet.cids.navigator.utils.SimpleMemoryMonitoringToolbarWidget;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.DrawingManager;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.DrawingSLDStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.WFSCapabilitiesTreeCellRenderer;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableListener;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.infowidgets.Legend;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.LayerDropUtils;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerEvent;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerListener;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerMenuItem;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToFeaturesWorker;
import de.cismet.cismap.commons.gui.options.CapabilityWidgetOptionsPanel;
import de.cismet.cismap.commons.gui.overviewwidget.OverviewComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.MapDnDListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.MapDnDEvent;
import de.cismet.cismap.commons.interaction.memento.MementoInterface;
import de.cismet.cismap.commons.rasterservice.ImageFileMetaData;
import de.cismet.cismap.commons.rasterservice.ImageFileUtils;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.tools.ExportCsvDownload;
import de.cismet.cismap.commons.tools.ExportDbfDownload;
import de.cismet.cismap.commons.tools.ExportDownload;
import de.cismet.cismap.commons.tools.ExportShapeDownload;
import de.cismet.cismap.commons.tools.ExportTxtDownload;
import de.cismet.cismap.commons.tools.PointReferencingDialog;
import de.cismet.cismap.commons.util.DnDUtils;
import de.cismet.cismap.commons.util.SelectionChangedEvent;
import de.cismet.cismap.commons.util.SelectionChangedListener;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.cismap.linearreferencing.CreateLinearReferencedLineListener;
import de.cismet.cismap.linearreferencing.CreateLinearReferencedPointListener;

import de.cismet.commons.concurrency.CismetConcurrency;
import de.cismet.commons.concurrency.CismetExecutors;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.lookupoptions.gui.OptionsClient;

import de.cismet.netutil.Proxy;
import de.cismet.netutil.ProxyHandler;

import de.cismet.tools.StaticDebuggingTools;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;
import de.cismet.tools.gui.downloadmanager.Download;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.historybutton.JHistoryButton;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
import de.cismet.tools.gui.startup.StaticStartupTools;

import de.cismet.veto.VetoException;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;
import de.cismet.watergis.broker.listener.DeleteFeatureListener;
import de.cismet.watergis.broker.listener.DrawingCountChangedEvent;
import de.cismet.watergis.broker.listener.DrawingsListener;
import de.cismet.watergis.broker.listener.PolygonChangeListener;

import de.cismet.watergis.check.BackgroundCheckEvent;
import de.cismet.watergis.check.BackgroundCheckListener;
import de.cismet.watergis.check.CheckBackgroundWorker;

import de.cismet.watergis.gui.actions.AddThemeAction;
import de.cismet.watergis.gui.actions.AnnexAction;
import de.cismet.watergis.gui.actions.InfoWindowAction;
import de.cismet.watergis.gui.actions.ShowWindowAction;
import de.cismet.watergis.gui.actions.checks.AbstractCheckResult;
import de.cismet.watergis.gui.actions.map.RemoveDrawingModeAction;
import de.cismet.watergis.gui.actions.reports.template.TemplateExportAction;
import de.cismet.watergis.gui.components.GeometryOpButton;
import de.cismet.watergis.gui.components.MeasureButton;
import de.cismet.watergis.gui.components.NewDrawingButton;
import de.cismet.watergis.gui.components.ScaleJComboBox;
import de.cismet.watergis.gui.components.SelectionButton;
import de.cismet.watergis.gui.components.SnappingButton;
import de.cismet.watergis.gui.dialog.ThemeExportDialog;
import de.cismet.watergis.gui.panels.GafProf;
import de.cismet.watergis.gui.panels.InfoPanel;
import de.cismet.watergis.gui.panels.MapPanel;
import de.cismet.watergis.gui.panels.Photo;
import de.cismet.watergis.gui.panels.RouteZoomPanel;
import de.cismet.watergis.gui.panels.SelectionPanel;
import de.cismet.watergis.gui.panels.StatusPanel;
import de.cismet.watergis.gui.panels.ZoomPanel;
import de.cismet.watergis.gui.recently_opened_files.FileMenu;
import de.cismet.watergis.gui.recently_opened_files.RecentlyOpenedFilesList;

import de.cismet.watergis.server.GeoLinkServer;

import de.cismet.watergis.utils.BookmarkManager;
import de.cismet.watergis.utils.FeatureServiceHelper;
import de.cismet.watergis.utils.WatergisTreeNodeVisualizationService;

import static java.awt.Frame.MAXIMIZED_BOTH;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class WatergisApp extends javax.swing.JFrame implements Configurable,
    WindowListener,
    Observer,
    FeatureCollectionListener,
    ThemeLayerListener,
    MapDnDListener,
    SelectionChangedListener {

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
    private static final String CONNECTION_PROPERTIES_FILE = "/serverConnection.properties";
    private static final ConnectionContext connectionContext = ConnectionContext.create(
            AbstractConnectionContext.Category.STARTUP,
            "WatergisApp");

    static {
        configManager.setDefaultFileName(FILENAME_WATERGIS_CONFIGURATION);
        configManager.setFileName(FILENAME_LOCAL_WATERGIS_CONFIGURATION);
        configManager.setClassPathFolder(CLASSPATH_WATERGIS_CONFIGURATION);
        configManager.setFolder(DIRECTORYNAME_WATERGISHOME);
    }

    //~ Instance fields --------------------------------------------------------

    DataFlavor CAPABILITY_WIDGET_FLAVOR = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType,
            "SelectionAndCapabilities"); // NOI18N

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
    private Photo pPhoto;
    private GafProf pGaf;
    private ThemeLayerWidget pTopicTree;
    private InfoPanel pInfo;
    private SelectionPanel pSelection;
    private MetaCatalogueTree pTable;
    private JScrollPane pTablePane = new JScrollPane();
    private OverviewComponent pOverview;
    private CapabilityWidget pCapabilities;
    private Legend pLegend;
    // Views
    private View vMap;
    private View vPhoto;
    private View vGaf;
    private View vTopicTree;
    private View vTable;
    private View vOverview;
    private View vCapability;
    private View vLegend;
    private MappingComponent mappingComponent;
    private ActiveLayerModel mappingModel = new ActiveLayerModel();
    private MetaClass routeMc;
    private MetaClass wwGrMc;
    private List<TemplateExportAction> allTemplates = new ArrayList<TemplateExportAction>();
    private AddThemeAction addThemeTreeAction;

    private String helpURL;
    private String infoURL;
    private boolean isInit = true;
    private Executor watergisSingleThreadExecutor = CismetExecutors.newSingleThreadExecutor();
    private String lastExportPath = null;
    private String currentLayoutFile = null;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cismet.watergis.gui.actions.AddThemeAction addThemeAction1;
    private de.cismet.watergis.gui.actions.AnnexAction annexAction;
    private de.cismet.watergis.gui.actions.checks.AusbauCheckAction ausbauCheckAction;
    private de.cismet.watergis.gui.actions.checks.BasicRoutesCheckAction basicRoutesCheckAction1;
    private de.cismet.watergis.gui.actions.checks.BauwerkeCheckAction bauwerkeCheckAction;
    private javax.swing.ButtonGroup btnGroupMapMode;
    private de.cismet.watergis.gui.actions.geoprocessing.BufferGeoprocessingAction bufferGeoprocessingAction;
    private javax.swing.JButton butIntermediateSave;
    private javax.swing.JComboBox cboScale;
    private javax.swing.JComboBox cboTheme;
    private de.cismet.watergis.gui.actions.CentralConfigAction centralConfigAction;
    private de.cismet.watergis.gui.actions.map.ChangePolygonModeAction changePolygonModeAction1;
    private de.cismet.watergis.gui.actions.gaf.CheckAction checkAction1;
    private de.cismet.watergis.gui.actions.geoprocessing.ClipGeoprocessingAction clipGeoprocessingAction;
    private de.cismet.watergis.gui.actions.CloseAction closeAction;
    private javax.swing.JButton cmdAddBookmark;
    private javax.swing.JButton cmdAnnex;
    private javax.swing.JButton cmdCopy;
    private javax.swing.JButton cmdDelete;
    private javax.swing.JButton cmdDownloadManager;
    private javax.swing.JButton cmdDownloadManager1;
    private javax.swing.JButton cmdDrawingMode;
    private javax.swing.JButton cmdDrawingOptions;
    private javax.swing.JButton cmdExportMap1;
    private javax.swing.JButton cmdExportMap2;
    private javax.swing.JButton cmdFreeStat;
    private javax.swing.JButton cmdFullExtend;
    private javax.swing.JButton cmdGeoLink;
    private javax.swing.JButton cmdGeometryOpMode;
    private javax.swing.JButton cmdGoTo;
    private javax.swing.JButton cmdInvertSelection;
    private javax.swing.JButton cmdLoadDrawings;
    private javax.swing.JButton cmdManageBookmarks;
    private javax.swing.JButton cmdMerge;
    private javax.swing.JToggleButton cmdMoveGeometry;
    private javax.swing.JToggleButton cmdNewLinestring;
    private javax.swing.JToggleButton cmdNewPoint;
    private javax.swing.JToggleButton cmdNewPolygon;
    private javax.swing.JToggleButton cmdNewText;
    private javax.swing.JButton cmdNextExtend;
    private javax.swing.JToggleButton cmdNodeAdd;
    private javax.swing.JToggleButton cmdNodeMove;
    private javax.swing.JToggleButton cmdNodeRemove;
    private javax.swing.JButton cmdOnlineHelp;
    private javax.swing.JButton cmdOpenProject;
    private javax.swing.JButton cmdPaste;
    private javax.swing.JButton cmdPresentation;
    private javax.swing.JButton cmdPreviousExtend;
    private javax.swing.JButton cmdPrint;
    private javax.swing.JButton cmdRefresh;
    private javax.swing.JButton cmdRelease;
    private javax.swing.JButton cmdRemoveGeometry;
    private javax.swing.JButton cmdRemoveSelectionAllThemes;
    private javax.swing.JButton cmdSaveDrawings;
    private javax.swing.JButton cmdSaveProject;
    private javax.swing.JButton cmdSaveSameFileProject;
    private javax.swing.JButton cmdSelectAll;
    private javax.swing.JButton cmdSelectAllDrawings;
    private javax.swing.JButton cmdSelectionAttribute;
    private javax.swing.JButton cmdSelectionLocation;
    private javax.swing.JButton cmdSelectionMode;
    private javax.swing.JButton cmdSnappingMode;
    private javax.swing.JButton cmdSplit;
    private javax.swing.JButton cmdUndo;
    private javax.swing.JButton cmdUnselectDrawings;
    private javax.swing.JButton cmdZoomIn;
    private javax.swing.JButton cmdZoomOut;
    private javax.swing.JButton cmdZoomSelectedObjects;
    private javax.swing.JButton cmdZoomSelectedThemes;
    private javax.swing.JButton cmdZoomToAllDrawings;
    private javax.swing.JButton cmdZoomToSelectedDrawings;
    private de.cismet.watergis.gui.actions.map.CopyObjectAction copyObjectAction1;
    private de.cismet.watergis.gui.actions.map.CreateGeoLinkAction createGeoLinkAction;
    private de.cismet.watergis.gui.actions.CreateViewsForUser createViewsForUser1;
    private de.cismet.watergis.gui.actions.DefaultConfig2Action defaultConfig2Action1;
    private de.cismet.watergis.gui.actions.DefaultConfigAction defaultConfigAction1;
    private de.cismet.watergis.gui.actions.foto.DeleteAction deleteAction1;
    private de.cismet.watergis.gui.actions.gaf.DeleteAction deleteActionGaf;
    private de.cismet.watergis.gui.actions.map.DeleteObjectAction deleteObjectAction1;
    private de.cismet.watergis.gui.actions.geoprocessing.DissolveGeoprocessingAction dissolveGeoprocessingAction;
    private de.cismet.watergis.gui.actions.DownloadManagerAction downloadManagerAction;
    private javax.swing.ButtonGroup drawingGroup;
    private javax.swing.ButtonGroup editGroup;
    private de.cismet.watergis.gui.actions.ExportAction exportAction1;
    private de.cismet.watergis.gui.actions.gaf.ExportAction exportActionGaf;
    private de.cismet.watergis.gui.actions.foto.ExportAction exportActionPhoto;
    private de.cismet.watergis.gui.actions.ExportIgmAction exportIgmAction;
    private de.cismet.watergis.gui.actions.map.ExportMapAction exportMapAction;
    private de.cismet.watergis.gui.actions.map.ExportMapToFileAction exportMapToFileAction;
    private de.cismet.watergis.gui.actions.ExportOptionAction exportOptionAction;
    private de.cismet.watergis.gui.actions.reports.KatasterFlaechenReportAction flaechenReportAction1;
    private de.cismet.watergis.gui.actions.map.FlipAction flipAction;
    private de.cismet.watergis.gui.actions.foto.FotoInfoAction fotoInfoAction1;
    private de.cismet.watergis.gui.actions.map.FullExtendAction fullExtendAction;
    private de.cismet.watergis.gui.actions.checks.GWKConnectionCheckAction gWKConnectionCheckAction;
    private de.cismet.watergis.gui.actions.gaf.GafInfoAction gafInfoAction;
    private de.cismet.watergis.gui.actions.reports.KatasterGemeindenReportAction gemeindenReportAction;
    private de.cismet.watergis.gui.actions.reports.GerinneGFlReportAction gerinneGFlReportAction1;
    private de.cismet.watergis.gui.actions.reports.GerinneGGewaesserReportAction gerinneGGewaesserReportAction1;
    private de.cismet.watergis.gui.actions.reports.GerinneGSbReportAction gerinneGSbReportAction1;
    private de.cismet.watergis.gui.actions.reports.GerinneOFlReportAction gerinneOFlReportAction1;
    private de.cismet.watergis.gui.actions.reports.GerinneOGemeindeReportAction gerinneOGemeindeReportAction;
    private de.cismet.watergis.gui.actions.reports.GerinneOGewReportAction gerinneOGewReportAction;
    private de.cismet.watergis.gui.actions.reports.GerinneOSbReportAction gerinneOSbReportAction;
    private de.cismet.watergis.gui.actions.reports.GerogaRsAction gerogaRsAction1;
    private de.cismet.watergis.gui.actions.reports.KatasterGewaesserReportAction gewaesserGewReportAction;
    private de.cismet.watergis.gui.actions.reports.GewaesserReportAction gewaesserReportAction;
    private de.cismet.watergis.gui.actions.map.GoToAction goToAction;
    private de.cismet.watergis.gui.actions.InfoAction infoAction;
    private de.cismet.watergis.gui.actions.InfoWindowAction infoWindowAction;
    private de.cismet.watergis.gui.actions.IntermediateSaveAction intermediateSaveAction;
    private de.cismet.watergis.gui.actions.selection.InvertSelectionAction invertSelectionAction;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator14;
    private javax.swing.JToolBar.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private de.cismet.watergis.gui.actions.checks.LawaCheckAction lawaCheckAction;
    private javax.swing.JLabel lblPlaceholder;
    private de.cismet.watergis.gui.actions.map.LoadDrawingsAction loadDrawingsAction;
    private de.cismet.watergis.gui.actions.LocalConfigAction localConfigAction;
    private de.cismet.watergis.gui.actions.map.MeasureAction measureAction;
    private de.cismet.watergis.gui.actions.map.MeasureLineAction measureLineAction;
    private javax.swing.JMenu menBasicChecks;
    private javax.swing.JMenu menBookmark;
    private javax.swing.JMenu menChecks;
    private javax.swing.JMenu menChecks1;
    private javax.swing.JMenu menDrawings;
    private javax.swing.JMenu menExtendedChecks;
    private javax.swing.JMenu menFile;
    private javax.swing.JMenu menGeoProcessing;
    private javax.swing.JMenu menGewaesser;
    private javax.swing.JMenu menGewaesser1;
    private javax.swing.JMenu menGewaesser2;
    private javax.swing.JMenu menGewaesser3;
    private javax.swing.JMenu menHelp;
    private javax.swing.JMenu menPhoto;
    private javax.swing.JMenu menProfiles;
    private javax.swing.JMenu menReport;
    private javax.swing.JMenu menSchablonen;
    private javax.swing.JMenu menSelection;
    private javax.swing.JMenu menStatistik;
    private javax.swing.JMenu menSteckbrief;
    private javax.swing.JMenu menSteckbriefGewaesser;
    private javax.swing.JMenu menSteckbriefPhotos;
    private javax.swing.JMenu menSteckbriefQp;
    private javax.swing.JMenu menSteckbriefWasserkoerper;
    private javax.swing.JMenu menTools;
    private javax.swing.JMenu menWindow;
    private javax.swing.JMenu menWsg;
    private de.cismet.watergis.gui.actions.MergeAction mergeAction;
    private de.cismet.watergis.gui.actions.geoprocessing.MergeGeoprocessingAction mergeGeoprocessingAction;
    private javax.swing.JMenuItem mniBuffer;
    private javax.swing.JMenuItem mniCheckAusbau;
    private javax.swing.JMenuItem mniCheckBasisRoutes;
    private javax.swing.JMenuItem mniCheckBauwerke;
    private javax.swing.JMenuItem mniCheckLawa;
    private javax.swing.JMenuItem mniCheckLawaConnection;
    private javax.swing.JMenuItem mniCheckSonstige;
    private javax.swing.JMenuItem mniCheckVerwaltung;
    private javax.swing.JMenuItem mniClip;
    private javax.swing.JMenuItem mniClose;
    private javax.swing.JMenuItem mniCreateBookmark;
    private javax.swing.JMenuItem mniCreateDbUser;
    private javax.swing.JMenuItem mniCreateGeoLink;
    private javax.swing.JMenuItem mniDefaultConfig;
    private javax.swing.JMenuItem mniDeleteGaf;
    private javax.swing.JMenuItem mniDeletePhoto;
    private javax.swing.JMenuItem mniDissolve;
    private javax.swing.JMenuItem mniDownloadManager;
    private javax.swing.JMenuItem mniDrawingOptions;
    private javax.swing.JMenuItem mniExport;
    private javax.swing.JMenuItem mniExportGaf;
    private javax.swing.JMenuItem mniExportMap;
    private javax.swing.JMenuItem mniExportOption;
    private javax.swing.JMenuItem mniExportPhoto;
    private javax.swing.JMenuItem mniFG;
    private javax.swing.JMenuItem mniFileOptions;
    private javax.swing.JMenuItem mniFl;
    private javax.swing.JMenuItem mniFl1;
    private javax.swing.JMenuItem mniFl2;
    private javax.swing.JMenuItem mniFlNu;
    private javax.swing.JMenuItem mniGafInfo;
    private javax.swing.JMenuItem mniGafOptions;
    private javax.swing.JMenuItem mniGafUpload;
    private javax.swing.JMenuItem mniGemeinde;
    private javax.swing.JMenuItem mniGemeinde1;
    private javax.swing.JMenuItem mniGemeinde2;
    private javax.swing.JMenuItem mniGewaesser;
    private javax.swing.JMenuItem mniGewaesserRep;
    private javax.swing.JMenuItem mniGewaesserRep1;
    private javax.swing.JMenuItem mniGewaesserRep2;
    private javax.swing.JMenuItem mniGewaesserRep3;
    private javax.swing.JMenuItem mniHelp;
    private javax.swing.JMenuItem mniIgmExport;
    private javax.swing.JMenuItem mniInfo;
    private javax.swing.JMenuItem mniLoadDrawings;
    private javax.swing.JMenuItem mniManageBookmarks;
    private javax.swing.JMenuItem mniMerge;
    private javax.swing.JMenuItem mniNewLineStringDrawing;
    private javax.swing.JMenuItem mniNewMarkerDrawing;
    private javax.swing.JMenuItem mniNewRectangleDrawing;
    private javax.swing.JMenuItem mniNewTextDrawing;
    private javax.swing.JMenuItem mniOpenProject;
    private javax.swing.JMenuItem mniOverview;
    private javax.swing.JMenuItem mniPhotoInfo;
    private javax.swing.JMenuItem mniPhotoOptions;
    private javax.swing.JMenuItem mniPointInLine;
    private javax.swing.JMenuItem mniPointInPolygon;
    private javax.swing.JMenuItem mniPrint;
    private javax.swing.JMenuItem mniPrintPhoto;
    private javax.swing.JMenuItem mniPrintQp;
    private javax.swing.JMenuItem mniRechtsgrundlage;
    private javax.swing.JMenuItem mniRechtsgrundlage1;
    private javax.swing.JMenuItem mniRefreshDbUser;
    private javax.swing.JMenuItem mniRemoveDrawing;
    private javax.swing.JMenuItem mniRemoveSelection;
    private javax.swing.JMenuItem mniReportGaf;
    private javax.swing.JMenuItem mniReportPhoto;
    private javax.swing.JMenuItem mniSaveDrawings;
    private javax.swing.JMenuItem mniSaveMapToFile;
    private javax.swing.JMenuItem mniSaveProject;
    private javax.swing.JMenuItem mniSaveProject1;
    private javax.swing.JMenuItem mniSb;
    private javax.swing.JMenuItem mniSb1;
    private javax.swing.JMenuItem mniSb2;
    private javax.swing.JMenuItem mniSelectAllDrawing;
    private javax.swing.JMenuItem mniSelectAttribute;
    private javax.swing.JMenuItem mniSelectEllipse;
    private javax.swing.JMenuItem mniSelectLocation;
    private javax.swing.JMenuItem mniSelectPolygon;
    private javax.swing.JMenuItem mniSelectRectangle;
    private javax.swing.JMenuItem mniShowDatasource;
    private javax.swing.JMenuItem mniShowDefaultConfig2;
    private javax.swing.JMenuItem mniShowInfo;
    private javax.swing.JMenuItem mniShowLegend;
    private javax.swing.JMenuItem mniShowMap;
    private javax.swing.JMenuItem mniShowOverview;
    private javax.swing.JMenuItem mniShowPhotos;
    private javax.swing.JMenuItem mniShowProblems;
    private javax.swing.JMenuItem mniShowProfiles;
    private javax.swing.JMenuItem mniShowTree;
    private javax.swing.JMenuItem mniStation;
    private javax.swing.JMenuItem mniTechProf;
    private javax.swing.JMenuItem mniUnion;
    private javax.swing.JMenuItem mniUnselectAllDrawing;
    private javax.swing.JMenuItem mniUpload;
    private javax.swing.JMenuItem mniZoomAllDrawings;
    private javax.swing.JMenuItem mniZoomSelectedDrawings;
    private javax.swing.JMenuItem mniZoomSelectedObjects;
    private javax.swing.JMenuItem mniZoomSelectedThemes;
    private de.cismet.watergis.gui.actions.map.MoveModeAction moveModeAction;
    private de.cismet.watergis.gui.actions.map.NewLinestringModeAction newLinestringModeAction;
    private de.cismet.watergis.gui.actions.map.NewMarkerModeAction newMarkerModeAction;
    private de.cismet.watergis.gui.actions.NewObjectAction newObjectAction;
    private de.cismet.watergis.gui.actions.map.NewRectangleModeAction newRectangleModeAction;
    private de.cismet.watergis.gui.actions.map.NewTextModeAction newTextModeAction;
    private de.cismet.watergis.gui.actions.map.NextExtendAction nextExtendAction;
    private de.cismet.watergis.gui.actions.OnlineHelpAction onlineHelpAction;
    private de.cismet.watergis.gui.actions.OpenProjectAction openProjectAction;
    private de.cismet.watergis.gui.actions.foto.OptionAction optionAction1;
    private de.cismet.watergis.gui.actions.gaf.OptionAction optionActionGaf;
    private de.cismet.watergis.gui.actions.map.OptionModeAction optionModeAction1;
    private de.cismet.watergis.gui.actions.OptionsAction optionsAction;
    private de.cismet.watergis.gui.actions.map.PanModeAction panAction;
    private javax.swing.JPanel panMain;
    private javax.swing.JPanel panSearch;
    private de.cismet.watergis.gui.actions.map.PasteObjectAction pasteObjectAction1;
    private de.cismet.watergis.gui.actions.geoprocessing.PointInLineGeoprocessingAction pointInLineGeoprocessingAction;
    private de.cismet.watergis.gui.actions.geoprocessing.PointInPolygonGeoprocessingAction
        pointInPolygonGeoprocessingAction;
    private de.cismet.watergis.gui.actions.PresentationAction presentationAction;
    private de.cismet.watergis.gui.actions.map.PreviousExtendAction previousExtendAction;
    private de.cismet.watergis.gui.actions.PrintAction printAction;
    private de.cismet.watergis.gui.actions.reports.RechtsgrundlageAction rechtsgrundlageAction1;
    private de.cismet.watergis.gui.actions.RefreshViewsForUser refreshViewsForUser1;
    private de.cismet.watergis.gui.actions.ReleaseAction releaseAction;
    private de.cismet.watergis.gui.actions.map.ReloadAction reloadAction1;
    private de.cismet.watergis.gui.actions.map.RemoveDrawingModeAction removeDrawingModeAction;
    private de.cismet.watergis.gui.actions.map.RemoveModeAction removeModeAction1;
    private de.cismet.watergis.gui.actions.selection.RemoveSelectionAllTopicsAction removeSelectionAllTopicsAction;
    private de.cismet.watergis.gui.actions.foto.ReportAction reportAction1;
    private de.cismet.watergis.gui.actions.gaf.ReportAction reportActionGaf;
    private de.cismet.watergis.gui.actions.reports.GerinneGGemeindeReportAction rlDDueReportAction;
    private de.cismet.watergis.gui.panels.RouteZoomPanel routeZoomPanel;
    private de.cismet.watergis.gui.actions.map.SaveDrawingsAction saveDrawingsAction;
    private de.cismet.watergis.gui.actions.SaveProjectAction saveProjectAction;
    private de.cismet.watergis.gui.actions.SaveToSameFileProjectAction saveToSameFileProjectAction1;
    private de.cismet.watergis.gui.actions.reports.KatasterSbReportAction sbReportAction;
    private de.cismet.watergis.gui.actions.selection.SelectAllAction selectAllAction;
    private de.cismet.watergis.gui.actions.selection.SelectAllDrawingsAction selectAllDrawingsAction;
    private de.cismet.watergis.gui.actions.selection.SelectionAttributeAction selectionAttributeAction;
    private de.cismet.watergis.gui.actions.selection.SelectionEllipseAction selectionEllipseAction;
    private de.cismet.watergis.gui.actions.selection.SelectionLocationAction selectionLocationAction;
    private de.cismet.watergis.gui.actions.map.SelectionModeAction selectionModeAction;
    private de.cismet.watergis.gui.actions.selection.SelectionPolygonAction selectionPolygonAction;
    private de.cismet.watergis.gui.actions.selection.SelectionRectangleAction selectionRectangleAction;
    private javax.swing.JPopupMenu.Separator sepCentralFilesEnd;
    private javax.swing.JPopupMenu.Separator sepCentralFilesStart;
    private javax.swing.JPopupMenu.Separator sepDrawingOperation;
    private javax.swing.JPopupMenu.Separator sepDrawingOperation1;
    private javax.swing.JPopupMenu.Separator sepLocalFilesEnd;
    private javax.swing.JPopupMenu.Separator sepWindowSeparator;
    private de.cismet.watergis.gui.actions.bookmarks.ShowCreateBookmarkDialogAction showCreateBookmarkDialogAction;
    private de.cismet.watergis.gui.actions.ShowWindowAction showDatasource;
    private de.cismet.watergis.gui.actions.window.ShowHideOverviewWindowAction showHideOverviewWindowAction;
    private de.cismet.watergis.gui.actions.ShowWindowAction showInfo;
    private de.cismet.watergis.gui.actions.InfoWindowAction showInfoWindowAction;
    private de.cismet.watergis.gui.actions.ShowWindowAction showLegend;
    private de.cismet.watergis.gui.actions.bookmarks.ShowManageBookmarksDialogAction showManageBookmarksDialogAction;
    private de.cismet.watergis.gui.actions.ShowWindowAction showMap;
    private de.cismet.watergis.gui.actions.ShowWindowAction showOverview;
    private de.cismet.watergis.gui.actions.ShowWindowAction showPhoto;
    private de.cismet.watergis.gui.actions.ShowWindowAction showProblems;
    private de.cismet.watergis.gui.actions.ShowWindowAction showProfiles;
    private de.cismet.watergis.gui.actions.ShowWindowAction showTree;
    private de.cismet.cids.navigator.utils.SimpleMemoryMonitoringToolbarWidget simpleMemoryMonitoringToolbarWidget1;
    private de.cismet.watergis.gui.actions.checks.SonstigeCheckAction sonstigeCheckAction;
    private de.cismet.watergis.gui.actions.SplitAction splitAction;
    private de.cismet.watergis.gui.actions.geoprocessing.StationAction stationAction1;
    private de.cismet.watergis.gui.panels.StatusBar statusBar1;
    private de.cismet.watergis.gui.panels.StatusPanel statusPanel1;
    private javax.swing.JToggleButton tbtNewObject;
    private javax.swing.JToggleButton tbtnAddMode;
    private javax.swing.JToggleButton tbtnInfo;
    private javax.swing.JButton tbtnMeasure;
    private javax.swing.JToggleButton tbtnMeasureLineMode;
    private javax.swing.JToggleButton tbtnPanMode;
    private javax.swing.JToggleButton tbtnPhotoInfoMode;
    private javax.swing.JToggleButton tbtnProfileInfoMode;
    private javax.swing.JToggleButton tbtnRemoveMode;
    private javax.swing.JToggleButton tbtnZoomMode;
    private de.cismet.watergis.gui.actions.geoprocessing.TechProfAction techProfAction1;
    private javax.swing.JToolBar tobDLM25W;
    private de.cismet.watergis.gui.actions.geoprocessing.UnionGeoprocessingAction unionGeoprocessingAction;
    private de.cismet.watergis.gui.actions.selection.UnselectAllDrawingsAction unselectAllDrawingsAction;
    private de.cismet.watergis.gui.actions.foto.UploadAction uploadAction1;
    private de.cismet.watergis.gui.actions.gaf.UploadAction uploadActionGaf;
    private de.cismet.watergis.gui.actions.checks.VerwaltungCheckAction verwaltungCheckAction;
    private de.cismet.watergis.gui.actions.WindowAction windowAction;
    private de.cismet.watergis.gui.actions.reports.WkFgReportAction wkFgReportAction;
    private de.cismet.watergis.gui.actions.reports.WsgAction wsgAction1;
    private de.cismet.watergis.gui.actions.reports.WsgWaldAction wsgWaldAction1;
    private de.cismet.watergis.gui.actions.selection.ZoomAllDrawingsAction zoomAllDrawingsAction;
    private de.cismet.watergis.gui.actions.map.ZoomInAction zoomInAction;
    private de.cismet.watergis.gui.actions.map.ZoomModeAction zoomModeAction;
    private de.cismet.watergis.gui.actions.map.ZoomOutAction zoomOutAction;
    private de.cismet.watergis.gui.actions.selection.ZoomSelectedDrawingsAction zoomSelectedDrawingsAction;
    private de.cismet.watergis.gui.actions.selection.ZoomSelectedObjectsAction zoomSelectedObjectsAction;
    private de.cismet.watergis.gui.actions.selection.ZoomSelectedThemesAction zoomSelectedThemesAction1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form WatergisApp.
     */
    public WatergisApp() {
        addWindowListener(this);
        CismapBroker.getInstance().setUseInternalDb(true);
        CismapBroker.getInstance().setFeatureStylingComponentKey("Jump");
        CismapBroker.getInstance().setHighlightFeatureOnMouseOver(false);
        CismapBroker.getInstance().setDefaultTranslucency(1.0f);
        StaticSwingTools.tweakUI();

        try {
            initConnection();
        } catch (Exception e) {
            LOG.error("Connection exception", e);
            final List<String> messages = new ArrayList<String>();
            final StackTraceElement[] elements = e.getStackTrace();
            messages.add("call server: " + AppBroker.getInstance().getCallserverUrl());
            messages.add("Connection class: " + AppBroker.getInstance().getConnectionClass());
            messages.add("Domain: " + AppBroker.getInstance().getDomain());
            if (ProxyHandler.getInstance().getProxy() != null) {
                messages.add("proxy: " + ProxyHandler.getInstance().getProxy().isEnabled());
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
        try {
            H2FeatureServiceFactory.tryDBConnection(null);
        } catch (SQLException e) {
            System.err.println("Database is already in use");
            final List<String> messages = new ArrayList<String>();
            messages.add(
                "Die interne Datenbank wird bereits genutzt. Wahrscheinlich läuft bereits eine Instanz der Anwendung und es können keine 2 Instanzen parallel laufen.");
            ExceptionManager.getManager()
                    .showExceptionDialog(this, ExceptionManager.ERROR, "Exception", e.getMessage(), messages);
            System.exit(1);
        }
        // clear all locks from the previous session
        H2FeatureService.clearLocks();
        H2FeatureService.removeUnusedSequences();
        final Map<HostConfiguration, Integer> maxHostConnections = new HashMap<HostConfiguration, Integer>();
        maxHostConnections.put(HostConfiguration.ANY_HOST_CONFIGURATION, 128);
        HttpConnectionManagerParams.getDefaultParams()
                .setParameter(HttpConnectionManagerParams.MAX_HOST_CONNECTIONS, maxHostConnections);
        HttpConnectionManagerParams.getDefaultParams()
                .setIntParameter(HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, 128);
        HttpConnectionManagerParams.getDefaultParams().setIntParameter(HttpConnectionManagerParams.SO_LINGER, 2);
        configManager.addConfigurable(this);
        configManager.configure(this);
        ClassCacheMultiple.setInstance(AppBroker.DOMAIN_NAME);
        AppBroker.setConfigManager(configManager);
        UIManager.put("Table.selectionBackground", new Color(195, 212, 232));
        UIManager.put("Tree.selectionBackground", new Color(195, 212, 232));
        UIManager.put("Cismap.featureSelectionForeground", new Color(255, 0, 0));
        initCismap();
        configManager.addConfigurable(OptionsClient.getInstance());
        configManager.configure(OptionsClient.getInstance());
        routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");
        wwGrMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_ww_gr");
        initComponents();

        boolean backgroundChecksEnabled = false;

        try {
            backgroundChecksEnabled = SessionManager.getProxy()
                        .getConfigAttr(SessionManager.getSession().getUser(),
                                "backgroundChecksEnabled",
                                connectionContext) != null;
        } catch (ConnectionException ex) {
            LOG.error("Cannot check for backgroundChecks permission", ex);
        }

        statusPanel1.setVisible(backgroundChecksEnabled);

        menSteckbriefWasserkoerper.setVisible(false);
        selectionModeAction.setButton(cmdSelectionMode);
        configureButtons();
        cmdSelectAll.setVisible(false);
        cmdInvertSelection.setVisible(false);
        cmdZoomIn.setVisible(false);
        cmdZoomOut.setVisible(false);
        mniOverview.setVisible(false);
        cmdDrawingMode.setVisible(false);
        cmdRefresh.setVisible(false);
        CismapBroker.getInstance().addMapDnDListener(this);
        cmdAnnex.setAction(new AnnexAction(true));
        cmdAnnex.setEnabled(false);
        if (simpleMemoryMonitoringToolbarWidget1.isVisible()) {
            jToolBar1.add(simpleMemoryMonitoringToolbarWidget1);
        }
        ((NewDrawingButton)cmdDrawingMode).setButtonGroup(btnGroupMapMode);
        exportIgmAction.setExport(exportAction1);
        retrievePermissionbeans();

        if (!SessionManager.getSession().getUser().getUserGroup().getName().equalsIgnoreCase("administratoren")) {
            mniCheckLawa.setEnabled(false);
            mniCheckLawaConnection.setEnabled(false);
            mniCreateDbUser.setEnabled(false);
            mniRefreshDbUser.setEnabled(false);
            mniSb.setEnabled(false);
            mniFl.setEnabled(false);
            mniGewaesserRep1.setEnabled(false);
            mniGewaesserRep2.setEnabled(false);
            mniGemeinde2.setEnabled(false);
            mniSb2.setEnabled(false);
            mniFl2.setEnabled(false);
            mniGemeinde1.setEnabled(false);
            mniSb1.setEnabled(false);
            mniFl1.setEnabled(false);
            menWsg.setVisible(false);
        }

        if (!AppBroker.getInstance().isAdminUser() && !AppBroker.getInstance().isGu()) {
            mniCheckAusbau.setEnabled(false);
            mniCheckBasisRoutes.setEnabled(false);
            mniCheckBauwerke.setEnabled(false);
            mniCheckSonstige.setEnabled(false);
            mniCheckVerwaltung.setEnabled(false);
        }

        if (!AppBroker.getInstance().isWawiOrAdminUser()) {
            mniIgmExport.setEnabled(false);
            mniExport.setEnabled(false);
            mniExportOption.setEnabled(false);
        }
        ((MeasureButton)tbtnMeasure).setButtonGroup(btnGroupMapMode);
        ((SelectionButton)cmdSelectionMode).setButtonGroup(btnGroupMapMode);
        final boolean drawingsExists = DrawingManager.getInstance().featuresExists();
        cmdSaveDrawings.setEnabled(drawingsExists);
        mniSaveDrawings.setEnabled(drawingsExists);
        mniZoomAllDrawings.setEnabled(drawingsExists);
        cmdZoomToAllDrawings.setEnabled(drawingsExists);
        cmdSelectAllDrawings.setEnabled(drawingsExists);
        mniSelectAllDrawing.setEnabled(drawingsExists);
        initDefaultPanels();

        if (backgroundChecksEnabled) {
            initialiseStatusPanel();
        }

        initMapModes();
        initHistoryButtonsAndRecentlyOpenedFiles();
        initInfoNode();
        initAttributeTable();
        configureFileMenu();
        initLog4JQuickConfig();
        initBookmarkManager();
        retrieveValidLawaCodes();
        AppBroker.getInstance().setInfoWindowAction(infoWindowAction);
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
        pTopicTree.addThemeLayerListener(this);
        AppBroker.getInstance().setPhotoExport(exportActionPhoto);
        AppBroker.getInstance().setGafExport(exportActionGaf);
        AppBroker.getInstance().setPhotoPrint(reportAction1);
        AppBroker.getInstance().setGafPrint(reportActionGaf);
        SelectionManager.getInstance().addSelectionChangedListener(this);

        DrawingManager.getInstance().addDrawingsListener(new DrawingsListener() {

                @Override
                public void drawingsCountChanged(final DrawingCountChangedEvent e) {
                    final boolean featuresExists = DrawingManager.getInstance().featuresExists();
                    final boolean drawsSelected = !RemoveDrawingModeAction.getSelectedDrawings().isEmpty();

                    cmdSaveDrawings.setEnabled(featuresExists);
                    mniZoomAllDrawings.setEnabled(featuresExists);
                    cmdZoomToAllDrawings.setEnabled(featuresExists);
                    mniSaveDrawings.setEnabled(featuresExists);
                    cmdSelectAllDrawings.setEnabled(featuresExists);
                    mniSelectAllDrawing.setEnabled(featuresExists);
                    cmdRemoveGeometry.setEnabled(drawsSelected);
                    mniRemoveDrawing.setEnabled(drawsSelected);
                    mniZoomSelectedDrawings.setEnabled(drawsSelected);
                    cmdZoomToSelectedDrawings.setEnabled(drawsSelected);
                    mniUnselectAllDrawing.setEnabled(drawsSelected);
                    cmdUnselectDrawings.setEnabled(drawsSelected);
                }
            });

        mappingComponent.addCustomInputListener("REMOVE_MULTIPOLYGON", new DeleteFeatureListener(mappingComponent));
        mappingComponent.addCustomInputListener("CHANGE_MULTIPOLYGON", new PolygonChangeListener(mappingComponent));
        AppBroker.getInstance().switchMapMode(mappingComponent.getInteractionMode());

        final String interactionMode = CismapBroker.getInstance().getMappingComponent().getHandleInteractionMode();

        if (interactionMode.equals(MappingComponent.MOVE_HANDLE)) {
            cmdNodeMove.setSelected(true);
        } else if (interactionMode.equals(MappingComponent.ADD_HANDLE)) {
            cmdNodeAdd.setSelected(true);
        } else if (interactionMode.equals(MappingComponent.REMOVE_HANDLE)) {
            cmdNodeRemove.setSelected(true);
        } else {
            CismapBroker.getInstance().getMappingComponent().setHandleInteractionMode(MappingComponent.ADD_HANDLE);
            cmdNodeMove.setSelected(true);
        }

        addThemeAction1.init(pCapabilities, rootWindow, vCapability);
        addThemeTreeAction.init(pCapabilities, rootWindow, vCapability);
        initThemeCombo();
        AutoCompleteDecorator.decorate(cboTheme);

        final Map<String, JMenu> menues = new HashMap<String, JMenu>();

        for (final TemplateExportAction action : allTemplates) {
            final String folder = action.getFolder();

            if (folder != null) {
                JMenu menu = menues.get(folder);

                if (menu == null) {
                    menu = new JMenu(folder);
                    menues.put(folder, menu);
                    menSchablonen.add(menu);
                }

                menu.add(action);
            } else {
                menSchablonen.add(action);
            }
        }

        mniGafUpload.setVisible(false);
        boolean checkForQP = false;
        try {
            checkForQP = SessionManager.getProxy()
                        .getConfigAttr(SessionManager.getSession().getUser(), "qpAllowed", connectionContext) != null;
        } catch (ConnectionException ex) {
            LOG.error("Cannot check for qp permission", ex);
        }
        mniGafUpload.setEnabled(checkForQP);
        mniGafUpload.setVisible(checkForQP);

        final boolean checkForTechProf = AppBroker.getInstance().isAdminUser()
                    || AppBroker.getInstance().getOwnWwGr().getProperty("ww_gr").equals(3100);
        mniTechProf.setVisible(checkForTechProf);
        mniTechProf.setEnabled(checkForTechProf);
        checkForCreatedObjects();
        Double scaleForPoint = 500.0;

        try {
            scaleForPoint = Double.valueOf(SessionManager.getProxy().getConfigAttr(
                        SessionManager.getSession().getUser(),
                        "zoomScaleForPointFeature",
                        connectionContext));
        } catch (Exception ex) {
            LOG.warn("zoomScaleForPointFeature is not set or not valid. 500 will be assumed", ex);
        }
        ZoomToFeaturesWorker.setScaleForPoint(500.0);

        try {
            final String zoomPercentage = SessionManager.getProxy()
                        .getConfigAttr(SessionManager.getSession().getUser(), "pfbZoomFactor", connectionContext);

            if (zoomPercentage != null) {
                AppBroker.getInstance().setProblemFeatureGeometryIncrease(Integer.parseInt(zoomPercentage));
            }
        } catch (ConnectionException ex) {
            LOG.error("Cannot check for qp permission", ex);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void initThemeCombo() {
        cboTheme.setModel(new DefaultComboBoxModel(new Object[] { "Lade ..." }));

        final SwingWorker sw = new SwingWorker<MetaObject[], Void>() {

                @Override
                protected MetaObject[] doInBackground() throws Exception {
                    final MetaClass MC = ClassCacheMultiple.getMetaClass(
                            AppBroker.DOMAIN_NAME,
                            "dlm25w.k_kate_such_1",
                            connectionContext);
                    String query = "select " + MC.getID() + ", " + MC.getPrimaryKey() + " from " + MC.getTableName();
                    query += " order by index";

                    final MetaObject[] metaObjects = SessionManager.getProxy()
                                .getMetaObjectByQuery(query, 0, connectionContext);

                    return metaObjects;
                }

                @Override
                protected void done() {
                    try {
                        final MetaObject[] mos = get();
                        cboTheme.setModel(new DefaultComboBoxModel(mos));
                    } catch (InterruptedException | ExecutionException e) {
                        LOG.error("Error while initializing the model of the theme combobox", e); // NOI18N
                    }
                }
            };

        CismetConcurrency.getInstance("watergis").getDefaultExecutor().execute(sw);
    }

    /**
     * DOCUMENT ME!
     */
    private void checkForCreatedObjects() {
        try {
            final User user = SessionManager.getSession().getUser();
            final InetAddress addr = InetAddress.getLocalHost();
            final CidsServerSearch cleanup = new CheckForCreatedObjectTable(
                    user.getDomain()
                            + "."
                            + user.getName(),
                    addr.getHostName());
            final ArrayList<ArrayList> objects = (ArrayList<ArrayList>)SessionManager.getProxy()
                        .customServerSearch(SessionManager.getSession().getUser(), cleanup);
            removeOldLocks();

            if ((objects != null) && (objects.size() > 0)) {
                final Map<Integer, List<Integer>> objectList = new HashMap<Integer, List<Integer>>();

                for (final ArrayList object : objects) {
                    List<Integer> oList = objectList.get((Integer)object.get(0));

                    if (oList == null) {
                        oList = new ArrayList<Integer>();

                        objectList.put((Integer)object.get(0), oList);
                    }

                    oList.add((Integer)object.get(1));
                }

                for (final Integer classId : objectList.keySet()) {
                    final List<Integer> objectIds = objectList.get(classId);
                    final MetaClass mc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, classId);

                    if (mc != null) {
                        final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                                mc.getName());
                        AbstractFeatureService layer;

                        if ((services == null) || services.isEmpty()) {
                            layer = new CidsLayer(mc);
                            AppBroker.getInstance().getMappingComponent().getMappingModel().addLayer(layer);
                            AttributeTableFactory.getInstance().switchProcessingMode(layer);
                        } else {
                            layer = services.get(0);
                            if (!SelectionManager.getInstance().getEditableServices().contains(services.get(0))) {
                                AttributeTableFactory.getInstance().switchProcessingMode(services.get(0));
                            }
                        }

                        final AttributeTable table = getAttributeTableByFeatureService(layer);

                        if (table != null) {
                            final List<FeatureServiceFeature> featureList = new ArrayList<FeatureServiceFeature>();

                            for (final Integer objectId : objectIds) {
                                final FeatureServiceFeature feature = table.getFeatureById(objectId);
                                if (feature != null) {
                                    featureList.add(feature);
                                    table.markFeatureAsNewFeature((DefaultFeatureServiceFeature)feature);
                                }
                            }

                            table.makeFeaturesEditable((List<FeatureServiceFeature>)featureList);
                            SelectionManager.getInstance().addSelectedFeatures(featureList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while checking for incomplete created objects", e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void initRouteCombo() {
        routeZoomPanel.init();
    }

    /**
     * DOCUMENT ME!
     */
    private void removeOldLocks() {
        try {
            final String user = SessionManager.getSession().getUser().getName();
            final InetAddress addr = InetAddress.getLocalHost();
            final String computerName = addr.getHostName();
            final CidsServerSearch removeLocks = new RemoveOldLocks(user, computerName);
            SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), removeLocks);
        } catch (Exception e) {
            LOG.error("cannot remove old locks", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the currentLayoutFile
     */
    public String getCurrentLayoutFile() {
        return currentLayoutFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  currentLayoutFile  the currentLayoutFile to set
     */
    public void setCurrentLayoutFile(final String currentLayoutFile) {
        this.currentLayoutFile = currentLayoutFile;
    }

    /**
     * DOCUMENT ME!
     */
    private void initCismap() {
        CismapBroker.getInstance().setEnableDummyLayerWhenAvailable(false);
        mappingComponent = new MappingComponent(true);
        mappingComponent.addCustomInputListener(MappingComponent.SELECT, new SelectionListener() {

                @Override
                protected Color getFillingColor() {
                    return new Color(255, 0, 0, 80);
                }
            });
        AppBroker.getInstance().setMappingComponent(mappingComponent);
        final SelectionListener sl = (SelectionListener)mappingComponent.getInputEventListener()
                    .get(MappingComponent.SELECT);
        sl.setFeaturesFromServicesSelectable(true);
        sl.setSelectMultipleFeatures(true);
        final SimpleMoveListener motion = (SimpleMoveListener)mappingComponent.getInputEventListener()
                    .get(MappingComponent.MOTION);
        motion.setUnderlyingObjectHalo(0.01);
        mappingModel.setInitalLayerConfigurationFromServer(false);
        configManager.addConfigurable(mappingComponent);
        configManager.addConfigurable((ActiveLayerModel)mappingModel);

        // First local configuration then serverconfiguration
        mappingComponent.preparationSetMappingModel(mappingModel);
        configManager.configure(mappingComponent);
        configManager.configure(mappingModel);

        mappingComponent.setMappingModel(mappingModel);

        mappingComponent.setInternalLayerWidgetAvailable(true);
        ((Observable)mappingComponent.getMemUndo()).addObserver(this);
        ((Observable)mappingComponent.getMemRedo()).addObserver(this);
        mappingComponent.getFeatureCollection().addFeatureCollectionListener(this);
        mappingComponent.getInputEventListener().put(PhotoInfoListener.MODE, new PhotoInfoListener(mappingComponent));
        mappingComponent.getInputEventListener().put(PhotoAngleListener.MODE, new PhotoAngleListener(mappingComponent));
        mappingComponent.getInputEventListener().put(GafInfoListener.MODE, new GafInfoListener(mappingComponent));
        mappingComponent.putCursor(PhotoInfoListener.MODE, new Cursor(Cursor.HAND_CURSOR));
        mappingComponent.putCursor(PhotoAngleListener.MODE, new Cursor(Cursor.CROSSHAIR_CURSOR));
        mappingComponent.putCursor(GafInfoListener.MODE, new Cursor(Cursor.HAND_CURSOR));
//        mappingComponent.setSnappingEnabled(true);
//        mappingComponent.setSnappingOnLineEnabled(true);
//        mappingComponent.setVisualizeSnappingRectEnabled(true);
        mappingComponent.setSnappingRectSize(80);
        CismapBroker.getInstance().setMappingComponent(mappingComponent);
        mappingComponent.setCrossHairColor(new Color(255, 0, 0, 255));
        mappingComponent.setCrossHairThickness(4);
        WFSCapabilitiesTreeCellRenderer.showTitle = true;
        PointReferencingDialog.setMIN_X(33000000d);
        PointReferencingDialog.setMAX_X(33999999d);
        PointReferencingDialog.setMIN_Y(5600000d);
        PointReferencingDialog.setMAX_Y(6399999d);
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final Thread t = new Thread("checkMapConfiguration") {

                            @Override
                            public void run() {
                                checkMapConfiguration(null);
                            }
                        };

                    t.start();
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file  DOCUMENT ME!
     */
    public void checkMapConfiguration(final File file) {
        final ActiveLayerModel model = (ActiveLayerModel)mappingComponent.getMappingModel();
        final TreeMap<Integer, MapService> services = new TreeMap<>(model.getMapServices());
        boolean layersChanged = false;

        for (final Integer key : services.keySet()) {
            final MapService service = services.get(key);

            if (service instanceof CidsLayer) {
                final CidsLayer layer = (CidsLayer)service;

                if ((layer.getMetaClass() == null) || (layer.getMetaClass().getClassAttribute("cidsLayer") == null)) {
                    layersChanged = true;
                    EventQueue.invokeLater(new Thread("removeLayer") {

                            @Override
                            public void run() {
                                model.removeLayer(layer);
                            }
                        });
                } else {
                    try {
                        final CidsLayer serverLayer = new CidsLayer(layer.getMetaClass());
                        serverLayer.initAndWait();
                        final Map<String, FeatureServiceAttribute> attrMap = layer.getLayerProperties()
                                    .getFeatureService()
                                    .getFeatureServiceAttributes();
                        final Map<String, FeatureServiceAttribute> serverAttrMap = serverLayer.getLayerProperties()
                                    .getFeatureService()
                                    .getFeatureServiceAttributes();

                        for (final String attrKey : attrMap.keySet()) {
                            final String type = attrMap.get(attrKey).getType();
                            if ((serverAttrMap.get(attrKey) == null)
                                        || ((serverAttrMap.get(attrKey) != null)
                                            && !serverAttrMap.get(attrKey).getType().equals(type))) {
                                // test if the attribute does not exist in the server or the type differs
                                layersChanged = true;
                                EventQueue.invokeLater(new Thread("switchLayer") {

                                        @Override
                                        public void run() {
                                            if (!model.switchLayer(layer, serverLayer)) {
                                                model.removeLayer(layer);
                                                model.addLayer(serverLayer);
                                            }
                                        }
                                    });
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // layer cannot be initialised
                        layersChanged = true;
                        EventQueue.invokeLater(new Thread("removeLayer") {

                                @Override
                                public void run() {
                                    model.removeLayer(layer);
                                }
                            });
                    }
                }
            }
        }

        if (layersChanged && (file != null)) {
            LOG.info("rewrite configuration file");
            saveToSameFileProjectAction1.actionPerformed(null);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void retrievePermissionbeans() {
        try {
            String query = "select " + wwGrMc.getID() + ", " + wwGrMc.getTableName() + "."
                        + wwGrMc.getPrimaryKey() + " from "
                        + wwGrMc.getTableName() + " WHERE owner ilike '"
                        + SessionManager.getSession().getUser().getUserGroup().getName() + "'"; // NOI18N
            MetaObject[] mos = SessionManager.getProxy().getMetaObjectByQuery(query, 0);
            List<CidsBean> beans = new ArrayList<CidsBean>();

            if (mos != null) {
                for (final MetaObject mo : mos) {
                    beans.add(mo.getBean());
                }
            }

            AppBroker.getInstance().setOwnWwGr(beans);

            final String NoOneQuery = "select " + wwGrMc.getID() + ", " + wwGrMc.getTableName() + "."
                        + wwGrMc.getPrimaryKey() + " from "
                        + wwGrMc.getTableName() + " WHERE owner = 'NIEMAND'"; // NOI18N
            final MetaObject[] noOneMos = SessionManager.getProxy().getMetaObjectByQuery(NoOneQuery, 0);

            if (noOneMos != null) {
                for (final MetaObject mo : noOneMos) {
                    AppBroker.getInstance().setNiemandWwGr(mo.getBean());
                }
            }

            query = "select " + wwGrMc.getID() + ", " + wwGrMc.getTableName() + "."
                        + wwGrMc.getPrimaryKey() + " from "
                        + wwGrMc.getTableName();
            mos = SessionManager.getProxy().getMetaObjectByQuery(query, 0);
            beans = new ArrayList<CidsBean>();

            if (mos != null) {
                for (final MetaObject mo : mos) {
                    beans.add(mo.getBean());
                }
            }
            AppBroker.getInstance().setWwGr(beans);
        } catch (Exception e) {
            LOG.error("Cannot retrieve premission beans", e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void retrieveValidLawaCodes() {
        try {
            final CidsServerSearch search = new ValidLawaCodes();
            final ArrayList<ArrayList> res = (ArrayList<ArrayList>)SessionManager.getProxy()
                        .customServerSearch(SessionManager.getSession().getUser(), search);

            final String[] lawaCodes = new String[res.size()];

            for (int i = 0; i < res.size(); ++i) {
                lawaCodes[i] = res.get(i).get(0).toString();
            }

            Arrays.sort(lawaCodes);

            AppBroker.getInstance().setValidLawaCodes(lawaCodes);
        } catch (Exception e) {
            LOG.error("Cannot retrieve premission beans", e);
        }
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
    private void initHttpServer() {
        GeoLinkServer.startServer();
    }

    /**
     * DOCUMENT ME!
     */
    private static void initLog4J() {
        try(final InputStream configStream = WatergisApp.class.getResourceAsStream(
                            "/de/cismet/watergis/configuration/log4j.xml")) {
            final ConfigurationSource source = new ConfigurationSource(configStream);
            final LoggerContext context = (LoggerContext)LogManager.getContext(false);
            context.start(new XmlConfiguration(context, source)); // Apply new configuration
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
        pPhoto = new Photo();
        pGaf = new GafProf();
        pTopicTree = new ThemeLayerWidget();
        pInfo = new InfoPanel();
        pSelection = new SelectionPanel();
        pTopicTree.setMappingModel(mappingModel);
        addThemeTreeAction = new AddThemeAction(pCapabilities, rootWindow, vCapability);
        pTopicTree.addAddThemeMenuItemListener(addThemeTreeAction);
        pTopicTree.addTreeSelectionListener(new TreeSelectionListener() {

                @Override
                public void valueChanged(final TreeSelectionEvent e) {
                    topicTreeSelectionChanged(e);
                }
            });
        pTopicTree.insertMenuItemIntoContextMenu(18, new MetaDocumentMenuItem());
        pTopicTree.insertMenuItemIntoContextMenu(18, new ExportMenuItem());
        pTopicTree.insertMenuItemIntoContextMenu(18, new DeleteMenuItem());
        pTopicTree.insertMenuItemIntoContextMenu(18, new PasteMenuItem());
        pTopicTree.insertMenuItemIntoContextMenu(18, new CopyMenuItem());
        pTopicTree.insertMenuItemIntoContextMenu(18, new ExportOptionsMenuItem());
        pOverview = new OverviewComponent();
        pOverview.setMasterMap(mappingComponent);
        configManager.addConfigurable(pOverview);
        configManager.configure(pOverview);

        pCapabilities = new CapabilityWidget();
        CapabilityWidgetOptionsPanel.setCapabilityWidget(pCapabilities);
        CismapBroker.getInstance().addMapBoundsListener(pCapabilities);
//        pLegend = new Legend();
        configManager.addConfigurable(exportAction1);
        configManager.configure(exportAction1);
        configManager.addConfigurable(pCapabilities);
        configManager.configure(pCapabilities);
        try {
            AppBroker.getInstance().initComponentRegistry(this);

            if (!AppBroker.getInstance().getOwner().startsWith("gaeste")) {
                // gaeste should not see the problem tree
                pTable = AppBroker.getInstance().getComponentRegistry().getCatalogueTree();
                pTablePane.setViewportView(pTable);
                final PureTreeNode treeNode = (PureTreeNode)((RootTreeNode)pTable.getModel().getRoot()).getChildAt(0);
                String childStat = treeNode.getMetaNode().getDynamicChildrenStatement();
                String user = "null";
                if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("administratoren")) {
                    user = "'" + AppBroker.getInstance().getOwner() + "'";
                }
                childStat = childStat.replace("$user", user);
                treeNode.getMetaNode().setDynamicChildrenStatement(childStat);
            } else {
                mniShowProblems.setVisible(false);
            }
        } catch (Exception e) {
            LOG.error("The problem tree cannot be created", e);
            pTable.setModel(null);
        }

        if (AppBroker.getInstance().getOwner().equalsIgnoreCase("administratoren")) {
            cmdAnnex.setEnabled(false);
            cmdRelease.setEnabled(false);
        }

        AppBroker.getInstance().addComponent(ComponentName.MAP, pMap);
        AppBroker.getInstance().addComponent(ComponentName.PHOTO, pPhoto);
        AppBroker.getInstance().addComponent(ComponentName.GAF_PROF, pGaf);
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
        CismapBroker.getInstance().addActiveLayerListener(statusBar1);

        LOG.info("set refernence for the main application in Broker: " + this);
        AppBroker.getInstance().addComponent(ComponentName.MAIN, this);
    }

    /**
     * DOCUMENT ME!
     */
    private void initialiseStatusPanel() {
        final CheckBackgroundWorker worker = new CheckBackgroundWorker();

        worker.addBackgroundCheckListener(new BackgroundCheckListener() {

                @Override
                public void checkComplete(final BackgroundCheckEvent e) {
                    final AbstractCheckResult result = e.getResult();
                    final int index = e.getIndex();
                    String text;
                    StatusPanel.IconColor icon;

                    if (result == null) {
                        text = e.getCheck().getValue(Action.NAME) + ":\nWährend der Prüfung trat ein Fehler auf. ";
                        icon = StatusPanel.IconColor.RED;
                    } else {
                        int problems = 0;

                        for (final String check : result.getCheckNames()) {
                            problems += result.getErrorsPerCheck(check);
                        }

                        if (problems == 0) {
                            text = e.getCheck().getValue(Action.NAME).toString();
                            icon = StatusPanel.IconColor.GREEN;
                        } else {
                            text = e.getCheck().getValue(Action.NAME).toString() + ":\nEs wurden " + problems
                                        + " Probleme festgestellt";
                            icon = StatusPanel.IconColor.RED;
                        }
                    }

                    statusPanel1.setStatusIcon(index, icon);
                    statusPanel1.setTooltip(index, text);
                    statusPanel1.updateUI();
                    statusPanel1.repaint();
                }
            });

        statusPanel1.setCount(worker.getChecks().length);

        for (int i = 0; i < worker.getChecks().length; ++i) {
            statusPanel1.setTooltip(i, (String)worker.getChecks()[i].getValue(Action.NAME));
        }

        worker.start();
    }

    /**
     * DOCUMENT ME!
     */
    private void initMapModes() {
        AppBroker.getInstance().addMapMode(MappingComponent.PAN, panAction);
        AppBroker.getInstance().addMapMode(MappingComponent.MOVE_POLYGON, moveModeAction);
        AppBroker.getInstance().addMapMode(MappingComponent.ZOOM, zoomModeAction);
        AppBroker.getInstance().addMapMode(MappingComponent.SELECT, selectionModeAction);
        AppBroker.getInstance().addMapMode(MappingComponent.FEATURE_INFO_MULTI_GEOM, infoWindowAction);
        AppBroker.getInstance().addMapMode(MappingComponent.NEW_POLYGON, newLinestringModeAction);
        AppBroker.getInstance().addMapMode(MappingComponent.REMOVE_POLYGON, removeDrawingModeAction);
        AppBroker.getInstance().addMapMode(AppBroker.MEASURE_MODE, measureAction);
        AppBroker.getInstance().addMapMode(FeatureCreator.SIMPLE_GEOMETRY_LISTENER_KEY, newObjectAction);
        AppBroker.getInstance().addMapMode(MappingComponent.LINEAR_REFERENCING, measureLineAction);
        AppBroker.getInstance()
                .addMapMode(CreateLinearReferencedLineListener.CREATE_LINEAR_REFERENCED_LINE_MODE, newObjectAction);
        AppBroker.getInstance()
                .addMapMode(CreateLinearReferencedPointListener.CREATE_LINEAR_REFERENCED_POINT_MODE, newObjectAction);
        AppBroker.getInstance().addMapMode(PhotoInfoListener.MODE, fotoInfoAction1);
        AppBroker.getInstance().addMapMode(GafInfoListener.MODE, gafInfoAction);
//        AppBroker.getInstance().addMapMode(SplitGeometryListener.LISTENER_KEY, splitAction);

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

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Photo");
        vPhoto = new View(title, null, pPhoto);
        viewMap.addView(title, vPhoto);
        AppBroker.getInstance().setPhotoView(vPhoto);
        vPhoto.addListener(new DockingWindowAdapter() {

                @Override
                public void windowClosed(final DockingWindow window) {
                    pPhoto.dispose();
                }
            });

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Gaf");
        vGaf = new View(title, null, pGaf);
        viewMap.addView(title, vGaf);
        AppBroker.getInstance().setGafView(vGaf);
        vGaf.addListener(new DockingWindowAdapter() {

                @Override
                public void windowClosed(final DockingWindow window) {
                    pGaf.dispose();
                }
            });

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Table");
        vTable = new View(title, null, pTablePane);
        viewMap.addView(title, vTable);

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Overview");
        vOverview = new View(title, null, pOverview);
        showHideOverviewWindowAction.setView(vOverview);
        viewMap.addView(title, vOverview);

        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Capabilities");
        vCapability = new View(title, null, pCapabilities);
        viewMap.addView(title, vCapability);

//        title = org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.initInfoNode().Legend");
//        vLegend = new View(title, null, pLegend);
//        viewMap.addView(title, vLegend);

        rootWindow = DockingUtil.createRootWindow(viewMap, true);
        rootWindow.getRootWindowProperties().getFloatingWindowProperties().setUseFrame(true);
        rootWindow.addListener(new DockingWindowAdapter() {

                @Override
                public void windowClosed(final DockingWindow window) {
                    for (int i = 0; i < window.getChildWindowCount(); ++i) {
                        if (window.getChildWindow(i).getTitle().startsWith("Attribute")) {
                            if (window.getChildWindow(i).getChildWindowCount() == 1) {
                                window.getChildWindow(i).getChildWindow(0).close();
                            }
                        }
                    }

                    super.windowClosed(window); // To change body of generated methods, choose Tools | Templates.
                }
            });

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
                .getTabAreaProperties()
                .setTabAreaVisiblePolicy(TabAreaVisiblePolicy.ALWAYS);
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
     *
     * @param  id  DOCUMENT ME!
     */
    public void showWindow(final String id) {
        final View view = viewMap.getView(id);

        if (!view.isClosable()) {
            view.restore();

            if (view == vTable) {
                final TreePath selectionPath = pTable.getSelectionPath();

                final Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            ((DefaultTreeModel)pTable.getModel()).reload();
                            if ((selectionPath != null) && (selectionPath.getPath().length > 0)) {
                                pTable.exploreSubtree(selectionPath);
                            }
                        }
                    };

                if (EventQueue.isDispatchThread()) {
                    r.run();
                } else {
                    EventQueue.invokeLater(r);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public StringViewMap getViewMap() {
        return viewMap;
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
        configManager.addConfigurable((GeometryOpButton)cmdGeometryOpMode);
        configManager.configure((GeometryOpButton)cmdGeometryOpMode);
//        configManager.addConfigurable((NewDrawingButton)cmdDrawingMode);
//        configManager.configure((NewDrawingButton)cmdDrawingMode);
        configManager.addConfigurable((SnappingButton)cmdSnappingMode);
        configManager.configure((SnappingButton)cmdSnappingMode);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void topicTreeSelectionChanged(final TreeSelectionEvent e) {
//        final TreePath tp = e.getNewLeadSelectionPath();
        final TreePath tp = pTopicTree.getLeadSelectionPath();
        tbtNewObject.setEnabled(false);
        cmdCopy.setEnabled(false);
        cmdPaste.setEnabled(false);
        cmdDelete.setEnabled(false);
        mniZoomSelectedThemes.setEnabled(false);
        cmdZoomSelectedThemes.setEnabled(false);
        newObjectAction.setSelectedService(null);

        if (tbtNewObject.isSelected() && (e != null)) {
            selectionModeAction.actionPerformed(null);
        }

        if ((tp != null) && (pTopicTree.getSelectionPath() != null) && (pTopicTree.getSelectionPath().length == 1)) {
            final Object o = tp.getLastPathComponent();

            if (o instanceof AbstractFeatureService) {
                final AbstractFeatureService service = (AbstractFeatureService)o;

                if (service.isEditable() && SelectionManager.getInstance().getEditableServices().contains(service)
                            && (service.getLayerProperties() != null)
                            && (service.getLayerProperties().getAttributeTableRuleSet() != null)
                            && (service.getLayerProperties().getAttributeTableRuleSet().getFeatureCreator() != null)) {
                    tbtNewObject.setEnabled(true);
                    newObjectAction.setSelectedService(service);

                    final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures(service);
                    if ((selectedFeatures != null) && !selectedFeatures.isEmpty()) {
                        boolean enable = false;

                        for (final Feature f : selectedFeatures) {
                            if (f instanceof FeatureServiceFeature) {
                                if (((FeatureServiceFeature)f).isEditable()) {
                                    enable = true;
                                    break;
                                }
                            }
                        }

                        if (enable) {
                            cmdDelete.setEnabled(true);
                        }
                    }

                    final AttributeTable table = getAttributeTableByFeatureService(service);

                    if ((table != null) && table.isPasteButtonEnabled()) {
                        cmdPaste.setEnabled(true);
                    }
                }
                final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures(service);

                if ((selectedFeatures != null) && !selectedFeatures.isEmpty()) {
                    cmdCopy.setEnabled(true);
                }
            }
            mniZoomSelectedThemes.setEnabled(true);
            cmdZoomSelectedThemes.setEnabled(true);
        } else if (tp != null) {
            mniZoomSelectedThemes.setEnabled(true);
            cmdZoomSelectedThemes.setEnabled(true);
        }
    }

    /**
     * Initializes the AttributeTableFactory.
     */
    private void initAttributeTable() {
        AttributeTableFactory.getInstance().setMappingComponent(mappingComponent);
        AttributeTableFactory.getInstance().setAttributeTableListener(new AttributeTableListener() {

                @Override
                public void showAttributeTable(final AttributeTable table,
                        final String id,
                        final String name,
                        final String tooltip) {
                    View view = attributeTableMap.get(id);
                    setTabWindow();

                    table.setExportEnabled(isExportEnabled(table.getFeatureService()));

                    if (view != null) {
                        final int viewIndex = tabWindow.getChildWindowIndex(view);

                        if (viewIndex != -1) {
                            tabWindow.setSelectedTab(viewIndex);
                        } else {
                            view.requestFocusInWindow();
                        }
                    } else {
                        String newName = name;

                        if (newName.indexOf(" ") != -1) {
                            newName = newName.substring(newName.indexOf(" ") + 1);
                        }
                        view = new View(newName, null, table);
                        addAttributeTableWindowListener(view, table);
                        viewMap.addView(id, view);
                        attributeTableMap.put(id, view);
                        tabWindow.addTab(view);
                        SelectionManager.getInstance().addConsideredAttributeTable(table);
                    }
                }

                @Override
                public void changeName(final String id, final String name) {
                    final View view = attributeTableMap.get(id);

                    if (view != null) {
                        view.getViewProperties().setTitle(name);
                    }
                }

                @Override
                public void processingModeChanged(final AbstractFeatureService service, final boolean active) {
                    SelectionManager.getInstance().switchProcessingMode(service);

                    if (!active) {
                        splitAction.undo(service);
                        final AttributeTable table = getAttributeTable(AttributeTableFactory.createId(service));

                        if ((table != null) && (service.getLayerProperties() != null)
                                    && (service.getLayerProperties().getFeatureService() != null)
                                    && (service.getLayerProperties().getFeatureService() instanceof CidsLayer)
                                    && ((CidsLayer)service.getLayerProperties().getFeatureService()).getMetaClass()
                                    .getTableName().equalsIgnoreCase("dlm25w.fg_bak")) {
                            final TreeSet<DefaultFeatureServiceFeature> rejectedFeatures =
                                table.getRejectedNewFeatures();
                            final TreeSet<DefaultFeatureServiceFeature> features =
                                new TreeSet<DefaultFeatureServiceFeature>(
                                    rejectedFeatures);

                            if (!features.isEmpty()) {
                                final Thread t = new Thread("removeFeaturesOnRoute") {

                                        @Override
                                        public void run() {
                                            removeCat2ObjectsOnRoutes(features);
                                        }
                                    };

                                t.start();
                            }
                        }

                        if (service instanceof CidsLayer) {
                            if ((service.getLayerProperties() != null)
                                        && (service.getLayerProperties().getFeatureService() != null)
                                        && (((CidsLayer)service.getLayerProperties().getFeatureService())
                                            .getMetaClass() != null)) {
                                final Thread t = new Thread("cleanupCreatedObjects") {

                                        @Override
                                        public void run() {
                                            final int classId = ((CidsLayer)service.getLayerProperties()
                                                            .getFeatureService()).getMetaClass().getID();
                                            cleanupCreatedObjectTable(classId);
                                        }
                                    };

                                t.start();
                            }
                        }

                        if (service instanceof CidsLayer) {
                            final CidsLayer cl = (CidsLayer)service;

                            if (cl.getMetaClass().getName().equals("foto")) {
                                if (vPhoto.isClosable()) {
                                    vPhoto.close();
                                }
                            } else if (cl.getMetaClass().getName().equals("qp")) {
                                if (vGaf.isClosable()) {
                                    vGaf.close();
                                }
                            }
                        }
                    }
                    topicTreeSelectionChanged(null);

                    if (tbtNewObject.isSelected()) {
                        final TreePath[] path = pTopicTree.getSelectionPath();

                        if ((path != null) && (path.length > 1)) {
                            selectionModeAction.actionPerformed(null);
                        } else if ((path != null) && (path.length == 1)
                                    && path[0].getLastPathComponent().equals(service)) {
                            selectionModeAction.actionPerformed(null);
                        }
                    }

                    final List<AbstractFeatureService> editableServices = SelectionManager.getInstance()
                                .getEditableServices();
                    final boolean savePossible = (editableServices != null) && !editableServices.isEmpty();
                    butIntermediateSave.setEnabled(savePossible);

                    if (service.isEditable()
                                && !SelectionManager.getInstance().getEditableServices().contains(service)) {
                        removeObjectsFromMap();
                        final FeatureCreator creator = AppBroker.getInstance().getActiveFeatureCreator();

                        if (creator != null) {
                            creator.cancel();
                        }

                        AppBroker.getInstance().setActiveFeatureCreator(null);
                    }
                }

                private void cleanupCreatedObjectTable(final int classid) {
                    try {
                        final User user = SessionManager.getSession().getUser();
                        final CidsServerSearch cleanup = new CleanupCreatedObjectTable(
                                user.getDomain()
                                        + "."
                                        + user.getName(),
                                classid);
                        SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), cleanup);
                    } catch (Exception e) {
                        LOG.error("Error while removing unused stations", e);
                    }
                }

                private void removeCat2ObjectsOnRoutes(final TreeSet<DefaultFeatureServiceFeature> features) {
                    for (final FeatureServiceFeature feature : features) {
                        try {
                            final CidsServerSearch nodesSearch = new RemoveUnnusedRoute(
                                    feature.getId(),
                                    RemoveUnnusedRoute.FG_BAK);
                            SessionManager.getProxy()
                                    .customServerSearch(SessionManager.getSession().getUser(), nodesSearch);
                        } catch (Exception e) {
                            LOG.error("Error while removing unused stations", e);
                        }

                        final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures();
                        final List<Feature> selectedFeaturesToRemove = new ArrayList<Feature>();

                        for (final Feature f : selectedFeatures) {
                            if (f instanceof CidsLayerFeature) {
                                final CidsLayerFeature clf = (CidsLayerFeature)f;

                                if ((clf.getProperty("ba_cd") != null) && (feature.getProperty("ba_cd") != null)) {
                                    final String selectedFeatureBaCd = String.valueOf(clf.getProperty("ba_cd"));
                                    final String deletedFeatureBaCd = String.valueOf(feature.getProperty("ba_cd"));

                                    if (selectedFeatureBaCd.equals(deletedFeatureBaCd)) {
                                        selectedFeaturesToRemove.add(f);
                                    }
                                }
                            }
                        }

                        for (final String tableId : attributeTableMap.keySet()) {
                            final AttributeTable table = getAttributeTable(tableId);
                            final AbstractFeatureService service = table.getFeatureService();
                            final Object masterBaCd = feature.getProperty("ba_cd");

                            if ((service instanceof CidsLayer)
                                        && ((CidsLayer)service).getMetaClass().getTableName().equalsIgnoreCase(
                                            "dlm25w.fg_bak")) {
                                continue;
                            }

                            if ((table != null) && (service instanceof CidsLayer)) {
                                final AttributeTableRuleSet ruleSet = ((CidsLayer)service).getLayerProperties()
                                            .getAttributeTableRuleSet();
                                final FeatureServiceFeature f = table.getFeatureByRow(0);

                                if (f.getProperty("ba_cd") != null) {
                                    for (int i = 0; i < table.getFeatureCount(); ++i) {
                                        final FeatureServiceFeature fe = table.getFeatureByRow(i);
                                        final Object baCd = fe.getProperty("ba_cd");

                                        if ((baCd != null) && baCd.equals(masterBaCd)) {
                                            if ((ruleSet == null) || ruleSet.isCatThree()) {
                                                fe.setProperty("ba_cd", null);
                                                if (fe.getProperty("ba_st_von") != null) {
                                                    fe.setProperty("ba_st_von", null);
                                                }
                                                if (fe.getProperty("ba_st_bis") != null) {
                                                    fe.setProperty("ba_st_bis", null);
                                                }
                                                if (fe.getProperty("ba_st") != null) {
                                                    fe.setProperty("ba_st", null);
                                                }

                                                if (fe.isEditable()) {
                                                    ((CidsLayerFeature)fe).removeStations();
                                                }
                                            } else {
                                                table.removeFeatureFromModel((DefaultFeatureServiceFeature)fe);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!selectedFeaturesToRemove.isEmpty()) {
                            SelectionManager.getInstance().removeSelectedFeatures(selectedFeaturesToRemove);
                        }
                    }
                }

                @Override
                public void closeAttributeTable(final AbstractFeatureService service) {
                    final View attributeTableView = attributeTableMap.remove(AttributeTableFactory.createId(service));

                    if (attributeTableView != null) {
                        attributeTableView.close();
                    }
                }

                @Override
                public AttributeTable getAttributeTable(final String id) {
                    final View view = attributeTableMap.get(id);

                    if (view != null) {
                        final Component c = view.getComponent();

                        if (c instanceof AttributeTable) {
                            return (AttributeTable)c;
                        }
                    }

                    return null;
                }

                @Override
                public void switchProcessingMode(final AbstractFeatureService service, final String id) {
                    if (!WatergisApp.this.switchProcessingMode(service)) {
                        setTabWindow();
                        int index = -1;

                        if ((tabWindow != null) && (tabWindow.getSelectedWindow() != null)) {
                            index = tabWindow.getChildWindowIndex(tabWindow.getSelectedWindow());
                        }

                        AttributeTableFactory.getInstance().showAttributeTable(service);

                        if ((index != -1) && (index < tabWindow.getChildWindowCount())) {
                            tabWindow.setSelectedTab(index);
                        }

                        final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(
                                WatergisApp.this,
                                true,
                                NbBundle.getMessage(
                                    WatergisApp.class,
                                    "WatergisApp.EditModeMenuItem.actionPerformed().wait"),
                                null,
                                200) {

                                @Override
                                protected Void doInBackground() throws Exception {
                                    final View view = attributeTableMap.get(id);

                                    if (view != null) {
                                        final Component c = view.getComponent();

                                        if (c instanceof AttributeTable) {
                                            final AttributeTable attrTable = (AttributeTable)c;

                                            while (attrTable.isLoading()) {
                                                Thread.sleep(100);
                                            }
                                        }
                                    }

                                    return null;
                                }

                                @Override
                                protected void done() {
                                    WatergisApp.this.switchProcessingMode(service);
                                }
                            };

                        wdt.start();
                    }
                }
            });
    }

    /**
     * Checks, if the download of the given service is allowed.
     *
     * @param   service  the service to check
     *
     * @return  true, iff the download is allowed
     */
    private boolean isExportEnabled(final AbstractFeatureService service) {
        if (service instanceof CidsLayer) {
            final CidsLayer layer = (CidsLayer)service;

            return layer.isDownloadAllowed();
        } else {
            return true;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void setTabWindow() {
        if ((vMap != null) && (vMap.getWindowParent() != null)) {
            if (vMap.getWindowParent() instanceof TabWindow) {
                tabWindow = (TabWindow)vMap.getWindowParent();
            }
        }

        if (tabWindow == null) {
            tabWindow = (TabWindow)((SplitWindow)rootWindow.getWindow()).getRightWindow();
        }
    }

    /**
     * Refreshs the content of the AttributeTable of the given service.
     *
     * @param  service  is used to determine the AttributeTable that should be refreshed
     */
    public void refreshAttributeTable(final AbstractFeatureService service) {
        final View view = attributeTableMap.get(AttributeTableFactory.createId(service));

        if (view != null) {
            final Component c = view.getComponent();

            if (c instanceof AttributeTable) {
                final AttributeTable attrTable = (AttributeTable)c;

                attrTable.reload();
            }
        }
    }

    /**
     * Add the given feature to its attribute table, if it is open.
     *
     * @param  feature  the feature to add
     */
    public void addFeatureToAttributeTable(final FeatureServiceFeature feature) {
        if (feature.getLayerProperties() == null) {
            return;
        }
        final AbstractFeatureService service = feature.getLayerProperties().getFeatureService();
        final View view = attributeTableMap.get(AttributeTableFactory.createId(service));

        if (view != null) {
            final Component c = view.getComponent();

            if (c instanceof AttributeTable) {
                final AttributeTable attrTable = (AttributeTable)c;

                attrTable.addFeature(feature);
            }
        }
    }

    /**
     * Determine the attribute table for the given feature, if it is open.
     *
     * @param   feature  the feature to add
     *
     * @return  DOCUMENT ME!
     */
    public AttributeTable getAttributeTableByFeature(final FeatureServiceFeature feature) {
        if (feature.getLayerProperties() == null) {
            return null;
        }
        final AbstractFeatureService service = feature.getLayerProperties().getFeatureService();

        return getAttributeTableByFeatureService(service);
    }

    /**
     * Determine the attribute table for the given feature, if it is open.
     *
     * @param   service  feature the feature to add
     *
     * @return  DOCUMENT ME!
     */
    public AttributeTable getAttributeTableByFeatureService(final AbstractFeatureService service) {
        final View view = attributeTableMap.get(AttributeTableFactory.createId(service));

        if (view != null) {
            final Component c = view.getComponent();

            if (c instanceof AttributeTable) {
                return (AttributeTable)c;
            }
        }

        return null;
    }

    /**
     * switches the processing mode of the given service.
     *
     * @param   service  DOCUMENT ME!
     *
     * @return  true, if the processing mode was switched
     */
    public boolean switchProcessingMode(final AbstractFeatureService service) {
        return switchProcessingMode(service, false);
    }

    /**
     * switches the processing mode of the given service.
     *
     * @param   service    DOCUMENT ME!
     * @param   forceSave  if true, the changed data will be saved without confirmation
     *
     * @return  true, if the processing mode was switched
     */
    public boolean switchProcessingMode(final AbstractFeatureService service, final boolean forceSave) {
        final View view = attributeTableMap.get(AttributeTableFactory.createId(service));

        if (view != null) {
            final Component c = view.getComponent();

            if (c instanceof AttributeTable) {
                final AttributeTable attrTable = (AttributeTable)c;

                attrTable.changeProcessingMode(forceSave);
                if (attrTable.isProcessingModeActive() && attrTable.isLoading()) {
                    final WaitingDialogThread wdt = new WaitingDialogThread(
                            this,
                            true,
                            "Bearbeitungsmodus wird gestartet",
                            null,
                            500) {

                            @Override
                            protected Object doInBackground() throws Exception {
                                while (attrTable.isLoading()) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ex) {
                                        LOG.warn("interrupted", ex);
                                    }
                                }

                                return null;
                            }
                        };
                    wdt.start();
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Determines, if the processing mode of the given service is active.
     *
     * @param   service  DOCUMENT ME!
     *
     * @return  true, if the processing mode is active
     */
    public boolean isProcessingModeActive(final AbstractFeatureService service) {
        final View view = attributeTableMap.get(AttributeTableFactory.createId(service));

        if (view != null) {
            final Component c = view.getComponent();

            if (c instanceof AttributeTable) {
                final AttributeTable attrTable = (AttributeTable)c;

                return attrTable.isProcessingModeActive();
            }
        }

        return false;
    }

    /**
     * Adds the window listener to the given view.
     *
     * @param  view   the view to add the listener
     * @param  table  the AttributeTable that is used inside the view
     */
    private void addAttributeTableWindowListener(final View view, final AttributeTable table) {
        final AttributeTableAncestorListener aListener = new AttributeTableAncestorListener(view, table);
        final DockingWindowListener listener = new DockingWindowAdapter() {

                @Override
                public void windowClosing(final DockingWindow window) throws OperationAbortedException {
                    final boolean disposeCompleted = table.dispose();

                    if (!disposeCompleted) {
                        throw new OperationAbortedException();
                    }
                }

                @Override
                public void windowClosed(final DockingWindow window) {
                    disposeTable(view, table, aListener, this);
                }
            };

        aListener.setDockingWindowListener(listener);
        view.addAncestorListener(aListener);
        view.addListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  view       DOCUMENT ME!
     * @param  table      DOCUMENT ME!
     * @param  aListener  DOCUMENT ME!
     * @param  listener   DOCUMENT ME!
     */
    private void disposeTable(final View view,
            final AttributeTable table,
            final AncestorListener aListener,
            final DockingWindowListener listener) {
        view.removeListener(listener);
        view.removeAncestorListener(aListener);
        if (view.getParent() != null) {
            view.getParent().remove(view);
        }
        viewMap.removeView("Attributtabelle " + table.getFeatureService().getName());
        attributeTableMap.remove(AttributeTableFactory.createId(table.getFeatureService()));

        SelectionManager.getInstance().removeConsideredAttributeTable(table);

        // The view is not removed from the root window and this will cause that the layout cannot be saved
        // when the application will be closed. So rootWindow.removeView(view) must be invoked. But without
        // the invocation of view.close(), the invocation of rootWindow.removeView(view) will do nothing To
        // avoid an infinite loop, view.removeListener(this) must be invoked before view.close();
        view.close();
        rootWindow.removeView(view);
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

        ObjectInputStream in = null;
        InputStream layoutInput = null;

        try {
            layoutInput = WatergisApp.class.getResourceAsStream(
                    "/de/cismet/watergis/configuration/defaultLayout.layout");
            if (layoutInput != null) {
                in = new ObjectInputStream(layoutInput);
                rootWindow.read(in);
                in.close();
            }
        } catch (Exception e) {
            LOG.error("Error while loading default layout", e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

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
        mergeAction = new de.cismet.watergis.gui.actions.MergeAction();
        splitAction = new de.cismet.watergis.gui.actions.SplitAction();
        releaseAction = new de.cismet.watergis.gui.actions.ReleaseAction();
        annexAction = new de.cismet.watergis.gui.actions.AnnexAction();
        measureLineAction = new de.cismet.watergis.gui.actions.map.MeasureLineAction();
        flipAction = new de.cismet.watergis.gui.actions.map.FlipAction();
        intermediateSaveAction = new de.cismet.watergis.gui.actions.IntermediateSaveAction();
        gWKConnectionCheckAction = new de.cismet.watergis.gui.actions.checks.GWKConnectionCheckAction();
        moveModeAction = new de.cismet.watergis.gui.actions.map.MoveModeAction();
        newLinestringModeAction = new de.cismet.watergis.gui.actions.map.NewLinestringModeAction();
        newRectangleModeAction = new de.cismet.watergis.gui.actions.map.NewRectangleModeAction();
        newMarkerModeAction = new de.cismet.watergis.gui.actions.map.NewMarkerModeAction();
        removeDrawingModeAction = new de.cismet.watergis.gui.actions.map.RemoveDrawingModeAction();
        newTextModeAction = new de.cismet.watergis.gui.actions.map.NewTextModeAction();
        loadDrawingsAction = new de.cismet.watergis.gui.actions.map.LoadDrawingsAction();
        saveDrawingsAction = new de.cismet.watergis.gui.actions.map.SaveDrawingsAction();
        selectAllDrawingsAction = new de.cismet.watergis.gui.actions.selection.SelectAllDrawingsAction();
        bufferGeoprocessingAction = new de.cismet.watergis.gui.actions.geoprocessing.BufferGeoprocessingAction();
        clipGeoprocessingAction = new de.cismet.watergis.gui.actions.geoprocessing.ClipGeoprocessingAction();
        dissolveGeoprocessingAction = new de.cismet.watergis.gui.actions.geoprocessing.DissolveGeoprocessingAction();
        mergeGeoprocessingAction = new de.cismet.watergis.gui.actions.geoprocessing.MergeGeoprocessingAction();
        pointInLineGeoprocessingAction =
            new de.cismet.watergis.gui.actions.geoprocessing.PointInLineGeoprocessingAction();
        pointInPolygonGeoprocessingAction =
            new de.cismet.watergis.gui.actions.geoprocessing.PointInPolygonGeoprocessingAction();
        unionGeoprocessingAction = new de.cismet.watergis.gui.actions.geoprocessing.UnionGeoprocessingAction();
        basicRoutesCheckAction1 = new de.cismet.watergis.gui.actions.checks.BasicRoutesCheckAction();
        exportAction1 = new de.cismet.watergis.gui.actions.ExportAction();
        lawaCheckAction = new de.cismet.watergis.gui.actions.checks.LawaCheckAction();
        verwaltungCheckAction = new de.cismet.watergis.gui.actions.checks.VerwaltungCheckAction();
        sonstigeCheckAction = new de.cismet.watergis.gui.actions.checks.SonstigeCheckAction();
        bauwerkeCheckAction = new de.cismet.watergis.gui.actions.checks.BauwerkeCheckAction();
        ausbauCheckAction = new de.cismet.watergis.gui.actions.checks.AusbauCheckAction();
        exportOptionAction = new de.cismet.watergis.gui.actions.ExportOptionAction();
        exportIgmAction = new de.cismet.watergis.gui.actions.ExportIgmAction();
        gewaesserReportAction = new de.cismet.watergis.gui.actions.reports.GewaesserReportAction();
        gemeindenReportAction = new de.cismet.watergis.gui.actions.reports.KatasterGemeindenReportAction();
        sbReportAction = new de.cismet.watergis.gui.actions.reports.KatasterSbReportAction();
        gewaesserGewReportAction = new de.cismet.watergis.gui.actions.reports.KatasterGewaesserReportAction();
        flaechenReportAction1 = new de.cismet.watergis.gui.actions.reports.KatasterFlaechenReportAction();
        rlDDueReportAction = new de.cismet.watergis.gui.actions.reports.GerinneGGemeindeReportAction();
        fotoInfoAction1 = new de.cismet.watergis.gui.actions.foto.FotoInfoAction();
        deleteAction1 = new de.cismet.watergis.gui.actions.foto.DeleteAction();
        exportActionPhoto = new de.cismet.watergis.gui.actions.foto.ExportAction();
        reportAction1 = new de.cismet.watergis.gui.actions.foto.ReportAction();
        uploadAction1 = new de.cismet.watergis.gui.actions.foto.UploadAction();
        optionAction1 = new de.cismet.watergis.gui.actions.foto.OptionAction();
        checkAction1 = new de.cismet.watergis.gui.actions.gaf.CheckAction();
        uploadActionGaf = new de.cismet.watergis.gui.actions.gaf.UploadAction();
        reportActionGaf = new de.cismet.watergis.gui.actions.gaf.ReportAction();
        optionActionGaf = new de.cismet.watergis.gui.actions.gaf.OptionAction();
        exportActionGaf = new de.cismet.watergis.gui.actions.gaf.ExportAction();
        deleteActionGaf = new de.cismet.watergis.gui.actions.gaf.DeleteAction();
        gafInfoAction = new de.cismet.watergis.gui.actions.gaf.GafInfoAction();
        gerinneGGewaesserReportAction1 = new de.cismet.watergis.gui.actions.reports.GerinneGGewaesserReportAction();
        optionModeAction1 = new de.cismet.watergis.gui.actions.map.OptionModeAction();
        gerinneGSbReportAction1 = new de.cismet.watergis.gui.actions.reports.GerinneGSbReportAction();
        gerinneGFlReportAction1 = new de.cismet.watergis.gui.actions.reports.GerinneGFlReportAction();
        wkFgReportAction = new de.cismet.watergis.gui.actions.reports.WkFgReportAction();
        unselectAllDrawingsAction = new de.cismet.watergis.gui.actions.selection.UnselectAllDrawingsAction();
        zoomSelectedDrawingsAction = new de.cismet.watergis.gui.actions.selection.ZoomSelectedDrawingsAction();
        zoomAllDrawingsAction = new de.cismet.watergis.gui.actions.selection.ZoomAllDrawingsAction();
        reloadAction1 = new de.cismet.watergis.gui.actions.map.ReloadAction();
        drawingGroup = new javax.swing.ButtonGroup();
        gerogaRsAction1 = new de.cismet.watergis.gui.actions.reports.GerogaRsAction();
        gerinneOGemeindeReportAction = new de.cismet.watergis.gui.actions.reports.GerinneOGemeindeReportAction();
        gerinneOGewReportAction = new de.cismet.watergis.gui.actions.reports.GerinneOGewReportAction();
        gerinneOSbReportAction = new de.cismet.watergis.gui.actions.reports.GerinneOSbReportAction();
        gerinneOFlReportAction1 = new de.cismet.watergis.gui.actions.reports.GerinneOFlReportAction();
        showMap = new ShowWindowAction(org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.initInfoNode().Map"));
        showTree = new ShowWindowAction(org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.initInfoNode().TopicTree"));
        showOverview = new ShowWindowAction(org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.initInfoNode().Overview"));
        showDatasource = new ShowWindowAction(org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.initInfoNode().Capabilities"));
        showInfo = new de.cismet.watergis.gui.actions.ShowWindowAction();
        showProblems = new ShowWindowAction(org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.initInfoNode().Table"));
        showPhoto = new ShowWindowAction(org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.initInfoNode().Photo"));
        showProfiles = new ShowWindowAction(org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.initInfoNode().Gaf"));
        showInfoWindowAction = new InfoWindowAction(true);
        defaultConfigAction1 = new de.cismet.watergis.gui.actions.DefaultConfigAction();
        defaultConfig2Action1 = new de.cismet.watergis.gui.actions.DefaultConfig2Action();
        copyObjectAction1 = new de.cismet.watergis.gui.actions.map.CopyObjectAction();
        pasteObjectAction1 = new de.cismet.watergis.gui.actions.map.PasteObjectAction();
        deleteObjectAction1 = new de.cismet.watergis.gui.actions.map.DeleteObjectAction();
        saveToSameFileProjectAction1 = new de.cismet.watergis.gui.actions.SaveToSameFileProjectAction();
        zoomSelectedThemesAction1 = new de.cismet.watergis.gui.actions.selection.ZoomSelectedThemesAction();
        simpleMemoryMonitoringToolbarWidget1 = new SimpleMemoryMonitoringToolbarWidget();
        showLegend = new ShowWindowAction(org.openide.util.NbBundle.getMessage(
                    WatergisApp.class,
                    "WatergisApp.initInfoNode().Legend"));
        removeModeAction1 = new de.cismet.watergis.gui.actions.map.RemoveModeAction();
        changePolygonModeAction1 = new de.cismet.watergis.gui.actions.map.ChangePolygonModeAction();
        stationAction1 = new de.cismet.watergis.gui.actions.geoprocessing.StationAction();
        addThemeAction1 = new de.cismet.watergis.gui.actions.AddThemeAction();
        createViewsForUser1 = new de.cismet.watergis.gui.actions.CreateViewsForUser();
        techProfAction1 = new de.cismet.watergis.gui.actions.geoprocessing.TechProfAction();
        wsgAction1 = new de.cismet.watergis.gui.actions.reports.WsgAction();
        rechtsgrundlageAction1 = new de.cismet.watergis.gui.actions.reports.RechtsgrundlageAction();
        refreshViewsForUser1 = new de.cismet.watergis.gui.actions.RefreshViewsForUser();
        wsgWaldAction1 = new de.cismet.watergis.gui.actions.reports.WsgWaldAction();
        tobDLM25W = new javax.swing.JToolBar();
        cmdOpenProject = new javax.swing.JButton();
        cmdSaveSameFileProject = new javax.swing.JButton();
        cmdSaveProject = new javax.swing.JButton();
        cmdPrint = new javax.swing.JButton();
        cmdExportMap1 = new javax.swing.JButton();
        cmdExportMap2 = new javax.swing.JButton();
        cmdGeoLink = new javax.swing.JButton();
        cmdDownloadManager = new javax.swing.JButton();
        cmdDownloadManager1 = new javax.swing.JButton();
        jSeparator14 = new javax.swing.JToolBar.Separator();
        cboScale = new ScaleJComboBox();
        lblPlaceholder = new javax.swing.JLabel();
        panSearch = new javax.swing.JPanel();
        routeZoomPanel = new de.cismet.watergis.gui.panels.RouteZoomPanel();
        cboTheme = new javax.swing.JComboBox();
        jSeparator15 = new javax.swing.JToolBar.Separator();
        cmdRefresh = new javax.swing.JButton();
        tbtnZoomMode = new javax.swing.JToggleButton();
        cmdZoomIn = new javax.swing.JButton();
        cmdZoomOut = new javax.swing.JButton();
        tbtnPanMode = new javax.swing.JToggleButton();
        cmdGoTo = new javax.swing.JButton();
        cmdFullExtend = new javax.swing.JButton();
        cmdPreviousExtend = new JHistoryButton(false);
        cmdNextExtend = new JHistoryButton(false);
        cmdSelectionMode = new SelectionButton();
        cmdSelectionAttribute = new javax.swing.JButton();
        cmdSelectionLocation = new javax.swing.JButton();
        cmdZoomSelectedObjects = new javax.swing.JButton();
        cmdZoomSelectedThemes = new javax.swing.JButton();
        cmdSelectAll = new javax.swing.JButton();
        cmdInvertSelection = new javax.swing.JButton();
        cmdRemoveSelectionAllThemes = new javax.swing.JButton();
        tbtnInfo = new javax.swing.JToggleButton();
        tbtnMeasure = new MeasureButton();
        tbtnMeasureLineMode = new javax.swing.JToggleButton();
        cmdFreeStat = new javax.swing.JButton();
        cmdAddBookmark = new javax.swing.JButton();
        cmdManageBookmarks = new javax.swing.JButton();
        tbtnPhotoInfoMode = new javax.swing.JToggleButton();
        tbtnProfileInfoMode = new javax.swing.JToggleButton();
        cmdOnlineHelp = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        tbtNewObject = new javax.swing.JToggleButton();
        butIntermediateSave = new javax.swing.JButton();
        cmdUndo = new javax.swing.JButton();
        cmdNodeMove = new javax.swing.JToggleButton();
        cmdNodeAdd = new javax.swing.JToggleButton();
        cmdNodeRemove = new javax.swing.JToggleButton();
        tbtnAddMode = new javax.swing.JToggleButton();
        tbtnRemoveMode = new javax.swing.JToggleButton();
        cmdSnappingMode = new SnappingButton();
        cmdPresentation = new javax.swing.JButton();
        cmdMoveGeometry = new javax.swing.JToggleButton();
        cmdMerge = new javax.swing.JButton();
        cmdSplit = new javax.swing.JButton();
        cmdRelease = new javax.swing.JButton();
        cmdAnnex = new javax.swing.JButton();
        cmdCopy = new javax.swing.JButton();
        cmdPaste = new javax.swing.JButton();
        cmdDelete = new javax.swing.JButton();
        cmdGeometryOpMode = new GeometryOpButton();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        cmdLoadDrawings = new javax.swing.JButton();
        cmdSaveDrawings = new javax.swing.JButton();
        cmdNewPolygon = new javax.swing.JToggleButton();
        cmdNewLinestring = new javax.swing.JToggleButton();
        cmdNewPoint = new javax.swing.JToggleButton();
        cmdNewText = new javax.swing.JToggleButton();
        cmdSelectAllDrawings = new javax.swing.JButton();
        cmdUnselectDrawings = new javax.swing.JButton();
        cmdZoomToAllDrawings = new javax.swing.JButton();
        cmdZoomToSelectedDrawings = new javax.swing.JButton();
        cmdRemoveGeometry = new javax.swing.JButton();
        cmdDrawingOptions = new javax.swing.JButton();
        cmdDrawingMode = new NewDrawingButton();
        statusPanel1 = new de.cismet.watergis.gui.panels.StatusPanel();
        panMain = new javax.swing.JPanel();
        statusBar1 = new de.cismet.watergis.gui.panels.StatusBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        menFile = new FileMenu();
        mniOpenProject = new javax.swing.JMenuItem();
        mniSaveProject1 = new javax.swing.JMenuItem();
        mniSaveProject = new javax.swing.JMenuItem();
        mniOverview = new javax.swing.JMenuItem();
        mniPrint = new javax.swing.JMenuItem();
        mniSaveMapToFile = new javax.swing.JMenuItem();
        mniExportMap = new javax.swing.JMenuItem();
        mniCreateGeoLink = new javax.swing.JMenuItem();
        mniDownloadManager = new javax.swing.JMenuItem();
        mniFileOptions = new javax.swing.JMenuItem();
        sepCentralFilesStart = new javax.swing.JPopupMenu.Separator();
        sepCentralFilesEnd = new javax.swing.JPopupMenu.Separator();
        sepLocalFilesEnd = new javax.swing.JPopupMenu.Separator();
        mniClose = new javax.swing.JMenuItem();
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
        mniZoomSelectedThemes = new javax.swing.JMenuItem();
        mniRemoveSelection = new javax.swing.JMenuItem();
        menTools = new javax.swing.JMenu();
        menGeoProcessing = new javax.swing.JMenu();
        mniBuffer = new javax.swing.JMenuItem();
        mniClip = new javax.swing.JMenuItem();
        mniUnion = new javax.swing.JMenuItem();
        mniMerge = new javax.swing.JMenuItem();
        mniDissolve = new javax.swing.JMenuItem();
        mniPointInLine = new javax.swing.JMenuItem();
        mniPointInPolygon = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mniStation = new javax.swing.JMenuItem();
        mniTechProf = new javax.swing.JMenuItem();
        menChecks = new javax.swing.JMenu();
        menBasicChecks = new javax.swing.JMenu();
        mniCheckBasisRoutes = new javax.swing.JMenuItem();
        mniCheckVerwaltung = new javax.swing.JMenuItem();
        mniCheckAusbau = new javax.swing.JMenuItem();
        mniCheckBauwerke = new javax.swing.JMenuItem();
        mniCheckSonstige = new javax.swing.JMenuItem();
        menExtendedChecks = new javax.swing.JMenu();
        mniCheckLawaConnection = new javax.swing.JMenuItem();
        mniCheckLawa = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        menChecks1 = new javax.swing.JMenu();
        mniExport = new javax.swing.JMenuItem();
        mniIgmExport = new javax.swing.JMenuItem();
        mniExportOption = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        mniCreateDbUser = new javax.swing.JMenuItem();
        mniRefreshDbUser = new javax.swing.JMenuItem();
        menReport = new javax.swing.JMenu();
        menSteckbrief = new javax.swing.JMenu();
        menSteckbriefGewaesser = new javax.swing.JMenu();
        mniGewaesser = new javax.swing.JMenuItem();
        menSteckbriefWasserkoerper = new javax.swing.JMenu();
        mniFG = new javax.swing.JMenuItem();
        menSteckbriefPhotos = new javax.swing.JMenu();
        mniPrintPhoto = new javax.swing.JMenuItem();
        menSteckbriefQp = new javax.swing.JMenu();
        mniPrintQp = new javax.swing.JMenuItem();
        menStatistik = new javax.swing.JMenu();
        menGewaesser = new javax.swing.JMenu();
        mniGewaesserRep = new javax.swing.JMenuItem();
        mniGemeinde = new javax.swing.JMenuItem();
        mniSb = new javax.swing.JMenuItem();
        mniFl = new javax.swing.JMenuItem();
        menGewaesser2 = new javax.swing.JMenu();
        mniGewaesserRep2 = new javax.swing.JMenuItem();
        mniGemeinde2 = new javax.swing.JMenuItem();
        mniSb2 = new javax.swing.JMenuItem();
        mniFl2 = new javax.swing.JMenuItem();
        menGewaesser1 = new javax.swing.JMenu();
        mniGewaesserRep1 = new javax.swing.JMenuItem();
        mniGemeinde1 = new javax.swing.JMenuItem();
        mniSb1 = new javax.swing.JMenuItem();
        mniFl1 = new javax.swing.JMenuItem();
        menGewaesser3 = new javax.swing.JMenu();
        mniGewaesserRep3 = new javax.swing.JMenuItem();
        menSchablonen = new javax.swing.JMenu();
        menWsg = new javax.swing.JMenu();
        mniFlNu = new javax.swing.JMenuItem();
        mniRechtsgrundlage = new javax.swing.JMenuItem();
        mniRechtsgrundlage1 = new javax.swing.JMenuItem();
        menPhoto = new javax.swing.JMenu();
        mniUpload = new javax.swing.JMenuItem();
        mniPhotoInfo = new javax.swing.JMenuItem();
        mniReportPhoto = new javax.swing.JMenuItem();
        mniExportPhoto = new javax.swing.JMenuItem();
        mniDeletePhoto = new javax.swing.JMenuItem();
        mniPhotoOptions = new javax.swing.JMenuItem();
        menProfiles = new javax.swing.JMenu();
        mniGafUpload = new javax.swing.JMenuItem();
        mniGafInfo = new javax.swing.JMenuItem();
        mniReportGaf = new javax.swing.JMenuItem();
        mniExportGaf = new javax.swing.JMenuItem();
        mniDeleteGaf = new javax.swing.JMenuItem();
        mniGafOptions = new javax.swing.JMenuItem();
        menBookmark = new javax.swing.JMenu();
        mniCreateBookmark = new javax.swing.JMenuItem();
        mniManageBookmarks = new javax.swing.JMenuItem();
        menDrawings = new javax.swing.JMenu();
        mniLoadDrawings = new javax.swing.JMenuItem();
        mniSaveDrawings = new javax.swing.JMenuItem();
        sepDrawingOperation = new javax.swing.JPopupMenu.Separator();
        mniNewRectangleDrawing = new javax.swing.JMenuItem();
        mniNewLineStringDrawing = new javax.swing.JMenuItem();
        mniNewMarkerDrawing = new javax.swing.JMenuItem();
        mniNewTextDrawing = new javax.swing.JMenuItem();
        sepDrawingOperation1 = new javax.swing.JPopupMenu.Separator();
        mniSelectAllDrawing = new javax.swing.JMenuItem();
        mniUnselectAllDrawing = new javax.swing.JMenuItem();
        mniZoomAllDrawings = new javax.swing.JMenuItem();
        mniZoomSelectedDrawings = new javax.swing.JMenuItem();
        mniRemoveDrawing = new javax.swing.JMenuItem();
        mniDrawingOptions = new javax.swing.JMenuItem();
        menWindow = new javax.swing.JMenu();
        mniShowMap = new javax.swing.JMenuItem();
        mniShowTree = new javax.swing.JMenuItem();
        mniShowOverview = new javax.swing.JMenuItem();
        mniShowDatasource = new javax.swing.JMenuItem();
        mniShowInfo = new javax.swing.JMenuItem();
        mniShowProblems = new javax.swing.JMenuItem();
        mniShowPhotos = new javax.swing.JMenuItem();
        mniShowProfiles = new javax.swing.JMenuItem();
        mniShowLegend = new javax.swing.JMenuItem();
        sepWindowSeparator = new javax.swing.JPopupMenu.Separator();
        mniDefaultConfig = new javax.swing.JMenuItem();
        mniShowDefaultConfig2 = new javax.swing.JMenuItem();
        menHelp = new javax.swing.JMenu();
        mniHelp = new javax.swing.JMenuItem();
        mniInfo = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.title")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        tobDLM25W.setFloatable(false);
        tobDLM25W.setRollover(true);
        tobDLM25W.setMaximumSize(new java.awt.Dimension(2000, 28));
        tobDLM25W.setMinimumSize(new java.awt.Dimension(200, 28));
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

        cmdSaveSameFileProject.setAction(saveToSameFileProjectAction1);
        cmdSaveSameFileProject.setFocusable(false);
        cmdSaveSameFileProject.setHideActionText(true);
        cmdSaveSameFileProject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSaveSameFileProject.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSaveSameFileProject.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSaveSameFileProject.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSaveSameFileProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdSaveSameFileProject);

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

        cmdExportMap1.setAction(exportMapToFileAction);
        cmdExportMap1.setFocusable(false);
        cmdExportMap1.setHideActionText(true);
        cmdExportMap1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdExportMap1.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdExportMap1.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdExportMap1.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdExportMap1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdExportMap1);

        cmdExportMap2.setAction(exportMapAction);
        cmdExportMap2.setFocusable(false);
        cmdExportMap2.setHideActionText(true);
        cmdExportMap2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdExportMap2.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdExportMap2.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdExportMap2.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdExportMap2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdExportMap2);

        cmdGeoLink.setAction(createGeoLinkAction);
        cmdGeoLink.setFocusable(false);
        cmdGeoLink.setHideActionText(true);
        cmdGeoLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdGeoLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdGeoLink);

        cmdDownloadManager.setAction(downloadManagerAction);
        cmdDownloadManager.setFocusable(false);
        cmdDownloadManager.setHideActionText(true);
        cmdDownloadManager.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdDownloadManager.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdDownloadManager);

        cmdDownloadManager1.setAction(addThemeAction1);
        cmdDownloadManager1.setFocusable(false);
        cmdDownloadManager1.setHideActionText(true);
        cmdDownloadManager1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdDownloadManager1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdDownloadManager1);

        jSeparator14.setOpaque(true);
        jSeparator14.setSeparatorSize(new java.awt.Dimension(20, 20));
        tobDLM25W.add(jSeparator14);

        cboScale.setEditable(true);
        cboScale.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cboScale.toolTipText")); // NOI18N
        cboScale.setMaximumSize(new java.awt.Dimension(90, 24));
        cboScale.setMinimumSize(new java.awt.Dimension(90, 24));
        cboScale.setPreferredSize(new java.awt.Dimension(90, 24));
        tobDLM25W.add(cboScale);

        lblPlaceholder.setMaximumSize(new java.awt.Dimension(4, 1));
        lblPlaceholder.setMinimumSize(new java.awt.Dimension(4, 1));
        lblPlaceholder.setPreferredSize(new java.awt.Dimension(4, 1));
        tobDLM25W.add(lblPlaceholder);

        panSearch.setMaximumSize(new java.awt.Dimension(240, 24));
        panSearch.setMinimumSize(new java.awt.Dimension(240, 24));
        panSearch.setPreferredSize(new java.awt.Dimension(240, 24));
        panSearch.setLayout(new java.awt.GridBagLayout());

        routeZoomPanel.setMaximumSize(new java.awt.Dimension(240, 24));
        routeZoomPanel.setMinimumSize(new java.awt.Dimension(245, 24));
        routeZoomPanel.setPreferredSize(new java.awt.Dimension(245, 24));
        panSearch.add(routeZoomPanel, new java.awt.GridBagConstraints());

        tobDLM25W.add(panSearch);

        cboTheme.setEditable(true);
        cboTheme.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cboTheme.toolTipText_1")); // NOI18N
        cboTheme.setMaximumSize(new java.awt.Dimension(163, 24));
        cboTheme.setMinimumSize(new java.awt.Dimension(163, 24));
        cboTheme.setPreferredSize(new java.awt.Dimension(163, 24));
        cboTheme.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cboThemeItemStateChanged(evt);
                }
            });
        tobDLM25W.add(cboTheme);

        jSeparator15.setOpaque(true);
        jSeparator15.setSeparatorSize(new java.awt.Dimension(20, 20));
        tobDLM25W.add(jSeparator15);

        cmdRefresh.setAction(reloadAction1);
        cmdRefresh.setFocusable(false);
        cmdRefresh.setHideActionText(true);
        cmdRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRefresh.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdRefresh.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdRefresh.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdRefresh);

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
        cmdGoTo.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdGoToActionPerformed(evt);
                }
            });
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
        cmdZoomSelectedObjects.setEnabled(false);
        cmdZoomSelectedObjects.setFocusable(false);
        cmdZoomSelectedObjects.setHideActionText(true);
        cmdZoomSelectedObjects.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomSelectedObjects.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedObjects.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedObjects.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedObjects.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdZoomSelectedObjects);

        cmdZoomSelectedThemes.setAction(zoomSelectedThemesAction1);
        cmdZoomSelectedThemes.setEnabled(false);
        cmdZoomSelectedThemes.setFocusable(false);
        cmdZoomSelectedThemes.setHideActionText(true);
        cmdZoomSelectedThemes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomSelectedThemes.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedThemes.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedThemes.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomSelectedThemes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdZoomSelectedThemes);

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

        cmdRemoveSelectionAllThemes.setAction(removeSelectionAllTopicsAction);
        cmdRemoveSelectionAllThemes.setEnabled(false);
        cmdRemoveSelectionAllThemes.setFocusable(false);
        cmdRemoveSelectionAllThemes.setHideActionText(true);
        cmdRemoveSelectionAllThemes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRemoveSelectionAllThemes.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAllThemes.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAllThemes.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdRemoveSelectionAllThemes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdRemoveSelectionAllThemes);

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

        tbtnMeasureLineMode.setAction(measureLineAction);
        btnGroupMapMode.add(tbtnMeasureLineMode);
        tbtnMeasureLineMode.setFocusable(false);
        tbtnMeasureLineMode.setHideActionText(true);
        tbtnMeasureLineMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnMeasureLineMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(tbtnMeasureLineMode);

        cmdFreeStat.setAction(stationAction1);
        cmdFreeStat.setFocusable(false);
        cmdFreeStat.setHideActionText(true);
        cmdFreeStat.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdFreeStat.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdFreeStat);

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

        tbtnPhotoInfoMode.setAction(fotoInfoAction1);
        btnGroupMapMode.add(tbtnPhotoInfoMode);
        tbtnPhotoInfoMode.setFocusable(false);
        tbtnPhotoInfoMode.setHideActionText(true);
        tbtnPhotoInfoMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnPhotoInfoMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(tbtnPhotoInfoMode);

        tbtnProfileInfoMode.setAction(gafInfoAction);
        btnGroupMapMode.add(tbtnProfileInfoMode);
        tbtnProfileInfoMode.setFocusable(false);
        tbtnProfileInfoMode.setHideActionText(true);
        tbtnProfileInfoMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnProfileInfoMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(tbtnProfileInfoMode);

        cmdOnlineHelp.setAction(onlineHelpAction);
        cmdOnlineHelp.setBorderPainted(false);
        cmdOnlineHelp.setFocusPainted(false);
        cmdOnlineHelp.setFocusable(false);
        cmdOnlineHelp.setHideActionText(true);
        cmdOnlineHelp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdOnlineHelp.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdOnlineHelp.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdOnlineHelp.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdOnlineHelp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tobDLM25W.add(cmdOnlineHelp);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(tobDLM25W, gridBagConstraints);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(2000, 28));
        jToolBar1.setMinimumSize(new java.awt.Dimension(200, 28));
        jToolBar1.setPreferredSize(new java.awt.Dimension(691, 28));

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.jLabel1.text", new Object[] {})); // NOI18N
        jToolBar1.add(jLabel1);

        tbtNewObject.setAction(newObjectAction);
        btnGroupMapMode.add(tbtNewObject);
        tbtNewObject.setFocusable(false);
        tbtNewObject.setHideActionText(true);
        tbtNewObject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtNewObject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(tbtNewObject);

        butIntermediateSave.setAction(intermediateSaveAction);
        butIntermediateSave.setEnabled(false);
        butIntermediateSave.setFocusable(false);
        butIntermediateSave.setHideActionText(true);
        butIntermediateSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butIntermediateSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butIntermediateSave);

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
        jToolBar1.add(cmdUndo);

        editGroup.add(cmdNodeMove);
        cmdNodeMove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/moveNodes.png"))); // NOI18N
        cmdNodeMove.setSelected(true);
        cmdNodeMove.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdNodeMove.toolTipText"));                                                 // NOI18N
        cmdNodeMove.setBorderPainted(false);
        cmdNodeMove.setEnabled(false);
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
        jToolBar1.add(cmdNodeMove);

        editGroup.add(cmdNodeAdd);
        cmdNodeAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/insertNodes.png"))); // NOI18N
        cmdNodeAdd.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdNodeAdd.toolTipText"));                                                   // NOI18N
        cmdNodeAdd.setBorderPainted(false);
        cmdNodeAdd.setEnabled(false);
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
        jToolBar1.add(cmdNodeAdd);

        editGroup.add(cmdNodeRemove);
        cmdNodeRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/removeNodes.png"))); // NOI18N
        cmdNodeRemove.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdNodeRemove.toolTipText"));                                                   // NOI18N
        cmdNodeRemove.setBorderPainted(false);
        cmdNodeRemove.setEnabled(false);
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
        jToolBar1.add(cmdNodeRemove);

        tbtnAddMode.setAction(changePolygonModeAction1);
        btnGroupMapMode.add(tbtnAddMode);
        tbtnAddMode.setEnabled(false);
        tbtnAddMode.setFocusable(false);
        tbtnAddMode.setHideActionText(true);
        tbtnAddMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnAddMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(tbtnAddMode);

        tbtnRemoveMode.setAction(removeModeAction1);
        btnGroupMapMode.add(tbtnRemoveMode);
        tbtnRemoveMode.setEnabled(false);
        tbtnRemoveMode.setFocusable(false);
        tbtnRemoveMode.setHideActionText(true);
        tbtnRemoveMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbtnRemoveMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(tbtnRemoveMode);

        cmdSnappingMode.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-snaptogrid.png"))); // NOI18N
        cmdSnappingMode.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdSnappingMode.toolTipText",
                new Object[] {}));                                                               // NOI18N
        cmdSnappingMode.setFocusable(false);
        cmdSnappingMode.setHideActionText(true);
        cmdSnappingMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSnappingMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdSnappingMode);

        cmdPresentation.setAction(flipAction);
        cmdPresentation.setEnabled(false);
        cmdPresentation.setFocusable(false);
        cmdPresentation.setHideActionText(true);
        cmdPresentation.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPresentation.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdPresentation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdPresentation);

        cmdMoveGeometry.setAction(moveModeAction);
        btnGroupMapMode.add(cmdMoveGeometry);
        cmdMoveGeometry.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdMoveGeometry.toolTipText")); // NOI18N
        cmdMoveGeometry.setBorderPainted(false);
        cmdMoveGeometry.setEnabled(false);
        cmdMoveGeometry.setFocusPainted(false);
        cmdMoveGeometry.setFocusable(false);
        cmdMoveGeometry.setHideActionText(true);
        cmdMoveGeometry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdMoveGeometry.setMaximumSize(new java.awt.Dimension(29, 29));
        cmdMoveGeometry.setMinimumSize(new java.awt.Dimension(29, 29));
        cmdMoveGeometry.setPreferredSize(new java.awt.Dimension(29, 29));
        cmdMoveGeometry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdMoveGeometry);

        cmdMerge.setAction(mergeAction);
        cmdMerge.setBorderPainted(false);
        cmdMerge.setEnabled(false);
        cmdMerge.setFocusPainted(false);
        cmdMerge.setFocusable(false);
        cmdMerge.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdMerge.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdMerge);

        cmdSplit.setAction(splitAction);
        cmdSplit.setBorderPainted(false);
        cmdSplit.setEnabled(false);
        cmdSplit.setFocusPainted(false);
        cmdSplit.setFocusable(false);
        cmdSplit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSplit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdSplit);

        cmdRelease.setAction(releaseAction);
        cmdRelease.setBorderPainted(false);
        cmdRelease.setEnabled(false);
        cmdRelease.setFocusPainted(false);
        cmdRelease.setFocusable(false);
        cmdRelease.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRelease.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdRelease);

        cmdAnnex.setAction(annexAction);
        cmdAnnex.setBorderPainted(false);
        cmdAnnex.setEnabled(false);
        cmdAnnex.setFocusPainted(false);
        cmdAnnex.setFocusable(false);
        cmdAnnex.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdAnnex.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdAnnex);

        cmdCopy.setAction(copyObjectAction1);
        cmdCopy.setBorderPainted(false);
        cmdCopy.setEnabled(false);
        cmdCopy.setFocusPainted(false);
        cmdCopy.setFocusable(false);
        cmdCopy.setHideActionText(true);
        cmdCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdCopy);

        cmdPaste.setAction(pasteObjectAction1);
        cmdPaste.setBorderPainted(false);
        cmdPaste.setEnabled(false);
        cmdPaste.setFocusPainted(false);
        cmdPaste.setFocusable(false);
        cmdPaste.setHideActionText(true);
        cmdPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdPaste);

        cmdDelete.setAction(deleteObjectAction1);
        cmdDelete.setBorderPainted(false);
        cmdDelete.setEnabled(false);
        cmdDelete.setFocusPainted(false);
        cmdDelete.setFocusable(false);
        cmdDelete.setHideActionText(true);
        cmdDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdDelete);

        cmdGeometryOpMode.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-wizard.png"))); // NOI18N
        cmdGeometryOpMode.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.cmdGeometryOpMode.toolTipText",
                new Object[] {}));                                                           // NOI18N
        cmdGeometryOpMode.setFocusable(false);
        cmdGeometryOpMode.setHideActionText(true);
        cmdGeometryOpMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdGeometryOpMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdGeometryOpMode);

        jSeparator11.setOpaque(true);
        jSeparator11.setSeparatorSize(new java.awt.Dimension(20, 20));
        jToolBar1.add(jSeparator11);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.jLabel2.text", new Object[] {})); // NOI18N
        jToolBar1.add(jLabel2);

        cmdLoadDrawings.setAction(loadDrawingsAction);
        cmdLoadDrawings.setBorderPainted(false);
        cmdLoadDrawings.setFocusPainted(false);
        cmdLoadDrawings.setFocusable(false);
        cmdLoadDrawings.setHideActionText(true);
        cmdLoadDrawings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdLoadDrawings.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdLoadDrawings.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdLoadDrawings.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdLoadDrawings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdLoadDrawings);

        cmdSaveDrawings.setAction(saveDrawingsAction);
        cmdSaveDrawings.setBorderPainted(false);
        cmdSaveDrawings.setFocusPainted(false);
        cmdSaveDrawings.setFocusable(false);
        cmdSaveDrawings.setHideActionText(true);
        cmdSaveDrawings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSaveDrawings.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSaveDrawings.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSaveDrawings.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSaveDrawings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdSaveDrawings);

        cmdNewPolygon.setAction(newRectangleModeAction);
        btnGroupMapMode.add(cmdNewPolygon);
        cmdNewPolygon.setBorderPainted(false);
        cmdNewPolygon.setFocusPainted(false);
        cmdNewPolygon.setFocusable(false);
        cmdNewPolygon.setHideActionText(true);
        cmdNewPolygon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNewPolygon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdNewPolygon);

        cmdNewLinestring.setAction(newLinestringModeAction);
        btnGroupMapMode.add(cmdNewLinestring);
        cmdNewLinestring.setBorderPainted(false);
        cmdNewLinestring.setFocusPainted(false);
        cmdNewLinestring.setFocusable(false);
        cmdNewLinestring.setHideActionText(true);
        cmdNewLinestring.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNewLinestring.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdNewLinestring);

        cmdNewPoint.setAction(newMarkerModeAction);
        btnGroupMapMode.add(cmdNewPoint);
        cmdNewPoint.setBorderPainted(false);
        cmdNewPoint.setFocusPainted(false);
        cmdNewPoint.setFocusable(false);
        cmdNewPoint.setHideActionText(true);
        cmdNewPoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNewPoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdNewPoint);

        cmdNewText.setAction(newTextModeAction);
        btnGroupMapMode.add(cmdNewText);
        cmdNewText.setBorderPainted(false);
        cmdNewText.setFocusPainted(false);
        cmdNewText.setFocusable(false);
        cmdNewText.setHideActionText(true);
        cmdNewText.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdNewText.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdNewText);

        cmdSelectAllDrawings.setAction(selectAllDrawingsAction);
        cmdSelectAllDrawings.setFocusable(false);
        cmdSelectAllDrawings.setHideActionText(true);
        cmdSelectAllDrawings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdSelectAllDrawings.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdSelectAllDrawings.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdSelectAllDrawings.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdSelectAllDrawings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdSelectAllDrawings);

        cmdUnselectDrawings.setAction(unselectAllDrawingsAction);
        cmdUnselectDrawings.setBorderPainted(false);
        cmdUnselectDrawings.setEnabled(false);
        cmdUnselectDrawings.setFocusPainted(false);
        cmdUnselectDrawings.setFocusable(false);
        cmdUnselectDrawings.setHideActionText(true);
        cmdUnselectDrawings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdUnselectDrawings.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdUnselectDrawings.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdUnselectDrawings.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdUnselectDrawings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdUnselectDrawings);

        cmdZoomToAllDrawings.setAction(zoomAllDrawingsAction);
        cmdZoomToAllDrawings.setBorderPainted(false);
        cmdZoomToAllDrawings.setFocusPainted(false);
        cmdZoomToAllDrawings.setFocusable(false);
        cmdZoomToAllDrawings.setHideActionText(true);
        cmdZoomToAllDrawings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomToAllDrawings.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomToAllDrawings.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomToAllDrawings.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomToAllDrawings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdZoomToAllDrawings);

        cmdZoomToSelectedDrawings.setAction(zoomSelectedDrawingsAction);
        cmdZoomToSelectedDrawings.setBorderPainted(false);
        cmdZoomToSelectedDrawings.setEnabled(false);
        cmdZoomToSelectedDrawings.setFocusPainted(false);
        cmdZoomToSelectedDrawings.setFocusable(false);
        cmdZoomToSelectedDrawings.setHideActionText(true);
        cmdZoomToSelectedDrawings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdZoomToSelectedDrawings.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdZoomToSelectedDrawings.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdZoomToSelectedDrawings.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdZoomToSelectedDrawings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdZoomToSelectedDrawings);

        cmdRemoveGeometry.setAction(removeDrawingModeAction);
        cmdRemoveGeometry.setBorderPainted(false);
        cmdRemoveGeometry.setEnabled(false);
        cmdRemoveGeometry.setFocusPainted(false);
        cmdRemoveGeometry.setFocusable(false);
        cmdRemoveGeometry.setHideActionText(true);
        cmdRemoveGeometry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRemoveGeometry.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdRemoveGeometry.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdRemoveGeometry.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdRemoveGeometry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdRemoveGeometry);

        cmdDrawingOptions.setAction(optionModeAction1);
        cmdDrawingOptions.setFocusable(false);
        cmdDrawingOptions.setHideActionText(true);
        cmdDrawingOptions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdDrawingOptions.setMaximumSize(new java.awt.Dimension(26, 26));
        cmdDrawingOptions.setMinimumSize(new java.awt.Dimension(26, 26));
        cmdDrawingOptions.setPreferredSize(new java.awt.Dimension(26, 26));
        cmdDrawingOptions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdDrawingOptions);

        cmdDrawingMode.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-palette-painting.png"))); // NOI18N
        cmdDrawingMode.setFocusable(false);
        cmdDrawingMode.setHideActionText(true);
        cmdDrawingMode.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdDrawingMode.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cmdDrawingMode);
        jToolBar1.add(statusPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jToolBar1, gridBagConstraints);

        panMain.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(panMain, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(statusBar1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            menFile,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menFile.text")); // NOI18N

        mniOpenProject.setAction(openProjectAction);
        mniOpenProject.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniOpenProject.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniOpenProject);

        mniSaveProject1.setAction(saveToSameFileProjectAction1);
        mniSaveProject1.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSaveProject1.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniSaveProject1);

        mniSaveProject.setAction(saveProjectAction);
        mniSaveProject.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSaveProject.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniSaveProject);

        mniOverview.setAction(showHideOverviewWindowAction);
        mniOverview.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniOverview.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniOverview);

        mniPrint.setAction(printAction);
        mniPrint.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniPrint.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniPrint);

        mniSaveMapToFile.setAction(exportMapToFileAction);
        mniSaveMapToFile.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSaveMapToFile.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniSaveMapToFile);

        mniExportMap.setAction(exportMapAction);
        mniExportMap.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniExportMap.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniExportMap);

        mniCreateGeoLink.setAction(createGeoLinkAction);
        mniCreateGeoLink.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCreateGeoLink.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniCreateGeoLink);

        mniDownloadManager.setAction(downloadManagerAction);
        mniDownloadManager.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniDownloadManager.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniDownloadManager);

        mniFileOptions.setAction(optionsAction);
        mniFileOptions.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniFileOptions.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniFileOptions);

        sepCentralFilesStart.setName("sepCentralFilesStart"); // NOI18N
        menFile.add(sepCentralFilesStart);

        sepCentralFilesEnd.setName("sepCentralFilesEnd"); // NOI18N
        menFile.add(sepCentralFilesEnd);

        sepLocalFilesEnd.setName("sepLocalFilesEnd"); // NOI18N
        menFile.add(sepLocalFilesEnd);

        mniClose.setAction(closeAction);
        mniClose.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniClose.toolTipText",
                new Object[] {})); // NOI18N
        menFile.add(mniClose);

        jMenuBar1.add(menFile);
        ((FileMenu)menFile).saveComponentsAfterInitialisation();

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
        mniSelectAttribute.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSelectAttribute.toolTipText",
                new Object[] {})); // NOI18N
        menSelection.add(mniSelectAttribute);

        mniSelectLocation.setAction(selectionLocationAction);
        mniSelectLocation.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSelectLocation.toolTipText",
                new Object[] {})); // NOI18N
        menSelection.add(mniSelectLocation);

        mniZoomSelectedObjects.setAction(zoomSelectedObjectsAction);
        mniZoomSelectedObjects.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniZoomSelectedObjects.toolTipText",
                new Object[] {})); // NOI18N
        mniZoomSelectedObjects.setEnabled(false);
        menSelection.add(mniZoomSelectedObjects);

        mniZoomSelectedThemes.setAction(zoomSelectedThemesAction1);
        mniZoomSelectedThemes.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniZoomSelectedThemes.toolTipText",
                new Object[] {})); // NOI18N
        mniZoomSelectedThemes.setEnabled(false);
        menSelection.add(mniZoomSelectedThemes);

        mniRemoveSelection.setAction(removeSelectionAllTopicsAction);
        mniRemoveSelection.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionremove.png")));        // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            mniRemoveSelection,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniRemoveSelection.text")); // NOI18N
        mniRemoveSelection.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniRemoveSelection.toolTipText",
                new Object[] {}));                                                                           // NOI18N
        mniRemoveSelection.setEnabled(false);
        menSelection.add(mniRemoveSelection);

        jMenuBar1.add(menSelection);

        org.openide.awt.Mnemonics.setLocalizedText(
            menTools,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menTools.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            menGeoProcessing,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menGeoProcessing.text")); // NOI18N

        mniBuffer.setAction(bufferGeoprocessingAction);
        mniBuffer.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniBuffer.toolTipText",
                new Object[] {})); // NOI18N
        menGeoProcessing.add(mniBuffer);

        mniClip.setAction(clipGeoprocessingAction);
        mniClip.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniClip.toolTipText",
                new Object[] {})); // NOI18N
        menGeoProcessing.add(mniClip);

        mniUnion.setAction(unionGeoprocessingAction);
        mniUnion.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniUnion.toolTipText",
                new Object[] {})); // NOI18N
        menGeoProcessing.add(mniUnion);

        mniMerge.setAction(mergeGeoprocessingAction);
        mniMerge.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniMerge.toolTipText",
                new Object[] {})); // NOI18N
        menGeoProcessing.add(mniMerge);

        mniDissolve.setAction(dissolveGeoprocessingAction);
        mniDissolve.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniDissolve.toolTipText",
                new Object[] {})); // NOI18N
        menGeoProcessing.add(mniDissolve);

        mniPointInLine.setAction(pointInLineGeoprocessingAction);
        mniPointInLine.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniPointInLine.toolTipText",
                new Object[] {})); // NOI18N
        menGeoProcessing.add(mniPointInLine);

        mniPointInPolygon.setAction(pointInPolygonGeoprocessingAction);
        mniPointInPolygon.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniPointInPolygon.toolTipText",
                new Object[] {})); // NOI18N
        menGeoProcessing.add(mniPointInPolygon);
        menGeoProcessing.add(jSeparator1);

        mniStation.setAction(stationAction1);
        mniStation.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniStation.toolTipText",
                new Object[] {})); // NOI18N
        menGeoProcessing.add(mniStation);

        mniTechProf.setAction(techProfAction1);
        mniTechProf.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniTechProf.toolTipText",
                new Object[] {})); // NOI18N
        menGeoProcessing.add(mniTechProf);

        menTools.add(menGeoProcessing);

        org.openide.awt.Mnemonics.setLocalizedText(
            menChecks,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menChecks.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            menBasicChecks,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menBasicChecks.text")); // NOI18N

        mniCheckBasisRoutes.setAction(basicRoutesCheckAction1);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniCheckBasisRoutes,
            org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCheckBasisRoutes.text",
                new Object[] {})); // NOI18N
        mniCheckBasisRoutes.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCheckBasisRoutes.toolTipText",
                new Object[] {})); // NOI18N
        menBasicChecks.add(mniCheckBasisRoutes);

        mniCheckVerwaltung.setAction(verwaltungCheckAction);
        mniCheckVerwaltung.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCheckVerwaltung.toolTipText",
                new Object[] {})); // NOI18N
        menBasicChecks.add(mniCheckVerwaltung);

        mniCheckAusbau.setAction(ausbauCheckAction);
        mniCheckAusbau.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCheckAusbau.toolTipText",
                new Object[] {})); // NOI18N
        menBasicChecks.add(mniCheckAusbau);

        mniCheckBauwerke.setAction(bauwerkeCheckAction);
        mniCheckBauwerke.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCheckBauwerke.toolTipText",
                new Object[] {})); // NOI18N
        menBasicChecks.add(mniCheckBauwerke);

        mniCheckSonstige.setAction(sonstigeCheckAction);
        mniCheckSonstige.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCheckSonstige.toolTipText",
                new Object[] {})); // NOI18N
        menBasicChecks.add(mniCheckSonstige);

        menChecks.add(menBasicChecks);

        org.openide.awt.Mnemonics.setLocalizedText(
            menExtendedChecks,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menExtendedChecks.text")); // NOI18N

        mniCheckLawaConnection.setAction(gWKConnectionCheckAction);
        mniCheckLawaConnection.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCheckLawaConnection.toolTipText",
                new Object[] {})); // NOI18N
        menExtendedChecks.add(mniCheckLawaConnection);

        mniCheckLawa.setAction(lawaCheckAction);
        mniCheckLawa.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCheckLawa.toolTipText",
                new Object[] {})); // NOI18N
        menExtendedChecks.add(mniCheckLawa);

        menChecks.add(menExtendedChecks);

        menTools.add(menChecks);
        menTools.add(jSeparator2);

        org.openide.awt.Mnemonics.setLocalizedText(
            menChecks1,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menChecks1.text")); // NOI18N

        mniExport.setAction(exportAction1);
        mniExport.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniExport.toolTipText",
                new Object[] {})); // NOI18N
        menChecks1.add(mniExport);

        mniIgmExport.setAction(exportIgmAction);
        mniIgmExport.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniIgmExport.toolTipText",
                new Object[] {})); // NOI18N
        menChecks1.add(mniIgmExport);

        mniExportOption.setAction(exportOptionAction);
        mniExportOption.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniExportOption.toolTipText",
                new Object[] {})); // NOI18N
        menChecks1.add(mniExportOption);

        menTools.add(menChecks1);

        org.openide.awt.Mnemonics.setLocalizedText(
            jMenu1,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.jMenu1.text", new Object[] {})); // NOI18N

        mniCreateDbUser.setAction(createViewsForUser1);
        jMenu1.add(mniCreateDbUser);

        mniRefreshDbUser.setAction(refreshViewsForUser1);
        jMenu1.add(mniRefreshDbUser);

        menTools.add(jMenu1);

        jMenuBar1.add(menTools);

        org.openide.awt.Mnemonics.setLocalizedText(
            menReport,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menReport.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            menSteckbrief,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menSteckbrief.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            menSteckbriefGewaesser,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menSteckbriefGewaesser.text")); // NOI18N

        mniGewaesser.setAction(gewaesserReportAction);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniGewaesser,
            org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniGewaesser.text",
                new Object[] {})); // NOI18N
        mniGewaesser.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniGewaesser.toolTipText",
                new Object[] {})); // NOI18N
        menSteckbriefGewaesser.add(mniGewaesser);

        menSteckbrief.add(menSteckbriefGewaesser);

        org.openide.awt.Mnemonics.setLocalizedText(
            menSteckbriefWasserkoerper,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menSteckbriefWasserkoerper.text")); // NOI18N

        mniFG.setAction(wkFgReportAction);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniFG,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniFG.text", new Object[] {})); // NOI18N
        mniFG.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniFG.toolTipText",
                new Object[] {}));                                                                               // NOI18N
        menSteckbriefWasserkoerper.add(mniFG);

        menSteckbrief.add(menSteckbriefWasserkoerper);

        org.openide.awt.Mnemonics.setLocalizedText(
            menSteckbriefPhotos,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menSteckbriefPhotos.text")); // NOI18N

        mniPrintPhoto.setAction(reportAction1);
        mniPrintPhoto.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniPrintPhoto.toolTipText",
                new Object[] {})); // NOI18N
        menSteckbriefPhotos.add(mniPrintPhoto);

        menSteckbrief.add(menSteckbriefPhotos);

        org.openide.awt.Mnemonics.setLocalizedText(
            menSteckbriefQp,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menSteckbriefQp.text")); // NOI18N

        mniPrintQp.setAction(reportActionGaf);
        mniPrintQp.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniPrintQp.toolTipText",
                new Object[] {})); // NOI18N
        menSteckbriefQp.add(mniPrintQp);

        menSteckbrief.add(menSteckbriefQp);

        menReport.add(menSteckbrief);

        org.openide.awt.Mnemonics.setLocalizedText(
            menStatistik,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menStatistik.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            menGewaesser,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menGewaesser.text")); // NOI18N

        mniGewaesserRep.setAction(gewaesserGewReportAction);
        menGewaesser.add(mniGewaesserRep);

        mniGemeinde.setAction(gemeindenReportAction);
        menGewaesser.add(mniGemeinde);

        mniSb.setAction(sbReportAction);
        menGewaesser.add(mniSb);

        mniFl.setAction(flaechenReportAction1);
        menGewaesser.add(mniFl);

        menStatistik.add(menGewaesser);

        org.openide.awt.Mnemonics.setLocalizedText(
            menGewaesser2,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menGewaesser2.text")); // NOI18N

        mniGewaesserRep2.setAction(gerinneOGewReportAction);
        menGewaesser2.add(mniGewaesserRep2);

        mniGemeinde2.setAction(gerinneOGemeindeReportAction);
        menGewaesser2.add(mniGemeinde2);

        mniSb2.setAction(gerinneOSbReportAction);
        menGewaesser2.add(mniSb2);

        mniFl2.setAction(gerinneOFlReportAction1);
        menGewaesser2.add(mniFl2);

        menStatistik.add(menGewaesser2);

        org.openide.awt.Mnemonics.setLocalizedText(
            menGewaesser1,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menGewaesser1.text")); // NOI18N

        mniGewaesserRep1.setAction(gerinneGGewaesserReportAction1);
        menGewaesser1.add(mniGewaesserRep1);

        mniGemeinde1.setAction(rlDDueReportAction);
        menGewaesser1.add(mniGemeinde1);

        mniSb1.setAction(gerinneGSbReportAction1);
        menGewaesser1.add(mniSb1);

        mniFl1.setAction(gerinneGFlReportAction1);
        menGewaesser1.add(mniFl1);

        menStatistik.add(menGewaesser1);

        menReport.add(menStatistik);

        org.openide.awt.Mnemonics.setLocalizedText(
            menGewaesser3,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menGewaesser3.text")); // NOI18N

        mniGewaesserRep3.setAction(gerogaRsAction1);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniGewaesserRep3,
            org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniGewaesserRep3.text",
                new Object[] {})); // NOI18N
        menGewaesser3.add(mniGewaesserRep3);

        menReport.add(menGewaesser3);

        org.openide.awt.Mnemonics.setLocalizedText(
            menSchablonen,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menSchablonen.text")); // NOI18N
        menReport.add(menSchablonen);

        org.openide.awt.Mnemonics.setLocalizedText(
            menWsg,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menWsg.text")); // NOI18N

        mniFlNu.setAction(wsgAction1);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniFlNu,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.mniFlNu.text", new Object[] {})); // NOI18N
        mniFlNu.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniFlNu.toolTipText",
                new Object[] {}));                                                                                 // NOI18N
        menWsg.add(mniFlNu);

        mniRechtsgrundlage.setAction(rechtsgrundlageAction1);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniRechtsgrundlage,
            org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniRechtsgrundlage.text",
                new Object[] {})); // NOI18N
        mniRechtsgrundlage.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniRechtsgrundlage.toolTipText",
                new Object[] {})); // NOI18N
        menWsg.add(mniRechtsgrundlage);

        mniRechtsgrundlage1.setAction(wsgWaldAction1);
        org.openide.awt.Mnemonics.setLocalizedText(
            mniRechtsgrundlage1,
            org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniRechtsgrundlage1.text",
                new Object[] {})); // NOI18N
        mniRechtsgrundlage1.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniRechtsgrundlage1.toolTipText",
                new Object[] {})); // NOI18N
        menWsg.add(mniRechtsgrundlage1);

        menReport.add(menWsg);

        jMenuBar1.add(menReport);

        org.openide.awt.Mnemonics.setLocalizedText(
            menPhoto,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menPhoto.text")); // NOI18N

        mniUpload.setAction(uploadAction1);
        mniUpload.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniUpload.toolTipText",
                new Object[] {})); // NOI18N
        menPhoto.add(mniUpload);

        mniPhotoInfo.setAction(fotoInfoAction1);
        mniPhotoInfo.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniPhotoInfo.toolTipText",
                new Object[] {})); // NOI18N
        menPhoto.add(mniPhotoInfo);

        mniReportPhoto.setAction(reportAction1);
        mniReportPhoto.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniReportPhoto.toolTipText",
                new Object[] {})); // NOI18N
        mniReportPhoto.setEnabled(false);
        menPhoto.add(mniReportPhoto);

        mniExportPhoto.setAction(exportActionPhoto);
        mniExportPhoto.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniExportPhoto.toolTipText",
                new Object[] {})); // NOI18N
        mniExportPhoto.setEnabled(false);
        menPhoto.add(mniExportPhoto);

        mniDeletePhoto.setAction(deleteAction1);
        mniDeletePhoto.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniDeletePhoto.toolTipText",
                new Object[] {})); // NOI18N
        mniDeletePhoto.setEnabled(false);
        menPhoto.add(mniDeletePhoto);

        mniPhotoOptions.setAction(optionAction1);
        mniPhotoOptions.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniPhotoOptions.toolTipText",
                new Object[] {})); // NOI18N
        menPhoto.add(mniPhotoOptions);

        jMenuBar1.add(menPhoto);

        org.openide.awt.Mnemonics.setLocalizedText(
            menProfiles,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menProfiles.text")); // NOI18N

        mniGafUpload.setAction(uploadActionGaf);
        mniGafUpload.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniGafUpload.toolTipText",
                new Object[] {})); // NOI18N
        mniGafUpload.setEnabled(false);
        menProfiles.add(mniGafUpload);

        mniGafInfo.setAction(gafInfoAction);
        mniGafInfo.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniGafInfo.toolTipText",
                new Object[] {})); // NOI18N
        menProfiles.add(mniGafInfo);

        mniReportGaf.setAction(reportActionGaf);
        mniReportGaf.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniReportGaf.toolTipText",
                new Object[] {})); // NOI18N
        mniReportGaf.setEnabled(false);
        menProfiles.add(mniReportGaf);

        mniExportGaf.setAction(exportActionGaf);
        mniExportGaf.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniExportGaf.toolTipText",
                new Object[] {})); // NOI18N
        mniExportGaf.setEnabled(false);
        menProfiles.add(mniExportGaf);

        mniDeleteGaf.setAction(deleteActionGaf);
        mniDeleteGaf.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniDeleteGaf.toolTipText",
                new Object[] {})); // NOI18N
        mniDeleteGaf.setEnabled(false);
        menProfiles.add(mniDeleteGaf);

        mniGafOptions.setAction(optionActionGaf);
        mniGafOptions.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniGafOptions.toolTipText",
                new Object[] {})); // NOI18N
        menProfiles.add(mniGafOptions);

        jMenuBar1.add(menProfiles);

        org.openide.awt.Mnemonics.setLocalizedText(
            menBookmark,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menBookmark.text")); // NOI18N

        mniCreateBookmark.setAction(showCreateBookmarkDialogAction);
        mniCreateBookmark.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniCreateBookmark.toolTipText",
                new Object[] {})); // NOI18N
        menBookmark.add(mniCreateBookmark);

        mniManageBookmarks.setAction(showManageBookmarksDialogAction);
        mniManageBookmarks.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniManageBookmarks.toolTipText",
                new Object[] {})); // NOI18N
        menBookmark.add(mniManageBookmarks);

        jMenuBar1.add(menBookmark);

        org.openide.awt.Mnemonics.setLocalizedText(
            menDrawings,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menDrawings.text")); // NOI18N

        mniLoadDrawings.setAction(loadDrawingsAction);
        mniLoadDrawings.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniLoadDrawings.toolTipText",
                new Object[] {})); // NOI18N
        menDrawings.add(mniLoadDrawings);

        mniSaveDrawings.setAction(saveDrawingsAction);
        mniSaveDrawings.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSaveDrawings.toolTipText",
                new Object[] {})); // NOI18N
        menDrawings.add(mniSaveDrawings);

        sepDrawingOperation.setName("sepCentralFilesStart"); // NOI18N
        menDrawings.add(sepDrawingOperation);

        mniNewRectangleDrawing.setAction(newRectangleModeAction);
        mniNewRectangleDrawing.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniNewRectangleDrawing.toolTipText",
                new Object[] {})); // NOI18N
        menDrawings.add(mniNewRectangleDrawing);

        mniNewLineStringDrawing.setAction(newLinestringModeAction);
        mniNewLineStringDrawing.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniNewLineStringDrawing.toolTipText",
                new Object[] {})); // NOI18N
        menDrawings.add(mniNewLineStringDrawing);

        mniNewMarkerDrawing.setAction(newMarkerModeAction);
        mniNewMarkerDrawing.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniNewMarkerDrawing.toolTipText",
                new Object[] {})); // NOI18N
        menDrawings.add(mniNewMarkerDrawing);

        mniNewTextDrawing.setAction(newTextModeAction);
        mniNewTextDrawing.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniNewTextDrawing.toolTipText",
                new Object[] {})); // NOI18N
        menDrawings.add(mniNewTextDrawing);

        sepDrawingOperation1.setName("sepCentralFilesStart"); // NOI18N
        menDrawings.add(sepDrawingOperation1);

        mniSelectAllDrawing.setAction(selectAllDrawingsAction);
        mniSelectAllDrawing.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniSelectAllDrawing.toolTipText",
                new Object[] {})); // NOI18N
        menDrawings.add(mniSelectAllDrawing);

        mniUnselectAllDrawing.setAction(unselectAllDrawingsAction);
        mniUnselectAllDrawing.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniUnselectAllDrawing.toolTipText",
                new Object[] {})); // NOI18N
        mniUnselectAllDrawing.setEnabled(false);
        menDrawings.add(mniUnselectAllDrawing);

        mniZoomAllDrawings.setAction(zoomAllDrawingsAction);
        mniZoomAllDrawings.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniZoomAllDrawings.toolTipText",
                new Object[] {})); // NOI18N
        menDrawings.add(mniZoomAllDrawings);

        mniZoomSelectedDrawings.setAction(zoomSelectedDrawingsAction);
        mniZoomSelectedDrawings.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniZoomSelectedDrawings.toolTipText",
                new Object[] {})); // NOI18N
        mniZoomSelectedDrawings.setEnabled(false);
        menDrawings.add(mniZoomSelectedDrawings);

        mniRemoveDrawing.setAction(removeDrawingModeAction);
        mniRemoveDrawing.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniRemoveDrawing.toolTipText",
                new Object[] {})); // NOI18N
        mniRemoveDrawing.setEnabled(false);
        menDrawings.add(mniRemoveDrawing);

        mniDrawingOptions.setAction(optionModeAction1);
        mniDrawingOptions.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniDrawingOptions.toolTipText",
                new Object[] {})); // NOI18N
        menDrawings.add(mniDrawingOptions);

        jMenuBar1.add(menDrawings);
        menDrawings.getAccessibleContext()
                .setAccessibleName(org.openide.util.NbBundle.getMessage(
                        WatergisApp.class,
                        "WatergisApp.menDrawings.AccessibleContext.accessibleName",
                        new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            menWindow,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menWindow.text")); // NOI18N

        mniShowMap.setAction(showMap);
        mniShowMap.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowMap.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowMap);

        mniShowTree.setAction(showTree);
        mniShowTree.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowTree.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowTree);

        mniShowOverview.setAction(showOverview);
        mniShowOverview.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowOverview.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowOverview);

        mniShowDatasource.setAction(showDatasource);
        mniShowDatasource.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowDatasource.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowDatasource);

        mniShowInfo.setAction(showInfoWindowAction);
        mniShowInfo.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowInfo.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowInfo);

        mniShowProblems.setAction(showProblems);
        mniShowProblems.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowProblems.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowProblems);

        mniShowPhotos.setAction(showPhoto);
        mniShowPhotos.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowPhotos.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowPhotos);

        mniShowProfiles.setAction(showProfiles);
        mniShowProfiles.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowProfiles.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowProfiles);

        mniShowLegend.setAction(showLegend);
        mniShowLegend.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowLegend.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowLegend);

        sepWindowSeparator.setName("sepCentralFilesStart"); // NOI18N
        menWindow.add(sepWindowSeparator);

        mniDefaultConfig.setAction(defaultConfigAction1);
        mniDefaultConfig.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniDefaultConfig.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniDefaultConfig);

        mniShowDefaultConfig2.setAction(defaultConfig2Action1);
        mniShowDefaultConfig2.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniShowDefaultConfig2.toolTipText",
                new Object[] {})); // NOI18N
        menWindow.add(mniShowDefaultConfig2);

        jMenuBar1.add(menWindow);

        org.openide.awt.Mnemonics.setLocalizedText(
            menHelp,
            org.openide.util.NbBundle.getMessage(WatergisApp.class, "WatergisApp.menHelp.text")); // NOI18N

        mniHelp.setAction(onlineHelpAction);
        mniHelp.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniHelp.toolTipText",
                new Object[] {})); // NOI18N
        menHelp.add(mniHelp);

        mniInfo.setAction(infoAction);
        mniInfo.setToolTipText(org.openide.util.NbBundle.getMessage(
                WatergisApp.class,
                "WatergisApp.mniInfo.toolTipText",
                new Object[] {})); // NOI18N
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
    private void cmdNodeMoveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdNodeMoveActionPerformed
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    CismapBroker.getInstance()
                            .getMappingComponent()
                            .setHandleInteractionMode(MappingComponent.MOVE_HANDLE);
                    cmdSelectionMode.setSelected(true);
                    selectionModeAction.actionPerformed(evt);
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
                    cmdSelectionMode.setSelected(true);
                    selectionModeAction.actionPerformed(evt);
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
                    cmdSelectionMode.setSelected(true);
                    selectionModeAction.actionPerformed(evt);
                }
            });
    } //GEN-LAST:event_cmdNodeRemoveActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdGoToActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdGoToActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_cmdGoToActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboThemeItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cboThemeItemStateChanged
        final Object o = cboTheme.getSelectedItem();

        if (o instanceof MetaObject) {
            final MetaObject selectedObject = (MetaObject)o;

            if (((MetaObject)o).getBean().getProperty("name").equals("FG: Code")) {
                routeZoomPanel.setVisible(true);

                for (int i = 0; i < panSearch.getComponentCount(); ++i) {
                    if (panSearch.getComponent(i).getClass() != RouteZoomPanel.class) {
                        panSearch.remove(panSearch.getComponent(i));
                    }
                }
            } else {
                routeZoomPanel.setVisible(false);
                for (int i = 0; i < panSearch.getComponentCount(); ++i) {
                    if (panSearch.getComponent(i).getClass() != RouteZoomPanel.class) {
                        panSearch.remove(panSearch.getComponent(i));
                    }
                }
                final MetaClass MC = ClassCacheMultiple.getMetaClass(
                        AppBroker.DOMAIN_NAME,
                        "dlm25w."
                                + selectedObject.getBean().getProperty("thema"),
                        connectionContext);
                panSearch.add(new ZoomPanel(
                        MC,
                        (String)selectedObject.getBean().getProperty("thema"),
                        (String)selectedObject.getBean().getProperty("attr")));
            }
        }
    } //GEN-LAST:event_cboThemeItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @throws  ConnectionException   DOCUMENT ME!
     * @throws  InterruptedException  DOCUMENT ME!
     */
    private void initConnection() throws ConnectionException, InterruptedException {
        final PropertyManager propertyManager = PropertyManager.getManager();
        propertyManager.load(this.getClass().getResourceAsStream("/cfg/navigator.cfg"));
        final Proxy proxyConfig = ProxyHandler.getInstance().init(propertyManager.getProxyProperties());

        final Connection connection = ConnectionFactory.getFactory()
                    .createConnection(AppBroker.getInstance().getConnectionClass(),
                        AppBroker.getInstance().getCallserverUrl(),
                        proxyConfig,
                        propertyManager.isCompressionEnabled());
        ConnectionSession session = null;
        ConnectionProxy proxy = null;

        if (AppBroker.getInstance().getConnectionInfo() != null) {
            try {
                session = ConnectionFactory.getFactory()
                            .createSession(connection, AppBroker.getInstance().getConnectionInfo(), true);
                proxy = ConnectionFactory.getFactory()
                            .createProxy("Sirius.navigator.connection.proxy.DefaultConnectionProxyHandler", session);
                SessionManager.init(proxy);
            } catch (UserException uexp) {
                LOG.error("autologin failed", uexp); // NOI18N
                session = null;
            }
        }

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
            final InputStream is = WatergisApp.class.getResourceAsStream(CONNECTION_PROPERTIES_FILE);

            if (is != null) {
                final Properties props = new Properties();
                props.load(is);
                final String callserverUrl = props.getProperty("CallserverUrl");

                if (callserverUrl != null) {
                    AppBroker.getInstance().setCallserverUrl(callserverUrl);
                }
                final String connectionClass = props.getProperty("ConnectionClass");

                if (connectionClass != null) {
                    AppBroker.getInstance().setConnectionClass(connectionClass);
                }
                final String domain = props.getProperty("Domain");

                if (domain != null) {
                    AppBroker.getInstance().setDomain(domain);
                }
            }

            LOG.warn("args: " + args.length);
            final Options options = new Options();
            options.addOption("u", true, "CallserverUrl");
            options.addOption("c", true, "ConnectionClass");
            options.addOption("d", true, "Domain");
            options.addOption("l", true, "Login");
            final PosixParser parser = new PosixParser();
            final CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("u") && (AppBroker.getInstance().getCallserverUrl() == null)) {
                AppBroker.getInstance().setCallserverUrl(cmd.getOptionValue("u"));
            } else {
                if (AppBroker.getInstance().getCallserverUrl() == null) {
                    LOG.warn("No Callserverhost specified, please specify it with the option -u.");
                    System.exit(1);
                }
            }
            if (cmd.hasOption("c") && (AppBroker.getInstance().getConnectionClass() == null)) {
                AppBroker.getInstance().setConnectionClass(cmd.getOptionValue("c"));
            } else {
                if (AppBroker.getInstance().getConnectionClass() == null) {
                    LOG.warn("No ConnectionClass specified, please specify it with the option -c.");
                    System.exit(1);
                }
            }
            if (cmd.hasOption("d") && (AppBroker.getInstance().getDomain() == null)) {
                AppBroker.getInstance().setDomain(cmd.getOptionValue("d"));
            } else {
                if (AppBroker.getInstance().getDomain() == null) {
                    LOG.error("No Domain specified, please specify it with the option -d.");
                    System.exit(1);
                }
            }
            if (cmd.hasOption("l")) {
                final String loginInfos = cmd.getOptionValue("l");

                final StringTokenizer st = new StringTokenizer(loginInfos, ";");

                if (st.countTokens() == 6) {
                    final String callServer = st.nextToken();
                    final String password = st.nextToken();
                    final String userDomain = st.nextToken();
                    final String userGroup = st.nextToken();
                    final String usergroupDomain = st.nextToken();
                    String username = st.nextToken();

                    final ConnectionInfo i = new ConnectionInfo();
                    i.setCallserverURL(callServer);
                    i.setPassword(password);
                    i.setUserDomain(userDomain);
                    i.setUsergroup(userGroup);
                    i.setUsergroupDomain(usergroupDomain);
                    if (username.endsWith("\"")) {
                        username = username.substring(0, username.length() - 1);
                    }
                    i.setUsername(username);

                    AppBroker.getInstance().setConnectionInfo(i);
                }
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

        final Element templates = parent.getChild("TemplateActions");

        if (templates != null) {
            try {
                final List<Element> templateList = templates.getChildren("Template"); // NOI18N

                for (final Element templateElement : templateList) {
                    final TemplateExportAction action = new TemplateExportAction(templateElement);

                    allTemplates.add(action);
                }

                Collections.sort(allTemplates);
            } catch (Throwable t) {
                LOG.error("Error while loading the templates", t); // NOI18N
            }
        }
    }

    @Override
    public void dispose() {
        try {
            for (final String key : attributeTableMap.keySet()) {
                final View attributeTableView = attributeTableMap.get(key);

                if (attributeTableView != null) {
                    final Component c = attributeTableView.getComponent();

                    if (c instanceof AttributeTable) {
                        final AttributeTable attrTable = (AttributeTable)c;

                        if (attrTable.isProcessingModeActive()) {
                            final String message = NbBundle.getMessage(
                                    WatergisApp.class,
                                    "WatergisApp.dispose().singleTable.message",
                                    attrTable.getFeatureService().getName());

                            final int ans = JOptionPane.showConfirmDialog(
                                    WatergisApp.this,
                                    message,
                                    NbBundle.getMessage(WatergisApp.class, "WatergisApp.dispose().title"),
                                    JOptionPane.YES_NO_CANCEL_OPTION);

                            if (ans == JOptionPane.YES_OPTION) {
                                attrTable.changeProcessingMode(true);
                            } else if (ans == JOptionPane.NO_OPTION) {
                                attrTable.unlockAll();
                            } else {
                                setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
                                return;
                            }
                        }
                    }
                }
            }

            boolean runningOrWaitingDownload = false;

            for (final Download d : DownloadManager.instance().getDownloads()) {
                if ((d.getStatus() == Download.State.RUNNING) || (d.getStatus() == Download.State.WAITING)) {
                    runningOrWaitingDownload = true;
                    break;
                }
            }

            if (runningOrWaitingDownload) {
                final int ans = JOptionPane.showConfirmDialog(
                        WatergisApp.this,
                        NbBundle.getMessage(WatergisApp.class, "WatergisApp.dispose().downloadMessage"),
                        NbBundle.getMessage(WatergisApp.class, "WatergisApp.dispose().downloadTitle"),
                        JOptionPane.YES_NO_OPTION);

                if (ans == JOptionPane.YES_OPTION) {
                    for (final Download d : DownloadManager.instance().getDownloads()) {
                        if (d.getStatus() == Download.State.WAITING) {
                            DownloadManager.instance().removeDownload(d);
                        } else if (d.getStatus() == Download.State.RUNNING) {
                            if (d instanceof AbstractCancellableDownload) {
                                ((AbstractCancellableDownload)d).cancel();
                            }
                        }
                    }
                } else {
                    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
                    return;
                }
            }

            boolean exportAlive = false;

            do {
                exportAlive = false;

                for (final Thread thread : Thread.getAllStackTraces().keySet()) {
                    if (thread.getThreadGroup().getName().equals("DownloadThreadPool") && thread.isAlive()) {
                        final StackTraceElement[] stack = Thread.getAllStackTraces().get(thread);

                        if ((stack.length > 0) && !stack[0].getMethodName().equalsIgnoreCase("park")) {
                            exportAlive = true;
                            break;
                        }
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } while (exportAlive);

            if (vPhoto.isClosable()) {
                vPhoto.close();
            }

            if (vGaf.isClosable()) {
                vGaf.close();
            }

            try {
                StaticStartupTools.saveScreenshotOfFrame(this, FILEPATH_SCREEN);
            } catch (Exception ex) {
                LOG.fatal("Error while capturing the app content", ex);
            }

            setVisible(false);
            LOG.info("Dispose(): Watergis is going to shut down");

            configManager.writeConfiguration();
            saveLayout(FILEPATH_DEFAULT_LAYOUT);

            super.dispose();
        } catch (RuntimeException ex) {
            LOG.error("Error while disposing main window", ex);
        }

        LOG.info("Dispose(): exit");
        // without the following line, the application will sometimes not close correctly
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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

            AppBroker.getInstance().getInfoWindowAction().dispose();

            // close all attribute table views
            final Map<String, View> attributeTables = new HashMap<String, View>(attributeTableMap);
            for (final String key : attributeTables.keySet()) {
                final View attrTableView = attributeTables.get(key);
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
        }
    }

    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
    }

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
        selectedFeaturesChanged();
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureCollectionChanged() {
    }

    /**
     * DOCUMENT ME!
     */
    private void removeObjectsFromMap() {
        WatergisTreeNodeVisualizationService.removeVisualisedFeatures();
    }

    /**
     * DOCUMENT ME!
     */
    private void selectedFeaturesChanged() {
        watergisSingleThreadExecutor.execute(new Runnable() {

                @Override
                public void run() {
                    final SelectionListener sl = (SelectionListener)mappingComponent.getInputEventListener()
                                .get(MappingComponent.SELECT);
                    final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures();

                    final boolean featuresSelected = !selectedFeatures.isEmpty();
                    final boolean enableAnnex = releaseAction.containsAnyRelevantFeature(selectedFeatures, false);
                    final boolean enableRelease = releaseAction.containsAnyRelevantFeature(selectedFeatures, true);
                    final boolean photoSelected = containsAnyRelevantFeature(selectedFeatures, "foto");
                    final boolean profileSelected = containsAnyRelevantFeature(selectedFeatures, "qp_upl");
                    final boolean drawsSelected = !RemoveDrawingModeAction.getSelectedDrawings().isEmpty();
                    boolean oneEditableFeature = false;
                    boolean selectedFeatueIsPolygon = false;
                    boolean mergeAllowed = false;
                    AbstractFeatureService editableService = null;
                    boolean editFeature = false;

//                    if (selectedFeatures.size() == 1) {
//                        final Feature f = selectedFeatures.get(0);
//                        if (f instanceof DefaultFeatureServiceFeature) {
//                            final DefaultFeatureServiceFeature serviceFeature = (DefaultFeatureServiceFeature)f;
//                            if ((serviceFeature.getLayerProperties() != null)
//                                        && (serviceFeature.getLayerProperties().getFeatureService() != null)) {
//                                if (SelectionManager.getInstance().getEditableServices().contains(
//                                                serviceFeature.getLayerProperties().getFeatureService())) {
//                                    oneEditableFeature = true;
//                                }
//                            }
//                        }
//                    }

                    if (selectedFeatures.size() > 0) {
                        for (int i = 0; i < selectedFeatures.size(); ++i) {
                            final Feature f = selectedFeatures.get(i);
                            if (f instanceof DefaultFeatureServiceFeature) {
                                final DefaultFeatureServiceFeature serviceFeature = (DefaultFeatureServiceFeature)f;
                                if ((serviceFeature.getLayerProperties() != null)
                                            && (serviceFeature.getLayerProperties().getFeatureService() != null)) {
                                    final AbstractFeatureService service = serviceFeature.getLayerProperties()
                                                .getFeatureService();
                                    if (SelectionManager.getInstance().getEditableServices().contains(service)
                                                && serviceFeature.isEditable()) {
                                        if (editableService == null) {
                                            editableService = serviceFeature.getLayerProperties().getFeatureService();
                                            oneEditableFeature = true;
                                            if ((serviceFeature.getGeometry() instanceof Polygon)
                                                        || (serviceFeature.getGeometry() instanceof MultiPolygon)) {
                                                selectedFeatueIsPolygon = true;
                                            }
                                        } else if (
                                            !editableService.equals(
                                                        serviceFeature.getLayerProperties().getFeatureService())) {
                                            mergeAllowed = false;
                                            oneEditableFeature = false;
                                            break;
                                        } else if (editableService.equals(
                                                        serviceFeature.getLayerProperties().getFeatureService())) {
                                            mergeAllowed = true;
                                            oneEditableFeature = false;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    final List<PFeature> selectedPFeature = sl.getAllSelectedPFeatures();
                    final Collection<Feature> selectedMapFeatures = mappingComponent.getFeatureCollection()
                                .getSelectedFeatures();

                    if (selectedPFeature != null) {
                        if (selectedPFeature.size() > 0) {
                            for (final PFeature f : selectedPFeature) {
                                if (f.getFeature() instanceof DrawingSLDStyledFeature) {
                                    editFeature = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (selectedMapFeatures != null) {
                        if (selectedMapFeatures.size() > 0) {
                            for (final Feature f : selectedMapFeatures) {
                                if (f.isEditable()) {
                                    editFeature = true;
                                    break;
                                }
                            }
                        }
                    }

                    final boolean splitEnabled = oneEditableFeature;
                    final boolean polygonOperationEnabled = oneEditableFeature && selectedFeatueIsPolygon;
                    final boolean mergeEnabled = mergeAllowed;
                    final boolean editOperationsEnabled = editFeature;

                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                cmdMerge.setEnabled(mergeEnabled);
                                cmdSplit.setEnabled(splitEnabled);
                                mniRemoveSelection.setEnabled(featuresSelected);
                                mniZoomSelectedObjects.setEnabled(featuresSelected);
                                cmdRemoveSelectionAllThemes.setEnabled(featuresSelected);
                                cmdZoomSelectedObjects.setEnabled(featuresSelected);
                                if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("administratoren")) {
                                    cmdAnnex.setEnabled(enableAnnex);
                                    cmdRelease.setEnabled(enableRelease);
                                }

                                mniReportPhoto.setEnabled(photoSelected);
                                mniExportPhoto.setEnabled(photoSelected);
                                mniDeletePhoto.setEnabled(photoSelected);
                                mniPrintPhoto.setEnabled(photoSelected);

//                                mniReportGaf.setEnabled(profileSelected);
                                mniExportGaf.setEnabled(profileSelected);
                                mniDeleteGaf.setEnabled(profileSelected);
                                mniPrintQp.setEnabled(profileSelected);

                                cmdRemoveGeometry.setEnabled(drawsSelected);
                                mniRemoveDrawing.setEnabled(drawsSelected);
                                cmdZoomToSelectedDrawings.setEnabled(drawsSelected);
                                cmdUnselectDrawings.setEnabled(drawsSelected);
                                mniZoomSelectedDrawings.setEnabled(drawsSelected);
                                mniUnselectAllDrawing.setEnabled(drawsSelected);

                                cmdNodeAdd.setEnabled(editOperationsEnabled);
                                cmdNodeMove.setEnabled(editOperationsEnabled);
                                cmdNodeRemove.setEnabled(editOperationsEnabled);
                                cmdPresentation.setEnabled(editOperationsEnabled);
                                cmdMoveGeometry.setEnabled(editOperationsEnabled);
                                tbtnAddMode.setEnabled(polygonOperationEnabled);
                                tbtnRemoveMode.setEnabled(polygonOperationEnabled);
                                if (!polygonOperationEnabled
                                            && (tbtnAddMode.isSelected() || tbtnRemoveMode.isSelected())) {
                                    cmdSelectionMode.getAction().actionPerformed(null);
                                }
                                topicTreeSelectionChanged(null);
                            }
                        });
                }
            });
    }

    /**
     * Determines if any feature of ther given class is contained in the array.
     *
     * @param   features   all selected features
     * @param   className  The name of the class that should be checked for
     *
     * @return  true, if any feature of the given class is contained
     */
    public boolean containsAnyRelevantFeature(final List<Feature> features, final String className) {
        for (final Feature f : features) {
            if (f instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)f;
                final CidsLayer cidsLayer = (CidsLayer)cidsFeature.getLayerProperties().getFeatureService();

                if (cidsLayer.getMetaClass().getName().toLowerCase().equals(className)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void removeLayer(final ThemeLayerEvent e) throws VetoException {
        if (e.getLayer() instanceof AbstractFeatureService) {
            final String id = AttributeTableFactory.createId(((AbstractFeatureService)e.getLayer()));
            final View attributeTableView = attributeTableMap.get(id);

            if (attributeTableView != null) {
                try {
                    attributeTableView.closeWithAbort();
                } catch (OperationAbortedException ex) {
                    throw new VetoException();
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mde  DOCUMENT ME!
     */
    @Override
    public void dropOnMap(final MapDnDEvent mde) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("drop on map"); // NOI18N
        }

        if (mde.getDte() instanceof DropTargetDropEvent) {
            final DropTargetDropEvent dtde = (DropTargetDropEvent)mde.getDte();
            final MappingComponent map = CismapBroker.getInstance().getMappingComponent();

            if (dtde.getTransferable().isDataFlavorSupported(CAPABILITY_WIDGET_FLAVOR)) {
                LayerDropUtils.drop(dtde, (ActiveLayerModel)map.getMappingModel(), map);
            } else if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                        || dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                if (DnDUtils.isFilesOrUriList((DropTargetDropEvent)mde.getDte())) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    try {
                        final List<File> files = DnDUtils.getFilesFrom(dtde);

                        for (final File file : files) {
                            if (ImageFileUtils.isImageFileEnding(file.getName())) {
                                ImageFileMetaData metaData = null;

                                try {
                                    switch (ImageFileUtils.determineMode(file)) {
                                        case WORLDFILE: {
                                            metaData = ImageFileUtils.getWorldFileMetaData(
                                                    file,
                                                    ImageFileUtils.getWorldFile(file));
                                        }
                                        case TIFF: {
                                            metaData = ImageFileUtils.getTiffMetaData(file);
                                        }
                                    }
                                } catch (final Exception ex) {
                                    LOG.warn("Failure during drag & drop opertation", ex); // NOI18N
                                }

                                if (metaData == null) {
                                    JOptionPane.showMessageDialog(StaticSwingTools.getParentFrame(this),
                                        NbBundle.getMessage(
                                            WatergisApp.class,
                                            "WatergisApp.dropOnMap.noGeoInfo.msg",
                                            file.getName()),
                                        NbBundle.getMessage(WatergisApp.class, "WatergisApp.dropOnMap.noGeoInfo.title"),
                                        JOptionPane.WARNING_MESSAGE); // NOI18N
                                }
                            }
                        }
                    } catch (final Exception ex) {
                        LOG.error("Failure during drag & drop opertation", ex); // NOI18N
                    }
                }

                LayerDropUtils.drop((DropTargetDropEvent)mde.getDte(), (ActiveLayerModel)map.getMappingModel(), map);
            } else {
                LOG.error("Unable to process the datatype." + dtde.getTransferable().getTransferDataFlavors()[0]); // NOI18N
            }
        }
    }

    @Override
    public void dragOverMap(final MapDnDEvent mde) {
    }

    @Override
    public void selectionChanged(final SelectionChangedEvent event) {
        selectedFeaturesChanged();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ExportMenuItem extends ThemeLayerMenuItem {

        //~ Instance fields ----------------------------------------------------

        private Filter filter = new Filter();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RemoveThemeMenuItem object.
         */
        public ExportMenuItem() {
            super(NbBundle.getMessage(
                    ThemeLayerWidget.class,
                    "ThemeLayerWidget.ExportMenuItem.pmenuItem.text"),
                NODE
                        | FEATURE_SERVICE
                        | GEOMETRY
                        | FEATURE_SELECTED
                        | NO_FEATURE_SELECTED
                        | NO_GEOMETRY);
            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  filter  DOCUMENT ME!
         */
        public void setFilter(final Filter filter) {
            this.filter = filter;
        }

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public boolean isSelectable(final List<ServiceLayer> serviceLayerList) {
            for (final ServiceLayer layer : serviceLayerList) {
                if (!filter.isValid(layer)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean isSelectable(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            List<Feature> features;
            final TreePath[] paths = pTopicTree.getSelectionPath();

            if (paths.length == 1) {
                final TreePath o = paths[0];
                final AbstractFeatureService afs = (AbstractFeatureService)o.getLastPathComponent();

                // this menu item is only visible, when exactly one service is selected
                if ((afs.getLayerProperties().getAttributeTableRuleSet() != null)
                            && afs.getLayerProperties().getAttributeTableRuleSet().hasCustomExportFeaturesMethod()) {
                    afs.getLayerProperties().getAttributeTableRuleSet().exportFeatures();
                    return;
                }
                features = SelectionManager.getInstance().getSelectedFeatures(afs);

                if (!((features != null) && !features.isEmpty())) {
                    features = null;
                }

                DefaultFeatureServiceFeature[] featureArray = null;

                if (features != null) {
                    featureArray = new DefaultFeatureServiceFeature[features.size()];

                    for (int i = 0; i < features.size(); ++i) {
                        featureArray[i] = (DefaultFeatureServiceFeature)features.get(i);
                    }
                }
                if (lastExportPath == null) {
                    if (DownloadManager.instance().getDestinationDirectory() != null) {
                        lastExportPath = DownloadManager.instance().getDestinationDirectory().toString();
                    }

                    if (lastExportPath == null) {
                        lastExportPath = DIRECTORYPATH_WATERGIS;
                    }
                }

                File outputFile = null;

                try {
                    afs.initAndWait();
                } catch (Exception ex) {
                    LOG.error("Cannot initialise service.", ex);
                }
                final String geometryType = afs.getGeometryType();

                if ((geometryType == null) || geometryType.equals(AbstractFeatureService.UNKNOWN)
                            || geometryType.equals(AbstractFeatureService.NONE)) {
                    outputFile = StaticSwingTools.chooseFileWithMultipleFilters(
                            lastExportPath,
                            true,
                            new String[] { "dbf", "csv", "txt" },
                            new String[] { "dbf", "csv", "txt" },
                            this);
                } else {
                    outputFile = StaticSwingTools.chooseFileWithMultipleFilters(
                            lastExportPath,
                            true,
                            new String[] { "shp", "dbf", "csv", "txt" },
                            new String[] { "shp", "dbf", "csv", "txt" },
                            this);
                }

                if (outputFile != null) {
                    ExportDownload ed;
                    final List<String[]> attributeNames;

                    if (!outputFile.getName().toLowerCase().endsWith("csv")
                                && !outputFile.getName().toLowerCase().endsWith("txt")) {
                        attributeNames = getAliasAttributeList(afs, true);
                    } else {
                        attributeNames = getAliasAttributeList(afs, false);
                    }
                    String query = null;

                    if (hasArrayValue(attributeNames, "ww_gr") && (afs instanceof CidsLayer)) {
                        if (!ThemeExportDialog.getInstance().has1501() || !ThemeExportDialog.getInstance().has1502()
                                    || !ThemeExportDialog.getInstance().has1503()
                                    || !ThemeExportDialog.getInstance().has1504()
                                    || !ThemeExportDialog.getInstance().has1505()) {
                            final List<String> wdms = new ArrayList<String>();

                            if (ThemeExportDialog.getInstance().has1501()) {
                                wdms.add("1501");
                            }
                            if (ThemeExportDialog.getInstance().has1502()) {
                                wdms.add("1502");
                            }
                            if (ThemeExportDialog.getInstance().has1503()) {
                                wdms.add("1503");
                            }
                            if (ThemeExportDialog.getInstance().has1504()) {
                                wdms.add("1504");
                            }
                            if (ThemeExportDialog.getInstance().has1505()) {
                                wdms.add("1505");
                            }

                            if (query == null) {
                                query = "dlm25wPk_ww_gr1.wdm in " + listToString(wdms);
                            } else {
                                query += " and dlm25wPk_ww_gr1.wdm in " + listToString(wdms);
                            }
                        }
                    }

                    if (outputFile.getName().toLowerCase().endsWith("dbf")) {
                        ed = new ExportDbfDownload();
                        ed.init(outputFile.getAbsolutePath(), "", featureArray, afs, attributeNames, query);
                    } else if (outputFile.getName().toLowerCase().endsWith("csv")) {
                        ed = new ExportCsvDownload();
                        ed.init(outputFile.getAbsolutePath(), "", featureArray, afs, attributeNames, query);
                    } else if (outputFile.getName().toLowerCase().endsWith("txt")) {
                        ed = new ExportTxtDownload();
                        ed.init(outputFile.getAbsolutePath(), "", featureArray, afs, attributeNames, query);
                    } else {
                        ed = new ExportShapeDownload();
                        ed.init(outputFile.getAbsolutePath(), "", featureArray, afs, attributeNames, query);
                    }

                    lastExportPath = outputFile.getParent();
                    DownloadManager.instance().add(ed);
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   list  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String listToString(final List<String> list) {
            final StringBuilder string = new StringBuilder("(");
            boolean first = true;

            for (final String tmp : list) {
                if (first) {
                    string.append(tmp);
                    first = false;
                } else {
                    string.append(",").append(tmp);
                }
            }

            string.append(")");

            return string.toString();
        }

        /**
         * DOCUMENT ME!
         *
         * @param   array  DOCUMENT ME!
         * @param   value  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private boolean hasArrayValue(final List<String[]> array, final String value) {
            for (final String[] tmp : array) {
                if (tmp[0].equals(value)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Provides a list with the alias names of all attributes.
         *
         * @param   service             DOCUMENT ME!
         * @param   withGeometryColumn  DOCUMENT ME!
         *
         * @return  the list contains string arrays. Every array has 2 strings. The first string is the alias name and
         *          the second string is the original name
         */
        private List<String[]> getAliasAttributeList(final AbstractFeatureService service,
                final boolean withGeometryColumn) {
            final List<String[]> attrNames = new ArrayList<String[]>();
            final Map<String, FeatureServiceAttribute> attributeMap = service.getFeatureServiceAttributes();

            for (final Object name : service.getOrderedFeatureServiceAttributes()) {
                final FeatureServiceAttribute attr = attributeMap.get(name);

                if ((attr != null)
                            && ((!attr.isVisible() || (!withGeometryColumn && attr.isGeometry()))
                                && !(withGeometryColumn && attr.isGeometry()))) {
                    continue;
                }

                final String[] aliasAttr = new String[2];

                aliasAttr[0] = (attr.getAlias().equals("") ? attr.getName() : attr.getAlias());
                aliasAttr[1] = attr.getName();

                attrNames.add(aliasAttr);
            }

            return attrNames;
        }

        //~ Inner Classes ------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @version  $Revision$, $Date$
         */
        private class Filter {

            //~ Methods --------------------------------------------------------

            /**
             * DOCUMENT ME!
             *
             * @param   layer  DOCUMENT ME!
             *
             * @return  DOCUMENT ME!
             */
            public boolean isValid(final ServiceLayer layer) {
                if (layer instanceof CidsLayer) {
                    return isExportEnabled((CidsLayer)layer);
                } else {
                    return true;
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class MetaDocumentMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new EditModeMenuItem object.
         */
        public MetaDocumentMenuItem() {
            super(NbBundle.getMessage(
                    MetaDocumentMenuItem.class,
                    "WatergisApp.MetaDocumentMenuItem.MetaDocumentMenuItem().title"),
                NODE
                        | FEATURE_SERVICE
                        | GEOMETRY
                        | FEATURE_SELECTED
                        | NO_FEATURE_SELECTED
                        | NO_GEOMETRY);
//            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] paths = pTopicTree.getSelectionPath();

            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof CidsLayer) {
                    final CidsLayer service = (CidsLayer)path.getLastPathComponent();

                    try {
                        final URL u = new URL(service.getMetaDocumentLink());

                        try {
                            de.cismet.tools.BrowserLauncher.openURL(u.toString());
                        } catch (Exception ex) {
                            LOG.error("Cannot open the url:" + u, ex);
                        }
                    } catch (MalformedURLException ex) {
                        // nothing to do
                    }
                }
            }
        }

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public boolean isSelectable(final int mask) {
            final boolean selectable = super.isSelectable(mask);

            if (selectable) {
                final TreePath[] paths = pTopicTree.getSelectionPath();

                if ((paths.length == 1) && (paths[0].getLastPathComponent() instanceof CidsLayer)) {
                    final CidsLayer layer = (CidsLayer)paths[0].getLastPathComponent();

                    return layer.getMetaDocumentLink() != null;
                }
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ExportOptionsMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new EditModeMenuItem object.
         */
        public ExportOptionsMenuItem() {
            super(NbBundle.getMessage(
                    MetaDocumentMenuItem.class,
                    "WatergisApp.ExportOptionsMenuItem.ExportOptionsMenuItem().title"),
                ROOT
                        | GEOMETRY
                        | FEATURE_SELECTED
                        | NO_FEATURE_SELECTED
                        | NO_GEOMETRY);
            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            StaticSwingTools.showDialog(ThemeExportDialog.getInstance());
        }

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask);
        }

        @Override
        public boolean isSelectable(final int mask) {
            return super.isSelectable(mask);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CopyMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new EditModeMenuItem object.
         */
        public CopyMenuItem() {
            super(NbBundle.getMessage(
                    CopyMenuItem.class,
                    "WatergisApp.CopyMenuItem.CopyMenuItem().title"),
                NODE
                        | FEATURE_SERVICE
                        | GEOMETRY
                        | FEATURE_SELECTED
                        | NO_FEATURE_SELECTED
                        | NO_GEOMETRY,
                NODE
                        | FEATURE_SERVICE
                        | GEOMETRY
                        | FEATURE_SELECTED
                        | NO_GEOMETRY);
//            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] tps = pTopicTree.getSelectionPath();

            if ((tps != null) && (tps.length == 1)) {
                final AbstractFeatureService service = (AbstractFeatureService)tps[0].getLastPathComponent();
                final List<Feature> features = SelectionManager.getInstance().getSelectedFeatures(service);
                final List<FeatureServiceFeature> featureServiceFeatures = new ArrayList<FeatureServiceFeature>();

                for (final Feature f : features) {
                    if (f instanceof FeatureServiceFeature) {
                        featureServiceFeatures.add((FeatureServiceFeature)f);
                    }
                }

                AttributeTable.copySelectedFeaturesToClipboard(featureServiceFeatures);
                topicTreeSelectionChanged(null);
            }
        }

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class PasteMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new EditModeMenuItem object.
         */
        public PasteMenuItem() {
            super(NbBundle.getMessage(
                    PasteMenuItem.class,
                    "WatergisApp.PasteMenuItem.PasteMenuItem().title"),
                NODE
                        | FEATURE_SERVICE
                        | GEOMETRY
                        | FEATURE_SELECTED
                        | NO_FEATURE_SELECTED
                        | NO_GEOMETRY);
//            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] tps = pTopicTree.getSelectionPath();

            if ((tps != null) && (tps.length == 1)) {
                final AbstractFeatureService service = (AbstractFeatureService)tps[0].getLastPathComponent();

                final AttributeTable table = AppBroker.getInstance()
                            .getWatergisApp()
                            .getAttributeTableByFeatureService(service);
                table.pasteSelectedFeaturesfromClipboard();
            }
        }

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public boolean isSelectable(final int mask) {
            final boolean selectable = super.isSelectable(mask);

            if (selectable) {
                final TreePath[] paths = pTopicTree.getSelectionPath();

                if ((paths.length == 1) && (paths[0].getLastPathComponent() instanceof AbstractFeatureService)) {
                    final AbstractFeatureService service = (AbstractFeatureService)paths[0].getLastPathComponent();

                    if (SelectionManager.getInstance().getEditableServices().contains(service)) {
                        final AttributeTable table = getAttributeTableByFeatureService(service);

                        return (table != null) && table.isPasteButtonEnabled();
                    }
                }
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DeleteMenuItem extends ThemeLayerMenuItem {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new EditModeMenuItem object.
         */
        public DeleteMenuItem() {
            super(NbBundle.getMessage(
                    DeleteMenuItem.class,
                    "WatergisApp.DeleteMenuItem.DeleteMenuItem().title"),
                NODE
                        | FEATURE_SERVICE
                        | GEOMETRY
                        | FEATURE_SELECTED
                        | NO_FEATURE_SELECTED
                        | NO_GEOMETRY);
//            newSection = true;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TreePath[] tps = pTopicTree.getSelectionPath();

            if ((tps != null) && (tps.length == 1)) {
                final AbstractFeatureService service = (AbstractFeatureService)tps[0].getLastPathComponent();
                final List<Feature> features = SelectionManager.getInstance().getSelectedFeatures(service);
                final List<FeatureServiceFeature> featureServiceFeatures = new ArrayList<FeatureServiceFeature>();

                for (final Feature f : features) {
                    if (f instanceof FeatureServiceFeature) {
                        featureServiceFeatures.add((FeatureServiceFeature)f);
                    }
                }

                if (!featureServiceFeatures.isEmpty()) {
                    final AttributeTable table = AppBroker.getInstance()
                                .getWatergisApp()
                                .getAttributeTableByFeature(featureServiceFeatures.get(0));
                    table.deleteFeatures();
                }
            }
        }

        @Override
        public boolean isVisible(final int mask) {
            return ((visibility & mask) == mask) && ((mask & FEATURE_SERVICE) != 0);
        }

        @Override
        public boolean isSelectable(final int mask) {
            final boolean selectable = super.isSelectable(mask);

            if (selectable) {
                final TreePath[] paths = pTopicTree.getSelectionPath();

                if ((paths.length == 1) && (paths[0].getLastPathComponent() instanceof AbstractFeatureService)) {
                    final AbstractFeatureService service = (AbstractFeatureService)paths[0].getLastPathComponent();

                    if (SelectionManager.getInstance().getEditableServices().contains(service)) {
                        final List<Feature> selectedFeatures = SelectionManager.getInstance()
                                    .getSelectedFeatures(service);

                        if ((selectedFeatures != null) && !selectedFeatures.isEmpty()) {
                            boolean enable = false;

                            for (final Feature f : selectedFeatures) {
                                if (f instanceof FeatureServiceFeature) {
                                    if (((FeatureServiceFeature)f).isEditable()) {
                                        enable = true;
                                        break;
                                    }
                                }
                            }

                            if (enable) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class RouteElement {

        //~ Instance fields ----------------------------------------------------

        private XBoundingBox env;
        private final String name;
        private final int id;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RouteElement object.
         *
         * @param  id    DOCUMENT ME!
         * @param  name  bean DOCUMENT ME!
         * @param  g     DOCUMENT ME!
         */
        public RouteElement(final int id, final String name, final Geometry g) {
            this.name = name;
            this.id = id;

            if (g != null) {
                env = new XBoundingBox(g);
            }
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public XBoundingBox getEnvelope() {
            return env;
        }

        @Override
        public String toString() {
            return name;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the id
         */
        public int getId() {
            return id;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AttributeTableAncestorListener implements AncestorListener {

        //~ Instance fields ----------------------------------------------------

        private DockingWindowListener listener = null;
        private final View view;
        private final AttributeTable table;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AttributeTableAncestorListener object.
         *
         * @param  view   DOCUMENT ME!
         * @param  table  DOCUMENT ME!
         */
        public AttributeTableAncestorListener(final View view, final AttributeTable table) {
            this.view = view;
            this.table = table;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  listener  DOCUMENT ME!
         */
        public void setDockingWindowListener(final DockingWindowListener listener) {
            this.listener = listener;
        }

        @Override
        public void ancestorAdded(final AncestorEvent event) {
        }

        @Override
        public void ancestorRemoved(final AncestorEvent event) {
            if (StaticSwingTools.getParentFrame(event.getComponent()) != WatergisApp.this) {
//            view.removeListener(listener);

                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            view.dock();
                        }
                    });
//                table.dispose();
//                disposeTable(view, table, this, listener);

//                view.removeAncestorListener(this);
//                if (view.getParent() != null) {
//                    view.getParent().remove(view);
//                }
//                viewMap.removeView("Attributtabelle " + table.getFeatureService().getName());
//                attributeTableMap.remove(AttributeTableFactory.createId(table.getFeatureService()));
//
//                SelectionManager.getInstance().removeConsideredAttributeTable(table);
//
//                // The view is not removed from the root window and this will cause that the layout cannot be saved
//                // when the application will be closed. So rootWindow.removeView(view) must be invoked. But without
//                // the invocation of view.close(), the invocation of rootWindow.removeView(view) will do nothing To
//                // avoid an infinite loop, view.removeListener(this) must be invoked before view.close();
//                view.close();
//                rootWindow.removeView(view);
//                view.close();
//                        view.dock();
//                        try {
//                            view.closeWithAbort();
//                        } catch (Exception e) {
//                            // nothing to do
//                        }
            }
        }

        @Override
        public void ancestorMoved(final AncestorEvent event) {
        }
    }
}
