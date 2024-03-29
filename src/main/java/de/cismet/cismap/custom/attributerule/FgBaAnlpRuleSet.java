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
import de.cismet.cismap.cidslayer.StationCreationCheck;
import de.cismet.cismap.cidslayer.StationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaAnlpRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("l_rl", new Catalogue("k_l_rl", false, true, new Varchar(2, false, false)));
        typeMap.put("anlp", new Catalogue("k_anlp", true, true, new Varchar(10, false, false)));
        typeMap.put("anlp_a", new Catalogue("k_sbef", false, true, new Varchar(10, false, false)));
        typeMap.put("material_a", new Catalogue("k_material", false, true, new Varchar(10, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, true));
        typeMap.put("obj_nr_gu", new Varchar(50, false, false));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true, new Varchar(10, false, false)));
        typeMap.put("traeger_gu", new Varchar(50, false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true, new Numeric(1, 0, false, false)));
        typeMap.put("esw", new BooleanAsInteger(false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br", new Numeric(6, 2, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("ww_gr") && !columnName.equals("ba_cd") && !columnName.equals("geom")
                    && !columnName.equals("obj_nr") && !columnName.equals("id");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (isValueEmpty(newValue)) {
            if (column.equals("anlp") || column.equals("l_rl")) {
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

        if (column.equals("br")) {
            if (!isValueEmpty(feature.getProperty("l_rl"))) {
                if (isValueIn(feature.getProperty("anlp"), new Object[] { "Steg", "VT", "Wee", "Z" }, false)) {
                    if (column.equals("br") && !checkRange(column, newValue, 1, 50, 1, 200, true, false, true)) {
                        return oldValue;
                    }
                } else if (!isValueIn(feature.getProperty("anlp"), new Object[] { "Steg", "VT", "Wee", "Z" }, false)) {
                    if (column.equals("br") && !checkRange(column, newValue, 0, 5, 0, 10, true, false, true)) {
                        return oldValue;
                    }
                }
            }
        }

        if (column.equals("l_rl") && !isValueEmpty(feature.getProperty("anlp"))) {
            if (isValueIn(
                            feature.getProperty("anlp"),
                            new Object[] { "Albw", "Elbw", "Fu", "Rsk", "Schi", "Slu", "Stt" },
                            false)) {
                final Object rl = newValue;

                if ((rl != null) && !rl.toString().equals("mi")) {
                    showMessage(
                        "Bei anlp = Albw/Elbw/Fu/Rsk/Schi/Slu/Stt darf das Attribut l_rl nur den Wert mi haben.",
                        column);
                    return oldValue;
                }
            } else if (isValueIn(
                            feature.getProperty("anlp"),
                            new Object[] { "P", "P-Gr", "P-Steg", "P-Gr-Ste", "P-Lat", "Sta" },
                            false)) {
                final Object rl = newValue;

                if ((rl == null)
                            || !(rl.toString().equals("mi") || rl.toString().equals("re")
                                || rl.toString().equals("li") || rl.toString().equals("bs")
                                || rl.toString().equals("nb"))) {
                    showMessage(
                        "Bei anlp = P/P-Gr/P-Steg/P-Gr-Ste/P-Lat/Sta darf das Attribut l_rl nur die Werte re/li/mi/bs/nb haben.",
                        column);
                    return oldValue;
                }
            } else if (isValueIn(feature.getProperty("anlp"), new Object[] { "Steg", "Vt", "Wes" }, false)) {
                final Object rl = newValue;

                if ((rl == null)
                            || !(rl.toString().equals("bs") || rl.toString().equals("re")
                                || rl.toString().equals("li") || rl.toString().equals("nb"))) {
                    showMessage(
                        "Bei anlp = Steg/Vt/Wes darf das Attribut l_rl nur die Werte re/li/bs/nb haben.",
                        column);
                    return oldValue;
                }
            }
        }

//        if (column.equals("anlp") && !isValueEmpty(feature.getProperty("l_rl"))) {
//            if (isValueIn(
//                            newValue,
//                            new Object[] { "Albw", "Elbw", "Fu", "Rsk", "Schi", "Slu", "Stt" },
//                            false)) {
//                final Object rl = feature.getProperty("l_rl");
//
//                if ((rl != null) && !rl.toString().equals("mi")) {
//                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                        "Bei anlp = Albw/Elbw/Fu/Rsk/Schi/Slu/Stt darf das Attribut l_rl nur den Wert mi haben.");
//                    return oldValue;
//                }
//            } else if (isValueIn(
//                            newValue,
//                            new Object[] { "P", "P-Gr", "P-Steg", "P-Gr-Ste", "P-Lat", "Sta" },
//                            false)) {
//                final Object rl = feature.getProperty("l_rl");
//
//                if ((rl == null)
//                            || !(rl.toString().equals("mi") || rl.toString().equals("re")
//                                || rl.toString().equals("li") || rl.toString().equals("bs"))) {
//                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                        "Bei anlp = P/P-Gr/P-Steg/P-Gr-Ste/P-Lat/Sta darf das Attribut l_rl nur die Werte re/li/mi/bs haben.");
//                    return oldValue;
//                }
//            } else if (isValueIn(newValue, new Object[] { "Steg", "Vt", "Wes" }, false)) {
//                final Object rl = feature.getProperty("l_rl");
//
//                if ((rl == null)
//                            || !(rl.toString().equals("bs") || rl.toString().equals("re")
//                                || rl.toString().equals("li"))) {
//                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                        "Bei anlp = Steg/Vt/Wes darf das Attribut l_rl nur die Werte re/li/bs haben.");
//                    return oldValue;
//                }
//            }
//        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ba_st")) {
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
        } else if (columnName.equals("anlp")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("anlp") + " - " + bean.getProperty("name");
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
        } else if (columnName.equals("l_rl")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    createCidsLayerFeatureFilter("anlp"));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("l_rl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("material_a")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("anlp_a");

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
        } else if (columnName.equals("anlp_a")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("fg_ba_anlp");

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
                        return bean.getProperty("sbef") + " - " + bean.getProperty("name");
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
            if (isValueEmpty(feature.getProperty("anlp"))) {
                showMessage("Das Attribut anlp darf nicht leer sein", "anlp");
                return new ErrorDetails(feature, "anlp");
            }
            if (isValueEmpty(feature.getProperty("l_rl"))) {
                showMessage("Das Attribut l_rl darf nicht leer sein", "l_rl");
                return new ErrorDetails(feature, "l_rl");
            }
            if (isValueEmpty(feature.getProperty("br"))) {
                showMessage("Das Attribut br darf nicht leer sein", "br");
                return new ErrorDetails(feature, "br");
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

            if (isValueIn(feature.getProperty("anlp"), new Object[] { "Steg", "VT", "Wee" }, false)) {
                final Number br = toNumber(feature.getProperty("br"));

                if ((br.doubleValue() < 1) || (br.doubleValue() > 200)) {
                    showMessage("Das Attribut br muss zwischen 1 und 200 liegen, wenn anlp = Steg/VT/Wee", "br");
                    return new ErrorDetails(feature, "br");
                }
            } else {
                final Number br = toNumber(feature.getProperty("br"));

                if ((br.doubleValue() < 0) || (br.doubleValue() > 10)) {
                    showMessage(
                        "Das Attribut br muss zwischen 0 und 10 liegen, wenn anlp nicht gleich Steg/VT/Wee",
                        "br");
                    return new ErrorDetails(feature, "br");
                }
            }

            if (isValueIn(
                            feature.getProperty("anlp"),
                            new Object[] { "Albw", "Elbw", "Fu", "Rsk", "Schi", "Slu", "Stt" },
                            false)) {
                final Object rl = feature.getProperty("l_rl");

                if ((rl != null) && !rl.toString().equals("mi")) {
                    showMessage(
                        "Bei anlp = Albw/Elbw/Fu/Rsk/Schi/Slu/Stt darf das Attribut l_rl nur den Wert mi haben.",
                        "l_rl");
                    return new ErrorDetails(feature, "l_rl");
                }
            } else if (isValueIn(
                            feature.getProperty("anlp"),
                            new Object[] { "P", "P-Gr", "P-Steg", "P-Gr-Ste", "P-Lat", "Sta" },
                            false)) {
                final Object rl = feature.getProperty("l_rl");

                if ((rl == null)
                            || !(rl.toString().equals("mi") || rl.toString().equals("re")
                                || rl.toString().equals("li") || rl.toString().equals("bs")
                                || rl.toString().equals("nb"))) {
                    showMessage(
                        "Bei anlp = P/P-Gr/P-Steg/P-Gr-Ste/P-Lat/Sta darf das Attribut l_rl nur die Werte re/li/mi/bs/nb haben.",
                        "l_rl");
                    return new ErrorDetails(feature, "l_rl");
                }
            } else if (isValueIn(feature.getProperty("anlp"), new Object[] { "Steg", "Vt", "Wes" }, false)) {
                final Object rl = feature.getProperty("l_rl");

                if ((rl == null)
                            || !(rl.toString().equals("bs") || rl.toString().equals("re")
                                || rl.toString().equals("li") || rl.toString().equals("nb"))) {
                    showMessage(
                        "Bei anlp = Steg/Vt/Wes darf das Attribut l_rl nur die Werte re/li/bs/nb haben.",
                        "l_rl");
                    return new ErrorDetails(feature, "l_rl");
                }
            }
        }

        return super.prepareForSaveWithDetails(features);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);

        if (isValueIn(
                        feature.getProperty("anlp"),
                        new Object[] { "Albw", "Elbw", "Fu", "Rsk", "Schi", "Slu", "Stt" },
                        false)) {
            final Object rl = feature.getProperty("l_rl");

            if (rl == null) {
                feature.setProperty("l_rl", getCatalogueElement("dlm25w.k_l_rl", "l_rl", "mi"));
            }
        }
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        // todo: nur auf eigenen Basisrouten
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");
        final StationCreationCheck check = new OnOwnRouteStationCheck();

        final StationCreator creator = new StationCreator(
                "ba_st",
                routeMc,
                "Basisgewässer (FG)",
                new LinearReferencingWatergisHelper());
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
