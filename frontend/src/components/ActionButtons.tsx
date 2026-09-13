import React, { useState } from 'react';
import { Copy, Check, Download, Trash2 } from 'lucide-react';
import { VideoItem } from '../types/playlist';

interface ActionButtonsProps {
  videos: VideoItem[];
  onClear: () => void;
}

export const ActionButtons: React.FC<ActionButtonsProps> = ({ videos, onClear }) => {
  const [copyFeedback, setCopyFeedback] = useState<string | null>(null);

  const getUrlsText = (): string => {
    return videos.map((v) => v.url).join('\n');
  };

  const handleCopyAll = async () => {
    if (videos.length === 0) return;
    const textToCopy = getUrlsText();

    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(textToCopy);
      } else {
        // Fallback for non-https or older browser environments
        const textArea = document.createElement('textarea');
        textArea.value = textToCopy;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
      }

      setCopyFeedback(`${videos.length} links copied`);
      setTimeout(() => {
        setCopyFeedback(null);
      }, 3000);
    } catch (err) {
      console.error('Failed to copy to clipboard', err);
    }
  };

  const handleDownloadTxt = () => {
    if (videos.length === 0) return;
    const textContent = getUrlsText();
    const blob = new Blob([textContent], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'youtube-playlist-links.txt';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="action-buttons-bar">
      <div className="action-buttons-group">
        <button
          id="copy-all-btn"
          type="button"
          onClick={handleCopyAll}
          className={`action-btn primary ${copyFeedback ? 'success' : ''}`}
          disabled={videos.length === 0}
          title="Copy all video links to clipboard"
        >
          {copyFeedback ? (
            <>
              <Check className="icon-small" />
              <span>{copyFeedback}</span>
            </>
          ) : (
            <>
              <Copy className="icon-small" />
              <span>Copy All Links</span>
            </>
          )}
        </button>

        <button
          id="download-txt-btn"
          type="button"
          onClick={handleDownloadTxt}
          className="action-btn secondary"
          disabled={videos.length === 0}
          title="Download links as a plain text file"
        >
          <Download className="icon-small" />
          <span>Download TXT</span>
        </button>

        <button
          id="clear-btn"
          type="button"
          onClick={onClear}
          className="action-btn danger"
          title="Clear inputs, results, and messages"
        >
          <Trash2 className="icon-small" />
          <span>Clear</span>
        </button>
      </div>

      {copyFeedback && (
        <div className="copy-feedback-toast" role="status">
          <Check className="icon-xs" />
          <span>{copyFeedback}</span>
        </div>
      )}
    </div>
  );
};
