package com.cj.model.select_statement;

import com.cj.model.Column;
import com.cj.sql_builder.SqlCleanser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;

public class Criterion {

    private Criterion parentCriterion;
    private Parenthesis openingParenthesis;
    private Conjunction conjunction;
    private Column column;
    private Operator operator;
    private String filter;
    private Parenthesis closingParenthesis;
    private List<Criterion> childCriteria = new ArrayList<>();

    public Criterion(Criterion parentCriterion, Conjunction conjunction, Column column, Operator operator, String filter,
                     List<Criterion> childCriteria) {
        this.parentCriterion = parentCriterion;
        this.conjunction = conjunction;
        this.column = column;
        this.operator = operator;
        this.filter = filter;
        this.childCriteria = childCriteria;
    }

    public Criterion getParentCriterion() {
        return parentCriterion;
    }

    public Parenthesis getOpeningParenthesis() {
        return ofNullable(this.openingParenthesis).orElse(Parenthesis.Empty);
    }

    public Conjunction getConjunction() {
        return ofNullable(this.conjunction).orElse(Conjunction.Empty);
    }

    public void setConjunction(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    public Column getColumn() {
        return column;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getFilter() {
        return ofNullable(this.filter).orElse("");
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Parenthesis getClosingParenthesis() {
        return ofNullable(this.closingParenthesis).orElse(Parenthesis.Empty);
    }

    public List<Criterion> getChildCriteria() {
        return childCriteria;
    }

    public void setChildCriteria(List<Criterion> childCriteria) {
        this.childCriteria = childCriteria;
    }

    public boolean isRoot() {
        return this.parentCriterion == null;
    }

    public List<String> getFilterItems() {
        return Arrays.asList(this.filter.split(","));
    }

    public boolean filterIsEmpty() {
        return this.getFilter().equals("");
    }

    @Override
    public String toString() throws IllegalArgumentException {
        String s = "";
        try {
            s = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ignored) {}

        return s;
    }

    /**
     * Returns the SQL string representation of the criterion in this format, if the schema were not null:
     * [AND/OR] [FRONT PARENTHESIS] `schema_name`.`table_name`.`column_name` [OPERATOR] filter [END PARENTHESIS]
     *
     * If the schema were null, the SQL string would be:
     * [AND/OR] [FRONT PARENTHESIS] `table_name`.`column_name` [OPERATOR] filter [END PARENTHESIS]
     *
     * @param beginningDelimiter The beginning delimiter based on the SQL dialect.
     * @param endingDelimiter The ending delimiter based on the SQL dialect.
     * @return The SQL string representation of the criterion.
     */
    public String toSql(char beginningDelimiter, char endingDelimiter, CriteriaSqlHolder criteriaSqlHolder) throws IllegalArgumentException {
        // Traverse the childCriteria field and make openingParenthesis and closingParenthesis non-empty if childCriteria
        // are not empty.
        List<String> criterionSqlStrings = new ArrayList<>();
        if (! this.childCriteria.isEmpty()) {
            this.openingParenthesis = Parenthesis.FrontParenthesis;
            this.closingParenthesis = Parenthesis.EndParenthesis;

            for (Criterion childCriterion : this.childCriteria) {
                String sql =
            }
        }

        String schema = ofNullable(column.getSchemaName())
                .map(SqlCleanser::escape)
                .orElse(null);

        String table = ofNullable(column.getTableName())
                .map(SqlCleanser::escape)
                .orElseThrow(IllegalArgumentException::new);

        String columnName = ofNullable(column.getColumnName())
                .map(SqlCleanser::escape)
                .orElseThrow(IllegalArgumentException::new);

        if (schema == null) {
            return String.format(" %s %s%s%s%s.%s%s%s %s %s%s ",
                    this.getConjunction(),
                    this.getOpeningParenthesis(),
                    beginningDelimiter, table, endingDelimiter,
                    beginningDelimiter, columnName, endingDelimiter,
                    this.getOperator(),
                    this.getFilter(),
                    this.getClosingParenthesis());
        } else {
            return String.format(" %s %s%s%s%s.%s%s%s.%s%s%s %s %s%s ",
                    this.getConjunction(),
                    this.getOpeningParenthesis(),
                    beginningDelimiter, schema, endingDelimiter,
                    beginningDelimiter, table, endingDelimiter,
                    beginningDelimiter, columnName, endingDelimiter,
                    this.getOperator(),
                    this.getFilter(),
                    this.getClosingParenthesis());
        }
    }

    public boolean isValid() {
        // column and operator must always be non-null.
        if (column == null || operator == null) {
            return false;
        }

        // If operator is `isNotNull` or `isNull` and filter is null or an empty string, then criterion is not valid.
        if ((operator != Operator.isNotNull || operator != Operator.isNull) && (filter == null || filter.equals(""))) {
            return false;
        }

        return true;
    }

}
