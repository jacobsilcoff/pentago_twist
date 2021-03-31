package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.io.Serializable;
import java.util.*;

public class MCTSDAG implements Serializable {

    private Map<PentagoBoardState, Node> nodeMap;
    private long timeLimit;
    private SimulationStrategy simulationStrategy;
    private Node root;

    public MCTSDAG() {
        nodeMap = new HashMap<>();
        timeLimit = 500;
        simulationStrategy = SimulationStrategy.RANDOM;
    }

    public MCTSDAG(long timeLimit, SimulationStrategy simulationStrategy) {
        nodeMap = new HashMap<>();
        this.timeLimit = timeLimit;
        this.simulationStrategy = simulationStrategy;
    }

    public PentagoMove nextMove(PentagoBoardState board) {
        long endTime = System.currentTimeMillis() + timeLimit;

        root = nodeMap.getOrDefault(board, new Node(board));

        while (System.currentTimeMillis() < endTime) {
            Node selectedNode = selectNode(root);
            if (!selectedNode.state.gameOver()) {
                expand(selectedNode);
            }
            Node exploreNode = selectedNode;
            if (!selectedNode.children.isEmpty()) {
                exploreNode = selectedNode.children.get((int) (Math.random() * selectedNode.children.size()));
            }
            backPropagate(exploreNode, simulate(exploreNode, simulationStrategy));
        }

        return Collections.max(root.children).move;
    }

    private static Node selectNode(Node node) {
        Node n = node;
        while (!n.children.isEmpty()) n = Collections.max(n.children);
        return n;
    }

    private void expand(Node node) {
        List<PentagoMove> moves = node.state.getAllLegalMoves();
        for (PentagoMove m : moves) {
            new Node(node, m);
        }
    }

    private static void backPropagate(Node node, int winner) {
        Queue<Node> nodes = new LinkedList<>();
        Queue<Double> scale = new LinkedList<>();
        nodes.offer(node);
        scale.offer(1.0);
        while (!nodes.isEmpty()) {
            Node n = nodes.remove();
            double s = scale.remove();
            n.visits += s;
            if (n.state.getTurnPlayer() != winner) {
                node.wins += s;
            }
            for (Node parent : n.parents) {
                nodes.offer(parent);
                scale.offer(s / n.parents.size());
            }
        }
    }

    private static int simulate(Node node, SimulationStrategy strategy) {
        PentagoBoardState state = (PentagoBoardState) node.state.clone();
        if (state.gameOver() && state.getWinner() != node.state.getTurnPlayer()) {
            // the parent node immediately results in a win for the player
            node.parents.get(0).wins = Integer.MIN_VALUE;
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

    private class Node implements Comparable<Node> {
        PentagoBoardState state;
        PentagoMove move;
        List<Node> children;
        List<Node> parents;
        double visits;
        double wins;

        Node(PentagoBoardState state) {
            children = new ArrayList<>();
            parents = new ArrayList<>();
            this.state = state;
        }

        Node(Node parent, PentagoMove move) {
            children = new ArrayList<>();
            parents = new ArrayList<>();
            state = (PentagoBoardState) parent.state.clone();
            state.processMove(move);
            this.move = move;
            if (nodeMap.containsKey(state)) {
                nodeMap.get(state).parents.add(parent);
                parent.children.add(nodeMap.get(state));
            } else {
                this.parents.add(parent);
                parent.children.add(this);
                nodeMap.put(state, this);
            }
        }

        double ucdVal() {
            if (visits == 0) return Integer.MAX_VALUE;
            double parentVisits = 0;
            for (Node p : parents) {
                parentVisits += p.visits;
            }
            return wins / visits + Math.sqrt(2.0*Math.log(parentVisits)/visits);
        }

        @Override
        public int compareTo(Node node) {
            return Double.compare(ucdVal(), node.ucdVal());
        }
    }
}
