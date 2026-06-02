package com.vcall.notification.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.notification.dto.NotificationPreferenceRequest;
import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.NotificationPreference;
import com.vcall.notification.entity.NotificationType;
import com.vcall.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Transactional(readOnly = true)
    public Page<NotificationPreference> getPreferences(UUID userId, Pageable pageable) {
        return preferenceRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public NotificationPreference updatePreference(NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository
                .findByUserIdAndChannelAndType(request.getUserId(), request.getChannel(), request.getType())
                .orElseGet(() -> {
                    NotificationPreference newPref = new NotificationPreference();
                    newPref.setUserId(request.getUserId());
                    newPref.setChannel(request.getChannel());
                    newPref.setType(request.getType());
                    return newPref;
                });
        preference.setIsEnabled(request.getIsEnabled());
        return preferenceRepository.save(preference);
    }

    @Transactional(readOnly = true)
    public boolean checkAllowed(UUID userId, NotificationChannel channel, NotificationType type) {
        return preferenceRepository.findByUserIdAndChannelAndType(userId, channel, type)
                .map(NotificationPreference::getIsEnabled)
                .orElse(true);
    }
}
