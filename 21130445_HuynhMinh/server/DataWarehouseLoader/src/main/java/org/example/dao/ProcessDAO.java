package org.example.dao;

import org.example.configuration.ConnectorFactory;
import org.example.entity.Process;
import org.example.mapper.ProcessMapper;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Call;

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
    public Process findLast(String processAt) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT * FROM process p
                                JOIN configs AS c ON p.config_id = c.id
                                WHERE p.process_at = :processAt
                                	AND p.status != 'FAILED'
                                	AND c.is_active = 1
                                ORDER BY p.update_date DESC
                                LIMIT 1""")
                        .bind("processAt", processAt)
                        .map(new ProcessMapper())
                        .findOne().orElse(null));
    }

    /**
     * Insert Process
     *
     * @param process
     * @return
     */
    public Process insert(Process process) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("""
                                INSERT INTO process (config_id, process_at, status)
                                VALUES (:configId, :processAt, :status)""")
                        .bind("configId", process.getConfig().getId())
                        .bind("processAt", process.getProcessAt())
                        .bind("status", process.getStatus())
                        .executeAndReturnGeneratedKeys("id") // Lấy ID tự động sinh
                        .mapTo(Integer.class) // Map kết quả về kiểu Integer (giả sử ID là Long)
                        .findOne() // Lấy ID đầu tiên (vì chỉ có một bản ghi được thêm)
                        .map(id -> {
                            process.setId(id); // Cập nhật ID vào đối tượng Process
                            return process;
                        })
                        .orElseThrow(() -> new IllegalStateException("Insert failed!")));
    }

    /**
     * Insert Process tiếp theo với status READY
     * @param configId
     * @param processAt
     */
    public void insertNextProcess(int configId, String processAt) {
        jdbi.useHandle(handle -> {
            // Call the stored procedure insert_next_process
            handle.createCall("CALL insert_next_process(?, ?)")
                    .bind(0, configId)
                    .bind(1, processAt)
                    .invoke();
        });
    }
}
