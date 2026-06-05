package com.vcall.xr.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagRequest {

    @NotBlank(message = "Feature key is required")
    private String featureKey;

    @NotNull(message = "Enabled flag is required")
    private boolean enabled;

    private String config;
}
