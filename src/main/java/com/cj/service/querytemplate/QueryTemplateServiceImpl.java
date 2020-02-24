package com.cj.service.querytemplate;

import com.google.gson.Gson;
import com.querybuilder4j.databasemetadata.QueryTemplateDao;
import com.querybuilder4j.statements.SelectStatement;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryTemplateServiceImpl implements QueryTemplateService {

    private QueryTemplateDao queryTemplateDao;

    @Autowired
    public QueryTemplateServiceImpl(QueryTemplateDao queryTemplateDao) {
        this.queryTemplateDao = queryTemplateDao;
    }

    @Override
    public boolean save(String primaryKey, String json) {
        return queryTemplateDao.save(primaryKey, json);
    }

    @Override
    public String findByName(String name) {
        SelectStatement stmt = queryTemplateDao.getQueryTemplateByName(name);
        return new Gson().toJson(stmt);
    }

    @Override
    public String getNames(Integer limit, Integer offset, boolean ascending) throws Exception {
        List<String> queryNames = queryTemplateDao.getNames(limit, offset, ascending);

        JSONObject jsonObject = new JSONObject()
                .put("queryTemplateNames", queryNames);

        return jsonObject.toString();
    }
}
