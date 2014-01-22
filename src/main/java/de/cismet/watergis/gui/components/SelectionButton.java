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

import org.jdom.Element;

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

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MessenGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.JPopupMenuButton;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.listener.SelectionModeChangedEvent;
import de.cismet.watergis.broker.listener.SelectionModeListener;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SelectionButton extends JPopupMenuButton implements PropertyChangeListener,
    Configurable,
    SelectionModeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final int POLYGON_MODE = 1;
    private static final int RECTANGLE_MODE = 2;
    private static final int ELLIPSE_MODE = 3;
    private static final String CONFIGURATION = "SelectionButton";
    private static final String MODE_ATTRIBUTE = "mode";

    //~ Instance fields --------------------------------------------------------

    private JPopupMenu popup = new JPopupMenu();
    private JRadioButtonMenuItem polygonMenu = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults()
                    .getColor(
                        "ProgressBar.foreground"),
            Color.WHITE);
    private JRadioButtonMenuItem rectangleMenu = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults()
                    .getColor(
                        "ProgressBar.foreground"),
            Color.WHITE);
    private JRadioButtonMenuItem ellipseMenu = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults()
                    .getColor(
                        "ProgressBar.foreground"),
            Color.WHITE);
    private int mode = RECTANGLE_MODE;
    private ButtonGroup buttonGroup;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MeasureButton object.
     */
    public SelectionButton() {
        setModel(new JToggleButton.ToggleButtonModel());

        ellipseMenu.setAction(new AbstractAction("Ellipse") {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final SelectionListener sl = (SelectionListener)AppBroker.getInstance().getMappingComponent()
                                .getInputEventListener()
                                .get(MappingComponent.SELECT);
                    final String oldMode = sl.getMode();
                    setMode(ELLIPSE_MODE, false);
                    sl.setMode(CreateGeometryListenerInterface.ELLIPSE);
                    AppBroker.getInstance()
                            .fireSelectionModeChanged(this, oldMode, CreateGeometryListenerInterface.ELLIPSE);
                }
            });

        rectangleMenu.setAction(new AbstractAction("Rechteck") {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final SelectionListener sl = (SelectionListener)AppBroker.getInstance().getMappingComponent()
                                .getInputEventListener()
                                .get(MappingComponent.SELECT);
                    final String oldMode = sl.getMode();
                    setMode(RECTANGLE_MODE, false);
                    sl.setMode(CreateGeometryListenerInterface.RECTANGLE);
                    AppBroker.getInstance()
                            .fireSelectionModeChanged(this, oldMode, CreateGeometryListenerInterface.RECTANGLE);
                }
            });

        polygonMenu.setAction(new AbstractAction("Polygon") {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final SelectionListener sl = (SelectionListener)AppBroker.getInstance().getMappingComponent()
                                .getInputEventListener()
                                .get(MappingComponent.SELECT);
                    final String oldMode = sl.getMode();
                    setMode(POLYGON_MODE, false);
                    sl.setMode(CreateGeometryListenerInterface.POLYGON);
                    AppBroker.getInstance()
                            .fireSelectionModeChanged(this, oldMode, CreateGeometryListenerInterface.POLYGON);
                }
            });

        rectangleMenu.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/rectangle.png")));
        ellipseMenu.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/ellipse.png")));
        polygonMenu.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/polygon.png")));
        rectangleMenu.setSelected(true);
        popup.add(rectangleMenu);
        popup.add(polygonMenu);
        popup.add(ellipseMenu);

        setPopupMenu(popup);
        setUI(new JToggleButton().getUI());

        AppBroker.getInstance().addSelecionModeListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  mode      DOCUMENT ME!
     * @param  internal  DOCUMENT ME!
     */
    private void setMode(final int mode, final boolean internal) {
        this.mode = mode;
        rectangleMenu.setSelected(mode == RECTANGLE_MODE);
        ellipseMenu.setSelected(mode == ELLIPSE_MODE);
        polygonMenu.setSelected(mode == POLYGON_MODE);

        if (!internal) {
            AppBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
        }
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

    @Override
    public void configure(final Element parent) {
        if (parent != null) {
            final Element conf = parent.getChild(CONFIGURATION);

            if (conf != null) {
                final String modeAttr = conf.getAttributeValue(MODE_ATTRIBUTE);
                try {
                    final int mode = Integer.parseInt(modeAttr);

                    setMode(mode, true);
                    final SelectionListener sl = (SelectionListener)AppBroker.getInstance().getMappingComponent()
                                .getInputEventListener()
                                .get(MappingComponent.SELECT);

                    final String oldMode = sl.getMode();
                    String newMode = null;
                    switch (mode) {
                        case 1: {
                            sl.setMode(CreateGeometryListenerInterface.POLYGON);
                            newMode = CreateGeometryListenerInterface.POLYGON;
                            break;
                        }
                        case 2: {
                            sl.setMode(CreateGeometryListenerInterface.RECTANGLE);
                            newMode = CreateGeometryListenerInterface.RECTANGLE;
                            break;
                        }
                        case 3: {
                            sl.setMode(CreateGeometryListenerInterface.ELLIPSE);
                            newMode = CreateGeometryListenerInterface.ELLIPSE;
                            break;
                        }
                    }

                    AppBroker.getInstance().fireSelectionModeChanged(this, oldMode, newMode);
                } catch (NumberFormatException e) {
                    // nothing to do
                }
            }
        }
    }

    @Override
    public void masterConfigure(final Element parent) {
        // the server configuration should be handled like the client configuration
        configure(parent);
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element conf = new Element(CONFIGURATION);
        conf.setAttribute(MODE_ATTRIBUTE, String.valueOf(mode));

        return conf;
    }

    @Override
    public void selectionModeChanged(final SelectionModeChangedEvent e) {
        int newMode = RECTANGLE_MODE;

        if (e.getNewMode().equals(CreateGeometryListenerInterface.ELLIPSE)) {
            newMode = ELLIPSE_MODE;
        } else if (e.getNewMode().equals(CreateGeometryListenerInterface.POLYGON)) {
            newMode = POLYGON_MODE;
        }

        setMode(newMode, true);
    }
}
