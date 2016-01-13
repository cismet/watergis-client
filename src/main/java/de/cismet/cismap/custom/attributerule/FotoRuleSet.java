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

import Sirius.server.middleware.types.MetaClass;

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

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.tools.CidsBeanFilter;

import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.PointAndStationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.DateCellEditor;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractBeanListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;
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

    static {
        try {
            final URL arrowUrl = FotoRuleSet.class.getResource(
                    "/de/cismet/watergis/res/icons16/angle.png");
            ARROW = ImageIO.read(arrowUrl);
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
                    && !columnName.equals("geom") && !columnName.equals("ba_cd") && !columnName.equals("obj_nr")
                    && !columnName.equals("ho") && !columnName.equals("re");
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
//
//        if (column.equals("ausbaujahr")
//                    && !checkRange(column, newValue, 1800, getCurrentYear() + 2, true, true, true)) {
//            return oldValue;
//        }
//
//        if (column.equals("winkel") && !checkRange(column, newValue, 0, 360, true, true, false)) {
//            return oldValue;
//        }

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
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
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
        } else if (columnName.equals("ba_st")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("datum")) {
            return new DateCellEditor();
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
//        for (final FeatureServiceFeature feature : features) {
//            if (feature.getProperty("foto") == null) {
//                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                    "Das Attribut foto darf nicht leer sein");
//                return false;
//            }
//
//            if (feature.getProperty("titel") == null) {
//                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                    "Das Attribut titel darf nicht leer sein");
//                return false;
//            }
//
//            if (!checkRange("winkel", feature.getProperty("winkel"), 0, 360, true, true, false)) {
//                return false;
//            }
//
//            if (
//                !checkRange(
//                            "ausbaujahr",
//                            feature.getProperty("ausbaujahr"),
//                            1800,
//                            getCurrentYear()
//                            + 2,
//                            true,
//                            true,
//                            true)) {
//                return false;
//            }
//        }
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
    public void mouseClicked(final JTable table, final String columnName, final Object value, final int clickCount) {
        if (columnName.equals("foto")) {
            if ((value instanceof String) && (clickCount == 1)) {
                downloadDocumentFromWebDav(getPhotoPath(), value.toString());
            }
        }
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol(final FeatureServiceFeature feature) {
        final Object angle = feature.getProperty("winkel");
        if (angle instanceof Double) {
            final double winkel = (Double)angle;
            final BufferedImage rotatedArrow = rotateImage(ARROW, winkel);
            final FeatureAnnotationSymbol symb = new FeatureAnnotationSymbol(rotatedArrow);
            symb.setSweetSpotX(0.5);
            symb.setSweetSpotY(0.5);
            return symb;
        }
        return new FeatureAnnotationSymbol(ARROW);
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
}