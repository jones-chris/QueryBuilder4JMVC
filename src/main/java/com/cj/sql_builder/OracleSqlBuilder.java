package com.cj.sql_builder;

import com.cj.model.select_statement.SelectStatement;

public class OracleSqlBuilder extends SqlBuilder {

    public OracleSqlBuilder(SelectStatement stmt) throws Exception {
        super(stmt);
        beginningDelimiter = '"';
        endingDelimiter = '"';
    }

    @Override
    public String buildSql() throws Exception {
        // Select
        createSelectClause(selectStatement.isDistinct(), selectStatement.getColumns());

        // From
        createFromClause(selectStatement.getTable());

        // Where
        createWhereClause(selectStatement.getCriteria());

        // Limit
        if (! selectStatement.getCriteria().isEmpty()) {
            this.stringBuilder.append(" AND ");
        } else {
            this.stringBuilder.append(" WHERE ");
        }
        createLimitClause(selectStatement.getLimit());

        // Group By
        createGroupByClause(selectStatement.getColumns());

        // Order By
        createOrderByClause(selectStatement.getColumns(), selectStatement.isAscending());

        // Offset
        createOffsetClause(selectStatement.getOffset());

        return this.stringBuilder.toString();
    }

    @Override
    protected void createLimitClause(Long limit) {
        if (limit != null) {
            this.stringBuilder.append(" ROWNUM < ").append(limit);
        }
    }

    @Override
    protected void createOffsetClause(Long offset) {
        if (offset != null) {
            this.stringBuilder.append(" OFFSET ").append(offset).append(" ROWS ");
        }
    }

}
