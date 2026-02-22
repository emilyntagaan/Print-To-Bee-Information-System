package com.println.service;

import com.println.dao.InventoryDAO;
import com.println.dao.OrderDAO;
import com.println.dao.ProductDAO;
import com.println.model.Order;
import com.println.model.OrderDetail;
import com.println.model.Product;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * OrderService acts as the middle layer between the UI and DAO classes.
 * It handles business logic such as generating order references,
 * coordinating order and order_details insertion, and managing
 * inventory updates on completion or cancellation.
 */
public class OrderService {

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final InventoryDAO inventoryDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
        this.inventoryDAO = new InventoryDAO();
    }

    // 1. Generate unique Order Reference
    private String generateOrderReference() {
        LocalDate today = LocalDate.now();
        String datePart = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        int sequence = (int) (System.currentTimeMillis() % 1000);
        return String.format("ORD-%s-%03d", datePart, sequence);
    }

    // -----------------------------------------------------
    // 🔹 2. Place a new order (delegates transaction to DAO)
    // -----------------------------------------------------
    public boolean placeOrder(Order order, List<OrderDetail> orderDetails, int createdBy) {
        try {
            order.setUserId(createdBy);
            order.setDetails(orderDetails);

            // --- Ensure Status Always Exists ---
            if (order.getStatus() == null || order.getStatus().trim().isEmpty()) {
                order.setStatus("Pending");
            }

            // --- Ensure Payment Status Is Kept (No Overwriting) ---
            if (order.getPaymentStatus() == null || order.getPaymentStatus().trim().isEmpty()) {
                order.setPaymentStatus("Unpaid"); // only defaults if UI gave nothing
            }

            // --- Ensure Payment Method Is Stored ---
            if (order.getPaymentMethod() == null || order.getPaymentMethod().trim().isEmpty()) {
                order.setPaymentMethod("Cash"); // default fallback, safe
            }

            int orderId = orderDAO.addOrder(order);

            if (orderId <= 0) {
                System.err.println("Failed to insert order.");
                return false;
            }

            System.out.println("✅ Order placed successfully!");
            return true;

        } catch (Exception e) {
            System.err.println("Error placing order: " + e.getMessage());
            return false;
        }
    }



    // 3. COMPLETE ORDER — Deduct inventory
    public boolean completeOrder(int orderId, int completedBy) {
        System.out.println("Completing order #" + orderId + "...");

        boolean done = orderDAO.completeOrder(orderId, completedBy);

        if (done) {
            System.out.println("Order #" + orderId + " completed & inventory updated.");
        } else {
            System.err.println("Failed to complete order.");
        }

        return done;
    }


    // 4. CANCEL ORDER — Restore inventory
    public boolean cancelOrder(int orderId, int cancelledBy) {
        System.out.println("Cancelling order #" + orderId + "...");

        List<OrderDetail> details = orderDAO.getOrderDetails(orderId);

        if (details != null) {
            for (OrderDetail d : details) {
                Product p = productDAO.getProductById(d.getProductId());
                if (p == null) continue;

                Integer invId = p.getInventoryId();
                if (invId == null) continue;

                inventoryDAO.incrementStock(invId, d.getQuantity());
            }
        }

        boolean statusUpdated = orderDAO.updateOrderStatus(orderId, "Cancelled", cancelledBy);

        if (statusUpdated) {
            System.out.println("Order #" + orderId + " cancelled. Inventory restored.");
            return true;
        }

        System.err.println("Failed to cancel order #" + orderId);
        return false;
    }

    //  5. Manual status change
    public boolean updateOrderStatus(int orderId, String status, int userId) {
        return orderDAO.updateOrderStatus(orderId, status, userId);
    }
}
