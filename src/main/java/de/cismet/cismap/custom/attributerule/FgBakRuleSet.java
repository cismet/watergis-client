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

import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import org.openide.util.NbBundle;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.search.FgBaCdCheck;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.CidsBeanFilter;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBakRuleSet extends DefaultAttributeTableRuleSet {

    //~ Instance fields --------------------------------------------------------

    private final Logger LOG = Logger.getLogger(FgBakRuleSet.class);
    private TreeSet<FeatureServiceFeature> changedBaCdObjects;

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("id") && !columnName.equals("laenge") && !columnName.equals("ba_cd")
                    && !columnName.equals("geom") && !columnName.equals("bak_st_von")
                    && !columnName.equals("bak_st_von");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        if (column.equals("ba_cd")
                    && ((oldValue == null) || oldValue.equals("")
                        || ((String)oldValue).startsWith(
                            AppBroker.getInstance().getNiemandWwGr().getProperty("praefix")
                            + ":"))) {
            if (changedBaCdObjects == null) {
                changedBaCdObjects = new TreeSet<FeatureServiceFeature>();
            }
            changedBaCdObjects.add(feature);
        }
        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return null;
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        if (columnName.equals("ww_gr")) {
            CidsBeanFilter filter = null;

            if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren")) {
                final String userName = AppBroker.getInstance().getOwner();
                filter = new CidsBeanFilter() {

                        @Override
                        public boolean accept(final CidsBean bean) {
                            if (bean == null) {
                                return false;
                            }
                            return bean.getProperty("owner").equals(userName);
                        }
                    };
            } else {
                filter = new CidsBeanFilter() {

                        @Override
                        public boolean accept(final CidsBean bean) {
                            return bean != null;
                        }
                    };
            }
            return new CidsLayerReferencedComboEditor(new FeatureServiceAttribute(
                        "ww_gr",
                        String.valueOf(Types.INTEGER),
                        true),
                    filter);
        }
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
        final Map<Integer, String> baCdMap = new HashMap<Integer, String>();

        for (final FeatureServiceFeature feature : features) {
            if ((changedBaCdObjects != null) && changedBaCdObjects.contains(feature)) {
                final String baCd = (String)feature.getProperty("ba_cd");

                if (feature.getProperty("ww_gr").equals(AppBroker.getInstance().getNiemandWwGr())) {
                    if ((baCd != null) && !baCd.equals("")) {
                        feature.setProperty("ww_gr", AppBroker.getInstance().getOwnWwGr());
                    }
                }
            }
            if ((feature.getProperty("ba_cd") == null) || feature.getProperty("ba_cd").equals("")) {
                feature.setProperty("ww_gr", AppBroker.getInstance().getNiemandWwGr());
                feature.setProperty("ba_cd", String.valueOf(feature.hashCode()));
            }

            setBaCd(feature);

            baCdMap.put((Integer)feature.getProperty("id"), (String)feature.getProperty("ba_cd"));
        }

        if (!baCdMap.isEmpty()) {
            try {
                final User user = SessionManager.getSession().getUser();
                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(user, new FgBaCdCheck(baCdMap));

                if ((attributes != null) && !attributes.isEmpty()) {
                    final ArrayList list = attributes.get(0);

                    if ((list != null) && !list.isEmpty()) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            NbBundle.getMessage(
                                FgBakRuleSet.class,
                                "FgBakRuleSet.prepareForSave().message",
                                list.get(0)),
                            NbBundle.getMessage(FgBakRuleSet.class, "FgBakRuleSet.prepareForSave().title"),
                            JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
            } catch (final Exception e) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(FgBakRuleSet.class, "FgBakRuleSet.prepareForSave().error.message"),
                    NbBundle.getMessage(FgBakRuleSet.class, "FgBakRuleSet.prepareForSave().error.title"),
                    JOptionPane.ERROR_MESSAGE);
                LOG.error("Error while checking the uniqueness of the ba_cd field.", e);
                return false;
            }
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void setBaCd(final FeatureServiceFeature feature) {
        if (feature instanceof CidsLayerFeature) {
            final CidsLayerFeature cLayerFeature = (CidsLayerFeature)feature;
            final CidsBean wwGr = (CidsBean)cLayerFeature.getPropertyObject("ww_gr");
            final String baCd = (String)feature.getProperty("ba_cd");
            final String praefix = (String)wwGr.getProperty("praefix");

            if (praefix != null) {
                if (!baCd.startsWith(praefix + ":")) {
                    feature.setProperty("ba_cd", praefix + ":" + baCd);
                }
            }
        }
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        feature.getProperties().put("fis_g_date", new Timestamp(System.currentTimeMillis()));
        feature.getProperties().put("fis_g_user", SessionManager.getSession().getUser().getName());
    }

    @Override
    public void afterSave(final TableModel model) {
        final Timer t = new Timer("reload");

        t.schedule(new TimerTask() {

                @Override
                public void run() {
                    final TreeMap<Integer, MapService> services = AppBroker.getInstance()
                                .getMappingComponent()
                                .getMappingModel()
                                .getRasterServices();

                    for (final MapService mapService : services.values()) {
                        if (mapService instanceof CidsLayer) {
                            final CidsLayer layer = (CidsLayer)mapService;

                            if ((layer.getLayerProperties() == null)
                                        || (layer.getLayerProperties().getAttributeTableRuleSet() == null)
                                        || !layer.getLayerProperties().getAttributeTableRuleSet().equals(this)) {
                                layer.retrieve(true);
                            }
                        }
                    }
                }
            }, 1000);
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[] { "bak_st_von", "bak_st_bis", "laenge" };
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        if (name.equals("laenge")) {
            return -3;
        } else if (name.equals("bak_st_von")) {
            return 4;
        } else if (name.equals("bak_st_bis")) {
            return 5;
        } else {
            return super.getIndexOfAdditionalFieldName(name);
        }
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        Double value = null;

        if (propertyName.equals("bak_st_von")) {
            return 0;
        }

        final Geometry geom = ((Geometry)feature.getProperty("geom"));

        if (geom != null) {
            value = geom.getLength();
        }
        return value;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.LINESTRING, getDefaultValues());
    }

    @Override
    public Map<String, Object> getDefaultValues() {
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("ww_gr", AppBroker.getInstance().getOwnWwGr());
        String baCd = String.valueOf(AppBroker.getInstance().getOwnWwGr().getProperty("praefix")) + ":";
        final Random r = new Random(new Date().getTime());

        baCd += r.nextInt();
        properties.put("ba_cd", baCd);

        return properties;
    }

    @Override
    public FeatureServiceFeature cloneFeature(final FeatureServiceFeature feature) {
        boolean unique;
        final FeatureServiceFeature newFeature = super.cloneFeature(feature);
        final CidsBean wwGr = (CidsBean)newFeature.getProperty("original:ww_gr");
        final String prefix = String.valueOf(wwGr.getProperty("praefix"));
        String baCd = (String)newFeature.getProperty("ba_cd");
        if (baCd != null) {
            baCd += "-1";
        } else {
            baCd = prefix + ":" + String.valueOf(Math.abs(feature.hashCode()));
        }

        do {
            // ensure, that the value of ba_cd is unique
            unique = true;
            try {
                final Map<Integer, String> baCdMap = new HashMap<Integer, String>();
                baCdMap.put(-1, baCd);
                final User user = SessionManager.getSession().getUser();
                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(user, new FgBaCdCheck(baCdMap));

                if ((attributes != null) && !attributes.isEmpty()) {
                    unique = false;
                }
            } catch (Exception e) {
                LOG.error("Error checking baCd", e);
            }
        } while (!unique);

        newFeature.setProperty("ba_cd", baCd);

        return newFeature;
    }
}
