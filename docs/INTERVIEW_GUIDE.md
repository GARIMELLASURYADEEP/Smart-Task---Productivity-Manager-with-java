# Interview & Viva Guide – Smart Task Manager

---

## 30-Second Project Introduction

> "I built a Smart Task and Productivity Manager – a full stack web app where users can sign up, login, and manage daily tasks with priorities and due dates. The frontend uses HTML, CSS, JavaScript, and Bootstrap with Chart.js for analytics. The backend is Core Java with a built-in HTTP server, JDBC for SQLite database, and simple session-based authentication. It has a dashboard showing completed, pending, and overdue tasks with productivity percentage."

---

## Recruiter Explanation (Simple English)

This project shows that I can build a **complete web application** without heavy frameworks. I handled:

- User login and security (password hashing)
- Database design and SQL queries
- Backend APIs in Java
- Frontend UI and charts
- Real CRUD operations on tasks

It is practical, runs locally in VS Code, and I can explain every file confidently.

---

## Architecture Explanation

```
Browser (HTML/CSS/JS)
        |
        |  HTTP Requests (fetch API + cookies)
        v
Java HttpServer (Port 8080)
        |
        +-- AuthHandler  --> UserDAO  --> SQLite (users)
        +-- TaskHandler  --> TaskDAO   --> SQLite (tasks)
        +-- StaticFileHandler --> frontend files
```

**Flow example – Login:**

1. User submits login form (JavaScript)
2. `POST /api/auth/login` with JSON body
3. `AuthHandler` validates username/password via `UserDAO`
4. `SessionManager` creates session ID, sends cookie to browser
5. Dashboard page checks `/api/auth/me` before loading data

**Flow example – Add Task:**

1. User fills modal and clicks Save
2. `POST /api/tasks` with task JSON
3. `TaskHandler` checks session cookie → gets user id
4. `TaskDAO.addTask()` runs INSERT with PreparedStatement
5. Success message returned, task list refreshed

---

## Key Technical Concepts Used

| Concept | Where Used |
|--------|------------|
| JDBC | `DatabaseConnection`, `UserDAO`, `TaskDAO` |
| PreparedStatement | Prevents SQL injection |
| SQLite | Lightweight file-based database |
| HttpServer | Core Java built-in web server |
| Session Cookies | `SessionManager`, `HttpUtil` |
| Password Hashing | SHA-256 + salt in `PasswordUtil` |
| REST-style APIs | `/api/auth`, `/api/tasks`, etc. |
| CRUD | Create, Read, Update, Delete tasks |
| Fetch API | Frontend `api.js` |
| Chart.js | Analytics pie/bar charts |
| Responsive UI | Bootstrap + custom CSS |
| Theme Toggle | CSS variables + localStorage + JavaScript |

---

## Possible Interview Questions & Answers

### 1. Why did you not use Spring Boot?

**Answer:** I wanted to learn Java fundamentals clearly – JDBC, HTTP handling, and SQL – without framework magic. This project proves I understand the basics before moving to Spring.

### 2. How does session management work?

**Answer:** After login, the server creates a random session ID and stores `sessionId → userId` in a `HashMap`. The session ID is sent as an HTTP-only cookie. Every API request sends this cookie, and the server looks up the user id before processing.

### 3. How are passwords stored?

**Answer:** Plain passwords are never stored. I hash them using SHA-256 with a random salt. The database stores `salt:hash` format, and during login I hash the entered password with the same salt and compare.

### 4. What is JDBC?

**Answer:** JDBC (Java Database Connectivity) is Java's standard API to connect and run SQL on databases. I use `DriverManager.getConnection()` and `PreparedStatement` for safe queries.

### 5. What is PreparedStatement and why use it?

**Answer:** PreparedStatement uses `?` placeholders for values. It prevents SQL injection and is cleaner than concatenating strings in SQL.

### 6. Explain your database schema.

**Answer:** Two tables – `users` for authentication and `tasks` linked by `user_id` foreign key. Tasks have title, description, priority, due_date, status, and created_at timestamp.

### 7. How do you calculate productivity percentage?

**Answer:** `(completed tasks / total tasks) * 100`. If there are zero tasks, productivity is 0%.

### 8. How does search and filter work?

**Answer:** Frontend sends query parameters like `?search=java&priority=High&status=pending`. Backend builds SQL dynamically with WHERE conditions and PreparedStatement parameters.

### 9. What are overdue tasks?

**Answer:** Tasks where `due_date` is before today AND status is not `completed`.

### 10. How does Chart.js get data?

**Answer:** Analytics page calls `GET /api/analytics`. Java runs GROUP BY queries and returns JSON. JavaScript renders pie, doughnut, and bar charts.

### 11. What happens if the server is not running?

**Answer:** Frontend fetch fails and shows a message like "Cannot connect to server. Start the Java backend first."

### 12. How does the day/night theme toggle work?

**Answer:** I use CSS variables for colors in `style.css`. The `data-theme` attribute on `<html>` switches between `dark` and `light`. JavaScript saves the choice in `localStorage` so it persists after refresh. A small script in the page `<head>` loads the saved theme early to avoid flicker.

### 13. How would you improve this project later?

**Answer:** I could add email reminders, file export, role-based access, or migrate backend to Spring Boot once fundamentals are strong.

---

## Viva Questions (College Project)

1. **What is full stack?** – Frontend + Backend + Database working together.
2. **What port does your app use?** – 8080
3. **Which design pattern did you use?** – Simple DAO pattern (Data Access Object) for database operations.
4. **What is CRUD?** – Create, Read, Update, Delete.
5. **Name SQL commands you used.** – SELECT, INSERT, UPDATE, DELETE, CREATE TABLE.

---

## GitHub README Tips

- Add 4–5 screenshots (login, dashboard, tasks, analytics)
- Mention demo credentials: `demo` / `demo123`
- List tech stack clearly
- Keep setup steps under 5 commands

---

## What Makes This Project Believable for a Fresher

- No over-engineering (no microservices, no Docker required)
- Clear folder structure
- Comments on important backend code
- Realistic feature set (auth + CRUD + dashboard + charts)
- Uses only technologies taught in most Java full stack courses
- Easy to demo live in interview within 2 minutes
