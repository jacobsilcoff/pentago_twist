package student_player;

import java.io.*;

public class TrainingFileManager {
    public static final String FILE_PATH = "data/training.ser";

    public static void write() {
        MonteCarloUCT mcts = new MonteCarloUCT();
        System.out.println("done training");
        MCTSNode node = MonteCarloUCT.INIT_ROOT;
        try {
            FileOutputStream fos = new FileOutputStream(FILE_PATH);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(node);
            oos.close();
            fos.close();
        } catch (IOException ignored) {}

    }

    public static MCTSNode read() {
        MCTSNode result = null;
        try {
            FileInputStream fis = new FileInputStream(FILE_PATH);
            ObjectInputStream ois = new ObjectInputStream(fis);

            result = (MCTSNode) ois.readObject();

            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
        return result;
    }
}
