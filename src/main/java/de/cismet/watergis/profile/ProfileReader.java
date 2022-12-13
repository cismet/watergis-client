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
package de.cismet.watergis.profile;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import java.io.File;

import java.util.List;
import java.util.Set;

import javax.swing.JDialog;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.watergis.utils.CustomGafCatalogueReader;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface ProfileReader {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum GAF_FIELDS {

        //~ Enum constants -----------------------------------------------------

        STATION, ID, Y, Z, KZ, RK, BK, HW, RW, HYK
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  gafFile  DOCUMENT ME!
     */
    void initFromFile(final File gafFile);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String[] checkFile();

    /**
     * DOCUMENT ME!
     *
     * @param   catalogueFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CustomGafCatalogueReader.FILE_TYPE addCustomCatalogue(final File catalogueFile);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    QpCheckResult checkFileForHints();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Set<Double> getProfiles();

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    LineString getNpLine(final Double profile);

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Point getProfilePoint(final Double profile);

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<ProfileLine> getProfileContent(final Double profile);

    /**
     * DOCUMENT ME!
     *
     * @param   field    DOCUMENT ME!
     * @param   gafLine  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object getProfileContent(final GAF_FIELDS field, final ProfileLine gafLine);

    /**
     * DOCUMENT ME!
     *
     * @param   parent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    AbstractImportDialog getImportDialog(java.awt.Frame parent);

    /**
     * DOCUMENT ME!
     *
     * @return  the lageBezug
     */
    CidsLayerFeature getLageBezug();

    /**
     * DOCUMENT ME!
     *
     * @param  lageBezug  the lageBezug to set
     */
    void setLageBezug(CidsLayerFeature lageBezug);

    /**
     * DOCUMENT ME!
     *
     * @return  the hoeheBezug
     */
    CidsLayerFeature getHoeheBezug();

    /**
     * DOCUMENT ME!
     *
     * @param  hoeheBezug  the hoeheBezug to set
     */
    void setHoeheBezug(CidsLayerFeature hoeheBezug);

    /**
     * DOCUMENT ME!
     *
     * @return  the status
     */
    CidsLayerFeature getStatus();

    /**
     * DOCUMENT ME!
     *
     * @param  status  the status to set
     */
    void setStatus(CidsLayerFeature status);

    /**
     * DOCUMENT ME!
     *
     * @return  the freigabe
     */
    CidsLayerFeature getFreigabe();

    /**
     * DOCUMENT ME!
     *
     * @param  freigabe  the freigabe to set
     */
    void setFreigabe(CidsLayerFeature freigabe);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<ProfileLine> getContent();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isCalc();
}
