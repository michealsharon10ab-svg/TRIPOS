# Bug Hunters Unified POS System

A comprehensive unified Point-of-Sale (POS) and Enterprise Management solution supporting Retail Sales, Inventory, Restaurant Operations, Hotel Room Management, and Multi-Branch Administration.

---

## Architecture Overview

```
bughunters-unified-pos/
├── backend/            # Spring Boot 3 REST API & WebSocket Services
├── admin-web/          # React + Vite Central Administration Dashboard
├── pos-desktop/        # JavaFX Offline-First Countertop POS Client
├── kitchen-display/    # React Kitchen Display System (KDS)
├── database/           # Schema diagrams, Flyway migrations, and seed data
├── docs/               # System architecture and technical documentation
└── deployment/         # Docker Compose and deployment configurations
```

---

## Tech Stack

- **Backend:** Java 17+, Spring Boot 3, Spring Data JPA, Spring Security, Spring WebSocket, Flyway, PostgreSQL, Redis
- **Admin Portal:** React, Vite, TailwindCSS / Vanilla CSS, React Router, Axios
- **Countertop POS Client:** JavaFX, Maven, Offline SQLite / H2 cache, Hardware Drivers (ESC/POS Printer, Cash Drawer, Barcode Scanner)
- **Kitchen Display System (KDS):** React, Vite, STOMP/WebSocket real-time sync
- **Deployment:** Docker, Docker Compose, GitHub Actions CI/CD
