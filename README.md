<p align="center">
  <img src="https://github.com/user-attachments/assets/16897406-0432-4734-a59f-26f85e5cf038" alt="logo" width="400" />
</p>

# Javelin WAS - A High-Performance Virtual Thread-Based Web Application Server 🚀

Javelin WAS is a lightweight, high-performance web application server built on Java Virtual Threads.  
It aims to provide a modern alternative to traditional Java-based WAS solutions like Tomcat and Jetty, leveraging Virtual Threads for maximum concurrency and efficiency.

---

## 🔥 Features
- **Virtual Thread-Based Concurrency** – Handles thousands of concurrent requests efficiently without blocking OS threads.
- **Lightweight & Fast** – Designed to be minimal and optimized for high performance.
- **Easy Routing** – Supports simple and flexible request handling.
- **Middleware System** – Easily extendable with logging, authentication, and security middleware.
- **JSON Support** – Built-in JSON serialization and deserialization.
- **Static File Serving** – Serves static files efficiently without external dependencies.
- **Designed for Modern Java Applications** – No heavy frameworks required.

---

## ☕ Requirements
- Java **21** or higher (required for Virtual Threads)
- Gradle **7+**

---

## 🛠️ Installation & Running

### 1. Clone the repository
```sh
git clone https://github.com/your-repo/javelin-was.git
cd javelin-was
```

### 2. Build & Run (Using Gradle)
```sh
./gradlew run
```

---

## 🚀 Test the Server

Once the server is running, open your browser or use `curl`:

```sh
curl http://localhost:8080
```

✅ **Expected Response:**
```
Hello from Javelin Virtual Thread WAS!
```

#### ✅ Example Code (Minimal)
```java
public class Main {
    public static void main(String[] args) {
        VirtualThreadServer server = new VirtualThreadServer(8080);

        server.get("/", ctx -> ctx.send("Hello from Javelin Virtual Thread WAS!"));

        server.start();
    }
}
```

---

## 🧪 Example Usage

### 🧩 Middleware
```java
server.use(ctx -> {
    System.out.println("Request Path: " + ctx.path());
    ctx.next();
});
```

### 📦 JSON Handling
```java
server.post("/echo", ctx -> {
    Map<String, Object> data = ctx.body(Map.class);
    ctx.json(Map.of("you_sent", data));
});
```

---

## 📁 Serving Static Files
To serve files from a `public/` directory under `/static`:

```java
server.use(new StaticFileHandler("/static", "public"));
```

Then access:
```
http://localhost:8080/static/index.html
```

---

## 🧠 How It Works

Javelin WAS uses **Java 21+ Virtual Threads** to efficiently handle HTTP requests without the overhead of traditional thread pools.  
Each request is processed in a lightweight Virtual Thread, allowing **massive concurrency with minimal resource usage**.

---

## 📜 License
This project is licensed under the **Apache License 2.0**.  
See the [`LICENSE`](LICENSE) file for more details.

---

## 👥 Contributing
We welcome contributions!  
Feel free to submit issues, feature requests, or pull requests.

---

## 📧 Contact
For inquiries or collaboration:  
**📩 krkarma777@gmail.com**

---
