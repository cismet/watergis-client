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
public class FgBakGbkRuleSet extends DefaultAttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("laenge") && !columnName.equals("geom") && !columnName.equals("ww_gr")
                    && !columnName.equals("ba_cd") && !columnName.equals("id");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
//        final String[] validLawaCodes = AppBroker.getInstance().getValidLawaCodes();
//        if (((validLawaCodes != null) && column.equals("gbk_lawa")
//                        && (Arrays.binarySearch(validLawaCodes, newValue.toString()) < 0))
//                    || (newValue == null)) {
//            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                "Das Attribut gbk_lawa hat keinen gültigen Wert");
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
        } else if (columnName.equals("gbk_lawa")) {
            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "gbk_lawa",
                        String.valueOf(Types.BIGINT),
                        true),
                    true);
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
//        final String[] validLawaCodes = AppBroker.getInstance().getValidLawaCodes();
//
//        for (final FeatureServiceFeature f : features) {
//            final Object gbk = f.getProperty("gbk_lawa");
//            if ((gbk == null) || (Arrays.binarySearch(validLawaCodes, gbk.toString()) < 0)) {
//                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                    "Das Attribut gbk_lawa hat keinen gültigen Wert");
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
            value = geom.getLength();
        }

        return value;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final Map properties = new HashMap();
        properties.put("gbk_lawa", 0);
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak");

        return new StationLineCreator("bak_st", routeMc, new LinearReferencingWatergisHelper(), 0.5f);
    }
}
