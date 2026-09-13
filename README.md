# YouTube Playlist Video Extractor

A modern, full-stack web application that extracts all video URLs from any YouTube playlist, automatically traversing pagination to retrieve every single video, and providing both rich-card and plain-text export options.

---

## Features

* **Complete Playlist Pagination**: Seamlessly fetches every video beyond the initial 50-video limit using YouTube Data API v3 `nextPageToken` iteration. Supports playlists containing hundreds of videos.
* **Flexible URL Support**:
  * Standard: `https://www.youtube.com/playlist?list=PLAYLIST_ID`
  * Short domain: `https://youtube.com/playlist?list=PLAYLIST_ID`
  * Watch URL with playlist: `https://www.youtube.com/watch?v=VIDEO_ID&list=PLAYLIST_ID`
  * Mobile and YouTube Music URLs: `m.youtube.com`, `music.youtube.com`
* **Dual Display Output**:
  * **Plain-Text URLs Area**: Displays pure video URLs on separate lines for quick copying and selection.
  * **Formatted Video Cards**: Displays video index, video title, and direct links with external opening support.
* **One-Click Clipboard Copying**: Copies all URLs to clipboard with instant feedback notification (`125 links copied`).
* **TXT File Export**: Direct `.txt` download (`youtube-playlist-links.txt`) with one URL per line.
* **Instant Clear**: Resets input, results, messages, and error states.
* **Robust Error Handling**: Distinct, user-friendly error messages for invalid URLs, private/unavailable playlists, playlists not found, empty playlists, missing API credentials, and YouTube API issues.
* **Security First**: YouTube Data API key stays exclusively on the backend and is never exposed to the frontend browser or committed to version control.
* **Interactive API Documentation**: Embedded Swagger UI / OpenAPI 3 documentation.

---

## Architecture

```text
React UI (TypeScript + Vite)
       |
       | REST (POST /api/playlists/extract)
       v
Spring Boot 3 REST API (Java 21)
       |
       v
Playlist Service (Interface)
       |
       v
YouTube Data API Client (RestClient)
       |
       v
Google YouTube Data API v3
```

---

## Technology Stack

### Backend
* **Language**: Java 21
* **Framework**: Spring Boot 3.4.x
* **Build Tool**: Maven 3.9+
* **HTTP Client**: Spring 6 `RestClient`
* **API Documentation**: SpringDoc OpenAPI 2.8+ (Swagger UI)
* **Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc

### Frontend
* **Library**: React 19
* **Language**: TypeScript 5.7+
* **Build Tool & Dev Server**: Vite 6
* **Icons**: Lucide React & custom SVG
* **Styling**: Vanilla CSS with design tokens, glassmorphism, and responsive breakpoints

---

## Prerequisites

* **Java**: JDK 21 or newer (compatible with Java 21 through 26)
* **Maven**: Apache Maven 3.9 or higher
* **Node.js**: Node.js 18 or higher (tested with Node v24)
* **npm**: npm 9 or higher
* **YouTube Data API Key**: A valid Google Cloud YouTube Data API v3 key

---

## YouTube API Key Setup

