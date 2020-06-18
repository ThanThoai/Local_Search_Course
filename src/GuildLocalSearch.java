//import localsearch.constraints.basic.IsEqual;
//import localsearch.constraints.basic.LessOrEqual;
//import localsearch.functions.basic.FuncMult;
//import localsearch.functions.sum.Sum;
//import localsearch.model.ConstraintSystem;
//import localsearch.model.LocalSearchManager;
//import localsearch.model.VarIntLS;
//
//import java.util.ArrayList;
//
//public class GuildLocalSearch {
//
//    static class Penalty {
//        private int p;
//        private int[][] penalty;
//
//        public Penalty(int p){
//            this.p = p;
//            penalty = new int[this.p][this.p];
//            for(int i = 0; i <= this.p; i++) {
//                for(int j = 0; j <= this.p; j++){
//                    penalty[i][j] = 0;
//                }
//            }
//        }
//
//        public int getP(){
//            return this.p;
//        }
//
//        public int getXY(int x, int y){
//            return penalty[x][y];
//        }
//
//        public void addOne(int x, int y){
//            penalty[x][y]++;
//        }
//    }
//
//    static class AssignMove{
//        int i;
//        int v;
//        public AssignMove(int i, int v){
//            this.i = i; this.v = v;
//        }
//    }
//
//    static class Index{
//        int i;
//        int j;
//
//        public Index(int i, int j){
//            this.i = i;
//            this.j = j;
//        }
//    }
//
//    static class Util{
//        private int u;
//
//        public Util(int u){
//            this.u = u;
//        }
//
//        public Index getMaxUtil(Penalty penalty, int[][] cost, int[][] I){
//            Index index = new Index(0, 0);
//            double max_util = -1;
//            for(int i = 0; i < this.u; i++){
//                for(int j = 0; j < this.u; j++){
//                    if(I[i][j] > 0){
//                        double util = I[i][j] * (cost[i][j] / (1.0 + penalty.getXY(i, j)));
//                        if(max_util < util){
//                            max_util = util;
//                            index.i = i;
//                            index.j = j;
//                        }
//                    }
//                }
//            }
//            return index;
//        }
//    }
//
//    private int N;
//    private int K;
//    LocalSearchManager mgr;
//    ConstraintSystem S;
//    VarIntLS[][][] X;
//    VarIntLS[][] Y;
//    VarIntLS[][][] F;
//
//    public void stateModel(){
//        mgr = new LocalSearchManager();
//        X = new VarIntLS[N][N][K];
//        for(int i = 0; i < N; i++){
//            for(int j = 0; j < N; j++){
//                for(int k = 0; k < K; k++){
//                    X[i][j][k] = new VarIntLS(mgr, 0, 1);
//                }
//            }
//        }
//        Y = new VarIntLS[N][K];
//        for(int i = 0; i < N; i++){
//            for(int k = 0; k < K; k++){
//                Y[i][k] = new VarIntLS(mgr, 0, 1);
//            }
//        }
//
//        F = new VarIntLS[N][N][K];
//        for(int i = 0; i < N; i++){
//            for(int j = 0; j < N; j++){
//                for(int k = 0; k < K; k++){
//                    F[i][j][k] = new VarIntLS(mgr, 0, N * N);
//                }
//            }
//        }
//
//        S = new ConstraintSystem(mgr);
//        VarIntLS[] Z = new VarIntLS[N * K];
//        int x = 0;
//        for(int k = 0; k < K; k++){
//            for(int j = 1; j < N; j++){
//                Z[x++] = X[1][j][k];
//            }
//        }
//        S.post(new IsEqual(new Sum(Z), K));
//
//        for(int i = 0; i < N; i++){
//            VarIntLS[] W = new VarIntLS[K];
//            for(int k = 0; k < K; k++){
//                W[i] = Y[i][k];
//            }
//
//            S.post(new IsEqual(new Sum(W), 1));
//        }
//
//        for(int i = 0; i < N; i++){
//            for(int k = 0; k < K; k++){
//                VarIntLS[] T = new VarIntLS[N];
//                for(int j = 0; j < N; j++){
//                    T[j] = X[j][i][k];
//                }
//                S.post(new IsEqual(new Sum(T), Y[i][k]));
//            }
//        }
//        for(int j = 0; j < N; j++){
//            for(int k = 0; k < K; k++){
//                VarIntLS[] T = new VarIntLS[N];
//                for(int i = 0; i < N; i++){
//                    T[i] = X[i][j][k];
//                }
//                S.post(new IsEqual(new Sum(T), Y[j][k]));
//            }
//        }
//
//        for(int i = 0; i < N; i++){
//            for(int j = 0; j < N; j++){
//                for(int k = 0; k < K; k++){
//                    S.post(new LessOrEqual(F[i][j][k], new FuncMult(X[i][j][k], N - 1)));
//                }
//            }
//        }
//
//        VarIntLS[] A = new VarIntLS[N * K];
//        x = 0;
//        for(int k = 0; k < K; k++){
//            for(int i = 0; i < N; i++){
//                A[x++] = F[i][0][k];
//            }
//        }
//        S.post(new IsEqual(new Sum(A), N - 1));
//
//        for(int i = 0; i < N; i++){
//            for(int k = 0; k < K; k++){
//                S.post(new IsEqual(F[0][i][k], 0));
//            }
//        }
//    }
//}
