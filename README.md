# 📁 VVF AI Smart File Manager Ultra

VVF AI Smart File Manager Ultra is a dual-platform file management and analysis system. It features a native **Android application** (Jetpack Compose, Clean Architecture, on-device vector math) and a **Full-Stack Web Sandbox Simulator** (React + Express + Vite + Gemini AI integration). This dual design allows for production mobile deployment while enabling instant, live browser-based previews and simulation of full-scale cloud storage, scanning algorithms, and AI assistance.

---

## 📖 Project Overview

This repository contains two main modules:
1. **Native Android Application (`/app`)**: Built with Kotlin and Jetpack Compose following strict Clean Architecture and MVVM guidelines. It features offline-first searching, local SQLite persistence using Room, on-device word embeddings for similarity searching, and memory safety with StrictMode and LeakCanary.
2. **Web Sandbox Simulator (`/`)**: A highly polished, responsive React single-page application paired with a robust Express server. It serves as a visual playground to test real duplicate scanner heuristics (Exact SHA-256, Perceptual Name/Size, Semantic Cosine Similarity), simulate Secure Folder AES-256 encryption vaults, and interface with Google Gemini AI for smart file diagnostics.

---

## 🏗️ Architectural Overview & Data Flow

Both modules strictly adhere to Unidirectional Data Flow (UDF) patterns, cleanly separating representation from business logic.

```
                                      +------------------------------------+
                                      |     Jetpack Compose / React UI     |
                                      | (LocalFileManager, CloudManager)   |
                                      +-----------------+------------------+
                                                        |
                                               Observes StateFlows / Hooks
                                                        |
                                                        v
                                      +------------------------------------+
                                      |          ViewModel / State         |
                                      |   (FileViewModel, CloudViewModel)  |
                                      +-----------------+------------------+
                                                        |
                                              Launches Jobs / Actions
                                                        |
                                                        v
                                      +------------------------------------+
                                      |       Engine & Service Layer       |
                                      |  (DuplicateDetector, SearchService)|
                                      +--------+------------------+--------+
                                                |                  |
                                      Fetches Local Files   Validates Vectors
                                                |                  |
                                                v                  v
                                      +--------+------------------+--------+
                                      |         Storage & DB Cache         |
                                      |    (Room SQLite / In-Memory JSON)  |
                                      +--------+------------------+--------+
                                                |                  |
                                        Cache NOT EXISTS     Loads Cache
                                                |                  |
                                                v                  v
                                      +--------+------------------+--------+
                                      |           AI Co-Pilot              |
                                      |   (On-Device / Gemini API Proxy)   |
                                      +------------------------------------+
```

### Core Components
* **Modern UI Engine**: Custom styled layouts utilizing Jetpack Compose on Android and Tailwind CSS + Motion on Web, prioritizing high visual density and fluid transitions.
* **Multi-Tier Duplicate Scanner**: Executes exact byte-level, syntactic, and vector-similarity comparisons to group and purge duplicates.
* **On-Device Word Projection & Vector DB**: An offline-first algorithm converting file categories and naming structures into 128-dimensional normalized float arrays.
* **AES-256 Armed Secure Folder**: Encrypts/decrypts secure file nodes via cryptographic streams (simulated on Web, native Cryptographic API on Android).

---

## 🔬 Multi-Tier Duplicate Detection (3 Scanning Modes)

The duplicate detector groups and categorizes files using three distinct physical and semantic profiles:

1. **Exact Duplicate (Byte & SHA-256 Hash)**
   - **Mechanism**: Reads files using an efficient `8KB` buffer stream to compute their SHA-256 checksums. Identical hashes represent identical bytes.
   - **Properties**: High speed, zero false positives.

2. **Near Duplicate (Perceptual Name & Size)**
   - **Mechanism**: Strips common duplication artifacts (e.g., ` copy`, `_copy`, `(1)`, ` - Copy`) and groups names. Shifts the name signature against corresponding file sizes to group copies that have small variations or adjusted metadata.
   - **Properties**: Catches slightly modified exports, downloaded images, and duplicated documents.

