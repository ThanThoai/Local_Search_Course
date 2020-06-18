
from __future__ import print_function
from ortools.constraint_solver import routing_enums_pb2
from ortools.constraint_solver import pywrapcp

import math
import json
import sys

def load_data():
    data = {}
    data['distance_matrix'] = [
        [
            0, 548, 776, 696, 582, 274, 502, 194, 308, 194, 536, 502, 388, 354,
            468, 776, 662
        ],
        [
            548, 0, 684, 308, 194, 502, 730, 354, 696, 742, 1084, 594, 480, 674,
            1016, 868, 1210
        ],
        [
            776, 684, 0, 992, 878, 502, 274, 810, 468, 742, 400, 1278, 1164,
            1130, 788, 1552, 754
        ],
        [
            696, 308, 992, 0, 114, 650, 878, 502, 844, 890, 1232, 514, 628, 822,
            1164, 560, 1358
        ],
        [
            582, 194, 878, 114, 0, 536, 764, 388, 730, 776, 1118, 400, 514, 708,
            1050, 674, 1244
        ],
        [
            274, 502, 502, 650, 536, 0, 228, 308, 194, 240, 582, 776, 662, 628,
            514, 1050, 708
        ],
        [
            502, 730, 274, 878, 764, 228, 0, 536, 194, 468, 354, 1004, 890, 856,
            514, 1278, 480
        ],
        [
            194, 354, 810, 502, 388, 308, 536, 0, 342, 388, 730, 468, 354, 320,
            662, 742, 856
        ],
        [
            308, 696, 468, 844, 730, 194, 194, 342, 0, 274, 388, 810, 696, 662,
            320, 1084, 514
        ],
        [
            194, 742, 742, 890, 776, 240, 468, 388, 274, 0, 342, 536, 422, 388,
            274, 810, 468
        ],
        [
            536, 1084, 400, 1232, 1118, 582, 354, 730, 388, 342, 0, 878, 764,
            730, 388, 1152, 354
        ],
        [
            502, 594, 1278, 514, 400, 776, 1004, 468, 810, 536, 878, 0, 114,
            308, 650, 274, 844
        ],
        [
            388, 480, 1164, 628, 514, 662, 890, 354, 696, 422, 764, 114, 0, 194,
            536, 388, 730
        ],
        [
            354, 674, 1130, 822, 708, 628, 856, 320, 662, 388, 730, 308, 194, 0,
            342, 422, 536
        ],
        [
            468, 1016, 788, 1164, 1050, 514, 514, 662, 320, 274, 388, 650, 536,
            342, 0, 764, 194
        ],
        [
            776, 868, 1552, 560, 674, 1050, 1278, 742, 1084, 810, 1152, 274,
            388, 422, 764, 0, 798
        ],
        [
            662, 1210, 754, 1358, 1244, 708, 480, 856, 514, 468, 354, 844, 730,
            536, 194, 798, 0
        ],
    ]
    data['num_vehicles'] = 4
    data['depot'] = 0
    return data

def read_data(path, write_json = True):
    with open(path, 'r') as f:
        data = f.readlines()

    result = {}
    # print(len(data))
    result['num_vehicles'] = list(map(int, data[0].split()))[1]
    result['num_customer'] = list(map(int, data[0].split()))[0]
    result['list_customer'] = []
    # result['depot'] = result['num_customer'] - 1
    result['depot'] = 0
    for i in range(1, len(data)):
        customer = {
            'id' : None,
            'x': None,
            'y': None,
            'demand': None
        }
        d = list(map(float, data[i].split()))
        print(d)
        customer['id'] = i - 1
        customer['demand'] = d[0]
        customer['x'] = d[1]
        customer['y'] = d[2]
        result['list_customer'].append(customer)
    result['distance_matrix'] = [[0 for i in range(result['num_customer'])] for j in range(result['num_customer'])]
    for cus_1 in result['list_customer']:
        for cus_2 in result['list_customer']:
            result['distance_matrix'][cus_1['id']][cus_2['id']] = math.sqrt((cus_1['x'] - cus_2['x']) ** 2 + (cus_1['y'] - cus_2['y']) ** 2) + cus_2['demand']
    file_name = path.split('/')[-1] + '.json'
                
    with open(file_name, 'w') as f:
        json.dump(result, f)
    return result
    


def print_solution(data, manager, routing, solution):
    max_route_distance = 0
    for vehicle_id in range(data['num_vehicles']):
        index = routing.Start(vehicle_id)
        plan_output = "Route of vehicle {}: \n".format(vehicle_id)
        route_distance = 0
        while not routing.IsEnd(index):
            plan_output += ' {} -> '.format(manager.IndexToNode(index))
            previous_index = index
            index = solution.Value(routing.NextVar(index))
            route_distance += routing.GetArcCostForVehicle(previous_index, index, vehicle_id)
        plan_output += '{}\n'.format(manager.IndexToNode(index))
        plan_output += 'Distance of the route: {}'.format(route_distance)
        print(plan_output)
        max_route_distance = max(route_distance, max_route_distance)
    print('Maximum of the route distances: {}'.format(max_route_distance))    


if __name__ == '__main__':
    if sys.argv[1] == '': 
        path = 'dataset/data_151_15_1'
    else:
        path = sys.argv[1]
    data = read_data(path)
    manager = pywrapcp.RoutingIndexManager(len(data['distance_matrix']), data['num_vehicles'], data['depot'])
    routing = pywrapcp.RoutingModel(manager)
    
    def distances_callback(from_index, to_index):
        from_node = manager.IndexToNode(from_index)
        to_node   = manager.IndexToNode(to_index)
        return data['distance_matrix'][from_node][to_node] 
    
    transit_callback_index = routing.RegisterTransitCallback(distances_callback)
    routing.SetArcCostEvaluatorOfAllVehicles(transit_callback_index)
    
    dismension_name = 'Distance'
    routing.AddDimension(transit_callback_index, 0, 3000, True, dismension_name)
    distance_dismension = routing.GetDimensionOrDie(dismension_name)
    distance_dismension.SetGlobalSpanCostCoefficient(100)
    
    
    search_parameters = pywrapcp.DefaultRoutingSearchParameters()
    # print('---------------')
    # print(search_parameters)
    # print('-------------')
    search_parameters.first_solution_strategy = (routing_enums_pb2.FirstSolutionStrategy.PATH_CHEAPEST_ARC)
    solution = routing.SolveWithParameters(search_parameters)
    
    if solution:
        print_solution(data, manager, routing, solution)
    
    # path = 'dataset/data_5_4_1'
    # read_data(path)
