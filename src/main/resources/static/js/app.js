// ATTENDIFY - Smart Attendance Management System
// Frontend Client Application

const API_BASE = '/api';

// Current State
let currentUser = null;
let currentToken = localStorage.getItem('attendify_token');
let currentRoster = [];
let subjectChart = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
  lucide.createIcons();
  loadRegistrationClasses();

  if (currentToken) {
    checkCurrentUser();
  } else {
    showView('guest');
  }
});

// Toast notification helper
function showToast(message, type = 'success') {
  const toast = document.getElementById('toast');
  const inner = document.getElementById('toast-inner');
  const msgEl = document.getElementById('toast-message');
  const icon = document.getElementById('toast-icon');

  msgEl.innerText = message;
  toast.classList.remove('hidden');

  if (type === 'error') {
    inner.className = 'px-4 py-3 rounded-xl shadow-lg border text-sm font-medium flex items-center gap-2.5 bg-red-50 border-red-200 text-red-900';
    icon.className = 'w-5 h-5 text-red-600';
    icon.setAttribute('data-lucide', 'alert-circle');
  } else {
    inner.className = 'px-4 py-3 rounded-xl shadow-lg border text-sm font-medium flex items-center gap-2.5 bg-emerald-50 border-emerald-200 text-emerald-900';
    icon.className = 'w-5 h-5 text-emerald-600';
    icon.setAttribute('data-lucide', 'check-circle');
  }
  lucide.createIcons();

  setTimeout(() => {
    toast.classList.add('hidden');
  }, 4500);
}

// Switch main view
function showView(viewName) {
  const views = ['guest', 'student', 'parent', 'teacher', 'admin'];
  views.forEach(v => {
    const el = document.getElementById(`view-${v}`);
    if (el) el.classList.add('hidden');
  });

  const target = document.getElementById(`view-${viewName}`);
  if (target) target.classList.remove('hidden');

  // Update header buttons
  const authUnauth = document.getElementById('auth-header-unauthenticated');
  const authAuthed = document.getElementById('auth-header-authenticated');
  const notifWrapper = document.getElementById('notif-wrapper');

  if (currentUser) {
    authUnauth.classList.add('hidden');
    authAuthed.classList.remove('hidden');
    notifWrapper.classList.remove('hidden');

    document.getElementById('user-display-name').innerText = currentUser.fullName;
    document.getElementById('user-avatar-initials').innerText = currentUser.fullName.split(' ').map(n=>n[0]).join('').substring(0,2);
    const roleBadge = document.getElementById('user-role-badge');
    roleBadge.innerText = currentUser.role.replace('ROLE_', '');

    if (currentUser.role === 'ROLE_ADMIN') {
      roleBadge.className = 'px-1.5 py-0.2 rounded text-[10px] font-bold bg-amber-100 text-amber-800';
    } else if (currentUser.role === 'ROLE_TEACHER') {
      roleBadge.className = 'px-1.5 py-0.2 rounded text-[10px] font-bold bg-teal-100 text-teal-800';
    } else if (currentUser.role === 'ROLE_PARENT') {
      roleBadge.className = 'px-1.5 py-0.2 rounded text-[10px] font-bold bg-purple-100 text-purple-800';
    } else {
      roleBadge.className = 'px-1.5 py-0.2 rounded text-[10px] font-bold bg-blue-100 text-blue-800';
    }

    loadNotifications();
  } else {
    authUnauth.classList.remove('hidden');
    authAuthed.classList.add('hidden');
    notifWrapper.classList.add('hidden');
  }

  lucide.createIcons();
}

// Fetch authenticated user profile
async function checkCurrentUser() {
  try {
    const res = await fetch(`${API_BASE}/auth/me`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    if (res.ok) {
      const data = await res.json();
      if (data.success && data.data) {
        currentUser = data.data;
        routeByRole();
      } else {
        logout();
      }
    } else {
      logout();
    }
  } catch (e) {
    logout();
  }
}

function routeByRole() {
  if (!currentUser) return;
  if (currentUser.role === 'ROLE_STUDENT') {
    showView('student');
    loadStudentDashboard();
  } else if (currentUser.role === 'ROLE_PARENT') {
    showView('parent');
    loadParentDashboard();
  } else if (currentUser.role === 'ROLE_TEACHER') {
    showView('teacher');
    loadTeacherDashboard();
  } else if (currentUser.role === 'ROLE_ADMIN') {
    showView('admin');
    loadAdminDashboard();
  }
}

// 1-Click Demo Login
async function demoLogin(role) {
  let identifier = '', password = '';
  if (role === 'admin') {
    identifier = 'admin@attendify.edu';
    password = 'Admin@123';
  } else if (role === 'teacher') {
    identifier = 'teacher.jenkins@attendify.edu';
    password = 'Teacher@123';
  } else if (role === 'parent') {
    identifier = 'rajesh.sharma@attendify.edu';
    password = 'Parent@123';
  } else if (role === 'student') {
    identifier = 'aarav.sharma@attendify.edu';
    password = 'Student@123';
  }

  showToast(`Signing in as ${role.toUpperCase()}...`);
  await executeLogin(identifier, password);
}

async function handleLogin(e) {
  e.preventDefault();
  const identifier = document.getElementById('login-identifier').value;
  const password = document.getElementById('login-password').value;
  await executeLogin(identifier, password);
}

async function executeLogin(identifier, password) {
  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ identifier, password })
    });
    const resData = await res.json();
    if (resData.success) {
      currentToken = resData.data.token;
      localStorage.setItem('attendify_token', currentToken);
      currentUser = resData.data;
      showToast(`Welcome back, ${currentUser.fullName}!`);
      routeByRole();
    } else {
      showToast(resData.message || 'Login failed', 'error');
    }
  } catch (err) {
    showToast('Failed to connect to server: ' + err.message, 'error');
  }
}

function logout() {
  currentUser = null;
  currentToken = null;
  localStorage.removeItem('attendify_token');
  showView('guest');
  showToast('Logged out successfully.');
}

