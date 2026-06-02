package com.vcall.cdr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vcall.cdr.dto.CdrImportRequest;
import com.vcall.cdr.entity.CdrImportLog;
import com.vcall.cdr.entity.CdrRecord;
import com.vcall.cdr.repository.CdrImportLogRepository;
import com.vcall.cdr.repository.CdrRecordRepository;
import com.vcall.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CdrImportService {

    private final CdrRecordRepository cdrRecordRepository;
    private final CdrImportLogRepository cdrImportLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public CdrImportLog importFromFile(CdrImportRequest request) {
        CdrImportLog importLog = new CdrImportLog();
        importLog.setFileName(request.getFileUrl());
        importLog.setImportedAt(LocalDateTime.now());
        importLog.setStatus(CdrImportLog.ImportStatus.PROCESSING);
        importLog.setTotalRecords(0);
        importLog.setSuccessCount(0);
        importLog.setFailedCount(0);
        importLog = cdrImportLogRepository.save(importLog);

        try {
            List<CdrRecord> records = parseFile(request);
            importLog.setTotalRecords(records.size());

            int successCount = 0;
            int failedCount = 0;
            List<String> errors = new ArrayList<>();

            for (CdrRecord record : records) {
                try {
                    validateCdrData(record);
                    cdrRecordRepository.save(record);
                    successCount++;
                } catch (Exception e) {
                    failedCount++;
                    errors.add("Error processing record: " + e.getMessage());
                }
            }

            importLog.setSuccessCount(successCount);
            importLog.setFailedCount(failedCount);
            importLog.setStatus(failedCount > 0 ? CdrImportLog.ImportStatus.COMPLETED : CdrImportLog.ImportStatus.COMPLETED);
            if (!errors.isEmpty()) {
                importLog.setErrorLog(String.join("\n", errors));
            }
        } catch (Exception e) {
            log.error("Import failed for file: {}", request.getFileUrl(), e);
            importLog.setStatus(CdrImportLog.ImportStatus.FAILED);
            importLog.setErrorLog(e.getMessage());
        }

        return cdrImportLogRepository.save(importLog);
    }

    private List<CdrRecord> parseFile(CdrImportRequest request) throws Exception {
        String format = request.getFileFormat().toUpperCase();
        return switch (format) {
            case "CSV" -> parseCsv(request.getFileUrl());
            case "JSON" -> parseJson(request.getFileUrl());
            case "SIP_CDR" -> parseSipCdr(request.getFileUrl());
            default -> throw new BadRequestException("Unsupported file format: " + request.getFileFormat());
        };
    }

    private List<CdrRecord> parseCsv(String fileUrl) throws Exception {
        List<CdrRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(URI.create(fileUrl).toURL().openStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return records;

            String[] headers = headerLine.split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                CdrRecord record = mapCsvRowToRecord(headers, values);
                if (record != null) {
                    records.add(record);
                }
            }
        }
        return records;
    }

    private CdrRecord mapCsvRowToRecord(String[] headers, String[] values) {
        CdrRecord record = new CdrRecord();
        record.setCallId(UUID.randomUUID().toString());
        record.setDirection(CdrRecord.Direction.INBOUND);
        record.setStatus(CdrRecord.Status.ANSWERED);
        record.setTenantId(UUID.randomUUID());
        for (int i = 0; i < Math.min(headers.length, values.length); i++) {
            String header = headers[i].trim().toLowerCase();
            String value = values[i].trim();
            if (value.isEmpty()) continue;
            switch (header) {
                case "caller_number" -> record.setCallerNumber(value);
                case "callee_number" -> record.setCalleeNumber(value);
                case "caller_name" -> record.setCallerName(value);
                case "direction" -> {
                    try {
                        record.setDirection(CdrRecord.Direction.valueOf(value.toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                }
                case "status" -> {
                    try {
                        record.setStatus(CdrRecord.Status.valueOf(value.toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                }
                case "duration" -> record.setDuration(Integer.parseInt(value));
                case "wait_duration" -> record.setWaitDuration(Integer.parseInt(value));
                case "cost" -> record.setCost(new BigDecimal(value));
                case "rate" -> record.setRate(new BigDecimal(value));
                case "currency" -> record.setCurrency(value);
                case "tenant_id" -> record.setTenantId(UUID.fromString(value));
            }
        }
        return record;
    }

    private List<CdrRecord> parseJson(String fileUrl) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JsonNode root = mapper.readTree(URI.create(fileUrl).toURL());
        List<CdrRecord> records = new ArrayList<>();
        if (root.isArray()) {
            for (JsonNode node : root) {
                CdrRecord record = new CdrRecord();
                record.setCallId(node.has("callId") ? node.get("callId").asText() : UUID.randomUUID().toString());
                record.setCallerNumber(node.has("callerNumber") ? node.get("callerNumber").asText() : null);
                record.setCalleeNumber(node.has("calleeNumber") ? node.get("calleeNumber").asText() : null);
                record.setCallerName(node.has("callerName") ? node.get("callerName").asText() : null);
                if (node.has("direction")) {
                    record.setDirection(CdrRecord.Direction.valueOf(node.get("direction").asText().toUpperCase()));
                }
                if (node.has("status")) {
                    record.setStatus(CdrRecord.Status.valueOf(node.get("status").asText().toUpperCase()));
                }
                if (node.has("duration")) record.setDuration(node.get("duration").asInt());
                if (node.has("cost")) record.setCost(new BigDecimal(node.get("cost").asText()));
                if (node.has("rate")) record.setRate(new BigDecimal(node.get("rate").asText()));
                if (node.has("tenantId")) record.setTenantId(UUID.fromString(node.get("tenantId").asText()));
                records.add(record);
            }
        }
        return records;
    }

    private List<CdrRecord> parseSipCdr(String fileUrl) throws Exception {
        List<CdrRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(URI.create(fileUrl).toURL().openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                CdrRecord record = new CdrRecord();
                record.setCallId(UUID.randomUUID().toString());
                record.setDirection(CdrRecord.Direction.INBOUND);
                record.setStatus(CdrRecord.Status.ANSWERED);
                record.setTenantId(UUID.randomUUID());

                if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    for (String part : parts) {
                        String[] kv = part.split("=", 2);
                        if (kv.length == 2) {
                            String key = kv[0].trim().toLowerCase();
                            String val = kv[1].trim();
                            switch (key) {
                                case "caller" -> record.setCallerNumber(val);
                                case "callee" -> record.setCalleeNumber(val);
                                case "duration" -> record.setDuration(Integer.parseInt(val));
                                case "status" -> {
                                    try {
                                        record.setStatus(CdrRecord.Status.valueOf(val.toUpperCase()));
                                    } catch (IllegalArgumentException ignored) {}
                                }
                            }
                        }
                    }
                }
                records.add(record);
            }
        }
        return records;
    }

    public void validateCdrData(CdrRecord record) {
        if (record.getTenantId() == null) {
            throw new BadRequestException("Tenant ID is required");
        }
        if (record.getCallId() == null || record.getCallId().isEmpty()) {
            throw new BadRequestException("Call ID is required");
        }
        if (record.getDirection() == null) {
            throw new BadRequestException("Direction is required");
        }
        if (record.getStatus() == null) {
            throw new BadRequestException("Status is required");
        }
    }

    @Transactional(readOnly = true)
    public CdrImportLog getImportStatus(Long id) {
        return cdrImportLogRepository.findById(id)
                .orElseThrow(() -> new com.vcall.common.exception.ResourceNotFoundException("Import log not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<CdrImportLog> getImportHistory(Pageable pageable) {
        return cdrImportLogRepository.findAll(pageable);
    }
}
