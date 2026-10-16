package com.example.smartspend.service;

import com.example.smartspend.utils.HostInfo;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class EmailService {
    private final SmtpSettingsService settings = new SmtpSettingsService();

    public boolean isConfigured() {
        return settings.isConfigured();
    }

    public String configurationStatus() {
        return settings.describeStatus();
    }

    public boolean sendPasswordResetCode(String toEmail, String code) {
        String subject = "SmartSpend - Ma xac minh dat lai mat khau";
        String body = "Xin chao,\n\n"
                + "Ban vua yeu cau dat lai mat khau SmartSpend.\n\n"
                + "Ma xac minh cua ban la: " + code + "\n"
                + "Ma nay co hieu luc trong 10 phut.\n\n"
                + "Neu ban khong yeu cau thao tac nay, hay bo qua email nay.\n\n"
                + signature();
        return sendPlainText(toEmail, subject, body);
    }

    public boolean sendWelcomeEmail(String toEmail, String displayName) {
        String subject = "Welcome to SmartSpend";
        String name = displayName == null || displayName.isBlank() ? "ban" : displayName.trim();
        String body = "Xin chao " + name + ",\n\n"
                + "Tai khoan SmartSpend cua ban da duoc tao thanh cong.\n"
                + "Ban co the dang nhap, them giao dich, theo doi ngan sach va xem Smart Coach ngay trong ung dung.\n\n"
                + "Host/Developer: " + HostInfo.HOST_NAME + "\n"
                + "Contact: " + HostInfo.HOST_EMAIL + "\n\n"
                + signature();
        return sendPlainText(toEmail, subject, body);
    }

    public boolean sendLoginNotice(String toEmail) {
        String subject = "SmartSpend - Dang nhap moi";
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String body = "Xin chao,\n\n"
                + "SmartSpend ghi nhan mot phien dang nhap vao tai khoan cua ban luc " + time + ".\n"
                + "Neu day la ban, ban khong can lam gi them.\n"
                + "Neu khong phai ban, hay dung Forgot Password de doi mat khau ngay.\n\n"
                + signature();
        return sendPlainText(toEmail, subject, body);
    }

    public boolean sendTestEmail(String toEmail) {
        String subject = "SmartSpend SMTP test";
        String body = "Ket noi SMTP cua SmartSpend da hoat dong.\n\n"
                + "Tu bay gio app co the gui email reset mat khau, email chao mung va canh bao dang nhap.\n\n"
                + signature();
        return sendPlainText(toEmail, subject, body);
    }

    public boolean sendPlainText(String toEmail, String subject, String body) {
        if (!isConfigured()) return false;
        try {
            sendSslSmtp(toEmail, subject, body);
            return true;
        } catch (Exception e) {
            System.err.println("Khong gui duoc email: " + e.getMessage());
            return false;
        }
    }

    private String signature() {
        return "--\n"
                + "SmartSpend Desktop\n"
                + "Host: " + HostInfo.HOST_NAME + "\n"
                + "Email: " + HostInfo.HOST_EMAIL;
    }

    private void sendSslSmtp(String toEmail, String subject, String body) throws Exception {
        String host = settings.getHost();
        int port = Integer.parseInt(settings.getPort());
        String from = settings.getFrom();
        String user = settings.getUser();
        String pass = settings.getPassword();

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            expect(reader, "220");
            send(writer, "EHLO smartspend.local"); expect(reader, "250");
            send(writer, "AUTH LOGIN"); expect(reader, "334");
            send(writer, Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8))); expect(reader, "334");
            send(writer, Base64.getEncoder().encodeToString(pass.getBytes(StandardCharsets.UTF_8))); expect(reader, "235");
            send(writer, "MAIL FROM:<" + from + ">"); expect(reader, "250");
            send(writer, "RCPT TO:<" + toEmail + ">"); expect(reader, "250");
            send(writer, "DATA"); expect(reader, "354");
            writer.write("From: SmartSpend <" + from + ">\r\n");
            writer.write("To: " + toEmail + "\r\n");
            writer.write("Subject: " + encodeHeader(subject) + "\r\n");
            writer.write("MIME-Version: 1.0\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            writer.write("Content-Transfer-Encoding: 8bit\r\n");
            writer.write("\r\n");
            writer.write(dotStuff(body).replace("\n", "\r\n"));
            writer.write("\r\n.\r\n");
            writer.flush();
            expect(reader, "250");
            send(writer, "QUIT");
        }
    }

    private String encodeHeader(String value) {
        String encoded = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        return "=?UTF-8?B?" + encoded + "?=";
    }

    private String dotStuff(String body) {
        String[] lines = body.split("\\R", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith(".")) sb.append('.');
            sb.append(line);
            if (i < lines.length - 1) sb.append('\n');
        }
        return sb.toString();
    }

    private void send(BufferedWriter writer, String command) throws Exception {
        writer.write(command + "\r\n");
        writer.flush();
    }

    private void expect(BufferedReader reader, String prefix) throws Exception {
        String line = reader.readLine();
        if (line == null || !line.startsWith(prefix)) {
            throw new IllegalStateException("SMTP expected " + prefix + " but got: " + line);
        }
        while (line.length() > 3 && line.charAt(3) == '-') {
            line = reader.readLine();
            if (line == null || !line.startsWith(prefix)) {
                throw new IllegalStateException("SMTP expected continuation " + prefix + " but got: " + line);
            }
        }
    }
}
