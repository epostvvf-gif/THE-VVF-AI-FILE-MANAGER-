import React, { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { Send, Bot, User, Sparkles, Loader2, RefreshCw } from "lucide-react";
import { ChatMessage } from "../types";

export default function AiAssistant() {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: "welcome",
      role: "assistant",
      content: "Hello! I am your VVF AI Smart Storage Co-pilot. I have indexed your emulated local files! I can analyze which images or recordings are duplicate signatures, explain how to optimize space, or suggest secure file practices for private bank records. Ask me anything about your files!",
      timestamp: new Date().toISOString(),
    },
  ]);
  const [inputValue, setInputValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputValue.trim() || isLoading) return;

    const userMsg: ChatMessage = {
      id: `msg-${Date.now()}`,
      role: "user",
      content: inputValue,
      timestamp: new Date().toISOString(),
    };

    setMessages(prev => [...prev, userMsg]);
    setInputValue("");
    setIsLoading(true);

    try {
      const chatHistory = [...messages, userMsg].map(msg => ({
        role: msg.role,
        content: msg.content,
      }));

      const res = await fetch("/api/gemini/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ messages: chatHistory }),
      });

      const data = await res.json();
      const assistantMsg: ChatMessage = {
        id: `msg-${Date.now() + 1}`,
        role: "assistant",
        content: data.text || "I apologize, but I could not generate a response right now. Please check if your Gemini API Key is configured correctly.",
        timestamp: new Date().toISOString(),
      };

      setMessages(prev => [...prev, assistantMsg]);
    } catch (error) {
      console.error(error);
      const errorMsg: ChatMessage = {
        id: `msg-${Date.now() + 1}`,
        role: "assistant",
        content: "Error reaching the AI server. Please check your network and make sure the Gemini API Key is configured in your Secrets panel.",
        timestamp: new Date().toISOString(),
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSuggestionClick = (text: string) => {
    setInputValue(text);
  };

  const suggestions = [
    "What files do I have on my device?",
    "Where is my blockchain voting draft?",
    "Tell me about my secure folder",
    "How can I save more storage space?",
  ];

  return (
    <div className="max-w-2xl mx-auto bg-white rounded-3xl border border-gray-100 shadow-xl overflow-hidden h-[540px] flex flex-col justify-between">
      {/* Header */}
      <div className="p-4 border-b border-gray-100 bg-linear-to-r from-blue-50/30 to-purple-50/20 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="p-2 bg-blue-500 text-white rounded-xl shadow-xs">
            <Bot className="w-5 h-5 animate-pulse" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-gray-900">VVF Smart File co-pilot</h2>
            <div className="flex items-center gap-1 text-[10px] text-emerald-600 font-bold">
              <Sparkles className="w-3 h-3 text-emerald-500" />
              Llama/Gemini Agent Active
            </div>
          </div>
        </div>
        <button
          onClick={() => {
            setMessages([
              {
                id: "welcome",
                role: "assistant",
                content: "Chat cleared and indexed files re-scanned! What can I help you find or optimize?",
                timestamp: new Date().toISOString(),
              },
            ]);
          }}
          title="Reset Chat History"
          className="p-1.5 hover:bg-gray-100 text-gray-400 hover:text-gray-700 rounded-lg transition"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Message Scroll View */}
      <div className="flex-1 p-6 overflow-y-auto space-y-4 bg-gray-50/30">
        {messages.map(msg => {
          const isUser = msg.role === "user";
          return (
            <div key={msg.id} className={`flex items-start gap-3 ${isUser ? "flex-row-reverse" : ""}`}>
              {/* Avatar */}
              <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 border shadow-2xs ${
                isUser ? "bg-gray-900 border-gray-800 text-white" : "bg-blue-50 border-blue-100 text-blue-600"
              }`}>
                {isUser ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
              </div>

              {/* Message Bubble */}
              <div className={`max-w-[80%] rounded-2xl px-4 py-2.5 text-xs shadow-2xs leading-relaxed ${
                isUser 
                  ? "bg-gray-900 text-white font-medium" 
                  : "bg-white border border-gray-100/80 text-gray-800 font-normal"
              }`}>
                {/* Simple formatting for lines */}
                {msg.content.split("\n").map((line, i) => (
                  <p key={i} className={line.trim() ? "mt-1.5 first:mt-0" : "h-2"}>
                    {line}
                  </p>
                ))}
              </div>
            </div>
          );
        })}

        {isLoading && (
          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-full bg-blue-50 border border-blue-100 text-blue-600 flex items-center justify-center shrink-0">
              <Bot className="w-4 h-4 animate-bounce" />
            </div>
            <div className="bg-white border border-gray-100 rounded-2xl px-4 py-3 text-xs text-gray-500 shadow-2xs flex items-center gap-2">
              <Loader2 className="w-3.5 h-3.5 animate-spin text-blue-500" />
              <span>Analyzing storage records...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Suggestions Tray (Only shown when input is empty and no load) */}
      {inputValue === "" && !isLoading && (
        <div className="px-6 py-2 border-t border-gray-50 overflow-x-auto whitespace-nowrap bg-gray-50/10 scrollbar-none flex gap-2">
          {suggestions.map((s, i) => (
            <button
              key={i}
              onClick={() => handleSuggestionClick(s)}
              className="inline-block text-[10px] font-semibold text-gray-500 hover:text-gray-900 bg-white border border-gray-150 rounded-full px-3 py-1 hover:bg-gray-50 cursor-pointer shadow-3xs shrink-0"
            >
              {s}
            </button>
          ))}
        </div>
      )}

      {/* Send Input Panel */}
      <form onSubmit={handleSendMessage} className="p-4 border-t border-gray-100 bg-white flex items-center gap-2">
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Ask the co-pilot (e.g. Find exact copies)..."
          className="flex-1 bg-gray-50 text-xs border border-gray-200 focus:border-blue-500 focus:bg-white rounded-xl p-3 outline-none text-gray-700 font-medium"
        />
        <button
          type="submit"
          disabled={!inputValue.trim() || isLoading}
          className="bg-gray-900 hover:bg-black disabled:bg-gray-100 disabled:text-gray-300 text-white p-3 rounded-xl shadow-xs transition cursor-pointer"
        >
          <Send className="w-4.5 h-4.5" />
        </button>
      </form>
    </div>
  );
}
