package com.vcall.pbx.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "extensions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Extension extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "extension_number", unique = true, nullable = false, length = 50)
    private String extensionNumber;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ExtensionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ExtensionStatus status = ExtensionStatus.ACTIVE;

    @Column(name = "voicemail_enabled")
    private Boolean voicemailEnabled = true;

    @Column(name = "call_forwarding", columnDefinition = "TEXT")
    private String callForwarding;

    @Column(name = "outbound_caller_id", length = 50)
    private String outboundCallerId;

    @Column(name = "max_concurrent_calls")
    private Integer maxConcurrentCalls = 6;

    @Column(name = "sip_account_id")
    private Long sipAccountId;

    public enum ExtensionType {
        VIRTUAL, SIP_MOBILE, EXTERNAL
    }

    public enum ExtensionStatus {
        ACTIVE, INACTIVE, BUSY
    }
}
