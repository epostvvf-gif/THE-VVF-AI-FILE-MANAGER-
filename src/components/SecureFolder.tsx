import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { ShieldAlert, Lock, Unlock, Eye, KeyRound, ArrowLeft, RefreshCw, EyeOff, ShieldCheck } from "lucide-react";
import { StorageFile } from "../types";

interface SecureFolderProps {
  onRefreshStats: () => void;
}

export default function SecureFolder({ onRefreshStats }: SecureFolderProps) {
  const [hasPin, setHasPin] = useState(false);
  const [isUnlocked, setIsUnlocked] = useState(false);
  const [pinInput, setPinInput] = useState("");
  const [secureFiles, setSecureFiles] = useState<StorageFile[]>([]);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isSettingUp, setIsSettingUp] = useState(false);
  const [setupPinInput, setSetupPinInput] = useState("");

  const checkPinStatus = async () => {
    try {
      const res = await fetch("/api/secure-folder/pin/status");
      const data = await res.json();
      setHasPin(data.hasPin);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchSecureFiles = async () => {
    try {
      const res = await fetch("/api/secure-folder/files");
      const data = await res.json();
      if (res.ok) {
        setSecureFiles(data.files || []);
      } else {
        setIsUnlocked(false);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    checkPinStatus();
  }, []);

  useEffect(() => {
    if (isUnlocked) {
      fetchSecureFiles();
    }
  }, [isUnlocked]);

  const handleVerifyPin = async (pinValue = pinInput) => {
    setErrorMsg(null);
    try {
      const res = await fetch("/api/secure-folder/pin/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ pin: pinValue }),
      });
      const data = await res.json();
      if (res.ok && data.unlocked) {
        setIsUnlocked(true);
        setPinInput("");
      } else {
        setErrorMsg(data.error || "Incorrect PIN");
        setPinInput("");
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleSetupPin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    if (setupPinInput.length < 4) {
      setErrorMsg("PIN must be at least 4 digits");
      return;
    }
    try {
      const res = await fetch("/api/secure-folder/pin/setup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ pin: setupPinInput }),
      });
      if (res.ok) {
        setHasPin(true);
        setIsSettingUp(false);
        // Automatically verify and unlock
        handleVerifyPin(setupPinInput);
        setSetupPinInput("");
      } else {
        const data = await res.json();
        setErrorMsg(data.error);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleLockFolder = async () => {
    try {
      await fetch("/api/secure-folder/lock", { method: "POST" });
      setIsUnlocked(false);
      setSecureFiles([]);
    } catch (e) {
      console.error(e);
    }
  };

  const handleRestoreFile = async (fileId: string) => {
    try {
      const res = await fetch("/api/secure-folder/restore", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fileId }),
      });
      if (res.ok) {
        fetchSecureFiles();
        onRefreshStats();
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleKeyPress = (num: string) => {
    setErrorMsg(null);
    if (pinInput.length < 6) {
      const newVal = pinInput + num;
      setPinInput(newVal);
      if (newVal.length === 4) {
        // Auto submit on 4 digits for sleek mobile feel
        handleVerifyPin(newVal);
      }
    }
  };

  const handleBackspace = () => {
    setPinInput(prev => prev.slice(0, -1));
  };

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  return (
    <div className="max-w-md mx-auto bg-white rounded-3xl border border-gray-100 shadow-xl overflow-hidden min-h-[520px] flex flex-col justify-between">
      {/* Locked Screen UI */}
      <AnimatePresence mode="wait">
        {!isUnlocked ? (
          <motion.div
            key="lock-screen"
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.98 }}
            className="p-8 flex-1 flex flex-col justify-between"
          >
            {/* Top Indicator */}
            <div className="text-center space-y-2 mt-4">
              <div className="w-14 h-14 bg-blue-50 border border-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto shadow-sm">
                <Lock className="w-6 h-6" />
              </div>
              <h2 className="text-lg font-bold text-gray-900">Secure Vault Lock</h2>
              <p className="text-xs text-gray-400 max-w-[280px] mx-auto leading-relaxed">
                AES-256 military-grade file encryption system. Authenticate via PIN to decrypt assets.
              </p>
              <div className="text-[10px] font-bold text-gray-400 uppercase tracking-widest font-mono">
                Default PIN is <span className="text-blue-500 underline">1234</span>
              </div>
            </div>

            {/* Password setup check */}
            {!hasPin || isSettingUp ? (
              <form onSubmit={handleSetupPin} className="space-y-4 my-6">
                <div>
                  <label className="block text-center text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">Configure Secure PIN</label>
                  <input
                    type="password"
                    pattern="\d*"
                    maxLength={6}
                    required
                    value={setupPinInput}
                    onChange={(e) => setSetupPinInput(e.target.value.replace(/\D/g, ""))}
                    placeholder="Enter 4 to 6 digit PIN"
                    className="w-full text-center bg-gray-50 text-base font-bold tracking-widest border border-gray-200 focus:border-blue-500 rounded-xl p-3 outline-none text-gray-800"
                  />
                </div>
                {errorMsg && <p className="text-center text-xs font-semibold text-red-500">{errorMsg}</p>}
                <button
                  type="submit"
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2.5 rounded-xl text-xs cursor-pointer shadow-xs"
                >
                  Confirm & Setup PIN
                </button>
              </form>
            ) : (
              <div className="space-y-6 my-6">
                {/* Visual PIN Dots */}
                <div className="flex justify-center gap-3">
                  {[...Array(4)].map((_, i) => (
                    <div
                      key={i}
                      className={`w-3.5 h-3.5 rounded-full border border-gray-200 transition-all duration-150 ${
                        i < pinInput.length ? "bg-gray-800 scale-110" : "bg-gray-50"
                      }`}
                    ></div>
                  ))}
                </div>

                {errorMsg && <p className="text-center text-xs font-semibold text-red-500 animate-shake">{errorMsg}</p>}

                {/* Smartphone Numpad */}
                <div className="grid grid-cols-3 gap-y-3 gap-x-5 max-w-[240px] mx-auto">
                  {["1", "2", "3", "4", "5", "6", "7", "8", "9"].map(num => (
                    <button
                      key={num}
                      onClick={() => handleKeyPress(num)}
                      className="w-12 h-12 bg-gray-50 hover:bg-gray-100 text-gray-700 text-sm font-bold rounded-full border border-gray-100 flex items-center justify-center transition active:scale-95 cursor-pointer shadow-2xs"
                    >
                      {num}
                    </button>
                  ))}
                  <button
                    onClick={() => {
                      setIsSettingUp(true);
                      setErrorMsg(null);
                    }}
                    className="text-[10px] font-bold text-gray-400 hover:text-gray-900 active:scale-95 cursor-pointer flex items-center justify-center"
                  >
                    Reset PIN
                  </button>
                  <button
                    onClick={() => handleKeyPress("0")}
                    className="w-12 h-12 bg-gray-50 hover:bg-gray-100 text-gray-700 text-sm font-bold rounded-full border border-gray-100 flex items-center justify-center transition active:scale-95 cursor-pointer shadow-2xs"
                  >
                    0
                  </button>
                  <button
                    onClick={handleBackspace}
                    className="text-[11px] font-bold text-gray-400 hover:text-gray-900 active:scale-95 cursor-pointer flex items-center justify-center"
                  >
                    Delete
                  </button>
                </div>
              </div>
            )}

            {/* Footer lock icon */}
            <div className="text-center text-[10px] text-gray-400 font-medium">
              Files are physically hashed & ciphered in local database cache.
            </div>
          </motion.div>
        ) : (
          /* Unlocked Decrypted View */
          <motion.div
            key="unlocked-screen"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 10 }}
            className="flex-1 flex flex-col h-full"
          >
            {/* Header */}
            <div className="p-6 border-b border-gray-100 flex items-center justify-between bg-emerald-50/20">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-emerald-500 animate-pulse" />
                <div>
                  <h3 className="text-sm font-bold text-gray-900">Secure Vault</h3>
                  <p className="text-[10px] text-emerald-600 font-medium">Decrypted Storage Area</p>
                </div>
              </div>
              <button
                onClick={handleLockFolder}
                className="flex items-center gap-1 text-[10px] bg-red-50 text-red-600 border border-red-100 px-2.5 py-1 rounded-lg font-bold hover:bg-red-100 transition cursor-pointer"
              >
                <Lock className="w-3 h-3" />
                Lock Vault
              </button>
            </div>

            {/* List of Secure Files */}
            <div className="p-6 flex-1 overflow-y-auto space-y-4">
              <span className="block text-[11px] font-bold uppercase tracking-wider text-gray-400 mb-2">Decrypted Files ({secureFiles.length})</span>
              {secureFiles.length > 0 ? (
                <div className="divide-y divide-gray-50">
                  {secureFiles.map(file => (
                    <div key={file.id} className="py-3 flex items-center justify-between gap-4">
                      <div className="truncate min-w-0">
                        <span className="block text-xs font-bold text-gray-800 truncate leading-snug">{file.name}</span>
                        <div className="flex items-center gap-2 mt-0.5 text-[10px] text-gray-400 font-medium">
                          <span>{formatBytes(file.size)}</span>
                          <span>•</span>
                          <span className="text-emerald-600 font-semibold bg-emerald-50 px-1 rounded-sm">AES-Ciphered</span>
                        </div>
                      </div>

                      <button
                        onClick={() => handleRestoreFile(file.id)}
                        title="Restore back to camera storage"
                        className="p-1.5 hover:bg-blue-50 text-blue-600 border border-transparent hover:border-blue-100 rounded-lg transition duration-150 cursor-pointer shrink-0"
                      >
                        <Unlock className="w-4 h-4" />
                      </button>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-12">
                  <KeyRound className="w-12 h-12 text-gray-200 mx-auto mb-3" />
                  <h4 className="text-sm font-semibold text-gray-900">Secure vault is empty</h4>
                  <p className="text-xs text-gray-400 mt-1 max-w-[240px] mx-auto leading-relaxed">
                    To protect sensitive images or bank logs, select them in 'Local Files' and tap the Lock icon.
                  </p>
                </div>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
