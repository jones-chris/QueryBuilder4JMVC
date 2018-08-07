package com.cj.config;

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

}
