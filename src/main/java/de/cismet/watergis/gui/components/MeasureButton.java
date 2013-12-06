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
package de.cismet.watergis.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.MessenGeometryListener;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.JPopupMenuButton;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class MeasureButton extends JPopupMenuButton implements PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final int POLYGON_MODE = 1;
    private static final int LINE_MODE = 2;

    //~ Instance fields --------------------------------------------------------

    private JPopupMenu popup = new JPopupMenu();
    private JRadioButtonMenuItem polygonMenu = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults()
                    .getColor(
                        "ProgressBar.foreground"),
            Color.WHITE);
    private JRadioButtonMenuItem lineMenu = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults()
                    .getColor(
                        "ProgressBar.foreground"),
            Color.WHITE);
    private int mode = POLYGON_MODE;
    private ButtonGroup buttonGroup;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MeasureButton object.
     */
    public MeasureButton() {
        setModel(new JToggleButton.ToggleButtonModel());

        lineMenu.setAction(new AbstractAction("Linie") {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    setMode(LINE_MODE);
                    AppBroker.getInstance().getMeasureListener().setMode(MessenGeometryListener.LINESTRING);
                }
            });

        polygonMenu.setAction(new AbstractAction("Polygon") {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    setMode(POLYGON_MODE);
                    AppBroker.getInstance().getMeasureListener().setMode(MessenGeometryListener.POLYGON);
                }
            });

        lineMenu.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/polyline.png")));
        polygonMenu.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/polygon.png")));
        polygonMenu.setSelected(true);
        popup.add(lineMenu);
        popup.add(polygonMenu);

        setPopupMenu(popup);
        setUI(new JToggleButton().getUI());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    private void setMode(final int mode) {
        this.mode = mode;
        lineMenu.setSelected(mode == LINE_MODE);
        polygonMenu.setSelected(mode == POLYGON_MODE);
        AppBroker.getInstance().getMappingComponent().setInteractionMode(AppBroker.MEASURE_MODE);
        AppBroker.getInstance().getMappingComponent().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the buttonGroup
     */
    public ButtonGroup getButtonGroup() {
        return buttonGroup;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  buttonGroup  the buttonGroup to set
     */
    public void setButtonGroup(final ButtonGroup buttonGroup) {
        this.buttonGroup = buttonGroup;

        buttonGroup.add(this);
    }

    @Override
    public void setSelected(final boolean b) {
        super.setSelected(b);
    }

    @Override
    public void setAction(final Action a) {
        super.setAction(a);
        a.addPropertyChangeListener(this);
        final Boolean selected = (Boolean)a.getValue(Action.SELECTED_KEY);

        if ((selected != null) && selected.booleanValue()) {
            setSelected(true);
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Action.SELECTED_KEY)) {
            if ((evt.getNewValue() != null) && ((Boolean)evt.getNewValue()).booleanValue()) {
                setSelected(true);
            }
        }
    }
}
