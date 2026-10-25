import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Store, Utensils, Bed, Lock, ArrowLeft, ShieldCheck, CheckCircle2, AlertCircle } from 'lucide-react';
import '../../styles/auth.css';

export default function Login() {
  const navigate = useNavigate();
  const { login, pinLogin, verify2Fa } = useAuth();

  // Mode: 'PASSWORD' | 'PIN' | '2FA' | 'BRANCH_SELECT'
  const [authMode, setAuthMode] = useState('PASSWORD');
  
  // Standard Credentials State
  const [username, setUsername] = useState('tenantadmin');
  const [password, setPassword] = useState('Tenant@12345');
  const [rememberTerminal, setRememberTerminal] = useState(true);

  // PIN Login State
  const [pinDigits, setPinDigits] = useState('');
  const tenantCode = 'LLG';
  const branchId = 1; // Colombo City Hotel
  const terminalCode = 'TERM-002'; // Terminal 2

  // 2FA State
  const [tempToken, setTempToken] = useState('');
  const [twoFaCode, setTwoFaCode] = useState('');

  // UI Feedback
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Handle Standard Password Login
  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setLoading(true);

    try {
      const result = await login(username, password, branchId, rememberTerminal);
      if (result.requires2FA) {
        setTempToken(result.tempToken);
        setAuthMode('2FA');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setErrorMessage(err.response?.data?.message || err.message || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  // Handle PIN Keypad Click
  const handleKeypadPress = (val) => {
    if (val === 'CLEAR') {
      setPinDigits('');
      return;
    }
    if (val === 'BACK') {
      setPinDigits(prev => prev.slice(0, -1));
      return;
    }
    if (pinDigits.length < 6) {
      const nextPin = pinDigits + val;
      setPinDigits(nextPin);
      if (nextPin.length === 4) {
        // Auto-submit 4-digit PIN for rapid countertop cashier access
        executePinLogin(nextPin);
      }
    }
  };

  const executePinLogin = async (pinToSubmit) => {
    setErrorMessage('');
    setLoading(true);
    try {
      await pinLogin(tenantCode, branchId, terminalCode, pinToSubmit);
      navigate('/dashboard');
    } catch (err) {
      setErrorMessage(err.response?.data?.message || err.message || 'Invalid PIN for this terminal');
      setPinDigits('');
    } finally {
      setLoading(false);
    }
  };

  // Handle 2FA Verification
  const handle2FaSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setLoading(true);
    try {
      await verify2Fa(tempToken, twoFaCode, branchId);
      navigate('/dashboard');
    } catch (err) {
      setErrorMessage(err.response?.data?.message || err.message || 'Invalid verification code');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      {/* Left Hero Banner - TriPOS Unified Brand Layout */}
      <div className="auth-hero">
        <div className="brand-header">
          <div className="brand-logo-icon">
            <div className="brand-square"></div>
            <div className="brand-square"></div>
            <div className="brand-square"></div>
            <div className="brand-square"></div>
          </div>
          <span className="brand-title">TriPOS</span>
        </div>

        <div className="hero-main-content">
          {/* Triangular arrangement of 3 yellow cards */}
          <div className="hero-badges-container">
            <div className="badge-top-row">
              <div className="yellow-badge" title="Retail POS">
                <Store size={44} color="#111827" strokeWidth={1.8} />
              </div>
            </div>
            <div className="badge-bottom-row">
              <div className="yellow-badge" title="Restaurant POS & Kitchen System">
                <Utensils size={44} color="#111827" strokeWidth={1.8} />
              </div>
              <div className="yellow-badge" title="Hotel Room Folios & Front Desk">
                <Bed size={44} color="#111827" strokeWidth={1.8} />
              </div>
            </div>
          </div>

          <h1 className="hero-heading">One POS for shops, restaurants and hotels</h1>
          <p className="hero-description">
            Billing, kitchen orders, room folios and stock — across every branch, even when the internet drops.
          </p>

          <div className="hero-tags">
            <div className="hero-tag"><span>●</span> Retail</div>
            <div className="hero-tag"><span>●</span> Restaurant</div>
            <div className="hero-tag"><span>●</span> Hotel</div>
          </div>
        </div>

        <div style={{ fontSize: '0.8rem', color: '#6b7280' }}>
          Bug Hunters Architecture · University Unified Enterprise Edition
        </div>
      </div>

      {/* Right Interaction Panel */}
      <div className="auth-panel">
        <div className="auth-card-inner">
          {/* Error Banner */}
          {errorMessage && (
            <div className="auth-error-alert">
              <AlertCircle size={18} />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* VIEW 1: Standard Username/Password Login */}
          {authMode === 'PASSWORD' && (
            <div>
              <div className="auth-header">
                <h1>Welcome back</h1>
                <p>Sign in to Lanka Leisure Group</p>
              </div>

              <form onSubmit={handlePasswordSubmit}>
                <div className="form-group">
                  <label htmlFor="username">Username or email</label>
                  <input
                    id="username"
                    type="text"
                    className="form-input"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    autoFocus
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="password">Password</label>
                  <input
                    id="password"
                    type="password"
                    className="form-input"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>

                <div className="form-options-row">
                  <label className="remember-checkbox">
                    <input
                      type="checkbox"
                      checked={rememberTerminal}
                      onChange={(e) => setRememberTerminal(e.target.checked)}
                    />
                    <span>Remember this terminal</span>
                  </label>
                  <a href="#forgot" className="forgot-link" onClick={(e) => { e.preventDefault(); alert('Please ask your Tenant Administrator to reset your password via Staff Management.'); }}>
                    Forgot password?
                  </a>
                </div>

                <button type="submit" className="btn-primary-yellow" disabled={loading}>
                  {loading ? 'Authenticating...' : 'Sign in'}
                </button>

                <div className="auth-divider">
                  <span>or</span>
                </div>

                <button
                  type="button"
                  className="btn-outline-pin"
                  onClick={() => { setAuthMode('PIN'); setPinDigits(''); setErrorMessage(''); }}
                >
                  <Lock size={16} />
                  <span>Use PIN login</span>
                </button>

                <div style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.875rem', color: '#94a3b8' }}>
                  <span>Don't have a business account? </span>
                  <button
                    type="button"
                    onClick={() => navigate('/signup')}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: '#f5b700',
                      fontWeight: '600',
                      cursor: 'pointer',
                      textDecoration: 'underline',
                      padding: 0
                    }}
                  >
                    Register Business
                  </button>
                </div>
              </form>

              <div className="auth-footer-caption">
                Colombo City Hotel · Terminal 2 · TriPOS v1.0
              </div>
            </div>
          )}

          {/* VIEW 2: Fast POS Countertop PIN Login */}
          {authMode === 'PIN' && (
            <div className="pin-pad-container">
              <div className="auth-header" style={{ textAlign: 'center', width: '100%' }}>
                <h1>Terminal PIN Login</h1>
                <p>Colombo City Hotel · Terminal 2</p>
              </div>

              {/* 4 to 6 PIN Dots Indicator */}
              <div className="pin-display-dots">
                {[0, 1, 2, 3].map((idx) => (
                  <div
                    key={idx}
                    className={`pin-dot ${pinDigits.length > idx ? 'filled' : ''}`}
                  />
                ))}
              </div>

              {/* Keypad */}
              <div className="keypad-grid">
                {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((digit) => (
                  <button
                    key={digit}
                    type="button"
                    className="keypad-btn"
                    onClick={() => handleKeypadPress(digit.toString())}
                    disabled={loading}
                  >
                    {digit}
                  </button>
                ))}
                <button
                  type="button"
                  className="keypad-btn action-btn"
                  onClick={() => handleKeypadPress('CLEAR')}
                  disabled={loading}
                >
                  C
                </button>
                <button
                  type="button"
                  className="keypad-btn"
                  onClick={() => handleKeypadPress('0')}
                  disabled={loading}
                >
                  0
                </button>
                <button
                  type="button"
                  className="keypad-btn action-btn"
                  onClick={() => handleKeypadPress('BACK')}
                  disabled={loading}
                >
                  ⌫
                </button>
              </div>

              <button
                type="button"
                className="btn-outline-pin"
                style={{ width: '100%' }}
                onClick={() => { setAuthMode('PASSWORD'); setErrorMessage(''); }}
              >
                <ArrowLeft size={16} />
                <span>Standard Password Login</span>
              </button>

              <div className="auth-footer-caption">
                Scoped to Terminal 2 · Rapid Cashier Switch
              </div>
            </div>
          )}

          {/* VIEW 3: Two-Factor Authentication Prompt */}
          {authMode === '2FA' && (
            <div>
              <div className="auth-header">
                <div style={{ display: 'inline-flex', padding: '0.75rem', background: '#fefce8', borderRadius: '12px', color: '#f5b700', marginBottom: '0.75rem' }}>
                  <ShieldCheck size={32} />
                </div>
                <h1>Two-Factor Verification</h1>
                <p>Enter the 6-digit code from Google Authenticator</p>
              </div>

              <form onSubmit={handle2FaSubmit}>
                <div className="form-group">
                  <label htmlFor="2fa-code">6-Digit Security Code</label>
                  <input
                    id="2fa-code"
                    type="text"
                    maxLength={6}
                    placeholder="123456"
                    className="form-input"
                    style={{ fontSize: '1.5rem', letterSpacing: '6px', textAlign: 'center' }}
                    value={twoFaCode}
                    onChange={(e) => setTwoFaCode(e.target.value.replace(/\D/g, ''))}
                    required
                    autoFocus
                  />
                </div>

                <button type="submit" className="btn-primary-yellow" disabled={loading || twoFaCode.length !== 6}>
                  {loading ? 'Verifying...' : 'Verify & Access POS'}
                </button>

                <button
                  type="button"
                  className="btn-outline-pin"
                  style={{ marginTop: '1rem' }}
                  onClick={() => { setAuthMode('PASSWORD'); setErrorMessage(''); }}
                >
                  <ArrowLeft size={16} />
                  <span>Cancel</span>
                </button>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