// Switch Auth Tabs
function switchAuthTab(tab) {
  ['login', 'register-student', 'register-parent', 'register-teacher'].forEach(t => {
    const form = document.getElementById(`form-${t}`);
    const btn = document.getElementById(`tab-btn-${t}`);
    if (form) form.classList.add('hidden');
    if (btn) {
      btn.className = 'flex-1 py-2.5 rounded-xl transition text-center text-slate-600 hover:text-slate-900';
    }
  });

  const activeForm = document.getElementById(`form-${tab}`);
  const activeBtn = document.getElementById(`tab-btn-${tab}`);
  if (activeForm) activeForm.classList.remove('hidden');
  if (activeBtn) {
    activeBtn.className = 'flex-1 py-2.5 rounded-xl transition text-center bg-white text-slate-900 shadow-sm font-bold';
  }
  lucide.createIcons();
}

function togglePasswordVisibility(fieldId) {
  const input = document.getElementById(fieldId);
  input.type = input.type === 'password' ? 'text' : 'password';
}

// Load classes for registration dropdown
async function loadRegistrationClasses() {
  try {
    const res = await fetch(`${API_BASE}/auth/classes`);
    const data = await res.json();
    if (data.success && data.data) {
      const select = document.getElementById('reg-student-class');
      if (select) {
        select.innerHTML = '<option value="">Select class...</option>' +
          data.data.map(c => `<option value="${c.id}">${c.name} (${c.division})</option>`).join('');
      }
    }
  } catch (e) {
    console.error('Failed to load classes', e);
  }
}

// OTP Helpers
async function sendOtpFor(phoneInputId, purpose, statusElId) {
  const phone = document.getElementById(phoneInputId).value.trim();
  if (!phone) {
    showToast('Please enter a mobile number first', 'error');
    return;
  }
  const statusEl = document.getElementById(statusElId);
  statusEl.classList.remove('hidden');
  statusEl.innerText = 'Sending OTP...';

  try {
    const res = await fetch(`${API_BASE}/auth/send-otp`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phone, purpose })
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message);
      if (data.data && data.data.devOtp) {
        statusEl.innerHTML = `<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-cyan-100 text-cyan-900 font-mono font-bold">
          <i data-lucide="key" class="w-3.5 h-3.5"></i> Dev Mode OTP: ${data.data.devOtp}
        </span> (Valid for 5 mins)`;
      } else {
        statusEl.innerText = 'OTP sent via SMS.';
      }
      lucide.createIcons();
    } else {
      statusEl.innerText = data.message;
      showToast(data.message, 'error');
    }
  } catch (e) {
    statusEl.innerText = 'Failed to send OTP';
    showToast(e.message, 'error');
  }
}

