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
package de.cismet.watergis.gui.panels;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.Container;

import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.LinearReferencingInfo;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanel;

import de.cismet.cismap.linearreferencing.tools.StationTableCellEditorInterface;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DocumentCellEditor extends AbstractCellEditor implements TableCellEditor, ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger LOG = Logger.getLogger(DocumentCellEditor.class);

    //~ Instance fields --------------------------------------------------------

    private String columnName;
    private DocumentPanel pan;

    private final ConnectionContext connectionContext;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StationTableCellEditor object. If this constructor is used, the method
     * {@link #setColumnName(java.lang.String) } must be invoked to set the column name, this editor is used on
     */
    public DocumentCellEditor() {
        this(null, ConnectionContext.createDeprecated());
    }

    /**
     * Creates a new StationTableCellEditor object.
     *
     * @param  columnName  DOCUMENT ME!
     */
    public DocumentCellEditor(final String columnName) {
        this(columnName, ConnectionContext.createDeprecated());
    }

    /**
     * Creates a new StationTableCellEditor object.
     *
     * @param  columnName         DOCUMENT ME!
     * @param  connectionContext  DOCUMENT ME!
     */
    public DocumentCellEditor(final String columnName, final ConnectionContext connectionContext) {
        this.columnName = columnName;
        this.connectionContext = connectionContext;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getTableCellEditorComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column) {
        Container o = table;

        while ((o.getParent() != null)
                    && (!(o.getParent() instanceof AttributeTable) && !(o.getParent() instanceof FeatureInfoPanel))) {
            o = o.getParent();
        }

        if (o.getParent() instanceof AttributeTable) {
            final AttributeTable tab = (AttributeTable)o.getParent();
            final FeatureServiceFeature feature = tab.getFeatureByRow(row);

            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                pan = new DocumentPanel();
            } else if (feature instanceof JDBCFeature) {
                final JDBCFeature f = (JDBCFeature)feature;

                pan = new DocumentPanel();
            }
        } else if (o.getParent() instanceof FeatureInfoPanel) {
            final FeatureInfoPanel infoPanel = (FeatureInfoPanel)o.getParent();
            final FeatureServiceFeature feature = infoPanel.getSelectedFeature();

            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                pan = new DocumentPanel();
            }
        }

        pan.requestFocus();
        return pan;
    }

    @Override
    public Object getCellEditorValue() {
        return pan.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}
