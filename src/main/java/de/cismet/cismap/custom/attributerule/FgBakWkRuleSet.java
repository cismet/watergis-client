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

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.actions.RefreshTemplateAction;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.reports.WkFgReportAction;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBakWkRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FgBakWkRuleSet.class);

    private static final String URL_TEMPLATE =
        "https://fis-wasser-mv.de/charts/steckbriefe/rw/rw_wk.php?schema=reporting_bp3&fg=%1s";
    private static final String URL_TEMPLATE_BB =
        "https://mluk.brandenburg.de/w/Steckbriefe/WRRL2021/RWBODY/DERW_%1s.pdf";
    private static final String URL_TEMPLATE_NS =
        "https://geoportal.bafg.de/birt_viewer/frameset?__report=RW_WKSB_21P1.rptdesign&param_wasserkoerper=DERW_DENI_%1s&agreeToDisclaimer=true";
    private static final String URL_TEMPLATE_SH =
        "https://geoportal.bafg.de/birt_viewer/frameset?__report=RW_WKSB_21P1.rptdesign&param_wasserkoerper=DERW_DESH_%1s&agreeToDisclaimer=true";

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("bak_cd", new Varchar(50, false, false));
        typeMap.put("bak_st_von", new Numeric(10, 2, false, true));
        typeMap.put("bak_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("wk_nr", new Catalogue("k_wk_fg", true, true, new Varchar(50, false, false)));
        typeMap.put("wk_fedfue", new Catalogue("k_wk_fg", false, false, new Varchar(2, false, false)));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("laenge_wk", new Numeric(10, 2, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("laenge") && !columnName.equals("geom")
                    && !columnName.equals("ba_cd") && !columnName.equals("id") && !columnName.equals("laenge_wk")
                    && !columnName.equals("wk_fedfue");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("wk_nr")) {
            return new LinkTableCellRenderer();
        } else {
            return super.getCellRenderer(columnName);
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("bak_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("bak_st_bis")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("wk_nr")) {
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
            refreshTemplate(RefreshTemplateAction.EZG_K_RL);
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
            return -4;
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
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak");
//        final OnOwnRouteStationCheck check = new OnOwnRouteStationCheck();

        final StationLineCreator creator = new StationLineCreator(
                "bak_st",
                routeMc,
                "Basisgewässer/komplett (FG/k)",
                new LinearReferencingWatergisHelper());

//        creator.setCheck(check);

        return creator;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("wk_nr")) {
            if ((value instanceof String) && (clickCount == 1)) {
                String urlTemplate = URL_TEMPLATE;
                final String ff = (String)feature.getProperty("federfuehrung");

                if (ff != null) {
                    if (ff.equals("Brandenburg")) {
                        urlTemplate = URL_TEMPLATE_BB;
                    } else if (ff.equals("Schleswig-Holstein")) {
                        urlTemplate = URL_TEMPLATE_SH;
                    } else if (ff.equals("Niedersachsen")) {
                        urlTemplate = URL_TEMPLATE_NS;
                    }
                }

                try {
                    final URL u = new URL(String.format(urlTemplate, value.toString()));

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

    /**
     * DOCUMENT ME!
     *
     * @param  bacd  DOCUMENT ME!
     */
    public static void createReport(final String bacd) {
        final WkFgReportAction action = new WkFgReportAction();
        action.actionPerformed(null);
    }
}
