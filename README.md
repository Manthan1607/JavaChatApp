#  JavaChatApp

A real-time chat application built with **Core Java** and **Socket Programming**. Implements a classic Client-Server architecture where multiple clients can connect to a server and exchange messages in real time.

---

## Features

- **Client-Server Architecture** — Server handles all incoming connections; clients connect and communicate through it
- **Real-Time Messaging** — Instant message delivery using Java Sockets
- **Multi-threaded Server** — Each client runs on its own thread, allowing simultaneous connections
- **Console-based Interface** — Lightweight, no GUI dependencies required

---

## Project Structure

```
JavaChatApp/
├── Server.java       # Server — listens for connections, broadcasts messages
├── Client.java       # Client — connects to server, sends/receives messages
└── README.md         # This file
```

---

## 🛠️ Tech Stack

| Technology | Usage |
|---|---|
| Java (JDK 8+) | Core language |
| `java.net.Socket` | Client-side connection |
| `java.net.ServerSocket` | Server-side listener |
| `java.io.*` | Input/Output streams |
| `java.lang.Thread` | Multi-threading per client |

---

 How to Run

 Prerequisites
- Java JDK 8 or above installed
- Terminal / Command Prompt

Step 1 — Clone the Repository
```bash
git clone https://github.com/Manthan1607/JavaChatApp.git
cd JavaChatApp
```

Step 2 — Compile
```bash
javac Server.java
javac Client.java
```
Step 3 — Start the Server
```bash
java Server
```
> Server starts listening on the configured port (default: `1234`)

Step 4 — Start a Client (in a new terminal)
```bash
java Client
```
> Open multiple terminals and run `java Client` in each to simulate multiple users.

---

 Configuration

To change the port, update this line in both `Server.java` and `Client.java`:

```java
// Server.java
ServerSocket serverSocket = new ServerSocket(1234); // change 1234

// Client.java
Socket socket = new Socket("localhost", 1234); // change 1234
```

To connect from another machine, replace `"localhost"` with the server's IP address:

```java
Socket socket = new Socket("192.168.x.x", 1234);
```

---

##How It Works

```
Client A ──┐
           ├──► Server (broadcasts) ──► All connected clients
Client B ──┘
```

1. Server starts and waits for connections on a port
2. Each client connects via `Socket`
3. Server spawns a new `Thread` for each client
4. Messages from one client are broadcast to all others
5. Connection closes when client types `exit`

---
 Concepts Demonstrated

- Socket Programming (`Socket`, `ServerSocket`)
- Multi-threading (`Thread`, `Runnable`)
- Stream I/O (`BufferedReader`, `PrintWriter`)
- Client-Server design pattern

---

Future Improvements

- [ ] Add a GUI using Java Swing or JavaFX
- [ ] Private / direct messaging between users
- [ ] Username support on login
- [ ] Message timestamps
- [ ] File sharing over socket

---

 Author

**Manthan** — [@Manthan1607](https://github.com/Manthan1607)

BSc Computer Science, Pillai HOC College, Mumbai

---

License

This project is open source and available under the [MIT License](LICENSE).
