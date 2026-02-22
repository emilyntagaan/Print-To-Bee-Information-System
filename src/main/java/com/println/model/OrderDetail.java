package com.println.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDetail {
    private int orderDetailId;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal; // DB has generated column, but we can store it here too
    private String materialUsed;
    private BigDecimal discount;
    private String printSize;
    private String colorType;
    private String remarks;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime dateUpdated;
    private BigDecimal tax;

    // --- Constructors ---
    public OrderDetail() {}

    public OrderDetail(int productId, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // --- Getters / Setters ---
    public int getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(int orderDetailId) { this.orderDetailId = orderDetailId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getMaterialUsed() { return materialUsed; }
    public void setMaterialUsed(String materialUsed) { this.materialUsed = materialUsed; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public String getPrintSize() { return printSize; }
    public void setPrintSize(String printSize) { this.printSize = printSize; }

    public String getColorType() { return colorType; }
    public void setColorType(String colorType) { this.colorType = colorType; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime dateUpdated) { this.dateUpdated = dateUpdated; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    @Override
    public String toString() {
        return "OrderDetail [id=" + orderDetailId + ", orderId=" + orderId +
               ", productId=" + productId + ", qty=" + quantity + ", unitPrice=" + unitPrice + "]";
    }
}
