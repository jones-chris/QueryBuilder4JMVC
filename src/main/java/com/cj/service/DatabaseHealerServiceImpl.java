package com.cj.service;

import com.cj.dao.DatabaseHealerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseHealerServiceImpl implements DatabaseHealerService {
    @Autowired
    private DatabaseHealerDao databaseHealerDao;

    @Override
    public boolean dropDatabase() {
        return databaseHealerDao.dropDatabase();
    }

    @Override
    public boolean createDatabase() {
        return databaseHealerDao.createDatabase();
    }

    @Override
    public boolean dropTable() {
        return databaseHealerDao.dropTable();
    }

    @Override
    public boolean dropAllTablesExcept(String tableNotToDrop) {
        return databaseHealerDao.dropAllTablesExcept(tableNotToDrop);
    }

    @Override
    public boolean createTable() {
        return databaseHealerDao.createTable();
    }

    @Override
    public boolean insertTestData() {
        return databaseHealerDao.insertTestData();
    }

    @Override
    public boolean healDatabaseEntirely() {
        return databaseHealerDao.healDatabaseEntirely();
    }
}
