package com.println.test;

import com.println.dao.OrderDAO;
import com.println.model.Order;
import com.println.model.OrderDetail;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestOrderDAO {
    public static void main(String[] args) {
        OrderDAO dao = new OrderDAO();

        // --- Create an order with 1 detail ---
        Order order = new Order();
        order.setCustomerId(1); // existing customer (or null for walk-in)
        order.setUserId(1); // staff/admin
        order.setStatus("Pending");
        order.setPaymentStatus("Unpaid");
        order.setPaymentMethod("Cash");
        order.setDiscount(BigDecimal.ZERO);
        order.setRemarks("Test order from TestOrderDAO");
        order.setQuantityTotal(0); // will be recalculated by DAO

        // Create details
        List<OrderDetail> details = new ArrayList<>();
        OrderDetail d1 = new OrderDetail();
        d1.setProductId(1);            // must exist in products table
        d1.setQuantity(2);
        d1.setUnitPrice(new BigDecimal("250.00"));
        d1.setDiscount(new BigDecimal("0.00"));
        d1.setTax(new BigDecimal("0.00"));
        d1.setCreatedBy(1);
        details.add(d1);

        order.setDetails(details);

        int orderId = dao.addOrder(order);
        if (orderId > 0) {
            System.out.println("✅ Order created with ID: " + orderId);
            Order saved = dao.getOrderById(orderId);
            System.out.println("Order loaded: " + saved);
            System.out.println("Details:");
            saved.getDetails().forEach(System.out::println);
        } else {
            System.out.println("Failed to create order.");
        }

        // --- Example: search orders ---
        List<Order> results = dao.searchOrders("ORD-");
        System.out.println("\nSearch results (sample):");
        for (Order o : results) {
            System.out.println(o);
        }
    }
}
