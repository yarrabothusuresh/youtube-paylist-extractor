import React from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingIndicatorProps {
  message?: string;
}

export const LoadingIndicator: React.FC<LoadingIndicatorProps> = ({
  message = 'Extracting playlist videos...',
}) => {
  return (
    <div className="loading-container" role="status" aria-live="polite">
      <div className="loading-spinner-wrapper">
        <Loader2 className="loading-spinner-icon" />
      </div>
      <div className="loading-text-group">
        <p className="loading-main-text">⏳ {message}</p>
        <p className="loading-sub-text">Traversing playlist pages and collecting all video links...</p>
      </div>
    </div>
  );
};
