package com.tripos.pos.controller;

import com.tripos.pos.model.CartItem;
import com.tripos.pos.model.PaymentMethod;
import com.tripos.pos.model.SaleReceipt;
import com.tripos.pos.service.CheckoutService;
import com.tripos.pos.util.CurrencyFormatter;

import javafx.animation.RotateTransition;
import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for PaymentView.fxml — B2 Payment screen.
 *
 * Supports:
 *  - Cash: numpad entry, quick amounts, change-due calculation.
 *  - Card/Tap: terminal waiting animation, cancel/manual-entry stubs.
 *  - LankaQR: placeholder panel.
 *  - Split payments: multiple payment entries tracked in "Payments added" panel.
 *
 * PROTOTYPE — no real payment gateway is connected.
 * Replace addCashPayment / card flow with real API calls when backend is ready.
 */
public class PaymentController implements Initializable {

    // ══════════════════════════════════════════════════════════════════════
    // State injected by CheckoutController before the stage is shown
    // ══════════════════════════════════════════════════════════════════════
    private List<CartItem>  cartItems;
    private double          totalDue;
    private Runnable        onSaleCompleted; // callback → resets main checkout

    private final CheckoutService checkout = new CheckoutService();

    // Split-payment tracking
    record PaymentEntry(PaymentMethod method, double amount) {}
    private final List<PaymentEntry> payments = new ArrayList<>();

    // Numpad buffer
    private final StringBuilder numpadBuffer = new StringBuilder("0");
    private PaymentMethod activeMethod = PaymentMethod.CASH;
    private ToggleGroup methodTabGroup;

    // ══════════════════════════════════════════════════════════════════════
    // FXML
    // ══════════════════════════════════════════════════════════════════════
    @FXML private Label  headerCustomerLabel;
    @FXML private Label  headerTotalLabel;

    // Method tabs
    @FXML private HBox   paymentMethodTabsBox;

    // Cash panel
    @FXML private VBox   cashPanel;
    @FXML private Label  cashDueLabel;
    @FXML private Label  cashReceivedDisplay;
    @FXML private HBox   quickAmountsBox;
    @FXML private VBox   changeDueBox;
    @FXML private Label  changeDueLabel;
    @FXML private Button addCashBtn;

    // Card panel
    @FXML private VBox              cardPanel;
    @FXML private Label             cardAmountLabel;
    @FXML private ProgressIndicator cardSpinner;

    // QR panel
    @FXML private VBox qrPanel;

    // Right — payments added
    @FXML private VBox   paymentsListBox;
    @FXML private Label  noPaymentsLabel;
    @FXML private Label  paymentsCountBadge;
    @FXML private Label  summaryTotalLabel;
    @FXML private Label  summaryPaidLabel;
    @FXML private Label  summaryRemainingLabel;
    @FXML private Button completeSaleBtn;