Obtain a YouTube Data API v3 key from the [Google Cloud Console](https://console.cloud.google.com/apis/credentials).

Configure it via the `YOUTUBE_API_KEY` environment variable:

### Windows (PowerShell)

```powershell
$env:YOUTUBE_API_KEY="your-actual-youtube-api-key"
```

### Windows (CMD)

```cmd
set YOUTUBE_API_KEY=your-actual-youtube-api-key
```

### Linux / macOS (Bash / Zsh)

```bash
export YOUTUBE_API_KEY="your-actual-youtube-api-key"
```

> **Note**: You can also set it directly in `backend/src/main/resources/application.properties`:
> ```properties
> youtube.api.key=your-actual-youtube-api-key
> ```

---

## Run Backend

From the root project directory:

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

The Spring Boot backend will start on `http://localhost:8080`.

---

## Run Frontend

Open a new terminal window:

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server will start on `http://localhost:5173`.

---

## API Example

### Endpoint

```http
POST /api/playlists/extract
Content-Type: application/json
```

### Sample Request

```json
{
  "playlistUrl": "https://youtube.com/playlist?list=PLI7xEYXD8JT0"
}
```

### Sample Response (HTTP 200 OK)

```json
{
  "playlistId": "PLI7xEYXD8JT0",
  "totalVideos": 3,
  "videos": [
    {
      "position": 1,
      "videoId": "abc123xyz",
      "title": "Introduction to Spring Boot 3",
      "url": "https://www.youtube.com/watch?v=abc123xyz"
    },
    {
      "position": 2,
      "videoId": "def456uvw",
      "title": "Spring Boot REST APIs with Java 21",
      "url": "https://www.youtube.com/watch?v=def456uvw"
    },
    {
      "position": 3,
      "videoId": "ghi789rst",
      "title": "Full-Stack React and Spring Boot Integration",
      "url": "https://www.youtube.com/watch?v=ghi789rst"
    }
  ]
}
```

### Sample Error Responses

* **Invalid URL (HTTP 400)**:
  ```json
  {
    "timestamp": "2026-09-12T21:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Please enter a valid YouTube playlist URL."
  }
  ```

* **Playlist Not Found (HTTP 404)**:
  ```json
  {
    "timestamp": "2026-09-12T21:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Playlist could not be found."
  }
  ```

* **Private Playlist (HTTP 403)**:
  ```json
  {
    "timestamp": "2026-09-12T21:30:00",
    "status": 403,
    "error": "Forbidden",
    "message": "This playlist is private or unavailable."
  }
  ```

* **API Key Missing (HTTP 500)**:
  ```json
  {
    "timestamp": "2026-09-12T21:30:00",
    "status": 500,
    "error": "Server Configuration Error",
    "message": "YouTube API key is not configured on the server. Please set the YOUTUBE_API_KEY environment variable."
  }
  ```

---

## Swagger / OpenAPI Documentation

Once the backend is running, open the interactive Swagger UI in your browser:

* **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Specification (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Testing

Run all backend unit and integration tests:

```bash
cd backend
mvn clean test
```

### Test Coverage Highlights
* `YouTubeUrlParserTest`: Validates standard playlist URLs, non-www URLs, watch URLs with list parameter, query parameter ordering, mobile URLs, music URLs, relative queries, and invalid URL formats.
* `YouTubePlaylistServiceTest`: Mocks YouTube API responses across 3 consecutive pages (125 videos) to verify `nextPageToken` iteration, 1-indexed numbering, URL generation, and termination when `nextPageToken == null`. Also tests empty playlist exception handling.
* `PlaylistControllerTest`: Verifies HTTP status codes, JSON payload responses, input validation, and `GlobalExceptionHandler` mapping using MockMvc.

---

## Project Structure

```text
youtube-playlist-extractor/
├── README.md
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/youtubeextractor/
│       │   │   ├── YouTubeExtractorApplication.java
│       │   │   ├── config/
│       │   │   │   ├── CorsConfig.java
│       │   │   │   ├── OpenApiConfig.java
│       │   │   │   └── YouTubeConfig.java
│       │   │   ├── controller/
│       │   │   │   └── PlaylistController.java
│       │   │   ├── service/
│       │   │   │   ├── PlaylistService.java
│       │   │   │   └── YouTubePlaylistService.java
│       │   │   ├── client/
│       │   │   │   └── YouTubeApiClient.java
│       │   │   ├── dto/
│       │   │   │   ├── PlaylistExtractRequest.java
│       │   │   │   ├── PlaylistExtractResponse.java
│       │   │   │   ├── VideoDto.java
│       │   │   │   └── youtube/
│       │   │   │       ├── PageInfo.java
│       │   │   │       ├── ResourceId.java
│       │   │   │       ├── Snippet.java
│       │   │   │       ├── YouTubePlaylistItem.java
│       │   │   │       └── YouTubePlaylistItemListResponse.java
│       │   │   ├── exception/
│       │   │   │   ├── EmptyPlaylistException.java
│       │   │   │   ├── ErrorResponse.java
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   ├── InvalidPlaylistException.java
│       │   │   │   ├── PlaylistNotFoundException.java
│       │   │   │   ├── PlaylistPrivateException.java
│       │   │   │   └── YouTubeApiException.java
│       │   │   └── util/
│       │   │       └── YouTubeUrlParser.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/example/youtubeextractor/
│               ├── controller/
│               │   └── PlaylistControllerTest.java
│               ├── service/
│               │   └── YouTubePlaylistServiceTest.java
│               └── util/
│                   └── YouTubeUrlParserTest.java
└── frontend/
    ├── package.json
    ├── tsconfig.json
    ├── tsconfig.app.json
    ├── tsconfig.node.json
    ├── vite.config.ts
    ├── index.html
    └── src/
        ├── App.css
        ├── App.tsx
        ├── index.css
        ├── main.tsx
        ├── vite-env.d.ts
        ├── components/
        │   ├── ActionButtons.tsx
        │   ├── ErrorMessage.tsx
        │   ├── LoadingIndicator.tsx
        │   ├── PlaylistInput.tsx
        │   └── VideoList.tsx
        ├── services/
        │   └── playlistApi.ts
        └── types/
            └── playlist.ts
```
