package student_player;

import boardgame.Board;
import pentago_twist.PentagoMove;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MCTSLight {
    private static final SimulationStrategy DEFAULT_STRATEGY = SimulationStrategy.CONNECTEDNESS_HEURISTIC;
    private static final long DEFAULT_TRAIN_TIME = 1200;
    private static final int INIT_TRAIN_TIME = 10000;

    private static final LightBoardState INIT_STATE = new LightBoardState();
    private static final MCTSNodeLight INIT_ROOT = new MCTSNodeLight();
    private static final Map<LightBoardState, MCTSNodeLight> INIT_CHILDREN = new HashMap<>();
    static {
        MCTSLight m = new MCTSLight();
        m.root = INIT_ROOT;
        m.train(INIT_STATE, INIT_TRAIN_TIME);
        for (MCTSNodeLight n : INIT_ROOT.children) {
            INIT_CHILDREN.put(n.state, n);
        }
    }

    private final Map<LightBoardState, MCTSNodeLight> descendantLookup;
    private long timeLimit;
    private SimulationStrategy simulationStrategy;
    private MCTSNodeLight root;

    public MCTSLight() {
        descendantLookup = new HashMap<>();
        timeLimit = DEFAULT_TRAIN_TIME;
        simulationStrategy = DEFAULT_STRATEGY;
    }

    public void train(LightBoardState board) {
        train(board, this.timeLimit);
    }

    public void train(LightBoardState board, long timeLimit) {
        long endTime = System.currentTimeMillis() + timeLimit;
        if (root == null || !(root.state.equals(board))) {
            root = descendantLookup.getOrDefault(
                    board, INIT_CHILDREN.getOrDefault(board, new MCTSNodeLight(board))
            );
        }
        while (System.currentTimeMillis() < endTime) {
            MCTSNodeLight selectedNode = selectNode(root);
            if (!selectedNode.state.gameOver()) {
                expand(selectedNode);
            }
            MCTSNodeLight exploreNode = selectedNode;
            if (!selectedNode.children.isEmpty()) {
                exploreNode = selectedNode.children.get((int) (Math.random() * selectedNode.children.size()));
            }
            int result = simulate(exploreNode, simulationStrategy);
            backPropagate(exploreNode, result);
        }
    }

    public PentagoMove nextMove(LightBoardState board) {
        train(board);
        PentagoMove move = Collections.max(root.children).move;
        updateChildMoves(root, move);
        return move;
    }

    private void updateChildMoves(MCTSNodeLight node, PentagoMove move) {
        descendantLookup.clear();
        MCTSNodeLight nextMove = null;
        for (MCTSNodeLight child : node.children) {
            if (child.move.equals(move)) {
                nextMove = child;
                break;
            }
        }
        if (nextMove == null) return;
        for (MCTSNodeLight child : nextMove.children) {
            descendantLookup.put(child.state, child);
        }
    }

    private static MCTSNodeLight selectNode(MCTSNodeLight node) {
        MCTSNodeLight n = node;
        while (!n.children.isEmpty()) n = Collections.max(n.children);
        return n;
    }

    private static void expand(MCTSNodeLight node) {
        List<PentagoMove> moves = node.state.getAllLegalMoves();
        for (PentagoMove m : moves) new MCTSNodeLight(node, m);
    }

    private static void backPropagate(MCTSNodeLight node, int winner) {
        MCTSNodeLight n = node;
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

    private static int simulate(MCTSNodeLight node, SimulationStrategy strategy) {
        LightBoardState state = (LightBoardState) node.state.clone();
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
}
