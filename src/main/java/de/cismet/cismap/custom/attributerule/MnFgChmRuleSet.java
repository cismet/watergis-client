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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Timestamp;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.PointAndStationCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.cismap.linearreferencing.RouteTableCellEditor;
import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.MIN_AREA_SIZE;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class MnFgChmRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("stalu", new Varchar(10, false, false));
        typeMap.put("wk_k", new Varchar(30, false, false));
        typeMap.put("messstelle", new Varchar(20, false, false));
        typeMap.put("wk_k", new Varchar(100, false, false));
        typeMap.put("lage", new Varchar(100, true, true));
        typeMap.put("lawa_typ", new Numeric(3, 0, true, true));
        typeMap.put("latest_y", new Numeric(4, 0, true, true));
        typeMap.put("rw", new Numeric(11, 2, true, true));
        typeMap.put("hw", new Numeric(10, 2, true, true));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("geom") && !columnName.equals("rw") && !columnName.equals("hw");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
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
        return new String[] { "rw", "hw" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("rw")) {
            return -3;
        } else if (name.equals("hw")) {
            return -3;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            if (propertyName.equals("rw")) {
                return geom.getCoordinates()[0].getOrdinate(Coordinate.X);
            } else if (propertyName.equals("hw")) {
                return geom.getCoordinates()[0].getOrdinate(Coordinate.Y);
            }
        }

        return null;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("rw")) {
            return "round(st_x(geom)::numeric, 2)";
        } else if (propertyName.equals("hw")) {
            return "round(st_y(geom)::numeric, 2)";
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
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT, false);

        return c;
    }
}
