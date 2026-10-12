import React, { useState, useEffect } from 'react';
import {
  Shield,
  Users,
  CreditCard,
  TrendingUp,
  CheckCircle2,
  XCircle,
  ToggleLeft,
  ToggleRight,
  UserCheck,
  RefreshCw,
  Eye,
} from 'lucide-react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import MetricCard from '../components/MetricCard';
import StatusBadge from '../components/StatusBadge';
import TransactionModal from '../components/TransactionModal';

export default function AdminPage() {
  const { isAdmin } = useAuth();
  const [metrics, setMetrics] = useState(null);
  const [users, setUsers] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTxn, setSelectedTxn] = useState(null);
  const [activeTab, setActiveTab] = useState('users'); // 'users' or 'transactions'

  const fetchAdminData = async () => {
    setLoading(true);
    try {
      const [metricsRes, usersRes, txnsRes] = await Promise.all([
        api.get('/api/admin/dashboard'),
        api.get('/api/admin/users?size=20'),
        api.get('/api/admin/transactions?size=20'),
      ]);
      setMetrics(metricsRes);
      setUsers(usersRes.content || []);
      setTransactions(txnsRes.content || []);
    } catch (err) {
      console.error('Failed to load admin data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAdmin) {
      fetchAdminData();
    }
  }, [isAdmin]);

  if (!isAdmin) {
    return (
      <div style={{ padding: '4rem 1rem', textAlign: 'center' }}>
        <Shield size={48} color="#ef4444" style={{ margin: '0 auto 1rem' }} />
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Access Denied</h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
          You must have administrator privileges to view this portal.
        </p>
      </div>
    );
  }

  const handleToggleActive = async (userId, currentActive) => {
    try {
      await api.put(`/api/admin/users/${userId}/status?active=${!currentActive}`);
      fetchAdminData();
    } catch (err) {
      alert('Failed to update user status: ' + err.message);
    }
  };

  const handleChangeRole = async (userId, currentRole) => {
    const newRole = currentRole === 'ADMIN' ? 'USER' : 'ADMIN';
    if (!window.confirm(`Change this user's role to ${newRole}?`)) return;
    try {
      await api.put(`/api/admin/users/${userId}/role?role=${newRole}`);
      fetchAdminData();
    } catch (err) {
      alert('Failed to update user role: ' + err.message);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Admin Title */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800, letterSpacing: '-0.02em', color: '#ffffff', display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <Shield size={26} color="var(--primary)" />
            Administrator Control Center
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.2rem' }}>
            Global user account governance and system-wide transaction auditing
          </p>
        </div>

        <button onClick={fetchAdminData} className="btn btn-secondary">
          <RefreshCw size={16} className={loading ? 'spin' : ''} />
          Refresh
        </button>
      </div>

      {/* Global System KPIs */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem' }}>
        <MetricCard
          title="System Users"
          value={users.length}
          subtitle="Registered accounts"
          icon={Users}
          color="#a855f7"
        />
        <MetricCard
          title="Global Transactions"
          value={metrics?.totalTransactions ?? 0}
          subtitle="Across all users"
          icon={CreditCard}
          color="#6366f1"
        />
        <MetricCard
          title="Global Approval Rate"
          value={`${metrics?.approvalRate ?? 0}%`}
          subtitle={`${metrics?.approvedCount ?? 0} total approved`}
          icon={CheckCircle2}
          color="#10b981"
        />
        <MetricCard
          title="Total Processed Volume"
          value={`₹${Number(metrics?.totalAmount ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}`}
          subtitle="System-wide aggregate volume"
          icon={TrendingUp}
          color="#06b6d4"
        />
      </div>

      {/* View Selector Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
        <button
          onClick={() => setActiveTab('users')}
          className={`btn ${activeTab === 'users' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
        >
          <Users size={15} />
          User Account Management ({users.length})
        </button>
        <button
          onClick={() => setActiveTab('transactions')}
          className={`btn ${activeTab === 'transactions' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
        >
          <CreditCard size={15} />
          Global Transaction Audit ({transactions.length})
        </button>
      </div>

      {/* User Management Section */}
      {activeTab === 'users' && (
        <div className="glass-card">
          <div style={{ marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 600, color: '#ffffff' }}>Registered User Accounts</h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Manage access permissions, active status, and administrative roles
            </p>
          </div>

          <div className="table-container">
            <table className="modern-table">
              <thead>
                <tr>
                  <th>User ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Account Status</th>
                  <th>Simulations</th>
                  <th>Joined Date</th>
                  <th style={{ textAlign: 'center' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>#{u.id}</td>
                    <td style={{ fontWeight: 600 }}>{u.name}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{u.email}</td>
                    <td>
                      <span className={`badge ${u.role === 'ADMIN' ? 'badge-admin' : 'badge-user'}`}>
                        {u.role}
                      </span>
                    </td>
                    <td>
                      <span
                        style={{
                          fontSize: '0.8rem',
                          fontWeight: 600,
                          color: u.active ? '#34d399' : '#f87171',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.35rem',
                        }}
                      >
                        {u.active ? <CheckCircle2 size={13} /> : <XCircle size={13} />}
                        {u.active ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>{u.transactionCount}</td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : 'N/A'}
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
                        <button
                          onClick={() => handleToggleActive(u.id, u.active)}
                          className="btn btn-secondary btn-sm"
                          title={u.active ? 'Disable Account' : 'Activate Account'}
                        >
                          {u.active ? 'Disable' : 'Enable'}
                        </button>
                        <button
                          onClick={() => handleChangeRole(u.id, u.role)}
                          className="btn btn-outline btn-sm"
                          title="Switch between USER and ADMIN"
                        >
                          {u.role === 'ADMIN' ? 'Demote' : 'Promote'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Global Transaction Audit Section */}
      {activeTab === 'transactions' && (
        <div className="glass-card">
          <div style={{ marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 600, color: '#ffffff' }}>Global Transaction Audit Ledger</h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Comprehensive log of all transactions processed by any system user
            </p>
          </div>

          <div className="table-container">
            <table className="modern-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Merchant</th>
                  <th>Amount</th>
                  <th>Card Suffix</th>
                  <th>Status</th>
                  <th>Reason</th>
                  <th>Timestamp</th>
                  <th style={{ textAlign: 'center' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((txn) => (
                  <tr key={txn.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--text-accent)' }}>
                      {txn.transactionReference}
                    </td>
                    <td style={{ fontWeight: 500 }}>{txn.merchantName}</td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>
                      {txn.currency} {Number(txn.amount).toFixed(2)}
                    </td>
                    <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                      {txn.maskedCardSuffix || 'N/A'}
                    </td>
                    <td>
                      <StatusBadge status={txn.status} />
                    </td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      {txn.reasonCode || '—'}
                    </td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      {txn.createdAt ? new Date(txn.createdAt).toLocaleString() : 'N/A'}
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <button
                        onClick={() => setSelectedTxn(txn)}
                        className="btn btn-secondary btn-sm"
                        title="Inspect transaction details"
                      >
                        <Eye size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {selectedTxn && (
        <TransactionModal
          transaction={selectedTxn}
          onClose={() => setSelectedTxn(null)}
        />
      )}
    </div>
  );
}
