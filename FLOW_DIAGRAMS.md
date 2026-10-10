# System Flow Diagrams: YouTube Playlist Video Extractor

This document provides visual flowcharts, sequence diagrams, and decision trees illustrating how data moves through the application.

---

## 1. End-to-End Extraction Sequence Diagram

This sequence diagram depicts the exact interactions when a user submits a YouTube playlist URL:

```mermaid
sequenceDiagram
    autonumber
    actor User as User Browser
    participant UI as React UI (App.tsx)
    participant Nginx as Nginx Reverse Proxy
    participant Controller as PlaylistController
    participant Parser as YouTubeUrlParser
    participant Service as YouTubePlaylistService
    participant Client as YouTubeApiClient
    participant Config as YouTubeConfig
    participant YT as YouTube Data API v3

    User->>UI: Pastes playlist URL & clicks "Extract Videos"
    UI->>UI: Validates URL client-side (Regex)
    alt URL is invalid
        UI-->>User: Displays error banner "Please enter a valid YouTube playlist URL."
    else URL is valid
        UI->>Nginx: POST /api/playlists/extract { playlistUrl }
        Nginx->>Controller: Forward request to Backend
        Controller->>Parser: extractPlaylistId(rawUrl)
        Parser-->>Controller: Returns playlistId (e.g. "PLjEaoINr3...")
        Controller->>Service: extractVideosFromPlaylist(playlistId)
        
        loop Page Traversal (Until nextPageToken is null)
            Service->>Client: fetchPlaylistItemsPage(playlistId, pageToken)
            Client->>Config: getApiKey()
            Config-->>Client: Returns API Key (from Env or C:\dev\Youtube_API.txt)
            Client->>YT: GET /playlistItems?part=snippet&maxResults=50&playlistId=...&key=...
            YT-->>Client: 200 OK (Items array + nextPageToken)
            Client-->>Service: Returns parsed YouTubePlaylistItemListResponse
            Service->>Service: Map items to VideoDto and append to result list
        end
        
        Service-->>Controller: Returns PlaylistExtractResponse (Total count + all videos)
        Controller-->>Nginx: 200 OK (JSON Response)
        Nginx-->>UI: 200 OK (JSON Response)
        UI-->>User: Renders video list table, enables Copy & Download TXT buttons
    end
```

---

## 2. YouTube URL Parsing & Validation Flow

This flowchart illustrates the logic inside `YouTubeUrlParser.java` when validating user input:

```mermaid
flowchart TD
    Start([Receive raw URL input]) --> CheckNull{Is string null or blank?}
    CheckNull -- Yes --> ThrowInvalid[Throw InvalidPlaylistException]
    CheckNull -- No --> ParseHost[Parse URI Host]

    ParseHost --> HasHost{Host present?}
    HasHost -- Yes --> ValidateHost{Is host youtube.com,<br/>youtu.be, or m.youtube.com?}
    ValidateHost -- No --> ThrowInvalid
    ValidateHost -- Yes --> RegexMatch[Run Regex: [?&]list=([a-zA-Z0-9_-]+)]

    HasHost -- No --> CheckPrefix{Does it start with<br/>playlist? or watch? or contain list=?}
    CheckPrefix -- No --> ThrowInvalid
    CheckPrefix -- Yes --> RegexMatch

    RegexMatch --> MatchFound{list= match found?}
    MatchFound -- Yes --> CheckIdFormat{Matches ID pattern<br/>[a-zA-Z0-9_-]{2,100}?}
    CheckIdFormat -- Yes --> ReturnId([Return extracted Playlist ID])
    CheckIdFormat -- No --> QueryFallback[Fallback to URI Query String Split]

    MatchFound -- No --> QueryFallback
    QueryFallback --> QueryParamLoop{Iterate key=value pairs:<br/>Is key 'list' and value valid?}
    QueryParamLoop -- Yes --> ReturnId
    QueryParamLoop -- No --> ThrowInvalid
```

---

## 3. Backend Pagination Traversal Flow

How `YouTubePlaylistService` gathers all videos from large playlists containing hundreds of entries:

