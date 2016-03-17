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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBakGwkRuleSet extends DefaultAttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("geo_field") && !columnName.equals("geom") && !columnName.equals("ba_cd");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
//        final String[] validLawaCodes = AppBroker.getInstance().getValidLawaCodes();
//        if (((validLawaCodes != null) && column.equals("la_cd")
//                        && (Arrays.binarySearch(validLawaCodes, newValue.toString()) < 0))
//                    || (newValue == null)) {
//            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                "Das Attribut la_cd hat keinen gültigen Wert");
//            return oldValue;
//        }

        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return null;
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("bak_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("bak_st_bis")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("la_cd")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.BIGINT),
                        true));

            return editor;
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
//        final String[] validLawaCodes = AppBroker.getInstance().getValidLawaCodes();

//        for (final FeatureServiceFeature f : features) {
//            final Object laCd = f.getProperty("la_cd");
//            if ((laCd == null) || (Arrays.binarySearch(validLawaCodes, laCd.toString()) < 0)) {
//                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                    "Das Attribut la_cd hat keinen gültigen Wert");
//                return false;
//            }
//        }

        return true;
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
    public FeatureCreator getFeatureCreator() {
        final Map properties = new HashMap();
        properties.put("la_cd", 0);
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak");

        final StationLineCreator slc = new StationLineCreator(
                "bak_st",
                routeMc,
                new LinearReferencingWatergisHelper(),
                0.5f);
        slc.setProperties(properties);

        return slc;
    }
}
