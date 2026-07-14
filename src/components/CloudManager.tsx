import React, { useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import { Cloud, GitCommit as GitIcon, RotateCw, CheckCircle, File, Database, Calendar } from "lucide-react";
import { CloudFile, GitCommit, StorageStats } from "../types";

interface CloudManagerProps {
  files: CloudFile[];
  commits: GitCommit[];
  stats: StorageStats;
  onRefresh: () => void;
}

export default function CloudManager({
  files,
  commits,
  stats,
  onRefresh,
}: CloudManagerProps) {
  const [isSyncing, setIsSyncing] = useState(false);
  const [syncResult, setSyncResult] = useState<{ count: number; hash: string } | null>(null);

  const totalSpace = stats.totalSpace; // 15GB GDrive
  const usedSpace = stats.usedSpace;
  const usedPercentage = (usedSpace / totalSpace) * 100;

  const formatBytes = (bytes: number, decimals = 2) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + " " + sizes[i];
  };

  const handleSync = async () => {
    setIsSyncing(true);
    setSyncResult(null);
    try {
      const res = await fetch("/api/cloud/sync", { method: "POST" });
      const data = await res.json();
      
      // Simulate on-device backup network latency for premium feel
      setTimeout(() => {
        setIsSyncing(false);
        onRefresh();
        if (data.syncCount > 0) {
          const latestCommit = data.commits[0];
          setSyncResult({ count: data.syncCount, hash: latestCommit.hash });
          setTimeout(() => setSyncResult(null), 6000);
        } else {
          setSyncResult({ count: 0, hash: "" });
          setTimeout(() => setSyncResult(null), 4000);
        }
      }, 2000);
    } catch (e) {
      setIsSyncing(false);
      console.error(e);
    }
  };

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-gray-900">Google Drive Sync & Sim</h1>
          <p className="text-sm text-gray-500">Securely backup local items and sync folder indices via simulated Git commits.</p>
        </div>

        {/* Sync Trigger Button */}
        <button
          onClick={handleSync}
          disabled={isSyncing}
          className="flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-200 disabled:text-gray-400 text-white font-bold py-2 px-5 text-xs rounded-xl shadow-xs transition duration-150 cursor-pointer"
        >
          <RotateCw className={`w-4 h-4 ${isSyncing ? "animate-spin" : ""}`} />
          {isSyncing ? "Syncing Workspace..." : "Sync Local to Cloud"}
        </button>
      </div>

      {/* Sync result announcement banner */}
      <AnimatePresence>
        {syncResult && (
          <motion.div
            initial={{ opacity: 0, y: -15 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -15 }}
            className={`p-4 rounded-2xl border flex items-start gap-3 ${
              syncResult.count > 0 
                ? "bg-emerald-50 border-emerald-100 text-emerald-800" 
                : "bg-blue-50 border-blue-100 text-blue-800"
            }`}
          >
            <CheckCircle className={`w-5 h-5 shrink-0 mt-0.5 ${syncResult.count > 0 ? "text-emerald-500" : "text-blue-500"}`} />
            <div>
              {syncResult.count > 0 ? (
                <>
                  <h4 className="text-xs font-bold">Cloud Sync Succeeded!</h4>
                  <p className="text-[11px] mt-1 leading-relaxed">
                    Successfully backed up <strong>{syncResult.count}</strong> clean file(s) into GDrive. 
                    Created Git Commit branch <span className="font-mono bg-emerald-100 px-1 py-0.5 rounded text-[10px] font-bold">@{syncResult.hash}</span>.
                  </p>
                </>
              ) : (
                <>
                  <h4 className="text-xs font-bold">Storage is Up-To-Date</h4>
                  <p className="text-[11px] mt-1 leading-relaxed">
                    All local items already have cloud matching indices. No backup needed.
                  </p>
                </>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Grid: Cloud Storage Status and Commit logs */}
      <div className="grid md:grid-cols-3 gap-6">
        {/* Cloud Stats */}
        <div className="md:col-span-1 space-y-6">
          <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-xs space-y-4">
            <div className="flex items-center gap-3 text-blue-600">
              <Cloud className="w-5 h-5" />
              <h3 className="text-sm font-bold text-gray-900">GDrive Space</h3>
            </div>

            <div className="space-y-1.5">
              <div className="flex justify-between text-xs text-gray-400 font-medium">
                <span>Used: {formatBytes(usedSpace)}</span>
                <span>Free Limit: 15 GB</span>
              </div>
              <div className="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
                <div
                  className="bg-blue-500 h-full rounded-full transition-all duration-500"
                  style={{ width: `${usedPercentage}%` }}
                ></div>
              </div>
            </div>

            <div className="text-[11px] text-gray-400 leading-relaxed font-mono pt-2">
              <span className="block">Host: drive.google.com/sim</span>
              <span className="block">Bandwidth: Uncapped (VVF Protocol)</span>
              <span className="block">Provider: simulated_gdrive_v1</span>
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-xs space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-gray-400">Sync Health Check</h4>
            <div className="flex items-center justify-between text-xs">
              <span className="text-gray-600 font-medium flex items-center gap-1.5">
                <div className="w-2 h-2 bg-emerald-500 rounded-full"></div>
                Connection Stable
              </span>
              <span className="text-gray-400 font-mono">15ms latency</span>
            </div>
          </div>
        </div>

        {/* Sync / Commits list */}
        <div className="md:col-span-2 space-y-6">
          {/* Synced Files list */}
          <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-xs space-y-4">
            <h3 className="text-sm font-bold text-gray-900">Synced Assets on Drive ({files.length})</h3>
            {files.length > 0 ? (
              <div className="divide-y divide-gray-50 max-h-60 overflow-y-auto pr-1">
                {files.map(file => (
                  <div key={file.id} className="py-2.5 flex items-center justify-between gap-3 text-xs">
                    <div className="flex items-center gap-2.5 min-w-0">
                      <File className="w-4 h-4 text-gray-400 shrink-0" />
                      <span className="font-bold text-gray-700 truncate">{file.name}</span>
                    </div>
                    <div className="flex items-center gap-4 text-gray-400 font-medium text-[11px]">
                      <span>{formatBytes(file.size)}</span>
                      <span className="text-[10px] bg-blue-50 text-blue-600 border border-blue-100/30 px-1.5 py-0.5 rounded-md shrink-0">Synced</span>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-xs text-gray-400 font-medium">
                No synced cloud files. Click 'Sync' to backup.
              </div>
            )}
          </div>

          {/* Commits logs */}
          <div className="bg-white rounded-2xl border border-gray-100 p-6 shadow-xs space-y-4">
            <div className="flex items-center gap-2">
              <GitIcon className="w-5 h-5 text-gray-600" />
              <h3 className="text-sm font-bold text-gray-900">Virtual Commit Log Registry</h3>
            </div>

            <div className="space-y-3">
              {commits.map(commit => (
                <div key={commit.id} className="flex gap-4 p-3 bg-gray-50 border border-gray-100 rounded-xl relative overflow-hidden">
                  <div className="flex flex-col items-center shrink-0">
                    <div className="w-7 h-7 bg-white border border-gray-200 rounded-full flex items-center justify-center font-mono text-[10px] font-bold text-gray-700 shadow-2xs">
                      #{commit.hash}
                    </div>
                    <div className="w-0.5 bg-gray-200 grow mt-2"></div>
                  </div>

                  <div className="space-y-1">
                    <h4 className="text-xs font-bold text-gray-800 leading-tight">{commit.message}</h4>
                    <div className="flex items-center gap-3 text-[10px] text-gray-400 font-medium pt-1">
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {new Date(commit.date).toLocaleDateString()} {new Date(commit.date).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                      </span>
                      <span>•</span>
                      <span>Synced {commit.filesCount} file(s)</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
