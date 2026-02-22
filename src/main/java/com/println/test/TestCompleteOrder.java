package com.println.test;

import com.println.dao.OrderDAO;

public class TestCompleteOrder {
    public static void main(String[] args) {
        OrderDAO dao = new OrderDAO();
        int orderId = 1; // change to an actual order id you created via TestOrderDAO
        boolean ok = dao.completeOrder(orderId, 1); // 1 = admin user_id who completed it
        System.out.println(ok ? "✅ Order completed and inventory updated." : "Failed to complete order.");
    }
}
