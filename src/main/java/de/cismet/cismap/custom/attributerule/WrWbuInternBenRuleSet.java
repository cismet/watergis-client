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

import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.PointAndStationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.RouteTableCellEditor;
import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.hasAccessToProtectedWbbl;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WrWbuInternBenRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(WrWbuAusRuleSet.class);
    private static final String URL_TEMPLATE_PUBLIC =
        "https://fis-wasser-mv.de/kvwmap/index.php?gast=37&go=Layer-Suche_Suchen&selected_layer_id=1118&value_wbbl_id=%1s&operator_wbbl_id==";
    private static final String URL_TEMPLATE_INTERN =
        "https://fis-wasser-mv.de/kvwmap/index.php?gast=37&go=Layer-Suche_Suchen&selected_layer_id=1117&value_wbbl_id=%1s&operator_wbbl_id==";

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, true));
        typeMap.put("la_cd", new Numeric(15, 0, false, false));
        typeMap.put("la_st", new Numeric(10, 2, false, false));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, true, true));
        typeMap.put("wbaktzei", new Varchar(75, false, true));
        typeMap.put("wbbehakt", new Varchar(65, false, true));
        typeMap.put("wbbenart1", new Varchar(75, false, true));
        typeMap.put("wbbenart2", new Varchar(75, false, true));
        typeMap.put("wbbenzw1", new Varchar(75, false, true));
        typeMap.put("wbbenzw2", new Varchar(75, false, true));
        typeMap.put("wbbenzweck", new Numeric(4, 0, false, true));
        typeMap.put("wbbengew", new Varchar(50, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

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
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("geom") && !columnName.equals("id") && !columnName.equals("la_cd")
                    && !columnName.equals("la_st");
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
    public FeatureCreator getFeatureCreator() {
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");

        return new PointAndStationCreator("ba_st", null, routeMc, new LinearReferencingWatergisHelper());
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("wbbl")) {
            if ((value != null) && (clickCount == 1)) {
                final String urlTemplate = (hasAccessToProtectedWbbl() ? URL_TEMPLATE_INTERN : URL_TEMPLATE_PUBLIC);
                final String wbbl = String.valueOf(value);

                try {
                    final URL u = new URL(String.format(urlTemplate, wbbl.toString()));

                    try {
                        de.cismet.tools.BrowserLauncher.openURL(u.toString());
                    } catch (Exception ex) {
                        LOG.error("Cannot open the url:" + u, ex);
                    }
                } catch (MalformedURLException ex) {
                    // nothing to do
                }
            }
        }
    }

    @Override
    public boolean isCatThree() {
        return true;
    }

    @Override
    public FeatureServiceFeature[] prepareFeaturesForExport(final FeatureServiceFeature[] features) {
        final List<FeatureServiceFeature> featureList = new ArrayList<FeatureServiceFeature>();

        for (final FeatureServiceFeature f : features) {
            final FeatureServiceFeature newFeature = (FeatureServiceFeature)f.clone();
            newFeature.removeProperty("ba_cd");
            newFeature.removeProperty("ba_st");
            newFeature.removeProperty("la_cd");
            newFeature.removeProperty("la_st");
            featureList.add(newFeature);
        }

        return featureList.toArray(new FeatureServiceFeature[featureList.size()]);
    }
}
