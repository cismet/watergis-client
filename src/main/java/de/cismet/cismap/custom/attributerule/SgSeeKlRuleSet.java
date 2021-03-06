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

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SgSeeKlRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
//        typeMap.put("ww_gr", new Catalogue("k_ww_gr", true, true, new Numeric(4, 0, false, false)));
        typeMap.put("see_gn", new Varchar(50, false, true));
        typeMap.put("gbk_lawa", new Numeric(15, 0, false, true));
        typeMap.put("flaeche", new Numeric(12, 0, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("flaeche") && !columnName.equals("geom") && !columnName.equals("id");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
//        if (columnName.equals("ww_gr")) {
//            CidsLayerFeatureFilter filter = null;
//
//            if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren")) {
//                final String userName = AppBroker.getInstance().getOwner();
//                filter = new CidsLayerFeatureFilter() {
//
//                        @Override
//                        public boolean accept(final CidsLayerFeature bean) {
//                            if (bean == null) {
//                                return false;
//                            }
//                            return bean.getProperty("owner").equals(userName) || bean.getProperty("ww_gr")
//                                        .equals(4000);
//                        }
//                    };
//            } else {
//                filter = new WwGrAdminFilter();
//            }
//            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
//                        "ww_gr",
//                        String.valueOf(Types.INTEGER),
//                        true),
//                    filter);
//        }
        return null;
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
        return new String[] { "flaeche" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("flaeche")) {
            return -3;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Long value = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            value = Math.round(geom.getArea());
        }

        return value;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("flaeche")) {
            return "round(st_area(geom))";
        } else {
            return null;
        }
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Long.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, getDefaultValues(), true);
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
