package com.cj.sql_builder;

import com.cj.model.Column;
import com.cj.model.select_statement.Criterion;
import com.cj.model.select_statement.Operator;
import com.cj.model.select_statement.SelectStatement;
import com.cj.model.select_statement.parser.SubQueryParser;
import com.cj.model.select_statement.validator.Validator;

import java.util.List;
import java.util.Map;

import static com.cj.sql_builder.SqlCleanser.escape;

/**
 * This class uses a SelectStatement to generate a SELECT SQL string.
 */
public abstract class SqlBuilder {

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
    protected SelectStatement stmt;

    /**
     * The class responsible for parsing subqueries.
     */
    protected SubQueryParser subQueryParser;


    public SqlBuilder(SelectStatement stmt) throws Exception {
        this.stmt = stmt;
        this.subQueryParser = new SubQueryParser(this.stmt);
    }

    public abstract String buildSql() throws Exception;

    /**
     * Creates the SELECT clause of a SELECT SQL statement.
     *
     * @param distinct Whether the generated SELECT SQL should have a DISTINCT clause.
     * @param columns A list of columns to generate the SELECT SQL statement.
     * @return StringBuilder If a Column object raises an Exception.
     */
    protected StringBuilder createSelectClause(boolean distinct, List<Column> columns) throws Exception {
        String startSql = (distinct) ? "SELECT DISTINCT " : "SELECT ";
        StringBuilder sql = new StringBuilder(startSql);
        for (Column column : columns) {
            String columnSql = column.toSql(beginningDelimiter, endingDelimiter);

            sql.append(columnSql)
                    .append(", ");
        }

        return sql.delete(sql.length() - 2, sql.length()).append(" ");
    }

    /**
     * Creates the FROM clause of a SELECT SQL statement.
     *
     * @param table The table name.
     * @return StringBuilder
     */
    protected StringBuilder createFromClause(String table) {
        String s = String.format(" FROM %s%s%s ", beginningDelimiter, escape(table), endingDelimiter);
        return new StringBuilder(s);
    }

    /**
     * Creates the JOIN clause of a SELECT SQL statement.
     *
     * @param joins A list of Join.
     * @return StringBuilder
     * @throws RuntimeException If a Join has differing numbers of parent table columns and target table columns.
     */
    protected StringBuilder createJoinClause(List<Join> joins) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<joins.size(); i++) {
            Join join = joins.get(i);

            if (join.getParentJoinColumns().size() != join.getTargetJoinColumns().size()) {
                final String joinColumnsSizeDiffMessage = "The parent and target join columns have differing number of elements.";
                throw new RuntimeException(joinColumnsSizeDiffMessage);
            }

            sb.append(join.getJoinType().toString());
            sb.append(String.format(" %s%s%s ", beginningDelimiter, join.getTargetTable(), endingDelimiter));

            for (int j=0; j<join.getParentJoinColumns().size(); j++) {
                String conjunction = (j == 0) ? "ON" : "AND";
                String[] parentJoinTableAndColumn = join.getParentJoinColumns().get(j).split("\\.");
                String[] targetJoinTableAndColumn = join.getTargetJoinColumns().get(j).split("\\.");

                //Format string in the form of " [ON/AND] `table1`.`column1` = `table2`.`column2` ", assuming the database
                // type is MySql.
                sb.append(String.format(" %s %s%s%s.%s%s%s = %s%s%s.%s%s%s ",
                        conjunction,
                        beginningDelimiter, parentJoinTableAndColumn[0], endingDelimiter,
                        beginningDelimiter, parentJoinTableAndColumn[1], endingDelimiter,
                        beginningDelimiter, targetJoinTableAndColumn[0], endingDelimiter,
                        beginningDelimiter, targetJoinTableAndColumn[1], endingDelimiter));
            }

        }

        return sb;
    }

    /**
     * Creates the WHERE clause of a SQL CRUD statement.
     *
     * @param criteria A list of Criteria.
     * @return StringBuilder
     * @throws Exception If cloning the a Criteria raises an Exception.
     */
    protected StringBuilder createWhereClause(List<Criterion> criteria) throws Exception {
        StringBuilder sql = new StringBuilder();

        if (! criteria.isEmpty()) {
            sql.append(" WHERE ");

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

                        Map<String, Map<String, Integer>> tableColumnTypes = this.stmt.getDatabaseMetaData()
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
                sql.append(criteriaSql).append(" ");
            }
        }

        return sql;
    }

    /**
     * Creates the GROUP BY clause of a SELECT SQL statement.
     *
     * @param columns A list of columns.
     * @return StringBuilder
     * @throws Exception If a Column object raises an Excepton.
     */
    protected StringBuilder createGroupByClause(List<Column> columns) throws Exception {
        StringBuilder sql = new StringBuilder(" GROUP BY ");

        for (Column column : columns) {
            sql.append(column.toSql(beginningDelimiter, endingDelimiter))
                    .append(", ");
        }

        return sql.delete(sql.length() - 2, sql.length()).append(" ");
    }

    /**
     * Creates the ORDER BY clause of a SELECT SQL statement.
     *
     * @param columns A list of columns.
     * @param ascending Whether the generated SQL ORDER BY clause should be ascending or not.
     * @return StringBuilder
     * @throws Exception If a Column object raises an Exception.
     */
    protected StringBuilder createOrderByClause(List<Column> columns, boolean ascending) throws Exception {
        StringBuilder sql = new StringBuilder(" ORDER BY ");

        for (Column column : columns) {
            sql.append(column.toSql(beginningDelimiter, endingDelimiter))
                    .append(", ");
        }

        sql.delete(sql.length() - 2, sql.length()).append(" ");
        return (ascending) ? sql.append(" ASC ") : sql.append(" DESC ");
    }

    /**
     * Creates the LIMIT clause of a SELECT SQL statement.
     *
     * @param limit The limit.
     * @return StringBuilder
     */
    protected StringBuilder createLimitClause(Long limit) throws IllegalArgumentException {
        if (limit == null) {
            return new StringBuilder();
        }

        return new StringBuilder(String.format(" LIMIT %s ", limit));
    }

    /**
     * Creates the OFFSET clause of a SELECT SQL statement.
     *
     * @param offset The offset.
     * @return StringBuilder
     */
    protected StringBuilder createOffsetClause(Long offset) throws IllegalArgumentException {
        if (offset == null) {
            return new StringBuilder();
        }

        return new StringBuilder(String.format(" OFFSET %s ", offset));
    }

    /**
     * Creates a WHERE clause condition that all columns in the columns parameter cannot be null.  This condition
     * should is used to not return records where all selected columns have a null value.
     *
     * @param columns A list of columns.
     * @return StringBuilder
     * @throws Exception If a Column object raises an Exception.
     */
    protected StringBuilder createSuppressNullsClause(List<Column> columns) throws Exception {
        StringBuilder sql = new StringBuilder();

        for (int i=0; i<columns.size(); i++) {
            Column column = columns.get(i);
            if (i == 0) {
                sql.append("(")
                        .append(column.toSql(beginningDelimiter, endingDelimiter))
                        .append(" IS NOT NULL ");
            } else {
                sql.append(" OR ")
                        .append(column.toSql(beginningDelimiter, endingDelimiter))
                        .append(" IS NOT NULL ");
            }
        }

        return sql.append(") ");
    }

}
