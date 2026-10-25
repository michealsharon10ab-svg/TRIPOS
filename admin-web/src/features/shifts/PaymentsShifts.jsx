import React, { useState } from 'react';
import { Printer, Check, Search, Bell, Clock, Building2, ChevronDown } from 'lucide-react';
import '../../styles/shifts.css';

export default function PaymentsShifts() {
  const [notes, setNotes] = useState('No issues');
  const [shiftClosed, setShiftClosed] = useState(false);

  const handleCloseShift = () => {
    alert('Shift closed successfully! Generating shift summary and X/Z Report...');
    setShiftClosed(true);
  };

  return (
    <div className="shifts-page-container">
      {/* Shift Cashier Card */}
      <div className="shift-cashier-card">
        <div className="cashier-left-profile">
          <div className="cashier-avatar-gold">KS</div>
          <div>
            <div className="cashier-info-title">Kavindi Silva · Terminal 2</div>
            <div className="cashier-info-subtitle">
              Shift 8:00 AM – 4:00 PM · 8 h · 46 sales · Gift shop, Colombo City Hotel
            </div>
          </div>
        </div>
        <div className="shift-status-pill">
          <span className="shift-status-dot"></span>
          <span>{shiftClosed ? 'Closed' : 'Closing'}</span>
        </div>
      </div>

      {/* Grid: Sales by tender & Cash drawer */}
      <div className="shift-grid-layout">
        {/* Left: Sales by Tender */}
        <div className="shift-card">
          <div className="shift-card-header">Sales by tender</div>
          <table className="tender-table">
            <thead>
              <tr>
                <th>Tender</th>
                <th>Sales</th>
                <th>Amount</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Cash</td>
                <td>18</td>
                <td>Rs. 42,350.00</td>
              </tr>
              <tr>
                <td>Card / Tap</td>
                <td>17</td>
                <td>Rs. 61,200.00</td>
              </tr>
              <tr>
                <td>LankaQR</td>
                <td>6</td>
                <td>Rs. 9,800.00</td>
              </tr>
              <tr>
                <td>Gift card</td>
                <td>2</td>
                <td>Rs. 2,000.00</td>
              </tr>
              <tr>
                <td>Loyalty points</td>
                <td>3</td>
                <td>Rs. 1,240.00</td>
              </tr>
              <tr className="total-row">
                <td>Total</td>
                <td>46</td>
                <td>Rs. 116,590.00</td>
              </tr>
            </tbody>
          </table>

          <div className="tender-footer-meta">
            <div>
              <span>Refunds: </span>
              <strong>1 · Rs. 2,450.00</strong>
            </div>
            <div>
              <span>Voids: </span>
              <strong>2 · approved by Tharindu</strong>
            </div>
            <div>
              <span>Discounts given: </span>
              <strong>Rs. 3,180.00</strong>
            </div>
          </div>
        </div>

        {/* Right: Cash Drawer */}
        <div className="shift-card">
          <div className="shift-card-header">Cash drawer</div>

          <div className="drawer-line-item">
            <span>Opening float</span>
            <strong>Rs. 10,000.00</strong>
          </div>
          <div className="drawer-line-item">
            <span>Cash sales</span>
            <strong>Rs. 42,350.00</strong>
          </div>
          <div className="drawer-line-item">
            <span>Cash refunds</span>
            <strong style={{ color: '#ef4444' }}>- Rs. 2,450.00</strong>
          </div>

          <div className="drawer-line-item expected">
            <span>Expected cash</span>
            <span>Rs. 49,900.00</span>
          </div>

          <div className="drawer-line-item counted">
            <span>Counted cash</span>
            <span>Rs. 49,900.00</span>
          </div>

          <div className="variance-banner">
            <div className="variance-left">
              <Check size={18} strokeWidth={2.5} />
              <span>Variance</span>
            </div>
            <div className="variance-amount">Rs. 0.00</div>
          </div>

          <div style={{ marginBottom: '0.4rem', fontSize: '0.825rem', color: '#64748b' }}>
            Notes
          </div>
          <input
            type="text"
            className="drawer-notes-input"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
          />

          <div style={{ marginBottom: '0.4rem', fontSize: '0.825rem', color: '#64748b' }}>
            Manager sign-off
          </div>
          <div className="manager-signoff-row">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
              <div className="manager-avatar-mini">TF</div>
              <span style={{ fontSize: '0.85rem', fontWeight: 600, color: '#0f172a' }}>
                Tharindu Fernando
              </span>
            </div>
            <div className="manager-pin-dots">
              <span className="dot"></span>
              <span className="dot"></span>
              <span className="dot"></span>
              <span className="dot"></span>
            </div>
          </div>

          <button
            type="button"
            className="btn-close-shift"
            onClick={handleCloseShift}
          >
            <Printer size={18} />
            <span>Close shift & print report</span>
          </button>
        </div>
      </div>
    </div>
  );
}
