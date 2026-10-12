import React, { useState } from 'react';
import { CreditCard, Loader2 } from 'lucide-react';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import SimulatePage from './pages/SimulatePage';
import HistoryPage from './pages/HistoryPage';
import AiAssistantPage from './pages/AiAssistantPage';
import AdminPage from './pages/AdminPage';
import ProfilePage from './pages/ProfilePage';

function MainApp() {
  const { isAuthenticated, loading } = useAuth();
  const [authMode, setAuthMode] = useState('login'); // 'login' or 'register'
  const [activeTab, setActiveTab] = useState('dashboard');

  if (loading) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '1rem',
          background: 'var(--bg-app)',
        }}
      >
        <div
          style={{
            width: '56px',
            height: '56px',
            borderRadius: '16px',
            background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 30px rgba(99, 102, 241, 0.5)',
          }}
        >
          <CreditCard size={28} color="#ffffff" />
        </div>
        <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Loader2 size={16} className="spin" />
          Initializing Payment Simulator...
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    if (authMode === 'register') {
      return <RegisterPage onSwitchToLogin={() => setAuthMode('login')} />;
    }
    return <LoginPage onSwitchToRegister={() => setAuthMode('register')} />;
  }

  return (
    <div className="app-container">
      <Navbar activeTab={activeTab} onSelectTab={setActiveTab} />

      <main className="main-content">
        {activeTab === 'dashboard' && (
          <DashboardPage
            onNavigateToSimulate={() => setActiveTab('simulate')}
            onNavigateToHistory={() => setActiveTab('history')}
          />
        )}
        {activeTab === 'simulate' && (
          <SimulatePage onNavigateToHistory={() => setActiveTab('history')} />
        )}
        {activeTab === 'history' && <HistoryPage />}
        {activeTab === 'ai' && <AiAssistantPage />}
        {activeTab === 'admin' && <AdminPage />}
        {activeTab === 'profile' && <ProfilePage />}
      </main>

      <footer
        style={{
          borderTop: '1px solid var(--border-subtle)',
          padding: '1.5rem',
          textAlign: 'center',
          fontSize: '0.8rem',
          color: 'var(--text-muted)',
          background: 'rgba(8, 12, 20, 0.6)',
        }}
      >
        <div style={{ maxWidth: '1200px', margin: '0 auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
          <div>
            <strong>Card Payment Transaction Simulator (CPTS)</strong> — Educational Full-Stack Architecture
          </div>
          <div>
            Spring Boot 3.3 • React 19 • MySQL 8 • JWT • RAG AI • Docker • Render
          </div>
        </div>
      </footer>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}
