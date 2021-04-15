package pentago_twist;

import student_player.LowMemoryBoardState;
import student_player.StudentPlayer;

import java.util.ArrayList;

public class AgentTests {
    public static void main(String[] args) {
        // issue -- slow! and why??
        //testRandomRepresentation();
        headToHead(new StudentPlayer(), new RandomPentagoPlayer(), 50);
//        TrainingFileManager.write();
//        MCTSNode node = TrainingFileManager.read();
//        System.out.println(node);
    }

    public static void testRandomRepresentation() {
        System.out.println();
        for (int i = 0; i < 100; i++) {
            PentagoBoardState std = (PentagoBoardState) (new PentagoBoard()).getBoardState();
            LowMemoryBoardState l = new LowMemoryBoardState();
            while (!(std.gameOver() && l.gameOver())) {
                ArrayList<PentagoMove> moves = std.getAllLegalMoves();
                assert(moves.equals(l.getAllLegalMoves()));
                PentagoMove move = moves.get((int)(Math.random() * moves.size()));
                l.processMove(move);
                std.processMove(move);
                assert(std.toString().equals(l.toString()));
            }
            assert(std.getWinner() == l.getWinner());
            System.out.print(i + ", ");
        }
        System.out.println();

    }

    public static void headToHead(PentagoPlayer p1, PentagoPlayer p2, int times) {
        int p1Wins = 0;
        int p2Wins = 0;
        int draws = 0;
        for (int i = 0; i < times; i++) {
            int total_time = 0;
            int max_time = 0;
            int first_time = -1;
            double moves = 0;
            PentagoBoardState state = new PentagoBoardState();
            while (!state.gameOver()) {
                if (state.getTurnPlayer() == 0) {
                    long start = System.currentTimeMillis();
                    PentagoMove move = (PentagoMove) p1.chooseMove(state);
                    state.processMove(move);
                    long end = System.currentTimeMillis();
                    int time = (int) (end - start);
                    if (first_time == -1) first_time = time;
                    else {
                        if (time > max_time) max_time = time;
                        total_time += time;
                        moves++;
                    }
                }
                else state.processMove((PentagoMove) p2.chooseMove(state));
            }
            if (state.getWinner() == 0) {
                p1Wins ++;
            } else if (state.getWinner() == 1) {
                p2Wins ++;
            } else {
                draws++;
            }
            System.out.println("P1:P2:Draw = " + p1Wins + ":"+p2Wins+":"+draws +
                    "   (mean: " + (total_time / moves) + "ms, max: " + max_time + "ms, first: " + first_time + "ms)");
        }
    }
}
