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

import org.apache.log4j.Logger;

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.tools.CidsBeanFilter;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.Proxy;

import de.cismet.tools.PasswordEncrypter;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.downloadmanager.WebDavDownload;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WatergisDefaultRuleSet extends DefaultAttributeTableRuleSet {

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
        if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
            final String jobname = DownloadManagerDialog.getJobname();
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
     * @param   name       DOCUMENT ME!
     * @param   extension  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String addExtension(final String name, final String extension) {
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
    protected CidsBeanFilter createCidsBeanFilter(final String propertyName) {
        return new CidsBeanFilter() {

                @Override
                public boolean accept(final CidsBean bean) {
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
     * @param   propName   DOCUMENT ME!
     * @param   value      DOCUMENT ME!
     * @param   allowNull  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static boolean isNoInteger(final String propName, final Object value, final boolean allowNull) {
        if (!isNumberOrNull(value)) {
            return false;
        } else {
            final Number n = toNumber(value);

            if (n == null) {
                return allowNull;
            }

            if (n.doubleValue() != n.intValue()) {
                return true;
            } else {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Das Attribut "
                            + propName
                            + " darf keine Nachkommastellen enthalten");

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
                        || ((tmp != null) && tmp.toLowerCase().equals(element.toLowerCase()))) {
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
}
