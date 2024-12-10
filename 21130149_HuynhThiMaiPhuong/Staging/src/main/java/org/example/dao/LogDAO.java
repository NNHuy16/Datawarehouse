package org.example.dao;

import org.example.configuration.ConnectorFactory;
import org.example.entity.Log;
import org.jdbi.v3.core.Jdbi;

public class LogDAO {
    private static LogDAO instance;
    private final Jdbi jdbi;

    public static LogDAO getInstance() {
        if (instance == null) {
            instance = new LogDAO(ConnectorFactory.controller());
        }
        return instance;
    }


    public LogDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /**
     * Insert Log
     * @param log
     * @return
     */
    public int insert(Log log) {
        Integer processId = log.getProcess() == null ? null : log.getProcess().getId();

        return jdbi.withHandle(handle ->
                handle.createUpdate("INSERT INTO logs (process_id, message, level) " +
                                "VALUES (:processId, :message, :level)")
                        .bind("processId", processId)
                        .bind("message", log.getMessage())
                        .bind("level", log.getLevel())
                        .execute()
        );
    }
}