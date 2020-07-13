package com.cj.sql_builder;

import com.cj.cache.DatabaseMetadataCache;
import com.cj.model.select_statement.validator.DatabaseMetadataCacheValidator;

@SuppressWarnings("DuplicatedCode")
public class MySqlSqlBuilder extends SqlBuilder {

    public MySqlSqlBuilder(DatabaseMetadataCache databaseMetadataCache,
                           DatabaseMetadataCacheValidator databaseMetadataCacheValidator) {
        super(databaseMetadataCache, databaseMetadataCacheValidator);
        beginningDelimiter = '`';
        endingDelimiter = '`';
    }

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
