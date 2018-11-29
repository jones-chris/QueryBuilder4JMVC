package com.cj.service;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.cj.dao.DynamoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DynamoServiceImpl implements DynamoService {
    @Autowired
    private DynamoDao dynamoDao;


    @Override
    public boolean save(String primaryKey, String json) {
        return dynamoDao.save(primaryKey, json);
    }

    @Override
    public Item findByName(String name) {
        return dynamoDao.findByName(name);
    }
}
