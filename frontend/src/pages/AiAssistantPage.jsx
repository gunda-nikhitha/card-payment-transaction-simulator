import React, { useState, useEffect, useRef } from 'react';
import {
  Bot,
  Send,
  Plus,
  Trash2,
  Sparkles,
  BookOpen,
  User,
  MessageSquare,
  HelpCircle,
  Lightbulb,
} from 'lucide-react';
import { api } from '../api/client';

export default function AiAssistantPage() {
  const [conversations, setConversations] = useState([]);
  const [activeConversationId, setActiveConversationId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const suggestedQuestions = [
    'Explain the difference between Authorization and Settlement',
    'Why do payment transactions return TIMEOUT?',
    'How does an Idempotency Key prevent double-charging?',
    'What are the PCI DSS rules for handling card data?',
    'How does this simulator differ from real payment gateways?',
  ];

  const fetchConversations = async () => {
    try {
      const list = await api.get('/api/ai/conversations');
      setConversations(list || []);
      if (!activeConversationId && list && list.length > 0) {
        selectConversation(list[0].id);
      }
    } catch (err) {
      console.error('Failed to load conversations', err);
    }
  };

  const selectConversation = async (convId) => {
    setActiveConversationId(convId);
    try {
      const detail = await api.get(`/api/ai/conversations/${convId}`);
      setMessages(detail.messages || []);
    } catch (err) {
      console.error('Failed to load conversation details', err);
    }
  };

  useEffect(() => {
    fetchConversations();
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const handleSendMessage = async (textToSend = inputMessage) => {
    const text = textToSend.trim();
    if (!text || loading) return;

    // Optimistic UI update
    const tempUserMsg = {
      id: Date.now(),
      role: 'user',
      content: text,
      createdAt: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, tempUserMsg]);
    setInputMessage('');
    setLoading(true);

    try {
      const res = await api.post('/api/ai/chat', {
        message: text,
        conversationId: activeConversationId,
      });

      // Update active thread ID if new conversation was created
      if (!activeConversationId && res.conversationId) {
        setActiveConversationId(res.conversationId);
        fetchConversations();
      }

      // Add assistant response
      const assistantMsg = {
        id: Date.now() + 1,
        role: 'assistant',
        content: res.reply,
        sources: res.sources,
        createdAt: res.timestamp || new Date().toISOString(),
      };
      setMessages((prev) => [...prev, assistantMsg]);
    } catch (err) {
      const errMsg = {
        id: Date.now() + 1,
        role: 'assistant',
        content: '⚠️ Failed to obtain AI response: ' + (err.message || 'Server error'),
        createdAt: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, errMsg]);
    } finally {
      setLoading(false);
    }
  };

  const handleNewChat = () => {
    setActiveConversationId(null);
    setMessages([]);
    setInputMessage('');
  };

  const handleDeleteConversation = async (convId, e) => {
    e.stopPropagation();
    if (!window.confirm('Delete this chat thread?')) return;
    try {
      await api.delete(`/api/ai/conversations/${convId}`);
      if (activeConversationId === convId) {
        handleNewChat();
      }
      fetchConversations();
    } catch (err) {
      console.error('Failed to delete conversation', err);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', height: 'calc(100vh - 140px)' }}>
      {/* Top Title */}
      <div>
        <h1 style={{ fontSize: '1.85rem', fontWeight: 800, letterSpacing: '-0.02em', color: '#ffffff', display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
          <Bot size={28} color="var(--primary)" />
          Payment Learning Assistant
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.2rem' }}>
          Retrieval-Augmented Generation (RAG) tutor for payment lifecycles and simulator mechanics
        </p>
      </div>

      {/* Main Split Layout: Left Threads, Right Chat */}
      <div style={{ display: 'grid', gridTemplateColumns: '280px 1fr', gap: '1.5rem', flex: 1, minHeight: 0 }}>
        {/* Left Sidebar: Threads */}
        <div
          className="glass-card"
          style={{
            display: 'flex',
            flexDirection: 'column',
            padding: '1.25rem',
            overflow: 'hidden',
          }}
        >
          <button
            onClick={handleNewChat}
            className="btn btn-primary"
            style={{ width: '100%', marginBottom: '1rem', gap: '0.5rem' }}
          >
            <Plus size={16} />
            New Conversation
          </button>

          <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '0.5rem', textTransform: 'uppercase' }}>
            Chat History
          </div>

          <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
            {conversations.length > 0 ? (
              conversations.map((c) => {
                const isActive = activeConversationId === c.id;
                return (
                  <div
                    key={c.id}
                    onClick={() => selectConversation(c.id)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '0.65rem 0.85rem',
                      borderRadius: 'var(--radius-md)',
                      background: isActive ? 'rgba(99, 102, 241, 0.15)' : 'rgba(255, 255, 255, 0.02)',
                      border: '1px solid',
                      borderColor: isActive ? 'var(--border-highlight)' : 'transparent',
                      cursor: 'pointer',
                      transition: 'all 0.15s ease',
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', overflow: 'hidden' }}>
                      <MessageSquare size={14} color={isActive ? '#818cf8' : 'var(--text-muted)'} />
                      <span
                        style={{
                          fontSize: '0.825rem',
                          color: isActive ? '#ffffff' : 'var(--text-secondary)',
                          whiteSpace: 'nowrap',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          fontWeight: isActive ? 600 : 400,
                        }}
                      >
                        {c.title}
                      </span>
                    </div>

                    <button
                      onClick={(e) => handleDeleteConversation(c.id, e)}
                      style={{
                        background: 'transparent',
                        border: 'none',
                        color: 'var(--text-muted)',
                        cursor: 'pointer',
                        padding: '0.2rem',
                        display: 'flex',
                      }}
                      title="Delete thread"
                    >
                      <Trash2 size={13} />
                    </button>
                  </div>
                );
              })
            ) : (
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', textAlign: 'center', marginTop: '2rem' }}>
                No past conversations.
              </div>
            )}
          </div>
        </div>

        {/* Right Pane: Message Feed & Prompt Area */}
        <div
          className="glass-card"
          style={{
            display: 'flex',
            flexDirection: 'column',
            padding: '1.25rem',
            overflow: 'hidden',
          }}
        >
          {/* Messages Stream */}
          <div style={{ flex: 1, overflowY: 'auto', paddingRight: '0.5rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            {messages.length === 0 && !loading ? (
              <div style={{ margin: 'auto', textAlign: 'center', maxWidth: '520px', padding: '2rem 1rem' }}>
                <div
                  style={{
                    width: '60px',
                    height: '60px',
                    borderRadius: '16px',
                    background: 'rgba(99, 102, 241, 0.15)',
                    border: '1px solid var(--border-highlight)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 1.25rem',
                  }}
                >
                  <Sparkles size={30} color="var(--primary)" />
                </div>
                <h3 style={{ fontSize: '1.35rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.5rem' }}>
                  Ask Me Anything About Card Payments
                </h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '1.75rem' }}>
                  I search MySQL knowledge documents for instant answers on transaction outcomes, banking networks, and idempotency.
                </p>

                <div style={{ textAlign: 'left' }}>
                  <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-accent)', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Lightbulb size={14} /> SUGGESTED TOPICS
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {suggestedQuestions.map((q) => (
                      <button
                        key={q}
                        onClick={() => handleSendMessage(q)}
                        className="btn btn-secondary btn-sm"
                        style={{ justifyContent: 'flex-start', textAlign: 'left', fontSize: '0.8rem', padding: '0.6rem 0.85rem' }}
                      >
                        {q}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              messages.map((m) => {
                const isUser = m.role === 'user';
                return (
                  <div
                    key={m.id}
                    style={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: isUser ? 'flex-end' : 'flex-start',
                    }}
                  >
                    <div
                      style={{
                        maxWidth: '85%',
                        padding: '1rem 1.25rem',
                        borderRadius: 'var(--radius-lg)',
                        background: isUser ? 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)' : 'rgba(15, 23, 42, 0.95)',
                        border: isUser ? 'none' : '1px solid var(--border-card)',
                        color: '#ffffff',
                        fontSize: '0.9rem',
                        lineHeight: 1.6,
                        boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
                      }}
                    >
                      <div style={{ whiteSpace: 'pre-wrap' }}>{m.content}</div>

                      {/* Cited Sources for Assistant */}
                      {!isUser && m.sources && m.sources.length > 0 && (
                        <div
                          style={{
                            marginTop: '0.85rem',
                            paddingTop: '0.75rem',
                            borderTop: '1px solid rgba(255, 255, 255, 0.1)',
                            display: 'flex',
                            flexWrap: 'wrap',
                            gap: '0.4rem',
                            alignItems: 'center',
                          }}
                        >
                          <span style={{ fontSize: '0.7rem', color: 'var(--text-accent)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                            <BookOpen size={12} /> CITED KNOWLEDGE:
                          </span>
                          {m.sources.map((s) => (
                            <span
                              key={s}
                              style={{
                                fontSize: '0.7rem',
                                padding: '0.15rem 0.5rem',
                                borderRadius: '4px',
                                background: 'rgba(99, 102, 241, 0.18)',
                                border: '1px solid rgba(99, 102, 241, 0.3)',
                                color: '#a5b4fc',
                              }}
                            >
                              {s}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                );
              })
            )}

            {loading && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-accent)', fontSize: '0.85rem', padding: '0.5rem 0' }}>
                <Bot size={18} className="spin" />
                Searching knowledge base and generating answer...
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Bottom Chat Input Form */}
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSendMessage();
            }}
            style={{
              display: 'flex',
              gap: '0.75rem',
              marginTop: '1rem',
              paddingTop: '1rem',
              borderTop: '1px solid var(--border-subtle)',
            }}
          >
            <input
              type="text"
              placeholder="Ask about authorization, decline codes, idempotency, PCI DSS..."
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              disabled={loading}
              className="form-input"
              style={{ flex: 1 }}
            />

            <button
              type="submit"
              disabled={!inputMessage.trim() || loading}
              className="btn btn-primary"
            >
              <Send size={16} />
              Send
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
