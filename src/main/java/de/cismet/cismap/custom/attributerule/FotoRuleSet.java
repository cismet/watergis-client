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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.net.URL;

import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.DateCellEditor;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

import de.cismet.cismap.linearreferencing.RouteTableCellEditor;
import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.Proxy;
import de.cismet.netutil.ProxyHandler;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.downloadmanager.WebDavDownload;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.foto.ReportAction;
import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
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
    public static final BufferedImage ARROW_NULL;
    public static final BufferedImage SELECTED_ARROW_NULL;

    static {
        try {
            final URL arrowUrl = QpRuleSet.class.getResource(
                    "/de/cismet/watergis/res/icons16/angle.png");
            ARROW = ImageIO.read(arrowUrl);
            final URL arrowSelectedUrl = FotoRuleSet.class.getResource(
                    "/de/cismet/watergis/res/icons16/angleSelected.png");
            SELECTED_ARROW = ImageIO.read(arrowSelectedUrl);
            final URL arrowNullUrl = FotoRuleSet.class.getResource(
                    "/de/cismet/watergis/res/icons16/angle_null.png");
            ARROW_NULL = ImageIO.read(arrowNullUrl);
            final URL arrowNullSelectedUrl = FotoRuleSet.class.getResource(
                    "/de/cismet/watergis/res/icons16/angle_null_selected.png");
            SELECTED_ARROW_NULL = ImageIO.read(arrowNullSelectedUrl);
        } catch (Exception ex) {
            LOG.fatal(ex, ex);
            throw new RuntimeException(ex);
        }
    }

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, false));
        typeMap.put("la_cd", new Numeric(15, 0, true, false));
        typeMap.put("la_st", new Numeric(10, 2, false, false));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("l_rl", new Catalogue("k_l_rl", false, true, new Varchar(2, false, false)));
        typeMap.put("re", new Numeric(11, 2, false, true));
        typeMap.put("ho", new Numeric(10, 2, false, true));
        typeMap.put("winkel", new Numeric(5, 1, false, true));
        typeMap.put("foto_nr", new Numeric(15, 0, false, false));
        typeMap.put("foto_nr_gu", new Varchar(50, false, false));
        typeMap.put("foto", new Varchar(250, false, false));
        typeMap.put("upl_name", new Varchar(50, false, false));
        typeMap.put("upl_datum", new Varchar(10, false, false));
        typeMap.put("upl_zeit", new Varchar(8, false, false));
        typeMap.put("aufn_name", new Varchar(50, false, true));
        typeMap.put("aufn_datum", new DateType(false, true));
        typeMap.put("aufn_zeit", new Time(false, true));
        typeMap.put("freigabe", new Catalogue("k_freigabe", true, true, new Varchar(10, false, false)));
        typeMap.put("titel", new Varchar(250, false, true));
        typeMap.put("beschreib", new Varchar(250, false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        if (columnName.equals("ww_gr")) {
            return AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren");
        } else {
            return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                        && !columnName.equals("geom") && !columnName.equals("id")
                        && !columnName.equals("foto")
                        && !columnName.equals("foto_nr") && !columnName.equals("la_st") && !columnName.equals("la_cd")
                        && !columnName.startsWith("upl");
        }
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (column.equals("ba_cd")) {
            final Object o = (Number)feature.getProperty("ba_st");
            Double baSt;

            if (o instanceof CidsBean) {
                baSt = (Double)((CidsBean)o).getProperty("wert");
            } else if (o == null) {
                baSt = null;
            } else {
                baSt = ((Number)feature.getProperty("ba_st")).doubleValue();
            }

            refreshLaStation(
                feature,
                (String)newValue,
                baSt,
                "la_cd",
                "la_st");
        }

        if (column.equals("ba_st")) {
            refreshLaStation(
                feature,
                (String)feature.getProperty("ba_cd"),
                ((Number)newValue).doubleValue(),
                "la_cd",
                "la_st");
        }

        if (column.equals("re") && !checkRange(column, newValue, 33000000, 33999999.99, false, true, true)) {
            return oldValue;
        }

        if (column.equals("ho") && !checkRange(column, newValue, 5600000, 6399999.99, false, true, true)) {
            return oldValue;
        }

        if (column.equals("re") && (newValue instanceof Double)) {
            final Geometry g = (Geometry)feature.getGeometry();
            final Geometry newPoint = g.getFactory().createPoint(new Coordinate((Double)newValue, g.getCoordinate().y));
            feature.setGeometry(newPoint);
        }

        if (column.equals("ho") && (newValue instanceof Double)) {
            final Geometry g = (Geometry)feature.getGeometry();
            final Geometry newPoint = g.getFactory().createPoint(new Coordinate(g.getCoordinate().x, (Double)newValue));
            feature.setGeometry(newPoint);
        }

        if (column.equals("winkel") && !checkRange(column, newValue, 0, 360, true, true, false)) {
            return oldValue;
        }

        if (column.equals("aufn_datum")) {
            if ((newValue != null) && (newValue instanceof Date)) {
                final Date d = (Date)newValue;

                // d.year = year - 1900
                if ((d.getYear() < 0) || d.after(new Date())) {
                    showMessage("Es sind nur Datumseingaben zwischen dem 01.01.1900 und heute erlaubt", "aufn_datum");
                    return oldValue;
                }

                if ((d.getYear() >= 0) && (d.getYear() < 50)) {
                    if (
                        !showSecurityQuestion(
                                    "Wert außerhalb Standardbereich (01.01.1950 .. heute) --> verwenden ?",
                                    column,
                                    newValue)) {
                        return oldValue;
                    }
                }
            }
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("foto")) {
            return new LinkTableCellRenderer();
        } else if (columnName.equals("foto_nr")) {
            return new LinkTableCellRenderer(JLabel.RIGHT);
        }

        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ba_cd")) {
            final RouteTableCellEditor editor = new RouteTableCellEditor("dlm25w.fg_ba", "ba_st", false);
            final String filterString = getRouteFilter();

            if (filterString != null) {
                editor.setRouteQuery(filterString);
            }

            return editor;
        } else if (columnName.equals("ww_gr")) {
            CidsLayerFeatureFilter filter = null;

            if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren")) {
                final String userName = AppBroker.getInstance().getOwner();
                filter = new CidsLayerFeatureFilter() {

                        @Override
                        public boolean accept(final CidsLayerFeature bean) {
                            if (bean == null) {
                                return false;
                            }
                            return bean.getProperty("owner").equals(userName);
                        }
                    };
            } else {
                filter = new WwGrAdminFilter();
            }
            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true),
                    filter);
        } else if (columnName.equals("l_st")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("nicht_qp");
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
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
                    createCidsLayerFeatureFilter("foto"));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
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

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("freigabe") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("ba_st")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("aufn_datum")) {
            return new DateCellEditor();
        } else {
            return super.getCellEditor(columnName);
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        return prepareForSaveWithDetails(features) == null;
    }

    @Override
    public ErrorDetails prepareForSaveWithDetails(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();
            if (!checkRange("winkel", feature.getProperty("winkel"), 0, 360, true, true, false)) {
                return new ErrorDetails(feature, "winkel");
            }
        }

        return super.prepareForSaveWithDetails(features);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
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
            return 9;
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
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("re")) {
            return "st_x(geom)";
        } else if (propertyName.equals("ho")) {
            return "st_y(geom)";
        }

        return null;
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
        if ((AppBroker.getInstance().getOwnWwGr() != null)) {
            properties.put("ww_gr", AppBroker.getInstance().getOwnWwGr());
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

                    final WebDavClient webDavClient = new WebDavClient(ProxyHandler.getInstance().getProxy(),
                            Photo.WEB_DAV_USER,
                            Photo.WEB_DAV_PASSWORD,
                            true);

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
        } else if (columnName.equals("foto_nr")) {
            if ((value != null) && (clickCount == 1)) {
                createFotoReport();
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void createFotoReport() {
        final ReportAction action = new ReportAction();
        action.actionPerformed(null);
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
                symb = new FeatureAnnotationSymbol(SELECTED_ARROW_NULL);
            } else {
                symb = new FeatureAnnotationSymbol(ARROW_NULL);
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
