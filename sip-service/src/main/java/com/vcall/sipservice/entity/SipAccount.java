package com.vcall.sipservice.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "sip_accounts")
@Getter
@Setter
@SQLRestriction("is_deleted = false")
public class SipAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String domain;

    private String realm;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(name = "max_channels")
    private Integer maxChannels = 10;

    @Column(name = "allow_registration")
    private Boolean allowRegistration = true;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    public enum AccountType {
        INTERNAL, EXTERNAL
    }

    public enum AccountStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
