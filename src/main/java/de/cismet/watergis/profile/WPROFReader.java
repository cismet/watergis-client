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

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

import org.apache.log4j.Logger;

import java.awt.Frame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WPROFReader extends AbstractProfileReader {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(WPROFReader.class);
    private static final ConnectionContext CC = ConnectionContext.create(
            AbstractConnectionContext.Category.OTHER,
            "WPROFReader");
    private static final MetaClass ROUTE_MC = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak",
            CC);
    private static final MetaClass GEOM_MC = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "geom",
            CC);

    //~ Instance fields --------------------------------------------------------

    private File csvFile;
    private String[] columnProposal = getColumnProposal();
    private boolean headerField = false;
    private CidsLayerFeature lageBezug = null;
    private CidsLayerFeature hoeheBezug = null;
    private CidsLayerFeature status = null;
    private CidsLayerFeature freigabe = null;
    private CidsLayerFeature lawaRoute = null;
    private double artificialStationId = -1.0;
    private MetaObject[] routes = null;
    private boolean duplicateSepAllowed = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WPROFReader object.
     *
     * @param  file  DOCUMENT ME!
     */
    public WPROFReader(final File file) {
        initFromFile(file);
    }

    /**
     * Creates a new GafReader object.
     *
     * @param  gafFeatures  gafFile DOCUMENT ME!
     */
    public WPROFReader(final List<DefaultFeatureServiceFeature> gafFeatures) {
        initFromFeatures(1., gafFeatures);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the duplicateSepAllowed
     */
    public boolean isDuplicateSepAllowed() {
        return duplicateSepAllowed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  duplicateSepAllowed  the duplicateSepAllowed to set
     */
    public void setDuplicateSepAllowed(final boolean duplicateSepAllowed) {
        this.duplicateSepAllowed = duplicateSepAllowed;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  profileId  DOCUMENT ME!
     * @param  features   DOCUMENT ME!
     */
    private void initFromFeatures(final Double profileId, final List<DefaultFeatureServiceFeature> features) {
        try {
            fieldMap = new HashMap<>();
            for (int i = 0; i < 10; ++i) {
                fieldMap.put(GAF_FIELDS.values()[i], i);
            }

            for (final DefaultFeatureServiceFeature feature : features) {
                final String[] contentFields = new String[10];
                contentFields[GAF_FIELDS.Y.ordinal()] = objectToString(feature.getProperty("y"), null);
                contentFields[GAF_FIELDS.Z.ordinal()] = objectToString(feature.getProperty("z"), null);
                contentFields[GAF_FIELDS.ID.ordinal()] = objectToString(feature.getProperty("gaf_id"), null);
                contentFields[GAF_FIELDS.KZ.ordinal()] = objectToString(feature.getProperty("kz"), null);
                contentFields[GAF_FIELDS.HW.ordinal()] = objectToString(feature.getProperty("hw"), null);
                contentFields[GAF_FIELDS.RW.ordinal()] = objectToString(feature.getProperty("rw"), null);
                contentFields[GAF_FIELDS.HYK.ordinal()] = objectToString(feature.getProperty("hyk"), null);
                contentFields[GAF_FIELDS.STATION.ordinal()] = objectToString(
                        profileId,
                        String.valueOf(profileId));
                contentFields[GAF_FIELDS.RK.ordinal()] = objectToString(feature.getProperty("rk"), null);
                contentFields[GAF_FIELDS.BK.ordinal()] = objectToString(feature.getProperty("bk"), null);

                final ProfileLine profLine = new ProfileLine(fieldMap, contentFields);
                content.add(profLine);

                final Double station = profLine.getFieldAsDouble(GAF_FIELDS.STATION);

                ArrayList<ProfileLine> profile = profiles.get(station);

                if (profile == null) {
                    profile = new ArrayList<>();
                    profiles.put(station, profile);
                }

                profile.add(profLine);
            }
        } catch (Exception e) {
            LOG.error("Error while reading GAF data from features", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the lageBezug
     */
    @Override
    public CidsLayerFeature getLageBezug() {
        return lageBezug;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lageBezug  the lageBezug to set
     */
    @Override
    public void setLageBezug(final CidsLayerFeature lageBezug) {
        this.lageBezug = lageBezug;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lawaRoute  DOCUMENT ME!
     */
    public void setRoute(final CidsLayerFeature lawaRoute) {
        this.lawaRoute = lawaRoute;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the hoeheBezug
     */
    @Override
    public CidsLayerFeature getHoeheBezug() {
        return hoeheBezug;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hoeheBezug  the hoeheBezug to set
     */
    @Override
    public void setHoeheBezug(final CidsLayerFeature hoeheBezug) {
        this.hoeheBezug = hoeheBezug;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the status
     */
    @Override
    public CidsLayerFeature getStatus() {
        return status;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  status  the status to set
     */
    @Override
    public void setStatus(final CidsLayerFeature status) {
        this.status = status;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the freigabe
     */
    @Override
    public CidsLayerFeature getFreigabe() {
        return freigabe;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  freigabe  the freigabe to set
     */
    @Override
    public void setFreigabe(final CidsLayerFeature freigabe) {
        this.freigabe = freigabe;
    }

    @Override
    public void initFromFile(final File csvFile) {
        this.csvFile = csvFile;

        analyseFile();
    }

    @Override
    public String[] checkFile() {
        return null;
    }

    @Override
    public QpCheckResult checkFileForHints() {
        int qPNumber = 0;
        if ((content == null) || content.isEmpty()) {
            readFile();
        }
        profiles.clear();
        final QpCheckResult result = checkData();

        if ((result.getErrors() != null) && !result.getErrors().isEmpty()) {
            return result;
        }
        profiles.clear();

        if (fieldMap.containsKey(GAF_FIELDS.STATION) && fieldMap.containsKey(GAF_FIELDS.HW)
                    && fieldMap.containsKey(GAF_FIELDS.RW)) {
            // Punktwolke 2
            final List<ProfileLine> parts = new ArrayList<>();
            final Double lastY = null;
            Coordinate lastPoint = null;
            Double lastStat = null;
            ArrayList<ProfileLine> currentProfile = new ArrayList<>();

            for (final ProfileLine line : content) {
                final Coordinate currentPoint = new Coordinate(line.getFieldAsDouble(GAF_FIELDS.RW),
                        line.getFieldAsDouble(GAF_FIELDS.HW));
                final Double currentY = ((lastPoint == null) ? 0.0 : lastPoint.distance(currentPoint));
                final Double currentStat = line.getFieldAsDouble(GAF_FIELDS.STATION);

                if (currentY == 0.0) {
                    lastPoint = currentPoint;
                }

                line.setField(GAF_FIELDS.Y, currentY);

                if ((lastStat != null) && !lastStat.equals(currentStat)) {
                    profiles.put(lastStat, currentProfile);
                    ++qPNumber;
                    currentProfile = new ArrayList<>();
                    line.setField(GAF_FIELDS.Y, 0.0);
                    lastPoint = currentPoint;
                }

                currentProfile.add(line);
//                lastPoint = currentPoint;
                lastStat = currentStat;
            }
            profiles.put(lastStat, currentProfile);
            ++qPNumber;
        } else if (!fieldMap.containsKey(GAF_FIELDS.STATION) && fieldMap.containsKey(GAF_FIELDS.HW)
                    && fieldMap.containsKey(GAF_FIELDS.RW)) {
            // Punktwolke 2
            ArrayList<ProfileLine> currentProfile = new ArrayList<>();

            final List<ProfileLine> parts = new ArrayList<>();
            Double lastY = null;
            Coordinate lastPoint = null;

            for (final ProfileLine line : content) {
                final Coordinate currentPoint = new Coordinate(line.getFieldAsDouble(GAF_FIELDS.RW),
                        line.getFieldAsDouble(GAF_FIELDS.HW));
                final Double currentY = ((lastPoint == null) ? 0.0 : lastPoint.distance(currentPoint));

                line.setField(GAF_FIELDS.Y, currentY);

                if ((lastY != null) && (lastY > currentY)) {
                    calculateStation(currentProfile);
                    profiles.put(--artificialStationId, currentProfile);
                    ++qPNumber;
                    currentProfile = new ArrayList<>();
                    line.setField(GAF_FIELDS.Y, 0.0);
                    lastY = 0.0;
                    lastPoint = currentPoint;
                } else if (lastY == null) {
                    lastY = currentY;
                    lastPoint = currentPoint;
                } else {
                    lastY = currentY;
                }

                currentProfile.add(line);
            }
            ++qPNumber;
            profiles.put(--artificialStationId, currentProfile);
            routes = null;
        } else if (fieldMap.containsKey(GAF_FIELDS.STATION)) {
            // RW/HW are not given, so they will be calculated. If RW/HW is given, y will be calculated
            // Punktwolke 1
            final List<ProfileLine> parts = new ArrayList<>();
            setCalc(true);
            Double lastY = null;
            Double lastStat = null;
            ArrayList<ProfileLine> currentProfile = new ArrayList<>();

            for (final ProfileLine line : content) {
                final Double currentY = line.getFieldAsDouble(GAF_FIELDS.Y);
                final Double currentStat = line.getFieldAsDouble(GAF_FIELDS.STATION);

                if ((lastStat == null) || lastStat.equals(currentStat)) {
                    parts.add(line);
                } else {
                    processParts(parts, lastY, lastStat);
                    ++qPNumber;
                    profiles.put(lastStat, currentProfile);
                    currentProfile = new ArrayList<>();
                    parts.clear();
                    parts.add(line);
                }

                lastY = currentY;
                lastStat = currentStat;
                currentProfile.add(line);
            }

            processParts(parts, lastY, lastStat);
            profiles.put(lastStat, currentProfile);
            ++qPNumber;
        }

        return checkData();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private QpCheckResult checkData() {
        final QpCheckResult result = new QpCheckResult();
        int profCount = 0;
        int incorrect = 0;

        String currentStat = null;
        boolean errorInCurrentQp = false;
        int lineNumber = (hasHeaderField() ? 2 : 1);
        final boolean addProfile = profiles.isEmpty();
        ArrayList<ProfileLine> currentProfile = new ArrayList<>();

        for (final ProfileLine line : content) {
            if (fieldMap.containsKey(GAF_FIELDS.HW) && fieldMap.containsKey(GAF_FIELDS.RW)
                        && fieldMap.containsKey(GAF_FIELDS.STATION)) {
                line.setBezug(lageBezug);

                if ((currentStat == null) || !currentStat.equals(line.getField(GAF_FIELDS.STATION))) {
                    ++profCount;
                    if (addProfile && !currentProfile.isEmpty()) {
                        profiles.put(currentProfile.get(0).getFieldAsDouble(GAF_FIELDS.STATION), currentProfile);
                    }
                    currentProfile = new ArrayList<>();
                    currentProfile.add(line);
                } else {
                    currentProfile.add(line);
                }

                if ((currentStat != null) && currentStat.equals(line.getField(GAF_FIELDS.STATION))
                            && errorInCurrentQp) {
                    continue;
                } else if (errorInCurrentQp) {
                    errorInCurrentQp = false;
                }

                try {
                    currentStat = line.getField(GAF_FIELDS.STATION);
                    final String rw = line.getField(GAF_FIELDS.RW).replace(',', '.');
                    final String hw = line.getField(GAF_FIELDS.HW).replace(',', '.');

                    final Double rwD = Double.parseDouble(rw);
                    final Double hwD = Double.parseDouble(hw);

                    if ((rwD < 33000000) || (rwD > 33999999) || (hwD < 5600000) || (hwD > 6399999)) {
                        final QpCheckResult.ErrorResult error = new QpCheckResult.ErrorResult();
                        error.setErrorText("Der Datensatz mit der ID " + line.getField(GAF_FIELDS.ID)
                                    + " hat eine ungültige Position");
                        error.setLine(lineNumber);
                        result.addErrors(error);
                        ++incorrect;
                        errorInCurrentQp = true;
                    }
                } catch (Throwable e) {
                    final QpCheckResult.ErrorResult error = new QpCheckResult.ErrorResult();
                    error.setErrorText("Der Datensatz mit der ID " + line.getField(GAF_FIELDS.ID)
                                + " hat eine ungültige Position");
                    error.setLine(lineNumber);
                    result.addErrors(error);
                    ++incorrect;
                    errorInCurrentQp = true;
                }
            }

            ++lineNumber;
        }
        if (addProfile) {
            profiles.put(currentProfile.get(0).getFieldAsDouble(GAF_FIELDS.STATION), currentProfile);
        }

        result.setIncorrect(incorrect);
        result.setCorrect(profCount - incorrect);

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parts  DOCUMENT ME!
     * @param  maxY   DOCUMENT ME!
     * @param  stat   DOCUMENT ME!
     */
    private void processParts(final List<ProfileLine> parts, final Double maxY, final Double stat) {
        final Geometry routeLine = lawaRoute.getGeometry();
        final LengthIndexedLine lil = new LengthIndexedLine(routeLine);
        final Coordinate point = lil.extractPoint(stat);
        final LocationIndexedLine lineLIL = new LocationIndexedLine(routeLine);
        final LinearLocation loc = lineLIL.indexOf(point);

        for (final ProfileLine part : parts) {
            final Coordinate partPoint = lineLIL.extractPoint(loc, part.getFieldAsDouble(GAF_FIELDS.Y) - (maxY / 2));
            part.setField(GAF_FIELDS.RW, partPoint.x);
            part.setField(GAF_FIELDS.HW, partPoint.y);
        }
//        final LocationIndexedLine lineLIL = new LocationIndexedLine(routeLine);
//        final LengthLocationMap lineLLM = new LengthLocationMap(routeLine);
//        lineLIL.extractPoint(new LinearLocation(0, 0))
//        final LinearLocation pointLL = lineLIL.indexOf(pointCoord);
//        final double pointPosition = lineLLM.getLength(pointLL);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parts  DOCUMENT ME!
     */
    private void calculateStation(final List<ProfileLine> parts) {
        try {
            final Coordinate[] c = new Coordinate[parts.size()];

            for (int i = 0; i < parts.size(); ++i) {
                c[i] = new Coordinate(parts.get(i).getFieldAsDouble(GAF_FIELDS.RW),
                        parts.get(i).getFieldAsDouble(GAF_FIELDS.HW));
            }
            final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 0);
            final Geometry line = gf.createLineString(c);

            MetaObject[] metaObjects = routes;
            int index = intersects(metaObjects, line);

            if ((metaObjects == null) || (index == -1)) {
                String query = "select " + GEOM_MC.getID() + ", geom." + GEOM_MC.getPrimaryKey() + " from "
                            + ROUTE_MC.getTableName();
                query += " fg join " + GEOM_MC.getTableName()
                            + " geom on (fg.geom = geom.id) where st_intersects(geom.geo_field, '" + line.toText()
                            + "') "
                            + " order by abs(0.5 - ST_LineLocatePoint('" + line.toText()
                            + "', st_geometryN(st_intersection('"
                            + line.toText() + "', geom.geo_field)), 1)";

                metaObjects = SessionManager.getProxy().getMetaObjectByQuery(query, 0, CC);
                routes = metaObjects;
                index = 0;
            }

            if ((metaObjects != null) && (metaObjects.length > 0)) {
                final MetaObject routeGeom = metaObjects[index];
                final Geometry g = (Geometry)routeGeom.getBean().getProperty("geo_field");
                final LocationIndexedLine lineLIL = new LocationIndexedLine(g);
                final LinearLocation loc = lineLIL.indexOf(line.intersection(g).getCoordinate());
                final double pos = loc.getSegmentFraction() * g.getLength();

                for (final ProfileLine part : parts) {
                    part.setField(GAF_FIELDS.STATION, pos);
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot retrieve route", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaObjects  DOCUMENT ME!
     * @param   line         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int intersects(final MetaObject[] metaObjects, final Geometry line) {
        if ((metaObjects != null) && (metaObjects.length > 0)) {
            for (int i = 0; i < metaObjects.length; ++i) {
                final CidsBean geometry = metaObjects[i].getBean();

                if (geometry.getProperty("geo_field") != null) {
                    final Geometry route = (Geometry)geometry.getProperty("geo_field");

                    if (route.intersects(line)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    @Override
    public AbstractImportDialog getImportDialog(final Frame parent) {
        return new CsvWprofImportDialog(parent, true, getColumnProposal(), this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fieldMap     DOCUMENT ME!
     * @param  headerField  DOCUMENT ME!
     */
    public void setHeader(final Map<ProfileReader.GAF_FIELDS, Integer> fieldMap, final boolean headerField) {
        this.fieldMap = fieldMap;
        this.headerField = headerField;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[] getColumnProposal() {
        if ((csvFile != null) && csvFile.exists()) {
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new FileReader(csvFile));
                final String headerLine = reader.readLine();

                if (headerLine != null) {
                    final StringTokenizer st = new StringTokenizer(headerLine, " \t");
                    final String[] columns = new String[st.countTokens()];
                    int i = 0;

                    while (st.hasMoreTokens()) {
                        final String tmp = st.nextToken();

                        columns[i++] = determineHeaderField(tmp);
                    }

                    return columns;
                }
            } catch (IOException e) {
                LOG.error("Error while reading the header line", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        // nop
                    }
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     */
    private void readFile() {
        if ((csvFile != null) && csvFile.exists()) {
            BufferedReader reader = null;
            content = new ArrayList<>();

            try {
                reader = new BufferedReader(new FileReader(csvFile));
                String line;

                if (headerField) {
                    reader.readLine();
                }

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    if (!duplicateSepAllowed) {
                        content.add(new ProfileLine(fieldMap, line.split("\\s")));
                    } else {
                        content.add(new ProfileLine(fieldMap, line.split("\\s+")));
                    }
                }

                if (fieldMap.size() == 3) {
                }
            } catch (IOException e) {
                LOG.error("Error while reading the header line", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        // nop
                    }
                }
            }
            for (final ProfileLine line : content) {
                line.setBezug(lageBezug);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[][] readExampleData() {
        final List<String[]> data = new ArrayList<String[]>();

        if ((csvFile != null) && csvFile.exists()) {
            BufferedReader reader = null;
            content = new ArrayList<>();

            try {
                reader = new BufferedReader(new FileReader(csvFile));
                String line;
                final int rows = 0;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    if (!duplicateSepAllowed) {
                        data.add(line.split("\\s"));
                    } else {
                        data.add(line.split("\\s+"));
                    }
                }
            } catch (IOException e) {
                LOG.error("Error while reading the header line", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        // nop
                    }
                }
            }
        }

        return data.toArray(new String[data.size()][]);
    }

    /**
     * DOCUMENT ME!
     */
    private void analyseFile() {
        this.columnProposal = getColumnProposal();
        int fieldrecognisedCount = 0;

        for (int i = 0; i < columnProposal.length; ++i) {
            if (columnProposal[i] != null) {
                ++fieldrecognisedCount;
            }
        }

        headerField = ((double)fieldrecognisedCount / columnProposal.length) > 0.5;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String determineHeaderField(final String name) {
        if (name.equalsIgnoreCase(GAF_FIELDS.BK.name())) {
            return GAF_FIELDS.BK.name();
        } else if (name.equalsIgnoreCase(GAF_FIELDS.HW.name())) {
            return GAF_FIELDS.HW.name();
        } else if (name.equalsIgnoreCase(GAF_FIELDS.HYK.name())) {
            return GAF_FIELDS.HYK.name();
        } else if (name.equalsIgnoreCase(GAF_FIELDS.ID.name())) {
            return GAF_FIELDS.ID.name();
        } else if (name.equalsIgnoreCase(GAF_FIELDS.KZ.name())) {
            return GAF_FIELDS.KZ.name();
        } else if (name.equalsIgnoreCase(GAF_FIELDS.RK.name())) {
            return GAF_FIELDS.RK.name();
        } else if (name.equalsIgnoreCase(GAF_FIELDS.RW.name())) {
            return GAF_FIELDS.RW.name();
        } else if (name.equalsIgnoreCase(GAF_FIELDS.STATION.name()) || name.toLowerCase().startsWith("st")) {
            return GAF_FIELDS.STATION.name();
        } else if (name.equalsIgnoreCase(GAF_FIELDS.Y.name())) {
            return GAF_FIELDS.Y.name();
        } else if (name.equalsIgnoreCase(GAF_FIELDS.Z.name())) {
            return GAF_FIELDS.Z.name();
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasHeaderField() {
        return headerField;
    }
}
