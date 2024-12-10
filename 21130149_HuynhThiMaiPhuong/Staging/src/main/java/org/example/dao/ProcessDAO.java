package org.example.dao;

import org.example.entity.Process;
import org.example.configuration.ConnectorFactory;
import org.example.mapper.ProcessMapper;
import org.jdbi.v3.core.Jdbi;
public class ProcessDAO {

    private static ProcessDAO instance;
    private final Jdbi jdbi;

    public static ProcessDAO getInstance() {
        if (instance == null) {
            instance = new ProcessDAO(ConnectorFactory.controller());
        }
        return instance;
    }


    public ProcessDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /**
     * Cập nhật Process
     *
     * @param process
     * @return
     */
    public int update(Process process) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("""
                                UPDATE process
                                SET process_at = :processAt, status = :status
                                WHERE id = :id""")
                        .bind("id", process.getId())
                        .bind("processAt", process.getProcessAt())
                        .bind("status", process.getStatus())
                        .execute()
        );
    }

    /**
     * Lấy ra Process mới nhất dựa theo processAt
     * crawl, staging, warehouse, datamart
     *
     * @param processAt
     * @return
     */
//    4. Kiểm tra process staging:status = READY
    public Process findLast(String processAt) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                            SELECT * FROM process p
                            JOIN configs AS c ON p.config_id = c.id
                            WHERE p.process_at = :processAt
                              AND p.status = 'READY'
                              AND DATE(p.update_date) = CURRENT_DATE
                              AND c.is_active = 1
                            ORDER BY p.update_date DESC
                            LIMIT 1""")
                        .bind("processAt", processAt)
                        .map(new ProcessMapper())
                        .findOne().orElse(null));
    }

    public void insertNextProcess(int configId, String processAt) {
        jdbi.useHandle(handle -> {
            // Call the stored procedure insert_next_process
            handle.createCall("CALL insert_next_process(?, ?)")
                    .bind(0, configId)
                    .bind(1, processAt)
                    .invoke();
        });
    }

    /**
     * Insert Process
     *
     * @param process
     * @return
     */

}
