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

import org.apache.log4j.Logger;

import org.jdom.Element;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.tools.ExportShapeDownload;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.reports.GerinneGFlReportAction;

import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;

/**
 * Exports a template
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TemplateExportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneGFlReportAction.class);
    protected static final ConnectionContext CC = ConnectionContext.create(
            AbstractConnectionContext.Category.ACTION,
            "TemplateExportAction");

    //~ Instance fields --------------------------------------------------------

    List<TemplateAttribute> attributes;
    private MetaClass metaClass;
    private int templateCrs = 5650;

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

        if ((nameElement != null) && (nameElement.getText() != null)) {
            putValue(NAME, nameElement.getText());
        }

        if ((tooltipElement != null) && (tooltipElement.getText() != null)) {
            putValue(SHORT_DESCRIPTION, tooltipElement.getText());
        }

        if ((iconElement != null) && (iconElement.getText() != null)) {
            final URL icon = getClass().getResource(tooltipElement.getText());

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
        try {
            final CidsLayer service = new CidsLayer(metaClass);
            service.initAndWait();

            final List<FeatureServiceFeature> features = service.getFeatureFactory()
                        .createFeatures(service.getQuery(), null, null);
            final List<String> ignoredAttributes = new ArrayList<String>();

            if (attributes != null) {
                for (final String attrName : service.getFeatureServiceAttributes().keySet()) {
                    final TemplateAttribute attr = getAttributeFromList(attributes, attrName);

                    if (attr != null) {
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

                for (final String ignoredAttribute : ignoredAttributes) {
                    f.getProperties().remove(ignoredAttribute);
                }
            }

            final File outputFile = StaticSwingTools.chooseFileWithMultipleFilters(
                    "",
                    true,
                    new String[] { "shp" },
                    new String[] { "shp" },
                    AppBroker.getInstance().getRootWindow());

            if (outputFile != null) {
                final ExportShapeDownload shapeDownload = new ExportShapeDownload(outputFile.getAbsolutePath(),
                        "",
                        features.toArray(new FeatureServiceFeature[features.size()]));

                DownloadManager.instance().add(shapeDownload);
            }
        } catch (Exception ex) {
            LOG.error("Error while receiving template features", ex);
        }
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class TemplateAttribute {

        //~ Instance fields ----------------------------------------------------

        private final String name;
        private final String alias;
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
                    return new Date();
                } else {
                    return fill;
                }
            }

            return fill;
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
}
