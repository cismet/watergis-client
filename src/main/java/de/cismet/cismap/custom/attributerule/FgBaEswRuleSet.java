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

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
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
 * Not used.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaEswRuleSet extends WatergisDefaultRuleSet {

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
            if (column.equals("eswa") || column.equals("lage")) {
                showMessage("Das Attribut "
                            + column
                            + " darf nicht leer sein", column);
                return oldValue;
            }
        }

        if (column.equals("ausbaujahr") && !checkRange(column, newValue, 1950, 2100, true, true, true)) {
            return oldValue;
        }

        return newValue;
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
        } else if (columnName.equals("lage")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("l_rl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("eswa")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("eswa") + " - " + bean.getProperty("name");
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
            if (isValueEmpty(feature.getProperty("eswa"))) {
                showMessage("Das Attribut eswa darf nicht leer sein", "eswa");
                return new ErrorDetails(feature, "eswa");
            }
            if (isValueEmpty(feature.getProperty("lage"))) {
                showMessage("Das Attribut lage darf nicht leer sein", "lage");
                return new ErrorDetails(feature, "lage");
            }
            if (!checkRange("ausbaujahr", feature.getProperty("ausbaujahr"), 1950, 2100, true, true, true)) {
                return new ErrorDetails(feature, "ausbaujahr");
            }

            if (((feature.getProperty("eswa") != null) && (feature.getProperty("eswa").toString().equals("VT")))
                        || feature.getProperty("eswa").toString().equals("Wes")) {
                if (!checkRange("br", feature.getProperty("br"), 0, 30, true, false, true)) {
                    return new ErrorDetails(feature, "br");
                }
            } else if ((feature.getProperty("eswa") != null) && feature.getProperty("eswa").toString().equals("Z")) {
                if (!checkRange("br", feature.getProperty("br"), 0, 500, true, false, true)) {
                    return new ErrorDetails(feature, "br");
                }
            } else {
                if (!checkRange("br", feature.getProperty("br"), 0, 10, true, false, true)) {
                    return new ErrorDetails(feature, "br");
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
