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
