package org.example.dao;

import org.example.configuration.ConnectorFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Call;
import org.jdbi.v3.core.statement.OutParameters;

public class WarehouseDAO {

    private static WarehouseDAO instance;
    private final Jdbi jdbi;

    public static WarehouseDAO getInstance() {
        if (instance == null) {
            instance = new WarehouseDAO(ConnectorFactory.datawarehouse());
        }
        return instance;
    }

    public WarehouseDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Long loadToWarehouse(String procedure) {
        return jdbi.withHandle(handle -> {
            String sql = "{CALL " + procedure + "()}";

            try (Call call = handle.createCall(sql)) {
                OutParameters result = call.invoke();

                return result.getResultSet().mapTo(Long.class)
                        .one();
            } catch (Exception e) {
                throw e;
            }
        });
    }
}