package student_player;

import boardgame.Board;
import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoCoord;
import pentago_twist.PentagoMove;

import java.util.ArrayList;
import java.util.function.UnaryOperator;

public class LightBoardState {
    public static final int BOARD_SIZE = 6;
    private static final int NUM_QUADS = 4;
    public static final int QUAD_SIZE = 6;
    public static final byte EMPTY = 0b00;
    public static final byte WHITE = 0b10;
    public static final byte BLACK = 0b01;
    public static final int MAX_TURNS = 18;

    private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX(), c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX()+1, c.getY());
    private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);


    private long first_32; // 8 bytes, holding the first 32 spaces;
    private byte last_4;
    private byte turnPlayer;
    private int winner;
    private byte turnNumber;

    public LightBoardState() {}

    public LightBoardState(PentagoBoardState other) {
        this.turnPlayer = (byte) other.getTurnPlayer();
        this.winner = other.getWinner();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                setPos(i,j, fromPiece(other.getPieceAt(i,j)));
            }
        }
        this.turnNumber = (byte) other.getTurnNumber();
    }

    public ArrayList<PentagoMove> getAllLegalMoves() {
        ArrayList<PentagoMove> legalMoves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) { //Iterate through positions on board
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (getPos(i,j) == EMPTY) {
                    for (int k = 0; k < NUM_QUADS; k++) { // Iterate through valid moves for rotate/flip
                        for (int l = 0; l < 2; l++) {
                            legalMoves.add(new PentagoMove(i, j, k, l, turnPlayer));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    public void processMove(PentagoMove m) throws IllegalArgumentException {
        updateQuadrants(m);
        updateWinner();
        if (turnPlayer != 0) { turnNumber += 1; }
        turnPlayer = (byte) (1 - turnPlayer); // Swap player
    }

    private void updateWinner() {
        boolean playerWin = checkVerticalWin(turnPlayer) || checkHorizontalWin(turnPlayer) || checkDiagRightWin(turnPlayer) || checkDiagLeftWin(turnPlayer);
        int otherPlayer = 1 - turnPlayer;
        boolean otherWin = checkVerticalWin(otherPlayer) || checkHorizontalWin(otherPlayer) || checkDiagRightWin(otherPlayer) || checkDiagLeftWin(otherPlayer);
        if (playerWin) { // Current player has won
            winner = otherWin ? Board.DRAW : turnPlayer;
        } else if (otherWin) { // Player's move caused the opponent to win
            winner = otherPlayer;
        } else if (gameOver()) {
            winner = Board.DRAW;
        }
    }

    public boolean gameOver() {
        return ((turnNumber >= MAX_TURNS - 1) && turnPlayer == BLACK) || winner != Board.NOBODY;
    }

    private boolean checkVerticalWin(int player) {
        return checkWinRange(player, 0, 2, 0, BOARD_SIZE, getNextVertical);
    }

    private boolean checkHorizontalWin(int player) {
        return checkWinRange(player, 0, BOARD_SIZE, 0, 2, getNextHorizontal);
    }

    private boolean checkDiagRightWin(int player) {
        return checkWinRange(player, 0, 2, 0, 2, getNextDiagRight);
    }

    private boolean checkDiagLeftWin(int player) {
        return checkWinRange(player, 0 ,2, BOARD_SIZE - 2, BOARD_SIZE, getNextDiagLeft);
    }

    private boolean checkWinRange(int player, int xStart, int xEnd, int yStart, int yEnd, UnaryOperator<PentagoCoord> direction) {
        boolean win = false;
        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
                win |= checkWin(player, new PentagoCoord(i, j), direction);
                if (win) { return true; }
            }
        }
        return false;
    }

    private boolean checkWin(int player, PentagoCoord start, UnaryOperator<PentagoCoord> direction) {
        int winCounter = 0;
        byte currColour = player == 0 ? WHITE : BLACK;
        PentagoCoord current = start;
        while(true) {
            try {
                if (currColour == getPos(current.getX(), current.getY())) {
                    winCounter++;
                    current = direction.apply(current);
                } else {
                    break;
                }
            } catch (IllegalArgumentException e) { //We have run off the board
                break;
            }
        }
        return winCounter >= 5;
    }

    private byte[][] getQuadrant(int q) {
        byte[][] t = new byte[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int x = i; int y = j;
                if (q == 1 || q == 3) y += QUAD_SIZE;
                if (q == 2 || q == 3) x += QUAD_SIZE;
                t[i][j] = getPos(x, y);
            }
        }
        return t;
    }

    private void writeQuadrant(int q, byte[][] newQ) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int x = i; int y = j;
                if (q == 1 || q == 3) y += QUAD_SIZE;
                if (q == 2 || q == 3) x += QUAD_SIZE;
                 setPos(x, y, newQ[i][j]);
            }
        }
    }

    private void updateQuadrants(PentagoMove m) {
        byte turnPiece = turnPlayer == 0 ? WHITE : BLACK;
        int x = m.getMoveCoord().getX();
        int y = m.getMoveCoord().getY();

        setPos(x, y, turnPiece);

        //Swapping mechanism
        int a = m.getASwap();
        int b = m.getBSwap();
        byte[][] tmp = getQuadrant(a);
        int N = tmp.length;
        byte[][] tmp2 = new byte [N][N];

        switch (b){
            case 0:
                // rotate 90 right
                //System.out.println("Rotate");
                for (int j = 0; j < N; j++)
                {
                    for (int i = N - 1; i >= 0; i--) {
                        System.arraycopy(tmp[i], j, tmp2[j],  N - 1 - i, 1);
                    }
                }
                break;
            case 1:
                // flip a quadrant
                //System.out.println("Flip");
                for (int j = 0; j < N; j++)
                {
                    for (int i = N - 1; i >= 0; i--) {
                        System.arraycopy(tmp[j], i, tmp2[j],  N - 1 - i, 1);
                    }
                }
                break;
        }
        writeQuadrant(a, tmp2);
    }

    private byte fromPiece(PentagoBoardState.Piece p) {
        if (p == PentagoBoardState.Piece.WHITE)
            return WHITE;
        if (p == PentagoBoardState.Piece.BLACK)
            return BLACK;
        return EMPTY;
    }

    private PentagoBoardState.Piece toPiece(byte p) {
        if (p == WHITE)
            return PentagoBoardState.Piece.WHITE;
        if (p == BLACK)
            return PentagoBoardState.Piece.BLACK;
        return PentagoBoardState.Piece.EMPTY;
    }

    public PentagoBoardState.Piece[][] getBoard() {
        PentagoBoardState.Piece[][] t = new PentagoBoardState.Piece[6][6];
        for (int i = 0; i < 6; i++) {
            for (int j= 0; j < 6; j++) {
                t[i][j] = toPiece(getPos(i,j));
            }
        }
        return t;
    }

    public byte getPos(int x, int y) {
        int position = y * BOARD_SIZE + x;
        if (position < 32) {
            long mask = 0b11L << (position*2);
            return (byte)((first_32 & mask) >>> (position*2));
        }
        position -= 32;
        byte mask = (byte) (0b11 << (position*2));
        return (byte)(((last_4 & mask) >>> (position*2)) & 0b11);
    }

    public void setPos(int x, int y, byte val) {
        int position = y * BOARD_SIZE + x;
        if (position < 32) {
            long mask = ~(0b11L << (position*2));
            first_32 &= mask;
            first_32 |= ((long) val) << (position*2);
            return;
        }
        position -= 32;
        byte mask = (byte)~(0b11 << (position*2));
        last_4 &= mask;
        last_4 |= ((long)val) << (position*2);
    }

    private static String pieceStr(byte p) {
        if (p == EMPTY) {
            return " ";
        }
        if (p == WHITE) {
            return "W";
        }
        return "B";
    }

    public String toString() {
        StringBuilder boardString = new StringBuilder();
        String rowMarker = "--------------------------\n";
        boardString.append(rowMarker);
        for (int i = 0; i < BOARD_SIZE; i++) {
            boardString.append("|");
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardString.append(" ");
                boardString.append(pieceStr(getPos(i,j)));
                boardString.append(" |");
                if (j == QUAD_SIZE - 1) {
                    boardString.append("|");
                }
            }
            boardString.append("\n");
            if (i == QUAD_SIZE - 1) {
                boardString.append(rowMarker);
            }
        }
        boardString.append(rowMarker);
        return boardString.toString();
    }

    public static void main(String[] args) {
        LightBoardState lbs = new LightBoardState();
        int parity = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                parity+=1;
                if (parity%2==0) {
                    lbs.setPos(i,j,WHITE);
                } else {
                    lbs.setPos(i,j,BLACK);
                }
            }
        }
        System.out.println(lbs.toString());
    }

    public boolean equals(Object o) {
        if (o instanceof LightBoardState) {
            LightBoardState other = (LightBoardState) o;
            return other.first_32 == this.first_32 && other.last_4 == this.last_4
                    && this.winner == other.winner && this.turnPlayer == other.turnPlayer;
        }
        return false;
    }

    public int getWinner() { return winner; }
    public int getTurnPlayer() { return turnPlayer; }
    public Move getRandomMove() {
        ArrayList<PentagoMove> moves = getAllLegalMoves();
        return moves.get((int) (Math.random() * moves.size()));
    }

    public Object clone() {
        LightBoardState other = new LightBoardState();
        other.turnPlayer = this.turnPlayer;
        other.winner = this.winner;
        other.first_32 = this.first_32;
        other.last_4 = this.last_4;
        other.turnNumber = this.turnNumber;
        return other;
    }

}
