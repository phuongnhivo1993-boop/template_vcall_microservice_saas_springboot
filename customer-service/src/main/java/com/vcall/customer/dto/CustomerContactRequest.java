package com.vcall.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerContactRequest {

    @NotNull(message = "Contact type is required")
    private String contactType;

    @NotBlank(message = "Contact value is required")
    private String contactValue;

    private boolean isPrimary;
}
