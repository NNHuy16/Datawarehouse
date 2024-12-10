package com.example.server_staging;

import com.example.server_staging.configuration.ConnectorFactory;
import com.example.server_staging.dao.ConfigDAO;
import com.example.server_staging.dao.LogDAO;
import com.example.server_staging.dao.ProcessDAO;
import com.example.server_staging.dao.StagingDAO;
import com.example.server_staging.entity.Log;
import com.example.server_staging.entity.Process;
import com.example.server_staging.service.EmailService;
import org.jdbi.v3.core.Handle;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;

public class Main {

    static LogDAO logDAO;
    static ProcessDAO processDAO;
    static StagingDAO stagingDAO;
    static ConfigDAO configDAO;

    static {
        logDAO = LogDAO.getInstance();
        processDAO = ProcessDAO.getInstance();
        stagingDAO = StagingDAO.getInstance();
        configDAO = ConfigDAO.getInstance();
    }

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        // Kiểm tra trạng thái của process staging
        Process process = processDAO.findLast("staging");
        Log log = new Log();
        System.out.println("Checking process status...");

        // Nếu process không sẵn sàng hoặc không tồn tại, cập nhật log
        if (process == null || !process.getStatus().equals("READY")) {
            log.setProcess(process);  // Gán process vào log
            log.setMessage("staging process not ready today");
            log.setLevel("warn");
            logDAO.insert(log); // Ghi log vào cơ sở dữ liệu
            System.out.println("Staging process is not ready today, log has been updated.");
        } else {
            // Tạo đường dẫn động cho file theo ngày hiện tại
            Date targetDate = new Date(System.currentTimeMillis());
            String filePath = configDAO.generateFilePath(targetDate); // Lấy đường dẫn file từ configDAO
            System.out.println("Generated file path: " + filePath);

            // Kiểm tra sự tồn tại và khả năng đọc của file
            if (!readDataFromFile(filePath)) {
                System.out.println("Error: File not found or unreadable: " + filePath);

                // Cập nhật trạng thái của process thành FAILED
                if (process != null) {
                    process.setStatus("FAILED");
                    processDAO.update(process);  // Cập nhật process với trạng thái FAILED
                }

                // Cập nhật log với thông báo lỗi
                log.setProcess(process);  // Gán process vào log
                log.setMessage("No valid data today");
                log.setLevel("error");
                logDAO.insert(log); // Ghi log vào cơ sở dữ liệu

                // Ném exception để dừng chương trình
                throw new RuntimeException("Load data into temp_staging table failed. File not found or unreadable: " + filePath);
            }

            // Nếu file hợp lệ, gọi thủ tục LoadDataIntoTempStaging để load dữ liệu vào bảng temp_staging
//            9. Load dữ liệu từ file lên bảng tạm temp_staging.
            configDAO.loadDataIntoTempStaging(targetDate);
            System.out.println("Step 9: Data loaded into temp_staging table.");

//            11. clean
            configDAO.cleanTempStagingData();

//            insert vào staging
            // 12. Gọi thủ tục MoveDataToStagingProducts
            try {
                stagingDAO.callMoveDataToStagingProcedure();
                System.out.println("Step 12: Data moved to staging products.");

                // Gửi email khi thành công
                String successMsg = "Data has been successfully moved to staging products.";
                EmailService.sendEmail("Load data from file to staging", successMsg);
            } catch (SQLException e) {
                e.printStackTrace();
                // Ghi log và cập nhật trạng thái nếu có lỗi
                log.setProcess(process);
                log.setMessage("Error moving data to staging: " + e.getMessage());
                log.setLevel("error");
                logDAO.insert(log);

                // Gửi email khi có lỗi
                String errorMsg = "Error occurred while moving data to staging: " + e.getMessage();
                EmailService.sendEmail("Load data from file to staging", errorMsg);
                /**
                 * Cập nhật Process SUCCESS
                 * Insert Log
                 */
                process.setStatus("SUCCESS");
                processDAO.update(process);
                logDAO.insert(log);

                /**
                 * Insert Process READY cho DataMart
                 * Insert Log
                 */
                processDAO.insertNextProcess(1, "staging");
            }

            }
    }

    // Phương thức kiểm tra sự tồn tại và khả năng đọc file
    private static boolean readDataFromFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.canRead();
    }


}
