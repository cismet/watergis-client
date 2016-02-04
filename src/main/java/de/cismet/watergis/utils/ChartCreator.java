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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ChartCreator {

    //~ Instance fields --------------------------------------------------------

    private final List<Point> points = new ArrayList<Point>();
    private final List<PointLine> pointLines = new ArrayList<PointLine>();
    private final List<HorizontalLine> horizontalLines = new ArrayList<HorizontalLine>();
    private final List<VerticalLine> verticalLines = new ArrayList<VerticalLine>();
    private final List<Circle> circles = new ArrayList<Circle>();
    private int leftBorder;
    private int buttomBorder;
    private int topBorder;
    private int rightBorder;
    private Rectangle2D dims;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  point  DOCUMENT ME!
     */
    public void addPoint(final Point point) {
        points.add(point);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  line  DOCUMENT ME!
     */
    public void addPointLines(final PointLine line) {
        pointLines.add(line);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  line  DOCUMENT ME!
     */
    public void addHorizontalLines(final HorizontalLine line) {
        horizontalLines.add(line);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  line  DOCUMENT ME!
     */
    public void addVerticalLines(final VerticalLine line) {
        verticalLines.add(line);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  circle  line DOCUMENT ME!
     */
    public void addCircle(final Circle circle) {
        circles.add(circle);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   width   DOCUMENT ME!
     * @param   height  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Image createImage(final int width, final int height) {
        final BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = im.createGraphics();
        final FontMetrics fmetrics = g.getFontMetrics();
        dims = getDimension();
        final Rectangle2D yTextSize = fmetrics.getStringBounds(String.valueOf(
                    ((int)((dims.getMinY() + dims.getHeight())))
                            + 0.8),
                g);
        leftBorder = (int)yTextSize.getBounds2D().getWidth() + 15;
        buttomBorder = (int)yTextSize.getBounds2D().getHeight() + 15;
        topBorder = 5;
        rightBorder = 5;
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, width, height);
        g.setStroke(new BasicStroke(2));
        drawCoordinateSystem(g, width, height);

        drawAllPoints(g, width, height);
        drawAllHorizontalLines(g, width, height);
        drawAllVerticalLines(g, width, height);
        drawAllPointLines(g, width, height);
        drawAllCircles(g, width, height);

        return im;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  g       DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     */
    private void drawAllPoints(final Graphics2D g, final int width, final int height) {
        for (final Point p : points) {
            drawPoint(p, g, width, height);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  g       DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     */
    private void drawAllCircles(final Graphics2D g, final int width, final int height) {
        for (final Circle c : circles) {
            drawCircle(c, g, width, height);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  g       DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     */
    private void drawAllPointLines(final Graphics2D g, final int width, final int height) {
        for (final PointLine pl : pointLines) {
            drawPointLine(pl, g, width, height);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  g       DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     */
    private void drawAllHorizontalLines(final Graphics2D g, final int width, final int height) {
        for (final HorizontalLine l : horizontalLines) {
            g.setColor(l.getColor());
            g.drawLine(transformToScreenX(l.getFrom().getX(), width),
                transformToScreenY(l.getFrom().getY(), height),
                transformToScreenX(l.getTo().getX(), width),
                transformToScreenY(l.getTo().getY(), height));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  g       DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     */
    private void drawAllVerticalLines(final Graphics2D g, final int width, final int height) {
        for (final VerticalLine l : verticalLines) {
            g.setColor(l.getColor());
            final Point p = l.getPoint();
            g.drawLine(transformToScreenX(p.getX(), width),
                transformToScreenY(p.getY(), height),
                transformToScreenX(p.getX(), width),
                height
                        - buttomBorder
                        - 1);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pl      DOCUMENT ME!
     * @param  g       DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     */
    private void drawPointLine(final PointLine pl, final Graphics2D g, final int width, final int height) {
        Point lastPoint = null;

        for (final Point p : pl.getPoints()) {
            drawPoint(p, g, width, height);

            if (lastPoint != null) {
                g.setColor(lastPoint.getColor());
                g.drawLine(transformToScreenX(lastPoint.getX(), width),
                    transformToScreenY(lastPoint.getY(), height),
                    transformToScreenX(p.getX(), width),
                    transformToScreenY(p.getY(), height));
            }

            lastPoint = p;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  p       DOCUMENT ME!
     * @param  g       DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     */
    private void drawPoint(final Point p, final Graphics2D g, final int width, final int height) {
        g.setColor(p.getColor());

        if (p.pointSymbol) {
            g.fillOval(transformToScreenX(p.getX(), width) - 4, transformToScreenY(p.getY(), height) - 4, 9, 9);
        }

        if (p.lineToBottom) {
            final Stroke oldStroke = g.getStroke();

            if (p.getStroke() != null) {
                g.setStroke(p.getStroke());
            }

            g.drawLine(transformToScreenX(p.getX(), width),
                transformToScreenY(p.getY(), height),
                transformToScreenX(p.getX(), width),
                height
                        - buttomBorder
                        - 1);

            g.setStroke(oldStroke);
        }

        if (p.getText() != null) {
            g.setColor(Color.BLACK);
            g.drawString(p.getText(),
                transformToScreenX(p.getX(), width)
                        - 4,
                transformToScreenY(p.getY(), height)
                        - 10);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  c       p DOCUMENT ME!
     * @param  g       DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     */
    private void drawCircle(final Circle c, final Graphics2D g, final int width, final int height) {
        g.setColor(c.getColor());
        g.drawOval(transformToScreenX(c.getX(), width) - 4,
            transformToScreenY(c.getY(), height),
            (int)c.getWidth(),
            (int)c.getHeight());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  g       DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     */
    private void drawCoordinateSystem(final Graphics2D g, final int width, final int height) {
        g.setColor(Color.BLACK);
        // left border
        g.drawLine(leftBorder, topBorder, leftBorder, height - buttomBorder);
        // right border
        g.drawLine(width - rightBorder, topBorder, width - rightBorder, height - buttomBorder);
        // top border
        g.drawLine(leftBorder, topBorder, width - rightBorder, topBorder);
        // buttom border
        g.drawLine(leftBorder, height - buttomBorder, width - rightBorder, height - buttomBorder);

        final FontMetrics fmetrics = g.getFontMetrics();
        final Rectangle2D xTextSize = fmetrics.getStringBounds(String.valueOf(
                    ((int)(10 * (dims.getMinX() + dims.getWidth())))
                            / 10),
                g);
        final int xValueCount = (int)((width - leftBorder - rightBorder) / (xTextSize.getWidth() + 20));
        final int yValueCount = (int)((height - topBorder - buttomBorder) / (xTextSize.getHeight() + 20));
        final double xPart = getPartDistance(xValueCount, dims.getBounds2D().getWidth());
        final double yPart = getPartDistance(yValueCount, dims.getBounds2D().getHeight());
        int x = 1;
        int y = 1;

        while (transformToScreenX((x * xPart) + dims.getX(), width) < (width - (int)(rightBorder + 15))) {
            final double xVal = (x * xPart) + dims.getX();
            g.drawLine(transformToScreenX(xVal, width),
                height
                        - buttomBorder,
                transformToScreenX(xVal, width),
                height
                        - buttomBorder
                        + 4);
            final double stringWidth = fmetrics.getStringBounds(String.valueOf(round(xVal)), g).getWidth();
            g.drawString(String.valueOf(round(xVal)),
                (int)(transformToScreenX(xVal, width) - (stringWidth / 2)),
                height
                        - 5);
            ++x;
        }

        while (transformToScreenY((y * yPart) + dims.getY(), height) > (int)(topBorder + 20)) {
            final double yVal = (y * yPart) + dims.getY();
            g.drawLine(leftBorder, transformToScreenY(yVal, height), leftBorder - 4, transformToScreenY(yVal, height));
            final double stringHeight = fmetrics.getStringBounds(String.valueOf(round(yVal)), g).getHeight();
            g.drawString(String.valueOf(round(yVal)), 5, (int)(transformToScreenY(yVal, height) + (stringHeight / 2)));
            ++y;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   val  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double round(final double val) {
        return (int)(val * 10) / 10.0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parts      DOCUMENT ME!
     * @param   totalDist  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getPartDistance(final int parts, final double totalDist) {
        if ((totalDist / parts) >= 1) {
            return (int)(totalDist / parts);
        } else {
            return ((int)(totalDist / parts * 10)) / 10.0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   x      DOCUMENT ME!
     * @param   width  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int transformToScreenX(final double x, final int width) {
        return (int)((x - dims.getX()) / dims.getWidth() * (width - rightBorder - leftBorder)) + leftBorder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   y       DOCUMENT ME!
     * @param   height  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int transformToScreenY(final double y, final int height) {
        return
            (int)((((dims.getHeight()) - (y - dims.getY())) / dims.getHeight())
                        * (height - topBorder - buttomBorder))
                    + topBorder;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Rectangle2D getDimension() {
        final Dimension dim = new Dimension();

        for (final Point p : points) {
            dim.addPoint(p);
        }

        for (final PointLine line : pointLines) {
            for (final Point p : line.points) {
                dim.addPoint(p);
            }
        }

        for (final HorizontalLine line : horizontalLines) {
            dim.addPoint(line.from);
            dim.addPoint(line.to);
        }

        for (final VerticalLine line : verticalLines) {
            dim.addPoint(line.point);
        }

        return dim.getDimensions();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class Dimension {

        //~ Instance fields ----------------------------------------------------

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  p  DOCUMENT ME!
         */
        public void addPoint(final Point p) {
            if (p.getX() < minX) {
                minX = p.getX();
            }

            if (p.getX() > maxX) {
                maxX = p.getX();
            }
            if (p.getY() > maxY) {
                maxY = p.getY();
            }
            if (p.getY() < minY) {
                minY = p.getY();
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Rectangle2D getDimensions() {
            final double width = maxX
                        - minX;
            final double height = maxY
                        - minY;

            final double additionalWidth = width
                        * 0.05;
            final double additionalHeight = height
                        * 0.05;

            minX -= additionalWidth;
            maxX += additionalWidth;

            minY -= additionalHeight;
            maxY += additionalHeight;

            return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    static class Point extends Point2D.Double {

        //~ Instance fields ----------------------------------------------------

        private final Color color;
        private final boolean pointSymbol;
        private final boolean lineToBottom;
        private final String text;
        private Stroke stroke = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Point object.
         *
         * @param  color         DOCUMENT ME!
         * @param  x             DOCUMENT ME!
         * @param  y             DOCUMENT ME!
         * @param  pointSymbol   DOCUMENT ME!
         * @param  lineToBottom  DOCUMENT ME!
         */
        public Point(final Color color,
                final double x,
                final double y,
                final boolean pointSymbol,
                final boolean lineToBottom) {
            this(color, x, y, pointSymbol, lineToBottom, null);
        }

        /**
         * Creates a new Point object.
         *
         * @param  color         DOCUMENT ME!
         * @param  x             DOCUMENT ME!
         * @param  y             DOCUMENT ME!
         * @param  pointSymbol   DOCUMENT ME!
         * @param  lineToBottom  DOCUMENT ME!
         * @param  text          DOCUMENT ME!
         */
        public Point(final Color color,
                final double x,
                final double y,
                final boolean pointSymbol,
                final boolean lineToBottom,
                final String text) {
            super(x, y);
            this.color = color;
            this.pointSymbol = pointSymbol;
            this.lineToBottom = lineToBottom;
            this.text = text;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the pointSymbol
         */
        public boolean isPointSymbol() {
            return pointSymbol;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the text
         */
        public String getText() {
            return text;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the stroke
         */
        public Stroke getStroke() {
            return stroke;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  stroke  the stroke to set
         */
        public void setStroke(final Stroke stroke) {
            this.stroke = stroke;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    static class Circle {

        //~ Instance fields ----------------------------------------------------

        private final Color color;
        private final double x;
        private final double y;
        private final double width;
        private final double height;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Circle object.
         *
         * @param  color   DOCUMENT ME!
         * @param  x       DOCUMENT ME!
         * @param  y       DOCUMENT ME!
         * @param  width   DOCUMENT ME!
         * @param  height  DOCUMENT ME!
         */
        public Circle(final Color color, final double x, final double y, final double width, final double height) {
            this.color = color;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the x
         */
        public double getX() {
            return x;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the y
         */
        public double getY() {
            return y;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the width
         */
        public double getWidth() {
            return width;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the height
         */
        public double getHeight() {
            return height;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    static class PointLine {

        //~ Instance fields ----------------------------------------------------

        private final List<Point> points;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PointLine object.
         *
         * @param  points  DOCUMENT ME!
         */
        public PointLine(final List<Point> points) {
            this.points = points;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the points
         */
        public List<Point> getPoints() {
            return points;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    static class VerticalLine {

        //~ Instance fields ----------------------------------------------------

        private final Color color;
        private final Point point;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new VerticalLine object.
         *
         * @param  color  DOCUMENT ME!
         * @param  point  DOCUMENT ME!
         */
        public VerticalLine(final Color color, final Point point) {
            this.color = color;
            this.point = point;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the point
         */
        public Point getPoint() {
            return point;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    static class HorizontalLine {

        //~ Instance fields ----------------------------------------------------

        private final Color color;
        private final Point from;
        private final Point to;
        private final Color backgroundColor;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new HorizontalLine object.
         *
         * @param  color            DOCUMENT ME!
         * @param  from             DOCUMENT ME!
         * @param  to               DOCUMENT ME!
         * @param  backgroundColor  DOCUMENT ME!
         */
        public HorizontalLine(final Color color, final Point from, final Point to, final Color backgroundColor) {
            this.color = color;
            this.from = from;
            this.to = to;
            this.backgroundColor = backgroundColor;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the from
         */
        public Point getFrom() {
            return from;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the to
         */
        public Point getTo() {
            return to;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the backgroundColor
         */
        public Color getBackgroundColor() {
            return backgroundColor;
        }
    }
}
