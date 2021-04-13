package student_player;

import boardgame.Board;
import pentago_twist.PentagoBoard;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonteCarloUCT {
    private static final SimulationStrategy DEFAULT_STRATEGY = SimulationStrategy.RANDOM;
    private static final long DEFAULT_TRAIN_TIME = 1200;
    private static final int INIT_TRAIN_TIME = 5000;

    private static final PentagoBoardState INIT_STATE = (PentagoBoardState) (new PentagoBoard()).getBoardState();
    private static final MCTSNode INIT_ROOT = new MCTSNode();
    private static final Map<PentagoBoardState, MCTSNode> INIT_CHILDREN = new HashMap<>();
    static {
        MonteCarloUCT m = new MonteCarloUCT();
        m.root = INIT_ROOT;
        m.train(INIT_STATE, INIT_TRAIN_TIME);
        for (MCTSNode n : INIT_ROOT.children) {
            INIT_CHILDREN.put(n.state, n);
        }
    }

    private final Map<PentagoBoardState, MCTSNode> descendantLookup;
    private long timeLimit;
    private SimulationStrategy simulationStrategy;
    private MCTSNode root;

    public MonteCarloUCT() {
        descendantLookup = new HashMap<>();
        timeLimit = DEFAULT_TRAIN_TIME;
        simulationStrategy = DEFAULT_STRATEGY;
    }

    public MonteCarloUCT(long timeLimit, SimulationStrategy simulationStrategy) {
        descendantLookup = new HashMap<>();
        this.timeLimit = timeLimit;
        this.simulationStrategy = simulationStrategy;
    }

    /**
    public void createTrainingFile(long timeLimit) {
        PentagoBoardState init = (PentagoBoardState) (new PentagoBoard()).getBoardState();
        train(init, timeLimit);
        try {
            FileOutputStream fileOut = new FileOutputStream(TRAINING_FILE);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(root);
            objectOut.close();
            System.out.println("The training data was written to a file");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadTrainingFile() {
        try {
            FileInputStream fileIn = new FileInputStream(TRAINING_FILE);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            root = (MCTSNode) objIn.readObject();
            descendantLookup.clear();
            for (MCTSNode child : root.children) {
                descendantLookup.put(child.state, child);
            }
            fileIn.close();
            objIn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    **/

    public void train(PentagoBoardState board) {
        train(board, this.timeLimit);
    }

    private static boolean boardEquals(PentagoBoardState b1, PentagoBoardState b2) {
        return b1.toString().equals(b2.toString()) && b1.getTurnPlayer() == b2.getTurnPlayer();
    }

    public void train(PentagoBoardState board, long timeLimit) {
        long endTime = System.currentTimeMillis() + timeLimit;
        if (root == null || !(boardEquals(root.state, board))) {
            root = descendantLookup.getOrDefault(
                    board, INIT_CHILDREN.getOrDefault(board, new MCTSNode(board))
            );
        }
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
    }

    public PentagoMove nextMove(PentagoBoardState board) {
        train(board);
        PentagoMove move = Collections.max(root.children).move;
        updateChildMoves(root, move);
        return move;
    }

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

    private static MCTSNode selectNode(MCTSNode node) {
        MCTSNode n = node;
        while (!n.children.isEmpty()) n = Collections.max(n.children);
        return n;
    }

    private static void expand(MCTSNode node) {
        List<PentagoMove> moves = node.state.getAllLegalMoves();
        for (PentagoMove m : moves) new MCTSNode(node, m);
    }

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

    private static int simulate(MCTSNode node, SimulationStrategy strategy) {
        PentagoBoardState state = (PentagoBoardState) node.state.clone();
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
