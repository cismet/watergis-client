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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.checkRange;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GuWiweRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final HashMap<String, String[]> allowedMaterial = new HashMap<String, String[]>();

    static {
        allowedMaterial.put(
            "Decke",
            new String[] { "As", "B", "Kies", "Kies-wg", "Rec", "Rec-wg", "Sand", "Sand-wg", "Scho", "Scho-wg" });
        allowedMaterial.put("Decke-Pfl", new String[] { "B" });
        allowedMaterial.put("Decke-Pl", new String[] { "B" });
        allowedMaterial.put("Decke-Rg", new String[] { "B" });
        allowedMaterial.put("Spur", new String[] { "As", "B" });
        allowedMaterial.put("Spur-Pfl", new String[] { "B" });
        allowedMaterial.put("Spur-Pl", new String[] { "B" });
        allowedMaterial.put("Spur-Rg", new String[] { "B" });
    }

    //~ Instance fields --------------------------------------------------------

    private final Logger LOG = Logger.getLogger(GuWiweRuleSet.class);
    private double lastLength = -1;

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", true, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true));
        typeMap.put("wiwe", new Catalogue("k_wiwe", true, true));
        typeMap.put("material", new Catalogue("k_material", false, true));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br", new Numeric(4, 2, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("id") && !columnName.equals("laenge") && !columnName.equals("ba_cd")
                    && !columnName.equals("geom") && !columnName.equals("obj_nr");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (column.equals("ausbaujahr")
                    && !checkRange(
                        column,
                        newValue,
                        1950,
                        getCurrentYear(),
                        1800,
                        getCurrentYear()
                        + 2,
                        true,
                        true,
                        true)) {
            return oldValue;
        }

        if (column.equals("br") && !checkRange(column, newValue, 0, 10, 0, 30, true, false, true)) {
            return oldValue;
        }

        if (newValue instanceof Geometry) {
            final Geometry g = (Geometry)newValue;
            if ((g.getLength() < 1) && (g.getLength() != lastLength)) {
                showMessage("Die Länge der Geometry darf nicht kleiner als 1m sein.");

                return oldValue;
            }
            lastLength = g.getLength();
        }

        if (column.equals("wiwe") && (newValue != null) && !isValueEmpty(feature.getProperty("material"))) {
            final String[] allowedMaterialArray = allowedMaterial.get(newValue.toString());

            if (allowedMaterialArray != null) {
                if ((isValueEmpty(feature.getProperty("material")))
                            || !arrayContains(allowedMaterialArray,
                                feature.getProperty("material").toString())) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut wiwe = "
                                + newValue.toString()
                                + ", dann muss das Attribut material "
                                + arrayToString(allowedMaterialArray)
                                + " sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("material") && (newValue != null) && (feature.getProperty("wiwe") != null)) {
            final String[] allowedMaterialArray = allowedMaterial.get(feature.getProperty("wiwe").toString());

            if (allowedMaterialArray != null) {
                if ((isValueEmpty(newValue))
                            || !arrayContains(allowedMaterialArray,
                                newValue.toString())) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Wenn das Attribut wiwe = "
                                + feature.getProperty("wiwe").toString()
                                + ", dann muss das Attribut material "
                                + arrayToString(allowedMaterialArray)
                                + " sein.");
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
            final CidsLayerReferencedComboEditor ed = new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true));
            ed.setNullable(false);

            return ed;
        } else if (columnName.equals("traeger")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("traeger") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("zust_kl")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("zust_kl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("wiwe")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("wiwe") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
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
        } else if (columnName.equals("material")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("wiwe");
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
                        return bean.getProperty("material") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        }
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            if (
                !checkRange(
                            "ausbaujahr",
                            feature.getProperty("ausbaujahr"),
                            1800,
                            getCurrentYear()
                            + 2,
                            true,
                            true,
                            true)) {
                return false;
            }
            if (!checkRange("br", feature.getProperty("br"), 0, 30, true, false, true)) {
                return false;
            }

            if (feature.getProperty("wiwe") != null) {
                final String[] allowedMaterialArray = allowedMaterial.get(feature.getProperty("wiwe").toString());

                if (allowedMaterialArray != null) {
                    if ((isValueEmpty(feature.getProperty("material")))
                                || !arrayContains(allowedMaterialArray,
                                    feature.getProperty("material").toString())) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Wenn das Attribut wiwe = "
                                    + feature.getProperty("wiwe").toString()
                                    + ", dann muss das Attribut material "
                                    + arrayToString(allowedMaterialArray)
                                    + " sein.");
                        return false;
                    }
                }
            }
        }

        return super.prepareForSave(features);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());
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
        // todo Länge >= 1
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.LINESTRING, getDefaultValues());
    }

    @Override
    public Map<String, Object> getDefaultValues() {
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("ww_gr", AppBroker.getInstance().getOwnWwGr());

        return properties;
    }

    @Override
    public FeatureServiceFeature cloneFeature(final FeatureServiceFeature feature) {
        return super.cloneFeature(feature);
    }
}