async function verifyOtpFor(phoneInputId, otpInputId, purpose, badgeId) {
  const phone = document.getElementById(phoneInputId).value.trim();
  const code = document.getElementById(otpInputId).value.trim();
  if (!phone || !code) {
    showToast('Enter both mobile and OTP code', 'error');
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/auth/verify-otp`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phone, code, purpose })
    });
    const data = await res.json();
    if (data.success) {
      showToast('OTP verified successfully!');
      const badge = document.getElementById(badgeId);
      if (badge) badge.classList.remove('hidden');
      lucide.createIcons();
    } else {
      showToast(data.message || 'Invalid OTP', 'error');
    }
  } catch (e) {
    showToast(e.message, 'error');
  }
}

// Student Registration
async function handleStudentRegister(e) {
  e.preventDefault();
  const payload = {
    fullName: document.getElementById('reg-student-name').value,
    email: document.getElementById('reg-student-email').value,
    admissionNumber: document.getElementById('reg-student-adm').value,
    classId: document.getElementById('reg-student-class').value,
    phone: document.getElementById('reg-student-phone').value,
    studentOtpCode: document.getElementById('reg-student-otp').value,
    parentPhone: document.getElementById('reg-student-parent-phone').value,
    parentOtpCode: document.getElementById('reg-student-parent-otp').value,
    password: document.getElementById('reg-student-password').value
  };

  try {
    const res = await fetch(`${API_BASE}/auth/register-student`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast('Registration successful! Logging in...');
      currentToken = data.data.token;
      localStorage.setItem('attendify_token', currentToken);
      currentUser = data.data;
      routeByRole();
    } else {
      showToast(data.message || 'Registration failed', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Parent Registration
async function handleParentRegister(e) {
  e.preventDefault();
  const payload = {
    fullName: document.getElementById('reg-parent-name').value,
    email: document.getElementById('reg-parent-email').value,
    phone: document.getElementById('reg-parent-phone').value,
    otpCode: document.getElementById('reg-parent-otp').value,
    relationship: document.getElementById('reg-parent-relationship').value,
    childAdmissionNumber: document.getElementById('reg-parent-child-adm').value,
    password: document.getElementById('reg-parent-password').value
  };

  try {
    const res = await fetch(`${API_BASE}/auth/register-parent`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast('Registration successful! Logging in...');
      currentToken = data.data.token;
      localStorage.setItem('attendify_token', currentToken);
      currentUser = data.data;
      routeByRole();
    } else {
      showToast(data.message || 'Registration failed', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Teacher Registration
async function handleTeacherRegister(e) {
  e.preventDefault();
  const payload = {
    fullName: document.getElementById('reg-teacher-name').value,
    email: document.getElementById('reg-teacher-email').value,
    employeeId: document.getElementById('reg-teacher-empid').value,
    department: document.getElementById('reg-teacher-dept').value,
    phone: document.getElementById('reg-teacher-phone').value,
    password: document.getElementById('reg-teacher-password').value
  };

  try {
    const res = await fetch(`${API_BASE}/auth/register-teacher`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message || 'Registration submitted for administrator approval.');
      switchAuthTab('login');
    } else {
      showToast(data.message || 'Registration failed', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ==========================================
// STUDENT DASHBOARD LOGIC
// ==========================================
async function loadStudentDashboard() {
  try {
    const res = await fetch(`${API_BASE}/student/dashboard`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (!data.success) return;

    const d = data.data;
    document.getElementById('std-dash-name').innerText = d.studentName;
    document.getElementById('std-dash-adm').innerText = d.admissionNumber;
    document.getElementById('std-dash-class').innerText = d.className;
    document.getElementById('std-dash-roll').innerText = d.rollNumber || 'N/A';
    document.getElementById('std-dash-pct').innerText = `${d.overallPercentage}%`;

    const badge = document.getElementById('std-dash-threshold-badge');
    const alertBox = document.getElementById('std-low-attendance-alert');
    if (d.lowAttendance) {
      badge.className = 'text-[10px] font-bold text-red-400';
      badge.innerText = '⚠️ Below Threshold (75%)';
      alertBox.classList.remove('hidden');
    } else {
      badge.className = 'text-[10px] font-bold text-emerald-400';
      badge.innerText = '✓ Above Threshold (75%)';
      alertBox.classList.add('hidden');
    }

    document.getElementById('std-kpi-total').innerText = d.totalPeriods;
    document.getElementById('std-kpi-attended').innerText = d.attendedPeriods;
    document.getElementById('std-kpi-absences').innerText = d.absences;
    document.getElementById('std-kpi-lates').innerText = d.lates;

    // Subject Breakdown Cards
    const subjList = document.getElementById('std-subject-list');
    subjList.innerHTML = d.subjectAttendance.map(s => {
      const color = s.percentage >= 75 ? 'bg-cyan-500' : 'bg-red-500';
      const textColor = s.percentage >= 75 ? 'text-cyan-700' : 'text-red-700';
      return `
        <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200">
          <div class="flex items-center justify-between text-xs mb-1.5 font-bold">
            <span class="text-slate-900">${s.subjectName} (${s.subjectCode})</span>
            <span class="${textColor}">${s.percentage}% (${s.attendedPeriods}/${s.totalPeriods} periods)</span>
          </div>
          <div class="w-full bg-slate-200 h-2 rounded-full overflow-hidden">
            <div class="${color} h-2 rounded-full transition-all" style="width: ${Math.min(100, s.percentage)}%"></div>
          </div>
        </div>
      `;
    }).join('') || '<p class="text-xs text-slate-400">No subject records available.</p>';

    // Chart.js Subject chart
    renderSubjectChart(d.subjectAttendance);

    // Recent records table
    const tbody = document.getElementById('std-history-tbody');
    tbody.innerHTML = d.recentRecords.map(r => {
      const badgeClass = r.status === 'PRESENT' ? 'badge-present' : (r.status === 'ABSENT' ? 'badge-absent' : 'badge-late');
      return `
        <tr class="hover:bg-slate-50 transition">
          <td class="py-3 px-4 font-medium text-slate-900">${r.date}</td>
          <td class="py-3 px-4 text-slate-600 font-bold">P${r.periodNumber}</td>
          <td class="py-3 px-4 text-slate-800 font-semibold">${r.subjectName}</td>
          <td class="py-3 px-4"><span class="px-2 py-0.5 rounded-full text-[10px] font-extrabold ${badgeClass}">${r.status}</span></td>
          <td class="py-3 px-4 text-slate-600">${r.teacherName}</td>
          <td class="py-3 px-4 text-slate-400 italic">${r.remarks || '—'}</td>
        </tr>
      `;
    }).join('') || '<tr><td colspan="6" class="text-center py-6 text-slate-400">No recent records found.</td></tr>';

  } catch (err) {
    console.error('Failed to load student dashboard', err);
  }
}

function renderSubjectChart(subjects) {
  const ctx = document.getElementById('std-subject-chart');
  if (!ctx) return;

  if (subjectChart) subjectChart.destroy();

  const labels = subjects.map(s => s.subjectCode);
  const attendedData = subjects.map(s => s.attendedPeriods);
  const totalData = subjects.map(s => s.totalPeriods);

  subjectChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [
        {
          label: 'Attended',
          data: attendedData,
          backgroundColor: '#00ADB5',
          borderRadius: 6
        },
        {
          label: 'Total',
          data: totalData,
          backgroundColor: '#CBD5E1',
          borderRadius: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 10 } } }
      },
      scales: {
        y: { beginAtZero: true, grid: { color: '#F1F5F9' } },
        x: { grid: { display: false } }
      }
    }
  });
}

// ==========================================
// PARENT DASHBOARD LOGIC
// ==========================================
let parentDashboardData = null;

async function loadParentDashboard(selectedChildId = null) {
  try {
    let url = `${API_BASE}/parent/dashboard`;
    if (selectedChildId) url += `?childId=${selectedChildId}`;

    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (!data.success) return;

    parentDashboardData = data.data;
    document.getElementById('parent-greeting').innerText = `Welcome, ${parentDashboardData.parentName}`;

    // Populate child selector
    const select = document.getElementById('parent-child-select');
    select.innerHTML = parentDashboardData.children.map(c =>
      `<option value="${c.studentId}" ${selectedChildId == c.studentId ? 'selected' : ''}>${c.studentName} (${c.className})</option>`
    ).join('');

    renderParentChildView(parentDashboardData.selectedChildDetail);
  } catch (err) {
    console.error('Failed to load parent dashboard', err);
  }
}

function onParentChildChange() {
  const childId = document.getElementById('parent-child-select').value;
  loadParentDashboard(childId);
}

function renderParentChildView(child) {
  const container = document.getElementById('parent-child-details-container');
  if (!child) {
    container.innerHTML = `
      <div class="bg-white p-8 rounded-3xl border border-slate-200 text-center">
        <p class="text-slate-500 text-sm">No child linked yet. Click "Link Child" to connect your child's student admission number.</p>
      </div>
    `;
    return;
  }

  const alertBanner = child.lowAttendance ? `
    <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-xl text-red-900 text-xs flex items-center gap-3">
      <i data-lucide="alert-triangle" class="w-5 h-5 text-red-600 shrink-0"></i>
      <div>
        <strong>Debarment Notice:</strong> ${child.studentName}'s attendance is currently <strong>${child.overallPercentage}%</strong>, which is below the minimum required 75%.
      </div>
    </div>
  ` : '';

  container.innerHTML = `
    ${alertBanner}

    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
        <span class="text-xs text-purple-700 font-bold uppercase">Attendance %</span>
        <div class="text-3xl font-black text-slate-900 mt-1">${child.overallPercentage}%</div>
      </div>
      <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
        <span class="text-xs text-slate-500 font-bold uppercase">Total Periods</span>
        <div class="text-3xl font-black text-slate-900 mt-1">${child.totalPeriods}</div>
      </div>
      <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
        <span class="text-xs text-emerald-600 font-bold uppercase">Attended</span>
        <div class="text-3xl font-black text-emerald-600 mt-1">${child.attendedPeriods}</div>
      </div>
      <div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
        <span class="text-xs text-red-600 font-bold uppercase">Absences</span>
        <div class="text-3xl font-black text-red-600 mt-1">${child.absences}</div>
      </div>
    </div>

    <!-- Subject list -->
    <div class="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm">
      <h3 class="font-bold text-slate-900 text-base mb-4 flex items-center gap-2">
        <i data-lucide="book-open" class="w-5 h-5 text-purple-600"></i> Subject Performance for ${child.studentName}
      </h3>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
        ${child.subjectAttendance.map(s => `
          <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 text-xs">
            <div class="flex justify-between font-bold mb-1">
              <span>${s.subjectName} (${s.subjectCode})</span>
              <span class="${s.percentage >= 75 ? 'text-emerald-700' : 'text-red-700'}">${s.percentage}%</span>
            </div>
            <div class="w-full bg-slate-200 h-2 rounded-full overflow-hidden">
              <div class="${s.percentage >= 75 ? 'bg-purple-600' : 'bg-red-500'} h-2 rounded-full" style="width: ${Math.min(100, s.percentage)}%"></div>
            </div>
            <div class="flex justify-between text-[11px] text-slate-400 mt-1">
              <span>Attended: ${s.attendedPeriods}/${s.totalPeriods}</span>
              <span>Absences: ${s.absences}</span>
            </div>
          </div>
        `).join('') || '<p class="text-xs text-slate-400">No subject breakdown available.</p>'}
      </div>
    </div>

    <!-- Attendance Record Timeline -->
    <div id="printable-area" class="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100 flex items-center justify-between">
        <div>
          <h3 class="font-bold text-slate-900 text-base">Verified Attendance Records for ${child.studentName}</h3>
          <p class="text-xs text-slate-500">Class: ${child.className} | Admission No: ${child.admissionNumber}</p>
        </div>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-left text-xs">
          <thead class="bg-slate-50 text-slate-500 font-bold uppercase tracking-wider border-b border-slate-200">
            <tr>
              <th class="py-3 px-4">Date</th>
              <th class="py-3 px-4">Period</th>
              <th class="py-3 px-4">Subject</th>
              <th class="py-3 px-4">Status</th>
              <th class="py-3 px-4">Teacher</th>
              <th class="py-3 px-4">Remarks</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            ${child.recentRecords.map(r => `
              <tr class="hover:bg-slate-50">
                <td class="py-3 px-4 font-medium text-slate-900">${r.date}</td>
                <td class="py-3 px-4 font-bold">P${r.periodNumber}</td>
                <td class="py-3 px-4 font-semibold">${r.subjectName}</td>
                <td class="py-3 px-4">
                  <span class="px-2 py-0.5 rounded-full text-[10px] font-extrabold ${r.status === 'PRESENT' ? 'badge-present' : (r.status === 'ABSENT' ? 'badge-absent' : 'badge-late')}">
                    ${r.status}
                  </span>
                </td>
                <td class="py-3 px-4 text-slate-600">${r.teacherName}</td>
                <td class="py-3 px-4 text-slate-400 italic">${r.remarks || '—'}</td>
              </tr>
            `).join('') || '<tr><td colspan="6" class="text-center py-6 text-slate-400">No records found.</td></tr>'}
          </tbody>
        </table>
      </div>
    </div>
  `;
  lucide.createIcons();
}

function showLinkChildModal() {
  document.getElementById('modal-link-child').classList.remove('hidden');
  lucide.createIcons();
}

function closeLinkChildModal() {
  document.getElementById('modal-link-child').classList.add('hidden');
}

async function handleLinkChildSubmit(e) {
  e.preventDefault();
  const adm = document.getElementById('link-child-adm').value.trim();
  try {
    const res = await fetch(`${API_BASE}/parent/link-child?admissionNumber=${encodeURIComponent(adm)}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message);
      closeLinkChildModal();
      loadParentDashboard();
    } else {
      showToast(data.message || 'Linking failed', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ==========================================
// TEACHER DASHBOARD LOGIC
// ==========================================
let teacherDashboardData = null;

async function loadTeacherDashboard() {
  try {
    const res = await fetch(`${API_BASE}/teacher/dashboard`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (!data.success) return;

    teacherDashboardData = data.data;
    document.getElementById('teacher-dash-name').innerText = teacherDashboardData.teacherName;
    document.getElementById('teacher-dash-empid').innerText = teacherDashboardData.employeeId;
    document.getElementById('teacher-dash-dept').innerText = teacherDashboardData.department;

    // Populate today's timetable slots
    const ttList = document.getElementById('teacher-timetable-list');
    ttList.innerHTML = teacherDashboardData.todayTimetable.map(t => `
      <div class="p-4 rounded-2xl border ${t.alreadyMarked ? 'bg-slate-50 border-slate-200' : 'bg-teal-50/50 border-teal-200'} flex flex-col justify-between">
        <div>
          <div class="flex items-center justify-between text-xs font-bold mb-1">
            <span class="text-slate-900">Period ${t.periodNumber}</span>
            <span class="text-slate-500">${t.startTime}</span>
          </div>
          <div class="font-bold text-sm text-slate-900">${t.subjectName}</div>
          <div class="text-xs text-slate-500">${t.className}</div>
        </div>
        <div class="mt-4">
          ${t.alreadyMarked ?
            '<span class="inline-flex items-center gap-1 text-[11px] font-bold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-full"><i data-lucide="check" class="w-3 h-3"></i> Attendance Submitted</span>' :
            `<button onclick="prepareQuickMark(${t.classId}, ${t.subjectId}, ${t.periodNumber})" class="w-full py-1.5 rounded-xl bg-teal-600 text-white font-bold text-xs hover:bg-teal-700 transition">Mark Attendance</button>`
          }
        </div>
      </div>
    `).join('') || '<p class="text-xs text-slate-400 py-4 col-span-4">No scheduled periods for today.</p>';

    // Populate assigned classes in Studio dropdown
    const classSelect = document.getElementById('mark-class-select');
    classSelect.innerHTML = teacherDashboardData.assignedClasses.map(c =>
      `<option value="${c.classId}" data-subject-id="${c.subjectId}" data-subject-name="${c.subjectName}">${c.className} (${c.division})</option>`
    ).join('');

    onMarkClassChange();

    // Default today's date
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('mark-date').value = today;

    // Render recent sessions
    renderTeacherSessions(teacherDashboardData.recentSessions);

    lucide.createIcons();
  } catch (err) {
    console.error('Failed to load teacher dashboard', err);
  }
}

function onMarkClassChange() {
  const classSelect = document.getElementById('mark-class-select');
  const selectedOption = classSelect.options[classSelect.selectedIndex];
  if (!selectedOption) return;

  const subjId = selectedOption.getAttribute('data-subject-id');
  const subjName = selectedOption.getAttribute('data-subject-name');

  const subjectSelect = document.getElementById('mark-subject-select');
  subjectSelect.innerHTML = `<option value="${subjId}">${subjName}</option>`;
}

function prepareQuickMark(classId, subjectId, periodNumber) {
  document.getElementById('mark-class-select').value = classId;
  onMarkClassChange();
  document.getElementById('mark-subject-select').value = subjectId;
  document.getElementById('mark-period').value = periodNumber;
  document.getElementById('mark-date').value = new Date().toISOString().split('T')[0];
  loadRoster();
  scrollToAttendanceStudio();
}

function scrollToAttendanceStudio() {
  document.getElementById('attendance-marking-studio').scrollIntoView({ behavior: 'smooth' });
}

// Load Class Roster for Attendance Marking
async function loadRoster() {
  const classId = document.getElementById('mark-class-select').value;
  if (!classId) {
    showToast('Please select a class first', 'error');
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/attendance/roster?classId=${classId}`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (data.success && data.data) {
      currentRoster = data.data.map(item => ({
        ...item,
        status: 'PRESENT',
        remarks: ''
      }));
      renderRosterTable();
      showToast(`Loaded ${currentRoster.length} students in roster.`);
    } else {
      showToast(data.message || 'Failed to load roster', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function renderRosterTable() {
  const tbody = document.getElementById('roster-tbody');
  if (currentRoster.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-6 text-slate-400">No students enrolled in this class.</td></tr>';
    updateRosterCounts();
    return;
  }

  tbody.innerHTML = currentRoster.map((s, idx) => `
    <tr class="hover:bg-slate-50 transition">
      <td class="py-3 px-4 font-bold text-slate-600">${s.rollNumber || (idx + 1)}</td>
      <td class="py-3 px-4 font-mono text-slate-500">${s.admissionNumber}</td>
      <td class="py-3 px-4 font-bold text-slate-900">${s.studentName}</td>
      <td class="py-3 px-4 text-slate-500">${s.parentPhone || '—'}</td>
      <td class="py-3 px-4 text-center">
        <div class="inline-flex rounded-xl p-1 bg-slate-100 gap-1">
          <button type="button" onclick="setStudentStatus(${idx}, 'PRESENT')"
                  class="px-2.5 py-1 rounded-lg font-bold text-xs transition ${s.status === 'PRESENT' ? 'bg-emerald-600 text-white shadow-sm' : 'text-slate-600 hover:text-slate-900'}">
            Present
          </button>
          <button type="button" onclick="setStudentStatus(${idx}, 'ABSENT')"
                  class="px-2.5 py-1 rounded-lg font-bold text-xs transition ${s.status === 'ABSENT' ? 'bg-red-600 text-white shadow-sm' : 'text-slate-600 hover:text-slate-900'}">
            Absent
          </button>
          <button type="button" onclick="setStudentStatus(${idx}, 'LATE')"
                  class="px-2.5 py-1 rounded-lg font-bold text-xs transition ${s.status === 'LATE' ? 'bg-amber-500 text-white shadow-sm' : 'text-slate-600 hover:text-slate-900'}">
            Late
          </button>
        </div>
      </td>
      <td class="py-3 px-4">
        <input type="text" value="${s.remarks || ''}" onchange="setStudentRemark(${idx}, this.value)"
               placeholder="Optional note" class="w-full px-2 py-1 rounded-lg border border-slate-200 text-xs">
      </td>
    </tr>
  `).join('');

  updateRosterCounts();
  lucide.createIcons();
}

function setStudentStatus(idx, status) {
  if (currentRoster[idx]) {
    currentRoster[idx].status = status;
    renderRosterTable();
  }
}

function setStudentRemark(idx, remark) {
  if (currentRoster[idx]) {
    currentRoster[idx].remarks = remark;
  }
}

function bulkMark(status) {
  currentRoster.forEach(s => s.status = status);
  renderRosterTable();
}

function updateRosterCounts() {
  const total = currentRoster.length;
  const p = currentRoster.filter(s => s.status === 'PRESENT').length;
  const a = currentRoster.filter(s => s.status === 'ABSENT').length;
  const l = currentRoster.filter(s => s.status === 'LATE').length;

  document.getElementById('roster-total-count').innerText = total;
  document.getElementById('roster-present-count').innerText = p;
  document.getElementById('roster-absent-count').innerText = a;
  document.getElementById('roster-late-count').innerText = l;
}

// Submit Attendance
async function submitAttendance() {
  if (currentRoster.length === 0) {
    showToast('Please load class roster before submitting', 'error');
    return;
  }

  const classId = document.getElementById('mark-class-select').value;
  const subjectId = document.getElementById('mark-subject-select').value;
  const date = document.getElementById('mark-date').value;
  const periodNumber = parseInt(document.getElementById('mark-period').value);
  const notes = document.getElementById('mark-notes').value;

  const records = currentRoster.map(s => ({
    studentId: s.studentId,
    status: s.status,
    remarks: s.remarks
  }));

  const payload = {
    classId: parseInt(classId),
    subjectId: parseInt(subjectId),
    date: date,
    periodNumber: periodNumber,
    records: records,
    notes: notes
  };

  const btn = document.getElementById('btn-submit-attendance');
  btn.disabled = true;
  btn.innerText = 'Submitting & Notifying...';

  try {
    const res = await fetch(`${API_BASE}/attendance/mark`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${currentToken}`
      },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message || 'Attendance saved and parents notified!');
      loadTeacherDashboard();
    } else {
      showToast(data.message || 'Failed to submit attendance', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<i data-lucide="send" class="w-4 h-4"></i> Submit Attendance';
    lucide.createIcons();
  }
}

function renderTeacherSessions(sessions) {
  const tbody = document.getElementById('teacher-sessions-tbody');
  tbody.innerHTML = sessions.map(s => `
    <tr class="hover:bg-slate-50">
      <td class="py-3 px-4 font-medium text-slate-900">${s.date}</td>
      <td class="py-3 px-4 font-bold">${s.className}</td>
      <td class="py-3 px-4">${s.subjectName}</td>
      <td class="py-3 px-4 font-bold">P${s.periodNumber}</td>
      <td class="py-3 px-4 text-center">
        <span class="text-emerald-700 font-bold">${s.presentCount}P</span> /
        <span class="text-red-700 font-bold">${s.absentCount}A</span> /
        <span class="text-amber-700 font-bold">${s.lateCount}L</span>
      </td>
      <td class="py-3 px-4 font-extrabold ${s.attendancePercentage >= 75 ? 'text-emerald-700' : 'text-red-700'}">${s.attendancePercentage}%</td>
      <td class="py-3 px-4 text-right">
        <button onclick="viewSessionForCorrection(${s.sessionId})" class="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-800 text-xs font-semibold">
          View / Correct
        </button>
      </td>
    </tr>
  `).join('') || '<tr><td colspan="7" class="text-center py-6 text-slate-400">No sessions recorded yet.</td></tr>';
}

async function viewSessionForCorrection(sessionId) {
  try {
    const res = await fetch(`${API_BASE}/attendance/session/${sessionId}/records`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (data.success && data.data && data.data.length > 0) {
      // Pick first record to show modal, or let teacher choose
      const rec = data.data[0];
      openCorrectionModal(rec.recordId, rec.studentName, rec.status);
    }
  } catch (e) {
    showToast(e.message, 'error');
  }
}

function openCorrectionModal(recordId, studentName, currentStatus) {
  document.getElementById('correct-record-id').value = recordId;
  document.getElementById('correct-student-name').value = studentName;
  document.getElementById('correct-new-status').value = currentStatus;
  document.getElementById('correct-reason').value = '';
  document.getElementById('modal-correction').classList.remove('hidden');
  lucide.createIcons();
}

function closeCorrectionModal() {
  document.getElementById('modal-correction').classList.add('hidden');
}

async function handleRecordCorrection(e) {
  e.preventDefault();
  const recordId = document.getElementById('correct-record-id').value;
  const newStatus = document.getElementById('correct-new-status').value;
  const reason = document.getElementById('correct-reason').value.trim();

  try {
    const res = await fetch(`${API_BASE}/attendance/correct`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${currentToken}`
      },
      body: JSON.stringify({
        recordId: parseInt(recordId),
        newStatus: newStatus,
        reason: reason
      })
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message);
      closeCorrectionModal();
      loadTeacherDashboard();
    } else {
      showToast(data.message || 'Correction failed', 'error');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ==========================================
// ADMINISTRATOR DASHBOARD LOGIC
// ==========================================
async function loadAdminDashboard() {
  try {
    const res = await fetch(`${API_BASE}/admin/dashboard`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (!data.success) return;

    const d = data.data;
    document.getElementById('adm-kpi-students').innerText = d.totalStudents;
    document.getElementById('adm-kpi-teachers').innerText = d.totalTeachers;
    document.getElementById('adm-kpi-classes').innerText = d.totalClasses;
    document.getElementById('adm-kpi-sessions-today').innerText = d.todayMarkedSessions;
    document.getElementById('adm-kpi-overall-pct').innerText = `${d.institutionAttendancePercentage}%`;
    document.getElementById('adm-kpi-low-count').innerText = d.lowAttendanceCount;

    // Load initial tab data
    loadPendingApprovals();
    loadAdminClassesAndSubjects();
    loadAdminRules();
    loadAuditLogs();
    loadDeliveryLogs();
  } catch (err) {
    console.error('Failed to load admin dashboard', err);
  }
}

function switchAdminTab(tab) {
  ['pending', 'classes', 'assign', 'reports', 'rules', 'audit', 'delivery'].forEach(t => {
    const panel = document.getElementById(`adm-panel-${t}`);
    const btn = document.getElementById(`adm-tab-${t}`);
    if (panel) panel.classList.add('hidden');
    if (btn) btn.className = 'px-4 py-2 rounded-xl text-slate-600 hover:text-slate-900';
  });

  const activePanel = document.getElementById(`adm-panel-${tab}`);
  const activeBtn = document.getElementById(`adm-tab-${tab}`);
  if (activePanel) activePanel.classList.remove('hidden');
  if (activeBtn) activeBtn.className = 'px-4 py-2 rounded-xl bg-slate-900 text-white font-bold';

  lucide.createIcons();
}

async function loadPendingApprovals() {
  try {
    // Teachers
    const tRes = await fetch(`${API_BASE}/admin/pending-teachers`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const tData = await tRes.json();
    const tList = document.getElementById('pending-teachers-list');
    if (tData.success && tData.data && tData.data.length > 0) {
      tList.innerHTML = tData.data.map(u => `
        <div class="py-3 flex items-center justify-between">
          <div>
            <div class="font-bold text-slate-900">${u.fullName}</div>
            <div class="text-[11px] text-slate-500">${u.email} • ${u.phone}</div>
          </div>
          <button onclick="approveTeacher(${u.id})" class="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs transition">
            Approve Account
          </button>
        </div>
      `).join('');
    } else {
      tList.innerHTML = '<p class="text-slate-400 py-4 text-center">No pending teacher applications.</p>';
    }

    // Links
    const lRes = await fetch(`${API_BASE}/admin/pending-links`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const lData = await lRes.json();
    const lList = document.getElementById('pending-links-list');
    if (lData.success && lData.data && lData.data.length > 0) {
      lList.innerHTML = lData.data.map(l => `
        <div class="py-3 flex items-center justify-between">
          <div>
            <div class="font-bold text-slate-900">Parent: ${l.parent.fullName} ↔ Student: ${l.student.fullName}</div>
            <div class="text-[11px] text-slate-500">Parent Phone: ${l.parent.phone} | Requested: ${l.requestedAt}</div>
          </div>
          <div class="flex gap-1.5">
            <button onclick="updateLinkStatus(${l.id}, 'APPROVED')" class="px-2.5 py-1 rounded-lg bg-emerald-600 text-white font-bold text-xs">
              Approve
            </button>
            <button onclick="updateLinkStatus(${l.id}, 'REJECTED')" class="px-2.5 py-1 rounded-lg bg-red-600 text-white font-bold text-xs">
              Reject
            </button>
          </div>
        </div>
      `).join('');
    } else {
      lList.innerHTML = '<p class="text-slate-400 py-4 text-center">No pending parent link requests.</p>';
    }
    lucide.createIcons();
  } catch (e) {
    console.error(e);
  }
}

async function approveTeacher(id) {
  try {
    const res = await fetch(`${API_BASE}/admin/teachers/${id}/approve`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message);
      loadPendingApprovals();
    }
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function updateLinkStatus(linkId, status) {
  try {
    const res = await fetch(`${API_BASE}/admin/links/${linkId}/status?status=${status}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message);
      loadPendingApprovals();
    }
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function loadAdminClassesAndSubjects() {
  try {
    const cRes = await fetch(`${API_BASE}/admin/classes`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const cData = await cRes.json();
    if (cData.success && cData.data) {
      const el = document.getElementById('adm-classes-list');
      el.innerHTML = cData.data.map(c => `
        <div class="px-2.5 py-1.5 rounded-lg bg-slate-50 border border-slate-200 flex justify-between">
          <span class="font-bold text-slate-800">${c.name} (${c.division})</span>
          <span class="text-slate-500">${c.roomNumber || ''}</span>
        </div>
      `).join('');

      // Populate selects
      ['assign-class-select', 'report-class-select'].forEach(selId => {
        const sel = document.getElementById(selId);
        if (sel) {
          sel.innerHTML = cData.data.map(c => `<option value="${c.id}">${c.name} (${c.division})</option>`).join('');
        }
      });
    }

    const sRes = await fetch(`${API_BASE}/admin/subjects`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const sData = await sRes.json();
    if (sData.success && sData.data) {
      const el = document.getElementById('adm-subjects-list');
      el.innerHTML = sData.data.map(s => `
        <div class="px-2.5 py-1.5 rounded-lg bg-slate-50 border border-slate-200 flex justify-between">
          <span class="font-bold text-slate-800">${s.name}</span>
          <span class="font-mono text-cyan-700">${s.code}</span>
        </div>
      `).join('');

      const sel = document.getElementById('assign-subject-select');
      if (sel) {
        sel.innerHTML = sData.data.map(s => `<option value="${s.id}">${s.name} (${s.code})</option>`).join('');
      }
    }

    // Load teachers for assign select
    const tRes = await fetch(`${API_BASE}/admin/teachers`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const tData = await tRes.json();
    if (tData.success && tData.data) {
      const sel = document.getElementById('assign-teacher-select');
      if (sel) {
        sel.innerHTML = tData.data.map(t => `<option value="${t.user.id}">${t.user.fullName} (${t.department})</option>`).join('');
      }
    }
  } catch (e) {
    console.error(e);
  }
}

async function handleCreateClass(e) {
  e.preventDefault();
  const payload = {
    name: document.getElementById('new-class-name').value,
    division: document.getElementById('new-class-div').value,
    roomNumber: document.getElementById('new-class-room').value
  };
  try {
    const res = await fetch(`${API_BASE}/admin/classes`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${currentToken}`
      },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast('Class created successfully!');
      loadAdminClassesAndSubjects();
      e.target.reset();
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleCreateSubject(e) {
  e.preventDefault();
  const payload = {
    name: document.getElementById('new-subj-name').value,
    code: document.getElementById('new-subj-code').value,
    description: document.getElementById('new-subj-desc').value
  };
  try {
    const res = await fetch(`${API_BASE}/admin/subjects`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${currentToken}`
      },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast('Subject created successfully!');
      loadAdminClassesAndSubjects();
      e.target.reset();
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleAssignTeacher(e) {
  e.preventDefault();
  const teacherId = document.getElementById('assign-teacher-select').value;
  const classId = document.getElementById('assign-class-select').value;
  const subjectId = document.getElementById('assign-subject-select').value;

  try {
    const res = await fetch(`${API_BASE}/admin/assign-teacher?teacherId=${teacherId}&classId=${classId}&subjectId=${subjectId}`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message || 'Teacher assigned successfully!');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function loadAdminRules() {
  try {
    const res = await fetch(`${API_BASE}/admin/rules`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (data.success && data.data) {
      const r = data.data;
      document.getElementById('rule-min-pct').value = r.minimumAttendancePercentage;
      document.getElementById('rule-late-mult').value = r.lateMultiplier;
      document.getElementById('rule-window-hours').value = r.correctionWindowHours;
      document.getElementById('rule-auto-notify').checked = r.autoNotifyParents;
      document.getElementById('rule-auto-sms').checked = r.autoSmsEnabled;
    }
  } catch (e) {
    console.error(e);
  }
}

async function handleSaveRules(e) {
  e.preventDefault();
  const payload = {
    minimumAttendancePercentage: parseFloat(document.getElementById('rule-min-pct').value),
    lateMultiplier: parseFloat(document.getElementById('rule-late-mult').value),
    correctionWindowHours: parseInt(document.getElementById('rule-window-hours').value),
    autoNotifyParents: document.getElementById('rule-auto-notify').checked,
    autoSmsEnabled: document.getElementById('rule-auto-sms').checked
  };

  try {
    const res = await fetch(`${API_BASE}/admin/rules`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${currentToken}`
      },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast('Attendance rules updated successfully!');
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function loadClassReport() {
  const classId = document.getElementById('report-class-select').value;
  const start = document.getElementById('report-start-date').value;
  const end = document.getElementById('report-end-date').value;

  let url = `${API_BASE}/reports/class?classId=${classId}`;
  if (start) url += `&startDate=${start}`;
  if (end) url += `&endDate=${end}`;

  try {
    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (!data.success) return;

    const r = data.data;
    const container = document.getElementById('report-result-container');
    container.innerHTML = `
      <div class="flex items-center justify-between p-4 bg-slate-50 rounded-2xl border border-slate-200 text-xs">
        <div>
          <span class="font-bold text-slate-900">${r.className}</span> •
          <span>${r.startDate} to ${r.endDate}</span>
        </div>
        <div class="flex items-center gap-4 font-bold">
          <span>Sessions: ${r.totalSessions}</span>
          <span class="text-cyan-700">Average: ${r.averageAttendance}%</span>
        </div>
      </div>

      <div class="overflow-x-auto rounded-2xl border border-slate-200">
        <table class="w-full text-left text-xs">
          <thead class="bg-slate-50 text-slate-500 font-bold uppercase tracking-wider border-b border-slate-200">
            <tr>
              <th class="py-3 px-4">Roll</th>
              <th class="py-3 px-4">Admission No</th>
              <th class="py-3 px-4">Student Name</th>
              <th class="py-3 px-4">Parent Phone</th>
              <th class="py-3 px-4 text-center">Attended</th>
              <th class="py-3 px-4 text-center">Absences</th>
              <th class="py-3 px-4 text-center">Late</th>
              <th class="py-3 px-4 font-black">Attendance %</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            ${r.students.map(s => `
              <tr class="hover:bg-slate-50 ${s.belowThreshold ? 'bg-red-50/50' : ''}">
                <td class="py-3 px-4 font-bold text-slate-700">${s.rollNumber || '—'}</td>
                <td class="py-3 px-4 font-mono text-slate-500">${s.admissionNumber}</td>
                <td class="py-3 px-4 font-bold text-slate-900 flex items-center gap-1.5">
                  ${s.studentName}
                  ${s.belowThreshold ? '<span class="px-1.5 py-0.5 rounded text-[9px] font-black bg-red-100 text-red-800">LOW</span>' : ''}
                </td>
                <td class="py-3 px-4 text-slate-600">${s.parentPhone}</td>
                <td class="py-3 px-4 text-center font-bold text-emerald-700">${s.presentCount}</td>
                <td class="py-3 px-4 text-center font-bold text-red-700">${s.absentCount}</td>
                <td class="py-3 px-4 text-center font-bold text-amber-700">${s.lateCount}</td>
                <td class="py-3 px-4 font-black ${s.belowThreshold ? 'text-red-600' : 'text-emerald-600'}">${s.percentage}%</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
    lucide.createIcons();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

function downloadReportCsv() {
  const classId = document.getElementById('report-class-select').value;
  const start = document.getElementById('report-start-date').value;
  const end = document.getElementById('report-end-date').value;

  let url = `${API_BASE}/reports/class/csv?classId=${classId}`;
  if (start) url += `&startDate=${start}`;
  if (end) url += `&endDate=${end}`;

  window.open(url, '_blank');
}

async function loadAuditLogs() {
  try {
    const res = await fetch(`${API_BASE}/admin/audit-logs`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (!data.success) return;

    const tbody = document.getElementById('adm-audit-tbody');
    tbody.innerHTML = data.data.map(a => `
      <tr class="hover:bg-slate-50">
        <td class="py-3 px-4 font-mono text-slate-500">${a.timestamp.substring(0, 19).replace('T', ' ')}</td>
        <td class="py-3 px-4 font-bold text-slate-800">${a.performedBy ? a.performedBy.fullName : 'System'}</td>
        <td class="py-3 px-4"><span class="px-2 py-0.5 rounded font-mono font-bold bg-slate-100 text-slate-800">${a.action}</span></td>
        <td class="py-3 px-4 text-slate-600">${a.entityName}</td>
        <td class="py-3 px-4 text-slate-600">${a.details || ''}</td>
      </tr>
    `).join('') || '<tr><td colspan="5" class="text-center py-6 text-slate-400">No audit logs recorded yet.</td></tr>';
  } catch (e) {
    console.error(e);
  }
}

async function loadDeliveryLogs() {
  try {
    const res = await fetch(`${API_BASE}/admin/delivery-logs`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (!data.success) return;

    const tbody = document.getElementById('adm-delivery-tbody');
    tbody.innerHTML = data.data.map(l => {
      const statusBadge = l.status === 'DELIVERED' ? 'bg-emerald-100 text-emerald-800' :
        (l.status === 'SIMULATED' ? 'bg-cyan-100 text-cyan-800' : 'bg-red-100 text-red-800');
      return `
        <tr class="hover:bg-slate-50">
          <td class="py-3 px-4 font-mono text-slate-500">${l.attemptedAt.substring(0, 19).replace('T', ' ')}</td>
          <td class="py-3 px-4 font-mono text-slate-800 font-bold">${l.recipientPhone || 'In-App'}</td>
          <td class="py-3 px-4 font-bold">${l.channel}</td>
          <td class="py-3 px-4"><span class="px-2 py-0.5 rounded-full text-[10px] font-black ${statusBadge}">${l.status}</span></td>
          <td class="py-3 px-4 text-slate-700 max-w-xs truncate" title="${l.notification ? l.notification.message : ''}">${l.notification ? l.notification.message : ''}</td>
          <td class="py-3 px-4 text-slate-500">${l.providerResponse || ''}</td>
        </tr>
      `;
    }).join('') || '<tr><td colspan="6" class="text-center py-6 text-slate-400">No delivery logs recorded yet.</td></tr>';
  } catch (e) {
    console.error(e);
  }
}

// ==========================================
// NOTIFICATIONS LOGIC
// ==========================================
function toggleNotifDropdown() {
  const dd = document.getElementById('notif-dropdown');
  dd.classList.toggle('hidden');
}

async function loadNotifications() {
  try {
    const res = await fetch(`${API_BASE}/notifications`, {
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const data = await res.json();
    if (!data.success) return;

    const notifs = data.data || [];
    const unread = notifs.filter(n => !n.read).length;

    const badge = document.getElementById('notif-badge');
    const countText = document.getElementById('notif-count-text');
    if (unread > 0) {
      badge.classList.remove('hidden');
      countText.innerText = `${unread} unread`;
    } else {
      badge.classList.add('hidden');
      countText.innerText = '0 unread';
    }

    const list = document.getElementById('notif-list');
    if (notifs.length === 0) {
      list.innerHTML = '<p class="text-center py-6 text-slate-400">No notifications yet.</p>';
      return;
    }

    list.innerHTML = notifs.map(n => `
      <div class="py-2.5 px-1 flex flex-col gap-1 ${n.read ? 'opacity-70' : 'bg-cyan-50/50 p-2 rounded-xl'}">
        <div class="flex items-center justify-between">
          <span class="font-bold text-slate-900">${n.title}</span>
          <span class="text-[10px] text-slate-400 font-mono">${n.createdAt.substring(11, 16)}</span>
        </div>
        <p class="text-slate-600 leading-snug">${n.message}</p>
        ${!n.read ? `<button onclick="markNotificationRead(${n.id})" class="text-[10px] text-cyan-600 hover:underline text-left mt-0.5">Mark as read</button>` : ''}
      </div>
    `).join('');
  } catch (e) {
    console.error(e);
  }
}

async function markNotificationRead(id) {
  try {
    await fetch(`${API_BASE}/notifications/${id}/read`, {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    loadNotifications();
  } catch (e) {
    console.error(e);
  }
}
