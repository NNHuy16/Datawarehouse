package org.example.dao;

import org.example.configuration.ConnectorFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Call;

public class DatamartDAO {

    private static DatamartDAO instance;
    private final Jdbi jdbi;

    public static DatamartDAO getInstance() {
        if (instance == null) {
            instance = new DatamartDAO(ConnectorFactory.datamart());
        }
        return instance;
    }

    public DatamartDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void loadToDatamart() {
        jdbi.useHandle(handle -> {
            String sql = "{CALL load_to_datamart()}"; // Your stored procedure call

            try (Call call = handle.createCall(sql)) {
                // Execute the stored procedure
                call.invoke();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
    }
}