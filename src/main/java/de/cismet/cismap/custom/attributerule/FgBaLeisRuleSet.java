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

import org.deegree.datatypes.Types;

import java.sql.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaLeisRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", true, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("l_rl", new Catalogue("k_l_rl", true, true, new Varchar(2, false, false)));
        typeMap.put("leis", new Catalogue("k_leis", true, true, new Varchar(10, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("obj_nr_gu", new Varchar(50, false, true));
        typeMap.put("esw", new BooleanAsInteger(false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
        minBaLength = 0.5;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("laenge") && !columnName.equals("ba_cd")
                    && !columnName.equals("geom") && !columnName.equals("obj_nr") && !columnName.equals("id")
                    && !columnName.equals("ww_gr");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ww_gr")) {
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
        } else if (columnName.equals("ba_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ba_st_bis")) {
            return new StationTableCellEditor(columnName);
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
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("leis");

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
                        return bean.getProperty("l_rl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("leis")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("leis") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
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
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");
        final OnOwnRouteStationCheck check = new OnOwnRouteStationCheck();

        final StationLineCreator creator = new StationLineCreator(
                "ba_st",
                routeMc,
                "Basisgewässer (FG)",
                new LinearReferencingWatergisHelper(),
                0.5f);
//        creator.setProperties(getDefaultValues());
        creator.setCheck(check);

        return creator;
    }

//    @Override
//    public Map<String, Object> getDefaultValues() {
//        final Map properties = new HashMap();
//        if ((AppBroker.getInstance().getOwnWwGr() != null)) {
//            properties.put("ww_gr", AppBroker.getInstance().getOwnWwGr());
//        } else {
//            properties.put("ww_gr", AppBroker.getInstance().getNiemandWwGr());
//        }
//
//        return properties;
//    }
}
