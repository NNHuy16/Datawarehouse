package org.example;

import org.example.configuration.ConnectorFactory;
import org.example.configuration.DBProperties;
import org.example.dao.ConfigDAO;
import org.example.dao.LogDAO;
import org.example.dao.ProcessDAO;
import org.example.dao.WarehouseDAO;
import org.example.entity.Config;
import org.example.entity.Log;
import org.example.entity.Process;
import org.example.service.EmailService;

import java.time.LocalDateTime;

public class Main {

    static LogDAO logDAO;
    static ProcessDAO processDAO;
    static ConfigDAO configDAO;
    static WarehouseDAO warehouseDAO;

    /**
     * 1. Load config from DB.properties
     */
    static {
        logDAO = LogDAO.getInstance();
        processDAO = ProcessDAO.getInstance();
        configDAO = ConfigDAO.getInstance();
        warehouseDAO = WarehouseDAO.getInstance();
    }

    public static void main(String[] args) {
        if (ConnectorFactory.controller() == null || ConnectorFactory.datawarehouse() == null) {
            return;
        }

        run();
    }

    public static void run() {
        /**
         * 4. Find last warehouse process
         */
        Process process = processDAO.findLast("warehouse");
        Log log = new Log();

        /**
         * 5. Check Process
         */
        if (process == null) {
            /**
             * 6. Check warehouse config
             */
            if (configDAO.findLast("warehouse") == null) {
                /**
                 * 7. Notify "No Config found"
                 */
                log.setMessage("No Config found.");
                EmailService.sendEmail("Load To DataWarehouse Process Failed", "No Config found.", LocalDateTime.now());
            } else {
                /**
                 * 8. Notify "READY not found"
                 */
                log.setMessage("READY warehouse process not found");
            }

            log.setLevel("warn");
            logDAO.insert(log);
            return;
        }

        String msg;
        log.setProcess(process);

        /**
         * 9. Check status Process
         */
        switch (process.getStatus().toLowerCase()) {
            /**
             * 10. Notify "Process is already running"
             */
            case "running":
                msg = "A warehouse process is already running";
                break;

            case "ready":
                log.setMessage("Starting load to DataWarehouse process");
                log.setLevel("info");
                logDAO.insert(log);
                loadToWarehouse(process);
                return;
            default:
                msg = "READY warehouse process not found";
        }

        log.setMessage(msg);
        log.setLevel("warn");
        logDAO.insert(log);
        EmailService.sendEmail("Load To DataWarehouse Process Failed", msg, process.getBeginDate().toLocalDateTime());
    }

    private static void loadToWarehouse(Process process) {
        Log log = new Log();
        log.setProcess(process);
        log.setLevel("info");

        /**
         * 11. Update Process running
         */
        Config config = process.getConfig();
        process.setStatus("RUNNING");
        processDAO.update(process);

        long totalLoaded;

        /**
         * 12. Load to Warehouse
         */
        try {
            totalLoaded = warehouseDAO.loadToWarehouse(config.getWarehouseProcedure());
        } catch (Exception e) {
            /**
             * 13. Notify Failed
             */
            String msgSpecial = "PROCEDURE " + DBProperties.dbDatawarehouse + "." + config.getWarehouseProcedure() + " does not exists";
            if (e.getMessage().contains("java.sql.SQLSyntaxErrorException")) {
                log.setMessage("Failed! Error: " + msgSpecial);
                process.setStatus("READY");
                EmailService.sendEmail("Error in Warehouse Procedure", "Error: " + msgSpecial, process.getBeginDate().toLocalDateTime());
            } else {
                log.setMessage("Failed! Error: " + e.getMessage());
                process.setStatus("FAILED");
                EmailService.sendEmail("Error in Warehouse Procedure", "Error: " + e.getMessage(), process.getBeginDate().toLocalDateTime());
            }
            log.setLevel("warn");
            processDAO.update(process);
            logDAO.insert(log);

            return;
        }

        String msg;
        if (totalLoaded > 0) {
            msg = "Successfully loaded " + totalLoaded + " records into DataWarehouse";
        } else {
            msg = "No new data loaded into DataWarehouse";
            log.setLevel("warn");
        }

        log.setMessage(msg);
        /**
         * 14. Cập nhật Process SUCCESS, Notify
         * Insert Log
         */
        process.setStatus("SUCCESS");
        processDAO.update(process);
        logDAO.insert(log);

        // Send email
        EmailService.sendEmail("Load to DataWarehouse Process Success", msg,
                process.getBeginDate().toLocalDateTime());

        /**
         * 15. Insert Process READY cho DataMart
         * Insert Log
         */
        processDAO.insertNextProcess(process.getConfig().getId(), process.getProcessAt());
    }
}