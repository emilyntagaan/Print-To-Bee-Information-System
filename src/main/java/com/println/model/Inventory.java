package com.println.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Inventory {
    private int inventoryId;
    private String itemName;
    private String description;
    private String category;
    private String unit;
    private int quantity;
    private int reorderLevel;
    private String supplierName;
    private LocalDate lastRestockDate;
    private String status;
    private Integer addedBy;
    private LocalDateTime dateAdded;
    private LocalDate dateUpdated;
    private String remarks;
    private BigDecimal costPerUnit;

    // --- Constructors ---
    public Inventory() {}

    public Inventory(String itemName, String unit, int quantity, BigDecimal costPerUnit) {
        this.itemName = itemName;
        this.unit = unit;
        this.quantity = quantity;
        this.costPerUnit = costPerUnit;
        this.status = "Available";
    }

    // --- Getters and Setters ---
    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public LocalDate getLastRestockDate() { return lastRestockDate; }
    public void setLastRestockDate(LocalDate lastRestockDate) { this.lastRestockDate = lastRestockDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getAddedBy() { return addedBy; }
    public void setAddedBy(Integer addedBy) { this.addedBy = addedBy; }

    public LocalDateTime getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; }

    public LocalDate getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDate dateUpdated) { this.dateUpdated = dateUpdated; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public BigDecimal getCostPerUnit() { return costPerUnit; }
    public void setCostPerUnit(BigDecimal costPerUnit) { this.costPerUnit = costPerUnit; }

    @Override
    public String toString() {
        return "Inventory [inventoryId=" + inventoryId + ", itemName=" + itemName +
                ", quantity=" + quantity + ", costPerUnit=" + costPerUnit +
                ", status=" + status + ", supplier=" + supplierName + "]";
    }
}
