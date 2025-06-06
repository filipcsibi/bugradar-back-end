package com.example.bugradar.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    // Poți integra cu Twilio, Firebase Cloud Messaging, etc.

    public void sendBanNotification(String phoneNumber, String reason) {
        System.out.println("Sending ban SMS to: " + phoneNumber);
        System.out.println("Reason: " + reason);

        String message = String.format(
                "Bug Radar: Your account has been suspended. Reason: %s. Contact support for more info.",
                reason
        );

        // Aici ai integra cu serviciul real de SMS
        // De exemplu: twilioClient.sendSms(phoneNumber, message);
    }

    public void sendUnbanNotification(String phoneNumber) {
        System.out.println("Sending unban SMS to: " + phoneNumber);

        String message = "Bug Radar: Your account has been restored. Welcome back!";

        // Implementează trimiterea SMS-ului real
    }
}