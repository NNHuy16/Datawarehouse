package org.example;

import org.example.dao.ConfigDAO;
import org.example.dao.LogDAO;
import org.example.dao.ProcessDAO;
import org.example.dao.StagingDAO;
import org.example.entity.Log;
import org.example.entity.Process;
import org.example.service.EmailService;

import java.io.File;
import java.sql.Date;
import java.sql.SQLException;

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

//        4. Kiểm tra process staging: status = READY
        Process process = processDAO.findLast("staging");
        Log log = new Log();
        System.out.println("Checking process status...");

//        5. update logs: message: "staging process not ready today", level: warn
        if (process == null || !process.getStatus().equals("READY")) {
            log.setProcess(process);  // Gán process vào log
            log.setMessage("staging process not ready today");
            log.setLevel("warn");
            logDAO.insert(log);
            System.out.println("Staging process is not ready today, log has been updated.");
            return;
        } else {

            // 6. Cập nhật trạng thái của process thành RUNNING
            process.setStatus("RUNNING");
            processDAO.update(process);
            System.out.println("Process status updated to RUNNING.");

//            7. Tạo đường dẫn động cho file theo ngày hiện tại
            Date targetDate = new Date(System.currentTimeMillis());
            String filePath = configDAO.generateFilePath(targetDate);
            System.out.println("Generated file path: " + filePath);

//            8. Kiểm tra sự tồn tại và đọc dữ liệu từ file theo ngày hiện tại
            if (!readDataFromFile(filePath)) {
                System.out.println("Error: File not found or unreadable: " + filePath);

//                9. Update process: status = FAILED, logs: message: "No valid data today"
                if (process != null) {
                    process.setStatus("FAILED");
                    processDAO.update(process);  // Cập nhật process với trạng thái FAILED
                }
                log.setProcess(process);
                log.setMessage("No valid data today");
                log.setLevel("error");
                logDAO.insert(log);

                // Ném exception để dừng chương trình
                throw new RuntimeException("Load data into temp_staging table failed. File not found or unreadable: " + filePath);
            }


            // 10. Load dữ liệu từ file lên bảng tạm
            try {
                configDAO.loadDataIntoTempStaging(targetDate);
                System.out.println("Step 10: Data loaded into temp_staging table.");
            } catch (Exception e) {
                System.out.println("Error: Loading data to temporary table failed.");
                e.printStackTrace();

//                11. Update process:status = FAILED, logs: message: "Loading data to temporary failed"
                process.setStatus("FAILED");
                processDAO.update(process);
                log.setProcess(process);
                log.setMessage("Loading data to temporary table failed: " + e.getMessage());
                log.setLevel("error");
                logDAO.insert(log);
                return;
            }


//            12. Xử lý dữ liệu trong bảng tạm.
            configDAO.cleanTempStagingData();

            // 13. Insert dữ liệu từ bảng tạm vào table staging_products
            try {
                stagingDAO.callMoveDataToStagingProcedure();
                System.out.println("13. Insert data from temp_staging to staging successfully.");

                // 14. Update process: status = SUCCESS, logs: "Load data into staging successfully"
                process.setStatus("SUCCESS");
                processDAO.update(process);
                log.setProcess(process);
                log.setMessage("Load data into staging successfully.");
                log.setLevel("info");
                logDAO.insert(log);

                // 15. Insert 1 dòng vào process: warehouse: READY
//                processDAO.insertNextProcess(1, "staging");
                processDAO.insertNextProcess(process.getConfig().getId(), process.getProcessAt());


//                16. Gửi mail thông báo: "Today's data has been successfully loaded into staging"
                String successMsg = "Today's data has been successfully loaded into staging.";
                EmailService.sendEmail("Load data from file to staging", successMsg);

                // 17. Xóa dữ liệu trong bảng tạm
                configDAO.deleteTempStaging();
                System.out.println("17.Delete Temporary data");
            } catch (SQLException e) {
                System.out.println("Error: Loading data to staging table failed.");
                e.printStackTrace();

                // 18. Update process: status = FAILED, logs: "Loading data to staging failed"
                process.setStatus("FAILED");
                processDAO.update(process);
                log.setProcess(process);
                log.setMessage("Loading data to staging failed: " + e.getMessage());
                log.setLevel("error");
                logDAO.insert(log);

//                19. Gửi mail thông báo: "Load data into staging failed"
                String errorMsg = "Load data into staging failed: " + e.getMessage();
                EmailService.sendEmail("Load data from file to staging", errorMsg);
            }
        }
    }
    // Phương thức kiểm tra sự tồn tại và khả năng đọc file
    private static boolean readDataFromFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.canRead();
    }


}
