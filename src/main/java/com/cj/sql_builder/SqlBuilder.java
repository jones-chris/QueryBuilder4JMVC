package com.cj.sql_builder;

import com.cj.model.Column;
import com.cj.model.Join;
import com.cj.model.Table;
import com.cj.model.select_statement.Criterion;
import com.cj.model.select_statement.Operator;
import com.cj.model.select_statement.SelectStatement;
import com.cj.model.select_statement.parser.SubQueryParser;
import com.cj.model.select_statement.validator.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cj.sql_builder.SqlCleanser.escape;

/**
 * This class uses a SelectStatement to generate a SELECT SQL string.
 */
public abstract class SqlBuilder {

    /**
     * Contains the SQL string that this class' methods will create.  Each class method that is responsible for creating
     * a SQL clause should append it's SQL string to this field.
     */
    StringBuilder stringBuilder = new StringBuilder();

    /**
     * The character to begin wrapping the table and column in a SQL statement.  For example, PostgreSQL uses a double quote
     * to wrap the table and column in a SELECT SQL statement like so:  SELECT "employees"."name" FROM "employees".  MySQL
     * uses back ticks like so:  SELECT `employees`.`name` from `employees`.
     */
    protected char beginningDelimiter;

    /**
     * The character to end wrapping the table and column in a SQL statement.  For example, PostgreSQL uses a double quote
     * to wrap the table and column in a SELECT SQL statement like so:  SELECT "employees"."name" FROM "employees".  MySQL
     * uses back ticks like so:  SELECT `employees`.`name` from `employees`.
     */
    protected char endingDelimiter;

    /**
     * The SelectStatement that encapsulates the data to generate the SELECT SQL string.
     */
    protected SelectStatement selectStatement;

    /**
     * The class responsible for parsing subqueries.
     */
    protected SubQueryParser subQueryParser;


    public SqlBuilder(SelectStatement selectStatement) throws Exception {
        this.selectStatement = selectStatement;
        this.subQueryParser = new SubQueryParser(this.selectStatement);
    }

    public abstract String buildSql() throws Exception;

    /**
     * Creates the SELECT clause of a SELECT SQL statement.
     *
     * @param distinct Whether the generated SELECT SQL should have a DISTINCT clause.
     * @param columns A list of columns to generate the SELECT SQL statement.
     */
    protected void createSelectClause(boolean distinct, List<Column> columns) {
        String startSql = (distinct) ? "SELECT DISTINCT " : "SELECT ";

        StringBuilder sb = new StringBuilder(startSql);

        // Get each column's SQL String representation.
        List<String> columnsSqlStrings = new ArrayList<>();
        columns.forEach(column -> {
            String columnSql = column.toSql(this.beginningDelimiter, this.endingDelimiter);
            columnsSqlStrings.add(columnSql);
        });

        // Join the column SQL strings with a ", " between each SQL string.
        String joinedColumnsSqlStrings = String.join(", ", columnsSqlStrings);

         sb.append(joinedColumnsSqlStrings);

         this.stringBuilder.append(sb);
    }

    /**
     * Creates the FROM clause of a SELECT SQL statement.
     *
     * @param table The table name.
     */
    protected void createFromClause(Table table) {
        StringBuilder sb = new StringBuilder(" FROM ");
        String tableSqlString = table.toSql(this.beginningDelimiter, this.endingDelimiter);
        sb.append(tableSqlString);
        this.stringBuilder.append(sb);
    }

    /**
     * Creates the JOIN clause of a SELECT SQL statement.
     *
     * @param joins A list of Join.
     */
    protected void createJoinClause(List<Join> joins) {
        StringBuilder sb = new StringBuilder();

        // Get each join's SQL string representation.
        List<String> joinSqlStrings = new ArrayList<>();
        joins.forEach(join -> {
            String joinSqlString = join.toSql(beginningDelimiter, endingDelimiter);
            joinSqlStrings.add(joinSqlString);
        });

        // Join the join SQL strings with a " " between each SQL string.
        String joinedSqlStrings = String.join(" ", joinSqlStrings);

        sb.append(joinedSqlStrings);

        this.stringBuilder.append(sb);
    }

