package com.cj.config;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    /**
     * Expectd data in the county_spending_detail table of querybuilder4j.db.
     */
    public static final String[][] currentData = {{"4", "2017", "General Government", "Liquor Control", "Retail Sales Operations", "214.85"},
            {"5", "2017", "General Government", "Liquor Control", "Warehouse Operations", "198459.06"},
            {"5", "2017", "Housing and Community Development", "Housing and Community Affairs", "Multi-Family Housing Programs", "200"},
            {"5", "2017", "Transportation", "Transportation", "Administration", "878"},
            {"6", "2014", "General Government", "Human Resources", "Health & Employee Welfare", "1576281.42"},
            {"6", "2017", "Health and Human Services", "Health and Human Services", "Outpatient Behavioral Health Services - Adult", "169.02"}};

    /**
     * The interval (in milliseconds) that the S3TransferWrapper timed task will run at.
     */
    public static final long s3TimedTaskInterval = 3600000L; //1 hour.

    public static final Map<Integer, String> tableauDataTypeMappings = new HashMap<Integer, String>() {{
//        put(Types.ARRAY, "");
        put(Types.BIGINT, "int");
//        put(Types.BINARY, true);
//        put(Types.BIT, false);
//        put(Types.BLOB, true);
        put(Types.BOOLEAN, "bool");
        put(Types.CHAR, "string");
//        put(Types.CLOB, true);
//        put(Types.DATALINK, false);
        put(Types.DATE, "date");
        put(Types.DECIMAL, "float");
//        put(Types.DISTINCT, true);
        put(Types.DOUBLE, "int");
        put(Types.FLOAT, "float");
        put(Types.INTEGER, "int");
//        put(Types.JAVA_OBJECT, true);
        put(Types.LONGNVARCHAR, "string");
//        put(Types.LONGVARBINARY, true);
        put(Types.LONGVARCHAR, "string");
        put(Types.NCHAR, "string");
//        put(Types.NCLOB, true);
//        put(Types.NULL, true);
        put(Types.NUMERIC, "int");
        put(Types.NVARCHAR, "string");
//        put(Types.OTHER, true);
//        put(Types.REAL, false);
//        put(Types.REF, true);
//        put(Types.REF_CURSOR, true);
//        put(Types.ROWID, false);
        put(Types.SMALLINT, "int");
//        put(Types.SQLXML, true);
//        put(Types.STRUCT, true);
//        put(Types.TIME, true);
//        put(Types.TIME_WITH_TIMEZONE, true);
        put(Types.TIMESTAMP, "datetime");
//        put(Types.TIMESTAMP_WITH_TIMEZONE, true);
        put(Types.TINYINT, "int");
//        put(Types.VARBINARY, true);
        put(Types.VARCHAR, "string");
    }};

}
