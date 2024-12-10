package org.example.configuration;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.example.service.EmailService;
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
// 2. Kết nối database
    private static Jdbi createConnection(String host, String port, String dbName, String username, String password) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName+ "?allowLoadLocalInfile=true");
        dataSource.setUser(username);
        dataSource.setPassword(password);

        try {
            dataSource.setAutoReconnect(true);
            dataSource.setUseCompression(true);

            Jdbi jdbi = Jdbi.create(dataSource);
            jdbi.useHandle(handle -> handle.execute("SELECT 1"));

            return jdbi;
        } catch (Exception e) {
//            3. Gửi mail thông báo kết nối thất bại.
            EmailService.sendEmail("Load To Staging Process Failed", "Can not connect to " + dbName);
            return null;
        }
    }

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
