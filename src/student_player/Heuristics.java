package student_player;

import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;
import pentago_twist.PentagoPlayer;

import java.util.List;

import static pentago_twist.PentagoBoardState.Piece;

/**
 * Holds a heuristic used for training MCTS
 *
 * It's pluralized since I was going to add more and test them against each other,
 * but ran out of time.
 */
public class Heuristics extends PentagoPlayer {

    private static final int HORIZ = 0;
    private static final int VERT = 1;
    private static final int DIAG_1 = 2;
    private static final int DIAG_2 = 3;
    private static final int[] DIRECTIONS = new int[]{HORIZ, VERT, DIAG_1, DIAG_2};
    private static final int[] WEIGHTS = new int[] {0, 1, 10, 100, 1000};

    /**
     * Chooses a move based on a connectedness heuristic that prioritizes
     * states where your pieces are lined up
     * @param state the state to chose a move from
     * @return the best move according to the heuristic
     */
    public static PentagoMove choseMove(LowMemoryBoardState state) {
        int maxScore = Integer.MIN_VALUE;
        PentagoMove bestMove = null;
        List<PentagoMove> moves = state.getAllLegalMoves();
        Piece you = state.getTurnPlayer() == PentagoBoardState.BLACK ? Piece.BLACK : Piece.WHITE;
        for (PentagoMove m : moves) {
            LowMemoryBoardState child = (LowMemoryBoardState) state.clone();
            child.processMove(m);
            int score = evaluateConnectedness(child, you);
            if (score > maxScore) {
                bestMove = m;
                maxScore = score;
            }
        }
        return bestMove;
    }

    /**
     * Evaluates the "connectedness" of a board state. It counts the number
     * of 1s, 2s, 3s, and 4s in-a-row for each player, scores them with a weight
     * (0, 10, 100, and 1000 respectively) and subtracts the score for the current
     * player from the score for the opponent.
     * @param state the state to evaluate
     * @param you the player to evaluate for
     * @return a "connectedness score"
     */
    public static int evaluateConnectedness(LowMemoryBoardState state, Piece you) {
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

    @Override
    public Move chooseMove(PentagoBoardState boardState) {
        return choseMove(new LowMemoryBoardState(boardState));
    }

    public Heuristics() {
        super("260767897");
    }
}
