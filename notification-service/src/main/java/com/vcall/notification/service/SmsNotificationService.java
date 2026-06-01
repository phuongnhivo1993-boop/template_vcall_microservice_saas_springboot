package com.vcall.notification.service;

import com.vcall.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationService {

    public void sendSms(Notification notification) {
        log.info("Sending SMS to {}: {}", notification.getRecipientAddress(), notification.getBody());
        // Integrate with Twilio, Vonage, AWS SNS, etc.
        // For now we simulate success
        notification.setSentAt(java.time.LocalDateTime.now());
    }
}
