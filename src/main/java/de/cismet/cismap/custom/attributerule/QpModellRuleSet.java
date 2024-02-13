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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpModellRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(QpModellRuleSet.class);
    private static final String[] ALLOWED_PRIO_VALUES = { "ü", "sehr hoch", "hoch", "moderat" };
    private static final String[] ALLOWED_M_DIM_VALUES = { "1D", "2D", "3D", "1D/2D", "1D/3D", "2D/3D", "1D/2D/3D" };
    private static final String[] ALLOWED_BOOL_VALUES = { "-", "x" };
    private static final String[] ALLOWED_M_TIME_VALUES = { "instationär", "stationär" };

    //~ Instance initializers --------------------------------------------------

    {
        minLaLength = 10.0;
        maxLaLength = 999999.0;
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("la_cd", new Numeric(15, 0, true, false));
        typeMap.put("la_st_von", new Numeric(10, 2, false, false));
        typeMap.put("la_st_bis", new Numeric(10, 2, false, false));
        typeMap.put("la_cd_k", new Numeric(15, 0, false, false));
        typeMap.put("la_gn", new Varchar(75, true, false));
        typeMap.put("m_traeger", new Catalogue("k_m_traeger", false, true, new Varchar(2, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, true));
        typeMap.put("abschnitt", new Varchar(100, true, false));
        typeMap.put("jahr", new Varchar(20, false, false));
        typeMap.put("m_plan", new Varchar(250, false, false));
        typeMap.put("m_ergeb", new Varchar(250, false, false));
        typeMap.put("m_kosten", new Numeric(6, 2, false, false, 1, 9999));
        typeMap.put("suchraum", new Varchar(250, false, true));
        typeMap.put("prio", new Catalogue("k_prio", false, true, new Varchar(10, false, false)));
        typeMap.put("m_obsolet", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("m_software", new Varchar(20, false, true));
        typeMap.put("m_exist", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("m_dim", new Catalogue("k_m_dim", false, true, new Varchar(20, false, false)));
        typeMap.put("m_time", new Catalogue("k_m_time", false, true, new Varchar(20, false, false)));
        typeMap.put("flood_area", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("m_hw_hq10", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("m_hw_hq100", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("m_hw_hq200", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("m_mnq", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("m_mq", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("m_q330", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("m_mhq", new Catalogue("k_geschehen", false, true, new Varchar(1, false, false)));
        typeMap.put("ba_gn", new Varchar(100, false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("geom") && !columnName.equals("id")
                    && !columnName.equals("la_cd") && !columnName.equals("la_cd_k") && !columnName.equals("la_gn")
                    && !columnName.equals("obj_nr") && !columnName.equals("laenge") && !columnName.equals("ww_gr");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("m_traeger")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        "m_traeger",
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("traeger");
                    }
                });

            return editor;
        } else if (columnName.equals("prio")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        "prio",
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("prio");
                    }
                });

            return editor;
        } else if (columnName.equals("m_dim")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        "m_dim",
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("dim");
                    }
                });

            return editor;
        } else if (columnName.equals("m_time")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        "m_time",
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("time");
                    }
                });

            return editor;
        } else if (columnName.equals("m_obsolet") || columnName.equals("flood_area") || columnName.equals("m_hw_hq10")
                    || columnName.equals("m_hw_hq100") || columnName.equals("m_hw_hq200")
                    || columnName.equals("m_mnq") || columnName.equals("m_mq") || columnName.equals("m_q330")
                    || columnName.equals("m_mhq") || columnName.equals("m_exist")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("geschehen");
                    }
                });

            return editor;
        } else if (columnName.equals("la_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("la_st_bis")) {
            return new StationTableCellEditor(columnName);
        } else {
            return null;
        }
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[] { "laenge" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("laenge")) {
            return -3;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Double value = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            value = round(geom.getLength());
        }

        return value;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("laenge")) {
            return "round(st_length(geom)::numeric, 2)";
        } else {
            return null;
        }
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(
                CreateGeometryListenerInterface.LINESTRING,
                true);

        return c;
//        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_la");
//        final OnOwnRouteStationCheck check = new OnOwnRouteStationCheck();
//
//        final StationLineCreator creator = new StationLineCreator(
//                "la_st",
//                routeMc,
//                "LAWA-Gewässer (LAWA)",
//                new LinearReferencingWatergisHelper());
//
//        return creator;
    }
}
