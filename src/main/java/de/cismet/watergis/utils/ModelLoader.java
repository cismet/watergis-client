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

import Sirius.navigator.tools.MetaObjectCache;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.WatergisApp;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ModelLoader extends SwingWorker<CidsBean[], Void> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ModelLoader.class);

    //~ Instance fields --------------------------------------------------------

    private final String catalogueName;
    private final JComboBox cBox;
    private final String criterium;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ModelLoader object.
     *
     * @param  catalogueName  DOCUMENT ME!
     * @param  cBox           DOCUMENT ME!
     * @param  criterium      DOCUMENT ME!
     */
    public ModelLoader(final String catalogueName, final JComboBox cBox, final String criterium) {
        this.catalogueName = catalogueName;
        this.cBox = cBox;
        this.criterium = criterium;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected CidsBean[] doInBackground() throws Exception {
        final MetaClass lstMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w." + catalogueName);

        if (lstMc != null) {
            final List<CidsBean> beans = new ArrayList<CidsBean>();
            final String queryRk = "select " + lstMc.getID() + ", " + lstMc.getPrimaryKey() + " from "
                        + lstMc.getTableName() + " where " + criterium; // NOI18N
            final MetaObject[] mos = MetaObjectCache.getInstance()
                        .getMetaObjectsByQuery(queryRk, AppBroker.DOMAIN_NAME);

            if ((mos != null)) {
                for (final MetaObject mo : mos) {
                    beans.add(mo.getBean());
                }
            }

            return beans.toArray(new CidsBean[beans.size()]);
        } else {
            return new CidsBean[0];
        }
    }

    @Override
    protected void done() {
        try {
            cBox.setModel(new DefaultComboBoxModel(get()));
        } catch (Exception e) {
            LOG.error("Error while initializing the model of the catalogue " + catalogueName, e); // NOI18N
        }
    }
}
