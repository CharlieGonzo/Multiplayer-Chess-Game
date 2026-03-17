package org.example.chesslibuiexample.util;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.example.chesslibuiexample.model.WinCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChessLibAdapterTest {

    private ChessLibAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ChessLibAdapter();
    }

    // --- Initial state ---

    @Test
    void initialSideIsWhite() {
        assertEquals("WHITE", adapter.getCurrentSide());
    }

    @Test
    void initialSelectionStateIsFalse() {
        assertFalse(adapter.getSelectionState());
    }

    @Test
    void initialCheckWinIsNone() {
        assertEquals(WinCondition.NONE, adapter.checkWin());
    }

    // --- seeMove ---

    @Test
    void seeMoveOnPawnPopulatesPossibleMoves() {
        adapter.seeMove(Square.E2);
        assertNotNull(adapter.getPossibleMoves());
        assertFalse(adapter.getPossibleMoves().isEmpty());
        assertTrue(adapter.getPossibleMoves().stream().allMatch(m -> m.getFrom().equals(Square.E2)));
    }

    @Test
    void seeMoveOnEmptySquareDoesNotChangePossibleMoves() {
        adapter.seeMove(Square.E4); // empty square at game start
        // possibleMoves stays null since no legal moves from empty square
        assertNull(adapter.getPossibleMoves());
    }

    @Test
    void seeMoveOnPawnSetsSelectedSquare() {
        adapter.seeMove(Square.E2);
        assertEquals(Square.E2, adapter.getSelectedSquare());
    }

    @Test
    void seeMoveOnOpponentPieceDoesNotSelect() {
        // White cannot select a black piece
        adapter.seeMove(Square.E7);
        assertNull(adapter.getSelectedSquare());
    }

    @Test
    void seeMoveE2HasTwoOptions() {
        adapter.seeMove(Square.E2);
        assertEquals(2, adapter.getPossibleMoves().size()); // e3 and e4
    }

    // --- confirmMove ---

    @Test
    void confirmMoveFailsWithoutSelectionState() {
        adapter.seeMove(Square.E2);
        // selectionState is still false (set by controller normally)
        boolean result = adapter.confirmMove(Square.E4, new Move(Square.E2, Square.E4));
        assertFalse(result);
    }

    @Test
    void confirmMoveSucceedsWithSelectionState() {
        adapter.seeMove(Square.E2);
        adapter.setSelectionState(true);
        boolean result = adapter.confirmMove(Square.E4, new Move(Square.E2, Square.E4));
        assertTrue(result);
    }

    @Test
    void confirmMoveChangesToBlacksTurn() {
        adapter.seeMove(Square.E2);
        adapter.setSelectionState(true);
        adapter.confirmMove(Square.E4, new Move(Square.E2, Square.E4));
        assertEquals("BLACK", adapter.getCurrentSide());
    }

    @Test
    void confirmMoveWithIllegalDestinationReturnsFalse() {
        adapter.seeMove(Square.E2);
        adapter.setSelectionState(true);
        boolean result = adapter.confirmMove(Square.E5, new Move(Square.E2, Square.E5)); // e5 is illegal for pawn
        assertFalse(result);
    }

    // --- clearActions ---

    @Test
    void clearActionsResetsSelectionState() {
        adapter.setSelectionState(true);
        adapter.clearActions();
        assertFalse(adapter.getSelectionState());
    }

    @Test
    void clearActionsEmptiesPossibleMoves() {
        adapter.seeMove(Square.E2);
        adapter.clearActions();
        assertTrue(adapter.getPossibleMoves().isEmpty());
    }

    // --- doMoveIfLegal ---

    @Test
    void doMoveIfLegalAppliesLegalMove() {
        adapter.doMoveIfLegal(Square.E2, Square.E4);
        assertEquals("BLACK", adapter.getCurrentSide());
    }

    @Test
    void doMoveIfLegalIgnoresIllegalMove() {
        adapter.doMoveIfLegal(Square.E2, Square.E5); // illegal
        assertEquals("WHITE", adapter.getCurrentSide()); // turn unchanged
    }

    @Test
    void doMoveIfLegalAllowsSequentialMoves() {
        adapter.doMoveIfLegal(Square.E2, Square.E4);
        adapter.doMoveIfLegal(Square.E7, Square.E5);
        assertEquals("WHITE", adapter.getCurrentSide());
    }

    // --- checkWin ---

    @Test
    void checkWinReturnsWhiteWinAfterScholarsMate() {
        // Scholar's mate: 1.e4 e5 2.Bc4 Nc6 3.Qh5 Nf6?? 4.Qxf7#
        adapter.doMoveIfLegal(Square.E2, Square.E4);
        adapter.doMoveIfLegal(Square.E7, Square.E5);
        adapter.doMoveIfLegal(Square.F1, Square.C4);
        adapter.doMoveIfLegal(Square.B8, Square.C6);
        adapter.doMoveIfLegal(Square.D1, Square.H5);
        adapter.doMoveIfLegal(Square.G8, Square.F6);
        adapter.doMoveIfLegal(Square.H5, Square.F7);

        assertEquals(WinCondition.WHITE_WIN, adapter.checkWin());
    }

    @Test
    void checkWinReturnsNoneAfterOpeningMoves() {
        adapter.doMoveIfLegal(Square.E2, Square.E4);
        adapter.doMoveIfLegal(Square.E7, Square.E5);
        assertEquals(WinCondition.NONE, adapter.checkWin());
    }

    @Test
    void checkWinDetectsStalemate() {
        // Load a known stalemate FEN: black king stalemated
        Board board = new Board();
        board.loadFromFen("5k2/5P2/5K2/8/8/8/8/8 b - - 0 1");
        ChessLibAdapter stalemateAdapter = new ChessLibAdapter(board);

        assertEquals(WinCondition.STALEMATE, stalemateAdapter.checkWin());
    }
}
