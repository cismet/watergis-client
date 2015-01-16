/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.gui.actions.selection;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DrawingFeature;
import de.cismet.cismap.commons.features.DrawingSLDStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SelectAllDrawingsAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SelectAllDrawingsAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public SelectAllDrawingsAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SelectAllDrawingsAction.class,
                "SelectAllDrawingsAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-newwindow.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        RemoveSelectionAllTopicsAction.removeSelectionInAllTopics();

        final MappingComponent mc = AppBroker.getInstance().getMappingComponent();
        final FeatureCollection fc = mc.getFeatureCollection();
        final List<Feature> toBeSelected = new ArrayList<Feature>();

        for (final Feature feature : fc.getAllFeatures()) {
            if (feature instanceof DrawingSLDStyledFeature) {
                final PFeature pFeature = mc.getPFeatureHM().get(feature);

                if (!pFeature.isSelected()) {
                    pFeature.setSelected(true);
                    final SelectionListener sl = (SelectionListener)CismapBroker.getInstance().getMappingComponent()
                                .getInputEventListener()
                                .get(MappingComponent.SELECT);
                    sl.addSelectedFeature(pFeature);
                    toBeSelected.add(feature);
                }
            }
        }

        ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection())
                .addToSelection(toBeSelected);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
