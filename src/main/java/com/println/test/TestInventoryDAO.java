package com.println.test;

import com.println.dao.InventoryDAO;
import com.println.model.Inventory;
import java.util.List;
import java.time.LocalDate;

public class TestInventoryDAO {
    // public static void main(String[] args) {
    //     InventoryDAO dao = new InventoryDAO();

    //     // --- Add inventory item ---
    //     Inventory inv = new Inventory();
    //     inv.setItemName("Ink Cartridge");
    //     inv.setDescription("Black ink cartridge for Epson printer");
    //     inv.setCategory("Printer Supplies");
    //     inv.setUnit("piece");
    //     inv.setQuantity(50);
    //     inv.setReorderLevel(10);
    //     inv.setSupplierName("Tech Supplies PH");
    //     inv.setLastRestockDate(LocalDate.now());
    //     inv.setStatus("Available");
    //     inv.setRemarks("Fresh stock delivered");
    //     inv.setCostPerUnit(120.50);
    //     inv.setAddedBy(1); // Assuming admin ID = 1

    //     boolean added = dao.addInventoryItem(inv);
    //     System.out.println(added ? "✅ Inventory item added!" : "❌ Failed to add inventory item.");

    //     // --- Read all items ---
    //     List<Inventory> list = dao.getAllItems();
    //     for (Inventory i : list) {
    //         System.out.println(i);
    //     }

    //     // --- Search ---
    //     System.out.println("\n🔍 Search results for 'Ink':");
    //     List<Inventory> results = dao.searchItems("Ink");
    //     for (Inventory i : results) {
    //         System.out.println(i);
    //     }
    // }
}
