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
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.actions.RefreshTemplateAction;
import de.cismet.cids.custom.watergis.server.search.FgBaCdCheck;
import de.cismet.cids.custom.watergis.server.search.RemoveUnnusedRoute;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.CidsLayerReferencedComboEditor;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.SimpleAttributeTableModel;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.cismap.linearreferencing.RouteCombo;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.SplitAction;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FgBakRuleSet extends WatergisDefaultRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Random RANDOM = new Random(new Date().getTime());

    //~ Instance fields --------------------------------------------------------

    private final Logger LOG = Logger.getLogger(FgBakRuleSet.class);
    private TreeSet<FeatureServiceFeature> changedBaCdObjects;

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new Geom(true, false));
        typeMap.put("ww_gr", new Catalogue("k_ww_gr", true, true, new Numeric(4, 0, false, false)));
        // wenn ba_cd == null, dann wird es automatisch gefuellt (in prepareforSave)
        typeMap.put("ba_cd", new Varchar(50, true));
        typeMap.put("bak_st_von", new Numeric(10, 2, false, false));
        typeMap.put("bak_st_bis", new Numeric(10, 2, false, false));
        typeMap.put("ba_gn", new Varchar(50, false));
        typeMap.put("bemerkung", new Varchar(250, false));
        typeMap.put("laenge", new Numeric(10, 2, false, false));
        typeMap.put("fis_g_date", new DateTime(false, false));
        typeMap.put("fis_g_user", new Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return !columnName.equals("fis_g_user") && !columnName.equals("fis_g_date")
                    && !columnName.equals("id") && !columnName.equals("laenge")
                    && !columnName.equals("geom") && !columnName.equals("bak_st_von")
                    && !columnName.equals("bak_st_bis");
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        idOfCurrentlyCheckedFeature = feature.getId();
        if (column.equals("ba_cd")) {
            if (changedBaCdObjects == null) {
                changedBaCdObjects = new TreeSet<FeatureServiceFeature>();
            }
            changedBaCdObjects.add(feature);
            if ((newValue != null) && !newValue.equals("")) {
                final String userGroup = SessionManager.getSession().getUser().getUserGroup().getName();

                if (!oldValue.equals(newValue) && !userGroup.equalsIgnoreCase("administratoren")) {
                    final String prefix = (String)AppBroker.getInstance().getOwnWwGr().getProperty("praefix");

                    if (prefix != null) {
                        return prefix + ":" + newValue;
                    }
                } else if (!oldValue.equals(newValue)) {
                    final String valString = String.valueOf(newValue);

                    if (valString.contains(":") && userGroup.equalsIgnoreCase("administratoren")) {
                        final String prefix = valString.substring(0, valString.indexOf(":"));
                        boolean listFound = false;

                        while (!listFound) {
                            final ComboBoxModel<Object> model = ((CidsLayerFeature)feature).getCatalogueCombo("ww_gr")
                                        .getModel();

                            if (model.getSize() > 1) {
                                listFound = true;

                                for (int i = 0; i < model.getSize(); ++i) {
                                    final Object tmp = model.getElementAt(i);

                                    if (tmp instanceof CidsLayerFeature) {
                                        final CidsLayerFeature wwGrFeature = (CidsLayerFeature)tmp;
                                        final Object prefixTmp = wwGrFeature.getProperty("praefix");

                                        if ((prefixTmp != null) && prefixTmp.equals(prefix)) {
                                            model.setSelectedItem(wwGrFeature);
                                            feature.setProperty("ww_gr", wwGrFeature);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                try {
                                    Thread.sleep(20);
                                } catch (InterruptedException ex) {
                                    // nothing to do
                                }
                            }
                        }
                    }
                }
            }
        }

        return super.afterEdit(feature, column, row, oldValue, newValue);
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
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
        }
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        final Map<Integer, String> baCdMap = new HashMap<Integer, String>();

        for (final FeatureServiceFeature feature : features) {
            idOfCurrentlyCheckedFeature = feature.getId();
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

        return super.prepareForSave(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void setBaCd(final FeatureServiceFeature feature) {
        if (feature instanceof CidsLayerFeature) {
            final CidsLayerFeature cLayerFeature = (CidsLayerFeature)feature;
            final Object o = cLayerFeature.getPropertyObject("ww_gr");

            if (o instanceof CidsLayerFeature) {
                final CidsLayerFeature wwGr = (CidsLayerFeature)o;
                final String baCd = (String)feature.getProperty("ba_cd");
                final String praefix = (String)wwGr.getProperty("praefix");

                if (praefix != null) {
                    if (!baCd.startsWith(praefix + ":")) {
                        feature.setProperty("ba_cd", praefix + ":" + baCd);
                    }
                }
            }
        }
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
        AppBroker.getInstance().getWatergisApp().initRouteCombo();
        RouteCombo.clearRouteCache();
        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak");
        final CidsLayer layer = new CidsLayer(routeMc);
        SplitAction.commit(layer);

        if (model instanceof SimpleAttributeTableModel) {
            final List<FeatureServiceFeature> removedFeatures = ((SimpleAttributeTableModel)model).getRemovedFeature();

            if ((removedFeatures != null) && !removedFeatures.isEmpty()) {
                final List<Feature> selectedFeaturesToRemove = new ArrayList<Feature>();

                for (final FeatureServiceFeature feature : removedFeatures) {
                    try {
                        final CidsServerSearch nodesSearch = new RemoveUnnusedRoute(feature.getId(),
                                RemoveUnnusedRoute.FG_BAK);
                        SessionManager.getProxy()
                                .customServerSearch(SessionManager.getSession().getUser(), nodesSearch);
                    } catch (Exception e) {
                        LOG.error("Error while removing unused stations", e);
                    }

                    final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures();

                    for (final Feature f : selectedFeatures) {
                        if (f instanceof CidsLayerFeature) {
                            final CidsLayerFeature clf = (CidsLayerFeature)f;

                            if ((clf.getProperty("ba_cd") != null) && (feature.getProperty("ba_cd") != null)) {
                                final String selectedFeatureBaCd = String.valueOf(clf.getProperty("ba_cd"));
                                final String deletedFeatureBaCd = String.valueOf(feature.getProperty("ba_cd"));

                                if (selectedFeatureBaCd.equals(deletedFeatureBaCd)) {
                                    selectedFeaturesToRemove.add(f);
                                }
                            }
                        }
                    }
                }

                if (!selectedFeaturesToRemove.isEmpty()) {
                    SelectionManager.getInstance().removeSelectedFeatures(selectedFeaturesToRemove);
                }
            }
        }

        try {
            refreshTemplate(RefreshTemplateAction.RW_SEG_GEOM);
            refreshTemplate(RefreshTemplateAction.EZG_K_RL);
        } catch (ConnectionException ex) {
            LOG.error("Cannot refresh templates", ex);
        }
        super.afterSave(model);

//        final Timer t = new Timer("reload");
//
//        t.schedule(new TimerTask() {
//
//                @Override
//                public void run() {
//                    final TreeMap<Integer, MapService> services = AppBroker.getInstance()
//                                .getMappingComponent()
//                                .getMappingModel()
//                                .getRasterServices();
//
//                    for (final MapService mapService : services.values()) {
//                        if (mapService instanceof CidsLayer) {
//                            final CidsLayer layer = (CidsLayer)mapService;
//
//                            if ((layer.getLayerProperties() == null)
//                                        || (layer.getLayerProperties().getAttributeTableRuleSet() == null)
//                                        || !layer.getLayerProperties().getAttributeTableRuleSet().equals(this)) {
//                                layer.retrieve(true);
//                            }
//                        }
//                    }
//                }
//            }, 1000);
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
            value = round(geom.getLength());
        }
        return value;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        if (propertyName.equals("laenge")) {
            return "round(st_length(geom)::numeric, 2)";
        } else if (propertyName.equals("bak_st_von")) {
            return "0";
        } else if (propertyName.equals("bak_st_bis")) {
            return "round(st_length(geom)::numeric, 2)";
        }

        return null;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return Double.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final PrimitiveGeometryCreator creator = new PrimitiveGeometryCreator(
                CreateGeometryListenerInterface.LINESTRING,
                getDefaultValues());
        creator.setMinLength(MIN_LINE_LENGTH);

        return creator;
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
        final CidsBean wwGr = (CidsBean)feature.getProperty("original:ww_gr");
        final String prefix = String.valueOf(wwGr.getProperty("praefix"));
        String baCd = (String)newFeature.getProperty("ba_cd");

        do {
            // ensure, that the value of ba_cd is unique
            unique = true;
            try {
                final Map<Integer, String> baCdMap = new HashMap<Integer, String>();
                baCd = prefix + ":" + String.valueOf(Math.abs(RANDOM.nextInt()));
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
