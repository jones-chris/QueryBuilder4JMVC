package com.cj.model.select_statement.validator;

import com.cj.constants.Constants;
import com.cj.model.select_statement.Criterion;
import com.cj.model.select_statement.SelectStatement;
import com.cj.sql_builder.SqlCleanser;

import java.util.List;
import java.util.Map;

import static com.cj.sql_builder.SqlCleanser.sqlIsClean;

public class Validator {

    /**
     * Test if stmt passes basic validation.
     *
     * @param selectStatement The SelectStatement object to validate.
     * @return boolean
     * @throws IllegalArgumentException If the selectStatement does not pass validation.
     */
    public static boolean passesBasicValidation(SelectStatement selectStatement) throws Exception {
        // Validate columns.
        if (selectStatement.getColumns() == null) {
            throw new IllegalArgumentException("Columns cannot be null");
        }

        if (selectStatement.getColumns().isEmpty()) {
            throw new IllegalArgumentException("Columns is empty");
        }

        // Validate table.
        if (selectStatement.getTable() == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }

        // Validate joins.
        if (selectStatement.getJoins() == null) {
            throw new IllegalArgumentException("Joins cannot be null");
        }

        // Validate criteria.
        if (selectStatement.getCriteria() == null) {
            throw new Exception("The Criteria cannot be null");
        }

        for (Criterion criterion : selectStatement.getCriteria()) {
            boolean isValid = criterion.isValid();
            if (! isValid) {
                throw new IllegalArgumentException(String.format("A criterion is not valid:  %s", criterion));
            }
        }

        // todo:  test if stmt joins pass basic validation.

        return true;
    }

    public static boolean passesDatabaseValidation(SelectStatement selectStatement) throws Exception {
        statementTablesAndColumnsExist(selectStatement); // Will throw exception instead of false.
        criteriaAreValid(selectStatement); // Will throw exception instead of false.
        return true;
    }

    /**
     * Tests whether each column in the columns parameter has a table and column that can be found in the target database.
     * This method assumes that each column is in the format of [table.column].  After splitting the column on a period (.),
     * the method will throw an exception if the resulting array does not have exactly 2 elements (a table and a column).
     *
     * False is never actually returned - instead an exception will be thrown.  True will be returned if the criteria are valid.
     *
     * @return boolean
     */
    //todo:  add joins' tables and columns here.
    private static boolean statementTablesAndColumnsExist(SelectStatement selectStatement) throws Exception {
        // Create list of statement's SELECT columns and WHERE columns.
        List<String> columns = selectStatement.getAllFullyQualifiedColumnNames();

        for (String column : columns) {
            String[] tableAndColumn = column.split("\\.");

            // Check that the tableAndColumn variable has two elements.  The column format should be [table.column].
            if (tableAndColumn.length != 2) { throw new Exception("One of the columns in either the SelectStatement's columns or criteria fields is " +
                    "either is not in the correct 'table.column' format or a column's table name or column name could not be " +
                    "found in the database."); }

            // Now that we know that the tableAndColumn variable has 2 elements, test if the table and column can be found
            // in the database.
            String table = tableAndColumn[Constants.TABLE_INDEX];
            boolean tableIsLegit = getTableColumnsTypes(selectStatement).containsKey(table);
            if (! tableIsLegit) { throw new Exception("This table could not be found in the database:  " + table); }

            String tableColumn = tableAndColumn[Constants.COLUMN_INDEX];
            boolean columnIsLegit = getTableColumnsTypes(selectStatement).get(table).containsKey(tableColumn);
            if (! columnIsLegit) { throw new Exception("This column could not be found in the database table:  " + tableColumn); }
        }

        // Check that statement's table is legit.
        boolean tablesAreValid = getTableColumnsTypes(selectStatement).containsKey(selectStatement.getTable().getTableName());
        if (! tablesAreValid) { throw new Exception("This table could not be found in the database:  " + selectStatement.getTable()); }

        return true;
    }