    /**
     * Creates the WHERE clause of a SQL CRUD statement.
     *
     * @param criteria A list of Criteria.
     */
    protected void createWhereClause(List<Criterion> criteria) throws Exception {
        StringBuilder sb = new StringBuilder();

        if (! criteria.isEmpty()) {
            sb.append(" WHERE ");

            for (Criterion criterion : criteria) {
                // The criteria's filter should be the subquery id that can be retrieved from builtSubQueries.
                String[] filterItems = criterion.getFilter().split(",");
                String[] newFilterItems = filterItems.clone();
                for (int i=0; i<filterItems.length; i++) {
                    String filterItem = filterItems[i];

                    if (SubQueryParser.argIsSubQuery(filterItem)) {
                        String subquery = subQueryParser.getBuiltSubQueries().get(filterItem);

                        if (subquery == null) {
                            throw new RuntimeException("Could not find subquery with name:  " + filterItem);
                        }

                        newFilterItems[i] = "(" + subquery + ")";
                    } else {
                        filterItem = escape(filterItem);

                        Map<String, Map<String, Integer>> tableColumnTypes = this.selectStatement.getDatabaseMetaData()
                                .getTablesMetaData()
                                .getTableColumnsTypes();

                        boolean shouldHaveQuotes = Validator.isColumnQuoted(table, column, tableColumnTypes); //todo:  make this a method in a Util class?
                        if (shouldHaveQuotes) {
                            filterItem = "'" + escape(filterItem) + "'";
                        }

                        newFilterItems[i] = filterItem;
                    }
                }
                criterion.setFilter(String.join(",", newFilterItems));

                // If the filter is 1) IN or NOT IN and 2) the first char is "(" or the last char is ")", then wrap in
                // parenthesises.
                if ((criteriaClone.operator.equals(Operator.in) || criteriaClone.operator.equals(Operator.notIn)) &&
                        (criteriaClone.filter.charAt(0) != '(' || criteriaClone.filter.charAt(criteriaClone.filter.length()-1) != ')')) {
                    criteriaClone.filter = "(" + criteriaClone.filter + ")";
                }

                String criteriaSql = criteriaClone.toSql(beginningDelimiter, endingDelimiter);
                sb.append(criteriaSql).append(" ");
            }
        }

        this.stringBuilder.append(sb);
    }

    /**
     * Creates the GROUP BY clause of a SELECT SQL statement.
     *
     * @param columns A list of columns.
     */
    @SuppressWarnings("DuplicatedCode")
    protected void createGroupByClause(List<Column> columns) {
        StringBuilder sb = new StringBuilder(" GROUP BY ");

        // Get each column's SQL string representation.
        List<String> columnsSqlStrings = new ArrayList<>();
        columns.forEach(column -> {
            String columnSqlString = column.toSql(beginningDelimiter, endingDelimiter, false);
            columnsSqlStrings.add(columnSqlString);
        });

        // Join the column SQL strings with a ", " between each SQL string.
        String joinedColumnSqlStrings = String.join(", ", columnsSqlStrings);

        sb.append(joinedColumnSqlStrings);

        this.stringBuilder.append(sb);
    }

    /**
     * Creates the ORDER BY clause of a SELECT SQL statement.
     *
     * @param columns A list of columns.
     * @param ascending Whether the generated SQL ORDER BY clause should be ascending or not.
     */
    @SuppressWarnings("DuplicatedCode")
    protected StringBuilder createOrderByClause(List<Column> columns, boolean ascending) {
        StringBuilder sb = new StringBuilder(" ORDER BY ");

        // Get each column's SQL string representation.
        List<String> columnsSqlStrings = new ArrayList<>();
        columns.forEach(column -> {
            String columnSqlString = column.toSql(beginningDelimiter, endingDelimiter, false);
            columnsSqlStrings.add(columnSqlString);
        });

        // Join the column SQL strings with a ", " between each SQL string.
        String joinedColumnSqlStrings = String.join(", ", columnsSqlStrings);

        sb.append(joinedColumnSqlStrings);

        // Append " ASC " or " DESC " depending on the value of the `ascending` parameter.
        if (ascending) {
            sb.append(" ASC ");
        } else {
            sb.append(" DESC ");
        }

        this.stringBuilder.append(sb);
    }

    /**
     * Creates the LIMIT clause of a SELECT SQL statement.
     *
     * @param limit The limit.
     */
    protected void createLimitClause(Long limit) {
        if (limit != null) {
            this.stringBuilder.append(String.format(" LIMIT %s ", limit));
        }
    }

    /**
     * Creates the OFFSET clause of a SELECT SQL statement.
     *
     * @param offset The offset.
     */
    protected void createOffsetClause(Long offset) {
        if (offset != null) {
            this.stringBuilder.append(String.format(" OFFSET %s ", offset));
        }
    }

}
