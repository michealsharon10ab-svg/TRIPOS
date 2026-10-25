# Bug Hunters Unified POS System - Authentication & Security API Specification

## 1. Overview
The Bug Hunters Unified POS application features an enterprise-grade security module powered by Spring Boot 3, Spring Security 6, stateless JSON Web Tokens (HS512/HS256), fine-grained permission-based access control (RBAC), multi-tenant data isolation, fast POS terminal PIN authentication, and RFC 6238 TOTP two-factor authentication (2FA).

---

## 2. Security Architecture Matrix

### 2.1 Role-to-Permission Mapping

| Permission | Category | SUPER_ADMIN | TENANT_ADMIN | BRANCH_MANAGER | CASHIER | INVENTORY_STAFF | WAITER | KITCHEN_STAFF | HOTEL_RECEPTIONIST |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| `USER_READ` | ADMINISTRATION | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `USER_CREATE` | ADMINISTRATION | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `USER_UPDATE` | ADMINISTRATION | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `USER_DISABLE` | ADMINISTRATION | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `ROLE_MANAGE` | ADMINISTRATION | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `BRANCH_READ` | BRANCH | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| `SALES_CREATE` | SALES | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| `SALES_READ` | SALES | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ✅ |
| `INVENTORY_READ` | INVENTORY | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| `INVENTORY_MANAGE` | INVENTORY | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `RESTAURANT_ORDER_CREATE` | RESTAURANT | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| `KITCHEN_ORDER_UPDATE` | RESTAURANT | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ |
| `HOTEL_RESERVATION_MANAGE`| HOTEL | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| `REPORT_VIEW` | REPORTING | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## 3. Authentication Endpoints (`/api/v1/auth`)

### 3.1 User Login
- **Endpoint:** `POST /api/v1/auth/login`
- **Access:** Public
- **Request Body:**
```json
{
  "username": "michealsharon.10ab@gmail.com",
  "password": "Tenant@12345",
  "branchId": 1,
  "rememberMe": true
}
```
- **Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "862f32cd-f336-4d6c-96bd-9d6fb65cf227",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "requires2FA": false,
    "tempToken": null,
    "user": {
      "id": 2,
      "username": "tenantadmin",
      "email": "michealsharon.10ab@gmail.com",
      "fullName": "Micheal Sharon",
      "roles": ["ROLE_TENANT_ADMIN"],
      "permissions": ["USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DISABLE", "BRANCH_READ", ...],
      "tenant": {
        "id": 1,
        "code": "LLG",
        "name": "Lanka Leisure Group"
      }
    },
    "activeBranch": {
      "id": 1,
      "code": "COLOMBO-CITY",
      "name": "Colombo City Hotel",
      "branchType": "HOTEL"
    }
  }
}
```

---

### 3.2 POS Countertop PIN Login
- **Endpoint:** `POST /api/v1/auth/pin-login`
- **Access:** Public (Terminal Hardware Context)
- **Request Body:**
```json
{
  "tenantCode": "LLG",
  "branchId": 1,
  "terminalCode": "TERM-002",
  "pin": "5678"
}
```
- **Response (200 OK):** Returns JWT access token and refresh token scoped to the terminal and branch. Max 5 failed attempts locks the PIN for 15 minutes.

---

### 3.3 Refresh Token Rotation
- **Endpoint:** `POST /api/v1/auth/refresh`
- **Access:** Public
- **Request Body:**
```json
{
  "refreshToken": "862f32cd-f336-4d6c-96bd-9d6fb65cf227"
}
```
- **Response (200 OK):** Issues new access token and newly rotated refresh token. Revokes previous refresh token. If a revoked token is reused, all sessions for the compromised user are terminated immediately.

---

### 3.4 Two-Factor Authentication (TOTP)
- **Setup:** `POST /api/v1/auth/2fa/setup` (Authenticated) - Generates secret and QR code URI.
- **Enable:** `POST /api/v1/auth/2fa/enable` - Verifies initial 6-digit TOTP code and activates 2FA.
- **Verify:** `POST /api/v1/auth/2fa/verify` - Challenge endpoint during login when 2FA is enabled.
- **Disable:** `POST /api/v1/auth/2fa/disable` - Deactivates 2FA upon password confirmation.

---

## 4. User Administration Endpoints (`/api/v1/admin/users`)

- `GET /api/v1/admin/users` - List staff members (Tenant-isolated). Requires `USER_READ`.
- `GET /api/v1/admin/users/{id}` - Get staff member profile. Requires `USER_READ`.
- `POST /api/v1/admin/users` - Create staff account. Requires `USER_CREATE`. Prevents privilege escalation.
- `PUT /api/v1/admin/users/{id}` - Update staff profile and role/branch assignments. Requires `USER_UPDATE`.
- `PATCH /api/v1/admin/users/{id}/status?active=false` - Activate or deactivate account. Requires `USER_DISABLE`.
- `POST /api/v1/admin/users/{id}/reset-password` - Admin-initiated password reset. Requires `USER_UPDATE`.
- `POST /api/v1/admin/users/{id}/set-pin` - Configure or reset POS terminal PIN. Requires `USER_UPDATE`.
