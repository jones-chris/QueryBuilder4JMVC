package com.cj.sql_builder;

import com.cj.model.select_statement.SelectStatement;

public class MySqlSqlBuilder extends SqlBuilder {

    public MySqlSqlBuilder(SelectStatement selectStatement) throws Exception {
        super(selectStatement);
        beginningDelimiter = '`';
        endingDelimiter = '`';
    }

    @Override
    public String buildSql() throws Exception {
        // Select
        this.createSelectClause(selectStatement.isDistinct(), selectStatement.getColumns());

        // From
        this.createFromClause(selectStatement.getTable());

        // Joins
        this.createJoinClause(selectStatement.getJoins());

        // Where
        this.createWhereClause(selectStatement.getCriteria());

        // Group By
        if (selectStatement.isGroupBy()) {
            this.createGroupByClause(selectStatement.getColumns()));
        }

        // Order By
        if (selectStatement.isOrderBy()) {
            this.createOrderByClause(selectStatement.getColumns(), selectStatement.isAscending());
        }

        // Limit
        this.createLimitClause(selectStatement.getLimit());

        // Offset
        this.createOffsetClause(selectStatement.getOffset());

        return this.stringBuilder.toString();
    }

}
