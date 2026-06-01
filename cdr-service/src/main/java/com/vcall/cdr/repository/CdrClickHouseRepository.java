package com.vcall.cdr.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CdrClickHouseRepository {

    @Qualifier("clickHouseJdbcTemplate")
    private final JdbcTemplate clickHouseJdbcTemplate;

    public List<Map<String, Object>> getCallVolumeByHour(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT
                toHour(start_time) AS hour,
                count() AS volume,
                countIf(status = 'ANSWERED') AS answered,
                countIf(status != 'ANSWERED') AS missed,
                avg(duration) AS avgDuration
            FROM cdr_records
            WHERE start_time >= ? AND start_time <= ?
            GROUP BY hour
            ORDER BY hour
            """;
        return clickHouseJdbcTemplate.query(sql, new MapRowMapper(), startDate, endDate);
    }

    public List<Map<String, Object>> getCallVolumeByDay(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT
                toDate(start_time) AS day,
                count() AS volume,
                countIf(status = 'ANSWERED') AS answered,
                countIf(status != 'ANSWERED') AS missed,
                avg(duration) AS avgDuration
            FROM cdr_records
            WHERE start_time >= ? AND start_time <= ?
            GROUP BY day
            ORDER BY day
            """;
        return clickHouseJdbcTemplate.query(sql, new MapRowMapper(), startDate, endDate);
    }

    public List<Map<String, Object>> getAgentPerformanceSummary(UUID agentId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT
                agent_id,
                count() AS totalCalls,
                countIf(status = 'ANSWERED') AS answeredCalls,
                sum(duration) AS totalDuration,
                avg(duration) AS avgDuration,
                max(duration) AS maxDuration
            FROM cdr_records
            WHERE agent_id = ?
              AND start_time >= ? AND start_time <= ?
            GROUP BY agent_id
            """;
        return clickHouseJdbcTemplate.query(sql, new MapRowMapper(), agentId.toString(), startDate, endDate);
    }

    public List<Map<String, Object>> getCostByTenant(UUID tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT
                tenant_id,
                currency,
                sum(cost) AS totalCost,
                count() AS totalCalls,
                avg(rate) AS avgRate
            FROM cdr_records
            WHERE tenant_id = ?
              AND start_time >= ? AND start_time <= ?
            GROUP BY tenant_id, currency
            """;
        return clickHouseJdbcTemplate.query(sql, new MapRowMapper(), tenantId.toString(), startDate, endDate);
    }

    public List<Map<String, Object>> getConcurrentCalls(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT
                toStartOfMinute(start_time) AS minute,
                count() AS concurrentCalls
            FROM cdr_records
            WHERE start_time >= ? AND start_time <= ?
              AND status = 'ANSWERED'
            GROUP BY minute
            ORDER BY concurrentCalls DESC
            LIMIT 100
            """;
        return clickHouseJdbcTemplate.query(sql, new MapRowMapper(), startDate, endDate);
    }

    private static class MapRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> row = new HashMap<>();
            var meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            return row;
        }
    }
}
