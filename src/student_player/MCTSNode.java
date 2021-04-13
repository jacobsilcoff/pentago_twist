package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.ArrayList;

public class MCTSNode implements Comparable<MCTSNode> {
    LowMemoryBoardState state;
    PentagoMove move;
    ArrayList<MCTSNode> children;
    MCTSNode parent;
    int visits;
    double wins;

    MCTSNode() {
        children = new ArrayList<>();
        this.state = new LowMemoryBoardState();
    }

    MCTSNode(LowMemoryBoardState state) {
        children = new ArrayList<>();
        this.state = (LowMemoryBoardState) state.clone();
    }

    MCTSNode(PentagoBoardState state) {
        children = new ArrayList<>();
        this.state = new LowMemoryBoardState(state);
    }

    MCTSNode(MCTSNode parent, PentagoMove move) {
        children = new ArrayList<>();
        state = (LowMemoryBoardState) parent.state.clone();
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
    public int compareTo(MCTSNode node) {
        return Double.compare(uctVal(), node.uctVal());
    }
}
