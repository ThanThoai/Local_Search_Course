import json
from ortools.sat.python import cp_model
from ortools.linear_solver import pywraplp
import numpy as np
import json


class MIP(object):

    def __init__(self, path_json):
        data = json.load(open(path_json, 'r'))
        self.solver = pywraplp.Solver(
            "MIP", pywraplp.Solver.CBC_MIXED_INTEGER_PROGRAMMING)
        # self.solver = cp_model.CpModel()
        self.num_customer = data['num_customer']
        self.num_vehicles = data['num_vehicles']
        self.distance = np.array(data['distance_matrix'])
        print(self.distance.shape)
        self.X = np.empty(
            shape=(self.num_customer, self.num_customer, self.num_vehicles))
        self.Y = np.empty(shape=(self.num_customer, self.num_vehicles))
        self.F = np.empty(
            shape=(self.num_customer, self.num_customer, self.num_vehicles))
        # self.O = np.empty(shape = (self.num_vehicles))

    def state_model(self):
        infinity = self.solver.infinity()

        self.X = np.array([[[self.solver.IntVar(0, 1, 'x[{0},{1},{2}]'.format(i, j, k)) for k in range(
            self.num_vehicles)] for j in range(self.num_customer)] for i in range(self.num_customer)])
        
        self.Y = np.array([self.solver.NumVar(0, infinity, 'y[{0}]'.format(i)) for i in range(self.num_customer)])

        # expr = []
        # for j in range(1, self.num_customer):
        #     for k in range(self.num_vehicles):
        #         expr.append(self.X[0, j, k])
        # self.solver.Add(self.solver.Sum(expr) == self.num_vehicles)

        # #3
        # expr = []
        # for i in range(1, self.num_customer):
        #     for k in range(self.num_vehicles):
        #         expr.append(self.X[i, 0, k])
        # self.solver.Add(self.solver.Sum(expr) == self.num_vehicles)
        
        
        for j in range(self.num_customer - 1):
            expr = []
            for i in range(self.num_customer):
                for k in range(self.num_vehicles):
                    expr.append(self.X[i, j, k])
            self.solver.Add(self.solver.Sum(expr) == 1)
        
        # for k in range(self.num_vehicles):
        #     expr_1 = []
        #     expr_2 = []
        #     for i in range(self.num_customer):
        #         expr_1.append(self.X[0, i, k])
        #         expr_2.append(self.X[i, 0, k])
        #     self.solver.Add(self.solver.Sum(expr_1) == self.num_vehicles)
        #     self.solver.Add(self.solver.Sum(expr_2) == self.num_vehicles)
                    
        
        for i in range(self.num_customer - 1):
            expr = []
            for j in range(self.num_customer):
                for k in range(self.num_vehicles):
                    expr.append(self.X[i, j, k])
            self.solver.Add(self.solver.Sum(expr) == 1)
                    
        
        for h in range(self.num_customer):
            for k in range(self.num_vehicles):
                expr_1 = []
                expr_2 = []
                for i in range(self.num_customer):
                    expr_1.append(self.X[i, h, k])
                    expr_2.append(self.X[h, i, k])
                self.solver.Add(self.solver.Sum(expr_1) - self.solver.Sum(expr_2) == 0)
                
        for k in range(self.num_vehicles):
            expr = []
            for j in range(1, self.num_customer):
                expr.append(self.X[self.num_customer - 1, j, k])
            self.solver.Add(self.solver.Sum(expr) <= 1)
        
        for k in range(self.num_vehicles):
            expr = []
            for i in range(1, self.num_customer):
                expr.append(self.X[i, self.num_customer - 1, k])
            self.solver.Add(self.solver.Sum(expr) <= 1)
            
        for k in range(self.num_vehicles):
            for i in range(self.num_customer - 1):
                for j in range(self.num_customer - 1):
                    if i != j:
                        self.solver.Add(self.Y[i] - self.Y[j] + self.num_customer * (self.X[i, j, k]) <= (self.num_vehicles - 1))
            
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
                self.objective.SetCoefficient(
                    self.X[i, j, 0], self.distance[i, j])
                
                

        self.objective.SetMinimization()
        status = self.solver.Solve()
        return status

    
    def run(self):
        status = self.state_model()
        distance = [0 for i in range(self.num_vehicles)]
        if status == self.solver.OPTIMAL:
            for k in range(self.num_vehicles):
                print('%s->' %(self.num_customer - 1), end='')
                for i in range(self.num_customer):
                    for j in range(self.num_customer):
                        if self.X[i, j, k].solution_value() != 0:
                            distance[k] += self.distance[i, j]
                            print(j, end='->')
                print(self.num_customer - 1)
        print(self.objective.Value())
        print(distance)
        
        
        
if __name__ == "__main__":
    path = 'data_16_3_1.json'
    app = MIP(path)
    app.run()
