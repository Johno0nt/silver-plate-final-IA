package com.silverplate.silverplate.Model;

import javafx.collections.ObservableList;

public class Product {
    private int productId;
    private String productCategory;
    private String productName;
    private float productPrice;
    private ObservableList<Inventory> ingredients;
    private ObservableList<Integer> ingredientQuantity;

    public Product(int productId, String productCategory, String productName, float productPrice, ObservableList<Inventory> ingredients, ObservableList<Integer> ingredientQuantity) {
        this.productId = productId;
        this.productCategory = productCategory;
        this.productName = productName;
        this.productPrice = productPrice;
        this.ingredients = ingredients;
        this.ingredientQuantity = ingredientQuantity;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public float getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(float productPrice) {
        this.productPrice = productPrice;
    }

    public ObservableList<Inventory> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ObservableList<Inventory> ingredients) {
        this.ingredients = ingredients;
    }

    public ObservableList<Integer> getIngredientQuantity() {
        return ingredientQuantity;
    }

    public void setIngredientQuantity(ObservableList<Inventory> ingredients) {
        this.ingredientQuantity = ingredientQuantity;
    }

}