    /**
     * Determines if the stmt's criteria are valid.  False is never actually returned - instead an exception will be thrown.
     * True will be returned if the criteria are valid.
     *
     * @return boolean
     * @throws Exception If the criteria is not valid or if the criteria is not clean SQL.
     */
    private static boolean criteriaAreValid(SelectStatement selectStatement) throws Exception {
        for (Criterion criterion : selectStatement.getCriteria()) {
            if (! criterion.isValid()) {
                throw new Exception("This criteria is not valid:  " + criterion);
            }

            if (! sqlIsClean(criterion)) {
                throw new Exception("This criterion failed to be clean SQL:  " + criterion);
            }

            // Now that we know that the criteria's operator is not 'isNull' or 'isNotNull', we can assume that the
            // criteria's filter is needed.  Therefore, we should check if the filter is null or an empty string.
            // If so, throw an exception.
            if (! criterion.filterIsEmpty()) {
                boolean shouldHaveQuotes = isColumnQuoted(
                        criterion.getColumn().getTableName(),
                        criterion.getColumn().getColumnName(),
                        getTableColumnsTypes(selectStatement)
                );

                if (! shouldHaveQuotes && ! criterion.filterIsEmpty()) {
                    criterion.getFilterItems().forEach(filterItem -> {
                        if (! SqlCleanser.canParseNonQuotedFilter(filterItem)) {
                            throw new RuntimeException("The criteria's filter is not an number type, but the column is a number type:  " + criterion);
                        }
                    });
                }
            }
        }

        return true;
    }

    /**
     *
     * First, gets the SQL JDBC Type for the table and column parameters.  Then, gets a boolean from the typeMappings
     * class field associated with the SQL JDBC Types parameter, which is an int.  The typeMappings field will return
     * true if the SQL JDBC Types parameter should be quoted in a WHERE SQL clause and false if it should not be quoted.
     *
     * For example, the VARCHAR Type will return true, because it should be wrapped in single quotes in a WHERE SQL condition.
     * On the other hand, the INTEGER Type will return false, because it should NOT be wrapped in single quotes in a WHERE SQL condition.
     *
     * @param table The table name.
     * @param columnName The column name.
     * @param tableSchemas A Map with the keys being table names and the values being the SQL type as an integer per java.sql.Types.
     * @return boolean
     * @throws Exception If the data type is not supported or if the column does not exist in the table.
     */
    public static boolean isColumnQuoted(String table, String columnName, Map<String, Map<String, Integer>> tableSchemas) throws Exception {
        Integer dataType = getColumnDataType(table, columnName, tableSchemas);

        Boolean isQuoted = Constants.TYPE_MAPPINGS.get(dataType); //todo:  make typeMappings a public static field in SelectStatementValidator so that it can be called?  Maybe even put it in Constants class because it's called by SelectStatementValidator and SqlBuilder?

        if (isQuoted == null) {
            throw new Exception(String.format("Data type, %s, is not recognized", dataType));
        }

        return isQuoted;
    }

    /**
     * Gets the SQL JDBC Type for the table and column parameters.
     *
     * @param table The table name.
     * @param columnName The column name.
     * @return int
     * @throws Exception If the column does not exist in the table.
     */
    // todo:  is this method needed anymore now that we have a static method by same name and isColumnQuoted is static also?
    private static int getColumnDataType(String table, String columnName, SelectStatement selectStatement) throws Exception {
        return getColumnDataType(table, columnName, getTableColumnsTypes(selectStatement));
    }

    /**
     * Gets the SQL JDBC Type for the table and column parameters.
     *
     * @param table The table name.
     * @param columnName The column name.
     * @param tableSchemas A Map with the keys being table names and the values being the SQL type as an integer per java.sql.Types.
     * @return int
     * @throws Exception If the column does not exist in the table.
     */
    public static int getColumnDataType(String table, String columnName, Map<String, Map<String, Integer>> tableSchemas) throws Exception {
        Integer dataType = tableSchemas.get(table).get(columnName); //todo:  pass tableSchemas as parameter into SqlBuilder from SelectStatementValidator?  Because SelectStatementValidator already got tableSchemas.

        if (dataType == null) {
            throw new Exception("Could not find column:  " + columnName);
        } else {
            return dataType;
        }
    }

    private static Map<String, Map<String, Integer>> getTableColumnsTypes(SelectStatement selectStatement) {
        return selectStatement.getDatabaseMetaData().getTablesMetaData().getTableColumnsTypes();
    }

}
