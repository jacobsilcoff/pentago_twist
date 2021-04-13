package student_player;

import boardgame.Move;
import pentago_twist.PentagoBoard;
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

    //These constructors are for testing purposes only:
    public StudentPlayer(long time, SimulationStrategy simulationStrategy) {
        super("260767897");
        mcts = new MonteCarloUCT(time, simulationStrategy);
        mcts.train((PentagoBoardState) (new PentagoBoard()).getBoardState(), 20000);
    }

    public StudentPlayer(boolean heuristicOnly) {
        super("260767897");
        this.heuristicOnly = heuristicOnly;
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        if (mcts == null) {
            mcts = new MonteCarloUCT();
        }
        if (heuristicOnly) return Heuristics.choseMove(boardState);
        return mcts.nextMove(boardState);
    }
}