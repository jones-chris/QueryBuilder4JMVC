package com.cj.sql_builder;

import com.cj.cache.DatabaseMetadataCache;
import com.cj.constants.DatabaseType;
import com.cj.model.select_statement.SelectStatement;
import com.cj.model.select_statement.validator.DatabaseMetadataCacheValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SqlBuilderFactory {

//    @Autowired
    private DatabaseMetadataCache databaseMetadataCache;

//    @Autowired
    private DatabaseMetadataCacheValidator databaseMetadataCacheValidator;

    @Autowired
    public SqlBuilderFactory(DatabaseMetadataCache databaseMetadataCache, DatabaseMetadataCacheValidator databaseMetadataCacheValidator) {
        this.databaseMetadataCache = databaseMetadataCache;
        this.databaseMetadataCacheValidator = databaseMetadataCacheValidator;
    }

    public SqlBuilder buildSqlBuilder(SelectStatement selectStatement) throws Exception {
        SqlBuilder sqlBuilder;

        DatabaseType databaseType = selectStatement.getDatabase().getDatabaseType();
        switch (databaseType) {
            case MySql:
                sqlBuilder = new MySqlSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator)
                        .setStatement(selectStatement);
                break;
            case Oracle:
                sqlBuilder = new OracleSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator)
                        .setStatement(selectStatement);
                break;
            case PostgreSQL:
                sqlBuilder = new PostgresSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator)
                        .setStatement(selectStatement);
                break;
            case SqlServer:
                sqlBuilder = new SqlServerSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator)
                        .setStatement(selectStatement);
                break;
            case Sqlite:
                sqlBuilder = new SqliteSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator)
                        .setStatement(selectStatement);
                break;
            default:
                throw new RuntimeException(String.format("Database type, %s, not recognized", databaseType));
        }

        return sqlBuilder;
    }

}
