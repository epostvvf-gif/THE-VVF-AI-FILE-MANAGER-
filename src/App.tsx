import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  HardDrive, FolderOpen, Cloud, Lock, Bot, Sparkles, RefreshCw, 
  HelpCircle 
} from "lucide-react";
import StorageDashboard from "./components/StorageDashboard";
import LocalFileManager from "./components/LocalFileManager";
import CloudManager from "./components/CloudManager";
import SecureFolder from "./components/SecureFolder";
import AiAssistant from "./components/AiAssistant";
import { StorageFile, CloudFile, GitCommit, StorageStats } from "./types";

export default function App() {
  const [activeTab, setActiveTab] = useState<string>("dashboard");
  const [localFiles, setLocalFiles] = useState<StorageFile[]>([]);
  const [cloudFiles, setCloudFiles] = useState<CloudFile[]>([]);
  const [gitCommits, setGitCommits] = useState<GitCommit[]>([]);
  const [storageStats, setStorageStats] = useState<StorageStats>({
    totalSpace: 128 * 1024 * 1024 * 1024,
    usedSpace: 0,
  });
  const [cloudStats, setCloudStats] = useState<StorageStats>({
    totalSpace: 15 * 1024 * 1024 * 1024,
    usedSpace: 0,
  });
  const [isRefreshing, setIsRefreshing] = useState(false);

  const fetchLocalFiles = async () => {
    try {
      const res = await fetch("/api/files");
      const data = await res.json();
      setLocalFiles(data.files || []);
      setStorageStats(data.stats || { totalSpace: 128 * 1024 * 1024 * 1024, usedSpace: 0 });
    } catch (e) {
      console.error("Error fetching local files:", e);
    }
  };

  const fetchCloudDetails = async () => {
    try {
      const res = await fetch("/api/cloud");
      const data = await res.json();
      setCloudFiles(data.cloudFiles || []);
      setGitCommits(data.commits || []);
      setCloudStats(data.stats || { totalSpace: 15 * 1024 * 1024 * 1024, usedSpace: 0 });
    } catch (e) {
      console.error("Error fetching cloud details:", e);
    }
  };

  const handleGlobalRefresh = async () => {
    setIsRefreshing(true);
    await Promise.all([fetchLocalFiles(), fetchCloudDetails()]);
    setTimeout(() => setIsRefreshing(false), 800);
  };

  useEffect(() => {
    fetchLocalFiles();
    fetchCloudDetails();
  }, []);

  const handleMoveToSecure = async (fileId: string) => {
    try {
      const res = await fetch("/api/secure-folder/move-in", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fileId }),
      });
      if (res.ok) {
        handleGlobalRefresh();
      }
    } catch (e) {
      console.error(e);
    }
  };

  const tabs = [
    { id: "dashboard", label: "Dashboard", icon: HardDrive },
    { id: "local_storage", label: "Local Files", icon: FolderOpen },
    { id: "cloud", label: "GDrive & Git", icon: Cloud },
    { id: "secure_vault", label: "Secure Folder", icon: Lock },
    { id: "ai_assistant", label: "AI Co-pilot", icon: Bot },
  ];

  return (
    <div className="min-h-screen bg-gray-50/50 flex flex-col justify-between font-sans">
      {/* Visual Navigation Header */}
      <header className="bg-white border-b border-gray-100 sticky top-0 z-40 shadow-2xs">
        <div className="max-w-6xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 bg-linear-to-tr from-blue-600 to-purple-600 text-white rounded-2xl flex items-center justify-center shadow-md">
              <Sparkles className="w-5 h-5 animate-pulse" />
            </div>
            <div>
              <span className="block font-black text-sm tracking-tight text-gray-900 font-mono">
                THE VVF AI FILE MANAGER
              </span>
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">
                Next-Gen Optimizer & Secure Lock
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleGlobalRefresh}
              disabled={isRefreshing}
              className="p-2 hover:bg-gray-50 border border-gray-150 rounded-xl text-gray-500 hover:text-gray-900 transition disabled:opacity-40 cursor-pointer shadow-3xs"
              title="Refresh Storage Index"
            >
              <RefreshCw className={`w-4 h-4 ${isRefreshing ? "animate-spin text-blue-600" : ""}`} />
            </button>
            <div className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 bg-gray-50 border border-gray-150 text-[10px] font-bold text-gray-500 uppercase tracking-wider rounded-xl">
              <span className="w-1.5 h-1.5 bg-emerald-500 rounded-full animate-pulse"></span>
              Virtual Sandbox Online
            </div>
          </div>
        </div>

        {/* Tab Navigation Menu */}
        <div className="bg-white border-t border-gray-50 px-4">
          <div className="max-w-6xl mx-auto flex gap-1 overflow-x-auto scrollbar-none py-1.5">
            {tabs.map(tab => {
              const TabIcon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => {
                    setActiveTab(tab.id);
                    // Refresh data corresponding to target screen
                    if (tab.id === "cloud") fetchCloudDetails();
                    else fetchLocalFiles();
                  }}
                  className={`flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-xl transition duration-150 cursor-pointer whitespace-nowrap shrink-0 ${
                    isActive
                      ? "bg-gray-900 text-white shadow-xs"
                      : "text-gray-500 hover:text-gray-950 hover:bg-gray-50"
                  }`}
                >
                  <TabIcon className="w-4 h-4 shrink-0" />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>
      </header>

      {/* Main View Port stage */}
      <main className="max-w-6xl w-full mx-auto px-4 py-8 flex-1">
        <AnimatePresence mode="wait">
          <motion.div
            key={activeTab}
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -15 }}
            transition={{ duration: 0.2 }}
            className="w-full"
          >
            {activeTab === "dashboard" && (
              <StorageDashboard
                stats={storageStats}
                filesCount={localFiles.length}
                onNavigateToTab={setActiveTab}
                onRefreshStats={fetchLocalFiles}
              />
            )}

            {activeTab === "local_storage" && (
              <LocalFileManager
                files={localFiles}
                onRefresh={fetchLocalFiles}
                onMoveToSecure={handleMoveToSecure}
                isSecureActive={true}
              />
            )}

            {activeTab === "cloud" && (
              <CloudManager
                files={cloudFiles}
                commits={gitCommits}
                stats={cloudStats}
                onRefresh={fetchCloudDetails}
              />
            )}

            {activeTab === "secure_vault" && (
              <SecureFolder onRefreshStats={fetchLocalFiles} />
            )}

            {activeTab === "ai_assistant" && <AiAssistant />}
          </motion.div>
        </AnimatePresence>
      </main>

      {/* Humble footer */}
      <footer className="bg-white border-t border-gray-100 py-6 text-center text-[10px] text-gray-400 font-medium">
        <div>THE VVF AI SMART FILE MANAGER ULTRA — PORTED TO SANDBOX WEB</div>
        <div className="mt-1">Powered by React, Tailwind CSS v4 & Gemini 3.5 Flash</div>
      </footer>
    </div>
  );
}
