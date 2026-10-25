import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutGrid,
  ShoppingCart,
  UtensilsCrossed,
  Tv,
  BedDouble,
  Package,
  Users,
  CreditCard,
  BarChart2,
  Settings,
  ChevronDown,
  Bed,
  Store,
  Utensils,
  PanelLeftClose,
  LogOut
} from 'lucide-react';
import '../styles/sidebar.css';

export default function Sidebar() {
  const { user, activeBranch, switchBranch, logout } = useAuth();
  const [branchDropdownOpen, setBranchDropdownOpen] = useState(false);

  // Available user branches
  const branches = user?.branches ? Array.from(user.branches) : [];

  // Active branch display data
  const currentBranchName = activeBranch?.name || 'Colombo City Hotel';
  const branchSubtitle = 'Hotel · Restaurant · Shop';

  // Get user avatar initials
  const getInitials = (name) => {
    if (!name) return 'DJ';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  };

  const displayName = user?.fullName || 'Dilani Jayasuriya';
  const displayRole = user?.roles?.some(r => r.includes('SUPER_ADMIN') || r.includes('TENANT_ADMIN'))
    ? 'Owner'
    : (user?.roles?.[0]?.replace('ROLE_', '').replace('_', ' ') || 'Owner');

  const avatarInitials = getInitials(displayName);

  // Navigation Items matching the exact screenshot
  const navItems = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutGrid, hasDropdown: false },
    { to: '/sales', label: 'POS / Sell', icon: ShoppingCart, hasDropdown: false },
    { to: '/restaurant', label: 'Tables & Orders', icon: UtensilsCrossed, hasDropdown: false },
    { to: '/kitchen', label: 'Kitchen Display', icon: Tv, hasDropdown: false },
    { to: '/hotel', label: 'Rooms & Reservations', icon: BedDouble, hasDropdown: false },
    { to: '/inventory', label: 'Inventory', icon: Package, hasDropdown: true },
    { to: '/users', label: 'Customers & Loya...', icon: Users, hasDropdown: true },
    { to: '/shifts', label: 'Payments & Shifts', icon: CreditCard, hasDropdown: false },
    { to: '/reports', label: 'Reports', icon: BarChart2, hasDropdown: false },
    { to: '/branches', label: 'Settings', icon: Settings, hasDropdown: true }
  ];

  return (
    <aside className="tripos-sidebar">
      <div>
        {/* Brand Header */}
        <div className="sidebar-brand">
          <div className="brand-icon-quad">
            <span className="dot"></span>
            <span className="dot"></span>
            <span className="dot"></span>
            <span className="dot"></span>
          </div>
          <span className="brand-name">TriPOS</span>
        </div>

        {/* Branch Selector Card */}
        <div
          className="sidebar-branch-card"
          onClick={() => setBranchDropdownOpen(!branchDropdownOpen)}
        >
          <div className="sidebar-branch-left">
            <Bed className="sidebar-branch-icon" size={20} />
            <div className="sidebar-branch-info">
              <span className="sidebar-branch-name">{currentBranchName}</span>
              <span className="sidebar-branch-types">{branchSubtitle}</span>
            </div>
          </div>
          <ChevronDown
            className={`sidebar-branch-chevron ${branchDropdownOpen ? 'open' : ''}`}
            size={16}
          />

          {/* Branch Dropdown Popover */}
          {branchDropdownOpen && (
            <div className="sidebar-branch-dropdown" onClick={(e) => e.stopPropagation()}>
              <div style={{ fontSize: '0.7rem', color: '#8b92a5', padding: '0.35rem 0.6rem', fontWeight: 700, textTransform: 'uppercase' }}>
                Select Active Branch
              </div>
              {branches.length > 0 ? (
                branches.map((b) => (
                  <button
                    key={b.id}
                    type="button"
                    className={`branch-dropdown-item ${activeBranch?.id === b.id ? 'active' : ''}`}
                    onClick={() => {
                      switchBranch(b);
                      setBranchDropdownOpen(false);
                    }}
                  >
                    <span>{b.name}</span>
                    <span style={{ fontSize: '0.7rem', opacity: 0.7 }}>{b.branchType}</span>
                  </button>
                ))
              ) : (
                <div style={{ padding: '0.5rem', fontSize: '0.8rem', color: '#8b92a5' }}>
                  Colombo City Hotel (Active)
                </div>
              )}
            </div>
          )}
        </div>

        {/* Navigation Menu List */}
        <nav className="sidebar-nav-container">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) => `sidebar-nav-item ${isActive ? 'active' : ''}`}
              >
                <div className="sidebar-nav-left">
                  <Icon className="nav-icon" size={20} strokeWidth={2} />
                  <span>{item.label}</span>
                </div>
                {item.hasDropdown && (
                  <ChevronDown className="nav-chevron" size={16} />
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Footer User Profile Widget */}
      <div className="sidebar-footer">
        <div className="sidebar-footer-profile">
          <div className="sidebar-avatar-pill">
            {avatarInitials}
          </div>
          <div className="sidebar-user-info">
            <span className="sidebar-user-name">{displayName}</span>
            <span className="sidebar-user-role">{displayRole}</span>
          </div>
        </div>

        <button
          type="button"
          className="sidebar-panel-toggle"
          title="Logout / Switch Account"
          onClick={() => {
            if (window.confirm('Log out of this POS terminal?')) {
              logout();
            }
          }}
        >
          <PanelLeftClose size={18} />
        </button>
      </div>
    </aside>
  );
}
