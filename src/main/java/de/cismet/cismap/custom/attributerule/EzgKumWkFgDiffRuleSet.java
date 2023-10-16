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

import com.vividsolutions.jts.geom.Geometry;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class EzgKumWkFgDiffRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("eu_cd_rw", new Varchar(42, true, true));
        typeMap.put("wk_nr", new Varchar(10, true, true));
        typeMap.put("flaeche_diff", new Numeric(12, 0, false, false));
        typeMap.put("anteil", new Numeric(6, 2, false, false));
        typeMap.put("flaeche", new Numeric(12, 0, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("flaeche") && !columnName.equals("geom")
                    && (!columnName.equals("id")
                        & !columnName.equals("anteil"));
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
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
        return new String[] { "flaeche", "anteil" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("flaeche")) {
            return -3;
        } else if (name.equals("anteil")) {
            return -3;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final String propertyName, final FeatureServiceFeature feature) {
        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            if (propertyName.equals("flaeche")) {
                return Math.round(geom.getArea());
            } else if (propertyName.equals("anteil")) {
                final Double diff = ((Double)feature.getProperty("flaeche_diff"));
                return Math.round((geom.getArea() * 100 / (diff + geom.getArea())) * 100) / 100.0;
            }
        }

        return null;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        switch (propertyName) {
            case "flaeche": {
                return "round(st_area(geom))";
            }
            case "anteil": {
                return "round( (st_area(geom) * 100 / (flaeche_diff + anteil) ) * 100) / 100.0";
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        if (index == 4) {
            return Double.class;
        } else {
            return Long.class;
        }
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
        c.setMinArea(MIN_AREA_SIZE);

        return c;
    }
}
