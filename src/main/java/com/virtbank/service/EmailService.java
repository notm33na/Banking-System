package com.virtbank.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from-address:noreply@virtbank.com}")
    private String fromAddress;

    @Value("${app.mail.from-name:VIRTBANK}")
    private String fromName;

    // ── 1. Welcome Email ──────────────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        String subject = "Welcome to VIRTBANK!";
        String body = wrap(
            greeting(name) +
            "<p>Your account has been successfully created. You can now access all banking features " +
            "through your personalized dashboard.</p>" +
            highlight("Account Status", "ACTIVE") +
            "<p>If you did not create this account, please contact our support team immediately.</p>"
        );
        send(toEmail, subject, body);
    }

    // ── 2. Transaction Alert ──────────────────────────────────────────
    @Async
    public void sendTransactionAlert(String toEmail, String name, String type,
                                     BigDecimal amount, BigDecimal balance) {
        String subject = "Transaction Alert — " + type;
        String body = wrap(
            greeting(name) +
            "<p>A <strong>" + type + "</strong> transaction has been processed on your account.</p>" +
            "<table style='width:100%;border-collapse:collapse;margin:16px 0;'>" +
            row("Transaction Type", type) +
            row("Amount", formatCurrency(amount)) +
            row("Balance After", formatCurrency(balance)) +
            "</table>" +
            "<p style='color:#64748b;font-size:13px;'>If you did not authorize this transaction, " +
            "please contact support immediately.</p>"
        );
        send(toEmail, subject, body);
    }

    // ── 3. Loan Status Email ──────────────────────────────────────────
    @Async
    public void sendLoanStatusEmail(String toEmail, String name, String status,
                                    BigDecimal amount) {
        String subject = "Loan Application " + status;
        String color = "APPROVED".equalsIgnoreCase(status) ? "#22c55e" : "#ef4444";
        String body = wrap(
            greeting(name) +
            "<p>Your loan application has been reviewed by our team.</p>" +
            "<table style='width:100%;border-collapse:collapse;margin:16px 0;'>" +
            row("Loan Amount", formatCurrency(amount)) +
            "<tr><td style='padding:10px 12px;border:1px solid #e2e8f0;font-weight:600;'>Status</td>" +
            "<td style='padding:10px 12px;border:1px solid #e2e8f0;'>" +
            "<span style='color:" + color + ";font-weight:700;'>" + status + "</span></td></tr>" +
            "</table>" +
            ("APPROVED".equalsIgnoreCase(status)
                ? "<p>Funds will be disbursed to your account shortly. You can view your repayment schedule in the Loans section.</p>"
                : "<p>If you have questions about this decision, please reach out to our support team.</p>")
        );
        send(toEmail, subject, body);
    }

    // ── 4. OTP Email ──────────────────────────────────────────────────
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Your VIRTBANK Verification Code";
        String body = wrap(
            "<p>Please use the following verification code:</p>" +
            "<div style='text-align:center;margin:24px 0;'>" +
            "<span style='display:inline-block;background:#f1f5f9;border:2px dashed #6366f1;" +
            "border-radius:8px;padding:16px 32px;font-size:28px;font-weight:700;" +
            "letter-spacing:6px;color:#1e293b;'>" + otp + "</span></div>" +
            "<p style='color:#64748b;font-size:13px;'>This code expires in 10 minutes. " +
            "Do not share it with anyone.</p>"
        );
        send(toEmail, subject, body);
    }

    // ── 5. Payroll Confirmation ───────────────────────────────────────
    @Async
    public void sendPayrollConfirmation(String toEmail, String employeeName,
                                        BigDecimal amount, String cycle) {
        String subject = "Payroll Payment Received";
        String body = wrap(
            greeting(employeeName) +
            "<p>Your payroll payment has been processed.</p>" +
            "<table style='width:100%;border-collapse:collapse;margin:16px 0;'>" +
            row("Amount", formatCurrency(amount)) +
            row("Pay Cycle", cycle) +
            row("Status", "COMPLETED") +
            "</table>" +
            "<p>The funds have been deposited to your registered account.</p>"
        );
        send(toEmail, subject, body);
    }

    // ── Private helpers ───────────────────────────────────────────────

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {} — {}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String wrap(String content) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;font-family:Inter,Arial,sans-serif;" +
               "background:#f1f5f9;'><div style='max-width:560px;margin:32px auto;background:#fff;" +
               "border-radius:12px;overflow:hidden;border:1px solid #e2e8f0;'>" +
               // Header
               "<div style='background:linear-gradient(135deg,#4f46e5,#6366f1);padding:24px 32px;" +
               "text-align:center;'><h1 style='margin:0;color:#fff;font-size:22px;letter-spacing:-0.5px;'>" +
               "VIRTBANK</h1></div>" +
               // Body
               "<div style='padding:28px 32px;color:#1e293b;font-size:15px;line-height:1.6;'>" +
               content + "</div>" +
               // Footer
               "<div style='padding:16px 32px;background:#f8fafc;border-top:1px solid #e2e8f0;" +
               "text-align:center;font-size:12px;color:#94a3b8;'>" +
               "© 2025 VIRTBANK. This is an automated message, please do not reply.</div>" +
               "</div></body></html>";
    }

    private String greeting(String name) {
        return "<p>Hello <strong>" + name + "</strong>,</p>";
    }

    private String highlight(String label, String value) {
        return "<div style='background:#f1f5f9;border-radius:8px;padding:16px 20px;margin:16px 0;" +
               "text-align:center;'><span style='color:#64748b;font-size:12px;text-transform:uppercase;" +
               "letter-spacing:1px;'>" + label + "</span><br/>" +
               "<span style='font-size:20px;font-weight:700;color:#22c55e;'>" + value + "</span></div>";
    }

    private String row(String label, String value) {
        return "<tr><td style='padding:10px 12px;border:1px solid #e2e8f0;font-weight:600;width:40%;'>" +
               label + "</td><td style='padding:10px 12px;border:1px solid #e2e8f0;'>" + value + "</td></tr>";
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }
}
