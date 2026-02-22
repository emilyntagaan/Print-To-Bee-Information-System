package com.println.dao;

import com.println.config.DBConnection;
import com.println.model.Order;
import com.println.model.OrderDetail;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class OrderDAO {

    /**
     * Add order and its details in a single transaction.
     * Returns generated order_id on success, or -1 on failure.
     */
    public int addOrder(Order order) {
        String insertOrderSql = "INSERT INTO orders (customer_id, user_id, due_date, status, total_amount, payment_status, payment_method, discount, remarks, quantity_total, order_reference, date_completed, printed_by) "
                              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String insertDetailSql = "INSERT INTO order_details (order_id, product_id, quantity, unit_price, material_used, discount, print_size, color_type, remarks, created_by, tax) "
                               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psDetail = null;
        ResultSet rsOrderKeys = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // start transaction

            // 1) Insert minimal order row (we'll update totals and reference after inserting details)
            psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            if (order.getCustomerId() != null) psOrder.setInt(1, order.getCustomerId()); else psOrder.setNull(1, Types.INTEGER);
            psOrder.setInt(2, order.getUserId());
            if (order.getDueDate() != null) psOrder.setDate(3, java.sql.Date.valueOf(order.getDueDate())); else psOrder.setNull(3, Types.DATE);
            psOrder.setString(4, order.getStatus() != null ? order.getStatus() : "Pending");
            // initial total_amount set to 0; will update after inserting details
            psOrder.setBigDecimal(5, order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
            psOrder.setString(6, order.getPaymentStatus() != null ? order.getPaymentStatus() : "Unpaid");
            psOrder.setString(7, order.getPaymentMethod());
            psOrder.setBigDecimal(8, order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO);
            psOrder.setString(9, order.getRemarks());
            psOrder.setInt(10, order.getQuantityTotal()); // initial, may update later
            psOrder.setString(11, order.getOrderReference()); // can be null; we'll generate and update later
            if (order.getDateCompleted() != null) psOrder.setDate(12, java.sql.Date.valueOf(order.getDateCompleted())); else psOrder.setNull(12, Types.DATE);
            if (order.getPrintedBy() != null) psOrder.setInt(13, order.getPrintedBy()); else psOrder.setNull(13, Types.INTEGER);

            int affected = psOrder.executeUpdate();
            if (affected == 0) {
                conn.rollback();
                return -1;
            }

            rsOrderKeys = psOrder.getGeneratedKeys();
            if (rsOrderKeys.next()) {
                int generatedOrderId = rsOrderKeys.getInt(1);

                // 2) Insert each order detail
                psDetail = conn.prepareStatement(insertDetailSql);
                int totalQty = 0;
                for (OrderDetail d : order.getDetails()) {
                    psDetail.setInt(1, generatedOrderId);
                    psDetail.setInt(2, d.getProductId());
                    psDetail.setInt(3, d.getQuantity());
                    psDetail.setBigDecimal(4, d.getUnitPrice());
                    psDetail.setString(5, d.getMaterialUsed());
                    psDetail.setBigDecimal(6, d.getDiscount() != null ? d.getDiscount() : BigDecimal.ZERO);
                    psDetail.setString(7, d.getPrintSize());
                    psDetail.setString(8, d.getColorType());
                    psDetail.setString(9, d.getRemarks());
                    if (d.getCreatedBy() != null) psDetail.setInt(10, d.getCreatedBy()); else psDetail.setNull(10, Types.INTEGER);
                    psDetail.setBigDecimal(11, d.getTax() != null ? d.getTax() : BigDecimal.ZERO);
                    psDetail.addBatch();

                    totalQty += d.getQuantity();
                }
                psDetail.executeBatch();

                // 3) Compute totals (subtotal is GENERATED in DB; sum it from order_details)
                BigDecimal totalAmount = BigDecimal.ZERO;
                String totalSql = 
                    "SELECT " +
                    "COALESCE(SUM(subtotal - (subtotal * (discount / 100)) + tax), 0) AS total, " +
                    "COALESCE(SUM(quantity), 0) AS qty_sum " +
                    "FROM order_details WHERE order_id = ?";
                try (PreparedStatement psTotal = conn.prepareStatement(totalSql)) {
                    psTotal.setInt(1, generatedOrderId);
                    try (ResultSet rsTotal = psTotal.executeQuery()) {
                        if (rsTotal.next()) {
                            totalAmount = rsTotal.getBigDecimal("total");
                            totalQty = rsTotal.getInt("qty_sum");
                        }
                    }
                }

                // 4) Generate a readable order_reference and update order with totals & reference & qty
                String ref = generateOrderReference(generatedOrderId);
                String updateOrderSql = "UPDATE orders SET total_amount = ?, quantity_total = ?, order_reference = ? WHERE order_id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateOrderSql)) {
                    psUpdate.setBigDecimal(1, totalAmount);
                    psUpdate.setInt(2, totalQty);
                    psUpdate.setString(3, ref);
                    psUpdate.setInt(4, generatedOrderId);
                    psUpdate.executeUpdate();
                }

                conn.commit();
                order.setOrderReference(ref);
                return generatedOrderId;
            } else {
                conn.rollback();
                return -1;
            }

        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            return -1;
        } finally {
            try { if (rsOrderKeys != null) rsOrderKeys.close(); } catch (SQLException ignored) {}
            try { if (psDetail != null) psDetail.close(); } catch (SQLException ignored) {}
            try { if (psOrder != null) psOrder.close(); } catch (SQLException ignored) {}
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    private String generateOrderReference(int orderId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String ts = LocalDateTime.now().format(fmt);
        return "ORD-" + orderId + "-" + ts;
    }

    // --- Get all orders (basic info) ---
    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.name AS customer_name, u.username AS user_name " +
                     "FROM orders o LEFT JOIN customers c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON o.user_id = u.user_id ORDER BY o.order_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Order o = new Order();
            o.setOrderId(rs.getInt("order_id"));
            o.setCustomerId(rs.getObject("customer_id") != null ? rs.getInt("customer_id") : null);
            o.setUserId(rs.getInt("user_id"));
            o.setOrderReference(rs.getString("order_reference"));
            o.setStatus(rs.getString("status"));
            o.setTotalAmount(rs.getBigDecimal("total_amount"));
            o.setQuantityTotal(rs.getInt("quantity_total"));
            o.setPaymentStatus(rs.getString("payment_status"));
            o.setPaymentMethod(rs.getString("payment_method"));
            o.setCustomerName(rs.getString("customer_name")); // <-- add this line

            Timestamp od = rs.getTimestamp("order_date");
            if (od != null) o.setOrderDate(od.toLocalDateTime());

            Date dc = rs.getDate("date_completed");
            if (dc != null) o.setDateCompleted(dc.toLocalDate());

            list.add(o);
        }

        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
        }
        return list;
    }

    // --- Get order by ID (with details) ---
    public Order getOrderById(int id) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order o = extractOrder(rs);
                    // fetch details
                    o.setDetails(getOrderDetails(o.getOrderId()));
                    return o;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching order by ID: " + e.getMessage());
        }
        return null;
    }

    // --- Get order details for an order ---
    public List<OrderDetail> getOrderDetails(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM order_details WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail d = new OrderDetail();
                    d.setOrderDetailId(rs.getInt("orderdetail_id"));
                    d.setOrderId(rs.getInt("order_id"));
                    d.setProductId(rs.getInt("product_id"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setUnitPrice(rs.getBigDecimal("unit_price"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    d.setMaterialUsed(rs.getString("material_used"));
                    d.setDiscount(rs.getBigDecimal("discount"));
                    d.setPrintSize(rs.getString("print_size"));
                    d.setColorType(rs.getString("color_type"));
                    d.setRemarks(rs.getString("remarks"));
                    d.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                    d.setCreatedBy(rs.getObject("created_by") != null ? rs.getInt("created_by") : null);
                    d.setDateUpdated(rs.getTimestamp("date_updated") != null ? rs.getTimestamp("date_updated").toLocalDateTime() : null);
                    d.setTax(rs.getBigDecimal("tax"));
                    list.add(d);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching order details: " + e.getMessage());
        }
        return list;
    }

    // --- Update order (status/payment) ---
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePayment(int orderId, String paymentStatus, String paymentMethod) {
        String sql = "UPDATE orders SET payment_status = ?, payment_method = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentStatus);
            ps.setString(2, paymentMethod);
            ps.setInt(3, orderId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating payment: " + e.getMessage());
            return false;
        }
    }

    // --- Delete order (order_details will be deleted by ON DELETE CASCADE) ---
    public boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
            return false;
        }
    }

    // --- Search orders by customer name or order_reference ---
    public List<Order> searchOrders(String keyword) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.* FROM orders o LEFT JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE c.name LIKE ? OR o.order_reference LIKE ? ORDER BY o.order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = extractOrder(rs);
                    list.add(o);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error searching orders: " + e.getMessage());
        }
        return list;
    }

    public boolean completeOrder(int orderId, Integer completedBy) {
        String sql = "UPDATE orders SET status = 'Completed', date_completed = CURDATE(), printed_by = ? WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            if (completedBy != null) {
                ps.setInt(1, completedBy);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            ps.setInt(2, orderId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error completing order: " + e.getMessage());
            return false;
        }
    }

    public boolean revertInventoryForOrder(int orderId, Integer revertedBy) {
        Connection conn = null;
        PreparedStatement psSelectDetails = null;
        PreparedStatement psGetProduct = null;
        PreparedStatement psUpdateInventory = null;
        PreparedStatement psInsertLog = null;
        PreparedStatement psGetInventoryAfter = null;
        PreparedStatement psUpdateStatus = null;
        ResultSet rsDetails = null;
        ResultSet rsProduct = null;

        String sqlSelectDetails = "SELECT product_id, quantity FROM order_details WHERE order_id = ?";
        String sqlGetProduct = "SELECT inventory_id FROM products WHERE product_id = ?";
        String sqlUpdateInventory = "UPDATE inventory SET quantity = quantity + ? WHERE inventory_id = ?";
        String sqlGetInventoryAfter = "SELECT quantity, reorder_level FROM inventory WHERE inventory_id = ?";
        String sqlUpdateStatus = "UPDATE inventory SET status = ? WHERE inventory_id = ?";
        String sqlInsertLog = "INSERT INTO logs (user_id, action, description) VALUES (?, ?, ?)";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            psSelectDetails = conn.prepareStatement(sqlSelectDetails);
            psSelectDetails.setInt(1, orderId);
            rsDetails = psSelectDetails.executeQuery();

            psGetProduct = conn.prepareStatement(sqlGetProduct);
            psUpdateInventory = conn.prepareStatement(sqlUpdateInventory);
            psInsertLog = conn.prepareStatement(sqlInsertLog);
            psGetInventoryAfter = conn.prepareStatement(sqlGetInventoryAfter);
            psUpdateStatus = conn.prepareStatement(sqlUpdateStatus);

            while (rsDetails.next()) {
                int productId = rsDetails.getInt("product_id");
                int qtyToReturn = rsDetails.getInt("quantity");

                // Get linked inventory item for product
                psGetProduct.setInt(1, productId);
                rsProduct = psGetProduct.executeQuery();

                if (rsProduct.next()) {
                    Integer inventoryId = rsProduct.getObject("inventory_id") != null ? rsProduct.getInt("inventory_id") : null;
                    if (inventoryId != null) {
                        // Restore stock
                        psUpdateInventory.setInt(1, qtyToReturn);
                        psUpdateInventory.setInt(2, inventoryId);
                        psUpdateInventory.executeUpdate();

                        // Get updated quantity and reorder level
                        psGetInventoryAfter.setInt(1, inventoryId);
                        try (ResultSet rsInv = psGetInventoryAfter.executeQuery()) {
                            if (rsInv.next()) {
                                int qty = rsInv.getInt("quantity");
                                int reorder = rsInv.getInt("reorder_level");
                                String newStatus = (qty == 0) ? "Out of Stock" : (qty <= reorder ? "Low" : "Available");

                                psUpdateStatus.setString(1, newStatus);
                                psUpdateStatus.setInt(2, inventoryId);
                                psUpdateStatus.executeUpdate();

                                // Log the restoration
                                String action = "Inventory Revert";
                                String desc = String.format("Order %d cancelled: Restored %d units to inventory_id=%d (new qty=%d)",
                                        orderId, qtyToReturn, inventoryId, qty);
                                if (revertedBy != null) psInsertLog.setInt(1, revertedBy);
                                else psInsertLog.setNull(1, java.sql.Types.INTEGER);
                                psInsertLog.setString(2, action);
                                psInsertLog.setString(3, desc);
                                psInsertLog.executeUpdate();
                            }
                        }
                    }
                }
                if (rsProduct != null) { rsProduct.close(); rsProduct = null; }
            }

            conn.commit();
            System.out.println("🔄 Inventory successfully reverted for cancelled order #" + orderId);
            return true;

        } catch (SQLException e) {
            System.err.println("Error reverting inventory for order: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            return false;
        } finally {
            try { if (rsDetails != null) rsDetails.close(); } catch (Exception ignored) {}
            try { if (rsProduct != null) rsProduct.close(); } catch (Exception ignored) {}
            try { if (psSelectDetails != null) psSelectDetails.close(); } catch (Exception ignored) {}
            try { if (psGetProduct != null) psGetProduct.close(); } catch (Exception ignored) {}
            try { if (psUpdateInventory != null) psUpdateInventory.close(); } catch (Exception ignored) {}
            try { if (psInsertLog != null) psInsertLog.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    public boolean updateOrderStatus(int orderId, String newStatus, Integer updatedBy) {
        Connection conn = null;
        PreparedStatement psUpdate = null;
        PreparedStatement psInsertLog = null;

        String sqlUpdate = "UPDATE orders SET status = ?, date_completed = " +
                "(CASE WHEN ? = 'Completed' THEN CURDATE() ELSE date_completed END) WHERE order_id = ?";
        String sqlInsertLog = "INSERT INTO logs (user_id, action, description) VALUES (?, ?, ?)";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            psUpdate = conn.prepareStatement(sqlUpdate);
            psUpdate.setString(1, newStatus);
            psUpdate.setString(2, newStatus);
            psUpdate.setInt(3, orderId);
            psUpdate.executeUpdate();

            // Log the status change
            psInsertLog = conn.prepareStatement(sqlInsertLog);
            if (updatedBy != null) psInsertLog.setInt(1, updatedBy);
            else psInsertLog.setNull(1, java.sql.Types.INTEGER);
            psInsertLog.setString(2, "Order Status Change");
            psInsertLog.setString(3, String.format("Order #%d status changed to '%s'", orderId, newStatus));
            psInsertLog.executeUpdate();

            conn.commit();
            System.out.println("✅ Order #" + orderId + " status updated to " + newStatus);
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            return false;
        } finally {
            try { if (psUpdate != null) psUpdate.close(); } catch (Exception ignored) {}
            try { if (psInsertLog != null) psInsertLog.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    // =============================================================
    //  Retrieve Order by Reference
    // =============================================================
    public Order getOrderByReference(String reference) {
        String sql = "SELECT * FROM orders WHERE order_reference = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reference);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order o = new Order();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setCustomerId(rs.getInt("customer_id"));
                    o.setUserId(rs.getInt("user_id"));
                    o.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                    o.setDueDate(rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null);
                    o.setStatus(rs.getString("status"));
                    o.setTotalAmount(rs.getBigDecimal("total_amount"));
                    o.setPaymentStatus(rs.getString("payment_status"));
                    o.setPaymentMethod(rs.getString("payment_method"));
                    o.setDiscount(rs.getBigDecimal("discount"));
                    o.setRemarks(rs.getString("remarks"));
                    o.setQuantityTotal(rs.getInt("quantity_total"));
                    o.setOrderReference(rs.getString("order_reference"));
                    o.setDateCompleted(rs.getDate("date_completed") != null ? rs.getDate("date_completed").toLocalDate() : null);
                    o.setPrintedBy(rs.getInt("printed_by"));
                    return o;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving order by reference: " + e.getMessage());
        }
        return null;
    }

    // --- Helper to convert ResultSet -> Order (without details) ---
    private Order extractOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getObject("customer_id") != null ? rs.getInt("customer_id") : null);
        o.setUserId(rs.getInt("user_id"));
        Timestamp od = rs.getTimestamp("order_date");
        if (od != null) o.setOrderDate(od.toLocalDateTime());
        Date dd = rs.getDate("due_date");
        if (dd != null) o.setDueDate(dd.toLocalDate());
        o.setStatus(rs.getString("status"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setPaymentStatus(rs.getString("payment_status"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setDiscount(rs.getBigDecimal("discount"));
        o.setRemarks(rs.getString("remarks"));
        o.setQuantityTotal(rs.getInt("quantity_total"));
        o.setOrderReference(rs.getString("order_reference"));
        Date dc = rs.getDate("date_completed");
        if (dc != null) o.setDateCompleted(dc.toLocalDate());
        o.setPrintedBy(rs.getObject("printed_by") != null ? rs.getInt("printed_by") : null);
        return o;
    }
}
