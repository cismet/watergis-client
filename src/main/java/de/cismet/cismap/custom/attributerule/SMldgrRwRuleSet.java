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
package de.cismet.cismap.custom.attributerule;

import Sirius.navigator.exception.ConnectionException;

import org.apache.log4j.Logger;

import javax.swing.table.TableModel;

import de.cismet.cids.custom.watergis.server.actions.RefreshTemplateAction;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SMldgrRwRuleSet extends WatergisDefaultRuleSet {

    //~ Instance fields --------------------------------------------------------

    private final Logger LOG = Logger.getLogger(FgBakGwkRuleSet.class);

    //~ Instance initializers --------------------------------------------------

    {
        typeMap.put("geom", new WatergisDefaultRuleSet.Geom(true, true));
        typeMap.put("land_cd", new WatergisDefaultRuleSet.Varchar(4, true));
        typeMap.put("fis_g_date", new WatergisDefaultRuleSet.DateTime(false, false));
        typeMap.put("fis_g_user", new WatergisDefaultRuleSet.Varchar(50, false, false));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return columnName.equals("land_cd");
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
        adjustFisGDateAndFisGUser(feature);
    }

    @Override
    public void afterSave(final TableModel model) {
        try {
            refreshTemplate(RefreshTemplateAction.RW_SEG_GEOM);
        } catch (ConnectionException ex) {
            LOG.error("Cannot refresh templates", ex);
        }
        super.afterSave(model);
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        final PrimitiveGeometryCreator c = new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, false);
        c.setMinArea(MIN_AREA_SIZE);

        return c;
    }
}
