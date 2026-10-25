import React from 'react';

export default function KitchenOrderCard({ order }) {
  return (
    <div className="kds-order-card">
      <h3>Order #{order?.id || '---'}</h3>
      <p>Status: {order?.status || 'PENDING'}</p>
    </div>
  );
}
