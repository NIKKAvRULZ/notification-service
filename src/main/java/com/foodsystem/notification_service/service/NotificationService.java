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

        // BRAND COLOR PALETTE
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
                                System.out.println("--- GOURMET LOG: Triggering email logic for status: "
                                                + request.getStatus());
                                if ("succeeded".equalsIgnoreCase(request.getStatus())
                                                || "completed".equalsIgnoreCase(request.getStatus())) {
                                        sendReceiptEmail(request.getUserId(), request.getOrderId());
                                } else if ("pending".equalsIgnoreCase(request.getStatus())) {
                                        sendOrderPendingPaymentEmail(request.getUserId(), request.getOrderId());
                                }
                        } else {
                                System.err.println("!!! FAILED: Could not find user metadata for ID: "
                                                + request.getUserId());
                        }
                } catch (Exception e) {
                        System.err.println("!!! INTEGRATION ERROR in processNotification: " + e.getMessage());
                }
        }

        private String getAuthDestination(UserDTO user) {
                // RESTORED: Hardcoded for demo/testing as requested
                return "nithika151@gmail.com";
        }

        private String getHeaderHtml(String title, String statusColor, String bannerUrl) {
                return "<!DOCTYPE html><html><body style='font-family: \"Segoe UI\", Roboto, sans-serif; background-color: "
                                + COLOR_BG + "; padding: 40px 20px; margin: 0; color: " + COLOR_TEXT_MAIN + ";'>" +
                                "<table width='100%' cellpadding='0' cellspacing='0' style='max-width: 600px; margin: 0 auto; background-color: "
                                + COLOR_SURFACE + "; border-radius: 20px; border: 1px solid " + COLOR_BORDER
                                + "; overflow: hidden; box-shadow: 0 15px 50px rgba(0,0,0,0.6);'>" +
                                (bannerUrl != null ? "<tr><td><img src='" + bannerUrl
                                                + "' style='width: 100%; display: block; height: 200px; object-fit: cover;' /></td></tr>"
                                                : "")
                                +
                                "<tr><td style='padding: 40px 35px 20px; text-align: center;'>" +
                                "<h1 style='color: " + COLOR_GOLD
                                + "; margin: 0; font-size: 24px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase;'>Gourmet<span style='color: #ffffff;'>Express</span></h1>"
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
                                + "; font-size: 11px; margin: 0 0 12px; text-transform: uppercase; letter-spacing: 1px;'>Gourmet Express HQ &bull; Colombo, SL</p>"
                                +
                                "<p style='color: #ef4444; font-size: 10px; margin: 0;'><i>DEMO TRACE: Authenticated for "
                                + user.getEmail() + "</i></p>" +
                                "</td></tr></table></body></html>";
        }

        public String sendWelcomeEmail(String userId) {
                System.out.println(">>> ENTERING: sendWelcomeEmail for UserID: " + userId);
                try {
                        UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
                        if (user == null) {
                                System.err.println("!!! FAILED: User Mapping (Identity Service) for ID: " + userId);
                                return "User Mapping Failed";
                        }
                        System.out.println("--- GOURMET LOG: Fetched User Profile for " + user.getUsername());

                        Email from = new Email("it22061348@my.sliit.lk");
                        Email to = new Email(getAuthDestination(user));
                        String subject = "Access Granted - Welcome to the Gourmet Express Network";

                        StringBuilder html = new StringBuilder();
                        html.append(getHeaderHtml("Identity Confirmed", "#3b82f6",
                                        "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=600&h=200&fit=crop"))
                                        .append("<tr><td style='padding: 0 40px 40px;'>")
                                        .append("<h2 style='font-weight: 300; font-size: 26px; margin: 0 0 20px;'>Welcome, "
                                                        + user.getUsername() + "</h2>")
                                        .append("<p style='font-size: 15px; color: " + COLOR_TEXT_DIM
                                                        + "; line-height: 1.6;'>Your client node has been successfully synchronized.</p>")
                                        .append("<div style='text-align: center; margin-top: 40px;'>")
                                        .append("<a href='https://gourmet-express.vercel.app/catalog' style='background: "
                                                        + COLOR_GOLD
                                                        + "; color: #000; padding: 16px 40px; border-radius: 50px; text-decoration: none; font-weight: 800;'>EXPLORE CATALOG →</a>")
                                        .append("</div></td></tr>")
                                        .append(getFooterHtml(user));

                        return dispatchEmail(from, to, subject, html.toString());
                } catch (Exception e) {
                        System.err.println("!!! EXCEPTION in sendWelcomeEmail: " + e.getMessage());
                        return "Failed: " + e.getMessage();
                }
        }

        public String sendOrderPendingPaymentEmail(String userId, String orderId) {
                System.out.println(">>> ENTERING: sendOrderPendingPaymentEmail for User: " + userId + ", Order: "
                                + orderId);
                try {
                        UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
                        java.util.Map<String, Object> orderDetails = restTemplate
                                        .getForObject(orderUrl + "/orders/" + orderId, java.util.Map.class);
                        if (user == null || orderDetails == null) {
                                System.err.println("!!! FAILED: Data Fetch (User or Orders missing) for Order: "
                                                + orderId);
                                return "Data Error";
                        }

                        Email from = new Email("it22061348@my.sliit.lk");
                        Email to = new Email(getAuthDestination(user));
                        String subject = "Action Required - Order #" + orderId;

                        StringBuilder html = new StringBuilder();
                        html.append(getHeaderHtml("Pending Authorization", COLOR_GOLD,
                                        "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=80&w=600&h=200&fit=crop"))
                                        .append("<tr><td style='padding: 0 40px 40px;'>")
                                        .append("<p style='color: " + COLOR_TEXT_DIM
                                                        + ";'>Your request is queued. Finalize the credit transfer to commence synthesis.</p>")
                                        .append("<div style='text-align: center; margin: 40px 0 20px;'>")
                                        .append("<a href='https://gourmet-express.vercel.app/payments/checkout/"
                                                        + orderId + "' style='background: " + COLOR_GOLD
                                                        + "; color: #000; padding: 18px 45px; border-radius: 50px; text-decoration: none; font-weight: 800;'>EXECUTE PAYMENT →</a>")
                                        .append("</div></td></tr>")
                                        .append(getFooterHtml(user));

                        return dispatchEmail(from, to, subject, html.toString());
                } catch (Exception e) {
                        System.err.println("!!! EXCEPTION in sendOrderPending: " + e.getMessage());
                        return "Failed: " + e.getMessage();
                }
        }

        public String sendReceiptEmail(String userId, String orderId) {
                System.out.println(">>> ENTERING: sendReceiptEmail for User: " + userId + ", Order: " + orderId);
                try {
                        UserDTO user = restTemplate.getForObject(identityUrl + "/api/users/" + userId, UserDTO.class);
                        PaymentResponse payResp = restTemplate.getForObject(
                                        paymentUrl + "/api/payments/order/" + orderId, PaymentResponse.class);
                        PaymentDTO payment = (payResp != null) ? payResp.getData() : null;
                        if (user == null || payment == null) {
                                System.err.println("!!! FAILED: Payment Sync (Data Missing) for Order: " + orderId);
                                return "Data Missing";
                        }

                        Email from = new Email("it22061348@my.sliit.lk");
                        Email to = new Email(getAuthDestination(user));
                        String subject = "Receipt - Order #" + orderId;

                        StringBuilder html = new StringBuilder();
                        html.append(getHeaderHtml("Payment Verified", COLOR_GREEN,
                                        "https://images.unsplash.com/photo-1555244162-803834f70033?q=80&w=600&h=200&fit=crop"))
                                        .append("<tr><td style='padding: 0 40px 40px;'>")
                                        .append("<h2 style='font-weight: 300; font-size: 24px;'>Payment authorized. Artifacts are being prepared.</h2>")
                                        .append("<p style='color: " + COLOR_GOLD + "; font-size: 20px;'>Total: LKR "
                                                        + payment.getAmount() + "</p>")
                                        .append("<div style='text-align: center; margin-top: 40px;'>")
                                        .append("<a href='https://gourmet-express.vercel.app/orders' style='border: 1px solid "
                                                        + COLOR_BORDER
                                                        + "; color: #fff; padding: 14px 35px; border-radius: 50px; text-decoration: none;'>TRACK ORDER</a>")
                                        .append("</div></td></tr>")
                                        .append(getFooterHtml(user));

                        return dispatchEmail(from, to, subject, html.toString());
                } catch (Exception e) {
                        System.err.println("!!! EXCEPTION in sendReceipt: " + e.getMessage());
                        return "Failed: " + e.getMessage();
                }
        }

        private String dispatchEmail(Email from, Email to, String subject, String htmlBody) {
                System.out.println("--- GOURMET DISPATCH: Initializing SendGrid for " + to.getEmail());
                try {
                        from.setName("Gourmet Express");
                        Content content = new Content("text/html", htmlBody);
                        Mail mail = new Mail(from, subject, to, content);

                        if (sendGridApiKey == null || sendGridApiKey.contains("UNSET") || sendGridApiKey.isEmpty()) {
                                System.err.println("!!! ERROR: SENDGRID_API_KEY is missing or invalid in environment.");
                        }

                        SendGrid sg = new SendGrid(sendGridApiKey);
                        Request request = new Request();
                        request.setMethod(Method.POST);
                        request.setEndpoint("mail/send");
                        request.setBody(mail.build());

                        Response response = sg.api(request);

                        System.out.println(">>> SENDGRID RESPONSE: Status=" + response.getStatusCode());
                        if (response.getStatusCode() >= 400) {
                                System.err.println("!!! SENDGRID FAILURE BODY: " + response.getBody());
                        }

                        return "Status: " + response.getStatusCode() + " | Body: " + response.getBody();
                } catch (IOException e) {
                        System.err.println("!!! IO EXCEPTION during SendGrid Dispatch: " + e.getMessage());
                        return "SendGrid Client Error: " + e.getMessage();
                }
        }
}