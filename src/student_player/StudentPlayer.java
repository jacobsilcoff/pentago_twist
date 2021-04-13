package student_player;

import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {


    private MonteCarloUCT mcts;
    private boolean heuristicOnly = false;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260767897");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        try {
            if (heuristicOnly) {
                System.out.println("chose from heuristic");
                return Heuristics.choseMove(new LowMemoryBoardState(boardState));
            }
            if (mcts == null) {
                mcts = new MonteCarloUCT();
            }
            return mcts.nextMove(new LowMemoryBoardState(boardState));
        } catch (OutOfMemoryError e) {
            mcts = null;
            System.gc();
            this.heuristicOnly = true;
            return Heuristics.choseMove(new LowMemoryBoardState(boardState));
        }
    }
}
