// ═══════════════════════════════════════════════════════════
//  TriPOS Hotel Frontend — Main Application
//  Member 5: Kajanika P (Hotel & Reporting)
//  All buttons interactive | No DB data (empty states)
// ═══════════════════════════════════════════════════════════

// ── HELPERS ─────────────────────────────────────────────────
const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];

// ── TOAST NOTIFICATIONS ─────────────────────────────────────
function showToast(message, type = 'info', duration = 3200) {
  const icons = {
    success: `<svg width="16" height="16" fill="none" stroke="#22c55e" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>`,
    error:   `<svg width="16" height="16" fill="none" stroke="#ef4444" stroke-width="2.5" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`,
    info:    `<svg width="16" height="16" fill="none" stroke="#ffd600" stroke-width="2.5" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`,
  };
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `${icons[type] || icons.info}<span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.classList.add('removing');
    setTimeout(() => toast.remove(), 250);
  }, duration);
}

// ── MODAL HELPERS ────────────────────────────────────────────
function openModal(id) {
  const m = document.getElementById(id);
  if (m) { m.classList.add('open'); m.querySelector('input, select')?.focus(); }
}
function closeModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.remove('open');
}
function closeAllModals() {
  $$('.modal-overlay.open').forEach(m => m.classList.remove('open'));
}

function initModals() {
  // Overlay click closes modal
  $$('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', e => {
      if (e.target === overlay) overlay.classList.remove('open');
    });
  });
  // .modal-close buttons
  $$('.modal-close').forEach(btn => {
    btn.addEventListener('click', () => {
      btn.closest('.modal-overlay')?.classList.remove('open');
    });
  });
  // Escape key
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') closeAllModals();
  });
}

// ── FORM VALIDATION ─────────────────────────────────────────
function validateField(input, message) {
  const val = input.value.trim();
  const err = input.parentElement.querySelector('.field-error');
  if (!val) {
    input.classList.add('invalid');
    input.classList.remove('valid');
    if (!err) {
      const span = document.createElement('span');
      span.className = 'field-error';
      span.textContent = message || 'This field is required';
      input.parentElement.appendChild(span);
    }
    return false;
  } else {
    input.classList.remove('invalid');
    input.classList.add('valid');
    if (err) err.remove();
    return true;
  }
}

function clearValidation(formEl) {
  formEl?.querySelectorAll('.form-control').forEach(el => {
    el.classList.remove('invalid', 'valid');
    el.parentElement.querySelector('.field-error')?.remove();
  });
}

// ── SIDEBAR NAV SWITCHING ────────────────────────────────────
function initSidebarNav() {
  const breadcrumbMap = {
    hotel:     ['Rooms & Reservations', 'Room status board'],
    reports:   ['Inventory & hotel reports', 'Hotel occupancy'],
    dashboard: ['Dashboard', 'Overview'],
    pos:       ['POS / Sell', ''],
    tables:    ['Tables & Orders', ''],
    kitchen:   ['Kitchen Display', ''],
    inventory: ['Inventory', ''],
    customers: ['Customers & Loyalty', ''],
    payments:  ['Payments & Shifts', ''],
    settings:  ['Settings', ''],
  };

  $$('.nav-item[data-page]').forEach(link => {
    link.addEventListener('click', e => {
      e.preventDefault();
      const page = link.dataset.page;

      // Update nav active
      $$('.nav-item').forEach(n => n.classList.remove('active'));
      link.classList.add('active');

      // Show correct page
      $$('.section-page').forEach(p => p.classList.remove('active'));
      const target = document.getElementById(`page-${page}`);
      if (target) target.classList.add('active');

      // Update breadcrumb
      const [parent, current] = breadcrumbMap[page] || [page, ''];
      const bcParent = document.getElementById('bc-parent');
      const bcCurrent = document.getElementById('bc-current');
      if (bcParent) bcParent.textContent = parent;
      if (bcCurrent) bcCurrent.textContent = current;
    });
  });
}

// ── HOTEL SUB-NAV TAB SWITCHING ─────────────────────────────
function initSubNav() {
  $$('.subnav-tab[data-tab]').forEach(tab => {
    tab.addEventListener('click', () => {
      const targetId = 'content-' + tab.dataset.tab;

      $$('.subnav-tab[data-tab]').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      $$('.hotel-tab-content').forEach(c => c.classList.remove('active'));
      document.getElementById(targetId)?.classList.add('active');

      // Breadcrumb
      const labels = {
        'room-board':   'Room status board',
        'reservations': 'Reservations',
        'checkin-out':  'Check-in / out',
        'guests':       'Guests',
      };
      const bcCurrent = document.getElementById('bc-current');
      if (bcCurrent) bcCurrent.textContent = labels[tab.dataset.tab] || tab.textContent;
    });
  });

  // Reports sub-tabs
  $$('.subnav-tab[data-rtab]').forEach(tab => {
    tab.addEventListener('click', () => {
      $$('.subnav-tab[data-rtab]').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      // When DB connected: switch report content
      showToast(`Showing: ${tab.textContent.trim()}`, 'info', 2000);
    });
  });
}

// ── STAT PILL FILTER ─────────────────────────────────────────
function initStatPills() {
  $$('.stat-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      $$('.stat-pill').forEach(p => p.classList.remove('active-pill'));
      pill.classList.add('active-pill');
      // Sync with filter pills
      const filterMap = { vacant:'vacant', occupied:'occupied', dirty:'dirty', maintenance:'maintenance' };
      const match = pill.dataset.filter?.toLowerCase();
      if (match) {
        $$('.fpill').forEach(p => p.classList.remove('active'));
        const fpill = $(`.fpill[data-filter="${match}"]`);
        if (fpill) fpill.classList.add('active');
      }
      showToast(`Filtered: ${pill.querySelector('.sp-label')?.textContent || 'All'}`, 'info', 1800);
    });
  });
}

// ── FILTER PILLS ─────────────────────────────────────────────
function initFilterPills() {
  $$('.fpill').forEach(pill => {
    pill.addEventListener('click', () => {
      $$('.fpill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      $$('.stat-pill').forEach(p => p.classList.remove('active-pill'));
    });
  });

  // Room type dropdown
  document.getElementById('room-type-filter')?.addEventListener('change', e => {
    showToast(`Room type: ${e.target.value}`, 'info', 1800);
  });
}

// ── VIEW TOGGLE (List / Timeline) ────────────────────────────
function initViewToggle() {
  $$('.rvt-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      $$('.rvt-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const view = btn.dataset.view;
      const listTbl = $('#res-table')?.closest('.table-card');
      const timelineView = document.getElementById('timeline-view');
      if (view === 'timeline') {
        if (listTbl) listTbl.style.display = 'none';
        if (timelineView) timelineView.style.display = 'block';
      } else {
        if (listTbl) listTbl.style.display = '';
        if (timelineView) timelineView.style.display = 'none';
      }
    });
  });
}

// ── LIVE CLOCK ───────────────────────────────────────────────
function initClock() {
  function tick() {
    const now = new Date();
    let h = now.getHours(), m = now.getMinutes();
    const ampm = h >= 12 ? 'PM' : 'AM';
    h = h % 12 || 12;
    const el = document.getElementById('clock-time');
    if (el) el.textContent = `${h}:${String(m).padStart(2,'0')} ${ampm}`;
  }
  tick();
  setInterval(tick, 30000);
}

// ── TODAY DATE ───────────────────────────────────────────────
function setTodayDate() {
  const el = document.getElementById('today-date-sub');
  if (!el) return;
  const d = new Date();
  el.textContent = d.toLocaleDateString('en-GB', { weekday:'long', day:'numeric', month:'long', year:'numeric' });
  // Default dates in reservation modal
  const today = d.toISOString().split('T')[0];
  const tmrw  = new Date(Date.now() + 86400000).toISOString().split('T')[0];
  const ci = document.getElementById('nr-checkin');
  const co = document.getElementById('nr-checkout');
  if (ci && !ci.value) ci.value = today;
  if (co && !co.value) co.value = tmrw;
  // Default date ranges in report
  const rptFrom = document.getElementById('rpt-date-from');
  const rptTo   = document.getElementById('rpt-date-to');
  if (rptFrom && !rptFrom.value) rptFrom.value = new Date(Date.now() - 30*86400000).toISOString().split('T')[0];
  if (rptTo   && !rptTo.value)   rptTo.value   = today;
}

// ── ROOM BOARD BUTTONS ───────────────────────────────────────
function initRoomBoardButtons() {
  // + Add Room button
  document.getElementById('btn-add-room')?.addEventListener('click', () => openModal('modal-add-room'));

  // Submit add room
  document.getElementById('btn-submit-room')?.addEventListener('click', () => {
    const noInput    = document.getElementById('ar-no');
    const floorInput = document.getElementById('ar-floor');
    let ok = true;
    ok = validateField(noInput, 'Room number is required') && ok;
    ok = validateField(floorInput, 'Floor is required') && ok;
    if (!ok) return;

    const btn = document.getElementById('btn-submit-room');
    btn.classList.add('btn-loading');
    btn.disabled = true;

    // Simulate API call delay
    setTimeout(() => {
      btn.classList.remove('btn-loading');
      btn.disabled = false;
      closeModal('modal-add-room');
      clearValidation(document.querySelector('#modal-add-room .modal-body'));
      noInput.value = ''; floorInput.value = '';
      showToast(`Room ${noInput.dataset.lastVal || '—'} added! Connect DB to persist.`, 'success');
    }, 800);

    noInput.dataset.lastVal = noInput.value;
  });
}

// ── RESERVATION BUTTONS ──────────────────────────────────────
function initReservationButtons() {
  document.getElementById('btn-new-res')?.addEventListener('click', () => openModal('modal-new-res'));

  // Submit new reservation
  document.getElementById('btn-submit-res')?.addEventListener('click', () => {
    const ci    = document.getElementById('nr-checkin');
    const co    = document.getElementById('nr-checkout');
    const name  = document.getElementById('nr-gname');
    const nic   = document.getElementById('nr-nic');
    let ok = true;
    ok = validateField(ci,   'Check-in date is required') && ok;
    ok = validateField(co,   'Check-out date is required') && ok;
    ok = validateField(name, 'Guest name is required') && ok;
    ok = validateField(nic,  'NIC / Passport is required') && ok;
    if (ci.value && co.value && co.value <= ci.value) {
      co.classList.add('invalid');
      showToast('Check-out must be after check-in', 'error');
      return;
    }
    if (!ok) { showToast('Please fill in all required fields', 'error'); return; }

    const btn = document.getElementById('btn-submit-res');
    btn.classList.add('btn-loading'); btn.disabled = true;

    setTimeout(() => {
      btn.classList.remove('btn-loading'); btn.disabled = false;
      closeModal('modal-new-res');
      clearValidation(document.querySelector('#modal-new-res .modal-body'));
      showToast(`Reservation for ${name.value} created! Connect DB to persist.`, 'success');
      name.value = ''; nic.value = '';
    }, 900);
  });

  // Reservation search (UI only)
  document.getElementById('res-search')?.addEventListener('input', e => {
    // When DB connected: filter by e.target.value
  });

  // Status filter
  document.getElementById('res-status-filter')?.addEventListener('change', () => {
    showToast('Filter will apply when database is connected', 'info', 2000);
  });
}

// ── CHECK-IN / OUT BUTTONS ───────────────────────────────────
function initCheckinButtons() {
  document.getElementById('btn-walkin')?.addEventListener('click', () => openModal('modal-new-res'));
  document.getElementById('btn-new-res-ci')?.addEventListener('click', () => openModal('modal-new-res'));

  // Print Folio — enabled only when a stay is selected
  document.getElementById('btn-print-folio')?.addEventListener('click', () => {
    showToast('No active stay selected. Select from the lists above.', 'info');
  });
  document.getElementById('btn-pdf-invoice')?.addEventListener('click', () => {
    showToast('No active stay selected. Select from the lists above.', 'info');
  });
}

// ── GUEST BUTTONS ────────────────────────────────────────────
function initGuestButtons() {
  document.getElementById('btn-add-guest')?.addEventListener('click', () => openModal('modal-add-guest'));

  document.getElementById('btn-submit-guest')?.addEventListener('click', () => {
    const name  = document.getElementById('ag-name');
    const nic   = document.getElementById('ag-nic');
    let ok = true;
    ok = validateField(name, 'Guest name is required') && ok;
    ok = validateField(nic,  'NIC / Passport is required') && ok;
    if (!ok) { showToast('Please fill in required fields', 'error'); return; }

    const btn = document.getElementById('btn-submit-guest');
    btn.classList.add('btn-loading'); btn.disabled = true;

    setTimeout(() => {
      btn.classList.remove('btn-loading'); btn.disabled = false;
      closeModal('modal-add-guest');
      clearValidation(document.querySelector('#modal-add-guest .modal-body'));
      showToast(`Guest "${name.value}" saved! Connect DB to persist.`, 'success');
      name.value = ''; nic.value = '';
      document.getElementById('ag-email').value = '';
      document.getElementById('ag-phone').value = '';
    }, 700);
  });

  document.getElementById('btn-export-csv')?.addEventListener('click', () => {
    showToast('No guest data to export. Connect database first.', 'info');
  });

  document.getElementById('guest-search')?.addEventListener('input', () => {
    // filter when DB connected
  });
}

// ── ROOM DETAIL PANEL ────────────────────────────────────────
// Called when a room card is clicked (once DB connected).
// Exposed globally so inline onclick attributes can call it.
function showRoomDetailPanel(room) {
  const panel = document.getElementById('room-detail-panel');
  if (!panel) return;

  const statusBadges = {
    VACANT:      'badge-green',
    BOOKED:      'badge-amber',
    OCCUPIED:    'badge-blue',
    DIRTY:       'badge-red',
    MAINTENANCE: 'badge-muted',
  };
  const avatarColors = ['#3b82f6','#8b5cf6','#f59e0b','#10b981','#ef4444'];
  const avatarColor  = avatarColors[Math.abs(room.id.toString().charCodeAt(0)) % 5];

  const initials = name => name?.split(' ').map(n=>n[0]).join('').slice(0,2).toUpperCase() || '?';

  // Build action buttons based on status
  let actionHTML = '';
  if (room.status === 'VACANT') {
    actionHTML = `
      <button class="btn-checkin" onclick="openModal('modal-new-res')">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        New Reservation
      </button>
      <button class="btn-mark-dirty" onclick="handleMarkDirty('${room.id}')">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        Mark dirty
      </button>`;
  } else if (room.status === 'BOOKED') {
    actionHTML = `
      <button class="btn-checkin" onclick="handleCheckin('${room.id}')">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
        Check in
      </button>
      <button class="btn-folio" onclick="handleFolio('${room.id}')">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        View folio
      </button>
      <button class="btn-mark-clean" onclick="handleMarkClean('${room.id}')">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
        Mark clean
      </button>`;
  } else if (room.status === 'OCCUPIED') {
    actionHTML = `
      <button class="btn-folio" onclick="handleFolio('${room.id}')">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        View folio
      </button>
      <button class="btn-checkout" onclick="handleCheckout('${room.id}')">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        Check out
      </button>`;
  } else if (room.status === 'DIRTY') {
    actionHTML = `
      <button class="btn-mark-clean" onclick="handleMarkClean('${room.id}')">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
        Mark clean
      </button>`;
  } else if (room.status === 'MAINTENANCE') {
    actionHTML = `
      <button class="btn-mark-clean" onclick="handleMarkClean('${room.id}')">
        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
        Mark available
      </button>`;
  }

  panel.innerHTML = `
    <div class="rdp-header">
      <div class="rdp-room-number">
        <span class="rdp-num">Room ${room.number}</span>
        <span class="badge ${statusBadges[room.status] || 'badge-muted'}">${room.statusLabel}</span>
      </div>
      <div class="rdp-type-line">${[room.type, `Floor ${room.floor}`, room.bedType, room.view, room.cleanStatus].filter(Boolean).join(' · ')}</div>
    </div>

    ${room.guestName ? `
    <div class="rdp-guest">
      <div class="rdp-guest-avatar" style="background:${avatarColor}">${initials(room.guestName)}</div>
      <div>
        <div class="rdp-guest-name">${room.guestName}</div>
        <div class="rdp-guest-sub">${room.guestOrigin || ''}</div>
      </div>
    </div>` : ''}

    <div class="rdp-info">
      ${room.reservationId ? `<div class="rdp-row"><span class="rdp-row-label">Reservation</span><span class="rdp-row-value" style="color:#2563eb">${room.reservationId}</span></div>` : ''}
      ${room.stay    ? `<div class="rdp-row"><span class="rdp-row-label">Stay</span><span class="rdp-row-value">${room.stay}</span></div>` : ''}
      ${room.guests  ? `<div class="rdp-row"><span class="rdp-row-label">Guests</span><span class="rdp-row-value">${room.guests}</span></div>` : ''}
      ${room.arrival ? `<div class="rdp-row"><span class="rdp-row-label">Arrival</span><span class="rdp-row-value">${room.arrival}</span></div>` : ''}
      ${room.rate    ? `<div class="rdp-row"><span class="rdp-row-label">Rate</span><span class="rdp-row-value">Rs. ${room.rate.toLocaleString('en-LK')} / night</span></div>` : ''}
    </div>

    ${room.roomCharges ? `
    <div class="rdp-charges">
      <div class="rdp-charge-row"><span>Room charges</span><span>Rs. ${room.roomCharges.toLocaleString('en-LK')}</span></div>
      <div class="rdp-charge-row" style="color:#16a34a"><span>Advance paid</span><span>− Rs. ${room.advancePaid.toLocaleString('en-LK')}</span></div>
      <div class="rdp-balance-row"><span>Balance due</span><span style="color:#dc2626">Rs. ${(room.roomCharges - room.advancePaid).toLocaleString('en-LK')}</span></div>
    </div>` : ''}

    <div class="rdp-actions">${actionHTML}</div>`;
}

// ── ACTION STUBS (called from inline onclick) ────────────────
function handleCheckin(roomId) {
  showToast(`Check-in: POST /api/hotel/reservations/{id}/checkin`, 'info');
  console.log('[API STUB] Check in room', roomId);
}
function handleCheckout(roomId) {
  showToast(`Check-out: POST /api/hotel/reservations/{id}/checkout`, 'info');
  console.log('[API STUB] Check out room', roomId);
}
function handleFolio(roomId) {
  showToast(`View folio: GET /api/hotel/reservations/{id}/folio`, 'info');
  console.log('[API STUB] View folio room', roomId);
}
function handleMarkClean(roomId) {
  showToast(`Mark clean: PATCH /api/hotel/rooms/${roomId}/status`, 'success');
  console.log('[API STUB] Mark clean', roomId);
}
function handleMarkDirty(roomId) {
  showToast(`Mark dirty: PATCH /api/hotel/rooms/${roomId}/status`, 'info');
  console.log('[API STUB] Mark dirty', roomId);
}

// ── REPORTS PAGE ─────────────────────────────────────────────
function initReportsPage() {
  // PDF button
  document.getElementById('btn-rpt-pdf')?.addEventListener('click', () => {
    showToast('PDF export requires database connection.', 'info');
  });

  // Reset filters
  document.getElementById('btn-rpt-reset')?.addEventListener('click', () => {
    const today = new Date().toISOString().split('T')[0];
    const from  = new Date(Date.now() - 30*86400000).toISOString().split('T')[0];
    const rptFrom = document.getElementById('rpt-date-from');
    const rptTo   = document.getElementById('rpt-date-to');
    const rptType = document.getElementById('rpt-room-type');
    if (rptFrom) rptFrom.value = from;
    if (rptTo)   rptTo.value   = today;
    if (rptType) rptType.value = 'all';
    showToast('Filters reset', 'success', 1800);
  });

  // Date range change
  ['rpt-date-from','rpt-date-to'].forEach(id => {
    document.getElementById(id)?.addEventListener('change', () => {
      showToast('Date range updated. Data loads when DB is connected.', 'info', 2000);
    });
  });

  // Room type filter
  document.getElementById('rpt-room-type')?.addEventListener('change', e => {
    showToast(`Filtering by: ${e.target.options[e.target.selectedIndex].text}`, 'info', 1800);
  });

  // Render empty chart skeleton (Chart.js — shows grid lines, no data)
  renderOccupancyChart();
}

function renderOccupancyChart() {
  const canvas = document.getElementById('occupancy-chart');
  const emptyEl = document.getElementById('occ-chart-empty');
  if (!canvas) return;

  // Show chart skeleton with empty bars (representing days in a month)
  const days = Array.from({ length: 30 }, (_, i) => {
    const d = new Date(); d.setDate(d.getDate() - 29 + i);
    return d.toLocaleDateString('en-GB', { day:'2-digit', month:'short' });
  });
  // All zeroes — no DB data
  const emptyData = Array(30).fill(0);

  if (emptyEl) emptyEl.style.display = 'none';
  canvas.style.display = 'block';

  new Chart(canvas, {
    type: 'bar',
    data: {
      labels: days,
      datasets: [{
        label: 'Occupancy %',
        data: emptyData,
        backgroundColor: days.map((_, i) => {
          // Weekend days slightly highlighted
          const d = new Date(); d.setDate(d.getDate() - 29 + i);
          const dow = d.getDay();
          return (dow === 0 || dow === 6) ? 'rgba(255,214,0,0.25)' : 'rgba(17,17,24,0.1)';
        }),
        borderColor: days.map((_, i) => {
          const d = new Date(); d.setDate(d.getDate() - 29 + i);
          const dow = d.getDay();
          return (dow === 0 || dow === 6) ? '#ffd600' : '#e8e9ee';
        }),
        borderWidth: 1,
        borderRadius: 4,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: ctx => ctx.raw > 0 ? `${ctx.raw}% occupied` : 'No data — connect database'
          }
        }
      },
      scales: {
        x: {
          grid: { color: '#f0f1f4' },
          ticks: { font: { size: 10, family: 'Inter' }, color: '#9ca3af', maxRotation: 45 }
        },
        y: {
          min: 0, max: 100,
          grid: { color: '#f0f1f4' },
          ticks: { font: { size: 10, family: 'Inter' }, color: '#9ca3af', callback: v => v + '%' }
        }
      }
    }
  });
}

// ── NOTIFICATION BUTTON ──────────────────────────────────────
function initNotifications() {
  document.getElementById('notif-btn')?.addEventListener('click', () => {
    showToast('No new notifications', 'info', 2000);
  });
}

// ── GLOBAL SEARCH ────────────────────────────────────────────
function initGlobalSearch() {
  document.getElementById('global-search')?.addEventListener('keydown', e => {
    if (e.key === 'Enter' && e.target.value.trim()) {
      showToast(`Search for "${e.target.value}" — connect database to see results.`, 'info');
      e.target.value = '';
    }
  });
}

// ── KEYBOARD SHORTCUTS ────────────────────────────────────────
function initKeyboardShortcuts() {
  document.addEventListener('keydown', e => {
    if (['INPUT','TEXTAREA','SELECT'].includes(e.target.tagName)) return;
    if (e.key === 'n' || e.key === 'N') openModal('modal-new-res');
    if (e.key === 'r' || e.key === 'R') openModal('modal-add-room');
  });
}

// ── DASHBOARD PAGE ────────────────────────────────────────────
function initDashboard() {
  // Greeting by time of day
  const greetEl = document.getElementById('db-greeting');
  if (greetEl) {
    const h = new Date().getHours();
    const part = h < 12 ? 'morning' : h < 17 ? 'afternoon' : 'evening';
    greetEl.textContent = `Good ${part}, Dilani`;
  }
  // Date display
  const dateEl = document.getElementById('db-greeting-date');
  if (dateEl) {
    dateEl.textContent = new Date().toLocaleDateString('en-GB', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    }) + ' · ' + new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
  }

  // Branch pills
  document.querySelectorAll('.db-bp').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.db-bp').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      showToast(`Branch: ${btn.textContent.trim()} — loads from DB`, 'info', 1800);
    });
  });

  // Date period pills
  document.querySelectorAll('.db-dp').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.db-dp').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      showToast(`Period: ${btn.textContent.trim()} — loads from DB`, 'info', 1800);
    });
  });

  // Sales — last 7 days chart (skeleton)
  renderSalesChart();

  // Sales by branch chart (skeleton)
  renderBranchChart();
}

function renderSalesChart() {
  const canvas = document.getElementById('db-sales-chart');
  if (!canvas) return;
  const days = [];
  for (let i = 6; i >= 0; i--) {
    const d = new Date(); d.setDate(d.getDate() - i);
    days.push(d.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric' }));
  }
  new Chart(canvas, {
    type: 'line',
    data: {
      labels: days,
      datasets: [{
        label: 'Sales',
        data: Array(7).fill(0),
        borderColor: '#ffd600',
        backgroundColor: 'rgba(255,214,0,0.08)',
        borderWidth: 2.5,
        pointBackgroundColor: '#ffd600',
        pointRadius: 4,
        pointHoverRadius: 6,
        fill: true,
        tension: 0.4,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: ctx => ctx.raw > 0 ? `Rs. ${ctx.raw.toLocaleString('en-LK')}` : 'No data — connect DB'
          }
        }
      },
      scales: {
        x: { grid: { display: false }, ticks: { font: { size: 10, family: 'Inter' }, color: '#9ca3af' } },
        y: {
          min: 0,
          grid: { color: '#f4f5f7' },
          ticks: {
            font: { size: 10, family: 'Inter' }, color: '#9ca3af',
            callback: v => v > 0 ? 'Rs. ' + (v / 1000).toFixed(0) + 'k' : '0'
          }
        }
      }
    }
  });
}

function renderBranchChart() {
  const canvas = document.getElementById('db-branch-chart');
  if (!canvas) return;
  const branches = ['Colombo City', 'Kandy Hill', 'Galle Road'];
  new Chart(canvas, {
    type: 'bar',
    data: {
      labels: branches,
      datasets: [{
        label: 'Sales today',
        data: [0, 0, 0],
        backgroundColor: ['#ffd600', '#111118', '#d1d5db'],
        borderRadius: 6,
        borderSkipped: false,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: ctx => ctx.raw > 0 ? `Rs. ${ctx.raw.toLocaleString('en-LK')}` : 'No data — connect DB'
          }
        }
      },
      scales: {
        x: { grid: { display: false }, ticks: { font: { size: 11, family: 'Inter' }, color: '#6b7280' } },
        y: {
          min: 0,
          grid: { color: '#f4f5f7' },
          ticks: {
            font: { size: 10, family: 'Inter' }, color: '#9ca3af',
            callback: v => v > 0 ? 'Rs. ' + (v / 1000).toFixed(0) + 'k' : '0'
          }
        }
      }
    }
  });
}

// ── BOOTSTRAP ────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  initSidebarNav();
  initSubNav();
  initStatPills();
  initFilterPills();
  initViewToggle();
  initModals();
  initRoomBoardButtons();
  initReservationButtons();
  initCheckinButtons();
  initGuestButtons();
  initReportsPage();
  initDashboard();
  initNotifications();
  initGlobalSearch();
  initKeyboardShortcuts();
  initClock();
  setTodayDate();

  // Welcome toast
  setTimeout(() => showToast('Hotel module loaded · Press N for New Reservation', 'info', 4000), 600);
});

// ── GLOBAL EXPORTS (for inline onclick) ─────────────────────
window.openModal          = openModal;
window.closeModal         = closeModal;
window.showRoomDetailPanel= showRoomDetailPanel;
window.handleCheckin      = handleCheckin;
window.handleCheckout     = handleCheckout;
window.handleFolio        = handleFolio;
window.handleMarkClean    = handleMarkClean;
window.handleMarkDirty    = handleMarkDirty;
window.showToast          = showToast;
