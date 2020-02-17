package com.cj.dao.database.data;

import com.cj.utils.Converter;
import com.querybuilder4j.statements.Column;
import com.querybuilder4j.statements.Criteria;
import com.querybuilder4j.statements.Operator;
import com.querybuilder4j.statements.SelectStatement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

@Repository
public class DatabaseDataDaoImpl implements DatabaseDataDao {

    private DataSource dataSource;
    private Properties dataSourceProperties;

    public DatabaseDataDaoImpl(@Qualifier("querybuilder4j.db") DataSource dataSource,
                                      @Qualifier("querybuilder4jdb_properties") Properties dataSourceProperties) {
        this.dataSource = dataSource;
        this.dataSourceProperties = dataSourceProperties;
    }

    @Override
    public String executeQuery(String sql) throws Exception {

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return Converter.convertToJSON(rs).toString();
        } catch (Exception ex) {
            throw ex;
        }

    }

    @Override
    public String getColumnMembers(String schema, String table, String column, int limit, int offset, boolean ascending,
                                   String search) throws Exception {
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

        String sql = selectStatement.toSql(dataSourceProperties);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return Converter.convertToJSON(rs).toString();
        } catch (Exception ex) {
            throw ex;
        }
    }
}
