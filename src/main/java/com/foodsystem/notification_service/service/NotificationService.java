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
    @Value("${services.catalog-url}")
    private String catalogUrl;

    @SuppressWarnings("unchecked")
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
            String shortOrderId = orderId != null && orderId.length() >= 8
                    ? orderId.substring(orderId.length() - 8).toUpperCase()
                    : orderId;
            String subject = "Gourmet Express - Receipt for Order #" + shortOrderId;
            Email to = new Email("nithika151@gmail.com"); // Hardcoded to your email to prevent bouncing

            StringBuilder html = new StringBuilder();
            html.append(
                    "<body style='font-family: Arial, sans-serif; background-color: #05080f; padding: 40px 20px; margin: 0; color: #f8fafc;'>")
                    .append("<table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 0 auto; background-color: #0f172a; border-radius: 16px; border: 1px solid #1e293b; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5);'>")
                    .append("<tr><td style='padding: 40px 30px; text-align: center; border-bottom: 1px solid #1e293b; background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, transparent 100%);'>")
                    .append("<h1 style='color: #eab308; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 1px; text-transform: uppercase;'>Gourmet<span style='color: #ffffff;'>Express</span></h1>")
                    .append("<p style='color: #10b981; margin: 10px 0 0 0; font-size: 14px; letter-spacing: 2px; text-transform: uppercase; font-weight: 600;'>Payment Verified</p></td></tr>")

                    .append("<tr><td style='padding: 40px 30px;'>")
                    .append("<h2 style='margin: 0 0 20px 0; color: #ffffff; font-weight: 300;'>Payment <span style='color: #eab308;'>Authorized</span></h2>")
                    .append("<p style='font-size: 16px; line-height: 1.6; color: #cbd5e1; margin: 0 0 20px 0;'>Hello <b style='color: #ffffff;'>")
                    .append(user.getUsername()).append("</b>,</p>")
                    .append("<p style='font-size: 15px; line-height: 1.6; color: #94a3b8; margin: 0 0 30px 0;'>Your payment transaction was successfully validated via the network. Your culinary request is now actively being synthesized.</p>");

            html.append(
                    "<div style='background-color: rgba(255,255,255,0.02); padding: 25px; border-radius: 12px; margin-bottom: 30px; border: 1px solid #1e293b; border-left: 4px solid #10b981;'>")
                    .append("<h4 style='margin-top: 0; color: #94a3b8; border-bottom: 1px solid #1e293b; padding-bottom: 10px; text-transform: uppercase; font-size: 11px; letter-spacing: 1px;'>Transaction Ledger</h4>")
                    .append("<table style='width: 100%; border-collapse: collapse;'>")
                    .append("<tr><td style='padding: 10px 0; color: #94a3b8; font-size: 14px; border-bottom: 1px solid rgba(255,255,255,0.05);'><b>Reference Trace:</b></td><td style='padding: 10px 0; color: #ffffff; font-size: 14px; text-align: right; border-bottom: 1px solid rgba(255,255,255,0.05);'>#")
                    .append(shortOrderId).append("</td></tr>")
                    .append("<tr><td style='padding: 10px 0; color: #94a3b8; font-size: 14px; border-bottom: 1px solid rgba(255,255,255,0.05);'><b>Transfer Amount:</b></td><td style='padding: 10px 0; color: #eab308; font-weight: 700; font-size: 16px; text-align: right; border-bottom: 1px solid rgba(255,255,255,0.05);'>LKR ")
                    .append(payment.getAmount()).append("</td></tr>")
                    .append("<tr><td style='padding: 10px 0; color: #94a3b8; font-size: 14px; border-bottom: 1px solid rgba(255,255,255,0.05);'><b>Drop Coordinate:</b></td><td style='padding: 10px 0; color: #ffffff; font-size: 14px; text-align: right; border-bottom: 1px solid rgba(255,255,255,0.05);'>")
                    .append(user.getDeliveryAddress() != null ? user.getDeliveryAddress() : "Not Specified")
                    .append("</td></tr>")
                    .append("</table></div>");

            if (orderDetails != null && !orderDetails.isEmpty()) {
                html.append(
                        "<div style='background-color: rgba(255,255,255,0.02); padding: 25px; border-radius: 12px; margin-bottom: 30px; border: 1px solid #1e293b;'>")
                        .append("<h4 style='margin-top: 0; color: #94a3b8; border-bottom: 1px solid #1e293b; padding-bottom: 10px; text-transform: uppercase; font-size: 11px; letter-spacing: 1px;'>Order Configuration</h4>")
                        .append("<table style='width: 100%; border-collapse: collapse;'>");
                for (java.util.Map.Entry<String, Object> entry : orderDetails.entrySet()) {
                    if (entry.getValue() != null && !(entry.getValue() instanceof java.util.Collection)
                            && !(entry.getValue() instanceof java.util.Map)) {
                        html.append("<tr>")
                                .append("<td style='padding: 10px 0; color: #94a3b8; font-size: 14px; border-bottom: 1px solid rgba(255,255,255,0.05); text-transform: capitalize;'><b>")
                                .append(entry.getKey()).append(":</b></td>")
                                .append("<td style='padding: 10px 0; color: #ffffff; font-size: 14px; text-align: right; border-bottom: 1px solid rgba(255,255,255,0.05);'>")
                                .append(entry.getValue()).append("</td>")
                                .append("</tr>");
                    }
                }
                html.append("</table></div>");
            }

            html.append("<div style='text-align: center; padding-top: 20px; border-top: 1px solid #1e293b;'>")
                    .append("<p style='color: #ef4444; font-size: 12px; margin: 0;'><i>Demo Mode Filter - Authentic Destination: ")
                    .append(user.getEmail()).append("</i></p>")
                    .append("</div>")
                    .append("</td></tr></table></body>");

            Content content = new Content("text/html", html.toString());
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

                String htmlContent = "<body style='font-family: Arial, sans-serif; background-color: #05080f; padding: 40px 20px; margin: 0; color: #f8fafc;'>"
                        + "<table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 0 auto; background-color: #0f172a; border-radius: 16px; border: 1px solid #1e293b; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5);'>"
                        + "<tr><td style='padding: 40px 30px; text-align: center; border-bottom: 1px solid #1e293b; background: linear-gradient(135deg, rgba(234, 179, 8, 0.1) 0%, transparent 100%);'>"
                        + "<h1 style='color: #eab308; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 1px; text-transform: uppercase;'>Gourmet<span style='color: #ffffff;'>Express</span></h1>"
                        + "<p style='color: #94a3b8; margin: 10px 0 0 0; font-size: 14px; letter-spacing: 2px; text-transform: uppercase;'>Identity Confirmed</p></td></tr>"
                        + "<tr><td style='padding: 40px 30px;'>"
                        + "<h2 style='margin: 0 0 20px 0; color: #ffffff; font-weight: 300;'>Access <span style='color: #eab308;'>Granted</span></h2>"
                        + "<p style='font-size: 16px; line-height: 1.6; color: #cbd5e1; margin: 0 0 20px 0;'>Welcome to the network, <b style='color: #ffffff;'>"
                        + user.getUsername() + "</b>.</p>"
                        + "<p style='font-size: 15px; line-height: 1.6; color: #94a3b8; margin: 0 0 30px 0;'>Your client node has been authenticated and registered to the Gourmet Express mainframe. You can now securely request authentic provisions and culinary artifacts.</p>"
                        + "<div style='background-color: rgba(255,255,255,0.02); padding: 25px; border-radius: 12px; margin-bottom: 30px; border: 1px solid #1e293b; border-left: 4px solid #3b82f6;'>"
                        + "<table style='width: 100%; border-collapse: collapse;'>"
                        + "<tr><td style='padding: 10px 0; color: #94a3b8; font-size: 14px; border-bottom: 1px solid rgba(255,255,255,0.05);'><b>Registered Signature:</b></td><td style='padding: 10px 0; color: #ffffff; font-size: 14px; text-align: right; border-bottom: 1px solid rgba(255,255,255,0.05);'>"
                        + user.getUsername() + "</td></tr>"
                        + "<tr><td style='padding: 10px 0; color: #94a3b8; font-size: 14px; border-bottom: 1px solid rgba(255,255,255,0.05);'><b>Secure Email Vector:</b></td><td style='padding: 10px 0; color: #eab308; font-size: 14px; text-align: right; border-bottom: 1px solid rgba(255,255,255,0.05);'>"
                        + user.getEmail() + "</td></tr>"
                        + "</table></div>"
                        + "<div style='text-align: center; padding-top: 20px; border-top: 1px solid #1e293b;'>"
                        + "<p style='color: #ef4444; font-size: 12px; margin: 0;'><i>Demo Mode Filter - Authentic Destination: "
                        + user.getEmail() + "</i></p>"
                        + "</div>"
                        + "</td></tr></table></body>";

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

    @SuppressWarnings("unchecked")
    public String sendOrderPendingPaymentEmail(String userId, String orderId) {
        try {
            // 1. Fetch User Data
            String cleanIdentityUrl = identityUrl.endsWith("/") ? identityUrl.substring(0, identityUrl.length() - 1)
                    : identityUrl;
            String userUrl = cleanIdentityUrl + "/api/users/" + userId;
            UserDTO user = restTemplate.getForObject(userUrl, UserDTO.class);

            // 2. Fetch Order Data
            java.util.Map<String, Object> orderDetails = null;
            try {
                String cleanOrderUrl = orderUrl.endsWith("/") ? orderUrl.substring(0, orderUrl.length() - 1) : orderUrl;
                String orderInfoUrl = cleanOrderUrl + "/orders/" + orderId;
                System.out.println("Calling Order Service: " + orderInfoUrl);
                orderDetails = restTemplate.getForObject(orderInfoUrl, java.util.Map.class);
            } catch (Exception e) {
                System.err.println("Could not fetch full order details: " + e.getMessage());
            }

            if (user != null) {
                Email from = new Email("it22061348@my.sliit.lk");
                Email to = new Email("nithika151@gmail.com");
                String shortOrderId = orderId != null && orderId.length() >= 8
                        ? orderId.substring(orderId.length() - 8).toUpperCase()
                        : orderId;
                String subject = "Gourmet Express - Action Required for Order #" + shortOrderId;

                StringBuilder html = new StringBuilder();
                html.append(
                        "<body style='font-family: Arial, sans-serif; background-color: #05080f; padding: 40px 20px; margin: 0; color: #f8fafc;'>")
                        .append("<table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 0 auto; background-color: #0f172a; border-radius: 16px; border: 1px solid #1e293b; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5);'>")
                        .append("<tr><td style='padding: 40px 30px; text-align: center; border-bottom: 1px solid #1e293b; background: linear-gradient(135deg, rgba(234, 179, 8, 0.1) 0%, transparent 100%);'>")
                        .append("<h1 style='color: #eab308; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 1px; text-transform: uppercase;'>Gourmet<span style='color: #ffffff;'>Express</span></h1>")
                        .append("<p style='color: #94a3b8; margin: 10px 0 0 0; font-size: 14px; letter-spacing: 2px; text-transform: uppercase;'>Pending Payment</p></td></tr>")

                        .append("<tr><td style='padding: 40px 30px;'>")
                        .append("<h2 style='margin: 0 0 20px 0; color: #ffffff; font-weight: 300;'>Action <span style='color: #eab308;'>Required</span></h2>")
                        .append("<p style='font-size: 16px; line-height: 1.6; color: #cbd5e1; margin: 0 0 20px 0;'>Hello <b style='color: #ffffff;'>")
                        .append(user.getUsername()).append("</b>,</p>")
                        .append("<p style='font-size: 15px; line-height: 1.6; color: #94a3b8; margin: 0 0 30px 0;'>Your order <b style='color: #eab308;'>#")
                        .append(shortOrderId)
                        .append("</b> has been successfully initiated in our network. To commence preparation, please securely complete the payment process.</p>");

                if (orderDetails != null && !orderDetails.isEmpty()) {
                    html.append(
                            "<div style='background-color: rgba(255,255,255,0.02); padding: 25px; border-radius: 12px; margin-bottom: 30px; border: 1px solid #1e293b;'>")
                            .append("<h4 style='margin-top: 0; color: #94a3b8; border-bottom: 1px solid #1e293b; padding-bottom: 10px; text-transform: uppercase; font-size: 11px; letter-spacing: 1px;'>Order Abstract</h4>")
                            .append("<table style='width: 100%; border-collapse: collapse;'>");

                    for (java.util.Map.Entry<String, Object> entry : orderDetails.entrySet()) {
                        if (entry.getValue() != null && !(entry.getValue() instanceof java.util.Collection)
                                && !(entry.getValue() instanceof java.util.Map)
                                && !entry.getKey().equals("id") && !entry.getKey().equals("userId")
                                && !entry.getKey().equals("items")) {
                            html.append("<tr>")
                                    .append("<td style='padding: 10px 0; color: #94a3b8; font-size: 14px; border-bottom: 1px solid rgba(255,255,255,0.05); text-transform: capitalize;'><b>")
                                    .append(entry.getKey()).append(":</b></td>")
                                    .append("<td style='padding: 10px 0; color: #ffffff; font-size: 14px; text-align: right; border-bottom: 1px solid rgba(255,255,255,0.05);'>")
                                    .append(entry.getValue()).append("</td>")
                                    .append("</tr>");
                        }
                    }
                    html.append("</table></div>");

                    // RENDER ITEMS TABLE WITH IMAGES
                    if (orderDetails.get("items") instanceof java.util.List) {
                        java.util.List<java.util.Map<String, Object>> itemsList = (java.util.List<java.util.Map<String, Object>>) orderDetails
                                .get("items");
                        if (!itemsList.isEmpty()) {
                            html.append(
                                    "<h4 style='margin-bottom: 15px; color: #cbd5e1; font-size: 14px;'>Requested Artifacts:</h4>");

                            for (java.util.Map<String, Object> itemMap : itemsList) {
                                String itemName = (itemMap.get("name") != null) ? itemMap.get("name").toString()
                                        : "Unknown Item";
                                String qty = (itemMap.get("quantity") != null) ? itemMap.get("quantity").toString()
                                        : "1";
                                String price = (itemMap.get("price") != null) ? itemMap.get("price").toString() : "0";
                                String imgUrl = "https://via.placeholder.com/80/1a1a1a/ffffff?text=No+Image"; // Fallback

                                // Fetch Graphic from Catalog Service
                                try {
                                    Object menuItemId = itemMap.get("menuItemId");
                                    if (menuItemId != null && catalogUrl != null) {
                                        String cleanCat = catalogUrl.endsWith("/")
                                                ? catalogUrl.substring(0, catalogUrl.length() - 1)
                                                : catalogUrl;
                                        String itmUrl = cleanCat + "/menu/items/" + menuItemId.toString();
                                        java.util.Map<String, Object> catResp = restTemplate.getForObject(itmUrl,
                                                java.util.Map.class);
                                        if (catResp != null && catResp.get("data") instanceof java.util.Map) {
                                            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) catResp
                                                    .get("data");
                                            if (dataMap.get("imageUrl") != null) {
                                                imgUrl = dataMap.get("imageUrl").toString();
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                }

                                html.append(
                                        "<div style='display: flex; align-items: center; margin-bottom: 15px; background: rgba(255,255,255,0.03); padding: 15px; border-radius: 12px; border: 1px solid #1e293b;'>")
                                        .append("<div style='width: 70px; height: 70px; border-radius: 8px; overflow: hidden; margin-right: 15px; border: 1px solid rgba(255,255,255,0.1);'>")
                                        .append("<img src='").append(imgUrl)
                                        .append("' style='width: 100%; height: 100%; object-fit: cover;' alt='")
                                        .append(itemName).append("'/>")
                                        .append("</div>")
                                        .append("<div style='flex: 1;'>")
                                        .append("<h3 style='margin: 0 0 5px 0; font-size: 15px; color: #ffffff;'>")
                                        .append(itemName).append("</h3>")
                                        .append("<p style='margin: 0; font-size: 13px; color: #94a3b8;'>Quantity: <span style='color: #ffffff; font-weight: 700;'>")
                                        .append(qty).append("</span></p>")
                                        .append("</div>")
                                        .append("<div>")
                                        .append("<p style='margin: 0; font-size: 15px; color: #eab308; font-weight: 700;'>LKR ")
                                        .append(price).append("</p>")
                                        .append("</div>")
                                        .append("</div>");
                            }
                        }
                    }
                }

                html.append("<div style='text-align: center; margin-bottom: 30px;'>")
                        .append("<a href='https://gourmet-express.vercel.app/payments/checkout/").append(orderId)
                        .append("' style='display: inline-block; padding: 15px 35px; background-color: #eab308; color: #000000; text-decoration: none; border-radius: 100px; font-weight: 700; font-size: 14px; letter-spacing: 0.5px;'>PROCEED TO SECURE CHECKOUT →</a>")
                        .append("<p style='color: #475569; font-size: 12px; margin-top: 15px;'>(or visit https://gourmet-express.onrender.com)</p>")
                        .append("</div>");

                html.append("<div style='text-align: center; padding-top: 20px; border-top: 1px solid #1e293b;'>")
                        .append("<p style='color: #ef4444; font-size: 12px; margin: 0;'><i>Demo Mode Filter - Authentic Destination: ")
                        .append(user.getEmail()).append("</i></p>")
                        .append("</div>")
                        .append("</td></tr></table></body>");

                Content content = new Content("text/html", html.toString());
                Mail mail = new Mail(from, subject, to, content);

                SendGrid sg = new SendGrid(sendGridApiKey);
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                Response response = sg.api(request);
                return "SendGrid Status: " + response.getStatusCode() + " - Body: " + response.getBody();
            }
            return "User mapping aborted for ID: " + userId;
        } catch (Exception ex) {
            return "Failed to dispatch email: " + ex.getMessage();
        }
    }
}