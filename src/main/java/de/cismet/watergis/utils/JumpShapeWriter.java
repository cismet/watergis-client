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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
//import com.vividsolutions.jump.io.ShapefileWriter;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;

import java.math.BigDecimal;

import java.net.URL;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.PersistentFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.tools.FeatureTools;
import de.cismet.cismap.commons.tools.ShapeWriter;

import de.cismet.security.WebAccessManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(
    position = 5,
    service = ShapeWriter.class
)
public class JumpShapeWriter implements ShapeWriter {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(JumpShapeWriter.class);
    private static final boolean WRITE_META_PDF = true;
    private static final boolean WRITE_PRJ = true;
    private static final String PRJ_CONTENT =
        "PROJCS[\"ETRS_1989_UTM_Zone_33N\",GEOGCS[\"GCS_ETRS_1989\",DATUM[\"D_ETRS_1989\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",33500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",15.0],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]";
    private static final boolean DATE_AS_STRING = true;
    public static final String DEFAULT_GEOM_PROPERTY_NAME = "the_geom";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JumpShapeWriter object.
     */
    public JumpShapeWriter() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void writeShape(final FeatureServiceFeature[] features,
            final List<String[]> aliasAttributeList,
            final File fileToSaveTo) throws Exception {
        writeShpFile(features, fileToSaveTo, aliasAttributeList, null);
        if (WRITE_PRJ) {
            writePrjFile(fileToSaveTo);
        }

        if (WRITE_META_PDF && (features.length > 0)) {
            if ((features[0].getLayerProperties() != null)
                        && (features[0].getLayerProperties().getFeatureService() != null)) {
                writeMetaPdf(fileToSaveTo, features[0].getLayerProperties().getFeatureService());
            }
        }
    }

