package com.tripos.pos.service;

import com.tripos.pos.model.Product;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides product catalogue data for the prototype.
 *
 * <p><strong>Mock data only.</strong> All products, prices and barcodes are
 * fictional Sri Lankan retail items drawn from the TriPOS Figma design reference
 * (01 Screens — B2 Sale / product grid). Categories match the UI tabs:
 * Tea & Coffee, Groceries, Clothing, Electronics, Gifts.</p>
 *
 * <p>When backend integration is added, replace the {@code CATALOGUE} list and
 * both {@code find*} methods with REST API calls, e.g.:
 * {@code GET /api/products?q=…} or {@code GET /api/products/barcode/{code}}.
 * </p>
 */
public class ProductCatalogueService {

    // -----------------------------------------------------------------------
    // MOCK CATALOGUE  –  Sri Lankan retail products (matches Figma design)
    //                    Replace with backend API call when ready
    // -----------------------------------------------------------------------
    private static final List<Product> CATALOGUE = List.of(

        // ── Tea & Coffee ────────────────────────────────────────────────────
        new Product("T001", "5014308046001", "Ceylon Black Tea 200g",      "Tea & Coffee",  1250.00, "pack"),
        new Product("T002", "5014308046002", "Ceylon Green Tea 100g",      "Tea & Coffee",  1100.00, "pack"),
        new Product("T003", "5014308046003", "Cinnamon Tea 20 Bags",       "Tea & Coffee",   850.00, "box"),
        new Product("T004", "5014308046004", "Lemongrass Herbal Tea",      "Tea & Coffee",   720.00, "box"),
        new Product("T005", "5014308046005", "Pure Ceylon OP1 Loose Leaf", "Tea & Coffee",  1650.00, "pack"),

        // ── Groceries ───────────────────────────────────────────────────────
        new Product("G001", "5014308047001", "Coconut Oil 1L",             "Groceries",      880.00, "bottle"),
        new Product("G002", "5014308047002", "Kithul Jaggery 500g",        "Groceries",      660.00, "pack"),
        new Product("G003", "5014308047003", "Cinnamon Sticks 50g",        "Groceries",      850.00, "pack"),
        new Product("G004", "5014308047004", "Dried Maldive Fish 100g",    "Groceries",      480.00, "pack"),
        new Product("G005", "5014308047005", "King Coconut Water 330ml",   "Groceries",      355.00, "each"),
        new Product("G006", "5014308047006", "Ayurvedic Balm 50g",         "Groceries",      720.00, "jar"),
        new Product("G007", "5014308047007", "Pol Sambol Paste 250g",      "Groceries",      390.00, "jar"),

        // ── Clothing ────────────────────────────────────────────────────────
        new Product("C001", "5014308048001", "Batik T-shirt (M)",          "Clothing",      2450.00, "each"),
        new Product("C002", "5014308048002", "Batik Sarong",               "Clothing",      1800.00, "each"),
        new Product("C003", "5014308048003", "Handloom Saree",             "Clothing",      3000.00, "each"),
        new Product("C004", "5014308048004", "Linen Beach Shirt",          "Clothing",      2800.00, "each"),

        // ── Electronics ─────────────────────────────────────────────────────
        new Product("E001", "5014308049001", "Bluetooth Speaker (Mini)",   "Electronics",  14900.00, "each"),
        new Product("E002", "5014308049002", "USB-C Charging Hub",         "Electronics",   4200.00, "each"),
        new Product("E003", "5014308049003", "Wireless Earbuds",           "Electronics",   8900.00, "each"),

        // ── Gifts ───────────────────────────────────────────────────────────
        new Product("F001", "5014308050001", "Spice Gift Box (Assorted)",  "Gifts",         4500.00, "box"),
        new Product("F002", "5014308050002", "Wooden Elephant Souvenir",   "Gifts",         3100.00, "each"),
        new Product("F003", "5014308050003", "Mask Fridge Magnet",         "Gifts",          495.00, "each"),
        new Product("F004", "5014308050004", "Colombo City Map Print",     "Gifts",          990.00, "each"),
        new Product("F005", "5014308050005", "Coconut Shell Bowl",         "Gifts",         1500.00, "each"),
        new Product("F006", "5014308050006", "Roorala Lace Coasters (4pk)","Gifts",         1200.00, "set")
    );

    // -----------------------------------------------------------------------
    // Public API  –  replace method bodies with HTTP calls when backend is up
    // -----------------------------------------------------------------------

    /**
     * Returns products whose name or barcode contains {@code query}
     * (case-insensitive). A blank query returns the full catalogue.
     */
    public List<Product> search(String query) {
        if (query == null || query.isBlank()) return CATALOGUE;
        String q = query.strip().toLowerCase();
        return CATALOGUE.stream()
                .filter(p -> p.getName().toLowerCase().contains(q)
                          || p.getBarcode().contains(q)
                          || p.getCategory().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /**
     * Looks up a product by exact barcode. Returns {@code null} when not found.
     * A real implementation: {@code GET /api/products/barcode/{code}}.
     */
    public Product findByBarcode(String barcode) {
        return CATALOGUE.stream()
                .filter(p -> p.getBarcode().equals(barcode))
                .findFirst()
                .orElse(null);
    }

    /** Returns the full mock catalogue (unfiltered). */
    public List<Product> getAll() { return CATALOGUE; }
}
