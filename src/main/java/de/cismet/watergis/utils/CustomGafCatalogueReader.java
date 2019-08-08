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

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CustomGafCatalogueReader {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CustomGafCatalogueReader.class);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum RK_FIELDS {

        //~ Enum constants -----------------------------------------------------

        RK, NAME, K, KST
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum BK_FIELDS {

        //~ Enum constants -----------------------------------------------------

        BK, NAME, AX, AY, DP
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum FILE_TYPE {

        //~ Enum constants -----------------------------------------------------

        RK, BK, UNKNOWN
    }

    //~ Instance fields --------------------------------------------------------

    private final int[] columnIndex = new int[5];
    private String[] header;
    private final ArrayList<String[]> content = new ArrayList<String[]>();
    private FILE_TYPE type;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CustomGafCatalogueReader object.
     *
     * @param   catalogueFile  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public CustomGafCatalogueReader(final File catalogueFile) throws IllegalArgumentException {
        init(catalogueFile);
    }

    /**
     * Creates a new CustomGafCatalogueReader object.
     */
    private CustomGafCatalogueReader() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the type
     */
    public FILE_TYPE getType() {
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   features  DOCUMENT ME!
     * @param   bkList    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CustomGafCatalogueReader createBkCatalogue(final List<DefaultFeatureServiceFeature> features,
            final List<CidsBean> bkList) {
        boolean hasCustomCatalogue = false;
        final CustomGafCatalogueReader catalogue = new CustomGafCatalogueReader();
        final TreeSet<BkObject> objectSet = new TreeSet<BkObject>();

        for (final DefaultFeatureServiceFeature feature : features) {
            if (feature.getProperty("bk") != null) {
                final CidsBean bean = getBeanByProperty(bkList, "bk", feature.getProperty("bk"));
                final BkObject bkObject = new BkObject(bean);
                objectSet.add(bkObject);
            } else if (feature.getProperty("bk_name") != null) {
                hasCustomCatalogue = true;
                final BkObject bkObject = new BkObject();

                bkObject.setName((String)feature.getProperty("bk_name"));
                bkObject.setAx((Double)feature.getProperty("bk_ax"));
                bkObject.setAy((Double)feature.getProperty("bk_ay"));
                bkObject.setDp((Double)feature.getProperty("bk_dp"));

                objectSet.add(bkObject);
            }
        }

        int number = 1000;
        for (final BkObject obj : objectSet) {
            final String[] contentFields = new String[5];
            Integer bkNumber = obj.getBk();

            if (bkNumber == null) {
                bkNumber = ++number;
            }
            contentFields[BK_FIELDS.BK.ordinal()] = bkNumber.toString();
            contentFields[BK_FIELDS.NAME.ordinal()] = obj.getName();
            contentFields[BK_FIELDS.AX.ordinal()] = obj.getAx().toString();
            contentFields[BK_FIELDS.AY.ordinal()] = obj.getAy().toString();
            contentFields[BK_FIELDS.DP.ordinal()] = obj.getDp().toString();
            catalogue.content.add(contentFields);
        }
        for (int i = 0; i < 5; ++i) {
            catalogue.columnIndex[i] = i;
        }

        catalogue.type = FILE_TYPE.BK;

        return catalogue;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   features  DOCUMENT ME!
     * @param   rkList    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CustomGafCatalogueReader createRkCatalogue(final List<DefaultFeatureServiceFeature> features,
            final List<CidsBean> rkList) {
        boolean hasCustomCatalogue = false;
        final CustomGafCatalogueReader catalogue = new CustomGafCatalogueReader();
        final TreeSet<RkObject> objectSet = new TreeSet<RkObject>();

        for (final DefaultFeatureServiceFeature feature : features) {
            if (feature.getProperty("rk") != null) {
                final CidsBean bean = getBeanByProperty(rkList, "rk", feature.getProperty("rk"));
                final RkObject rkObject = new RkObject(bean);
                objectSet.add(rkObject);
            } else if (feature.getProperty("rk_name") != null) {
                hasCustomCatalogue = true;
                final RkObject rkObject = new RkObject();

                rkObject.setName((String)feature.getProperty("rk_name"));
                rkObject.setK((Double)feature.getProperty("rk_k"));
                rkObject.setKst((Double)feature.getProperty("rk_kst"));

                objectSet.add(rkObject);
            }
        }

        int number = 1000;
        for (final RkObject obj : objectSet) {
            final String[] contentFields = new String[4];
            Integer rkNumber = obj.getRk();

            if (rkNumber == null) {
                rkNumber = ++number;
            }
            contentFields[RK_FIELDS.RK.ordinal()] = rkNumber.toString();
            contentFields[RK_FIELDS.NAME.ordinal()] = obj.getName();
            contentFields[RK_FIELDS.K.ordinal()] = obj.getK().toString();
            contentFields[RK_FIELDS.KST.ordinal()] = obj.getKst().toString();

            catalogue.content.add(contentFields);
        }
        for (int i = 0; i < 4; ++i) {
            catalogue.columnIndex[i] = i;
        }

        catalogue.type = FILE_TYPE.RK;

        return catalogue;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   list      DOCUMENT ME!
     * @param   property  DOCUMENT ME!
     * @param   value     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static CidsBean getBeanByProperty(final List<CidsBean> list, final String property, final Object value) {
        for (final CidsBean b : list) {
            final Object p = b.getProperty(property);
            if ((p != null) && p.toString().equals(value.toString())) {
                return b;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   catalogueFile  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private void init(final File catalogueFile) throws IllegalArgumentException {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(catalogueFile));
            final String headerLine = reader.readLine();
            final List<String> headers = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(headerLine, "\t;");
            int index = 0;

            determineFileType(headerLine);

            if (type.equals(FILE_TYPE.UNKNOWN)) {
                LOG.error("unknown file type. Custom rk or custom bk catalogue expected.");
                throw new IllegalArgumentException(
                    "Die Kopfzeile passt weder zu einer Rauheits- noch zu einer Bewuchsklasse");
            }

            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                headers.add(token);

                try {
                    if (type.equals(FILE_TYPE.RK)) {
                        final RK_FIELDS field = RK_FIELDS.valueOf(token.toUpperCase());
                        columnIndex[field.ordinal()] = index;
                    } else {
                        final BK_FIELDS field = BK_FIELDS.valueOf(token.toUpperCase());
                        columnIndex[field.ordinal()] = index;
                    }
                } catch (IllegalArgumentException e) {
                    // nothing to do
                }
                ++index;
            }
            header = headers.toArray(new String[headers.size()]);

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.length() < 2) {
                    // end of file. The last line contains a single character
                    break;
                }
                st = new StringTokenizer(line, "\t;");
                final List<String> contFields = new ArrayList<String>();

                while (st.hasMoreTokens()) {
                    final String token = st.nextToken();
                    contFields.add(token);
                }
                final String[] contentFields = contFields.toArray(new String[contFields.size()]);

                content.add(contentFields);
            }

            // check file format
            int i = 0;
            for (final String[] fileLine : content) {
                i++;
                try {
                    if (type.equals(FILE_TYPE.RK)) {
                        createRkObjectFromLine(fileLine);
                    } else if (type.equals(FILE_TYPE.BK)) {
                        createBkObjectFromLine(fileLine);
                    }
                } catch (IllegalArgumentException e) {
                    LOG.error("not parsable line found " + i, e);

                    throw new IllegalArgumentException("Ungültige Werte in Zeile " + i + " gefunden.");
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error while reading GAF file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    LOG.error("Cannot close reader", ex);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  headerLine  DOCUMENT ME!
     */
    private void determineFileType(final String headerLine) {
        if (headerLine.toUpperCase().contains("BK") && headerLine.toUpperCase().contains("NAME")
                    && headerLine.toUpperCase().contains("AX") && headerLine.toUpperCase().contains("AY")
                    && headerLine.toUpperCase().contains("DP")) {
            type = FILE_TYPE.BK;
        } else if (headerLine.toUpperCase().contains("RK") && headerLine.toUpperCase().contains("NAME")
                    && headerLine.toUpperCase().contains("K") && headerLine.toUpperCase().contains("KST")) {
            type = FILE_TYPE.RK;
        } else {
            type = FILE_TYPE.UNKNOWN;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String createCatalogueFile() {
        final StringBuilder gafContent = new StringBuilder();

        if (type.equals(FILE_TYPE.RK)) {
            for (final RK_FIELDS col : RK_FIELDS.values()) {
                gafContent.append(col.name());

                if (col.ordinal() != (RK_FIELDS.values().length - 1)) {
                    gafContent.append("\t");
                }
            }
        } else {
            for (final BK_FIELDS col : BK_FIELDS.values()) {
                gafContent.append(col.name());

                if (col.ordinal() != (RK_FIELDS.values().length - 1)) {
                    gafContent.append("\t");
                }
            }
        }
        gafContent.append("\r\n");

        if (type.equals(FILE_TYPE.RK)) {
            for (final String[] line : content) {
                for (final RK_FIELDS col : RK_FIELDS.values()) {
                    gafContent.append(line[columnIndex[col.ordinal()]]);

                    if (col.ordinal() != (RK_FIELDS.values().length - 1)) {
                        gafContent.append("\t");
                    }
                }
                gafContent.append("\r\n");
            }
        } else {
            for (final String[] line : content) {
                for (final BK_FIELDS col : BK_FIELDS.values()) {
                    gafContent.append(line[columnIndex[col.ordinal()]]);

                    if (col.ordinal() != (BK_FIELDS.values().length - 1)) {
                        gafContent.append("\t");
                    }
                }
                gafContent.append("\r\n");
            }
        }

        return gafContent.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public List<Integer> getAllRk() {
        final List<Integer> rk = new ArrayList<Integer>();

        if (!type.equals(FILE_TYPE.RK)) {
            throw new IllegalArgumentException("The getAllRk method is only allowed on a rk file type");
        }

        for (final String[] line : content) {
            try {
                rk.add(Integer.parseInt(line[columnIndex[RK_FIELDS.RK.ordinal()]]));
            } catch (NumberFormatException e) {
                LOG.error("Rk is not a number", e);
            }
        }

        return rk;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public List<Integer> getAllBk() {
        final List<Integer> bk = new ArrayList<Integer>();

        if (!type.equals(FILE_TYPE.BK)) {
            throw new IllegalArgumentException("The getAllBk method is only allowed on a bk file type");
        }

        for (final String[] line : content) {
            try {
                bk.add(Integer.parseInt(line[columnIndex[BK_FIELDS.BK.ordinal()]]));
            } catch (NumberFormatException e) {
                LOG.error("Bk is not a number", e);
            }
        }

        return bk;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     * @param   k     DOCUMENT ME!
     * @param   kst   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public String getRkId(final String name, final Double k, final Double kst) {
        if (!type.equals(FILE_TYPE.RK)) {
            throw new IllegalArgumentException("The getRkId method is only allowed on a rk file type");
        }

        for (final String[] line : content) {
            if (line[columnIndex[RK_FIELDS.NAME.ordinal()]].equals(name)
                        && line[columnIndex[RK_FIELDS.K.ordinal()]].equals(k.toString())
                        && line[columnIndex[RK_FIELDS.KST.ordinal()]].equals(kst.toString())) {
                return line[columnIndex[RK_FIELDS.RK.ordinal()]];
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     * @param   ax    DOCUMENT ME!
     * @param   ay    DOCUMENT ME!
     * @param   dp    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public String getBkId(final String name, final Double ax, final Double ay, final Double dp) {
        if (!type.equals(FILE_TYPE.BK)) {
            throw new IllegalArgumentException("The getBkId method is only allowed on a bk file type");
        }

        for (final String[] line : content) {
            if (line[columnIndex[BK_FIELDS.NAME.ordinal()]].equals(name)
                        && line[columnIndex[BK_FIELDS.AX.ordinal()]].equals(ax.toString())
                        && line[columnIndex[BK_FIELDS.AY.ordinal()]].equals(ay.toString())
                        && line[columnIndex[BK_FIELDS.DP.ordinal()]].equals(dp.toString())) {
                return line[columnIndex[BK_FIELDS.BK.ordinal()]];
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public RkObject getRkById(final String id) {
        if (!type.equals(FILE_TYPE.RK)) {
            throw new IllegalArgumentException("The getRkById method is only allowed on a rk file type");
        }

        for (final String[] line : content) {
            if (line[columnIndex[RK_FIELDS.RK.ordinal()]].equals(id)) {
                return createRkObjectFromLine(line);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public BkObject getBkById(final String id) {
        if (!type.equals(FILE_TYPE.BK)) {
            throw new IllegalArgumentException("The getBkById method is only allowed on a bk file type");
        }

        for (final String[] line : content) {
            if (line[columnIndex[BK_FIELDS.BK.ordinal()]].equals(id)) {
                return createBkObjectFromLine(line);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private BkObject createBkObjectFromLine(final String[] line) throws IllegalArgumentException {
        if (!type.equals(FILE_TYPE.BK)) {
            throw new IllegalArgumentException("The createBkObjectFromLine method is only allowed on a bk file type");
        }
        final BkObject obj = new BkObject();

        try {
            obj.setBk(Integer.parseInt(line[columnIndex[BK_FIELDS.BK.ordinal()]]));
            obj.setName(line[columnIndex[BK_FIELDS.NAME.ordinal()]]);
            obj.setAx(Double.parseDouble(line[columnIndex[BK_FIELDS.AX.ordinal()]].replace(',', '.')));
            obj.setAy(Double.parseDouble(line[columnIndex[BK_FIELDS.AY.ordinal()]].replace(',', '.')));
            obj.setDp(Double.parseDouble(line[columnIndex[BK_FIELDS.DP.ordinal()]].replace(',', '.')));
        } catch (NumberFormatException e) {
            LOG.error("Cannot parse bk line", e);

            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
        return obj;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    private RkObject createRkObjectFromLine(final String[] line) throws IllegalArgumentException {
        if (!type.equals(FILE_TYPE.RK)) {
            throw new IllegalArgumentException("The createRkObjectFromLine method is only allowed on a rk file type");
        }
        final RkObject obj = new RkObject();

        try {
            obj.setRk(Integer.parseInt(line[columnIndex[RK_FIELDS.RK.ordinal()]]));
            obj.setName(line[columnIndex[RK_FIELDS.NAME.ordinal()]]);
            obj.setK(Double.parseDouble(line[columnIndex[RK_FIELDS.K.ordinal()]].replace(',', '.')));
            obj.setKst(Double.parseDouble(line[columnIndex[RK_FIELDS.KST.ordinal()]].replace(',', '.')));
        } catch (NumberFormatException e) {
            LOG.error("Cannot parse rk line", e);

            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
        return obj;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class RkObject implements Comparable<RkObject> {

        //~ Instance fields ----------------------------------------------------

        private Integer rk;
        private String name;
        private Double k;
        private Double kst;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RkObject object.
         */
        public RkObject() {
        }

        /**
         * Creates a new RkObject object.
         *
         * @param  bean  DOCUMENT ME!
         */
        protected RkObject(final CidsBean bean) {
            rk = (Integer)bean.getProperty("rk");
            name = (String)bean.getProperty("name");
            k = (Double)bean.getProperty("k");
            kst = (Double)bean.getProperty("kst");
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the rk
         */
        public Integer getRk() {
            return rk;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rk  the rk to set
         */
        public void setRk(final int rk) {
            this.rk = rk;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the name
         */
        public String getName() {
            return name;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  name  the name to set
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the k
         */
        public Double getK() {
            return k;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  k  the k to set
         */
        public void setK(final double k) {
            this.k = k;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the kst
         */
        public Double getKst() {
            return kst;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  kst  the kst to set
         */
        public void setKst(final double kst) {
            this.kst = kst;
        }

        @Override
        public int compareTo(final RkObject o) {
            if ((o.rk == null) && (rk != null)) {
                return -1;
            } else if ((o.rk != null) && (rk == null)) {
                return 1;
            } else if ((o.rk == null) && (rk == null)) {
                if (name.equals(o.name)) {
                    if (k.equals(o.k)) {
                        return kst.compareTo(o.kst);
                    } else {
                        return k.compareTo(o.k);
                    }
                } else {
                    return name.compareTo(o.name);
                }
            } else {
                return rk.compareTo(o.rk);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class BkObject implements Comparable<BkObject> {

        //~ Instance fields ----------------------------------------------------

        private Integer bk;
        private String name;
        private Double ax;
        private Double ay;
        private Double dp;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new BkObject object.
         */
        public BkObject() {
        }

        /**
         * Creates a new BkObject object.
         *
         * @param  bean  DOCUMENT ME!
         */
        protected BkObject(final CidsBean bean) {
            bk = (Integer)bean.getProperty("bk");
            name = (String)bean.getProperty("name");
            ax = (Double)bean.getProperty("ax");
            ay = (Double)bean.getProperty("ay");
            dp = (Double)bean.getProperty("dp");
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the bk
         */
        public Integer getBk() {
            return bk;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  bk  the bk to set
         */
        public void setBk(final int bk) {
            this.bk = bk;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the name
         */
        public String getName() {
            return name;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  name  the name to set
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the ax
         */
        public Double getAx() {
            return ax;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  ax  the ax to set
         */
        public void setAx(final double ax) {
            this.ax = ax;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the ay
         */
        public Double getAy() {
            return ay;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  ay  the ay to set
         */
        public void setAy(final double ay) {
            this.ay = ay;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the dp
         */
        public Double getDp() {
            return dp;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  dp  the dp to set
         */
        public void setDp(final double dp) {
            this.dp = dp;
        }

        @Override
        public int compareTo(final BkObject o) {
            if ((o.bk == null) && (bk != null)) {
                return -1;
            } else if ((o.bk != null) && (bk == null)) {
                return 1;
            } else if ((o.bk == null) && (bk == null)) {
                if (name.equals(o.name)) {
                    if (ax.equals(o.ax)) {
                        if (ay.equals(o.ay)) {
                            return dp.compareTo(o.dp);
                        } else {
                            return ay.compareTo(o.ay);
                        }
                    } else {
                        return ax.compareTo(o.ax);
                    }
                } else {
                    return name.compareTo(o.name);
                }
            } else {
                return bk.compareTo(o.bk);
            }
        }
    }
}
