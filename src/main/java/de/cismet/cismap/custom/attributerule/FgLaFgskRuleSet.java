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

import java.io.File;

import java.sql.Timestamp;

import java.util.List;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinearReferencingWatergisHelper;
import de.cismet.watergis.utils.LinkTableCellRenderer;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.checkRange;

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
        typeMap.put("vorkart", new BooleanAsInteger(true, true));
        typeMap.put("sonderfall", new Varchar(10, false, true));
        typeMap.put("seeausfl", new BooleanAsInteger(false, true));
        typeMap.put("wasserf", new Varchar(2, false, true));
        typeMap.put("gu_status", new BooleanAsInteger(false, true));
        typeMap.put("gk_sohle", new Numeric(1, 0, false, true, 0, 5));
        typeMap.put("gk_ufer", new Numeric(1, 0, false, true, 0, 5));
        typeMap.put("gk_land", new Numeric(1, 0, false, true, 0, 5));
        typeMap.put("gk_gesamt", new Numeric(1, 0, false, true, 0, 5));
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

        return super.afterEdit(feature, column, row, oldValue, newValue);
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
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("laenge")) {
            return "st_length(geom)";
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
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_la");
        final OnOwnRouteStationCheck check = new OnOwnRouteStationCheck();

        final StationLineCreator creator = new StationLineCreator(
                "la_st",
                routeMc,
                "Basisgewässer/komplett (FG/k)",
                new LinearReferencingWatergisHelper());
        creator.setCheck(check);

        return creator;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("wk_nr")) {
            if ((value instanceof String) && (clickCount == 1)) {
                downloadDocumentFromWebDav(WK_FG_WEBDAV_PATH, addExtension(value.toString().toUpperCase(), "pdf"));
            }
        }
    }

//    /**
//     * DOCUMENT ME!
//     *
//     * @param  wkk  DOCUMENT ME!
//     */
//    private void createWkFgReport(final String wkk) {
//        if (DownloadManagerDialog.getInstance().showAskingForUserTitleDialog(AppBroker.getInstance().getRootWindow())) {
//            final String jobname = DownloadManagerDialog.getInstance().getJobName();
//            String dir;
//
//            if ((jobname != null) && !jobname.equals("")) {
//                final File path = new File(DownloadManager.instance().getDestinationDirectory().getAbsolutePath(),
//                        DownloadManagerDialog.getInstance().getJobName());
//                dir = path.getAbsolutePath();
//            } else {
//                dir = DownloadManager.instance().getDestinationDirectory().getAbsolutePath();
//            }
//
//            final WkFgDownload download = new WkFgDownload(dir, wkk);
//
//            DownloadManager.instance().add(download);
//        }
//    }
}
