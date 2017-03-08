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
package de.cismet.watergis.gui.components;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.method.MethodManager;
import Sirius.navigator.plugin.interfaces.PluginMethod;
import Sirius.navigator.plugin.ui.PluginMenuItem;
import Sirius.navigator.types.treenode.PureTreeNode;
import Sirius.navigator.types.treenode.RootTreeNode;
import Sirius.navigator.ui.ComponentRegistry;
import Sirius.navigator.ui.MutablePopupMenu;
import Sirius.navigator.ui.tree.MetaCatalogueTree;

import org.apache.log4j.Logger;

import java.awt.EventQueue;

import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class RefreshMenuItem extends PluginMenuItem implements PluginMethod {

    //~ Instance fields --------------------------------------------------------

    Logger LOG = Logger.getLogger(RefreshMenuItem.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExploreSubTreeMethod object.
     */
    public RefreshMenuItem() {
        super(MethodManager.NONE);

        this.pluginMethod = this;

        this.setText(org.openide.util.NbBundle.getMessage(
                RefreshMenuItem.class,
                "RefreshMenuItem.RefreshMenuItem.text")); // NOI18N
//        this.setIcon(resources.getIcon("teilbaum_neu_laden.gif")); // NOI18N
        this.setAccelerator(KeyStroke.getKeyStroke("F5")); // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public void invoke() throws Exception {
        final MetaCatalogueTree currentTree = ComponentRegistry.getRegistry().getActiveCatalogue();
        final TreePath selectionPath = currentTree.getSelectionPath();

        if ((selectionPath != null) && (selectionPath.getPath().length > 0)) {
            final RootTreeNode rootTreeNode = new RootTreeNode(SessionManager.getProxy().getRoots());

            try {
                final PureTreeNode treeNode = (PureTreeNode)rootTreeNode.getChildAt(0);
                String childStat = treeNode.getMetaNode().getDynamicChildrenStatement();
                String user = "null";
                if (!AppBroker.getInstance().getOwner().equalsIgnoreCase("administratoren")) {
                    user = "'" + AppBroker.getInstance().getOwner() + "'";
                }
                childStat = childStat.replace("$user", user);
                treeNode.getMetaNode().setDynamicChildrenStatement(childStat);
                rootTreeNode.remove(0);
                rootTreeNode.add(treeNode);
            } catch (Exception e) {
                LOG.error("The problem tree cannot be created", e);
            }

            final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        ((DefaultTreeModel)currentTree.getModel()).setRoot(rootTreeNode);
                        ((DefaultTreeModel)currentTree.getModel()).reload();
                        currentTree.exploreSubtree(selectionPath);
                    }
                };

            if (EventQueue.isDispatchThread()) {
                r.run();
            } else {
                EventQueue.invokeLater(r);
            }
        }
    }
}
