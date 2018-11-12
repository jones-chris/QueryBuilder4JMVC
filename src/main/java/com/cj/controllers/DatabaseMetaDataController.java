package com.cj.controllers;

import com.cj.service.DatabaseHealerService;
import com.cj.service.DatabaseMetaDataService;
import com.cj.service.LoggingService;
import com.cj.service.DatabaseAuditService;
import com.cj.utils.Converter;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
public class DatabaseMetaDataController {
    @Autowired
    private LoggingService loggingService;
    @Autowired
    private DatabaseAuditService databaseAuditService;


    @Autowired
    private DatabaseHealerService databaseHealerService;
    @Autowired
    private DatabaseMetaDataService databaseMetaDataService;

    //get query templates
    @RequestMapping(value = "/queryTemplates", method = RequestMethod.GET)
    @ResponseBody
    public String getQueryTemplates() throws Exception {
        return "[{\"data\":\"query template 1\"}]";
    }

//    //get query template by unique name
//    @RequestMapping(value = "/queryTemplates/{name}", method = RequestMethod.GET)
//    @ResponseBody
//    public JSONArray getQueryTemplateById(@PathVariable String name) throws Exception {
//        Statement stmt = dataSource.getConnection().createStatement();
//        String sql = String.format("SELECT query_object FROM qb_templates WHERE name = '%s';", name);
//        ResultSet rs = stmt.executeQuery(sql);
//
//        return Converter.convertToJSON(rs);
//    }
//
    //get schemas
    @RequestMapping(value = "/schemas", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getSchemas() {
        try {
            String schemasJson = databaseMetaDataService.getSchemas();
            return new ResponseEntity<>(schemasJson, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.OK);
        }
    }

    //get tables/views
    @RequestMapping(value = "/tablesAndViews/{schema}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getTablesAndViews(@PathVariable(value = "schema", required = true) String schema) {
        try {
            schema = (schema.equals("null")) ? null : schema;
            String tablesJson = databaseMetaDataService.getTablesAndViews(schema);
            return new ResponseEntity<>(tablesJson, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //get table/view columns
    @RequestMapping(value = "/columns/{schema}/{tables}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getColumns(@PathVariable String schema,
                                             @PathVariable String tables)  {
        try {
            schema = (schema.equals("null")) ? null : schema;

            String[] splitTables = tables.split("&");

            Map<String, Integer> columnsMap = new HashMap<>();
            for (String table : splitTables) {
                Map<String, Integer> columns = databaseMetaDataService.getColumns(schema, table);
                columnsMap.putAll(columns);
            }

            String columnsJson = Converter.convertToJSON(columnsMap.keySet().toArray(), "column").toString();
            return new ResponseEntity<>(columnsJson, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //execute query and return results
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> getQueryResults(SelectStatement selectStatement) {

        try {
            //Load properties file.
            Properties props = new Properties();
            InputStream input = this.getClass().getClassLoader().getResourceAsStream("querybuilder4j_db.properties");
            props.load(input);

            String sql = selectStatement.toSql(props);
            SqlParameterSource paramMap = selectStatement.getSqlParameterMap();
            String queryResults = databaseMetaDataService.executeQuery(sql, paramMap);

            //Log the SelectStatement and the database audit results to logging.db
            //If any of the database audit results return false (a failure - meaning this statement changed the querybuilder4j
            //  database in some way), then send email to querybuilder4j@gamil.com for immediate notification.
            Map<String, Boolean> databaseAuditResults = databaseAuditService.runAllChecks(1, 1, new String[1], 1);
            loggingService.add(selectStatement, sql, databaseAuditResults);

            Map<String, Runnable> healerFunctions = buildHealerFunctionsMap();
            for (String key : databaseAuditResults.keySet()) {
                if (databaseAuditResults.get(key) == false) {
                    healerFunctions.get(key).run();
                }
            }

            //Create JSON string to be added to response body.  JSON string includes query results, selectStatement in SQL format,
            //  and database audit results.
            JSONObject jsonObject = new JSONObject(databaseAuditResults);
            jsonObject.append("queryResults", queryResults);
            jsonObject.append("sqlResult", sql);

            return new ResponseEntity<>(jsonObject.toString(4), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private Map<String, Runnable> buildHealerFunctionsMap() {
        Map<String, Runnable> healerFunctions = new HashMap<>();

        healerFunctions.put("databaseExists", () -> {
            databaseHealerService.createDatabase();
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });
        healerFunctions.put("tableExists", () -> {
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });
        healerFunctions.put("tablesAreSame", () -> {
            databaseHealerService.dropAllTablesExcept("county_spending_detail");
        });
        healerFunctions.put("numOfTableColumnsAreSame", () -> {
            databaseHealerService.dropTable();
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });
        healerFunctions.put("numOfTableRowsAreSame", () -> {
            databaseHealerService.dropTable();
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });
        healerFunctions.put("tableDataIsSame", () -> {
            databaseHealerService.dropTable();
            databaseHealerService.createTable();
            databaseHealerService.insertTestData();
        });

        return healerFunctions;
    }
}
