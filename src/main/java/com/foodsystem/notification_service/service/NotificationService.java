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

        @Value("${sendgrid.api-key}")
        private String sendGridApiKey;

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

        // BRAND COLOR PALETTE
        private static final String COLOR_GOLD = "#eab308";
        private static final String COLOR_GREEN = "#10b981";
        private static final String COLOR_BG = "#05080f";
        private static final String COLOR_SURFACE = "#0f172a";
        private static final String COLOR_BORDER = "#1e293b";
        private static final String COLOR_TEXT_DIM = "#94a3b8";
        private static final String COLOR_TEXT_MAIN = "#f8fafc";

        public void processNotification(NotificationRequest request) {
                try {
                        String userUrl = identityUrl + "/api/users/" + request.getUserId();
                        UserDTO user = restTemplate.getForObject(userUrl, UserDTO.class);

                        if (user != null) {
                                // If it's a payment status update, send the receipt
                                if ("succeeded".equalsIgnoreCase(request.getStatus())
                                                || "completed".equalsIgnoreCase(request.getStatus())) {
                                        sendReceiptEmail(request.getUserId(), request.getOrderId());
                                } else if ("pending".equalsIgnoreCase(request.getStatus())) {
                                        sendOrderPendingPaymentEmail(request.getUserId(), request.getOrderId());
                                }
                        }
                } catch (Exception e) {
                        System.err.println("Integration Error: " + e.getMessage());
                }
        }

        private String getAuthDestination(UserDTO user) {
                // Hardcoded for demo/testing as requested previously, change to user.getEmail()
                // for production
                return "nithika151@gmail.com";
        }

        private String getHeaderHtml(String title, String statusColor, String bannerUrl) {
                String websiteUrl = "https://food-ordering-frontend-2c1l.onrender.com/";
                return "<!DOCTYPE html><html lang='en'><head>" +
                                "<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                                +
                                "<style>" +
                                "  body { margin: 0; padding: 0; background-color: " + COLOR_BG
                                + "; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }"
                                +
                                "  .wrapper { width: 100%; table-layout: fixed; background-color: " + COLOR_BG
                                + "; padding-bottom: 40px; }" +
                                "  .main { background-color: " + COLOR_SURFACE
                                + "; margin: 0 auto; width: 100%; max-width: 600px; border-spacing: 0; color: "
                                + COLOR_TEXT_MAIN + "; border-radius: 12px; overflow: hidden; border: 1px solid "
                                + COLOR_BORDER + "; }" +
                                "  .banner { width: 100%; height: auto; display: block; }" +
                                "  .content { padding: 30px 25px; }" +
                                "  .btn { display: inline-block; padding: 14px 30px; background-color: " + COLOR_GOLD
                                + " !important; color: #000000 !important; text-decoration: none; border-radius: 50px; font-weight: 800; font-size: 14px; letter-spacing: 1px; text-transform: uppercase; }"
                                +
                                "  @media screen and (max-width: 600px) {" +
                                "    .content { padding: 20px 15px !important; }" +
                                "    h1 { font-size: 20px !important; }" +
                                "    .hero-title { font-size: 24px !important; }" +
                                "  }" +
                                "</style></head><body>" +
                                "<center class='wrapper'>" +
                                "<table class='main' width='100%' cellpadding='0' cellspacing='0'>" +
                                (bannerUrl != null
                                                ? "<tr><td><img src='" + bannerUrl
                                                                + "' class='banner' alt='Gourmet' /></td></tr>"
                                                : "")
                                +
                                "<tr><td class='content' style='text-align: center; border-bottom: 1px solid "
                                + COLOR_BORDER + ";'>" +
                                "<a href='" + websiteUrl + "' style='text-decoration: none;'><h1 style='color: "
                                + COLOR_GOLD
                                + "; margin: 0; font-size: 22px; font-weight: 900; letter-spacing: 2px; text-transform: uppercase;'>Gourmet<span style='color: #ffffff;'>Express</span></h1></a>"
                                +
                                "<p style='color: " + statusColor
                                + "; margin: 10px 0 0; font-size: 12px; letter-spacing: 3px; text-transform: uppercase; font-weight: 700;'>"
                                + title + "</p>" +
                                "</td></tr>";
        }

        private String getFooterHtml(UserDTO user) {
                return "<tr><td class='content' style='text-align: center; background-color: rgba(0,0,0,0.2);'>" +
                                "<p style='color: " + COLOR_TEXT_DIM
                                + "; font-size: 11px; margin: 0 0 10px; line-height: 1.5;'>Gourmet Express &bull; Cyber-Kitchen Node 01 &bull; Colombo, SL</p>"
                                +
                                "<p style='color: " + COLOR_TEXT_DIM
                                + "; font-size: 11px; margin: 0;'>Sent to <span style='color: " + COLOR_GOLD + ";'>"
                                + user.getEmail() + "</span></p>" +
                                "<p style='color: " + COLOR_TEXT_DIM
                                + "; font-size: 10px; margin-top: 15px;'>&copy; 2024 Gourmet Express. All rights reserved.</p>"
                                +
                                "</td></tr></table></center></body></html>";
        }

        public String sendWelcomeEmail(String userId) {
                try {
                        UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
                        if (user == null)
                                return "User Mapping Failed";

                        Email from = new Email("it22061348@my.sliit.lk");
                        Email to = new Email(getAuthDestination(user));
                        String subject = "Access Granted - Welcome to Gourmet Express";

                        StringBuilder html = new StringBuilder();
                        html.append(getHeaderHtml("Identity Confirmed", "#3b82f6",
                                        "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=1200"))
                                        .append("<tr><td class='content'>")
                                        .append("<h2 class='hero-title' style='font-weight: 300; font-size: 28px; margin: 0 0 20px; color: #fff;'>Welcome, <span style='color: "
                                                        + COLOR_GOLD + ";'>" + user.getUsername() + "</span>.</h2>")
                                        .append("<p style='font-size: 15px; color: " + COLOR_TEXT_DIM
                                                        + "; line-height: 1.6; margin-bottom: 25px;'>Your profile has been successfully integrated into the Gourmet Express mainframe. You can now request artisanal provisions and monitor your delivery coordinates in real-time.</p>")
                                        .append("<div style='background: rgba(255,255,255,0.03); border: 1px solid "
                                                        + COLOR_BORDER
                                                        + "; border-left: 4px solid #3b82f6; border-radius: 8px; padding: 20px;'>")
                                        .append("<table width='100%'>")
                                        .append("<tr><td style='color: " + COLOR_TEXT_DIM
                                                        + "; font-size: 13px;'>Username</td><td style='text-align: right; color: #fff;'>"
                                                        + user.getUsername() + "</td></tr>")
                                        .append("<tr><td style='color: " + COLOR_TEXT_DIM
                                                        + "; font-size: 13px; padding-top: 8px;'>Email Node</td><td style='text-align: right; color: "
                                                        + COLOR_GOLD + "; padding-top: 8px;'>" + user.getEmail()
                                                        + "</td></tr>")
                                        .append("</table></div>")
                                        .append("<div style='text-align: center; margin-top: 35px;'>")
                                        .append("<a href='https://food-ordering-frontend-2c1l.onrender.com/' class='btn'>LAUNCH APP →</a>")
                                        .append("</div></td></tr>")
                                        .append(getFooterHtml(user));

                        return dispatchEmail(from, to, subject, html.toString());
                } catch (Exception e) {
                        return "Error: " + e.getMessage();
                }
        }

        @SuppressWarnings("unchecked")
        public String sendOrderPendingPaymentEmail(String userId, String orderId) {
                try {
                        UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
                        java.util.Map<String, Object> orderDetails = restTemplate
                                        .getForObject(orderUrl + "/orders/" + orderId, java.util.Map.class);
                        if (user == null || orderDetails == null)
                                return "Data Error";

                        String shortId = orderId.length() >= 8 ? orderId.substring(orderId.length() - 8).toUpperCase()
                                        : orderId;
                        Email from = new Email("it22061348@my.sliit.lk");
                        Email to = new Email(getAuthDestination(user));
                        String subject = "Action Required - Order #" + shortId;

                        StringBuilder html = new StringBuilder();
                        html.append(getHeaderHtml("Pending Authorization", COLOR_GOLD,
                                        "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=80&w=1200"))
                                        .append("<tr><td class='content'>")
                                        .append("<p style='font-size: 15px; color: " + COLOR_TEXT_DIM
                                                        + "; line-height: 1.6;'>Hello <b>" + user.getUsername()
                                                        + "</b>, your request <b>#" + shortId
                                                        + "</b> is synchronized. To begin preparation, please authorize the credit transfer.</p>");

                        renderItemTable(html, orderDetails);

                        html.append("<div style='text-align: center; margin-top: 35px;'>")
                                        .append("<a href='https://food-ordering-frontend-2c1l.onrender.com/payments/checkout/"
                                                        + orderId + "' class='btn'>SETTLE PROVISIONS →</a>")
                                        .append("</div></td></tr>")
                                        .append(getFooterHtml(user));

                        return dispatchEmail(from, to, subject, html.toString());
                } catch (Exception e) {
                        return "Error: " + e.getMessage();
                }
        }

        @SuppressWarnings("unchecked")
        public String sendReceiptEmail(String userId, String orderId) {
                try {
                        UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
                        PaymentResponse payResp = restTemplate.getForObject(
                                        paymentUrl + "/api/payments/order/" + orderId, PaymentResponse.class);
                        PaymentDTO payment = (payResp != null) ? payResp.getData() : null;
                        java.util.Map<String, Object> orderDetails = restTemplate
                                        .getForObject(orderUrl + "/orders/" + orderId, java.util.Map.class);

                        if (user == null || payment == null)
                                return "Data Missing";

                        String shortId = orderId.length() >= 8 ? orderId.substring(orderId.length() - 8).toUpperCase()
                                        : orderId;
                        Email from = new Email("it22061348@my.sliit.lk");
                        Email to = new Email(getAuthDestination(user));
                        String subject = "Receipt - Order #" + shortId;

                        StringBuilder html = new StringBuilder();
                        html.append(getHeaderHtml("Payment Verified", COLOR_GREEN,
                                        "https://images.unsplash.com/photo-1555244162-803834f70033?q=80&w=1200"))
                                        .append("<tr><td class='content'>")
                                        .append("<h2 class='hero-title' style='font-weight: 300; font-size: 26px; margin: 0 0 10px; color: #fff;'>Order <span style='color: "
                                                        + COLOR_GOLD + ";'>Confirmed</span></h2>")
                                        .append("<p style='font-size: 15px; color: " + COLOR_TEXT_DIM
                                                        + "; line-height: 1.6;'>Your payment has been successfully authorized. Our chefs have received your coordinates and are preparing your provisions.</p>")

                                        .append("<div style='background: rgba(16,185,129,0.05); border: 1px solid rgba(16,185,129,0.1); border-radius: 8px; padding: 20px; margin-top: 25px;'>")
                                        .append("<table width='100%' style='border-spacing: 0;'>")
                                        .append("<tr><td style='color: " + COLOR_TEXT_DIM
                                                        + "; font-size: 12px; padding: 4px 0;'>Reference</td><td style='text-align: right; color: #fff; font-size: 12px; font-family: monospace;'>"
                                                        + (payment.getStripePaymentIntentId()
                                                                        .length() > 15 ? payment.getStripePaymentIntentId().substring(0, 15) + "..." : payment.getStripePaymentIntentId())
                                                        + "</td></tr>")
                                        .append("<tr><td style='color: " + COLOR_TEXT_DIM
                                                        + "; font-size: 12px; padding: 4px 0;'>Destination</td><td style='text-align: right; color: #fff; font-size: 12px;'>"
                                                        + (user.getDeliveryAddress() != null ? user.getDeliveryAddress()
                                                                        : "Standard Pickup")
                                                        + "</td></tr>")
                                        .append("<tr><td style='color: #fff; font-size: 16px; font-weight: 800; padding-top: 15px;'>Amount Paid</td><td style='text-align: right; color: "
                                                        + COLOR_GOLD
                                                        + "; font-size: 20px; font-weight: 800; padding-top: 15px;'>LKR "
                                                        + payment.getAmount() + "</td></tr>")
                                        .append("</table></div>");

                        renderItemTable(html, orderDetails);

                        html.append("<div style='text-align: center; margin-top: 40px;'>")
                                        .append("<a href='https://food-ordering-frontend-2c1l.onrender.com/' class='btn'>TRACK ORDER →</a>")
                                        .append("</div></td></tr>")
                                        .append(getFooterHtml(user));

                        return dispatchEmail(from, to, subject, html.toString());
                } catch (Exception e) {
                        return "Error: " + e.getMessage();
                }
        }

        @SuppressWarnings("unchecked")
        private void renderItemTable(StringBuilder html, java.util.Map<String, Object> orderDetails) {
                if (orderDetails == null || !(orderDetails.get("items") instanceof java.util.List))
                        return;
                java.util.List<java.util.Map<String, Object>> items = (java.util.List<java.util.Map<String, Object>>) orderDetails
                                .get("items");
                if (items.isEmpty())
                        return;

                html.append("<div style='margin-top: 30px;'><h4 style='font-size: 11px; text-transform: uppercase; letter-spacing: 2px; color: "
                                + COLOR_TEXT_DIM + "; margin-bottom: 15px; border-bottom: 1px solid " + COLOR_BORDER
                                + "; padding-bottom: 8px;'>Requested Artifacts</h4>");

                for (java.util.Map<String, Object> itm : items) {
                        String name = (itm.get("name") != null) ? itm.get("name").toString() : "Gourmet Provision";
                        String qty = (itm.get("quantity") != null) ? itm.get("quantity").toString() : "1";
                        String price = (itm.get("price") != null) ? itm.get("price").toString() : "0";
                        String imgUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=200&h=200";

                        try {
                                Object menuId = itm.get("menuItemId");
                                if (menuId != null && catalogUrl != null) {
                                        java.util.Map<String, Object> cat = restTemplate.getForObject(
                                                        catalogUrl + "/menu/items/" + menuId, java.util.Map.class);
                                        if (cat != null && cat.get("data") instanceof java.util.Map) {
                                                java.util.Map<String, Object> d = (java.util.Map<String, Object>) cat
                                                                .get("data");
                                                if (d.get("imageUrl") != null)
                                                        imgUrl = d.get("imageUrl").toString();
                                        }
                                }
                        } catch (Exception ignored) {
                        }

                        html.append("<table width='100%' style='margin-bottom: 12px; border-spacing: 0;'><tr>")
                                        .append("<td style='width: 50px; vertical-align: middle;'><img src='" + imgUrl
                                                        + "' style='width: 45px; height: 45px; border-radius: 6px; object-fit: cover; border: 1px solid "
                                                        + COLOR_BORDER + ";' /></td>")
                                        .append("<td style='padding-left: 15px;'><div style='font-weight: 600; font-size: 14px; color: #fff;'>"
                                                        + name + "</div><div style='color: " + COLOR_TEXT_DIM
                                                        + "; font-size: 12px;'>Quantity: " + qty + "</div></td>")
                                        .append("<td style='text-align: right; color: " + COLOR_GOLD
                                                        + "; font-weight: 800; font-size: 14px;'>LKR " + price
                                                        + "</td>")
                                        .append("</tr></table>");
                }
                html.append("</div>");
        }

        private String dispatchEmail(Email from, Email to, String subject, String htmlBody) {
                try {
                        from.setName("Gourmet Express Concierge");
                        String plainText = subject
                                        + "\n\nHello from Gourmet Express. Please view in HTML mode for the full experience.";

                        Mail mail = new Mail();
                        mail.setFrom(from);
                        mail.setSubject(subject);
                        Personalization p = new Personalization();
                        p.addTo(to);
                        mail.addPersonalization(p);
                        mail.addContent(new Content("text/plain", plainText));
                        mail.addContent(new Content("text/html", htmlBody));

                        SendGrid sg = new SendGrid(sendGridApiKey);
                        Request r = new Request();
                        r.setMethod(Method.POST);
                        r.setEndpoint("mail/send");
                        r.setBody(mail.build());
                        Response resp = sg.api(r);
                        return "Status: " + resp.getStatusCode();
                } catch (IOException e) {
                        return "API Error: " + e.getMessage();
                }
        }
}
