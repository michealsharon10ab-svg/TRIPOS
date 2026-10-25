import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import AdminLayout from '../layouts/AdminLayout';
import Login from '../features/auth/Login';
import Signup from '../features/auth/Signup';
import Dashboard from '../features/dashboard/Dashboard';
import UserManagement from '../features/users/UserManagement';
import BranchManagement from '../features/branches/BranchManagement';
import SalesOverview from '../features/sales/SalesOverview';
import InventoryList from '../features/inventory/InventoryList';
import TableLayout from '../features/restaurant/TableLayout';
import RoomBooking from '../features/hotel/RoomBooking';
import ReportsView from '../features/reports/ReportsView';
import PaymentsShifts from '../features/shifts/PaymentsShifts';

function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>
        Verifying secure session...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

function PublicRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>
        Loading...
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={
        <PublicRoute>
          <Login />
        </PublicRoute>
      } />

      <Route path="/signup" element={
        <PublicRoute>
          <Signup />
        </PublicRoute>
      } />

      <Route path="/register" element={
        <PublicRoute>
          <Signup />
        </PublicRoute>
      } />

      <Route path="/" element={
        <ProtectedRoute>
          <AdminLayout />
        </ProtectedRoute>
      }>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="users" element={<UserManagement />} />
        <Route path="branches" element={<BranchManagement />} />
        <Route path="sales" element={<SalesOverview />} />
        <Route path="pos" element={<SalesOverview />} />
        <Route path="inventory" element={<InventoryList />} />
        <Route path="restaurant" element={<TableLayout />} />
        <Route path="kitchen" element={<TableLayout />} />
        <Route path="hotel" element={<RoomBooking />} />
        <Route path="shifts" element={<PaymentsShifts />} />
        <Route path="payments" element={<PaymentsShifts />} />
        <Route path="reports" element={<ReportsView />} />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
