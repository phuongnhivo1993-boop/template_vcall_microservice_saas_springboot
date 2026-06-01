package com.vcall.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.vcall.notification.entity.NotificationChannel;
import com.vcall.notification.entity.PushDevice;
import com.vcall.notification.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushDeviceRepository pushDeviceRepository;

    public void sendPush(com.vcall.notification.entity.Notification notification) {
        List<PushDevice> devices = pushDeviceRepository.findByUserId(notification.getRecipientId());
        for (PushDevice device : devices) {
            if (Boolean.TRUE.equals(device.getIsActive())) {
                sendToDevice(device, notification);
            }
        }
        notification.setSentAt(java.time.LocalDateTime.now());
    }

    public void sendToDevice(PushDevice device, com.vcall.notification.entity.Notification notification) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(device.getDeviceToken())
                    .setNotification(Notification.builder()
                            .setTitle(notification.getTitle())
                            .setBody(notification.getBody())
                            .build());

            switch (device.getPlatform()) {
                case ANDROID -> messageBuilder.setAndroidConfig(
                        AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build());
                case IOS -> messageBuilder.setApnsConfig(
                        ApnsConfig.builder().putHeader("apns-priority", "10").build());
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Push notification sent to device {}: {}", device.getDeviceToken(), response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push to device {}: {}", device.getDeviceToken(), e.getMessage());
        }
    }

    public void sendToTopic(String topic, com.vcall.notification.entity.Notification notification) {
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(notification.getTitle())
                            .setBody(notification.getBody())
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification sent to topic {}: {}", topic, response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push to topic {}: {}", topic, e.getMessage());
        }
    }
}
