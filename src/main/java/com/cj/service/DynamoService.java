package com.cj.service;

import com.amazonaws.services.dynamodbv2.document.Item;

public interface DynamoService {

    boolean save(String primaryKey, String json);
    Item findByName(String name);

}
