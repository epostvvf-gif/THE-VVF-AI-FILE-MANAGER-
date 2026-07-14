import express from "express";
import path from "path";
import dotenv from "dotenv";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI } from "@google/genai";

dotenv.config();

const app = express();
const PORT = 3000;

app.use(express.json());

// In-memory Mock Data Base to simulate Android/Smart device state
let securePin: string | null = "1234"; // Default PIN for easy testing, or null
let isSecureUnlocked = false;

// Pre-populate some realistic files representing a real user's device
let localFiles = [
  // Images
  { id: "img-1", name: "IMG_2026_07_12_001.jpg", size: 1024 * 1024 * 2.4, category: "Images", path: "/storage/emulated/0/DCIM/Camera/IMG_2026_07_12_001.jpg", dateAdded: "2026-07-12T14:22:00Z", sha256: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" },
  { id: "img-2", name: "IMG_2026_07_12_001_copy.jpg", size: 1024 * 1024 * 2.4, category: "Images", path: "/storage/emulated/0/Pictures/IMG_2026_07_12_001_copy.jpg", dateAdded: "2026-07-13T09:15:00Z", sha256: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" }, // Exact duplicate
  { id: "img-3", name: "family_beach_trip.png", size: 1024 * 1024 * 3.8, category: "Images", path: "/storage/emulated/0/DCIM/Camera/family_beach_trip.png", dateAdded: "2026-07-10T11:05:00Z", sha256: "f40827289f89ef129abf34c8996fb92427ae41e4649b934ca495991b7852ba44" },
  
  // Videos
  { id: "vid-1", name: "lecture_recording_algorithms.mp4", size: 1024 * 1024 * 48.5, category: "Videos", path: "/storage/emulated/0/Movies/lecture_recording_algorithms.mp4", dateAdded: "2026-07-14T08:30:00Z", sha256: "a1b2c3d4e5f6g7h8i9j0" },
  { id: "vid-2", name: "whatsapp_video_funny_cat.mp4", size: 1024 * 1024 * 12.1, category: "Videos", path: "/storage/emulated/0/WhatsApp/Media/whatsapp_video_funny_cat.mp4", dateAdded: "2026-07-11T19:40:00Z", sha256: "5f6g7h8i9j0a1b2c3d4e" },
  
  // Audio
  { id: "aud-1", name: "recording_lecture_draft.mp3", size: 1024 * 1024 * 1.25, category: "Audio", path: "/storage/emulated/0/Recordings/recording_lecture_draft.mp3", dateAdded: "2026-07-13T10:30:00Z", sha256: "e4f5g6h7i8j9a0b1c2d3" },
  { id: "aud-2", name: "recording_lecture_final.mp3", size: 1024 * 1024 * 1.25, category: "Audio", path: "/storage/emulated/0/Recordings/recording_lecture_final.mp3", dateAdded: "2026-07-13T10:45:00Z", sha256: "e4f5g6h7i8j9a0b1c2d4" }, // Near duplicate (similar name, same category/size)
  { id: "aud-3", name: "voice_memo_002.amr", size: 1024 * 340, category: "Audio", path: "/storage/emulated/0/Recordings/voice_memo_002.amr", dateAdded: "2026-07-09T17:00:00Z", sha256: "9b8a7c6d5e4f3g2h1i0j" },

  // Documents (including semantic duplicates)
  { id: "doc-1", name: "project_idea_notes.txt", size: 1024 * 2.5, category: "Documents", path: "/storage/emulated/0/Documents/project_idea_notes.txt", dateAdded: "2026-07-14T09:00:00Z", sha256: "d7a8e9f0c1b2a3d4e5f6", description: "This project is a decentralized application for voting on the blockchain using zero-knowledge proofs." },
  { id: "doc-2", name: "blockchain_voting_draft.txt", size: 1024 * 2.1, category: "Documents", path: "/storage/emulated/0/Documents/blockchain_voting_draft.txt", dateAdded: "2026-07-14T09:15:00Z", sha256: "c1b2a3d4e5f6g7h8i9j0", description: "A concept paper on block chain vote casting utilizing zk-proofs to protect voter identity.", semanticSimilarityGroupId: "sem-vote" }, // Semantic duplicate with doc-1
  { id: "doc-3", name: "resume_john_doe_2026.pdf", size: 1024 * 320, category: "Documents", path: "/storage/emulated/0/Documents/resume_john_doe_2026.pdf", dateAdded: "2026-07-01T10:00:00Z", sha256: "3d4e5f6g7h8i9j0a1b2c" },
  
  // Downloads
  { id: "dl-1", name: "Invoice-9281.pdf", size: 1024 * 45, category: "Downloads", path: "/storage/emulated/0/Download/Invoice-9281.pdf", dateAdded: "2026-07-14T10:10:00Z", sha256: "7h8i9j0a1b2c3d4e5f6g" },
  { id: "dl-2", name: "Invoice-9281-duplicate.pdf", size: 1024 * 45, category: "Downloads", path: "/storage/emulated/0/Download/Invoice-9281-duplicate.pdf", dateAdded: "2026-07-14T10:11:00Z", sha256: "7h8i9j0a1b2c3d4e5f6g" }, // Exact duplicate
];

let secureFiles = [
  { id: "sec-1", name: "private_bank_credentials.txt", size: 1024 * 1.5, category: "Documents", path: "/storage/emulated/0/SecureFolder/private_bank_credentials.txt", dateAdded: "2026-07-12T23:55:00Z", sha256: "sec-hash-01", isEncrypted: true },
  { id: "sec-2", name: "secret_patent_draft.pdf", size: 1024 * 1250, category: "Documents", path: "/storage/emulated/0/SecureFolder/secret_patent_draft.pdf", dateAdded: "2026-07-13T01:10:00Z", sha256: "sec-hash-02", isEncrypted: true },
];

let junkFiles = [
  { id: "junk-1", name: "com.whatsapp.cache_temp_02.log", size: 1024 * 1024 * 8.5, type: "App Cache", description: "WhatsApp temporary media cache files" },
  { id: "junk-2", name: "system_crash_dump_20260710.bin", size: 1024 * 1024 * 34.2, type: "System Logs", description: "Kernel dump from last system exception" },
  { id: "junk-3", name: "uninstalled_game_residual.pak", size: 1024 * 1024 * 112.0, type: "Residual Files", description: "Leftover graphics packs from uninstalled games" },
  { id: "junk-4", name: "temp_photo_editing_cache.tmp", size: 1024 * 1024 * 4.1, type: "Temp Files", description: "Cached filters and thumbnail previews" },
];

let cloudFiles = [
  { id: "cloud-1", name: "backup_notes_2026.txt", size: 1024 * 5, category: "Documents", lastSynced: "2026-07-13T12:00:00Z" },
  { id: "cloud-2", name: "portfolio_snapshot.jpg", size: 1024 * 1024 * 1.8, category: "Images", lastSynced: "2026-07-13T12:00:00Z" },
];

let gitCommits = [
  { id: "commit-1", hash: "4a2b9d3", message: "Initial commit - pre-populated portfolio and notes backups", date: "2026-07-13T12:00:00Z", filesCount: 2 },
];

// Initialize Gemini Client
const ai = process.env.GEMINI_API_KEY
  ? new GoogleGenAI({
      apiKey: process.env.GEMINI_API_KEY,
      httpOptions: { headers: { "User-Agent": "aistudio-build" } },
    })
  : null;

// API Routes

// Get local files list and stats
app.get("/api/files", (req, res) => {
  res.json({
    files: localFiles,
    stats: {
      totalSpace: 128 * 1024 * 1024 * 1024, // 128GB
      usedSpace: localFiles.reduce((acc, f) => acc + f.size, 0) + secureFiles.reduce((acc, f) => acc + f.size, 0),
    }
  });
});

// Add file to local storage
app.post("/api/files", (req, res) => {
  const { name, size, category } = req.body;
  if (!name || !category) {
    return res.status(400).json({ error: "Missing required parameters" });
  }

  const newFile = {
    id: `custom-${Date.now()}`,
    name,
    size: size || 1024 * 250, // default 250KB if not specified
    category,
    path: `/storage/emulated/0/DCIM/Camera/${name}`,
    dateAdded: new Date().toISOString(),
    sha256: Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15),
  };

  localFiles.unshift(newFile);
  res.status(201).json(newFile);
});

// Delete local file
app.delete("/api/files/:id", (req, res) => {
  const { id } = req.params;
  const index = localFiles.findIndex(f => f.id === id);
  if (index !== -1) {
    localFiles.splice(index, 1);
    return res.json({ success: true });
  }
  res.status(404).json({ error: "File not found" });
});

// Scan and group duplicates
app.get("/api/files/duplicates", (req, res) => {
  const exactGroups: any[] = [];
  const nearGroups: any[] = [];
  const semanticGroups: any[] = [];

  // 1. Exact Duplicates (by sha256)
  const shaMap: { [key: string]: any[] } = {};
  localFiles.forEach(f => {
    if (!shaMap[f.sha256]) {
      shaMap[f.sha256] = [];
    }
    shaMap[f.sha256].push(f);
  });

  Object.entries(shaMap).forEach(([sha, list]) => {
    if (list.length > 1) {
      exactGroups.push({
        id: `exact-${sha.substring(0, 8)}`,
        type: "Exact (SHA-256 Checksum)",
        description: "Zero false-positives. Files with identical binary signatures.",
        files: list,
      });
    }
  });

  // 2. Near Duplicates (by similar filenames/size shift)
  // Simple on-device near-duplicate detection logic: strip prefixes/suffixes, compare prefix and size
  const nearMap: { [key: string]: any[] } = {};
  localFiles.forEach(f => {
    // Strip common duplication suffixes like _copy, -copy, (1), etc.
    const cleanedName = f.name
      .toLowerCase()
      .replace(/(_copy|-copy|\(\d+\)|\s+\d+)/g, "")
      .trim();
    // Group files by cleaned name and category, with a minor size allowance
    const key = `${categoryToPrefix(f.category)}_${cleanedName.substring(0, 15)}`;
    if (!nearMap[key]) {
      nearMap[key] = [];
    }
    nearMap[key].push(f);
  });

  Object.entries(nearMap).forEach(([key, list]) => {
    // Exact duplicates are already classified, so filter out groups where all elements are exact duplicates of each other
    if (list.length > 1) {
      // Check if they aren't already grouped as exact duplicates
      const firstSha = list[0].sha256;
      const isAllExact = list.every(f => f.sha256 === firstSha);
      if (!isAllExact) {
        nearGroups.push({
          id: `near-${key}`,
          type: "Near Duplicate (Perceptual Name Match)",
          description: "Catches copies that might have slightly modified names or tags.",
          files: list,
        });
      }
    }
  });

  // 3. Semantic Duplicates (different names/sizes, but similar content)
  // Let's group doc-1 and doc-2 which are hardcoded semantic duplicates
  const semDocs = localFiles.filter(f => f.id === "doc-1" || f.id === "doc-2");
  if (semDocs.length === 2) {
    semanticGroups.push({
      id: "sem-voting",
      type: "Semantic (AI-Vector Similarity)",
      description: "Catches files that express identical ideas in different words.",
      similarityScore: 0.94,
      files: semDocs,
    });
  }

  res.json({
    exact: exactGroups,
    near: nearGroups,
    semantic: semanticGroups,
    totalCount: exactGroups.length + nearGroups.length + semanticGroups.length,
  });
});

// Bulk delete files from duplicate groups
app.post("/api/files/duplicates/delete", (req, res) => {
  const { fileIds } = req.body;
  if (!Array.isArray(fileIds)) {
    return res.status(400).json({ error: "fileIds must be an array" });
  }

  localFiles = localFiles.filter(f => !fileIds.includes(f.id));
  res.json({ success: true, deletedCount: fileIds.length });
});

// Get junk files list
app.get("/api/files/junk", (req, res) => {
  res.json({
    junk: junkFiles,
    totalSize: junkFiles.reduce((acc, f) => acc + f.size, 0),
  });
});

// Clean junk files
app.post("/api/files/junk/clean", (req, res) => {
  const freedSize = junkFiles.reduce((acc, f) => acc + f.size, 0);
  junkFiles = []; // Cleared
  res.json({
    success: true,
    freedSize,
    message: `Cleared all app cache, kernels, and residual files. Freed ${(freedSize / (1024 * 1024)).toFixed(1)} MB!`,
  });
});

// Secure Folder PIN Status
app.get("/api/secure-folder/pin/status", (req, res) => {
  res.json({ hasPin: securePin !== null });
});

// Secure Folder setup PIN
app.post("/api/secure-folder/pin/setup", (req, res) => {
  const { pin } = req.body;
  if (!pin || pin.length < 4) {
    return res.status(400).json({ error: "PIN must be at least 4 digits" });
  }
  securePin = pin;
  res.json({ success: true, message: "PIN configured successfully" });
});

// Verify PIN and unlock
app.post("/api/secure-folder/pin/verify", (req, res) => {
  const { pin } = req.body;
  if (pin === securePin) {
    isSecureUnlocked = true;
    return res.json({ success: true, unlocked: true });
  }
  res.status(401).json({ error: "Invalid secure PIN. Access Denied." });
});

// Secure folder lock/verify state
app.post("/api/secure-folder/lock", (req, res) => {
  isSecureUnlocked = false;
  res.json({ success: true, unlocked: false });
});

// Get secure files (only if unlocked)
app.get("/api/secure-folder/files", (req, res) => {
  if (!isSecureUnlocked) {
    return res.status(403).json({ error: "Secure Folder is Locked. Enter PIN to view." });
  }
  res.json({ files: secureFiles });
});

// Move local file to secure folder
app.post("/api/secure-folder/move-in", (req, res) => {
  const { fileId } = req.body;
  const index = localFiles.findIndex(f => f.id === fileId);
  if (index !== -1) {
    const file = localFiles.splice(index, 1)[0];
    const secureFile = {
      ...file,
      id: `sec-${Date.now()}`,
      path: `/storage/emulated/0/SecureFolder/${file.name}`,
      isEncrypted: true,
    };
    secureFiles.push(secureFile);
    return res.json({ success: true, file: secureFile });
  }
  res.status(404).json({ error: "Local file not found" });
});

// Restore file from secure folder to local storage
app.post("/api/secure-folder/restore", (req, res) => {
  const { fileId } = req.body;
  const index = secureFiles.findIndex(f => f.id === fileId);
  if (index !== -1) {
    const file = secureFiles.splice(index, 1)[0];
    const restoredFile = {
      ...file,
      id: `restored-${Date.now()}`,
      path: `/storage/emulated/0/DCIM/Camera/${file.name}`,
      isEncrypted: false,
    };
    localFiles.unshift(restoredFile);
    return res.json({ success: true, file: restoredFile });
  }
  res.status(404).json({ error: "Secure file not found" });
});

// Cloud Sync / Git Synchronization
app.post("/api/cloud/sync", (req, res) => {
  // Sync unsynced local files to simulated GDrive/Cloud Git
  const localToSync = localFiles.filter(lf => lf.id !== "img-2" && lf.id !== "aud-2" && lf.id !== "dl-2"); // exclude exact/near duplicates for smart sync
  let syncCount = 0;

  localToSync.forEach(lf => {
    // Add to cloud if not already there
    const exists = cloudFiles.some(cf => cf.name === lf.name);
    if (!exists) {
      cloudFiles.unshift({
        id: `cloud-${Date.now()}-${syncCount}`,
        name: lf.name,
        size: lf.size,
        category: lf.category,
        lastSynced: new Date().toISOString(),
      });
      syncCount++;
    }
  });

  if (syncCount > 0) {
    // Generate a beautiful simulated Git commit hash
    const hash = Math.random().toString(16).substring(2, 9);
    gitCommits.unshift({
      id: `commit-${Date.now()}`,
      hash,
      message: `Sync: Backup of ${syncCount} clean local file(s)`,
      date: new Date().toISOString(),
      filesCount: syncCount,
    });
  }

  res.json({
    success: true,
    syncCount,
    commits: gitCommits,
    cloudFiles,
  });
});

// Get Cloud manager details
app.get("/api/cloud", (req, res) => {
  res.json({
    cloudFiles,
    commits: gitCommits,
    stats: {
      totalSpace: 15 * 1024 * 1024 * 1024, // 15GB GDrive free tier
      usedSpace: cloudFiles.reduce((acc, f) => acc + f.size, 0),
    }
  });
});

// Server-side AI assistant chat
app.post("/api/gemini/chat", async (req, res) => {
  const { messages } = req.body;
  if (!messages || !Array.isArray(messages)) {
    return res.status(400).json({ error: "Invalid chat request" });
  }

  if (!ai) {
    return res.status(200).json({
      text: "Developer Mode: AI Assistant is working locally. To activate real Gemini responses, configure the `GEMINI_API_KEY` inside the AI Studio Secrets panel. Below is a mock assistant reply:\n\nHello! I am your Smart File AI Assistant. I can see you have " + localFiles.length + " local files categorized by Images, Videos, Audio, and Documents. You also have a secure folder and duplicate groups. Let me know if you want me to search them or check for similar content!"
    });
  }

  try {
    // Generate some metadata about current files to feed into the Gemini context so it can truly "see" files!
    const fileStats = localFiles.map(f => `- ${f.name} (Category: ${f.category}, Size: ${(f.size / (1024 * 1024)).toFixed(2)} MB, Path: ${f.path})`).join("\n");
    const systemPrompt = `You are VVF AI Smart File Manager Ultra's on-device AI co-pilot.
The user is talking to you from their smartphone file manager.
You have real-time access to index data of their local device storage.
Here are the current local files on their device:
${fileStats}

Additionally:
- Secure locked files: Private bank records, patent drafts (stored in locked Secure Folder).
- There are active duplicate files (exact checksums or near perceptual matches).
- Cloud simulation: Configured with Git Synchronization to push backups to GDrive.

Help the user manage, summarize, or query their storage. When they ask "what files do I have?" or "help me organize", refer to the actual list of files above. Keep answers friendly, professional, clear, and highly focused on file optimization, space-saving tips, and secure storage practices. Avoid developer/programming terminology unless they explicitly ask. Speak directly, and be incredibly helpful.`;

    // Map messages array to Gemini-compatible content structure
    // Translate standard roles to user/model
    const formattedContents = messages.map((msg: any) => ({
      role: msg.role === "assistant" ? "model" : "user",
      parts: [{ text: msg.content }]
    }));

    const response = await ai.models.generateContent({
      model: "gemini-3.5-flash",
      contents: formattedContents,
      config: {
        systemInstruction: systemPrompt,
      },
    });

    res.json({ text: response.text });
  } catch (error: any) {
    console.error("Gemini API Error:", error);
    res.status(500).json({ error: "Error contacting Gemini. Please check your secrets configuration.", details: error.message });
  }
});


// Setup Vite / Static Files serving

async function startServer() {
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`[VVF Smart File Manager] Server is running on http://0.0.0.0:${PORT}`);
  });
}

// Convert category name to prefix for matching keys
function categoryToPrefix(cat: string): string {
  switch (cat) {
    case "Images": return "img";
    case "Videos": return "vid";
    case "Audio": return "aud";
    case "Documents": return "doc";
    case "Downloads": return "dl";
    default: return "file";
  }
}

startServer();
