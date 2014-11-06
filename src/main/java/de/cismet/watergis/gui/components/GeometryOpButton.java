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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.JPopupMenuButton;

import de.cismet.watergis.gui.actions.geoprocessing.AbstractGeoprocessingAction;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GeometryOpButton extends JPopupMenuButton implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final String CONFIGURATION = "GeometryOpButton";
    private static final String MODE_ATTRIBUTE = "mode";

    //~ Instance fields --------------------------------------------------------

    private final JPopupMenu popup = new JPopupMenu();
    private final List<AbstractGeoprocessingAction> geoprocessingActions = new ArrayList<AbstractGeoprocessingAction>();
    private AbstractGeoprocessingAction currentGeoprocessingAction = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MeasureButton object.
     */
    public GeometryOpButton() {
        setModel(new JToggleButton.ToggleButtonModel());

        geoprocessingActions.addAll(Lookup.getDefault().lookupAll(AbstractGeoprocessingAction.class));
        Collections.sort(geoprocessingActions, new Comparator<AbstractGeoprocessingAction>() {

                @Override
                public int compare(final AbstractGeoprocessingAction o1, final AbstractGeoprocessingAction o2) {
                    return new Integer(o1.getSortOrder()).compareTo(o2.getSortOrder());
                }
            });

        for (final AbstractGeoprocessingAction geoProcessingAction : geoprocessingActions) {
            if (currentGeoprocessingAction == null) {
                setCurrentGeoprocessingAction(geoProcessingAction);
            }
            final JRadioButtonMenuItem geoprocessingMenu = new HighlightingRadioButtonMenuItem(javax.swing.UIManager
                            .getDefaults().getColor(
                        "ProgressBar.foreground"),
                    Color.WHITE);

            geoprocessingMenu.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/watergis/res/icons16/icon-calcequals.png")));

            geoprocessingMenu.setAction(geoProcessingAction);

            geoProcessingAction.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (e.getSource() instanceof HighlightingRadioButtonMenuItem) {
                            final HighlightingRadioButtonMenuItem menu = (HighlightingRadioButtonMenuItem)e.getSource();
                            setCurrentGeoprocessingAction((AbstractGeoprocessingAction)menu.getAction());
                        }
                    }
                });

            popup.add(geoprocessingMenu);
        }

        setPopupMenu(popup);

        addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (currentGeoprocessingAction != null) {
                        currentGeoprocessingAction.actionPerformed(e);
                    }
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    public void setMode(final String mode) {
        for (final AbstractGeoprocessingAction geoprocessingAction : geoprocessingActions) {
            if (geoprocessingAction.getName().equals(mode)) {
                setCurrentGeoprocessingAction(geoprocessingAction);
                break;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  currentGeoprocessingAction  DOCUMENT ME!
     */
    public void setCurrentGeoprocessingAction(final AbstractGeoprocessingAction currentGeoprocessingAction) {
        this.currentGeoprocessingAction = currentGeoprocessingAction;
        for (final Component component : popup.getComponents()) {
            if (component instanceof HighlightingRadioButtonMenuItem) {
                final HighlightingRadioButtonMenuItem menu = (HighlightingRadioButtonMenuItem)component;
                menu.setSelected(menu.getAction().equals(currentGeoprocessingAction));
            }
        }
    }

    @Override
    public void setSelected(final boolean b) {
        super.setSelected(false);
    }

    @Override
    public void configure(final Element parent) {
        if (parent != null) {
            final Element conf = parent.getChild(CONFIGURATION);

            if (conf != null) {
                final String modeAttr = conf.getAttributeValue(MODE_ATTRIBUTE);
                try {
                    final String mode = modeAttr;

                    setMode(mode);
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
        if (currentGeoprocessingAction == null) {
            conf.setAttribute(MODE_ATTRIBUTE, null);
        } else {
            conf.setAttribute(MODE_ATTRIBUTE, currentGeoprocessingAction.getName());
        }

        return conf;
    }
}
