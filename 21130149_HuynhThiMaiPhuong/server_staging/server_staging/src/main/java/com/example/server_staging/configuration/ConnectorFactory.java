package com.example.server_staging.configuration;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.jdbi.v3.core.Jdbi;

import java.sql.SQLException;

public class ConnectorFactory {

    private static Jdbi controller, staging;

    /**
     * Tạo kết nối tới database
     *
     * @param host
     * @param port
     * @param dbName
     * @param username
     * @param password
     * @return
     */
    private static Jdbi createConnection(String host, String port, String dbName, String username, String password) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName+ "?allowLoadLocalInfile=true");
        dataSource.setUser(username);
        dataSource.setPassword(password);

        try {
            dataSource.setAutoReconnect(true);
            dataSource.setUseCompression(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error while configuring the data source: " + e.getMessage(), e);
        }

        return Jdbi.create(dataSource);
    }

    /**
     * 1.Tạo kết nối tới db_controller
     *
     * @return
     */
    public static Jdbi controller() {
        if (controller == null) {
            controller = createConnection(
                    DBProperties.dbControllerHost,
                    DBProperties.dbControllerPort,
                    DBProperties.dbController,
                    DBProperties.dbControllerUsername,
                    DBProperties.dbControllerPassword
            );
        }
        return controller;
    }

    /**
     * 2.Tạo kết nối tới db_Staging
     *
     * @return
     */
    public static Jdbi staging() {
        if (staging == null) {
            staging = createConnection(
                    DBProperties.dbStagingHost,
                    DBProperties.dbStagingPort,
                    DBProperties.dbStaging,
                    DBProperties.dbStagingUsername,
                    DBProperties.dbStagingPassword
            );
        }
        return staging;
    }

    private ConnectorFactory() {

    }
}
