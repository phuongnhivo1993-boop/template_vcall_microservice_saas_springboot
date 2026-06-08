package com.vcall.agent.controller;

import com.vcall.agent.dto.AgentSessionRequest;
import com.vcall.agent.dto.AgentSessionResponse;
import com.vcall.agent.service.AgentSessionService;
import com.vcall.common.dto.ApiResponse;
import com.vcall.common.util.CsvExportUtil;
import com.vcall.common.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/agent-sessions")
@RequiredArgsConstructor
public class AgentSessionController {

    private final AgentSessionService agentSessionService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AgentSessionResponse>> startSession(@Valid @RequestBody AgentSessionRequest request) {
        AgentSessionResponse response = agentSessionService.startSession(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session started successfully", response));
    }

    @PutMapping("/{id}/end")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AgentSessionResponse>> endSession(@PathVariable Long id) {
        AgentSessionResponse response = agentSessionService.endSession(id);
        return ResponseEntity.ok(ApiResponse.success("Session ended successfully", response));
    }

    @GetMapping("/active/{agentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<AgentSessionResponse>> getActiveSession(@PathVariable UUID agentId) {
        AgentSessionResponse response = agentSessionService.getActiveSession(agentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<Page<AgentSessionResponse>>> searchSessions(
            @RequestParam(required = false) UUID agentId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable) {
        Specification<com.vcall.agent.entity.AgentSession> spec = Specification.where(null);
        if (agentId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("agent").get("id"), agentId));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("loginTime"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("loginTime"), endDate));
        }
        Page<AgentSessionResponse> response = agentSessionService.searchSessions(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportSessionsCsv(@RequestParam(required = false) UUID agentId,
                                  @RequestParam(required = false) LocalDateTime startDate,
                                  @RequestParam(required = false) LocalDateTime endDate,
                                  HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("loginTime").descending());
        Specification<com.vcall.agent.entity.AgentSession> spec = Specification.where(null);
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("agent").get("id"), agentId));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("loginTime"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("loginTime"), endDate));
        }
        Page<AgentSessionResponse> sessions = agentSessionService.searchSessions(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Agent ID", "Login Time", "Logout Time", "Duration", "Session Type");
        List<List<String>> rows = CsvExportUtil.toRows(sessions.getContent(),
                Arrays.asList("id", "agentId", "loginTime", "logoutTime", "duration", "sessionType"));
        CsvExportUtil.writeCsv(response, "agent-sessions.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    public void exportSessionsExcel(@RequestParam(required = false) UUID agentId,
                                    @RequestParam(required = false) LocalDateTime startDate,
                                    @RequestParam(required = false) LocalDateTime endDate,
                                    HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("loginTime").descending());
        Specification<com.vcall.agent.entity.AgentSession> spec = Specification.where(null);
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("agent").get("id"), agentId));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("loginTime"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("loginTime"), endDate));
        }
        Page<AgentSessionResponse> sessions = agentSessionService.searchSessions(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Agent ID", "Login Time", "Logout Time", "Duration", "Session Type");
        ExcelExportUtil.writeExcel(response, "agent-sessions.xlsx", headers, sessions.getContent(),
                Arrays.asList("id", "agentId", "loginTime", "logoutTime", "duration", "sessionType"));
    }
}
