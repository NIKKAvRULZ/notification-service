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
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

        @Autowired
        private RestTemplate restTemplate;

        @Value("${sendgrid.api-key:UNSET}")
        private String sendGridApiKey;

        @Value("${services.identity-url}")
        private String identityUrl;
        @Value("${services.payment-url}")
        private String paymentUrl;
        @Value("${services.order-url}")
        private String orderUrl;
        @Value("${services.catalog-url}")
        private String catalogUrl;

        // BRAND LUXURY PALETTE
        private static final String COLOR_GOLD = "#eab308";
        private static final String COLOR_GREEN = "#10b981";
        private static final String COLOR_BG = "#05080f";
        private static final String COLOR_SURFACE = "#0f172a";
        private static final String COLOR_BORDER = "#1e293b";
        private static final String COLOR_TEXT_DIM = "#94a3b8";
        private static final String COLOR_TEXT_MAIN = "#f8fafc";

        public void processNotification(NotificationRequest request) {
                System.out.println("--- TRIGGER: processNotification received for User: " + request.getUserId()
                                + ", Status: " + request.getStatus());
                try {
                        String userUrl = identityUrl + "/api/users/" + request.getUserId();
                        UserDTO user = restTemplate.getForObject(userUrl, UserDTO.class);

                        if (user != null) {
                                if ("succeeded".equalsIgnoreCase(request.getStatus())
                                                || "completed".equalsIgnoreCase(request.getStatus())) {
                                        sendReceiptEmail(request.getUserId(), request.getOrderId());
                                } else if ("pending".equalsIgnoreCase(request.getStatus())) {
                                        sendOrderPendingPaymentEmail(request.getUserId(), request.getOrderId());
                                }
                        }
                } catch (Exception e) {
                        System.err.println("!!! INTEGRATION ERROR: " + e.getMessage());
                }
        }

        private String getAuthDestination(UserDTO user) {
                // Hardcoded to your inbox for the demo/presentation
                return "nithika151@gmail.com";
        }

        private String getHeaderHtml(String title, String statusColor, String bannerUrl) {
                return "<!DOCTYPE html><html><body style='font-family: \"Segoe UI\", Roboto, sans-serif; background-color: "
                                + COLOR_BG + "; padding: 40px 20px; margin: 0; color: " + COLOR_TEXT_MAIN + ";'>" +
                                "<table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 0 auto; background-color: "
                                + COLOR_SURFACE + "; border-radius: 20px; border: 1px solid " + COLOR_BORDER
                                + "; overflow: hidden;'>" +
                                "<tr><td><img src='" + bannerUrl
                                + "' style='width: 100%; display: block; height: 180px; object-fit: cover;' /></td></tr>"
                                +
                                "<tr><td style='padding: 40px 35px 20px; text-align: center;'>" +
                                "<h1 style='color: " + COLOR_GOLD
                                + "; margin: 0; font-size: 26px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase;'>Gourmet<span style='color: #ffffff;'>Express</span></h1>"
                                +
                                "<div style='height: 1px; width: 40px; background: " + statusColor
                                + "; margin: 15px auto;'></div>" +
                                "<p style='color: " + statusColor
                                + "; margin: 5px 0 0; font-size: 13px; letter-spacing: 3px; text-transform: uppercase; font-weight: 700;'>"
                                + title + "</p>" +
                                "</td></tr>";
        }

        private String getFooterHtml(UserDTO user) {
                return "<tr><td style='padding: 30px; text-align: center; border-top: 1px solid " + COLOR_BORDER
                                + "; background: rgba(0,0,0,0.1);'>" +
                                "<p style='color: " + COLOR_TEXT_DIM
                                + "; font-size: 11px; margin: 0 0 12px; letter-spacing: 1px;'>GOURMET EXPRESS NETWORKS &bull; COLOMBO HQ</p>"
                                +
                                "<p style='color: #ef4444; font-size: 10px; margin: 0;'><i>INTERNAL TRACE: User verified as "
                                + user.getEmail() + "</i></p>" +
                                "</td></tr></table></body></html>";
        }

        @SuppressWarnings("unchecked")
        private void renderItemTable(StringBuilder html, Map<String, Object> orderDetails) {
                if (orderDetails == null)
                        return;

                // Handle nested 'data' wrapper if it exists
                if (orderDetails.containsKey("data") && orderDetails.get("data") instanceof Map) {
                        orderDetails = (Map<String, Object>) orderDetails.get("data");
                }

                List<Map<String, Object>> items = null;

                if (orderDetails.get("items") instanceof List) {
                        items = (List<Map<String, Object>>) orderDetails.get("items");
                } else if (orderDetails.containsKey("product")) {
                        // Legacy single-item order wrapper
                        Map<String, Object> single = new java.util.HashMap<>();
                        single.put("name", orderDetails.get("product"));
                        single.put("quantity", orderDetails.get("quantity"));
                        single.put("price", orderDetails.get("price"));
                        single.put("menuItemId", orderDetails.get("productId")); // sometimes maps to product ID
                        items = java.util.Collections.singletonList(single);
                }

                if (items == null || items.isEmpty())
                        return;

                html.append("<div style='margin-top: 30px;'><h4 style='font-size: 12px; text-transform: uppercase; letter-spacing: 1px; color: "
                                + COLOR_TEXT_DIM + "; margin-bottom: 15px;'>Your Selection</h4>");

                for (Map<String, Object> itm : items) {
                        String name = itm.getOrDefault("name", "Gourmet Dish").toString();
                        String qty = itm.getOrDefault("quantity", "1").toString();
                        String price = itm.getOrDefault("price", "0").toString();
                        // Elegant default imagery
                        String imgUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=150&h=150&fit=crop";

                        // TRY TO FETCH HIGH-RES IMAGE FROM CATALOG SERVICE
                        try {
                                Object menuItemId = itm.get("menuItemId");
                                if (menuItemId == null)
                                        menuItemId = itm.get("productId"); // Fallback for legacy

                                if (menuItemId != null) {
                                        String itmUrl = catalogUrl + "/menu/items/" + menuItemId.toString();
                                        Map<String, Object> catResp = restTemplate.getForObject(itmUrl, Map.class);
                                        if (catResp != null && catResp.get("data") instanceof Map) {
                                                Map<String, Object> data = (Map<String, Object>) catResp.get("data");
                                                if (data.get("imageUrl") != null) {
                                                        imgUrl = data.get("imageUrl").toString();
                                                }
                                        } else if (catResp != null && catResp.get("imageUrl") != null) {
                                                // Fallback for direct response
                                                imgUrl = catResp.get("imageUrl").toString();
                                        }
                                }
                        } catch (Exception ignored) {
                                System.out.println("--- GOURMET LOG: Using default image for " + name);
                        }

                        html.append("<div style='display: flex; align-items: center; padding: 15px; border-radius: 12px; margin-bottom: 10px; border: 1px solid "
                                        + COLOR_BORDER + "; background: rgba(255,255,255,0.02);'>")
                                        .append("<div style='width: 60px; height: 60px; border-radius: 8px; overflow: hidden; margin-right: 15px;'><img src='"
                                                        + imgUrl
                                                        + "' style='width: 100%; height: 100%; object-fit: cover;' /></div>")
                                        .append("<div style='flex: 1;'><div style='font-weight: 600; font-size: 15px; color: #fff; margin-bottom: 4px;'>"
                                                        + name + "</div><div style='color: " + COLOR_TEXT_DIM
                                                        + "; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px;'>Qty: "
                                                        + qty + "</div></div>")
                                        .append("<div style='font-weight: 800; font-size: 15px; color: " + COLOR_GOLD
                                                        + ";'>LKR " + price
                                                        + "</div>")
                                        .append("</div>");
                }
                html.append("</div>");
        }

        public String sendReceiptEmail(String userId, String orderId) {
                System.out.println(">>> ENTERING: sendReceiptEmail for " + orderId);
                try {
                        UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
                        PaymentResponse payResp = restTemplate.getForObject(
                                        paymentUrl + "/api/payments/order/" + orderId, PaymentResponse.class);
                        PaymentDTO payment = (payResp != null) ? payResp.getData() : null;
                        Map<String, Object> orderDetails = restTemplate.getForObject(orderUrl + "/orders/" + orderId,
                                        Map.class);

                        if (user == null || payment == null)
                                return "Data Sync Error";

                        String from = "it22061348@my.sliit.lk";
                        String to = getAuthDestination(user);
                        String subject = "Payment Confirmed - Receipt #"
                                        + orderId.substring(Math.max(0, orderId.length() - 8));

                        StringBuilder html = new StringBuilder();
                        html.append(getHeaderHtml("Transaction Succeeded", COLOR_GREEN,
                                        "https://images.unsplash.com/photo-1555244162-803834f70033?q=80&w=600&h=200&fit=crop"))
                                        .append("<tr><td style='padding: 0 40px 40px;'>")
                                        .append("<h2 style='font-weight: 300; font-size: 22px;'>Artifacts are being <span style='color: "
                                                        + COLOR_GOLD + ";'>prepared</span>.</h2>")
                                        .append("<p style='color: " + COLOR_TEXT_DIM
                                                        + "; line-height: 1.6;'>Greetings, " + user.getUsername()
                                                        + ". Your payment has been verified and your order is now entering the preparation phase.</p>")
                                        .append("<div style='background: rgba(16,185,129,0.05); border: 1px solid rgba(16,185,129,0.2); border-radius: 12px; padding: 20px; margin-bottom: 25px;'>")
                                        .append("<table width='100%'>")
                                        .append("<tr><td style='color: " + COLOR_TEXT_DIM
                                                        + "; font-size: 13px;'>Reference</td><td style='text-align: right; font-family: monospace; font-size: 13px;'>"
                                                        + payment.getStripePaymentIntentId() + "</td></tr>")
                                        .append("<tr><td style='color: " + COLOR_TEXT_MAIN
                                                        + "; font-size: 18px; font-weight: 800; padding-top: 15px;'>Total Paid</td><td style='text-align: right; color: "
                                                        + COLOR_GOLD
                                                        + "; font-size: 22px; font-weight: 800; padding-top: 15px;'>LKR "
                                                        + payment.getAmount() + "</td></tr>")
                                        .append("</table></div>");

                        renderItemTable(html, orderDetails);

                        html.append("<div style='text-align: center; margin-top: 40px;'>")
                                        .append("<a href='https://food-ordering-frontend-2c1l.onrender.com' style='background: "
                                                        + COLOR_GOLD
                                                        + "; color: #000; padding: 14px 35px; border-radius: 50px; text-decoration: none; font-weight: 700; font-size: 13px;'>TRACK SHIPMENT STATUS</a>")
                                        .append("</div></td></tr>")
                                        .append(getFooterHtml(user));

                        return dispatchEmail(from, to, subject, html.toString());
                } catch (Exception e) {
                        return "Receipt Failed: " + e.getMessage();
                }
        }

        public String sendWelcomeEmail(String userId) {
                System.out.println(">>> ENTERING: sendWelcomeEmail for " + userId);
                try {
                        UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
                        if (user == null)
                                return "User Missing";

                        String from = "it22061348@my.sliit.lk";
                        String to = getAuthDestination(user);
                        String subject = "Identity Verified - Welcome to Gourmet Express";

                        StringBuilder html = new StringBuilder();
                        html.append(getHeaderHtml("Access Authorized", "#3b82f6",
                                        "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?q=80&w=600&h=200&fit=crop"))
                                        .append("<tr><td style='padding: 0 40px 40px;'>")
                                        .append("<h2 style='font-weight: 300; font-size: 24px; text-align: center;'>Welcome to the <span style='color: "
                                                        + COLOR_GOLD + ";'>Circle</span></h2>")
                                        .append("<p style='color: " + COLOR_TEXT_DIM
                                                        + "; line-height: 1.6; text-align: center;'>Your credentials have been harmonized with our global network. Premium culinary exploration awaits.</p>")
                                        .append("<div style='text-align: center; margin-top: 40px;'>")
                                        .append("<a href='https://food-ordering-frontend-2c1l.onrender.com' style='background: "
                                                        + COLOR_GOLD
                                                        + "; color: #000; padding: 15px 40px; border-radius: 50px; text-decoration: none; font-weight: 800; font-size: 14px;'>BEGIN EXPLORATION →</a>")
                                        .append("</div></td></tr>")
                                        .append(getFooterHtml(user));

                        return dispatchEmail(from, to, subject, html.toString());
                } catch (Exception e) {
                        return "Welcome Failed: " + e.getMessage();
                }
        }

        public String sendOrderPendingPaymentEmail(String userId, String orderId) {
                System.out.println(">>> ENTERING: sendOrderPending for " + orderId);
                try {
                        UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
                        Map<String, Object> orderDetails = restTemplate.getForObject(orderUrl + "/orders/" + orderId,
                                        Map.class);
                        if (user == null || orderDetails == null)
                                return "Data Lost";

                        String from = "it22061348@my.sliit.lk";
                        String to = getAuthDestination(user);
                        String subject = "Priority Action - Secure Your Artifacts";

                        StringBuilder html = new StringBuilder();
                        html.append(getHeaderHtml("Payment Required", COLOR_GOLD,
                                        "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=80&w=600&h=200&fit=crop"))
                                        .append("<tr><td style='padding: 0 40px 40px;'>")
                                        .append("<p style='color: " + COLOR_TEXT_DIM
                                                        + "; line-height: 1.6;'>Your selection is locked. To initiate synthesis and secure delivery, finalize your transfer through our encrypted payment gateway.</p>");

                        renderItemTable(html, orderDetails);

                        html.append("<div style='text-align: center; margin-top: 40px;'>")
                                        .append("<a href='https://food-ordering-frontend-2c1l.onrender.com"
                                                        + orderId + "' style='background: " + COLOR_GOLD
                                                        + "; color: #000; padding: 18px 45px; border-radius: 100px; text-decoration: none; font-weight: 800; font-size: 14px; letter-spacing: 1px;'>EXECUTE PAYMENT →</a>")
                                        .append("</div></td></tr>")
                                        .append(getFooterHtml(user));

                        return dispatchEmail(from, to, subject, html.toString());
                } catch (Exception e) {
                        return "Pending Failed: " + e.getMessage();
                }
        }

        private String dispatchEmail(String fromEmail, String toEmail, String subject, String htmlBody) {
                System.out.println("--- DISPATCHING to " + toEmail + " via SendGrid...");
                try {
                        Email from = new Email(fromEmail);
                        from.setName("Gourmet Express Concierge");
                        Email to = new Email(toEmail);
                        Content content = new Content("text/html", htmlBody);
                        Mail mail = new Mail(from, subject, to, content);

                        SendGrid sg = new SendGrid(sendGridApiKey);
                        Request request = new Request();
                        request.setMethod(Method.POST);
                        request.setEndpoint("mail/send");
                        request.setBody(mail.build());
                        Response response = sg.api(request);

                        System.out.println(">>> SENDGRID STATUS: " + response.getStatusCode());
                        return "Success: " + response.getStatusCode();
                } catch (IOException e) {
                        System.err.println("!!! SENDGRID ERROR: " + e.getMessage());
                        return "SendGrid Error: " + e.getMessage();
                }
        }
}