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

import Sirius.navigator.exception.ConnectionException;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.actions.RefreshTemplateAction;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SgDetailRuleSet extends WatergisDefaultRuleSet {

    //~ Instance fields --------------------------------------------------------

    private final Logger LOG = Logger.getLogger(SgDetailRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", true, true, new Numeric(4, 0, false, false)));
        typeMap.put("vl", new Numeric(1, 0, false, true));
        typeMap.put("kg", new Numeric(1, 0, false, true));
        typeMap.put("see_gn", new Varchar(50, false, true));
        typeMap.put("see_gn_t", new Numeric(1, 0, false, true));
        typeMap.put("see_lawa", new Varchar(20, false, true));
        typeMap.put("gbk_lawa", new Numeric(15, 0, false, true));
        typeMap.put("gwk_lawa", new Numeric(15, 0, false, true));
        typeMap.put("see_sp", new Varchar(8, false, true));
        typeMap.put("see_typ", new Varchar(3, false, true));
        typeMap.put("see_art", new Varchar(3, false, true));
        typeMap.put("see_wrrl", new Numeric(1, 0, false, true));
        typeMap.put("see_nhn", new Numeric(6, 2, false, true));
        typeMap.put("see_verm", new Numeric(1, 0, false, true));
        typeMap.put("verm_datum", new Varchar(10, false, true));
        typeMap.put("verm_nhn", new Numeric(6, 2, false, true));
        typeMap.put("bz", new Numeric(2, 0, false, true));
        typeMap.put("tmax", new Numeric(5, 2, false, true));
        typeMap.put("td", new Numeric(5, 2, false, true));
        typeMap.put("vol", new Numeric(10, 0, false, true));
        typeMap.put("tg", new Numeric(6, 3, false, true));
        typeMap.put("ue", new Numeric(6, 3, false, true));
        typeMap.put("ul", new Numeric(7, 3, false, true));
        typeMap.put("leff", new Numeric(6, 3, false, true));
        typeMap.put("beff", new Numeric(6, 3, false, true));
        typeMap.put("tabelle", new Varchar(50, false, true));
        typeMap.put("ezg", new Numeric(1, 0, false, true));
        typeMap.put("ezg_fl", new Numeric(12, 0, false, true));
        typeMap.put("flaeche", new Numeric(12, 0, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("flaeche") && !columnName.equals("geom") && !columnName.equals("id");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("tabelle")) {
            return new LinkTableCellRenderer();
        } else {
            return super.getCellRenderer(columnName);
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ww_gr")) {
            CidsLayerFeatureFilter filter = null;

            if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren")) {
                final String userName = AppBroker.getInstance().getOwner();
                filter = new CidsLayerFeatureFilter() {

                        @Override
                        public boolean accept(final CidsLayerFeature bean) {
                            if (bean == null) {
                                return false;
                            }
                            return bean.getProperty("owner").equals(userName) || bean.getProperty("ww_gr")
                                        .equals(4000);
                        }
                    };
            } else {
                filter = new WwGrAdminFilter();
            }
            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true),
                    filter);
        }
        return null;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
        try {
            refreshTemplate(RefreshTemplateAction.EZG_K_RL);
        } catch (ConnectionException ex) {
            LOG.error("Cannot refresh templates", ex);
        }
        super.afterSave(model);
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[] { "flaeche" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("flaeche")) {
            return -3;
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
            return "round(st_area(geom))";
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
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(
                CreateGeometryListenerInterface.POLYGON,
                getDefaultValues(),
                true);
        c.setMinArea(MIN_AREA_SIZE);

        return c;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("tabelle")) {
            if ((value instanceof String) && (clickCount == 1)) {
                downloadDocumentFromWebDav(getSgLinkTablePath(), addExtension(value.toString(), "xlsx"));
            }
        }
    }

    @Override
    public Map<String, Object> getDefaultValues() {
        final Map properties = new HashMap();
        if ((AppBroker.getInstance().getOwnWwGr() != null)) {
            properties.put("ww_gr", AppBroker.getInstance().getOwnWwGr());
        } else {
            properties.put("ww_gr", AppBroker.getInstance().getNiemandWwGr());
        }

        return properties;
    }
}
