package com.vcall.agent.controller;

import com.vcall.agent.dto.AgentRequest;
import com.vcall.agent.dto.AgentResponse;
import com.vcall.agent.dto.AgentStatusRequest;
import com.vcall.agent.dto.AgentStatusResponse;
import com.vcall.agent.entity.Agent.AgentStatusEnum;
import com.vcall.agent.entity.AgentGroupMember;
import com.vcall.agent.service.AgentService;
import com.vcall.agent.service.AgentStatusService;
import com.vcall.common.dto.ApiResponse;
import com.vcall.common.dto.BulkStatusRequest;
import com.vcall.common.util.BulkOperationUtil;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final AgentStatusService agentStatusService;

    @PostMapping
    public ResponseEntity<ApiResponse<AgentResponse>> createAgent(@Valid @RequestBody AgentRequest request) {
        AgentResponse response = agentService.createAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agent created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AgentResponse>>> getAllAgents(Pageable pageable) {
        Page<AgentResponse> agents = agentService.getAllAgents(pageable);
        return ResponseEntity.ok(ApiResponse.success(agents));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentResponse>> getAgent(@PathVariable UUID id) {
        AgentResponse response = agentService.getAgent(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentResponse>> updateAgent(@PathVariable UUID id,
                                                                   @Valid @RequestBody AgentRequest request) {
        AgentResponse response = agentService.updateAgent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Agent updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAgent(@PathVariable UUID id) {
        agentService.deleteAgent(id);
        return ResponseEntity.ok(ApiResponse.success("Agent deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AgentResponse>> updateStatus(@PathVariable UUID id,
                                                                    @Valid @RequestBody AgentStatusRequest request) {
        AgentResponse response = agentService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AgentResponse>>> getAgentsByStatus(@PathVariable String status) {
        AgentStatusEnum statusEnum = AgentStatusEnum.valueOf(status.toUpperCase());
        List<AgentResponse> agents = agentService.getByStatus(statusEnum);
        return ResponseEntity.ok(ApiResponse.success(agents));
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<List<AgentStatusResponse>>> getStatusHistory(@PathVariable UUID id) {
        List<AgentStatusResponse> history = agentStatusService.getStatusHistory(id, null, null);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AgentResponse>> getProfile(@RequestParam UUID userId) {
        AgentResponse response = agentService.getAgentByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AgentResponse>>> searchAgents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String skill,
            Pageable pageable) {
        Specification<com.vcall.agent.entity.Agent> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("agentCode")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(root.get("phone"), "%" + keyword + "%")
                    ));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), AgentStatusEnum.valueOf(status.toUpperCase())));
        }
        if (skill != null && !skill.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("skill")), "%" + skill.toLowerCase() + "%"));
        }
        if (groupId != null) {
            spec = spec.and((root, query, cb) -> {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<AgentGroupMember> memberRoot = subquery.from(AgentGroupMember.class);
                subquery.select(memberRoot.get("agent").get("id"))
                        .where(cb.equal(memberRoot.get("group").get("id"), groupId));
                return cb.in(root.get("id")).value(subquery);
            });
        }
        Page<AgentResponse> response = agentService.searchAgents(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    public void exportAgentsCsv(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) Long groupId,
                                @RequestParam(required = false) String skill,
                                HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<com.vcall.agent.entity.Agent> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("agentCode")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), AgentStatusEnum.valueOf(status.toUpperCase())));
        }
        if (skill != null && !skill.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("skill")), "%" + skill.toLowerCase() + "%"));
        }
        if (groupId != null) {
            spec = spec.and((root, query, cb) -> {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<AgentGroupMember> memberRoot = subquery.from(AgentGroupMember.class);
                subquery.select(memberRoot.get("agent").get("id"))
                        .where(cb.equal(memberRoot.get("group").get("id"), groupId));
                return cb.in(root.get("id")).value(subquery);
            });
        }
        Page<AgentResponse> agents = agentService.searchAgents(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Agent Code", "Full Name", "Email", "Phone", "Status", "Skill", "Max Calls");
        List<List<String>> rows = CsvExportUtil.toRows(agents.getContent(),
                Arrays.asList("id", "agentCode", "fullName", "email", "phone", "status", "skill", "maxConcurrentCalls"));
        CsvExportUtil.writeCsv(response, "agents.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportAgentsExcel(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) Long groupId,
                                  @RequestParam(required = false) String skill,
                                  HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Specification<com.vcall.agent.entity.Agent> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("agentCode")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), AgentStatusEnum.valueOf(status.toUpperCase())));
        }
        if (skill != null && !skill.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("skill")), "%" + skill.toLowerCase() + "%"));
        }
        if (groupId != null) {
            spec = spec.and((root, query, cb) -> {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<AgentGroupMember> memberRoot = subquery.from(AgentGroupMember.class);
                subquery.select(memberRoot.get("agent").get("id"))
                        .where(cb.equal(memberRoot.get("group").get("id"), groupId));
                return cb.in(root.get("id")).value(subquery);
            });
        }
        Page<AgentResponse> agents = agentService.searchAgents(spec, pageable);
        List<String> headers = Arrays.asList("ID", "Agent Code", "Full Name", "Email", "Phone", "Status", "Skill", "Max Calls");
        ExcelExportUtil.writeExcel(response, "agents.xlsx", headers, agents.getContent(),
                Arrays.asList("id", "agentCode", "fullName", "email", "phone", "status", "skill", "maxConcurrentCalls"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = agentService.getAgentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkDelete(
            @RequestBody List<UUID> ids) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        for (UUID id : ids) {
            try {
                agentService.deleteAgent(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk delete completed", result));
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<UUID>>> bulkStatus(
            @RequestBody BulkStatusRequest request) {
        BulkOperationUtil.BulkResult<UUID> result = new BulkOperationUtil.BulkResult<>();
        AgentStatusRequest statusRequest = new AgentStatusRequest(request.getStatus(), request.getReason());
        for (UUID id : request.getIds()) {
            try {
                agentService.updateStatus(id, statusRequest);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Bulk status update completed", result));
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkOperationUtil.BulkResult<?>>> importCsv(
            @RequestParam("file") MultipartFile file) throws IOException {
        List<AgentRequest> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 2) {
                    AgentRequest request = new AgentRequest();
                    request.setAgentCode(fields[0].trim());
                    request.setFullName(fields[1].trim());
                    if (fields.length > 2) request.setEmail(fields[2].trim());
                    if (fields.length > 3) request.setPhone(fields[3].trim());
                    if (fields.length > 4) request.setSkill(fields[4].trim());
                    if (fields.length > 5) {
                        try { request.setMaxConcurrentCalls(Integer.parseInt(fields[5].trim())); }
                        catch (NumberFormatException ignored) {}
                    }
                    items.add(request);
                }
            }
        }
        BulkOperationUtil.BulkResult<?> result = BulkOperationUtil.bulkCreate(items, item ->
                agentService.createAgent((AgentRequest) item));
        return ResponseEntity.ok(ApiResponse.success("Import completed", result));
    }
}
