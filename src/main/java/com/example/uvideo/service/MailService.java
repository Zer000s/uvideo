package com.example.uvideo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    JavaMailSender mailSender;

    @Value("${UI_URL}")
    String UI_URL;

    public void sendPasswordReset(String to, String code) {
        String link = UI_URL + "/reset/" + code;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Password Recovery");
        msg.setText("Click to reset your password: " + link);
        mailSender.send(msg);
    }
}