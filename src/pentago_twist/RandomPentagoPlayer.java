package pentago_twist;

import boardgame.Move;
import student_player.StudentPlayer;

/**
 * @author mgrenander
 */
public class RandomPentagoPlayer extends PentagoPlayer {
    public RandomPentagoPlayer() {
        super("RandomPlayer");
    }

    public RandomPentagoPlayer(String name) {
        super(name);
    }

    @Override
    public Move chooseMove(PentagoBoardState boardState) {
        return boardState.getRandomMove();
    }


    public static void main(String[] args) {
        //used to test results
        int winCount = 0;
        for (int i = 0; i < 100; i++) {
            StudentPlayer sp = new StudentPlayer();
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
