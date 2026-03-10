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
    @Value("${services.order-url}")
    private String orderUrl;

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
                    java.util.Map<String, Object> orderDetails = null;
                    try {
                        String cleanOrderUrl = orderUrl.endsWith("/") ? orderUrl.substring(0, orderUrl.length() - 1)
                                : orderUrl;
                        String orderInfoUrl = cleanOrderUrl + "/orders/" + request.getOrderId();
                        System.out.println("Calling Order Service: " + orderInfoUrl);
                        orderDetails = restTemplate.getForObject(orderInfoUrl, java.util.Map.class);
                    } catch (Exception ex) {
                        System.err.println("Could not fetch full order details: " + ex.getMessage());
                    }
                    sendMeaningfulEmail(user, payment, request.getOrderId(), orderDetails);
                }
            }
        } catch (Exception e) {
            System.err.println("Critical Integration Error: " + e.getMessage());
        }
    }

    private void sendMeaningfulEmail(UserDTO user, PaymentDTO payment, String orderId,
            java.util.Map<String, Object> orderDetails) {
        try {
            // Must match the verified sender in your screenshot
            Email from = new Email("it22061348@my.sliit.lk");
            String subject = "Gourmet Express - Order Confirmation #" + orderId;
            Email to = new Email("nithika151@gmail.com"); // Hardcoded to your email to prevent bouncing

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append(
                    "<html><body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>");
            htmlContent.append(
                    "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>");
            htmlContent.append(
                    "<div style='text-align: center; border-bottom: 2px solid #ff4757; padding-bottom: 20px; margin-bottom: 20px;'>");
            htmlContent.append("<h1 style='color: #ff4757; margin: 0;'>Gourmet Express</h1>");
            htmlContent.append("<h3 style='color: #555; margin-top: 5px;'>Order Receipt</h3>");
            htmlContent.append("</div>");

            htmlContent.append("<p style='font-size: 16px; color: #333;'>Hello <b>").append(user.getUsername())
                    .append("</b>,</p>");
            htmlContent.append(
                    "<p style='font-size: 15px; color: #555;'>Great news! Your payment was successfully processed and your delicious food is being prepared.</p>");

            htmlContent.append(
                    "<div style='background-color: #f1f2f6; padding: 15px; border-radius: 8px; margin: 20px 0;'>");
            htmlContent.append(
                    "<h4 style='margin-top: 0; color: #2f3542; border-bottom: 1px solid #dfe4ea; padding-bottom: 10px;'>Transaction Details</h4>");
            htmlContent.append("<p style='margin: 5px 0;'><b>Order Number:</b> #").append(orderId).append("</p>");
            htmlContent.append("<p style='margin: 5px 0;'><b>Amount Paid:</b> LKR ").append(payment.getAmount())
                    .append("</p>");
            htmlContent.append("<p style='margin: 5px 0;'><b>Delivery Address:</b> ")
                    .append(user.getDeliveryAddress() != null ? user.getDeliveryAddress() : "Address on File")
                    .append("</p>");
            htmlContent.append("</div>");

            if (orderDetails != null && !orderDetails.isEmpty()) {
                htmlContent.append(
                        "<div style='background-color: #f1f2f6; padding: 15px; border-radius: 8px; margin: 20px 0;'>");
                htmlContent.append(
                        "<h4 style='margin-top: 0; color: #2f3542; border-bottom: 1px solid #dfe4ea; padding-bottom: 10px;'>Order Summary</h4>");
                htmlContent.append("<table style='width: 100%; border-collapse: collapse;'>");
                for (java.util.Map.Entry<String, Object> entry : orderDetails.entrySet()) {
                    htmlContent.append("<tr>");
                    htmlContent.append("<td style='padding: 8px 0; color: #555; border-bottom: 1px solid #eee;'><b>")
                            .append(entry.getKey()).append(":</b></td>");
                    htmlContent.append(
                            "<td style='padding: 8px 0; color: #333; text-align: right; border-bottom: 1px solid #eee;'>")
                            .append(entry.getValue()).append("</td>");
                    htmlContent.append("</tr>");
                }
                htmlContent.append("</table>");
                htmlContent.append("</div>");
            }

            htmlContent.append("<br><div style='text-align: center; margin-top: 30px;'>");
            htmlContent.append("<p style='color: red; font-size: 13px;'><i>(Demo Mode - Original intended recipient: ")
                    .append(user.getEmail()).append(")</i></p>");
            htmlContent.append("</div>");
            htmlContent.append("</div></body></html>");

            Content content = new Content("text/html", htmlContent.toString());
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

    public String sendWelcomeEmail(String userId) {
        try {
            // 1. Fetch User Data from your Identity Service on Render
            String cleanIdentityUrl = identityUrl.endsWith("/") ? identityUrl.substring(0, identityUrl.length() - 1)
                    : identityUrl;
            String userUrl = cleanIdentityUrl + "/api/users/" + userId;
            UserDTO user = restTemplate.getForObject(userUrl, UserDTO.class);

            if (user != null) {
                // 2. Prepare SendGrid API Objects
                Email from = new Email("it22061348@my.sliit.lk"); // Must match your verified sender
                Email to = new Email("nithika151@gmail.com"); // Hardcoded to your email to prevent bouncing
                String subject = "Welcome to Gourmet Express, " + user.getUsername() + "!";

                String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #ffffff; padding: 30px; border: 1px solid #eee; border-radius: 15px;'>"
                        +
                        "<h1 style='color: #d9534f;'>Welcome to the Family!</h1>" +
                        "<p>Hi <b>" + user.getUsername() + "</b>,</p>" +
                        "<p>Thank you for registering. Your account is now active!</p>" +
                        "<p><b>Login Email:</b> " + user.getEmail() + "</p>" +
                        "<br><p style='color: red;'><i>(Demo Mode - This email was originally intended for: "
                        + user.getEmail() + ")</i></p>" +
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
                return "SendGrid Status: " + response.getStatusCode() + " - Body: " + response.getBody();
            } else {
                return "User fetch returned null for ID: " + userId;
            }
        } catch (Exception e) {
            throw new RuntimeException("Welcome Email API Error: " + e.getMessage());
        }
    }
}