3. **Semantic Duplicate (Vector Cosine Similarity)**
   - **Mechanism**: Maps file tags, paths, and content categories to a 128-dimensional space. Computes the cosine similarity:
     $$\text{Cosine Similarity} = \frac{\mathbf{A} \cdot \mathbf{B}}{\|\mathbf{A}\| \|\mathbf{B}\|}$$
   - **Properties**: Identifies conceptually related files (e.g., pairing `quarterly_receipt.pdf` with `tax_invoice.jpg`) where similarity is $\ge 0.88$.

---

## ⚙️ Installation & Setup

### Prerequisites
- **For Android Development**: Android Studio (Ladybug or newer) and JDK 17+.
- **For Web Simulator & Scripts**: Node.js (v18 or newer), npm (v9 or newer), and Python 3.

### 1. Web Simulator Setup (Vite + Express Backend)

To install dependencies and start the local sandbox environment:

```bash
# 1. Install Node.js package dependencies
npm install

# 2. Create the Python virtual environment
python3 -m venv venv --without-pip

# 3. Configure environment variables
# Copy the template env file
cp .env.example .env

# Edit .env and enter your Gemini API Key for server-side AI assistance:
# GEMINI_API_KEY="your_api_key_here"
```

To run the web application:
```bash
# Start the development server (runs on port 3000)
npm run dev

# Build the production bundle
npm run build

# Start the compiled production server
npm run start
```

### 2. Native Android Application Setup (Gradle)

To import and build the Android mobile app using the command line:

```bash
# Ensure execute permissions for gradlew
chmod +x gradlew

# Clean and compile the Android build variants
./gradlew clean assembleDebug

# Run unit and integration tests
./gradlew test
```

---

## 💡 Usage Examples

### 1. Web Sandbox Interface
Open `http://localhost:3000` in your browser. The application features a clean, responsive single-screen dashboard:
* **Storage Distribution Chart**: Displays physical storage distribution segmented by audio, video, documents, and secure vaults.
* **Duplicate Scanner Panel**: Run any of the three scans (Exact, Near, Semantic) on mock filesystems, review duplicate groups, and delete unnecessary duplicates instantly.
* **AES-256 Vault**: Drag and drop sensitive files into the "Secure Folder", type a passcode to encrypt, and lock the safe to hide items from general directory listings.
* **Sync Manager**: Simulate live synchronizations to Google Drive or custom GitHub repositories.
* **AI Assistant**: Interact with the Gemini-powered sidebar to ask for recommendations on freeing up storage space, finding loose duplicate patterns, or reviewing file health.

### 2. Client-Server API Routes
The Express backend (`/server.ts`) exposes several useful sandbox APIs:
* `GET /api/health`: Returns server status.
* `POST /api/chat`: Proxies prompt payloads directly to Gemini server-side.

---

## 🤝 Contribution Guidelines

We welcome contributions to both the Android code and Web Simulator modules. To maintain high code quality, please adhere to these standards:

### Android Code Guidelines
* **MVVM & UDF**: All Composables must consume state as read-only Kotlin `StateFlow` types from a ViewModel. Actions must be passed upwards as clean lambdas.
* **Clean Architecture**: Place data models and room tables in `data/`, repository abstractions/business logic in `domain/`, and UI components/view-models in `presentation/`.
* **Resource Optimization**: Ensure all IO streams are properly closed using `.use { ... }` or automatic resources management to prevent leaks, verified via StrictMode.

### Web Simulator Guidelines
* **Tailwind & Responsive Styling**: Style elements using Tailwind utility classes directly. Ensure layouts are fluid and adapt from small mobile screens up to wide desktops.
* **Component Modularity**: Keep file sizes manageable by dividing components cleanly (e.g. `StorageDashboard`, `SecureFolder`).
* **Safe API Keys**: Keep secrets strictly on the server-side (`server.ts` or `process.env`). Never expose keys to client-side react components.

### Verification Steps
Before opening a Pull Request, verify your changes compile and pass the linter:
```bash
# Check code syntax and imports
npm run lint

# Build the bundle to ensure no compile-time regressions
npm run build
```

---

## 🛡️ License

This project is licensed under the MIT License - see the LICENSE file for details.
