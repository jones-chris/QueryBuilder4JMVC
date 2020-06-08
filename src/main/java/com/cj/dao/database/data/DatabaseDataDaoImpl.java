package com.cj.dao.database.data;

import com.cj.config.Qb4jConfig;
import com.cj.model.QueryResult;
import com.cj.utils.Converter;
import com.querybuilder4j.statements.Column;
import com.querybuilder4j.statements.Criteria;
import com.querybuilder4j.statements.Operator;
import com.querybuilder4j.statements.SelectStatement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

@Repository
public class DatabaseDataDaoImpl implements DatabaseDataDao {

    private Qb4jConfig qb4jConfig;

    @Autowired
    public DatabaseDataDaoImpl(Qb4jConfig qb4jConfig) {
        this.qb4jConfig = qb4jConfig;
    }

    @Override
    public QueryResult executeQuery(String databaseName, String sql) throws Exception {
        DataSource dataSource = qb4jConfig.getTargetDataSourceAsDataSource(databaseName);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return new QueryResult(rs, sql);
//            return Converter.convertToJSON(rs);
        } catch (Exception ex) {
            throw ex;
        }

    }

    @Override
    public String getColumnMembers(String databaseName, String schema, String table, String column, int limit, int offset,
                                   boolean ascending, String search) throws Exception {
        schema = (schema.equals("null")) ? null : schema;
        String tableAndColumn = table + "." + column;

        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDistinct(true);
        selectStatement.getColumns().add(new Column(tableAndColumn));
        selectStatement.setTable(table);
        if (search != null) {
            Criteria criterion = new Criteria(0);
            criterion.setColumn(tableAndColumn);
            criterion.setOperator(Operator.like);
            criterion.setFilter(search);
            selectStatement.getCriteria().add(criterion);
        }
        selectStatement.setLimit(Integer.toUnsignedLong(limit));
        selectStatement.setOffset(Integer.toUnsignedLong(offset));
        selectStatement.setOrderBy(true);
        selectStatement.setAscending(ascending);

        Properties dataSourceProperties = qb4jConfig.getTargetDataSources().stream()
                .filter(source -> source.getName().equals(databaseName))
                .findFirst()
                .get()
                .getProperties();
        String sql = selectStatement.toSql(dataSourceProperties);

        DataSource dataSource = qb4jConfig.getTargetDataSourceAsDataSource(databaseName);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return Converter.convertToJSON(rs).toString();
        } catch (Exception ex) {
            throw ex;
        }
    }
}
