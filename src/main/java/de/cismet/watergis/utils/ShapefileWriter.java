/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package de.cismet.watergis.utils;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.EndianDataOutputStream;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPWriter;
import com.vividsolutions.jump.workbench.ui.OKCancelDialog;

import org.geotools.dbffile.DbfFieldDef;
import org.geotools.dbffile.DbfFile;
import org.geotools.dbffile.DbfFileWriter;
import org.geotools.shapefile.Shapefile;

import java.io.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import java.net.URL;

import java.nio.charset.Charset;

import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;

import de.cismet.cismap.commons.util.FilePersistenceManager;

import de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet;

/**
 * ShapefileWriter is a {@link JUMPWriter} specialized to write Shapefiles.
 *
 * <p>DataProperties for the ShapefileWriter write(DataProperties) interface:<br>
 * <br>
 * </p>
 *
 * <table border='1' cellspacing='0' cellpadding='4'>
 *   <tr>
 *     <th>Parameter</th>
 *     <th>Meaning</th>
 *   </tr>
 *   <tr>
 *     <td>OutputFile or DefaultValue</td>
 *     <td>File name for the output .shp file</td>
 *   </tr>
 *   <tr>
 *     <td>ShapeType</td>
 *     <td>Dimentionality of the Shapefile - 'xy', 'xym' or 'xyz'. 'xymz' and 'xyzm' are the same as 'xyz'</td>
 *   </tr>
 * </table>
 * <br>
 *
 * <p>NOTE: The input .dbf and .shx is assumed to be 'beside' (in the same directory) as the .shp file.</p>
 *
 * <p>The shapefile writer consists of two parts: writing attributes (.dbf) and writing geometries (.shp).</p>
 *
 * <p>JUMP columns are converted to DBF columns by:</p>
 *
 * <table border='1' cellspacing='0' cellpadding='4'>
 *   <tr>
 *     <th>JUMP Column</th>
 *     <th>DBF column</th>
 *   </tr>
 *   <tr>
 *     <td>STRING</td>
 *     <td>Type 'C' length is size of longest string in the FeatureCollection</td>
 *   </tr>
 *   <tr>
 *     <td>DOUBLE</td>
 *     <td>Type 'N' length is 33, with 16 digits right of the decimal</td>
 *   </tr>
 *   <tr>
 *     <td>INTEGER</td>
 *     <td>Type 'N' length is 16, with 0 digits right of the decimal</td>
 *   </tr>
 * </table>
 *
 * <p>For more information on the DBF file format, see the <a target='_new'
 * href='http://www.apptools.com/dbase/faq/qformt.htm'>DBF Specification FAQ</a></p>
 *
 * <p>Since shape files may contain only one type of geometry (POINT, MULTPOINT, POLYLINE, POLYGON, POINTM, MULTPOINTM,
 * POLYLINEM, POLYGONM, POINTZ, MULTPOINTZ, POLYLINEZ, or POLYGONZ), the FeatureCollection must be first be normalized
 * to one type:</p>
 *
 * <table border='1' cellspacing='0' cellpadding='4'>
 *   <tr>
 *     <th>First non-NULL non-Point geometry in FeatureCollection</th>
 *     <th>Coordinate Dimensionality</th>
 *     <th>Shape Type</th>
 *   </tr>
 *   <tr>
 *     <td>MULTIPOINT</td>
 *     <td>xy xym xyzm</td>
 *     <td>MULTIPOINT MULTIPOINTM MULTIPOINTZ</td>
 *   </tr>
 *   <tr>
 *     <td>LINESTRING/MULTILINESTRING</td>
 *     <td>xy xym xyzm</td>
 *     <td>POLYLINE POLYLINEM POLYLINEZ</td>
 *   </tr>
 *   <tr>
 *     <td>POLYGON/MULTIPOLYGON</td>
 *     <td>xy xym xyzm</td>
 *     <td>POLYGON POLYGONM POLYGONZ</td>
 *   </tr>
 *   <tr>
 *     <th>All geometries in FeatureCollection are</th>
 *     <th>Coordinate Dimensionality</th>
 *     <th>Shape Type</th>
 *   </tr>
 *   <tr>
 *     <td>POINT</td>
 *     <td>xy xym xyzm</td>
 *     <td>POINT POINTM POINTZ</td>
 *   </tr>
 * </table>
 *
 * <p>During this normalization process any non-consistent geometry will be replaced by a NULL geometry.</p>
 *
 * <p>For example, if the shapetype is determined to be 'POLYLINE' any POINT, MULTIPOINT, or POLYGON geometries in the
 * FeatureCollection will be replaced with a NULL geometry.</p>
 *
 * <p>The coordinate dimensionality can be explicitly set with a DataProperties tag of 'ShapeType': 'xy', 'xym', or
 * 'xyz' ('xymz' and 'xyzm' are pseudonyms for 'xyz'). If this DataProperties is unspecified, it will be auto set to
 * 'xy' or 'xyz' based on the first non-NULL geometry having a Z coordinate.</p>
 *
 * <p>Since JUMP and JTS do not currently support a M (measure) coordinate, it will always be set to �10E40 in the shape
 * file (type 'xym' or 'xyzm'). This value represents the Measure "no data" value (page 2, ESRI Shapefile Technical
 * Description). Since the 'NaN' DOUBLE values for Z coordinates is invalid in a shapefile, it is converted to '0.0'.
 * </p>
 *
 * <p>For more information on the shapefile format, see the <a
 * href='http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf'>ESRI Shapefile Spec</a></p>
 *
 * <p><TODO> The link referencing the DBF format specification is broken - fix it!</TODO></p>
 *
 * @version  $Revision$, $Date$
 */
public class ShapefileWriter implements JUMPWriter {

