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

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.text.DecimalFormat;

import java.util.Base64;

import javax.imageio.ImageIO;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ConversionUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final DecimalFormat format = new DecimalFormat("0.00");

    static {
        final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        format.setDecimalFormatSymbols(symbols);
        format.setGroupingUsed(true);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   d  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String numberToString(final Object d) {
        if (d == null) {
            return "";
        } else {
            return format.format(d);
        }
    }

    /**
     * Converts the given image to a base64 string.
     *
     * @param   i  the image to convert
     *
     * @return  a base64 string representation of the given image
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static String image2String(final BufferedImage i) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(i, "png", out);

        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    /**
     * Converts the given base64 string to an image.
     *
     * @param   s  the string to convert (a base64 representation of an image)
     *
     * @return  the image that was generated by the given base 64 string
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static BufferedImage String2Image(final String s) throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(s));

        return ImageIO.read(in);
    }
}
