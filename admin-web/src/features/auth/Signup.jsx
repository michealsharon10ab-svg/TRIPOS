import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  Store,
  Utensils,
  Bed,
  ArrowRight,
  ArrowLeft,
  Building2,
  User,
  Mail,
  Phone,
  Lock,
  Eye,
  EyeOff,
  CheckCircle2,
  AlertCircle,
  ShieldCheck,
  MapPin,
  Sparkles
} from 'lucide-react';
import '../../styles/auth.css';

export default function Signup() {
  const navigate = useNavigate();
  const { register } = useAuth();

  // Wizard Step: 1 = Business Details, 2 = Admin Account Credentials
  const [currentStep, setCurrentStep] = useState(1);

  // Form State
  const [formData, setFormData] = useState({
    businessName: '',
    businessType: 'RETAIL',
    branchName: '',
    branchAddress: '',
    phone: '',
    fullName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreeTerms: true
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    if (errorMessage) setErrorMessage('');
  };

  const handleBusinessTypeSelect = (type) => {
    setFormData(prev => ({ ...prev, businessType: type }));
  };

  // Password Strength Calculation
  const calculatePasswordStrength = (pwd) => {
    if (!pwd) return 0;
    let score = 0;
    if (pwd.length >= 6) score += 1;
    if (pwd.length >= 8) score += 1;
    if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) score += 1;
    if (/\d/.test(pwd) || /[^A-Za-z0-9]/.test(pwd)) score += 1;
    return score;
  };

  const passwordScore = calculatePasswordStrength(formData.password);

  // Validate Step 1
  const validateStep1 = () => {
    if (!formData.businessName.trim()) {
      setErrorMessage('Please enter your Business or Company name');
      return false;
    }
    if (!formData.branchAddress.trim()) {
      setErrorMessage('Please enter your primary branch location or city');
      return false;
    }
    if (!formData.phone.trim()) {
      setErrorMessage('Please enter a business contact phone number');
      return false;
    }
    return true;
  };

  // Next Step Action
  const handleNextStep = (e) => {
    e.preventDefault();
    setErrorMessage('');
    if (validateStep1()) {
      setCurrentStep(2);
    }
  };

  // Final Submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (!formData.fullName.trim()) {
      setErrorMessage('Please enter your Full Name');
      return;
    }
    if (!formData.username.trim() || formData.username.length < 3) {
      setErrorMessage('Username must be at least 3 characters');
      return;
    }
    if (!formData.email.trim() || !formData.email.includes('@')) {
      setErrorMessage('Please enter a valid work email address');
      return;
    }
    if (!formData.password || formData.password.length < 6) {
      setErrorMessage('Password must be at least 6 characters long');
      return;
    }
    if (formData.password !== formData.confirmPassword) {
      setErrorMessage('Passwords do not match');
      return;
    }
    if (!formData.agreeTerms) {
      setErrorMessage('Please accept the Terms of Service to proceed');
      return;
    }

    setLoading(true);

    try {
      const payload = {
        businessName: formData.businessName.trim(),
        businessType: formData.businessType,
        branchName: formData.branchName.trim() || `${formData.businessName.trim()} Main`,
        branchAddress: formData.branchAddress.trim(),
        phone: formData.phone.trim(),
        fullName: formData.fullName.trim(),
        username: formData.username.trim().toLowerCase(),
        email: formData.email.trim().toLowerCase(),
        password: formData.password
      };

      await register(payload);
      setSuccessMessage('Organization and Admin account created successfully! Redirecting...');
      setTimeout(() => {
        navigate('/dashboard');
      }, 1000);
    } catch (err) {
      setErrorMessage(err.response?.data?.message || err.message || 'Registration failed. Please verify your details.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      {/* Left Hero Banner */}
      <div className="auth-hero">
        <div className="brand-header">
          <div className="brand-logo-icon">
            <span className="brand-square"></span>
            <span className="brand-square"></span>
            <span className="brand-square"></span>
            <span className="brand-square"></span>
          </div>
          <span className="brand-title">TriPOS</span>
        </div>

        <div className="hero-main-content">
          <div className="hero-badges-container">
            <div className="badge-top-row">
              <div className="yellow-badge" title="Retail & Supermarket">
                <Store size={38} color="#0f1117" strokeWidth={2.2} />
              </div>
            </div>
            <div className="badge-bottom-row">
              <div className="yellow-badge" title="Restaurant & Dining">
                <Utensils size={36} color="#0f1117" strokeWidth={2.2} />
              </div>
              <div className="yellow-badge" title="Hotel & Lodging">
                <Bed size={36} color="#0f1117" strokeWidth={2.2} />
              </div>
            </div>
          </div>

          <h1 className="hero-heading">Launch Your Business with TriPOS</h1>
          <p className="hero-description">
            Register your business in under 2 minutes. Get instant access to multi-branch POS, live inventory, kitchen display, room reservations, and unified accounting.
          </p>

          <div style={{ marginTop: '2rem', display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#cbd5e1', fontSize: '0.9rem' }}>
              <CheckCircle2 size={18} color="#f5b700" />
              <span>Multi-Tenant Enterprise Architecture</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#cbd5e1', fontSize: '0.9rem' }}>
              <CheckCircle2 size={18} color="#f5b700" />
              <span>Retail, Restaurant & Hotel in One Single System</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#cbd5e1', fontSize: '0.9rem' }}>
              <CheckCircle2 size={18} color="#f5b700" />
              <span>Role-Based Access Control & Quick Cashier PINs</span>
            </div>
          </div>
        </div>

        <div className="hero-footer">
          <span>TriPOS Cloud Platform · Unified Hospitality & Retail Architecture</span>
        </div>
      </div>

      {/* Right Content Section */}
      <div className="auth-form-container">
        <div className="signup-card-container">
          
          {/* Header */}
          <div className="auth-header">
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', color: '#f5b700', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.5rem' }}>
              <Sparkles size={16} />
              <span>NEW BUSINESS ONBOARDING</span>
            </div>
            <h1>Create Business Account</h1>
            <p>Setup your organization and primary administrator login</p>
          </div>

          {/* Stepper Tabs */}
          <div className="signup-stepper">
            <button
              type="button"
              className={`step-tab ${currentStep === 1 ? 'active' : ''} ${currentStep > 1 ? 'completed' : ''}`}
              onClick={() => setCurrentStep(1)}
            >
              <span className="step-number">1</span>
              <span>Business Profile</span>
            </button>
            <button
              type="button"
              className={`step-tab ${currentStep === 2 ? 'active' : ''}`}
              onClick={() => {
                if (validateStep1()) setCurrentStep(2);
              }}
            >
              <span className="step-number">2</span>
              <span>Admin Account</span>
            </button>
          </div>

          {/* Error Alert */}
          {errorMessage && (
            <div className="auth-alert error" role="alert" style={{ marginBottom: '1.25rem' }}>
              <AlertCircle size={18} />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Success Alert */}
          {successMessage && (
            <div className="auth-alert success" role="alert" style={{ marginBottom: '1.25rem', background: 'rgba(16, 185, 129, 0.15)', borderColor: '#10b981', color: '#10b981' }}>
              <CheckCircle2 size={18} />
              <span>{successMessage}</span>
            </div>
          )}

          {/* STEP 1: Business Details */}
          {currentStep === 1 && (
            <form onSubmit={handleNextStep}>
              <div className="form-group">
                <label htmlFor="businessName">
                  Business / Company Name <span style={{ color: '#f5b700' }}>*</span>
                </label>
                <input
                  id="businessName"
                  name="businessName"
                  type="text"
                  placeholder="e.g., Ceylon Royal Spices & Mart"
                  className="form-input"
                  value={formData.businessName}
                  onChange={handleChange}
                  required
                  autoFocus
                />
              </div>

              {/* Business Type Selector */}
              <div className="form-group">
                <label>
                  Primary Business Category <span style={{ color: '#f5b700' }}>*</span>
                </label>
                <div className="business-type-grid">
                  <div
                    className={`business-type-card ${formData.businessType === 'RETAIL' ? 'active' : ''}`}
                    onClick={() => handleBusinessTypeSelect('RETAIL')}
                  >
                    <div className="business-type-icon">
                      <Store size={20} />
                    </div>
                    <span className="business-type-title">Retail Shop</span>
                    <span className="business-type-desc">Barcode, Stock, Quick Cashier</span>
                  </div>

                  <div
                    className={`business-type-card ${formData.businessType === 'RESTAURANT' ? 'active' : ''}`}
                    onClick={() => handleBusinessTypeSelect('RESTAURANT')}
                  >
                    <div className="business-type-icon">
                      <Utensils size={20} />
                    </div>
                    <span className="business-type-title">Restaurant</span>
                    <span className="business-type-desc">Dine-in Tables, KDS & Orders</span>
                  </div>

                  <div
                    className={`business-type-card ${formData.businessType === 'HOTEL' ? 'active' : ''}`}
                    onClick={() => handleBusinessTypeSelect('HOTEL')}
                  >
                    <div className="business-type-icon">
                      <Bed size={20} />
                    </div>
                    <span className="business-type-title">Hotel / PMS</span>
                    <span className="business-type-desc">Rooms, Bookings & Guest Billing</span>
                  </div>
                </div>
              </div>

              <div className="form-row-2col">
                <div className="form-group">
                  <label htmlFor="branchName">Main Branch Name</label>
                  <input
                    id="branchName"
                    name="branchName"
                    type="text"
                    placeholder="e.g., Colombo Main Branch"
                    className="form-input"
                    value={formData.branchName}
                    onChange={handleChange}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="phone">
                    Contact Phone <span style={{ color: '#f5b700' }}>*</span>
                  </label>
                  <input
                    id="phone"
                    name="phone"
                    type="tel"
                    placeholder="+94 11 234 5678"
                    className="form-input"
                    value={formData.phone}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="branchAddress">
                  Branch Location / Address <span style={{ color: '#f5b700' }}>*</span>
                </label>
                <input
                  id="branchAddress"
                  name="branchAddress"
                  type="text"
                  placeholder="e.g., Galle Road, Colombo 03"
                  className="form-input"
                  value={formData.branchAddress}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="step-actions">
                <button
                  type="submit"
                  className="btn-primary-yellow"
                  style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
                >
                  <span>Continue to Admin Setup</span>
                  <ArrowRight size={18} />
                </button>
              </div>
            </form>
          )}

          {/* STEP 2: Administrator Credentials */}
          {currentStep === 2 && (
            <form onSubmit={handleSubmit}>
              <div className="form-row-2col">
                <div className="form-group">
                  <label htmlFor="fullName">
                    Admin Full Name <span style={{ color: '#f5b700' }}>*</span>
                  </label>
                  <input
                    id="fullName"
                    name="fullName"
                    type="text"
                    placeholder="e.g., Micheal Sharon"
                    className="form-input"
                    value={formData.fullName}
                    onChange={handleChange}
                    required
                    autoFocus
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="username">
                    Admin Username <span style={{ color: '#f5b700' }}>*</span>
                  </label>
                  <input
                    id="username"
                    name="username"
                    type="text"
                    placeholder="e.g., michealadmin"
                    className="form-input"
                    value={formData.username}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="email">
                  Work Email Address <span style={{ color: '#f5b700' }}>*</span>
                </label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="micheal@bughunters.pos"
                  className="form-input"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-row-2col">
                <div className="form-group">
                  <label htmlFor="password">
                    Password <span style={{ color: '#f5b700' }}>*</span>
                  </label>
                  <div className="input-password-wrapper">
                    <input
                      id="password"
                      name="password"
                      type={showPassword ? 'text' : 'password'}
                      placeholder="Min 6 characters"
                      className="form-input"
                      value={formData.password}
                      onChange={handleChange}
                      required
                    />
                    <button
                      type="button"
                      className="toggle-password-btn"
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="confirmPassword">
                    Confirm Password <span style={{ color: '#f5b700' }}>*</span>
                  </label>
                  <div className="input-password-wrapper">
                    <input
                      id="confirmPassword"
                      name="confirmPassword"
                      type={showConfirmPassword ? 'text' : 'password'}
                      placeholder="Re-type password"
                      className="form-input"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      required
                    />
                    <button
                      type="button"
                      className="toggle-password-btn"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    >
                      {showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                </div>
              </div>

              {/* Password Strength Indicator */}
              {formData.password && (
                <div className="password-strength-container">
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: '#94a3b8' }}>
                    <span>Password Strength</span>
                    <span style={{
                      fontWeight: 600,
                      color: passwordScore <= 1 ? '#ef4444' : passwordScore === 2 || passwordScore === 3 ? '#f59e0b' : '#10b981'
                    }}>
                      {passwordScore <= 1 ? 'Weak' : passwordScore === 2 || passwordScore === 3 ? 'Good' : 'Strong'}
                    </span>
                  </div>
                  <div className="password-strength-bars">
                    <span className={`strength-bar ${passwordScore >= 1 ? (passwordScore === 1 ? 'weak' : passwordScore <= 3 ? 'medium' : 'strong') : ''}`} />
                    <span className={`strength-bar ${passwordScore >= 2 ? (passwordScore <= 3 ? 'medium' : 'strong') : ''}`} />
                    <span className={`strength-bar ${passwordScore >= 3 ? (passwordScore <= 3 ? 'medium' : 'strong') : ''}`} />
                    <span className={`strength-bar ${passwordScore >= 4 ? 'strong' : ''}`} />
                  </div>
                </div>
              )}

              {/* Terms Checkbox */}
              <div className="form-options" style={{ marginTop: '0.75rem' }}>
                <label className="checkbox-label" style={{ fontSize: '0.8rem', color: '#cbd5e1' }}>
                  <input
                    type="checkbox"
                    name="agreeTerms"
                    checked={formData.agreeTerms}
                    onChange={handleChange}
                  />
                  <span>
                    I agree to the <strong style={{ color: '#f5b700' }}>TriPOS Multi-Tenant Terms</strong> and acknowledge the role as Tenant Administrator.
                  </span>
                </label>
              </div>

              <div className="step-actions">
                <button
                  type="button"
                  className="btn-outline-pin"
                  onClick={() => setCurrentStep(1)}
                  disabled={loading}
                >
                  <ArrowLeft size={16} />
                  <span>Back</span>
                </button>

                <button
                  type="submit"
                  className="btn-primary-yellow"
                  disabled={loading}
                >
                  {loading ? 'Creating Account...' : 'Complete Registration'}
                </button>
              </div>
            </form>
          )}

          {/* Footer Back to Login */}
          <div style={{ marginTop: '2rem', textAlign: 'center', fontSize: '0.875rem', color: '#94a3b8', borderTop: '1px solid rgba(255,255,255,0.08)', paddingTop: '1.25rem' }}>
            <span>Already have an account? </span>
            <button
              type="button"
              onClick={() => navigate('/login')}
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
              Sign In
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}
