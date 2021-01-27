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
package de.cismet.watergis.gui.panels;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingWorker;

import de.cismet.cids.custom.watergis.server.search.RouteEnvelopes;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.concurrency.CismetConcurrency;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class RouteZoomPanel extends ZoomPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(RouteZoomPanel.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RouteZoomPanel object.
     */
    public RouteZoomPanel() {
        super(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba"), "fg_ba", null);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void init() {
        cbObjects.setModel(new DefaultComboBoxModel(new Object[] { "Lade ..." }));

        final SwingWorker sw = new SwingWorker<ZoomPanel.GeometryElement[], Void>() {

                @Override
                protected ZoomPanel.GeometryElement[] doInBackground() throws Exception {
                    if (objectMc != null) {
                        CidsServerSearch search;

                        if (AppBroker.getInstance().getOwner().equalsIgnoreCase("administratoren")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("admin_edit")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("gaeste")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("lung")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("lv_wbv")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("lu")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("uwb_hro")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("uwb_lro")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("uwb_lup")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("uwb_mse")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("uwb_nwm")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("uwb_sn")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("uwb_vg")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("uwb_vr")
                                    || AppBroker.getInstance().getOwner().equalsIgnoreCase("anonymous")) {
                            search = new RouteEnvelopes(null);
                        } else {
                            final String praefixGroup = ((AppBroker.getInstance().getOwnWwGr() != null)
                                    ? (String)AppBroker.getInstance().getOwnWwGr().getProperty("praefixgroup") : null);

                            if (praefixGroup != null) {
                                search = new RouteEnvelopes(" dlm25wPk_ww_gr1.owner = '"
                                                + AppBroker.getInstance().getOwner()
                                                + "' or dlm25wPk_ww_gr1.praefixgroup = '" + praefixGroup + "'");
                            } else {
                                search = new RouteEnvelopes(" dlm25wPk_ww_gr1.owner = '"
                                                + AppBroker.getInstance().getOwner() + "'");
                            }
                        }

                        final List<ZoomPanel.GeometryElement> beans = new ArrayList<ZoomPanel.GeometryElement>();
                        final User user = SessionManager.getSession().getUser();
                        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager
                                    .getProxy().customServerSearch(user, search);

                        if ((attributes != null) && !attributes.isEmpty()) {
                            final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(
                                        PrecisionModel.FLOATING),
                                    CismapBroker.getInstance().getDefaultCrsAlias());
                            final WKBReader wkbReader = new WKBReader(geomFactory);

                            for (final ArrayList f : attributes) {
                                if (f.get(0) instanceof byte[]) {
                                    final Geometry g = wkbReader.read((byte[])f.get(0));
                                    g.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

                                    beans.add(new ZoomPanel.GeometryElement(
                                            (Integer)f.get(2),
                                            (String)f.get(1),
                                            g,
                                            false));
                                }
                            }
                        }

                        return beans.toArray(new ZoomPanel.GeometryElement[beans.size()]);
                    } else {
                        // The user has no read permissions for the route meta class
                        return new ZoomPanel.GeometryElement[0];
                    }
                }

                @Override
                protected void done() {
                    try {
                        final ZoomPanel.GeometryElement[] tmp = get();
                        cbObjects.setModel(new DefaultComboBoxModel(tmp));
                        routeModelInitialised = true;
                    } catch (InterruptedException | ExecutionException e) {
                        LOG.error("Error while initializing the model of the route combobox", e); // NOI18N
                    }
                }
            };

        CismetConcurrency.getInstance("watergis").getDefaultExecutor().execute(sw);
    }
}
