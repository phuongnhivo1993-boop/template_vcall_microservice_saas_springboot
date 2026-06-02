package com.vcall.customer360.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer360Response {
    private UUID id;
    private UUID customerId;
    private String fullName;
    private String email;
    private String phone;
    private Integer totalCalls;
    private Integer totalTickets;
    private Integer totalLeads;
    private Integer totalOpportunities;
    private BigDecimal totalSpent;
    private LocalDateTime lastContactAt;
    private BigDecimal lifetimeValue;
    private BigDecimal satisfactionScore;
    private String segment;
    private String notes;

    private List<RecentInteraction> recentInteractions;
    private List<OpenTicket> openTickets;
    private List<CrmActivity> recentActivities;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RecentInteraction {
        private String type;
        private String summary;
        private String status;
        private LocalDateTime timestamp;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OpenTicket {
        private UUID ticketId;
        private String ticketNumber;
        private String title;
        private String priority;
        private String status;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CrmActivity {
        private Long activityId;
        private String type;
        private String subject;
        private LocalDateTime date;
    }
}
