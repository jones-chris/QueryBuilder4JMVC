package com.cj.controllers;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.cj.service.*;
import com.cj.utils.Converter;
import com.google.gson.Gson;
import com.querybuilder4j.sqlbuilders.statements.SelectStatement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    private DynamoService dynamoService;

    //get query templates
    @RequestMapping(value = "/queryTemplates", method = RequestMethod.GET)
    @ResponseBody
    public String getQueryTemplates() {
        return "[{\"data\":\"query template 1\"}]";
    }

    //get query template by unique name
    @RequestMapping(value = "/queryTemplates/{name}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getQueryTemplateById(@PathVariable String name) throws Exception {
        try {
            Item item = dynamoService.findByName(name);
            String json = item.toJSON();
            return new ResponseEntity<>(json, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
//        String sql = String.format("SELECT query_object FROM qb_templates WHERE name = '%s';", name);
    }


    //save query template
    @RequestMapping(value = "/saveQueryTemplate/{name}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> saveQueryTemplate(SelectStatement stmt,
                                                    @PathVariable(value = "name") String name) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(stmt);

            // save json
            dynamoService.save(name, json);

            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //get schemas
    @RequestMapping(value = "/schemas", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getSchemas() {
        try {
            String schemasJson = databaseMetaDataService.getSchemas();
            return new ResponseEntity<>(schemasJson, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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

    // Get column members
    @RequestMapping(value = "/columns-members/{schema}/{table}/{column}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getColumnMembers(@PathVariable String schema,
                                                   @PathVariable String table,
                                                   @PathVariable String column,
                                                   @RequestParam int limit,
                                                   @RequestParam int offset,
                                                   @RequestParam boolean ascending,
                                                   @RequestParam(required = false) String search) {
        try {
            String jsonResults = databaseMetaDataService.getColumnMembers(schema, table, column, limit, offset, ascending, search);
            return new ResponseEntity<>(jsonResults, HttpStatus.OK);
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
            String queryResults = databaseMetaDataService.executeQuery(sql);

            //Log the SelectStatement and the database audit results to logging.db
            //If any of the database audit results return false (a failure - meaning this statement changed the querybuilder4j
            //  database in some way), then send email to querybuilder4j@gamil.com for immediate notification.
            Map<String, Boolean> databaseAuditResults = databaseAuditService.runAllChecks(3, 1, new String[1], 1);
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
