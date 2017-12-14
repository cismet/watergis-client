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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.StaticDecimalTools;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.dialog.GotoDialog;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class GoToAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GoToAction.class);

    //~ Instance fields --------------------------------------------------------

    private ImageIcon pointIcon = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/linRefPoint.png"));

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public GoToAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                GoToAction.class,
                "GoToAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                GoToAction.class,
                "GoToAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                GoToAction.class,
                "GoToAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-map-marker.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Goto to position");
        }
        final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
        final XBoundingBox c = (XBoundingBox)mappingComponent.getCurrentBoundingBoxFromCamera();
        final double x = (c.getX1() + c.getX2()) / 2;
        final double y = (c.getY1() + c.getY2()) / 2;
//        final String message = org.openide.util.NbBundle.getMessage(
//                GoToAction.class,
//                "GoToAction.actionPerformed().dialogMessage");
//        final String title = "Gehe zu xy";

//        final Object s = JOptionPane.showInputDialog(
//                AppBroker.getInstance().getComponent(ComponentName.MAIN),
//                message,
//                title,
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                null,
//                StaticDecimalTools.round(x).replace('.', ',')
//                        + " "
//                        + StaticDecimalTools.round(y).replace('.', ','));

        final GotoDialog dialog = new GotoDialog(StaticSwingTools.getParentFrame(
                    AppBroker.getInstance().getComponent(ComponentName.MAIN)),
                true);
        dialog.setXVal(StaticDecimalTools.round(x).replace('.', ','));
        dialog.setYVal(StaticDecimalTools.round(y).replace('.', ','));
        StaticSwingTools.showDialog(dialog);

        if (!dialog.isCancelled()) {
            try {
//                final String[] sa = ((String)s).split(" ");
                final Double gotoX = new Double(dialog.getXVal().replace(',', '.'));
                final Double gotoY = new Double(dialog.getYVal().replace(',', '.'));
                final String currentCrsCode = CismapBroker.getInstance().getSrs().getCode();
                final XBoundingBox bb = new XBoundingBox(
                        gotoX,
                        gotoY,
                        gotoX,
                        gotoY,
                        currentCrsCode,
                        mappingComponent.isInMetricSRS());
                mappingComponent.gotoBoundingBox(bb, true, false, mappingComponent.getAnimationDuration());

                final Timer t = new Timer();
                t.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            showPointInMap(mappingComponent, gotoX, gotoY);
                        }
                    }, mappingComponent.getAnimationDuration());
            } catch (Exception skip) {
            }
        }
    }

    /**
     * shows the given point on the map.
     *
     * @param  mappingComponent  DOCUMENT ME!
     * @param  x                 DOCUMENT ME!
     * @param  y                 DOCUMENT ME!
     */
    private void showPointInMap(final MappingComponent mappingComponent, final double x, final double y) {
        final DefaultStyledFeature styledFeature = new DefaultStyledFeature();
        final String currentCrsCode = CismapBroker.getInstance().getSrs().getCode();
        final int srid = CrsTransformer.extractSridFromCrs(currentCrsCode);
        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
        styledFeature.setGeometry(gf.createPoint(new Coordinate(x, y)));
        final FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(pointIcon.getImage());
        fas.setSweetSpotX(0.5);
        fas.setSweetSpotY(0.5);
        styledFeature.setPointAnnotationSymbol(fas);
        mappingComponent.highlightFeature(styledFeature, 1500);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
