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
import Sirius.server.newuser.User;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.search.AllRoutes;
import de.cismet.cids.custom.watergis.server.search.CalculateFgLa;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.PointAndStationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.RouteTableCellEditor;
import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgLaKmRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, true));
        typeMap.put("la_cd", new Numeric(15, 0, false, false));
        typeMap.put("la_st", new Numeric(10, 2, false, true));
        typeMap.put("km_km", new Numeric(7, 1, false, true));
        typeMap.put("km_km_c", new Varchar(9, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("geom") && !columnName.equals("id")
                    && !columnName.equals("la_st") && !columnName.equals("la_cd");
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

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
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
        } else if (columnName.equals("ba_st")) {
            return new StationTableCellEditor(columnName);
        }

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
    public FeatureCreator getFeatureCreator() {
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");

        return new PointAndStationCreator("ba_st", null, routeMc, new LinearReferencingWatergisHelper());
    }

    @Override
    public boolean isCatThree() {
        return true;
    }
}
