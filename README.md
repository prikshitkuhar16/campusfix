# CampusFix 🏫🔧

CampusFix is a **multi-tenant maintenance management system** designed for educational institutions to streamline complaint handling and facility maintenance.

---

## 🚀 Overview

CampusFix allows students to raise maintenance complaints and enables staff/admins to manage, assign, and resolve them efficiently.

The system supports **multiple campuses** with strict data isolation and role-based access control.

---

## 🏗️ Architecture

```text
Android App (Jetpack Compose)
        ↓
Spring Boot Backend (REST API)
        ↓
PostgreSQL Database (Neon)
        ↓
Firebase Authentication (JWT)
```

---

## 👥 User Roles

* **Student** – Raise and track complaints
* **Staff** – Handle assigned complaints
* **Building Admin** – Manage building-level operations
* **Campus Admin** – Manage entire campus

---

## ✨ Features

* 🔐 Firebase-based authentication (Email/Password)
* 🏫 Multi-campus support (tenant isolation)
* 🧑‍🔧 Role-based access control
* 📝 Complaint lifecycle management
  (Create → Assign → Resolve → Verify)
* 📧 Email-based invite system (staff/admin onboarding)
* 📊 Complaint tracking & history
* 🗂️ Structured backend with clean architecture

---

## 🛠️ Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Security (JWT via Firebase)
* JPA (Hibernate)
* Flyway (DB migrations)
* PostgreSQL (Neon)

### Frontend

* Kotlin
* Jetpack Compose
* MVVM Architecture
* Retrofit
* Firebase Authentication

---

## 🔐 Authentication Model

* Firebase handles:

  * Login / Signup
  * Password management
  * JWT (ID Token)

* Backend handles:

  * Token verification
  * Role resolution
  * Authorization
  * Campus isolation

---

## 📁 Project Structure

```text
campusfix/
  ├── campusfix-backend/    # Spring Boot backend
  ├── campusfix-android/   # Android app (Jetpack Compose)
```

---

## ⚙️ Setup Instructions

### 🔹 Backend

```bash
cd campusfix-backend
./mvnw spring-boot:run
```

#### Environment Variables Required

```text
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
FIREBASE_PROJECT_ID=
MAIL_USERNAME=
MAIL_PASSWORD=
GOOGLE_APPLICATION_CREDENTIALS=
```

---

### 🔹 Android App

* Open `campusfix-android` in Android Studio
* Add `google-services.json` (from Firebase Console)
* Update API base URL
* Run on emulator or device

---


## 🧠 Key Learnings

* Multi-tenant backend design
* Secure authentication with Firebase JWT
* Clean architecture (MVVM + layered backend)
* Real-world deployment considerations
* API design and role-based authorization
