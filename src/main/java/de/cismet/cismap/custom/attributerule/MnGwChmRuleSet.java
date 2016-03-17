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
public class MnGwChmRuleSet extends WatergisDefaultRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("geom") && !columnName.equals("id");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if ((column.equals("ms_nr") || column.equals("ms_name") || column.equals("re") || column.equals("ho")
                        || column.equals("pn_von") || column.equals("pn_bis") || column.equals("cl")
                        || column.equals("nh4")
                        || column.equals("no3") || column.equals("so4"))
                    && (newValue == null)) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut "
                        + column
                        + " darf nicht null sein");
            return oldValue;
        }

        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return null;
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
        for (final FeatureServiceFeature f : features) {
            if (f.getProperty("ms_nr") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut ms_nr darf nicht null sein");
                return false;
            }

            if (f.getProperty("ms_name") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut ms_name darf nicht null sein");
                return false;
            }

            if (f.getProperty("re") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut re darf nicht null sein");
                return false;
            }

            if (f.getProperty("ho") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut ho darf nicht null sein");
                return false;
            }

            if (f.getProperty("pn_von") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut pn_von darf nicht null sein");
                return false;
            }

            if (f.getProperty("pn_bis") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut pn_bis darf nicht null sein");
                return false;
            }

            if (f.getProperty("cl") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut cl darf nicht null sein");
                return false;
            }

            if (f.getProperty("nh4") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut nh4 darf nicht null sein");
                return false;
            }

            if (f.getProperty("no3") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut no3 darf nicht null sein");
                return false;
            }

            if (f.getProperty("so4") == null) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut so4 darf nicht null sein");
                return false;
            }

            if (isNumberOrNull(f.getProperty("cl"))) {
                if (((Number)f.getProperty("cl")).intValue() == 0) {
                    if ((f.getProperty("cl_min") != null) || (f.getProperty("cl_mw") != null)
                                || (f.getProperty("cl_max") != null)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute cl_min / cl_mw / cl_max müssen NULL sein, wenn cl = 0.");
                        return false;
                    }
                } else if (((Number)f.getProperty("cl")).intValue() == 1) {
                    if (!((f.getProperty("cl_min") == f.getProperty("cl_mw"))
                                    && (f.getProperty("cl_mw") == f.getProperty("cl_max"))
                                    && (f.getProperty("cl_max") != null))) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute cl_min / cl_mw / cl_max müssen identisch und ungleich NULL sein, wenn pn = 1.");
                        return false;
                    }
                } else if (((Number)f.getProperty("cl")).intValue() > 1) {
                    if ((f.getProperty("cl_min") == null) || (f.getProperty("cl_mw") == null)
                                || (f.getProperty("cl_max") == null)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute cl_min / cl_mw / cl_max dürfen nicht NULL sein, wenn pn > 1.");
                        return false;
                    }
                }
            }

            if (isNumberOrNull(f.getProperty("nh4"))) {
                if (((Number)f.getProperty("nh4")).intValue() == 0) {
                    if ((f.getProperty("nh4_min") != null) || (f.getProperty("nh4_mw") != null)
                                || (f.getProperty("nh4_max") != null)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute nh4_min / nh4_mw / nh4_max müssen NULL sein, wenn nh4 = 0.");
                        return false;
                    }
                } else if (((Number)f.getProperty("nh4")).intValue() == 1) {
                    if (!((f.getProperty("nh4_min") == f.getProperty("nh4_mw"))
                                    && (f.getProperty("nh4_mw") == f.getProperty("nh4_max"))
                                    && (f.getProperty("nh4_max") != null))) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute nh4_min / nh4_mw / nh4_max müssen identisch und ungleich NULL sein, wenn pn = 1.");
                        return false;
                    }
                } else if (((Number)f.getProperty("nh4")).intValue() > 1) {
                    if ((f.getProperty("nh4_min") == null) || (f.getProperty("nh4_mw") == null)
                                || (f.getProperty("nh4_max") == null)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute nh4_min / nh4_mw / nh4_max dürfen nicht NULL sein, wenn pn > 1.");
                        return false;
                    }
                }
            }

            if (isNumberOrNull(f.getProperty("no3"))) {
                if (((Number)f.getProperty("no3")).intValue() == 0) {
                    if ((f.getProperty("no3_min") != null) || (f.getProperty("no3_mw") != null)
                                || (f.getProperty("no3_max") != null)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute no3_min / no3_mw / no3_max müssen NULL sein, wenn no3 = 0.");
                        return false;
                    }
                } else if (((Number)f.getProperty("no3")).intValue() == 1) {
                    if (!((f.getProperty("no3_min") == f.getProperty("no3_mw"))
                                    && (f.getProperty("no3_mw") == f.getProperty("no3_max"))
                                    && (f.getProperty("no3_max") != null))) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute no3_min / no3_mw / no3_max müssen identisch und ungleich NULL sein, wenn pn = 1.");
                        return false;
                    }
                } else if (((Number)f.getProperty("no3")).intValue() > 1) {
                    if ((f.getProperty("no3_min") == null) || (f.getProperty("no3_mw") == null)
                                || (f.getProperty("no3_max") == null)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute no3_min / no3_mw / no3_max dürfen nicht NULL sein, wenn pn > 1.");
                        return false;
                    }
                }
            }

            if (isNumberOrNull(f.getProperty("so4"))) {
                if (((Number)f.getProperty("so4")).intValue() == 0) {
                    if ((f.getProperty("so4_min") != null) || (f.getProperty("so4_mw") != null)
                                || (f.getProperty("so4_max") != null)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute so4_min / so4_mw / so4_max müssen NULL sein, wenn so4 = 0.");
                        return false;
                    }
                } else if (((Number)f.getProperty("so4")).intValue() == 1) {
                    if (!((f.getProperty("so4_min") == f.getProperty("so4_mw"))
                                    && (f.getProperty("so4_mw") == f.getProperty("so4_max"))
                                    && (f.getProperty("so4_max") != null))) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute so4_min / so4_mw / so4_max müssen identisch und ungleich NULL sein, wenn pn = 1.");
                        return false;
                    }
                } else if (((Number)f.getProperty("so4")).intValue() > 1) {
                    if ((f.getProperty("so4_min") == null) || (f.getProperty("so4_mw") == null)
                                || (f.getProperty("so4_max") == null)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Attribute so4_min / so4_mw / so4_max dürfen nicht NULL sein, wenn pn > 1.");
                        return false;
                    }
                }
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
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT);
    }
}
