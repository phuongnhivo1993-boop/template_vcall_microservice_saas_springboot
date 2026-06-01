package com.vcall.omnichannel.entity;

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

@Entity
@Table(name = "channel_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20, nullable = false, unique = true)
    private Conversation.Channel channel;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    @Column(name = "config", columnDefinition = "JSONB")
    private String config;
}
