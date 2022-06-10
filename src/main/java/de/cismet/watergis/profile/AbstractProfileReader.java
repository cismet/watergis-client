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

import Sirius.navigator.tools.MetaObjectCache;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Point2D;

import java.io.File;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.CustomGafCatalogueReader;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractProfileReader implements ProfileReader {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractProfileReader.class);
    private static Double artificialStationCounter = -1.0;

    //~ Instance fields --------------------------------------------------------

    protected List<ProfileLine> content = new ArrayList<>();
    protected Map<ProfileReader.GAF_FIELDS, Integer> fieldMap;
    protected CustomGafCatalogueReader customRkCatalogue;
    protected CustomGafCatalogueReader customBkCatalogue;
    protected final Map<Double, ArrayList<ProfileLine>> profiles = new HashMap<>();
    protected boolean catalogueInitialised = false;
    protected final List<CidsBean> rkList = new ArrayList<>();
    protected final List<CidsBean> bkList = new ArrayList<>();
    protected final List<CidsBean> kz = new ArrayList<>();
    private boolean calc = false;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the calc
     */
    @Override
    public boolean isCalc() {
        return calc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  calc  the calc to set
     */
    public void setCalc(final boolean calc) {
        this.calc = calc;
    }

    /**
     * DOCUMENT ME!
     */
    protected synchronized void initCatalogues() {
        if (!catalogueInitialised) {
            final MetaClass rkMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_qp_gaf_rk");
            final MetaClass kzMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_qp_gaf_kz");
            final MetaClass bkMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_qp_gaf_bk");

            try {
                final String queryRk = "select " + rkMc.getID() + ", " + rkMc.getPrimaryKey() + " from "
                            + rkMc.getTableName(); // NOI18N
                final String queryKz = "select " + kzMc.getID() + ", " + kzMc.getPrimaryKey() + " from "
                            + kzMc.getTableName(); // NOI18N
                final String queryBk = "select " + bkMc.getID() + ", " + bkMc.getPrimaryKey() + " from "
                            + bkMc.getTableName(); // NOI18N

                final MetaObject[] moRk = MetaObjectCache.getInstance()
                            .getMetaObjectsByQuery(queryRk, AppBroker.DOMAIN_NAME);
                final MetaObject[] moBk = MetaObjectCache.getInstance()
                            .getMetaObjectsByQuery(queryBk, AppBroker.DOMAIN_NAME);
                final MetaObject[] moKz = MetaObjectCache.getInstance()
                            .getMetaObjectsByQuery(queryKz, AppBroker.DOMAIN_NAME);

                if (moRk != null) {
                    for (final MetaObject mo : moRk) {
                        rkList.add(mo.getBean());
                    }
                }

                if (moBk != null) {
                    for (final MetaObject mo : moBk) {
                        bkList.add(mo.getBean());
                    }
                }

                if (moKz != null) {
                    for (final MetaObject mo : moKz) {
                        kz.add(mo.getBean());
                    }
                }
            } catch (Exception e) {
                LOG.error("Cannot receive gaf catalogue meta objects.", e);
            }

            catalogueInitialised = true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Double getArtificialStation() {
        return artificialStationCounter--;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj           DOCUMENT ME!
     * @param   defaultValue  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String objectToString(final Object obj, final String defaultValue) {
        if (obj == null) {
            return defaultValue;
        } else {
            if (obj instanceof Double) {
                final DecimalFormat format = new DecimalFormat();
                final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
                symbols.setDecimalSeparator('.');
                format.setDecimalFormatSymbols(symbols);
                format.setGroupingUsed(false);

                return format.format(obj);
            } else {
                return String.valueOf(obj);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected double getStationNumber(final ProfileLine line) {
        try {
            return line.getFieldAsDouble(GAF_FIELDS.STATION);
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   catalogueFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    @Override
    public CustomGafCatalogueReader.FILE_TYPE addCustomCatalogue(final File catalogueFile)
            throws IllegalArgumentException {
        final CustomGafCatalogueReader catFile = new CustomGafCatalogueReader(catalogueFile);

        if (catFile.getType().equals(CustomGafCatalogueReader.FILE_TYPE.BK)) {
            customBkCatalogue = catFile;
            return CustomGafCatalogueReader.FILE_TYPE.BK;
        } else if (catFile.getType().equals(CustomGafCatalogueReader.FILE_TYPE.RK)) {
            customRkCatalogue = catFile;
            return CustomGafCatalogueReader.FILE_TYPE.RK;
        } else {
            return CustomGafCatalogueReader.FILE_TYPE.UNKNOWN;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public QpCheckResult checkFileForHints() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Set<Double> getProfiles() {
        return profiles.keySet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     *
     * @return  the geometry of the normal profil
     */
    @Override
    public LineString getNpLine(final Double profile) {
        return getLineBetween(profile, "PA", "PE");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     * @param   width    DOCUMENT ME!
     * @param   height   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Image createImage(final Double profile, final int width, final int height) {
        final ChartCreator chart = new ChartCreator();

        final ArrayList<ProfileLine> gaf = profiles.get(profile);
        final List<ChartCreator.Point> pointList = new ArrayList<>();
        final double water = Double.MIN_VALUE;
        final List<ChartCreator.Point> schlamm = new ArrayList<>();
        final List<ChartCreator.Point> baUk = new ArrayList<>();
        final List<ChartCreator.Point> baOk = new ArrayList<>();
        final List<Point2D> eiprof = new ArrayList<>();
        final List<Point2D> kprof = new ArrayList<>();
        final List<Point2D> maprof = new ArrayList<>();
        double xStart = 0;
        double xEnd = 0;
        boolean started = false;

        for (int i = 0; i < gaf.size(); ++i) {
            try {
                final ProfileLine line = gaf.get(i);
                String kzVal = line.getField(GAF_FIELDS.KZ);

                if (kzVal == null) {
                    if ((i == 0)) {
                        kzVal = "PA";
                    } else if (i == (gaf.size() - 1)) {
                        kzVal = "PE";
                    } else {
                        kzVal = "PP";
                    }
                }

                if (kzVal.equalsIgnoreCase("PA")) {
                    xStart = Double.parseDouble(line.getField(GAF_FIELDS.Y));
                    started = true;
                }
                if (started) {
                    final double x = Double.parseDouble(line.getField(GAF_FIELDS.Y));
                    final double y = Double.parseDouble(line.getField(GAF_FIELDS.Z));
                    final ChartCreator.Point point = new ChartCreator.Point(new Color(234, 156, 39), x, y, true, false);
                    pointList.add(point);

                    final ChartCreator.Point pointWithLine = new ChartCreator.Point(
                            Color.BLACK,
                            x,
                            y,
                            true,
                            false);
//                    pointWithLine.setStroke(new BasicStroke(
//                            1,
//                            BasicStroke.CAP_SQUARE,
//                            BasicStroke.JOIN_MITER,
//                            2,
//                            new float[] { 1f, 3f },
//                            0));
                    chart.addPoint(pointWithLine);
                }
                if (kzVal.equalsIgnoreCase("PE")) {
                    xEnd = Double.parseDouble(line.getField(GAF_FIELDS.Y));
                    started = false;
                }
            } catch (NumberFormatException e) {
                LOG.error("Invalid value found. Not a number.", e);
            }
        }

        if (water != Double.MIN_VALUE) {
            chart.addHorizontalLines(new ChartCreator.HorizontalLine(
                    Color.BLUE,
                    new ChartCreator.Point(Color.BLUE, xStart, water, false, false),
                    new ChartCreator.Point(Color.BLUE, xEnd, water, false, false),
                    null));
        }

        chart.addPointLines(new ChartCreator.PointLine(pointList));

        return chart.createImage(width, height);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     * @param   kzStart  DOCUMENT ME!
     * @param   kzEnd    DOCUMENT ME!
     *
     * @return  the geometry of the normal profil
     */
    protected LineString getLineBetween(final Double profile, final String kzStart, final String kzEnd) {
        final ArrayList<ProfileLine> profContent = profiles.get(profile);
        final List<Coordinate> coordinateList = new ArrayList<>();
        boolean started = false;
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CismapBroker.getInstance().getDefaultCrsAlias());

        for (final ProfileLine line : profContent) {
            try {
                if ((line.getField(GAF_FIELDS.KZ) == null) || line.getField(GAF_FIELDS.KZ).equalsIgnoreCase(kzStart)) {
                    started = true;
                }
                if (started) {
                    final double ho = line.getFieldAsDouble(GAF_FIELDS.HW);
                    final double re = line.getFieldAsDouble(GAF_FIELDS.RW);
                    final Coordinate coord = new Coordinate(re, ho);
                    coordinateList.add(coord);
                }
                if ((line.getField(GAF_FIELDS.KZ) != null) && line.getField(GAF_FIELDS.KZ).equalsIgnoreCase(kzEnd)) {
                    started = false;
                }
            } catch (NumberFormatException e) {
                LOG.error("Invalid value found. Not a number.", e);
            }
        }

        if (!coordinateList.isEmpty()) {
            return factory.createLineString(coordinateList.toArray(new Coordinate[coordinateList.size()]));
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Point getProfilePoint(final Double profile) {
        final ArrayList<ProfileLine> profContent = profiles.get(profile);
        Double luY = null;
        Double ruY = null;
        Double lbukY = null;
        Double rbukY = null;
        Double lbokY = null;
        Double rbokY = null;
        Double paY = null;
        Double peY = null;

        for (final ProfileLine line : profContent) {
            try {
                if (line.getField(GAF_FIELDS.KZ).equalsIgnoreCase("LU")) {
                    luY = line.getFieldAsDouble(GAF_FIELDS.Y);
                }
                if (line.getField(GAF_FIELDS.KZ).equalsIgnoreCase("RU")) {
                    ruY = line.getFieldAsDouble(GAF_FIELDS.Y);
                }
                if (line.getField(GAF_FIELDS.KZ).equalsIgnoreCase("LBUK")) {
                    lbukY = line.getFieldAsDouble(GAF_FIELDS.Y);
                }
                if (line.getField(GAF_FIELDS.KZ).equalsIgnoreCase("RBUK")) {
                    rbukY = line.getFieldAsDouble(GAF_FIELDS.Y);
                }
                if (line.getField(GAF_FIELDS.KZ).equalsIgnoreCase("LBOK")) {
                    lbokY = line.getFieldAsDouble(GAF_FIELDS.Y);
                }
                if (line.getField(GAF_FIELDS.KZ).equalsIgnoreCase("RBOK")) {
                    rbokY = line.getFieldAsDouble(GAF_FIELDS.Y);
                }
                if (line.getField(GAF_FIELDS.KZ).equalsIgnoreCase("PA")) {
                    paY = line.getFieldAsDouble(GAF_FIELDS.Y);
                }
                if (line.getField(GAF_FIELDS.KZ).equalsIgnoreCase("PE")) {
                    peY = line.getFieldAsDouble(GAF_FIELDS.Y);
                }
            } catch (NumberFormatException e) {
                LOG.error("Invalid value found. Not a number.", e);
            }
        }

        if (peY == null) {
            return null;
        }

        if ((luY != null) && (ruY != null)) {
            final double stat = (Math.abs(luY - ruY) / 2) + Math.min(luY, ruY) - peY;
            return getCentralpoint(getNpLine(profile), stat);
        }

        if ((lbukY != null) && (rbukY != null)) {
            final double stat = (Math.abs(lbukY - rbukY) / 2) + Math.min(lbukY, rbukY) - peY;
            return getCentralpoint(getNpLine(profile), stat);
        }

        if ((lbokY != null) && (rbokY != null)) {
            final double stat = (Math.abs(lbokY - rbokY) / 2) + Math.min(lbokY, rbokY) - peY;
            return getCentralpoint(getNpLine(profile), stat);
        }

        if ((paY != null) && (peY != null)) {
            final double stat = (Math.abs(paY - peY) / 2) + Math.min(paY, peY) - peY;
            return getCentralpoint(getNpLine(profile), stat);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public List<ProfileLine> getProfileContent(final Double profile) {
        return profiles.get(profile);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field    DOCUMENT ME!
     * @param   gafLine  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object getProfileContent(final GAF_FIELDS field, final ProfileLine gafLine) {
        if ((field == GAF_FIELDS.Z) || (field == GAF_FIELDS.Y) || (field == GAF_FIELDS.STATION)
                    || (field == GAF_FIELDS.HW)
                    || (field == GAF_FIELDS.RW)) {
            return toDouble(gafLine.getField(field).replace(",", ","));
        } else if (field == GAF_FIELDS.RK) {
            initCatalogues();

            if (customRkCatalogue != null) {
                final CustomGafCatalogueReader.RkObject obj = customRkCatalogue.getRkById(
                        gafLine.getField(field));

                if (obj != null) {
                    final CidsBean bean = getCentralRkBean(obj);

                    if (bean != null) {
                        return bean;
                    } else {
                        return obj;
                    }
                }
            }
            return toCatalogueElement(rkList, gafLine.getField(field), "rk");
        } else if (field == GAF_FIELDS.BK) {
            initCatalogues();
            if (customBkCatalogue != null) {
                final CustomGafCatalogueReader.BkObject obj = customBkCatalogue.getBkById(
                        gafLine.getField(field));

                if (obj != null) {
                    final CidsBean bean = getCentralBkBean(obj);

                    if (bean != null) {
                        return bean;
                    } else {
                        return obj;
                    }
                }
            }
            return toCatalogueElement(bkList, gafLine.getField(field), "bk");
        } else if (field == GAF_FIELDS.KZ) {
            initCatalogues();
            return toCatalogueElement(kz, gafLine.getField(field), "kz");
        } else {
            return gafLine.getField(field);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Double toDouble(final String obj) {
        try {
            return Double.parseDouble(obj);
        } catch (NumberFormatException e) {
            LOG.error("Not a number", e);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rk  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected CidsBean getCentralRkBean(final CustomGafCatalogueReader.RkObject rk) {
        for (final CidsBean rkBean : rkList) {
            final String name = (String)rkBean.getProperty("name");
            final Double k = (Double)rkBean.getProperty("k");
            final Double kst = (Double)rkBean.getProperty("kst");

            if (((name != null) && name.equalsIgnoreCase(rk.getName())) && (k == rk.getK()) && (kst == rk.getKst())) {
                return rkBean;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bk  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected CidsBean getCentralBkBean(final CustomGafCatalogueReader.BkObject bk) {
        for (final CidsBean bkBean : bkList) {
            final String name = (String)bkBean.getProperty("name");
            final Double ax = (Double)bkBean.getProperty("ax");
            final Double ay = (Double)bkBean.getProperty("ay");
            final Double dp = (Double)bkBean.getProperty("dp");

            if (((name != null) && name.equalsIgnoreCase(bk.getName())) && (ax == bk.getAx()) && (ay == bk.getAy())
                        && (dp == bk.getDp())) {
                return bkBean;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   catalogue    DOCUMENT ME!
     * @param   name         DOCUMENT ME!
     * @param   catPropName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected CidsBean toCatalogueElement(final List<CidsBean> catalogue, final String name, final String catPropName) {
        for (final CidsBean tmpCat : catalogue) {
            if (String.valueOf(tmpCat.getProperty(catPropName)).equalsIgnoreCase(name)) {
                return tmpCat;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line      DOCUMENT ME!
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Point getCentralpoint(final LineString line, final double position) {
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CismapBroker.getInstance().getDefaultCrsAlias());
        final LengthIndexedLine lil = new LengthIndexedLine(line);
        final Coordinate coordinate = lil.extractPoint(position);

        return factory.createPoint(coordinate);
    }

    @Override
    public List<ProfileLine> getContent() {
        return content;
    }
}
