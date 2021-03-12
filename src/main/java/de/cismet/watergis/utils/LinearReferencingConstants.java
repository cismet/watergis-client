/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.utils;

/**
 * Copy from cids-custom-wrrl-db.
 *
 * @version  $Revision$, $Date$
 */
public interface LinearReferencingConstants {

    //~ Instance fields --------------------------------------------------------

    String CN_STATIONLINE = "station_linie"; // NOI18N
    String CN_STATION = "station";           // NOI18N
    String CN_ROUTE = "route";               // NOI18N
    String CN_GEOM = "geom";                 // NOI18N

    String PROP_ID = "id"; // NOI18N

    String PROP_STATIONLINIE_FROM = "von";  // NOI18N
    String PROP_STATIONLINIE_TO = "bis";    // NOI18N
    String PROP_STATIONLINIE_GEOM = "geom"; // NOI18N

    String PROP_STATION_VALUE = "wert";      // NOI18N
    String PROP_STATION_ROUTE = "route";     // NOI18N
    String PROP_STATION_GEOM = "real_point"; // NOI18N

    String PROP_ROUTE_GWK = "gwk";   // NOI18N
    String PROP_ROUTE_GEOM = "geom"; // NOI18N

    String PROP_GEOM_GEOFIELD = "geo_field"; // NOI18N

    boolean FROM = true;
    boolean TO = false;
}
