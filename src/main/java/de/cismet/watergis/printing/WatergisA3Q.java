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
package de.cismet.watergis.printing;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WatergisA3Q extends AbstractWatergisPrintingTemplate {

    //~ Static fields/initializers ---------------------------------------------

    private static final int LEGEND_HEIGHT = 758;
    private static final int LEGEND_WIDTH = 1130;

    //~ Methods ----------------------------------------------------------------

    @Override
    protected int getLegendWidth() {
        return LEGEND_WIDTH;
    }

    @Override
    protected int getLegendHeight() {
        return LEGEND_HEIGHT;
    }
}
