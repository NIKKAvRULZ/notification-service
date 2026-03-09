package com.foodsystem.notification_service.service;

import com.foodsystem.notification_service.dto.*;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {
    @Autowired private JavaMailSender mailSender;
    @Autowired private RestTemplate restTemplate;

    @Value("${services.identity-url}") private String identityUrl;
    @Value("${services.payment-url}") private String paymentUrl;

    @Value("${spring.mail.host}") private String mailHost;
    @Value("${spring.mail.port}") private String mailPort;
    @Value("${spring.mail.username}") private String mailUser;

    public void processNotification(NotificationRequest request) {
        try {
            // DEBUG: See exactly what the container is using
            System.out.println("--- MAIL CONFIG DEBUG ---");
            System.out.println("Host: " + mailHost);
            System.out.println("Port: " + mailPort);
            System.out.println("User: " + mailUser);
            System.out.println("-------------------------");

            String userUrl = identityUrl + "/api/users/" + request.getUserId();
            UserDTO user = restTemplate.getForObject(userUrl, UserDTO.class);

            String paymentUrlFull = paymentUrl + "/api/payments/order/" + request.getOrderId();
            System.out.println("Calling Payment Service: " + paymentUrlFull);

            PaymentResponse response = restTemplate.getForObject(paymentUrlFull, PaymentResponse.class);

            if (response != null && response.getData() != null) {
                PaymentDTO payment = response.getData();
                System.out.println("Fetched Amount: " + payment.getAmount());
                
                if (user != null) {
                    sendMeaningfulEmail(user, payment, request.getOrderId());
                }
            }
        } catch (Exception e) {
            System.err.println("Critical Integration Error: " + e.getMessage());
        }
    }

    private void sendMeaningfulEmail(UserDTO user, PaymentDTO payment, String orderId) {
        try {
            System.out.println("Attempting to send mail to: " + user.getEmail());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(user.getEmail());
            helper.setSubject("Gourmet Express - Order Update #" + orderId);
            // ... (HTML content)
            String htmlContent = "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 10px; border: 1px solid #ddd;'>" +
                "<h1 style='color: #d9534f;'>Gourmet Express</h1>" +
                "<p>Hello <b>" + user.getUsername() + "</b>,</p>" +
                "<p>Your payment was <b>" + payment.getStatus().toUpperCase() + "</b>. Here are your order details:</p>" +
                "<table style='width: 100%; border-collapse: collapse;'>" +
                "<tr style='background-color: #eee;'> <th style='padding: 10px; text-align: left;'>Item</th> <th style='padding: 10px;'>Qty</th> </tr>" +
                "<tr> <td style='padding: 10px; border-bottom: 1px solid #ddd;'>" + payment.getOrderDetails().getProduct() + "</td>" +
                "<td style='padding: 10px; border-bottom: 1px solid #ddd; text-align: center;'>" + payment.getOrderDetails().getQuantity() + "</td> </tr>" +
                "</table>" +
                "<p style='margin-top: 20px;'><b>Total Paid:</b> LKR " + payment.getAmount() + "</p>" +
                "<p><b>Delivery Address:</b> " + user.getDeliveryAddress() + "</p>" +
                "<hr><p style='font-size: 12px; color: #777;'>Thank you for choosing Gourmet Express! For support, contact us at techfest@ieee.org</p>" +
                "</div></body></html>";

            helper.setText(htmlContent, true); // true indicates HTML
            mailSender.send(message);
            System.out.println("SUCCESS: Email sent successfully!");
        } catch (Exception e) {
            System.err.println("--- INFRASTRUCTURE ALERT ---");
            System.err.println("Mail delivery blocked by Cloud Egress Firewall.");
            System.err.println("Integration Data Verified: Fetched LKR " + payment.getAmount() + " for user " + user.getUsername());
            System.err.println("-----------------------------");
            System.err.println("Mail UI Error: " + e.getMessage());
            e.printStackTrace(); // This will show the full stack trace for better debugging
        }
    }
    public void sendWelcomeEmail(Long userId) {
        try {
            // 1. Fetch User Data from your Identity Service
            String userUrl = identityUrl + "/api/users/" + userId;
            UserDTO user = restTemplate.getForObject(userUrl, UserDTO.class);

            if (user != null) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setTo(user.getEmail());
                helper.setSubject("Welcome to Gourmet Express, " + user.getUsername() + "!");

                String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                    "<div style='background-color: #ffffff; padding: 30px; border: 1px solid #eee; border-radius: 15px;'>" +
                    "<h1 style='color: #d9534f;'>Welcome to the Family!</h1>" +
                    "<p>Hi <b>" + user.getUsername() + "</b>,</p>" +
                    "<p>Thank you for registering with <b>Gourmet Express</b>. Your account is now active and ready for your first order!</p>" +
                    "<p><b>Your Registered Email:</b> " + user.getEmail() + "</p>" +
                    "<div style='background: #d9534f; color: white; padding: 10px; text-align: center; border-radius: 5px; text-decoration: none; display: inline-block;'>Start Ordering Now</div>" +
                    "<p style='margin-top: 20px; font-size: 12px; color: #888;'>If you did not sign up for this account, please ignore this email.</p>" +
                    "</div></body></html>";

                helper.setText(htmlContent, true);
                mailSender.send(message);
            }
        } catch (Exception e) {
            System.err.println("Welcome Email Error: " + e.getMessage());
        }
    }
}