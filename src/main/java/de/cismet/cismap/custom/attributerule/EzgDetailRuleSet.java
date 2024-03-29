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

import Sirius.navigator.exception.ConnectionException;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.awt.Component;

import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.actions.RefreshTemplateAction;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.SimpleAttributeTableModel;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class EzgDetailRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(EzgDetailRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("gbk_lawa", new Catalogue("k_gbk_lawa", false, true, true, new Numeric(15, 0, false, false)));
        typeMap.put("gbk_lawa_k", new Catalogue("k_gbk_lawa", false, true, new Numeric(15, 0, false, false)));
        typeMap.put("gwk_lawa", new Catalogue("k_gwk_lawa", false, true, true, new Numeric(15, 0, false, false)));
        typeMap.put("gwk_gn", new Catalogue("k_gwk_lawa", false, true, new Varchar(60, false, false)));
        typeMap.put("gbk_von", new Catalogue("k_gbk_lawa", false, true, new Varchar(100, false, false)));
        typeMap.put("gbk_bis", new Catalogue("k_gbk_lawa", false, true, new Varchar(100, false, false)));
        typeMap.put("gbk_ordn", new Catalogue("k_gbk_lawa", false, true, new Numeric(2, 0, false, false)));
        typeMap.put("gbk_pl", new Catalogue("k_gbk_lawa", false, true, new Numeric(10, 0, false, false)));
        typeMap.put("ezg_fl", new Numeric(12, 0, false, true));
        typeMap.put("flaeche", new Numeric(12, 0, false, false));
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
        if (column.equals("gbk_lawa") && (newValue != null)) {
            final CidsLayerFeature cf = (CidsLayerFeature)feature;
            final Object o = cf.getCatalogueCombo("gbk_lawa").getSelectedItem();

            if (o instanceof CidsLayerFeature) {
                final CidsLayerFeature layerFeature = (CidsLayerFeature)o;
                feature.setProperty("gbk_lawa_k", layerFeature.getBean());
                feature.setProperty("gbk_von", layerFeature.getBean());
                feature.setProperty("gbk_bis", layerFeature.getBean());
                feature.setProperty("gbk_ordn", layerFeature.getBean());
                feature.setProperty("gbk_pl", layerFeature.getBean());
                try {
                    ((CidsLayerFeature)feature).getCatalogueCombo("gbk_lawa_k").setSelectedItem(layerFeature);
                    ((CidsLayerFeature)feature).getCatalogueCombo("gbk_von").setSelectedItem(layerFeature);
                    ((CidsLayerFeature)feature).getCatalogueCombo("gbk_bis").setSelectedItem(layerFeature);
                    ((CidsLayerFeature)feature).getCatalogueCombo("gbk_ordn").setSelectedItem(layerFeature);
                    ((CidsLayerFeature)feature).getCatalogueCombo("gbk_pl").setSelectedItem(layerFeature);
                } catch (Exception e) {
                    LOG.error("Cannot set dependent properties", e);
                }
            }

            return newValue;
        } else if (column.equals("gwk_lawa") && (newValue != null)) {
            final CidsLayerFeature cf = (CidsLayerFeature)feature;
            final Object o = cf.getCatalogueCombo("gwk_lawa").getSelectedItem();

            if (o instanceof CidsLayerFeature) {
                final CidsLayerFeature layerFeature = (CidsLayerFeature)o;
                feature.setProperty("gwk_gn", layerFeature.getBean());
                try {
                    ((CidsLayerFeature)feature).getCatalogueCombo("gwk_gn").setSelectedItem(layerFeature);
                } catch (Exception e) {
                    LOG.error("Cannot set dependent properties", e);
                }
            }

            return newValue;
        } else {
            return super.afterEdit(feature, column, row, oldValue, newValue);
        }
    }

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("flaeche") && !columnName.equals("geom") && !columnName.equals("id")
                    && !columnName.equals("gbk_lawa_k") && !columnName.equals("gbk_von")
                    && !columnName.equals("gbk_bis") && !columnName.equals("gbk_pl") && !columnName.equals("gbk_ordn")
                    && !columnName.equals("gwk_gn");
    }

    @Override
    public FeatureServiceFeature cloneFeature(final FeatureServiceFeature feature) {
        final DefaultFeatureServiceFeature newFeature = (DefaultFeatureServiceFeature)feature
                    .getLayerProperties().getFeatureService().getFeatureFactory().createNewFeature();

        final HashMap<String, Object> properties = feature.getProperties();
        final CidsBean bean = ((CidsLayerFeature)feature).getBean();

        for (final String propertyKey : properties.keySet()) {
            if (propertyKey.equalsIgnoreCase("gbk_lawa_k") || propertyKey.equalsIgnoreCase("gbk_von")
                        || propertyKey.equalsIgnoreCase("gbk_bis") || propertyKey.equalsIgnoreCase("gbk_pl")
                        || propertyKey.equalsIgnoreCase("gbk_ordn")) {
                final CidsLayerFeature clf = (CidsLayerFeature)feature;
                Object value = clf.getPropertyObject("gbk_lawa");

                if (value instanceof CidsLayerFeature) {
                    value = ((CidsLayerFeature)value).getBean();
                }

                newFeature.setProperty(propertyKey, value);
            } else if (propertyKey.equalsIgnoreCase("gwk_gn")) {
                final CidsLayerFeature clf = (CidsLayerFeature)feature;
                Object value = clf.getPropertyObject("gwk_lawa");

                if (value instanceof CidsLayerFeature) {
                    value = ((CidsLayerFeature)value).getBean();
                }

                newFeature.setProperty(propertyKey, value);
            } else if (!propertyKey.equalsIgnoreCase("id") && !propertyKey.equals(feature.getIdExpression())
                        && !propertyKey.equalsIgnoreCase("obj_nr")) {
                if (bean.getProperty(propertyKey) == null) {
                    newFeature.setProperty(propertyKey, feature.getProperty(propertyKey));
                } else {
                    if ((bean.getProperty(propertyKey) instanceof CidsBean)
                                && bean.getProperty(propertyKey).getClass().getName().equals(
                                    "de.cismet.cids.dynamics.Geom")) {
                        newFeature.setProperty(propertyKey, feature.getGeometry());
                    } else {
                        newFeature.setProperty(propertyKey, bean.getProperty(propertyKey));
                    }
                }
            }
        }

        newFeature.setGeometry(feature.getGeometry());

        return newFeature;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("gbk_lawa_k") || columnName.equals("gbk_von") || columnName.equals("gbk_bis")
                    || columnName.equals("gbk_pl") || columnName.equals("gbk_ordn") || columnName.equals("gwk_gn")) {
            return new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(final JTable table,
                            final Object value,
                            final boolean isSelected,
                            final boolean hasFocus,
                            final int row,
                            final int column) {
                        Object val = value;

                        if (table.getModel() instanceof SimpleAttributeTableModel) {
                            final SimpleAttributeTableModel model = (SimpleAttributeTableModel)table.getModel();
                            val = model.getFeatureServiceFeature(table.convertRowIndexToModel(row))
                                        .getProperty(columnName);
                        }

                        if (val instanceof CidsBean) {
                            if (columnName.equals("gwk_gn")) {
                                val = ((CidsBean)val).getProperty("la_gn");
                            } else {
                                val = ((CidsBean)val).getProperty(columnName);
                            }
                        }

                        final Component c = super.getTableCellRendererComponent(
                                table,
                                val,
                                isSelected,
                                hasFocus,
                                row,
                                column);

                        if ((c instanceof JLabel) && !columnName.equals("gwk_gn") && !columnName.equals("gbk_von")
                                    && !columnName.equals("gbk_bis")) {
                            ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                            ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                        }

                        return c;
                    }
                };
        }
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("gbk_lawa")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.NUMERIC),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        final Object result = bean.getProperty("gbk_lawa");

                        if (result != null) {
                            return result.toString();
                        } else {
                            return null;
                        }
                    }
                });

            return editor;
        }

        if (columnName.equals("gwk_lawa")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.NUMERIC),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        final Object result = bean.getProperty("la_cd");

                        if (result != null) {
                            return result.toString();
                        } else {
                            return null;
                        }
                    }
                });

            return editor;
        }

        return super.getCellEditor(columnName);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
        super.beforeSave(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
        try {
            refreshTemplate(RefreshTemplateAction.DRAIN_BASIN);
            refreshTemplate(RefreshTemplateAction.EZG_K_RL);
        } catch (ConnectionException ex) {
            LOG.error("Cannot refresh template", ex);
        }

        super.afterSave(model);
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
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
        c.setMinArea(MIN_AREA_SIZE);

        return c;
    }
}
