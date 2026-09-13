import React, { useState } from 'react';
import { VideoItem } from '../types/playlist';
import { List, AlignLeft, ExternalLink, Video } from 'lucide-react';

interface VideoListProps {
  videos: VideoItem[];
  playlistId: string;
}

export const VideoList: React.FC<VideoListProps> = ({ videos, playlistId }) => {
  const [activeTab, setActiveTab] = useState<'formatted' | 'plain'>('plain');

  const plainTextUrls = videos.map((v) => v.url).join('\n');

  return (
    <div className="video-list-container">
      <div className="video-list-header">
        <div className="title-group">
          <h2 className="section-title">Playlist Videos</h2>
          <span className="playlist-id-badge" title={`YouTube Playlist ID: ${playlistId}`}>
            ID: {playlistId}
          </span>
        </div>
        <div className="meta-group">
          <span className="total-badge" id="total-count-badge">
            Total Videos: {videos.length}
          </span>
          <div className="view-mode-tabs" role="tablist">
            <button
              type="button"
              role="tab"
              aria-selected={activeTab === 'plain'}
              className={`tab-btn ${activeTab === 'plain' ? 'active' : ''}`}
              onClick={() => setActiveTab('plain')}
            >
              <AlignLeft className="icon-xs" />
              <span>Plain Text URLs</span>
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={activeTab === 'formatted'}
              className={`tab-btn ${activeTab === 'formatted' ? 'active' : ''}`}
              onClick={() => setActiveTab('formatted')}
            >
              <List className="icon-xs" />
              <span>Formatted List</span>
            </button>
          </div>
        </div>
      </div>

      {activeTab === 'plain' ? (
        <div className="plain-text-area-wrapper">
          <div className="plain-text-header">
            <span className="plain-text-hint">Plain-text output (one URL per line):</span>
            <button
              type="button"
              className="select-all-btn"
              onClick={(e) => {
                const textarea = (e.currentTarget.parentElement?.nextElementSibling as HTMLTextAreaElement);
                if (textarea) {
                  textarea.focus();
                  textarea.select();
                }
              }}
            >
              Select All Text
            </button>
          </div>
          <textarea
            id="plain-urls-textarea"
            className="plain-urls-textarea"
            readOnly
            value={plainTextUrls}
            rows={Math.min(Math.max(videos.length, 6), 20)}
            aria-label="Plain video URLs, one per line"
          />
        </div>
      ) : (
        <div className="formatted-list-wrapper">
          <ol className="video-ordered-list">
            {videos.map((video) => (
              <li key={`${video.videoId}-${video.position}`} className="video-item-card">
                <div className="video-item-index">{video.position}.</div>
                <div className="video-item-details">
                  <div className="video-item-title-row">
                    <Video className="icon-xs text-muted" />
                    <span className="video-title" title={video.title}>
                      {video.title}
                    </span>
                  </div>
                  <div className="video-link-row">
                    <a
                      href={video.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="video-url-link"
                    >
                      <span>{video.url}</span>
                      <ExternalLink className="icon-xs link-ext" />
                    </a>
                  </div>
                </div>
              </li>
            ))}
          </ol>
        </div>
      )}
    </div>
  );
};
