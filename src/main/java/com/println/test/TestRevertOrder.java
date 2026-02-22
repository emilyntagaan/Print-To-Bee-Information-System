package com.println.test;

import com.println.dao.OrderDAO;

public class TestRevertOrder {
    public static void main(String[] args) {
        OrderDAO dao = new OrderDAO();
        int orderId = 1; // replace with order ID to revert
        boolean ok = dao.revertInventoryForOrder(orderId, 1); // 1 = admin user ID
        System.out.println(ok ? "✅ Revert successful!" : "Revert failed.");
    }
}
