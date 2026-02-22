package com.println.dao;

import com.println.config.DBConnection;
import com.println.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // --- CREATE ---
    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, contact_no, email, address, city, gender, notes, status, customer_type, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getContactNo());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getCity());
            ps.setString(6, customer.getGender());
            ps.setString(7, customer.getNotes());
            ps.setString(8, customer.getStatus() != null ? customer.getStatus() : "Active");
            ps.setString(9, customer.getCustomerType() != null ? customer.getCustomerType() : "Individual");

            // FIXED: created_by is now INT
            ps.setInt(10, customer.getCreatedBy());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding customer: " + e.getMessage());
            return false;
        }
    }


    // --- READ ALL ---
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractCustomer(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving customers: " + e.getMessage());
        }

        return list;
    }

    // --- READ ONE ---
    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractCustomer(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching customer by ID: " + e.getMessage());
        }
        return null;
    }

    // --- UPDATE ---
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name=?, contact_no=?, email=?, address=?, city=?, gender=?, notes=?, status=?, customer_type=?, data_updated=NOW() "
                   + "WHERE customer_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getContactNo());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getCity());
            ps.setString(6, customer.getGender());
            ps.setString(7, customer.getNotes());
            ps.setString(8, customer.getStatus());
            ps.setString(9, customer.getCustomerType());
            ps.setInt(10, customer.getCustomerId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            return false;
        }
    }

    // --- DELETE ---
    public boolean deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            return false;
        }
    }

    // --- SEARCH ---
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE name LIKE ? OR email LIKE ? OR contact_no LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractCustomer(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error searching customers: " + e.getMessage());
        }

        return list;
    }

    // --- Helper Method: Convert ResultSet → Customer object ---
    private Customer extractCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setName(rs.getString("name"));
        c.setContactNo(rs.getString("contact_no"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setGender(rs.getString("gender"));
        c.setNotes(rs.getString("notes"));
        c.setStatus(rs.getString("status"));
        c.setCustomerType(rs.getString("customer_type"));
        c.setTotalOrders(rs.getInt("total_orders"));
        // created_by read as string now
        try {
            c.setCreatedBy(rs.getInt("created_by"));
        } catch (SQLException ignore) {
            c.setCreatedBy(0); // default or fallback value
        }

        // date_registered (may be null)
        try {
            Timestamp ts = rs.getTimestamp("date_registered");
            if (ts != null) c.setDateRegistered(ts.toLocalDateTime());
        } catch (SQLException ignore) {
            // ignore
        }

        // date_updated
        try {
            Timestamp upd = rs.getTimestamp("data_updated");
            if (upd != null) c.setDataUpdated(upd.toLocalDateTime());
        } catch (SQLException ignore) {}

        return c;
    }
}
