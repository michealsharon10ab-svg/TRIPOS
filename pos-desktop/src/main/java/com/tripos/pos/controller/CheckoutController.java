package com.tripos.pos.controller;

import com.tripos.pos.model.CartItem;
import com.tripos.pos.model.PaymentMethod;
import com.tripos.pos.model.Product;
import com.tripos.pos.service.CheckoutService;
import com.tripos.pos.service.ProductCatalogueService;
import com.tripos.pos.util.CurrencyFormatter;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Checkout controller — TriPOS POS Sale screen.
 *
 * Layout matches Figma "B2 Sale":
 *  - LEFT  : Product catalogue with category tabs + product card grid.
 *  - RIGHT : Current sale panel (cart, totals, Pay button).
 *
 * All mock data is clearly isolated in {@link ProductCatalogueService} and
 * {@link CheckoutService} for later replacement with backend API calls.
 */
public class CheckoutController implements Initializable {

    // ── Services ──────────────────────────────────────────────────────────
    private final ProductCatalogueService catalogue = new ProductCatalogueService();
    private final CheckoutService         checkout  = new CheckoutService();

    // ── Cart state ────────────────────────────────────────────────────────
    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();
    private double appliedDiscountPct = 0.0;

    // ── Category filter ───────────────────────────────────────────────────
    private static final String ALL_CAT = "All";
    private final List<String> categories = List.of(
        ALL_CAT, "Tea & Coffee", "Groceries", "Clothing", "Electronics", "Gifts");
    private String activeCategory = ALL_CAT;
    private ToggleGroup categoryGroup;

    // ══════════════════════════════════════════════════════════════════════
    // FXML injections
    // ══════════════════════════════════════════════════════════════════════

    // Header
    @FXML private Label parkedBillsLabel;

    // Left panel – catalogue
    @FXML private TextField  searchField;
    @FXML private Button     clearSearchBtn;
    @FXML private HBox       categoryTabsBox;
    @FXML private FlowPane   productGrid;
    @FXML private ScrollPane productScrollPane;
    @FXML private Label      catalogueStatusLabel;

    // Right panel – current sale
    @FXML private Label  invoiceRefLabel;
    @FXML private Label  customerInitials;
    @FXML private Label  customerName;
    @FXML private Label  customerPoints;
    @FXML private Label  loyaltyBadge;
    @FXML private VBox   saleItemsBox;
    @FXML private VBox   emptyCartPlaceholder;
    @FXML private Label  subtotalLabel;
    @FXML private Label  discountLabel;
    @FXML private Label  discountRowLabel;
    @FXML private Label  vatLabel;
    @FXML private Label  totalLabel;
    @FXML private Button payButton;
    @FXML private Label  validationLabel;

    // ══════════════════════════════════════════════════════════════════════
    // Initialise
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildCategoryTabs();
        refreshProductGrid(catalogue.getAll());
        refreshSalePanel();

        // Live search
        searchField.textProperty().addListener((obs, old, nv) -> {
            clearSearchBtn.setVisible(!nv.isBlank());
            clearSearchBtn.setManaged(!nv.isBlank());
            filterProducts();
        });

