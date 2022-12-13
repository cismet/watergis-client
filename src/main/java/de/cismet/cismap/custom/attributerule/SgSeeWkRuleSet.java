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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Timestamp;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.actions.RefreshTemplateAction;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SgSeeWkRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final String URL_TEMPLATE =
        "https://fis-wasser-mv.de/charts/steckbriefe/lw/lw_wk.php?schema=reporting_bp3&sg=%1s";

    //~ Instance fields --------------------------------------------------------

    private final Logger LOG = Logger.getLogger(SgSeeWkRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("wk_nr", new Catalogue("k_wk_sg", true, true, new Varchar(50, false, false)));
        typeMap.put("wk_gn", new Varchar(50, false, true));
        typeMap.put("wk_fedfue", new Catalogue("k_wk_sg", false, false, new Varchar(2, false, false)));
        typeMap.put("gbk_lawa", new Numeric(15, 0, false, true));
        typeMap.put("lake_cat", new Varchar(6, false, true));
        typeMap.put("see_gn", new Varchar(50, true, true));
        typeMap.put("see_lawa", new Varchar(20, true, true));
        typeMap.put("see_sp", new Varchar(8, true, true));
        typeMap.put("tmax", new Numeric(5, 2, false, true));
        typeMap.put("td", new Numeric(5, 2, false, true));
        typeMap.put("vol", new Numeric(10, 0, false, true));
        typeMap.put("tg", new Numeric(6, 3, false, true));
        typeMap.put("ue", new Numeric(6, 3, false, true));
        typeMap.put("ul", new Numeric(7, 3, false, true));
        typeMap.put("leff", new Numeric(6, 3, false, true));
        typeMap.put("beff", new Numeric(6, 3, false, true));
        typeMap.put("tabelle", new Varchar(50, false, true));
        typeMap.put("flaeche", new Numeric(12, 0, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("flaeche") && !columnName.equals("geom") && !columnName.equals("id")
                    && !columnName.equals("wk_fedfue");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (isValueEmpty(newValue)) {
            if (column.equalsIgnoreCase("wk_nr")) {
                showMessage("Das Attribut wk_nr darf nicht leer sein", "wk_nr");
                return oldValue;
            } else if (column.equalsIgnoreCase("see_gn")) {
                showMessage("Das Attribut see_gn darf nicht leer sein", "see_gn");
                return oldValue;
            } else if (column.equalsIgnoreCase("see_lawa")) {
                showMessage("Das Attribut see_lawa darf nicht leer sein", "see_lawa");
                return oldValue;
            } else if (column.equalsIgnoreCase("see_sp")) {
                showMessage("Das Attribut see_sp darf nicht leer sein", "see_sp");
                return oldValue;
            }
        }
        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        switch (columnName) {
            case "wk_nr": {
                return new LinkTableCellRenderer();
            }
            case "tabelle": {
                return new LinkTableCellRenderer();
            }
            default: {
                return super.getCellRenderer(columnName);
            }
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("wk_nr")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("wk_nr");
                    }
                });

            return editor;
        }

        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        return prepareForSaveWithDetails(features) == null;
    }

    @Override
    public ErrorDetails prepareForSaveWithDetails(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();
            if (isValueEmpty(feature.getProperty("wk_nr"))) {
                showMessage("Das Attribut wk_nr darf nicht leer sein", "wk_nr");
                return new ErrorDetails(feature, "wk_nr");
            } else if (isValueEmpty(feature.getProperty("see_gn"))) {
                showMessage("Das Attribut see_gn darf nicht leer sein", "see_gn");
                return new ErrorDetails(feature, "see_gn");
            } else if (isValueEmpty(feature.getProperty("see_lawa"))) {
                showMessage("Das Attribut gbk_lawa darf nicht leer sein", "see_lawa");
                return new ErrorDetails(feature, "see_lawa");
            } else if (isValueEmpty(feature.getProperty("see_sp"))) {
                showMessage("Das Attribut see_sp darf nicht leer sein", "see_sp");
                return new ErrorDetails(feature, "see_sp");
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
        try {
            refreshTemplate(RefreshTemplateAction.RW_SEG_GEOM);
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
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
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
        } else if (columnName.equals("wk_nr")) {
            if ((value instanceof String) && (clickCount == 1)) {
                try {
                    final URL u = new URL(String.format(URL_TEMPLATE, value.toString()));

                    try {
                        de.cismet.tools.BrowserLauncher.openURL(u.toString());
                    } catch (Exception ex) {
                        LOG.error("Cannot open the url:" + u, ex);
                    }
                } catch (MalformedURLException ex) {
                    // nothing to do
                }
            }
        }
    }
}