    @Override
    public void writeDbf(final FeatureServiceFeature[] features,
            final List<String[]> aliasAttributeList,
            final File fileToSaveTo) throws Exception {
        File tmpFile = fileToSaveTo;
        if (fileToSaveTo.getName().contains(".")
                    && fileToSaveTo.getName().substring(fileToSaveTo.getName().lastIndexOf(".")).equalsIgnoreCase(
                        ".dbf")) {
            tmpFile = new File(fileToSaveTo.getParent(),
                    fileToSaveTo.getName().substring(0, fileToSaveTo.getName().lastIndexOf("."))
                            + ".shp");
        } else if (!fileToSaveTo.getAbsolutePath().contains(".")) {
            tmpFile = new File(fileToSaveTo.getParent(),
                    fileToSaveTo.getName()
                            + ".shp");
        }
        writeShape(features, aliasAttributeList, tmpFile);

        String fileNameWithoutExt = fileToSaveTo.getAbsolutePath();

        if (fileToSaveTo.getAbsolutePath().contains(".")) {
            fileNameWithoutExt = fileToSaveTo.getAbsolutePath()
                        .substring(0, fileToSaveTo.getAbsolutePath().lastIndexOf("."));
        }

        String fileName = fileNameWithoutExt + ".shp";

        deleteFileIfExists(fileName);
        fileName = fileNameWithoutExt + ".shx";
        deleteFileIfExists(fileName);
        fileName = fileNameWithoutExt + ".prj";
        deleteFileIfExists(fileName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   features            DOCUMENT ME!
     * @param   file                DOCUMENT ME!
     * @param   aliasAttributeList  DOCUMENT ME!
     * @param   charset             DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void writeShpFile(final FeatureServiceFeature[] features,
            final File file,
            final List<String[]> aliasAttributeList,
            final String charset) throws Exception {
        final ShapefileWriter writer = new ShapefileWriter();
        final List<Feature> basicFeatures = cidsFeatures2BasicFeature(features, aliasAttributeList);
        final FeatureSchema schema = basicFeatures.get(0).getSchema();
        final FeatureDataset set = new FeatureDataset(basicFeatures, schema);
        final DriverProperties properties = new DriverProperties();

        // charset property can also be defined
        properties.set(ShapefileWriter.FILE_PROPERTY_KEY, file.getAbsolutePath());
        properties.set(ShapefileWriter.SHAPE_TYPE_PROPERTY_KEY, "xy");

        if (charset != null) {
            properties.set("charset", charset);
        }

        if (features[0] instanceof PersistentFeature) {
            writer.writePersistentFeatures(set, properties);
        } else {
            writer.write(set, properties);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file  DOCUMENT ME!
     */
    private void writePrjFile(final File file) {
        try {
            String fileWithoutExtension = file.getAbsolutePath();

            if (fileWithoutExtension.contains(".")) {
                fileWithoutExtension = fileWithoutExtension.substring(0, fileWithoutExtension.lastIndexOf("."));
            }

            final BufferedWriter bw = new BufferedWriter(new FileWriter(fileWithoutExtension + ".prj"));
            bw.write(PRJ_CONTENT);
            bw.close();
        } catch (Exception e) {
            LOG.error("Error while writing prj file");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file     DOCUMENT ME!
     * @param  service  DOCUMENT ME!
     */
    private void writeMetaPdf(final File file, final AbstractFeatureService service) {
        if (service instanceof CidsLayer) {
            final CidsLayer cl = (CidsLayer)service;
            final String link = cl.getMetaDocumentLink();

            if (link != null) {
                // write pdf
                BufferedInputStream bin = null;
                BufferedOutputStream out = null;

                try {
                    final URL u = new URL(link);
                    final InputStream in = WebAccessManager.getInstance().doRequest(u);
                    String fileWithoutExtension = file.getAbsolutePath();

                    if (fileWithoutExtension.contains(".")) {
                        fileWithoutExtension = fileWithoutExtension.substring(0, fileWithoutExtension.lastIndexOf("."));
                    }

                    bin = new BufferedInputStream(in);
                    out = new BufferedOutputStream(new FileOutputStream(fileWithoutExtension + ".pdf"));
                    final byte[] tmp = new byte[256];
                    int byteCount;

                    while ((byteCount = bin.read(tmp, 0, tmp.length)) != -1) {
                        out.write(tmp, 0, byteCount);
                    }
                } catch (Exception e) {
                    LOG.error("Error while downloading meta document.", e);
                } finally {
                    try {
                        if (bin != null) {
                            bin.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (final Exception ex) {
                        LOG.error("Cannot close stream", ex);
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   features            DOCUMENT ME!
     * @param   aliasAttributeList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<Feature> cidsFeatures2BasicFeature(final FeatureServiceFeature[] features,
            final List<String[]> aliasAttributeList) {
        final List<Feature> featureList = new ArrayList<Feature>();
        final Map<String, FeatureServiceAttribute> attributesMap = features[0].getLayerProperties()
                    .getFeatureService()
                    .getFeatureServiceAttributes();
        final FeatureSchema schema = createScheme(attributesMap, aliasAttributeList);
        List<String[]> names = aliasAttributeList;
        final boolean hasGeometry = hasGeometry(attributesMap, aliasAttributeList);
        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 0);
        final Geometry defaultGeom = gf.createPoint(new Coordinate(0, 0));

        if (names == null) {
            names = generateAliasAttributeList(attributesMap);
        }

        for (final FeatureServiceFeature f : features) {
            Feature bf = null;

            if (f instanceof PersistentFeature) {
                bf = new JumpFeature(f, schema);
            } else {
                bf = new BasicFeature(schema);

                for (final String[] name : names) {
                    Object value = f.getProperty(name[1]);

                    if (value instanceof Boolean) {
                        value = String.valueOf(value);
                    } else if (DATE_AS_STRING && ((value instanceof Timestamp) || (value instanceof Date))) {
                        value = String.valueOf(value);
                    }

                    if ((schema.getAttributeType(name[0]) == AttributeType.DOUBLE) && (value instanceof Integer)) {
                        value = ((Integer)value).doubleValue();
                    }

                    bf.setAttribute(name[0], value);
                }

                if (!hasGeometry) {
                    bf.setAttribute(DEFAULT_GEOM_PROPERTY_NAME, defaultGeom);
                }
            }

            featureList.add(bf);
        }

        return featureList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributes          DOCUMENT ME!
     * @param   aliasAttributeList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private FeatureSchema createScheme(final Map<String, FeatureServiceAttribute> attributes,
            final List<String[]> aliasAttributeList) {
        List<String[]> attributeNames = aliasAttributeList;
        final FeatureSchema schema = new FeatureSchema();

        if (aliasAttributeList == null) {
            attributeNames = generateAliasAttributeList(attributes);
        }

        if (!hasGeometry(attributes, aliasAttributeList)) {
            schema.addAttribute(DEFAULT_GEOM_PROPERTY_NAME, AttributeType.GEOMETRY);
        }

        for (final String[] name : attributeNames) {
            final FeatureServiceAttribute attr = attributes.get(name[1]);
            schema.addAttribute(name[0], getPropertyType(attr));
        }

        return schema;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributes          DOCUMENT ME!
     * @param   aliasAttributeList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean hasGeometry(final Map<String, FeatureServiceAttribute> attributes,
            final List<String[]> aliasAttributeList) {
        List<String[]> attributeNames = aliasAttributeList;
        boolean hasGeometry = false;

        if (aliasAttributeList == null) {
            attributeNames = generateAliasAttributeList(attributes);
        }

        for (final String[] name : attributeNames) {
            final FeatureServiceAttribute attr = attributes.get(name[1]);

            if (attr.isGeometry()) {
                hasGeometry = true;
                break;
            }
        }

        return hasGeometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributeMap  features DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String[]> generateAliasAttributeList(final Map<String, FeatureServiceAttribute> attributeMap) {
        final List<String[]> aliasAttrList = new ArrayList<String[]>();

        for (final Object key : attributeMap.keySet()) {
            final String[] aliasAttr = new String[2];
            aliasAttr[0] = key.toString();
            aliasAttr[1] = key.toString();
            aliasAttrList.add(aliasAttr);
        }

        return aliasAttrList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private AttributeType getPropertyType(final FeatureServiceAttribute attr) {
        if ((attr != null)) {
            if (attr.isGeometry()) {
                return AttributeType.GEOMETRY;
            }
            final Class cl = FeatureTools.getClass(attr);
            if (cl.equals(String.class)) {
                return AttributeType.STRING;
            } else if (cl.equals(Integer.class)) {
                return AttributeType.INTEGER;
            } else if (cl.equals(Long.class)) {
                return AttributeType.INTEGER;
            } else if (cl.equals(Double.class)) {
                return AttributeType.DOUBLE;
            } else if (cl.equals(Date.class)) {
                if (DATE_AS_STRING) {
                    return AttributeType.STRING;
                } else {
                    return AttributeType.DATE;
                }
            } else if (cl.equals(Boolean.class)) {
                return AttributeType.STRING;
            } else if (cl.equals(BigDecimal.class)) {
                return AttributeType.STRING;
            } else {
                return AttributeType.STRING;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fileName  DOCUMENT ME!
     */
    private void deleteFileIfExists(final String fileName) {
        final File fileToDelete = new File(fileName);

        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }
}
