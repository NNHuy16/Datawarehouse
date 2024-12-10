package org.example.dao;

import org.example.configuration.ConnectorFactory;
import org.example.entity.Config;
import org.jdbi.v3.core.Jdbi;

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

    /**
     * Tìm Config mới nhất dựa theo process
     * crawl, staging, warehouse, datamart
     * @param processAt
     * @return Config
     */
    public Config findLast(String processAt) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT c.* FROM configs c
                                JOIN process AS p ON p.config_id = c.id
                                WHERE p.process_at = :processAt
                                	AND c.is_active = 1
                                ORDER BY c.update_date
                                LIMIT 1""")
                        .bind("processAt", processAt)
                        .mapToBean(Config.class)
                        .findOne()).orElse(null);
    }
}