    // ══════════════════════════════════════════════════════════════════════
    // Initialise
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildMethodTabs();
        showCashPanel();
    }

    /** Called by CheckoutController before showing this stage. */
    public void setup(List<CartItem> items, double total, Runnable onComplete) {
        this.cartItems       = List.copyOf(items);
        this.totalDue        = total;
        this.onSaleCompleted = onComplete;

        headerTotalLabel.setText(CurrencyFormatter.format(total));
        refreshSummary();
        refreshCashDisplay();
        buildQuickAmounts();
        cardAmountLabel.setText(CurrencyFormatter.format(remainingBalance()));
    }

    /** Optional: show customer name in header. */
    public void setCustomerName(String name) {
        headerCustomerLabel.setText(name);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Method tabs
    // ══════════════════════════════════════════════════════════════════════

    private static final List<PaymentMethod> TABS = List.of(
        PaymentMethod.CASH, PaymentMethod.CARD, PaymentMethod.CONTACTLESS);

    private void buildMethodTabs() {
        methodTabGroup = new ToggleGroup();
        boolean first = true;
        for (PaymentMethod m : TABS) {
            ToggleButton btn = new ToggleButton(tabLabel(m));
            btn.getStyleClass().add("pmt-method-tab");
            btn.setToggleGroup(methodTabGroup);
            if (first) { btn.setSelected(true); first = false; }
            PaymentMethod method = m;
            btn.setOnAction(e -> switchMethod(method));
            paymentMethodTabsBox.getChildren().add(btn);
        }
        // Extra stub tabs matching Figma (Gift card, Loyalty, Store credit)
        for (String stub : List.of("Gift card", "Loyalty points", "Store credit")) {
            ToggleButton btn = new ToggleButton(stub);
            btn.getStyleClass().add("pmt-method-tab");
            btn.setToggleGroup(methodTabGroup);
            btn.setOnAction(e -> switchToStubPanel(stub));
            paymentMethodTabsBox.getChildren().add(btn);
        }
    }

    private String tabLabel(PaymentMethod m) {
        return switch (m) {
            case CASH        -> "💵  Cash";
            case CARD        -> "💳  Card / Tap";
            case CONTACTLESS -> "📱  LankaQR";
        };
    }

    private void switchMethod(PaymentMethod m) {
        activeMethod = m;
        cashPanel.setVisible(m == PaymentMethod.CASH);
        cashPanel.setManaged(m == PaymentMethod.CASH);
        cardPanel.setVisible(m == PaymentMethod.CARD);
        cardPanel.setManaged(m == PaymentMethod.CARD);
        qrPanel.setVisible(m == PaymentMethod.CONTACTLESS);
        qrPanel.setManaged(m == PaymentMethod.CONTACTLESS);
        if (m == PaymentMethod.CARD) {
            cardAmountLabel.setText(CurrencyFormatter.format(remainingBalance()));
        }
    }

    private void switchToStubPanel(String label) {
        // Show QR panel as placeholder for gift/loyalty/store-credit
        activeMethod = PaymentMethod.CONTACTLESS;
        cashPanel.setVisible(false); cashPanel.setManaged(false);
        cardPanel.setVisible(false); cardPanel.setManaged(false);
        qrPanel.setVisible(true);   qrPanel.setManaged(true);
    }

    private void showCashPanel() {
        cashPanel.setVisible(true);  cashPanel.setManaged(true);
        cardPanel.setVisible(false); cardPanel.setManaged(false);
        qrPanel.setVisible(false);   qrPanel.setManaged(false);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Numpad
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void onNumpad(javafx.event.ActionEvent e) {
        String digit = ((Button) e.getSource()).getText();
        if (numpadBuffer.toString().equals("0") && !digit.equals(".")) {
            numpadBuffer.setLength(0);
        }
        // Prevent multiple decimal points
        if (digit.equals(".") && numpadBuffer.toString().contains(".")) return;
        // Max 2 decimal places
        if (numpadBuffer.toString().contains(".")) {
            int dotIdx = numpadBuffer.indexOf(".");
            if (numpadBuffer.length() - dotIdx > 2) return;
        }
        numpadBuffer.append(digit);
        refreshCashDisplay();
    }

    @FXML
    private void onNumpadClear() {
        numpadBuffer.setLength(0);
        numpadBuffer.append("0");
        refreshCashDisplay();
    }

    private void refreshCashDisplay() {
        double received = parsedNumpad();
        cashReceivedDisplay.setText(CurrencyFormatter.format(received));

        double remaining = remainingBalance();
        cashDueLabel.setText(CurrencyFormatter.format(remaining));

        double change = received - remaining;
        if (received > 0 && change >= 0) {
            changeDueBox.setVisible(true);
            changeDueBox.setManaged(true);
            changeDueLabel.setText(CurrencyFormatter.format(change));
        } else {
            changeDueBox.setVisible(false);
            changeDueBox.setManaged(false);
        }
        addCashBtn.setDisable(received <= 0);
    }

    private double parsedNumpad() {
        try { return Double.parseDouble(numpadBuffer.toString()); }
        catch (NumberFormatException ex) { return 0; }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Quick amount buttons
    // ══════════════════════════════════════════════════════════════════════

    private void buildQuickAmounts() {
        quickAmountsBox.getChildren().clear();
        double rem = remainingBalance();

        // Exact
        addQuickBtn("Exact  " + CurrencyFormatter.format(rem), rem);

        // Common round amounts above remaining
        for (double amt : List.of(1000.0, 2000.0, 5000.0, 10000.0)) {
            if (amt > rem) { addQuickBtn(CurrencyFormatter.format(amt), amt); break; }
        }
        addQuickBtn(CurrencyFormatter.format(5000), 5000);
        addQuickBtn(CurrencyFormatter.format(10000), 10000);
    }

    private void addQuickBtn(String label, double amount) {
        Button btn = new Button(label);
        btn.getStyleClass().add("quick-amount-btn");
        btn.setOnAction(e -> {
            numpadBuffer.setLength(0);
            numpadBuffer.append(String.format("%.0f", amount));
            refreshCashDisplay();
        });
        quickAmountsBox.getChildren().add(btn);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Add cash payment
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void onAddCashPayment() {
        double received = parsedNumpad();
        double remaining = remainingBalance();
        double applied = Math.min(received, remaining); // never overpay

        payments.add(new PaymentEntry(PaymentMethod.CASH, applied));
        numpadBuffer.setLength(0); numpadBuffer.append("0");
        refreshCashDisplay();
        refreshSummary();
        buildQuickAmounts();

        if (remainingBalance() <= 0.005) {
            completeSaleBtn.setDisable(false);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Card panel actions
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void onCancelCard() {
        // In production: send cancel to payment terminal API
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Card Request Cancelled");
        a.setHeaderText("Card terminal request cancelled");
        a.setContentText("PROTOTYPE — connect to real terminal API to cancel the transaction.");
        a.showAndWait();
    }

    @FXML
    private void onEnterCardManually() {
        // Simulate manual card approval and add to payments
        double amount = remainingBalance();
        payments.add(new PaymentEntry(PaymentMethod.CARD, amount));
        refreshSummary();
        if (remainingBalance() <= 0.005) {
            completeSaleBtn.setDisable(false);
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Card Payment");
        a.setHeaderText("Simulated card payment added");
        a.setContentText("PROTOTYPE — " + CurrencyFormatter.format(amount) +
                " added as card payment.\nConnect to real terminal API in production.");
        a.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Complete sale
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void onCompleteSale() {
        // Determine primary payment method from first payment entry
        PaymentMethod primary = payments.isEmpty()
                ? PaymentMethod.CASH
                : payments.get(0).method();

        SaleReceipt receipt = checkout.completeSale(cartItems, 0, primary);

        // Show receipt summary
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction ref: ").append(receipt.getTransactionRef()).append("\n\n");
        sb.append("Items sold:\n");
        for (CartItem ci : cartItems) {
            sb.append("  • ").append(ci.getName())
              .append("  x").append(ci.getQuantity())
              .append("  ").append(CurrencyFormatter.format(ci.getLineTotal())).append("\n");
        }
        sb.append("\n");
        sb.append("Total:  ").append(CurrencyFormatter.format(totalDue)).append("\n");
        sb.append("\nPayments:\n");
        for (PaymentEntry pe : payments) {
            sb.append("  • ").append(pe.method().getLabel())
              .append("  ").append(CurrencyFormatter.format(pe.amount())).append("\n");
        }
        sb.append("\n⚠  PROTOTYPE RECEIPT — Not a valid tax invoice.");

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sale Complete");
        a.setHeaderText("Sale completed successfully (simulated)");
        a.setContentText(sb.toString());
        a.showAndWait();

        // Close payment window and reset main checkout
        if (onSaleCompleted != null) onSaleCompleted.run();
        closeStage();
    }

    @FXML
    private void onClose() {
        closeStage();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Summary refresh
    // ══════════════════════════════════════════════════════════════════════

    private void refreshSummary() {
        double paid      = payments.stream().mapToDouble(PaymentEntry::amount).sum();
        double remaining = Math.max(0, totalDue - paid);

        summaryTotalLabel.setText(CurrencyFormatter.format(totalDue));
        summaryPaidLabel.setText(CurrencyFormatter.format(paid));
        summaryRemainingLabel.setText(CurrencyFormatter.format(remaining));

        // Rebuild payments list
        paymentsListBox.getChildren().clear();
        if (payments.isEmpty()) {
            paymentsListBox.getChildren().add(noPaymentsLabel);
        } else {
            for (PaymentEntry pe : payments) {
                paymentsListBox.getChildren().add(buildPaymentRow(pe));
            }
        }

        // Badge
        if (!payments.isEmpty()) {
            paymentsCountBadge.setText(String.valueOf(payments.size()));
            paymentsCountBadge.setVisible(true);
            paymentsCountBadge.setManaged(true);
        }

        // Remaining highlight
        summaryRemainingLabel.getStyleClass().removeAll("pmt-remaining-value-zero");
        if (remaining <= 0.005) {
            summaryRemainingLabel.getStyleClass().add("pmt-remaining-value-zero");
            completeSaleBtn.setDisable(false);
        }
    }

    private HBox buildPaymentRow(PaymentEntry pe) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 16, 8, 16));
        row.getStyleClass().add("payment-entry-row");

        Label method = new Label(pe.method().getIcon() + "  " + pe.method().getLabel());
        method.getStyleClass().add("payment-entry-method");
        HBox.setHgrow(method, Priority.ALWAYS);

        Label amount = new Label(CurrencyFormatter.format(pe.amount()));
        amount.getStyleClass().add("payment-entry-amount");

        Button remove = new Button("✕");
        remove.getStyleClass().add("payment-entry-remove");
        remove.setOnAction(e -> {
            payments.remove(pe);
            refreshSummary();
            completeSaleBtn.setDisable(remainingBalance() > 0.005);
        });

        row.getChildren().addAll(method, amount, remove);
        return row;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    private double remainingBalance() {
        double paid = payments.stream().mapToDouble(PaymentEntry::amount).sum();
        return Math.max(0, totalDue - paid);
    }

    private void closeStage() {
        if (cardSpinner != null) {
            // stop any running animation
        }
        Stage stage = (Stage) completeSaleBtn.getScene().getWindow();
        stage.close();
    }
}
