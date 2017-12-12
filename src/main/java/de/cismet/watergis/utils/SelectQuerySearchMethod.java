/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.utils;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import de.cismet.cids.search.QuerySearch;
import de.cismet.cids.search.QuerySearchMethod;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.factory.AbstractFeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.commons.concurrency.CismetExecutors;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = QuerySearchMethod.class)
public class SelectQuerySearchMethod implements QuerySearchMethod {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SelectQuerySearchMethod.class);

    //~ Instance fields --------------------------------------------------------

    private QuerySearch querySearch;
    private boolean searching = false;
    private SearchAndSelectThread searchThread;
    private Object lastLayer;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setQuerySearch(final QuerySearch querySearch) {
        this.querySearch = querySearch;
    }

    @Override
    public void actionPerformed(final Object layer, final String query) {
        if (LOG.isInfoEnabled()) {
            LOG.info((searching ? "Cancel" : "Search") + " button was clicked.");
        }

        if (searching) {
            if (searchThread != null) {
                if (lastLayer instanceof AbstractFeatureService) {
                    final FeatureFactory ff = ((AbstractFeatureService)lastLayer).getFeatureFactory();
                    if (ff instanceof AbstractFeatureFactory) {
                        ((AbstractFeatureFactory)ff).waitUntilInterruptedIsAllowed();
                    }
                }
                searchThread.cancel(true);
            }
        } else {
            lastLayer = layer;
            searchThread = new SearchAndSelectThread(layer, query, querySearch);
            CismetExecutors.newSingleThreadExecutor().submit(searchThread);

            searching = true;
            querySearch.setControlsAccordingToState(searching);
        }
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(SelectQuerySearchMethod.class, "SelectQuerySearchMethod.toString");
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class SearchAndSelectThread extends AbstractSearchAndSelectThread {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SearchAndSelectThread object.
         *
         * @param  layer        DOCUMENT ME!
         * @param  query        DOCUMENT ME!
         * @param  querySearch  DOCUMENT ME!
         */
        public SearchAndSelectThread(final Object layer, final String query, final QuerySearch querySearch) {
            super(layer, query, querySearch);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected void done() {
            try {
                final List<Feature> features = get();

                if (isCancelled()) {
                    return;
                }

                if ((features != null) && (layer instanceof AbstractFeatureService)) {
                    SelectionManager.getInstance().setSelectedFeatures(features);
                } else {
                    SelectionManager.getInstance()
                            .removeSelectedFeatures(SelectionManager.getInstance().getSelectedFeatures());
                }
            } catch (ExecutionException e) {
                if (e.getCause() instanceof TooManyResultsException) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        e.getCause().getMessage(),
                        "Hinweis",
                        JOptionPane.WARNING_MESSAGE);
                }
                LOG.error("Error while selecting features", e);
            } catch (Exception e) {
                LOG.error("Error while selecting features", e);
            }
            searching = false;
            querySearch.setControlsAccordingToState(searching);
        }
    }
}
