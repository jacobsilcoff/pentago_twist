package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.*;

public class MCTS {

    private Map<PentagoBoardState, Node> opponentsMoves;
    private long timeLimit;

    public MCTS() {
        opponentsMoves = new HashMap<>();
        timeLimit = 500;
    }

    public MCTS(long timeLimit) {
        opponentsMoves = new HashMap<>();
        this.timeLimit = timeLimit;
    }

    public PentagoMove nextMove(PentagoBoardState board) {
        long endTime = System.currentTimeMillis() + timeLimit;

        Node root = opponentsMoves.getOrDefault(board, new Node(board));

        while (System.currentTimeMillis() < endTime) {
            Node selectedNode = selectNode(root);
            if (!selectedNode.state.gameOver()) {
                expand(selectedNode);
            }
            Node exploreNode = selectedNode;
            if (!selectedNode.children.isEmpty()) {
                exploreNode = selectedNode.children.get((int) (Math.random() * selectedNode.children.size()));
            }
            backPropagate(exploreNode, simulateRandom(exploreNode));
        }

        PentagoMove move = Collections.max(root.children).move;
        updateChildMoves(root, move);
        return move;
    }

    private void updateChildMoves(Node node, PentagoMove move) {
        opponentsMoves.clear();
        Node nextMove = null;
        for (Node child : node.children) {
            if (child.move.equals(move)) {
                nextMove = child;
                break;
            }
        }
        if (nextMove == null) return;
        for (Node child : nextMove.children) {
            opponentsMoves.put(child.state, child);
        }
    }

    private static Node selectNode(Node node) {
        Node n = node;
        while (!n.children.isEmpty()) n = Collections.max(n.children);
        return n;
    }

    private void expand(Node node) {
        List<PentagoMove> moves = node.state.getAllLegalMoves();
        for (PentagoMove m : moves) new Node(node, m);
    }

    private void backPropagate(Node node, int winner) {
        Node n = node;
        while (n != null) {
            n.visits ++;
            if (n.state.getTurnPlayer() != winner) {
                node.wins ++;
            }
            n = n.parent;
        }
    }

    private int simulateRandom(Node node) {
        PentagoBoardState state = (PentagoBoardState) node.state.clone();
        if (state.gameOver() && state.getWinner() != node.state.getTurnPlayer()) {
            // the parent node immediately results in a win for the player
            node.parent.wins = Integer.MIN_VALUE;
            return state.getWinner();
        }
        while (!state.gameOver()) {
            state.processMove((PentagoMove) state.getRandomMove());
        }
        return state.getWinner();
    }

    private class Node implements Comparable<Node>{
        PentagoBoardState state;
        PentagoMove move;
        List<Node> children;
        Node parent;
        int visits;
        double wins;

        Node(PentagoBoardState state) {
            children = new ArrayList<>();
            this.state = state;
        }

        Node(Node parent, PentagoMove move) {
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
        public int compareTo(Node node) {
            return Double.compare(uctVal(), node.uctVal());
        }
    }
}
