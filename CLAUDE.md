# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the application
./mvnw javafx:run

# Compile
./mvnw compile

# Run tests
./mvnw test

# Package
./mvnw package
```

On Windows CMD use `mvnw.cmd` instead of `./mvnw`.

## Architecture

JavaFX chess game GUI that delegates all chess rules to the [chesslib](https://github.com/bhlangonijr/chesslib) library via an adapter.

**Entry point:** `Launcher` → `HelloApplication` loads `Board.fxml`, which wires up `BoardController`.

**Key classes:**

- `BoardController` — Central controller. Owns the game loop: handles click events on board squares, calls `ChessLibAdapter` to validate/execute moves, updates square images, manages per-side countdown timers, and checks win conditions.
- `ChessLibAdapter` — Wraps chesslib. All chess logic (legal moves, move execution, win/stalemate detection) goes through here.
- `BoardBox` — Custom `StackPane` for a single square. Holds a `Square` reference and the current piece `Image`.
- `PieceImageContainer` — Loads piece PNGs from `src/main/resources/.../img/` using `img.txt` as a mapping config.
- `WinCondition` — Enum: `BLACK_WIN`, `WHITE_WIN`, `DRAW`, `STALEMATE`, `NONE`.

**Interaction flow:**

```
User clicks BoardBox
  → BoardController.clickEventForBox()
      → ChessLibAdapter.seeMove()      // get legal moves for selected piece
      → ChessLibAdapter.confirmMove()  // execute chosen move
      → update BoardBox images
      → check WinCondition
      → redraw board highlights
```

**Java version:** 24
**JavaFX version:** 21.0.6
**Build system:** Maven (wrapper included)

## Multiplayer Architecture (not yet implemented)

Multiplayer uses a separate hosted Spring Boot server with STOMP over WebSocket.

### Screen flow
```
StartScreen → UsernameScreen → QueueScreen → BoardScreen → StartScreen
```

### Server (Spring Boot)
- `MatchmakingService` — holds a queue of waiting players; when 2 are present, pairs them, creates a `GameSession`, assigns WHITE/BLACK, and notifies both via `/user/queue/matched`
- `GameSession` — holds `gameId`, both usernames, assigned sides, and a chesslib board instance
- `GameService` — receives moves on `/app/game/{gameId}/move`, validates via chesslib, broadcasts result to `/topic/game/{gameId}`

### WebSocket message flow
```
Client connects → sends JoinQueueMessage{username} to /app/queue
Server pairs players → sends MatchFoundMessage{gameId, opponentUsername, assignedSide}
                        to each player via /user/queue/matched
Both clients subscribe to /topic/game/{gameId} and navigate to BoardScreen

Player moves → sends MoveMessage{gameId, from, to} to /app/game/{gameId}/move
Server validates + broadcasts MoveResult{from, to, nextSide, winCondition}
Both clients apply move and redraw

Game ends → server sends GameOverMessage{result} → both clients return to StartScreen
```

### Client additions
- `UsernameController` — captures username, initiates WebSocket connection
- `QueueController` — sends `JoinQueueMessage`, subscribes to `/user/queue/matched`, shows "Waiting for opponent…"
- `BoardController` multiplayer mode — knows `assignedSide`, disables clicks when it is not the local player's turn, applies incoming moves from the server rather than local chesslib

### Key decisions
- **Server-authoritative moves:** client sends intent, server applies it to its own chesslib instance and broadcasts the result; client never directly mutates game state in multiplayer
- **Timers are client-side:** each client runs its own countdown; server does not track time
- WebSocket connection is opened when entering the queue and closed when returning to StartScreen
