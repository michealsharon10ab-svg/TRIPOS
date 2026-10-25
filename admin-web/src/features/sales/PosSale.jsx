import React, { useState, useMemo } from 'react';
import {
  Search,
  Scan,
  Box,
  Plus,
  Minus,
  X,
  Tag,
  FileText,
  PauseCircle,
  Ban,
  CheckCircle2,
  Check,
  Printer,
  CreditCard,
  Banknote,
  QrCode,
  Gift,
  Star,
  Wallet,
  Building,
  Monitor,
  User,
  Clock,
  Wifi,
  MoreHorizontal
} from 'lucide-react';
import '../../styles/pos.css';

// 15 Standard Products from TriPOS Catalog
const INITIAL_PRODUCTS = [
  { id: 1, name: 'Ceylon Black Tea 200 g', price: 1250, category: 'Tea & Coffee', stockStatus: 'in-stock', unit: 'Pack' },
  { id: 2, name: 'Ceylon Green Tea 100 g', price: 1100, category: 'Tea & Coffee', stockStatus: 'in-stock', unit: 'Pack' },
  { id: 3, name: 'Cinnamon Sticks 100 g', price: 850, category: 'Groceries', stockStatus: 'in-stock', unit: 'Pack' },
  { id: 4, name: 'Coconut Oil 1 L', price: 980, category: 'Groceries', stockStatus: 'in-stock', unit: 'Bottle' },
  { id: 5, name: 'Kithul Jaggery 500 g', price: 650, category: 'Groceries', stockStatus: 'in-stock', unit: 'Pack' },
  { id: 6, name: 'King Coconut Water', price: 350, category: 'Groceries', stockStatus: 'low-stock', stockCount: 3, unit: 'Bottle' },
  { id: 7, name: 'Spice Gift Box', price: 4500, category: 'Gifts', stockStatus: 'in-stock', unit: 'Box' },
  { id: 8, name: 'Batik T-shirt', price: 2450, category: 'Clothing', stockStatus: 'in-stock', discountPercent: 10, unit: 'M · Black' },
  { id: 9, name: 'Handloom Sarong', price: 3800, category: 'Clothing', stockStatus: 'in-stock', unit: 'Each' },
  { id: 10, name: 'Ayurvedic Balm 50 g', price: 720, category: 'Groceries', stockStatus: 'out-stock', unit: 'Jar' },
  { id: 11, name: 'Wooden Elephant Souvenir', price: 3200, category: 'Gifts', stockStatus: 'low-stock', stockCount: 2, unit: 'Each' },
  { id: 12, name: 'Mask Fridge Magnet', price: 495, category: 'Gifts', stockStatus: 'in-stock', unit: 'Each' },
  { id: 13, name: 'Coconut Shell Bowl', price: 1500, category: 'Gifts', stockStatus: 'in-stock', unit: 'Each' },
  { id: 14, name: 'Beeralu Lace Coasters', price: 1200, category: 'Gifts', stockStatus: 'in-stock', unit: 'Set' },
  { id: 15, name: 'Bluetooth Speaker', price: 14900, category: 'Electronics', stockStatus: 'in-stock', unit: 'Box' }
];

// Initial Cart matching the user's screenshot
const INITIAL_CART = [
  { productId: 1, name: 'Ceylon Black Tea 200 g', unit: 'Pack', qty: 2, price: 1250, discount: 0 },
  { productId: 8, name: 'Batik T-shirt', unit: 'M · Black', qty: 1, price: 2450, discount: 245, discountPercent: 10 },
  { productId: 4, name: 'Coconut Oil 1 L', unit: 'Bottle', qty: 1, price: 980, discount: 0 },
  { productId: 5, name: 'Kithul Jaggery 500 g', unit: 'Pack', qty: 1, price: 650, discount: 0 },
  { productId: 12, name: 'Mask Fridge Magnet', unit: 'Each', qty: 1, price: 495, discount: 0 }
];

