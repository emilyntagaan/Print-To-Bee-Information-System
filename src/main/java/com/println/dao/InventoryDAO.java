package com.println.dao;

import com.println.config.DBConnection;
import com.println.model.Inventory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    // --- GET ONE ---
    public Inventory getInventoryById(int id) {
        String sql = "SELECT * FROM inventory WHERE inventory_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractInventory(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching inventory by ID: " + e.getMessage());
        }
        return null;
    }

    // --- GET BY ITEM NAME ---
    public Inventory getInventoryByName(String itemName) {
        String sql = "SELECT * FROM inventory WHERE item_name = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractInventory(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching inventory by item name: " + e.getMessage());
        }
        return null;
    }


    // --- GET ALL ---
    public List<Inventory> getAllInventory() {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory ORDER BY inventory_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractInventory(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all inventory: " + e.getMessage());
        }
        return list;
    }

    // --- DECREMENT STOCK (used when order is completed) ---
    public boolean decrementStock(int inventoryId, int quantity) {
        String sql = "UPDATE inventory SET quantity = quantity - ? WHERE inventory_id = ? AND quantity >= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, inventoryId);
            ps.setInt(3, quantity);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                updateInventoryStatus(inventoryId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error decrementing stock: " + e.getMessage());
        }
        return false;
    }

    // --- INCREMENT STOCK (used when order is cancelled) ---
    public boolean incrementStock(int inventoryId, int quantity) {
        String sql = "UPDATE inventory SET quantity = quantity + ? WHERE inventory_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, inventoryId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                updateInventoryStatus(inventoryId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error incrementing stock: " + e.getMessage());
        }
        return false;
    }

    public void updateInventoryStatus(int inventoryId) {
        String sql = 
            "UPDATE inventory " +
            "SET " +
            "   status = CASE " +
            "       WHEN quantity <= 0 THEN 'Out of Stock' " +
            "       WHEN quantity <= reorder_level THEN 'Low' " +
            "       ELSE 'Available' " +
            "   END, " +
            "   date_updated = NOW() " +
            "WHERE inventory_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inventoryId);

            int updated = ps.executeUpdate();
            System.out.println("Status updated for ID " + inventoryId + ": rows=" + updated);

        } catch (SQLException e) {
            System.err.println("Error updating inventory status: " + e.getMessage());
        }
    }

    // --- Helper method to map inventory object ---
    private Inventory extractInventory(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setInventoryId(rs.getInt("inventory_id"));
        inv.setItemName(rs.getString("item_name"));
        inv.setDescription(rs.getString("description"));
        inv.setCategory(rs.getString("category"));
        inv.setUnit(rs.getString("unit"));
        inv.setQuantity(rs.getInt("quantity"));
        inv.setReorderLevel(rs.getInt("reorder_level"));
        inv.setSupplierName(rs.getString("supplier_name"));
        inv.setStatus(rs.getString("status"));
        inv.setCostPerUnit(rs.getBigDecimal("cost_per_unit"));

        // Handle nullable dates
        Date lastRestock = rs.getDate("last_restock_date");
        if (lastRestock != null) inv.setLastRestockDate(lastRestock.toLocalDate());

        Date updated = rs.getDate("date_updated");
        if (updated != null) inv.setDateUpdated(updated.toLocalDate());

        return inv;
    }
}
