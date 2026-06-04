package com.vcall.common.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 200)
    private String companyName;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Email must be valid")
    private String adminEmail;

    @NotBlank(message = "Admin name is required")
    private String adminName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    private String password;

    private String phone;

    private String plan;
}
