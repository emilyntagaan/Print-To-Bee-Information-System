package com.println.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int productId;
    private String productName;
    private String description;
    private String category;
    private BigDecimal price;
    private String unit;
    private String materialUsed;
    private int quantityUsed;
    private int reorderLevel;
    private String status;
    private LocalDateTime dateAdded;
    private Integer addedBy;
    private String printTime;
    private String size;
    private String notes;
    private Integer inventoryId;

    // --- Constructors ---
    public Product() {}

    public Product(String productName, BigDecimal price, String unit, String category) {
        this.productName = productName;
        this.price = price;
        this.unit = unit;
        this.category = category;
        this.status = "Active";
    }

    // --- Getters and Setters ---
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getMaterialUsed() { return materialUsed; }
    public void setMaterialUsed(String materialUsed) { this.materialUsed = materialUsed; }

    public int getQuantityUsed() { return quantityUsed; }
    public void setQuantityUsed(int quantityUsed) { this.quantityUsed = quantityUsed; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; }

    public Integer getAddedBy() { return addedBy; }
    public void setAddedBy(Integer addedBy) { this.addedBy = addedBy; }

    public String getPrintTime() { return printTime; }
    public void setPrintTime(String printTime) { this.printTime = printTime; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Integer getInventoryId() {return inventoryId;}   
    public void setInventoryId(Integer inventoryId) {this.inventoryId = inventoryId;}

    @Override
    public String toString() {
        return "Product [productId=" + productId + ", name=" + productName +
                ", category=" + category + ", price=" + price +
                ", unit=" + unit + ", status=" + status + "]";
    }
}
