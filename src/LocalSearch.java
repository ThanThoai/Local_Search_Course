import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.MaxVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class LocalSearch {

    static class Move {
        Point x1;
        Point y1;
        Point x2;
        Point y2;

        public Move(Point x1, Point y1) {
            this.x1 = x1;
            this.y1 = y1;
        }

        public Move(Point x1, Point y1, Point x2, Point y2){
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }
    }

    static class Point2D {
        double x;
        double y;

        public Point2D(double x, double y){
            this.x = x;
            this.y = y;
        }

        public static double distance(Point2D a, Point2D b){
            return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
        }
    }



    public static final String ONE_POINT_MOVE = "One Point Move";
    public static final String TWO_POINT_MOVE = "Two Points Move";
    public static final String TWO_OPT_MOVE_1 = "Two Opt Move 1";
    public static final String CROSS_EXCHANGE_MOVE = "Cross Exchange Move";


    int K = 2;
    int N = 10;


    double[] timeService;
    ArrayList<Point2D> points;
    ArrayList<Point> starts;
    ArrayList<Point> ends;
    ArrayList<Point> clients;
    ArrayList<Point> allPoints;

    VRManager mgr;
    VarRoutesVR routers;
    ArcWeightsManager awm;
    HashMap<Point, Integer> mapPoint2ID;
    IFunctionVR cost;
//    LexMultiFunctions F;
    ConstraintSystemVR CS;
    IFunctionVR[] costRoute;
    IFunctionVR[] d;

    Random R = new Random();


    public LocalSearch(Dataset dataset){
        K = dataset.getK();
        N = dataset.getN();
        this.timeService = dataset.getDemand();
        this.points = dataset.getPoints();
    }

    public void mapping(){
        starts = new ArrayList<>();
        ends   = new ArrayList<>();
        clients = new ArrayList<>();
        allPoints = new ArrayList<>();
        mapPoint2ID = new HashMap<>();

        for(int i = 0; i < K; i++){
            Point s = new Point(0);
            Point e = new Point(0);

            starts.add(s);
            ends.add(e);
            allPoints.add(s);
            allPoints.add(e);
            mapPoint2ID.put(s, 0);
            mapPoint2ID.put(e, 0);
        }

        for(int i = 1; i < N; i++){
            Point c = new Point(i);
            clients.add(c);
            allPoints.add(c);
            allPoints.add(c);
            mapPoint2ID.put(c, i);
        }

        awm = new ArcWeightsManager(allPoints);

        for(Point p: allPoints) {
            for(Point q: allPoints) {
//                awm.setWeight(p, q, Point2D.distance((points.get(mapPoint2ID.get(p)), points.get(mapPoint2ID.get(q)))));
                if(p.ID >= N || q.ID >= N) awm.setWeight(p, q, 0);
                else
                    awm.setWeight(p, q, Point2D.distance(points.get(mapPoint2ID.get(p)), points.get(mapPoint2ID.get(q))) + timeService[mapPoint2ID.get(q)]);

            }
        }
    }

    public void stateModel(){
        mgr = new VRManager();
        routers = new VarRoutesVR(mgr);

        for(int i = 0; i < starts.size(); i++){
            routers.addRoute(starts.get(i), ends.get(i));
        }

        for(Point c : clients){
            routers.addClientPoint(c);
        }

        AccumulatedWeightEdgesVR aew = new AccumulatedWeightEdgesVR(routers, awm);
        costRoute = new IFunctionVR[starts.size()];
        for(int k = 0; k < K; k++){
            costRoute[k] = new AccumulatedEdgeWeightsOnPathVR(aew, routers.endPoint(k + 1));
        }

        cost = new MaxVR(costRoute);
        mgr.close();
    }

    public void initSolution(){
        ArrayList<Point> listPoint = new ArrayList<>();
        for(int k = 1; k <= routers.getNbRoutes(); k++){
            listPoint.add(routers.startPoint(k));
        }
        for(Point p: clients){
            Point x = listPoint.get(R.nextInt(listPoint.size()));
            mgr.performAddOnePoint(p, x);
            listPoint.add(p);
        }
    }

    public void exploreNeihborhoodOnePointMove(ArrayList<Move> cand) {
        cand.clear();
        double bestDelta = Double.MAX_VALUE;
        for(int k = 1; k <= K; k++){
            for(Point y = routers.startPoint(k); y != routers.endPoint(k); y = routers.next(y)) {
                for(Point x : clients){
                    if(x == y || x == routers.next(y))
                        continue;
//                    LexMultiValues eval = F.evaluateOnePointMove(x, y);
                    double delta = cost.evaluateOnePointMove(x, y);
                    if(delta < bestDelta) {
                        cand.clear();
                        cand.add(new Move(x, y));
                        bestDelta = delta;
                    } else if (delta == bestDelta) {
                        cand.add(new Move(x, y));
                    }
                }
            }
        }
    }

    public void exploreNeighborhoodTwoPointsMove(ArrayList<Move> cand){
        cand.clear();
        double bestDelta = Double.MAX_VALUE;
        for(int k = 1; k <= K; k++){
            for(Point y = routers.next(routers.startPoint(k)); y != routers.endPoint(k); y = routers.next(y)) {
                for(Point x = routers.next(y); x != routers.endPoint(k); x = routers.next(x)){
                    if(x == routers.next(y)) continue;
                    double delta = cost.evaluateTwoPointsMove(x, y);
                    if(delta < bestDelta){
                        cand.clear();
                        cand.add(new Move(x, y));
                        bestDelta = delta;
                    }
                    else if(delta == bestDelta){
                        cand.add(new Move(x, y));
                    }
                }
            }
        }
    }

    public void exploreNeighoodTowOptMove(ArrayList<Move> cand){
        cand.clear();
        double bestDelta = Double.MAX_VALUE;
        for(int k1 = 1; k1 < K; k1 ++){
            for(int k2 = k1 + 1; k2 <= K; k2 ++){
                for(Point y = routers.startPoint(k1); y != routers.endPoint(k1); y = routers.next(y)){
                    for(Point x = routers.startPoint(k2); x != routers.endPoint(k2); x = routers.next(x)){
                        if(x == routers.startPoint(k2) || y == routers.startPoint(k1))
                            continue;
                        double delta = cost.evaluateTwoOptMove1(x, y);
                        if(delta < bestDelta){
                            cand.clear();
                            cand.add(new Move(x, y));
                            bestDelta = delta;
                        }
                        else if(delta == bestDelta){
                            cand.add(new Move(x, y));
                        }
                    }
                }
            }
        }
    }

    public void exploreNeighborhoodCrossExchangeMove(ArrayList<Move> cand){
        cand.clear();
        double bestDelta = Double.MAX_VALUE;
        for (int k1 = 1; k1 < K; k1++) {
            for (int k2 = k1 + 1; k2 <= K; k2++) {
                for (Point y1 = routers.startPoint(k1); y1 != routers.endPoint(k1); y1 = routers.next(y1)) {
                    for (Point x1 = routers.startPoint(k1); x1 != y1; x1 = routers.next(x1)) {
                        for (Point y2 = routers.startPoint(k2); y2 != routers.endPoint(k2); y2 = routers.next(y2)) {
                            for (Point x2 = routers.startPoint(k2); x2 != y2; x2 = routers.next(x2)) {
                                double delta = cost.evaluateCrossExchangeMove(x1, y1, x2, y2);
                                if (delta < bestDelta) {
                                    cand.clear();
                                    cand.add(new Move(x1, y1, x2, y2));
                                    bestDelta = delta;
                                } else if (delta == bestDelta) {
                                    cand.add(new Move(x1, y1, x2, y2));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private StringBuilder printPath(){
        StringBuilder s = new StringBuilder();
        for(int k = 1; k <= K; k++){
            s.append("route[").append(k).append("] = ");
            Point x;
            int sum=0;
            for(x = routers.getStartingPointOfRoute(k); x != routers.getTerminatingPointOfRoute(k); x = routers.next(x)) {
                if
                ()
                s.append(x.getID()).append(" -> ");
                if(x.getID()!=0){
                    sum+=Point2D.distance(allPoints[x.getID()],allPoints[routers.next(x).getID()]);
                    sum+=timeService[routers.next(x).getID()];
                }
            }
            s.append(x.getID()).append("\n");

//            s.append(x.getID()).append(" -> ").append(routers.getStartingPointOfRoute(k).getID()).append("\n");
        }
        return s;
    }

    public void search(int loop, int limit, final String type){
        initSolution();
        int i = 0;
        ArrayList<Move> cand = new ArrayList<>();
        Move m;
        double bestResult = Double.MAX_VALUE;
        int check = 0;
        while(i < loop){
            switch (type){
                case ONE_POINT_MOVE:
                    exploreNeihborhoodOnePointMove(cand);
                    if(cand.size() == 0){
                        System.out.println("Local Optimazation");
                        break;
                    }
                    m = cand.get(R.nextInt(cand.size()));
                    mgr.performOnePointMove(m.x1, m.y1);
                    break;
                case TWO_POINT_MOVE:
                    exploreNeighborhoodTwoPointsMove(cand);
                    if(cand.size() == 0){
                        System.out.println("Local Optimazation");
                        break;
                    }
                    m = cand.get(R.nextInt(cand.size()));
                    mgr.performTwoPointsMove(m.x1, m.y1);
                    break;
                case TWO_OPT_MOVE_1:
                    exploreNeighoodTowOptMove(cand);
                    if(cand.size() == 0){
                        System.out.println("Local Optimazation");
                        break;
                    }
                    m = cand.get(R.nextInt(cand.size()));
                    mgr.performTwoOptMove1(m.x1, m.y1);
                    break;
                case CROSS_EXCHANGE_MOVE:
                    exploreNeighborhoodCrossExchangeMove(cand);
                    if(cand.size() == 0){
                        System.out.println("Local Optimazation");
                        break;
                    }
                    m = cand.get(R.nextInt(cand.size()));
                    mgr.performCrossExchangeMove(m.x1, m.y1, m.x2, m.y2);
                    break;
                default:
                    System.err.println(type + " not define");
                    return;
            }

            if(bestResult > cost.getValue()){
                bestResult = cost.getValue();
                check = 0;
            } else if(bestResult >= cost.getValue()){
                check ++;
                if (check == limit) break;
            }
            System.out.println("Step " + i + ", cost = " + cost.getValue());
            i++;

        }

        System.out.println(printPath());
        for(int k = 1; k <= K; k++){
            System.out.println("Cost of router " + k + " = " + costRoute[k - 1].getValue());
        }
    }

    public static void main(String[] args){
        Dataset dataset = new Dataset("./dataset/data_21_6_1");
        LocalSearch app = new LocalSearch(dataset);
        app.mapping();
        app.stateModel();
        app.search(10000, 1000, CROSS_EXCHANGE_MOVE);
    }
}
