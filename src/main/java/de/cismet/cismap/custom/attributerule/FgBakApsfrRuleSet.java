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
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.sql.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.actions.RefreshTemplateAction;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo;
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
public class FgBakApsfrRuleSet extends WatergisDefaultRuleSet {

    //~ Instance fields --------------------------------------------------------

    private final Logger LOG = Logger.getLogger(FgBakApsfrRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ba_cd", new Varchar(50, true, false));
        typeMap.put("bak_st_von", new Numeric(10, 2, true, false));
        typeMap.put("bak_st_bis", new Numeric(10, 2, true, false));
        typeMap.put("apsfr_cd", new Catalogue("k_apsfr", true, true, true, new Varchar(50, false, false)));
        typeMap.put("apsfr_cdri", new Catalogue("k_apsfr", true, true, true, new Varchar(50, false, false)));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FgBakGwkRuleSet object.
     */
    public FgBakApsfrRuleSet() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("geo_field") && !columnName.equals("geom") && !columnName.equals("ba_cd")
                    && !columnName.equals("laenge");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("bak_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("bak_st_bis")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("apsfr_cd")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));

            return editor;
        } else if (columnName.equals("apsfr_cdri")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));

            return editor;
        } else {
            return null;
        }
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
        try {
            refreshTemplate(RefreshTemplateAction.RW_SEG_GEOM);
        } catch (ConnectionException ex) {
            LOG.error("Cannot refresh templates", ex);
        }
        super.afterSave(model);
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
            value = round(geom.getLength());
        }
        return value;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("laenge")) {
            return "round(st_length(geom)::numeric, 2)";
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
        final Map properties = new HashMap();
//        properties.put("la_cd", 0);
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak");
        final OnOwnRouteStationCheck check = new OnOwnRouteStationCheck();

        final StationLineCreator slc = new StationLineCreator(
                "bak_st",
                routeMc,
                "Basisgewässer/komplett (FG/k)",
                new LinearReferencingWatergisHelper(),
                0.5f);
        slc.setProperties(properties);
        slc.setCheck(check);

        return slc;
    }
}
