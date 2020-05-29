package com.cj.utils;

import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.ResultSet;
import java.util.Map;

/**
 * Utility for converting ResultSets into some Output formats
 * @author marlonlom
 */
public class Converter {

    /**
     * Convert a result set into a JSONArray.
     * @param resultSet
     * @return a JSONArray
     * @throws Exception
     */
    public static JSONObject convertToJSON(ResultSet resultSet) throws Exception {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("columns", new JSONArray());  // Add a JSONArray to hold the query columns.  // todo:  make this a more general "metadata" json array for future use?
        jsonObject.put("data", new JSONArray());  // Add a JSONArray to hold the data from the result of the query.

        boolean columnNamesRetrieved = false;

        while (resultSet.next()) {
            JSONArray newRowJsonArray = new JSONArray();
            int totalColumns = resultSet.getMetaData().getColumnCount();

            // If column names have not been retrieved yet, build the json array before getting data.
            if (! columnNamesRetrieved) {
                for (int i = 0; i < totalColumns; i++) {
                    jsonObject.getJSONArray("columns").put(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase());
                }

                columnNamesRetrieved = true;
            }

            // Now get the row's data, put it in a json array, and add it to the json object.
            for (int i = 0; i < totalColumns; i++) {
                newRowJsonArray.put(resultSet.getObject(i + 1));
            }

            jsonObject.getJSONArray("data").put(newRowJsonArray);
        }

        return jsonObject;
    }

    /**
     * Convert a Map into a JSONArray.
     * @param map
     * @return
     */
    public static JSONArray convertToJSON(Map<Object, Object> map) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for (Object key : map.keySet()) {
            String value = map.get(key).toString();

            jsonObject = new JSONObject();
            jsonObject.put(key.toString(), value);

            jsonArray.put(jsonObject);
        }

        return jsonArray;
    }

    /**
     * Converts a String array into a JSON Array.  The key will be used as the key for each item in the array.
     * @param array
     * @param key
     * @return
     * @throws Exception
     */
    public static JSONArray convertToJSON(Object[] array, String key) throws Exception {

        JSONArray jsonArray = new JSONArray();
        JSONObject obj;
        for (Object item : array) {
            obj = new JSONObject();
            obj.put(key, item.toString());
            jsonArray.put(obj);
        }

        return jsonArray;

    }

    /**
     * Convert a result set into a XML List
     * @param resultSet
     * @return a XML String with list elements
     * @throws Exception if something happens
     */
    public static String convertToXML(ResultSet resultSet) throws Exception {

        StringBuffer xmlArray = new StringBuffer("<results>");
        while (resultSet.next()) {
            int total_rows = resultSet.getMetaData().getColumnCount();
            xmlArray.append("<result ");
            for (int i = 0; i < total_rows; i++) {
                xmlArray.append(" " + resultSet.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase() + "='" + resultSet.getObject(i + 1) + "'"); }
            xmlArray.append(" />");
        }
        xmlArray.append("</results>");
        return xmlArray.toString();

    }

}
