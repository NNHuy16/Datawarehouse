package com.example.server_staging.service;


import com.example.server_staging.configuration.DBProperties;
import com.example.server_staging.dao.LogDAO;
import com.example.server_staging.entity.Log;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;


import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

@Slf4j
public class EmailService {

    public static void sendEmail(String subject, String body) {
        // Thông tin tài khoản email
        String host = "smtp.gmail.com";
        final String user = "21130149@st.hcmuaf.edu.vn";  // Địa chỉ email của bạn
        final String password = "veqm zvxe uwrz lsni";    // Mật khẩu ứng dụng của Gmail (nếu dùng Gmail)

        // Địa chỉ người nhận
        String to = DBProperties.email;  // Địa chỉ người nhận
        // Thiết lập các thuộc tính của server mail
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session;

        session = Session.getInstance(properties, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Tạo đối tượng MimeMessage
            MimeMessage message = new MimeMessage(session);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject, "utf-8");
            message.addHeader("Content-type", "text/HTML; charset=UTF-8");
            message.setContent(body, "text/html; charset=UTF-8");
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