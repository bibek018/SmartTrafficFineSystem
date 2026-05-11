# Smart Traffic Fine System
## Java Swing + OpenCV + MySQL | College Mini Project

---

## Project Structure
```
SmartTrafficFineSystem/
├── src/com/trafficfine/
│   ├── main/         → Main.java (entry point)
│   ├── ui/           → All Swing screens
│   ├── camera/       → OpenCV CameraCapture.java
│   ├── dao/          → UserDAO, VehicleDAO, FineDAO
│   ├── model/        → User, Vehicle, Fine, ViolationType
│   └── utils/        → DBConnection, SessionManager
├── db/
│   └── schema.sql    → Run this in MySQL first
├── lib/              → Put all JARs here
├── captured_violations/ → Auto-created; stores snapshots
└── haarcascade_russian_plate_number.xml  ← Download this
```

---

## Setup Instructions (IntelliJ IDEA)

### Step 1 — MySQL Setup
1. Open MySQL Workbench or terminal
2. Run: `source /path/to/db/schema.sql`
3. Update `DBConnection.java` with your MySQL password

### Step 2 — Download Required JARs (put in /lib)
| JAR | Download |
|-----|----------|
| mysql-connector-j-8.x.jar | https://dev.mysql.com/downloads/connector/j/ |
| opencv-4xx.jar | https://opencv.org/releases/ (Java bindings) |
| flatlaf-3.x.jar | https://github.com/JFormDesigner/FlatLaf/releases |

### Step 3 — Add JARs in IntelliJ
- File → Project Structure → Libraries → + → Java → select all JARs

### Step 4 — OpenCV Native Library
1. Download OpenCV for Windows from opencv.org
2. Run/Install to `C:\opencv\`
3. In IntelliJ: Run → Edit Configurations → VM Options:
   ```
   -Djava.library.path=C:\opencv\build\java\x64
   ```
   (or x86 for 32-bit JDK)

### Step 5 — Haar Cascade XML
Download from OpenCV GitHub:
`https://github.com/opencv/opencv/blob/master/data/haarcascades/haarcascade_russian_plate_number.xml`
Place in project ROOT directory (same level as src/).

### Step 6 — Run
- Main class: `com.trafficfine.main.Main`
- Login: admin/admin123 (Admin) or owner1/owner123 (Owner)

---

## Features
- ✅ Live webcam feed with OpenCV
- ✅ License plate region detection (Haar Cascade)
- ✅ Snapshot saved on violation issuance
- ✅ Role-based login (Admin/Owner)
- ✅ Issue, search, pay, dispute fines
- ✅ Vehicle registration & management
- ✅ Dashboard with stats
- ✅ Dark theme UI

---

## Default Credentials
| Role  | Username | Password |
|-------|----------|----------|
| Admin | admin    | admin123 |
| Owner | owner1   | owner123 |
