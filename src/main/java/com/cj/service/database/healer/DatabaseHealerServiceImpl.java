package com.cj.service.database.healer;

import com.cj.dao.database.healer.DatabaseHealerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseHealerServiceImpl implements DatabaseHealerService {

    private DatabaseHealerDao databaseHealerDao;

    @Autowired
    public DatabaseHealerServiceImpl(DatabaseHealerDao databaseHealerDao) {
        this.databaseHealerDao = databaseHealerDao;
    }

    @Override
    public boolean dropDatabase() {
        return databaseHealerDao.dropDatabase();
    }

    @Override
    public boolean createDatabase() {
        return databaseHealerDao.createDatabase();
    }

    @Override
    public boolean dropTable(String databaseName) {
        return databaseHealerDao.dropTable(databaseName);
    }

    @Override
    public boolean dropAllTablesExcept(String databaseName, String tableNotToDrop) {
        return databaseHealerDao.dropAllTablesExcept(databaseName, tableNotToDrop);
    }

    @Override
    public boolean createTable(String databaseName) {
        return databaseHealerDao.createTable(databaseName);
    }

    @Override
    public boolean insertTestData(String databaseName) {
        return databaseHealerDao.insertTestData(databaseName);
    }

    @Override
    public boolean healDatabaseEntirely(String databaseName) {
        return databaseHealerDao.healDatabaseEntirely(databaseName);
    }
}
