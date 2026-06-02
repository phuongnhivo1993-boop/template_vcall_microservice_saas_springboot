package com.vcall.agent.controller;

import com.vcall.agent.dto.AgentGroupRequest;
import com.vcall.agent.dto.AgentGroupResponse;
import com.vcall.agent.service.AgentGroupService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent-groups")
@RequiredArgsConstructor
public class AgentGroupController {

    private final AgentGroupService agentGroupService;

    @PostMapping
    public ResponseEntity<ApiResponse<AgentGroupResponse>> createGroup(@Valid @RequestBody AgentGroupRequest request) {
        AgentGroupResponse response = agentGroupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Group created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AgentGroupResponse>>> getAllGroups(Pageable pageable) {
        Page<AgentGroupResponse> groups = agentGroupService.getAllGroups(pageable);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentGroupResponse>> getGroup(@PathVariable Long id) {
        AgentGroupResponse response = agentGroupService.getGroup(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentGroupResponse>> updateGroup(@PathVariable Long id,
                                                                        @Valid @RequestBody AgentGroupRequest request) {
        AgentGroupResponse response = agentGroupService.updateGroup(id, request);
        return ResponseEntity.ok(ApiResponse.success("Group updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        agentGroupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Group deleted successfully", null));
    }

    @PostMapping("/{groupId}/agents/{agentId}")
    public ResponseEntity<ApiResponse<Void>> addMember(@PathVariable Long groupId, @PathVariable UUID agentId) {
        agentGroupService.addMember(groupId, agentId);
        return ResponseEntity.ok(ApiResponse.success("Agent added to group successfully", null));
    }

    @DeleteMapping("/{groupId}/agents/{agentId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long groupId, @PathVariable UUID agentId) {
        agentGroupService.removeMember(groupId, agentId);
        return ResponseEntity.ok(ApiResponse.success("Agent removed from group successfully", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AgentGroupResponse>>> searchGroups(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Specification<com.vcall.agent.entity.AgentGroup> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    ));
        }
        Page<AgentGroupResponse> response = agentGroupService.searchGroups(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/export/csv")
    public void exportGroupsCsv(@RequestParam(required = false) String keyword,
                                HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<AgentGroupResponse> groups;
        if (keyword != null && !keyword.isEmpty()) {
            Specification<com.vcall.agent.entity.AgentGroup> spec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    );
            groups = agentGroupService.searchGroups(spec, pageable);
        } else {
            groups = agentGroupService.getAllGroups(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description", "Member Count");
        List<List<String>> rows = CsvExportUtil.toRows(groups.getContent(),
                Arrays.asList("id", "name", "description", "memberCount"));
        CsvExportUtil.writeCsv(response, "agent-groups.csv", headers, rows);
    }

    @GetMapping("/export/excel")
    public void exportGroupsExcel(@RequestParam(required = false) String keyword,
                                  HttpServletResponse response) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<AgentGroupResponse> groups;
        if (keyword != null && !keyword.isEmpty()) {
            Specification<com.vcall.agent.entity.AgentGroup> spec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                    );
            groups = agentGroupService.searchGroups(spec, pageable);
        } else {
            groups = agentGroupService.getAllGroups(pageable);
        }
        List<String> headers = Arrays.asList("ID", "Name", "Description", "Member Count");
        ExcelExportUtil.writeExcel(response, "agent-groups.xlsx", headers, groups.getContent(),
                Arrays.asList("id", "name", "description", "memberCount"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = agentGroupService.getGroupStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
