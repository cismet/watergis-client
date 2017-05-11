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
package de.cismet.cismap.custom.attributerule;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;

import java.io.IOException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import java.sql.Date;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.cismet.cids.custom.watergis.server.search.UniquenessCheck;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.attributetable.LinkCellRenderer;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.Proxy;

import de.cismet.tools.PasswordEncrypter;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.downloadmanager.WebDavDownload;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.LinkTableCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WatergisDefaultRuleSet extends DefaultCidsLayerAttributeTableRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(WatergisDefaultRuleSet.class);
    private static final String PROTECTEC_WBBL_PATH = "http://fry.fis-wasser-mv.de/watergis_secure/wr_wbu_wbbl_g/";
    private static final String UNPROTECTEC_WBBL_PATH = "http://fry.fis-wasser-mv.de/watergis/wr_wbu_wbbl_o/";
    private static final String SG_LINK_TABLE_PATH = "http://fry.fis-wasser-mv.de/watergis/sg_link_tabelle/";
    private static final String PHOTO_PATH = "http://fry.fis-wasser-mv.de/watergis/";
    private static String WEB_DAV_PASSWORD = null;
    private static String WEB_DAV_USER = null;
    private static Boolean accessToProtectedWbbl = null;

    private static int currentYear = 0;

    /**
     * DOCUMENT ME!
     *
     * @param   propertyName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static final String PROTECTED_AREA_ACTION = "geschuetzte_wbbl";
    protected static Double minBaLength = null;

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    enum Validation {

        //~ Enum constants -----------------------------------------------------

        OK, NULL, WRONG_DATA_TYPE, OUT_OF_SIZE, SIZE_CORRECTION, WRONG_RANGE, WBBL_NOT_ACCESSIBLE, NOT_BINARY
    }

    //~ Instance fields --------------------------------------------------------

    protected final Map<String, DataType> typeMap = new HashMap<String, DataType>();
    private final Map<DataType, TreeSet<FeatureServiceFeature>> changedObjectMap =
        new HashMap<DataType, TreeSet<FeatureServiceFeature>>();

    //~ Instance initializers --------------------------------------------------

    {
        // The ResourceBundle cannot be used in a static block. Don't know why

        if (WEB_DAV_USER == null) {
            if (hasAccessToProtectedWbbl()) {
                final ResourceBundle bundle = ResourceBundle.getBundle(
                        "de/cismet/watergis/configuration/wbblWebDev");

                if (bundle == null) {
                    WEB_DAV_PASSWORD = null;
                    WEB_DAV_USER = "unknown";
                    LOG.error("wbblWebDav.properties not found");
                } else {
                    String pass = bundle.getString("password");

                    if ((pass != null)) {
                        pass = new String(PasswordEncrypter.decrypt(pass.toCharArray(), true));
                    }

                    WEB_DAV_PASSWORD = pass;
                    WEB_DAV_USER = bundle.getString("username");
                }
            } else {
                WEB_DAV_PASSWORD = null;
                WEB_DAV_USER = "unknown";
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  path  DOCUMENT ME!
     * @param  file  DOCUMENT ME!
     */
    protected void downloadDocumentFromWebDav(String path, String file) {
        if (DownloadManagerDialog.getInstance().showAskingForUserTitleDialog(AppBroker.getInstance().getRootWindow())) {
            final String jobname = DownloadManagerDialog.getInstance().getJobName();
            String extension = null;
            String filename;

            // remove slashs from the file
            while (file.startsWith("/")) {
                file = file.substring(1);
            }

            if (!path.endsWith("/")) {
                path = path + "/";
            }

            if (file.contains(".")) {
                extension = file.substring(file.lastIndexOf("."));
                filename = file.substring(0, file.lastIndexOf("."));
            } else {
                filename = file;
            }

            if (filename.contains("/")) {
                filename = filename.substring(filename.lastIndexOf("/") + 1);
            }

            final WebDavClient webDavClient = new WebDavClient(Proxy.fromPreferences(), WEB_DAV_USER, WEB_DAV_PASSWORD);

            DownloadManager.instance()
                    .add(new WebDavDownload(
                            webDavClient,
                            path
                            + WebDavHelper.encodeURL(file),
                            jobname,
                            filename
                            + extension,
                            filename,
                            extension));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   path  DOCUMENT ME!
     * @param   file  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean checkDocumentExistenceOnWebDav(String path, String file) {
        // remove slashs from the file
        while (file.startsWith("/")) {
            file = file.substring(1);
        }

        if (!path.endsWith("/")) {
            path = path + "/";
        }

        final WebDavClient webDavClient = new WebDavClient(Proxy.fromPreferences(), WEB_DAV_USER, WEB_DAV_PASSWORD);

        try {
            final int statusCode = webDavClient.getStatusCode(path + WebDavHelper.encodeURL(file));

            return (statusCode == 200) || (statusCode == 204);
        } catch (IOException ex) {
            LOG.warn("Check link failed with exception.", ex);

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name       DOCUMENT ME!
     * @param   extension  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static String addExtension(final String name, final String extension) {
        if (!name.toLowerCase().endsWith("." + extension.toLowerCase())) {
            return name + "." + extension;
        }

        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getWbblPath() {
        if (hasAccessToProtectedWbbl()) {
            return PROTECTEC_WBBL_PATH;
        } else {
            return UNPROTECTEC_WBBL_PATH;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getSgLinkTablePath() {
        return SG_LINK_TABLE_PATH;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getPhotoPath() {
        return PHOTO_PATH;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     *
     * @return  true, iff tzhe given string is null or empty
     */
    protected static boolean isValueEmpty(final Object field) {
        return (field == null) || field.toString().isEmpty();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n1  DOCUMENT ME!
     * @param   n2  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean isEqual(final Number n1, final Number n2) {
        if (n1 == n2) {
            return true;
        } else if ((n1 == null) || (n2 == null)) {
            return false;
        } else {
            return n1.doubleValue() == n2.doubleValue();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean hasAccessToProtectedWbbl() {
        if (accessToProtectedWbbl == null) {
            try {
                final String attr = SessionManager.getProxy()
                            .getConfigAttr(SessionManager.getSession().getUser(), PROTECTED_AREA_ACTION);

                if (attr != null) {
                    accessToProtectedWbbl = true;
                } else {
                    accessToProtectedWbbl = false;
                }
            } catch (Exception e) {
                LOG.error("Error while checking action attribute", e);

                return false;
            }
        }

        return accessToProtectedWbbl;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   propertyName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected CidsLayerFeatureFilter createCidsLayerFeatureFilter(final String propertyName) {
        return new CidsLayerFeatureFilter() {

                @Override
                public boolean accept(final CidsLayerFeature bean) {
                    if (bean == null) {
                        return true;
                    }

                    return (bean.getProperty(propertyName) != null) && (Boolean)bean.getProperty(propertyName);
                }
            };
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static int getCurrentYear() {
        if (currentYear == 0) {
            currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);
        }

        return currentYear;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected double round(final double value) {
        return Math.round(value * 100) / 100.0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   className      DOCUMENT ME!
     * @param   attributeName  DOCUMENT ME!
     * @param   value          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static CidsBean getCatalogueElement(final String className,
            final String attributeName,
            final String value) {
        final MetaClass mc = ClassCacheMultiple.getMetaClass(
                AppBroker.DOMAIN_NAME,
                className);

        String query = "select " + mc.getID() + ", " + mc.getPrimaryKey() + " from " + mc.getTableName(); // NOI18N
        query += " WHERE " + attributeName + " = '" + value + "'";

        try {
            final MetaObject[] mo = SessionManager.getConnection()
                        .getMetaObjectByQuery(SessionManager.getSession().getUser(), query);

            if ((mo != null) && (mo.length > 0)) {
                return mo[0].getBean();
            }
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     * @param   newValue    DOCUMENT ME!
     * @param   from        DOCUMENT ME!
     * @param   to          DOCUMENT ME!
     * @param   nullable    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean checkRangeBetweenOrEqual(final String columnName,
            final Object newValue,
            final double from,
            final double to,
            final boolean nullable) {
        return checkRange(columnName, newValue, from, to, nullable, true, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     * @param   newValue    DOCUMENT ME!
     * @param   from        DOCUMENT ME!
     * @param   to          DOCUMENT ME!
     * @param   nullable    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean checkRangeBetweenOrEqual(final String columnName,
            final Object newValue,
            final int from,
            final int to,
            final boolean nullable) {
        return checkRange(columnName, newValue, from, to, nullable, true, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     * @param   newValue    DOCUMENT ME!
     * @param   from        DOCUMENT ME!
     * @param   to          DOCUMENT ME!
     * @param   fromMax     DOCUMENT ME!
     * @param   toMax       DOCUMENT ME!
     * @param   nullable    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean checkRangeBetweenOrEqual(final String columnName,
            final Object newValue,
            final double from,
            final double to,
            final double fromMax,
            final double toMax,
            final boolean nullable) {
        return checkRange(columnName, newValue, from, to, fromMax, toMax, nullable, true, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   propName   DOCUMENT ME!
     * @param   value      DOCUMENT ME!
     * @param   allowNull  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean isNoInteger(final String propName, final Object value, final boolean allowNull) {
        if (!isNumberOrNull(value)) {
            return true;
        } else {
            final Number n = toNumber(value);

            if (n == null) {
                return allowNull;
            }

            if (n.doubleValue() != n.intValue()) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + propName
                            + " darf keine Nachkommastellen enthalten");
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName        DOCUMENT ME!
     * @param   newValue          DOCUMENT ME!
     * @param   from              DOCUMENT ME!
     * @param   to                DOCUMENT ME!
     * @param   nullable          DOCUMENT ME!
     * @param   fromEqualAllowed  DOCUMENT ME!
     * @param   toEqualAllowed    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean checkRange(final String columnName,
            Object newValue,
            final double from,
            final double to,
            final boolean nullable,
            final boolean fromEqualAllowed,
            final boolean toEqualAllowed) {
        if ((newValue == null) && !nullable) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut "
                        + columnName
                        + " darf nicht leer sein");
            return false;
        } else if (newValue == null) {
            return true;
        }

        if (!(newValue instanceof Number)) {
            try {
                newValue = Double.parseDouble(String.valueOf(newValue));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + columnName
                            + " darf nur numerische Werte enthalten");
                return false;
            }
        }

        if ((!(fromEqualAllowed && (((Number)newValue).doubleValue() >= from))
                        && !(!fromEqualAllowed && (((Number)newValue).doubleValue() > from)))
                    || (!(toEqualAllowed && (((Number)newValue).doubleValue() <= to))
                        && !(!toEqualAllowed && (((Number)newValue).doubleValue() < to)))) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Beim Attribut "
                        + columnName
                        + " sind nur Werte von "
                        + from
                        + " bis "
                        + to
                        + " erlaubt.");
            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName        DOCUMENT ME!
     * @param   newValue          DOCUMENT ME!
     * @param   from              DOCUMENT ME!
     * @param   to                DOCUMENT ME!
     * @param   nullable          DOCUMENT ME!
     * @param   fromEqualAllowed  DOCUMENT ME!
     * @param   toEqualAllowed    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean checkRange(final String columnName,
            Object newValue,
            final int from,
            final int to,
            final boolean nullable,
            final boolean fromEqualAllowed,
            final boolean toEqualAllowed) {
        if ((newValue == null) && !nullable) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut "
                        + columnName
                        + " darf nicht leer sein");
            return false;
        } else if (newValue == null) {
            return true;
        }

        if (!(newValue instanceof Number)) {
            try {
                newValue = Double.parseDouble(String.valueOf(newValue));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + columnName
                            + " darf nur numerische Werte enthalten");
                return false;
            }
        }

        if ((!(fromEqualAllowed && (((Number)newValue).doubleValue() >= from))
                        && !(!fromEqualAllowed && (((Number)newValue).doubleValue() > from)))
                    || (!(toEqualAllowed && (((Number)newValue).doubleValue() <= to))
                        && !(!toEqualAllowed && (((Number)newValue).doubleValue() < to)))) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Beim Attribut "
                        + columnName
                        + " sind nur Werte von "
                        + from
                        + " bis "
                        + to
                        + " erlaubt.");
            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName        DOCUMENT ME!
     * @param   newValue          DOCUMENT ME!
     * @param   from              DOCUMENT ME!
     * @param   to                DOCUMENT ME!
     * @param   fromMax           DOCUMENT ME!
     * @param   toMax             DOCUMENT ME!
     * @param   nullable          DOCUMENT ME!
     * @param   fromEqualAllowed  DOCUMENT ME!
     * @param   toEqualAllowed    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean checkRange(final String columnName,
            Object newValue,
            final double from,
            final double to,
            final double fromMax,
            final double toMax,
            final boolean nullable,
            final boolean fromEqualAllowed,
            final boolean toEqualAllowed) {
        if ((newValue == null) && !nullable) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut "
                        + columnName
                        + " darf nicht leer sein");
            return false;
        } else if (newValue == null) {
            return true;
        }

        if (!(newValue instanceof Number)) {
            try {
                newValue = Double.parseDouble(String.valueOf(newValue));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + columnName
                            + " darf nur numerische Werte enthalten");
                return false;
            }
        }

        final double newDoubleValue = ((Number)newValue).doubleValue();

        if ((fromEqualAllowed && ((newDoubleValue < from) && (newDoubleValue >= fromMax)))
                    || (!fromEqualAllowed && ((newDoubleValue <= from) && (newDoubleValue > fromMax)))
                    || (toEqualAllowed && ((newDoubleValue <= toMax) && (newDoubleValue > to)))
                    || (!toEqualAllowed && ((newDoubleValue < toMax) && (newDoubleValue >= to)))) {
            return showSecurityQuestion("Wert außerhalb Standardbereich (" + from + ", " + to + ") --> verwenden ?");
        } else if ((fromEqualAllowed && (newDoubleValue < fromMax))
                    || (!fromEqualAllowed && (newDoubleValue <= fromMax))
                    || (toEqualAllowed && (newDoubleValue > toMax))
                    || (!toEqualAllowed && (newDoubleValue >= toMax))) {
            showMessage("Wert nicht zulässig, weil außerhalb "
                        + (fromEqualAllowed ? " [" : " (")
                        + fromMax
                        + ", "
                        + toMax
                        + (toEqualAllowed ? " ]" : " )"));
            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName        DOCUMENT ME!
     * @param   newValue          DOCUMENT ME!
     * @param   from              DOCUMENT ME!
     * @param   to                DOCUMENT ME!
     * @param   fromMax           DOCUMENT ME!
     * @param   toMax             DOCUMENT ME!
     * @param   nullable          DOCUMENT ME!
     * @param   fromEqualAllowed  DOCUMENT ME!
     * @param   toEqualAllowed    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean checkRange(final String columnName,
            Object newValue,
            final int from,
            final int to,
            final int fromMax,
            final int toMax,
            final boolean nullable,
            final boolean fromEqualAllowed,
            final boolean toEqualAllowed) {
        if ((newValue == null) && !nullable) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut "
                        + columnName
                        + " darf nicht leer sein");
            return false;
        } else if (newValue == null) {
            return true;
        }

        if (!(newValue instanceof Number)) {
            try {
                newValue = Double.parseDouble(String.valueOf(newValue));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + columnName
                            + " darf nur numerische Werte enthalten");
                return false;
            }
        }

        final double newDoubleValue = ((Number)newValue).doubleValue();

        if ((fromEqualAllowed && ((newDoubleValue < from) && (newDoubleValue >= fromMax)))
                    || (!fromEqualAllowed && ((newDoubleValue <= from) && (newDoubleValue > fromMax)))
                    || (toEqualAllowed && ((newDoubleValue <= toMax) && (newDoubleValue > to)))
                    || (!toEqualAllowed && ((newDoubleValue < toMax) && (newDoubleValue >= to)))) {
            return showSecurityQuestion("Wert außerhalb Standardbereich (" + from + ", " + to + ") --> verwenden ?");
        } else if ((fromEqualAllowed && (newDoubleValue < fromMax))
                    || (!fromEqualAllowed && (newDoubleValue <= fromMax))
                    || (toEqualAllowed && (newDoubleValue > toMax))
                    || (!toEqualAllowed && (newDoubleValue >= toMax))) {
            showMessage("Wert nicht zulässig, weil außerhalb "
                        + (fromEqualAllowed ? " [" : " (")
                        + fromMax
                        + ", "
                        + toMax
                        + (toEqualAllowed ? " ]" : " )"));
            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  text  DOCUMENT ME!
     */
    protected static void showMessage(final String text) {
        final MessageDialog d = new MessageDialog(AppBroker.getInstance().getWatergisApp(), true, text);
        d.setSize(500, 80);
        StaticSwingTools.showDialog(d);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   text  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean showSecurityQuestion(final String text) {
        final int answ = JOptionPane.showConfirmDialog(AppBroker.getInstance().getWatergisApp(),
                text,
                "Bestätigung",
                JOptionPane.YES_NO_OPTION);

        return answ == JOptionPane.YES_OPTION;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName       DOCUMENT ME!
     * @param   newValue         DOCUMENT ME!
     * @param   minVal           from DOCUMENT ME!
     * @param   nullable         DOCUMENT ME!
     * @param   minEqualAllowed  to DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean checkGreaterThan(final String columnName,
            Object newValue,
            final double minVal,
            final boolean nullable,
            final boolean minEqualAllowed) {
        if ((newValue == null) && !nullable) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut "
                        + columnName
                        + " darf nicht leer sein");
            return false;
        } else if (newValue == null) {
            return true;
        }

        if (!(newValue instanceof Number)) {
            try {
                newValue = Double.parseDouble(String.valueOf(newValue));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + columnName
                            + " darf nur numerische Werte enthalten");
                return false;
            }
        }

        if ((minEqualAllowed && (((Number)newValue).doubleValue() < minVal))
                    || (!minEqualAllowed && (((Number)newValue).doubleValue() <= minVal))) {
            final String greaterText = (minEqualAllowed ? " größer oder gleich " : " größer ");
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut "
                        + columnName
                        + " muss"
                        + greaterText
                        + minVal
                        + " sein.");
            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value          DOCUMENT ME!
     * @param   allowedValues  DOCUMENT ME!
     * @param   nullAllowed    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean isValueIn(final Object value, final Object[] allowedValues, final boolean nullAllowed) {
        if (value == null) {
            return nullAllowed;
        }
        final String stringValue = value.toString();

        for (final Object o : allowedValues) {
            if (stringValue.equals(o)) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     * @param   newValue    DOCUMENT ME!
     * @param   value       from DOCUMENT ME!
     * @param   nullable    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean hasValue(final String columnName,
            Object newValue,
            final double value,
            final boolean nullable) {
        if ((newValue == null) && !nullable) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                "Das Attribut "
                        + columnName
                        + " darf nicht leer sein");
            return false;
        } else if (newValue == null) {
            return true;
        }

        if (!(newValue instanceof Number)) {
            try {
                newValue = Double.parseDouble(String.valueOf(newValue));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + columnName
                            + " darf nur numerische Werte enthalten");
                return false;
            }
        }

        if (((Number)newValue).doubleValue() != value) {
            return false;
        }

        return true;
    }

    /**
     * Converts the given argument to a number. Returns 0, if the given argument cannot be converted to a valid number
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static Number toNumber(final Object o) {
        if (o == null) {
            return 0.0;
        }

        if (!(o instanceof Number)) {
            try {
                return Double.parseDouble(String.valueOf(o));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else {
            return (Number)o;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean isNumberOrNull(final Object o) {
        if (o == null) {
            return true;
        }

        if (!(o instanceof Number)) {
            try {
                final Double d = Double.parseDouble(String.valueOf(o));
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   array    DOCUMENT ME!
     * @param   element  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean arrayContains(final String[] array, final String element) {
        for (final String tmp : array) {
            if (((tmp == null) && (element == null))
                        || ((tmp != null) && (element != null) && tmp.toLowerCase().equals(element.toLowerCase()))) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   array  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static String arrayToString(final String[] array) {
        final StringBuilder builder = new StringBuilder();

        for (final String tmp : array) {
            if (builder.length() > 0) {
                builder.append("/");
            }

            builder.append(tmp);
        }

        return builder.toString();
    }

    @Override
    public void copyProperties(final FeatureServiceFeature sourceFeature, final FeatureServiceFeature targetFeature) {
        // copy properties
        final Map<String, Object> defaultValues = getDefaultValues();

        if (defaultValues != null) {
            for (final String propName : defaultValues.keySet()) {
                targetFeature.setProperty(propName, defaultValues.get(propName));
            }
        }

        final boolean hasIdExpression = targetFeature.getLayerProperties().getIdExpressionType()
                    == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME;
        final Map<String, FeatureServiceAttribute> attributeMap = targetFeature.getLayerProperties()
                    .getFeatureService()
                    .getFeatureServiceAttributes();

        for (final String attrKey : attributeMap.keySet()) {
            if (hasIdExpression
                        && targetFeature.getLayerProperties().getIdExpression().equalsIgnoreCase(attrKey)) {
                // do not change the id
                continue;
            }
            if (isColumnEditable(attrKey) || attrKey.equalsIgnoreCase("ba_cd") || attrKey.equalsIgnoreCase("bak_cd")) {
                final Object val = getFeaturePropertyIgnoreCase(sourceFeature, attrKey);
                if (val != null) {
                    // without this null check, the geometry will probably be overwritten
                    targetFeature.setProperty(attrKey, val);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     * @param   name     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Object getFeaturePropertyIgnoreCase(final FeatureServiceFeature feature, final String name) {
        for (final Object prop : feature.getProperties().keySet()) {
            if (prop instanceof String) {
                final String propName = (String)prop;
                if (propName.equalsIgnoreCase(name)) {
                    return feature.getProperty(propName);
                }
            }
        }

        return null;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        final DataType type = typeMap.get(columnName);

        if (columnName.equals("id") || columnName.equals("ww_gr")) {
            return new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(final JTable table,
                            final Object value,
                            final boolean isSelected,
                            final boolean hasFocus,
                            final int row,
                            final int column) {
                        final Component c = super.getTableCellRendererComponent(
                                table,
                                value,
                                isSelected,
                                hasFocus,
                                row,
                                column);

                        if (c instanceof JLabel) {
                            ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                            ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                        }

                        return c;
                    }
                };
        }

        if (type != null) {
            if (type instanceof Numeric) {
                return new DefaultTableCellRenderer() {

                        DecimalFormat format = new DecimalFormat();
                        DecimalFormat formatWithOutdecimals = new DecimalFormat();

                        {
                            format.setGroupingUsed(false);
                            format.setMaximumFractionDigits(((Numeric)type).getScale());
                            format.setMinimumFractionDigits(((Numeric)type).getScale());
                            formatWithOutdecimals.setGroupingUsed(false);
                            formatWithOutdecimals.setMaximumFractionDigits(0);
                        }

                        @Override
                        public Component getTableCellRendererComponent(final JTable table,
                                final Object value,
                                final boolean isSelected,
                                final boolean hasFocus,
                                final int row,
                                final int column) {
                            Object val = value;

                            if (value instanceof Number) {
                                val = format.format(value);
                            } else if (value instanceof String) {
                                try {
                                    val = Double.parseDouble((String)value);
                                    val = format.format(val);
                                } catch (NumberFormatException e) {
                                    // should not happen
                                    LOG.error("Numeric field does not contain a numeric value", e);
                                }
                            }

                            if ((val != null) && ((Numeric)type).isShowDecimalsOnlyIfExists()) {
                                try {
                                    final double doubleVal = format.parse(val.toString()).doubleValue();
                                    final long longVal = (long)doubleVal;

                                    if (doubleVal == longVal) {
                                        val = formatWithOutdecimals.format(longVal);
                                    }
                                } catch (final Exception e) {
                                    // should not happen
                                    LOG.error("Numeric field does not contain a numeric value", e);
                                }
                            }

                            final Component c = super.getTableCellRendererComponent(
                                    table,
                                    val,
                                    isSelected,
                                    hasFocus,
                                    row,
                                    column);

                            if (c instanceof JLabel) {
                                ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                                ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                            }

                            return c;
                        }
                    };
            }

            if (type instanceof BooleanAsInteger) {
                return new DefaultTableCellRenderer() {

                        @Override
                        public Component getTableCellRendererComponent(final JTable table,
                                final Object value,
                                final boolean isSelected,
                                final boolean hasFocus,
                                final int row,
                                final int column) {
                            final Component c = super.getTableCellRendererComponent(
                                    table,
                                    value,
                                    isSelected,
                                    hasFocus,
                                    row,
                                    column);

                            if (c instanceof JLabel) {
                                ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
                                ((JLabel)c).setBorder(new EmptyBorder(0, 0, 0, 2));
                            }

                            return c;
                        }
                    };
            }

            if (((type instanceof Catalogue) && ((Catalogue)type).isRightAlignment())
                        || ((type instanceof Link) && ((Link)type).isRightAlignment())) {
                return new LinkTableCellRenderer(JLabel.RIGHT);
            }
        }

        return null;
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        final DataType type = typeMap.get(column);

        if (type != null) {
            // unique check
            if (type.isUnique()) {
                TreeSet<FeatureServiceFeature> changedObjects = changedObjectMap.get(type);
                if (changedObjects == null) {
                    changedObjects = new TreeSet<FeatureServiceFeature>();
                    changedObjectMap.put(type, changedObjects);
                }
                changedObjects.add(feature);

                // start unique check
                final Map<Integer, String> idValueMap = new HashMap<Integer, String>();

                if ((changedObjects != null) && changedObjects.contains(feature)) {
                    final String stalu = (String)newValue;
                    idValueMap.put((Integer)feature.getProperty("id"), stalu);
                }

                if (!idValueMap.isEmpty()) {
                    try {
                        final User user = SessionManager.getSession().getUser();
                        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager
                                    .getProxy()
                                    .customServerSearch(user, new UniquenessCheck(idValueMap, type.field, type.table));

                        if ((attributes != null) && !attributes.isEmpty()) {
                            final ArrayList list = attributes.get(0);

                            if ((list != null) && !list.isEmpty()) {
                                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                    NbBundle.getMessage(
                                        WatergisDefaultRuleSet.class,
                                        "WatergisDefaultRuleSet.prepareForSave().message",
                                        list.get(0),
                                        type.field),
                                    NbBundle.getMessage(
                                        WatergisDefaultRuleSet.class,
                                        "WatergisDefaultRuleSet.prepareForSave().title",
                                        type.field),
                                    JOptionPane.ERROR_MESSAGE);
                                return oldValue;
                            }
                        }
                    } catch (final Exception e) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            NbBundle.getMessage(
                                WatergisDefaultRuleSet.class,
                                "WatergisDefaultRuleSet.prepareForSave().error.message",
                                type.field),
                            NbBundle.getMessage(
                                WatergisDefaultRuleSet.class,
                                "WatergisDefaultRuleSet.prepareForSave().error.title"),
                            JOptionPane.ERROR_MESSAGE);
                        LOG.error("Error while checking the uniqueness of the ba_cd field.", e);
                        return oldValue;
                    }
                }
            }
            // end unique check

            final ValidationResult result = type.isValidValue(newValue);

            if (result.getValidationResult() == Validation.OK) {
                return newValue;
            } else if (result.getValidationResult() == Validation.NULL) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " darf nicht leer sein");
                return oldValue;
            } else if (result.getValidationResult() == Validation.SIZE_CORRECTION) {
                return result.getChangedValue();
            } else if (result.getValidationResult() == Validation.OUT_OF_SIZE) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " muss den Datentyp "
                            + type.toString()
                            + " haben");
                return oldValue;
            } else if (result.getValidationResult() == Validation.WRONG_DATA_TYPE) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " muss den Datentyp "
                            + type.toString()
                            + " haben");
                return oldValue;
            } else if (result.getValidationResult() == Validation.WRONG_RANGE) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + column
                            + " hat folgenden Wertebereich: "
                            + type.range(column));
                return oldValue;
            } else if (result.getValidationResult() == Validation.NOT_BINARY) {
                showMessage("Wert nicht zulässig, weil außerhalb (0/1"
                            + (type.isNotNull() ? "" : "/NULL")
                            + ")");
                return oldValue;
            } else if (result.getValidationResult() == Validation.WBBL_NOT_ACCESSIBLE) {
                showMessage("Wert nicht zulässig, weil Wasserbuchblatt nicht existiert");
                return oldValue;
            }
        }
        return newValue;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        for (final String attribute : typeMap.keySet()) {
            final Map<Integer, String> idValueMap = new HashMap<Integer, String>();
            final DataType type = typeMap.get(attribute);

            if (!type.editable) {
                continue;
            }

            for (final FeatureServiceFeature feature : features) {
                if (type.isUnique()) {
                    final TreeSet<FeatureServiceFeature> changedObjects = changedObjectMap.get(type);

                    if ((changedObjects != null) && changedObjects.contains(feature)) {
                        final String stalu = (String)feature.getProperty(type.field);
                        idValueMap.put((Integer)feature.getProperty("id"), stalu);
                    }
                }

                final ValidationResult result = type.isValidValue(feature.getProperty(attribute));

                if (result.getValidationResult() == Validation.NULL) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut "
                                + attribute
                                + " darf nicht leer sein");
                    return false;
                } else if (result.getValidationResult() == Validation.SIZE_CORRECTION) {
                    try {
                        feature.setProperty(attribute, result.changedValue);
                    } catch (Exception e) {
                        LOG.error("Error while set corrected property value", e);
                    }
                } else if (result.getValidationResult() == Validation.OUT_OF_SIZE) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut "
                                + attribute
                                + " muss den Datentyp "
                                + type.toString()
                                + " haben");
                    return false;
                } else if (result.getValidationResult() == Validation.WRONG_DATA_TYPE) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut "
                                + attribute
                                + " muss den Datentyp "
                                + type.toString()
                                + " haben");
                    return false;
                } else if (result.getValidationResult() == Validation.WRONG_RANGE) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut "
                                + attribute
                                + " hat folgenden Wertebereich: "
                                + type.range(attribute));
                    return false;
                } else if (result.getValidationResult() == Validation.NOT_BINARY) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        "Das Attribut "
                                + attribute
                                + " hat einen ungültigen Wert. Der Wertebereich ist (0/1"
                                + (type.isNotNull() ? "" : "/NULL")
                                + ")");
                    return false;
                }
            }

            // check uniqueness
            if (!idValueMap.isEmpty()) {
                try {
                    final User user = SessionManager.getSession().getUser();
                    final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                                .customServerSearch(user, new UniquenessCheck(idValueMap, type.field, type.table));

                    if ((attributes != null) && !attributes.isEmpty()) {
                        final ArrayList list = attributes.get(0);

                        if ((list != null) && !list.isEmpty()) {
                            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                NbBundle.getMessage(
                                    WatergisDefaultRuleSet.class,
                                    "WatergisDefaultRuleSet.prepareForSave().message",
                                    list.get(0),
                                    type.field),
                                NbBundle.getMessage(
                                    WatergisDefaultRuleSet.class,
                                    "WatergisDefaultRuleSet.prepareForSave().title",
                                    type.field),
                                JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                } catch (final Exception e) {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        NbBundle.getMessage(
                            WatergisDefaultRuleSet.class,
                            "WatergisDefaultRuleSet.prepareForSave().error.message",
                            type.field),
                        NbBundle.getMessage(
                            WatergisDefaultRuleSet.class,
                            "WatergisDefaultRuleSet.prepareForSave().error.title"),
                        JOptionPane.ERROR_MESSAGE);
                    LOG.error("Error while checking the uniqueness of the ba_cd field.", e);
                    return false;
                }
            }
        }

        for (final FeatureServiceFeature feature : features) {
            if (minBaLength != null) {
                final Double from = (Double)feature.getProperty("ba_st_von");
                final Double till = (Double)feature.getProperty("ba_st_bis");

                if ((from != null) && (till != null)) {
                    if (Math.abs(till - from) < minBaLength) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Die Länge des Objektes mit der id "
                                    + feature.getId()
                                    + " darf nicht kleiner "
                                    + minBaLength
                                    + " sein",
                            "Ungültiger Wert",
                            JOptionPane.ERROR_MESSAGE);

                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        BigDecimal bd = new BigDecimal("14E2");
        bd = bd.stripTrailingZeros();
        final int tmpScale = bd.scale();
        final int tmpPrecision = bd.precision();
        final int digitsOnTheLeft = bd.toBigInteger().toString().length();
        System.out.println("scale " + tmpScale + " precision " + tmpPrecision + " left " + digitsOnTheLeft);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected abstract static class DataType {

        //~ Instance fields ----------------------------------------------------

        private final boolean notNull;
        private final boolean unique;
        private final boolean editable;
        private final String field;
        private final String table;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DataType object.
         *
         * @param  notNull   DOCUMENT ME!
         * @param  unique    DOCUMENT ME!
         * @param  editable  DOCUMENT ME!
         * @param  field     DOCUMENT ME!
         * @param  table     DOCUMENT ME!
         */
        public DataType(final boolean notNull,
                final boolean unique,
                final boolean editable,
                final String field,
                final String table) {
            this.notNull = notNull;
            this.unique = unique;
            this.field = field;
            this.table = table;
            this.editable = editable;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the notNull
         */
        public boolean isNotNull() {
            return notNull;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   value  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public ValidationResult isValidValue(final Object value) {
            if (notNull && isValueEmpty(value)) {
                return new ValidationResult(Validation.NULL);
            } else {
                return new ValidationResult(Validation.OK);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isUnique() {
            return unique;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getField() {
            return field;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getTable() {
            return table;
        }

        @Override
        public boolean equals(final Object obj) {
            // two object are only equal, if they are the same physically object
            return this == obj;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public abstract String toString();

        /**
         * DOCUMENT ME!
         *
         * @param   name  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String range(final String name) {
            return "";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class Numeric extends DataType {

        //~ Instance fields ----------------------------------------------------

        private final int precision;
        private final int scale;
        private Double min = null;
        private Double max = null;
        private boolean minEqualsAllowd = true;
        private boolean maxEqualsAllowd = true;
        private boolean showDecimalsOnlyIfExists = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Numeric object.
         *
         * @param  precision  DOCUMENT ME!
         * @param  scale      DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         */
        public Numeric(final int precision, final int scale, final boolean notNull) {
            super(notNull, false, true, null, null);
            this.precision = precision;
            this.scale = scale;
        }

        /**
         * Creates a new Numeric object.
         *
         * @param  precision  DOCUMENT ME!
         * @param  scale      DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         * @param  editable   DOCUMENT ME!
         */
        public Numeric(final int precision, final int scale, final boolean notNull, final boolean editable) {
            super(notNull, false, editable, null, null);
            this.precision = precision;
            this.scale = scale;
        }

        /**
         * Creates a new Numeric object.
         *
         * @param  precision                 DOCUMENT ME!
         * @param  scale                     DOCUMENT ME!
         * @param  notNull                   DOCUMENT ME!
         * @param  editable                  DOCUMENT ME!
         * @param  showDecimalsOnlyIfExists  DOCUMENT ME!
         */
        public Numeric(final int precision,
                final int scale,
                final boolean notNull,
                final boolean editable,
                final boolean showDecimalsOnlyIfExists) {
            this(precision, scale, notNull, editable);
            this.showDecimalsOnlyIfExists = showDecimalsOnlyIfExists;
        }

        /**
         * Creates a new Numeric object.
         *
         * @param  precision  DOCUMENT ME!
         * @param  scale      DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         * @param  editable   DOCUMENT ME!
         * @param  min        DOCUMENT ME!
         * @param  max        DOCUMENT ME!
         */
        public Numeric(final int precision,
                final int scale,
                final boolean notNull,
                final boolean editable,
                final double min,
                final double max) {
            super(notNull, false, editable, null, null);
            this.precision = precision;
            this.scale = scale;
            this.min = min;
            this.max = max;
        }

        /**
         * Creates a new Numeric object.
         *
         * @param  precision  DOCUMENT ME!
         * @param  scale      DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         * @param  unique     DOCUMENT ME!
         * @param  field      DOCUMENT ME!
         * @param  table      DOCUMENT ME!
         */
        public Numeric(final int precision,
                final int scale,
                final boolean notNull,
                final boolean unique,
                final String field,
                final String table) {
            super(notNull, unique, true, field, table);
            this.precision = precision;
            this.scale = scale;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the showDecimalsOnlyIfExists
         */
        public boolean isShowDecimalsOnlyIfExists() {
            return showDecimalsOnlyIfExists;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  showDecimalsOnlyIfExists  the showDecimalsOnlyIfExists to set
         */
        public void setShowDecimalsOnlyIfExists(final boolean showDecimalsOnlyIfExists) {
            this.showDecimalsOnlyIfExists = showDecimalsOnlyIfExists;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getPrecision() {
            return precision;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getScale() {
            return scale;
        }

        @Override
        public ValidationResult isValidValue(final Object value) {
            ValidationResult result = super.isValidValue(value);

            if (result.getValidationResult() == Validation.OK) {
                if ((value instanceof String) || (value instanceof Number)) {
                    final String numberString = value.toString();
                    BigDecimal bd = null;

                    try {
                        bd = new BigDecimal(numberString);
                        bd = bd.stripTrailingZeros();
                        int tmpScale = bd.scale();
                        final int digitsOnTheLeft = bd.precision() - tmpScale;

                        if (tmpScale < 0) {
                            tmpScale = 0;
                        }

                        if (digitsOnTheLeft > (precision - scale)) {
                            result = new ValidationResult(Validation.OUT_OF_SIZE);
                        } else if (tmpScale > scale) {
                            final String doubleString = bd.round(new MathContext(precision, RoundingMode.HALF_UP))
                                        .toString();
                            result = new ValidationResult(Validation.SIZE_CORRECTION, true, doubleString);
                        }
                    } catch (NumberFormatException e) {
                        result = new ValidationResult(Validation.WRONG_DATA_TYPE);
                    }

                    if ((bd != null) && (result.getValidationResult() == Validation.OK)) {
                        if (min != null) {
                            if ((minEqualsAllowd && (bd.doubleValue() < min))
                                        || (!minEqualsAllowd && (bd.doubleValue() <= min))) {
                                result = new ValidationResult(Validation.WRONG_RANGE);
                            }
                        }
                        if (max != null) {
                            if ((maxEqualsAllowd && (bd.doubleValue() > max))
                                        || (!maxEqualsAllowd && (bd.doubleValue() >= max))) {
                                result = new ValidationResult(Validation.WRONG_RANGE);
                            }
                        }
                    }
                } else {
                    if (value != null) {
                        result = new ValidationResult(Validation.WRONG_DATA_TYPE);
                    }
                }
            }

            return result;
        }

        @Override
        public String toString() {
            return "Numeric(" + precision + "," + scale + ")";
        }

        @Override
        public String range(final String name) {
            final String minOp = (minEqualsAllowd ? " >= " : " > ");
            final String maxOp = (maxEqualsAllowd ? " <= " : " < ");

            if ((min != null) && (max != null)) {
                if (scale == 0) {
                    return name + minOp + min.intValue() + " und " + name + maxOp + max.intValue();
                } else {
                    return name + minOp + min + " und " + name + maxOp + max;
                }
            } else if (min != null) {
                if (scale == 0) {
                    return name + minOp + min.intValue();
                } else {
                    return name + minOp + min;
                }
            } else if (max != null) {
                if (scale == 0) {
                    return name + maxOp + max.intValue();
                } else {
                    return name + maxOp + max;
                }
            } else {
                return "";
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the min
         */
        public Double getMin() {
            return min;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  min  the min to set
         */
        public void setMin(final Double min) {
            this.min = min;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the max
         */
        public Double getMax() {
            return max;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  max  the max to set
         */
        public void setMax(final Double max) {
            this.max = max;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  min  DOCUMENT ME!
         * @param  max  the max to set
         */
        public void setRange(final Double min, final Double max) {
            this.min = min;
            this.max = max;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the minEqualsAllowd
         */
        public boolean isMinEqualsAllowd() {
            return minEqualsAllowd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  minEqualsAllowd  the minEqualsAllowd to set
         */
        public void setMinEqualsAllowd(final boolean minEqualsAllowd) {
            this.minEqualsAllowd = minEqualsAllowd;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the maxEqualsAllowd
         */
        public boolean isMaxEqualsAllowd() {
            return maxEqualsAllowd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  maxEqualsAllowd  the maxEqualsAllowd to set
         */
        public void setMaxEqualsAllowd(final boolean maxEqualsAllowd) {
            this.maxEqualsAllowd = maxEqualsAllowd;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class BooleanAsInteger extends DataType {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new BooleanAsInteger object.
         *
         * @param  notNull   DOCUMENT ME!
         * @param  editable  DOCUMENT ME!
         */
        public BooleanAsInteger(final boolean notNull, final boolean editable) {
            super(notNull, false, editable, null, null);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public ValidationResult isValidValue(final Object value) {
            ValidationResult result = super.isValidValue(value);

            if (result.getValidationResult() == Validation.OK) {
                if ((value instanceof String) || (value instanceof Number)) {
                    final String stringValue = value.toString();

                    try {
                        final int val = Integer.parseInt(stringValue);

                        if ((val != 1) && (val != 0)) {
                            result = new ValidationResult(
                                    Validation.NOT_BINARY);
                        }
                    } catch (NumberFormatException e) {
                        result = new ValidationResult(
                                Validation.NOT_BINARY);
                    }
                } else {
                    if (value != null) {
                        result = new ValidationResult(Validation.WRONG_DATA_TYPE);
                    }
                }
            }

            return result;
        }

        @Override
        public String toString() {
            return "Numeric(1,0)";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class Varchar extends DataType {

        //~ Instance fields ----------------------------------------------------

        private final int maxLength;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Varchar object.
         *
         * @param  maxLength  DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         */
        public Varchar(final int maxLength, final boolean notNull) {
            super(notNull, false, true, null, null);
            this.maxLength = maxLength;
        }

        /**
         * Creates a new Varchar object.
         *
         * @param  maxLength  DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         * @param  editable   DOCUMENT ME!
         */
        public Varchar(final int maxLength, final boolean notNull, final boolean editable) {
            super(notNull, false, editable, null, null);
            this.maxLength = maxLength;
        }

        /**
         * Creates a new Varchar object.
         *
         * @param  maxLength  DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         * @param  unique     DOCUMENT ME!
         * @param  field      DOCUMENT ME!
         * @param  table      DOCUMENT ME!
         */
        public Varchar(final int maxLength,
                final boolean notNull,
                final boolean unique,
                final String field,
                final String table) {
            super(notNull, unique, true, field, table);
            this.maxLength = maxLength;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getMaxLength() {
            return maxLength;
        }

        @Override
        public ValidationResult isValidValue(final Object value) {
            ValidationResult result = super.isValidValue(value);

            if (result.getValidationResult() == Validation.OK) {
                if (value instanceof String) {
                    final String stringValue = (String)value;

                    if (stringValue.length() > maxLength) {
                        result = new ValidationResult(
                                Validation.SIZE_CORRECTION,
                                true,
                                stringValue.substring(0, maxLength));
                    }
                } else {
                    if (value != null) {
                        result = new ValidationResult(Validation.WRONG_DATA_TYPE);
                    }
                }
            }

            return result;
        }

        @Override
        public String toString() {
            return "Varchar(" + maxLength + ")";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class Link extends Varchar {

        //~ Instance fields ----------------------------------------------------

        private boolean rightAlignment = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Varchar object.
         *
         * @param  maxLength  DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         */
        public Link(final int maxLength, final boolean notNull) {
            super(maxLength, notNull);
        }

        /**
         * Creates a new Varchar object.
         *
         * @param  maxLength  DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         * @param  editable   DOCUMENT ME!
         */
        public Link(final int maxLength, final boolean notNull, final boolean editable) {
            super(maxLength, notNull, editable);
        }

        /**
         * Creates a new Varchar object.
         *
         * @param  maxLength       DOCUMENT ME!
         * @param  notNull         DOCUMENT ME!
         * @param  editable        DOCUMENT ME!
         * @param  rightAlignment  DOCUMENT ME!
         */
        public Link(final int maxLength, final boolean notNull, final boolean editable, final boolean rightAlignment) {
            this(maxLength, notNull, editable);
            this.rightAlignment = rightAlignment;
        }

        /**
         * Creates a new Varchar object.
         *
         * @param  maxLength  DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         * @param  unique     DOCUMENT ME!
         * @param  field      DOCUMENT ME!
         * @param  table      DOCUMENT ME!
         */
        public Link(final int maxLength,
                final boolean notNull,
                final boolean unique,
                final String field,
                final String table) {
            super(maxLength, notNull, unique, field, table);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the rightAlignment
         */
        public boolean isRightAlignment() {
            return rightAlignment;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rightAlignment  the rightAlignment to set
         */
        public void setRightAlignment(final boolean rightAlignment) {
            this.rightAlignment = rightAlignment;
        }

        @Override
        public ValidationResult isValidValue(final Object value) {
            final ValidationResult result = super.isValidValue(value);

            return result;
        }

        @Override
        public String toString() {
            return "Link(" + getMaxLength() + ")";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class WbblLink extends Link {

        //~ Instance fields ----------------------------------------------------

        private String wbblPath;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new WbblLink object.
         *
         * @param  wbblPath   DOCUMENT ME!
         * @param  maxLength  DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         */
        public WbblLink(final String wbblPath, final int maxLength, final boolean notNull) {
            super(maxLength, notNull);
            this.wbblPath = wbblPath;
            setRightAlignment(true);
        }

        /**
         * Creates a new WbblLink object.
         *
         * @param  wbblPath   DOCUMENT ME!
         * @param  maxLength  DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         * @param  editable   DOCUMENT ME!
         */
        public WbblLink(final String wbblPath, final int maxLength, final boolean notNull, final boolean editable) {
            super(maxLength, notNull, editable, true);
            this.wbblPath = wbblPath;
        }

        /**
         * Creates a new WbblLink object.
         *
         * @param  wbblPath   DOCUMENT ME!
         * @param  maxLength  DOCUMENT ME!
         * @param  notNull    DOCUMENT ME!
         * @param  unique     DOCUMENT ME!
         * @param  field      DOCUMENT ME!
         * @param  table      DOCUMENT ME!
         */
        public WbblLink(final String wbblPath,
                final int maxLength,
                final boolean notNull,
                final boolean unique,
                final String field,
                final String table) {
            super(maxLength, notNull, unique, field, table);
            this.wbblPath = wbblPath;
            setRightAlignment(true);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public ValidationResult isValidValue(final Object value) {
            ValidationResult result = super.isValidValue(value);

            if ((value != null) && !value.equals("") && (result.getValidationResult() == Validation.OK)) {
                if (!checkDocumentExistenceOnWebDav(wbblPath, addExtension(value.toString(), "pdf"))) {
                    result = new ValidationResult(Validation.WBBL_NOT_ACCESSIBLE);
                }
            }

            return result;
        }
    }
    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class Geom extends DataType {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Geom object.
         *
         * @param  notNull   DOCUMENT ME!
         * @param  editable  DOCUMENT ME!
         */
        public Geom(final boolean notNull, final boolean editable) {
            super(notNull, false, editable, null, null);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public ValidationResult isValidValue(final Object value) {
            return super.isValidValue(value);
        }

        @Override
        public String toString() {
            return "Geometry";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class DateTime extends DataType {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Time object.
         *
         * @param  notNull   DOCUMENT ME!
         * @param  editable  DOCUMENT ME!
         */
        public DateTime(final boolean notNull, final boolean editable) {
            super(notNull, false, editable, null, null);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public ValidationResult isValidValue(final Object value) {
            return super.isValidValue(value);
        }

        @Override
        public String toString() {
            return "Timestamp";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class Time extends Varchar {

        //~ Instance fields ----------------------------------------------------

        private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Time object.
         *
         * @param  notNull   DOCUMENT ME!
         * @param  editable  DOCUMENT ME!
         */
        public Time(final boolean notNull, final boolean editable) {
            super(8, notNull, editable);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public ValidationResult isValidValue(final Object value) {
            ValidationResult result = super.isValidValue(value);

            if ((value != null) && !value.equals("") && (result.getValidationResult() == Validation.OK)) {
                if (value instanceof String) {
                    try {
                        formatter.parse((String)value);
                    } catch (ParseException e) {
                        result = new ValidationResult(Validation.WRONG_DATA_TYPE);
                    }
                }
            }

            return result;
        }

        @Override
        public String toString() {
            return "Time (00:00:00...23:59:59)";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class DateType extends DataType {

        //~ Instance fields ----------------------------------------------------

        GregorianCalendar min = null;
        GregorianCalendar max = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Time object.
         *
         * @param  notNull   DOCUMENT ME!
         * @param  editable  DOCUMENT ME!
         */
        public DateType(final boolean notNull, final boolean editable) {
            super(notNull, false, editable, null, null);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public ValidationResult isValidValue(final Object value) {
            ValidationResult result = super.isValidValue(value);

            if (result.getValidationResult() == Validation.OK) {
                if (value instanceof java.sql.Date) {
                    final java.sql.Date date = (java.sql.Date)value;
                    final GregorianCalendar val = new GregorianCalendar(date.getYear() + 1900,
                            date.getMonth(),
                            date.getDate());

                    if (((min != null) && val.after(min)) || ((max != null) && val.after(max))) {
                        result = new ValidationResult(Validation.WRONG_RANGE);
                    }
                } else if (value != null) {
                    LOG.warn("Enexpected data type to check " + value.getClass().getName());
                }
            }

            return result;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  min  DOCUMENT ME!
         * @param  max  DOCUMENT ME!
         */
        public void setRange(final GregorianCalendar min, final GregorianCalendar max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String range(final String name) {
            if ((min != null) && (max != null)) {
                return name + " >= " + toDateString(min) + " und " + name + " <= " + toDateString(max);
            } else if (min != null) {
                return name + " >= " + toDateString(min);
            } else if (max != null) {
                return name + " <= " + toDateString(max);
            } else {
                return "";
            }
        }

        @Override
        public String toString() {
            return "Date";
        }

        /**
         * DOCUMENT ME!
         *
         * @param   cal  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String toDateString(final GregorianCalendar cal) {
            return cal.get(GregorianCalendar.DATE) + "." + (cal.get(GregorianCalendar.MONTH) + 1) + "."
                        + cal.get(GregorianCalendar.YEAR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class Catalogue extends DataType {

        //~ Instance fields ----------------------------------------------------

        private final String catalogueReference;
        private boolean rightAlignment = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Time object.
         *
         * @param  catalogueReference  DOCUMENT ME!
         * @param  notNull             DOCUMENT ME!
         * @param  editable            DOCUMENT ME!
         */
        public Catalogue(final String catalogueReference, final boolean notNull, final boolean editable) {
            super(notNull, false, editable, null, null);
            this.catalogueReference = catalogueReference;
        }

        /**
         * Creates a new Time object.
         *
         * @param  catalogueReference  DOCUMENT ME!
         * @param  notNull             DOCUMENT ME!
         * @param  editable            DOCUMENT ME!
         * @param  rightAlignment      DOCUMENT ME!
         */
        public Catalogue(final String catalogueReference,
                final boolean notNull,
                final boolean editable,
                final boolean rightAlignment) {
            this(catalogueReference, notNull, editable);
            this.rightAlignment = rightAlignment;
        }

        /**
         * Creates a new Catalogue object.
         *
         * @param  catalogueReference  DOCUMENT ME!
         * @param  notNull             DOCUMENT ME!
         * @param  unique              DOCUMENT ME!
         * @param  field               DOCUMENT ME!
         * @param  table               DOCUMENT ME!
         */
        public Catalogue(final String catalogueReference,
                final boolean notNull,
                final boolean unique,
                final String field,
                final String table) {
            super(notNull, unique, true, field, table);
            this.catalogueReference = catalogueReference;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the rightAlignment
         */
        public boolean isRightAlignment() {
            return rightAlignment;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  rightAlignment  the rightAlignment to set
         */
        public void setRightAlignment(final boolean rightAlignment) {
            this.rightAlignment = rightAlignment;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getCatalogueReference() {
            return catalogueReference;
        }

        @Override
        public ValidationResult isValidValue(final Object value) {
            return super.isValidValue(value);
        }

        @Override
        public String toString() {
            return "Katalog";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class ValidationResult {

        //~ Instance fields ----------------------------------------------------

        private final Validation validationResult;
        private final boolean valueChanged;
        private final Object changedValue;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ValidationResult object.
         *
         * @param  validationResult  DOCUMENT ME!
         */
        public ValidationResult(final Validation validationResult) {
            this(validationResult, false, null);
        }

        /**
         * Creates a new ValidationResult object.
         *
         * @param  validationResult  DOCUMENT ME!
         * @param  valueChanged      DOCUMENT ME!
         * @param  changedValue      DOCUMENT ME!
         */
        public ValidationResult(final Validation validationResult,
                final boolean valueChanged,
                final Object changedValue) {
            this.validationResult = validationResult;
            this.valueChanged = valueChanged;
            this.changedValue = changedValue;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the validationResult
         */
        public Validation getValidationResult() {
            return validationResult;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the valueChanged
         */
        public boolean isValueChanged() {
            return valueChanged;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the changedValue
         */
        public Object getChangedValue() {
            return changedValue;
        }
    }

    /**
     * This is only a filter for GU. Hinweis themes are not concerned
     *
     * @version  $Revision$, $Date$
     */
    protected static class WwGrAdminFilter implements CidsLayerFeatureFilter {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean accept(final CidsLayerFeature bean) {
            return (bean != null) && (bean.getProperty("praefix") != null);
        }
    }
}
