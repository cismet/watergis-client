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

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
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

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.arrayContains;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaUbefRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final HashMap<String, String[]> allowedMaterial = new HashMap<String, String[]>();

    static {
        allowedMaterial.put("Fa", new String[] { "H-Rsg", "Kok" });
        allowedMaterial.put("Gtr", new String[] { "B", "K" });
        allowedMaterial.put("Mte", new String[] { "Vl" });
        allowedMaterial.put("Pfr", new String[] { "B", "H", "K", "St" });
        allowedMaterial.put("Pl", new String[] { "B" });
        allowedMaterial.put("SP", new String[] { "Ste", "Ste-Fs", "Ste-Wb" });
        allowedMaterial.put("Spw", new String[] { "H", "K", "St", "St-B", "Ste-Gab" });
        allowedMaterial.put("Wistü", new String[] { "B", "K", "St", "St-B" });
    }

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("l_rl", new Catalogue("k_l_rl", true, true, new Varchar(2, false, false)));
        typeMap.put("ubef", new Catalogue("k_ubef", true, true, new Varchar(10, false, false)));
        typeMap.put("material", new Catalogue("k_material", false, true, new Varchar(10, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("obj_nr_gu", new Varchar(50, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true, new Varchar(10, false, false)));
        typeMap.put("traeger_gu", new Varchar(50, false, false));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true, new Numeric(1, 0, false, false)));
        typeMap.put("esw", new BooleanAsInteger(false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br", new Numeric(4, 2, false, true));
        typeMap.put("ho_d_o", new Numeric(4, 2, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
        minBaLength = 0.5;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("laenge") && !columnName.equals("ww_gr") && !columnName.equals("ba_cd")
                    && !columnName.equals("geom") && !columnName.equals("obj_nr");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (isValueEmpty(newValue)) {
            if (column.equals("l_rl") || column.equals("ubef")) {
                showMessage("Das Attribut "
                            + column
                            + " darf nicht leer sein", "ubef");
                return oldValue;
            }
        }

        if (column.equals("br") && !checkRange(column, newValue, 0, 2, 0, 10, true, false, true)) {
            return oldValue;
        }

        if (column.equals("ho_d_o") && !checkRange(column, newValue, 0, 10, 0, 15, true, false, true)) {
            return oldValue;
        }

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

        if (column.equals("material") && !isValueEmpty(newValue)) {
            final String[] allowedMaterialVArray = allowedMaterial.get(feature.getProperty("ubef").toString());

            if (allowedMaterialVArray != null) {
                if (!arrayContains(allowedMaterialVArray, (newValue.toString()))) {
                    showMessage("Wenn das Attribut ubef = "
                                + feature.getProperty("ubef").toString()
                                + ", dann muss das Attribut material "
                                + arrayToString(allowedMaterialVArray)
                                + " sein.",
                        "material");
                    return oldValue;
                }
            }
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ba_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ba_st_bis")) {
            return new StationTableCellEditor(columnName);
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
        } else if (columnName.equals("l_rl")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    createCidsLayerFeatureFilter("ubef"));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("l_rl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("ubef")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("ubef") + " - " + bean.getProperty("name");
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
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    createCidsLayerFeatureFilter("ubef"));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("material") + " - " + bean.getProperty("name");
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
            if (isValueEmpty(feature.getProperty("l_rl"))) {
                showMessage("Das Attribut l_rl darf nicht leer sein", "l_rl");
                return new ErrorDetails(feature, "l_rl");
            }
            if (isValueEmpty(feature.getProperty("ubef"))) {
                showMessage("Das Attribut ubef darf nicht leer sein", "ubef");
                return new ErrorDetails(feature, "ubef");
            }
            if (!checkRange("br", feature.getProperty("br"), 0, 10, true, false, true)) {
                return new ErrorDetails(feature, "br");
            }
            if (!checkRange("ho_d_o", feature.getProperty("ho_d_o"), 0, 15, true, false, true)) {
                return new ErrorDetails(feature, "ho_d_o");
            }

            if (
                !checkRangeBetweenOrEqual(
                            "ausbaujahr",
                            feature.getProperty("ausbaujahr"),
                            1800,
                            getCurrentYear()
                            + 2,
                            true)) {
                return new ErrorDetails(feature, "ausbaujahr");
            }

            if (feature.getProperty("ubef") != null) {
                final String[] allowedMaterialVArray = allowedMaterial.get(feature.getProperty("ubef").toString());

                if (allowedMaterialVArray != null) {
                    if (!isValueEmpty(feature.getProperty("material"))
                                && !arrayContains(
                                    allowedMaterialVArray,
                                    ((feature.getProperty("material") != null)
                                        ? feature.getProperty("material").toString() : null))) {
                        showMessage("Wenn das Attribut ubef = "
                                    + feature.getProperty("ubef").toString()
                                    + ", dann muss das Attribut material "
                                    + arrayToString(allowedMaterialVArray)
                                    + " sein.",
                            "material");
                        return new ErrorDetails(feature, "material");
                    }
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
        creator.setCheck(check);

        return creator;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("wbbl")) {
            if ((value instanceof String) && (clickCount == 1)) {
                downloadDocumentFromWebDav(getWbblPath(), addExtension(value.toString(), "pdf"));
            }
        }
    }
}
