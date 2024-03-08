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
public class AslRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("szenario", new Varchar(100, false, true));
        typeMap.put("plan", new Bool(false, true));
        typeMap.put("ist", new Bool(false, true));
        typeMap.put("qp_id", new Numeric(10, 0, false, true));
        typeMap.put("obsolet", new Bool(false, true));
        typeMap.put("bemerkung", new Varchar(255, false, true));
        typeMap.put("ba_gn", new Varchar(100, false, true));
        typeMap.put("m_traeger", new Catalogue("k_m_traeger", false, true, new Varchar(2, false, false)));
        typeMap.put("abschnitt", new Varchar(100, false, true));
        typeMap.put("jahr", new Varchar(50, false, true));
        typeMap.put("m_software", new Varchar(50, false, true));
        typeMap.put("m_dim", new Varchar(10, false, true));
        typeMap.put("flaeche", new Numeric(12, 0, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("geom") && !columnName.equals("id") && !columnName.equals("flaeche")
                    && !columnName.equals("ba_gn") && !columnName.equals("m_traeger") && !columnName.equals("abschnitt")
                    && !columnName.equals("jahr") && !columnName.equals("m_software") && !columnName.equals("m_dim");
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return super.getCellEditor(columnName);
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
        return new String[] { "flaeche" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("flaeche")) {
            return -2;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Long value = null;

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            value = Math.round(geom.getArea());
        }

        return value;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("flaeche")) {
            return "round(st_area(dlm25w.asl.geom))";
        } else {
            return null;
        }
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Long.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
        c.setMinArea(MIN_AREA_SIZE);

        return c;
    }
}
