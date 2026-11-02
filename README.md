# 💬 ChatSpace: Real-Time Chat Application

A full-stack chat web application built for the **Full Stack Developer Technical Interview Assignment**.

- **Frontend:** React 18 (Vite) + React Router + plain responsive CSS
- **Backend:** Java 17+ / Spring Boot 3.5 (REST APIs + WebSocket/STOMP + Spring Security + JPA)
- **Database:** H2 (embedded, file-based: nothing to install)
- **Real-time:** WebSocket with the STOMP protocol

Users can register, log in, chat in a public room, see who is **Online** or **LIVE**, find other users, send **private chat requests**, and, once a request is accepted, chat **privately**. Everything updates in real time.

---

## 📑 Table of contents

1. [Quick start (TL;DR)](#1--quick-start-tldr)
2. [What you need to install first](#2--what-you-need-to-install-first)
3. [Running the app step by step](#3--running-the-app-step-by-step)
4. [Try it out: a guided test scenario](#4--try-it-out-a-guided-test-scenario)
5. [Features vs. assignment requirements](#5--features-vs-assignment-requirements)
6. [Project structure](#6--project-structure)
7. [How it works (architecture, in detail)](#7--how-it-works-architecture-in-detail)
8. [Database design](#8--database-design)
9. [API reference (REST + WebSocket)](#9--api-reference-rest--websocket)
10. [Security](#10--security)
11. [Validation rules](#11--validation-rules)
12. [Configuration](#12--configuration)
13. [Looking inside the database (H2 console)](#13--looking-inside-the-database-h2-console)
14. [Troubleshooting](#14--troubleshooting)
15. [Limitations and possible improvements](#15--limitations-and-possible-improvements)
16. [Stopping, resetting and uninstalling](#16--stopping-resetting-and-uninstalling)

---

## 1. 🚀 Quick start (TL;DR)

You need **Java 17+** and **Node.js 18+** installed (see [section 2](#2--what-you-need-to-install-first)). Then:

### Windows (Command Prompt or PowerShell)

```bat
:: 1. go to the folder where chat-app.zip was saved (example: Downloads)
cd %USERPROFILE%\Downloads

:: 2. extract it
tar -xf chat-app.zip

:: 3. enter the project and start everything
cd chat-app
start.bat
```

> In PowerShell use `cd $HOME\Downloads` for step 1 and `.\start.bat` for step 3.
> You can also simply right-click the zip → **Extract All…**, open the `chat-app` folder and **double-click `start.bat`**.

### macOS / Linux (Terminal)

```bash
cd ~/Downloads
unzip chat-app.zip
cd chat-app
bash start.sh
```

Then open **http://localhost:5173** in your browser (the script opens it automatically).

> ⏳ **The first start is slow (3–5 minutes)** because Maven and all Java libraries are downloaded once.
> Later starts take about 15 seconds.

---

## 2. 🧰 What you need to install first

| Tool | Version | Why | Download |
|------|---------|-----|----------|
| **Java JDK** | 17 or newer (17, 21, 25 all work) | Runs the Spring Boot backend | https://adoptium.net (choose "Temurin", LTS) |
| **Node.js** | 18 or newer (includes `npm`) | Runs the React frontend dev server | https://nodejs.org (choose "LTS") |
| A modern browser | Chrome, Edge, Firefox, Safari | Uses the app | |
| Internet connection | first run only | Downloads libraries | |

**You do NOT need to install:** Maven (the included `mvnw` "Maven Wrapper" downloads it automatically), a database server (H2 is embedded), or any IDE.

### Check that they are installed

Open a **new** terminal (important after installing) and run:

```bash
java -version
node -v
npm -v
```

Expected output (numbers may be higher):

```
openjdk version "17.0.x" ...      <- must be 17 or more
v20.x.x                           <- must be 18 or more
10.x.x
```

> ⚠️ **Windows tip:** when installing Temurin, enable the option **"Set JAVA_HOME variable"** and **"Add to PATH"**.
> If `java -version` shows an old version (e.g. `1.8`), uninstall the old Java or move the new one first in PATH.

---

## 3. ▶️ Running the app step by step

There are two ways: **A) the start script** (easiest) or **B) manually in two terminals** (useful to understand what happens).

### A) Using the start script

**Windows:** run `start.bat`. It will:

1. check that `java` and `npm` exist;
2. run `npm install` inside `frontend/` (first time only);
3. open a window titled **"ChatSpace BACKEND - port 8080"** running `mvnw.cmd spring-boot:run`;
4. open a window titled **"ChatSpace FRONTEND - port 5173"** running `npm run dev`;
5. wait until the backend answers, then open http://localhost:5173 in your browser.

To stop: close the BACKEND and FRONTEND windows.

**macOS/Linux:** run `bash start.sh`. It does the same, but the backend runs in the background (its output goes to `backend.log`) and the frontend runs in the same terminal. Press **Ctrl+C** to stop both.

### B) Manually, in two terminals

**Terminal 1: backend**

```bash
cd chat-app/backend
# Windows:
mvnw.cmd spring-boot:run
# macOS / Linux:
chmod +x mvnw
./mvnw spring-boot:run
```

Wait until you see a line like:

```
Started ChatApplication in 7.4 seconds
```

The backend now listens on **http://localhost:8080**.

**Terminal 2: frontend**

```bash
cd chat-app/frontend
npm install        # first time only
npm run dev
```

You will see:

```
VITE v6.x  ready in 300 ms
➜  Local:   http://localhost:5173/
```

Open **http://localhost:5173**.

### Optional: build a single runnable JAR for the backend

```bash
cd backend
./mvnw clean package          # Windows: mvnw.cmd clean package
java -jar target/chat-backend-1.0.0.jar
```

---

## 4. 🧪 Try it out: a guided test scenario

To see the real-time features you need **two different users at the same time**. A browser shares its login between tabs, so use **two different browsers** (e.g. Chrome + Edge) or **one normal window + one Incognito/Private window**.

| Step | Window 1 (Alice) | Window 2 (Bob) | What you should see |
|------|------------------|----------------|---------------------|
| 1 | Open http://localhost:5173 → **Create one** → register `Alice` / `alice` / `alice@test.com` / `pass123` | | You land in **Global chat**, logged in. |
| 2 | | Register `Bob` / `bob` / `bob@test.com` / `pass123` | Alice's right-hand panel shows Bob appear **instantly**. |
| 3 | Type "Hello everyone!" and press **Enter** | | The message appears immediately in Bob's window with name + time. |
| 4 | | Look at the **Online** panel | Both users have a red pulsing **● LIVE** badge (they are on the chat screen). |
| 5 | | Click **People** in the menu | In Alice's window, Bob changes from **LIVE** to **Online** within a moment: he is connected but not on a chat screen. |
| 6 | | Search "ali", click **Alice** → **📨 Send chat request** | Alice gets a toast "Bob sent you a chat request" and a red badge on **Requests**. |
| 7 | **Requests** → **Incoming** → **Accept** | | Bob gets "Alice accepted your chat request". |
| 8 | **Private chats** → **Bob** → write a message | | Bob gets a toast + unread badge; opening the chat shows the message. Private messages are visible only to these two. |
| 9 | | Close the window | In Alice's window Bob turns **Offline** (grey) within a few seconds. |
| 10 | Refresh the page | | All messages are still there: they are stored in the database. |

**Validation examples to try:** register with an existing username (`ALICE` also counts as taken), an invalid email, a password without a digit, or log in with a wrong password. You will get a clear error message.

**Security example:** register a third user `carol`, open `http://localhost:5173/chats/alice` as Carol. You will see 🔒 "You can chat privately … only after a chat request is accepted". The server refuses both reading and sending (HTTP 403).

---

## 5. ✅ Features vs. assignment requirements

### Module 1: User Registration and Login
| Requirement | Implementation |
|---|---|
| Registration page with name, username, email, password | `RegisterPage.jsx` → `POST /api/auth/register` |
| Login and logout | `LoginPage.jsx` (username **or** email) → `POST /api/auth/login`; logout in sidebar & Settings → `POST /api/auth/logout` (token is revoked) |
| Validate user inputs | In the browser (instant feedback) **and** on the server with Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Pattern`) |
| Prevent duplicate usernames and emails | Checked in `AuthService` (case-insensitive) + `UNIQUE` constraints in the DB → HTTP 409 |
| Restrict chat to authenticated users | `ProtectedRoute.jsx` in the UI; Spring Security on every `/api/**` endpoint; JWT required on the WebSocket CONNECT |

### Module 2: Global Chat
| Requirement | Implementation |
|---|---|
| Shared public chat room | `GlobalChatPage.jsx` |
| Send and receive messages | Send: STOMP `/app/global.send`; receive: subscription `/topic/global` |
| Sender name, content, timestamp | Shown on every bubble (`MessageList.jsx`) |
| Save messages in DB | Table `global_messages` |
| Load previous messages on open | `GET /api/global/messages?limit=100` |
| Real-time updates | WebSocket + STOMP (Spring simple broker) |

### Module 3: Online and LIVE status
| Requirement | Implementation |
|---|---|
| Online/offline indicators | Coloured dot on every avatar (green = online, grey = offline) + "Online"/"Offline" badges |
| LIVE badge | Red pulsing "● LIVE" badge and red dot |
| Update on connect/disconnect | Spring `SessionConnectedEvent` / `SessionDisconnectEvent` → broadcast on `/topic/presence` |
| Distinguish active chat session from general online connection | **Online** = WebSocket open (any page). **LIVE** = a chat screen is open, the tab is visible and heartbeats arrive (see [7.6](#76-online-and-live-status)) |

### Module 4: User Discovery
| Requirement | Implementation |
|---|---|
| List registered users | `UsersPage.jsx` → `GET /api/users` |
| Search by username or display name | `GET /api/users?search=...` (case-insensitive `LIKE` on both columns) |
| Filter by Online / LIVE | Tabs **All / Online / LIVE / Offline** (also supported by the API: `?filter=online`) |
| Public profile | `ProfilePage.jsx` → `GET /api/users/{username}` (name, username, bio, join date, last seen, status) |

### Module 5: Private Chat Request
| Requirement | Implementation |
|---|---|
| Send a request | Button on a profile → `POST /api/requests` |
| Show incoming requests | `RequestsPage.jsx` → Incoming tab + red badge in menu + toast, all real time |
| Accept / reject | `POST /api/requests/{id}/accept` and `/reject` (only the recipient may do this) |
| Update the request status | `PENDING → ACCEPTED / REJECTED / CANCELLED`, pushed live to both users |
| Private messaging only after acceptance | Every private read/write checks for an `ACCEPTED` request, otherwise HTTP 403 |

### Module 6: Private Messaging
| Requirement | Implementation |
|---|---|
| Approved users exchange messages | `ChatsPage.jsx` → STOMP `/app/private.send`, delivered on `/user/queue/private` |
| Store in DB | Table `private_messages` |
| Load history for participants | `GET /api/private/{username}/messages` |
| Block unauthorized users | Participant + accepted-request check on the server; users can only subscribe to **their own** queue |

### Module 7: Basic Security
| Requirement | Implementation |
|---|---|
| Hash passwords | BCrypt (salted, slow hash) |
| Protect APIs | JWT in `Authorization: Bearer` header, Spring Security filter |
| Backend validation | Bean Validation on all request bodies + message length/empty checks |
| No unauthorized access to private conversations | See [section 10](#10--security) |
| Error handling | `GlobalExceptionHandler` returns uniform JSON errors; WebSocket errors go to `/user/queue/errors` and appear as toasts |

### Expected UI screens
| # | Screen | Route |
|---|---|---|
| 1 | Registration | `/register` |
| 2 | Login | `/login` |
| 3 | Global chat dashboard | `/` |
| 4 | Online & LIVE users list | right panel of `/` (desktop) and **People → Online / LIVE** tabs |
| 5 | User search & profile | `/users`, `/users/:username` |
| 6 | Incoming & outgoing requests | `/requests` |
| 7 | Private chat | `/chats`, `/chats/:username` |
| 8 | Settings / logout | `/settings` (edit name & bio, change password, log out) + logout button in sidebar |

The layout is **responsive**: on screens narrower than 800 px the sidebar becomes a bottom navigation bar and private chats show one panel at a time. Light and dark themes follow the operating-system setting.

---

## 6. 🗂️ Project structure

```
chat-app/
├── README.md                 ← this file
├── start.bat                 ← one-click launcher (Windows)
├── start.sh                  ← one-command launcher (macOS/Linux)
│
├── backend/                  ← Spring Boot application (Java)
│   ├── pom.xml               ← Maven build file: dependencies and Java version
│   ├── mvnw, mvnw.cmd        ← Maven Wrapper: downloads Maven automatically
│   ├── .mvn/wrapper/         ← Maven Wrapper settings
│   ├── data/                 ← (created on first run) H2 database file chatdb.mv.db
│   └── src/main/
│       ├── resources/application.properties   ← port, DB, JWT secret, LIVE timeout
│       └── java/com/chatapp/
│           ├── ChatApplication.java           ← main() entry point
│           ├── config/
│           │   ├── SecurityConfig.java        ← which URLs are public/protected, CORS, BCrypt
│           │   ├── WebSocketConfig.java       ← /ws endpoint, /app /topic /queue /user prefixes
│           │   └── StompAuthChannelInterceptor.java ← checks JWT on WebSocket CONNECT, guards SUBSCRIBE/SEND
│           ├── security/
│           │   ├── JwtService.java            ← create / verify / revoke JWT tokens
│           │   ├── JwtAuthFilter.java         ← reads "Authorization: Bearer" on each HTTP request
│           │   └── TokenBlacklist.java        ← remembers logged-out tokens
│           ├── model/                         ← JPA entities = database tables
│           │   ├── User.java
│           │   ├── GlobalMessage.java
│           │   ├── ChatRequest.java
│           │   ├── RequestStatus.java         ← PENDING / ACCEPTED / REJECTED / CANCELLED
│           │   └── PrivateMessage.java
│           ├── repository/                    ← Spring Data interfaces (SQL generated automatically)
│           ├── dto/                           ← request/response objects (what travels as JSON)
│           ├── service/                       ← business logic
│           │   ├── AuthService.java           ← register / login / logout
│           │   ├── UserService.java           ← search, profiles, settings
│           │   ├── PresenceService.java       ← Online / LIVE tracking
│           │   ├── PresenceEventListener.java ← reacts to WebSocket connect/disconnect
│           │   ├── GlobalChatService.java
│           │   ├── ChatRequestService.java
│           │   ├── PrivateChatService.java
│           │   └── MessageValidator.java
│           ├── controller/                    ← REST endpoints + @MessageMapping (WebSocket) handlers
│           └── exception/                     ← ApiException + global error handlers
│
└── frontend/                 ← React application (JavaScript)
    ├── package.json          ← npm dependencies and scripts
    ├── vite.config.js        ← dev server on 5173 + proxy of /api and /ws to 8080
    ├── index.html
    └── src/
        ├── main.jsx          ← React entry point (router + providers)
        ├── App.jsx           ← all routes
        ├── index.css         ← all styles (responsive, light/dark)
        ├── api/client.js     ← fetch wrapper (adds JWT, handles errors)
        ├── context/
        │   ├── AuthContext.jsx    ← logged-in user, login/register/logout
        │   ├── SocketContext.jsx  ← the single WebSocket connection, presence, unread counts
        │   └── ToastContext.jsx   ← pop-up notifications
        ├── components/        ← Layout, Avatar, StatusBadge, MessageList, MessageComposer, ...
        ├── pages/             ← one file per screen
        └── utils/format.js    ← date/time formatting
```

---

## 7. ⚙️ How it works (architecture, in detail)

### 7.1 Big picture

```
 ┌──────────────────────────── Browser ────────────────────────────┐
 │  React app (http://localhost:5173)                               │
 │   • REST calls:  fetch('/api/...')   with  Authorization: Bearer │
 │   • WebSocket:   ws://localhost:5173/ws   (STOMP protocol)       │
 └───────────────┬──────────────────────────────┬───────────────────┘
                 │ HTTP                          │ WebSocket
                 ▼                               ▼
 ┌──────────── Vite dev server (port 5173) ─────────────────────────┐
 │  serves the React files; PROXIES /api and /ws to port 8080       │
 └───────────────┬──────────────────────────────┬───────────────────┘
                 ▼                               ▼
 ┌──────────── Spring Boot backend (port 8080) ─────────────────────┐
 │  Security filter (JWT) ─► REST controllers ─► Services ─► JPA    │
 │  STOMP interceptor (JWT) ─► @MessageMapping ─► Services          │
 │  Simple message broker: /topic/* (everyone)  /user/queue/* (one) │
 └───────────────────────────────┬──────────────────────────────────┘
                                 ▼
                      H2 database file  backend/data/chatdb.mv.db
```

**Why a proxy?** The browser only talks to `localhost:5173`. Vite forwards `/api` and `/ws` to the backend, so there are no cross-origin problems. (CORS for `localhost:5173` is configured in the backend anyway.)

**Why both REST and WebSocket?**
- **REST** is used for request/response actions: register, login, load history, search users, accept a request…
- **WebSocket** is used for things the *server* must push without being asked: new messages, presence changes, request notifications.

### 7.2 Registration and login (JWT)

```
Browser                                   Backend
  │ POST /api/auth/register {name,username,email,password}
  │ ───────────────────────────────────────►│ 1. @Valid checks every field (400 if invalid)
  │                                         │ 2. lower-case username/email, check duplicates (409)
  │                                         │ 3. hash password with BCrypt, INSERT INTO users
  │                                         │ 4. create JWT (signed with HMAC-SHA256)
  │ ◄─────────────────────────────────────── │ 201 { token, user }
  │ store token in localStorage
  │
  │ every later request:  Authorization: Bearer <token>
  │ ───────────────────────────────────────►│ JwtAuthFilter verifies signature + expiry + not logged out
```

- A **JWT** (JSON Web Token) is a signed string containing the username, a unique id and an expiry (24 h). The server can verify it without storing sessions (stateless).
- **Login** accepts username *or* email. The error message is the same for "unknown user" and "wrong password" so attackers cannot discover which usernames exist.
- **Logout** sends `POST /api/auth/logout`; the token's id is put on a blacklist so it can't be reused, the WebSocket is closed, and the browser forgets the token.
- After a page refresh, the app calls `GET /api/users/me` with the stored token to restore the session.

### 7.3 WebSocket connection (STOMP)

STOMP is a simple text protocol on top of WebSocket with commands like `CONNECT`, `SUBSCRIBE`, `SEND`. The library `@stomp/stompjs` handles it in the browser.

1. After login, `SocketContext.jsx` opens **one** connection to `/ws` and sends `CONNECT` with header `Authorization: Bearer <token>`.
2. `StompAuthChannelInterceptor` verifies the token. Invalid → connection refused. Valid → the username is attached to the WebSocket session.
3. The browser subscribes to:

| Destination | Who receives | Used for |
|---|---|---|
| `/topic/global` | everyone | new global chat messages |
| `/topic/presence` | everyone | someone went online / offline / LIVE |
| `/user/queue/private` | only me | private messages to/from me |
| `/user/queue/requests` | only me | chat request created/accepted/rejected/cancelled |
| `/user/queue/errors` | only me (this tab) | errors for messages I sent |

4. Heartbeats every 10 s in both directions detect dead connections. If the connection drops, the client reconnects every 3 s and reloads history so nothing is missed.

`/user/queue/private` is a Spring feature: the server sends with `convertAndSendToUser("bob", "/queue/private", msg)` and Spring delivers it only to Bob's sessions.

### 7.4 Global chat message flow

```
Alice types "Hi" + Enter
  └─► STOMP SEND /app/global.send {"content":"Hi"}
        └─► GlobalChatController.send()            (@MessageMapping("/global.send"))
              └─► GlobalChatService.send()
                    1. MessageValidator: trim, not empty, ≤ 1000 chars
                    2. INSERT INTO global_messages
                    3. broadcast to /topic/global
                    4. mark Alice LIVE
  ◄── every connected browser (including Alice's) receives the message and appends it
```

When the page opens it calls `GET /api/global/messages?limit=100` to show earlier messages.

### 7.5 Chat requests

```
Bob → POST /api/requests {"toUsername":"alice"}
        rules: not yourself · not already connected · no pending request either way
        INSERT chat_requests (status = PENDING)
        push to /user/queue/requests of BOTH users  → Alice sees a toast + badge
Alice → POST /api/requests/7/accept     (only the recipient; only while PENDING)
        UPDATE status = ACCEPTED
        push to both users                          → Bob sees "accepted"
```

A sender can **cancel** a pending request; after a rejection the sender may send a new one later.
The profile page shows the right button from the `relation` field: `NONE`, `OUTGOING_PENDING`, `INCOMING_PENDING`, `ACCEPTED` or `SELF`.

### 7.6 Online and LIVE status

This is handled by `PresenceService` (backend) and `useLiveStatus()` (frontend).

| State | Meaning | How it is detected |
|---|---|---|
| **Offline** (grey) | no open connection | no WebSocket session for the user |
| **Online** (green) | the app is open somewhere | at least one WebSocket session. Set on `SessionConnectedEvent`, removed on `SessionDisconnectEvent` of the **last** session (several tabs supported) |
| **LIVE** (red, pulsing) | actively in a chat right now | a chat screen (global or private) is open **and** the browser tab is visible. That screen sends `/app/presence.live` immediately and every **15 s**; sending a message also counts. |

LIVE is removed when:
- the user leaves the chat screen or hides the tab (`/app/presence.idle` is sent immediately), or
- no heartbeat arrived for **45 s** (`app.presence.live-timeout-seconds`). A background job checks every 5 s; this covers a crashed browser or a sleeping laptop, or
- the user goes offline.

Each change is broadcast on `/topic/presence` as `{ "username": "bob", "online": true, "live": false }`, and every browser updates the dots, badges, the online panel and the People filters immediately. When the last connection closes, `last_seen` is saved in the database ("Last seen 5 min ago" on profiles).

This is the **simplified LIVE mechanism** the assignment allows; its limitations are listed in [section 15](#15--limitations-and-possible-improvements).

### 7.7 Private messaging

```
Alice → STOMP SEND /app/private.send {"to":"bob","content":"Hey"}
          PrivateChatService.send():
            1. both users exist, not the same person
            2. an ACCEPTED request exists between them   (else 403 → /user/queue/errors)
            3. validate content, INSERT INTO private_messages
            4. convertAndSendToUser("bob",   "/queue/private", msg)
               convertAndSendToUser("alice", "/queue/private", msg)  (keeps Alice's other tabs in sync)
History:  GET /api/private/bob/messages   (same permission check, else 403)
List:     GET /api/private/conversations  (everyone I'm connected with + last message)
```

If a private message arrives while you are elsewhere in the app, you get a toast and an unread counter on **Private chats**.

### 7.8 Frontend structure

- **`AuthContext`** keeps the current user; `ProtectedRoute` redirects to `/login` when there is none.
- **`SocketContext`** owns the single STOMP connection. Pages listen with `useSocketEvent('global' | 'private' | 'requests', handler)` and send with `publish(destination, body)`. It also keeps the presence map, the pending-request count and unread counters.
- **`api/client.js`** adds the token to every request and turns error responses into readable messages. On a 401 (token expired or logged out) the user is logged out automatically.

---

## 8. 🗄️ Database design

Tables are created automatically from the entity classes (`spring.jpa.hibernate.ddl-auto=update`).

```
users                         global_messages
──────────────────────        ─────────────────────────
id            PK              id          PK
name          varchar(50)     sender_id   FK → users.id
username      UNIQUE (20)     content     varchar(1000)
email         UNIQUE (100)    sent_at     timestamp   (indexed)
password_hash varchar         
bio           varchar(200)    private_messages
created_at    timestamp       ─────────────────────────
last_seen     timestamp       id           PK
                              sender_id    FK → users.id
chat_requests                 recipient_id FK → users.id
──────────────────────        content      varchar(1000)
id            PK              sent_at      timestamp
sender_id     FK → users.id   (index on sender_id+recipient_id and sent_at)
recipient_id  FK → users.id
status        PENDING | ACCEPTED | REJECTED | CANCELLED
created_at    timestamp
responded_at  timestamp
```

Relationships: one user sends many global messages, many private messages, and many chat requests. Two users "may chat privately" exactly when a `chat_requests` row with `status = ACCEPTED` links them (in either direction).

---

## 9. 📡 API reference (REST + WebSocket)

All endpoints except register/login need the header `Authorization: Bearer <token>`.
Errors always have this shape:

```json
{ "timestamp": "...", "status": 409, "error": "Conflict",
  "message": "Username is already taken",
  "fieldErrors": { "username": "Username is already taken" } }
```

### Auth
| Method | URL | Body | Result |
|---|---|---|---|
| POST | `/api/auth/register` | `{name, username, email, password}` | 201 `{token, user}` · 400 validation · 409 duplicate |
| POST | `/api/auth/login` | `{usernameOrEmail, password}` | 200 `{token, user}` · 401 wrong credentials |
| POST | `/api/auth/logout` | | 204 (token revoked) |

### Users
| Method | URL | Result |
|---|---|---|
| GET | `/api/users?search=al&filter=all\|online\|live\|offline` | list of users with `online`/`live` |
| GET | `/api/users/me` | own profile (with email) |
| PUT | `/api/users/me` `{name, bio}` | update profile |
| PUT | `/api/users/me/password` `{currentPassword, newPassword}` | 204 |
| GET | `/api/users/{username}` | public profile + `relation` + `requestId` |
| GET | `/api/presence` | everyone currently online: `[{username, online, live}]` |

### Global chat
| Method | URL | Result |
|---|---|---|
| GET | `/api/global/messages?limit=100` | latest messages, oldest first |

### Chat requests
| Method | URL | Result |
|---|---|---|
| POST | `/api/requests` `{toUsername}` | 201 request · 400 self · 409 already pending/connected |
| GET | `/api/requests/incoming` | requests sent to me |
| GET | `/api/requests/outgoing` | requests I sent |
| GET | `/api/requests/pending-count` | `{count}` for the badge |
| POST | `/api/requests/{id}/accept` | recipient only |
| POST | `/api/requests/{id}/reject` | recipient only |
| POST | `/api/requests/{id}/cancel` | sender only |

### Private chat
| Method | URL | Result |
|---|---|---|
| GET | `/api/private/conversations` | partners + last message |
| GET | `/api/private/{username}/messages?limit=100` | history · 403 if not connected |

### WebSocket (STOMP), endpoint `ws://localhost:8080/ws`
| Direction | Destination | Payload |
|---|---|---|
| client → server | `/app/global.send` | `{content}` |
| client → server | `/app/private.send` | `{to, content}` |
| client → server | `/app/presence.live` | `{}` (heartbeat) |
| client → server | `/app/presence.idle` | `{}` |
| server → all | `/topic/global` | message `{id, senderUsername, senderName, content, sentAt}` |
| server → all | `/topic/presence` | `{username, online, live}` |
| server → one user | `/user/queue/private` | message (with `recipientUsername`) |
| server → one user | `/user/queue/requests` | chat request `{id, sender, recipient, status, ...}` |
| server → one session | `/user/queue/errors` | error object |

### Trying the API with curl

```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" \
     -d '{"name":"Test User","username":"tester","email":"t@test.com","password":"pass123"}'
# copy the "token" value from the answer, then:
curl http://localhost:8080/api/users -H "Authorization: Bearer PASTE_TOKEN_HERE"
```

(On Windows `cmd`, replace the single quotes with double quotes and escape the inner ones: `-d "{\"name\":\"Test User\", ...}"`.)

---

## 10. 🔐 Security

| Threat | Protection |
|---|---|
| Stolen database reveals passwords | Passwords stored only as **BCrypt** hashes (salted, deliberately slow). |
| Calling APIs without logging in | Spring Security: every `/api/**` endpoint except register/login requires a valid JWT → **401**. |
| Forged or modified token | JWT is signed with a server secret (HMAC-SHA256); any change breaks the signature. Tokens expire after 24 h. |
| Reusing a token after logout | Token id is blacklisted on logout. |
| Opening a WebSocket without login | JWT checked on STOMP `CONNECT`; invalid → connection refused. |
| Listening to someone else's private messages | Clients may subscribe only to `/topic/**` and **their own** `/user/queue/**`; direct `/queue/**` subscriptions are rejected. Private messages are sent only to the two participants. |
| Sending frames directly to the broker | Clients may `SEND` only to `/app/**` (our validated handlers). |
| Reading/sending private messages without permission | Server checks participant + `ACCEPTED` request on **every** read and write → **403**. |
| Acting on someone else's request | Only the recipient can accept/reject; only the sender can cancel; requests of other people return 404. |
| Invalid / oversized input | Bean Validation on all request bodies; messages trimmed, non-empty, max 1000 chars. |
| XSS (script injection in messages) | React escapes all text by default; messages are never inserted as HTML. |
| Leaking internals in errors | `GlobalExceptionHandler` returns clean JSON; stack traces are only logged on the server. |
| Username enumeration on login | Same error for wrong username and wrong password. |

---

## 11. ✏️ Validation rules

| Field | Rule |
|---|---|
| Name | required, 2–50 characters |
| Username | required, 3–20 characters, letters/digits/underscore only, unique (case-insensitive, stored lower-case) |
| Email | required, valid format, max 100, unique (case-insensitive) |
| Password | 6–64 characters, at least one letter **and** one digit |
| Bio | optional, max 200 characters |
| Message | not empty after trimming, max 1000 characters |

Rules are checked in the browser for quick feedback **and** again on the server (the server is the source of truth).

---

## 12. 🔧 Configuration

File: `backend/src/main/resources/application.properties`

| Property | Default | Meaning |
|---|---|---|
| `server.port` | `8080` | Backend port. If you change it, also change the proxy target in `frontend/vite.config.js`. |
| `spring.datasource.url` | `jdbc:h2:file:./data/chatdb` | DB file location (relative to `backend/`) |
| `app.jwt.secret` | long default string | Signing key, ≥ 32 characters. **Change it** (or set env var `APP_JWT_SECRET`) for any real deployment. |
| `app.jwt.expiration-minutes` | `1440` | Token lifetime (24 h) |
| `app.cors.allowed-origins` | `http://localhost:5173,http://127.0.0.1:5173` | Origins allowed for REST and WebSocket |
| `app.presence.live-timeout-seconds` | `45` | How long LIVE survives without a heartbeat |
| `spring.h2.console.enabled` | `true` | DB web console (disable in production) |

### Switching to MySQL (optional)

1. Add the driver to `backend/pom.xml`:
   ```xml
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```
2. Create a database: `CREATE DATABASE chatdb;`
3. Replace the datasource lines in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/chatdb
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   ```
   Tables are created automatically on startup. (PostgreSQL works the same way with `org.postgresql:postgresql`.)

---

## 13. 🔍 Looking inside the database (H2 console)

While the backend is running:

1. Open **http://localhost:8080/h2-console**
2. Fill in:
   - **JDBC URL:** `jdbc:h2:file:./data/chatdb`
   - **User Name:** `sa`
   - **Password:** *(leave empty)*
3. Click **Connect** and run, for example:
   ```sql
   SELECT id, username, email, password_hash FROM users;   -- passwords are BCrypt hashes
   SELECT * FROM global_messages ORDER BY sent_at DESC;
   SELECT * FROM chat_requests;
   SELECT * FROM private_messages;
   ```

---

## 14. 🩺 Troubleshooting

| Problem | Cause / fix |
|---|---|
| `'java' is not recognized` / `command not found` | Java not installed or not in PATH. Install JDK 17+, then open a **new** terminal. |
| `'npm' is not recognized` | Install Node.js 18+, then open a **new** terminal. |
| Backend error `release version 17 not supported` or `UnsupportedClassVersionError` | Your Java is older than 17. Check `java -version`; set `JAVA_HOME` to the new JDK. |
| `Port 8080 was already in use` | Another program uses 8080. Stop it, or change `server.port` **and** the proxy target in `vite.config.js`. Windows: `netstat -ano \| findstr :8080` then `taskkill /PID <pid> /F`. macOS/Linux: `lsof -i :8080`. |
| `Port 5173 is already in use` | An old frontend is still running; close it (or `npx kill-port 5173`). |
| Browser shows "Cannot reach the server" or "Server error or backend not running" | The backend is not running or still starting. Wait for `Started ChatApplication` in the backend window. |
| Sidebar shows "○ Reconnecting…" | WebSocket can't connect: backend down, or you opened the app on a different port than 5173. |
| First start "hangs" on `Downloading ...` | Normal, it is downloading Maven and libraries (≈150 MB). Behind a company proxy, configure Maven/npm proxy settings. |
| `mvnw: Permission denied` (macOS/Linux) | `chmod +x backend/mvnw` (start.sh does this for you). |
| `npm install` errors (`EACCES`, network) | Check internet, delete `frontend/node_modules` and `package-lock.json` only if asked by the error, then run `npm install` again. |
| Logged in as the same user in both windows | Browser tabs share the login. Use two different browsers or an Incognito window. |
| H2 console: "Database not found" | Use exactly `jdbc:h2:file:./data/chatdb` and start the backend from the `backend` folder (the scripts do this). |
| Strange database state | Stop the backend and delete the `backend/data` folder for a fresh empty database. |

---

## 15. ⚠️ Limitations and possible improvements

**LIVE status (simplified mechanism):**
- LIVE means "a chat screen is open, the tab is visible, and heartbeats arrive". It cannot tell whether the person is actually looking at the screen or typing. A tab left open and visible stays LIVE.
- After a crash or network loss, LIVE can stay up to **45 s** (heartbeat timeout), and Online until the WebSocket heartbeat (≈10–20 s) detects the dead connection.
- Presence is stored **in server memory**: a backend restart resets it (clients reconnect automatically and re-announce themselves), and it would not be shared between several backend instances.
- *Improvement ideas:* typing indicators / "last activity" timestamps, storing presence in Redis, configurable idle timeout per user.

**Other prototype limitations:**
- The in-memory simple broker and token blacklist work for **one** backend instance. Scaling out would need an external broker (RabbitMQ/ActiveMQ via STOMP relay) and Redis.
- The JWT is kept in `localStorage` (simple; an HttpOnly cookie would resist XSS better).
- History loads the latest 100–200 messages; there is no "load older" pagination or message search yet.
- No read receipts, file/image sharing, message edit/delete, blocking, or email verification.
- No rate limiting on login or messages.
- H2 is great for demos; use MySQL/PostgreSQL for production (see section 12).

---

## 16. 🧹 Stopping, resetting and uninstalling

- **Stop:** Windows: close the BACKEND and FRONTEND windows. macOS/Linux: press `Ctrl+C` in the terminal running `start.sh`.
- **Reset all data (users, messages):** stop the backend and delete the folder `backend/data`.
- **Free disk space:** delete `frontend/node_modules` and `backend/target`. The Maven download cache is in `~/.m2` (Windows: `C:\Users\<you>\.m2`) and can also be deleted.
- **Uninstall:** delete the `chat-app` folder.

---

*Built with Spring Boot 3.5, React 18, STOMP over WebSocket and H2.*
