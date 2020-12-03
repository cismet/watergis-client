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
package de.cismet.watergis.gui.actions;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;

import de.cismet.cids.tools.CidsLayerUtil;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerConfig;
import de.cismet.cismap.cidslayer.CidsLayerTreeModel;

import de.cismet.cismap.commons.gui.capabilitywidget.TreeFolder;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CreateViewsForUser extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateViewsForUser.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            final MetaClass[] mc = SessionManager.getProxy().getClasses("dlm25w", ConnectionContext.createDummy());

            for (final MetaClass clazz : mc) {
                final Collection attributes = clazz.getAttributeByName("cidsLayer");
                final Collection hidden = clazz.getAttributeByName("hidden");
                if ((attributes == null) || attributes.isEmpty()
                            || ((hidden != null) && !hidden.isEmpty() && hidden.toArray()[0].toString().equals(
                                    "true"))) {
                    continue;
                }

//                CidsLayer layer = new CidsLayer(clazz);
                final CidsLayerInfo info = CidsLayerUtil.getCidsLayerInfo(clazz, SessionManager.getSession().getUser());
                info.getSelectString();
            }
        } catch (ConnectionException ex) {
            LOG.error("Error while creating cids layer tree", ex);
        }
    }
}
