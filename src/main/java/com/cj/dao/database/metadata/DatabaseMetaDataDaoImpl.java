package com.cj.dao.database.metadata;

import com.cj.model.Table;
import com.cj.model.mappers.TableMapper;
import com.cj.utils.Converter;
import com.querybuilder4j.utils.ResultSetToHashMapConverter;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;

@Repository
@PropertySource("application.properties")
public class DatabaseMetaDataDaoImpl implements DatabaseMetaDataDao {

    private DataSource dataSource;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${query.tables-and-views}")
    private String tablesAndViewsSql;

    @Autowired
    public DatabaseMetaDataDaoImpl(@Qualifier("querybuilder4j.db") DataSource dataSource) {
        this.dataSource = dataSource;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public String getSchemas() throws Exception {

        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getSchemas();
            JSONArray jsonArray = Converter.convertToJSON(rs);
            return jsonArray.toString();
        } catch (Exception ex) {
            throw ex;
        }

    }

    public List<Table> getTablesAndViews(String schema) {
        return namedParameterJdbcTemplate.query(this.tablesAndViewsSql, new TableMapper());
    }

    public Map<String, Integer> getColumns(String schema, String table) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getColumns(null, schema, table, "%");
            return ResultSetToHashMapConverter.toHashMap(rs);
        } catch (SQLException ex) {
            throw ex;
        }

    }

}
