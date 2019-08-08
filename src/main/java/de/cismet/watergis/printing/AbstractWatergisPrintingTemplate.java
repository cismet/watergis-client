/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.printing;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.commons.utils.Pair;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.InputStream;

import java.net.URL;
import java.net.URLEncoder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.SLDStyledLayer;
import de.cismet.cismap.commons.gui.printing.AbstractPrintingInscriber;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleLegendProvider;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.commons.security.AccessHandler;

import de.cismet.security.WebAccessManager;

import de.cismet.tools.gui.Static2DTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.ConversionUtils;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractWatergisPrintingTemplate extends AbstractPrintingInscriber {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractWatergisPrintingTemplate.class);

    //~ Instance fields --------------------------------------------------------

    String prefix = null;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField txtZeile1;
    private javax.swing.JTextField txtZeile2;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form A4H.
     */
    public AbstractWatergisPrintingTemplate() {
        initComponents();

        try {
            final MetaClass GU_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_gu");
            final User user = SessionManager.getSession().getUser();
            final String query = "select " + GU_MC.getID() + ", " + GU_MC.getPrimaryKey()
                        + " from dlm25w.k_gu where code in ( select praefix from dlm25w.k_ww_gr where owner = '"
                        + user.getUserGroup().getName() + "')";
            final MetaObject[] mo = SessionManager.getProxy().getMetaObjectByQuery(user, query);

            if ((mo != null) && (mo.length == 1)) {
                final String stempel1 = (String)mo[0].getBean().getProperty("stempel_z1");
                final String stempel2 = (String)mo[0].getBean().getProperty("stempel_z2");

                if (stempel1 != null) {
                    txtZeile1.setText(stempel1);
                }
                if (stempel2 != null) {
                    txtZeile2.setText(stempel2);
                }

                prefix = (String)mo[0].getBean().getProperty("code");
            }
        } catch (Exception e) {
            LOG.error("Error while retrieving gu signature", e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This Method should return the values in the Form<br>
     * key: placeholderName value: value
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public HashMap<String, String> getValues() {
        final HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("Ueberschrift", txtZeile1.getText()); // NOI18N
        hm.put("Unterschrift", txtZeile2.getText()); // NOI18N
        final UserGroup ug = SessionManager.getSession().getUser().getUserGroup();
        String imageName = "lung";

        imageName = ug.getName().toLowerCase();

        if (imageName.contains("_") && (imageName.indexOf("_") != imageName.lastIndexOf("_"))) {
            imageName = imageName.substring(0, imageName.lastIndexOf("_"));
        }

        URL url = AbstractWatergisPrintingTemplate.class.getResource("/de/cismet/cismap/commons/gui/printing/"
                        + imageName + ".jpg");

        if (url == null) {
            url = AbstractWatergisPrintingTemplate.class.getResource("/" + imageName + ".jpg");

            if (url == null) {
                url = AbstractWatergisPrintingTemplate.class.getResource("/de/cismet/watergis/printing/mv.png");
            }
        }

        try {
            hm.put("logo", ConversionUtils.image2String(ImageIO.read(url))); // NOI18N
        } catch (Exception e) {
            LOG.error("Error while reading image data", e);
        }

        try {
            final BufferedImage legend = getLegend();

            if (legend != null) {
//                ImageIO.write(legend, "jpg", new File("/home/therter/legende.jpg"));
                hm.put("legend", ConversionUtils.image2String(rescaleImage(legend))); // NOI18N
            }
        } catch (Exception e) {
            LOG.error("Error while reading image data", e);
        }

        return hm;
    }

    /**
     * Create the legend for the layers.
     *
     * @return  DOCUMENT ME!
     */
    protected BufferedImage getLegend() {
        final MappingModel model = CismapBroker.getInstance().getMappingComponent().getMappingModel();
        final TreeMap<Integer, MapService> serviceMap = model.getRasterServices();
        BufferedImage legend = null;

        for (final Integer key : serviceMap.keySet()) {
            final MapService service = serviceMap.get(key);

            if ((service.getPNode() != null) && !service.getPNode().getVisible()) {
                // no legend should be created, if the layer is not visible
                continue;
            }

            if (service instanceof AbstractFeatureService) {
                final AbstractFeatureService afService = (AbstractFeatureService)service;
                XBoundingBox currentBox;

                if (afService.getBoundingBox() instanceof XBoundingBox) {
                    currentBox = (XBoundingBox)afService.getBoundingBox();
                } else {
                    final Geometry g = afService.getBoundingBox()
                                .getGeometry(CrsTransformer.extractSridFromCrs(
                                        CismapBroker.getInstance().getSrs().getCode()));
                    currentBox = new XBoundingBox(g);
                }

                if (!afService.isVisibleInBoundingBox(currentBox)) {
                    continue;
                }
            }

            try {
                final BufferedImage image = getServiceLegend(service);

                if (image != null) {
                    if (legend == null) {
                        legend = image;
                    } else {
                        legend = Static2DTools.appendImage(legend, image);
                    }
                }
            } catch (Exception e) {
                String name = "unknown";

                if (service instanceof AbstractFeatureService) {
                    name = ((AbstractFeatureService)service).getName();
                } else if (service instanceof WMSServiceLayer) {
                    name = ((WMSServiceLayer)service).getName();
                }

                LOG.error("Cannot create legend for layer" + name, e);
            }
        }

        return legend;
    }

    /**
     * Create the legend of the given service.
     *
     * @param   service  the legend of this service will be created
     *
     * @return  the legend of the given service
     */
    protected BufferedImage getServiceLegend(final MapService service) {
        BufferedImage legendImage = null;
        String name = "";

        if (service instanceof WMSServiceLayer) {
            legendImage = getWMSLegendImage((WMSServiceLayer)service);
            name = ((WMSServiceLayer)service).getName();
        } else if (service instanceof SimpleLegendProvider) {
            final SimpleLegendProvider slp = (SimpleLegendProvider)service;
            legendImage = getImageFromUrl(slp.getLegendUrl());
        } else if (service instanceof SlidableWMSServiceLayerGroup) {
            final SlidableWMSServiceLayerGroup wmsLayer = (SlidableWMSServiceLayerGroup)service;
            final List v = wmsLayer.getLayers();
            final Iterator it = v.iterator();
            if (it.hasNext()) {
                final Object elem = it.next();
                if (elem instanceof WMSServiceLayer) {
                    legendImage = getWMSLegendImage((WMSServiceLayer)elem);
                    name = ((WMSServiceLayer)elem).getTitle();
                }
            }
        } else if (service instanceof SLDStyledLayer) {
            final SLDStyledLayer sldLayer = (SLDStyledLayer)service;
            final Pair<Integer, Integer> size = sldLayer.getLegendSize();
            legendImage = new BufferedImage(size.first, size.second, BufferedImage.TYPE_4BYTE_ABGR);
            sldLayer.getLegend(legendImage.getWidth(), legendImage.getHeight(), legendImage.createGraphics());
        }

        if (service instanceof AbstractFeatureService) {
            name = ((AbstractFeatureService)service).getName();
        }

        if (legendImage != null) {
            legendImage = addLegendTitle(legendImage, name);
        }

        return legendImage;
    }

    /**
     * Adds the given title to the legend image.
     *
     * @param   legend  the legend, the title should be added to
     * @param   title   the title to add
     *
     * @return  The legend image with the title
     */
    protected BufferedImage addLegendTitle(final BufferedImage legend, final String title) {
        final Font basicFont = new Font("Arial", Font.BOLD, 12);
        final FontMetrics fmetrics = legend.getGraphics().getFontMetrics(basicFont);
        final int maxWidth = Math.max(legend.getWidth(null),
                2
                        + (int)fmetrics.getStringBounds(title, legend.getGraphics()).getWidth());
        final int maxHeight = legend.getHeight(null) + 16;
        final BufferedImage image = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(Color.BLACK);
        graphics2D.setFont(basicFont);
        graphics2D.drawString(title, 0, 12);
        graphics2D.drawImage(legend, 0, 16, null);
        graphics2D.dispose();

        return image;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   legend  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected BufferedImage rescaleImage(final BufferedImage legend) {
        final BufferedImage image = new BufferedImage((int)(getLegendWidth() * 1.3),
                (int)(getLegendHeight() * 1.3),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics2D = image.createGraphics();
        graphics2D.drawImage(legend.getScaledInstance(
                (int)(legend.getWidth() / 1.0),
                (int)(legend.getHeight() / 1.0),
                BufferedImage.SCALE_SMOOTH),
            0,
            0,
            null);
        graphics2D.dispose();

        return image;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract int getLegendWidth();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract int getLegendHeight();

    /**
     * DOCUMENT ME!
     *
     * @param   wmsLayer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected BufferedImage getWMSLegendImage(final WMSServiceLayer wmsLayer) {
        BufferedImage legendImage = null;

        if (!wmsLayer.isDummy()) {
            final List v = wmsLayer.getWMSLayers();
            final Iterator it = v.iterator();
            while (it.hasNext()) {
                final Object elem = it.next();

                if (elem instanceof WMSLayer) {
                    final WMSLayer wl = (WMSLayer)elem;
                    String url = null;

                    try {
                        final URL[] lua = wl.getSelectedStyle().getLegendURL();
                        url = getValidUrlString(lua[0]);

                        if (url != null) {
                            legendImage = getImageFromUrl(url);
                        }
                    } catch (final Exception t) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Could not find legend for " + wl.getOgcCapabilitiesLayer().getTitle(), t); // NOI18N
                        }
                    }
                }
            }
        }

        return legendImage;
    }

    /**
     * The legend url can contains umlaute and colons, which cannot be handled by the ImageRetrieval class. So this
     * characters should be encoded
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private String getValidUrlString(final URL url) throws Exception {
        String urlString = null;

        try {
            urlString = url.toURI().toASCIIString();
        } catch (final Exception t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot convert legend url to ascii string", t); // NOI18N
            }
            urlString = url.toString();

            if (urlString.contains("?")) {
                final String param = urlString.substring(urlString.indexOf("?") + 1);
                urlString = urlString.substring(0, urlString.indexOf("?")) + "?";
                final StringTokenizer stParam = new StringTokenizer(param, "&");

                while (stParam.hasMoreTokens()) {
                    final StringTokenizer stKeyVal = new StringTokenizer(stParam.nextToken(), "=");

                    if (stKeyVal.countTokens() == 2) {
                        urlString += "&" + stKeyVal.nextToken() + "="
                                    + URLEncoder.encode(stKeyVal.nextToken(), "UTF-8");
                    }
                }
            }
        }

        return urlString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected BufferedImage getImageFromUrl(final String url) {
        BufferedImage legendImage = null;

        if (url != null) {
            int indexOfCharacter = 0;
            String urlBase;
            String requestParameter;

            if ((indexOfCharacter = url.indexOf('?')) != -1) {
                urlBase = url.substring(0, indexOfCharacter);

                if ((indexOfCharacter + 1) < url.length()) {
                    requestParameter = url.substring(indexOfCharacter + 1, url.length());
                } else {
                    requestParameter = ""; // NOI18N
                }
            } else {
                urlBase = url;
                requestParameter = "";     // NOI18N
            }

            try {
                final InputStream is = WebAccessManager.getInstance()
                            .doRequest(
                                new URL(urlBase),
                                requestParameter,
                                AccessHandler.ACCESS_METHODS.GET_REQUEST);
                legendImage = ImageIO.read(is);
            } catch (Exception e) {
                LOG.error("Error while retrieving legend", e);
            }
        }

        return legendImage;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        txtZeile1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtZeile2 = new javax.swing.JTextField();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(
                AbstractWatergisPrintingTemplate.class,
                "AbstractWatergisPrintingTemplate.jLabel1.text")); // NOI18N

        txtZeile1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtZeile1ActionPerformed(evt);
                }
            });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(
                AbstractWatergisPrintingTemplate.class,
                "AbstractWatergisPrintingTemplate.jLabel2.text")); // NOI18N

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        layout.createSequentialGroup().add(jLabel1).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            txtZeile1,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            101,
                            Short.MAX_VALUE)).add(
                        layout.createSequentialGroup().add(jLabel2).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            txtZeile2,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            101,
                            Short.MAX_VALUE))).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().addContainerGap().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel1).add(
                        txtZeile1,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel2).add(
                        txtZeile2,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap(
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)));
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtZeile1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtZeile1ActionPerformed
    }                                                                             //GEN-LAST:event_txtZeile1ActionPerformed
}
