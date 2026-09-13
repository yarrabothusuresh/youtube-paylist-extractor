import React from 'react';
import { AlertCircle, X } from 'lucide-react';

interface ErrorMessageProps {
  message: string;
  onDismiss?: () => void;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({ message, onDismiss }) => {
  if (!message) return null;

  return (
    <div className="error-banner" role="alert">
      <div className="error-icon-wrapper">
        <AlertCircle className="error-icon" />
      </div>
      <div className="error-content">
        <span className="error-title">Error</span>
        <p className="error-message-text">{message}</p>
      </div>
      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          className="error-dismiss-btn"
          aria-label="Dismiss error"
        >
          <X className="icon-small" />
        </button>
      )}
    </div>
  );
};
