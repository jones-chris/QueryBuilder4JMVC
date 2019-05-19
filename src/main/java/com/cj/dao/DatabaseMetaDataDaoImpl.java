package com.cj.dao;

import com.cj.utils.Converter;
import com.querybuilder4j.config.DatabaseType;
import com.querybuilder4j.config.Operator;
import com.querybuilder4j.sqlbuilders.statements.Criteria;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import com.querybuilder4j.utils.ResultSetToHashMapConverter;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;
import java.util.Properties;

@Repository
public class DatabaseMetaDataDaoImpl implements DatabaseMetaDataDao {
    @Qualifier("querybuilder4j.db")
    @Autowired
    private DataSource dataSource;
    @Qualifier("querybuilder4jdb_properties")
    @Autowired
    private Properties dataSourceProperties;


    @Override
    public String getSchemas() throws Exception {

        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getSchemas();
            JSONArray jsonArray = Converter.convertToJSON(rs);
            return jsonArray.toString();
        } catch (Exception ex) {
            throw ex;
        }

    }

    @Override
    public String getTablesAndViews(String schema) throws Exception {

        String sql = "SELECT tbl_name FROM sqlite_master where type ='table' OR type ='view';";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            JSONArray jsonArray = Converter.convertToJSON(rs);
            return jsonArray.toString();
        } catch (SQLException ex) {
            throw ex;
        }

    }

    @Override
    public Map<String, Integer> getColumns(String schema, String table) throws SQLException {

        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getColumns(null, schema, table, "%");
            return ResultSetToHashMapConverter.toHashMap(rs);
        } catch (SQLException ex) {
            // I must rethrow exception to calling code because I need to use the try-with-resources block to close the conn and stmt.
            throw ex;
        }

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
        selectStatement.getColumns().add(tableAndColumn);
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
        selectStatement.setDatabaseType(DatabaseType.Sqlite);

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