    //~ Static fields/initializers ---------------------------------------------

    public static final String FILE_PROPERTY_KEY = "File";
    public static final String DEFAULT_VALUE_PROPERTY_KEY = "DefaultValue";
    public static final String SHAPE_TYPE_PROPERTY_KEY = "ShapeType";
    public static boolean truncate = false;
    private static long lastTimeTruncate = new Date(0).getTime();

    protected static CGAlgorithms cga = new RobustCGAlgorithms();

    //~ Instance fields --------------------------------------------------------

    private WatergisDefaultRuleSet ruleSet = null;
    private Map<String, String> aliasNameToNameMap = new HashMap<String, String>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new ShapefileWriter.
     */
    public ShapefileWriter() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Main method - write the featurecollection to a shapefile (2d, 3d or 4d).
     *
     * @param   featureCollection  collection to write
     * @param   dp                 'OutputFile' or 'DefaultValue' to specify where to write, and 'ShapeType' to specify
     *                             dimentionality.
     *
     * @throws  IllegalParametersException  DOCUMENT ME!
     * @throws  Exception                   DOCUMENT ME!
     */
    @Override
    public void write(final FeatureCollection featureCollection, final DriverProperties dp)
            throws IllegalParametersException, Exception {
        String shpfileName;
        final String dbffname;
        final String shxfname;

        final String path;
        final String fname;
        final String fname_withoutextention;
        int shapeType;
        int loc;

        final GeometryCollection gc;

        // sstein: check for mixed geometry types in the FC
        this.checkIfGeomsAreMixed(featureCollection);

        shpfileName = dp.getProperty(FILE_PROPERTY_KEY);

        if (shpfileName == null) {
            shpfileName = dp.getProperty(DEFAULT_VALUE_PROPERTY_KEY);
        }

        if (shpfileName == null) {
            throw new IllegalParametersException(I18N.get("io.ShapefileWriter.no-output-filename-specified"));
        }

        loc = shpfileName.lastIndexOf(File.separatorChar);

        if (loc == -1) {
            // probably using the wrong path separator character.
            throw new Exception(
                I18N.getMessage("io.ShapefileWriter.path-separator-not-found",
                    new Object[] { File.separatorChar }));
        } else {
            path = shpfileName.substring(0, loc + 1); // ie. "/data1/hills.shp" -> "/data1/"
            fname = shpfileName.substring(loc + 1);   // ie. "/data1/hills.shp" -> "hills.shp"
        }

        loc = fname.lastIndexOf(".");

        if (loc == -1) {
            throw new IllegalParametersException(I18N.get("io.ShapefileWriter.filename-must-end-in-shp"));
        }

        fname_withoutextention = fname.substring(0, loc); // ie. "hills.shp" -> "hills."
        dbffname = path + fname_withoutextention + ".dbf";

        String charsetName = dp.getProperty("charset");
        if (charsetName == null) {
            charsetName = Charset.defaultCharset().name();
        }
        writeDbf(featureCollection, dbffname, Charset.forName(charsetName));

        // this gc will be a collection of either multi-points, multi-polygons, or multi-linestrings
        // polygons will have the rings in the correct order
        gc = makeSHAPEGeometryCollection(featureCollection);

        shapeType = 2; // x,y

        if (dp.getProperty(SHAPE_TYPE_PROPERTY_KEY) != null) {
            final String st = dp.getProperty(SHAPE_TYPE_PROPERTY_KEY);

            if (st.equalsIgnoreCase("xy")) {
                shapeType = 2;
            } else if (st.equalsIgnoreCase("xym")) {
                shapeType = 3;
            } else if (st.equalsIgnoreCase("xymz")) {
                shapeType = 4;
            } else if (st.equalsIgnoreCase("xyzm")) {
                shapeType = 4;
            } else if (st.equalsIgnoreCase("xyz")) {
                shapeType = 4;
            } else {
                throw new IllegalParametersException(
                    I18N.get("io.ShapefileWriter.unknown-type"));
            }
        } else {
            if (gc.getNumGeometries() > 0) {
                shapeType = guessCoordinateDims(gc.getGeometryN(0));
            }
        }

        final URL url = new URL("file", "localhost", shpfileName);
        final Shapefile myshape = new Shapefile(url);
        myshape.write(gc, shapeType);

        shxfname = path + fname_withoutextention + ".shx";

        final BufferedOutputStream in = new BufferedOutputStream(new FileOutputStream(
                    shxfname));
        final EndianDataOutputStream sfile = new EndianDataOutputStream(in);

        myshape.writeIndex(gc, sfile, shapeType);
        // If long fields have been truncated, remember the end process timestamp
        if (truncate) {
            lastTimeTruncate = new Date().getTime();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ruleSet             DOCUMENT ME!
     * @param  aliasNameToNameMap  DOCUMENT ME!
     */
    public void setRuleSet(final WatergisDefaultRuleSet ruleSet, final Map<String, String> aliasNameToNameMap) {
        this.ruleSet = ruleSet;

        if (aliasNameToNameMap != null) {
            this.aliasNameToNameMap = aliasNameToNameMap;
        }
    }

    /**
     * Main method - write the featurecollection to a shapefile (2d, 3d or 4d).
     *
     * @param   featureCollection  collection to write
     * @param   dp                 'OutputFile' or 'DefaultValue' to specify where to write, and 'ShapeType' to specify
     *                             dimentionality.
     *
     * @throws  IllegalParametersException  DOCUMENT ME!
     * @throws  Exception                   DOCUMENT ME!
     * @throws  InterruptedException        DOCUMENT ME!
     */
    public void writePersistentFeatures(final FeatureCollection featureCollection, final DriverProperties dp)
            throws IllegalParametersException, Exception, InterruptedException {
        String shpfileName;
        final String dbffname;
        final String shxfname;

        final String path;
        final String fname;
        final String fname_withoutextention;
        int shapeType;
        int loc;

        final GeometryCollection gc;

        // sstein: check for mixed geometry types in the FC. This takes too long for large exports
// this.checkIfGeomsAreMixed(featureCollection);

        shpfileName = dp.getProperty(FILE_PROPERTY_KEY);

        if (shpfileName == null) {
            shpfileName = dp.getProperty(DEFAULT_VALUE_PROPERTY_KEY);
        }

        if (shpfileName == null) {
            throw new IllegalParametersException(I18N.get("io.ShapefileWriter.no-output-filename-specified"));
        }

        loc = shpfileName.lastIndexOf(File.separatorChar);

        if (loc == -1) {
            // probably using the wrong path separator character.
            throw new Exception(
                I18N.getMessage("io.ShapefileWriter.path-separator-not-found",
                    new Object[] { File.separatorChar }));
        } else {
            path = shpfileName.substring(0, loc + 1); // ie. "/data1/hills.shp" -> "/data1/"
            fname = shpfileName.substring(loc + 1);   // ie. "/data1/hills.shp" -> "hills.shp"
        }

        loc = fname.lastIndexOf(".");

        if (loc == -1) {
            throw new IllegalParametersException(I18N.get("io.ShapefileWriter.filename-must-end-in-shp"));
        }

        fname_withoutextention = fname.substring(0, loc); // ie. "hills.shp" -> "hills."
        dbffname = path + fname_withoutextention + ".dbf";

        String charsetName = dp.getProperty("charset");
        if (charsetName == null) {
            charsetName = Charset.defaultCharset().name();
        }
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        writeDbf(featureCollection, dbffname, Charset.forName(charsetName));
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        // this gc will be a collection of either multi-points, multi-polygons, or multi-linestrings
        // polygons will have the rings in the correct order
        gc = makePersistentSHAPEGeometryCollection(featureCollection);
        if (Thread.interrupted() || (gc == null)) {
            throw new InterruptedException();
        }

        shapeType = 2; // x,y

        if (dp.getProperty(SHAPE_TYPE_PROPERTY_KEY) != null) {
            final String st = dp.getProperty(SHAPE_TYPE_PROPERTY_KEY);

            if (st.equalsIgnoreCase("xy")) {
                shapeType = 2;
            } else if (st.equalsIgnoreCase("xym")) {
                shapeType = 3;
            } else if (st.equalsIgnoreCase("xymz")) {
                shapeType = 4;
            } else if (st.equalsIgnoreCase("xyzm")) {
                shapeType = 4;
            } else if (st.equalsIgnoreCase("xyz")) {
                shapeType = 4;
            } else {
                throw new IllegalParametersException(
                    I18N.get("io.ShapefileWriter.unknown-type"));
            }
        } else {
            if (gc.getNumGeometries() > 0) {
                shapeType = guessCoordinateDims(gc.getGeometryN(0));
            }
        }

        final URL url = new URL("file", "localhost", shpfileName);
        final Shapefile myshape = new Shapefile(url);
        myshape.write(gc, shapeType);
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        shxfname = path + fname_withoutextention + ".shx";

        final BufferedOutputStream in = new BufferedOutputStream(new FileOutputStream(
                    shxfname));
        final EndianDataOutputStream sfile = new EndianDataOutputStream(in);

        myshape.writeIndex(gc, sfile, shapeType);
        // If long fields have been truncated, remember the end process timestamp
        if (truncate) {
            lastTimeTruncate = new Date().getTime();
        }
    }

    /**
     * Returns:<br>
     * 2 for 2d (default)<br>
     * 4 for 3d - one of the oordinates has a non-NaN z value<br>
     * (3 is for x,y,m but thats not supported yet)<br>
     *
     * @param   g  geometry to test - looks at 1st coordinate
     *
     * @return  DOCUMENT ME!
     */
    public int guessCoordinateDims(final Geometry g) {
        final Coordinate[] cs = g.getCoordinates();

        for (int t = 0; t < cs.length; t++) {
            if (!(Double.isNaN(cs[t].z))) {
                return 4;
            }
        }

        return 2;
    }

    /**
     * Write a dbf file with the information from the featureCollection. For compatibilty reasons, this method is is now
     * a wrapper for the changed/new one with Charset functions.
     *
     * @param   featureCollection  DOCUMENT ME!
     * @param   fname              DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     *
     * @see     writeDbf(FeatureCollection featureCollection, String fname, Charset charset)
     */
    void writeDbf(final FeatureCollection featureCollection, final String fname) throws Exception {
        writeDbf(featureCollection, fname, Charset.defaultCharset());
    }

    /**
     * Write a dbf file with the information from the featureCollection.
     *
     * @param   featureCollection  column data from collection
     * @param   fname              name of the dbf file to write to July 2, 2010 - modified by beckerl to read existing
     *                             dbf file header and use the existing numeric field definitions.
     * @param   charset            DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void writeDbf(final FeatureCollection featureCollection, final String fname, final Charset charset)
            throws Exception {
        final DbfFileWriter dbf;
        final FeatureSchema fs;
        int t;
        int f;
        int u;
        final int num;

        final HashMap fieldMap = null;
//        if (new File(fname).exists()) {
//            final DbfFile dbfFile = new DbfFile(fname);
//            final int numFields = dbfFile.getNumFields();
//            fieldMap = new HashMap(numFields);
//            for (int i = 0; i < numFields; i++) {
//                final String fieldName = dbfFile.getFieldName(i);
//                fieldMap.put(fieldName, dbfFile.fielddef[i]);
//            }
//            dbfFile.close();
//        }

        fs = featureCollection.getFeatureSchema();

        // -1 because one of the columns is geometry
        final DbfFieldDef[] fields = new DbfFieldDef[fs.getAttributeCount() - 1];
        final Map<String, Integer> nameToIndex = new HashMap<String, Integer>();
        // dbf column type and size
        f = 0;

        for (t = 0; t < fs.getAttributeCount(); t++) {
            final AttributeType columnType = fs.getAttributeType(t);
            final String columnName = fs.getAttributeName(t);
            nameToIndex.put(columnName, f);

            if (columnType == AttributeType.INTEGER) {
                if (getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.Numeric) {
                    final WatergisDefaultRuleSet.Numeric type = (WatergisDefaultRuleSet.Numeric)getTypeFromRuleSet(
                            columnName);
                    fields[f] = new DbfFieldDef(columnName.toUpperCase(), 'N', type.getPrecision(), 0); // LDB: previously 16
                } else if (getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.SubDataType) {
                    final WatergisDefaultRuleSet.SubDataType cat = (WatergisDefaultRuleSet.SubDataType)
                        getTypeFromRuleSet(
                            columnName);

                    if ((cat.getDataType() != null) && (cat.getDataType() instanceof WatergisDefaultRuleSet.Numeric)) {
                        final WatergisDefaultRuleSet.Numeric type = (WatergisDefaultRuleSet.Numeric)cat.getDataType();
                        fields[f] = new DbfFieldDef(columnName.toUpperCase(), 'N', type.getPrecision(), 0); // LDB: previously 16
                    } else {
                        fields[f] = new DbfFieldDef(columnName.toUpperCase(), 'N', 11, 0);                  // LDB: previously 16
                    }
                } else {
                    fields[f] = new DbfFieldDef(columnName.toUpperCase(), 'N', 11, 0);                      // LDB: previously 16
                }
                fields[f] = overrideWithExistingCompatibleDbfFieldDef(fields[f], fieldMap);
                f++;
            } else if (columnType == AttributeType.DOUBLE) {
                if (getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.Numeric) {
                    final WatergisDefaultRuleSet.Numeric type = (WatergisDefaultRuleSet.Numeric)getTypeFromRuleSet(
                            columnName);
                    // if the number has decimals, then the decimal character will decrease the digits of the number
                    // by one
                    final int lengthOfDecimalCharacter = (int)Math.signum((double)type.getScale());
                    fields[f] = new DbfFieldDef(
                            columnName.toUpperCase(),
                            'N',
                            type.getPrecision()
                                    + lengthOfDecimalCharacter,
                            type.getScale()); // LDB: previously 16
                } else if (getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.SubDataType) {
                    final WatergisDefaultRuleSet.SubDataType cat = (WatergisDefaultRuleSet.SubDataType)
                        getTypeFromRuleSet(
                            columnName);

                    if ((cat.getDataType() != null) && (cat.getDataType() instanceof WatergisDefaultRuleSet.Numeric)) {
                        final WatergisDefaultRuleSet.Numeric type = (WatergisDefaultRuleSet.Numeric)cat.getDataType();
                        final int lengthOfDecimalCharacter = (int)Math.signum((double)type.getScale());
                        fields[f] = new DbfFieldDef(
                                columnName.toUpperCase(),
                                'N',
                                type.getPrecision()
                                        + lengthOfDecimalCharacter,
                                type.getScale()); // LDB: previously 16
                    } else {
                        fields[f] = new DbfFieldDef(columnName.toUpperCase(), 'N', 33, 16);
                    }
                } else {
                    fields[f] = new DbfFieldDef(columnName.toUpperCase(), 'N', 33, 16);
                }
                fields[f] = overrideWithExistingCompatibleDbfFieldDef(fields[f], fieldMap);
                f++;
            } else if ((columnType == AttributeType.OBJECT)
                        && ((getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.Numeric)
                            || (getSubType(getTypeFromRuleSet(columnName)) instanceof WatergisDefaultRuleSet.Numeric))) {
                // numeric value
                final WatergisDefaultRuleSet.DataType type = getTypeFromRuleSet(columnName);
                WatergisDefaultRuleSet.Numeric numericType = null;

                if (type instanceof WatergisDefaultRuleSet.SubDataType) {
                    numericType = (WatergisDefaultRuleSet.Numeric)getSubType(type);
                } else {
                    numericType = (WatergisDefaultRuleSet.Numeric)type;
                }

                final int lengthOfDecimalCharacter = (int)Math.signum((double)numericType.getScale());
                fields[f] = new DbfFieldDef(
                        columnName.toUpperCase(),
                        'N',
                        numericType.getPrecision()
                                + lengthOfDecimalCharacter,
                        numericType.getScale()); // LDB: previously 16
                fields[f] = overrideWithExistingCompatibleDbfFieldDef(fields[f], fieldMap);
                f++;
            } else if ((columnType == AttributeType.OBJECT) && isBigDecimal(featureCollection, t)) {
                final int[] places = determinePrecision(featureCollection, t);
                final int precision = places[0];
                final int scale = places[1];

                final int lengthOfDecimalCharacter = (int)Math.signum((double)scale);
                fields[f] = new DbfFieldDef(
                        columnName.toUpperCase(),
                        'N',
                        precision
                                + lengthOfDecimalCharacter,
                        scale);                                                   // LDB: previously 16
                fields[f] = overrideWithExistingCompatibleDbfFieldDef(fields[f], fieldMap);
                f++;
            } else if ((columnType == AttributeType.STRING)
                        && (getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.Bool)) {
                fields[f] = new DbfFieldDef(columnName.toUpperCase(), 'N', 1, 0); // LDB: previously 16
                fields[f] = overrideWithExistingCompatibleDbfFieldDef(fields[f], fieldMap);
                f++;
            } else if (columnType == AttributeType.STRING) {
                int maxlength;

                if (getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.Varchar) {
                    final WatergisDefaultRuleSet.Varchar type = (WatergisDefaultRuleSet.Varchar)getTypeFromRuleSet(
                            columnName);
                    maxlength = type.getMaxLength();
                } else if (getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.SubDataType) {
                    final WatergisDefaultRuleSet.SubDataType cat = (WatergisDefaultRuleSet.SubDataType)
                        getTypeFromRuleSet(
                            columnName);
                    if ((cat.getDataType() != null) && (cat.getDataType() instanceof WatergisDefaultRuleSet.Varchar)) {
                        final WatergisDefaultRuleSet.Varchar type = (WatergisDefaultRuleSet.Varchar)cat.getDataType();
                        maxlength = type.getMaxLength();
                    } else {
                        maxlength = findMaxStringLength(featureCollection, t);
                    }
                } else if (getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.DateType) {
                    maxlength = 10;
                } else if (getTypeFromRuleSet(columnName) instanceof WatergisDefaultRuleSet.DateTime) {
                    maxlength = 26;
                } else {
                    maxlength = findMaxStringLength(featureCollection, t);
                }

                if (maxlength > 255) {
                    // If truncate option has been applied for less than 30 s
                    // automatically switch to truncate option
                    if ((new Date().getTime() - lastTimeTruncate) < 30000) {
                        maxlength = 255;
                    } else {
                        final OKCancelDialog okCancelDialog = getLongFieldManagementDialogBox();
                        okCancelDialog.setLocationRelativeTo(null);
                        okCancelDialog.setVisible(true);
                        if (okCancelDialog.wasOKPressed()) {
                            maxlength = 255;
                            truncate = true;
                        } else {
                            truncate = false;
                            throw new Exception(
                                I18N.get("io.ShapefileWriter.export-cancelled")
                                        + " "
                                        + I18N.get("io.ShapefileWriter.more-than-255-characters-field-found"));
                        }
                    }
                }

                fields[f] = new DbfFieldDef(columnName.toUpperCase(), 'C', maxlength, 0);
                // fields[f] = overrideWithExistingCompatibleDbfFieldDef(fields[f], fieldMap);
                f++;
            } else if (columnType == AttributeType.DATE) {
                fields[f] = new DbfFieldDef(columnName.toUpperCase(), 'D', 8, 0);
                f++;
            } else if (columnType == AttributeType.GEOMETRY) {
                // do nothing - the .shp file handles this
            } else {
                throw new Exception(I18N.get("io.ShapefileWriter.unsupported-attribute-type"));
            }
        }

        // write header
        dbf = new DbfFileWriter(fname);
        dbf.setCharset(charset);
        dbf.writeHeader(fields, featureCollection.size());

        // write rows
        num = featureCollection.size();

        final List features = featureCollection.getFeatures();

        for (t = 0; t < num; t++) {
            // System.out.println("dbf: record "+t);
            final Feature feature = (Feature)features.get(t);
            final Vector DBFrow = new Vector();

            // make data for each column in this feature (row)
            for (u = 0; u < fs.getAttributeCount(); u++) {
                final AttributeType columnType = fs.getAttributeType(u);
                final String name = fs.getAttributeName(u);

                if (columnType == AttributeType.INTEGER) {
                    final Object a = feature.getAttribute(u);

                    if (a == null) {
                        DBFrow.add(null);
                    } else {
                        if (a instanceof Integer) {
                            DBFrow.add((Integer)a);
                        } else if (a instanceof Long) {
                            DBFrow.add(((Long)a));
                        }
                    }
                } else if (columnType == AttributeType.DOUBLE) {
                    final Object a = feature.getAttribute(u);

                    if (a == null) {
                        DBFrow.add(null);
                    } else {
                        final int index = nameToIndex.get(name);
                        if (fields[index].fieldnumdec > 0) {
                            final BigDecimal bd = new BigDecimal((Double)a);
                            final Double d = bd.setScale(fields[index].fieldnumdec, RoundingMode.HALF_UP).doubleValue();
                            DBFrow.add(d);
                        } else {
                            final long val = Math.round((Double)a);
                            DBFrow.add(val);
                        }
                    }
                } else if (columnType == AttributeType.DATE) {
                    final Object a = feature.getAttribute(u);

                    if (a == null) {
                        DBFrow.add("");
                    } else {
                        DBFrow.add(DbfFile.DATE_PARSER.format((Date)a));
                    }
                } else if (columnType == AttributeType.OBJECT) {
                    final Object a = feature.getAttribute(u);

                    if (a instanceof BigDecimal) {
                        DBFrow.add(((BigDecimal)a).doubleValue());
                    } else {
                        DBFrow.add(null);
                    }
                } else if ((columnType == AttributeType.STRING)
                            && (getTypeFromRuleSet(name) instanceof WatergisDefaultRuleSet.Bool)) {
                    final Object a = feature.getAttribute(u);

                    if (a instanceof Boolean) {
                        if (((Boolean)a)) {
                            DBFrow.add(1);
                        } else {
                            DBFrow.add(0);
                        }
                    } else if (a instanceof String) {
                        if (String.valueOf(a).equalsIgnoreCase("true")) {
                            DBFrow.add(1);
                        } else {
                            DBFrow.add(0);
                        }
                    } else {
                        DBFrow.add(null);
                    }
                } else if (columnType == AttributeType.STRING) {
                    final Object a = feature.getAttribute(u);

                    if (a == null) {
                        DBFrow.add(new String(""));
                    } else {
                        // MD 16 jan 03 - added some defensive programming
                        if (a instanceof String) {
                            DBFrow.add(a);
                        } else {
                            DBFrow.add(a.toString());
                        }
                    }
                }
            }

            dbf.writeRecord(DBFrow);
        }

        dbf.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureCollection  DOCUMENT ME!
     * @param   index              DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isBigDecimal(final FeatureCollection featureCollection, final int index) {
        for (final Object o : featureCollection.getFeatures()) {
            if (o instanceof Feature) {
                final Feature f = (Feature)o;
                final Object attr = f.getAttribute(index);

                if ((attr != null) && !(attr instanceof BigDecimal)) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureCollection  DOCUMENT ME!
     * @param   index              DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int[] determinePrecision(final FeatureCollection featureCollection, final int index) {
        int p = 1;
        int s = 0;

        for (final Object o : featureCollection.getFeatures()) {
            if (o instanceof Feature) {
                final Feature f = (Feature)o;
                final Object attr = f.getAttribute(index);

                if (attr instanceof BigDecimal) {
                    if (((BigDecimal)attr).precision() > p) {
                        p = ((BigDecimal)attr).precision();
                    }
                    if (((BigDecimal)attr).scale() > s) {
                        s = ((BigDecimal)attr).scale();
                    }
                }
            }
        }

        return new int[] { p, s };
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WatergisDefaultRuleSet.DataType getTypeFromRuleSet(final String columnName) {
        return (ruleSet == null) ? null : ruleSet.getType(coalesce(aliasNameToNameMap.get(columnName), columnName));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   arg           DOCUMENT ME!
     * @param   defaultValue  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String coalesce(final String arg, final String defaultValue) {
        return (arg == null) ? defaultValue : arg;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WatergisDefaultRuleSet.DataType getSubType(final WatergisDefaultRuleSet.DataType type) {
        if (type instanceof WatergisDefaultRuleSet.SubDataType) {
            return ((WatergisDefaultRuleSet.SubDataType)type).getDataType();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field      DOCUMENT ME!
     * @param   columnMap  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private DbfFieldDef overrideWithExistingCompatibleDbfFieldDef(final DbfFieldDef field, final Map columnMap) {
        final String fieldname = field.fieldname.toString().trim();
        if ((columnMap != null) && (columnMap.containsKey(fieldname))) {
            final DbfFieldDef dbfFieldDef = (DbfFieldDef)columnMap.get(fieldname);
            dbfFieldDef.fieldname = field.fieldname; // must have null padded version to work
            switch (dbfFieldDef.fieldtype) {
                case 'C':
                case 'c': {
                    // character case not working yet
                    if (field.fieldtype == 'C') {
                        if (field.fieldlen > dbfFieldDef.fieldlen) { // allow string expansion if needed
                            return field;
                        } else {
                            dbfFieldDef.fieldtype = field.fieldtype;
                            return dbfFieldDef;
                        }
                    }
                    break;
                }
                case 'N':
                case 'n':
                case 'F':
                case 'f': {
                    if (field.fieldtype == 'N') {
                        dbfFieldDef.fieldtype = field.fieldtype;
                        return dbfFieldDef;
                    }
                    break;
                }
            }
        }
        return field;
    }

    /**
     * look at all the data in the column of the featurecollection, and find the largest string!
     *
     * @param   fc               features to look at
     * @param   attributeNumber  which of the column to test.
     *
     * @return  DOCUMENT ME!
     */
    int findMaxStringLength(final FeatureCollection fc, final int attributeNumber) {
        int l;
        int maxlen = 0;
        Feature f;

        for (final Iterator i = fc.iterator(); i.hasNext();) {
            f = (Feature)i.next();
            // patch from Hisaji Ono for Double byte characters
            if (f.getString(attributeNumber) != null) {
                l = f.getString(attributeNumber).getBytes().length;

                if (l > maxlen) {
                    maxlen = l;
                }
            }
        }

        return Math.max(1, maxlen); // LDB: don't allow zero length strings
    }

    /**
     * Find the generic geometry type of the feature collection. Simple method - find the 1st non null geometry and its
     * type is the generic type. returns 0 : only empty geometry collection<br>
     * 1 : only single points<br>
     * 3 : at least one line or multiline<br>
     * 5 : at least one polygon or multipolygon<br>
     * 8 : at least one multipoint<br>
     * 31 : only non empty geometry collection<br>
     *
     * @param   fc  feature collection containing tet geometries.
     *
     * @return  DOCUMENT ME!
     */
    int findBestGeometryType(final FeatureCollection fc) {
        Geometry geom;
        boolean onlyPoints = true;
        boolean onlyEmptyGeometryCollection = true;
        // [mmichaud 2007-06-12] : add the type variable to test if
        // all geometries are single Point
        // maybe it would be clearer using shapefile types integer for type

        for (final Iterator i = fc.iterator(); i.hasNext();) {
            geom = ((Feature)i.next()).getGeometry();

            if (onlyPoints && !(geom instanceof Point)) {
                onlyPoints = false;
            }

            if (onlyEmptyGeometryCollection && !(geom.isEmpty())) {
                onlyEmptyGeometryCollection = false;
            }

            if (geom instanceof MultiPoint) {
                return 8;
            }

            if (geom instanceof Polygon) {
                return 5;
            }

            if (geom instanceof MultiPolygon) {
                return 5;
            }

            if (geom instanceof LineString) {
                return 3;
            }

            if (geom instanceof MultiLineString) {
                return 3;
            }
        }

        if (onlyPoints) {
            return 1;
        } else if (onlyEmptyGeometryCollection) {
            return 0;
        } else {
            return 31;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureCollection  DOCUMENT ME!
     *
     * @throws  IllegalParametersException  DOCUMENT ME!
     * @throws  Exception                   DOCUMENT ME!
     */
    public void checkIfGeomsAreMixed(final FeatureCollection featureCollection) throws IllegalParametersException,
        Exception {
        // -- sstein: check first if features are of different geometry type.
        int i = 0;
        Class firstClass = null;
        Geometry firstGeom = null;
        // System.out.println("ShapeFileWriter: start mixed-geom-test");
        for (final Iterator iter = featureCollection.iterator(); iter.hasNext();) {
            final Feature myf = (Feature)iter.next();
            if (i == 0) {
                firstClass = myf.getGeometry().getClass();
                firstGeom = myf.getGeometry();
            } else {
                if (firstClass != myf.getGeometry().getClass()) {
                    // System.out.println("first test failed");
                    if ((firstGeom instanceof Polygon) && (myf.getGeometry() instanceof MultiPolygon)) {
                        // everything is ok
                    } else if ((firstGeom instanceof MultiPolygon) && (myf.getGeometry() instanceof Polygon)) {
                        // everything is ok
                    } else if ((firstGeom instanceof Point) && (myf.getGeometry() instanceof MultiPoint)) {
                        // everything is ok
                    } else if ((firstGeom instanceof MultiPoint) && (myf.getGeometry() instanceof Point)) {
                        // everything is ok
                    } else if ((firstGeom instanceof LineString) && (myf.getGeometry() instanceof MultiLineString)) {
                        // everything is ok
                    } else if ((firstGeom instanceof MultiLineString) && (myf.getGeometry() instanceof LineString)) {
                        // everything is ok
                    } else {
                        throw new IllegalParametersException(
                            I18N.get("io.ShapefileWriter.unsupported-mixed-geometry-type"));
                    }
                }
            }
            i++;
        }
    }

    /**
     * Reverses the order of points in lr (is CW -> CCW or CCW->CW).
     *
     * @param   lr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    LinearRing reverseRing(final LinearRing lr) {
        final int numPoints = lr.getNumPoints();
        final Coordinate[] newCoords = new Coordinate[numPoints];

        for (int t = 0; t < numPoints; t++) {
            newCoords[t] = lr.getCoordinateN(numPoints - t - 1);
        }

        return new LinearRing(newCoords, new PrecisionModel(), 0);
    }

    /**
     * make sure outer ring is CCW and holes are CW.
     *
     * @param   p  polygon to check
     *
     * @return  DOCUMENT ME!
     */
    Polygon makeGoodSHAPEPolygon(final Polygon p) {
        if (p.isEmpty()) {
            return p;
        }

        LinearRing outer;
        final LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
        Coordinate[] coords;

        coords = p.getExteriorRing().getCoordinates();

        if (cga.isCCW(coords)) {
            outer = reverseRing((LinearRing)p.getExteriorRing());
        } else {
            outer = (LinearRing)p.getExteriorRing();
        }

        for (int t = 0; t < p.getNumInteriorRing(); t++) {
            coords = p.getInteriorRingN(t).getCoordinates();
            if (!(cga.isCCW(coords))) {
                holes[t] = reverseRing((LinearRing)p.getInteriorRingN(t));
            } else {
                holes[t] = (LinearRing)p.getInteriorRingN(t);
            }
        }

        return new Polygon(outer, holes, new PrecisionModel(), 0);
    }

    /**
     * make sure outer ring is CCW and holes are CW for all the polygons in the Geometry.
     *
     * @param   mp  set of polygons to check
     *
     * @return  DOCUMENT ME!
     */
    MultiPolygon makeGoodSHAPEMultiPolygon(final MultiPolygon mp) {
        final MultiPolygon result;
        final Polygon[] ps = new Polygon[mp.getNumGeometries()];

        // check each sub-polygon
        for (int t = 0; t < mp.getNumGeometries(); t++) {
            ps[t] = makeGoodSHAPEPolygon((Polygon)mp.getGeometryN(t));
        }

        result = new MultiPolygon(ps, new PrecisionModel(), 0);

        return result;
    }

    /**
     * return a single geometry collection<Br>
     * result.GeometryN(i) = the i-th feature in the FeatureCollection<br>
     * All the geometry types will be the same type (ie. all polygons) - or they will be set to<br>
     * NULL geometries<br>
     * <br>
     * GeometryN(i) = {Multipoint,Multilinestring, or Multipolygon)<br>
     *
     * @param   fc  feature collection to make homogeneous
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public GeometryCollection makeSHAPEGeometryCollection(final FeatureCollection fc) throws Exception {
        final GeometryCollection result;
        final Geometry[] allGeoms = new Geometry[fc.size()];

        final int geomtype = findBestGeometryType(fc);

        if (geomtype == 31) {
            throw new Exception(
                I18N.get("io.ShapefileWriter.unsupported-geometry-collection"));
        }

        final List features = fc.getFeatures();

        for (int t = 0; t < features.size(); t++) {
            final Geometry geom = ((Feature)features.get(t)).getGeometry();

            switch (geomtype) {
                case 0: {
                    // empty geometry collection
                    // empty geometry collections are arbitrarily written in a Point shapefile
                    allGeoms[t] = geom.getFactory().createGeometryCollection(new Geometry[0]);
                    break;
                }

                case 1: {
                    // single point

                    if ((geom instanceof Point)) {
                        allGeoms[t] = (Point)geom;
                    } else {
                        allGeoms[t] = new Point(null, new PrecisionModel(), 0);
                    }

                    break;
                }

                case 8: {
                    // point

                    if ((geom instanceof Point)) {
                        // good!
                        final Point[] p = new Point[1];
                        p[0] = (Point)geom;

                        allGeoms[t] = new MultiPoint(p, new PrecisionModel(), 0);
                    } else if (geom instanceof MultiPoint) {
                        allGeoms[t] = geom;
                    } else {
                        allGeoms[t] = new MultiPoint(null, new PrecisionModel(), 0);
                    }

                    break;
                }

                case 3: {
                    // line

                    if ((geom instanceof LineString)) {
                        final LineString[] l = new LineString[1];
                        l[0] = (LineString)geom;

                        allGeoms[t] = new MultiLineString(l, new PrecisionModel(), 0);
                    } else if (geom instanceof MultiLineString) {
                        allGeoms[t] = geom;
                    } else {
                        allGeoms[t] = new MultiLineString(null,
                                new PrecisionModel(), 0);
                    }

                    break;
                }

                case 5: {
                    // polygon

                    if (geom instanceof Polygon) {
                        // good!
                        final Polygon[] p = new Polygon[1];
                        p[0] = (Polygon)geom;

                        allGeoms[t] = makeGoodSHAPEMultiPolygon(new MultiPolygon(
                                    p,
                                    new PrecisionModel(),
                                    0));
                    } else if (geom instanceof MultiPolygon) {
                        allGeoms[t] = makeGoodSHAPEMultiPolygon((MultiPolygon)geom);
                    } else {
                        allGeoms[t] = new MultiPolygon(null, new PrecisionModel(), 0);
                    }

                    break;
                }
            }
        }

        result = new GeometryCollection(allGeoms, new PrecisionModel(), 0);

        return result;
    }

    /**
     * return a single geometry collection<Br>
     * result.GeometryN(i) = the i-th feature in the FeatureCollection<br>
     * All the geometry types will be the same type (ie. all polygons) - or they will be set to<br>
     * NULL geometries<br>
     * <br>
     * GeometryN(i) = {Multipoint,Multilinestring, or Multipolygon)<br>
     *
     * @param   fc  feature collection to make homogeneous
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public GeometryCollection makePersistentSHAPEGeometryCollection(final FeatureCollection fc) throws Exception {
        final GeometryCollectionWrapper result;
        final PersistentGeometryWrapper[] allGeoms = new PersistentGeometryWrapper[fc.size()];

        final int geomtype = findBestGeometryType(fc);

        if (geomtype == 31) {
            throw new Exception(
                I18N.get("io.ShapefileWriter.unsupported-geometry-collection"));
        }

        final List features = fc.getFeatures();
        final FilePersistenceManager pm = ((JumpFeature)features.get(0)).getPersistenceManager();
        for (int t = 0; t < features.size(); t++) {
            if (Thread.interrupted()) {
                return null;
            }
            final Geometry geom = ((Feature)features.get(t)).getGeometry();

            switch (geomtype) {
                case 0: {
                    // empty geometry collection
                    // empty geometry collections are arbitrarily written in a Point shapefile
                    allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(),
                            geom.getFactory().createGeometryCollection(new Geometry[0]),
                            pm);
                    break;
                }

                case 1: {
                    // single point

                    if ((geom instanceof Point)) {
                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(), (Feature)features.get(t));
                    } else {
                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(),
                                new Point(null, new PrecisionModel(), 0),
                                pm);
                    }

                    break;
                }

                case 8: {
                    // point

                    if ((geom instanceof Point)) {
                        // good!
                        final Point[] p = new Point[1];
                        p[0] = (Point)geom;

                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(),
                                new MultiPoint(p, new PrecisionModel(), 0),
                                pm);
                    } else if (geom instanceof MultiPoint) {
                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(), (Feature)features.get(t));
                    } else {
                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(),
                                new MultiPoint(null, new PrecisionModel(), 0),
                                pm);
                    }

                    break;
                }

                case 3: {
                    // line

                    if ((geom instanceof LineString)) {
                        final LineString[] l = new LineString[1];
                        l[0] = (LineString)geom;

                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(),
                                new MultiLineString(l, new PrecisionModel(), 0),
                                pm);
                    } else if (geom instanceof MultiLineString) {
                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(), (Feature)features.get(t));
                    } else {
                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(),
                                new MultiLineString(null, new PrecisionModel(), 0),
                                pm);
                    }

                    break;
                }

                case 5: {
                    // polygon

                    if (geom instanceof Polygon) {
                        // good!
                        final Polygon[] p = new Polygon[1];
                        p[0] = (Polygon)geom;
                        final Geometry validGeom = makeGoodSHAPEMultiPolygon(new MultiPolygon(
                                    p,
                                    new PrecisionModel(),
                                    0));

                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(), validGeom, pm);
//                    allGeoms[t] = makeGoodSHAPEMultiPolygon(new MultiPolygon(
//                                p, new PrecisionModel(), 0));
                    } else if (geom instanceof MultiPolygon) {
                        final Geometry validGeom = makeGoodSHAPEMultiPolygon((MultiPolygon)geom);
                        if (validGeom.equalsExact(geom)) {
                            allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(), (Feature)features.get(t));
                        } else {
                            allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(), validGeom, pm);
                        }
//                    allGeoms[t] = makeGoodSHAPEMultiPolygon((MultiPolygon) geom);
                    } else {
                        allGeoms[t] = new PersistentGeometryWrapper(geom.getFactory(),
                                new MultiPolygon(null, new PrecisionModel(), 0),
                                pm);
//                    allGeoms[t] = new MultiPolygon(null, new PrecisionModel(), 0);
                    }

                    break;
                }
            }
        }

        result = new GeometryCollectionWrapper(allGeoms, new PrecisionModel(), 0);

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private OKCancelDialog getLongFieldManagementDialogBox() {
        return new OKCancelDialog((JFrame)null,
                I18N.get("io.ShapefileWriter.fields-too-long"),
                true,
                new JLabel(
                    "<html><br/>"
                            + I18N.get("io.ShapefileWriter.more-than-255-characters-field-found")
                            + "<br/><br/>"
                            + I18N.get("io.ShapefileWriter.truncate-option")
                            + "<br/></html>"),
                null);
    }
}
