/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.custom.beans.watergis;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cids.dynamics.CidsBean;

/**
 *
 * @author Gilles Baatz
 */
public class Bookmark extends CidsBean{
    private String name;
    private String description;
    private Geometry geom;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }
    
    
    
}
