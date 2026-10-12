import React from 'react';
import { CheckCircle2, XCircle, Clock, Loader2 } from 'lucide-react';

export default function StatusBadge({ status }) {
  const norm = (status || '').toUpperCase();

  if (norm === 'APPROVED') {
    return (
      <span className="badge badge-approved">
        <CheckCircle2 size={13} />
        Approved
      </span>
    );
  }

  if (norm === 'DECLINED') {
    return (
      <span className="badge badge-declined">
        <XCircle size={13} />
        Declined
      </span>
    );
  }

  if (norm === 'TIMEOUT') {
    return (
      <span className="badge badge-timeout">
        <Clock size={13} />
        Timeout
      </span>
    );
  }

  return (
    <span className="badge badge-processing">
      <Loader2 size={13} className="spin" />
      {norm || 'Processing'}
    </span>
  );
}
