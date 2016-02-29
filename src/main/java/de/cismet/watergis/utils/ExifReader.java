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
package de.cismet.watergis.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.io.File;
import java.io.IOException;

import java.util.Date;

/**
 * Reads exif meta data of image files.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExifReader {

    //~ Instance fields --------------------------------------------------------

    Metadata metadata;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExifReader object.
     *
     * @param   file  The image file
     *
     * @throws  ImageProcessingException  DOCUMENT ME!
     * @throws  IOException               DOCUMENT ME!
     */
    public ExifReader(final File file) throws ImageProcessingException, IOException {
        metadata = ImageMetadataReader.readMetadata(file);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Determines the coordinatesof the image in EPSG:4326.
     *
     * @return  The coordinatesof the image in EPSG:4326
     */
    public Point getGpsCoords() {
        Point p = null;
        final GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);

        if (gpsDirectory != null) {
            if ((gpsDirectory.getGeoLocation() != null) && !gpsDirectory.getGeoLocation().isZero()) {
                final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
                p = factory.createPoint(new Coordinate(
                            gpsDirectory.getGeoLocation().getLongitude(),
                            gpsDirectory.getGeoLocation().getLatitude()));
            }
        }

        return p;
    }

    /**
     * Determines the direction of the image.
     *
     * @return  the direction of the image
     *
     * @throws  MetadataException  If an error occurs during the reading of the image metadata
     */
    public Double getGpsDirection() throws MetadataException {
        final GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);

        if (gpsDirectory != null) {
            return gpsDirectory.getDouble(GpsDirectory.TAG_GPS_IMG_DIRECTION);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Date getTime() {
        final ExifIFD0Directory dir = metadata.getDirectory(ExifIFD0Directory.class);

        if (dir != null) {
            return dir.getDate(ExifIFD0Directory.TAG_DATETIME);
        }

        return null;
    }

    /**
     * Prints all exif attributes to stdout.
     */
    public void printAllAttributes() {
        for (final Directory directory : metadata.getDirectories()) {
            for (final Tag tag : directory.getTags()) {
                System.out.println(tag);
            }
        }
    }

    /**
     * Only for test purposes.
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        try {
            final long time = System.currentTimeMillis();
            final File file = new File("/home/therter/archive/RIMG0012.JPG");
            final ExifReader reader = new ExifReader(file);
            final Point po = reader.getGpsCoords();
            final double direction = reader.getGpsDirection();
            System.out.println(po.toText());
            System.out.println(direction);
            System.out.println("Zeit " + (System.currentTimeMillis() - time));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
