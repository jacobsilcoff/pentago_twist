package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class MCTSNode implements Comparable<MCTSNode>, Serializable {
    @Serial
    private static final long serialVersionUID = 29062230958230L;
    PentagoBoardState state;
    PentagoMove move;
    ArrayList<MCTSNode> children;
    MCTSNode parent;
    int visits;
    double wins;

    MCTSNode(PentagoBoardState state) {
        children = new ArrayList<>();
        this.state = (PentagoBoardState) state.clone();
    }

    MCTSNode(MCTSNode parent, PentagoMove move) {
        children = new ArrayList<>();
        state = (PentagoBoardState) parent.state.clone();
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
