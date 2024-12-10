package org.example.dao;


import org.example.configuration.ConnectorFactory;
import org.jdbi.v3.core.Jdbi;

import java.sql.*;

public class ConfigDAO {

    private static ConfigDAO instance;
    private final Jdbi jdbi;

    public static ConfigDAO getInstance() {
        if (instance == null) {
            instance = new ConfigDAO(ConnectorFactory.controller());
        }
        return instance;
    }

    public ConfigDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }


//    7. Tạo đường dẫn động cho file theo ngày hiện tại
    public String generateFilePath(Date targetDate) {
        String sql = "{CALL GenerateFilePath(?, ?)}"; // Thủ tục với 2 tham số: 1 IN, 1 OUT
        return jdbi.withHandle(handle -> {
            try (Connection conn = handle.getConnection();
                 CallableStatement stmt = conn.prepareCall(sql)) {

                // Đặt tham số IN
                stmt.setDate(1, targetDate);

                // Đăng ký tham số OUT, với chỉ định là VARCHAR
                stmt.registerOutParameter(2, java.sql.Types.VARCHAR);

                // Thực thi thủ tục
                stmt.execute();

                // Lấy kết quả từ tham số OUT
                return stmt.getString(2);
            } catch (SQLException e) {
                e.printStackTrace(); // Log chi tiết lỗi
                throw new RuntimeException("Error while calling the stored procedure", e);
            }
        });
    }

    /**
     * Gọi thủ tục LoadDataIntoTempStaging để load dữ liệu vào bảng temp_staging.
     *
     * @param targetDate Ngày để gọi thủ tục và load dữ liệu.
     */
    public void loadDataIntoTempStaging(Date targetDate) {
        String sql = "{CALL LoadDataIntoTempStaging(?)}"; // Thủ tục với 1 tham số IN

        jdbi.withHandle(handle -> {
            try (Connection conn = handle.getConnection();
                 CallableStatement stmt = conn.prepareCall(sql)) {

                // Đặt tham số IN
                stmt.setDate(1, targetDate);

                // Thực thi thủ tục
                try (ResultSet resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        // Lấy câu lệnh SQL động từ kết quả thủ tục
                        String dynamicSql = resultSet.getString("dynamic_sql");
                        System.out.println("Generated dynamic SQL: " + dynamicSql);

                        // Thực thi câu lệnh SQL động
                        executeDynamicSql(conn, dynamicSql);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace(); // Log chi tiết lỗi
                throw new RuntimeException("Error while calling LoadDataIntoTempStaging procedure", e);
            }
            return null; // No return value from the procedure
        });
    }

    /**
     * Thực thi câu lệnh SQL động được tạo từ thủ tục.
     *
     * @param conn       Kết nối cơ sở dữ liệu.
     * @param dynamicSql Câu lệnh SQL động để thực thi.
     */
    private void executeDynamicSql(Connection conn, String dynamicSql) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall(dynamicSql)) {
            // Thực thi câu lệnh SQL động
            stmt.execute();
            System.out.println("Data loaded successfully into temp_staging table.");
        } catch (SQLException e) {
            e.printStackTrace(); // Log chi tiết lỗi
            throw new SQLException("Error executing dynamic SQL", e);
        }
    }

//  12. Xử lý dữ liệu trong bảng tạm.
    public void cleanTempStagingData() {
        String procedureCall = "{CALL CleanTempStagingData()}";

        jdbi.withHandle(handle -> {
            try {
                // Sử dụng createCall để gọi thủ tục
                handle.createCall(procedureCall).invoke();
                System.out.println("Stored procedure `CleanTempStagingData` executed successfully.");
            } catch (Exception e) {
                System.out.println("Error executing stored procedure `CleanTempStagingData`: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error executing stored procedure `CleanTempStagingData`.", e);
            }
            return null; // Không cần trả về giá trị.
        });
    }
    /**
     * Gọi thủ tục `CleanTempStaging` để xóa toàn bộ dữ liệu trong bảng temp_staging.
     */
    public void deleteTempStaging() {
        String procedureCall = "{CALL DeleteTempStaging()}";

        jdbi.withHandle(handle -> {
            try (Connection conn = handle.getConnection();
                 CallableStatement stmt = conn.prepareCall(procedureCall)) {

                // Thực thi thủ tục
                stmt.execute();
                System.out.println("Stored procedure `DeleteTempStaging` executed successfully.");

            } catch (SQLException e) {
                System.out.println("Error executing stored procedure `DeleteTempStaging`: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error executing stored procedure `DeleteTempStaging`.", e);
            }
            return null; // Không có giá trị trả về từ thủ tục.
        });
    }


}
