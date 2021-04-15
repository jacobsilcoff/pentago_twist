package student_player;

import pentago_twist.PentagoMove;

import java.io.*;
import java.util.ArrayList;

/**
 * A node used for MCTS
 */
public class MCTSNode implements Comparable<MCTSNode>, Serializable {
    LowMemoryBoardState state;
    transient PentagoMove move;
    ArrayList<MCTSNode> children;
    MCTSNode parent;
    int visits;
    double wins;

    MCTSNode() {
        children = new ArrayList<>();
        this.state = new LowMemoryBoardState();
    }

    /**
     * Creates an MCTS node based on a given board state
     * @param state the state the node should hold
     */
    MCTSNode(LowMemoryBoardState state) {
        children = new ArrayList<>();
        this.state = (LowMemoryBoardState) state.clone();
    }

    /**
     * Creates a node given the nodes parent and the move taken
     * to get to the newly created node. This has the side effect of
     * adding the node to its parent's list of children.
     * @param parent the parent of the node to be created
     * @param move the move that this node results from
     */
    MCTSNode(MCTSNode parent, PentagoMove move) {
        children = new ArrayList<>();
        state = (LowMemoryBoardState) parent.state.clone();
        state.processMove(move);
        this.parent = parent;
        this.move = move;
        parent.children.add(this);
    }

    /**
     * Calculates the UCT value of the node
     */
    double uctVal() {
        if (visits == 0) return Integer.MAX_VALUE;
        return wins / visits + Math.sqrt(2.0*Math.log(parent.visits)/visits);
    }

    @Override
    public int compareTo(MCTSNode node) {
        return Double.compare(uctVal(), node.uctVal());
    }

    /*
    private int playerId;
    private int xMove;
    private int yMove;
    private int aSwap;
    private int bSwap;
     */

    @Serial
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        System.out.println("RAN");
        aInputStream.defaultReadObject();
        if (parent != null) {
            int id = aInputStream.readInt();
            int x = aInputStream.readInt();
            int y = aInputStream.readInt();
            int a = aInputStream.readInt();
            int b = aInputStream.readInt();
            this.move = new PentagoMove(x, y, a, b, id);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
        if (move != null) {
            aOutputStream.writeInt(move.getPlayerID());
            aOutputStream.writeInt(move.getMoveCoord().getX());
            aOutputStream.writeInt(move.getMoveCoord().getY());
            aOutputStream.writeInt(move.getASwap());
            aOutputStream.writeInt(move.getBSwap());
        }
    }
}
