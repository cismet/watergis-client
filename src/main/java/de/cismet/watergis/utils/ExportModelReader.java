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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportModelReader {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger LOG = Logger.getLogger(ExportModelReader.class);

    //~ Instance fields --------------------------------------------------------

    /** speichert den Namen der Konfigurationsdatei. */
    private String filename = "config/config.xml";
    private List<Export> exports = new ArrayList<Export>();

    //~ Constructors -----------------------------------------------------------

    /**
     * privater Konstruktor.
     *
     * @param  filename  DOCUMENT ME!
     */
    public ExportModelReader(final String filename) {
        readConfFile();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * fuellt die Membervariablen mit den Werten aus der Konfigurationsdatei.
     */
    private synchronized void readConfFile() {
        try {
            final SAXBuilder saxb = new SAXBuilder();
            final Document doc = saxb.build(getClass().getResourceAsStream(filename));
            final Element root = doc.getRootElement();

            final List entryList = root.getChildren("export");
            final Iterator it = entryList.iterator();

            while (it.hasNext()) {
                final Element element = (Element)it.next();
                final Export export = new Export(element);
                exports.add(export);
            }
        } catch (Exception e) {
            System.err.println("[CONFIG] Err while reading the configuration file: ");
            e.printStackTrace();
            System.exit(5);
        }
    }
}
