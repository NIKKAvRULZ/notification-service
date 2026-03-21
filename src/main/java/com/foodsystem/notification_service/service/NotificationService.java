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
        return "<!DOCTYPE html><html><body style='font-family: \"Segoe UI\", Roboto, sans-serif; background-color: "
                + COLOR_BG + "; padding: 40px 20px; margin: 0; color: " + COLOR_TEXT_MAIN + ";'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 0 auto; background-color: "
                + COLOR_SURFACE + "; border-radius: 20px; border: 1px solid " + COLOR_BORDER
                + "; overflow: hidden; box-shadow: 0 15px 50px rgba(0,0,0,0.6);'>" +
                (bannerUrl != null ? "<tr><td><img src='" + bannerUrl
                        + "' style='width: 100%; display: block; height: 200px; object-fit: cover;' /></td></tr>" : "")
                +
                "<tr><td style='padding: 40px 35px 20px; text-align: center;'>" +
                "<h1 style='color: " + COLOR_GOLD
                + "; margin: 0; font-size: 24px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase;'>Gourmet<span style='color: #ffffff;'>Express</span></h1>"
                +
                "<div style='height: 1px; width: 40px; background: " + statusColor + "; margin: 15px auto;'></div>" +
                "<p style='color: " + statusColor
                + "; margin: 5px 0 0; font-size: 13px; letter-spacing: 3px; text-transform: uppercase; font-weight: 700;'>"
                + title + "</p>" +
                "</td></tr>";
    }

    private String getFooterHtml(UserDTO user) {
        return "<tr><td style='padding: 30px; text-align: center; border-top: 1px solid " + COLOR_BORDER
                + "; background: rgba(0,0,0,0.1);'>" +
                "<p style='color: " + COLOR_TEXT_DIM
                + "; font-size: 11px; margin: 0 0 12px; text-transform: uppercase; letter-spacing: 1px;'>Gourmet Express HQ &bull; 123 Culinary Drive &bull; Colombo, SL</p>"
                +
                "<p style='color: " + COLOR_TEXT_DIM
                + "; font-size: 12px; margin: 0 0 10px;'>Questions regarding your order? Contact our Concierge at support@gourmetexpress.com</p>"
                +
                "<p style='color: #ef4444; font-size: 10px; margin: 0;'><i>DEMO TRACE: Authenticated for "
                + user.getEmail() + "</i></p>" +
                "</td></tr></table></body></html>";
    }

    public String sendWelcomeEmail(String userId) {
        try {
            UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
            if (user == null)
                return "User Mapping Failed";

            Email from = new Email("it22061348@my.sliit.lk");
            Email to = new Email(getAuthDestination(user));
            String subject = "Access Granted - Welcome to the Gourmet Express Network";

            StringBuilder html = new StringBuilder();
            html.append(getHeaderHtml("Identity Confirmed", "#3b82f6",
                    "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=600&h=200&fit=crop"))
                    .append("<tr><td style='padding: 0 40px 40px;'>")
                    .append("<h2 style='font-weight: 300; font-size: 26px; margin: 0 0 20px;'>Welcome to the <span style='color: "
                            + COLOR_GOLD + ";'>Mainframe</span></h2>")
                    .append("<p style='font-size: 16px; color: " + COLOR_TEXT_DIM + "; line-height: 1.6;'>Hello <b>"
                            + user.getUsername() + "</b>,</p>")
                    .append("<p style='font-size: 15px; color: " + COLOR_TEXT_DIM
                            + "; line-height: 1.6; margin-bottom: 30px;'>Your client node has been successfully synchronized with the Gourmet Express network. You are now authorized to request premium culinary provisions and artisanal artifacts.</p>")
                    .append("<div style='background: rgba(255,255,255,0.02); border: 1px solid " + COLOR_BORDER
                            + "; border-left: 4px solid #3b82f6; border-radius: 12px; padding: 25px;'>")
                    .append("<table width='100%'>")
                    .append("<tr><td style='color: " + COLOR_TEXT_DIM
                            + "; font-size: 14px; padding-bottom: 10px;'>Signature</td><td style='text-align: right; font-weight: 600;'>"
                            + user.getUsername() + "</td></tr>")
                    .append("<tr><td style='color: " + COLOR_TEXT_DIM
                            + "; font-size: 14px;'>Email Vector</td><td style='text-align: right; color: " + COLOR_GOLD
                            + ";'>" + user.getEmail() + "</td></tr>")
                    .append("</table></div>")
                    .append("<div style='text-align: center; margin-top: 40px;'>")
                    .append("<a href='https://gourmet-express.vercel.app/catalog' style='background: " + COLOR_GOLD
                            + "; color: #000; padding: 16px 40px; border-radius: 50px; text-decoration: none; font-weight: 800; font-size: 14px; letter-spacing: 1px;'>EXPLORE CATALOG →</a>")
                    .append("</div></td></tr>")
                    .append(getFooterHtml(user));

            return dispatchEmail(from, to, subject, html.toString());
        } catch (Exception e) {
            return "Failed to send Welcome Email: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    public String sendOrderPendingPaymentEmail(String userId, String orderId) {
        try {
            UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
            java.util.Map<String, Object> orderDetails = restTemplate.getForObject(orderUrl + "/orders/" + orderId,
                    java.util.Map.class);

            if (user == null || orderDetails == null)
                return "Data Fetch Error";

            String shortId = orderId.length() >= 8 ? orderId.substring(orderId.length() - 8).toUpperCase() : orderId;
            Email from = new Email("it22061348@my.sliit.lk");
            Email to = new Email(getAuthDestination(user));
            String subject = "Action Required - Settle Provisions for Order #" + shortId;

            StringBuilder html = new StringBuilder();
            html.append(getHeaderHtml("Pending Authorization", COLOR_GOLD,
                    "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=80&w=600&h=200&fit=crop"))
                    .append("<tr><td style='padding: 0 40px 40px;'>")
                    .append("<p style='font-size: 16px; color: " + COLOR_TEXT_DIM + ";'>Hello <b>" + user.getUsername()
                            + "</b>,</p>")
                    .append("<p style='font-size: 15px; color: " + COLOR_TEXT_DIM
                            + "; line-height: 1.6;'>Your request <b>#" + shortId
                            + "</b> is queued. To commence synthesis and dispatch, please finalize the credit transfer via our secure channel.</p>");

            // Item List
            renderItemTable(html, orderDetails);

            html.append("<div style='text-align: center; margin: 40px 0 20px;'>")
                    .append("<a href='https://gourmet-express.vercel.app/payments/checkout/" + orderId
                            + "' style='background: " + COLOR_GOLD
                            + "; color: #000; padding: 18px 45px; border-radius: 50px; text-decoration: none; font-weight: 800; font-size: 15px; letter-spacing: 1px;'>EXECUTE PAYMENT NOW →</a>")
                    .append("</div>")
                    .append("</td></tr>")
                    .append(getFooterHtml(user));

            return dispatchEmail(from, to, subject, html.toString());
        } catch (Exception e) {
            return "Failed to send Pending Email: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    public String sendReceiptEmail(String userId, String orderId) {
        try {
            UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
            PaymentResponse payResp = restTemplate.getForObject(paymentUrl + "/api/payments/order/" + orderId,
                    PaymentResponse.class);
            PaymentDTO payment = (payResp != null) ? payResp.getData() : null;
            java.util.Map<String, Object> orderDetails = restTemplate.getForObject(orderUrl + "/orders/" + orderId,
                    java.util.Map.class);

            if (user == null || payment == null)
                return "Data Sync Missing";

            String shortId = orderId.length() >= 8 ? orderId.substring(orderId.length() - 8).toUpperCase() : orderId;
            Email from = new Email("it22061348@my.sliit.lk");
            Email to = new Email(getAuthDestination(user));
            String subject = "Transfer Successful - Receipt #" + shortId;

            StringBuilder html = new StringBuilder();
            html.append(getHeaderHtml("Payment Verified", COLOR_GREEN,
                    "https://images.unsplash.com/photo-1555244162-803834f70033?q=80&w=600&h=200&fit=crop"))
                    .append("<tr><td style='padding: 0 40px 40px;'>")
                    .append("<h2 style='font-weight: 300; font-size: 24px; margin: 0 0 10px;'>Order <span style='color: "
                            + COLOR_GOLD + ";'>Confirmed</span></h2>")
                    .append("<p style='font-size: 15px; color: " + COLOR_TEXT_DIM + "; line-height: 1.6;'>Gratitude, "
                            + user.getUsername()
                            + ". Your payment has been authorized. Our artisans have begun preparation of your requested provisions.</p>")

                    // Transaction Card
                    .append("<div style='background: rgba(16,185,129,0.05); border: 1px solid rgba(16,185,129,0.2); border-radius: 12px; padding: 25px; margin-bottom: 30px;'>")
                    .append("<div style='font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: "
                            + COLOR_GREEN + "; margin-bottom: 10px; font-weight: 700;'>Transaction Details</div>")
                    .append("<table width='100%'>")
                    .append("<tr><td style='color: " + COLOR_TEXT_DIM
                            + "; font-size: 14px; padding: 5px 0;'>Reference Trace</td><td style='text-align: right; font-family: monospace; font-size: 14px;'>"
                            + payment.getStripePaymentIntentId() + "</td></tr>")
                    .append("<tr><td style='color: " + COLOR_TEXT_DIM
                            + "; font-size: 14px; padding: 5px 0;'>Dispatch Point</td><td style='text-align: right;'>"
                            + (user.getDeliveryAddress() != null ? user.getDeliveryAddress() : "Standard Pickup")
                            + "</td></tr>")
                    .append("<tr><td style='color: " + COLOR_TEXT_MAIN
                            + "; font-size: 18px; font-weight: 800; padding: 15px 0 0;'>Total Paid</td><td style='text-align: right; color: "
                            + COLOR_GOLD + "; font-size: 22px; font-weight: 800; padding: 15px 0 0;'>LKR "
                            + payment.getAmount() + "</td></tr>")
                    .append("</table></div>");

            // Item List
            renderItemTable(html, orderDetails);

            html.append("<div style='text-align: center; margin-top: 40px;'>")
                    .append("<a href='https://gourmet-express.vercel.app/orders' style='border: 1px solid "
                            + COLOR_BORDER
                            + "; color: #fff; padding: 14px 35px; border-radius: 50px; text-decoration: none; font-weight: 600; font-size: 13px;'>TRACK SHIPMENT STATUS</a>")
                    .append("</div></td></tr>")
                    .append(getFooterHtml(user));

            return dispatchEmail(from, to, subject, html.toString());
        } catch (Exception e) {
            return "Receipt Dispatch Failure: " + e.getMessage();
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

        html.append(
                "<div style='margin-top: 30px;'><h4 style='font-size: 13px; text-transform: uppercase; letter-spacing: 1px; color: "
                        + COLOR_TEXT_DIM + "; margin-bottom: 15px;'>Order Configuration</h4>");

        for (java.util.Map<String, Object> itm : items) {
            String name = (itm.get("name") != null) ? itm.get("name").toString() : "Gourmet Item";
            String qty = (itm.get("quantity") != null) ? itm.get("quantity").toString() : "1";
            String price = (itm.get("price") != null) ? itm.get("price").toString() : "0";
            String imgUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=100&h=100&fit=crop";

            // Dynamic Lookups
            try {
                Object menuItemId = itm.get("menuItemId");
                if (menuItemId != null && catalogUrl != null) {
                    String itmUrl = catalogUrl + "/menu/items/" + menuItemId.toString();
                    java.util.Map<String, Object> catResp = restTemplate.getForObject(itmUrl, java.util.Map.class);
                    if (catResp != null && catResp.get("data") instanceof java.util.Map) {
                        java.util.Map<String, Object> data = (java.util.Map<String, Object>) catResp.get("data");
                        if (data.get("imageUrl") != null)
                            imgUrl = data.get("imageUrl").toString();
                    }
                }
            } catch (Exception ignored) {
            }

            html.append(
                    "<div style='display: flex; align-items: center; padding: 15px; background: rgba(255,255,255,0.02); border-radius: 12px; margin-bottom: 12px; border: 1px solid "
                            + COLOR_BORDER + ";'>")
                    .append("<div style='width: 50px; height: 50px; border-radius: 10px; overflow: hidden; margin-right: 15px; border: 1px solid "
                            + COLOR_BORDER + ";'><img src='" + imgUrl
                            + "' style='width: 100%; height: 100%; object-fit: cover;' /></div>")
                    .append("<div style='flex: 1;'><div style='font-weight: 600; font-size: 15px; color: #ffffff;'>"
                            + name + "</div><div style='color: " + COLOR_TEXT_DIM + "; font-size: 13px;'>Qty: <b>" + qty
                            + "</b></div></div>")
                    .append("<div style='font-weight: 700; color: " + COLOR_GOLD + "; font-size: 15px;'>LKR " + price
                            + "</div>")
                    .append("</div>");
        }
        html.append("</div>");
    }

    private String dispatchEmail(Email from, Email to, String subject, String htmlBody) {
        try {
            // Set a display name for the sender to look more professional
            from.setName("Gourmet Express Concierge");

            // Generate a simple plain-text fallback to satisfy spam filters (Multi-part
            // MIME)
            String plainText = subject + "\n\nHello, this is a message from Gourmet Express. " +
                    "Please view this email in an HTML-enabled client for the full experience.";

            Content textContent = new Content("text/plain", plainText);
            Content htmlContent = new Content("text/html", htmlBody);

            Mail mail = new Mail();
            mail.setFrom(from);
            mail.setSubject(subject);
            Personalization personalization = new Personalization();
            personalization.addTo(to);
            mail.addPersonalization(personalization);
            mail.addContent(textContent);
            mail.addContent(htmlContent);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            return "Status: " + response.getStatusCode();
        } catch (IOException e) {
            return "API Error: " + e.getMessage();
        }
    }
}
