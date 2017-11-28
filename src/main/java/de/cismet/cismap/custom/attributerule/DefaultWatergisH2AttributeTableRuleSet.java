/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.custom.attributerule;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.awt.Component;

import java.lang.reflect.Constructor;

import java.text.DecimalFormat;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LinearReferencingInfo;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.H2AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.WithoutGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.tools.FeatureTools;

import de.cismet.cismap.linearreferencing.tools.StationTableCellEditorInterface;

import de.cismet.watergis.utils.LinkTableCellRenderer;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.minBaLength;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(
    supersedes = "de.cismet.cismap.commons.gui.attributetable.DefaultH2AttributeTableRuleSet",
    service = H2AttributeTableRuleSet.class
)
public class DefaultWatergisH2AttributeTableRuleSet extends DefaultAttributeTableRuleSet
        implements H2AttributeTableRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DefaultWatergisH2AttributeTableRuleSet.class);

    //~ Instance fields --------------------------------------------------------

    protected final Map<String, WatergisDefaultRuleSet.DataType> typeMap =
        new HashMap<String, WatergisDefaultRuleSet.DataType>();

    private List<LinearReferencingInfo> refInfos = null;
    private Map<String, LinearReferencingInfo> refInfoMap = null;
    private String geometryType = null;
    private Map<String, FeatureServiceAttribute> attributesMap = new HashMap<String, FeatureServiceAttribute>();
    private boolean isCheckTable = false;

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new WatergisDefaultRuleSet.Geom(true, false));
        typeMap.put("ww_gr", new WatergisDefaultRuleSet.Catalogue("k_ww_gr", false, false));
        typeMap.put("ba_cd", new WatergisDefaultRuleSet.Varchar(50, false, false));
        typeMap.put("ba_st_von", new WatergisDefaultRuleSet.Numeric(10, 2, false, true));
        typeMap.put("ba_st_bis", new WatergisDefaultRuleSet.Numeric(10, 2, false, true));
        typeMap.put("bak_st_von", new WatergisDefaultRuleSet.Numeric(10, 2, false, true));
        typeMap.put("bak_st_bis", new WatergisDefaultRuleSet.Numeric(10, 2, false, true));
        typeMap.put("obj_nr", new WatergisDefaultRuleSet.Numeric(20, 0, false, false));
        typeMap.put("ausbaujahr", new WatergisDefaultRuleSet.Numeric(4, 0, false, true));
        typeMap.put("bemerkung", new WatergisDefaultRuleSet.Varchar(250, false, true));
        typeMap.put("br_dm_li", new WatergisDefaultRuleSet.Numeric(7, 3, false, true));
        typeMap.put("ho_li", new WatergisDefaultRuleSet.Numeric(7, 3, false, true));
        typeMap.put("br_tr_o_li", new WatergisDefaultRuleSet.Numeric(5, 3, false, true));
        typeMap.put("ho_e", new WatergisDefaultRuleSet.Numeric(6, 2, false, true));
        typeMap.put("ho_a", new WatergisDefaultRuleSet.Numeric(6, 2, false, true));
        typeMap.put("gefaelle", new WatergisDefaultRuleSet.Numeric(6, 2, false, true));
        typeMap.put("ho_d_e", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("ho_d_a", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("ho_d_m", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("laenge", new WatergisDefaultRuleSet.Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new WatergisDefaultRuleSet.DateTime(false, false));
        typeMap.put("fis_g_user", new WatergisDefaultRuleSet.Varchar(50, false, false));
        typeMap.put("ba_gn", new WatergisDefaultRuleSet.Varchar(50, false, false));
        typeMap.put("km_von", new WatergisDefaultRuleSet.Numeric(10, 2, false, true));
        typeMap.put("km_bis", new WatergisDefaultRuleSet.Numeric(10, 2, false, true));
        typeMap.put("nr", new WatergisDefaultRuleSet.Varchar(50, false, true));
        typeMap.put("name", new WatergisDefaultRuleSet.Varchar(50, false, true));
        typeMap.put("berme_w", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("berme_b", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("esw", new WatergisDefaultRuleSet.BooleanAsInteger(false, true));
        typeMap.put("br_f", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("br_k", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("ho_k_f", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("ho_k_pn", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("ho_bhw_pn", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("ho_mw_pn", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("bv_w", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("bv_b", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("br", new WatergisDefaultRuleSet.Numeric(6, 2, false, true));
        typeMap.put("ho_d_o", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("ho_d_u", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("ho_ea", new WatergisDefaultRuleSet.Numeric(6, 2, false, true));
        typeMap.put("ho_d_ea", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("ba_st", new WatergisDefaultRuleSet.Numeric(10, 2, false, true));
        typeMap.put("ho_so", new WatergisDefaultRuleSet.Numeric(6, 2, false, true));
        typeMap.put("ho_d_so_ok", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("sz", new WatergisDefaultRuleSet.Numeric(6, 2, false, true));
        typeMap.put("az", new WatergisDefaultRuleSet.Numeric(6, 2, false, true));
        typeMap.put("ezg_fl", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("v_fl", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("pu", new WatergisDefaultRuleSet.Numeric(1, 0, false, true));
        typeMap.put("pu_foel", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("br_li", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("wbbl", new WatergisDefaultRuleSet.WbblLink(WatergisDefaultRuleSet.getWbblPath(), 10, false, true));
        typeMap.put("bv_re", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("bh_re", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("bl_re", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("bv_li", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("bh_li", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("bl_li", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("mw", new WatergisDefaultRuleSet.Numeric(4, 2, false, true));
        typeMap.put("la_cd", new WatergisDefaultRuleSet.Numeric(15, 0, true, false));
        typeMap.put("la_st_von", new WatergisDefaultRuleSet.Numeric(10, 2, false, false));
        typeMap.put("la_st_bis", new WatergisDefaultRuleSet.Numeric(10, 2, false, false));
        typeMap.put("lak_st_von", new WatergisDefaultRuleSet.Numeric(10, 2, false, false));
        typeMap.put("lak_st_bis", new WatergisDefaultRuleSet.Numeric(10, 2, false, false));
        typeMap.put("la_cd_k", new WatergisDefaultRuleSet.Numeric(20, 0, true, false));
        typeMap.put("la_gn", new WatergisDefaultRuleSet.Varchar(75, true, false));
        typeMap.put("la_gn_t", new WatergisDefaultRuleSet.Numeric(1, 0, true, false));
        typeMap.put("la_lage", new WatergisDefaultRuleSet.Varchar(1, true, false));
        typeMap.put("la_ordn", new WatergisDefaultRuleSet.Numeric(2, 0, true, false));
        typeMap.put("la_wrrl", new WatergisDefaultRuleSet.Numeric(1, 0, true, false));
        typeMap.put("gbk_lawa", new WatergisDefaultRuleSet.Numeric(15, 0, true, true));
        typeMap.put("gbk_lawa_k", new WatergisDefaultRuleSet.Numeric(4, 0, true, true));
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultH2AttributeTableRuleSet object.
     */
    public DefaultWatergisH2AttributeTableRuleSet() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a new H2AttributeTableRuleSet object.
     *
     * @param  refInfos      DOCUMENT ME!
     * @param  geometryType  DOCUMENT ME!
     * @param  attributes    DOCUMENT ME!
     * @param  tableName     DOCUMENT ME!
     */
    @Override
    public void init(final List<LinearReferencingInfo> refInfos,
            final String geometryType,
            final List<FeatureServiceAttribute> attributes,
            final String tableName) {
        this.refInfos = refInfos;
        this.geometryType = geometryType;
        isCheckTable = tableName.startsWith("Prüfungen->");

        if (attributes != null) {
            for (final FeatureServiceAttribute attribute : attributes) {
                attributesMap.put(attribute.getName(), attribute);
            }
        }

        if (refInfos != null) {
            refInfoMap = new HashMap<String, LinearReferencingInfo>();

            for (final LinearReferencingInfo info : refInfos) {
                refInfoMap.put(info.getFromField(), info);

                if (info.getTillField() != null) {
                    refInfoMap.put(info.getTillField(), info);
                }
            }
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        final LinearReferencingInfo refInfo = getInfoForColumn(columnName);

        if (refInfo != null) {
            final Collection<? extends StationTableCellEditorInterface> cellEditor = Lookup.getDefault()
                        .lookupAll(StationTableCellEditorInterface.class);

            if ((cellEditor != null) && (cellEditor.size() > 0)) {
                final StationTableCellEditorInterface editor =
                    cellEditor.toArray(new StationTableCellEditorInterface[1])[0];

                final StationTableCellEditorInterface editorCopy = createNewInstance(editor);

                if (editorCopy != null) {
                    editorCopy.setLinRefInfos(refInfos);
                    editorCopy.setColumnName(columnName);
                    return editorCopy;
                }
            }
        }

        return super.getCellEditor(columnName);
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        if ((refInfos == null) || refInfos.isEmpty()) {
            // primitive type
            if (geometryType.equalsIgnoreCase("Point")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT, false);
            } else if (geometryType.equalsIgnoreCase("MultiPoint")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT, true);
            } else if (geometryType.equalsIgnoreCase("LineString")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.LINESTRING, false);
            } else if (geometryType.equalsIgnoreCase("MultiLineString")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.LINESTRING, true);
            } else if (geometryType.equalsIgnoreCase("Polygon")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, false);
            } else if (geometryType.equalsIgnoreCase("MultiPolygon")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
            } else if (geometryType.equalsIgnoreCase("none")) {
                return new WithoutGeometryCreator();
            }
        }

        return super.getFeatureCreator();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   editor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private StationTableCellEditorInterface createNewInstance(final StationTableCellEditorInterface editor) {
        try {
            final Constructor<? extends StationTableCellEditorInterface> c = editor.getClass().getConstructor();
            return c.newInstance();
        } catch (Exception e) {
            LOG.error("Cannot create a new instance of class " + editor.getClass().getName(), e);
        }

        return null;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        if (isCheckTable) {
            return getSpecialRenderer(columnName);
        }

        final LinearReferencingInfo refInfo = getInfoForColumn(columnName);

        if (refInfo != null) {
            return new DefaultTableCellRenderer() {

                    DecimalFormat format = new DecimalFormat();

                    {
                        format.setGroupingUsed(false);
                        format.setMaximumFractionDigits(2);
                        format.setMinimumFractionDigits(2);
                    }

                    @Override
                    public Component getTableCellRendererComponent(final JTable table,
                            final Object value,
                            final boolean isSelected,
                            final boolean hasFocus,
                            final int row,
                            final int column) {
                        Object val = value;

                        if (value instanceof Number) {
                            val = format.format(value);
                        } else if (value instanceof String) {
                            try {
                                val = Double.parseDouble((String)value);
                                val = format.format(val);
                            } catch (NumberFormatException e) {
                                // should not happen
                                LOG.error("Numeric field does not contain a numeric value", e);
                            }
                        }
                        final Component c = super.getTableCellRendererComponent(
                                table,
                                val,
                                isSelected,
                                hasFocus,
                                row,
                                column);

                        if (c instanceof JLabel) {
                            ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                            ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                        }

                        return c;
                    }
                };
        }

        final FeatureServiceAttribute attr = attributesMap.get(columnName);

        if (attr != null) {
            if (FeatureTools.getH2DataType(attr).equalsIgnoreCase("double")
                        || FeatureTools.getH2DataType(attr).equalsIgnoreCase("integer")
                        || FeatureTools.getH2DataType(attr).equalsIgnoreCase("bigint")
                        || FeatureTools.getH2DataType(attr).equalsIgnoreCase("numeric")) {
                if (columnName.equals("ba_st_von") || columnName.equals("ba_st_bis") || columnName.equals("laenge")
                            || columnName.equals("bak_st_von") || columnName.equals("bak_st_bis")
                            || columnName.equals("lak_st_von") || columnName.equals("lak_st_bis")
                            || columnName.equals("la_st_von") || columnName.equals("la_st_bis")) {
                    return new DefaultTableCellRenderer() {

                            DecimalFormat format = new DecimalFormat();

                            {
                                format.setGroupingUsed(false);
                                format.setMaximumFractionDigits(2);
                                format.setMinimumFractionDigits(2);
                            }

                            @Override
                            public Component getTableCellRendererComponent(final JTable table,
                                    final Object value,
                                    final boolean isSelected,
                                    final boolean hasFocus,
                                    final int row,
                                    final int column) {
                                Component c;
                                if (value != null) {
                                    c = super.getTableCellRendererComponent(
                                            table,
                                            format.format(value).replace('.', ','),
                                            isSelected,
                                            hasFocus,
                                            row,
                                            column);
                                } else {
                                    c = super.getTableCellRendererComponent(
                                            table,
                                            value,
                                            isSelected,
                                            hasFocus,
                                            row,
                                            column);
                                }

                                if (c instanceof JLabel) {
                                    ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                                    ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                                }

                                return c;
                            }
                        };
                } else {
                    return new DefaultTableCellRenderer() {

                            DecimalFormat format = new DecimalFormat();

                            {
                                format.setGroupingUsed(false);
                            }

                            @Override
                            public Component getTableCellRendererComponent(final JTable table,
                                    final Object value,
                                    final boolean isSelected,
                                    final boolean hasFocus,
                                    final int row,
                                    final int column) {
                                Component c;
                                if ((value != null) && (value instanceof Number)) {
                                    c = super.getTableCellRendererComponent(
                                            table,
                                            format.format(value).replace('.', ','),
                                            isSelected,
                                            hasFocus,
                                            row,
                                            column);
                                } else {
                                    c = super.getTableCellRendererComponent(
                                            table,
                                            value,
                                            isSelected,
                                            hasFocus,
                                            row,
                                            column);
                                }

                                if (c instanceof JLabel) {
                                    ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                                    ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                                }

                                return c;
                            }
                        };
                }
            }
        }

        return null;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
        if (columnName.equals("wbbl")) {
            if ((value instanceof String) && (clickCount == 1)) {
                WatergisDefaultRuleSet.downloadDocumentFromWebDav(WatergisDefaultRuleSet.getWbblPath(),
                    WatergisDefaultRuleSet.addExtension(value.toString(), "pdf"));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private TableCellRenderer getSpecialRenderer(final String columnName) {
        final WatergisDefaultRuleSet.DataType type = typeMap.get(columnName);

        if (columnName.equals("id") || columnName.equals("ww_gr")) {
            return new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(final JTable table,
                            final Object value,
                            final boolean isSelected,
                            final boolean hasFocus,
                            final int row,
                            final int column) {
                        final Component c = super.getTableCellRendererComponent(
                                table,
                                value,
                                isSelected,
                                hasFocus,
                                row,
                                column);

                        if (c instanceof JLabel) {
                            ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                            ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                        }

                        return c;
                    }
                };
        }

        if (type != null) {
            if (type instanceof WatergisDefaultRuleSet.Numeric) {
                return new DefaultTableCellRenderer() {

                        DecimalFormat format = new DecimalFormat();
                        DecimalFormat formatWithOutdecimals = new DecimalFormat();

                        {
                            format.setGroupingUsed(false);
                            format.setMaximumFractionDigits(((WatergisDefaultRuleSet.Numeric)type).getScale());
                            format.setMinimumFractionDigits(((WatergisDefaultRuleSet.Numeric)type).getScale());
                            formatWithOutdecimals.setGroupingUsed(false);
                            formatWithOutdecimals.setMaximumFractionDigits(0);
                        }

                        @Override
                        public Component getTableCellRendererComponent(final JTable table,
                                final Object value,
                                final boolean isSelected,
                                final boolean hasFocus,
                                final int row,
                                final int column) {
                            Object val = value;

                            if (value instanceof Number) {
                                val = format.format(value);
                            } else if (value instanceof String) {
                                try {
                                    val = Double.parseDouble((String)value);
                                    val = format.format(val);
                                } catch (NumberFormatException e) {
                                    // should not happen
                                    LOG.error("Numeric field does not contain a numeric value", e);
                                }
                            }

                            if ((val != null) && ((WatergisDefaultRuleSet.Numeric)type).isShowDecimalsOnlyIfExists()) {
                                try {
                                    final double doubleVal = format.parse(val.toString()).doubleValue();
                                    final long longVal = (long)doubleVal;

                                    if (doubleVal == longVal) {
                                        val = formatWithOutdecimals.format(longVal);
                                    }
                                } catch (final Exception e) {
                                    // should not happen
                                    LOG.error("Numeric field does not contain a numeric value", e);
                                }
                            }

                            final Component c = super.getTableCellRendererComponent(
                                    table,
                                    val,
                                    isSelected,
                                    hasFocus,
                                    row,
                                    column);

                            if (c instanceof JLabel) {
                                ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                                ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                            }

                            return c;
                        }
                    };
            }

            if ((type instanceof WatergisDefaultRuleSet.BooleanAsInteger)
                        || ((type instanceof WatergisDefaultRuleSet.Catalogue)
                            && ((WatergisDefaultRuleSet.Catalogue)type).isRightAlignment())) {
                return new DefaultTableCellRenderer() {

                        @Override
                        public Component getTableCellRendererComponent(final JTable table,
                                final Object value,
                                final boolean isSelected,
                                final boolean hasFocus,
                                final int row,
                                final int column) {
                            final Component c = super.getTableCellRendererComponent(
                                    table,
                                    value,
                                    isSelected,
                                    hasFocus,
                                    row,
                                    column);

                            if (c instanceof JLabel) {
                                ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                                ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                            }

                            return c;
                        }
                    };
            }

            if (((type instanceof WatergisDefaultRuleSet.Link)
                            && ((WatergisDefaultRuleSet.Link)type).isRightAlignment())) {
                return new LinkTableCellRenderer(JLabel.RIGHT);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public LinearReferencingInfo getInfoForColumn(final String columnName) {
        if (refInfoMap == null) {
            return null;
        }
        return refInfoMap.get(columnName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public List<LinearReferencingInfo> getAllLinRefInfos() {
        return refInfos;
    }

    @Override
    public H2AttributeTableRuleSet clone() {
        final DefaultWatergisH2AttributeTableRuleSet ruleSet = new DefaultWatergisH2AttributeTableRuleSet();
        ruleSet.refInfos = refInfos;
        ruleSet.refInfoMap = refInfoMap;
        ruleSet.attributesMap = attributesMap;
        ruleSet.geometryType = geometryType;
        ruleSet.isCheckTable = isCheckTable;

        return ruleSet;
    }
}
