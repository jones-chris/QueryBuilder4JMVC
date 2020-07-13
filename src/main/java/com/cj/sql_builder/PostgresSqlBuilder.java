package com.cj.sql_builder;

import com.cj.cache.DatabaseMetadataCache;
import com.cj.model.select_statement.validator.DatabaseMetadataCacheValidator;

public class PostgresSqlBuilder extends SqlBuilder {

    public PostgresSqlBuilder(DatabaseMetadataCache databaseMetadataCache,
                              DatabaseMetadataCacheValidator databaseMetadataCacheValidator) {
        super(databaseMetadataCache, databaseMetadataCacheValidator);
        beginningDelimiter = '"';
        endingDelimiter = '"';
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public String buildSql() {
        this.createSelectClause();
        this.createFromClause();
        this.createJoinClause();
        this.createWhereClause();
        this.createGroupByClause();
        this.createOrderByClause();
        this.createLimitClause();
        this.createOffsetClause();

        return this.stringBuilder.toString();
    }

}
