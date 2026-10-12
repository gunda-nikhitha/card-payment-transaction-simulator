import React, { useState, useEffect } from 'react';
import {
  CreditCard,
  TrendingUp,
  CheckCircle2,
  XCircle,
  Clock,
  ArrowUpRight,
  RefreshCw,
  PlusCircle,
} from 'lucide-react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip as RechartsTooltip,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
} from 'recharts';
import { api } from '../api/client';
import MetricCard from '../components/MetricCard';
import StatusBadge from '../components/StatusBadge';
import TransactionModal from '../components/TransactionModal';

export default function DashboardPage({ onNavigateToSimulate, onNavigateToHistory }) {
  const [summary, setSummary] = useState(null);
  const [distribution, setDistribution] = useState([]);
  const [trends, setTrends] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTxn, setSelectedTxn] = useState(null);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const [sumRes, distRes, trendRes] = await Promise.all([
        api.get('/api/dashboard/summary'),
        api.get('/api/dashboard/status-distribution'),
        api.get('/api/dashboard/trends?days=14'),
      ]);
      setSummary(sumRes);
      setDistribution(distRes || []);
      setTrends(trendRes || []);
    } catch (err) {
      console.error('Failed to load dashboard metrics', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const pieColors = {
    APPROVED: '#10b981',
    DECLINED: '#ef4444',
    TIMEOUT: '#f59e0b',
  };

  const chartDistData = distribution.map((item) => ({
    name: item.status,
    value: item.count,
    color: pieColors[item.status] || '#6366f1',
  }));

  const hasChartData = chartDistData.some((d) => d.value > 0);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Top Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800, letterSpacing: '-0.02em', color: '#ffffff' }}>
            Payment Dashboard
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.2rem' }}>
            Real-time simulation analytics and transaction performance
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button onClick={fetchDashboardData} className="btn btn-secondary" title="Refresh metrics">
            <RefreshCw size={16} className={loading ? 'spin' : ''} />
            Refresh
          </button>
          <button onClick={onNavigateToSimulate} className="btn btn-primary">
            <PlusCircle size={17} />
            Simulate Transaction
          </button>
        </div>
      </div>

      {/* KPI Stats Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem' }}>
        <MetricCard
          title="Total Transactions"
          value={summary?.totalTransactions ?? 0}
          subtitle="All processed simulations"
          icon={CreditCard}
          color="#6366f1"
        />
        <MetricCard
          title="Approval Rate"
          value={`${summary?.approvalRate ?? 0}%`}
          subtitle={`${summary?.approvedCount ?? 0} approved authorizations`}
          icon={CheckCircle2}
          color="#10b981"
        />
        <MetricCard
          title="Total Volume"
          value={`₹${Number(summary?.totalAmount ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}`}
          subtitle="Aggregate transaction volume"
          icon={TrendingUp}
          color="#06b6d4"
        />
        <MetricCard
          title="Declined & Timeouts"
          value={`${(summary?.declinedCount ?? 0) + (summary?.timeoutCount ?? 0)}`}
          subtitle={`${summary?.declinedCount ?? 0} declined, ${summary?.timeoutCount ?? 0} timed out`}
          icon={XCircle}
          color="#ef4444"
        />
      </div>

      {/* Analytics Charts Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '1.5rem' }}>
        {/* Donut Chart: Status Distribution */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '0.5rem', color: '#ffffff' }}>
            Outcome Distribution
          </h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.25rem' }}>
            Ratio of approved, declined, and timeout outcomes
          </p>

          <div style={{ width: '100%', height: 260, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            {hasChartData ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={chartDistData}
                    innerRadius={65}
                    outerRadius={95}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {chartDistData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <RechartsTooltip
                    contentStyle={{
                      background: '#0f172a',
                      borderColor: 'var(--border-card)',
                      borderRadius: '8px',
                      color: '#ffffff',
                    }}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                No simulation data recorded yet.
              </div>
            )}
          </div>

          {/* Distribution Legend */}
          <div style={{ display: 'flex', justifyContent: 'space-around', marginTop: '1rem', borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem' }}>
            {chartDistData.map((d) => (
              <div key={d.name} style={{ textAlign: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', justifyContent: 'center', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                  <div style={{ width: 8, height: 8, borderRadius: '50%', background: d.color }} />
                  {d.name}
                </div>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                  {d.value}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Area Chart: Daily Trends */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '0.5rem', color: '#ffffff' }}>
            Daily Volume Trends
          </h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.25rem' }}>
            Simulated transaction counts over the last 14 days
          </p>

          <div style={{ width: '100%', height: 260 }}>
            {trends.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={trends} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="trendGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#6366f1" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                  <XAxis dataKey="date" stroke="var(--text-muted)" fontSize={11} tickLine={false} />
                  <YAxis stroke="var(--text-muted)" fontSize={11} tickLine={false} />
                  <RechartsTooltip
                    contentStyle={{
                      background: '#0f172a',
                      borderColor: 'var(--border-card)',
                      borderRadius: '8px',
                      color: '#ffffff',
                    }}
                  />
                  <Area
                    type="monotone"
                    dataKey="totalTransactions"
                    name="Transactions"
                    stroke="#6366f1"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#trendGradient)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Run simulations to view daily volume trends.
              </div>
            )}
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem', borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            <span>Timeframe: Past 14 Days</span>
            <span style={{ color: 'var(--text-accent)', cursor: 'pointer' }} onClick={onNavigateToHistory}>
              View Ledger →
            </span>
          </div>
        </div>
      </div>

      {/* Recent Transactions Table */}
      <div className="glass-card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <div>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 600, color: '#ffffff' }}>Recent Transactions</h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Latest simulated card authorizations</p>
          </div>
          <button onClick={onNavigateToHistory} className="btn btn-outline btn-sm">
            View All History
            <ArrowUpRight size={15} />
          </button>
        </div>

        <div className="table-container">
          <table className="modern-table">
            <thead>
              <tr>
                <th>Reference</th>
                <th>Merchant</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              {summary?.recentTransactions && summary.recentTransactions.length > 0 ? (
                summary.recentTransactions.map((txn) => (
                  <tr
                    key={txn.reference}
                    onClick={() => {
                      // Fetch full transaction details on click
                      api.get(`/api/transactions/ref/${txn.reference}`).then(setSelectedTxn).catch(console.error);
                    }}
                    style={{ cursor: 'pointer' }}
                  >
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--text-accent)' }}>
                      {txn.reference}
                    </td>
                    <td style={{ fontWeight: 500 }}>{txn.merchantName}</td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>
                      {txn.currency} {Number(txn.amount).toFixed(2)}
                    </td>
                    <td>
                      <StatusBadge status={txn.status} />
                    </td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                      {txn.createdAt ? new Date(txn.createdAt).toLocaleTimeString() : 'N/A'}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5" style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text-muted)' }}>
                    No transactions yet. Click <strong>Simulate Transaction</strong> to create your first simulation!
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Transaction Details Modal */}
      {selectedTxn && (
        <TransactionModal
          transaction={selectedTxn}
          onClose={() => setSelectedTxn(null)}
        />
      )}
    </div>
  );
}
