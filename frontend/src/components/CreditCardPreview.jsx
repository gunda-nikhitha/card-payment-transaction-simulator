import React from 'react';
import { Wifi } from 'lucide-react';

export default function CreditCardPreview({
  cardNumber = '**** **** **** 1234',
  cardholderName = 'ALEX MORGAN',
  expiry = '12/28',
  status = null,
}) {
  // Format card number to groups of 4
  const cleanNum = (cardNumber || '**** **** **** 1234').replace(/\s+/g, '');
  const formattedNumber = cleanNum.padEnd(16, '*').replace(/(.{4})/g, '$1 ').trim();

  // Pick border/glow accent based on status if simulated
  let borderColor = 'rgba(255, 255, 255, 0.15)';
  if (status === 'APPROVED') borderColor = 'rgba(16, 185, 129, 0.6)';
  if (status === 'DECLINED') borderColor = 'rgba(239, 68, 68, 0.6)';
  if (status === 'TIMEOUT') borderColor = 'rgba(245, 158, 11, 0.6)';

  return (
    <div
      className="virtual-card"
      style={{
        borderColor: borderColor,
      }}
    >
      {/* Top Header: Network + Contactless Icon */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', zIndex: 1 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div className="card-chip" />
          <Wifi size={20} style={{ color: 'rgba(255, 255, 255, 0.6)', transform: 'rotate(90deg)' }} />
        </div>
        <div
          style={{
            fontStyle: 'italic',
            fontWeight: 800,
            fontSize: '1.25rem',
            color: '#ffffff',
            letterSpacing: '0.05em',
            textShadow: '0 2px 4px rgba(0,0,0,0.5)',
          }}
        >
          GLOBAL PAY
        </div>
      </div>

      {/* Card Number */}
      <div className="card-number" style={{ zIndex: 1 }}>
        {formattedNumber}
      </div>

      {/* Bottom Footer: Cardholder & Expiry */}
      <div className="card-meta" style={{ zIndex: 1 }}>
        <div>
          <div style={{ fontSize: '0.65rem', color: 'rgba(255, 255, 255, 0.5)', letterSpacing: '0.05em', marginBottom: '2px' }}>
            CARDHOLDER NAME
          </div>
          <div className="card-holder-name">
            {cardholderName || 'CARDHOLDER'}
          </div>
        </div>

        <div>
          <div style={{ fontSize: '0.65rem', color: 'rgba(255, 255, 255, 0.5)', letterSpacing: '0.05em', marginBottom: '2px', textAlign: 'right' }}>
            EXPIRES
          </div>
          <div className="card-expiry">
            {expiry || '12/28'}
          </div>
        </div>
      </div>
    </div>
  );
}
