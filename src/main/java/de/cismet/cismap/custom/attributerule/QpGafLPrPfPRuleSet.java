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

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.download.QpDownload;

import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpGafLPrPfPRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(QpGafLPrPfPRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new Varchar(50, false, false));
        typeMap.put("ba_st", new Numeric(10, 2, false, false));
        typeMap.put("la_cd", new Numeric(15, 0, false, false));
        typeMap.put("la_st", new Numeric(10, 2, false, false));
        typeMap.put("l_st", new Catalogue("k_l_st", false, false));
        typeMap.put("re", new Numeric(11, 2, false, false));
        typeMap.put("ho", new Numeric(10, 2, false, false));
        typeMap.put("qp_nr", new Numeric(20, 0, false, false));
        typeMap.put("upl_name", new Varchar(50, false, false));
        typeMap.put("upl_datum", new Varchar(10, false, false));
        typeMap.put("upl_zeit", new Varchar(8, false, false));
        typeMap.put("aufn_name", new Varchar(50, false, false));
        typeMap.put("aufn_datum", new Varchar(10, false, false));
        typeMap.put("aufn_zeit", new Time(false, false));
        typeMap.put("freigabe", new Catalogue("k_freigabe", true, false));
        typeMap.put("titel", new Varchar(250, false, false));
        typeMap.put("beschreib", new Varchar(250, false, false));
        typeMap.put("bemerkung", new Varchar(250, false, false));
        typeMap.put("winkel", new Numeric(5, 1, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return false;
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("qp_nr")) {
            return new LinkTableCellRenderer();
        } else {
            return super.getCellRenderer(columnName);
        }
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[] { "upl_datum", "aufn_datum" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("upl_datum")) {
            return 11;
        } else if (name.equals("aufn_datum")) {
            return 14;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        if (propertyName.equals("upl_datum")) {
            final Object time = feature.getProperty("upl_zeit");
            if (time instanceof Date) {
                final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                return format.format(time);
            }
        } else if (propertyName.equals("aufn_datum")) {
            final Object time = feature.getProperty("aufn_zeit");
            if (time instanceof Date) {
                final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                return format.format(time);
            }
        }

        return null;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        return null; // todo eventuell aendern
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return String.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return null;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("qp_nr")) {
            if ((value instanceof Integer) && (clickCount == 1) && (feature instanceof CidsLayerFeature)) {
                if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
                    final String jobname = DownloadManagerDialog.getJobname();
                    final String filename = value.toString();
                    final String extension = ".pdf";

                    DownloadManager.instance().add(new QpDownload(
                            filename,
                            extension,
                            jobname,
                            (Integer)value,
                            null));
                }
            }
        }
    }

    @Override
    public boolean hasCustomExportFeaturesMethod() {
        return true;
    }

    @Override
    public void exportFeatures() {
        AppBroker.getInstance().getGafExport().actionPerformed(null);
    }

    @Override
    public boolean hasCustomPrintFeaturesMethod() {
        return true;
    }

    @Override
    public void printFeatures() {
        AppBroker.getInstance().getGafPrint().actionPerformed(null);
        ;
    }
}
