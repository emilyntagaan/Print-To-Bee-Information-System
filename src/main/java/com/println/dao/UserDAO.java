package com.println.dao;

import com.println.config.DBConnection;
import com.println.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // --- CREATE ---
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, role, first_name, last_name, email, contact_no, status, address, created_by, remarks) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFirstName());
            ps.setString(5, user.getLastName());
            ps.setString(6, user.getEmail());
            ps.setString(7, user.getContactNo());
            ps.setString(8, user.getStatus() != null ? user.getStatus() : "Active");
            ps.setString(9, user.getAddress());
            if (user.getCreatedBy() != null) ps.setInt(10, user.getCreatedBy());
            else ps.setNull(10, Types.INTEGER);
            ps.setString(11, user.getRemarks());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    // --- READ ALL ---
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(extractUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving users: " + e.getMessage());
        }

        return users;
    }

    // --- READ ONE ---
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }
        return null;
    }

    // --- UPDATE ---
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username=?, password=?, role=?, first_name=?, last_name=?, email=?, contact_no=?, status=?, address=?, remarks=? WHERE user_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFirstName());
            ps.setString(5, user.getLastName());
            ps.setString(6, user.getEmail());
            ps.setString(7, user.getContactNo());
            ps.setString(8, user.getStatus());
            ps.setString(9, user.getAddress());
            ps.setString(10, user.getRemarks());
            ps.setInt(11, user.getUserId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    // --- DELETE ---
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    // --- LOGIN VALIDATION ---
    public User validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'Active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Login successful for user: " + username);
                    return extractUser(rs);
                } else {
                    System.out.println("Invalid username or password.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error validating login: " + e.getMessage());
        }
        return null;
    }

    // --- Helper method to convert ResultSet → User object ---
    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setContactNo(rs.getString("contact_no"));
        user.setStatus(rs.getString("status"));
        user.setAddress(rs.getString("address"));
        user.setProfileImage(rs.getString("profile_image"));
        user.setRemarks(rs.getString("remarks"));
        int createdBy = rs.getInt("created_by");
        user.setCreatedBy(rs.wasNull() ? null : createdBy);
        return user;
    }
}
