package com.tripos.pos.model;

/**
 * Represents a product in the catalogue.
 *
 * <p>This is a plain data holder intentionally free of persistence annotations
 * so it can be adapted to a backend API response later (e.g. mapped from JSON).
 * All product data here is <strong>mock / prototype data</strong>.</p>
 */
public class Product {

    private final String id;
    private final String barcode;
    private final String name;
    private final String category;
    private final double unitPrice;
    /** Display unit, e.g. "each", "kg", "litre" */
    private final String unit;

    public Product(String id, String barcode, String name,
                   String category, double unitPrice, String unit) {
        this.id        = id;
        this.barcode   = barcode;
        this.name      = name;
        this.category  = category;
        this.unitPrice = unitPrice;
        this.unit      = unit;
    }

    // ---- Accessors --------------------------------------------------------

    public String getId()        { return id; }
    public String getBarcode()   { return barcode; }
    public String getName()      { return name; }
    public String getCategory()  { return category; }
    public double getUnitPrice() { return unitPrice; }
    public String getUnit()      { return unit; }

    /** Convenience: returns "name (barcode)" for list display. */
    @Override
    public String toString() {
        return name + "  [" + barcode + "]";
    }
}
