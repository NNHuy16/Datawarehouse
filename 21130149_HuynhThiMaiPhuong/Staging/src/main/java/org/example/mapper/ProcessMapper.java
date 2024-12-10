package org.example.mapper;

import org.example.entity.Config;
import org.example.entity.Process;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProcessMapper implements RowMapper<Process> {
    @Override
    public Process map(ResultSet rs, StatementContext ctx) throws SQLException {
        Process process = new Process();

        process.setId(rs.getInt("p.id"));
        process.setProcessAt(rs.getString("p.process_at"));
        process.setStatus(rs.getString("p.status"));
        process.setBeginDate(rs.getTimestamp("p.begin_date"));
        process.setUpdateDate(rs.getTimestamp("p.update_date"));

        Config config = new Config();
        config.setId(rs.getInt("c.id"));
        config.setFileName(rs.getString("c.file_name"));
        config.setSourcePath(rs.getString("c.source_path"));
        config.setFileLocation(rs.getString("c.file_location"));
        config.setBackupPath(rs.getString("c.backup_path"));
        config.setWarehouseProcedure(rs.getString("c.warehouse_procedure"));
        config.setVersion(rs.getString("c.version"));
        config.setIsActive(rs.getBoolean("c.is_active"));
        config.setInsertDate(rs.getTimestamp("c.insert_date"));
        config.setUpdateDate(rs.getTimestamp("c.update_date"));

        process.setConfig(config);

        return process;
    }
}
