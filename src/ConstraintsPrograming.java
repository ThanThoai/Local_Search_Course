import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;

public class ConstraintsPrograming {
    protected int K;
    protected int N;

    int[] timeService;
    ArrayList<LocalSearch.Point2D> points;
    Model model;
    IntVar[] X;
    IntVar[] router;
    IntVar[] times;
    IntVar obj;

}
