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
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaSchwRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false, new Numeric(4, 0, false, false)));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("name", new Varchar(250, false, false));
        typeMap.put("schw", new Catalogue("k_schw", true, true, new Varchar(10, false, false)));
        typeMap.put("obj_nr", new Numeric(20, 0, false, false));
        typeMap.put("obj_nr_gu", new Varchar(50, false, true));
        typeMap.put("traeger", new Catalogue("k_traeger", false, true, new Varchar(10, false, false)));
        typeMap.put("traeger_gu", new Varchar(50, false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("ausbaujahr", new Numeric(4, 0, false, true));
        typeMap.put("zust_kl", new Catalogue("k_zust_kl", false, true, true, new Numeric(1, 0, false, false)));
        typeMap.put("esw", new BooleanAsInteger(false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("br", new Numeric(6, 2, false, true));
        typeMap.put("sz", new Numeric(6, 2, false, true));
        typeMap.put("az", new Numeric(6, 2, false, true));
        typeMap.put("ezg_fl", new Numeric(6, 2, false, true));
        typeMap.put("v_fl", new Numeric(6, 2, false, true));
        typeMap.put("pu_anz1", new Numeric(1, 0, false, true));
        typeMap.put("pu_typ1", new Varchar(10, false, false));
        typeMap.put("pu_motl1", new Numeric(5, 1, false, true));
        typeMap.put("pu_foel1", new Numeric(7, 1, false, true));
        typeMap.put("pu_anz2", new Numeric(1, 0, false, true));
        typeMap.put("pu_typ2", new Varchar(10, false, false));
        typeMap.put("pu_motl2", new Numeric(5, 1, false, true));
        typeMap.put("pu_foel2", new Numeric(7, 1, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("ww_gr") && !columnName.equals("ba_cd") && !columnName.equals("geom")
                    && !columnName.equals("obj_nr");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (isValueEmpty(newValue)) {
            if (column.equals("schw")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
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

        if (column.equals("br") && !checkRange(column, newValue, 0, 10, 0, 30, true, false, true)) {
            return oldValue;
        }

        if (column.equals("sz") && !checkRange(column, newValue, -6, 179, true, true, true)) {
            return oldValue;
        }

        if (column.equals("az") && !checkRange(column, newValue, -6, 179, true, true, true)) {
            return oldValue;
        }

        if (column.equals("ezg_fl") && !checkRange(column, newValue, 0, 50, 0, 100, true, false, true)) {
            return oldValue;
        }

        if (column.equals("v_fl") && !checkRange(column, newValue, 0, 50, 0, 100, true, false, true)) {
            return oldValue;
        }

        if (column.equals("pu_anz1") && !checkRange(column, newValue, 1, 4, 1, 9, true, true, true)) {
            return oldValue;
        }

        if (column.equals("pu_motl1") && !checkRange(column, newValue, 0, 250, 0, 500, true, false, true)) {
            return oldValue;
        }

        if (column.equals("pu_foel1") && !checkRange(column, newValue, 0, 50, 0, 100, true, false, true)) {
            return oldValue;
        }

        if (column.equals("pu_anz2") && !checkRange(column, newValue, 1, 4, 1, 9, true, true, true)) {
            return oldValue;
        }

        if (column.equals("pu_motl2") && !checkRange(column, newValue, 0, 250, 0, 500, true, false, true)) {
            return oldValue;
        }

        if (column.equals("pu_foel2") && !checkRange(column, newValue, 0, 50, 0, 100, true, false, true)) {
            return oldValue;
        }

        if (column.equals("sz")) {
            if ((newValue != null) && (feature.getProperty("az") != null)) {
                if (((Number)newValue).doubleValue()
                            <= ((Number)feature.getProperty("az")).doubleValue()) {
                    showMessage("Das Attribut sz muss größer als das Attribut az sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("az")) {
            if (((feature.getProperty("sz") != null) && (newValue != null))) {
                if (((Number)feature.getProperty("sz")).doubleValue()
                            <= ((Number)newValue).doubleValue()) {
                    showMessage("Das Attribut sz muss größer als das Attribut az sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("ezg_fl")) {
            if ((newValue != null) && (feature.getProperty("v_fl") != null)) {
                if (((Number)newValue).doubleValue()
                            < ((Number)feature.getProperty("v_fl")).doubleValue()) {
                    showMessage("Das Attribut ezg_fl darf nicht kleiner als das Attribut v_fl sein.");
                    return oldValue;
                }
            }
        }

        if (column.equals("v_fl")) {
            if ((feature.getProperty("ezg_fl") != null) && (newValue != null)) {
                if (((Number)feature.getProperty("ezg_fl")).doubleValue()
                            < ((Number)newValue).doubleValue()) {
                    showMessage("Das Attribut ezg_fl darf nicht kleiner als das Attribut v_fl sein.");
                    return oldValue;
                }
            }
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
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
        } else if (columnName.equals("schw")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("schw") + " - " + bean.getProperty("name");
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
        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();
            if (isValueEmpty(feature.getProperty("schw"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut schw darf nicht leer sein");
                return false;
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
                return false;
            }
            if (!checkRange("br", feature.getProperty("br"), 0, 30, true, false, true)) {
                return false;
            }
            if (!checkRange("ezg_fl", feature.getProperty("ezg_fl"), 0, 100, true, false, true)) {
                return false;
            }
            if (!checkRange("v_fl", feature.getProperty("v_fl"), 0, 100, true, false, true)) {
                return false;
            }
            if (!checkRange("pu_anz1", feature.getProperty("pu_anz1"), 1, 9, true, true, true)) {
                return false;
            }
            if (!checkRange("pu_motl1", feature.getProperty("pu_motl1"), 0, 500, true, false, true)) {
                return false;
            }
            if (!checkRange("pu_foel1", feature.getProperty("pu_foel1"), 0, 100, true, false, true)) {
                return false;
            }

            if (!checkRange("pu_anz2", feature.getProperty("pu_anz2"), 1, 9, true, true, true)) {
                return false;
            }
            if (!checkRange("pu_motl2", feature.getProperty("pu_motl2"), 0, 500, true, false, true)) {
                return false;
            }
            if (!checkRange("pu_foel2", feature.getProperty("pu_foel2"), 0, 100, true, false, true)) {
                return false;
            }

            if ((feature.getProperty("sz") != null) && (feature.getProperty("az") != null)) {
                if (((Number)feature.getProperty("sz")).doubleValue()
                            <= ((Number)feature.getProperty("az")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut sz muss größer als das Attribut az sein.");
                    return false;
                }
            }

            if ((feature.getProperty("ezg_fl") != null) && (feature.getProperty("v_fl") != null)) {
                if (((Number)feature.getProperty("ezg_fl")).doubleValue()
                            < ((Number)feature.getProperty("v_fl")).doubleValue()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut ezg_fl darf nicht kleiner als das Attribut v_fl sein.");
                    return false;
                }
            }
        }

        return super.prepareForSave(features);
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
