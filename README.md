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

## 🛠️ Installation & Running
### **1. Clone the repository**
```sh
git clone https://github.com/your-repo/javelin-was.git
cd javelin-was
```

### **2. Build & Run (Using Gradle)**
```sh
./gradlew run
```

### **3. Test the Server**
Once the server is running, open your browser or use `curl`:
```sh
curl http://localhost:8080
```
✅ **Expected Response:**
```
Hello from Javelin Virtual Thread WAS!
```

---

## 🛠️ How It Works
Javelin WAS uses **Java 21+ Virtual Threads** to efficiently handle HTTP requests without the overhead of traditional thread pools. Each request is processed in a lightweight Virtual Thread, allowing **massive concurrency with minimal resource usage**.

---

## 📜 License
This project is licensed under the **Apache License 2.0**.  
For details, see the [`LICENSE`](LICENSE) file.

---

## 👥 Contributing
We welcome contributions!  
Feel free to submit issues, feature requests, or pull requests.

---

## 📧 Contact
For inquiries or collaboration, contact: **[krkarma777@gmail.com]**

🚀 **Let's build a next-generation Java WAS together!** 🚀
