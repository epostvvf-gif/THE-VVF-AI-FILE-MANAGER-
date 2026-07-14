import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { HardDrive, Trash2, ShieldAlert, Sparkles, AlertTriangle, CheckCircle, ArrowRight } from "lucide-react";
import { StorageStats, JunkFile } from "../types";

interface StorageDashboardProps {
  stats: StorageStats;
  filesCount: number;
  onNavigateToTab: (tab: string) => void;
  onRefreshStats: () => void;
}

export default function StorageDashboard({
  stats,
  filesCount,
  onNavigateToTab,
  onRefreshStats,
}: StorageDashboardProps) {
  const [junkList, setJunkList] = useState<JunkFile[]>([]);
  const [isCleaning, setIsCleaning] = useState(false);
  const [freedMessage, setFreedMessage] = useState<string | null>(null);
  const [duplicateCount, setDuplicateCount] = useState(0);

  const totalGB = stats.totalSpace; // 128 GB
  const usedSpace = stats.usedSpace;
  const usedPercentage = (usedSpace / totalGB) * 100;

  // Format bytes to human readable form
  const formatBytes = (bytes: number, decimals = 1) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ["Bytes", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + " " + sizes[i];
  };

  const fetchJunk = async () => {
    try {
      const res = await fetch("/api/files/junk");
      const data = await res.json();
      setJunkList(data.junk);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchDuplicates = async () => {
    try {
      const res = await fetch("/api/files/duplicates");
      const data = await res.json();
      setDuplicateCount(data.totalCount || 0);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchJunk();
    fetchDuplicates();
  }, [stats]);

  const handleCleanJunk = async () => {
    setIsCleaning(true);
    try {
      const res = await fetch("/api/files/junk/clean", { method: "POST" });
      const data = await res.json();
      setTimeout(() => {
        setIsCleaning(false);
        setFreedMessage(data.message);
        setJunkList([]);
        onRefreshStats();
        setTimeout(() => setFreedMessage(null), 5000);
      }, 1500);
    } catch (e) {
      setIsCleaning(false);
      console.error(e);
    }
  };

  const totalJunkSize = junkList.reduce((acc, j) => acc + j.size, 0);

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="flex items-start justify-between">
        <div>
          <h1 id="dashboard-title" className="text-2xl font-bold tracking-tight text-gray-900">Storage Analyzer</h1>
          <p className="text-sm text-gray-500">Deep-clean system caches, optimize assets, and secure private vaults.</p>
        </div>
        <div className="flex items-center gap-1.5 px-3 py-1 bg-amber-50 border border-amber-200 text-amber-700 text-xs font-semibold rounded-full shadow-xs">
          <Sparkles className="w-3.5 h-3.5 text-amber-500 animate-pulse" />
          On-Device AI Engine Active
        </div>
      </div>

      {/* Main Stats Card */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-xs relative overflow-hidden">
        {/* Background Accent */}
        <div className="absolute top-0 right-0 w-32 h-32 bg-blue-50/50 rounded-full blur-2xl -mr-6 -mt-6"></div>

        <div className="grid md:grid-cols-3 gap-6 items-center">
          {/* Radial Storage Progress */}
          <div className="flex flex-col items-center justify-center space-y-2 py-4">
            <div className="relative w-36 h-36 flex items-center justify-center">
              {/* Outer SVG Circle */}
              <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                <circle
                  cx="50"
                  cy="50"
                  r="42"
                  stroke="#f1f5f9"
                  strokeWidth="8"
                  fill="transparent"
                />
                <circle
                  cx="50"
                  cy="50"
                  r="42"
                  stroke="#3b82f6"
                  strokeWidth="8"
                  fill="transparent"
                  strokeDasharray="263.8"
                  strokeDashoffset={263.8 - (263.8 * Math.max(usedPercentage, 0.5)) / 100}
                  strokeLinecap="round"
                  className="transition-all duration-1000 ease-out"
                />
              </svg>
              {/* Center Metrics */}
              <div className="absolute text-center">
                <span className="block text-2xl font-bold text-gray-900">{usedPercentage.toFixed(1)}%</span>
                <span className="text-[10px] uppercase tracking-wider text-gray-400 font-semibold">Used Space</span>
              </div>
            </div>
            <div className="text-center">
              <span className="text-xs font-medium text-gray-500">
                {formatBytes(usedSpace)} / {formatBytes(totalGB)}
              </span>
            </div>
          </div>

          {/* Quick Metrics Breakdown */}
          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center gap-3">
              <div className="p-2.5 bg-blue-50 text-blue-600 rounded-xl">
                <HardDrive className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-semibold text-gray-900">Emulated Storage Index</h3>
                <p className="text-xs text-gray-400">Index tracking {filesCount} local documents, downloads & media records</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="bg-gray-50 rounded-xl p-3.5 border border-gray-100">
                <span className="block text-xs font-medium text-gray-400">Secure Vault Status</span>
                <span className="text-sm font-semibold text-emerald-600 mt-1 block flex items-center gap-1.5">
                  <div className="w-2 h-2 rounded-full bg-emerald-500 animate-ping"></div>
                  AES-256 Armed
                </span>
              </div>
              <div className="bg-gray-50 rounded-xl p-3.5 border border-gray-100">
                <span className="block text-xs font-medium text-gray-400">Integrations</span>
                <span className="text-sm font-semibold text-blue-600 mt-1 block flex items-center gap-1">
                  GDrive Git Sync Sim
                </span>
              </div>
            </div>

            {/* Custom visual progress bar */}
            <div className="space-y-1.5">
              <div className="flex justify-between text-xs text-gray-400 font-medium">
                <span>Free Space: {formatBytes(totalGB - usedSpace)}</span>
                <span>Used: {formatBytes(usedSpace)}</span>
              </div>
              <div className="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
                <div
                  className="bg-blue-500 h-full rounded-full transition-all duration-1000"
                  style={{ width: `${usedPercentage}%` }}
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Action Bento Grid */}
      <div className="grid md:grid-cols-2 gap-6">
        {/* Junk Clean Card */}
        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-red-50 text-red-600 rounded-xl">
                <Trash2 className="w-6 h-6" />
              </div>
              <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-red-50 border border-red-100 text-red-700">
                Cache & Logs
              </span>
            </div>
            <h3 className="text-lg font-bold text-gray-900">Junk Files Deep Clean</h3>
            <p className="text-sm text-gray-500 mt-1">
              Safely clean background log files, temporary application caches, and diagnostic dump files.
            </p>

            <AnimatePresence mode="wait">
              {junkList.length > 0 ? (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  className="mt-4 p-3 bg-red-50/50 rounded-xl border border-red-100 space-y-1.5"
                >
                  <div className="flex justify-between items-center text-xs font-semibold text-red-700">
                    <span className="flex items-center gap-1">
                      <AlertTriangle className="w-3.5 h-3.5" />
                      {junkList.length} Junk categories found
                    </span>
                    <span>{formatBytes(totalJunkSize)} recoverable</span>
                  </div>
                  <div className="text-[11px] text-gray-400 space-y-0.5 leading-relaxed font-mono max-h-24 overflow-y-auto">
                    {junkList.map(j => (
                      <div key={j.id} className="flex justify-between">
                        <span>• {j.name}</span>
                        <span>{formatBytes(j.size)}</span>
                      </div>
                    ))}
                  </div>
                </motion.div>
              ) : (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  className="mt-4 p-4 bg-emerald-50 text-emerald-800 rounded-xl border border-emerald-100 flex items-center gap-3"
                >
                  <CheckCircle className="w-5 h-5 text-emerald-500 shrink-0" />
                  <div>
                    <h4 className="text-xs font-bold text-emerald-800">System is Cleaned!</h4>
                    <p className="text-[10px] text-emerald-600 mt-0.5">All cache and leftover files have been removed.</p>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          <div className="mt-6 pt-4 border-t border-gray-50">
            {junkList.length > 0 ? (
              <button
                id="btn-clean-junk"
                onClick={handleCleanJunk}
                disabled={isCleaning}
                className="w-full bg-red-600 hover:bg-red-700 disabled:bg-gray-200 disabled:text-gray-400 text-white font-semibold py-2.5 px-4 rounded-xl shadow-xs transition duration-150 flex items-center justify-center gap-2 cursor-pointer"
              >
                {isCleaning ? (
                  <>
                    <svg className="animate-spin h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Executing Purge Script...
                  </>
                ) : (
                  <>
                    <Trash2 className="w-4.5 h-4.5" />
                    Clean Junk ({formatBytes(totalJunkSize)})
                  </>
                )}
              </button>
            ) : (
              <div className="text-center text-xs text-gray-400 font-medium">No temporary logs or caches detected.</div>
            )}
          </div>
        </div>

        {/* Duplicate Group Scanner */}
        <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-amber-50 text-amber-600 rounded-xl">
                <ShieldAlert className="w-6 h-6" />
              </div>
              <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-amber-50 border border-amber-100 text-amber-700">
                Space Optimizer
              </span>
            </div>
            <h3 className="text-lg font-bold text-gray-900">Multi-Tier Duplicate Finder</h3>
            <p className="text-sm text-gray-500 mt-1">
              AI-driven scan classifies files into Exact Signature matches, Perceptual Name copies, and Semantic Vector matches.
            </p>

            <div className="mt-4 p-4 bg-gray-50 rounded-xl border border-gray-100 flex items-center justify-between">
              <div>
                <span className="block text-[11px] font-semibold text-gray-400 uppercase tracking-wider">Scanned Status</span>
                <span className="text-sm font-bold text-gray-800 mt-0.5 block">
                  {duplicateCount > 0 ? `${duplicateCount} duplicate groups found` : "All files analyzed"}
                </span>
              </div>
              <div className="text-right">
                <span className="block text-[11px] font-semibold text-gray-400 uppercase tracking-wider">Precision</span>
                <span className="text-xs font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded-md mt-0.5 inline-block">
                  L2 Cosine Norm
                </span>
              </div>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-gray-50">
            <button
              id="btn-scan-duplicates"
              onClick={() => onNavigateToTab("local_storage")}
              className="w-full bg-gray-900 hover:bg-black text-white font-semibold py-2.5 px-4 rounded-xl shadow-xs transition duration-150 flex items-center justify-center gap-2 cursor-pointer text-sm"
            >
              Scan & Group Duplicates
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Freed message Toast */}
      <AnimatePresence>
        {freedMessage && (
          <motion.div
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            className="fixed bottom-20 left-1/2 transform -translate-x-1/2 z-50 bg-gray-900 text-white text-xs font-semibold px-4 py-3 rounded-full shadow-lg flex items-center gap-2 border border-gray-800"
          >
            <CheckCircle className="w-4.5 h-4.5 text-emerald-400 shrink-0" />
            <span>{freedMessage}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
