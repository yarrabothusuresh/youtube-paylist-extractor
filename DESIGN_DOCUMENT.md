# System Design Document: YouTube Playlist Video Extractor

## 1. Executive Summary
The **YouTube Playlist Video Extractor** is a production-ready, full-stack web application designed to parse YouTube playlist URLs, systematically traverse paginated YouTube Data API v3 endpoints, and return a clean, consolidated list of video URLs, titles, and positions. It offers an intuitive, high-performance UI allowing users to view, copy, and export extracted links.

---

## 2. High-Level Architecture Overview

The system follows a modern decoupled client-server architecture:

```mermaid
graph TB
    subgraph ClientLayer["Frontend Presentation Layer (React 19 + TypeScript)"]
        UI["User Interface (SPA)"]
        StateMgr["React State & Event Handlers"]
        ClientService["API Client Service (playlistApi.ts)"]
    end

    subgraph ReverseProxy["Reverse Proxy & Gateway"]
        Nginx["Nginx Alpine Gateway (Port 80/8080)"]
    end

    subgraph ServerLayer["Backend Application Layer (Spring Boot 3.4.3 + Java 21)"]
        Controller["PlaylistController (/api/playlists/extract)"]
        Parser["YouTubeUrlParser"]
        Service["YouTubePlaylistService"]
        Client["YouTubeApiClient (RestClient)"]
        Config["YouTubeConfig (Secret Fallback)"]
        ExceptionHandler["GlobalExceptionHandler"]
    end

    subgraph ExternalServices["External Cloud & API Providers"]
        YouTubeAPI["Google YouTube Data API v3"]
        SecretStorage["Local Key File / Cloud Secret Manager"]
    end

    UI --> StateMgr
    StateMgr --> ClientService
    ClientService -->|HTTP POST| Nginx
    Nginx -->|Proxy Pass /api/| Controller
    Controller --> Parser
    Controller --> Service
    Service --> Client
    Client --> Config
    Config -.-> SecretStorage
    Client -->|HTTPS REST| YouTubeAPI
    Controller -.-> ExceptionHandler
```

---

## 3. Component Design & Decomposition

### 3.1 Frontend Component Hierarchy
The UI is decomposed into atomic, focused React components adhering to single-responsibility design:

```mermaid
graph TD
    App["App.tsx (Root Container & State Coordinator)"]
    Header["App Header & YouTube Brand Badge"]
    PlaylistInput["PlaylistInput.tsx<br/>(URL input, validation feedback, submit button)"]
    ErrorMessage["ErrorMessage.tsx<br/>(Dismissible alert banner)"]
    LoadingIndicator["LoadingIndicator.tsx<br/>(Animated spinner & status messages)"]
    ResultsSection["Results Section (Conditional)"]
    ActionButtons["ActionButtons.tsx<br/>(Copy All, Download TXT, Clear)"]
    VideoList["VideoList.tsx<br/>(Table / Card list of extracted videos)"]
    Footer["App Footer"]

    App --> Header
    App --> PlaylistInput
    App --> ErrorMessage
    App --> LoadingIndicator
    App --> ResultsSection
    ResultsSection --> ActionButtons
    ResultsSection --> VideoList
    App --> Footer
```

### 3.2 Backend Component Design
The backend follows Spring Boot’s layered architecture:
1. **Presentation Layer (`PlaylistController`)**: Exposes REST endpoints, validates incoming JSON payloads using Jakarta Bean Validation (`@Valid`).
2. **Domain/Service Layer (`YouTubePlaylistService`)**: Handles the business orchestration of traversing all pages until `nextPageToken` is null.
3. **Integration Layer (`YouTubeApiClient`)**: Interacts with the external YouTube Data API v3 using Spring 6's modern synchronous `RestClient`.
4. **Configuration & Security Layer (`YouTubeConfig`)**: Resolves API keys from environment variables, system properties, or physical key files with dynamic fallback and sanitization.
5. **Cross-Cutting Concerns (`GlobalExceptionHandler`, `CorsConfig`)**: Centralized error mapping and CORS policy enforcement.

---

## 4. State Management & Data Flow

```mermaid
stateDiagram-v2
    [*] --> Idle: Application Loaded
    Idle --> Validating: User inputs URL & Clicks "Extract Videos"
    Validating --> Idle: Regex Validation Fails (Shows Client Error)
    Validating --> Loading: Regex Validation Passes
    Loading --> FetchingPage1: API Call Initiated
    FetchingPage1 --> TraversingPages: nextPageToken != null
    TraversingPages --> TraversingPages: Fetching next 50 items
    TraversingPages --> Success: nextPageToken == null (Extraction Complete)
    Loading --> Error: API Error / 404 / 403 / 500
    Error --> Idle: User Dismisses Error or Edits URL
    Success --> Idle: User Clicks "Clear"
```

---

## 5. Security & Reliability Design

### 5.1 API Key Security
- **No Hardcoded Keys**: API keys are never embedded in frontend code or Git repositories.
- **Backend Key Shielding**: All YouTube Data API calls happen on the backend server; the client browser never sees the API key.
- **Fail-Safe Fallback**: If `YOUTUBE_API_KEY` is not present in container environment variables, `YouTubeConfig` safely attempts reading from the pre-configured local file `C:\dev\Youtube_API.txt`.

### 5.2 Input Sanitization & Validation
- **URL Syntax Filtering**: Client and server validate URLs against YouTube patterns (`youtube.com/playlist?list=...`, `youtu.be/...`).
- **Quota & Rate Limiting**: The backend requests `part=snippet` only (lowest quota consumption of 1 unit per 50 playlist items) instead of expensive content detail queries.

### 5.3 High Availability & Scalability
- **Stateless Backend**: The Spring Boot backend retains no user sessions or server state, making it horizontally scalable across multiple Cloud Run or Kubernetes instances.
- **Nginx Caching**: Static frontend assets (`.js`, `.css`, SVG) are served with `Cache-Control: public, immutable` and 1-year expiration headers.
