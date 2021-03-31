package pentago_twist;

import student_player.MonteCarloUCT;
import student_player.SimulationStrategy;
import student_player.StudentPlayer;

public class AgentTests {
    public static void main(String[] args) {
        //vsRandom(new StudentPlayer);
        //headToHead(new StudentPlayer(500, CONNECTEDNESS_HEURISTIC), new RandomPentagoPlayer(), 25);
        new MonteCarloUCT(1900, SimulationStrategy.RANDOM).createTrainingFile(500);
    }

    public static void headToHead(PentagoPlayer p1, PentagoPlayer p2, int times) {
        int p1Wins = 0;
        int p2Wins = 0;
        int draws = 0;
        for (int i = 0; i < times; i++) {
            PentagoBoardState state = new PentagoBoardState();
            while (!state.gameOver()) {
                if (state.getTurnPlayer() == 0) {
                    PentagoMove move = (PentagoMove) p1.chooseMove(state);
                    state.processMove(move);
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
            System.out.println("P1:P2:Draw = " + p1Wins + ":"+p2Wins+":"+draws);
        }
    }

    public static void vsRandom(StudentPlayer sp) {
        int winCount = 0;
        for (int i = 0; i < 100; i++) {
            RandomPentagoPlayer rp = new RandomPentagoPlayer();
            PentagoBoardState state = new PentagoBoardState();
            while (!state.gameOver()) {
                if (state.getTurnPlayer() == 0) {
                    state.processMove((PentagoMove) sp.chooseMove(state));
                } else {
                    state.processMove((PentagoMove) rp.chooseMove(state));
                }
            }
            if (state.getWinner() == 0) {
                winCount++;
            }
            System.out.println("Won " + winCount + " of " + (i+1) + ".");
        }
    }
}
