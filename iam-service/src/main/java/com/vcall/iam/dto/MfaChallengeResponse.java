package com.vcall.iam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MfaChallengeResponse {
    private boolean mfaRequired;
    private String mfaToken;
}
