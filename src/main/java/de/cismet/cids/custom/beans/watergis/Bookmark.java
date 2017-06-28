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
package de.cismet.cids.custom.beans.watergis;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.apache.log4j.Logger;

import org.jdom.Attribute;
import org.jdom.Element;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class Bookmark extends CidsBean implements Comparable<Bookmark> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(Bookmark.class);

    //~ Instance fields --------------------------------------------------------

    private String name;
    private String description;
    private Geometry geometry;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Bookmark object.
     */
    public Bookmark() {
    }

    /**
     * Creates a new Bookmark object.
     *
     * @param  bookmarkElement  DOCUMENT ME!
     */
    public Bookmark(final Element bookmarkElement) {
        name = bookmarkElement.getAttributeValue("name");
        description = bookmarkElement.getChildText("description");
        final Element geometryElement = bookmarkElement.getChild("geometry");
        final int crs = Integer.parseInt(geometryElement.getAttributeValue("crs"));

        final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), crs);
        final WKTReader wktReader = new WKTReader(geomFactory);
        final String geomText = bookmarkElement.getChildText("geometry");

        try {
            geometry = wktReader.read(geomText);
        } catch (final ParseException ex) {
            LOG.error("cannot create geometry from WKT: " + geomText, ex); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDescription() {
        return description;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  description  DOCUMENT ME!
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geometry  DOCUMENT ME!
     */
    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (53 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
        hash = (53 * hash) + ((this.description != null) ? this.description.hashCode() : 0);
        hash = (53 * hash) + ((this.geometry != null) ? this.geometry.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bookmark other = (Bookmark)obj;
        if ((this.name == null) ? (other.name != null) : (!this.name.equals(other.name))) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : (!this.description.equals(other.description))) {
            return false;
        }
        if ((this.geometry != other.geometry) && ((this.geometry == null) || !this.geometry.equals(other.geometry))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final Bookmark o) {
        return this.name.compareTo(o.name);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element toElement() {
        final Element bookmark = new Element("bookmark");
        final Attribute nameAttribute = new Attribute("name", name);
        final Element geometryElement = new Element("geometry");

        geometryElement.addContent(geometry.toText());
        geometryElement.setAttribute(new Attribute("crs", String.valueOf(geometry.getSRID())));
        bookmark.setAttribute(nameAttribute);
        bookmark.addContent(geometryElement);

        if (description != null) {
            final Element descriptionElement = new Element("description");
            descriptionElement.addContent(description);
            bookmark.addContent(descriptionElement);
        }

        return bookmark;
    }
}
