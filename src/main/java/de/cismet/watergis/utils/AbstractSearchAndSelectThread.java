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
package de.cismet.watergis.utils;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;

import java.util.List;

import javax.swing.SwingWorker;

import de.cismet.cids.search.QuerySearch;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceUtilities;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractSearchAndSelectThread extends SwingWorker<List<Feature>, Void> {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Logger LOG = Logger.getLogger(AbstractSearchAndSelectThread.class);

    //~ Instance fields --------------------------------------------------------

    protected final Object layer;
    protected final String query;
    protected final QuerySearch querySearch;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchAndSelectThread object.
     *
     * @param  layer        DOCUMENT ME!
     * @param  query        DOCUMENT ME!
     * @param  querySearch  DOCUMENT ME!
     */
    public AbstractSearchAndSelectThread(final Object layer, final String query, final QuerySearch querySearch) {
        this.layer = layer;
        this.query = query;
        this.querySearch = querySearch;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected List<Feature> doInBackground() throws Exception {
        List<Feature> features = null;

        if (layer instanceof WebFeatureService) {
            final WebFeatureService wfs = (WebFeatureService)layer;
            try {
                final Element e = (Element)wfs.getQueryElement().clone();
                final Element queryElement = e.getChild(
                        "Query",
                        Namespace.getNamespace("wfs", "http://www.opengis.net/wfs"));
                queryElement.removeChild("Filter", Namespace.getNamespace("ogc", "http://www.opengis.net/ogc"));
                final Element filterElement = new Element(
                        "Filter",
                        Namespace.getNamespace("ogc", "http://www.opengis.net/ogc"));
                final SAXBuilder builder = new SAXBuilder();
                final Document d = builder.build(new StringReader(query));
                filterElement.addContent((Element)d.getRootElement().clone());
                queryElement.addContent(0, filterElement);
                features = wfs.getFeatureFactory()
                            .createFeatures(FeatureServiceUtilities.elementToString(e),
                                    getServiceBounds(),
                                    null,
                                    0,
                                    0,
                                    null);
            } catch (Exception ex) {
                LOG.error("Error while retrieving features", ex);
            }
        } else if (layer instanceof AbstractFeatureService) {
            final AbstractFeatureService fs = (AbstractFeatureService)layer;
            features = fs.getFeatureFactory().createFeatures(
                    query,
                    getServiceBounds(),
                    null,
                    0,
                    0,
                    null);
        }

        return features;
    }

    @Override
    protected abstract void done();

    /**
     * DOCUMENT ME!
     *
     * @param   f     DOCUMENT ME!
     * @param   list  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isFeatureInList(final FeatureWithId f, final List<FeatureWithId> list) {
        for (final FeatureWithId tmp : list) {
            if (tmp.getId() == f.getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private XBoundingBox getServiceBounds() {
        final Geometry g = ZoomToLayerWorker.getServiceBounds((RetrievalServiceLayer)layer);
        XBoundingBox bounds = null;

        if (g != null) {
            bounds = new XBoundingBox(g);
            final String crs = CismapBroker.getInstance().getSrs().getCode();

            try {
                final CrsTransformer trans = new CrsTransformer(crs);
                bounds = trans.transformBoundingBox(bounds);
            } catch (Exception e) {
                LOG.error("Error while transforming the bounding box of the service bounds.", e);
            }
        }

        return bounds;
    }
}
