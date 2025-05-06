package com.healthlink.Services;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSService {
    // Replace with your actual Twilio credentials
    private static final String ACCOUNT_SID = "ACb3df1055486b72f98c41f4d8105c7d1a";
    private static final String AUTH_TOKEN = "89cbe63807c23cc16f49c9794aa5b2c9";
    private static final String TWILIO_PHONE_NUMBER = "+13527079042";

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    static {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            System.out.println("Twilio client initialized successfully with Account SID: " + ACCOUNT_SID);
        } catch (Exception e) {
            System.err.println("Failed to initialize Twilio client: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Twilio initialization failed", e);
        }
    }

    public SMSService() {
    }

    public void sendConfirmationMessage(String toPhoneNumber, String doctorName, String date) {
        String messageBody = String.format(
                "Votre rendez-vous avec Dr. %s le %s a été confirmé.",
                doctorName, date
        );
        sendMessage(toPhoneNumber, messageBody);
    }

    public static void sendSMS(String to, String message) {
        try {
            System.out.println("Sending SMS to: " + to + " with message: " + message);
            Message twilioMessage = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    message
            ).create();
            System.out.println("SMS sent successfully to " + to + " with SID: " + twilioMessage.getSid());
        } catch (Exception e) {
            System.err.println("Failed to send SMS to " + to + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    public void sendCancellationMessage(String toPhoneNumber, String doctorName, String date) {
        String messageBody = String.format(
                "Votre rendez-vous avec Dr. %s le %s a été annulé.",
                doctorName, date
        );
        sendMessage(toPhoneNumber, messageBody);
    }

    public void sendReminderMessage(String toPhoneNumber, String doctorName, String date) {
        String messageBody = String.format(
                "Rappel : Votre rendez-vous avec Dr. %s est prévu pour demain, le %s.",
                doctorName, date
        );
        sendMessage(toPhoneNumber, messageBody);
    }

    private void sendMessage(String toPhoneNumber, String messageBody) {
        if (toPhoneNumber == null || toPhoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Recipient phone number cannot be null or empty");
        }

        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                System.out.println("Attempting to send SMS to: " + toPhoneNumber + " (Attempt " + (attempt + 1) + ")");
                Message message = Message.creator(
                        new PhoneNumber(toPhoneNumber),
                        new PhoneNumber(TWILIO_PHONE_NUMBER),
                        messageBody
                ).create();
                System.out.println("SMS sent successfully to " + toPhoneNumber + " with SID: " + message.getSid());
                return; // Success, exit the method
            } catch (ApiException e) {
                attempt++;
                System.err.println("Error sending SMS to " + toPhoneNumber + " (Attempt " + attempt + "): " + e.getMessage());
                e.printStackTrace();
                if (e.getMessage().contains("Authenticate")) {
                    throw new ApiException("Authentication failure. Please check Twilio credentials.", e);
                }
                if (attempt < MAX_RETRIES) {
                    try {
                        System.out.println("Retrying in " + RETRY_DELAY_MS + "ms...");
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("Retry interrupted: " + ie.getMessage());
                    }
                } else {
                    throw new ApiException("Failed to send SMS after " + MAX_RETRIES + " attempts: " + e.getMessage(), e);
                }
            }
        }
    }
}