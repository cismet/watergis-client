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

import org.openide.util.Lookup;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.MessenGeometryListener;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.JPopupMenuButton;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.components.location.SelectionMethodInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class NewDrawingButton extends JPopupMenuButton implements PropertyChangeListener, Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final String CONFIGURATION = "NewDrawingButton";
    private static final String MODE_ATTRIBUTE = "mode";

    //~ Instance fields --------------------------------------------------------

    private final JPopupMenu popup = new JPopupMenu();
    private final JRadioButtonMenuItem[] menuItems;
    private ButtonGroup menuItemButtonGroup = new ButtonGroup();
    private ButtonGroup buttonGroup;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MeasureButton object.
     */
    public NewDrawingButton() {
        setModel(new JToggleButton.ToggleButtonModel());
        final Collection<? extends DrawingMode> modes = Lookup.getDefault().lookupAll(DrawingMode.class);

        menuItems = new HighlightingRadioButtonMenuItem[modes.size()];
        int i = 0;

        for (final DrawingMode drawingMode : modes) {
            menuItems[i] = new HighlightingRadioButtonMenuItem(javax.swing.UIManager.getDefaults().getColor(
                        "ProgressBar.foreground"),
                    Color.WHITE);
            menuItems[i].setName(drawingMode.getClass().getName());
            menuItems[i].setAction((AbstractAction)drawingMode);
            menuItemButtonGroup.add(menuItems[i]);
            popup.add(menuItems[i++]);
        }

        menuItems[0].setSelected(true);

        setPopupMenu(popup);
        setUI(new JToggleButton().getUI());
    }

    //~ Methods ----------------------------------------------------------------

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
                final String mode = modeAttr;

                for (final JRadioButtonMenuItem item : menuItems) {
                    if (item.getName().equals(mode)) {
                        item.setSelected(true);
                        item.doClick();
                        break;
                    }
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
        for (final JRadioButtonMenuItem item : menuItems) {
            if (item.isSelected()) {
                conf.setAttribute(MODE_ATTRIBUTE, item.getName());
                break;
            }
        }

        return conf;
    }
}
