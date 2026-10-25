import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Search, Bell, Clock, Building2, ChevronDown, LogOut, CheckCircle2 } from 'lucide-react';

export default function Header() {
  const { user, activeBranch, switchBranch, logout } = useAuth();
  const [showBranchMenu, setShowBranchMenu] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const branches = user?.branches ? Array.from(user.branches) : [];
  const currentBranchName = activeBranch?.name || 'Colombo City Hotel';

  const getInitials = (name) => {
    if (!name) return 'DJ';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  };

  const displayName = user?.fullName || 'Dilani Jayasuriya';
  const avatarInitials = getInitials(displayName);

  return (
    <header style={{
      padding: '0.85rem 2rem',
      backgroundColor: '#ffffff',
      borderBottom: '1px solid #eef2f6',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      gap: '1.5rem',
      boxSizing: 'border-box'
    }}>
      {/* Left: Breadcrumbs & Page Title */}
      <div>
        <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginBottom: '0.15rem' }}>
          Payments & Shifts / Close shift
        </div>
        <h1 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#0f172a', margin: 0, letterSpacing: '-0.3px' }}>
          Close shift
        </h1>
      </div>

      {/* Center: Search Bar */}
      <div style={{ flex: '0 1 360px', position: 'relative' }}>
        <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)' }} />
        <input
          type="text"
          placeholder="Search..."
          style={{
            width: '100%',
            padding: '0.55rem 0.85rem 0.55rem 2.4rem',
            borderRadius: '10px',
            border: '1px solid #e2e8f0',
            backgroundColor: '#f1f5f9',
            fontSize: '0.85rem',
            color: '#1e293b',
            outline: 'none',
            boxSizing: 'border-box'
          }}
        />
      </div>

      {/* Right Controls Matching Screenshot */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
        {/* Branch Selector Pill */}
        <div style={{ position: 'relative' }}>
          <button
            type="button"
            onClick={() => setShowBranchMenu(!showBranchMenu)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.45rem',
              backgroundColor: '#ffffff',
              border: '1.5px solid #e2e8f0',
              padding: '0.45rem 0.85rem',
              borderRadius: '9999px',
              fontSize: '0.825rem',
              fontWeight: 600,
              color: '#1e293b',
              cursor: 'pointer'
            }}
          >
            <Building2 size={15} color="#475569" />
            <span>{currentBranchName}</span>
            <ChevronDown size={14} color="#64748b" />
          </button>

          {showBranchMenu && (
            <div style={{
              position: 'absolute',
              top: '115%',
              right: 0,
              backgroundColor: '#ffffff',
              border: '1px solid #e2e8f0',
              borderRadius: '10px',
              boxShadow: '0 12px 25px rgba(0,0,0,0.12)',
              padding: '0.4rem',
              minWidth: '220px',
              zIndex: 100
            }}>
              <div style={{ padding: '0.35rem 0.6rem', fontSize: '0.7rem', fontWeight: 700, color: '#94a3b8', textTransform: 'uppercase' }}>
                Switch Operating Branch
              </div>
              {branches.length > 0 ? (
                branches.map((b) => (
                  <button
                    key={b.id}
                    onClick={() => { switchBranch(b); setShowBranchMenu(false); }}
                    style={{
                      width: '100%',
                      textAlign: 'left',
                      padding: '0.5rem 0.75rem',
                      borderRadius: '6px',
                      border: 'none',
                      backgroundColor: activeBranch?.id === b.id ? '#fefce8' : 'transparent',
                      color: activeBranch?.id === b.id ? '#854d0e' : '#1e293b',
                      fontWeight: activeBranch?.id === b.id ? 700 : 500,
                      fontSize: '0.85rem',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between'
                    }}
                  >
                    <span>{b.name}</span>
                    <span style={{ fontSize: '0.7rem', color: '#64748b' }}>{b.branchType}</span>
                  </button>
                ))
              ) : (
                <div style={{ padding: '0.5rem 0.75rem', fontSize: '0.825rem', color: '#64748b' }}>
                  Colombo City Hotel (Current)
                </div>
              )}
            </div>
          )}
        </div>

        {/* Online Status Pill */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.35rem',
          backgroundColor: '#f0fdf4',
          border: '1px solid #bbf7d0',
          padding: '0.4rem 0.75rem',
          borderRadius: '9999px',
          fontSize: '0.775rem',
          fontWeight: 600,
          color: '#16a34a'
        }}>
          <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#16a34a' }}></span>
          <span>Online</span>
        </div>

        {/* Notifications Icon with Badge 5 */}
        <div style={{ position: 'relative', cursor: 'pointer', padding: '0.35rem' }}>
          <Bell size={18} color="#334155" />
          <span style={{
            position: 'absolute',
            top: '0px',
            right: '0px',
            backgroundColor: '#ef4444',
            color: '#ffffff',
            fontSize: '0.65rem',
            fontWeight: 800,
            borderRadius: '9999px',
            width: '16px',
            height: '16px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            border: '2px solid #ffffff'
          }}>
            5
          </span>
        </div>

        {/* Shift open 8:00 AM badge */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.4rem',
          backgroundColor: '#fffbeb',
          border: '1px solid #fef3c7',
          padding: '0.4rem 0.75rem',
          borderRadius: '9999px',
          fontSize: '0.775rem',
          fontWeight: 600,
          color: '#b45309'
        }}>
          <Clock size={14} color="#d97706" />
          <span>Shift open · 8:00 AM</span>
        </div>

        {/* User Avatar with Dropdown */}
        <div style={{ position: 'relative' }}>
          <button
            type="button"
            onClick={() => setShowUserMenu(!showUserMenu)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.35rem',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer',
              padding: '0.15rem'
            }}
          >
            <div style={{
              width: '34px',
              height: '34px',
              borderRadius: '50%',
              backgroundColor: '#f5b700',
              color: '#0b0c10',
              fontWeight: 800,
              fontSize: '0.85rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              {avatarInitials}
            </div>
            <ChevronDown size={14} color="#64748b" />
          </button>

          {showUserMenu && (
            <div style={{
              position: 'absolute',
              top: '115%',
              right: 0,
              backgroundColor: '#ffffff',
              border: '1px solid #e2e8f0',
              borderRadius: '10px',
              boxShadow: '0 12px 25px rgba(0,0,0,0.12)',
              padding: '0.5rem',
              minWidth: '180px',
              zIndex: 100
            }}>
              <div style={{ padding: '0.4rem 0.6rem', borderBottom: '1px solid #f1f5f9', marginBottom: '0.4rem' }}>
                <div style={{ fontWeight: 700, fontSize: '0.85rem', color: '#0f172a' }}>{displayName}</div>
                <div style={{ fontSize: '0.725rem', color: '#64748b' }}>Owner / Administrator</div>
              </div>
              <button
                onClick={logout}
                style={{
                  width: '100%',
                  textAlign: 'left',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.5rem 0.6rem',
                  border: 'none',
                  backgroundColor: 'transparent',
                  color: '#dc2626',
                  fontSize: '0.825rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  borderRadius: '6px'
                }}
              >
                <LogOut size={14} />
                <span>Sign Out</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
