package com.println.test;

import com.println.dao.OrderDAO;
import com.println.model.Order;
import com.println.model.OrderDetail;
import com.println.service.OrderService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestOrderService {
    public static void main(String[] args) {
        OrderService service = new OrderService();
        OrderDAO orderDAO = new OrderDAO();

        // --- Create a mock order ---
        Order order = new Order();
        order.setCustomerId(1);
        order.setUserId(1);
        order.setTotalAmount(new BigDecimal("500.00"));
        order.setPaymentMethod("Cash");
        order.setRemarks("Test Order from Service Layer");

        // --- Add order details ---
        List<OrderDetail> details = new ArrayList<>();
        OrderDetail d1 = new OrderDetail();
        d1.setProductId(1);
        d1.setQuantity(5);
        d1.setUnitPrice(new BigDecimal("100.00"));
        details.add(d1);

        // --- Place Order ---
        boolean placed = service.placeOrder(order, details, 1);
        System.out.println(placed ? "Order placed!" : "Failed to place order.");

        // --- Verification: Fetch and display the newly inserted order ---
        if (placed) {
            System.out.println("\n🔍 Verifying inserted order...");

            // Optional: if you stored the reference inside the object, use that
            String ref = order.getOrderReference();
            if (ref != null) {
                Order fetched = orderDAO.getOrderByReference(ref);
                if (fetched != null) {
                    System.out.println("Order found in database:");
                    System.out.println("   Order ID: " + fetched.getOrderId());
                    System.out.println("   Reference: " + fetched.getOrderReference());
                    System.out.println("   Total Amount: " + fetched.getTotalAmount());
                    System.out.println("   Status: " + fetched.getStatus());
                } else {
                    System.out.println("Order not found in database (reference: " + ref + ")");
                }
            } else {
                System.out.println("No reference available — skipping DB check.");
            }
        }

        // --- Complete Order (optional) ---
        // boolean completed = service.completeOrder(1, 1);
        // System.out.println(completed ? "Order completed!" : "Failed to complete order.");
    }
}