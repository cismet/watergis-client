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

import java.sql.Timestamp;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class MnGwPegelRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ms_nr", new Varchar(20, true, true));
        typeMap.put("ms_name", new Varchar(50, true, true));
        typeMap.put("re", new Numeric(11, 2, true, true));
        typeMap.put("ho", new Numeric(10, 2, true, true));
        typeMap.put("baujahr", new Numeric(4, 0, false, true));
        typeMap.put("h_gel", new Numeric(6, 2, false, true));
        typeMap.put("h_mp", new Numeric(6, 2, false, true));
        typeMap.put("fl_von", new Numeric(6, 2, false, true));
        typeMap.put("fl_bis", new Numeric(6, 2, false, true));
        typeMap.put("pn", new Numeric(5, 0, true, true));
        typeMap.put("pn_von", new Varchar(10, false, true));
        typeMap.put("pn_bis", new Varchar(10, false, true));
        typeMap.put("pn_von_h", new Numeric(6, 2, false, true));
        typeMap.put("pn_bis_h", new Numeric(6, 2, false, true));
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
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature f : features) {
            idOfCurrentlyCheckedFeature = f.getId();
            if (isNumberOrNull(f.getProperty("pn")) && (f.getProperty("pn") != null)) {
                if (((Number)f.getProperty("pn")).intValue() == 0) {
                    if ((f.getProperty("pn_von") != null) || (f.getProperty("pn_bis") != null)
                                || (f.getProperty("pn_von_h") != null)
                                || (f.getProperty("pn_bis_h") != null)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute  pn_von / pn_bis / pn_von_h / pn_bis_h müssen NULL sein, wenn pn = 0.");
                        return false;
                    }
                } else if ((f.getProperty("pn") != null) && (((Number)f.getProperty("pn")).intValue() > 0)) {
                    if ((isValueEmpty(f.getProperty("pn_von"))) || (isValueEmpty(f.getProperty("pn_bis")))
                                || (isValueEmpty(f.getProperty("pn_von_h")))
                                || (isValueEmpty(f.getProperty("pn_bis_h")))) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute  pn_von / pn_bis / pn_von_h / pn_bis_h dürfen nicht NULL sein, wenn pn > 0.");
                        return false;
                    }
                }
            }
        }

        return super.prepareForSave(features);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT);
    }
}
