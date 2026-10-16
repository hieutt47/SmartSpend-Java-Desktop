package com.example.smartspend.service;

import com.example.smartspend.utils.HostInfo;

import java.util.prefs.Preferences;

public class SmtpSettingsService {
    private static final String KEY_HOST = "smtp.host";
    private static final String KEY_PORT = "smtp.port";
    private static final String KEY_FROM = "smtp.from";
    private static final String KEY_USER = "smtp.user";
    private static final String KEY_PASSWORD = "smtp.password";
    private static final String KEY_SSL = "smtp.ssl";

    private final Preferences prefs = Preferences.userNodeForPackage(SmtpSettingsService.class);

    public String getHost() { return value(KEY_HOST, env("SMARTSPEND_SMTP_HOST", "smtp.gmail.com")); }
    public String getPort() { return value(KEY_PORT, env("SMARTSPEND_SMTP_PORT", "465")); }
    public String getFrom() { return value(KEY_FROM, env("SMARTSPEND_SMTP_FROM", HostInfo.HOST_EMAIL)); }
    public String getUser() { return value(KEY_USER, env("SMARTSPEND_SMTP_USER", HostInfo.HOST_EMAIL)); }
    public String getPassword() { return value(KEY_PASSWORD, env("SMARTSPEND_SMTP_PASSWORD", "")); }
    public boolean isSsl() { return Boolean.parseBoolean(value(KEY_SSL, env("SMARTSPEND_SMTP_SSL", "true"))); }

    public void save(String host, String port, String from, String user, String password, boolean ssl) {
        prefs.put(KEY_HOST, safe(host));
        prefs.put(KEY_PORT, safe(port));
        prefs.put(KEY_FROM, safe(from));
        prefs.put(KEY_USER, safe(user));
        if (password != null && !password.isBlank()) prefs.put(KEY_PASSWORD, password.trim());
        prefs.putBoolean(KEY_SSL, ssl);
    }

    public boolean isConfigured() {
        return !getHost().isBlank()
                && !getPort().isBlank()
                && !getFrom().isBlank()
                && !getUser().isBlank()
                && !getPassword().isBlank();
    }

    public String describeStatus() {
        if (!isConfigured()) {
            return "SMTP chưa cấu hình. Hãy nhập Gmail + App Password để gửi email thật.";
        }
        return "SMTP đã cấu hình: " + getFrom() + " qua " + getHost() + ":" + getPort();
    }

    private String value(String key, String fallback) {
        String value = prefs.get(key, "");
        return value == null || value.isBlank() ? fallback : value;
    }

    private String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null ? fallback : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
