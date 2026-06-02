package com.vcall.customer360.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Customer360SearchRequest {
    private String keyword;
    private String segment;
    private String sortBy;
    private String sortDir;
    private int page;
    private int size;
}
