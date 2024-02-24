package com.silverplate.silverplate.Model;

import com.silverplate.silverplate.SQLConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.function.Supplier;

public class Inventory {


    private int inventoryId;
    private String inventoryName;
    private int supplierId;
    public String supplierName;
    private int inventoryQuantity;
    private LocalDate dateReceived;
    private String inventoryCategory;

    public Inventory(int inventoryId, String inventoryName, int supplierId, String supplierName, int inventoryQuantity, LocalDate dateReceived, String inventoryCategory) {
        this.inventoryId = inventoryId;
        this.inventoryName = inventoryName;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.inventoryQuantity = inventoryQuantity;
        this.dateReceived = dateReceived;
        this.inventoryCategory = inventoryCategory;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public int getInventoryQuantity() {
        return inventoryQuantity;
    }

    public void setInventoryQuantity(int inventoryQuantity) {
        this.inventoryQuantity = inventoryQuantity;
    }

    public LocalDate getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(LocalDate dateReceived) {
        this.dateReceived = dateReceived;
    }

    public String getInventoryCategory() {
        return inventoryCategory;
    }

    public void setInventoryCategory(String inventoryCategory) {
        this.inventoryCategory = inventoryCategory;
    }

    @Override
    public String toString() {
        return getInventoryName() +
                " | Quantity: " + getInventoryQuantity() +
                " | Date Received: " + getDateReceived() +
                " | Category: " + getInventoryCategory() +
                " | Supplier: " + getSupplierName();
    }
}
