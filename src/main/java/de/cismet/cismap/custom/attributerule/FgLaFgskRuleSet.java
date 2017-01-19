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

import Sirius.server.middleware.types.MetaClass;
import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cids.navigator.utils.ClassCacheMultiple;
import de.cismet.cismap.cidslayer.StationLineCreator;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.checkRange;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgLaFgskRuleSet extends WatergisDefaultRuleSet {

    //~ Instance fields --------------------------------------------------------

    private final Logger LOG = Logger.getLogger(FgLaFgskRuleSet.class);
    private TreeSet<FeatureServiceFeature> changedBaCdObjects;

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("la_cd", new Numeric(15, 0, false, false));
        typeMap.put("la_st_von", new Numeric(10, 2, false, true));
        typeMap.put("la_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("fgsk_id", new Varchar(20, true, true));
        typeMap.put("laenge", new Numeric(10, 2, false, true));
        typeMap.put("wk_nr", new Varchar(10, false, true));
        typeMap.put("typ_lawa", new Numeric(2, 0, false, true));
        typeMap.put("vorkart", new Numeric(1, 0, true, true));
        typeMap.put("sonderfall", new Varchar(10, false, true));
        typeMap.put("seeausfl", new Numeric(1, 0, false, true));
        typeMap.put("wasserf", new Varchar(2, false, true));
        typeMap.put("gu_status", new Numeric(1, 0, false, true));
        typeMap.put("gk_sohle", new Numeric(1, 0, false, true));
        typeMap.put("gk_ufer", new Numeric(1, 0, false, true));
        typeMap.put("gk_land", new Numeric(1, 0, false, true));
        typeMap.put("gk_gesamt", new Numeric(1, 0, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("id") && !columnName.equals("laenge") && !columnName.equals("la_cd")
                    && !columnName.equals("geom");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (isValueEmpty(newValue)) {
            if (column.equals("fgsk_id") || column.equals("vorkart")) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
                return oldValue;
            }
        }

        if ((column.equals("gk_sohle") || column.equals("gk_ufer") || column.equals("gk_land")
                        || column.equals("gk_gesamt"))
                    && !checkRange(column, newValue, 0, 5, false, true, true)) {
            return oldValue;
        }

        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("la_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("la_st_bis")) {
            return new StationTableCellEditor(columnName);
        } else {
            return null;
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            if (isValueEmpty(feature.getProperty("fgsk_id"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut fgsk_id darf nicht leer sein");
                return false;
            }
            if (isValueEmpty(feature.getProperty("vorkart"))) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut vorkart darf nicht leer sein");
                return false;
            }
            if (!checkRange("gk_sohle", feature.getProperty("gk_sohle"), 0, 5, false, true, true)) {
                return false;
            }

            if (!checkRange("gk_ufer", feature.getProperty("gk_ufer"), 0, 5, false, true, true)) {
                return false;
            }

            if (!checkRange("gk_land", feature.getProperty("gk_land"), 0, 5, false, true, true)) {
                return false;
            }

            if (!checkRange("gk_gesamt", feature.getProperty("gk_gesamt"), 0, 5, false, true, true)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
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
            return 5;
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
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_la");

        return new StationLineCreator("la_st", routeMc, new LinearReferencingWatergisHelper());
    }
}
