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
package de.cismet.watergis.gui.panels;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.sql.Date;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PhotoWrapper implements PropertyChangeListener {

    //~ Instance fields --------------------------------------------------------

    private CidsLayerFeature feature;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PhotoWrapper object.
     *
     * @param  feature  DOCUMENT ME!
     */
    public PhotoWrapper(final CidsLayerFeature feature) {
        this.feature = feature;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Double getWinkel() {
        if (feature != null) {
            return (Double)feature.getProperty("winkel");
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  winkel  DOCUMENT ME!
     */
    public void setWinkel(final Double winkel) {
        if (feature != null) {
            feature.setProperty("winkel", winkel);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTitel() {
        if (feature != null) {
            return (String)feature.getProperty("titel");
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  titel  winkel DOCUMENT ME!
     */
    public void setTitel(final String titel) {
        if (feature != null) {
            feature.setProperty("titel", titel);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getBemerkung() {
        if (feature != null) {
            return (String)feature.getProperty("bemerkung");
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bem  winkel DOCUMENT ME!
     */
    public void setBemerkung(final String bem) {
        if (feature != null) {
            feature.setProperty("bemerkung", bem);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Double getBast() {
        if (feature != null) {
            return (Double)feature.getProperty("ba_st");
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bast  bem winkel DOCUMENT ME!
     */
    public void setBast(final Double bast) {
        if (feature != null) {
            feature.setProperty("ba_st", bast);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Date getAufndatum() {
        if (feature != null) {
            return (Date)feature.getProperty("aufn_datum");
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aufn_datum  bem winkel DOCUMENT ME!
     */
    public void setAufndatum(final Date aufn_datum) {
        if (feature != null) {
            feature.setProperty("aufn_datum", aufn_datum);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getBeschreibung() {
        if (feature != null) {
            return (String)feature.getProperty("beschreib");
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  beschreib  winkel DOCUMENT ME!
     */
    public void setBeschreibung(final String beschreib) {
        if (feature != null) {
            feature.setProperty("beschreib", beschreib);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAufnahmezeit() {
        if (feature != null) {
            return (String)feature.getProperty("aufn_zeit");
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aufnZeit  winkel DOCUMENT ME!
     */
    public void setAufnahmezeit(final String aufnZeit) {
        if (feature != null) {
            feature.setProperty("aufn_zeit", aufnZeit);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAufnahmename() {
        if (feature != null) {
            return (String)feature.getProperty("aufn_name");
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aufnName  winkel DOCUMENT ME!
     */
    public void setAufnahmename(final String aufnName) {
        if (feature != null) {
            feature.setProperty("aufn_name", aufnName);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getlRl() {
        if (feature != null) {
            final Object value = feature.getProperty("l_rl");

            if ((value instanceof String) && (feature.getCatalogueCombo("l_rl") != null)) {
                final Object o = feature.getCatalogueCombo("l_rl").getSelectedItem();

                if (o instanceof String) {
                    return feature.getInitialCalatogueValue("l_rl");
                } else {
                    return o;
                }
            } else if (feature.getCatalogueCombo("l_rl") == null) {
                return feature.getInitialCalatogueValue("l_rl");
            } else {
                return value;
            }
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lRl  aufnName winkel DOCUMENT ME!
     */
    public void setlRl(final Object lRl) {
        if (feature != null) {
            feature.setProperty("l_rl", lRl);

            if (feature.getCatalogueCombo("l_rl") != null) {
                feature.getCatalogueCombo("l_rl").setSelectedItem(lRl);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getlSt() {
        if (feature != null) {
            final Object value = feature.getProperty("l_st");

            if ((value instanceof String) && (feature.getCatalogueCombo("l_st") != null)) {
                final Object o = feature.getCatalogueCombo("l_st").getSelectedItem();

                if (o instanceof String) {
                    return feature.getInitialCalatogueValue("l_st");
                } else {
                    return o;
                }
            } else if (feature.getCatalogueCombo("l_st") == null) {
                return feature.getInitialCalatogueValue("l_st");
            } else {
                return value;
            }
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lSt  aufnName winkel DOCUMENT ME!
     */
    public void setlSt(final Object lSt) {
        if (feature != null) {
            feature.setProperty("l_st", lSt);

            if (feature.getCatalogueCombo("l_st") != null) {
                feature.getCatalogueCombo("l_st").setSelectedItem(lSt);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getFreigabe() {
        if (feature != null) {
            final Object value = feature.getProperty("freigabe");

            if ((value instanceof String) && (feature.getCatalogueCombo("freigabe") != null)) {
                final Object o = feature.getCatalogueCombo("freigabe").getSelectedItem();

                if (o instanceof String) {
                    return feature.getInitialCalatogueValue("freigabe");
                } else {
                    return o;
                }
            } else if (feature.getCatalogueCombo("freigabe") == null) {
                return feature.getInitialCalatogueValue("freigabe");
            } else {
                return value;
            }
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  freigabe  aufnName winkel DOCUMENT ME!
     */
    public void setFreigabe(final Object freigabe) {
        if (feature != null) {
            feature.setProperty("freigabe", freigabe);

            if (feature.getCatalogueCombo("freigabe") != null) {
                feature.getCatalogueCombo("freigabe").setSelectedItem(freigabe);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the feature
     */
    public CidsLayerFeature getFeature() {
        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  the feature to set
     */
    public void setFeature(final CidsLayerFeature feature) {
        if (this.feature != null) {
            this.feature.removePropertyChangeListener(this);
        }
        this.feature = feature;
        if (this.feature != null) {
            this.feature.addPropertyChangeListener(this);
        }
    }

    /**
     * Add a new PropertyChangeListener.
     *
     * @param  l  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    /**
     * Remove the given PropertyChangeListener.
     *
     * @param  l  DOCUMENT ME!
     */
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final DefaultCidsLayerBindableReferenceCombo box = feature.getCatalogueCombo(evt.getPropertyName());

        if (box != null) {
            changeSupport.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), box.getSelectedItem());
        } else {
            changeSupport.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }
}
