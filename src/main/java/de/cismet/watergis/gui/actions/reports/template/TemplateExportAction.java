/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.gui.actions.reports.template;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.jdom.Element;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.watergis.server.actions.RefreshTemplateAction;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.actions.ServerActionParameter;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.tools.ExportShapeDownload;

import de.cismet.cismap.custom.attributerule.MessageDialog;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.reports.GerinneGFlReportAction;

import de.cismet.watergis.utils.GeometryUtils;

import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;

/**
 * Exports a template.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TemplateExportAction extends AbstractAction implements Comparable<TemplateExportAction> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneGFlReportAction.class);
    protected static final ConnectionContext CC = ConnectionContext.create(
            AbstractConnectionContext.Category.ACTION,
            "TemplateExportAction");

    //~ Instance fields --------------------------------------------------------

    List<TemplateAttribute> attributes;
    private MetaClass metaClass;
    private int templateCrs = 5650;
    private int position = -1;
    private String folder;
    private String refresh = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TemplateExportAction object.
     *
     * @param  config  DOCUMENT ME!
     */
    public TemplateExportAction(final Element config) {
        init(config);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Initialise with the configuration element.
     *
     * @param  config  DOCUMENT ME!
     */
    private void init(final Element config) {
        final Element nameElement = config.getChild("Name");
        final Element tooltipElement = config.getChild("Tooltip");
        final Element iconElement = config.getChild("Icon");
        final Element mnemonicElement = config.getChild("Mnemonic");
        final Element crsElement = config.getChild("Crs");
        final Element tableElement = config.getChild("TableName");
        final Element attributesElement = config.getChild("Attributes");
        final Element positionElement = config.getChild("Position");
        final Element folderElement = config.getChild("Folder");
        final Element refreshElement = config.getChild("refresh");

        if ((positionElement != null) && (positionElement.getText() != null)) {
            try {
                position = Integer.parseInt(positionElement.getText());
            } catch (NumberFormatException e) {
                LOG.error("Invalid position found: " + positionElement.getText(), e);
            }
        }

        if ((nameElement != null) && (nameElement.getText() != null)) {
            putValue(NAME, nameElement.getText());
        }

        if ((folderElement != null) && (folderElement.getText() != null)) {
            folder = folderElement.getText();
        }

        if ((refreshElement != null) && (refreshElement.getText() != null)) {
            refresh = refreshElement.getText();
        }

        if ((tooltipElement != null) && (tooltipElement.getText() != null)) {
            putValue(SHORT_DESCRIPTION, tooltipElement.getText());
        }

        if ((iconElement != null) && (iconElement.getText() != null)) {
            final URL icon = getClass().getResource(iconElement.getText());

            if (icon != null) {
                putValue(SMALL_ICON, new javax.swing.ImageIcon(icon));
            }
        }

        if ((mnemonicElement != null) && (mnemonicElement.getText() != null)) {
            putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonicElement.getText()).getKeyCode());
        }

        if ((crsElement != null) && (crsElement.getText() != null)) {
            try {
                templateCrs = Integer.parseInt(crsElement.getText());
            } catch (NumberFormatException e) {
                LOG.error("Invalid template crs found", e);
            }
        }

        if ((tableElement != null) && (tableElement.getText() != null)) {
            metaClass = ClassCacheMultiple.getMetaClass(AppBroker.getInstance().getDomain(),
                    tableElement.getText(),
                    CC);
        }

        if (attributesElement != null) {
            attributes = new ArrayList<TemplateAttribute>();

            final List<Element> attributeList = attributesElement.getChildren("FeatureServiceAttribute");

            for (final Element attr : attributeList) {
                final TemplateAttribute attribute = new TemplateAttribute(attr);

                attributes.add(attribute);
            }
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final WaitingDialogThread<TemplateDataContainer> wdt = new WaitingDialogThread<TemplateDataContainer>(
                StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                true,
                "Empfange Daten der Schablone",
                null,
                100,
                true) {

                @Override
                protected TemplateDataContainer doInBackground() throws Exception {
                    final List<String[]> aliasAttributeList = new ArrayList<String[]>();

                    if (refresh != null) {
                        wd.setText("Aktualisiere Template");
                        final ServerActionParameter paramTemplate = new ServerActionParameter(
                                RefreshTemplateAction.ParameterType.TEMPLATE.toString(),
                                refresh);
                        final ServerActionParameter paramWait = new ServerActionParameter(
                                RefreshTemplateAction.ParameterType.WAIT.toString(),
                                "true");
                        SessionManager.getProxy()
                                .executeTask(
                                    RefreshTemplateAction.TASK_NAME,
                                    AppBroker.getInstance().getDomain(),
                                    (Object)null,
                                    ConnectionContext.createDummy(),
                                    paramTemplate,
                                    paramWait);
                        wd.setText("Erstelle Shape-Datei");
                    }

                    final CidsLayer service = new CidsLayer(metaClass);
                    service.initAndWait();

                    final List<FeatureServiceFeature> features = service.getFeatureFactory()
                                .createFeatures(service.getQuery(), null, null);
                    final List<String> ignoredAttributes = new ArrayList<String>();

                    if (attributes != null) {
                        for (final String attrName : service.getFeatureServiceAttributes().keySet()) {
                            final TemplateAttribute attr = getAttributeFromList(attributes, attrName);

                            if (attr != null) {
                                aliasAttributeList.add(new String[] { attr.getName(), attr.getAlias() });

                                if (attr.hasFillValue()) {
                                    for (final FeatureServiceFeature f : features) {
                                        f.setProperty(attrName, attr.getFillValue());
                                    }
                                }
                            } else {
                                ignoredAttributes.add(attrName);
                            }
                        }
                    }

                    // remove attributes
                    for (final String ignoredAttribute : ignoredAttributes) {
                        service.getFeatureServiceAttributes().remove(ignoredAttribute);
                        service.getOrderedFeatureServiceAttributes().remove(ignoredAttribute);
                    }

                    final String crsString = CrsTransformer.createCrsFromSrid(templateCrs);

                    for (final FeatureServiceFeature f : features) {
                        f.setGeometry(CrsTransformer.transformToGivenCrs(f.getGeometry(), crsString));

                        if (!f.getGeometry().isValid()) {
                            final Geometry validGeometry = GeometryUtils.makeValid(f.getGeometry());
                            f.setGeometry(validGeometry);
                        }

                        for (final String ignoredAttribute : ignoredAttributes) {
                            f.getProperties().remove(ignoredAttribute);
                        }
                    }

                    return new TemplateDataContainer(aliasAttributeList, service, features);
                }

                @Override
                protected void done() {
                    try {
                        final TemplateDataContainer data = get();

                        final List<String[]> aliasAttributeList = data.getAliasAttributeList();
                        final CidsLayer service = data.getService();
                        final List<FeatureServiceFeature> features = data.getFeatures();

                        // show warning
                        final MessageDialog d = new MessageDialog(AppBroker.getInstance().getWatergisApp(),
                                true,
                                NbBundle.getMessage(
                                    TemplateExportAction.class,
                                    "TemplateExportAction.actionPerformed.crs.message",
                                    new Object[] { templateCrs }),
                                NbBundle.getMessage(
                                    TemplateExportAction.class,
                                    "TemplateExportAction.actionPerformed.crs.title"));
                        d.setSize(500, 80);
                        StaticSwingTools.showDialog(d);

                        // choose file name and create shape file
                        final File outputFile = StaticSwingTools.chooseFileWithMultipleFilters(
                                "",
                                true,
                                new String[] { "shp" },
                                new String[] { "shp" },
                                AppBroker.getInstance().getRootWindow());

                        if (outputFile != null) {
                            final ExportShapeDownload shapeDownload = new ExportShapeDownload();

                            aliasAttributeList.add(new String[] { "$charset$", "windows-1252" });
                            aliasAttributeList.add(new String[] { "$charset_alias$", "ANSI 1252" });

                            shapeDownload.init(outputFile.getAbsolutePath(),
                                "",
                                features.toArray(new FeatureServiceFeature[features.size()]),
                                service,
                                aliasAttributeList,
                                null);
                            DownloadManager.instance().add(shapeDownload);
                        }
                    } catch (Exception e) {
                        LOG.error("Error while receiving template features", e);
                    }
                }
            };

        wdt.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributes  DOCUMENT ME!
     * @param   name        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private TemplateAttribute getAttributeFromList(final List<TemplateAttribute> attributes, final String name) {
        for (final TemplateAttribute tmp : attributes) {
            if (tmp.getName().equalsIgnoreCase(name)) {
                return tmp;
            }
        }

        return null;
    }

    @Override
    public int compareTo(final TemplateExportAction o) {
        return ((Integer)position).compareTo(o.position);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class TemplateAttribute {

        //~ Instance fields ----------------------------------------------------

        private final String name;
        private String alias;
        private final String fill;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TemplateAttribute object.
         *
         * @param  config  DOCUMENT ME!
         */
        public TemplateAttribute(final Element config) {
            name = config.getAttributeValue("name");
            alias = config.getAttributeValue("alias");

            if ((alias == null) || alias.equals("")) {
                alias = name;
            }

            fill = config.getAttributeValue("fill");
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getName() {
            return name;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getAlias() {
            return alias;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Object getFillValue() {
            if (fill != null) {
                if (fill.equals("$username")) {
                    return SessionManager.getSession().getUser().getName();
                } else if (fill.equals("$date")) {
                    final GregorianCalendar date = new GregorianCalendar();

                    return date.get(GregorianCalendar.YEAR) + to2Digits((date.get(GregorianCalendar.MONTH) + 1))
                                + to2Digits(date.get(GregorianCalendar.DATE));
                } else {
                    return fill;
                }
            }

            return fill;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   value  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String to2Digits(final int value) {
            if (value > 9) {
                return "" + value;
            } else {
                return "0" + value;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean hasFillValue() {
            return fill != null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class TemplateDataContainer {

        //~ Instance fields ----------------------------------------------------

        private List<String[]> aliasAttributeList;
        private CidsLayer service;
        private List<FeatureServiceFeature> features;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TemplateDataContainer object.
         *
         * @param  aliasAttributeList  DOCUMENT ME!
         * @param  service             DOCUMENT ME!
         * @param  features            DOCUMENT ME!
         */
        public TemplateDataContainer(final List<String[]> aliasAttributeList,
                final CidsLayer service,
                final List<FeatureServiceFeature> features) {
            this.aliasAttributeList = aliasAttributeList;
            this.service = service;
            this.features = features;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the aliasAttributeList
         */
        public List<String[]> getAliasAttributeList() {
            return aliasAttributeList;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  aliasAttributeList  the aliasAttributeList to set
         */
        public void setAliasAttributeList(final List<String[]> aliasAttributeList) {
            this.aliasAttributeList = aliasAttributeList;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the service
         */
        public CidsLayer getService() {
            return service;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  service  the service to set
         */
        public void setService(final CidsLayer service) {
            this.service = service;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the features
         */
        public List<FeatureServiceFeature> getFeatures() {
            return features;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  features  the features to set
         */
        public void setFeatures(final List<FeatureServiceFeature> features) {
            this.features = features;
        }
    }
}
