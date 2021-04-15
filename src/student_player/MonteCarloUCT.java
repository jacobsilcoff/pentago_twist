package student_player;

import boardgame.Board;
import pentago_twist.PentagoMove;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the monte carlo tree search algorithm
 */
public class MonteCarloUCT {
    //basic parameters
    private static final SimulationStrategy DEFAULT_STRATEGY = SimulationStrategy.CONNECTEDNESS_HEURISTIC;
    private static final long DEFAULT_TRAIN_TIME = 1950;
    private static final int INIT_TRAIN_TIME = 27000;

    //variables storing the tree stemming from an empty board
    private static final LowMemoryBoardState INIT_STATE = new LowMemoryBoardState();
    protected static final MCTSNode INIT_ROOT = new MCTSNode();
    private static final Map<LowMemoryBoardState, MCTSNode> INIT_CHILDREN = new HashMap<>();
    static {
        // when the class initializes, we use an instance of MonteCarloUCT to
        // train the static search tree
        MonteCarloUCT m = new MonteCarloUCT();
        m.root = INIT_ROOT;
        m.train(INIT_STATE, INIT_TRAIN_TIME);
        for (MCTSNode n : INIT_ROOT.children) {
            INIT_CHILDREN.put(n.state, n);
        }
    }


    private final Map<LowMemoryBoardState, MCTSNode> descendantLookup;
    private long timeLimit;
    private SimulationStrategy simulationStrategy;
    protected MCTSNode root;

    /**
     * Creates an instance of MonteCarloUCT
     */
    public MonteCarloUCT() {
        descendantLookup = new HashMap<>();
        timeLimit = DEFAULT_TRAIN_TIME;
        simulationStrategy = DEFAULT_STRATEGY;
    }

    /**
     * Trains the algorithm from a specified board state for the
     * default amount of time
     * @param board the state to train from
     */
    public PentagoMove train(LowMemoryBoardState board) {
        return train(board, this.timeLimit);
    }

    /**
     * Trains the algorithm from a specified board state for a specified
     * @param board the state to train from
     * @param timeLimit the time, in ms, to train for
     */
    public PentagoMove train(LowMemoryBoardState board, long timeLimit) {
        long endTime = System.currentTimeMillis() + timeLimit;
        /*
        If the root doesn't correspond to the desired state to train from,
        we'll check the descendantLookup for the state before initializing the
        root to a new node at the specified board state
         */
        if (root == null || !(root.state.equals(board))) {
            root = descendantLookup.getOrDefault(
                    board, INIT_CHILDREN.getOrDefault(board, new MCTSNode(board))
            );
        }

        /*
        Performs a shallow search to prevent an immediate loss
         */
        if (root.children.isEmpty()) expand(root);
        for (MCTSNode child : root.children) {
            if (root.state.turnNumber < 4) break;
            if (child.state.gameOver() && child.state.getWinner() == root.state.getTurnPlayer()) {
                System.out.println("FOUND WINNER?");
                return child.move;
            }
            if (child.children.isEmpty()) expand(child);
            for (MCTSNode grandChild : child.children) {
                if (grandChild.state.gameOver() && grandChild.state.getWinner() != Board.DRAW
                        && grandChild.state.getWinner() != root.state.getTurnPlayer()) {
                    child.wins = Double.MIN_VALUE;
                    child.visits = 1;
                    break;
                }
            }
        }


        /*
         Runs a cycle of select, explore, simulate, and back propagate until
         the time limit expires
         */
        while (System.currentTimeMillis() < endTime) {
            MCTSNode selectedNode = selectNode(root);
            if (!selectedNode.state.gameOver()) {
                expand(selectedNode);
            }
            MCTSNode exploreNode = selectedNode;
            if (!selectedNode.children.isEmpty()) {
                exploreNode = selectedNode.children.get((int) (Math.random() * selectedNode.children.size()));
            }
            int result = simulate(exploreNode, simulationStrategy);
            backPropagate(exploreNode, result);
        }
        return null;
    }

    /**
     * Selects the best move, according to MCTS, from the given board state
     * @param board the board to select a move for
     * @return the optimal move
     */
    public PentagoMove nextMove(LowMemoryBoardState board) {
        PentagoMove m = train(board);
        if (m != null) return m;
        PentagoMove move = Collections.max(root.children).move;
        updateChildMoves(root, move);
        return move;
    }


    /**
     * Selects then leaf node with the largest UCT value
     * starting from a specified root node
     * @param node the node to select from
     * @return the leaf node with largest UCT value
     */
    private static MCTSNode selectNode(MCTSNode node) {
        MCTSNode n = node;
        while (!n.children.isEmpty()) n = Collections.max(n.children);
        return n;
    }

    /**
     * Expands the node by calculating the results of all legal moves, and
     * adding them as children
     * @param node a leaf node to expand
     */
    private static void expand(MCTSNode node) {
        List<PentagoMove> moves = node.state.getAllLegalMoves();
        for (PentagoMove m : moves) new MCTSNode(node, m);
    }

    /**
     * Backpropagate the result of a simulation from a node
     * @param node the node we simulated from
     * @param winner the player that won at that node
     */
    private static void backPropagate(MCTSNode node, int winner) {
        MCTSNode n = node;
        while (n != null) {
            n.visits ++;
            if (winner == Board.DRAW) {
                n.wins += 5;
            }
            else if (n.state.getTurnPlayer() != winner) {
                n.wins += 10;
            }
            n = n.parent;
        }
    }

    /**
     * Runs a simulation using the specified simulation strategy
     * @param node the node to simulate from
     * @param strategy the strategy to use for simulation
     * @return the winner
     */
    private static int simulate(MCTSNode node, SimulationStrategy strategy) {
        LowMemoryBoardState state = (LowMemoryBoardState) node.state.clone();
        if (state.gameOver() && state.getWinner() != node.state.getTurnPlayer()) {
            // the parent node immediately results in a win for the player
            node.parent.wins = Integer.MIN_VALUE;
            return state.getWinner();
        }
        while (!state.gameOver()) {
            if (strategy == SimulationStrategy.RANDOM)
                state.processMove((PentagoMove) state.getRandomMove());
            else if (strategy == SimulationStrategy.CONNECTEDNESS_HEURISTIC) {
                state.processMove(Heuristics.choseMove(state));
            }
        }
        return state.getWinner();
    }

    /**
     * Used when a move is made to store all of the opponents possible moves
     * in descendantLookup
     * @param node the node from which we played a move
     * @param move the move we played, who's resultant state will be analyzed
     */
    private void updateChildMoves(MCTSNode node, PentagoMove move) {
        descendantLookup.clear();
        MCTSNode nextMove = null;
        for (MCTSNode child : node.children) {
            if (child.move.equals(move)) {
                nextMove = child;
                break;
            }
        }
        if (nextMove == null) return;
        for (MCTSNode child : nextMove.children) {
            descendantLookup.put(child.state, child);
        }
    }
}
