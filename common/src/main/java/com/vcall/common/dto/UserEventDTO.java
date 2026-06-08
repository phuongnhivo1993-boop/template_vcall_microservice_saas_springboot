package com.vcall.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String status;
    private Set<String> roles;
}
