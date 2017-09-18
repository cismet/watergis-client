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

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRViewer;

import org.openide.util.NbBundle;

import de.cismet.cismap.commons.gui.attributetable.AttributeTable;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CustomJrViewer extends JRViewer {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CustomJrViewer object.
     *
     * @param  jrPrint  DOCUMENT ME!
     */
    public CustomJrViewer(final JasperPrint jrPrint) {
        super(jrPrint);
        btnReload.setVisible(false);
        btnSave.setToolTipText(NbBundle.getMessage(AttributeTable.class, "AttributeTable.butExport.toolTipText"));
        btnSave.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-export.png")));
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butExport.toolTipText"));
        btnPrint.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-print.png")));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void loadReport(final JasperPrint jrPrint) {
        super.loadReport(jrPrint);
        btnReload.setVisible(false);
    }
}
