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
package de.cismet.watergis.gui.actions.map;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class CopyObjectAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CopyObjectAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public CopyObjectAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                CopyObjectAction.class,
                "CopyObjectAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(CopyObjectAction.class,
                "CopyObjectAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                CopyObjectAction.class,
                "CopyObjectAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-copy.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final ThemeLayerWidget tree = (ThemeLayerWidget)AppBroker.getInstance().getComponent(ComponentName.TREE);
        final TreePath[] tps = tree.getSelectionPath();

        if ((tps != null) && (tps.length == 1)) {
            final AbstractFeatureService service = (AbstractFeatureService)tps[0].getLastPathComponent();
            final List<Feature> features = SelectionManager.getInstance().getSelectedFeatures(service);
            final List<FeatureServiceFeature> featureServiceFeatures = new ArrayList<FeatureServiceFeature>();

            for (final Feature f : features) {
                if (f instanceof FeatureServiceFeature) {
                    featureServiceFeatures.add((FeatureServiceFeature)f);
                }
            }

            AttributeTable.copySelectedFeaturesToClipboard(featureServiceFeatures);
            AppBroker.getInstance().getWatergisApp().topicTreeSelectionChanged(null);
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
