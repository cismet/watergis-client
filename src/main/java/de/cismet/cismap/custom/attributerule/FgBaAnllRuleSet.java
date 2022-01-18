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

import java.util.List;

import javax.swing.JLabel;
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
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaAnllRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("anll", new Catalogue("k_anll", true, true, new Varchar(10, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("obj_nr_gu", new Varchar(50, false, true));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true, new Varchar(10, false, false)));
        typeMap.put("traeger_gu", new Varchar(50, false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true, new Numeric(1, 0, false, false)));
        typeMap.put("esw", new BooleanAsInteger(false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("ww_gr") && !columnName.equals("laenge") && !columnName.equals("obj_nr")
                    && !columnName.equals("id") && !columnName.equals("geom") && !columnName.equals("ba_cd");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (isValueEmpty(newValue)) {
            if (column.equals("anll")) {
                showMessage("Das Attribut "
                            + column
                            + " darf nicht leer sein", column);
                return oldValue;
            }
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

        if (column.equals("ba_st_von") || (column.equals("ba_st_bis") && (newValue != null))) {
            final Double from = (column.equals("ba_st_von") ? (Double)newValue
                                                            : (Double)feature.getProperty("ba_st_von"));
            final Double till = (column.equals("ba_st_von") ? (Double)feature.getProperty("ba_st_bis")
                                                            : (Double)newValue);

            if ((from != null) && (till != null)) {
                final double length = Math.abs(till - from);
                if ((feature.getProperty("anll") != null)
                            && isValueIn(feature.getProperty("anll"), new String[] { "See", "Spei" }, false)) {
                    if (!checkRange("länge", length, 20, 20000, 5, 50000, false, true, true)) {
                        return false;
                    }
                } else if ((feature.getProperty("anll") != null)
                            && isValueIn(feature.getProperty("anll"), new String[] { "Drte", "Faa", "Rb" }, false)) {
                    if (!checkRange("länge", length, 10, 100, 5, 200, false, true, true)) {
                        return false;
                    }
                } else if ((feature.getProperty("anll") != null)
                            && isValueIn(
                                feature.getProperty("anll"),
                                new String[] { "Ds", "Sf", "Si", "Sleu", "Tosb", "WKA" },
                                false)) {
                    if (!checkRange("länge", length, 1, 50, 1, 200, false, true, true)) {
                        return false;
                    }
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
        } else if (columnName.equals("anll")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("anll") + " - " + bean.getProperty("name");
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
            if (isValueEmpty(feature.getProperty("anll"))) {
                showMessage("Das Attribut anll darf nicht leer sein", "anll");

                return new ErrorDetails(feature, "anll");
            }

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
                return new ErrorDetails(feature, "ausbaujahr");
            }

            if ((feature.getProperty("anll") != null)
                        && (feature.getProperty("anll").equals("See") || feature.getProperty("anll").equals("Spei"))) {
                final Geometry geom = (Geometry)feature.getProperty("geom");

                if (geom != null) {
                    final double length = round(geom.getLength());

                    if (!checkRange("laenge", length, 20, 20000, 5, 50000, true, true, false)) {
                        return new ErrorDetails(feature, "ba_st_bis");
                    }
                }
            }

            if ((feature.getProperty("anll") != null)
                        && (feature.getProperty("anll").equals("Drte") || feature.getProperty("anll").equals("Faa")
                            || feature.getProperty("anll").equals("Rb"))) {
                final Geometry geom = (Geometry)feature.getProperty("geom");

                if (geom != null) {
                    final double length = round(geom.getLength());

                    if (!checkRange("laenge", length, 10, 100, 5, 200, true, true, false)) {
                        return new ErrorDetails(feature, "ba_st_bis");
                    }
                }
            }

            if ((feature.getProperty("anll") != null)
                        && (feature.getProperty("anll").equals("Ds") || feature.getProperty("anll").equals("Sf")
                            || feature.getProperty("anll").equals("Si") || feature.getProperty("anll").equals("Sleu")
                            || feature.getProperty("anll").equals("Tosb") || feature.getProperty("anll").equals(
                                "WKA"))) {
                final Geometry geom = (Geometry)feature.getProperty("geom");

                if (geom != null) {
                    final double length = round(geom.getLength());

                    if (!checkRange("laenge", length, 1, 50, 1, 200, true, true, false)) {
                        return new ErrorDetails(feature, "ba_st_bis");
                    }
                }
            }

            final Double from = (Double)feature.getProperty("ba_st_von");
            final Double till = (Double)feature.getProperty("ba_st_bis");

            if ((from != null) && (till != null)) {
                final double length = Math.abs(till - from);
                if ((feature.getProperty("anll") != null)
                            && isValueIn(feature.getProperty("anll"), new String[] { "See", "Spei" }, false)) {
                    if (!checkRange("länge", length, 20, 20000, 5, 50000, false, true, true)) {
                        return new ErrorDetails(feature, "ba_st_bis");
                    }
                } else if ((feature.getProperty("anll") != null)
                            && isValueIn(feature.getProperty("anll"), new String[] { "Drte", "Faa", "Rb" }, false)) {
                    if (!checkRange("länge", length, 10, 100, 5, 200, false, true, true)) {
                        return new ErrorDetails(feature, "ba_st_bis");
                    }
                } else if ((feature.getProperty("anll") != null)
                            && isValueIn(
                                feature.getProperty("anll"),
                                new String[] { "Ds", "Sf", "Si", "Sleu", "Tosb", "WKA" },
                                false)) {
                    if (!checkRange("länge", length, 1, 20, 1, 200, false, true, true)) {
                        return new ErrorDetails(feature, "ba_st_bis");
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
                1,
                50000);
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
