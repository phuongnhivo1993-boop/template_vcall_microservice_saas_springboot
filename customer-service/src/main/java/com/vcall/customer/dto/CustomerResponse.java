package com.vcall.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private UUID id;
    private String customerCode;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String idNumber;
    private String nationality;
    private String company;
    private String position;
    private String notes;
    private Set<CustomerTagResponse> tags;
    private List<CustomerContactResponse> contacts;
    private List<CustomerAddressResponse> addresses;
    private LocalDateTime createdAt;
}
