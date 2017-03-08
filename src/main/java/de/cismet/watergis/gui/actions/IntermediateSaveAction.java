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
package de.cismet.watergis.gui.actions;

import org.openide.util.NbBundle;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.WatergisApp;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class IntermediateSaveAction extends AbstractAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DownloadManagerAction object.
     */
    public IntermediateSaveAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-editalt.png");

        if (icon != null) {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(icon));
        }

        putValue(
            SHORT_DESCRIPTION,
            NbBundle.getMessage(IntermediateSaveAction.class,
                "IntermediateSaveAction.toolTipText"));
        putValue(
            MNEMONIC_KEY,
            KeyStroke.getKeyStroke(
                NbBundle.getMessage(IntermediateSaveAction.class,
                    "IntermediateSaveAction.mnemonic")).getKeyCode());
        putValue(NAME, NbBundle.getMessage(IntermediateSaveAction.class,
                "IntermediateSaveAction.text"));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final ActiveLayerModel model = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        final WatergisApp watergis = AppBroker.getInstance().getWatergisApp();
        final TreeMap<Integer, MapService> map = model.getMapServices();

        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    for (final MapService mapService : map.values()) {
                        if (mapService instanceof AbstractFeatureService) {
                            final AbstractFeatureService service = (AbstractFeatureService)mapService;

                            if (watergis.isProcessingModeActive(service)) {
                                watergis.switchProcessingMode(service, true);
                                watergis.switchProcessingMode(service, true);
                            }
                        }
                    }
                }
            };

        EventQueue.invokeLater(r);
    }
}
