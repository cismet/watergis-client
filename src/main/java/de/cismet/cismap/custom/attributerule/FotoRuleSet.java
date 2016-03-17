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
package de.cismet.cismap.custom.attributerule;

import Sirius.navigator.connection.SessionManager;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.net.URL;

import java.sql.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.CidsBeanFilter;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.DateCellEditor;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.Proxy;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.downloadmanager.WebDavDownload;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.utils.AbstractBeanListCellRenderer;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FotoRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(FotoRuleSet.class);
    public static final BufferedImage ARROW;
    public static final BufferedImage SELECTED_ARROW;

    static {
        try {
            final URL arrowUrl = QpRuleSet.class.getResource(
                    "/de/cismet/watergis/res/icons16/angle.png");
            ARROW = ImageIO.read(arrowUrl);
            final URL arrowSelectedUrl = QpRuleSet.class.getResource(
                    "/de/cismet/watergis/res/icons16/angleSelected.png");
            SELECTED_ARROW = ImageIO.read(arrowSelectedUrl);
//            ARROW_NULL = new FeatureAnnotationSymbol(new ImageIcon(
//                        "/de/cismet/cids/custom/objecteditors/wrrl_db_mv/angle_null.png").getImage());
        } catch (Exception ex) {
            LOG.fatal(ex, ex);
            throw new RuntimeException(ex);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("geom") && !columnName.equals("ba_cd") && !columnName.equals("id")
                    && !columnName.equals("ho") && !columnName.equals("re") && !columnName.equals("foto")
                    && !columnName.equals("foto_nr") && !columnName.equals("la_st") && !columnName.equals("la_cd")
                    && !columnName.equals("ww_gr") && !columnName.startsWith("upl");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
//        if (newValue == null) {
//            if (column.equals("foto") || column.equals("title")) {
//                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                    "Das Attribut "
//                            + column
//                            + " darf nicht null sein");
//                return oldValue;
//            }
//        }

//        if (column.equals("ausbaujahr")
//                    && !checkRange(column, newValue, 1800, getCurrentYear() + 2, true, true, true)) {
//            return oldValue;
//        }

        if (column.equals("winkel") && !checkRange(column, newValue, 0, 360, true, true, false)) {
            return oldValue;
        }

        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("foto")) {
            return new LinkTableCellRenderer();
        }
        return null;
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ww_gr")) {
            CidsBeanFilter filter = null;

            if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren")) {
                final String userName = AppBroker.getInstance().getOwner();
                filter = new CidsBeanFilter() {

                        @Override
                        public boolean accept(final CidsBean bean) {
                            if (bean == null) {
                                return false;
                            }
                            return bean.getProperty("owner").equals(userName);
                        }
                    };
            } else {
                filter = new CidsBeanFilter() {

                        @Override
                        public boolean accept(final CidsBean bean) {
                            return bean != null;
                        }
                    };
            }
            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true),
                    filter);
        } else if (columnName.equals("l_st")) {
            final CidsBeanFilter filter = createCidsBeanFilter("nicht_qp");
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);
            editor.setNullable(true);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("l_st") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("l_rl")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    createCidsBeanFilter("foto"));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("l_rl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("freigabe")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractBeanListCellRenderer() {

                    @Override
                    protected String toString(final CidsBean bean) {
                        return bean.getProperty("freigabe") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("ba_st")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("aufn_datum")) {
            return new DateCellEditor();
        } else if (columnName.equals("aufn_zeit")) {
            return new DateCellEditor();
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
        for (final FeatureServiceFeature feature : features) {
            if (!checkRange("winkel", feature.getProperty("winkel"), 0, 360, true, true, false)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[] { "ho", "re" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("re")) {
            return 8;
        } else if (name.equals("ho")) {
            return 9;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Double value = null;

        final Geometry geom = feature.getGeometry();

        if (geom instanceof Point) {
            if (propertyName.equals("re")) {
                value = ((Point)geom).getX();
            } else if (propertyName.equals("ho")) {
                value = ((Point)geom).getY();
            }
        }

        return value;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return null;
    }

    @Override
    public Map<String, Object> getDefaultValues() {
        final Map properties = new HashMap();
        if ((AppBroker.getInstance().getOwnWwGrList() != null) && !AppBroker.getInstance().getOwnWwGrList().isEmpty()) {
            properties.put("ww_gr", AppBroker.getInstance().getOwnWwGrList().get(0));
        } else {
            properties.put("ww_gr", AppBroker.getInstance().getNiemandWwGr());
        }

        return properties;
    }

    @Override
    public boolean isCatThree() {
        return true;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("foto")) {
            if ((value instanceof String) && (clickCount == 1) && (feature instanceof CidsLayerFeature)) {
                if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
                    final String jobname = DownloadManagerDialog.getJobname();
                    final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                    // the attribute dateipfad is not visible. So it can also be accessed via the bean.
                    final String path = Photo.WEB_DAV_DIRECTORY
                                + String.valueOf(cidsFeature.getBean().getProperty("dateipfad"));
                    final String file = value.toString();
                    String filename;
                    String extension = null;

                    if (file.contains(".")) {
                        extension = file.substring(file.lastIndexOf("."));
                        filename = file.substring(0, file.lastIndexOf("."));
                    } else {
                        filename = file;
                    }

                    final WebDavClient webDavClient = new WebDavClient(Proxy.fromPreferences(),
                            Photo.WEB_DAV_USER,
                            Photo.WEB_DAV_PASSWORD);

                    DownloadManager.instance()
                            .add(new WebDavDownload(
                                    webDavClient,
                                    path
                                    + WebDavHelper.encodeURL(file),
                                    jobname,
                                    filename
                                    + extension,
                                    filename,
                                    extension));
                }
            }
        }
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol(final FeatureServiceFeature feature) {
        final Object angle = feature.getProperty("winkel");
        FeatureAnnotationSymbol symb;

        if (angle instanceof Double) {
            final double winkel = (Double)angle;
            final BufferedImage rotatedArrow;
            if ((Photo.selectedFeature != null) && (Photo.selectedFeature.getId() == feature.getId())) {
                rotatedArrow = rotateImage(SELECTED_ARROW, winkel);
            } else {
                rotatedArrow = rotateImage(ARROW, winkel);
            }
            symb = new FeatureAnnotationSymbol(rotatedArrow);
        } else {
            if ((Photo.selectedFeature != null) && (Photo.selectedFeature.getId() == feature.getId())) {
                symb = new FeatureAnnotationSymbol(SELECTED_ARROW);
            } else {
                symb = new FeatureAnnotationSymbol(ARROW);
            }
        }

        symb.setSweetSpotX(0.5);
        symb.setSweetSpotY(0.5);

        return symb;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   src      DOCUMENT ME!
     * @param   degrees  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static BufferedImage rotateImage(final BufferedImage src, final double degrees) {
        final AffineTransform affineTransform = AffineTransform.getRotateInstance(
                Math.toRadians(degrees),
                src.getWidth()
                        / 2,
                src.getHeight()
                        / 2);
        final BufferedImage rotatedImage = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        final Graphics2D g = (Graphics2D)rotatedImage.getGraphics();
        g.setTransform(affineTransform);
        g.drawImage(src, 0, 0, null);
        return rotatedImage;
    }

    @Override
    public boolean hasCustomExportFeaturesMethod() {
        return true;
    }

    @Override
    public void exportFeatures() {
        AppBroker.getInstance().getPhotoExport().actionPerformed(null);
    }

    @Override
    public boolean hasCustomPrintFeaturesMethod() {
        return true;
    }

    @Override
    public void printFeatures() {
        AppBroker.getInstance().getPhotoPrint().actionPerformed(null);
    }
}