export default function PosSale() {
  const [cart, setCart] = useState(INITIAL_CART);
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [parkedCount, setParkedCount] = useState(2);
  const [customer, setCustomer] = useState({
    name: 'Kasun Wickramasinghe',
    phone: '077 123 4567',
    tier: 'Gold',
    points: '1,240 pts',
    initials: 'KW'
  });

  // Checkout Modal State
  const [showCheckout, setShowCheckout] = useState(true); // Open by default or when Pay is clicked!
  const [selectedTender, setSelectedTender] = useState('Card / Tap');
  const [chargeAmount, setChargeAmount] = useState('3,000.00');
  const [paymentsAdded, setPaymentsAdded] = useState([]);
  const [saleCompleted, setSaleCompleted] = useState(false);

  const categories = ['All', 'Tea & Coffee', 'Groceries', 'Clothing', 'Electronics', 'Gifts'];

  // Filter products by category and search
  const filteredProducts = useMemo(() => {
    return INITIAL_PRODUCTS.filter(item => {
      const matchCat = selectedCategory === 'All' || item.category === selectedCategory;
      const matchSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase());
      return matchCat && matchSearch;
    });
  }, [selectedCategory, searchQuery]);

  // Set of product IDs currently in cart
  const cartProductIds = useMemo(() => {
    return new Set(cart.map(i => i.productId));
  }, [cart]);

  // Total count of items in cart
  const totalItemCount = useMemo(() => {
    return cart.reduce((sum, item) => sum + item.qty, 0);
  }, [cart]);

  // Calculations
  const subtotal = useMemo(() => {
    return cart.reduce((sum, item) => sum + (item.price * item.qty), 0);
  }, [cart]);

  const totalDiscount = useMemo(() => {
    return cart.reduce((sum, item) => sum + (item.discount || 0), 0);
  }, [cart]);

  const total = subtotal - totalDiscount;
  const vatAmount = (total * 0.18) / 1.18; // 18% included

  // Add Product to Cart
  const handleProductClick = (product) => {
    if (product.stockStatus === 'out-stock') {
      alert('This item is currently out of stock!');
      return;
    }

    setCart(prev => {
      const existing = prev.find(i => i.productId === product.id);
      if (existing) {
        return prev.map(i => {
          if (i.productId === product.id) {
            const nextQty = i.qty + 1;
            const itemDiscount = product.discountPercent ? (product.price * nextQty * (product.discountPercent / 100)) : 0;
            return { ...i, qty: nextQty, discount: itemDiscount };
          }
          return i;
        });
      } else {
        const itemDiscount = product.discountPercent ? (product.price * (product.discountPercent / 100)) : 0;
        return [
          ...prev,
          {
            productId: product.id,
            name: product.name,
            unit: product.unit,
            qty: 1,
            price: product.price,
            discount: itemDiscount,
            discountPercent: product.discountPercent || 0
          }
        ];
      }
    });
  };

  // Change Quantity
  const handleQtyChange = (productId, delta) => {
    setCart(prev => {
      return prev
        .map(item => {
          if (item.productId === productId) {
            const newQty = item.qty + delta;
            if (newQty <= 0) return null;
            const discountPerUnit = item.discountPercent ? (item.price * (item.discountPercent / 100)) : 0;
            return { ...item, qty: newQty, discount: discountPerUnit * newQty };
          }
          return item;
        })
        .filter(Boolean);
    });
  };

  // Remove Item
  const handleRemoveItem = (productId) => {
    setCart(prev => prev.filter(i => i.productId !== productId));
  };

  // Void Cart
  const handleVoidSale = () => {
    if (cart.length === 0) return;
    if (window.confirm('Are you sure you want to VOID and clear this sale?')) {
      setCart([]);
    }
  };

  // Park Bill
  const handleParkBill = () => {
    if (cart.length === 0) return;
    setParkedCount(prev => prev + 1);
    setCart([]);
    alert('Bill parked successfully! Added to Parked Bills queue.');
  };

  // Add Note
  const handleAddNote = () => {
    const note = prompt('Enter special instructions or order note:', 'Gift wrapping requested');
    if (note) alert(`Note added: "${note}"`);
  };

  const totalPaid = useMemo(() => {
    return paymentsAdded.reduce((sum, p) => sum + p.amount, 0);
  }, [paymentsAdded]);

  const remainingBalance = Math.max(0, total - totalPaid);

  const handleAddPayment = () => {
    const cleanNum = parseFloat(chargeAmount.toString().replace(/,/g, '')) || remainingBalance;
    const amountToAdd = Math.min(cleanNum, remainingBalance);
    if (amountToAdd <= 0) return;

    const newPayment = {
      id: Date.now(),
      method: selectedTender,
      amount: amountToAdd
    };

    setPaymentsAdded(prev => [...prev, newPayment]);
    const nextRemaining = Math.max(0, remainingBalance - amountToAdd);
    setChargeAmount(nextRemaining.toLocaleString('en-US', { minimumFractionDigits: 2 }));
  };

  const handleRemovePayment = (id) => {
    setPaymentsAdded(prev => prev.filter(p => p.id !== id));
  };

  // Complete Payment
  const handleFinishCheckout = () => {
    if (remainingBalance > 0) return;
    setSaleCompleted(true);
    setTimeout(() => {
      alert(`Sale completed successfully! Total Rs. ${total.toLocaleString()}.00 received.\nReceipt INV-CCH-000245 printed.`);
      setShowCheckout(false);
      setSaleCompleted(false);
      setPaymentsAdded([]);
      setCart([]);
    }, 600);
  };

  return (
    <div className="pos-sale-wrapper">
      {/* Top Black Header Bar Matching Screenshot */}
      <div className="pos-top-bar">
        <div className="pos-top-left">
          <div className="pos-brand-tag">
            <div className="pos-brand-quad">
              <span className="dot"></span>
              <span className="dot"></span>
              <span className="dot"></span>
              <span className="dot"></span>
            </div>
            <span className="pos-brand-title">TriPOS</span>
          </div>

          <div className="pos-meta-item">
            <span>Colombo City Hotel · Gift shop</span>
          </div>
          <div className="pos-meta-item">
            <span>Terminal 2</span>
          </div>
          <div className="pos-meta-item">
            <span>Kavindi Silva · Cashier</span>
          </div>
          <div className="pos-meta-item">
            <Clock size={13} color="#f5b700" />
            <span>Shift open · 8:00 AM</span>
          </div>
        </div>

        <div className="pos-top-right">
          <div className="pos-status-online">
            <Wifi size={13} />
            <span>Online</span>
          </div>

          <div className="pos-parked-bills" onClick={() => alert(`${parkedCount} bills currently parked in queue.`)}>
            <span>Parked bills</span>
            <span className="parked-badge-count">{parkedCount}</span>
          </div>

          <button className="pos-menu-dots-btn" title="Options">
            <MoreHorizontal size={16} />
          </button>
        </div>
      </div>

      {/* Main Split Grid: Catalog on Left | Current Sale on Right */}
      <div className="pos-body-grid">
        {/* Left Section: Catalog */}
        <div className="pos-catalog-panel">
          {/* Search Bar */}
          <div className="pos-search-wrapper">
            <Search className="pos-search-icon" size={18} />
            <input
              type="text"
              placeholder="Scan barcode or search items"
              className="pos-search-input"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <span className="pos-search-f2-badge">F2</span>
            <button
              className="pos-scanner-btn"
              title="Barcode Scanner Mode"
              onClick={() => alert('Barcode scanner hardware listener active (Ready to scan).')}
            >
              <Scan size={16} />
            </button>
          </div>

          {/* Category Tabs */}
          <div className="pos-category-tabs">
            {categories.map((cat) => (
              <button
                key={cat}
                type="button"
                className={`category-pill ${selectedCategory === cat ? 'active' : ''}`}
                onClick={() => setSelectedCategory(cat)}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Products Grid (5-column layout) */}
          <div className="pos-products-grid">
            {filteredProducts.map((prod) => {
              const isSelected = cartProductIds.has(prod.id);
              return (
                <div
                  key={prod.id}
                  className={`pos-product-card ${isSelected ? 'selected' : ''} ${prod.stockStatus === 'out-stock' ? 'out-of-stock' : ''}`}
                  onClick={() => handleProductClick(prod)}
                >
                  <div>
                    <div className="pos-product-img-box">
                      <Box size={24} strokeWidth={1.5} />
                    </div>
                    <div className="pos-product-title">{prod.name}</div>
                    <div className="pos-product-price">Rs. {prod.price.toLocaleString()}.00</div>
                  </div>

                  <div>
                    {prod.stockStatus === 'in-stock' && (
                      <span className="stock-pill in-stock">In stock</span>
                    )}
                    {prod.stockStatus === 'low-stock' && (
                      <span className="stock-pill low-stock">Low: {prod.stockCount}</span>
                    )}
                    {prod.stockStatus === 'out-stock' && (
                      <span className="stock-pill out-stock">Out of stock</span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          {/* Keyboard Shortcuts Footer */}
          <div className="pos-shortcuts-bar">
            <span>F2 Search</span>
            <span>·</span>
            <span>F4 Customer</span>
            <span>·</span>
            <span>F8 Pay</span>
            <span>·</span>
            <span>F9 Park</span>
            <span>·</span>
            <span>Esc Clear</span>
          </div>
        </div>

        {/* Right Section: Current Sale (Cart & Checkout) */}
        <div className="pos-cart-panel">
          <div className="cart-header">
            <h2 className="cart-title">Current sale</h2>
            <span className="cart-ref-badge">INV-CCH-000245 · {totalItemCount} items</span>
          </div>

          {/* Customer Card */}
          {customer && (
            <div className="pos-customer-card">
              <div className="customer-left">
                <div className="customer-avatar-gold">{customer.initials}</div>
                <div>
                  <div className="customer-name">{customer.name}</div>
                  <div className="customer-phone">{customer.phone}</div>
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <span className="customer-loyalty-pill">{customer.tier} · {customer.points}</span>
                <button
                  className="customer-remove-btn"
                  title="Remove customer"
                  onClick={() => setCustomer(null)}
                >
                  <X size={15} />
                </button>
              </div>
            </div>
          )}

          {/* Cart Items List */}
          <div className="cart-items-scroll">
            {cart.length === 0 ? (
              <div style={{ textAlign: 'center', color: '#94a3b8', padding: '3rem 1rem', fontSize: '0.9rem' }}>
                Cart is empty. Click any item on the left or scan barcode to add.
              </div>
            ) : (
              cart.map((item) => {
                const lineTotal = (item.price * item.qty) - (item.discount || 0);
                return (
                  <div key={item.productId} className="cart-item-row">
                    <div className="cart-item-top">
                      <span className="cart-item-name">{item.name}</span>
                      <button
                        className="cart-item-remove-btn"
                        onClick={() => handleRemoveItem(item.productId)}
                        title="Remove"
                      >
                        <X size={14} />
                      </button>
                    </div>

                    <div className="cart-item-bottom">
                      <div className="cart-item-meta">
                        <span>{item.unit}</span>
                        {item.discountPercent > 0 && (
                          <span className="discount-badge-yellow">−{item.discountPercent}%</span>
                        )}
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                        <div className="cart-qty-controls">
                          <button
                            type="button"
                            className="qty-btn"
                            onClick={() => handleQtyChange(item.productId, -1)}
                          >
                            −
                          </button>
                          <span className="qty-count">{item.qty}</span>
                          <button
                            type="button"
                            className="qty-btn"
                            onClick={() => handleQtyChange(item.productId, 1)}
                          >
                            +
                          </button>
                        </div>

                        <span className="cart-unit-price">@ Rs. {item.price.toLocaleString()}.00</span>
                        <span className="cart-item-total">Rs. {lineTotal.toLocaleString()}.00</span>
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Financial Summary */}
          <div className="cart-summary-section">
            <div className="summary-row">
              <span>Subtotal</span>
              <span>Rs. {subtotal.toLocaleString()}.00</span>
            </div>

            {totalDiscount > 0 && (
              <div className="summary-row discount-row">
                <span>Discount</span>
                <span>− Rs. {totalDiscount.toLocaleString()}.00</span>
              </div>
            )}

            <div className="summary-row vat-row">
              <span>VAT 18% (included)</span>
              <span>Rs. {vatAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
            </div>

            <div className="cart-grand-total">
              <span className="total-label">Total</span>
              <span className="total-amount-display">Rs. {total.toLocaleString()}.00</span>
            </div>

            {/* Actions: Discount, Note, Park, Void */}
            <div className="cart-actions-row">
              <button
                type="button"
                className="cart-action-btn"
                onClick={() => {
                  const disc = prompt('Enter discount percentage (e.g. 5 for 5%):', '5');
                  if (disc) alert(`Applied ${disc}% discount across the cart.`);
                }}
              >
                <Tag size={14} />
                <span>Discount</span>
              </button>

              <button type="button" className="cart-action-btn" onClick={handleAddNote}>
                <FileText size={14} />
                <span>Note</span>
              </button>

              <button type="button" className="cart-action-btn" onClick={handleParkBill}>
                <PauseCircle size={14} />
                <span>Park</span>
              </button>

              <button type="button" className="cart-action-btn void-btn" onClick={handleVoidSale}>
                <Ban size={14} />
                <span>Void</span>
              </button>
            </div>

            {/* Big Yellow Pay Button */}
            <button
              type="button"
              className="btn-pay-now"
              onClick={() => {
                if (cart.length === 0) {
                  alert('Please add items to cart before checkout.');
                  return;
                }
                setShowCheckout(true);
              }}
            >
              <span>Pay Rs. {total.toLocaleString()}.00 · F8</span>
            </button>
          </div>
        </div>
      </div>

      {/* Take Payment Modal Dialog (Matching Screenshot) */}
      {showCheckout && (
        <div className="payment-dialog-backdrop" onClick={() => setShowCheckout(false)}>
          <div className="payment-dialog-card" onClick={(e) => e.stopPropagation()}>
            
            {/* Header */}
            <div className="payment-dialog-header">
              <div>
                <h2 className="payment-dialog-title">Take payment</h2>
                <div className="payment-dialog-sub">
                  INV-CCH-000245 · Kasun Wickramasinghe · Gold
                </div>
              </div>

              <div className="payment-header-right">
                <div className="payment-total-due-block">
                  <div className="total-due-lbl">Total due</div>
                  <div className="total-due-num">Rs. {total.toLocaleString()}.00</div>
                </div>

                <button
                  type="button"
                  className="payment-close-btn"
                  onClick={() => setShowCheckout(false)}
                  title="Close payment dialog"
                >
                  <X size={20} />
                </button>
              </div>
            </div>

            {/* 2-Column Body Grid */}
            <div className="payment-body-grid">
              
              {/* Left Column: Tenders & Reader Box */}
              <div>
                {/* 6 Tenders Selection Row */}
                <div className="tenders-nav-row">
                  <button
                    type="button"
                    className={`tender-tab-btn ${selectedTender === 'Cash' ? 'active' : ''}`}
                    onClick={() => setSelectedTender('Cash')}
                  >
                    <Banknote size={20} strokeWidth={selectedTender === 'Cash' ? 2.5 : 2} />
                    <span>Cash</span>
                  </button>

                  <button
                    type="button"
                    className={`tender-tab-btn ${selectedTender === 'Card / Tap' ? 'active' : ''}`}
                    onClick={() => setSelectedTender('Card / Tap')}
                  >
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={selectedTender === 'Card / Tap' ? 2.8 : 2} strokeLinecap="round">
                      <path d="M8.5 16.5a5 5 0 0 1 0-9" />
                      <path d="M12 19a8.5 8.5 0 0 1 0-14" />
                      <path d="M15.5 21.5a12 12 0 0 1 0-19" />
                    </svg>
                    <span>Card / Tap</span>
                  </button>

                  <button
                    type="button"
                    className={`tender-tab-btn ${selectedTender === 'LankaQR' ? 'active' : ''}`}
                    onClick={() => setSelectedTender('LankaQR')}
                  >
                    <QrCode size={20} strokeWidth={selectedTender === 'LankaQR' ? 2.5 : 2} />
                    <span>LankaQR</span>
                  </button>

                  <button
                    type="button"
                    className={`tender-tab-btn ${selectedTender === 'Gift card' ? 'active' : ''}`}
                    onClick={() => setSelectedTender('Gift card')}
                  >
                    <Gift size={20} strokeWidth={selectedTender === 'Gift card' ? 2.5 : 2} />
                    <span>Gift card</span>
                  </button>

                  <button
                    type="button"
                    className={`tender-tab-btn ${selectedTender === 'Loyalty points' ? 'active' : ''}`}
                    onClick={() => setSelectedTender('Loyalty points')}
                  >
                    <Star size={20} strokeWidth={selectedTender === 'Loyalty points' ? 2.5 : 2} />
                    <span>Loyalty points</span>
                  </button>

                  <button
                    type="button"
                    className={`tender-tab-btn ${selectedTender === 'Store credit' ? 'active' : ''}`}
                    onClick={() => setSelectedTender('Store credit')}
                  >
                    <Wallet size={20} strokeWidth={selectedTender === 'Store credit' ? 2.5 : 2} />
                    <span>Store credit</span>
                  </button>
                </div>

                {/* Amount to charge input box */}
                <div className="charge-amount-section">
                  <label className="charge-label">
                    {selectedTender === 'Cash' ? 'Amount to pay in cash' : `Amount to charge on ${selectedTender.toLowerCase()}`}
                  </label>
                  <div className="charge-input-box">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                      <span style={{ fontSize: '1.35rem', fontWeight: 800, color: '#0c0e14' }}>Rs.</span>
                      <input
                        type="text"
                        className="charge-input"
                        value={chargeAmount}
                        onChange={(e) => setChargeAmount(e.target.value)}
                      />
                    </div>
                    <span className="charge-suffix">of Rs. {total.toLocaleString()}.00</span>
                  </div>
                </div>

                {/* Waiting for Card Terminal Box */}
                <div className="terminal-waiting-card">
                  <div className="terminal-contactless-circle">
                    <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="#f5b700" strokeWidth="2.8" strokeLinecap="round">
                      <path d="M8.5 16.5a5 5 0 0 1 0-9" />
                      <path d="M12 19a8.5 8.5 0 0 1 0-14" />
                      <path d="M15.5 21.5a12 12 0 0 1 0-19" />
                    </svg>
                  </div>

                  <div className="terminal-waiting-title">Waiting for card terminal</div>
                  <div className="terminal-waiting-sub">
                    Ask the customer to tap, insert or swipe on Terminal 2 · EDC
                  </div>

                  <div className="terminal-pill-tags">
                    <span className="terminal-tag-pill">
                      <span className="dot"></span>
                      <span>Contactless card</span>
                    </span>
                    <span className="terminal-tag-pill">
                      <span className="dot"></span>
                      <span>Apple Pay</span>
                    </span>
                    <span className="terminal-tag-pill">
                      <span className="dot"></span>
                      <span>Google Pay</span>
                    </span>
                    <span className="terminal-tag-pill">
                      <span className="dot"></span>
                      <span>Chip & PIN</span>
                    </span>
                  </div>
                </div>

                {/* Actions: Cancel request & Enter card manually */}
                <div className="terminal-action-buttons">
                  <button
                    type="button"
                    className="btn-cancel-req"
                    onClick={() => setShowCheckout(false)}
                  >
                    Cancel request
                  </button>

                  <button
                    type="button"
                    className="btn-manual-card"
                    onClick={handleAddPayment}
                  >
                    Enter card manually
                  </button>
                </div>
              </div>

              {/* Right Column: Payments added & Balance */}
              <div className="payments-added-box">
                <div>
                  <div className="payments-added-title">Payments added</div>

                  {paymentsAdded.length === 0 ? (
                    <div className="no-payments-placeholder">
                      <CreditCard size={32} strokeWidth={1.5} color="#94a3b8" />
                      <div style={{ fontWeight: 700, color: '#475569', marginTop: '0.65rem' }}>
                        No payments yet
                      </div>
                      <p>Split across tenders: add a card amount, then cash</p>
                    </div>
                  ) : (
                    <div style={{ marginBottom: '1.25rem' }}>
                      {paymentsAdded.map((p) => (
                        <div key={p.id} className="payment-item-card">
                          <div>
                            <div style={{ fontWeight: 700, color: '#0f172a' }}>{p.method}</div>
                            <div style={{ fontSize: '0.725rem', color: '#64748b' }}>Approved EDC Authorization</div>
                          </div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                            <strong>Rs. {p.amount.toLocaleString()}.00</strong>
                            <button
                              type="button"
                              onClick={() => handleRemovePayment(p.id)}
                              style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer' }}
                            >
                              <X size={15} />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Totals & Complete Sale */}
                <div className="balance-summary-block">
                  <div className="balance-row">
                    <span>Total</span>
                    <strong>Rs. {total.toLocaleString()}.00</strong>
                  </div>
                  <div className="balance-row">
                    <span>Paid</span>
                    <strong>Rs. {totalPaid.toLocaleString()}.00</strong>
                  </div>

                  <div className="remaining-row">
                    <span>Remaining balance</span>
                    <span className="remaining-val">Rs. {remainingBalance.toLocaleString()}.00</span>
                  </div>

                  <button
                    type="button"
                    className={`btn-complete-sale ${remainingBalance === 0 ? 'active' : 'disabled'}`}
                    disabled={remainingBalance > 0 || saleCompleted}
                    onClick={handleFinishCheckout}
                  >
                    <Check size={18} strokeWidth={2.5} />
                    <span>{saleCompleted ? 'Printing Receipt...' : 'Complete sale'}</span>
                  </button>

                  <div className="complete-sale-hint">
                    {remainingBalance === 0
                      ? '✓ Full amount received. Ready to complete sale.'
                      : 'Complete sale unlocks when the balance is Rs. 0.00'}
                  </div>
                </div>

              </div>

            </div>

          </div>
        </div>
      )}
    </div>
  );
}
