# ATTENDIFY — Smart Attendance Management System

**ATTENDIFY** is a responsive, full-stack, enterprise-grade school and college attendance platform built completely in **Java (Spring Boot 3.3.4 + Java 21)**. It eliminates manual register work, prevents duplicate attendance records, maintains an immutable digital audit ledger, calculates percentages from live database records, and automatically dispatches notifications and SMS alerts to parents when attendance is recorded.

---

## 🌟 Key Features

### 1. Student Portal
- **Dual-OTP Registration**: Requires verification of **both** the student's mobile number and the parent's mobile number via secure 6-digit OTPs before account creation.
- **Secure Password Authentication**: Passwords hashed with BCrypt are required on every sign-in.
- **Attendance Analytics**:
  - Live overall attendance percentage ring.
  - Automatic **Debarment Warning Banner** if attendance falls below the institutional threshold (default **75%**).
  - KPI counters: Total Scheduled Periods, Attended, Absences, and Late marks.
  - Subject-by-subject breakdown with graphical progress bars.
  - Interactive **Chart.js** subject performance chart.
  - Complete digital attendance history ledger with teacher remarks.
- **In-App Notification Center**: Instant updates for session marks and low-attendance warnings.

### 2. Parent / Guardian Portal
- **OTP Mobile Verification**: Verified mobile number used for real-time attendance SMS dispatch.
- **Multi-Child Account Support**: Seamlessly toggle between multiple linked children (e.g. Aarav Sharma and Rohan Sharma) from a single parent account.
- **Link New Children**: Securely submit linking requests by student admission number (auto-approved on matching verified parent phone or routed to administrator approval).
- **Comprehensive Child Views**: View subject breakdowns, attendance trends, absences, and monthly history.
- **Printable Reports**: 1-click printable and downloadable report view.

### 3. Teacher Attendance Studio
- **Authorized Rosters Only**: Teachers are restricted to assigned classes and subjects.
- **Today's Timetable**: Direct 1-click access to scheduled periods.
- **Fast Attendance Entry**:
  - Bulk actions: **Mark All Present**, **Mark All Absent**, **Mark All Late**.
  - Interactive student-by-student toggles with immediate present/absent/late counters.
  - Individual remarks field.
- **Transactional Save & Parent Dispatch**:
  - Validates permissions, checks against holidays, and saves transactionally.
  - Instantly triggers in-app parent alerts and formatted SMS messages:
    > `ATTENDIFY: Attendance update for [Student Name]. Class: [Class]. Subject: [Subject]. Period: [Period]. Status: [Present/Absent/Late]. Date: [Date].`
- **Duplicate Prevention**: Database constraints and server validation guarantee that duplicate sessions cannot be submitted for the same class, subject, date, and period.
- **Record Corrections**: Permitted within a configurable window (default 48 hours) with mandatory reason and an immutable audit trail.

### 4. Administrator Command Center
- **System KPIs**: Real-time counters for Students, Teachers, Classes, Today's Marked Sessions, Overall Attendance %, and Low-Attendance Students.
- **Approvals & Verifications**:
  - Approve pending teacher registrations.
  - Review and approve/reject parent-child link requests.
- **Academic Management**:
  - Create and manage Classes, Divisions, and Rooms.
  - Create and manage Subjects and Course Codes.
  - Assign Teachers to Classes and Subjects.
- **Dynamic Attendance Rules**:
  - Configure minimum attendance percentage threshold (e.g. 75%).
  - Configure late mark multiplier (1.0 = full present, 0.5 = half present, 0.0 = absent).
  - Configure teacher correction window (e.g. 48 hours).
  - Toggle automatic SMS dispatch and in-app notifications.
- **Reports & CSV Export**:
  - Filterable by Class, Start Date, and End Date.
  - Summary metrics and student-by-student attendance breakdown.
  - Instant **Download CSV** button for official record keeping.
- **Audit Logs**: Complete timestamped audit trail of corrections, approvals, and administrative actions.
- **SMS & Delivery Logs**: Transparency logs for in-app, SMS, and carrier responses.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Language & Runtime** | Java 21 (LTS), Maven 3.9.9 |
| **Backend Framework** | Spring Boot 3.3.4 (Spring Web, Spring Security, Spring Data JPA) |
| **Security & Auth** | Spring Security 6, BCrypt Password Encoding, JJWT (HMAC-SHA256) |
| **Database** | H2 file-backed database (zero configuration required out-of-the-box); PostgreSQL ready |
| **Frontend UI** | HTML5, Tailwind CSS (CDN), Lucide Icons, Chart.js |
| **Notifications & SMS** | In-app notification center + Twilio SMS client with Dev Mode simulation |

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- **Java 21** or later installed.
- Maven 3.9+ (or use the configured Maven in `C:\Users\arjun\apache-maven-3.9.9\bin\mvn.cmd`).

### 2. Run the Application
In PowerShell or command prompt:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"
cd C:\Users\arjun\.gemini\antigravity\scratch\attendify
& "C:\Users\arjun\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
```
Or run the packaged fat JAR:
```powershell
java -jar target/attendify-1.0.0.jar
```

### 3. Open in Browser
Navigate to:
```
http://localhost:8080
```
*(The H2 Console is also accessible at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:file:./data/attendifydb`, user `sa`, password `password`).*

---

## 🔑 Pre-Seeded Demo Accounts (1-Click Switcher Available in UI)

The system automatically initializes seed data on first run with rich historical records:

| Role | Name | Email / Identifier | Password | Phone |
|---|---|---|---|---|
| **Administrator** | Dr. Robert Vance | `admin@attendify.edu` | `Admin@123` | `9876543210` |
| **Teacher** | Prof. Sarah Jenkins | `teacher.jenkins@attendify.edu` | `Teacher@123` | `9876543211` |
| **Parent** | Rajesh Sharma | `rajesh.sharma@attendify.edu` | `Parent@123` | `9876543230` |
| **Student** | Aarav Sharma | `aarav.sharma@attendify.edu` | `Student@123` | `9876543220` |

*Note: You can also click the quick buttons in the top demo bar on the website to sign in instantly without typing credentials.*

---

## 📱 Registration & OTP Workflow

1. **Student Registration**:
   - Provide Student Name, Email, Admission No., Class, and Password.
   - Enter **Student Mobile** -> Click **Send OTP** -> Verify Code.
   - Enter **Parent Mobile** -> Click **Send OTP** -> Verify Code.
   - In Development Mode (`attendify.otp.dev-mode=true`), the OTP code appears directly in an animated helper badge in the UI and is logged in the console.
2. **Production Twilio SMS Setup**:
   To enable live production SMS delivery, set the following environment variables:
   ```bash
   TWILIO_ACCOUNT_SID=your_account_sid
   TWILIO_AUTH_TOKEN=your_auth_token
   TWILIO_PHONE_NUMBER=your_twilio_phone_number
   ```

---

## 🗄️ PostgreSQL Production Configuration

To switch from H2 to PostgreSQL:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/attendifydb
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

---

## 🧪 Automated Tests

Run the test suite with:
```powershell
& "C:\Users\arjun\apache-maven-3.9.9\bin\mvn.cmd" test
```
The test suite verifies:
1. Application context loading and database schema generation.
2. OTP generation, expiration, and verification.
3. Attendance duplicate prevention constraint (enforces single session per class, subject, date, and period).
4. Weighted percentage calculations (attended eligible periods / total eligible periods × 100).
5. Class attendance report generation and CSV export structure.
6. Password hashing and JWT login authentication.
