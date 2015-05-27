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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.jdom.Element;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.CidsLayerFactory;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.ExportShapeDownload;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.downloadmanager.Download;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.downloadmanager.MultipleDownload;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.checks.AbstractCheckAction;

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

    //~ Instance fields --------------------------------------------------------

    private List<AbstractFeatureService> servicesToExport;
    private List<AbstractCheckAction> checks;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DownloadManagerAction object.
     */
    public ExportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-download-alt.png");
        String text = "Export";
        String tooltiptext = "Datenexport";
        String mnemonic = "E";

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
        final List<Download> downloads = new ArrayList<Download>();
        boolean askedForDirectory = false;

        for (final AbstractCheckAction check : checks) {
            if (!check.startCheck(true)) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(
                        ExportAction.class,
                        "ExportAction.actionPerformed().result.text",
                        check.getValue(NAME).toString()),
                    NbBundle.getMessage(
                        ExportAction.class,
                        "ExportAction.actionPerformed().result.title"),
                    JOptionPane.INFORMATION_MESSAGE);

                return;
            }
        }

        for (final AbstractFeatureService service : servicesToExport) {
            // export all features
            final Geometry g = ZoomToLayerWorker.getServiceBounds(service);
            final XBoundingBox bb = new XBoundingBox(g);

            try {
                final List<DefaultFeatureServiceFeature> features = service.getFeatureFactory()
                            .createFeatures(service.getQuery(), bb, null, 0, 0, null);
                String name = service.getName();

                if (name.contains(".")) {
                    name = name.substring(0, name.lastIndexOf("."));
                }

                if (!askedForDirectory) {
                    DownloadManagerDialog.showAskingForUserTitle(CismapBroker.getInstance().getMappingComponent());
                    askedForDirectory = true;
                }

                downloads.add(new ExportShapeDownload(
                        name,
                        ".shp",
                        features.toArray(new DefaultFeatureServiceFeature[features.size()])));
            } catch (Exception ex) {
                LOG.error("Error while retrieving features", ex);
            }
        }

        if (downloads.size() > 1) {
            DownloadManager.instance()
                    .add(new MultipleDownload(
                            downloads,
                            NbBundle.getMessage(
                                ExportAction.class,
                                "ExportAction.actionPerformed().download.text")));
        } else if (downloads.size() == 1) {
            DownloadManager.instance().add(downloads.get(0));
        }
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
}
