package com.cj.dao;

import com.amazonaws.services.dynamodbv2.document.Item;

public interface DynamoDao {

    boolean save(String primaryKey, String json);
    Item findByName(String name);

}
