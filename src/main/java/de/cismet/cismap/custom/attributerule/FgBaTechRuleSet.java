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

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.isValueEmpty;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaTechRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", true, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("tech", new Catalogue("k_tech", true, true, new Varchar(10, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("obj_nr_gu", new Varchar(50, false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("na_gu", new Catalogue("k_na_gu", false, true, new Numeric(4, 0, false, false)));
        typeMap.put("mahd_gu", new Catalogue("k_mahd_gu", false, true, new Varchar(10, false, false)));
        typeMap.put("gu_gu", new Catalogue("k_tech", false, true, new Varchar(10, false, false)));
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
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (isValueEmpty(newValue)) {
            if (column.equals("tech")) {
                if (isValueEmpty(feature.getProperty("na_gu")) && isValueEmpty(feature.getProperty("mahd_gu"))
                            && isValueEmpty(feature.getProperty("gu_gu"))) {
                    showMessage("Die Attribute tech, na_gu, mahd_gu und gu_gu dürfen nicht alle leer sein", column);
                    return oldValue;
                }
            }
            if (column.equals("na_gu")) {
                if (isValueEmpty(feature.getProperty("tech")) && isValueEmpty(feature.getProperty("mahd_gu"))
                            && isValueEmpty(feature.getProperty("gu_gu"))) {
                    showMessage("Die Attribute tech, na_gu, mahd_gu und gu_gu dürfen nicht alle leer sein", column);
                    return oldValue;
                }
            }
            if (column.equals("mahd_gu")) {
                if (isValueEmpty(feature.getProperty("tech")) && isValueEmpty(feature.getProperty("na_gu"))
                            && isValueEmpty(feature.getProperty("gu_gu"))) {
                    showMessage("Die Attribute tech, na_gu, mahd_gu und gu_gu dürfen nicht alle leer sein", column);
                    return oldValue;
                }
            }
            if (column.equals("gu_gu")) {
                if (isValueEmpty(feature.getProperty("tech")) && isValueEmpty(feature.getProperty("na_gu"))
                            && isValueEmpty(feature.getProperty("mahd_gu"))) {
                    showMessage("Die Attribute tech, na_gu, mahd_gu und gu_gu dürfen nicht alle leer sein", column);
                    return oldValue;
                }
            }
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
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
        } else if (columnName.equals("tech")) {
            String wbvPraefix = (String)AppBroker.getInstance().getOwnWwGr().getProperty("praefix");

            try {
                Integer.parseInt(wbvPraefix);
            } catch (NumberFormatException w) {
                wbvPraefix = null;
            }

            final CidsLayerFeatureFilter filter = ((wbvPraefix != null)
                    ? createCidsLayerFeatureFilter("wbv", wbvPraefix) : null);
//            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("erlaubt");
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("tech") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("gu_gu")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("gu_gu") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("mahd_gu")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("mahd_gu") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("na_gu")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("na_gu") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else {
            return null;
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
            if (isValueEmpty(feature.getProperty("tech")) && isValueEmpty(feature.getProperty("na_gu"))
                        && isValueEmpty(feature.getProperty("mahd_gu"))
                        && isValueEmpty(feature.getProperty("gu_gu"))) {
                showMessage(
                    "Die Attribute tech, na_gu, mahd_gu und gu_gu dürfen nicht alle leer sein",
                    "tech / na_gu / mahd_gu / gu_gu");
                if (isValueEmpty(feature.getProperty("tech"))) {
                    return new ErrorDetails(feature, "tech");
                } else if (isValueEmpty(feature.getProperty("na_gu"))) {
                    return new ErrorDetails(feature, "na_gu");
                } else if (isValueEmpty(feature.getProperty("mahd_gu"))) {
                    return new ErrorDetails(feature, "mahd_gu");
                } else {
                    return new ErrorDetails(feature, "gu_gu");
                }
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
