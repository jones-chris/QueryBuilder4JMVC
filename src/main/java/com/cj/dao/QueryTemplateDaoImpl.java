package com.cj.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class QueryTemplateDaoImpl implements QueryTemplateDao {
    @Qualifier("query_templates.db")
    @Autowired
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;


    @Override
    public boolean save(String primaryKey, String json) {
        String sql = "insert into query_templates " +
                     "values (?, ?)";

        jdbcTemplate = new JdbcTemplate(dataSource);
        int numRowsInserted = jdbcTemplate.update(sql, new Object[] {primaryKey, json});

        return numRowsInserted > 0;
    }

    @Override
    public String findByName(String name) {
        String sql = "select query_json " +
                     "from query_templates " +
                     "where name = ? ";

        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForObject(sql, new Object[] {name}, String.class);
    }

    @Override
    public List<String> getNames(Integer limit, Integer offset, boolean ascending) throws Exception {
        // Throw exception if offset is not null and limit is null.  SQL does not allow this.
        if (offset != null && limit == null) {
            throw new Exception("Limit cannot be null if offset is not null");
        }

        String asc = (ascending) ? "asc" : "desc";

        String sql = "select distinct name " +
                     "from query_templates " +
                     "order by name " + asc + " ";

        // Determine how many elements should be in the Object[] that will hold the query parameters.
        int numArrayElementsNeeded = 0;
        if (limit != null) numArrayElementsNeeded += 1;
        if (offset != null) numArrayElementsNeeded += 1;
        Object[] params = new Object[numArrayElementsNeeded];

        // Add LIMIT and OFFSET SQL clauses, if needed.
        // Add limit and offset to param Object[] to be used in parameterized SQL query.
        if (limit != null) {
            sql += "limit ? ";
            params[0] = limit;
        }

        if (offset != null) {
            sql += "offset ? ";
            params[1] = offset;
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForList(sql, params, String.class);
    }

}
