package com.cj.sql_builder;

import com.cj.constants.DatabaseType;
import com.cj.model.select_statement.SelectStatement;

public class SqlBuilderFactory {

    public static SqlBuilder buildSqlBuilder(SelectStatement selectStatement) throws Exception {
        SqlBuilder sqlBuilder;

        DatabaseType databaseType = selectStatement.getDatabase().getDatabaseType();
        switch (databaseType) {
            case MySql:
                sqlBuilder = new MySqlSqlBuilder(selectStatement);
                break;
            case Oracle:
                sqlBuilder = new OracleSqlBuilder(selectStatement);
                break;
            case PostgreSQL:
                sqlBuilder = new PostgresSqlBuilder(selectStatement);
                break;
            case SqlServer:
                sqlBuilder = new SqlServerSqlBuilder(selectStatement);
                break;
            case Sqlite:
                sqlBuilder = new SqliteSqlBuilder(selectStatement);
                break;
            default:
                throw new RuntimeException(String.format("Database type, %s, not recognized", databaseType));
        }

        return sqlBuilder;
    }

}
