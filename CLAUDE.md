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
