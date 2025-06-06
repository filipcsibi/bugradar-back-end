package com.example.bugradar.service;

import com.example.bugradar.config.EmailConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired(required = false) // Optional - nu va da eroare dacă nu e configurat
    private JavaMailSender mailSender;

    @Autowired
    private EmailConfigProperties emailConfig;

    @Value("${spring.mail.from-email:noreply@bugradar.com}")
    private String fromEmail;

    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Trimite notificare de ban prin email
     */
    public void sendBanNotification(String email, String reason) {
        String subject = "🚫 Account Suspended - " + emailConfig.getName();
        String message = createBanMessage(reason);

        sendEmail(email, subject, message, "BAN");
    }

    /**
     * Trimite notificare de unban prin email
     */
    public void sendUnbanNotification(String email) {
        String subject = "✅ Account Restored - " + emailConfig.getName();
        String message = createUnbanMessage();

        sendEmail(email, subject, message, "UNBAN");
    }

    /**
     * Metodă generică pentru trimiterea email-urilor
     */
    private void sendEmail(String toEmail, String subject, String messageText, String type) {
        // Afișează întotdeauna în consolă
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 EMAIL " + type + " NOTIFICATION");
        System.out.println("=".repeat(60));
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("-".repeat(60));
        System.out.println(messageText);
        System.out.println("=".repeat(60) + "\n");

        // Încearcă să trimită email real dacă e configurat
        if (emailEnabled && mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(messageText);

                mailSender.send(message);
                System.out.println("✅ Email sent successfully to: " + toEmail);

            } catch (Exception e) {
                System.err.println("❌ Failed to send email to: " + toEmail);
                System.err.println("❌ Error: " + e.getMessage());
                System.out.println("📧 Email notification shown in console instead.");
            }
        } else {
            System.out.println("📧 Email service disabled or not configured - notification shown in console only.");
        }
    }

    /**
     * Creează mesajul pentru ban
     */
    private String createBanMessage(String reason) {
        return String.format("""
            Hello,
            
            Your account has been SUSPENDED from %s.
            
            🔍 REASON: %s
            
            What this means:
            ❌ You cannot access your account
            ❌ You cannot post bugs or comments  
            ❌ You cannot vote on content
            
            What you can do:
            📧 Contact support: %s
            📖 Review our community guidelines
            ⏰ Wait for the suspension to be lifted
            
            Suspended on: %s
            
            Best regards,
            %s Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """,
                emailConfig.getName(),
                reason,
                emailConfig.getSupport().getEmail(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                emailConfig.getName()
        );
    }

    /**
     * Creează mesajul pentru unban
     */
    private String createUnbanMessage() {
        return String.format("""
            Hello,
            
            🎉 GREAT NEWS! Your account has been RESTORED!
            
            You can now access %s again and enjoy all features:
            ✅ Access your account normally
            🐛 Report and manage bugs
            💬 Post comments and engage with community
            👍 Vote on bugs and comments
            🏆 Participate in leaderboard
            
            Moving forward:
            📖 Please review our community guidelines
            🤝 Help maintain a positive environment
            📧 Contact support if you have questions: %s
            
            Account restored on: %s
            
            Welcome back!
            %s Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """,
                emailConfig.getName(),
                emailConfig.getSupport().getEmail(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                emailConfig.getName()
        );
    }

    /**
     * Test email simplu
     */
    public void sendTestEmail(String toEmail) {
        String subject = "🧪 Test Email - " + emailConfig.getName();
        String message = String.format("""
            This is a test email from %s.
            
            ✅ Email service is working correctly!
            
            Configuration:
            - From: %s
            - To: %s
            - Time: %s
            
            If you received this email, the email service is configured properly.
            
            Best regards,
            %s Team
            """,
                emailConfig.getName(),
                fromEmail,
                toEmail,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                emailConfig.getName()
        );

        sendEmail(toEmail, subject, message, "TEST");
    }

    /**
     * Dezactivează complet email-urile (doar console)
     */
    public void disableEmails() {
        this.emailEnabled = false;
        System.out.println("📧 Email service disabled - notifications will only appear in console.");
    }

    /**
     * Activează email-urile
     */
    public void enableEmails() {
        this.emailEnabled = true;
        System.out.println("📧 Email service enabled.");
    }

    /**
     * Verifică dacă email service este activ
     */
    public boolean isEmailEnabled() {
        return emailEnabled && mailSender != null;
    }
}