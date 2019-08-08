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

import Sirius.navigator.connection.SessionManager;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;
import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.feature.FeatureCollection;

import org.jdom.Element;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;

import java.nio.charset.Charset;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.helper.SQLFormatter;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.CidsLayerFactory;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.tools.ExportCsvDownload;
import de.cismet.cismap.commons.tools.SimpleFeatureCollection;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.security.WebAccessManager;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.download.FakeFileDownload;

import de.cismet.watergis.gui.WatergisApp;
import de.cismet.watergis.gui.actions.checks.AbstractCheckAction;
import de.cismet.watergis.gui.dialog.ExportDialog;

import de.cismet.watergis.utils.GeometryUtils;
import de.cismet.watergis.utils.JumpShapeWriter;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportAction extends AbstractAction implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ExportAction.class);
    private static final String CHECK_LIST = "CheckList";
    private static final String ROOT_TAG = "ExportAction";
    private static final String FEATURE_SERVICES_TAG = "FeatureServices";
    private static final String JAVA_CLASS_ATTR = "JavaClass";
    private static final String CHECK = "Check";
    private static final String THEME_FOLDER = "Themen";
    private static final String CATALOGUE_FOLDER = "Kataloge";
    private static final String PRJ_CONTENT =
        "PROJCS[\"ETRS_1989_UTM_Zone_33N\",GEOGCS[\"GCS_ETRS_1989\",DATUM[\"D_ETRS_1989\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",33500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",15.0],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]";

    //~ Instance fields --------------------------------------------------------

    private List<AbstractFeatureService> servicesToExport;
    private List<AbstractCheckAction> checks;
    private Map<Integer, CidsBean> wwGrBeans = null;
    private List<DefaultFeatureServiceFeature> expFeatures = null;
    private String path = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportAction object.
     */
    public ExportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-download-alt.png");
        String text = "GDV";
        String tooltiptext = "GDV-Export";
        String mnemonic = "G";

        try {
            text = NbBundle.getMessage(ExportAction.class,
                    "ExportAction.text");
            tooltiptext = NbBundle.getMessage(ExportAction.class,
                    "ExportAction.toolTipText");
            mnemonic = NbBundle.getMessage(ExportAction.class,
                    "ExportAction.mnemonic");
        } catch (MissingResourceException e) {
            LOG.error("Couldn't find resources. Using fallback settings.", e);
        }

        if (icon != null) {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(icon));
        }

        putValue(SHORT_DESCRIPTION, tooltiptext);
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        putValue(NAME, text);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final File file = StaticSwingTools.chooseFile(
                (path == null) ? DownloadManager.instance().getDestinationDirectory().toString() : path,
                true,
                new String[] { "zip" },
                org.openide.util.NbBundle.getMessage(
                    ExportDialog.class,
                    "ExportDialog.butFileActionPerformed().getDescription.return"),
                AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (file == null) {
            return;
        }

        if (file.getParent() != null) {
            path = file.getParent();
        }

        final WaitingDialogThread<Boolean> wdt = new WaitingDialogThread<Boolean>(
                StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                true,
                NbBundle.getMessage(ExportAction.class,
                    "ExportAction.actionPerformed().dialog"),
                null,
                100,
                true) {

                @Override
                protected Boolean doInBackground() throws Exception {
                    final boolean askedForDirectory = false;
                    int steps = servicesToExport.size();
                    int checkSteps = 0;
                    final List<ErrorContainer> errors = new ArrayList<ErrorContainer>();
                    final List<H2FeatureService> errorServices = new ArrayList<H2FeatureService>();

                    for (final AbstractCheckAction check : checks) {
                        checkSteps += check.getProgressSteps();
                    }

                    steps += checkSteps;
                    wd.setMax(steps);

                    if (wwGrBeans == null) {
                        setWwGrBeans();
                    }

                    setFgBaExpBeans();

                    for (final AbstractCheckAction check : checks) {
                        try {
                            errorServices.clear();
                            wd.setText("Prüfe " + check.getValue(NAME).toString());
                            check.startCheck(true, wd, errorServices);
                            if (Thread.interrupted()) {
                                return null;
                            }
                            filterCheckResult(errorServices);
                            errors.addAll(createErrorObjects(check.getValue(NAME).toString(), errorServices));
                        } catch (Exception e) {
                            return false;
                        } finally {
                            for (final H2FeatureService service : errorServices) {
                                H2FeatureService.removeTableIfExists(service.getTableName());
                            }
                        }
                    }

                    int number = 0;
                    String path = file.getAbsolutePath();
                    final char seperator = ((path.lastIndexOf('/') > path.lastIndexOf('\\')) ? '/' : '\\');
                    path = path.substring(0, path.lastIndexOf(seperator));
                    String tempPath = path + seperator + "exp" + number;

                    if (!errors.isEmpty()) {
                        // errors found. Create a error protocol and abort the export
                        final SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd-H-mm");
                        final String filename = formatter.format(new Date()) + "-Export-Fehlerprotokoll";
                        new File(path + seperator).mkdirs();
                        final List<DefaultFeatureServiceFeature> errorFeatures = createErrorFileContent(errors);
                        final ExportCsvDownload d = new ExportCsvDownload(
                                path
                                        + seperator
                                        + filename,
                                ".csv",
                                errorFeatures.toArray(new DefaultFeatureServiceFeature[errorFeatures.size()]),
                                null,
                                null);
                        DownloadManager.instance().add(d);

//                        final User user = SessionManager.getSession().getUser();
//
//                        if (!user.getName().equalsIgnoreCase("admin")) {
//                            return false;
//                        }
                    }

                    while (new File(tempPath).exists()) {
                        tempPath = path + seperator + "exp" + (++number);
                    }

                    new File(tempPath + seperator + THEME_FOLDER).mkdirs();
                    new File(tempPath + seperator + CATALOGUE_FOLDER).mkdirs();

                    try {
                        for (final AbstractFeatureService service : servicesToExport) {
                            // export all features
                            wd.setText("Exportiere " + service.toString());
                            wd.setProgress(++checkSteps);
                            String sortingColumn = null;

                            if (e.getActionCommand().equalsIgnoreCase("igm")
                                        && (service.getName().equalsIgnoreCase("Technologien")
                                            || service.getName().equalsIgnoreCase("Leistungen")
                                            || service.getName().equalsIgnoreCase("Dokumente")
                                            || service.getName().equalsIgnoreCase("Projekte")
                                            || service.getName().equalsIgnoreCase("fg_ba_doku")
                                            || service.getName().equalsIgnoreCase("fg_ba_proj")
                                            || service.getName().equalsIgnoreCase("k_leis")
                                            || service.getName().equalsIgnoreCase("k_tech")
                                            || service.getName().equalsIgnoreCase("k_gu_gu")
                                            || service.getName().equalsIgnoreCase("k_mahd_gu")
                                            || service.getName().equalsIgnoreCase("k_na_gu"))) {
                                continue;
                            }

                            if (service instanceof CidsLayer) {
                                final ClassAttribute ca = ((CidsLayer)service).getMetaClass()
                                            .getClassAttribute("sortingColumn");

                                if (ca != null) {
                                    sortingColumn = String.valueOf(ca.getValue());
                                }
                            }

                            try {
                                service.initAndWait();
                                final boolean geometryFound = hasGeometry(service);
                                String[] selectedbaCdArray = null;
                                final User user = SessionManager.getSession().getUser();

//                                if (user.getUserGroup().getName().equalsIgnoreCase("Administratoren")
//                                            || user.getUserGroup().getName().equalsIgnoreCase("lung_edit1")) {
                                selectedbaCdArray = getAttributesOfSelectedObjects("fg_ba", "ba_cd");
//                                }
                                String query = null;

                                if (!service.getName().startsWith("k_")) {
                                    // Issue 40: Kataloge: werden immer komplett ausgespielt
                                    query = createExportQuery(service, selectedbaCdArray);
                                }
                                List<DefaultFeatureServiceFeature> features = service.getFeatureFactory()
                                            .createFeatures(query,
                                                null,
                                                null,
                                                0,
                                                0,
                                                null);
                                if (Thread.interrupted()) {
                                    return null;
                                }

                                if (!service.getName().startsWith("k_")) {
                                    // Issue 40: Kataloge: werden immer komplett ausgespielt
                                    features = filterFeatures(service, features);
                                }

                                String name = service.getName();

                                if (service instanceof CidsLayer) {
                                    name = ((CidsLayer)service).getMetaClass().getTableName();
                                }

                                final String filename;

                                if (name.contains(".")) {
                                    name = name.substring(name.lastIndexOf(".") + 1);
                                }

                                if (geometryFound) {
                                    filename = tempPath + seperator + THEME_FOLDER + seperator + name;
                                } else {
                                    filename = tempPath + seperator + CATALOGUE_FOLDER + seperator + name;
                                }

                                final List<String[]> attribList = new ArrayList<String[]>();

                                for (final String attr : (List<String>)service.getOrderedFeatureServiceAttributes()) {
                                    final String[] attrName = new String[2];
                                    attrName[0] = attr;
                                    attrName[1] = attr;
                                    attribList.add(attrName);
                                }

                                if (sortingColumn != null) {
                                    final String col = sortingColumn;

                                    Collections.sort(features, new Comparator<DefaultFeatureServiceFeature>() {

                                            @Override
                                            public int compare(final DefaultFeatureServiceFeature o1,
                                                    final DefaultFeatureServiceFeature o2) {
                                                final Object attr1 = o1.getProperty(col);
                                                final Object attr2 = o2.getProperty(col);

                                                if ((attr1 == null) && (attr2 == null)) {
                                                    return 0;
                                                } else if (attr1 == null) {
                                                    return 1;
                                                } else if (attr2 == null) {
                                                    return -1;
                                                } else {
                                                    if ((attr1 instanceof Comparable)
                                                                && (attr2 instanceof Comparable)) {
                                                        return ((Comparable)attr1).compareTo(((Comparable)attr2));
                                                    } else {
                                                        return 0;
                                                    }
                                                }
                                            }
                                        });
                                }

                                exportShp(String.valueOf(checkSteps),
                                    features.toArray(new DefaultFeatureServiceFeature[features.size()]),
                                    filename,
                                    attribList,
                                    geometryFound,
                                    service);

                                if (geometryFound) {
                                    // write prj
                                    createPrjFile(filename + ".prj");
                                }

                                if (service instanceof CidsLayer) {
                                    final CidsLayer cl = (CidsLayer)service;
                                    final String link = cl.getMetaDocumentLink();

                                    if (link != null) {
                                        // write pdf
                                        BufferedInputStream bin = null;
                                        BufferedOutputStream out = null;

                                        try {
                                            final URL u = new URL(link);
                                            String outputFile;
                                            final String file = u.getFile().substring(u.getFile().lastIndexOf("/") + 1);

                                            if (geometryFound) {
                                                outputFile = tempPath + seperator + THEME_FOLDER + seperator
                                                            + file;
                                            } else {
                                                outputFile = tempPath + seperator + CATALOGUE_FOLDER + seperator
                                                            + file;
                                            }
                                            final InputStream in = WebAccessManager.getInstance().doRequest(u);
                                            bin = new BufferedInputStream(in);
                                            out = new BufferedOutputStream(new FileOutputStream(outputFile));
                                            final byte[] tmp = new byte[256];
                                            int byteCount;

                                            while ((byteCount = bin.read(tmp, 0, tmp.length)) != -1) {
                                                out.write(tmp, 0, byteCount);
                                            }
                                        } catch (Exception e) {
                                            LOG.error("Error while downloading meta document.", e);
                                        } finally {
                                            if (bin != null) {
                                                bin.close();
                                            }
                                            if (out != null) {
                                                out.close();
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                LOG.error("Error while retrieving features", ex);
                            }
                        }

                        final File zipFile = new File(file.getAbsolutePath());
                        zipDirectory(new File(tempPath), zipFile);

                        DownloadManager.instance().add(new FakeFileDownload(zipFile));
                    } finally {
                        final File f = new File(tempPath);

                        if (f.exists() && f.isDirectory()) {
                            FileUtils.deleteDirectory(f);
                        }
                    }

                    return true;
                }

                private void showDownloadManager() {
                    final JDialog downloadManager = DownloadManagerDialog.instance(AppBroker.getInstance().getComponent(
                                ComponentName.MAIN));
                    downloadManager.pack();
                    StaticSwingTools.showDialog(downloadManager);
                }

                private List<DefaultFeatureServiceFeature> createErrorFileContent(final List<ErrorContainer> errors) {
                    final List<DefaultFeatureServiceFeature> errorFeatures =
                        new ArrayList<DefaultFeatureServiceFeature>();
                    final TreeSet<String> set = new TreeSet<String>(new Comparator<String>() {

                                @Override
                                public int compare(final String o1, final String o2) {
                                    // wsa vor wbv, NIEMAND am Schluss und sonst in alphabetischer Reihenfolge
                                    if ((o1 == null) && (o2 == null)) {
                                        return 0;
                                    } else if ((o1 == null) && (o2 != null)) {
                                        return -1;
                                    } else if ((o1 != null) && (o2 == null)) {
                                        return 1;
                                    } else if (o1.startsWith("NIEMAND") && !o2.startsWith("NIEMAND")) {
                                        return 1;
                                    } else if (!o1.startsWith("NIEMAND") && o2.startsWith("NIEMAND")) {
                                        return -1;
                                    } else if (o1.startsWith("wsa") && o2.startsWith("wbv")) {
                                        return -1;
                                    } else if (o1.startsWith("wbv") && o2.startsWith("wsa")) {
                                        return 1;
                                    } else {
                                        return o1.compareTo(o2);
                                    }
                                }
                            });
//                    Collections.sort(errors);
                    String lastErrorMessage = "";
                    final Map<String, FeatureServiceAttribute> attributeMap =
                        new HashMap<String, FeatureServiceAttribute>();

                    for (final ErrorContainer ec : errors) {
                        set.add(ec.getOwner());
                    }

                    attributeMap.put(
                        "Prüfung",
                        new FeatureServiceAttribute("Prüfung", String.valueOf(Types.VARCHAR), true));

                    for (final String owner : set) {
                        attributeMap.put(
                            owner,
                            new FeatureServiceAttribute(owner, String.valueOf(Types.VARCHAR), true));
                    }

                    DefaultFeatureServiceFeature f = null;
                    for (final ErrorContainer ec : errors) {
                        if (!lastErrorMessage.equals(ec.getErrorMessage())) {
                            f = new DefaultFeatureServiceFeature();
                            errorFeatures.add(f);
                            f.setProperty("Prüfung", ec.getErrorMessage());
                            for (final String owner : set) {
                                f.setProperty(owner, "");
                            }
                            lastErrorMessage = ec.getErrorMessage();
                        }
                        if (f != null) {
                            f.setProperty(ec.getOwner(), "X");
                        }
                    }

                    return errorFeatures;
                }

                private boolean hasGeometry(final AbstractFeatureService service) {
                    final Map<String, FeatureServiceAttribute> attrMap = service.getFeatureServiceAttributes();

                    for (final FeatureServiceAttribute attr : attrMap.values()) {
                        if (attr.isGeometry()) {
                            return true;
                        }
                    }

                    return false;
                }

                private List<ErrorContainer> createErrorObjects(final String checkname,
                        final List<H2FeatureService> services) {
                    final TreeSet<String> errorSet = new TreeSet<String>();

                    try {
                        for (final H2FeatureService service : services) {
                            final List<JDBCFeature> features = service.getFeatureFactory()
                                        .createFeatures(null, null, null, 0, 0, null);
//                                        .createFeatures(createQuery(service), null, null, 0, 0, null);

                            if ((features != null) && !features.isEmpty()) {
                                for (final JDBCFeature feature : features) {
                                    final String owner = getGu(feature);
                                    if (!errorSet.contains(owner)) {
                                        errorSet.add(owner);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error while creating error message.", e);
                    }

                    final List<ErrorContainer> result = new ArrayList<ErrorContainer>();

                    for (final String owner : errorSet) {
                        result.add(new ErrorContainer(checkname, owner));
                    }

                    return result;
                }

                private void filterCheckResult(final List<H2FeatureService> services) {
                    try {
                        for (final AbstractFeatureService service : services) {
                            final List<JDBCFeature> features = service.getFeatureFactory()
                                        .createFeatures(null, null, null, 0, 0, null);
//                                        .createFeatures(createQuery(service), null, null, 0, 0, null);

                            if (e.getActionCommand().equalsIgnoreCase("igm")
                                        && (service.getName().contains("Leis")
                                            || service.getName().contains("Tech"))) {
                                // do nothing to remove all features from the service
                            } else {
                                List<DefaultFeatureServiceFeature> copiedFeaturesList = new ArrayList(features);
                                copiedFeaturesList = filterErrorFeatures(service, copiedFeaturesList);
                                features.removeAll(copiedFeaturesList);
                            }

                            for (final JDBCFeature f : features) {
                                f.delete();
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error while filtering check results.", e);
                    }
                }

                private List<DefaultFeatureServiceFeature> filterErrorFeatures(final AbstractFeatureService service,
                        final List<DefaultFeatureServiceFeature> features) {
                    final User user = SessionManager.getSession().getUser();
                    final Map<String, FeatureServiceAttribute> attributes = service.getFeatureServiceAttributes();
                    boolean hasWdm = false;

                    if ((features == null) || features.isEmpty()) {
                        return features;
                    }

                    for (final FeatureServiceAttribute attr : attributes.values()) {
                        if (attr.getName().equals("ww_gr")) {
                            hasWdm = true;
                        }
                    }

                    if (hasWdm) {
                        final List<DefaultFeatureServiceFeature> acceptedFeatures =
                            new ArrayList<DefaultFeatureServiceFeature>(features.size());

                        for (final DefaultFeatureServiceFeature feature : features) {
                            boolean acceptFeature = true;
                            if (!ExportDialog.getInstance().has1501() && isWdm1501(feature)) {
                                acceptFeature = false;
                            }
                            if (!ExportDialog.getInstance().has1502() && isWdm1502(feature)) {
                                acceptFeature = false;
                            }
                            if (!ExportDialog.getInstance().has1503() && isWdm1503(feature)) {
                                acceptFeature = false;
                            }
                            if (!ExportDialog.getInstance().has1504() && isWdm1504(feature)) {
                                acceptFeature = false;
                            }
                            if (!ExportDialog.getInstance().has1505() && isWdm1505(feature)) {
                                acceptFeature = false;
                            }

                            if (!user.getUserGroup().getName().equalsIgnoreCase("administratoren")
                                        && isInvalidForeignData(feature)) {
                                acceptFeature = false;
                            }

                            if (acceptFeature) {
                                acceptedFeatures.add(feature);
                            }
                        }

                        return acceptedFeatures;
                    } else {
                        return features;
                    }
                }

                private List<DefaultFeatureServiceFeature> filterFeatures(final AbstractFeatureService service,
                        final List<DefaultFeatureServiceFeature> features) {
                    final User user = SessionManager.getSession().getUser();
                    final Map<String, FeatureServiceAttribute> attributes = service.getFeatureServiceAttributes();
                    boolean hasWdm = false;

                    if ((features == null) || features.isEmpty()) {
                        return features;
                    }

                    for (final FeatureServiceAttribute attr : attributes.values()) {
                        if (attr.getName().equals("ww_gr")) {
                            hasWdm = true;
                        }
                    }
                    if (!hasWdm) {
                        if (features.get(0).getProperty("ww_gr") != null) {
                            hasWdm = true;
                        }
                    }

                    if (hasWdm && !user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                        final List<DefaultFeatureServiceFeature> acceptedFeatures =
                            new ArrayList<DefaultFeatureServiceFeature>(features.size());

                        for (final DefaultFeatureServiceFeature feature : features) {
                            boolean acceptFeature = true;
                            if (!ExportDialog.getInstance().has1501() && isWdm1501(feature)) {
                                acceptFeature = false;
                            }
                            if (!ExportDialog.getInstance().has1502() && isWdm1502(feature)) {
                                acceptFeature = false;
                            }
                            if (!ExportDialog.getInstance().has1503() && isWdm1503(feature)) {
                                acceptFeature = false;
                            }
                            if (!ExportDialog.getInstance().has1504() && isWdm1504(feature)) {
                                acceptFeature = false;
                            }
                            if (!ExportDialog.getInstance().has1505() && isWdm1505(feature)) {
                                acceptFeature = false;
                            }

                            if (acceptFeature && isInvalidForeignData(feature)) {
                                acceptFeature = false;
                            }

                            if (acceptFeature) {
                                acceptedFeatures.add(feature);
                            }
                        }

                        return acceptedFeatures;
                    } else {
                        return features;
                    }
                }

                private void setWwGrBeans() throws Exception {
                    final MetaClass WW_GR_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_ww_gr");
                    final String query = "select distinct " + WW_GR_MC.getID() + ", " + WW_GR_MC.getPrimaryKey()
                                + " from dlm25w.k_ww_gr";
                    final User user = SessionManager.getSession().getUser();
                    final MetaObject[] mos = SessionManager.getProxy().getMetaObjectByQuery(user, query);

                    if (mos != null) {
                        wwGrBeans = new HashMap<Integer, CidsBean>();

                        for (final MetaObject mo : mos) {
                            wwGrBeans.put((Integer)mo.getBean().getProperty("ww_gr"), mo.getBean());
                        }
                    }
                }

                private void setFgBaExpBeans() throws Exception {
                    final MetaClass FG_BA_EXP_MC = ClassCacheMultiple.getMetaClass(
                            AppBroker.DOMAIN_NAME,
                            "dlm25w.fg_ba_exp");
                    final User user = SessionManager.getSession().getUser();
                    final String query = "dlm25w.fg_ba_exp.ww_gr in (select id from dlm25w.k_ww_gr where owner = '"
                                + user.getUserGroup().getName() + "')";
                    final CidsLayer layer = new CidsLayer(FG_BA_EXP_MC);
                    layer.initAndWait();
                    expFeatures = layer.getFeatureFactory().createFeatures(query, null, null, 0, 0, null);
                }

                private String getGu(final DefaultFeatureServiceFeature f) {
                    Object wwGrObj = null;
                    try {
                        wwGrObj = f.getProperty("ww_gr");
                    } catch (Throwable t) {
                        LOG.error("error", t);
                    }
                    Integer wwGr = null;

                    if (wwGrObj instanceof String) {
                        try {
                            wwGr = Integer.parseInt((String)f.getProperty("ww_gr"));
                        } catch (NumberFormatException e) {
                            LOG.error("wdm is not a number");
                        }
                    } else if (wwGrObj instanceof Integer) {
                        wwGr = (Integer)wwGrObj;
                    }

                    if (wwGr != null) {
                        final CidsBean wwGrBean = wwGrBeans.get(wwGr);

                        return (String)wwGrBean.getProperty("owner");
                    }

                    return "unbekannt";
                }

                private boolean isWdm(final DefaultFeatureServiceFeature feature, final int wdm) {
                    final Integer wwGr = (Integer)feature.getProperty("ww_gr");

                    if (wwGr != null) {
                        final CidsBean wwGrBean = wwGrBeans.get(wwGr);

                        if (((Integer)wwGrBean.getProperty("wdm")) == wdm) {
                            return true;
                        }
                    }

                    return false;
                }

                private boolean isWdm1501(final DefaultFeatureServiceFeature feature) {
                    return isWdm(feature, 1501);
                }

                private boolean isWdm1502(final DefaultFeatureServiceFeature feature) {
                    return isWdm(feature, 1502);
                }

                private boolean isWdm1503(final DefaultFeatureServiceFeature feature) {
                    return isWdm(feature, 1503);
                }

                private boolean isWdm1504(final DefaultFeatureServiceFeature feature) {
                    return isWdm(feature, 1504);
                }

                private boolean isWdm1505(final DefaultFeatureServiceFeature feature) {
                    return isWdm(feature, 1505);
                }

                private boolean isInvalidForeignData(final DefaultFeatureServiceFeature feature) {
                    final Integer wwGr = (Integer)feature.getProperty("ww_gr");

                    if ((wwGr == null) || ((wwGr != null) && !isOwnWwGr(wwGr))) {
                        if (!ExportDialog.getInstance().hasForeignData()) {
                            return true;
                        }
                        final String baCd = (String)feature.getProperty("ba_cd");

                        final List<DefaultFeatureServiceFeature> correspondingExp = getExpForBaCd(baCd);

                        for (final DefaultFeatureServiceFeature exp : correspondingExp) {
                            Geometry g = exp.getGeometry();
                            g = g.buffer(0.1, 8, BufferParameters.CAP_FLAT);

                            if (feature.getGeometry().intersects(g)) {
                                if (feature.getGeometry().within(g)) {
                                    return false;
                                } else {
                                    feature.setGeometry(feature.getGeometry().intersection(g));
                                    final Double von = (Double)feature.getProperty("ba_st_von");
                                    final Double bis = (Double)feature.getProperty("ba_st_bis");
                                    final Double expVon = (Double)exp.getProperty("ba_st_von");
                                    final Double expBis = (Double)exp.getProperty("ba_st_bis");

                                    if ((von != null) && (expVon != null) && (von < expVon)) {
                                        feature.setProperty("ba_st_von", expVon);
                                    }
                                    if ((bis != null) && (expBis != null) && (bis > expBis)) {
                                        feature.setProperty("ba_st_bis", expBis);
                                    }

                                    return false;
                                        // todo: Pruefungen sollen exp berücksichtigen
                                }
                            }
                        }

                        return true;
                    }

                    return false;
                }

                private List<DefaultFeatureServiceFeature> getExpForBaCd(final String baCd) {
                    final List<DefaultFeatureServiceFeature> result = new ArrayList<DefaultFeatureServiceFeature>();

                    for (final DefaultFeatureServiceFeature feature : expFeatures) {
                        final String baCdTmp = (String)feature.getProperty("ba_cd");

                        if ((baCdTmp != null) && baCdTmp.equals(baCd)) {
                            result.add(feature);
                        }
                    }

                    return result;
                }

                private boolean isOwnWwGr(final Integer wwGr) {
                    final List<CidsBean> ownBeans = AppBroker.getInstance().getOwnWwGrList();

                    for (final CidsBean bean : ownBeans) {
                        if ((bean.getProperty("ww_gr") != null) && bean.getProperty("ww_gr").equals(wwGr)) {
                            return true;
                        }
                    }

                    return false;
                }

                private String createQuery(final AbstractFeatureService service) {
                    String query = null;
                    final Map<String, FeatureServiceAttribute> attributes = service.getFeatureServiceAttributes();
                    boolean hasWdm = false;

                    for (final FeatureServiceAttribute attr : attributes.values()) {
                        if (attr.getName().equals("ww_gr")) {
                            hasWdm = true;
                        }
                    }

                    if (!hasWdm) {
                        return null;
                    }

                    if (!ExportDialog.getInstance().has1501()) {
                        if (query == null) {
                            query = "1501";
                        } else {
                            query += ", 1501";
                        }
                    }
                    if (!ExportDialog.getInstance().has1502()) {
                        if (query == null) {
                            query = "1502";
                        } else {
                            query += ", 1502";
                        }
                    }
                    if (!ExportDialog.getInstance().has1503()) {
                        if (query == null) {
                            query = "1503";
                        } else {
                            query += ",1503";
                        }
                    }
                    if (!ExportDialog.getInstance().has1504()) {
                        if (query == null) {
                            query = "1504";
                        } else {
                            query += ", 1504";
                        }
                    }
                    if (!ExportDialog.getInstance().has1505()) {
                        if (query == null) {
                            query = "1505";
                        } else {
                            query += ", 1505";
                        }
                    }

                    if (query != null) {
                        query = " dlm25wPk_ww_gr1.wdm not in (" + query + ")";
                    }

                    return query;
                }

                private String createExportQuery(final AbstractFeatureService service,
                        final String[] selectedbaCdArray) {
                    String query = null;
                    final Map<String, FeatureServiceAttribute> attributes = service.getFeatureServiceAttributes();
                    boolean hasWdm = false;

                    for (final FeatureServiceAttribute attr : attributes.values()) {
                        if (attr.getName().equals("ww_gr")) {
                            hasWdm = true;
                        }
                    }

                    boolean hasBaCd = false;

                    for (final FeatureServiceAttribute attr : attributes.values()) {
                        if (attr.getName().equals("ba_cd")) {
                            hasBaCd = true;
                        }
                    }

                    if (hasWdm || service.getName().equals("Stationen")) {
                        if (!ExportDialog.getInstance().has1501()) {
                            if (query == null) {
                                query = "1501";
                            } else {
                                query += ", 1501";
                            }
                        }
                        if (!ExportDialog.getInstance().has1502()) {
                            if (query == null) {
                                query = "1502";
                            } else {
                                query += ", 1502";
                            }
                        }
                        if (!ExportDialog.getInstance().has1503()) {
                            if (query == null) {
                                query = "1503";
                            } else {
                                query += ",1503";
                            }
                        }
                        if (!ExportDialog.getInstance().has1504()) {
                            if (query == null) {
                                query = "1504";
                            } else {
                                query += ", 1504";
                            }
                        }
                        if (!ExportDialog.getInstance().has1505()) {
                            if (query == null) {
                                query = "1505";
                            } else {
                                query += ", 1505";
                            }
                        }

                        if (query != null) {
                            if (service.getName().equals("Stationen")) {
                                query =
                                    "  ba_cd in (select ba_cd from dlm25w.fg_ba b join dlm25w.k_ww_gr gr on (b.ww_gr = gr.id) where  gr.wdm not in ("
                                            + query
                                            + "))";
                            } else {
                                query = " dlm25wPk_ww_gr1.wdm not in ("
                                            + query
                                            + ")";
                            }
                        }

                        final User user = SessionManager.getSession().getUser();

                        if (!user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
                            if (isGu()) {
                                if (service.getName().equals("Stationen")) {
                                    if (query == null) {
                                        query =
                                            "  ba_cd in (select ba_cd from dlm25w.fg_ba b join dlm25w.k_ww_gr gr on (b.ww_gr = gr.id) where  gr.wdm in (select wdm from dlm25w.k_ww_gr where owner = '"
                                                    + user.getUserGroup().getName()
                                                    + "'))";
                                    } else {
                                        query +=
                                            " and ba_cd in (select ba_cd from dlm25w.fg_ba b join dlm25w.k_ww_gr gr on (b.ww_gr = gr.id) where  gr.wdm in (select wdm from dlm25w.k_ww_gr where owner = '"
                                                    + user.getUserGroup().getName()
                                                    + "'))";
                                    }
                                } else {
                                    if (query == null) {
                                        query =
                                            " dlm25wPk_ww_gr1.wdm in (select wdm from dlm25w.k_ww_gr where owner = '"
                                                    + user.getUserGroup().getName()
                                                    + "')";
                                    } else {
                                        query +=
                                            " and dlm25wPk_ww_gr1.wdm in (select wdm from dlm25w.k_ww_gr where owner = '"
                                                    + user.getUserGroup().getName()
                                                    + "')";
                                    }
                                }
                            } else {
                                if (query == null) {
                                    query = "false";
                                } else {
                                    query = "("
                                                + query
                                                + ") and false";
                                }
                            }

                            if (ExportDialog.getInstance().hasForeignData()) {
                                if ((query != null) && hasBaCd) {
                                    query = "("
                                                + query
                                                + ") or (ba_cd in (select ba.ba_cd from dlm25w.fg_ba_exp e join "
                                                + "dlm25w.fg_ba_linie l on (ba_st = l.id) join dlm25w.fg_ba_punkt p "
                                                + "on (l.von = p.id) join dlm25w.fg_ba ba on (ba.id = p.route) "
                                                + "where e.ww_gr  in (select id from dlm25w.k_ww_gr where owner = '"
                                                + user.getUserGroup().getName()
                                                + "')))";
                                }
                            }
                        }
                    }

                    if (selectedbaCdArray != null) {
                        if (hasBaCd) {
                            if (query == null) {
                                query = "(ba_cd = any("
                                            + SQLFormatter.createSqlArrayString(selectedbaCdArray)
                                            + "))";
                            } else {
                                query = "("
                                            + query
                                            + ") and (ba_cd = any("
                                            + SQLFormatter.createSqlArrayString(selectedbaCdArray)
                                            + "))";
                            }
                        }
                    }

                    return query;
                }

                private boolean isGu() {
                    final User user = SessionManager.getSession().getUser();

                    for (final CidsBean bean : wwGrBeans.values()) {
                        final String owner = (String)bean.getProperty("owner");

                        if ((owner != null) && owner.equals(user.getUserGroup().getName())) {
                            final String prefix = (String)bean.getProperty("praefix");

                            if (prefix != null) {
                                return true;
                            }
                        }
                    }

                    return false;
                }

                private void exportShp(final String id,
                        final FeatureServiceFeature[] features,
                        final String filename,
                        final List<String[]> attribList,
                        final boolean hasGeometry,
                        final AbstractFeatureService service) throws Exception {
                    try {
                        boolean emptyShape = false;
                        FeatureServiceFeature[] featureArray = features;

                        if (features.length == 0) {
                            // create en empty shape file. Therefore create a dummy feature and then remove it from the
                            // files to get empty files, which contains only the header (The ShapeFile class does not
                            // create any file, if the feature collection is empty)
                            emptyShape = true;
                            final FeatureServiceFeature feature = service.getFeatureFactory().createNewFeature();
                            final String geometryType = service.getGeometryType();
                            final Geometry g = GeometryUtils.createDummyGeometry(geometryType);
                            feature.setGeometry(g);
                            featureArray = new FeatureServiceFeature[] { feature };
                        }

                        final JumpShapeWriter shapeWriter = new JumpShapeWriter();
                        final String charset = Charset.defaultCharset().name();

                        shapeWriter.writeShpFile(featureArray,
                            new File(filename + ".shp"),
                            attribList,
                            charset);

                        if (emptyShape) {
                            final String geometryType = service.getGeometryType();
                            final int shpGeoType = GeometryUtils.getShpGeometryType(geometryType);

                            try {
                                GeometryUtils.clearShpOrShxFile(filename + ".shp", shpGeoType);
                                GeometryUtils.clearShpOrShxFile(filename + ".shx", shpGeoType);
                                GeometryUtils.clearDbfFile(filename + ".dbf");
                            } catch (Exception e) {
                                LOG.error("Cannot remove content from shape. So remove it completely", e);
                                final File shapeFile = new File(filename + ".shp");
                                final File shxFile = new File(filename + ".shx");
                                final File dbfFile = new File(filename + ".dbf");

                                if (shapeFile.exists()) {
                                    shapeFile.delete();
                                }
                                if (shxFile.exists()) {
                                    shxFile.delete();
                                }
                                if (dbfFile.exists()) {
                                    dbfFile.delete();
                                }
                            }
                        }
                        final BufferedWriter bwCpg = new BufferedWriter(new FileWriter(
                                    filename
                                            + ".cpg"));
                        bwCpg.write(charset);
                        bwCpg.close();

                        if (!hasGeometry) {
                            String shpFileName = filename
                                        + ".shp";

                            deleteFileIfExists(shpFileName);
                            shpFileName = filename
                                        + ".shx";
                            deleteFileIfExists(shpFileName);
                        }
                    } catch (Exception ex) {
                        LOG.error("Error during export", ex);
                        throw ex;
                    }
                }

                private void createPrjFile(final String filename) {
                    try {
                        final BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
                        bw.write(PRJ_CONTENT);
                        bw.close();
                    } catch (Exception e) {
                        LOG.error("Error while writing .prj file.", e);
                    }
                }

                private void deleteFileIfExists(final String fileName) {
                    final File fileToDelete = new File(fileName);

                    if (fileToDelete.exists()) {
                        fileToDelete.delete();
                    }
                }

                /**
                 * Determines all attribute values of all selected features of the given class.
                 *
                 * @param   cidsClassName  the cids class name of the selected features
                 * @param   attribute      the name of the attribute
                 *
                 * @return  an array with all atrtibute values of the selected features of the given class.
                 */
                private String[] getAttributesOfSelectedObjects(final String cidsClassName, final String attribute) {
                    final ActiveLayerModel model = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                                .getMappingModel();
                    final TreeSet<String> ids = new TreeSet<String>();
                    final String[] resultArray;
                    int index = 0;

                    for (final MapService service : model.getMapServices().values()) {
                        if (service instanceof CidsLayer) {
                            final CidsLayer cidsLayer = (CidsLayer)service;

                            if (cidsLayer.getMetaClass().getName().equals(cidsClassName)) {
                                final List<Feature> selectedFeatureList = SelectionManager.getInstance()
                                            .getSelectedFeatures(cidsLayer);

                                if (selectedFeatureList != null) {
                                    for (final Feature f : selectedFeatureList) {
                                        final CidsLayerFeature feature = (CidsLayerFeature)f;
                                        ids.add((String)feature.getProperty(attribute));
                                    }
                                }
                            }
                        }
                    }

                    resultArray = new String[ids.size()];

                    for (final String i : ids) {
                        resultArray[index++] = i;
                    }

                    if (resultArray.length == 0) {
                        return null;
                    } else {
                        return resultArray;
                    }
                }

                @Override
                protected void done() {
                    try {
                        final Boolean exportResult = get();

                        if ((exportResult != null) && exportResult) {
                            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                NbBundle.getMessage(
                                    ExportAction.class,
                                    "ExportAction.actionPerformed().result.success.text"),
                                NbBundle.getMessage(
                                    ExportAction.class,
                                    "ExportAction.actionPerformed().result.success.title"),
                                JOptionPane.INFORMATION_MESSAGE);
                        } else if ((exportResult != null) && !exportResult) {
                            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                NbBundle.getMessage(
                                    ExportAction.class,
                                    "ExportAction.actionPerformed().result.text"),
                                NbBundle.getMessage(
                                    ExportAction.class,
                                    "ExportAction.actionPerformed().result.title"),
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        LOG.error("Error during export", e);
                    }
                }
            };

        wdt.start();
    }

    @Override
    public void configure(final Element parent) {
        // This action ignores the local configuration
    }

    @Override
    public void masterConfigure(final Element parent) {
        final Element actionRootElement = parent.getChild(ROOT_TAG);
        servicesToExport = new ArrayList<AbstractFeatureService>();
        checks = new ArrayList<AbstractCheckAction>();

        if (actionRootElement == null) {
            return;
        }

        if (!AppBroker.getInstance().isWawiOrAdminUser()) {
            // The export function should be disabled
            return;
        }

        final Element services = actionRootElement.getChild(FEATURE_SERVICES_TAG);

        final Element[] orderedLayers = CidsLayerFactory.orderLayers(services);

        for (final Element curLayerElement : orderedLayers) {
            // The capability and model attribute is not required for feature services and other services
            // does not make sense
            final ServiceLayer sl = CidsLayerFactory.createLayer(curLayerElement, null, null);

            if (sl instanceof AbstractFeatureService) {
                servicesToExport.add((AbstractFeatureService)sl);
            } else {
                LOG.warn(sl + " is not a feature service and will be ignored for the export.");
            }
        }

        final Element checkListElement = actionRootElement.getChild(CHECK_LIST);
        final List<Element> checkList = checkListElement.getChildren(CHECK);

        for (final Element check : checkList) {
            final String checkClass = check.getAttributeValue(JAVA_CLASS_ATTR);

            if (checkClass != null) {
                try {
                    final Class clazz = Class.forName(checkClass);

                    if (AbstractCheckAction.class.isAssignableFrom(clazz)) {
                        checks.add((AbstractCheckAction)clazz.newInstance());
                    } else {
                        LOG.warn("The checkclass " + checkClass + " is no instance of AbstractCheckAction");
                    }
                } catch (Exception e) {
                    LOG.warn("Cannot instantiate the check class " + checkClass, e);
                }
            } else {
                LOG.warn("Check without java class found");
            }
        }
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        // This action has no local configuration
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        try {
            zipDirectory(new File("/home/therter/tmp/exp"), new File("/home/therter/tmp/exp.zip"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a zip file that contains the content of the given directory.
     *
     * @param   inputDir  the directory that should be zipped
     * @param   file      the zip file that should be created
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void zipDirectory(final File inputDir, final File file) throws Exception {
        ZipOutputStream out = null;

        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream((file))));
//            out.setEncoding("Cp850");//requires org.apache.tools.zip.ZipOutputStream;
//            out.setEncoding("Cp437");
//            out.setEncoding("UTF-8");

            zipDirectory(inputDir, out, "");
        } catch (Exception e) {
            LOG.error("Error while creating zip file.", e);
            throw e;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   inputDir  DOCUMENT ME!
     * @param   out       DOCUMENT ME!
     * @param   dirName   DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void zipDirectory(final File inputDir, final ZipOutputStream out, final String dirName)
            throws Exception {
        final byte[] tmp = new byte[256];
        int byteCount;

        for (final File f : inputDir.listFiles()) {
            if (f.isDirectory()) {
                if (dirName.equals("")) {
                    zipDirectory(f, out, f.getName() + "/");
                } else {
                    zipDirectory(f, out, dirName + f.getName() + "/");
                }
            } else {
                final ZipEntry entry = new ZipEntry(dirName + f.getName());
                out.putNextEntry(entry);
                final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));

                try {
                    while ((byteCount = bis.read(tmp, 0, 256)) != -1) {
                        out.write(tmp, 0, byteCount);
                    }
                } finally {
                    out.closeEntry();
                    bis.close();
                }
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ErrorContainer implements Comparable<ErrorContainer> {

        //~ Instance fields ----------------------------------------------------

        private String errorMessage;
        private String owner;
        private int count = 1;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ErrorContainer object.
         *
         * @param  errorMessage  DOCUMENT ME!
         * @param  owner         DOCUMENT ME!
         */
        public ErrorContainer(final String errorMessage, final String owner) {
            this.errorMessage = errorMessage;
            this.owner = owner;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the errorMessage
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  errorMessage  the errorMessage to set
         */
        public void setErrorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the owner
         */
        public String getOwner() {
            return owner;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  owner  the owner to set
         */
        public void setOwner(final String owner) {
            this.owner = owner;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the count
         */
        public int getCount() {
            return count;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  count  the count to set
         */
        public void setCount(final int count) {
            this.count = count;
        }

        /**
         * DOCUMENT ME!
         */
        public void increaseCount() {
            ++count;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ErrorContainer) {
                final ErrorContainer other = (ErrorContainer)obj;

                if (other.owner.equals(owner) && other.errorMessage.equals(errorMessage)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = (17 * hash)
                        + ((this.errorMessage != null) ? this.errorMessage.hashCode() : 0);
            hash = (17 * hash)
                        + ((this.owner != null) ? this.owner.hashCode() : 0);
            return hash;
        }

        @Override
        public int compareTo(final ErrorContainer o) {
            if (errorMessage.equals(o.errorMessage)) {
                return owner.compareTo(o.owner);
            } else {
                return errorMessage.compareTo(o.errorMessage);
            }
        }
    }
}
