import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  File, Image, Video, Music, FolderOpen, Download, Search, 
  Trash2, ShieldAlert, ArrowUpDown, Plus, Lock, Check, Eye 
} from "lucide-react";
import { StorageFile, DuplicateGroup } from "../types";

interface LocalFileManagerProps {
  files: StorageFile[];
  onRefresh: () => void;
  onMoveToSecure: (fileId: string) => void;
  isSecureActive: boolean;
}

export default function LocalFileManager({
  files,
  onRefresh,
  onMoveToSecure,
  isSecureActive,
}: LocalFileManagerProps) {
  const [selectedCategory, setSelectedCategory] = useState<string>("Images");
  const [searchQuery, setSearchQuery] = useState("");
  const [sortBy, setSortBy] = useState<"name" | "size" | "date">("date");
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");
  const [selectedFiles, setSelectedFiles] = useState<string[]>([]);
  
  // Duplicates UI States
  const [isDuplicateScanOpen, setIsDuplicateScanOpen] = useState(false);
  const [duplicateGroups, setDuplicateGroups] = useState<{
    exact: DuplicateGroup[];
    near: DuplicateGroup[];
    semantic: DuplicateGroup[];
  }>({ exact: [], near: [], semantic: [] });
  const [selectedDuplicatesToDelete, setSelectedDuplicatesToDelete] = useState<string[]>([]);
  const [isDeletingDuplicates, setIsDeletingDuplicates] = useState(false);

  // Add simulated local file state
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [newFileName, setNewFileName] = useState("");
  const [newFileSize, setNewFileSize] = useState("1.5");
  const [newFileCategory, setNewFileCategory] = useState("Images");

  const categories = [
    { name: "Images", icon: Image, color: "text-blue-500 bg-blue-50 border-blue-100" },
    { name: "Videos", icon: Video, color: "text-purple-500 bg-purple-50 border-purple-100" },
    { name: "Audio", icon: Music, color: "text-emerald-500 bg-emerald-50 border-emerald-100" },
    { name: "Documents", icon: File, color: "text-amber-500 bg-amber-50 border-amber-100" },
    { name: "Downloads", icon: Download, color: "text-pink-500 bg-pink-50 border-pink-100" },
  ];

  const fetchDuplicates = async () => {
    try {
      const res = await fetch("/api/files/duplicates");
      const data = await res.json();
      setDuplicateGroups(data);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    if (isDuplicateScanOpen) {
      fetchDuplicates();
    }
  }, [isDuplicateScanOpen, files]);

  const handleCreateFile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newFileName) return;

    try {
      const res = await fetch("/api/files", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: newFileName,
          size: parseFloat(newFileSize) * 1024 * 1024,
          category: newFileCategory,
        }),
      });

      if (res.ok) {
        setNewFileName("");
        setIsAddOpen(false);
        onRefresh();
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleDeleteFile = async (id: string) => {
    try {
      const res = await fetch(`/api/files/${id}`, { method: "DELETE" });
      if (res.ok) {
        onRefresh();
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleBulkDeleteDuplicates = async () => {
    if (selectedDuplicatesToDelete.length === 0) return;
    setIsDeletingDuplicates(true);
    try {
      const res = await fetch("/api/files/duplicates/delete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fileIds: selectedDuplicatesToDelete }),
      });
      if (res.ok) {
        setSelectedDuplicatesToDelete([]);
        onRefresh();
        fetchDuplicates();
      }
    } catch (e) {
      console.error(e);
    } finally {
      setIsDeletingDuplicates(false);
    }
  };

  const toggleSelectDuplicate = (id: string) => {
    setSelectedDuplicatesToDelete(prev => 
      prev.includes(id) ? prev.filter(item => item !== id) : [...prev, id]
    );
  };

  // Filter & Sort
  const filteredFiles = files
    .filter(f => f.category === selectedCategory)
    .filter(f => f.name.toLowerCase().includes(searchQuery.toLowerCase()));

  const sortedFiles = [...filteredFiles].sort((a, b) => {
    let comp = 0;
    if (sortBy === "name") {
      comp = a.name.localeCompare(b.name);
    } else if (sortBy === "size") {
      comp = a.size - b.size;
    } else if (sortBy === "date") {
      comp = new Date(a.dateAdded).getTime() - new Date(b.dateAdded).getTime();
    }
    return sortOrder === "asc" ? comp : -comp;
  });

  const toggleSort = (type: "name" | "size" | "date") => {
    if (sortBy === type) {
      setSortOrder(prev => (prev === "asc" ? "desc" : "asc"));
    } else {
      setSortBy(type);
      setSortOrder("desc");
    }
  };

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  const formatDate = (isoString: string) => {
    const date = new Date(isoString);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="space-y-6">
      {/* Top Banner with Search and Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-gray-900">Local Files</h1>
          <p className="text-sm text-gray-500">Categorize, search, and run multi-tier filters on local storage assets.</p>
        </div>
        <div className="flex items-center gap-2">
          {/* Scan Duplicates Button */}
          <button
            onClick={() => setIsDuplicateScanOpen(true)}
            className="flex items-center gap-2 px-4 py-2 text-xs font-semibold bg-amber-50 border border-amber-200 text-amber-700 hover:bg-amber-100 rounded-xl shadow-xs transition duration-150 cursor-pointer"
          >
            <ShieldAlert className="w-4 h-4 text-amber-500" />
            Duplicate Scanner
          </button>
          
          {/* Add simulated file button */}
          <button
            onClick={() => setIsAddOpen(true)}
            className="flex items-center gap-1.5 bg-gray-900 hover:bg-black text-white px-4 py-2 text-xs font-semibold rounded-xl shadow-xs transition duration-150 cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            Simulate File
          </button>
        </div>
      </div>

      {/* Category Horizontal Filter Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
        {categories.map(cat => {
          const CatIcon = cat.icon;
          const isSelected = selectedCategory === cat.name;
          const count = files.filter(f => f.category === cat.name).length;

          return (
            <button
              key={cat.name}
              onClick={() => {
                setSelectedCategory(cat.name);
                setSelectedFiles([]);
              }}
              className={`flex flex-col items-center justify-center p-3 rounded-2xl border text-center transition duration-150 cursor-pointer ${
                isSelected
                  ? "bg-gray-900 border-gray-900 text-white"
                  : "bg-white border-gray-100 hover:bg-gray-50/50 text-gray-600"
              }`}
            >
              <div className={`p-2 rounded-xl mb-2 border ${cat.color}`}>
                <CatIcon className="w-5 h-5" />
              </div>
              <span className="text-xs font-bold block">{cat.name}</span>
              <span className="text-[10px] text-gray-400 mt-1">{count} files</span>
            </button>
          );
        })}
      </div>

      {/* Filter and sorting sub-bar */}
      <div className="bg-white rounded-xl border border-gray-100 p-3 shadow-xs flex flex-col md:flex-row gap-3 justify-between items-center">
        {/* Search Input */}
        <div className="relative w-full md:w-72">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder={`Search ${selectedCategory.toLowerCase()}...`}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-gray-50 text-xs border border-gray-200 focus:border-blue-500 rounded-lg pl-9 pr-3 py-2 outline-none text-gray-700"
          />
        </div>

        {/* Sorting controls */}
        <div className="flex gap-2 w-full md:w-auto overflow-x-auto">
          <button
            onClick={() => toggleSort("name")}
            className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium border rounded-lg cursor-pointer ${
              sortBy === "name" ? "bg-blue-50 border-blue-200 text-blue-700" : "bg-white border-gray-200 text-gray-600 hover:bg-gray-50"
            }`}
          >
            Name
            {sortBy === "name" && <ArrowUpDown className="w-3.5 h-3.5" />}
          </button>
          <button
            onClick={() => toggleSort("size")}
            className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium border rounded-lg cursor-pointer ${
              sortBy === "size" ? "bg-blue-50 border-blue-200 text-blue-700" : "bg-white border-gray-200 text-gray-600 hover:bg-gray-50"
            }`}
          >
            Size
            {sortBy === "size" && <ArrowUpDown className="w-3.5 h-3.5" />}
          </button>
          <button
            onClick={() => toggleSort("date")}
            className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium border rounded-lg cursor-pointer ${
              sortBy === "date" ? "bg-blue-50 border-blue-200 text-blue-700" : "bg-white border-gray-200 text-gray-600 hover:bg-gray-50"
            }`}
          >
            Date Added
            {sortBy === "date" && <ArrowUpDown className="w-3.5 h-3.5" />}
          </button>
        </div>
      </div>

      {/* Files List Layout */}
      <div className="bg-white rounded-2xl border border-gray-100 p-4 shadow-xs">
        {sortedFiles.length > 0 ? (
          <div className="divide-y divide-gray-50">
            {sortedFiles.map(file => (
              <div key={file.id} className="py-3 flex items-center justify-between gap-4 group">
                <div className="flex items-center gap-3 min-w-0">
                  <div className="p-2 bg-gray-50 border border-gray-100 rounded-xl text-gray-400">
                    <File className="w-5 h-5 text-gray-400" />
                  </div>
                  <div className="min-w-0">
                    <span className="block text-xs font-bold text-gray-800 truncate leading-snug">{file.name}</span>
                    <div className="flex items-center gap-2 mt-0.5 text-[10px] text-gray-400 font-medium">
                      <span>{formatBytes(file.size)}</span>
                      <span>•</span>
                      <span>{formatDate(file.dateAdded)}</span>
                    </div>
                  </div>
                </div>

                {/* File Action Tray */}
                <div className="flex items-center gap-1.5 shrink-0">
                  {/* Secure Vault Action */}
                  <button
                    onClick={() => onMoveToSecure(file.id)}
                    title="Lock to Secure Vault (AES-256)"
                    className="p-2 hover:bg-emerald-50 text-emerald-600 border border-transparent hover:border-emerald-100 rounded-lg transition duration-150 cursor-pointer"
                  >
                    <Lock className="w-4 h-4" />
                  </button>

                  {/* Delete Button */}
                  <button
                    onClick={() => handleDeleteFile(file.id)}
                    title="Delete File"
                    className="p-2 hover:bg-red-50 text-red-500 border border-transparent hover:border-red-100 rounded-lg transition duration-150 cursor-pointer"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <FolderOpen className="w-12 h-12 text-gray-200 mx-auto mb-3" />
            <h3 className="text-sm font-semibold text-gray-900">No files found</h3>
            <p className="text-xs text-gray-400 mt-1">There are no files in this category. Click 'Simulate File' to add some!</p>
          </div>
        )}
      </div>

      {/* Duplicate Scan Drawer/Overlay Modal */}
      <AnimatePresence>
        {isDuplicateScanOpen && (
          <div className="fixed inset-0 bg-black/40 backdrop-blur-xs z-50 flex justify-end">
            <motion.div
              initial={{ x: "100%" }}
              animate={{ x: 0 }}
              exit={{ x: "100%" }}
              transition={{ type: "spring", damping: 25, stiffness: 200 }}
              className="w-full max-w-xl bg-white h-full shadow-2xl flex flex-col justify-between"
            >
              {/* Header */}
              <div className="p-6 border-b border-gray-100">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <ShieldAlert className="w-5 h-5 text-amber-500 animate-pulse" />
                    <h2 className="text-lg font-bold text-gray-900">Multi-Tier Duplicate Scanner</h2>
                  </div>
                  <button
                    onClick={() => setIsDuplicateScanOpen(false)}
                    className="text-xs font-semibold px-2.5 py-1 text-gray-400 hover:text-gray-900 border border-gray-100 rounded-lg cursor-pointer"
                  >
                    Close
                  </button>
                </div>
                <p className="text-xs text-gray-400 mt-2">
                  Scans files via Exact Signatures (SHA-256), Perceptual Name grouping, and Semantic Vector similarity mappings.
                </p>
              </div>

              {/* Scrollable Group List */}
              <div className="p-6 overflow-y-auto flex-1 space-y-6">
                {/* Exact Duplicates */}
                <div>
                  <h3 className="text-xs font-bold uppercase tracking-wider text-gray-400 mb-3">Exact Copies</h3>
                  {duplicateGroups.exact.length > 0 ? (
                    <div className="space-y-4">
                      {duplicateGroups.exact.map(group => (
                        <div key={group.id} className="bg-amber-50/30 border border-amber-100 rounded-2xl p-4">
                          <span className="text-[10px] uppercase font-bold text-amber-700 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-full inline-block">
                            {group.type}
                          </span>
                          <span className="block text-xs font-semibold text-gray-800 mt-2 leading-relaxed">
                            {group.description}
                          </span>
                          <div className="mt-3 divide-y divide-amber-100">
                            {group.files.map(f => (
                              <div key={f.id} className="py-2 flex items-center justify-between gap-3 text-xs">
                                <div className="truncate min-w-0">
                                  <span className="block font-medium text-gray-700 truncate">{f.name}</span>
                                  <span className="block text-[10px] text-gray-400 font-mono truncate">{f.path}</span>
                                </div>
                                <div className="flex items-center gap-2.5">
                                  <span className="text-[10px] font-medium text-gray-400 shrink-0">{formatBytes(f.size)}</span>
                                  <input
                                    type="checkbox"
                                    checked={selectedDuplicatesToDelete.includes(f.id)}
                                    onChange={() => toggleSelectDuplicate(f.id)}
                                    className="w-4 h-4 text-blue-600 border-gray-300 rounded-sm focus:ring-blue-500 cursor-pointer"
                                  />
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-4 bg-gray-50 rounded-xl border border-dashed border-gray-200 text-xs text-gray-400">
                      No exact duplicate groups found.
                    </div>
                  )}
                </div>

                {/* Near Match Duplicates */}
                <div>
                  <h3 className="text-xs font-bold uppercase tracking-wider text-gray-400 mb-3">Near Name Matching</h3>
                  {duplicateGroups.near.length > 0 ? (
                    <div className="space-y-4">
                      {duplicateGroups.near.map(group => (
                        <div key={group.id} className="bg-purple-50/20 border border-purple-100 rounded-2xl p-4">
                          <span className="text-[10px] uppercase font-bold text-purple-700 bg-purple-50 border border-purple-200 px-2 py-0.5 rounded-full inline-block">
                            {group.type}
                          </span>
                          <span className="block text-xs font-semibold text-gray-800 mt-2 leading-relaxed">
                            {group.description}
                          </span>
                          <div className="mt-3 divide-y divide-purple-100/50">
                            {group.files.map(f => (
                              <div key={f.id} className="py-2 flex items-center justify-between gap-3 text-xs">
                                <div className="truncate min-w-0">
                                  <span className="block font-medium text-gray-700 truncate">{f.name}</span>
                                  <span className="block text-[10px] text-gray-400 font-mono truncate">{f.path}</span>
                                </div>
                                <div className="flex items-center gap-2.5">
                                  <span className="text-[10px] font-medium text-gray-400 shrink-0">{formatBytes(f.size)}</span>
                                  <input
                                    type="checkbox"
                                    checked={selectedDuplicatesToDelete.includes(f.id)}
                                    onChange={() => toggleSelectDuplicate(f.id)}
                                    className="w-4 h-4 text-blue-600 border-gray-300 rounded-sm focus:ring-blue-500 cursor-pointer"
                                  />
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-4 bg-gray-50 rounded-xl border border-dashed border-gray-200 text-xs text-gray-400">
                      No near-duplicate groups found.
                    </div>
                  )}
                </div>

                {/* Semantic Duplicates */}
                <div>
                  <h3 className="text-xs font-bold uppercase tracking-wider text-gray-400 mb-3">Semantic Matches</h3>
                  {duplicateGroups.semantic.length > 0 ? (
                    <div className="space-y-4">
                      {duplicateGroups.semantic.map(group => (
                        <div key={group.id} className="bg-blue-50/20 border border-blue-100 rounded-2xl p-4">
                          <div className="flex justify-between items-center">
                            <span className="text-[10px] uppercase font-bold text-blue-700 bg-blue-50 border border-blue-200 px-2 py-0.5 rounded-full">
                              {group.type}
                            </span>
                            <span className="text-xs font-bold text-blue-600 flex items-center gap-1 bg-blue-50/50 border border-blue-100/30 px-2 py-0.5 rounded-lg">
                              <Check className="w-3.5 h-3.5" />
                              {Math.round((group.similarityScore || 0.94) * 100)}% Similarity
                            </span>
                          </div>
                          <span className="block text-xs font-semibold text-gray-800 mt-2 leading-relaxed">
                            {group.description}
                          </span>
                          <div className="mt-3 divide-y divide-blue-100/50">
                            {group.files.map(f => (
                              <div key={f.id} className="py-2 flex items-center justify-between gap-3 text-xs">
                                <div className="truncate min-w-0">
                                  <span className="block font-medium text-gray-700 truncate">{f.name}</span>
                                  <span className="block text-[10px] text-gray-400 font-mono truncate">{f.path}</span>
                                </div>
                                <div className="flex items-center gap-2.5">
                                  <span className="text-[10px] font-medium text-gray-400 shrink-0">{formatBytes(f.size)}</span>
                                  <input
                                    type="checkbox"
                                    checked={selectedDuplicatesToDelete.includes(f.id)}
                                    onChange={() => toggleSelectDuplicate(f.id)}
                                    className="w-4 h-4 text-blue-600 border-gray-300 rounded-sm focus:ring-blue-500 cursor-pointer"
                                  />
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-4 bg-gray-50 rounded-xl border border-dashed border-gray-200 text-xs text-gray-400">
                      No semantic duplicates found.
                    </div>
                  )}
                </div>
              </div>

              {/* Footer */}
              <div className="p-6 border-t border-gray-100 bg-gray-50">
                <button
                  onClick={handleBulkDeleteDuplicates}
                  disabled={selectedDuplicatesToDelete.length === 0 || isDeletingDuplicates}
                  className="w-full bg-red-600 hover:bg-red-700 disabled:bg-gray-200 disabled:text-gray-400 text-white font-bold py-3 px-4 rounded-xl shadow-xs transition duration-150 flex items-center justify-center gap-2 cursor-pointer text-sm"
                >
                  {isDeletingDuplicates ? (
                    "Processing Bulk Delete..."
                  ) : (
                    <>
                      <Trash2 className="w-4.5 h-4.5" />
                      Bulk Delete Selected ({selectedDuplicatesToDelete.length} files)
                    </>
                  )}
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Add Simulated File Modal */}
      <AnimatePresence>
        {isAddOpen && (
          <div className="fixed inset-0 bg-black/40 backdrop-blur-xs z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-white rounded-2xl border border-gray-100 max-w-sm w-full p-6 shadow-2xl relative"
            >
              <h2 className="text-lg font-bold text-gray-900">Simulate File Upload</h2>
              <p className="text-xs text-gray-400 mt-1">Add a mock file to emulated device storage to test filtering and optimization.</p>

              <form onSubmit={handleCreateFile} className="mt-4 space-y-4">
                <div>
                  <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5">File Name</label>
                  <input
                    type="text"
                    required
                    value={newFileName}
                    onChange={(e) => setNewFileName(e.target.value)}
                    placeholder="e.g. holiday_video.mp4"
                    className="w-full bg-gray-50 text-xs border border-gray-200 focus:border-blue-500 rounded-lg p-2.5 outline-none text-gray-700 font-medium"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5">Size (MB)</label>
                    <input
                      type="number"
                      step="0.1"
                      required
                      value={newFileSize}
                      onChange={(e) => setNewFileSize(e.target.value)}
                      className="w-full bg-gray-50 text-xs border border-gray-200 focus:border-blue-500 rounded-lg p-2.5 outline-none text-gray-700 font-medium"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5">Category</label>
                    <select
                      value={newFileCategory}
                      onChange={(e) => setNewFileCategory(e.target.value)}
                      className="w-full bg-gray-50 text-xs border border-gray-200 focus:border-blue-500 rounded-lg p-2.5 outline-none text-gray-700 font-medium"
                    >
                      <option value="Images">Images</option>
                      <option value="Videos">Videos</option>
                      <option value="Audio">Audio</option>
                      <option value="Documents">Documents</option>
                      <option value="Downloads">Downloads</option>
                    </select>
                  </div>
                </div>

                <div className="pt-4 flex gap-2">
                  <button
                    type="button"
                    onClick={() => setIsAddOpen(false)}
                    className="w-1/2 bg-gray-50 hover:bg-gray-100 text-gray-600 font-semibold py-2 rounded-xl text-xs cursor-pointer border border-gray-200/50"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="w-1/2 bg-gray-900 hover:bg-black text-white font-semibold py-2 rounded-xl text-xs cursor-pointer shadow-xs"
                  >
                    Simulate Upload
                  </button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
