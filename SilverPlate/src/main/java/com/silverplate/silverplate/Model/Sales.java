package com.silverplate.silverplate.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.time.LocalDate;
import java.util.Date;

public class Sales {
    private int saleId;
    private LocalDate saleDate;
    private float salePrice;
    private ObservableList<Product> products;
    private ObservableList<Integer> quantities;
    private ObservableList<String> productsAndQuantitiesDisplay = FXCollections.observableArrayList();



    public Sales(int saleId, LocalDate saleDate, float salePrice, ObservableList<Product> products, ObservableList<Integer> quantities) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.salePrice = salePrice;
        this.products = products;
        this.quantities = quantities;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public float getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(float salePrice) {
        this.salePrice = salePrice;
    }

    public ObservableList<Product> getProducts() {
        return products;
    }

    public void setProducts(ObservableList<Product> products) {
        this.products = products;
    }

    public ObservableList<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(ObservableList<Integer> quantities) {
        this.quantities = quantities;
    }

    public ObservableList<String> getProductsAndQuantitiesDisplay() {
        return this.productsAndQuantitiesDisplay;
    }

}
