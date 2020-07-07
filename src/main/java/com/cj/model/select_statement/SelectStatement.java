package com.cj.model.select_statement;

import com.cj.model.Column;
import com.cj.model.Table;
import com.cj.model.select_statement.parser.SubQueryParser;
import com.cj.model.select_statement.validator.Validator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.internal.util.Join;

import javax.validation.Valid;
import java.util.*;

public class SelectStatement {

    private String name = "";
    private List<Column> columns = new ArrayList<>();
    private Table table;
    private List<Criterion> criteria = new ArrayList<>();
    private List<Join> joins = new ArrayList<>();
    private boolean distinct;
    private boolean groupBy;
    private boolean orderBy;
    private Long limit = null;
    private boolean ascending;
    private Long offset = null;
    private boolean suppressNulls;
    private Map<String, String> subQueries = new HashMap<>();

    /**
     * The query's criteria runtime arguments.  The key is the name of the parameter to find in the query criteria.  The
     * value is what will be passed into the query criteria.
     */
    private Map<String, String> criteriaArguments = new HashMap<>();

    /**
     * The query's criteria parameters.  The key is the name of the parameter to find in the query criteria.  The value is
     * a description of the parameter that can be referenced by developers or in the application UI.
     */
    private List<CriterionParameter> criteriaParameters = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<Criterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<Criterion> criteria) {
        this.criteria = criteria;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isGroupBy() {
        return groupBy;
    }

    public void setGroupBy(boolean groupBy) {
        this.groupBy = groupBy;
    }

    public boolean isOrderBy() {
        return orderBy;
    }

    public void setOrderBy(boolean orderBy) {
        this.orderBy = orderBy;
    }

    public Long getLimit() {
        return limit;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public Long getOffset() {
        return offset;
    }

    public boolean isSuppressNulls() {
        return suppressNulls;
    }

    public void setSuppressNulls(boolean suppressNulls) {
        this.suppressNulls = suppressNulls;
    }

    public Map<String, String> getSubQueries() {
        return subQueries;
    }

    /**
     * One of two setters for subQueries field.  It's not recommended that developers use this because the parameter-less
     * setter will set subQueries automatically if subQuery calls are hand-written into the a criterion's filter.
     * @param subQueries A Map of subqueries.
     */
    public void setSubQueries(Map<String, String> subQueries) {
        this.subQueries = subQueries;
    }

    /**
     * Automatically sets the subQueries field assuming that the subQuery calls are hand-written into a criterion's filter.
     * If you want to set the subQueries field manually, use the public setSubQueries method.
     */
    //todo:  move to the SubQueryParser class or put in a new class?
    void setSubqueries() throws IllegalArgumentException {
        if (! criteria.isEmpty()) {
            criteria.forEach((criterion) -> {
                if (SubQueryParser.argIsSubQuery(criterion.getFilter())) {
                    LinkedList<Integer> begSubQueryIndeces = new LinkedList<>();
                    LinkedList<Integer> endSubQueryIndeces = new LinkedList<>();
                    char[] filterChars = criterion.getFilter().toCharArray();

                    for (int i=0; i<filterChars.length; i++) {
                        if (filterChars[i] == '$') {
                            begSubQueryIndeces.add(i);
                        } else if (filterChars[i] == ')') {
                            endSubQueryIndeces.add(i);
                        }
                    }

                    // Check that there are equal number of beginning and ending subQuery indeces - otherwise we have
                    // a malformed subQuery call.
                    if (begSubQueryIndeces.size() == endSubQueryIndeces.size()) {
                        // It's okay to make the while condition based on only one of the LinkedLists because we know at this
                        // point that both LinkedLists are equal sizes.
                        String newFilter = new String(criterion.getFilter());
                        while (begSubQueryIndeces.size() != 0) {
                            String subQueryId = "$" + subQueries.size();
                            int begSubQueryIndex = begSubQueryIndeces.removeLast();
                            int endSubQueryIndex = 1000;
                            // Find ending index that is greater than beginning index, but closest to ending index.
                            for (Integer endIndex : endSubQueryIndeces) {
                                if (endIndex > begSubQueryIndex) {
                                    if ((endIndex - begSubQueryIndex) < (endSubQueryIndex - begSubQueryIndex)) {
                                        endSubQueryIndex = endIndex;
                                    }
                                }
                            }
                            endSubQueryIndeces.remove(new Integer(endSubQueryIndex));

                            // Now, get the subQueryCall from filter (which does not change)
                            String subQueryCall = newFilter.substring(begSubQueryIndex + 1, endSubQueryIndex + 1);

                            // Now, look in newFilter (which changes) and replace that subQueryCall with the subQueryId
                            newFilter = newFilter.replace("$" + subQueryCall, subQueryId);

                            // Now, add the subQueryId and subQueryCall to subQueries.
                            subQueries.put(subQueryId, subQueryCall);

                            for (int i=0; i<begSubQueryIndeces.size(); i++) {
                                int begElement = begSubQueryIndeces.get(i);
                                if (begElement > begSubQueryIndex) {
                                    int newElement = begElement - (subQueryCall.length());
                                    begSubQueryIndeces.set(i, newElement);
                                }

                                int endElement = endSubQueryIndeces.get(i);
                                if (endElement > endSubQueryIndex) {
                                    int newElement = endElement - (subQueryCall.length()-1);
                                    endSubQueryIndeces.set(i, newElement);
                                }
                            }
                        }
                        criterion.setFilter(newFilter);
                    } else {
                        throw new IllegalArgumentException("SubQuery is malformed");
                    }
                }
            });
        }
    }

    public Map<String, String> getCriteriaArguments() {
        return criteriaArguments;
    }

    public void setCriteriaArguments(Map<String, String> criteriaArguments) {
        this.criteriaArguments = criteriaArguments;
    }

    public List<CriterionParameter> getCriteriaParameters() {
        return criteriaParameters;
    }

    public void setCriteriaParameters(List<CriterionParameter> criteriaParameters) {
        this.criteriaParameters = criteriaParameters;
    }

    public String toSql(Properties properties) {
        try {
            addExcludingJoinCriteria();

            // If subQueries has not been set (if this is the case, it will have a 0 size), then set subQueries.
            // This is done because if this SelectStatement is a subquery, then it will already have subQueries and we
            // don't want to change them.
            if (! subQueries.isEmpty()) {
                setSubqueries();
            }

            replaceParameters();

            Validator.passesBasicValidation(this);

            Validator.passesDatabaseValidation();

            SqlBuilder sqlBuilder = SqlBuilderFactory.buildSqlBuilder(this); // subQueries get built here.
            return sqlBuilder.buildSql(); // root query gets built here.
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ignored) {}
    }

    /**
     * Checks that there is an equal number of parameters in the criteria (not the criteriaParameters field) and
     * criteriaArguments.  After doing so, it attempts to replace the parameters in the criteria (again, not the
     * criteriaParameters field) with the relevant value from criteriaArguments.
     *
     * @throws NoMatchingParameterException if the parameter cannot be found as a key in criteriaArguments.
     */
    private void replaceParameters() throws NoMatchingParameterException {
        // Now that we know there are equal number of parameters and arguments, try replacing the parameters with arguments.
        if (criteriaArguments.size() != 0) {
            for (Criteria criterion : criteria) {

                String filter = criterion.filter;
                String[] splitFilters = filter.split(",");
                List<String> resultFilters = new ArrayList<>();

                for (String splitFilter : splitFilters) {
                    if (splitFilter.length() >= 1 && splitFilter.substring(0, 1).equals("@")) {
                        String paramName = splitFilter.substring(1);
                        String paramValue = criteriaArguments.get(paramName);
                        if (paramValue != null) {
                            resultFilters.add(paramValue);
                        } else {
                            String message = String.format("No criteria parameter was found with the name, %s", paramName);
                            throw new NoMatchingParameterException(message);
                        }
                    }
                }

                if (resultFilters.size() != 0) {
                    criterion.filter = String.join(",", resultFilters);
                }
            }
        }
    }

    /**
     * Adds isNull criterion to criteria if any of the statement's joins are an 'excluding' join, such as LEFT_JOIN_EXCLUDING,
     * RIGHT_JOIN_EXCLUDING, or FULL_OUTER_JOIN_EXCLUDING.
     */
    private void addExcludingJoinCriteria() {
        if (! this.joins.isEmpty()) {
            for (Join join : joins) {
                Join.JoinType joinType = join.getJoinType();
                if (joinType.equals(LEFT_EXCLUDING)) {
                    addCriteriaForExcludingJoin(join.getTargetJoinColumns());
                } else if (joinType.equals(RIGHT_EXCLUDING)) {
                    addCriteriaForExcludingJoin(join.getParentJoinColumns());
                } else if (joinType.equals(FULL_OUTER_EXCLUDING)) {
                    List<String> allJoinColumnns = new ArrayList<>();
                    allJoinColumnns.addAll(join.getParentJoinColumns());
                    allJoinColumnns.addAll(join.getTargetJoinColumns());
                    addCriteriaForExcludingJoin(allJoinColumnns);
                }
            }
        }
    }

    private void addCriteriaForExcludingJoin(List<String> columns) {
        // Get max id
        int maxId = 0;
        for (Criteria criterion : criteria) {
            if (criterion.getId() > maxId) {
                maxId = criterion.getId();
            }
        }

        int nextId = maxId + 1;
        int parentId = 0;
        for (int i=0; i<columns.size(); i++) {
            Criteria criterion = new Criteria(nextId);

            // For the first iteration in this for loop, the parentId will be null.  On every iteration afterwards,
            // the parentId should be set to the nextId so that every criterion generated by this method is a child of
            // the first criterion that has a null parentId.
            if (i == 0) {
                parentId = nextId;
                criterion.setParentId(null);
                if (! criteria.isEmpty()) {
                    criterion.setConjunction(Conjunction.And);
                }
            } else {
                criterion.setParentId(parentId);
                criterion.setConjunction(Conjunction.Or);
            }

            criterion.setColumn(columns.get(i));
            criterion.setOperator(Operator.isNull);
            criteria.add(criterion);
        }
    }

    /**
     * Returns a List of all fully qualified column names (table.column) contained in the SelectStatement, which includes
     * all Columns and all Criteria Columns.
     *
     * @return
     */
    public List<String> getAllFullyQualifiedColumnNames() {
        List<String> allFullyQualifiedNames = new ArrayList<>();
        columns.forEach(column -> allFullyQualifiedNames.add(column.getFullyQualifiedName()));
        criteria.forEach(criterion -> allFullyQualifiedNames.add(criterion.column));
        return allFullyQualifiedNames;
    }

}
