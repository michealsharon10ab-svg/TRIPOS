package com.tripos.pos.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * An immutable record of a completed (simulated) sale.
 *
 * <p>This is a <strong>prototype-only</strong> data structure. In production it
 * would be persisted via the backend API and would contain a real transaction
 * reference from the payment gateway.</p>
 */
public class SaleReceipt {

    private final String          transactionRef;
    private final LocalDateTime   timestamp;
    private final List<CartItem>  items;
    private final double          subtotal;
    private final double          discountAmount;
    private final double          taxAmount;
    private final double          total;
    private final PaymentMethod   paymentMethod;
    /** Human-readable note shown on the receipt. */
    private final String          taxNote;

    public SaleReceipt(String transactionRef,
                       LocalDateTime timestamp,
                       List<CartItem> items,
                       double subtotal,
                       double discountAmount,
                       double taxAmount,
                       double total,
                       PaymentMethod paymentMethod,
                       String taxNote) {
        this.transactionRef  = transactionRef;
        this.timestamp       = timestamp;
        this.items           = List.copyOf(items);
        this.subtotal        = subtotal;
        this.discountAmount  = discountAmount;
        this.taxAmount       = taxAmount;
        this.total           = total;
        this.paymentMethod   = paymentMethod;
        this.taxNote         = taxNote;
    }

    public String          getTransactionRef()  { return transactionRef; }
    public LocalDateTime   getTimestamp()       { return timestamp; }
    public List<CartItem>  getItems()           { return items; }
    public double          getSubtotal()        { return subtotal; }
    public double          getDiscountAmount()  { return discountAmount; }
    public double          getTaxAmount()       { return taxAmount; }
    public double          getTotal()           { return total; }
    public PaymentMethod   getPaymentMethod()   { return paymentMethod; }
    public String          getTaxNote()         { return taxNote; }
}
