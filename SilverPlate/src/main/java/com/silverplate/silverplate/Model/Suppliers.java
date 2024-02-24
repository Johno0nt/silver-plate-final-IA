package com.silverplate.silverplate.Model;

public class Suppliers {
    private String supplierName;
    private String supplierEmail;
    private String supplierPhone;
    private String inventoryCategory;
    private int supplierId;

    public Suppliers(String supplierName, String supplierPhone, String supplierEmail, String inventoryCategory, int supplierId) {
        this.supplierName = supplierName;
        this.supplierEmail = supplierEmail;
        this.supplierPhone = supplierPhone;
        this.inventoryCategory = inventoryCategory;
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    public String getSupplierPhone() {
        return supplierPhone;
    }

    public void setSupplierPhone(String supplierPhone) {
        this.supplierPhone = supplierPhone;
    }

    public String getInventoryCategory() {
        return inventoryCategory;
    }

    public void setInventoryCategory(String inventoryCategory) {
        this.inventoryCategory = inventoryCategory;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }
}
