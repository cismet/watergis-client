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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import org.apache.log4j.Logger;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.creator.GeometryFinishedListener;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.watergis.gui.actions.split.FeatureSplitter;
import de.cismet.watergis.gui.actions.split.FeatureSplitterFactory;

import de.cismet.watergis.utils.SplitGeometryListener;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SplitAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SplitAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public SplitAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SplitAction.class,
                "SplitAction.cmdSplitAction.toolTipText",
                new Object[] { " " });
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-addshape.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final FeatureServiceFeature validFeature = determineValidFeature();
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();

        if (validFeature != null) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final String oldInteractionMode = mc.getInteractionMode();

                        final SplitGeometryListener listener = new SplitGeometryListener(
                                mc,
                                new GeometryFinishedListener() {

                                    @Override
                                    public void geometryFinished(final Geometry g) {
                                        mc.setInteractionMode(oldInteractionMode);
                                        final FeatureSplitterFactory factory = new FeatureSplitterFactory();

                                        final FeatureSplitter splitter = factory.getFeatureMergerForFeature(
                                                validFeature);

                                        if (splitter != null) {
                                            final Feature newFeature = splitter.split(validFeature, (LineString)g);

                                            if ((newFeature instanceof ModifiableFeature)
                                                        && (validFeature instanceof ModifiableFeature)) {
                                                // Save the merged feature
                                                try {
                                                    ((ModifiableFeature)newFeature).saveChanges();
                                                    ((ModifiableFeature)validFeature).saveChanges();
                                                } catch (Exception ex) {
                                                    LOG.error("Error while saving changes", ex);
                                                }

                                                if (validFeature instanceof FeatureServiceFeature) {
                                                    ((FeatureServiceFeature)validFeature).getLayerProperties()
                                                                .getFeatureService()
                                                                .retrieve(true);
                                                }
                                            }
                                        }
                                    }
                                });

                        mc.addInputListener(SplitGeometryListener.LISTENER_KEY, listener);
                        mc.putCursor(SplitGeometryListener.LISTENER_KEY, new Cursor(Cursor.CROSSHAIR_CURSOR));
                        mc.setInteractionMode(SplitGeometryListener.LISTENER_KEY);
                    }
                });
        }
    }

    /**
     * determines all valid features to merge.
     *
     * @return  DOCUMENT ME!
     */
    private FeatureServiceFeature determineValidFeature() {
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)mc.getInputEventListener().get(MappingComponent.SELECT);
        final List<PFeature> features = sl.getAllSelectedPFeatures();

        if ((features != null) && (features.size() == 1)) {
            final PFeature f = features.get(0);

            if (!f.getFeature().getGeometry().getGeometryType().equalsIgnoreCase("POINT")) {
                if (features.get(0).getFeature() instanceof FeatureServiceFeature) {
                    return (FeatureServiceFeature)features.get(0).getFeature();
                }
            }
        } else {
            // todo message: ungleich ein Feature selektiert
        }

        return null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
