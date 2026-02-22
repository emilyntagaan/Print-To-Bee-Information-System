package com.println.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private Integer customerId; // nullable for walk-ins
    private String customerName;
    private Integer userId; // staff who created the order
    private LocalDateTime orderDate;
    private LocalDate dueDate;
    private String status;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal discount;
    private String remarks;
    private int quantityTotal;
    private String orderReference;
    private LocalDate dateCompleted;
    private Integer printedBy;
    private List<OrderDetail> details = new ArrayList<>();

    // --- Constructors ---
    public Order() {}

    public Order(Integer customerId, Integer userId, BigDecimal totalAmount) {
        this.customerId = customerId;
        this.userId = userId;
        this.totalAmount = totalAmount;
    }

    // --- Getters / Setters ---
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public int getQuantityTotal() { return quantityTotal; }
    public void setQuantityTotal(int quantityTotal) { this.quantityTotal = quantityTotal; }

    public String getOrderReference() { return orderReference; }
    public void setOrderReference(String orderReference) { this.orderReference = orderReference; }

    public LocalDate getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(LocalDate dateCompleted) { this.dateCompleted = dateCompleted; }

    public Integer getPrintedBy() { return printedBy; }
    public void setPrintedBy(Integer printedBy) { this.printedBy = printedBy; }

    public List<OrderDetail> getDetails() { return details; }
    public void setDetails(List<OrderDetail> details) { this.details = details; }

    @Override
    public String toString() {
        return "Order [orderId=" + orderId +
               ", cust=" + customerId +
               ", user=" + userId +
               ", total=" + totalAmount +
               ", status=" + status +
               ", qty=" + quantityTotal +
               ", ref=" + orderReference + "]";
    }
}
