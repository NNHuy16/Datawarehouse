package org.example.dao;

import org.example.configuration.ConnectorFactory;
import org.jdbi.v3.core.Jdbi;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class StagingDAO {

    private static StagingDAO instance;
    private final Jdbi jdbi;

    public static StagingDAO getInstance() {
        if (instance == null) {
            instance = new StagingDAO(ConnectorFactory.staging());
        }
        return instance;
    }

    public StagingDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    // Phương thức gọi thủ tục MoveDataToStagingProducts
    // Phương thức gọi thủ tục MoveDataToStagingProducts
    public void callMoveDataToStagingProcedure() throws SQLException {
        String callProcedureSQL = "{CALL MoveDataToStagingProducts()}";

        try (Connection connection = jdbi.open().getConnection();
             CallableStatement callableStatement = connection.prepareCall(callProcedureSQL)) {
            callableStatement.execute();
            System.out.println("Stored procedure MoveDataToStagingProducts executed successfully.");
        } catch (SQLException e) {
            System.out.println("Error executing stored procedure MoveDataToStagingProducts: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error executing stored procedure MoveDataToStagingProducts.", e);
        }
    }
}
