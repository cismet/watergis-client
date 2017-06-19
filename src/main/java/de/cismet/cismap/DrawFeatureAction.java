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
package de.cismet.cismap;

import org.openide.util.lookup.ServiceProvider;

import java.awt.Color;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.features.CommonFeatureAction;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DrawingSLDStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.NewTextDialog;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CommonFeatureAction.class)
public class DrawFeatureAction extends AbstractAction implements CommonFeatureAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DrawFeatureAction.class);

    //~ Instance fields --------------------------------------------------------

    Feature currentFeature = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DuplicateGeometryFeatureAction object.
     */
    public DrawFeatureAction() {
//        super(NbBundle.getMessage(DrawFeatureAction.class,
//                "DrawFeatureAction.DrawFeatureAction()"));
        super("Ändere Schrift");
        super.putValue(
            Action.SMALL_ICON,
            new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-settingsthree-gears.png")));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getSorter() {
        return 1;
    }

    @Override
    public Feature getSourceFeature() {
        return currentFeature;
    }

    @Override
    public boolean isActive() {
        return ((currentFeature instanceof DrawingSLDStyledFeature)
                        && ((DrawingSLDStyledFeature)currentFeature).getGeometryType().equals(
                            AbstractNewFeature.geomTypes.TEXT));
    }

    @Override
    public void setSourceFeature(final Feature source) {
        currentFeature = source;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final DrawingSLDStyledFeature feature = (DrawingSLDStyledFeature)currentFeature;
        final NewTextDialog dialog = new NewTextDialog(StaticSwingTools.getParentFrame(
                    CismapBroker.getInstance().getMappingComponent()),
                false);
        dialog.setFont(feature.getPrimaryAnnotationFont());
//        dialog.settext(feature.getText());
//        dialog.setAutoScaleEnabled(feature.isAutoscale());
//        dialog.setHaloEnabled(feature.getPrimaryAnnotationHalo() != null);
        dialog.setRunWhenFinish(new Runnable() {

                @Override
                public void run() {
                    if (dialog.isConfirmed()) {
                        feature.setText(dialog.getText());

                        if (!dialog.isAutoScaleEnabled()) {
                            final int fontSize = (int)(CismapBroker.getInstance().getMappingComponent()
                                            .getScaleDenominator() / 3700 * 12);
                            Font f = dialog.getFont();

                            if (f == null) {
                                f = new Font("sansserif", Font.PLAIN, fontSize);
                            }
                            feature.setPrimaryAnnotationFont(f);
                        } else {
                            Font f = dialog.getFont();
                            if (f == null) {
                                final int fontSize = (int)(CismapBroker.getInstance().getMappingComponent()
                                                .getScaleDenominator() / 3700 * 12);
                                f = new Font("sansserif", Font.PLAIN, fontSize);
                            } else {
                                final int fontSize = (int)(CismapBroker.getInstance().getMappingComponent()
                                                .getScaleDenominator() / 3700 * f.getSize());
                                f = f.deriveFont(fontSize);
                            }
                            feature.setPrimaryAnnotationFont(f);
                        }
                        feature.setAutoScale(dialog.isAutoScaleEnabled());

                        if (dialog.isHaloEnabled()) {
                            feature.setPrimaryAnnotationHalo(Color.WHITE);
                        }

                        final List<Feature> changedFeatures = new ArrayList<Feature>();
                        changedFeatures.add(feature);
                        ((DefaultFeatureCollection)CismapBroker.getInstance().getMappingComponent()
                                    .getFeatureCollection()).fireFeaturesChanged(changedFeatures);
                    }
                }
            });

        final java.awt.Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        dialog.setLocation((int)(mouseLocation.getX() + 10), (int)mouseLocation.getY());
        dialog.setVisible(true);
    }
}
