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

import java.net.MalformedURLException;
import java.net.URL;

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

import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class MnGwChmRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ms_nr", new Varchar(20, true, true));
        typeMap.put("ms_name", new Varchar(50, true, true));
        typeMap.put("re", new Numeric(11, 2, true, true));
        typeMap.put("ho", new Numeric(10, 2, true, true));
        typeMap.put("pn_von", new Varchar(10, false, true));
        typeMap.put("pn_bis", new Varchar(10, false, true));
        typeMap.put("cl", new Numeric(3, 0, true, true));
        typeMap.put("cl_min", new Numeric(12, 6, false, true));
        typeMap.put("cl_mw", new Numeric(12, 6, false, true));
        typeMap.put("cl_max", new Numeric(12, 6, false, true));
        typeMap.put("nh4", new Numeric(3, 0, true, true));
        typeMap.put("nh4_min", new Numeric(12, 6, false, true));
        typeMap.put("nh4_mw", new Numeric(12, 6, false, true));
        typeMap.put("nh4_max", new Numeric(12, 6, false, true));
        typeMap.put("no3", new Numeric(3, 0, true, true));
        typeMap.put("no3_min", new Numeric(12, 6, false, true));
        typeMap.put("no3_mw", new Numeric(12, 6, false, true));
        typeMap.put("no3_max", new Numeric(12, 6, false, true));
        typeMap.put("so4", new Numeric(3, 0, true, true));
        typeMap.put("so4_min", new Numeric(12, 6, false, true));
        typeMap.put("so4_mw", new Numeric(12, 6, false, true));
        typeMap.put("so4_max", new Numeric(12, 6, false, true));
        typeMap.put("ms_aktiv", new BooleanAsInteger(false, true));
        typeMap.put("ue_sw_psm", new BooleanAsInteger(false, true));
        typeMap.put("chart_s1", new Varchar(250, false, false));
        typeMap.put("chart_m1", new Varchar(250, false, false));
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
        if (columnName.equals("chart_s1") || columnName.equals("chart_m1")) {
            return new LinkTableCellRenderer(false);
        } else {
            return super.getCellRenderer(columnName);
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        return prepareForSaveWithDetails(features) == null;
    }

    @Override
    public ErrorDetails prepareForSaveWithDetails(final List<FeatureServiceFeature> features) {
        for (final FeatureServiceFeature f : features) {
            idOfCurrentlyCheckedFeature = f.getId();
            if (isValueEmpty(f.getProperty("ms_nr"))) {
                showMessage("Das Attribut ms_nr darf nicht null sein", "ms_nr");
                return new ErrorDetails(f, "ms_nr");
            }

            if (isValueEmpty(f.getProperty("ms_name"))) {
                showMessage("Das Attribut ms_name darf nicht null sein", "ms_name");
                return new ErrorDetails(f, "ms_name");
            }

            if (isValueEmpty(f.getProperty("re"))) {
                showMessage("Das Attribut re darf nicht null sein", "re");
                return new ErrorDetails(f, "re");
            }

            if (isValueEmpty(f.getProperty("ho"))) {
                showMessage("Das Attribut ho darf nicht null sein", "ho");
                return new ErrorDetails(f, "ho");
            }

            if (isValueEmpty(f.getProperty("cl"))) {
                showMessage("Das Attribut cl darf nicht null sein", "cl");
                return new ErrorDetails(f, "cl");
            }

            if (isValueEmpty(f.getProperty("nh4"))) {
                showMessage("Das Attribut nh4 darf nicht null sein", "nh4");
                return new ErrorDetails(f, "nh4");
            }

            if (isValueEmpty(f.getProperty("no3"))) {
                showMessage("Das Attribut no3 darf nicht null sein", "no3");
                return new ErrorDetails(f, "no3");
            }

            if (isValueEmpty(f.getProperty("so4"))) {
                showMessage("Das Attribut so4 darf nicht null sein", "so4");
                return new ErrorDetails(f, "so4");
            }

            if (isNumberOrNull(f.getProperty("cl"))) {
                if (((Number)f.getProperty("cl")).intValue() == 0) {
                    if ((f.getProperty("cl_min") != null) || (f.getProperty("cl_mw") != null)
                                || (f.getProperty("cl_max") != null)) {
                        showMessage(
                            "Die Attribute cl_min / cl_mw / cl_max müssen NULL sein, wenn cl = 0.",
                            "cl_min / cl_mw / cl_max");
                        if (f.getProperty("cl_min") != null) {
                            return new ErrorDetails(f, "cl_min");
                        } else if (f.getProperty("cl_mw") != null) {
                            return new ErrorDetails(f, "cl_mw");
                        } else {
                            return new ErrorDetails(f, "cl_max");
                        }
                    }
                } else if (((Number)f.getProperty("cl")).intValue() == 1) {
                    if (!(isEqual((Number)f.getProperty("cl_min"), (Number)f.getProperty("cl_mw"))
                                    && isEqual((Number)f.getProperty("cl_mw"), (Number)f.getProperty("cl_max"))
                                    && (f.getProperty("cl_max") != null))) {
                        showMessage(
                            "Die Attribute cl_min / cl_mw / cl_max müssen identisch und ungleich NULL sein, wenn cl = 1.",
                            "cl_min");
                        return new ErrorDetails(f, "cl_min");
                    }
                } else if (((Number)f.getProperty("cl")).intValue() > 1) {
                    if ((isValueEmpty(f.getProperty("cl_min"))) || (isValueEmpty(f.getProperty("cl_mw")))
                                || (isValueEmpty(f.getProperty("cl_max")))) {
                        showMessage(
                            "Die Attribute cl_min / cl_mw / cl_max dürfen nicht NULL sein, wenn cl > 1.",
                            "cl_min / cl_mw / cl_max");
                        if (isValueEmpty(f.getProperty("cl_min"))) {
                            return new ErrorDetails(f, "cl_min");
                        } else if (isValueEmpty(f.getProperty("cl_mw"))) {
                            return new ErrorDetails(f, "cl_mw");
                        } else {
                            return new ErrorDetails(f, "cl_max");
                        }
                    }
                }
            }

            if (isNumberOrNull(f.getProperty("nh4"))) {
                if (((Number)f.getProperty("nh4")).intValue() == 0) {
                    if ((f.getProperty("nh4_min") != null) || (f.getProperty("nh4_mw") != null)
                                || (f.getProperty("nh4_max") != null)) {
                        showMessage(
                            "Die Attribute nh4_min / nh4_mw / nh4_max müssen NULL sein, wenn nh4 = 0.",
                            "nh4_min / nh4_mw / nh4_max");
                        if (f.getProperty("nh4_min") != null) {
                            return new ErrorDetails(f, "nh4_min");
                        } else if (f.getProperty("nh4_mw") != null) {
                            return new ErrorDetails(f, "nh4_mw");
                        } else {
                            return new ErrorDetails(f, "nh4_max");
                        }
                    }
                } else if (((Number)f.getProperty("nh4")).intValue() == 1) {
                    if (!(isEqual((Number)f.getProperty("nh4_min"), (Number)f.getProperty("nh4_mw"))
                                    && isEqual((Number)f.getProperty("nh4_mw"), (Number)f.getProperty("nh4_max"))
                                    && (f.getProperty("nh4_max") != null))) {
                        showMessage(
                            "Die Attribute nh4_min / nh4_mw / nh4_max müssen identisch und ungleich NULL sein, wenn nh4 = 1.",
                            "nh4_min / nh4_mw / nh4_max");
                        return new ErrorDetails(f, "nh4_min");
                    }
                } else if (((Number)f.getProperty("nh4")).intValue() > 1) {
                    if ((isValueEmpty(f.getProperty("nh4_min"))) || (isValueEmpty(f.getProperty("nh4_mw")))
                                || (isValueEmpty(f.getProperty("nh4_max")))) {
                        showMessage(
                            "Die Attribute nh4_min / nh4_mw / nh4_max dürfen nicht NULL sein, wenn nh4 > 1.",
                            "nh4_min / nh4_mw / nh4_max");
                        if (isValueEmpty(f.getProperty("nh4_min"))) {
                            return new ErrorDetails(f, "nh4_min");
                        } else if (isValueEmpty(f.getProperty("nh4_mw"))) {
                            return new ErrorDetails(f, "nh4_mw");
                        } else {
                            return new ErrorDetails(f, "nh4_max");
                        }
                    }
                }
            }

            if (isNumberOrNull(f.getProperty("no3"))) {
                if (((Number)f.getProperty("no3")).intValue() == 0) {
                    if ((f.getProperty("no3_min") != null) || (f.getProperty("no3_mw") != null)
                                || (f.getProperty("no3_max") != null)) {
                        showMessage(
                            "Die Attribute no3_min / no3_mw / no3_max müssen NULL sein, wenn no3 = 0.",
                            "no3_min / no3_mw / no3_max");
                        if (f.getProperty("no3_min") != null) {
                            return new ErrorDetails(f, "no3_min");
                        } else if (f.getProperty("no3_mw") != null) {
                            return new ErrorDetails(f, "no3_mw");
                        } else {
                            return new ErrorDetails(f, "no3_max");
                        }
                    }
                } else if (((Number)f.getProperty("no3")).intValue() == 1) {
                    if (!(isEqual((Number)f.getProperty("no3_min"), (Number)f.getProperty("no3_mw"))
                                    && isEqual((Number)f.getProperty("no3_mw"), (Number)f.getProperty("no3_max"))
                                    && (f.getProperty("no3_max") != null))) {
                        showMessage(
                            "Die Attribute no3_min / no3_mw / no3_max müssen identisch und ungleich NULL sein, wenn no3 = 1.",
                            "no3_min");
                        return new ErrorDetails(f, "no3_min");
                    }
                } else if (((Number)f.getProperty("no3")).intValue() > 1) {
                    if ((isValueEmpty(f.getProperty("no3_min"))) || (isValueEmpty(f.getProperty("no3_mw")))
                                || (isValueEmpty(f.getProperty("no3_max")))) {
                        showMessage(
                            "Die Attribute no3_min / no3_mw / no3_max dürfen nicht NULL sein, wenn no3 > 1.",
                            "no3_min / no3_mw / no3_max");
                        if (isValueEmpty(f.getProperty("no3_min"))) {
                            return new ErrorDetails(f, "no3_min");
                        } else if (isValueEmpty(f.getProperty("no3_mw"))) {
                            return new ErrorDetails(f, "no3_mw");
                        } else {
                            return new ErrorDetails(f, "no3_max");
                        }
                    }
                }
            }

            if (isNumberOrNull(f.getProperty("so4"))) {
                if (((Number)f.getProperty("so4")).intValue() == 0) {
                    if ((f.getProperty("so4_min") != null) || (f.getProperty("so4_mw") != null)
                                || (f.getProperty("so4_max") != null)) {
                        showMessage(
                            "Die Attribute so4_min / so4_mw / so4_max müssen NULL sein, wenn so4 = 0.",
                            "so4_min / so4_mw / so4_max");
                        if (f.getProperty("so4_min") != null) {
                            return new ErrorDetails(f, "so4_min");
                        } else if (f.getProperty("so4_mw") != null) {
                            return new ErrorDetails(f, "so4_mw");
                        } else {
                            return new ErrorDetails(f, "so4_max");
                        }
                    }
                } else if (((Number)f.getProperty("so4")).intValue() == 1) {
                    if (!(isEqual((Number)f.getProperty("so4_min"), (Number)f.getProperty("so4_mw"))
                                    && isEqual((Number)f.getProperty("so4_mw"), (Number)f.getProperty("so4_max"))
                                    && (f.getProperty("so4_max") != null))) {
                        showMessage(
                            "Die Attribute so4_min / so4_mw / so4_max müssen identisch und ungleich NULL sein, wenn pn = 1.",
                            "so4_min / so4_mw / so4_max");
                        return new ErrorDetails(f, "so4_min");
                    }
                } else if (((Number)f.getProperty("so4")).intValue() > 1) {
                    if ((isValueEmpty(f.getProperty("so4_min"))) || (isValueEmpty(f.getProperty("so4_mw")))
                                || (isValueEmpty(f.getProperty("so4_max")))) {
                        showMessage(
                            "Die Attribute so4_min / so4_mw / so4_max dürfen nicht NULL sein, wenn pn > 1.",
                            "so4_min / so4_mw / so4_max");
                        if (isValueEmpty(f.getProperty("so4_min"))) {
                            return new ErrorDetails(f, "so4_min");
                        } else if (isValueEmpty(f.getProperty("so4_mw"))) {
                            return new ErrorDetails(f, "so4_mw");
                        } else {
                            return new ErrorDetails(f, "so4_max");
                        }
                    }
                }
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
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT);
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("chart_s1") || columnName.equals("chart_m1")) {
            if ((value instanceof String) && (clickCount == 1)) {
                try {
                    final URL u = new URL((String)value.toString());

                    try {
                        de.cismet.tools.BrowserLauncher.openURL(u.toString());
                    } catch (Exception ex) {
//                        LOG.error("Cannot open the url:" + u, ex);
                    }
                } catch (MalformedURLException ex) {
                    // nothing to do
                }
            }
        }
    }
}
