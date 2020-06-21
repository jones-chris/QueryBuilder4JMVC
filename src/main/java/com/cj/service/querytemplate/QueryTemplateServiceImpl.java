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
    public SelectStatement findByName(String name) {
        return queryTemplateDao.getQueryTemplateByName(name);
    }

    @Override
    public List<String> getNames(Integer limit, Integer offset, boolean ascending) throws Exception {
         return queryTemplateDao.getNames(limit, offset, ascending);
    }
}
