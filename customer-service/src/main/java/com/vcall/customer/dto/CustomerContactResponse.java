package com.vcall.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerContactResponse {

    private Long id;
    private String contactType;
    private String contactValue;
    private boolean isPrimary;
}
