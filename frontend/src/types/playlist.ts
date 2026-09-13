export interface VideoItem {
  position: number;
  videoId: string;
  title: string;
  url: string;
}

export interface PlaylistExtractRequest {
  playlistUrl: string;
}

export interface PlaylistExtractResponse {
  playlistId: string;
  totalVideos: number;
  videos: VideoItem[];
}

export interface ApiError {
  timestamp?: string;
  status?: number;
  error?: string;
  message: string;
}
