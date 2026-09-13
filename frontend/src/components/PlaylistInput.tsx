import React from 'react';
import { Play, Sparkles } from 'lucide-react';

interface PlaylistInputProps {
  url: string;
  setUrl: (url: string) => void;
  onExtract: () => void;
  isLoading: boolean;
}

export const PlaylistInput: React.FC<PlaylistInputProps> = ({
  url,
  setUrl,
  onExtract,
  isLoading,
}) => {
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isLoading) {
      onExtract();
    }
  };

  return (
    <form className="playlist-input-card" onSubmit={handleSubmit}>
      <label htmlFor="playlist-url-input" className="input-label">
        YouTube Playlist URL
      </label>
      <div className="input-row">
        <div className="input-wrapper">
          <input
            id="playlist-url-input"
            type="text"
            placeholder="https://www.youtube.com/playlist?list=PLI7xEYXD8JT0"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            disabled={isLoading}
            autoComplete="off"
            spellCheck="false"
            className="url-text-input"
          />
        </div>
        <button
          id="extract-button"
          type="submit"
          disabled={isLoading || !url.trim()}
          className="extract-btn"
        >
          {isLoading ? (
            <>
              <span className="button-spinner" aria-hidden="true" />
              <span>Extracting...</span>
            </>
          ) : (
            <>
              <Play className="icon-small" fill="currentColor" />
              <span>Extract Videos</span>
            </>
          )}
        </button>
      </div>

      <div className="input-hint">
        <Sparkles className="icon-xs" />
        <span>
          Supported: <code>playlist?list=...</code>, <code>watch?v=...&list=...</code>, and mobile links.
        </span>
      </div>
    </form>
  );
};
