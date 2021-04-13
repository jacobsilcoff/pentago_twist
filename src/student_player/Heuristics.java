package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.List;

import static pentago_twist.PentagoBoardState.Piece;

public class Heuristics {

    private static final int HORIZ = 0;
    private static final int VERT = 1;
    private static final int DIAG_1 = 2;
    private static final int DIAG_2 = 3;
    private static final int[] DIRECTIONS = new int[]{HORIZ, VERT, DIAG_1, DIAG_2};
    private static final int[] WEIGHTS = new int[] {0, 1, 10, 100, 1000};

    public static PentagoMove choseMove(PentagoBoardState state) {
        int maxScore = Integer.MIN_VALUE;
        PentagoMove bestMove = null;
        List<PentagoMove> moves = state.getAllLegalMoves();
        Piece you = state.getTurnPlayer() == PentagoBoardState.BLACK ? Piece.BLACK : Piece.WHITE;
        for (PentagoMove m : moves) {
            PentagoBoardState child = (PentagoBoardState) state.clone();
            child.processMove(m);
            int score = evaluateConnectedness(child, you);
            if (score > maxScore) {
                bestMove = m;
                maxScore = score;
            }
        }
        return bestMove;
    }

    public static int evaluateConnectedness(PentagoBoardState state, Piece you) {
        if (state.gameOver()) {
            if (state.getWinner() == state.getTurnPlayer()) return Integer.MIN_VALUE;
            return Integer.MAX_VALUE;
        }

        Piece[][] pieces = state.getBoard();
        boolean[][][] checked = new boolean[pieces.length][pieces[0].length][DIRECTIONS.length];
        int score = 0;
        Piece opponentPiece = (you == Piece.BLACK) ? Piece.WHITE : Piece.BLACK;

        for (int row = 0; row < pieces.length; row++) {
            for (int col = 0; col < pieces[row].length; col++) {
                score += checkConnections(pieces, you, checked, row, col);
                score -= checkConnections(pieces, opponentPiece, checked, row, col);
            }
        }

        return score;
    }

    public static PentagoMove choseMove(LightBoardState state) {
        int maxScore = Integer.MIN_VALUE;
        PentagoMove bestMove = null;
        List<PentagoMove> moves = state.getAllLegalMoves();
        Piece you = state.getTurnPlayer() == PentagoBoardState.BLACK ? Piece.BLACK : Piece.WHITE;
        for (PentagoMove m : moves) {
            LightBoardState child = (LightBoardState) state.clone();
            child.processMove(m);
            int score = evaluateConnectedness(child, you);
            if (score > maxScore) {
                bestMove = m;
                maxScore = score;
            }
        }
        return bestMove;
    }

    public static int evaluateConnectedness(LightBoardState state, Piece you) {
        if (state.gameOver()) {
            if (state.getWinner() == state.getTurnPlayer()) return Integer.MIN_VALUE;
            return Integer.MAX_VALUE;
        }

        Piece[][] pieces = state.getBoard();
        boolean[][][] checked = new boolean[pieces.length][pieces[0].length][DIRECTIONS.length];
        int score = 0;
        Piece opponentPiece = (you == Piece.BLACK) ? Piece.WHITE : Piece.BLACK;

        for (int row = 0; row < pieces.length; row++) {
            for (int col = 0; col < pieces[row].length; col++) {
                score += checkConnections(pieces, you, checked, row, col);
                score -= checkConnections(pieces, opponentPiece, checked, row, col);
            }
        }

        return score;
    }

    private static int checkConnections(Piece[][] pieces, Piece color, boolean[][][] checked, int row, int col) {
        if (pieces[row][col] != color) return 0;
        int score = 0;
        for (int d : DIRECTIONS) {
            if (checked[row][col][d]) continue;
            int count = 0;
            int i = row;
            int j = col;
            while (i >= 0 && j >= 0 && i < pieces.length && j < pieces[0].length
                    && count < 4 && pieces[i][j] == color) {
                count++;
                checked[i][j][d] = true;
                if (d == VERT || d == DIAG_1 || d == DIAG_2) i++;
                if (d == HORIZ || d == DIAG_1) j++;
                if (d == DIAG_2) j--;
            }
            score += WEIGHTS[count];
        }
        return score;
    }
}
