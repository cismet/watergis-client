/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.gui.panels;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.lookupoptions.AbstractOptionsCategory;
import de.cismet.lookupoptions.OptionsCategory;
import java.awt.Image;
import org.openide.util.ImageUtilities;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = OptionsCategory.class)
public class WatergisOptionsCategory extends AbstractOptionsCategory {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NbBundle.getMessage(
                WatergisOptionsCategory.class,
                "WatergisOptionsCategory.name");
    }

    @Override
    public Icon getIcon() {
        final Image image = ImageUtilities.loadImage("de/cismet/watergis/res/icons32/icon-settingsfour-gearsalt.png");
        if (image != null) {
            return new ImageIcon(image);
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getTooltip() {
        return NbBundle.getMessage(
                WatergisOptionsCategory.class,
                "WatergisOptionsCategory.tooltip");
    }
}
