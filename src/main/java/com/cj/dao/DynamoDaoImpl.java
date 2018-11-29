package com.cj.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class DynamoDaoImpl implements DynamoDao {
    @Qualifier("dynamo.db")
    @Autowired
    private DynamoDB dynamoDB;
    private final String DYNAMODB_QUERY_TEMPLATES = "query_builder_templates";
    private final String HASH_KEY_NAME = "name";


    @Override
    public boolean save(String primaryKey, String json) {
        Item item = Item.fromJSON(json).withPrimaryKey(HASH_KEY_NAME, primaryKey);
        PutItemOutcome outcome = dynamoDB.getTable(DYNAMODB_QUERY_TEMPLATES).putItem(item);
        return true;
    }

    @Override
    public Item findByName(String name) {
        Table table = dynamoDB.getTable(DYNAMODB_QUERY_TEMPLATES);
        return table.getItem(HASH_KEY_NAME, name);
    }

}
