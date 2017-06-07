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

import java.math.BigDecimal;

import java.sql.Timestamp;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.StationLineCreator;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;

import de.cismet.cismap.linearreferencing.StationTableCellEditor;
import de.cismet.cismap.linearreferencing.TableStationEditor;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinearReferencingWatergisHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SgSuUsgRuleSet extends WatergisDefaultRuleSet {

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("su_cd", new Varchar(50, false, false));
        typeMap.put("su_st_von", new Numeric(10, 2, false, true));
        typeMap.put("su_st_bis", new Numeric(10, 2, false, true));
        typeMap.put("see_gn", new Varchar(50, false, false));
        typeMap.put("see_lawa", new Varchar(20, false, false));
        typeMap.put("see_sp", new Varchar(8, false, false));
        typeMap.put("sgk_id", new Varchar(20, false, true));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("bild_datum", new Varchar(10, false, true));
        typeMap.put("seetyp", new Numeric(2, 0, false, true));
        typeMap.put("trophie", new Varchar(13, false, true));
        typeMap.put("lb_nn", new Numeric(6, 2, false, true));
        typeMap.put("vol_quot", new Numeric(8, 2, false, true));
        typeMap.put("gew_reg", new Numeric(1, 0, false, true));
        typeMap.put("stauhoehe", new Numeric(6, 2, false, true));
        typeMap.put("lg_gew_reg", new Numeric(4, 0, false, true));
        typeMap.put("qbw_auss", new Numeric(1, 0, false, true));
        typeMap.put("wass_str", new Numeric(1, 0, false, true));
        typeMap.put("geol_ufer", new Varchar(17, false, true));
        typeMap.put("br_fwz", new Numeric(6, 2, false, true));
        typeMap.put("br_pswz", new Numeric(6, 2, false, true));
        typeMap.put("kf", new Numeric(3, 0, false, true));
        typeMap.put("kf_b", new Varchar(18, false, true));
        typeMap.put("ku", new Numeric(3, 0, false, true));
        typeMap.put("ku_b", new Varchar(8, false, true));
        typeMap.put("kg", new Numeric(3, 0, false, true));
        typeMap.put("kg_b", new Varchar(10, false, true));
        typeMap.put("uf_ges", new Numeric(3, 0, false, true));
        typeMap.put("uf_kv", new Numeric(3, 0, false, true));
        typeMap.put("uf_gew", new Numeric(3, 0, false, true));
        typeMap.put("uf_schilf", new Numeric(3, 0, false, true));
        typeMap.put("uf_seggen", new Numeric(3, 0, false, true));
        typeMap.put("uf_geh", new Numeric(3, 0, false, true));
        typeMap.put("uf_s", new Numeric(3, 0, false, true));
        typeMap.put("uf_sb", new Varchar(49, false, true));
        typeMap.put("uu_ges", new Numeric(3, 0, false, true));
        typeMap.put("uu_kv", new Numeric(3, 0, false, true));
        typeMap.put("uu_gew", new Numeric(3, 0, false, true));
        typeMap.put("uu_schilf", new Numeric(3, 0, false, true));
        typeMap.put("uu_seggen", new Numeric(3, 0, false, true));
        typeMap.put("uu_kraut", new Numeric(3, 0, false, true));
        typeMap.put("uu_wiese", new Numeric(3, 0, false, true));
        typeMap.put("uu_geh", new Numeric(3, 0, false, true));
        typeMap.put("uu_lw", new Numeric(3, 0, false, true));
        typeMap.put("uu_nw", new Numeric(3, 0, false, true));
        typeMap.put("uu_mw_nw", new Numeric(3, 0, false, true));
        typeMap.put("uu_mw_lw", new Numeric(3, 0, false, true));
        typeMap.put("uu_s", new Numeric(3, 0, false, true));
        typeMap.put("uu_sb", new Varchar(51, false, true));
        typeMap.put("ug_ges", new Numeric(3, 0, false, true));
        typeMap.put("ug_kv", new Numeric(3, 0, false, true));
        typeMap.put("ug_gew", new Numeric(3, 0, false, true));
        typeMap.put("ug_schilf", new Numeric(3, 0, false, true));
        typeMap.put("ug_seggen", new Numeric(3, 0, false, true));
        typeMap.put("ug_kraut", new Numeric(3, 0, false, true));
        typeMap.put("ug_wiese", new Numeric(3, 0, false, true));
        typeMap.put("ug_geh", new Numeric(3, 0, false, true));
        typeMap.put("ug_lw", new Numeric(3, 0, false, true));
        typeMap.put("ug_nw", new Numeric(3, 0, false, true));
        typeMap.put("ug_mw_nw", new Numeric(3, 0, false, true));
        typeMap.put("ug_mw_lw", new Numeric(3, 0, false, true));
        typeMap.put("ug_s", new Numeric(3, 0, false, true));
        typeMap.put("ug_sb", new Varchar(39, false, true));
        typeMap.put("lf_ges", new Numeric(3, 0, false, true));
        typeMap.put("lf_s", new Numeric(3, 0, false, true));
        typeMap.put("lf_sb", new Varchar(10, false, true));
        typeMap.put("lu_ges", new Numeric(3, 0, false, true));
        typeMap.put("lu_intgru", new Numeric(3, 0, false, true));
        typeMap.put("lu_acker", new Numeric(3, 0, false, true));
        typeMap.put("lu_extgru", new Numeric(3, 0, false, true));
        typeMap.put("lu_brache", new Numeric(3, 0, false, true));
        typeMap.put("lu_s", new Numeric(3, 0, false, true));
        typeMap.put("lu_sb", new Varchar(11, false, true));
        typeMap.put("lg_ges", new Numeric(3, 0, false, true));
        typeMap.put("lg_intgru", new Numeric(3, 0, false, true));
        typeMap.put("lg_acker", new Numeric(3, 0, false, true));
        typeMap.put("lg_extgru", new Numeric(3, 0, false, true));
        typeMap.put("lg_brache", new Numeric(3, 0, false, true));
        typeMap.put("lg_s", new Numeric(3, 0, false, true));
        typeMap.put("lg_sb", new Varchar(13, false, true));
        typeMap.put("bf_ges", new Numeric(3, 0, false, true));
        typeMap.put("bf_vv", new Numeric(3, 0, false, true));
        typeMap.put("bf_vt", new Numeric(3, 0, false, true));
        typeMap.put("bf_s", new Numeric(3, 0, false, true));
        typeMap.put("bf_sb", new Varchar(33, false, true));
        typeMap.put("bu_ges", new Numeric(3, 0, false, true));
        typeMap.put("bu_vv", new Numeric(3, 0, false, true));
        typeMap.put("bu_vt", new Numeric(3, 0, false, true));
        typeMap.put("bu_s", new Numeric(3, 0, false, true));
        typeMap.put("bu_sb", new Varchar(33, false, true));
        typeMap.put("bg_ges", new Numeric(3, 0, false, true));
        typeMap.put("bg_vv", new Numeric(3, 0, false, true));
        typeMap.put("bg_vt", new Numeric(3, 0, false, true));
        typeMap.put("bg_s", new Numeric(3, 0, false, true));
        typeMap.put("bg_sb", new Varchar(33, false, true));
        typeMap.put("sf_kein", new Numeric(1, 0, false, true));
        typeMap.put("sf_deponie", new Varchar(5, false, true));
        typeMap.put("sf_muell", new Varchar(17, false, true));
        typeMap.put("sf_aufschu", new Varchar(17, false, true));
        typeMap.put("sf_rohstof", new Varchar(17, false, true));
        typeMap.put("sf_entnahm", new Varchar(16, false, true));
        typeMap.put("sv_bvs", new Varchar(17, false, true));
        typeMap.put("sf_bojen", new Varchar(17, false, true));
        typeMap.put("sf_hafen", new Varchar(17, false, true));
        typeMap.put("sf_fisch", new Varchar(17, false, true));
        typeMap.put("sf_netz", new Varchar(16, false, true));
        typeMap.put("sf_s", new Varchar(10, false, true));
        typeMap.put("sf_sb", new Varchar(78, false, true));
        typeMap.put("su_kein", new Numeric(1, 0, false, true));
        typeMap.put("su_deponie", new Varchar(5, false, true));
        typeMap.put("su_muell", new Varchar(17, false, true));
        typeMap.put("su_aufschu", new Varchar(17, false, true));
        typeMap.put("su_rohstof", new Varchar(17, false, true));
        typeMap.put("su_entwaes", new Varchar(16, false, true));
        typeMap.put("su_entnahm", new Varchar(16, false, true));
        typeMap.put("su_bvs", new Varchar(17, false, true));
        typeMap.put("su_feuer", new Varchar(16, false, true));
        typeMap.put("su_zufahrt", new Varchar(16, false, true));
        typeMap.put("su_bojen", new Varchar(17, false, true));
        typeMap.put("su_hafen", new Varchar(17, false, true));
        typeMap.put("su_s", new Varchar(10, false, true));
        typeMap.put("su_sb", new Varchar(78, false, true));
        typeMap.put("sg_kein", new Numeric(1, 0, false, true));
        typeMap.put("sg_deponie", new Varchar(13, false, true));
        typeMap.put("sg_muell", new Varchar(17, false, true));
        typeMap.put("sg_aufschu", new Varchar(17, false, true));
        typeMap.put("sg_rohstof", new Varchar(17, false, true));
        typeMap.put("sg_entwaes", new Varchar(16, false, true));
        typeMap.put("sg_entnahm", new Varchar(16, false, true));
        typeMap.put("sg_hafen", new Varchar(17, false, true));
        typeMap.put("sg_fischt", new Varchar(26, false, true));
        typeMap.put("sg_s", new Varchar(10, false, true));
        typeMap.put("sg_sb", new Varchar(55, false, true));
        typeMap.put("fp_nk", new Numeric(1, 0, false, true));
        typeMap.put("fp_nkb", new Varchar(24, false, true));
        typeMap.put("fp_bedeck", new Numeric(3, 0, false, true));
        typeMap.put("fb0", new Numeric(3, 0, false, true));
        typeMap.put("fb5", new Numeric(3, 0, false, true));
        typeMap.put("fb525", new Numeric(3, 0, false, true));
        typeMap.put("fb2550", new Numeric(3, 0, false, true));
        typeMap.put("fb50100", new Numeric(3, 0, false, true));
        typeMap.put("fb100_", new Numeric(3, 0, false, true));
        typeMap.put("fb_nk", new Numeric(3, 0, false, true));
        typeMap.put("fb_nkb", new Varchar(46, false, true));
        typeMap.put("fa", new Varchar(50, false, true));
        typeMap.put("fa_nk", new Numeric(1, 0, false, true));
        typeMap.put("fa_nkb", new Varchar(18, false, true));
        typeMap.put("fs_kein", new Numeric(1, 0, false, true));
        typeMap.put("fs_pfade", new Varchar(16, false, true));
        typeMap.put("fs_stoppel", new Varchar(17, false, true));
        typeMap.put("fs_bau", new Varchar(17, false, true));
        typeMap.put("fs_stege", new Varchar(16, false, true));
        typeMap.put("fs_s", new Varchar(10, false, true));
        typeMap.put("fs_sb", new Varchar(70, false, true));
        typeMap.put("fs_nk", new Varchar(10, false, true));
        typeMap.put("fs_nkb", new Varchar(10, false, true));
        typeMap.put("fb_totholz", new Varchar(15, false, true));
        typeMap.put("fb_rasen", new Varchar(18, false, true));
        typeMap.put("fb_steine", new Varchar(15, false, true));
        typeMap.put("fb_s", new Varchar(10, false, true));
        typeMap.put("fb_sb", new Varchar(65, false, true));
        typeMap.put("um_erosion", new Varchar(16, false, true));
        typeMap.put("um_form", new Varchar(15, false, true));
        typeMap.put("um_flach", new Numeric(3, 0, false, true));
        typeMap.put("um_boesch", new Numeric(3, 0, false, true));
        typeMap.put("um_steil", new Numeric(3, 0, false, true));
        typeMap.put("uv_kein", new Numeric(3, 0, false, true));
        typeMap.put("uv_mauer", new Numeric(3, 0, false, true));
        typeMap.put("uv_steine", new Numeric(3, 0, false, true));
        typeMap.put("uv_steinsc", new Numeric(3, 0, false, true));
        typeMap.put("uv_faschin", new Numeric(3, 0, false, true));
        typeMap.put("uv_s", new Numeric(3, 0, false, true));
        typeMap.put("uv_sb", new Varchar(78, false, true));
        typeMap.put("uv_nk", new Numeric(3, 0, false, true));
        typeMap.put("uv_nkb", new Varchar(46, false, true));
        typeMap.put("ub_totholz", new Varchar(15, false, true));
        typeMap.put("ub_strand", new Varchar(18, false, true));
        typeMap.put("ub_kliff", new Varchar(15, false, true));
        typeMap.put("ub_inseln", new Varchar(15, false, true));
        typeMap.put("ub_delta", new Varchar(15, false, true));
        typeMap.put("gg_vorhand", new Numeric(1, 0, false, true));
        typeMap.put("gt0", new Numeric(3, 0, false, true));
        typeMap.put("gt50", new Numeric(3, 0, false, true));
        typeMap.put("gt50100", new Numeric(3, 0, false, true));
        typeMap.put("gt100_", new Numeric(3, 0, false, true));
        typeMap.put("gtb", new Varchar(88, false, true));
        typeMap.put("gr_kein", new Numeric(3, 0, false, true));
        typeMap.put("gr_saum", new Numeric(3, 0, false, true));
        typeMap.put("gr_ausgepr", new Numeric(3, 0, false, true));
        typeMap.put("gr_flaeche", new Numeric(3, 0, false, true));
        typeMap.put("sg_note", new Numeric(1, 0, false, true));
        typeMap.put("sg", new Numeric(3, 0, false, true));
        typeMap.put("fg_note", new Numeric(1, 0, false, true));
        typeMap.put("fg", new Numeric(3, 0, false, true));
        typeMap.put("ug_note", new Numeric(1, 0, false, true));
        typeMap.put("ug", new Numeric(3, 0, false, true));
        typeMap.put("gg_note", new Numeric(1, 0, false, true));
        typeMap.put("gg", new Numeric(3, 0, false, true));
        typeMap.put("fhr_note", new Numeric(1, 0, false, true));
        typeMap.put("fhr", new Numeric(3, 0, false, true));
        typeMap.put("fhf_note", new Numeric(1, 0, false, true));
        typeMap.put("fhf", new Numeric(3, 0, false, true));
        typeMap.put("uhm_note", new Numeric(1, 0, false, true));
        typeMap.put("uhm", new Numeric(3, 0, false, true));
        typeMap.put("uhu_note", new Numeric(1, 0, false, true));
        typeMap.put("uhu", new Numeric(3, 0, false, true));
        typeMap.put("uhf_note", new Numeric(1, 0, false, true));
        typeMap.put("uhf", new Numeric(3, 0, false, true));
        typeMap.put("ghg_note", new Numeric(1, 0, false, true));
        typeMap.put("ghg", new Numeric(3, 0, false, true));
        typeMap.put("ghf_note", new Numeric(1, 0, false, true));
        typeMap.put("ghf", new Numeric(3, 0, false, true));
        typeMap.put("fwz1", new Numeric(2, 0, false, true));
        typeMap.put("fwz2", new Numeric(1, 0, false, true));
        typeMap.put("fwz3", new Numeric(3, 0, false, true));
        typeMap.put("fwz4", new Numeric(2, 0, false, true));
        typeMap.put("fwz5", new Numeric(1, 0, false, true));
        typeMap.put("fwz6", new Numeric(3, 0, false, true));
        typeMap.put("fwz7", new Numeric(3, 0, false, true));
        typeMap.put("ufr1", new Numeric(3, 0, false, true));
        typeMap.put("ufr2", new Numeric(2, 0, false, true));
        typeMap.put("ufr3", new Numeric(2, 0, false, true));
        typeMap.put("ufr4", new Numeric(3, 0, false, true));
        typeMap.put("ufr5", new Numeric(1, 0, false, true));
        typeMap.put("ufr6", new Numeric(3, 0, false, true));
        typeMap.put("ufr7", new Numeric(3, 0, false, true));
        typeMap.put("gwu1", new Varchar(32, false, true));
        typeMap.put("gwu2", new Varchar(67, false, true));
        typeMap.put("gwu3", new Numeric(3, 0, false, true));
        typeMap.put("gwu4", new Numeric(3, 0, false, true));
        typeMap.put("gwu5", new Numeric(2, 0, false, true));
        typeMap.put("wk1", new Numeric(6, 2, false, true));
        typeMap.put("wk2", new Numeric(6, 2, false, true));
        typeMap.put("wk3", new Varchar(82, false, true));
        typeMap.put("wk4", new Numeric(1, 0, false, true));
        typeMap.put("wk5", new Numeric(1, 0, false, true));
        typeMap.put("wk6", new Numeric(1, 0, false, true));
        typeMap.put("wk7", new Numeric(1, 0, false, true));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("laenge") && !columnName.equals("geom") && !columnName.equals("id")
                    && !columnName.equals("see_gn") && !columnName.equals("see_gnsee_lawa")
                    && !columnName.equals("see_sp") && !columnName.equals("su_cd") && !columnName.equals("see_lawa");
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("von")) {
            return new StationTableCellEditor(columnName);
        } else if (columnName.equals("bis")) {
            return new StationTableCellEditor(columnName);
        } else {
            return null;
        }
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        refreshDerivedFields(feature);
        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        refreshDerivedFields(feature);
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void refreshDerivedFields(final FeatureServiceFeature feature) {
        if (feature instanceof CidsLayerFeature) {
            final CidsLayerFeature f = (CidsLayerFeature)feature;
            final TableStationEditor editor = f.getStationEditor("su_st_bis");

            if (editor != null) {
                final CidsBean bean = editor.getCidsBean();

                if (bean != null) {
                    final String seeGn = (String)bean.getProperty("route.see_gn");
                    final String seeSp = (String)bean.getProperty("route.see_sp");
                    final String seeLawa = (String)bean.getProperty("route.see_lawa");

                    feature.setProperty("see_gn", seeGn);
                    feature.setProperty("see_sp", seeSp);
                    feature.setProperty("see_lawa", seeLawa);
                }
            }
        }
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
            return 9;
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
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.sg_su");

        return new StationLineCreator("sg_su_stat", routeMc, "Seeufer (SU)", new LinearReferencingWatergisHelper());
    }
}
