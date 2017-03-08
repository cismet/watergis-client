/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.broker;

import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.gui.piccolo.PFeature;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PushpinVertexStyle extends VertexStyle {

    //~ Instance fields --------------------------------------------------------

    private ImageIcon icon = new javax.swing.ImageIcon(PFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/pushpinSelected.png"));

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PushpinVertexStyle object.
     */
    public PushpinVertexStyle() {
        super(new Polygon());
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void paint(final Graphics2D g, final Point2D p) {
        if (getSize() != 0) {
            final int s = getSize();
            g.drawImage(icon.getImage(), (int)p.getX() - (s / 2), (int)p.getY()
                        - (s / 2), s, s, null);
        } else {
            g.drawImage(icon.getImage(),
                (int)p.getX()
                        - ((icon.getImage().getWidth(null)) / 2),
                (int)p.getY()
                        - ((icon.getImage().getHeight(null)) / 2),
                null);
        }
    }
}
