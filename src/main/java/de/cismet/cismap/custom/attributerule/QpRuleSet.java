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

import java.awt.image.BufferedImage;

import java.net.URL;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.search.RemoveUnnusedRoute;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.DateCellEditor;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.SimpleAttributeTableModel;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.cismap.linearreferencing.RouteTableCellEditor;
import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.gaf.ReportAction;
import de.cismet.watergis.gui.panels.GafProf;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(QpRuleSet.class);
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

    //~ Instance initializers --------------------------------------------------

    {
        final DateType date = new DateType(false, true);

        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", true, true, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, false));
        typeMap.put("la_cd", new Numeric(20, 0, false, false));
        typeMap.put("la_st", new Numeric(10, 2, false, false));
        typeMap.put("l_st", new Catalogue("k_l_st", true, true, new Varchar(10, false, false)));
        typeMap.put("re", new Numeric(11, 2, true, false));
        typeMap.put("ho", new Numeric(10, 2, true, false));
        typeMap.put("qp_nr", new Numeric(20, 0, true, false));
        typeMap.put("upl_name", new Varchar(50, true, false));
        typeMap.put("upl_datum", new Varchar(10, true, false));
        typeMap.put("upl_zeit", new Varchar(8, true, false));
        typeMap.put("aufn_name", new Varchar(50, false, true));
        typeMap.put("aufn_datum", date);
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
            // Sonderfall ba_st und ba_st sollen nicht bearbeitbar sein
            return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                        && !columnName.equals("geom") && !columnName.equals("id")
                        && !columnName.equals("ho") && !columnName.equals("re") && !columnName.equals("qp_nr")
                        && !columnName.equals("la_st") && !columnName.equals("la_cd")
                        && !columnName.equals("upl_name") && !columnName.equals("upl_datum")
                        && !columnName.equals("upl_zeit") && !columnName.equals("ba_cd") && !columnName.equals("ba_st");
        }
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (column.equals("aufn_datum")) {
            if ((newValue != null) && (newValue instanceof Date)) {
                final Date d = (Date)newValue;

                // d.year = year - 1900
                if ((d.getYear() < 0) || d.after(new Date())) {
                    showMessage("Es sind nur Datumseingaben zwischen dem 01.01.1900 und heute erlaubt", column);
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
        if (columnName.equals("qp_nr")) {
            return new LinkTableCellRenderer(JLabel.RIGHT);
        } else {
            return super.getCellRenderer(columnName);
        }
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
        } else if (columnName.equals("ba_st")) {
            return new StationTableCellEditor(columnName);
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
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("qp");
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
        } else if (columnName.equals("freigabe")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("qp");
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
                        return bean.getProperty("freigabe") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("ba_st")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("aufn_datum")) {
            return new DateCellEditor();
        } else {
            return null;
        }
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
        if (model instanceof SimpleAttributeTableModel) {
            final List<FeatureServiceFeature> removedFeatures = ((SimpleAttributeTableModel)model).getRemovedFeature();

            if ((removedFeatures != null) && !removedFeatures.isEmpty()) {
                final List<Feature> selectedFeaturesToRemove = new ArrayList<Feature>();

                for (final FeatureServiceFeature feature : removedFeatures) {
                    final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures();

                    for (final Feature f : selectedFeatures) {
                        if (f instanceof CidsLayerFeature) {
                            final CidsLayerFeature clf = (CidsLayerFeature)f;

                            if ((clf.getProperty("qp_nr") != null) && (feature.getProperty("qp_nr") != null)) {
                                final Integer selectedFeatureBaCd = (Integer)(clf.getProperty("qp_nr"));
                                final Integer deletedFeatureBaCd = (Integer)(feature.getProperty("qp_nr"));

                                if (selectedFeatureBaCd.equals(deletedFeatureBaCd)) {
                                    selectedFeaturesToRemove.add(f);
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
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[] { "re", "ho" };
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
        if (columnName.equals("qp_nr")) {
            if ((value instanceof Integer) && (clickCount == 1) && (feature instanceof CidsLayerFeature)) {
//                if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
//                    final String jobname = DownloadManagerDialog.getJobname();
//                    final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
//                    final String filename = value.toString();
//                    final String extension = ".pdf";
//
//                    DownloadManager.instance().add(new QpDownload(
//                            filename,
//                            extension,
//                            jobname,
//                            null,
//                            cidsFeature));
//                }
                final ReportAction action = new ReportAction();
                action.actionPerformed(null);
            }
        }
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol(final FeatureServiceFeature feature) {
        final FeatureAnnotationSymbol symb;

        if ((GafProf.selectedFeature != null) && (GafProf.selectedFeature.getId() == feature.getId())) {
            symb = new FeatureAnnotationSymbol(SELECTED_ARROW);
        } else {
            symb = new FeatureAnnotationSymbol(ARROW);
        }

        symb.setSweetSpotX(0.5);
        symb.setSweetSpotY(0.5);

        return symb;
    }

    @Override
    public boolean hasCustomExportFeaturesMethod() {
        return true;
    }

    @Override
    public void exportFeatures() {
        AppBroker.getInstance().getGafExport().actionPerformed(null);
    }

    @Override
    public boolean hasCustomPrintFeaturesMethod() {
        return true;
    }

    @Override
    public void printFeatures() {
        AppBroker.getInstance().getGafPrint().actionPerformed(null);
    }
}
