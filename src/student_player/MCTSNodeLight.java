package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.ArrayList;

public class MCTSNodeLight implements Comparable<MCTSNodeLight> {
    LightBoardState state;
    PentagoMove move;
    ArrayList<MCTSNodeLight> children;
    MCTSNodeLight parent;
    int visits;
    double wins;

    MCTSNodeLight() {
        children = new ArrayList<>();
        this.state = new LightBoardState();
    }

    MCTSNodeLight(LightBoardState state) {
        children = new ArrayList<>();
        this.state = (LightBoardState) state.clone();
    }

    MCTSNodeLight(PentagoBoardState state) {
        children = new ArrayList<>();
        this.state = new LightBoardState(state);
    }

    MCTSNodeLight(MCTSNodeLight parent, PentagoMove move) {
        children = new ArrayList<>();
        state = (LightBoardState) parent.state.clone();
        state.processMove(move);
        this.parent = parent;
        this.move = move;
        parent.children.add(this);
    }

    double uctVal() {
        if (visits == 0) return Integer.MAX_VALUE;
        return wins / visits + Math.sqrt(2.0*Math.log(parent.visits)/visits);
    }

    @Override
    public int compareTo(MCTSNodeLight node) {
        return Double.compare(uctVal(), node.uctVal());
    }
}
