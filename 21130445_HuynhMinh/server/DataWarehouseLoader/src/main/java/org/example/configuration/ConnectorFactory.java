package org.example.configuration;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.example.service.EmailService;
import org.jdbi.v3.core.Jdbi;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.time.LocalDateTime;

public class ConnectorFactory {

    private static Jdbi controller, datawarehouse;

    /**
     * 2. Kết nối DB
     */
    private static Jdbi createConnection(String host, String port, String dbName, String username, String password) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        try {
            dataSource.setAutoReconnect(true);
            dataSource.setUseCompression(true);

            Jdbi jdbi = Jdbi.create(dataSource);
            jdbi.useHandle(handle -> handle.execute("SELECT 1"));

            return jdbi;
        } catch (Exception e) {
            /**
             * 3. Notify "Can not connect"
             */
            EmailService.sendEmail("Load To DataWarehouse Process Failed", "Can not connect to " + dbName, LocalDateTime.now());
            return null;
        }
    }

    /**
     * Tạo kết nối tới db_controller
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
     * Tạo kết nối tới db_datawarehouse
     *
     * @return
     */
    public static Jdbi datawarehouse() {
        if (datawarehouse == null) {
            datawarehouse = createConnection(
                    DBProperties.dbDatawarehouseHost,
                    DBProperties.dbDatawarehousePort,
                    DBProperties.dbDatawarehouse,
                    DBProperties.dbDatawarehouseUsername,
                    DBProperties.dbDatawarehousePassword
            );
        }
        return datawarehouse;
    }

    private ConnectorFactory() {

    }
}
