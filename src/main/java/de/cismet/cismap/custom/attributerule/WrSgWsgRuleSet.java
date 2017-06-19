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

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Timestamp;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WrSgWsgRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(WrSgWsgRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("kopplung", new Numeric(4, 0, false, true));
        typeMap.put("zone", new Varchar(3, false, true));
        typeMap.put("wsg_zone", new Varchar(4, false, true));
        typeMap.put("wsg_nr", new Varchar(14, false, true));
        typeMap.put("wsg_name", new Varchar(75, false, true));
        typeMap.put("kreisalt", new Varchar(50, false, true));
        typeMap.put("kreisneu", new Varchar(50, false, true));
        typeMap.put("beschlussn", new Varchar(50, false, true));
        typeMap.put("beschlussd", new Varchar(12, false, true));
        typeMap.put("beschlusn2", new Varchar(50, false, true));
        typeMap.put("beschlusd2", new Varchar(12, false, true));
        typeMap.put("bemerkung", new Varchar(250, false, true));
        typeMap.put("wbbl", new WbblLink(getWbblPath(), 10, false, true));
        typeMap.put("pruef_uwb", new Numeric(1, 0, false, true));
        typeMap.put("pruef_alk", new Numeric(1, 0, false, true));
        typeMap.put("src_erfass", new Varchar(50, false, true));
        typeMap.put("src_quelle", new Varchar(50, false, true));
        typeMap.put("recht", new Link(250, false, true));
        typeMap.put("info", new Link(250, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("geom") && !columnName.equals("id");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (columnName.equals("info") || columnName.equals("recht")) {
            return new LinkTableCellRenderer();
        } else {
            return super.getCellRenderer(columnName);
        }
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("recht") || columnName.equals("info")) {
            if ((value instanceof String) && (clickCount == 1)) {
                try {
                    final URL u = new URL(value.toString());

                    try {
                        de.cismet.tools.BrowserLauncher.openURL(u.toString());
                    } catch (Exception ex) {
                        LOG.error("Cannot open the url:" + u, ex);
                    }
                } catch (MalformedURLException ex) {
                    // nothing to do
                }
            }
        } else if (columnName.equals("wbbl")) {
            if ((value instanceof String) && (clickCount == 1)) {
                downloadDocumentFromWebDav(getWbblPath(), addExtension(value.toString(), "pdf"));
            }
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
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
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
    }
}
