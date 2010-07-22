package org.openshapa.views;

import com.usermetrix.jclient.Logger;
import com.usermetrix.jclient.UserMetrix;
import java.util.logging.Level;
import org.openshapa.OpenSHAPA;
import org.openshapa.models.db.DataColumn;
import org.openshapa.models.db.Database;
import org.openshapa.models.db.ExternalColumnListListener;
import org.openshapa.models.db.SystemErrorException;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.openshapa.models.db.LogicErrorException;
import org.openshapa.models.db.MacshapaDatabase;
import org.openshapa.views.discrete.SpreadsheetColumn;

/**
 * The dialog to list database variables.
 */
public final class ListVariables extends OpenSHAPADialog
implements ExternalColumnListListener, TableModelListener {

    /** The logger for this class. */
    private Logger logger = UserMetrix.getLogger(ListVariables.class);

    /** The column for database ID of column. This column is not visible. */
    private static final int IDCOLUMN = 0;

    /** The column for if a variable is visible or not. */
    private static final int VCOLUMN = 1;

    /** The column for a variables name. */
    private static final int NCOLUMN = 2;

    /** The column for a variables type. */
    private static final int TCOLUMN = 3;

    /** The column for a variables comment. */
    private static final int CCOLUMN = 4;

    /** The total number of columns in the variables list. */
    private static final int TOTAL_COLUMNS = 5;

    /** The database containing the variables you wish to list. */
    private Database database;

    /** The table model of the JTable that lists the actual variables. */
    private VListTableModel tableModel;

    /** Mapping between database column id - to the table model. */
    private HashMap<Long, Integer> dbToTableMap;

    private ResourceMap rMap = Application.getInstance(OpenSHAPA.class)
                                      .getContext()
                                      .getResourceMap(ListVariables.class);

    /**
     * Creates new form ListVariablesView.
     *
     * @param parent The parent frame for this dialog.
     * @param modal Is this dialog to be modal (true), or not.
     * @param db The database containing the variables you wish to list.
     */
    public ListVariables(final java.awt.Frame parent,
                         final boolean modal,
                         final Database db) {
        super(parent, modal);
            tableModel = new VListTableModel();
        dbToTableMap = new HashMap<Long, Integer>();
        initComponents();
        setName(this.getClass().getSimpleName());
        database = db;
        // Set the names of the columns.
        tableModel.addColumn(rMap.getString("Table.idColumn"));
        tableModel.addColumn(rMap.getString("Table.visibleColumn"));
        tableModel.addColumn(rMap.getString("Table.nameColumn"));
        tableModel.addColumn(rMap.getString("Table.typeColumn"));
        tableModel.addColumn(rMap.getString("Table.commentColumn"));
        //Use JTextfield to edit variable name cells
        variableList.getColumnModel().getColumn(NCOLUMN).setCellEditor(new DefaultCellEditor(new JTextField()));
        // Populate table with a variable listing from the database
        try {
            Vector<SpreadsheetColumn> ssColumns = OpenSHAPA.getView().getSpreadsheetPanel().getColumns();
            Vector<DataColumn> dbColumns = database.getDataColumns();
            if (ssColumns.size() != database.getColumns().size()) {
                for (int i = 0; i < dbColumns.size(); i++) {
                    DataColumn dbColumn = dbColumns.elementAt(i);
                    // TODO bug #21 Add comment field.
                    addRow(dbColumn, rMap);
                }
            } else {
                for (int i = 0; i < ssColumns.size(); i++) {
                    SpreadsheetColumn ssColumn = ssColumns.elementAt(i);
                    DataColumn dbColumn = getDataColumn(ssColumn.getColID());
                    if (dbColumn != null) {
                        // TODO bug #21 Add comment field.
                        addRow(dbColumn, rMap);
                    }
                }
            }
        } catch (SystemErrorException e) {
            logger.error("Unable to list variables.", e);
        }
        variableList.getColumnModel().removeColumn(variableList.getColumnModel().getColumn(IDCOLUMN));
        //Listeners
        tableModel.addTableModelListener(this);
    }

    
     /**
     * Returns DataColumn with the specific column name.
     * @param columnName name of column variable
     * @return DataColumn for column, null if not found.
     */
    private DataColumn getDataColumn(final String columnName) throws SystemErrorException {
        Vector<DataColumn> dataCol = database.getDataColumns();

        for (DataColumn dc : dataCol) {
            if (dc.getName().equalsIgnoreCase(columnName)) {
                return dc;
            }
        }

        return null;
    }

         /**
     * Returns DataColumn with the specific column name.
     * @param columnID name of column variable
     * @return DataColumn for column, null if not found.
     */
    private DataColumn getDataColumn(final long columnID) throws SystemErrorException {
        Vector<DataColumn> dataCol = database.getDataColumns();

        for (DataColumn dc : dataCol) {
            if (dc.getID() == columnID) {
                return dc;
            }
        }

        return null;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);

        long varID = (Long)model.getValueAt(row, IDCOLUMN);
        DataColumn dc = null;
        try {
            dc = database.getDataColumn(varID);
        } catch (SystemErrorException ex) {
            java.util.logging.Logger.getLogger(ListVariables.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (columnName.equals(rMap.getString("Table.visibleColumn"))) {
            try {
                dc.setHidden(!(Boolean)data);
                MacshapaDatabase msdb = OpenSHAPA.getProjectController().getDB();
                msdb.replaceColumn(dc);
            } catch (SystemErrorException ex) {
                java.util.logging.Logger.getLogger(ListVariables.class.getName()).log(Level.SEVERE, null, ex);
            }            
        } else if (columnName.equals(rMap.getString("Table.nameColumn"))) {
            try {
                if ((!dc.getName().equals((String)data)) && (dc.isValidColumnName(database, (String)data))) {
                    MacshapaDatabase msdb = OpenSHAPA.getProjectController().getDB();
                    dc.setName((String)data);
                    msdb.replaceColumn(dc);
                }
            } catch (LogicErrorException fe) {
                OpenSHAPA.getApplication().showWarningDialog(fe);
                tableModel.setValueAt(dc.getName(), row, column);
            } catch (SystemErrorException see) {
                OpenSHAPA.getApplication().showErrorDialog();
                tableModel.setValueAt(dc.getName(), row, column);
            }
        }
        OpenSHAPA.getView().showSpreadsheet();
    }


    /**
     * Add a new row to the variable list.
     *
     * @param dbColumn the DataColumn being added to the variable list.
     * @param rMap The resource map - used to find localised string bundles.
     */
    public void addRow(final DataColumn dbColumn, final ResourceMap rMap) {
        Object[] vals = new Object[TOTAL_COLUMNS];

        vals[IDCOLUMN] = dbColumn.getID();
        vals[VCOLUMN] = !dbColumn.getHidden();
        vals[NCOLUMN] = dbColumn.getName();
        switch (dbColumn.getItsMveType()) {
            case FLOAT:
                vals[TCOLUMN] = rMap.getString("VarType.float");
                break;
            case INTEGER:
                vals[TCOLUMN] = rMap.getString("VarType.integer");
                break;
            case TEXT:
                vals[TCOLUMN] = rMap.getString("VarType.text");
                break;
            case NOMINAL:
                vals[TCOLUMN] = rMap.getString("VarType.nominal");
                break;
            case PREDICATE:
                vals[TCOLUMN] = rMap.getString("VarType.predicate");
                break;
            case MATRIX:
                vals[TCOLUMN] = rMap.getString("VarType.matrix");
                break;
            default:
                vals[TCOLUMN] = rMap.getString("VarType.undefined");
                break;
        }

        // TODO bug #21 Add comment field.

        // Add new row to table model.
        int rowId = tableModel.getRowCount();
        tableModel.insertRow(rowId, vals);
        dbToTableMap.put(dbColumn.getID(), rowId);
    }

    /**
     * Action to invoke when a column is removed from a database.
     *
     * @param db The database that the column has been removed from.
     * @param colID The id of the freshley removed column.
     * @param old_cov The column order vector prior to the deletion.
     * @param new_cov The column order vector after to the deletion.
     */
    public void colDeletion(final Database db, 
                            final long colID,
                            final Vector<Long> old_cov,
                            final Vector<Long> new_cov) {
        int tableModelID = dbToTableMap.get(Long.valueOf(colID));
        tableModel.removeRow(tableModelID);
    }

    /**
     * Action to invoke when a column is added to a database.
     *
     * @param db The database that the column has been added to.
     * @param colID The id of the newly added column.
     * @param old_cov The column order vector prior to the insertion.
     * @param new_cov The column order vector after to the insertion.
     */
    public void colInsertion(final Database db, 
                             final long colID,
                             final Vector<Long> old_cov,
                             final Vector<Long> new_cov) {
        ResourceMap rMap = Application.getInstance(OpenSHAPA.class)
                                      .getContext()
                                      .getResourceMap(ListVariables.class);

        try {
            DataColumn dbColumn = db.getDataColumn(colID);
            addRow(dbColumn, rMap);

        } catch (SystemErrorException e) {
            logger.error("Unable to insert column into variable list", e);
        }
    }

    /**
     * Action to invoke when the column order list is edited (i.e, the order
     * of the columns is changed without any insertions or deletions).
     *
     * @param db The database that the column has been added to.
     * @param old_cov The column order vector prior to the insertion.
     * @param new_cov The column order vector after to the insertion.
     */
    public final void colOrderVectorEdited(final Database db,
                                           final Vector<Long> old_cov,
                                           final Vector<Long> new_cov) {
        // do nothing for now
        return;
    }

    class VListTableModel extends DefaultTableModel {

        @Override
        public Class getColumnClass(int column) {
            try {
                if (column == VCOLUMN) {
                    return Class.forName("java.lang.Boolean");
                }
                return Class.forName("java.lang.Object");
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == VCOLUMN || column == NCOLUMN) {
                return true;
            }
            return false;
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        variableList = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.openshapa.OpenSHAPA.class).getContext().getResourceMap(ListVariables.class);
        setTitle(resourceMap.getString("variableListDialog.title")); // NOI18N
        setName("variableListDialog"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        variableList.setModel(tableModel);
        variableList.setMinimumSize(new java.awt.Dimension(400, 200));
        variableList.setName("variableList");
        variableList.setAutoCreateRowSorter(true);
        jScrollPane1.setViewportView(variableList);

        MigLayout layout = new MigLayout("nogrid");
                //new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        getContentPane().add(jScrollPane1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable variableList;
    // End of variables declaration//GEN-END:variables

}
