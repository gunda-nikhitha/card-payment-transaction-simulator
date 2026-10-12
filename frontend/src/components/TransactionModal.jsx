import React from 'react';
import { X, Copy, Check, Clock, ShieldCheck, ArrowRight, Activity } from 'lucide-react';
import StatusBadge from './StatusBadge';

export default function TransactionModal({ transaction, onClose }) {
  const [copied, setCopied] = React.useState(false);

  if (!transaction) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(transaction.transactionReference);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '1.25rem 1.5rem',
            borderBottom: '1px solid var(--border-subtle)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 600 }}>Transaction Details</h3>
            <StatusBadge status={transaction.status} />
          </div>
          <button
            onClick={onClose}
            style={{
              background: 'transparent',
              border: 'none',
              color: 'var(--text-muted)',
              cursor: 'pointer',
              display: 'flex',
              padding: '0.25rem',
            }}
          >
            <X size={20} />
          </button>
        </div>

        {/* Modal Body */}
        <div style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {/* Main Amount & Reference Card */}
          <div
            style={{
              padding: '1.25rem',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(255, 255, 255, 0.02)',
              border: '1px solid var(--border-card)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Amount Charged</div>
              <div
                style={{
                  fontSize: '1.75rem',
                  fontWeight: 700,
                  fontFamily: 'var(--font-mono)',
                  color: 'var(--text-primary)',
                }}
              >
                {transaction.currency} {Number(transaction.amount).toFixed(2)}
              </div>
            </div>

            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.25rem' }}>
                Transaction Reference
              </div>
              <button
                onClick={handleCopy}
                className="btn btn-secondary btn-sm"
                style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem' }}
              >
                {transaction.transactionReference}
                {copied ? <Check size={14} color="#10b981" /> : <Copy size={14} />}
              </button>
            </div>
          </div>

          {/* Educational Lifecycle Stepper */}
          <div
            style={{
              padding: '1rem',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(15, 23, 42, 0.6)',
              border: '1px solid var(--border-subtle)',
            }}
          >
            <div style={{ fontSize: '0.8rem', color: 'var(--text-accent)', fontWeight: 600, marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Activity size={14} /> SIMULATED PROCESSING LIFECYCLE
            </div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              <span style={{ color: '#10b981', fontWeight: 600 }}>1. RECEIVED</span>
              <ArrowRight size={14} />
              <span style={{ color: '#6366f1', fontWeight: 600 }}>2. PROCESSING</span>
              <ArrowRight size={14} />
              <span style={{ fontWeight: 700, color: transaction.status === 'APPROVED' ? '#10b981' : transaction.status === 'DECLINED' ? '#ef4444' : '#f59e0b' }}>
                3. {transaction.status}
              </span>
            </div>
          </div>

          {/* Grid Information */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(2, 1fr)',
              gap: '1rem',
              fontSize: '0.875rem',
            }}
          >
            <div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>MERCHANT</div>
              <div style={{ fontWeight: 600, marginTop: '2px' }}>{transaction.merchantName}</div>
            </div>

            <div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>CARDHOLDER</div>
              <div style={{ fontWeight: 600, marginTop: '2px' }}>{transaction.cardholderName || 'N/A'}</div>
            </div>

            <div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>CARD NUMBER</div>
              <div style={{ fontFamily: 'var(--font-mono)', marginTop: '2px' }}>{transaction.maskedCardSuffix || 'N/A'}</div>
            </div>

            <div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>PROCESSING DURATION</div>
              <div style={{ fontFamily: 'var(--font-mono)', marginTop: '2px' }}>
                {transaction.processingDurationMs ? `${transaction.processingDurationMs} ms` : 'N/A'}
              </div>
            </div>

            {transaction.reasonCode && (
              <div>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>REASON CODE</div>
                <div style={{ color: '#f87171', fontWeight: 600, marginTop: '2px' }}>{transaction.reasonCode}</div>
              </div>
            )}

            {transaction.statusMessage && (
              <div style={{ gridColumn: 'span 2' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>STATUS MESSAGE</div>
                <div style={{ color: 'var(--text-secondary)', marginTop: '2px' }}>{transaction.statusMessage}</div>
              </div>
            )}

            <div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>CREATED AT</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '2px' }}>
                {transaction.createdAt ? new Date(transaction.createdAt).toLocaleString() : 'N/A'}
              </div>
            </div>

            <div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>PROCESSED AT</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '2px' }}>
                {transaction.processedAt ? new Date(transaction.processedAt).toLocaleString() : 'N/A'}
              </div>
            </div>

            {transaction.description && (
              <div style={{ gridColumn: 'span 2' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>DESCRIPTION / NOTE</div>
                <div style={{ color: 'var(--text-secondary)', marginTop: '2px' }}>{transaction.description}</div>
              </div>
            )}
          </div>
        </div>

        {/* Modal Footer */}
        <div
          style={{
            padding: '1rem 1.5rem',
            borderTop: '1px solid var(--border-subtle)',
            display: 'flex',
            justifyContent: 'flex-end',
          }}
        >
          <button onClick={onClose} className="btn btn-secondary">
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
