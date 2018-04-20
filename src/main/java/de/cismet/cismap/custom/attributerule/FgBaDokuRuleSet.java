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

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Timestamp;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;
import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.DokuLinkTableCellRenderer;
import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBaDokuRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger log = Logger.getLogger(FgBaDokuRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st_von", new Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true));
        typeMap.put("l_rl", new Catalogue("k_l_rl", true, true));
        typeMap.put("name", new Varchar(250, false, true));
        typeMap.put("doc_nr", new Numeric(20, 0, false, false));
        typeMap.put("doc_nr_gu", new Varchar(50, false, true));
        typeMap.put("doc", new Link(250, false, true));
        typeMap.put("aufn_datum", new DateType(false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
        minBaLength = 0.5;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date") && !columnName.equals("id")
                    && !columnName.equals("ww_gr") && !columnName.equals("laenge") && !columnName.equals("geom")
                    && !columnName.equals("doc_nr");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();

        if (column.equals("doc")) {
            if (isLink(newValue)) {
                final File f = new File((String)newValue);

                if (!f.exists()) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Die angegebene Datei existiert nicht");
                    return oldValue;
                }
            }
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ba_st_von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ba_st_bis")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("ww_gr")) {
            final CidsLayerReferencedComboEditor ed = new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true));
            ed.setNullable(false);

            return ed;
        } else if (columnName.equals("l_st")) {
            final CidsLayerFeatureFilter filter = createCidsLayerFeatureFilter("nicht_qp");
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("l_st") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else if (columnName.equals("l_rl")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        columnName,
                        String.valueOf(Types.VARCHAR),
                        true),
                    createCidsLayerFeatureFilter("proj"));
            editor.setNullable(true);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return bean.getProperty("l_rl") + " - " + bean.getProperty("name");
                    }
                });

            return editor;
        } else {
            return super.getCellEditor(columnName);
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("doc")) {
            return new DokuLinkTableCellRenderer(JLabel.RIGHT);
        } else {
            return super.getCellRenderer(columnName);
        }
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();

            if (!isValueEmpty(feature.getProperty("doc"))) {
                if (isLink(feature.getProperty("doc"))) {
                    final File f = new File((String)feature.getProperty("doc"));

                    if (!f.exists()) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die angegebene Datei existiert nicht");
                        return false;
                    }
                }
            }
        }

        return super.prepareForSave(features);
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
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");
        final OnOwnRouteStationCheck check = new OnOwnRouteStationCheck();

        final StationLineCreator creator = new StationLineCreator(
                "ba_st",
                routeMc,
                "Basisgewässer (FG)",
                new LinearReferencingWatergisHelper(),
                0.5f);
        creator.setCheck(check);

        return creator;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("doc")) {
            if ((value instanceof String) && (clickCount == 1)) {
                if (isLink(value)) {
                    try {
                        final File f = new File((String)value);

                        if (f.exists()) {
                            final URL u = new URL("file://" + value);

                            try {
                                de.cismet.tools.BrowserLauncher.openURLorFile(u.toString());
                            } catch (Exception ex) {
                                log.error("Cannot open the url:" + u, ex);
                            }
                        } else {
                            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                "Dokument wurde verschoben, gelöscht oder umbenannt.");
                        }
                    } catch (MalformedURLException ex) {
                        // nothing to do
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isLink(final Object value) {
        if (value instanceof String) {
            final String stringVal = (String)value;
            if (stringVal.length() > 2) {
                final boolean firstCharLetter = Character.isUpperCase(stringVal.charAt(0))
                            || Character.isLowerCase(stringVal.charAt(0));

                return firstCharLetter && stringVal.substring(1, 2).equals(":");
            }
        }

        return false;
    }
}