```mermaid
flowchart TD
    Start([Start extraction for playlistId]) --> Init["Initialize empty List&lt;VideoDto&gt;<br/>Set pageToken = null<br/>Set positionCounter = 1"]
    Init --> CallAPI["Call YouTubeApiClient.fetchPlaylistItemsPage(playlistId, pageToken)"]
    
    CallAPI --> CheckError{Response Status}
    CheckError -- 404 --> ThrowNotFound[Throw PlaylistNotFoundException]
    CheckError -- 403 --> ThrowPrivate[Throw PlaylistPrivateException]
    CheckError -- 200 OK --> ProcessItems["Iterate through response.items"]

    ProcessItems --> ForEachItem["For each video item:<br/>Extract title, videoId, and construct URL<br/>Add VideoDto(position++, id, title, url)"]
    ForEachItem --> CheckNextPage{"Is nextPageToken present and not empty?"}

    CheckNextPage -- Yes --> SetToken["Set pageToken = response.getNextPageToken()"]
    SetToken --> CallAPI

    CheckNextPage -- No --> CheckEmpty{"Is total videos == 0?"}
    CheckEmpty -- Yes --> ThrowEmpty[Throw EmptyPlaylistException]
    CheckEmpty -- No --> BuildResponse["Construct PlaylistExtractResponse(playlistId, totalCount, videos)"]
    BuildResponse --> Finish([Return extracted response])
```

---

## 4. Error Handling & Status Code Decision Tree

How exceptions are mapped to standard HTTP status codes in `GlobalExceptionHandler.java`:

```mermaid
flowchart TD
    ExceptionOccurred([Exception thrown during request]) --> TypeCheck{Exception Type}

    TypeCheck -- InvalidPlaylistException --> R400["HTTP 400 Bad Request<br/>'Please enter a valid YouTube playlist URL.'"]
    TypeCheck -- MethodArgumentNotValidException --> R400
    TypeCheck -- HttpMessageNotReadableException --> R400

    TypeCheck -- PlaylistNotFoundException --> R404["HTTP 404 Not Found<br/>'Playlist could not be found.'"]
    TypeCheck -- EmptyPlaylistException --> R404

    TypeCheck -- PlaylistPrivateException --> R403["HTTP 403 Forbidden<br/>'This playlist is private or unavailable.'"]

    TypeCheck -- IllegalStateException --> R500["HTTP 500 Server Configuration Error<br/>'YouTube API key is not configured...'"]
    TypeCheck -- YouTubeApiException --> R502["HTTP 502 Bad Gateway<br/>'YouTube API error: ...'"]
    TypeCheck -- Unhandled Exception --> R500Generic["HTTP 500 Internal Server Error<br/>'Unable to retrieve playlist videos. Please try again.'"]

    R400 --> JSONOutput([Format ErrorResponse JSON])
    R404 --> JSONOutput
    R403 --> JSONOutput
    R500 --> JSONOutput
    R502 --> JSONOutput
    R500Generic --> JSONOutput
```

---

## 5. Dual-Cloud Deployment & Routing Architecture

Comparison of how user traffic is routed in Google Cloud Run vs. Oracle Cloud Infrastructure:

```mermaid
graph TD
    subgraph ClientBrowsers["User Traffic"]
        UserGCP["Client Request (Google Cloud)"]
        UserOCI["Client Request (Oracle Cloud)"]
    end

    subgraph GCP["Google Cloud Run (Serverless)"]
        GCP_FE["Cloud Run Frontend Service<br/>(Nginx Container)"]
        GCP_BE["Cloud Run Backend Service<br/>(Spring Boot Container)"]
        GCP_FE -->|Internal Reverse Proxy /api/| GCP_BE
    end

    subgraph OCI["Oracle Cloud Infrastructure (Compute VM)"]
        OCI_VCN["VCN Security List Ingress (Port 80)"]
        OCI_HostFW["Ubuntu iptables / OS Firewall"]
        OCI_FE["Frontend Container (Nginx Port 80)"]
        OCI_BE["Backend Container (Spring Boot Port 8089)"]

        OCI_VCN --> OCI_HostFW
        OCI_HostFW --> OCI_FE
        OCI_FE -->|Docker Network Bridge /api/| OCI_BE
    end

    subgraph GoogleAPI["External Services"]
        YT["YouTube Data API v3"]
    end

    UserGCP --> GCP_FE
    UserOCI --> OCI_VCN

    GCP_BE -->|HTTPS with API Key| YT
    OCI_BE -->|HTTPS with API Key| YT
```
