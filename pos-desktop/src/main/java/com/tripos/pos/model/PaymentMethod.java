package com.tripos.pos.model;

/**
 * Supported payment methods for the checkout prototype.
 *
 * <p>In production this enum (or a backend-supplied list) would drive which
 * payment terminals / gateways are invoked. For the prototype, payment is
 * simulated only – no real transaction occurs.</p>
 */
public enum PaymentMethod {
    CASH("Cash", "💵"),
    CARD("Card", "💳"),
    CONTACTLESS("Contactless", "📡");

    private final String label;
    private final String icon;

    PaymentMethod(String label, String icon) {
        this.label = label;
        this.icon  = icon;
    }

    public String getLabel() { return label; }
    public String getIcon()  { return icon;  }

    @Override
    public String toString() { return icon + "  " + label; }
}
