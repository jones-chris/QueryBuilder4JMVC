package com.cj.service.querytemplate;

import com.cj.dao.querytemplate.QueryTemplateDao;
import com.cj.model.select_statement.SelectStatement;
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
        return queryTemplateDao.findByName(name);
    }

    @Override
    public List<String> getNames(Integer limit, Integer offset, boolean ascending) throws Exception {
         return queryTemplateDao.listNames(limit, offset, ascending);
    }
}
