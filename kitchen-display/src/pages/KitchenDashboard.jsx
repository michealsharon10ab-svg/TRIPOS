import React, { useState } from 'react';
import KitchenOrderCard from '../components/KitchenOrderCard';

export default function KitchenDashboard() {
  const [orders, setOrders] = useState([]);

  return (
    <div className="kds-dashboard">
      <header>
        <h1>Kitchen Display System (KDS)</h1>
      </header>
      <div className="orders-grid">
        {orders.length === 0 ? <p>No active orders</p> : null}
      </div>
    </div>
  );
}
