package com.cj.sql_builder;

import com.cj.cache.DatabaseMetadataCache;
import com.cj.model.Column;
import com.cj.model.Join;
import com.cj.model.Table;
import com.cj.model.select_statement.*;
import com.cj.model.select_statement.parser.SubQueryParser;
import com.cj.model.select_statement.validator.DatabaseMetadataCacheValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static com.cj.model.Join.JoinType.*;
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
     * The cache of the target data source(s) and query template data source, which is built from the Qb4jConfig.json file.
     */
    protected DatabaseMetadataCache databaseMetadataCache;

    /**
     * The class responsible for parsing sub queries.
     */
    protected SubQueryParser subQueryParser;

    protected DatabaseMetadataCacheValidator databaseMetadataCacheValidator;

    public SqlBuilder(SelectStatement selectStatement) throws Exception {
        this.selectStatement = selectStatement;
        this.subQueryParser = new SubQueryParser(this.selectStatement);


        // Prepare the SelectStatement.
        this.addExcludingJoinCriteria();

        if (this.selectStatement.isSuppressNulls()) {
            this.addSuppressNullsCriteria();
        }

        // If subQueries has not been set (if this is the case, it will have a 0 size), then set subQueries.
        // This is done because if this SelectStatement is a subquery, then it will already have subQueries and we
        // don't want to change them.
        if (! this.selectStatement.getSubQueries().isEmpty()) {
            this.interpolateSubQueries();
        }

        this.replaceParameters();
        this.quoteCriteriaFilterItems();
    }

    @Autowired
    public void setDatabaseMetadataCache(DatabaseMetadataCache databaseMetadataCache) {
        this.databaseMetadataCache = databaseMetadataCache;
    }

    @Autowired
    public void setDatabaseMetadataCacheValidator(DatabaseMetadataCacheValidator databaseMetadataCacheValidator) {
        this.databaseMetadataCacheValidator = databaseMetadataCacheValidator;
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
        CriteriaSqlStringHolder criteriaSqlStringHolder = new CriteriaSqlStringHolder();

        this.selectStatement.getCriteria().forEach(criterion -> {
            criterion.toSqlDeep(this.beginningDelimiter, this.endingDelimiter, criteriaSqlStringHolder);
        });

        this.stringBuilder.append(" WHERE ");
        String joinedCriteriaSqlStrings = String.join(" ", criteriaSqlStringHolder.getCriterionSqlStrings());
        this.stringBuilder.append(joinedCriteriaSqlStrings);
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
    protected void createOrderByClause(List<Column> columns, boolean ascending) {
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

    /**
     * Adds isNull criterion to criteria if any of the statement's joins are an 'excluding' join, such as LEFT_JOIN_EXCLUDING,
     * RIGHT_JOIN_EXCLUDING, or FULL_OUTER_JOIN_EXCLUDING.
     */
    private void addExcludingJoinCriteria() {
        this.selectStatement.getJoins().forEach(join -> {
            Join.JoinType joinType = join.getJoinType();
            if (joinType.equals(LEFT_EXCLUDING)) {
                this.addCriterionForExcludingJoin(join.getTargetJoinColumns());
            }
            else if (joinType.equals(RIGHT_EXCLUDING)) {
                this.addCriterionForExcludingJoin(join.getParentJoinColumns());
            }
            else if (joinType.equals(FULL_OUTER_EXCLUDING)) {
                List<Column> allJoinColumns = join.getParentJoinColumns().stream()
                        .collect(Collectors.toCollection(join::getTargetJoinColumns));

                this.addCriterionForExcludingJoin(allJoinColumns);
            }
        });
    }

    private void addCriterionForExcludingJoin(List<Column> columns) {
        // Create parent criterion.
        Column firstColumn = columns.get(0);
        Criterion parentCriterion = new Criterion(null, Conjunction.And, firstColumn, Operator.isNull, null, null);

        // Create child criteria, if there is more than one column.
        List<Criterion> childCriteria = new ArrayList<>();
        if (columns.size() > 1) {
            for (int i=1; i<columns.size(); i++) {
                Column column = columns.get(i);
                Criterion childCriterion = new Criterion(parentCriterion, Conjunction.Or, column, Operator.isNull, null, null);
                childCriteria.add(childCriterion);
            }

            parentCriterion.setChildCriteria(childCriteria);
        }

        // Add parent criterion to this class' criteria.
        this.selectStatement.getCriteria().add(parentCriterion);
    }

    /**
     * Add a criterion to the SelectStatement for each of the SelectStatement's columns so that a "suppress nulls" clause
     * is included in the SelectStatement's SQL string representation's WHERE clause.
     */
    private void addSuppressNullsCriteria() {
        // Create root criteria for first column.
        boolean addAndConjunction = ! this.selectStatement.getCriteria().isEmpty();
        Conjunction conjunction = (addAndConjunction) ? Conjunction.And : Conjunction.Empty;
        Column firstColumn = this.selectStatement.getColumns().get(0);
        Criterion parentCriterion = new Criterion(null, conjunction, firstColumn, Operator.isNotNull, null, null);

        // Create list of children criteria, which are all columns except for the first column.
        List<Criterion> childCriteria = Collections.emptyList();
        this.selectStatement.getColumns().forEach(column -> {
            Criterion childCriterion = new Criterion(parentCriterion, Conjunction.And, column, Operator.isNotNull, null, null);
        });

        // Add child criteria to parent criterion.
        parentCriterion.setChildCriteria(childCriteria);

        // Add parent criterion to SelectStatement's criteria.
        this.selectStatement.getCriteria().add(parentCriterion);
    }

    /**
     * Replaces sub queries in each criterion.  The criterion's filter should be the subquery id that can be retrieved
     * from this class' subQueryParser's builtSubQueries method.
     */
    private void interpolateSubQueries() {
        for (Criterion criterion : this.selectStatement.getCriteria()) {
            String filter = criterion.getFilter();
            String newFilter = filter;

            if (SubQueryParser.argIsSubQuery(filter)) {
                String subquery = this.subQueryParser.getBuiltSubQueries().get(filter);

                if (subquery == null) {
                    throw new RuntimeException("Could not find subquery with name:  " + filter);
                }

                newFilter = "(" + subquery + ")";
            }

            // Join newFilterItems with a "," and set the criterion's filter to the resulting string.
            criterion.setFilter(newFilter);
        }
    }

    /**
     * Checks that there is an equal number of parameters in the criteria (not the criteriaParameters field) and
     * criteriaArguments.  After doing so, it attempts to replace the parameters in the criteria (again, not the
     * criteriaParameters field) with the relevant value from criteriaArguments.
     *
     * @throws Exception if the parameter cannot be found as a key in criteriaArguments.
     */
    private void replaceParameters() throws Exception {
        // Now that we know there are equal number of parameters and arguments, try replacing the parameters with arguments.
        if (this.selectStatement.getCriteriaArguments().size() != 0) {
            for (Criterion criterion : this.selectStatement.getCriteria()) {

                String filter = criterion.getFilter();
                String[] splitFilters = filter.split(",");
                List<String> resultFilters = new ArrayList<>();

                for (String splitFilter : splitFilters) {
                    if (splitFilter.length() >= 1 && splitFilter.substring(0, 1).equals("@")) {
                        String paramName = splitFilter.substring(1);
                        String paramValue = this.selectStatement.getCriteriaArguments().get(paramName);
                        if (paramValue != null) {
                            resultFilters.add(paramValue);
                        } else {
                            String message = String.format("No criteria parameter was found with the name, %s", paramName);
                            throw new Exception(message);
                        }
                    }
                }

                if (resultFilters.size() != 0) {
                    String joinedResultFilters = String.join(",", resultFilters);
                    criterion.setFilter(joinedResultFilters);
                }
            }
        }
    }

    /**
     * Wrap each column's filter items (after splitting on ",") in quotes based on the column's data type.
     *
     * @throws Exception
     */
    private void quoteCriteriaFilterItems() throws Exception {
        for (Criterion criterion : this.selectStatement.getCriteria()) {
            String[] filterItems = criterion.getFilter().split(",");
            String[] newFilterItems = filterItems.clone();
            for (int i=0; i<filterItems.length; i++) {
                String filterItem = filterItems[i];

                filterItem = escape(filterItem);

                // Get the column's data type from the cache, because we don't trust the column's data type that the client
                // sent.
                int columnDataType = this.databaseMetadataCache.getColumnDataType(criterion.getColumn());
                boolean shouldHaveQuotes = this.databaseMetadataCacheValidator.isColumnQuoted(columnDataType);
                if (shouldHaveQuotes) {
                    filterItem = String.format("'%s'", filterItem);
                }

                newFilterItems[i] = filterItem;
            }

            criterion.setFilter(String.join(",", newFilterItems));
        }
    }

}
