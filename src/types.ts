export interface StorageFile {
  id: string;
  name: string;
  size: number;
  category: string;
  path: string;
  dateAdded: string;
  sha256: string;
  isEncrypted?: boolean;
}

export interface DuplicateGroup {
  id: string;
  type: string;
  description: string;
  similarityScore?: number;
  files: StorageFile[];
}

export interface JunkFile {
  id: string;
  name: string;
  size: number;
  type: string;
  description: string;
}

export interface CloudFile {
  id: string;
  name: string;
  size: number;
  category: string;
  lastSynced: string;
}

export interface GitCommit {
  id: string;
  hash: string;
  message: string;
  date: string;
  filesCount: number;
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  timestamp: string;
}

export interface StorageStats {
  totalSpace: number;
  usedSpace: number;
}
