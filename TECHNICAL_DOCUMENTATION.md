# Technical Documentation: YouTube Playlist Video Extractor

## 1. Technology Stack & Specifications

### 1.1 Backend Stack
- **Framework**: Spring Boot 3.4.3
- **Language / Runtime**: Java 21 LTS (Amazon Corretto / Eclipse Temurin)
- **HTTP Client**: Spring Framework 6.2 `RestClient`
- **Validation**: Jakarta Validation API (`jakarta.validation-api`) & Hibernate Validator
- **API Documentation**: SpringDoc OpenAPI UI 2.8.5 (OpenAPI v3.1)
- **Build System**: Apache Maven 3.9+
- **Unit Testing**: JUnit 5, Mockito, Spring Boot Test

### 1.2 Frontend Stack
- **Library / Core**: React 19.0.0
- **Language**: TypeScript 5.7.2
- **Build Tool / Bundler**: Vite 6.2.0 (`@vitejs/plugin-react`)
- **Icons**: Lucide React 1.16.0
- **Styling**: Vanilla CSS3 with Modern CSS Variables, Flexbox, CSS Grid, Glassmorphism
- **HTTP Client**: Native browser `fetch` API

### 1.3 Infrastructure & Deployment
- **Container Engine**: Docker with multi-stage builds
- **Web Server / Reverse Proxy**: Nginx 1.31 (Alpine)
- **Primary Cloud**: Google Cloud Run (Serverless Containers)
- **Alternative Cloud**: Oracle Cloud Infrastructure (OCI Compute + Docker Compose)

---

## 2. API Specification & Contracts

### 2.1 Extract Playlist Videos
- **Endpoint**: `POST /api/playlists/extract`
- **Content-Type**: `application/json`
- **Description**: Extracts all video URLs, titles, and IDs from the given YouTube playlist.

#### Request Body
```json
{
  "playlistUrl": "https://www.youtube.com/playlist?list=PLjEaoINr3zgEPv5y--4MKpciLaoQYZB1Z"
}
```

#### Response Body (`200 OK`)
```json
{
  "playlistId": "PLjEaoINr3zgEPv5y--4MKpciLaoQYZB1Z",
  "totalVideos": 14,
  "videos": [
    {
      "position": 1,
      "videoId": "B0J27sf9N1Y",
      "title": "Beginner Blender 4.0 Tutorial (2023)",
      "url": "https://www.youtube.com/watch?v=B0J27sf9N1Y"
    },
    {
      "position": 2,
      "videoId": "tBpnKTAc5Eo",
      "title": "Beginner Blender 4.0 Tutorial - Part 2: Basic Modelling",
      "url": "https://www.youtube.com/watch?v=tBpnKTAc5Eo"
    }
  ]
}
```

#### Error Response Contract (`400`, `404`, `403`, `500`)
```json
{
  "timestamp": "2026-10-09T00:14:07",
  "status": 404,
  "error": "Not Found",
  "message": "Playlist could not be found."
}
```

#### Status Code Mapping

| Status Code | Meaning | Cause |
| :--- | :--- | :--- |
| `200 OK` | Extraction Succeeded | Playlist parsed and all videos retrieved |
| `400 Bad Request` | Invalid URL / Payload | Malformed URL, missing `list=` param, blank body |
| `404 Not Found` | Playlist Not Found | Playlist ID does not exist or has been deleted |
| `403 Forbidden` | Private / Unavailable | Playlist is private, region-restricted, or quota reached |
| `500 Internal Error` | Server Error | Unconfigured server API key or network exception |

---

## 3. Key Algorithmic Implementations

### 3.1 YouTube URL Parsing Algorithm (`YouTubeUrlParser.java`)
The parser supports standard URLs, mobile URLs, shortlinks, and raw query strings:
1. Validates host (`youtube.com`, `m.youtube.com`, `youtu.be`).
2. Extracts candidate playlist ID via regular expression:
   ```java
   private static final Pattern LIST_PARAM_REGEX = Pattern.compile("[?&]list=([a-zA-Z0-9_-]+)");
   ```
3. Validates playlist ID against YouTube character constraints:
   ```java
   private static final Pattern PLAYLIST_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{2,100}$");
   ```
4. Fallback: URI query parameter parsing with UTF-8 decoding.

### 3.2 Pagination Traversal Algorithm (`YouTubePlaylistService.java`)
Traverses YouTube Data API pages recursively/iteratively:
```java
String pageToken = null;
int positionCounter = 1;
do {
    YouTubePlaylistItemListResponse responsePage = 
        youTubeApiClient.fetchPlaylistItemsPage(playlistId, pageToken);
    
    for (YouTubePlaylistItem item : responsePage.getItems()) {
        videos.add(new VideoDto(
            positionCounter++,
            item.getSnippet().getResourceId().getVideoId(),
            item.getSnippet().getTitle(),
            "https://www.youtube.com/watch?v=" + item.getSnippet().getResourceId().getVideoId()
        ));
    }
    pageToken = responsePage.getNextPageToken();
} while (pageToken != null && !pageToken.trim().isEmpty());
```

---

## 4. Configuration Reference

| Property Name | Environment Variable | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `server.port` | `PORT` | `8089` | HTTP server listening port |
| `youtube.api.key` | `YOUTUBE_API_KEY` | *(None)* | Google YouTube Data API v3 key |
| `youtube.api.key-file` | `YOUTUBE_API_KEY_FILE` | `C:/dev/Youtube_API.txt` | Fallback file location for API key |
| `youtube.api.base-url` | - | `https://www.googleapis.com/youtube/v3` | YouTube API base endpoint |
| `cors.allowed-origins` | `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed CORS origins |

---

## 5. Docker Containerization Architecture

### 5.1 Backend Multi-Stage Build
- **Builder Stage**: `maven:3.9.9-eclipse-temurin-21` -> downloads dependencies, compiles and packages executable JAR with `-DskipTests`.
- **Runtime Stage**: `eclipse-temurin:21-jre-alpine` -> copies only the fat JAR. Runs as non-root user `appuser`.
- **Image Size**: Reduced from ~800 MB (JDK + Maven) to ~140 MB (Alpine JRE).

### 5.2 Frontend Multi-Stage Build
- **Builder Stage**: `node:20-slim` -> runs `npm install` and `npm run build` using Vite.
- **Runtime Stage**: `nginx:alpine` -> static assets served via Nginx. Reverse-proxy configuration for `/api/` routing.
- **Image Size**: Reduced from ~450 MB (Node modules) to ~25 MB (Alpine Nginx).

---

## 6. Build & Test Commands

### Backend
```powershell
cd backend

# Run all unit and integration tests
mvn clean test

# Package production JAR
mvn clean package -DskipTests

# Run locally
mvn spring-boot:run
```

### Frontend
```powershell
cd frontend

# Install dependencies
npm install

# Start local dev server (port 5173)
npm run dev

# Run TypeScript type check and production build
npm run build
```
