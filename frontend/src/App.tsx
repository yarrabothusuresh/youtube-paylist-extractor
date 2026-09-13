import { useState } from 'react';
import { PlaylistInput } from './components/PlaylistInput';
import { VideoList } from './components/VideoList';
import { ActionButtons } from './components/ActionButtons';
import { LoadingIndicator } from './components/LoadingIndicator';
import { ErrorMessage } from './components/ErrorMessage';
import { extractPlaylistVideos } from './services/playlistApi';
import { PlaylistExtractResponse } from './types/playlist';
import { Layers } from 'lucide-react';
import './App.css';

const YouTubeBrandIcon = () => (
  <svg
    className="icon-youtube"
    viewBox="0 0 24 24"
    fill="currentColor"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z" />
  </svg>
);

export function App() {
  const [url, setUrl] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [result, setResult] = useState<PlaylistExtractResponse | null>(null);

  const validateUrl = (rawUrl: string): boolean => {
    if (!rawUrl || !rawUrl.trim()) {
      return false;
    }
    const trimmed = rawUrl.trim();

    // Check for list= parameter
    const hasListParam = /[?&]list=[a-zA-Z0-9_-]+/i.test(trimmed);
    if (!hasListParam) {
      return false;
    }

    // If host exists, must be youtube
    try {
      if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
        const parsed = new URL(trimmed);
        const host = parsed.hostname.toLowerCase();
        return host === 'youtube.com' || host.endsWith('.youtube.com') || host === 'youtu.be';
      }
    } catch {
      return false;
    }

    return true;
  };

  const handleExtract = async () => {
    setErrorMessage(null);

    if (!validateUrl(url)) {
      setErrorMessage('Please enter a valid YouTube playlist URL.');
      return;
    }

    setIsLoading(true);
    try {
      const data = await extractPlaylistVideos(url);
      setResult(data);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unable to retrieve playlist videos. Please try again.';
      setErrorMessage(message);
      setResult(null);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClear = () => {
    setUrl('');
    setResult(null);
    setErrorMessage(null);
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-badge">
          <YouTubeBrandIcon />
          <span>YouTube Tool</span>
        </div>
        <h1 className="app-title">YouTube Playlist Video Extractor</h1>
        <p className="app-subtitle">
          Paste a YouTube playlist and extract all video links.
        </p>
      </header>

      <main className="app-main">
        {/* URL Input Section */}
        <section className="section-input" aria-label="Playlist URL input">
          <PlaylistInput
            url={url}
            setUrl={setUrl}
            onExtract={handleExtract}
            isLoading={isLoading}
          />
        </section>

        {/* Error message */}
        {errorMessage && (
          <section className="section-feedback" aria-label="Error feedback">
            <ErrorMessage
              message={errorMessage}
              onDismiss={() => setErrorMessage(null)}
            />
          </section>
        )}

        {/* Loading Indicator */}
        {isLoading && (
          <section className="section-feedback" aria-label="Loading feedback">
            <LoadingIndicator message="Extracting playlist videos..." />
          </section>
        )}

        {/* Results & Actions */}
        {result && result.videos.length > 0 && !isLoading && (
          <section className="section-results" aria-label="Extraction results">
            <div className="results-card">
              <ActionButtons
                videos={result.videos}
                onClear={handleClear}
              />
              <VideoList
                videos={result.videos}
                playlistId={result.playlistId}
              />
            </div>
          </section>
        )}

        {/* Informational feature box if no result yet */}
        {!result && !isLoading && (
          <section className="features-card">
            <div className="feature-item">
              <div className="feature-icon-box">
                <Layers className="icon-small text-accent" />
              </div>
              <div className="feature-text">
                <h3>Full Pagination Support</h3>
                <p>Seamlessly extracts all videos from large playlists containing hundreds of videos.</p>
              </div>
            </div>
          </section>
        )}
      </main>

      <footer className="app-footer">
        <p>YouTube Playlist Video Extractor • Powered by Spring Boot 3 &amp; React</p>
      </footer>
    </div>
  );
}

export default App;
