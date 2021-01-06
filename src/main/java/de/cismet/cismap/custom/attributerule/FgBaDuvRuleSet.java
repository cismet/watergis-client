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

import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.search.FgBaCdCheck;
import de.cismet.cids.custom.watergis.server.search.RemoveUnnusedRoute;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.SimpleAttributeTableModel;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaDuvRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Random RANDOM = new Random(new Date().getTime());
    private static final Logger LOG = Logger.getLogger(FgBaDuvRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(false, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, true, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, true));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, false));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, false));
        typeMap.put("ba_gn", new Varchar(50, false));
        typeMap.put("wdm", new Numeric(4, 0, false));
        typeMap.put("gu_zust", new Varchar(2, false));
        typeMap.put("gu_cd", new Varchar(50, false));
        typeMap.put("bemerkung", new Varchar(250, false));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !(columnName.equals("laenge") || columnName.equals("id") || columnName.equals("fis_g_date")
                        || columnName.equals("fis_g_user"));
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
//        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "duv.fg_ba_duv");
//        final CidsLayer layer = new CidsLayer(routeMc);
//        SplitAction.commit(layer);

        if (model instanceof SimpleAttributeTableModel) {
            final List<FeatureServiceFeature> removedFeatures = ((SimpleAttributeTableModel)model).getRemovedFeature();

            if ((removedFeatures != null) && !removedFeatures.isEmpty()) {
                final List<Feature> selectedFeaturesToRemove = new ArrayList<Feature>();

                for (final FeatureServiceFeature feature : removedFeatures) {
                    try {
                        final CidsServerSearch nodesSearch = new RemoveUnnusedRoute(feature.getId(),
                                RemoveUnnusedRoute.FG_BA_DUV);
                        SessionManager.getProxy()
                                .customServerSearch(SessionManager.getSession().getUser(), nodesSearch);
                    } catch (Exception e) {
                        LOG.error("Error while removing unused stations", e);
                    }

//                    final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures();
//
//                    for (final Feature f : selectedFeatures) {
//                        if (f instanceof CidsLayerFeature) {
//                            final CidsLayerFeature clf = (CidsLayerFeature)f;
//
//                            if ((clf.getProperty("ba_cd") != null) && (feature.getProperty("ba_cd") != null)) {
//                                final String selectedFeatureBaCd = String.valueOf(clf.getProperty("ba_cd"));
//                                final String deletedFeatureBaCd = String.valueOf(feature.getProperty("ba_cd"));
//
//                                if (selectedFeatureBaCd.equals(deletedFeatureBaCd)) {
//                                    selectedFeaturesToRemove.add(f);
//                                }
//                            }
//                        }
//                    }
                }

//                if (!selectedFeaturesToRemove.isEmpty()) {
//                    SelectionManager.getInstance().removeSelectedFeatures(selectedFeaturesToRemove);
//                }
            }
        }
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
        final PrimitiveGeometryCreator creator = new PrimitiveGeometryCreator(
                CreateGeometryListenerInterface.LINESTRING,
                getDefaultValues());
        creator.setMinLength(MIN_LINE_LENGTH);

        return creator;
    }

    @Override
    public Map<String, Object> getDefaultValues() {
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("ww_gr", AppBroker.getInstance().getOwnWwGr());
        String baCd = String.valueOf(AppBroker.getInstance().getOwnWwGr().getProperty("praefix")) + ":";
        final Random r = new Random(new Date().getTime());

        baCd += r.nextInt();
        properties.put("ba_cd", baCd);

        return properties;
    }

    @Override
    public FeatureServiceFeature cloneFeature(final FeatureServiceFeature feature) {
        boolean unique;
        final FeatureServiceFeature newFeature = super.cloneFeature(feature);
        final CidsBean wwGr = (CidsBean)feature.getProperty("original:ww_gr");
        final String prefix = String.valueOf(wwGr.getProperty("praefix"));
        String baCd = (String)newFeature.getProperty("ba_cd");

        do {
            // ensure, that the value of ba_cd is unique
            unique = true;
            try {
                final Map<Integer, String> baCdMap = new HashMap<Integer, String>();
                baCd = prefix + ":" + String.valueOf(Math.abs(RANDOM.nextInt()));
                baCdMap.put(-1, baCd);
                final User user = SessionManager.getSession().getUser();
                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(user, new FgBaCdCheck(baCdMap));

                if ((attributes != null) && !attributes.isEmpty()) {
                    unique = false;
                }
            } catch (Exception e) {
                LOG.error("Error checking baCd", e);
            }
        } while (!unique);

        newFeature.setProperty("ba_cd", baCd);

        return newFeature;
    }
}
