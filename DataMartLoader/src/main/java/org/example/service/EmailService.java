package org.example.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.configuration.DBProperties;
import org.example.dao.LogDAO;
import org.example.entity.Log;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

@Slf4j
public class EmailService {

    public static void sendEmail(String subject, String body, LocalDateTime beginLocalDateTime) {
        // Thông tin tài khoản email
        String host = "smtp.gmail.com";
        final String user = "21130445@st.hcmuaf.edu.vn";
        final String password = "algp vrut gqrm qgtr";

        // Địa chỉ người nhận
        String to = DBProperties.email;  // Địa chỉ người nhận

        // Thiết lập các thuộc tính của server mail
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session;

        session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String beginDate = beginLocalDateTime.format(formatter);
        String endDate = LocalDateTime.now().format(formatter);

        String emailBody = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2> %s</h2>
                    <p><strong>Message:</strong> %s</p>
                    <p><strong>Begin Date:</strong> %s</p>
                    <p><strong>End Date:</strong> %s</p>
                </body>
                </html>
                """.formatted(subject, body, beginDate, endDate);

        try {
            // Tạo đối tượng MimeMessage
            MimeMessage message = new MimeMessage(session);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject, "utf-8");
            message.addHeader("Content-type", "text/HTML; charset=UTF-8");
            message.setContent(emailBody, "text/html; charset=UTF-8");
            InternetAddress fromAddress = new InternetAddress(user, "DataWarehouse Loader");
            message.setFrom(fromAddress);
            message.setSentDate(new Date());

            // Gửi email
            Transport.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LogDAO logDAO = LogDAO.getInstance();
            Log log = new Log();
            log.setMessage("Send email failed! " + e.getMessage());
            logDAO.insert(log);
        }
    }
}