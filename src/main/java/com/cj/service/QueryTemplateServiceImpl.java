package com.cj.service;

import com.cj.dao.QueryTemplateDao;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryTemplateServiceImpl implements QueryTemplateService {
    @Autowired
    private QueryTemplateDao queryTemplateDao;


    @Override
    public boolean save(String primaryKey, String json) {
        return queryTemplateDao.save(primaryKey, json);
    }

    @Override
    public String findByName(String name) {
        return queryTemplateDao.findByName(name);
    }

    @Override
    public String getNames(Integer limit, Integer offset, boolean ascending) throws Exception {
        List<String> queryNames = queryTemplateDao.getNames(limit, offset, ascending);
        JSONArray jsonArray = new JSONArray();
        queryNames.forEach((queryName) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", queryName);
            jsonArray.put(jsonObject);
        });
        return jsonArray.toString();
    }
}
