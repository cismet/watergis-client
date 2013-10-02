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

import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import de.cismet.tools.gui.downloadmanager.AbstractDownload;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ImageDownload extends AbstractDownload implements Cancellable {

    //~ Instance fields --------------------------------------------------------

    String extension;
    Future<Image> futureImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageDownload object.
     *
     * @param  filename      DOCUMENT ME!
     * @param  extension     DOCUMENT ME!
     * @param  fileToSaveTo  DOCUMENT ME!
     * @param  futureImage   DOCUMENT ME!
     */
    public ImageDownload(
            final String filename,
            final String extension,
            final File fileToSaveTo,
            final Future<Image> futureImage) {
        this.extension = extension;
        this.futureImage = futureImage;

        status = State.WAITING;
        this.fileToSaveTo = fileToSaveTo;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != State.WAITING) {
            return;
        }
        status = State.RUNNING;

        stateChanged();

        Image image = null;
        if (futureImage != null) {
            try {
                if (!Thread.interrupted()) {
                    image = (Image)futureImage.get();
                } else {
                    deleteFile();
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                status = State.COMPLETED_WITH_ERROR;
                stateChanged();
                deleteFile();
                return;
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
                status = State.COMPLETED_WITH_ERROR;
                stateChanged();
                deleteFile();
                return;
            }
        }

        if ((image != null) && !Thread.interrupted()) {
            try {
                ImageIO.write(removeTransparency(image), extension, fileToSaveTo);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                status = State.COMPLETED_WITH_ERROR;
                stateChanged();
                deleteFile();
                return;
            }
        } else {
            status = State.COMPLETED_WITH_ERROR;
            stateChanged();
            deleteFile();
            return;
        }

        if (status == State.RUNNING) {
            status = State.COMPLETED;
            stateChanged();
        }
    }

    /**
     * Removes the transparency from an image and returns an opaque image. This method is needed as the image should be
     * saved as jpg, which is unable to handle transparency.
     *
     * @param   transparentImage  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage removeTransparency(final Image transparentImage) {
        final BufferedImage whiteBackgroundImage = new BufferedImage(transparentImage.getWidth(null),
                transparentImage.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = null;
        try {
            g2 = whiteBackgroundImage.createGraphics();

             g2.setColor(Color.WHITE);
             g2.fillRect(0, 0, whiteBackgroundImage.getWidth(), whiteBackgroundImage.getHeight());

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(
                transparentImage,
                0,
                0,
                whiteBackgroundImage.getWidth(),
                whiteBackgroundImage.getHeight(),
                null);
        } finally {
            if (g2 != null) {
                g2.dispose();
            }
        }

        // BufferedImage bufImg = ImageIO.read( imageURL );
        final BufferedImage opaqueImage = new BufferedImage(whiteBackgroundImage.getWidth(),
                whiteBackgroundImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        opaqueImage.getGraphics().drawImage(whiteBackgroundImage, 0, 0, null);

        return (BufferedImage)opaqueImage;
    }

    @Override
    public boolean cancel() {
        boolean cancelled = true;
        if (downloadFuture != null) {
            cancelled = downloadFuture.cancel(true);
        }
        if (cancelled) {
            status = State.ABORTED;
            stateChanged();
        }
        return cancelled;
    }

    /**
     * DOCUMENT ME!
     */
    private void deleteFile() {
        if (fileToSaveTo.exists() && fileToSaveTo.isFile()) {
            fileToSaveTo.delete();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = (37 * hash) + ((this.extension != null) ? this.extension.hashCode() : 0);
        hash = (37 * hash) + ((this.futureImage != null) ? this.futureImage.hashCode() : 0);
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
        final ImageDownload other = (ImageDownload)obj;
        if ((this.extension == null) ? (other.extension != null) : (!this.extension.equals(other.extension))) {
            return false;
        }
        if ((this.futureImage != other.futureImage)
                    && ((this.futureImage == null) || !this.futureImage.equals(other.futureImage))) {
            return false;
        }
        return true;
    }
}
