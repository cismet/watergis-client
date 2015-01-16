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
package de.cismet.watergis.gui.actions.map;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.DrawingManager;

import de.cismet.cismap.commons.RestrictedFileSystemView;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.DrawingFeature;
import de.cismet.cismap.commons.features.DrawingSLDStyledFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.tools.ExportCsvDownload;
import de.cismet.cismap.commons.tools.ExportDownload;

import de.cismet.tools.gui.ConfirmationJFileChooser;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SaveDrawingsAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SaveDrawingsAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public SaveDrawingsAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SaveDrawingsAction.class,
                "SaveDrawingsAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-savetodrive.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        JFileChooser fc;

        try {
            fc = new ConfirmationJFileChooser(WatergisApp.getDIRECTORYPATH_WATERGIS());
        } catch (Exception bug) {
            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
            fc = new JFileChooser(WatergisApp.getDIRECTORYPATH_WATERGIS(), new RestrictedFileSystemView());
        }

        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    return f.isDirectory()
                                || f.getName().toLowerCase().endsWith(".ze"); // NOI18N
                }

                @Override
                public String getDescription() {
                    return org.openide.util.NbBundle.getMessage(
                            SaveDrawingsAction.class,
                            "SaveDrawingsAction.actionPerformed.FileFilter.getDescription"); // NOI18N
                }
            });

        final int state = fc.showSaveDialog(AppBroker.getInstance().getComponent(ComponentName.MAIN));
        if (LOG.isDebugEnabled()) {
            LOG.debug("state:" + state); // NOI18N
        }

        if (state == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            if (file.exists()) {
                file.delete();
            }

            if (!file.getName().endsWith(".ze")) { // NOI18N
                file = new File(file.getAbsolutePath() + ".ze");
            }

            saveDrawing(file);
        }
    }

    /**
     * Save the drawings in an csv file with the ending ze.
     *
     * @param  file  the file to save the drawings in
     */
    private void saveDrawing(final File file) {
        try {
            final List<DrawingSLDStyledFeature> features = DrawingManager.getAllFeatures();
            final List<DefaultFeatureServiceFeature> featureList = new ArrayList<DefaultFeatureServiceFeature>();
            final LayerProperties layerProps = new DefaultLayerProperties();
            H2FeatureService service = new H2FeatureService(
                    "tmpDrawing",
                    H2FeatureServiceFactory.DB_NAME,
                    DrawingManager.DRAWING_TABLE_NAME,
                    null);
            service.initAndWait();
            layerProps.setFeatureService(service);

            for (final DrawingSLDStyledFeature dFeature : features) {
                final DefaultFeatureServiceFeature f = new DefaultFeatureServiceFeature(dFeature.getId(),
                        dFeature.getGeometry(),
                        layerProps);
                final HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", dFeature.getId());
                properties.put("geom", dFeature.getGeometry());
                properties.put("type", dFeature.getGeometryType().toString());
                properties.put("text", dFeature.getText());
                properties.put("autoscale", dFeature.isAutoscale());
                properties.put("background", dFeature.getPrimaryAnnotationHalo() != null);
                properties.put(
                    "fontsize",
                    ((dFeature.getPrimaryAnnotationFont() != null) ? dFeature.getPrimaryAnnotationFont().getSize()
                                                                   : null));
                f.setProperties(properties);
                featureList.add(f);
            }

            final ExportDownload ed = new ExportCsvDownload(
                    file.getName().substring(0, file.getName().lastIndexOf(".")),
                    ".ze",
                    featureList.toArray(new FeatureServiceFeature[featureList.size()]),
                    service,
                    null);
            DownloadManager.instance().setDestinationDirectory(file.getParentFile());
            DownloadManager.instance().add(ed);
            ((H2FeatureServiceFactory)service.getFeatureFactory()).closeConnection();
            service = null;
        } catch (Exception ee) {
            LOG.error("", ee);
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
