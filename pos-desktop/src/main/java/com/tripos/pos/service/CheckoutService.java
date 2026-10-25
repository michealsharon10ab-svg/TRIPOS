package com.tripos.pos.service;

import com.tripos.pos.model.CartItem;
import com.tripos.pos.model.PaymentMethod;
import com.tripos.pos.model.SaleReceipt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Calculates bill totals and processes a simulated checkout.
 *
 * <h3>Tax note</h3>
 * <p>The 15 % VAT rate used here is a <strong>sample / placeholder</strong>
 * value included solely to demonstrate the bill-summary UI layout.
 * Real tax logic (rates, exemptions, multi-tier bands) must be implemented
 * based on the applicable jurisdiction and integrated with the backend
 * before going live.</p>
 *
 * <h3>Payment note</h3>
 * <p>The {@link #completeSale} method <strong>simulates</strong> a payment
 * locally. No real payment gateway is called and no funds are moved.
 * This is a UI prototype only.</p>
 */
public class CheckoutService {

    // -----------------------------------------------------------------------
    // Sample tax configuration  –  MUST be replaced with real tax rules
    // -----------------------------------------------------------------------
    /**
     * Sample VAT rate (18 %). This matches the "VAT 18% included" line shown
     * in the TriPOS Figma design reference. This is a placeholder for the
     * prototype. Replace with jurisdiction-specific rates from backend
     * configuration before going live.
     */
    public static final double SAMPLE_TAX_RATE = 0.18;
    public static final String TAX_LABEL       = "VAT (18% – sample rate)";

    // -----------------------------------------------------------------------
    // Calculation helpers
    // -----------------------------------------------------------------------

    /** Computes the subtotal (sum of all line totals) for the given cart. */
    public double subtotal(List<CartItem> cart) {
        return cart.stream().mapToDouble(CartItem::getLineTotal).sum();
    }

    /**
     * Applies the cashier-entered discount percentage to the subtotal.
     *
     * @param subtotal       pre-discount subtotal
     * @param discountPct    discount percentage 0-100
     * @return               discount amount (not the final price)
     */
    public double discountAmount(double subtotal, double discountPct) {
        return subtotal * (discountPct / 100.0);
    }

    /**
     * Computes the sample tax amount.
     * Tax is applied to the post-discount amount.
     */
    public double taxAmount(double subtotal, double discountAmount) {
        return (subtotal - discountAmount) * SAMPLE_TAX_RATE;
    }

    /** Final total = subtotal − discount + tax. */
    public double total(double subtotal, double discountAmount, double taxAmount) {
        return subtotal - discountAmount + taxAmount;
    }

    // -----------------------------------------------------------------------
    // Simulated sale completion  –  replace internals with real API call
    // -----------------------------------------------------------------------

    /**
     * Simulates completing a sale.
     *
     * <p><strong>PROTOTYPE ONLY</strong> – no real payment gateway is invoked.
     * The transaction reference is randomly generated locally. When backend
     * integration is added, replace this method body with an API call such as
     * {@code POST /api/sales} and use the real transaction reference returned
     * by the payment gateway.</p>
     *
     * @param cart          items in the cart
     * @param discountPct   discount percentage entered by cashier
     * @param method        chosen payment method
     * @return              a {@link SaleReceipt} for display
     */
    public SaleReceipt completeSale(List<CartItem> cart,
                                    double discountPct,
                                    PaymentMethod method) {
        double sub      = subtotal(cart);
        double disc     = discountAmount(sub, discountPct);
        double tax      = taxAmount(sub, disc);
        double fin      = total(sub, disc, tax);
        String ref      = "DEMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new SaleReceipt(
                ref,
                LocalDateTime.now(),
                cart,
                sub,
                disc,
                tax,
                fin,
                method,
                TAX_LABEL
        );
    }
}
