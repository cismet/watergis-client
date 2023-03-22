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

import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.MIN_AREA_SIZE;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Alkis42000RuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("oid", new Varchar(16, false, true));
        typeMap.put("objart", new Numeric(5, 0, false, true));
        typeMap.put("zus", new Varchar(4, false, true));
        typeMap.put("fkt", new Varchar(4, false, true));
        typeMap.put("bkt", new Varchar(4, false, true));
        typeMap.put("art", new Varchar(4, false, true));
        typeMap.put("flaeche", new Numeric(12, 0, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("geom") && !columnName.equals("id");
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            final Long flaeche = Math.round(geom.getArea());
            feature.getProperties().put("flaeche", flaeche);
        }
    }

    @Override
    public void afterSave(final TableModel model) {
    }

//    @Override
//    public String[] getAdditionalFieldNames() {
//        return new String[] { "flaeche" };
//    }
//
//    @Override
//    public int getIndexOfAdditionalFieldName(final String name) {
//        if (name.equals("flaeche")) {
//            return -1;
//        } else {
//            return super.getIndexOfAdditionalFieldName(name);
//        }
//    }
//
//    @Override
//    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
//        Long value = null;
//
//        final Geometry geom = ((Geometry)feature.getProperty("geom"));
//
//        if (geom != null) {
//            value = Math.round(geom.getArea());
//        }
//
//        return value;
//    }
//
//    @Override
//    public String getAdditionalFieldFormula(final String propertyName) {
//        if (propertyName.equals("flaeche")) {
//            return "round(st_area(geom))";
//        } else {
//            return null;
//        }
//    }
//
//    @Override
//    public Class getAdditionalFieldClass(final int index) {
//        return Long.class;
//    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, false);
        c.setMinArea(MIN_AREA_SIZE);

        return c;
    }
}
