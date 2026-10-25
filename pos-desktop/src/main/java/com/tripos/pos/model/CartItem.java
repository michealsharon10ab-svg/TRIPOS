package com.tripos.pos.model;

import javafx.beans.property.*;

/**
 * A single line-item in the shopping cart, wrapping a {@link Product} with a
 * quantity that supports JavaFX property binding so the cart table updates live.
 *
 * <p>Line total is derived: {@code quantity × product.unitPrice}.</p>
 */
public class CartItem {

    private final Product product;
    private final IntegerProperty quantity = new SimpleIntegerProperty(1);

    public CartItem(Product product) {
        this.product = product;
    }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity.set(quantity);
    }

    // ---- Product passthrough -----------------------------------------------

    public Product getProduct()   { return product; }
    public String  getName()      { return product.getName(); }
    public double  getUnitPrice() { return product.getUnitPrice(); }
    public String  getBarcode()   { return product.getBarcode(); }

    // ---- Quantity (observable) ---------------------------------------------

    public IntegerProperty quantityProperty() { return quantity; }
    public int  getQuantity()                 { return quantity.get(); }
    public void setQuantity(int qty)          { quantity.set(qty); }

    // ---- Derived -----------------------------------------------------------

    /** Returns {@code quantity × unitPrice}. */
    public double getLineTotal() {
        return quantity.get() * product.getUnitPrice();
    }
}
