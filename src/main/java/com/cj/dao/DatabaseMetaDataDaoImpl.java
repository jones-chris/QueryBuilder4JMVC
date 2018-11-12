package com.cj.dao;

import com.cj.utils.Converter;
import com.querybuilder4j.utils.JSONRowMapper;
import com.querybuilder4j.utils.ResultSetToHashMapConverter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DatabaseMetaDataDaoImpl implements DatabaseMetaDataDao {
    @Qualifier("querybuilder4j.db")
    @Autowired
    private DataSource dataSource;


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
    public String executeQuery(String sql, SqlParameterSource paramMap) throws Exception {

        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        List<JSONObject> jsonObjects = jdbcTemplate.query(sql, paramMap, new JSONRowMapper());
        JSONArray jsonArray = new JSONArray(jsonObjects);
        return jsonArray.toString();

//        try (Connection conn = dataSource.getConnection();
//             Statement stmt = conn.createStatement()) {
//            ResultSet rs = stmt.executeQuery(sql);
//            return Converter.convertToJSON(rs).toString();
//        } catch (Exception ex) {
//            throw ex;
//        }

    }

}
