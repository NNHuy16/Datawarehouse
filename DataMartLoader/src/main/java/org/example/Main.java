package org.example;

import org.example.configuration.ConnectorFactory;
import org.example.configuration.DBProperties;
import org.example.dao.ConfigDAO;
import org.example.dao.DatamartDAO;
import org.example.dao.LogDAO;
import org.example.dao.ProcessDAO;
import org.example.entity.Log;
import org.example.entity.Process;
import org.example.service.EmailService;

import java.time.LocalDateTime;

public class Main {

    static LogDAO logDAO;
    static ProcessDAO processDAO;
    static ConfigDAO configDAO;
    static DatamartDAO datamartDAO;

    /**
     * 1. Load config from DB.properties
     *
     * 3.connect Database.
     */
    static {
        logDAO = LogDAO.getInstance();
        processDAO = ProcessDAO.getInstance();
        configDAO = ConfigDAO.getInstance();
        datamartDAO = DatamartDAO.getInstance();
    }

    public static void main(String[] args) {
        if (ConnectorFactory.controller() == null || ConnectorFactory.datamart() == null) {
            return;
        }

        run();
    }

    public static void run() {
        /**
         * 4. Find last Datamart process
         */
        Process process = processDAO.findLast("datamart");
        Log log = new Log();

        /**
         * 5. Check Process
         */
        if (process == null) {
            /**
             * 6. Check Datamart config
             */
            if (configDAO.findLast("Datamart") == null) {
                /**
                 * 7. Notify "No Config found"
                 */
                log.setMessage("No Config found.");
                EmailService.sendEmail("Load To Datamart Process Failed", "No Config found.", LocalDateTime.now());
            } else {
                /**
                 * 8. Notify "READY not found"
                 */
                log.setMessage("READY Datamart process not found");
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
                msg = "A Datamart process is already running";
                break;

            case "ready":
                log.setMessage("Starting load to Datamart process");
                log.setLevel("info");
                logDAO.insert(log);
                loadToDatamart(process);
                return;
            default:
                msg = "READY Datamart process not found";
        }

        log.setMessage(msg);
        log.setLevel("warn");
        logDAO.insert(log);
        EmailService.sendEmail("Load To Datamart Process Failed", msg, process.getBeginDate().toLocalDateTime());
    }

    private static void loadToDatamart(Process process) {
        Log log = new Log();
        log.setProcess(process);
        log.setLevel("info");

        /**
         * 11. Update Process running
         */
        process.setStatus("RUNNING");
        processDAO.update(process);


        /**
         * 12. Load to Datamart
         */
        try {
            datamartDAO.loadToDatamart();
        } catch (Exception e) {
            /**
             * 13. Notify Failed
             */
            String msgSpecial = "PROCEDURE " + DBProperties.dbDatamart + "." + "load_to_datamart" + " does not exists";
            if (e.getMessage().contains("java.sql.SQLSyntaxErrorException")) {
                log.setMessage("Failed! Error: " + msgSpecial);
                process.setStatus("READY");
                EmailService.sendEmail("Error in Datamart Procedure", "Error: " + msgSpecial, process.getBeginDate().toLocalDateTime());
            } else {
                log.setMessage("Failed! Error: " + e.getMessage());
                process.setStatus("FAILED");
                EmailService.sendEmail("Error in Datamart Procedure", "Error: " + e.getMessage(), process.getBeginDate().toLocalDateTime());
            }
            log.setLevel("warn");
            processDAO.update(process);
            logDAO.insert(log);

            return;
        }

        String msg = "Successfully loaded into Datamart";
        log.setMessage(msg);
        /**
         * 14. Cập nhật Process SUCCESS, Notify
         * Insert Log
         */
        process.setStatus("SUCCESS");
        processDAO.update(process);
        logDAO.insert(log);

        // Send email
        EmailService.sendEmail("Load to Datamart Process Success", msg,
                process.getBeginDate().toLocalDateTime());
    }
}