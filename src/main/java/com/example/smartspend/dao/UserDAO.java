package com.example.smartspend.dao;

import com.example.smartspend.database.DatabaseConnection;
import com.example.smartspend.utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserDAO {

    public boolean isEmailExists(String email) {
        String sql = "SELECT id FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra email: " + e.getMessage());
            return true;
        }
    }

    public boolean registerUser(String name, String email, String password) {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, normalizeEmail(email));
            ps.setString(3, PasswordUtil.hash(password));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng ký user: " + e.getMessage());
            return false;
        }
    }

    public int validateLogin(String email, String password) {
        String sql = "SELECT id, password FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.matches(password, rs.getString("password"))) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi validate login: " + e.getMessage());
        }
        return -1;
    }

    public boolean savePasswordResetCode(String email, String code, LocalDateTime expiresAt) {
        String clearOld = "DELETE FROM password_reset_codes WHERE LOWER(email) = LOWER(?)";
        String insert = "INSERT INTO password_reset_codes (email, code_hash, expires_at, used) VALUES (?, ?, ?, FALSE)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deletePs = conn.prepareStatement(clearOld);
                 PreparedStatement insertPs = conn.prepareStatement(insert)) {
                String normalized = normalizeEmail(email);
                deletePs.setString(1, normalized);
                deletePs.executeUpdate();

                insertPs.setString(1, normalized);
                insertPs.setString(2, PasswordUtil.hash(code));
                insertPs.setTimestamp(3, Timestamp.valueOf(expiresAt));
                boolean ok = insertPs.executeUpdate() > 0;
                conn.commit();
                return ok;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lưu mã reset mật khẩu: " + e.getMessage());
            return false;
        }
    }

    public boolean resetPasswordWithCode(String email, String code, String newPassword) {
        String find = "SELECT reset_id, code_hash FROM password_reset_codes " +
                "WHERE LOWER(email) = LOWER(?) AND used = FALSE AND expires_at > CURRENT_TIMESTAMP " +
                "ORDER BY created_at DESC LIMIT 1";
        String updatePassword = "UPDATE users SET password = ? WHERE LOWER(email) = LOWER(?)";
        String markUsed = "UPDATE password_reset_codes SET used = TRUE WHERE reset_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement findPs = conn.prepareStatement(find)) {
                String normalized = normalizeEmail(email);
                findPs.setString(1, normalized);
                int resetId;
                String storedHash;
                try (ResultSet rs = findPs.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    resetId = rs.getInt("reset_id");
                    storedHash = rs.getString("code_hash");
                }
                if (!PasswordUtil.matches(code, storedHash)) {
                    conn.rollback();
                    return false;
                }
                try (PreparedStatement updatePs = conn.prepareStatement(updatePassword);
                     PreparedStatement usedPs = conn.prepareStatement(markUsed)) {
                    updatePs.setString(1, PasswordUtil.hash(newPassword));
                    updatePs.setString(2, normalized);
                    if (updatePs.executeUpdate() <= 0) {
                        conn.rollback();
                        return false;
                    }
                    usedPs.setInt(1, resetId);
                    usedPs.executeUpdate();
                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi reset mật khẩu: " + e.getMessage());
            return false;
        }
    }

    public int getUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy user id theo email: " + e.getMessage());
        }
        return -1;
    }

    public int getOrCreateExternalUser(String name, String email, String provider) {
        String normalized = normalizeEmail(email);
        int existingId = getUserIdByEmail(normalized);
        if (existingId > 0) return existingId;

        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        String generatedPassword = provider + ":" + normalized + ":smartspend-oauth-ready";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, (name == null || name.isBlank()) ? normalized : name.trim());
            ps.setString(2, normalized);
            ps.setString(3, PasswordUtil.hash(generatedPassword));
            if (ps.executeUpdate() <= 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tạo external user: " + e.getMessage());
        }
        return -1;
    }


    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
