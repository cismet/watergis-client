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
package de.cismet.watergis.gui.actions.selection;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class InvertSelectionAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(InvertSelectionAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public InvertSelectionAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                InvertSelectionAction.class,
                "InvertSelectionAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                InvertSelectionAction.class,
                "InvertSelectionAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                InvertSelectionAction.class,
                "InvertSelectionAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-selection-rectangleselection.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final ActiveLayerModel model = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        final TreeMap<Integer, MapService> map = model.getMapServices();
        final List<Feature> toBeSelected = new ArrayList<Feature>();
        final List<Feature> toBeUnselected = new ArrayList<Feature>();

        for (final MapService mapService : map.values()) {
            if (mapService instanceof AbstractFeatureService) {
                final AbstractFeatureService service = (AbstractFeatureService)mapService;
                if (service.getPNode().getVisible()) {
                    for (final Object featureObject : service.getPNode().getChildrenReference()) {
                        final PFeature feature = (PFeature)featureObject;

                        feature.setSelected(!feature.isSelected());
                        final SelectionListener sl = (SelectionListener)CismapBroker.getInstance().getMappingComponent()
                                    .getInputEventListener()
                                    .get(MappingComponent.SELECT);

                        if (feature.isSelected()) {
                            sl.addSelectedFeature(feature);
                            toBeSelected.add(feature.getFeature());
                        } else {
                            sl.removeSelectedFeature(feature);
                            toBeUnselected.add(feature.getFeature());
                        }
                    }
                }
            }
        }

        ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection())
                .addToSelection(toBeSelected);
        ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent().getFeatureCollection()).unselect(
            toBeUnselected);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
