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

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.DateCellEditor;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.FormattedTextCellEditor;

import de.cismet.commons.security.WebDavClient;

import de.cismet.netutil.ProxyHandler;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.profile.QpUplDownload;

import de.cismet.watergis.utils.AbstractCidsLayerListCellRenderer;
import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpUplRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(QpUplRuleSet.class);
    private static final String[] ALLOWED_CALC_VALUES = { "calc", "-" };

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", true, true, new Numeric(4, 0, false, false)));
        typeMap.put("l_st", new Catalogue("k_l_st", false, true, new Varchar(10, false, false)));
        typeMap.put("l_bezug", new Catalogue("k_l_bezug", false, true, new Numeric(5, 0, false, false)));
        typeMap.put("h_bezug", new Catalogue("k_h_bezug", false, true, new Numeric(5, 0, false, false)));
        typeMap.put("l_calc", new Varchar(4, false, false, ALLOWED_CALC_VALUES));
        typeMap.put("upl_nr", new Numeric(20, 0, false, false));
        typeMap.put("upl_name", new Varchar(50, true, false));
        typeMap.put("upl_datum", new Varchar(10, true, false));
        typeMap.put("upl_zeit", new Varchar(8, true, false));
        typeMap.put("aufn_name", new Varchar(50, false, false));
        typeMap.put("aufn_datum", new DateType(false, true));
        typeMap.put("aufn_zeit", new Varchar(8, false, false));
        typeMap.put("freigabe", new Catalogue("k_freigabe", false, true, new Varchar(10, false, false)));
        typeMap.put("titel", new Varchar(250, false, false));
        typeMap.put("beschreib", new Varchar(250, false, false));
        typeMap.put("bemerkung", new Varchar(250, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("id") && !columnName.equals("upl_name") && !columnName.equals("upl_datum")
                    && !columnName.equals("upl_zeit") && !columnName.equals("upl_nr") && !columnName.equals("l_bezug")
                    && !columnName.equals("h_bezug") && !columnName.equals("l_calc");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();

        if (!isUploader(feature)) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut "
                        + column
                        + " darf nur von Uploader geändert werden");
            return oldValue;
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public void afterSave(final TableModel model) {
        reloadService("qp_npl");
        reloadService("qp_pkte");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isUploader(final FeatureServiceFeature feature) {
        return SessionManager.getSession().getUser().getUserGroup().getName().equalsIgnoreCase("administratoren")
                    || feature.getProperty("upl_name")
                    .equals(SessionManager.getSession().getUser().getName());
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("upl_nr")) {
            return new LinkTableCellRenderer(JLabel.RIGHT);
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
                            return bean.getProperty("owner").equals(userName);
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
        } else if (columnName.equals("l_st")) {
            final CidsLayerFeatureFilter filter = new CidsLayerFeatureFilter() {

                    @Override
                    public boolean accept(final CidsLayerFeature bean) {
                        if (bean == null) {
                            return true;
                        }

                        return (bean.getProperty("qp") != null)
                                    && (Boolean)bean.getProperty("qp");
                    }
                };

            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        "l_st",
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("l_st");
                    }
                });

            return editor;
        } else if (columnName.equals("l_bezug")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        "l_bezug",
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("l_bezug");
                    }
                });

            return editor;
        } else if (columnName.equals("h_bezug")) {
            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        "h_bezug",
                        String.valueOf(Types.VARCHAR),
                        true));
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("h_bezug");
                    }
                });

            return editor;
        } else if (columnName.equals("freigabe")) {
            final CidsLayerFeatureFilter filter = new CidsLayerFeatureFilter() {

                    @Override
                    public boolean accept(final CidsLayerFeature bean) {
                        if (bean == null) {
                            return true;
                        }

                        return (bean.getProperty("qp") != null)
                                    && (Boolean)bean.getProperty("qp");
                    }
                };

            final CidsLayerReferencedComboEditor editor = new CidsLayerReferencedComboEditor(
                    new FeatureServiceAttribute(
                        "freigabe",
                        String.valueOf(Types.VARCHAR),
                        true),
                    filter);
            editor.setNullable(false);

            editor.setListRenderer(new AbstractCidsLayerListCellRenderer() {

                    @Override
                    protected String toString(final CidsLayerFeature bean) {
                        return (String)bean.getProperty("freigabe");
                    }
                });

            return editor;
        } else if (columnName.equals("aufn_datum")) {
            return new DateCellEditor();
        } else if (columnName.equals("aufn_zeit")) {
            return new FormattedTextCellEditor(new TimeFormatter());
        } else {
            return null;
        }
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return null;
    }

    @Override
    public Map<String, Object> getDefaultValues() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("ww_gr", AppBroker.getInstance().getOwnWwGr());

        return properties;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("upl_nr")) {
            if ((value instanceof Integer) && (clickCount == 1) && (feature instanceof CidsLayerFeature)) {
                if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
                    final String jobname = DownloadManagerDialog.getInstance().getJobName();
                    final List<FeatureServiceFeature> features = new ArrayList<>();
                    features.add(feature);

                    final WebDavClient webDavClient = new WebDavClient(ProxyHandler.getInstance().getProxy(),
                            Photo.WEB_DAV_USER,
                            Photo.WEB_DAV_PASSWORD,
                            true);
                    final File f = new File(DownloadManager.instance().getDestinationDirectory(), jobname);

                    DownloadManager.instance()
                            .add(new QpUplDownload(webDavClient, f.getAbsolutePath(), "Download Profil", features));
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
}
