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

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.search.RemoveUnnusedRoute;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.SimpleAttributeTableModel;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.util.SelectionManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SgSuRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SgSuRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("su_cd", new Varchar(50, true, true));
        typeMap.put("su_st_von", new Numeric(10, 2, false, false));
        typeMap.put("su_st_bis", new Numeric(10, 2, false, false));
        typeMap.put("see_gn", new Varchar(50, true, true));
        typeMap.put("see_lawa", new Varchar(20, true, true));
        typeMap.put("see_sp", new Varchar(8, true, true));
        typeMap.put("see_wrrl", new Numeric(1, 0, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("laenge") && !columnName.equals("su_st_von")
                    && !columnName.equals("su_st_bis")
                    && !columnName.equals("geom") && !columnName.equals("id");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ww_gr")) {
            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true));
        }
        return null;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());
    }

    @Override
    public void afterSave(final TableModel model) {
        if (model instanceof SimpleAttributeTableModel) {
            final List<FeatureServiceFeature> removedFeatures = ((SimpleAttributeTableModel)model).getRemovedFeature();

            if ((removedFeatures != null) && !removedFeatures.isEmpty()) {
                final List<Feature> selectedFeaturesToRemove = new ArrayList<Feature>();

                for (final FeatureServiceFeature feature : removedFeatures) {
                    try {
                        final CidsServerSearch nodesSearch = new RemoveUnnusedRoute(feature.getId(),
                                RemoveUnnusedRoute.SU);
                        SessionManager.getProxy()
                                .customServerSearch(SessionManager.getSession().getUser(), nodesSearch);
                    } catch (Exception e) {
                        LOG.error("Error while removing unused stations", e);
                    }

                    final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures();

                    for (final Feature f : selectedFeatures) {
                        if (f instanceof CidsLayerFeature) {
                            final CidsLayerFeature clf = (CidsLayerFeature)f;

                            if ((clf.getProperty("su_cd") != null) && (feature.getProperty("su_cd") != null)) {
                                final String selectedFeatureBaCd = String.valueOf(clf.getProperty("su_cd"));
                                final String deletedFeatureBaCd = String.valueOf(feature.getProperty("su_cd"));

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
        return new String[] { "su_st_von", "su_st_bis", "laenge" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("laenge")) {
            return -3;
        } else if (name.equals("su_st_von")) {
            return 3;
        } else if (name.equals("su_st_bis")) {
            return 4;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Double value = null;

        if (propertyName.equals("su_st_von")) {
            return 0;
        }

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
        } else if (propertyName.equals("su_st_von")) {
            return "0";
        } else if (propertyName.equals("su_st_bis")) {
            return "round(st_length(geom)::numeric, 2)";
        }

        return null;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.LINESTRING);
    }
}
