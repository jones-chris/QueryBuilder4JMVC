package com.cj.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;

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

}
