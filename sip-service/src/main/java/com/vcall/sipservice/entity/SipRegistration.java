package com.vcall.sipservice.entity;

import com.vcall.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "sip_registrations")
@Getter
@Setter
@SQLRestriction("is_deleted = false")
public class SipRegistration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sip_account_id", nullable = false)
    private SipAccount sipAccount;

    @Column(name = "contact_uri")
    private String contactUri;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    private Integer port;

    @Enumerated(EnumType.STRING)
    private Transport transport;

    private Integer expires;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "last_refresh")
    private LocalDateTime lastRefresh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    public enum Transport {
        UDP, TCP, TLS
    }

    public enum RegistrationStatus {
        REGISTERED, EXPIRED, UNREGISTERED
    }
}
