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
package de.cismet.watergis.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class NewObjectAction extends AbstractAction {

    //~ Instance fields --------------------------------------------------------

    private AbstractFeatureService service;
    private boolean firstCall = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public NewObjectAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                NewObjectAction.class,
                "NewObjectAction.cmdNewObject.toolTipText",
                new Object[] { " ", "" });
        putValue(SHORT_DESCRIPTION, tooltip);
//        final String text = org.openide.util.NbBundle.getMessage(NewObjectAction.class, "CloseAction.text");
//        putValue(NAME, text);
//        final String mnemonic = org.openide.util.NbBundle.getMessage(NewObjectAction.class, "CloseAction.mnemonic");
//        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-plus-sign.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if ((service != null) && !(!firstCall && e.getSource().equals(AppBroker.getInstance()))) {
            final AttributeTableRuleSet ruleSet = service.getLayerProperties().getAttributeTableRuleSet();
            final FeatureCreator creator = ruleSet.getFeatureCreator();
            final FeatureServiceFeature feature = service.getFeatureFactory().createNewFeature();
            ruleSet.beforeSave(feature);
            creator.createFeature(CismapBroker.getInstance().getMappingComponent(), feature);
        }

        firstCall = false;
    }

    @Override
    public boolean isEnabled() {
        return service != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    public void setSelectedService(final AbstractFeatureService service) {
        this.service = service;

        if (service == null) {
            final String tooltip = org.openide.util.NbBundle.getMessage(
                    NewObjectAction.class,
                    "NewObjectAction.cmdNewObject.toolTipText",
                    new Object[] { " ", "" });
            putValue(SHORT_DESCRIPTION, tooltip);
        } else {
            final String type = "("
                        + service.getLayerProperties().getAttributeTableRuleSet().getFeatureCreator().getTypeName()
                        + ")";

            final String tooltip = org.openide.util.NbBundle.getMessage(
                    NewObjectAction.class,
                    "NewObjectAction.cmdNewObject.toolTipText",
                    new Object[] { " " + service.getName() + " ", type });
            putValue(SHORT_DESCRIPTION, tooltip);
        }
    }
}
