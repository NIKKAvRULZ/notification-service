package com.foodsystem.notification_service.service;

import com.foodsystem.notification_service.dto.*;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;

@Service
public class NotificationService {

    @Autowired
    private RestTemplate restTemplate;

    // SendGrid API Key from Environment Variables
    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    // Your verified SendGrid sender email
    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${services.identity-url}")
    private String identityUrl;
    @Value("${services.payment-url}")
    private String paymentUrl;

    public void processNotification(NotificationRequest request) {
        try {
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
            // Must match the verified sender in your screenshot
            Email from = new Email("it22061348@my.sliit.lk");
            String subject = "Gourmet Express - Order Update #" + orderId;
            Email to = new Email(user.getEmail());

            String htmlContent = "<h1>Order Confirmed</h1>" +
                    "<p>Hello " + user.getUsername() + ", your payment of LKR " +
                    payment.getAmount() + " was processed successfully.</p>";
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response apiResponse = sg.api(request);

            // Log status and body for your project report
            System.out.println("SendGrid API Status: " + apiResponse.getStatusCode());
            if (apiResponse.getStatusCode() == 202) {
                System.out.println("SUCCESS: Email queued for delivery!");
            } else {
                System.err.println("SendGrid Error Body: " + apiResponse.getBody());
            }
        } catch (IOException e) {
            System.err.println("API Connection Error: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(String userId) {
        try {
            // 1. Fetch User Data from your Identity Service on Render
            String userUrl = identityUrl + "/api/users/" + userId;
            UserDTO user = restTemplate.getForObject(userUrl, UserDTO.class);

            if (user != null) {
                // 2. Prepare SendGrid API Objects
                Email from = new Email("it22061348@my.sliit.lk"); // Must match your verified sender
                Email to = new Email(user.getEmail());
                String subject = "Welcome to Gourmet Express, " + user.getUsername() + "!";

                String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #ffffff; padding: 30px; border: 1px solid #eee; border-radius: 15px;'>"
                        +
                        "<h1 style='color: #d9534f;'>Welcome to the Family!</h1>" +
                        "<p>Hi <b>" + user.getUsername() + "</b>,</p>" +
                        "<p>Thank you for registering. Your account is now active!</p>" +
                        "<p><b>Login Email:</b> " + user.getEmail() + "</p>" +
                        "<hr><p style='font-size: 12px; color: #888;'>Gourmet Express Cloud Notification System</p>" +
                        "</div></body></html>";

                Content content = new Content("text/html", htmlContent);
                Mail mail = new Mail(from, subject, to, content);

                // 3. Execute API Request via Port 443
                SendGrid sg = new SendGrid(sendGridApiKey);
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                Response response = sg.api(request);
                System.out.println("Welcome Email Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Welcome Email API Error: " + e.getMessage());
        }
    }
}