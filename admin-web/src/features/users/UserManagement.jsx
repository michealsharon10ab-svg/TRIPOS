import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { usersApi } from '../../api/usersApi';
import { 
  Users, UserPlus, Search, KeyRound, Lock, ShieldCheck, 
  CheckCircle, XCircle, MoreVertical, Building2, AlertTriangle, X 
} from 'lucide-react';

export default function UserManagement() {
  const { user: currentUser, hasPermission, hasRole } = useAuth();
  
  const [users, setUsers] = useState([]);
  const [branches, setBranches] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [feedbackMsg, setFeedbackMsg] = useState({ type: '', text: '' });

  // Modal States
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showPinModal, setShowPinModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  // Form States
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    fullName: '',
    phone: '',
    password: '',
    pin: '',
    roleIds: [4], // Default CASHIER
    branchIds: []
  });
  const [newPassword, setNewPassword] = useState('');
  const [newPin, setNewPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');

  const availableRoles = [
    { id: 1, name: 'ROLE_SUPER_ADMIN', label: 'Super Administrator', superAdminOnly: true },
    { id: 2, name: 'ROLE_TENANT_ADMIN', label: 'Tenant Administrator' },
    { id: 3, name: 'ROLE_BRANCH_MANAGER', label: 'Branch Manager' },
    { id: 4, name: 'ROLE_CASHIER', label: 'Front Counter Cashier' },
    { id: 5, name: 'ROLE_INVENTORY_STAFF', label: 'Inventory Controller' },
    { id: 6, name: 'ROLE_WAITER', label: 'Restaurant Waiter' },
    { id: 7, name: 'ROLE_KITCHEN_STAFF', label: 'Kitchen Chef (KDS)' },
    { id: 8, name: 'ROLE_HOTEL_RECEPTIONIST', label: 'Hotel Receptionist' }
  ];

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await usersApi.listUsers({ search: searchQuery });
      if (res?.success && res.data) {
        setUsers(res.data.content || []);
      }
    } catch (err) {
      setFeedbackMsg({ type: 'error', text: err.response?.data?.message || 'Failed to load staff list' });
    } finally {
      setLoading(false);
    }
  };

  const fetchBranches = async () => {
    try {
      const res = await usersApi.getBranches();
      if (res?.success && res.data) {
        setBranches(res.data);
      }
    } catch (err) {
      console.warn('Could not load branches:', err);
    }
  };

  useEffect(() => {
    fetchUsers();
    fetchBranches();
  }, [searchQuery]);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    try {
      await usersApi.createUser(formData);
      setFeedbackMsg({ type: 'success', text: `Staff account "${formData.username}" created successfully!` });
      setShowCreateModal(false);
      setFormData({
        username: '',
        email: '',
        fullName: '',
        phone: '',
        password: '',
        pin: '',
        roleIds: [4],
        branchIds: []
      });
      fetchUsers();
    } catch (err) {
      setFeedbackMsg({ type: 'error', text: err.response?.data?.message || err.message });
    }
  };

  const handleToggleStatus = async (user) => {
    const nextStatus = !user.active;
    if (!window.confirm(`Are you sure you want to ${nextStatus ? 'activate' : 'deactivate'} ${user.username}?`)) {
      return;
    }
    try {
      await usersApi.toggleUserStatus(user.id, nextStatus);
      setFeedbackMsg({ type: 'success', text: `User ${user.username} is now ${nextStatus ? 'ACTIVE' : 'DEACTIVATED'}` });
      fetchUsers();
    } catch (err) {
      setFeedbackMsg({ type: 'error', text: err.response?.data?.message || 'Failed to update user status' });
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    if (!selectedUser) return;
    try {
      await usersApi.resetPassword(selectedUser.id, newPassword);
      setFeedbackMsg({ type: 'success', text: `Password reset successfully for ${selectedUser.username}` });
      setShowPasswordModal(false);
      setNewPassword('');
    } catch (err) {
      setFeedbackMsg({ type: 'error', text: err.response?.data?.message || 'Failed to reset password' });
    }
  };

  const handleSetPin = async (e) => {
    e.preventDefault();
    if (!selectedUser) return;
    try {
      await usersApi.setUserPin(selectedUser.id, newPin, confirmPin);
      setFeedbackMsg({ type: 'success', text: `Terminal PIN configured for ${selectedUser.username}` });
      setShowPinModal(false);
      setNewPin('');
      setConfirmPin('');
      fetchUsers();
    } catch (err) {
      setFeedbackMsg({ type: 'error', text: err.response?.data?.message || 'Failed to set PIN' });
    }
  };

  return (
    <div style={{ padding: '1rem', maxWidth: '1200px', margin: '0 auto' }}>
      {/* Page Title & Stats */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800, color: '#0f172a', marginBottom: '0.25rem' }}>
            Staff & User Administration
          </h1>
          <p style={{ color: '#64748b', fontSize: '0.95rem' }}>
            Role-based authorization and branch assignment for {currentUser?.tenant?.name || 'Your Enterprise'}
          </p>
        </div>

        {(hasPermission('USER_CREATE') || hasRole('SUPER_ADMIN')) && (
          <button
            onClick={() => setShowCreateModal(true)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              backgroundColor: '#f5b700',
              color: '#0f172a',
              fontWeight: 700,
              padding: '0.75rem 1.25rem',
              borderRadius: '8px',
              border: 'none',
              cursor: 'pointer',
              boxShadow: '0 4px 12px rgba(245, 183, 0, 0.25)'
            }}
          >
            <UserPlus size={18} />
            <span>Add Staff Member</span>
          </button>
        )}
      </div>

      {/* Alert feedback */}
      {feedbackMsg.text && (
        <div style={{
          padding: '0.9rem 1.2rem',
          borderRadius: '8px',
          marginBottom: '1.5rem',
          backgroundColor: feedbackMsg.type === 'success' ? '#f0fdf4' : '#fef2f2',
          border: `1px solid ${feedbackMsg.type === 'success' ? '#bbf7d0' : '#fecaca'}`,
          color: feedbackMsg.type === 'success' ? '#166534' : '#991b1b',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <span>{feedbackMsg.text}</span>
          <button onClick={() => setFeedbackMsg({ type: '', text: '' })} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
            <X size={16} />
          </button>
        </div>
      )}

      {/* Search Bar */}
      <div style={{
        background: '#ffffff',
        padding: '0.75rem 1rem',
        borderRadius: '10px',
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem',
        border: '1px solid #e2e8f0',
        marginBottom: '1.5rem'
      }}>
        <Search size={18} color="#94a3b8" />
        <input
          type="text"
          placeholder="Search staff by username, full name, or email..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          style={{ border: 'none', outline: 'none', width: '100%', fontSize: '0.95rem' }}
        />
      </div>

      {/* Staff Table */}
      <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#475569', fontSize: '0.85rem', textTransform: 'uppercase' }}>
              <th style={{ padding: '1rem' }}>User Profile</th>
              <th style={{ padding: '1rem' }}>Assigned Roles</th>
              <th style={{ padding: '1rem' }}>Permitted Branches</th>
              <th style={{ padding: '1rem' }}>Security</th>
              <th style={{ padding: '1rem' }}>Status</th>
              <th style={{ padding: '1rem', textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="6" style={{ padding: '2rem', textAlign: 'center', color: '#64748b' }}>
                  Loading staff accounts...
                </td>
              </tr>
            ) : users.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ padding: '2.5rem', textAlign: 'center', color: '#64748b' }}>
                  No staff accounts found.
                </td>
              </tr>
            ) : (
              users.map((u) => (
                <tr key={u.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                  <td style={{ padding: '1rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <div style={{
                        width: '38px',
                        height: '38px',
                        borderRadius: '50%',
                        backgroundColor: '#f5b700',
                        color: '#0f172a',
                        fontWeight: 700,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '0.9rem'
                      }}>
                        {u.fullName?.charAt(0) || u.username?.charAt(0)}
                      </div>
                      <div>
                        <div style={{ fontWeight: 700, color: '#0f172a' }}>{u.fullName}</div>
                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>@{u.username} · {u.email}</div>
                      </div>
                    </div>
                  </td>

                  <td style={{ padding: '1rem' }}>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                      {u.roles?.map((r) => (
                        <span key={r} style={{
                          backgroundColor: r.includes('SUPER') ? '#fee2e2' : r.includes('ADMIN') ? '#fef3c7' : '#e0e7ff',
                          color: r.includes('SUPER') ? '#991b1b' : r.includes('ADMIN') ? '#92400e' : '#3730a3',
                          fontSize: '0.75rem',
                          fontWeight: 600,
                          padding: '0.2rem 0.55rem',
                          borderRadius: '6px'
                        }}>
                          {r.replace('ROLE_', '')}
                        </span>
                      ))}
                    </div>
                  </td>

                  <td style={{ padding: '1rem' }}>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                      {u.branches?.length > 0 ? (
                        u.branches.map((b) => (
                          <span key={b.id} style={{
                            backgroundColor: '#f1f5f9',
                            color: '#334155',
                            fontSize: '0.75rem',
                            padding: '0.2rem 0.55rem',
                            borderRadius: '4px',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.25rem'
                          }}>
                            <Building2 size={12} />
                            {b.name}
                          </span>
                        ))
                      ) : (
                        <span style={{ color: '#94a3b8', fontSize: '0.8rem' }}>All / Unrestricted</span>
                      )}
                    </div>
                  </td>

                  <td style={{ padding: '1rem' }}>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                      {u.twoFactorEnabled && (
                        <span title="Two-factor authentication active" style={{ color: '#16a34a' }}>
                          <ShieldCheck size={18} />
                        </span>
                      )}
                      {u.pinConfigured && (
                        <span title="POS terminal PIN configured" style={{ color: '#f5b700' }}>
                          <KeyRound size={18} />
                        </span>
                      )}
                    </div>
                  </td>

                  <td style={{ padding: '1rem' }}>
                    <span style={{
                      backgroundColor: u.active ? '#dcfce7' : '#fee2e2',
                      color: u.active ? '#15803d' : '#b91c1c',
                      fontSize: '0.75rem',
                      fontWeight: 700,
                      padding: '0.25rem 0.6rem',
                      borderRadius: '9999px',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '0.3rem'
                    }}>
                      {u.active ? <CheckCircle size={12} /> : <XCircle size={12} />}
                      {u.active ? 'ACTIVE' : 'DISABLED'}
                    </span>
                  </td>

                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                      {(hasPermission('USER_UPDATE') || hasRole('SUPER_ADMIN')) && (
                        <>
                          <button
                            onClick={() => { setSelectedUser(u); setShowPasswordModal(true); }}
                            title="Reset Staff Password"
                            style={{ padding: '0.4rem 0.6rem', borderRadius: '6px', border: '1px solid #cbd5e1', background: '#ffffff', cursor: 'pointer' }}
                          >
                            <Lock size={14} />
                          </button>
                          <button
                            onClick={() => { setSelectedUser(u); setShowPinModal(true); }}
                            title="Set POS Terminal PIN"
                            style={{ padding: '0.4rem 0.6rem', borderRadius: '6px', border: '1px solid #cbd5e1', background: '#ffffff', cursor: 'pointer' }}
                          >
                            <KeyRound size={14} />
                          </button>
                        </>
                      )}

                      {(hasPermission('USER_DISABLE') || hasRole('SUPER_ADMIN')) && u.id !== currentUser?.id && (
                        <button
                          onClick={() => handleToggleStatus(u)}
                          style={{
                            padding: '0.4rem 0.75rem',
                            borderRadius: '6px',
                            border: 'none',
                            fontSize: '0.8rem',
                            fontWeight: 600,
                            backgroundColor: u.active ? '#fee2e2' : '#dcfce7',
                            color: u.active ? '#b91c1c' : '#15803d',
                            cursor: 'pointer'
                          }}
                        >
                          {u.active ? 'Disable' : 'Enable'}
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* CREATE STAFF MODAL */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-card" style={{ maxWidth: '580px' }}>
            <div className="modal-header">
              <h3>Create New Staff Account</h3>
              <button onClick={() => setShowCreateModal(false)} style={{ border: 'none', background: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleCreateSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Username</label>
                  <input
                    type="text"
                    required
                    className="form-input"
                    value={formData.username}
                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Email Address</label>
                  <input
                    type="email"
                    required
                    className="form-input"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Full Name</label>
                  <input
                    type="text"
                    required
                    className="form-input"
                    value={formData.fullName}
                    onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Phone Number</label>
                  <input
                    type="text"
                    className="form-input"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Initial Password (Min 8 chars, 1 uppercase, 1 digit, 1 symbol)</label>
                  <input
                    type="password"
                    required
                    className="form-input"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Terminal POS PIN (4-6 digits, optional)</label>
                  <input
                    type="password"
                    maxLength={6}
                    className="form-input"
                    placeholder="e.g. 1234"
                    value={formData.pin}
                    onChange={(e) => setFormData({ ...formData, pin: e.target.value.replace(/\D/g, '') })}
                  />
                </div>
              </div>

              {/* Roles Selection */}
              <div className="form-group">
                <label>Assigned Role</label>
                <select
                  className="form-input"
                  value={formData.roleIds[0]}
                  onChange={(e) => setFormData({ ...formData, roleIds: [Number(e.target.value)] })}
                >
                  {availableRoles
                    .filter(r => !r.superAdminOnly || hasRole('SUPER_ADMIN'))
                    .map((r) => (
                      <option key={r.id} value={r.id}>
                        {r.label}
                      </option>
                    ))}
                </select>
              </div>

              {/* Branch Assignment */}
              <div className="form-group">
                <label>Permitted Branches</label>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.25rem' }}>
                  {branches.map((b) => {
                    const isSelected = formData.branchIds.includes(b.id);
                    return (
                      <button
                        type="button"
                        key={b.id}
                        onClick={() => {
                          const next = isSelected
                            ? formData.branchIds.filter(id => id !== b.id)
                            : [...formData.branchIds, b.id];
                          setFormData({ ...formData, branchIds: next });
                        }}
                        style={{
                          padding: '0.4rem 0.8rem',
                          borderRadius: '6px',
                          border: `1.5px solid ${isSelected ? '#f5b700' : '#cbd5e1'}`,
                          backgroundColor: isSelected ? '#fefce8' : '#ffffff',
                          fontWeight: isSelected ? 700 : 500,
                          fontSize: '0.85rem',
                          cursor: 'pointer'
                        }}
                      >
                        {b.name}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  style={{ padding: '0.65rem 1.25rem', border: '1px solid #cbd5e1', borderRadius: '8px', background: '#ffffff', cursor: 'pointer' }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary-yellow"
                  style={{ width: 'auto', padding: '0.65rem 1.5rem' }}
                >
                  Create Staff Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* RESET PASSWORD MODAL */}
      {showPasswordModal && selectedUser && (
        <div className="modal-overlay">
          <div className="modal-card">
            <div className="modal-header">
              <h3>Reset Password for @{selectedUser.username}</h3>
              <button onClick={() => setShowPasswordModal(false)} style={{ border: 'none', background: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleResetPassword}>
              <div className="form-group">
                <label>New Password (Min 8 chars, 1 uppercase, 1 digit, 1 symbol)</label>
                <input
                  type="password"
                  required
                  className="form-input"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  autoFocus
                />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button
                  type="button"
                  onClick={() => setShowPasswordModal(false)}
                  style={{ padding: '0.65rem 1.25rem', border: '1px solid #cbd5e1', borderRadius: '8px', background: '#ffffff', cursor: 'pointer' }}
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary-yellow" style={{ width: 'auto' }}>
                  Reset Password
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* SET POS TERMINAL PIN MODAL */}
      {showPinModal && selectedUser && (
        <div className="modal-overlay">
          <div className="modal-card">
            <div className="modal-header">
              <h3>Set POS Terminal PIN for @{selectedUser.username}</h3>
              <button onClick={() => setShowPinModal(false)} style={{ border: 'none', background: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleSetPin}>
              <div className="form-group">
                <label>4 to 6 Digit PIN</label>
                <input
                  type="password"
                  maxLength={6}
                  required
                  className="form-input"
                  value={newPin}
                  onChange={(e) => setNewPin(e.target.value.replace(/\D/g, ''))}
                  autoFocus
                />
              </div>
              <div className="form-group">
                <label>Confirm PIN</label>
                <input
                  type="password"
                  maxLength={6}
                  required
                  className="form-input"
                  value={confirmPin}
                  onChange={(e) => setConfirmPin(e.target.value.replace(/\D/g, ''))}
                />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button
                  type="button"
                  onClick={() => setShowPinModal(false)}
                  style={{ padding: '0.65rem 1.25rem', border: '1px solid #cbd5e1', borderRadius: '8px', background: '#ffffff', cursor: 'pointer' }}
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary-yellow" style={{ width: 'auto' }}>
                  Save PIN
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
