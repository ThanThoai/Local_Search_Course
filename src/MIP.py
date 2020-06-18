import json
from ortools.sat.python import cp_model
from ortools.linear_solver import pywraplp
import numpy as np
import json

class MIP(object):
    
    def __init__(self, path_json):
        data = json.load(open(path_json, 'r'))
        self.solver = pywraplp.Solver(
            "MIP", pywraplp.Solver.GLOP_LINEAR_PROGRAMMING)
        # self.solver = cp_model.CpModel()
        self.num_customer = data['num_customer']  
        self.num_vehicles = data['num_vehicles']  
        self.distance = np.array(data['distance_matrix'])
        print(self.distance.shape)
        self.X = np.empty(shape = (self.num_customer, self.num_customer, self.num_vehicles))
        self.Y = np.empty(shape = (self.num_customer, self.num_vehicles))
        self.F = np.empty(shape = (self.num_customer, self.num_customer, self.num_vehicles))
        # self.O = np.empty(shape = (self.num_vehicles))
    
    def state_model(self):
        infinity = self.solver.infinity()
        
        self.X = np.array([[[self.solver.IntVar(0, 1, 'x[{0},{1},{2}]'.format(i, j, k)) for k in range(self.num_vehicles)] for j in range(self.num_customer)] for i in range(self.num_customer)])
        
        self.Y = np.array([[self.solver.IntVar(0, 1, 'y[{0},{1}]'.format(i, k)) for k in range(self.num_vehicles)] for i in range(self.num_customer)])
        
        self.F = np.array([[[self.solver.NumVar(0, infinity, 'f[{0},{1},{2}]'.format(i, j, k)) for k in range(self.num_vehicles)] for j in range(self.num_customer)] for i in range(self.num_customer)])
        
        self.routes = np.array([self.solver.NumVar(0.0, infinity, 'router[{0}]'.format(i)) for i in range(self.num_vehicles)])
        
        #4
        for i in range(self.num_customer):
            expr = []
            for k in range(self.num_vehicles):
                expr.append(self.Y[i, k])
            self.solver.Add(self.solver.Sum(expr) == 1)
            
        #2  
        expr = []
        for j in range(1, self.num_customer):
            for k in range(self.num_vehicles):
                expr.append(self.X[0, j, k])
        self.solver.Add(self.solver.Sum(expr) == self.num_vehicles)
        
        #3
        expr = []
        for i in range(1, self.num_customer):
            for k in range(self.num_vehicles):
                expr.append(self.X[i, 0, k])
        self.solver.Add(self.solver.Sum(expr) == self.num_vehicles)
        
        #5
        for i in range(self.num_customer):
            for k in range(self.num_vehicles):
                expr = []
                for h in range(self.num_customer):
                    expr.append(self.X[h, i, k])
                self.solver.Add(self.solver.Sum(expr) == self.Y[i, k])
        
        
        #6
        for i in range(self.num_customer):
            for k in range(self.num_vehicles):
                expr = []
                for j in range(self.num_customer):
                    expr.append(self.X[i, j, k])
                self.solver.Add(self.solver.Sum(expr) == self.Y[i, k])
        
        # 7
        for i in range(self.num_customer):
            for j in range(self.num_customer):
                for k in range(self.num_vehicles):
                    self.solver.Add(self.F[i, j, k] <= (self.num_customer - 1) * self.X[i, j, k])
        
        
        # 8
        expr = []
        for i in range(self.num_customer):
            for k in range(self.num_vehicles):
                expr.append(self.F[i, 0, k])
        self.solver.Add(self.solver.Sum(expr) == (self.num_customer - 1))
        
        9
        for j in range(self.num_customer):
            for k in range(self.num_vehicles):
                self.solver.Add(self.F[0, j, k] == 0)
        
        # 10
        for i in range(self.num_customer):
            for k in range(self.num_vehicles):
                expr_1 = []
                expr_2 = []
                for j in range(self.num_customer):
                    expr_1.append(self.F[i, j, k])
                    expr_2.append(self.F[j, i, k])
                self.solver.Add((self.solver.Sum(expr_1) - self.solver.Sum(expr_2)) == self.Y[i, k])
        
        for i in range(self.num_customer):
            for j in range(self.num_customer):
                for k in range(self.num_vehicles):
                    self.solver.Add(self.F[i, j, k] >= 0)
            
        expr_0 = []
        for i in range(self.num_customer):
            for j in range(self.num_customer):
                expr_0.append(self.X[i, j, 0] * self.distance[i, j])
        for k in range(1, self.num_vehicles):
            expr_k = []
            for i in range(self.num_customer):
                for j in range(self.num_customer):
                    expr_k.append(self.X[i, j, k] * self.distance[i, j])
            self.solver.Add(self.solver.Sum(expr_k) <= self.solver.Sum(expr_0))

        self.objective = self.solver.Objective()
        for i in range(self.num_customer):
            for j in range(self.num_customer):
                self.objective.SetCoefficient(self.X[i, j, 0], self.distance[i, j])
        
        self.objective.SetMinimization()
        status = self.solver.Solve()
        return status
            
        
    
    
    def run(self):
        status = self.state_model()
        print("Number of Vehilces :", self.num_vehicles)
        print("Number of Customers :", self.num_customer)
        print('Number of variables : ', self.solver.NumVariables())
        print('Number of Contraints : ', self.solver.NumConstraints())
        print("Solving .....")
        # status = self.solver.Solve()
        print("Status: ", status) 
        print(self.objective.Value())
        if status == self.solver.OPTIMAL:
            for i in range(self.num_vehicles):
                print(self.routes[i].solution_value())
                
                
if __name__ == "__main__":
    path = 'data_21_4_1.json'
    app = MIP(path)
    app.run()
        
        
        
        
