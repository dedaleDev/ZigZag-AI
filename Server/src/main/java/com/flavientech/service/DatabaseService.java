package com.flavientech.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import com.flavientech.PathChecker;

import org.springframework.beans.factory.InitializingBean;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class DatabaseService implements InitializingBean {

    @Autowired
    private DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        checkAndLoadTables();
    }
    
    private void checkAndLoadTables() {
        if (!areTablesPresent() || !areColumnsValid()) {
            loadSqlScript();
        }
    }

    private boolean areTablesPresent() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "flashmemory", null);
            if (!resultSet.next()) {
                return false;
            }
            resultSet = metaData.getTables(null, null, "longmemory", null);
            if (!resultSet.next()) {
                return false;
            }
            resultSet = metaData.getTables(null, null, "user", null);
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean areColumnsValid() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return checkTableColumns(metaData, "flashmemory", new String[]{"Id", "Request", "Answer"}) &&
                   checkTableColumns(metaData, "longmemory", new String[]{"Id", "User", "Date", "Résumé"}) &&
                   checkTableColumns(metaData, "user", new String[]{"Id", "Username"});
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkTableColumns(DatabaseMetaData metaData, String tableName, String[] columns) throws SQLException {
        ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
        int columnCount = 0;
        while (resultSet.next()) {
            String columnName = resultSet.getString("COLUMN_NAME");
            for (String column : columns) {
                if (column.equalsIgnoreCase(columnName)) {
                    columnCount++;
                    break;
                }
            }
        }
        return columnCount == columns.length;
    }

    private void loadSqlScript() {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(PathChecker.checkPath("zigzag.sql")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}