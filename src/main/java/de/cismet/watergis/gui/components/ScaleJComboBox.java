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

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.StatusListener;
import de.cismet.cismap.commons.interaction.events.StatusEvent;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ScaleJComboBox extends JComboBox implements StatusListener, ItemListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ScaleJComboBox.class);

    //~ Instance fields --------------------------------------------------------

    private Pattern p = Pattern.compile("1 *: *\\d+ ");

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ScaleJComboBox object.
     */
    public ScaleJComboBox() {
        super();
        CismapBroker.getInstance().addStatusListener(this);
        this.addItemListener(this);
        setModelWithScales();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void statusValueChanged(final StatusEvent e) {
        if (e.getName().equals(StatusEvent.SCALE)) {
            this.removeItemListener(this);

            final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
            final int sd = (int)(mappingComponent.getScaleDenominator() + 0.5);
            this.setSelectedItem("1:" + sd);

            this.addItemListener(this);
        }
    }

    @Override
    public void itemStateChanged(final ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            final String item = (String)event.getItem();
            if (isValid(item)) {
                CismapBroker.getInstance().removeStatusListener(this);

                final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
                final String[] array = item.split(":");
                final Integer i = new Integer(array[1].trim());
                mappingComponent.gotoBoundingBoxWithHistory(mappingComponent.getBoundingBoxFromScale(i));

                this.setBackground(Color.white, Color.white);
                CismapBroker.getInstance().addStatusListener(this);
            } else {
                this.setBackground(Color.red, Color.white);
            }
        }
    }

    @Override
    public void setModel(final ComboBoxModel aModel) {
        super.setModel(aModel);
    }

    /**
     * DOCUMENT ME!
     */
    private void setModelWithScales() {
        final ArrayList<String> scales = new ArrayList<String>();
        for (final Scale s : AppBroker.getInstance().getMappingComponent().getScales()) {
            if (s.getDenominator() > 0) {
                scales.add(s.getText());
            }
        }
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(scales.toArray(
                    new String[scales.size()]));
        this.setModel(model);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   selectedItem  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isValid(final String selectedItem) {
        final Matcher m = p.matcher(selectedItem);
        return m.matches();
    }

    /**
     * Sets the background color of the editable field and afterwards the background color of the pop-up. The result of
     * this is, that the can have different colors.
     *
     * @param  editField  DOCUMENT ME!
     * @param  popUp      DOCUMENT ME!
     */
    public void setBackground(final Color editField, final Color popUp) {
        if ((this.getEditor() != null) && (this.getEditor().getEditorComponent() != null)) {
            this.getEditor().getEditorComponent().setBackground(editField);
        }
        super.setBackground(popUp);
    }
}
