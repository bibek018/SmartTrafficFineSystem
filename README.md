# 🚦 Smart Traffic Fine System

### Java Swing Desktop Application using OpenCV + Tesseract OCR + MySQL

![Java](https://img.shields.io/badge/Java-17-orange)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-blue)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
![OpenCV](https://img.shields.io/badge/OpenCV-4.13.0-green)
![OCR](https://img.shields.io/badge/OCR-Tesseract-red)
![Maven](https://img.shields.io/badge/Build-Maven-purple)

---

# 📌 Project Overview

Smart Traffic Fine System is a desktop-based traffic violation management system developed as a college mini project using Java Swing.

The system uses:
- 📹 OpenCV for live webcam feed and number plate detection
- 🔍 Tesseract OCR for automatic number plate reading
- 🗄️ MySQL database for vehicle and fine management
- 👥 Role-based authentication for Officers and Vehicle Owners

Traffic officers can detect vehicles, issue fines, capture evidence snapshots, and manage violations digitally.

Vehicle owners can:
- View fines
- Pay fines
- Dispute fines
- Track fine status

---

# ✨ Features

| Feature | Description |
|---|---|
| 🎥 Live Camera Feed | Real-time webcam stream using OpenCV |
| 🔍 Number Plate Detection | Detects license plates using Haar Cascade |
| 📖 OCR Plate Reading | Reads vehicle number automatically using Tesseract OCR |
| 👮 Admin Dashboard | Officer controls and management |
| 👤 Owner Dashboard | Vehicle owner portal |
| ⚡ Fine Issuing System | Issue fines digitally |
| 📸 Evidence Capture | Saves vehicle snapshot automatically |
| 🚗 Vehicle Management | Register and manage vehicles |
| 📋 Fine Tracking | View pending, paid, disputed fines |
| 💰 Payment Support | Owners can mark fines as paid |
| 📊 Dashboard Statistics | Live fine counts and analytics |
| 🔐 Role Based Login | Separate access for admin and owner |

---

# 🛠️ Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | JDK 17+ | Core Programming |
| Java Swing | Built-in | Desktop GUI |
| OpenCV | 4.13.0 | Camera + Plate Detection |
| Tesseract OCR | 5.x | OCR Recognition |
| Tess4J | 5.11.0 | Java OCR Wrapper |
| MySQL | 8.0+ | Database |
| Maven | 3.x | Dependency Management |
| FlatLaf | 3.7.1 | Modern UI Theme |
| IntelliJ IDEA | 2026.x | Development IDE |

---

# 📁 Project Structure

```text
SmartTrafficFineSystem/
│
├── src/
│   └── com/trafficfine/
│
│       ├── main/
│       │   └── Main.java
│
│       ├── ui/
│       │   ├── LoginScreen.java
│       │   ├── AdminDashboard.java
│       │   ├── CameraScreen.java
│       │   ├── AllFinesScreen.java
│       │   ├── OwnerDashboard.java
│       │   └── AddUserScreen.java
│
│       ├── camera/
│       │   └── CameraCapture.java
│
│       ├── dao/
│       │   ├── UserDAO.java
│       │   ├── VehicleDAO.java
│       │   └── FineDAO.java
│
│       ├── model/
│       │   ├── User.java
│       │   ├── Vehicle.java
│       │   ├── Fine.java
│       │   └── ViolationType.java
│
│       └── utils/
│           ├── DBConnection.java
│           └── SessionManager.java
│
├── db/
│   └── schema.sql
│
├── tessdata/
│   └── eng.traineddata
│
├── captured_violations/
│
├── lib/
│   └── opencv-4130.jar
│
├── haarcascade_russian_plate_number.xml
├── pom.xml
└── README.md
```

---

# ⚙️ Setup Instructions

# 1️⃣ Install Java JDK 17+

Download and install:
```text
https://www.oracle.com/java/technologies/downloads/
```

Verify:
```bash
java -version
```

---

# 2️⃣ Install MySQL

Download:
```text
https://dev.mysql.com/downloads/mysql/
```

Install:
- MySQL Server
- MySQL Workbench

---

# 3️⃣ Install Tesseract OCR

Download:
```text
https://github.com/UB-Mannheim/tesseract/wiki
```

Install:
```text
tesseract-ocr-w64-setup-5.x.x.exe
```

Default path:
```text
C:\Program Files\Tesseract-OCR\
```

During installation:
✅ Check English language data

---

# 4️⃣ Install OpenCV

Download OpenCV:
```text
https://github.com/opencv/opencv/releases/tag/4.13.0
```

Extract:
```text
C:\opencv\
```

---

# 5️⃣ Add OpenCV DLL Path to Environment Variables

Add this path to System PATH:

```text
C:\opencv\build\java\x64
```

Restart IntelliJ IDEA afterward.

---

# 6️⃣ Database Setup

Open MySQL Workbench and run:

```sql
source /path/to/project/db/schema.sql
```

Or manually execute:
```text
db/schema.sql
```

---

# 7️⃣ Update Database Password

Open:

```text
src/com/trafficfine/utils/DBConnection.java
```

Update:

```java
private static final String PASSWORD = "yourpassword";
```

---

# 8️⃣ Update OCR & OpenCV Paths

Open:

```text
src/com/trafficfine/camera/CameraCapture.java
```

Update paths:

```java
private static final String OPENCV_DLL =
"C:\\opencv\\build\\java\\x64\\opencv_java4130.dll";

private static final String TESS_PATH =
"C:\\Program Files\\Tesseract-OCR";

private static final String TESSDATA_PATH =
"C:\\Program Files\\Tesseract-OCR\\tessdata";
```

---

# 9️⃣ Required Files

Place these files in project root:

```text
haarcascade_russian_plate_number.xml
tessdata/eng.traineddata
```

Download Haar Cascade:

```text
https://github.com/opencv/opencv/blob/master/data/haarcascades/haarcascade_russian_plate_number.xml
```

---

# 🔟 Run Project

Main Class:

```text
com.trafficfine.main.Main
```

Run using IntelliJ IDEA.

Expected Console Output:

```text
OpenCV loaded OK
Tesseract initialized OK
Camera started successfully
```

---

# 🔐 Default Login Credentials

| Role | Username | Password |
|---|---|---|
| 👮 Admin | admin | admin123 |
| 👤 Owner | owner1 | owner123 |

---

# 🔄 System Workflow

```text
Officer starts camera
        ↓
OpenCV detects vehicle plate
        ↓
OCR reads plate number
        ↓
Vehicle searched in database
        ↓
Officer selects violation type
        ↓
Fine generated automatically
        ↓
Evidence image saved
        ↓
Owner logs in
        ↓
Owner pays or disputes fine
        ↓
Database updated
```

---

# 🗄️ Database Tables

| Table | Purpose |
|---|---|
| users | Stores admin and owner accounts |
| vehicles | Stores registered vehicles |
| violation_types | Fine categories and amounts |
| fines | Stores issued fines and status |

---

# 📸 OCR Demo Tips

For best OCR accuracy:

- Use bright lighting
- Keep number plate clearly visible
- Avoid blurry camera feed
- Keep plate close to camera
- Use printed number plates during demo

If OCR fails:
- Manually edit the detected text

---

# 🚀 Future Improvements

- AI-based plate recognition
- Automatic speed detection
- Email/SMS fine notifications
- Online payment gateway
- Cloud database integration
- Mobile application support
- Real-time traffic analytics

---


# 📄 License

This project is developed for educational and academic purposes.

---