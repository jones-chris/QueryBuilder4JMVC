package com.cj.sql_builder;

import com.cj.model.select_statement.SelectStatement;

public class SqlServerSqlBuilder extends SqlBuilder {

    public SqlServerSqlBuilder(SelectStatement selectStatement) throws Exception {
        super(selectStatement);
        beginningDelimiter = '[';
        endingDelimiter = ']';
    }

    @Override
    public String buildSql() throws Exception {
        // Select
        this.createSelectClause(selectStatement.isDistinct(), selectStatement.getColumns());

        // From
        this.createFromClause(selectStatement.getTable());

        // Where
        this.createWhereClause(selectStatement.getCriteria());

        // Group By
        if (this.selectStatement.isGroupBy()) {
            this.createGroupByClause(selectStatement.getColumns());
        }

        // Order By
        if (this.selectStatement.isOrderBy()) {
            this.createOrderByClause(selectStatement.getColumns(), selectStatement.isAscending());
        }

        // Offset
        this.createOffsetClause(selectStatement.getOffset());

        // Fetch/Limit
        this.createFetchClause(selectStatement.getLimit());

        return this.stringBuilder.toString();
    }

    @Override
    protected void createOffsetClause(Long offset) {
        if (offset != null) {
            this.stringBuilder.append(" OFFSET ").append(offset).append(" ROWS ");
        }
    }

    private void createFetchClause(Long limit) {
        if (limit != null) {
            this.stringBuilder.append(" FETCH NEXT ").append(limit).append(" ROWS ONLY ");
        }
    }

}
