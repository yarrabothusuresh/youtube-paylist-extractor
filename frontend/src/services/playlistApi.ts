import { PlaylistExtractRequest, PlaylistExtractResponse, ApiError } from '../types/playlist';

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * Sends a request to the backend to extract all video URLs from a YouTube playlist.
 *
 * @param playlistUrl YouTube playlist URL
 * @returns Extracted playlist data including all videos
 */
export async function extractPlaylistVideos(playlistUrl: string): Promise<PlaylistExtractResponse> {
  const payload: PlaylistExtractRequest = { playlistUrl: playlistUrl.trim() };

  let response: Response;
  try {
    response = await fetch(`${API_BASE}/playlists/extract`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });
  } catch (err) {
    console.error('Network error during playlist extraction:', err);
    throw new Error('Unable to connect to the server. Please check your backend connection.');
  }

  if (!response.ok) {
    let errorMessage = 'Unable to retrieve playlist videos. Please try again.';
    try {
      const errorData: ApiError = await response.json();
      if (errorData && errorData.message) {
        errorMessage = errorData.message;
      }
    } catch {
      // Body was not JSON, use HTTP status text fallback
      if (response.status === 404) {
        errorMessage = 'Playlist could not be found.';
      } else if (response.status === 403) {
        errorMessage = 'This playlist is private or unavailable.';
      } else if (response.status === 400) {
        errorMessage = 'Please enter a valid YouTube playlist URL.';
      }
    }
    throw new Error(errorMessage);
  }

  return response.json();
}
