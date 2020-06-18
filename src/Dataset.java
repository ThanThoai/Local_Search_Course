import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Dataset {
    protected String root;
    protected ArrayList<LocalSearch.Point2D> points;
    protected double[] demand;
    protected int N;
    protected int K;

    public Dataset(String root) {
        this.root = root;
        try {
            System.setIn(new FileInputStream(root));
        } catch (FileNotFoundException e) {
            System.err.println("File " + root + " not found");
        }
        Scanner sc = new Scanner(System.in);
        String[] n = sc.nextLine().split("\\s+");
        N = Integer.parseInt(n[0]);
        K = Integer.parseInt(n[1]);
        points = new ArrayList<>();
//        points.add(new LocalSearch.Point2D(0, 0));
        demand = new double[N];
//        demand[0] = 0.0;
        for (int i = 0; i < N; i++) {
            String[] number = sc.nextLine().split("\\s+");
            double x = Double.parseDouble(number[1]);
            double y = Double.parseDouble(number[2]);
            demand[i] = Double.parseDouble(number[0]);
            points.add(new LocalSearch.Point2D(x, y));
        }
    }

    public ArrayList<LocalSearch.Point2D> getPoints() {
        return points;
    }

    public double[] getDemand() {
        return demand;
    }

    public int getN() {
        return N;
    }

    public int getK(){
        return K;
    }

    public static void main(String[] args) {
        Dataset dataset = new Dataset("dataset/data_5_4_1.txt");
    }
}
