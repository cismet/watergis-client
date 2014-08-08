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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.HighlightingRadioButtonMenuItem;
import de.cismet.tools.gui.JPopupMenuButton;

import de.cismet.watergis.gui.actions.geoprocessing.DissolveGeoprocessingAction;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GeometryOpButton extends JPopupMenuButton implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    public static final int DISSOLVE_MODE = 1;
    private static final String CONFIGURATION = "GeometryOpButton";
    private static final String MODE_ATTRIBUTE = "mode";

    //~ Instance fields --------------------------------------------------------

    private final JPopupMenu popup = new JPopupMenu();
    private final JRadioButtonMenuItem dissolveMenu = new HighlightingRadioButtonMenuItem(javax.swing.UIManager
                    .getDefaults().getColor(
                "ProgressBar.foreground"),
            Color.WHITE);
    private final JRadioButtonMenuItem intersectMenu = new HighlightingRadioButtonMenuItem(javax.swing.UIManager
                    .getDefaults().getColor(
                "ProgressBar.foreground"),
            Color.WHITE);
    private int mode = DISSOLVE_MODE;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MeasureButton object.
     */
    public GeometryOpButton() {
        setModel(new JToggleButton.ToggleButtonModel());

        dissolveMenu.setAction(new DissolveGeoprocessingAction(this, DISSOLVE_MODE));

        dissolveMenu.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-calcequals.png")));
        popup.add(dissolveMenu);

        setPopupMenu(popup);
//        setUI(new JToggleButton().getUI());

        addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (mode == DISSOLVE_MODE) {
                        dissolveMenu.getAction().actionPerformed(e);
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
    public void setMode(final int mode) {
        this.mode = mode;
        dissolveMenu.setSelected(mode == DISSOLVE_MODE);
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
                    final int mode = Integer.parseInt(modeAttr);

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
        conf.setAttribute(MODE_ATTRIBUTE, String.valueOf(mode));

        return conf;
    }
}
