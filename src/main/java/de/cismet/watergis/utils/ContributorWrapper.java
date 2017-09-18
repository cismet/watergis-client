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
package de.cismet.watergis.utils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRSaveContributor;

import java.io.File;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ContributorWrapper extends JRSaveContributor {

    //~ Instance fields --------------------------------------------------------

    private final JRSaveContributor contributor;
    private final String description;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ContributorWrapper object.
     *
     * @param  contributor  DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     */
    public ContributorWrapper(final JRSaveContributor contributor, final String description) {
        this.contributor = contributor;
        this.description = description;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ContributorWrapper) {
            return contributor.equals(((ContributorWrapper)obj).contributor);
        } else {
            return contributor.equals(obj);
        }
    }

    @Override
    public void save(final JasperPrint jp, final File file) throws JRException {
        contributor.save(jp, file);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean accept(final File f) {
        return contributor.accept(f);
    }
}
