import React, { useState } from 'react';
import {
  CreditCard,
  Play,
  RotateCcw,
  CheckCircle2,
  XCircle,
  Clock,
  Sparkles,
  Shield,
  Layers,
  ArrowRight,
} from 'lucide-react';
import { api } from '../api/client';
import CreditCardPreview from '../components/CreditCardPreview';
import StatusBadge from '../components/StatusBadge';
import TransactionModal from '../components/TransactionModal';

export default function SimulatePage({ onNavigateToHistory }) {
  const [merchantName, setMerchantName] = useState('Amazon India');
  const [amount, setAmount] = useState('1499.00');
  const [currency, setCurrency] = useState('INR');
  const [maskedCardSuffix, setMaskedCardSuffix] = useState('**** **** **** 1234');
  const [cardholderName, setCardholderName] = useState('ALEX MORGAN');
  const [description, setDescription] = useState('Online purchase simulation');
  const [idempotencyKey, setIdempotencyKey] = useState(() => crypto.randomUUID());

  const [processingState, setProcessingState] = useState('IDLE'); // IDLE, RECEIVED, PROCESSING, COMPLETED
  const [simulationResult, setSimulationResult] = useState(null);
  const [error, setError] = useState('');
  const [selectedTxn, setSelectedTxn] = useState(null);

  const merchants = [
    'Amazon India',
    'Netflix Subscription',
    'Uber Technologies',
    'Apple Store',
    'Swiggy Foods',
    'Starbucks Coffee',
  ];

  const handleTestScenario = (type) => {
    if (type === 'APPROVE') {
      setMaskedCardSuffix('**** **** **** 1111');
      setAmount('450.00');
      setDescription('Deterministic Test: Card 1111 forces APPROVAL');
    } else if (type === 'DECLINE') {
      setMaskedCardSuffix('**** **** **** 0000');
      setAmount('750.00');
      setDescription('Deterministic Test: Card 0000 forces DECLINE');
    } else if (type === 'TIMEOUT') {
      setMaskedCardSuffix('**** **** **** 9999');
      setAmount('1200.00');
      setDescription('Deterministic Test: Card 9999 forces TIMEOUT');
    } else if (type === 'RULE_99') {
      setMaskedCardSuffix('**** **** **** 4321');
      setAmount('99.99');
      setDescription('Deterministic Test: Amount ending in .99 triggers decline scenario');
    }
    setIdempotencyKey(crypto.randomUUID());
  };

  const handleSimulate = async (e) => {
    e.preventDefault();
    setError('');
    setSimulationResult(null);

    // Multi-stage visual state transition
    setProcessingState('RECEIVED');

    try {
      setTimeout(() => {
        setProcessingState('PROCESSING');
      }, 400);

      const payload = {
        merchantName,
        amount: parseFloat(amount),
        currency,
        maskedCardSuffix,
        cardholderName,
        description,
        idempotencyKey,
      };

      const result = await api.post('/api/transactions/simulate', payload);

      setTimeout(() => {
        setSimulationResult(result);
        setProcessingState('COMPLETED');
      }, 900);
    } catch (err) {
      setProcessingState('IDLE');
      setError(err.message || 'Simulation failed');
    }
  };

  const handleReset = () => {
    setProcessingState('IDLE');
    setSimulationResult(null);
    setIdempotencyKey(crypto.randomUUID());
    setError('');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Page Header */}
      <div>
        <h1 style={{ fontSize: '1.85rem', fontWeight: 800, letterSpacing: '-0.02em', color: '#ffffff' }}>
          Simulate Payment Transaction
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.2rem' }}>
          Configure dummy card and merchant parameters to observe authorization outcomes
        </p>
      </div>

      {error && (
        <div
          style={{
            padding: '1rem',
            borderRadius: 'var(--radius-md)',
            background: 'rgba(239, 68, 68, 0.12)',
            border: '1px solid rgba(239, 68, 68, 0.35)',
            color: '#f87171',
            fontSize: '0.9rem',
          }}
        >
          {error}
        </div>
      )}

      {/* Main Grid: Form Left, Card + Results Right */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '2rem', alignItems: 'start' }}>
        {/* Left Column: Form & Triggers */}
        <div className="glass-card">
          <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '0.5rem', color: '#ffffff', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Layers size={18} color="var(--primary)" />
            Transaction Parameters
          </h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
            All card details are masked and strictly educational.
          </p>

          {/* Test Trigger Scenario Presets */}
          <div
            style={{
              marginBottom: '1.5rem',
              padding: '1rem',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(255, 255, 255, 0.02)',
              border: '1px dashed var(--border-highlight)',
            }}
          >
            <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-accent)', marginBottom: '0.65rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <Sparkles size={14} /> 1-CLICK TEST SCENARIO PRESETS
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
              <button
                type="button"
                onClick={() => handleTestScenario('APPROVE')}
                className="btn btn-secondary btn-sm"
                style={{ color: '#34d399', borderColor: 'rgba(16, 185, 129, 0.3)' }}
              >
                🟢 Card 1111 (Approve)
              </button>
              <button
                type="button"
                onClick={() => handleTestScenario('DECLINE')}
                className="btn btn-secondary btn-sm"
                style={{ color: '#f87171', borderColor: 'rgba(239, 68, 68, 0.3)' }}
              >
                🔴 Card 0000 (Decline)
              </button>
              <button
                type="button"
                onClick={() => handleTestScenario('TIMEOUT')}
                className="btn btn-secondary btn-sm"
                style={{ color: '#fbbf24', borderColor: 'rgba(245, 158, 11, 0.3)' }}
              >
                🟡 Card 9999 (Timeout)
              </button>
              <button
                type="button"
                onClick={() => handleTestScenario('RULE_99')}
                className="btn btn-secondary btn-sm"
              >
                ⚡ .99 Rule (Decline)
              </button>
            </div>
          </div>

          <form onSubmit={handleSimulate}>
            {/* Merchant selection */}
            <div className="form-group">
              <label className="form-label">Merchant Name</label>
              <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem', flexWrap: 'wrap' }}>
                {merchants.slice(0, 3).map((m) => (
                  <button
                    key={m}
                    type="button"
                    onClick={() => setMerchantName(m)}
                    className="btn btn-secondary btn-sm"
                    style={{
                      background: merchantName === m ? 'rgba(99, 102, 241, 0.2)' : undefined,
                      borderColor: merchantName === m ? 'var(--primary)' : undefined,
                    }}
                  >
                    {m}
                  </button>
                ))}
              </div>
              <input
                type="text"
                required
                value={merchantName}
                onChange={(e) => setMerchantName(e.target.value)}
                className="form-input"
              />
            </div>

            {/* Amount & Currency */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 140px', gap: '1rem' }}>
              <div className="form-group">
                <label className="form-label">Amount</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="form-input"
                  style={{ fontFamily: 'var(--font-mono)' }}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Currency</label>
                <select
                  value={currency}
                  onChange={(e) => setCurrency(e.target.value)}
                  className="form-select"
                >
                  <option value="INR">INR (₹)</option>
                  <option value="USD">USD ($)</option>
                  <option value="EUR">EUR (€)</option>
                  <option value="GBP">GBP (£)</option>
                </select>
              </div>
            </div>

            {/* Cardholder Name & Masked Card Suffix */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="form-group">
                <label className="form-label">Cardholder Name</label>
                <input
                  type="text"
                  required
                  value={cardholderName}
                  onChange={(e) => setCardholderName(e.target.value.toUpperCase())}
                  className="form-input"
                />
              </div>

              <div className="form-group">
                <label className="form-label">Masked Card Suffix</label>
                <input
                  type="text"
                  required
                  value={maskedCardSuffix}
                  onChange={(e) => setMaskedCardSuffix(e.target.value)}
                  placeholder="**** **** **** 1234"
                  className="form-input"
                  style={{ fontFamily: 'var(--font-mono)' }}
                />
              </div>
            </div>

            {/* Idempotency Key */}
            <div className="form-group">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                <label className="form-label" style={{ margin: 0 }}>Idempotency Key (UUID)</label>
                <button
                  type="button"
                  onClick={() => setIdempotencyKey(crypto.randomUUID())}
                  style={{ background: 'transparent', border: 'none', color: 'var(--text-accent)', fontSize: '0.75rem', cursor: 'pointer' }}
                >
                  Generate New
                </button>
              </div>
              <input
                type="text"
                readOnly
                value={idempotencyKey}
                className="form-input"
                style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: 'var(--text-muted)' }}
              />
            </div>

            {/* Action Buttons */}
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
              <button
                type="submit"
                disabled={processingState !== 'IDLE' && processingState !== 'COMPLETED'}
                className="btn btn-primary btn-lg"
                style={{ flex: 1 }}
              >
                <Play size={18} />
                Process Simulated Payment
              </button>

              <button
                type="button"
                onClick={handleReset}
                className="btn btn-secondary btn-lg"
              >
                <RotateCcw size={18} />
              </button>
            </div>
          </form>
        </div>

        {/* Right Column: Virtual Card Preview & Live Output */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Virtual Card */}
          <CreditCardPreview
            cardNumber={maskedCardSuffix}
            cardholderName={cardholderName}
            status={simulationResult?.status}
          />

          {/* Lifecycle Progress Bar */}
          <div className="glass-card" style={{ padding: '1.25rem' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.85rem' }}>
              SIMULATION PIPELINE STATE
            </div>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', position: 'relative' }}>
              {/* Step 1: Received */}
              <div style={{ textAlign: 'center', zIndex: 1 }}>
                <div
                  style={{
                    width: 32,
                    height: 32,
                    borderRadius: '50%',
                    background: processingState !== 'IDLE' ? '#10b981' : 'rgba(255,255,255,0.1)',
                    color: '#ffffff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 0.25rem',
                    fontWeight: 700,
                    fontSize: '0.8rem',
                  }}
                >
                  1
                </div>
                <div style={{ fontSize: '0.75rem', fontWeight: 600, color: processingState !== 'IDLE' ? '#10b981' : 'var(--text-muted)' }}>
                  RECEIVED
                </div>
              </div>

              {/* Connecting line */}
              <div style={{ flex: 1, height: 2, background: processingState === 'PROCESSING' || processingState === 'COMPLETED' ? '#6366f1' : 'rgba(255,255,255,0.1)', margin: '0 0.5rem -1rem' }} />

              {/* Step 2: Processing */}
              <div style={{ textAlign: 'center', zIndex: 1 }}>
                <div
                  style={{
                    width: 32,
                    height: 32,
                    borderRadius: '50%',
                    background: processingState === 'PROCESSING' || processingState === 'COMPLETED' ? '#6366f1' : 'rgba(255,255,255,0.1)',
                    color: '#ffffff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 0.25rem',
                    fontWeight: 700,
                    fontSize: '0.8rem',
                  }}
                >
                  2
                </div>
                <div style={{ fontSize: '0.75rem', fontWeight: 600, color: processingState === 'PROCESSING' || processingState === 'COMPLETED' ? '#818cf8' : 'var(--text-muted)' }}>
                  PROCESSING
                </div>
              </div>

              {/* Connecting line */}
              <div style={{ flex: 1, height: 2, background: processingState === 'COMPLETED' ? '#10b981' : 'rgba(255,255,255,0.1)', margin: '0 0.5rem -1rem' }} />

              {/* Step 3: Result */}
              <div style={{ textAlign: 'center', zIndex: 1 }}>
                <div
                  style={{
                    width: 32,
                    height: 32,
                    borderRadius: '50%',
                    background: processingState === 'COMPLETED' ? '#10b981' : 'rgba(255,255,255,0.1)',
                    color: '#ffffff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 0.25rem',
                    fontWeight: 700,
                    fontSize: '0.8rem',
                  }}
                >
                  3
                </div>
                <div style={{ fontSize: '0.75rem', fontWeight: 600, color: processingState === 'COMPLETED' ? '#34d399' : 'var(--text-muted)' }}>
                  {simulationResult?.status || 'RESULT'}
                </div>
              </div>
            </div>
          </div>

          {/* Simulation Result Card */}
          {simulationResult && (
            <div
              className="glass-card"
              style={{
                border: '1px solid',
                borderColor: simulationResult.status === 'APPROVED' ? 'var(--status-approved-border)' : simulationResult.status === 'DECLINED' ? 'var(--status-declined-border)' : 'var(--status-timeout-border)',
                background: simulationResult.status === 'APPROVED' ? 'rgba(16, 185, 129, 0.06)' : simulationResult.status === 'DECLINED' ? 'rgba(239, 68, 68, 0.06)' : 'rgba(245, 158, 11, 0.06)',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <StatusBadge status={simulationResult.status} />
                <span style={{ fontSize: '0.8rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                  {simulationResult.processingDurationMs} ms
                </span>
              </div>

              <div style={{ fontSize: '1.4rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.5rem' }}>
                {simulationResult.currency} {Number(simulationResult.amount).toFixed(2)}
              </div>

              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.75rem' }}>
                {simulationResult.statusMessage}
              </div>

              <div style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
                Ref: {simulationResult.transactionReference}
              </div>

              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <button
                  type="button"
                  onClick={() => setSelectedTxn(simulationResult)}
                  className="btn btn-secondary btn-sm"
                  style={{ flex: 1 }}
                >
                  Inspect Full Details
                </button>
                <button
                  type="button"
                  onClick={onNavigateToHistory}
                  className="btn btn-outline btn-sm"
                >
                  View Ledger
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {selectedTxn && (
        <TransactionModal
          transaction={selectedTxn}
          onClose={() => setSelectedTxn(null)}
        />
      )}
    </div>
  );
}
