package com.cj.dao.database.data;

import com.cj.cache.DatabaseMetadataCache;
import com.cj.config.Qb4jConfig;
import com.cj.model.Column;
import com.cj.model.QueryResult;
import com.cj.model.Table;
import com.cj.model.select_statement.Criterion;
import com.cj.model.select_statement.Operator;
import com.cj.model.select_statement.SelectStatement;
import com.cj.sql_builder.SqlBuilderFactory;
import com.cj.utils.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Repository
public class DatabaseDataDaoImpl implements DatabaseDataDao {

    private Qb4jConfig qb4jConfig;

    private DatabaseMetadataCache databaseMetadataCache;

    @Autowired
    public DatabaseDataDaoImpl(Qb4jConfig qb4jConfig, DatabaseMetadataCache databaseMetadataCache) {
        this.qb4jConfig = qb4jConfig;
        this.databaseMetadataCache = databaseMetadataCache;
    }

    @Override
    public QueryResult executeQuery(String databaseName, String sql) throws Exception {
        DataSource dataSource = qb4jConfig.getTargetDataSourceAsDataSource(databaseName);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return new QueryResult(rs, sql);
        }

    }

    @Override
    public String getColumnMembers(String databaseName, String schemaName, String tableName, String columnName, int limit, int offset,
                                   boolean ascending, String search) throws Exception {
        schemaName = (schemaName.equals("null")) ? null : schemaName;

        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDistinct(true);
        int columnDataType = this.databaseMetadataCache.findColumnByName(databaseName, schemaName, tableName, columnName)
                .getDataType();
        Column column = new Column(databaseName, schemaName, tableName, columnName, columnDataType, null);
        selectStatement.getColumns().add(column);
        selectStatement.setTable(new Table(databaseName, schemaName, tableName));
        if (search != null) {
            Criterion criterion = new Criterion(null, null, column, Operator.like, search, null);
            selectStatement.getCriteria().add(criterion);
        }
        selectStatement.setLimit(Integer.toUnsignedLong(limit));
        selectStatement.setOffset(Integer.toUnsignedLong(offset));
        selectStatement.setOrderBy(true);
        selectStatement.setAscending(ascending);

        String sql = SqlBuilderFactory.buildSqlBuilder(selectStatement)
                .buildSql();

        DataSource dataSource = qb4jConfig.getTargetDataSourceAsDataSource(databaseName);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return Converter.convertToJSON(rs).toString();
        }

    }
}
