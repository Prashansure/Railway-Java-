package railway.services;

import railway.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Handles user login, registration, and account-related database queries. */
public final class AuthService {

    private final DBConnection db;

    public AuthService(DBConnection db) {
        this.db = db;
    }

    /** Returns the logged-in user details, or null when the credentials are invalid. */
    public Map<String, Object> login(String username, String password) {
        String sql = "SELECT id, username, role FROM users WHERE username = ? AND password = ?";

        try (Connection connection = db.connect("users");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", result.getInt("id"));
                    user.put("username", result.getString("username"));
                    user.put("role", result.getString("role"));
                    return user;
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not log in the user.", exception);
        }

        return null;
    }

    /** Returns null on success or a user-friendly error message on failure. */
    public String register(String username, String password) {
        String sql = "INSERT INTO users(username, password, role) VALUES (?, ?, ?)";

        try (Connection connection = db.connect("users");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, "user");
            statement.executeUpdate();
            return null;
        } catch (SQLException exception) {
            return "Username already exists.";
        }
    }

    public String changePassword(String username, String oldPassword, String newPassword) {
        try (Connection connection = db.connect("users")) {
            String checkSql = "SELECT id FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement check = connection.prepareStatement(checkSql)) {
                check.setString(1, username);
                check.setString(2, oldPassword);

                try (ResultSet result = check.executeQuery()) {
                    if (!result.next()) {
                        return "Current password is incorrect.";
                    }
                }
            }

            String updateSql = "UPDATE users SET password = ? WHERE username = ?";
            try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                update.setString(1, newPassword);
                update.setString(2, username);
                update.executeUpdate();
            }

            return null;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not change the password.", exception);
        }
    }

    /** Returns all users for the administrator screen. */
    public List<Object[]> getAllUsers() {
        List<Object[]> users = new ArrayList<>();
        String sql = "SELECT id, username, role FROM users";

        try (Connection connection = db.connect("users");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                users.add(new Object[]{
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("role")
                });
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load users.", exception);
        }

        return users;
    }
}