        // Enter key on search = barcode lookup
        // (handled via onSearchEnter FXML binding)
    }

    // ══════════════════════════════════════════════════════════════════════
    // Category tabs (built programmatically to avoid FXML ToggleGroup refs)
    // ══════════════════════════════════════════════════════════════════════

    private void buildCategoryTabs() {
        categoryGroup = new ToggleGroup();
        for (String cat : categories) {
            ToggleButton btn = new ToggleButton(cat);
            btn.getStyleClass().add("category-tab");
            btn.setToggleGroup(categoryGroup);
            if (cat.equals(ALL_CAT)) btn.setSelected(true);
            btn.setOnAction(e -> {
                activeCategory = cat;
                filterProducts();
            });
            categoryTabsBox.getChildren().add(btn);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Product grid
    // ══════════════════════════════════════════════════════════════════════

    private void filterProducts() {
        String query = searchField.getText().strip();
        List<Product> results = catalogue.search(query);

        if (!activeCategory.equals(ALL_CAT)) {
            results = results.stream()
                    .filter(p -> p.getCategory().equals(activeCategory))
                    .toList();
        }
        refreshProductGrid(results);
    }

    private void refreshProductGrid(List<Product> products) {
        productGrid.getChildren().clear();
        for (Product p : products) {
            productGrid.getChildren().add(buildProductCard(p));
        }
        int total = catalogue.getAll().size();
        int shown = products.size();
        catalogueStatusLabel.setText(shown == total
                ? total + " products"
                : shown + " of " + total + " products");
    }

    /** Builds a single product card matching the Figma card design. */
    private VBox buildProductCard(Product product) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(160);
        card.setMaxWidth(160);
        card.setOnMouseClicked(e -> addToCart(product));

        // ── Image placeholder area ────────────────────────────────────────
        StackPane imgArea = new StackPane();
        imgArea.getStyleClass().add("card-img-area");
        imgArea.setPrefHeight(110);

        // Category icon (simple letter/emoji as placeholder)
        Label icon = new Label(categoryIcon(product.getCategory()));
        icon.getStyleClass().add("card-img-icon");
        imgArea.getChildren().add(icon);

        // Stock badge (top-right)
        Label stockBadge = new Label("in stock");
        stockBadge.getStyleClass().add("badge-in-stock");
        StackPane.setAlignment(stockBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(stockBadge, new Insets(8, 8, 0, 0));
        imgArea.getChildren().add(stockBadge);

        card.getChildren().add(imgArea);

        // ── Card body ─────────────────────────────────────────────────────
        VBox body = new VBox(4);
        body.getStyleClass().add("card-body");
        body.setPadding(new Insets(10, 10, 10, 10));

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("card-name");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(CurrencyFormatter.format(product.getUnitPrice()));
        priceLabel.getStyleClass().add("card-price");

        body.getChildren().addAll(nameLabel, priceLabel);
        card.getChildren().add(body);

        return card;
    }

    private String categoryIcon(String category) {
        return switch (category) {
            case "Tea & Coffee" -> "☕";
            case "Groceries"    -> "🥥";
            case "Clothing"     -> "👕";
            case "Electronics"  -> "🔌";
            case "Gifts"        -> "🎁";
            default             -> "📦";
        };
    }

    // ══════════════════════════════════════════════════════════════════════
    // Cart management
    // ══════════════════════════════════════════════════════════════════════

    private void addToCart(Product product) {
        // If already in cart, increment quantity
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                refreshSalePanel();
                return;
            }
        }
        cart.add(new CartItem(product, 1));
        refreshSalePanel();
        clearValidation();
    }

    private void removeFromCart(CartItem item) {
        cart.remove(item);
        refreshSalePanel();
    }

    private void changeQty(CartItem item, int delta) {
        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) {
            // Confirm before removing
            cart.remove(item);
        } else {
            item.setQuantity(newQty);
        }
        refreshSalePanel();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Right panel refresh
    // ══════════════════════════════════════════════════════════════════════

    private void refreshSalePanel() {
        boolean empty = cart.isEmpty();

        emptyCartPlaceholder.setVisible(empty);
        emptyCartPlaceholder.setManaged(empty);

        // Rebuild item rows
        saleItemsBox.getChildren().clear();
        for (CartItem ci : cart) {
            saleItemsBox.getChildren().add(buildSaleItemRow(ci));
            // thin divider between items
            Separator sep = new Separator();
            sep.getStyleClass().add("item-separator");
            saleItemsBox.getChildren().add(sep);
        }

        // Totals
        double sub  = checkout.subtotal(cart);
        double disc = checkout.discountAmount(sub, appliedDiscountPct);
        double vat  = checkout.taxAmount(sub, disc);
        double tot  = checkout.total(sub, disc, vat);

        subtotalLabel.setText(CurrencyFormatter.format(sub));

        if (disc > 0) {
            discountLabel.setText(CurrencyFormatter.formatDiscount(disc));
            discountRowLabel.setText("Discount (" + (int) appliedDiscountPct + "%)");
        } else {
            discountLabel.setText("–");
            discountRowLabel.setText("Discount");
        }

        vatLabel.setText(CurrencyFormatter.format(vat));
        totalLabel.setText(CurrencyFormatter.format(tot));

        // Pay button label
        payButton.setText(empty
                ? "Pay  ·  F8"
                : "Pay " + CurrencyFormatter.format(tot) + "  ·  F8");

        // Invoice ref
        invoiceRefLabel.setText(empty
                ? "New sale"
                : "Inv CCN-" + String.format("%06d", cart.hashCode() & 0xFFFFF) + "  ·  " + cart.size() + " item" + (cart.size() == 1 ? "" : "s"));
    }

    /** Builds a single current-sale item row matching the Figma style. */
    private HBox buildSaleItemRow(CartItem ci) {
        HBox row = new HBox(10);
        row.getStyleClass().add("sale-item-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));

        // Product info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(ci.getName());
        nameLbl.getStyleClass().add("sale-item-name");
        Label unitLbl = new Label(ci.getProduct().getUnit());
        unitLbl.getStyleClass().add("sale-item-variant");
        info.getChildren().addAll(nameLbl, unitLbl);

        // Qty controls  [−] n [+]
        HBox qtyBox = new HBox(4);
        qtyBox.getStyleClass().add("qty-control");
        qtyBox.setAlignment(Pos.CENTER);

        Button minus = new Button("−");
        minus.getStyleClass().add("qty-btn");
        Label qtyLbl = new Label(String.valueOf(ci.getQuantity()));
        qtyLbl.getStyleClass().add("qty-value");
        qtyLbl.setMinWidth(24);
        qtyLbl.setAlignment(Pos.CENTER);
        Button plus = new Button("+");
        plus.getStyleClass().add("qty-btn");

        minus.setOnAction(e -> changeQty(ci, -1));
        plus.setOnAction(e -> changeQty(ci, +1));
        qtyBox.getChildren().addAll(minus, qtyLbl, plus);

        // Price
        Label priceLbl = new Label(CurrencyFormatter.format(ci.getLineTotal()));
        priceLbl.getStyleClass().add("sale-item-price");
        priceLbl.setMinWidth(90);
        priceLbl.setAlignment(Pos.CENTER_RIGHT);

        // Remove button
        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("sale-remove-btn");
        removeBtn.setOnAction(e -> removeFromCart(ci));

        row.getChildren().addAll(info, qtyBox, priceLbl, removeBtn);
        return row;
    }

    // ══════════════════════════════════════════════════════════════════════
    // FXML actions — Search
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void onSearchEnter() {
        String q = searchField.getText().strip();
        // Try barcode lookup first
        Product found = catalogue.findByBarcode(q);
        if (found != null) {
            addToCart(found);
            searchField.clear();
        } else {
            filterProducts();
        }
    }

    @FXML
    private void onClearSearch() {
        searchField.clear();
        filterProducts();
    }

    // ══════════════════════════════════════════════════════════════════════
    // FXML actions — Sale actions
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void onDiscountClicked() {
        if (cart.isEmpty()) { showValidation("Add items before applying a discount."); return; }

        TextInputDialog dialog = new TextInputDialog(String.valueOf((int) appliedDiscountPct));
        dialog.setTitle("Apply Discount");
        dialog.setHeaderText("Enter discount percentage (0 – 100)");
        dialog.setContentText("Discount %:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double pct = Double.parseDouble(input.strip());
                if (pct < 0 || pct > 100) { showValidation("Discount must be between 0 and 100."); return; }
                appliedDiscountPct = pct;
                refreshSalePanel();
                clearValidation();
            } catch (NumberFormatException ex) {
                showValidation("Please enter a valid number.");
            }
        });
    }

    @FXML
    private void onNoteClicked() {
        showInfoAlert("Note", "Add Sale Note",
                "Note functionality will connect to the backend sale record.\nNot implemented in this prototype.");
    }

    @FXML
    private void onParkClicked() {
        showInfoAlert("Park Sale", "Park Current Sale",
                "Parked sales will be saved to the backend and retrievable from 'Parked bills'.\nNot implemented in this prototype.");
    }

    @FXML
    private void onVoidClicked() {
        if (cart.isEmpty()) { showValidation("Nothing to void."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Void Sale");
        confirm.setHeaderText("Void this sale?");
        confirm.setContentText("All items will be cleared. This cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                cart.clear();
                appliedDiscountPct = 0;
                refreshSalePanel();
                clearValidation();
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // FXML actions — Pay
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void onPayClicked() {
        if (cart.isEmpty()) {
            showValidation("Add at least one item before payment.");
            return;
        }
        clearValidation();
        showPaymentDialog();
    }

    private void showPaymentDialog() {
        double sub  = checkout.subtotal(cart);
        double disc = checkout.discountAmount(sub, appliedDiscountPct);
        double vat  = checkout.taxAmount(sub, disc);
        double tot  = checkout.total(sub, disc, vat);

        try {
            URL fxml = getClass().getResource("/com/tripos/pos/view/PaymentView.fxml");
            if (fxml == null) { showValidation("PaymentView.fxml not found."); return; }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxml);
            javafx.scene.Parent root = loader.load();

            PaymentController pc = loader.getController();
            // Pass cart snapshot, total, and a callback to reset the sale
            pc.setup(List.copyOf(cart), tot, () -> {
                cart.clear();
                appliedDiscountPct = 0;
                javafx.application.Platform.runLater(this::refreshSalePanel);
            });
            pc.setCustomerName("Kasun Wickramasinghe – Test");  // prototype customer

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            URL css = getClass().getResource("/css/pos-theme.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            javafx.stage.Stage payStage = new javafx.stage.Stage();
            payStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            payStage.initOwner(payButton.getScene().getWindow());
            payStage.setTitle("TriPOS — Take Payment  [PROTOTYPE]");
            payStage.setScene(scene);
            payStage.setMinWidth(820);
            payStage.setMinHeight(560);
            payStage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showValidation("Could not open payment screen: " + ex.getMessage());
        }
    }

    private void showReceiptAlert(com.tripos.pos.model.SaleReceipt receipt, double total) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sale Complete — PROTOTYPE");
        alert.setHeaderText("✅  Payment Accepted (Simulated)\nRef: " + receipt.getTransactionRef());

        StringBuilder sb = new StringBuilder();
        sb.append("Items:\n");
        for (var ci : receipt.getItems()) {
            sb.append("  • ").append(ci.getName())
              .append("  ×").append(ci.getQuantity())
              .append("  ").append(CurrencyFormatter.format(ci.getLineTotal())).append("\n");
        }
        sb.append("\nSubtotal:  ").append(CurrencyFormatter.format(receipt.getSubtotal()));
        if (receipt.getDiscountAmount() > 0)
            sb.append("\nDiscount:  ").append(CurrencyFormatter.formatDiscount(receipt.getDiscountAmount()));
        sb.append("\nVAT (18%): ").append(CurrencyFormatter.format(receipt.getTaxAmount()));
        sb.append("\nTotal:     ").append(CurrencyFormatter.format(receipt.getTotal()));
        sb.append("\nPayment:   ").append(receipt.getPaymentMethod().getLabel());
        sb.append("\n\n⚠ PROTOTYPE RECEIPT — Not a valid tax invoice.");

        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    private void showValidation(String message) {
        validationLabel.setText("⚠  " + message);
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(150), validationLabel);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void clearValidation() {
        validationLabel.setText("");
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(header); a.setContentText(content);
        a.showAndWait();
    }
}
