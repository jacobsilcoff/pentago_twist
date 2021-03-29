package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.*;

public class MCTSAlgorithm {

    public static final double C = Math.sqrt(2);
    private Tree searchTree;
    private int player;

    public MCTSAlgorithm(PentagoBoardState pbs) {
        searchTree = new Tree(pbs);
        player = pbs.getTurnPlayer();
    }

    public PentagoMove getMove(int numIterations) {
        for (int i = 0; i < numIterations; i++) {
            Tree bestNode = selectNode(); // find promising root node
            if (bestNode.state.gameOver()) {
                expandNode(bestNode); // expand it with all possible children
            }
            Tree exploreNode = bestNode;
            if (bestNode.hasChildren()) {
                exploreNode = bestNode.getRandomChild(); // select random child node to explore
            }
            boolean result = simulateNode(exploreNode); // run a random game to completion
            exploreNode.backPropagate(result);
        }

        return Collections.max(searchTree.children).move;
    }

    public Tree selectNode() {
        Tree t = searchTree;
        while (t.hasChildren()) {
            if (this.player == t.state.getTurnPlayer()) {
                t = Collections.max(t.children);
            } else {
                t = Collections.min(t.children);
            }
        }
        return t;
    }

    public void expandNode(Tree leaf) {
        for (PentagoMove m : leaf.state.getAllLegalMoves()) {
            PentagoBoardState pbs = (PentagoBoardState) leaf.state.clone();
            pbs.processMove(m);
            leaf.children.add(new Tree(pbs, leaf, m));
        }
    }


    public boolean simulateNode(Tree node) {
        PentagoBoardState s =  (PentagoBoardState) node.state.clone();
        while (!s.gameOver()) {
            s.processMove((PentagoMove) s.getRandomMove());
        }
        return s.getWinner() == this.player;
    }


    private class Tree implements Comparable<Tree> {
        PentagoBoardState state;
        Tree parent;
        PentagoMove move;
        List<Tree> children;
        int visitCount;
        int winCount;

        Tree(PentagoBoardState state) {
            this.state = state;
            children = new ArrayList<>();
        }

        Tree(PentagoBoardState state, Tree parent, PentagoMove move) {
            this.state = state;
            children = new ArrayList<>();
            this.move = move;
            this.parent = parent;
        }

        double Q() {
            if (visitCount == 0) // it is always best to explore completely unexplored nodes
                return (this.state.getTurnPlayer() == player) ? Double.MAX_VALUE : Double.MIN_VALUE;
            return (double) winCount / visitCount +
                    C*Math.sqrt(log(parent.visitCount)/visitCount);
        }

        boolean hasChildren() { return !children.isEmpty(); }

        Tree getRandomChild() {
            return children.get((int)(Math.random() * children.size()));
        }

        void backPropagate(boolean win) {
            Tree t = this;
            while (t != null) {
                t.visitCount += 1;
                if (win) {
                    t.winCount += 1;
                }
                t = t.parent;
            }
        }

        @Override
        public int compareTo(Tree tree) {
            return Double.compare(this.Q(), tree.Q());
        }
    }
}
