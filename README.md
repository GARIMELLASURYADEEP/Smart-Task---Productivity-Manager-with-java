# Smart Task & Productivity Manager

**TaskFlow** is a modern full stack productivity and task management web application that helps users organize, track, and manage daily tasks through an interactive dashboard. Built with **Core Java**, **JDBC**, **SQLite**, **HTML**, **CSS**, **JavaScript**, and **Bootstrap** — ideal for fresher resumes, college projects, and technical interviews.

![Java](https://img.shields.io/badge/Java-21-orange)
![SQLite](https://img.shields.io/badge/SQLite-3-blue)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5-purple)

---

## About the Project

The platform allows users to create, update, delete, prioritize, and monitor tasks while improving productivity through visual analytics and task completion tracking. Users can organize tasks based on priority levels, due dates, and completion status.

The system includes secure user authentication (login/signup), a responsive dashboard with productivity insights, search and filtering, and **Day Theme / Night Theme** modes with smooth transitions.

---

## Features

- **User Authentication** – Signup, Login, Logout, Session management
- **Task Management** – Add, Edit, Delete, Mark complete, Due dates, Descriptions
- **Priority Levels** – High, Medium, Low
- **Dashboard** – Total, Completed, Pending, Overdue tasks, Productivity %
- **Search & Filter** – By keyword, priority, status, due date
- **Analytics** – Pie, Doughnut, and Bar charts using Chart.js
- **Modern UI** – Dark & Light theme toggle with smooth transitions

---

## Tech Stack

| Layer      | Technology                          |
|-----------|--------------------------------------|
| Frontend  | HTML, CSS, JavaScript, Bootstrap 5  |
| Backend   | Core Java (HttpServer)              |
| Database  | SQLite                              |
| Connectivity | JDBC (PreparedStatement)         |
| Charts    | Chart.js                            |

**No Spring Boot, No Hibernate, No React, No Maven complexity.**

---

## Project Structure

```
Smart-Task---Productivity-Manager-with-java/
├── backend/
│   ├── src/com/smarttask/
│   │   ├── MainServer.java          # Server entry point
│   │   ├── db/                      # JDBC connection & init
│   │   ├── model/                   # User & Task models
│   │   ├── dao/                     # Database CRUD
│   │   ├── handler/                 # API & static file handlers
│   │   └── util/                    # Session, password, JSON helpers
│   ├── lib/
│   │   └── sqlite-jdbc.jar
│   └── database/
│       └── tasks.db                 # Auto-created on first run
├── frontend/
│   ├── login.html, signup.html
│   ├── dashboard.html, tasks.html, analytics.html
│   ├── css/style.css
│   └── js/                          # API, auth, dashboard, tasks, analytics, theme
├── docs/
│   └── INTERVIEW_GUIDE.md
├── run.bat                          # Windows quick start
├── run.sh                           # Linux/Mac quick start
└── README.md
```

---

## Prerequisites

- **Java JDK 17+** (tested with Java 21)
- **VS Code** (optional, recommended)
- Internet (first run only – for Bootstrap & Chart.js CDN)

---

## How to Run (VS Code / Terminal)

### Option 1: Double-click (Windows)

1. Clone the repository and open the project folder in VS Code
2. Double-click `run.bat`
3. Open browser: **http://localhost:8080**

### Option 2: Manual commands

```bash
# 1. Go to project root
cd Smart-Task---Productivity-Manager-with-java

# 2. Compile (Windows)
javac -d backend/out -cp backend/lib/sqlite-jdbc.jar backend/src/com/smarttask/model/*.java backend/src/com/smarttask/db/*.java backend/src/com/smarttask/util/*.java backend/src/com/smarttask/dao/*.java backend/src/com/smarttask/handler/*.java backend/src/com/smarttask/MainServer.java

# 3. Run server (Windows)
java -cp "backend/lib/sqlite-jdbc.jar;backend/out" com.smarttask.MainServer

# Linux/Mac classpath separator is colon (:)
java -cp "backend/lib/sqlite-jdbc.jar:backend/out" com.smarttask.MainServer
```

### Demo Login

| Field    | Value    |
|---------|----------|
| Username | `demo`   |
| Password | `demo123`|

---

## VS Code Setup

1. Install extension: **Extension Pack for Java**
2. Open the cloned project folder
3. Run from terminal using commands above
4. Open `http://localhost:8080/login.html`

> **Important:** Always run the server from the **project root** folder so database and frontend paths work correctly.

---

## API Endpoints

| Method | Endpoint                    | Description        |
|--------|-----------------------------|--------------------|
| POST   | `/api/auth/signup`          | Register user      |
| POST   | `/api/auth/login`           | Login user         |
| POST   | `/api/auth/logout`          | Logout user        |
| GET    | `/api/auth/me`              | Current user       |
| GET    | `/api/tasks`                | List tasks         |
| POST   | `/api/tasks`                | Add task           |
| PUT    | `/api/tasks/{id}`           | Update task        |
| DELETE | `/api/tasks/{id}`           | Delete task        |
| PUT    | `/api/tasks/{id}/complete`  | Mark complete      |
| GET    | `/api/dashboard`            | Dashboard stats    |
| GET    | `/api/analytics`            | Chart data         |

---

## Database Tables

**users** – id, username, email, password (hashed)

**tasks** – id, user_id, title, description, priority, due_date, status, created_at

Database file: `backend/database/tasks.db` (created automatically)

---

## Screenshot Ideas (for GitHub/README)

1. Login page with dark theme
2. Dashboard with stat cards and progress bar
3. Tasks page with filters and task cards
4. Analytics page with 3 Chart.js charts
5. Add/Edit task modal
6. Day theme vs Night theme toggle

---

## Resume Project Description

> Developed a full stack Smart Task & Productivity Manager (TaskFlow) using Core Java and JDBC with SQLite. Built REST-style APIs using Java HttpServer, implemented user authentication with session cookies and password hashing, and created a responsive dashboard with HTML, CSS, JavaScript, Bootstrap, and Chart.js for task analytics with day/night theme support.

---

## Interview Support

See **[docs/INTERVIEW_GUIDE.md](docs/INTERVIEW_GUIDE.md)** for:

- 30-second project pitch
- Architecture explanation
- Viva questions & answers
- Technical concepts list

---

## Author

**GARIMELLASURYADEEP** – Fresher / Student Full Stack Project for portfolio and campus placements.

---

## License

Free to use for learning and academic projects.
