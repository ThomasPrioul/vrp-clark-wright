package dii.vrp.tp;

import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClarkWright
{

    private IDistanceMatrix distances;
    private IDemands demands;
    private double Q;

    public ClarkWright(IDistanceMatrix distances, IDemands demands, double Q) {
        this.distances = distances;
        this.demands = demands;
        this.Q = Q;
    }

    private List<SavingsNode> createSavingsList() {

        ArrayList<SavingsNode> savingsList = new ArrayList<>();

        for (int i = 0; i < distances.size(); i++) {
            for (int j = 0; j < distances.size(); j++) {
                if (i != j) {
                    double saving = distances.getDistance(0, i) + distances.getDistance(0, j) - distances.getDistance(i, j);

                    savingsList.add(new SavingsNode(i, j, saving));
                }
            }
        }

        // Sort the savings
        savingsList.sort(Comparator.comparing(SavingsNode::getSavings));
        Collections.reverse(savingsList);

        for (SavingsNode saving : savingsList) {
            System.out.println(saving.getSavings());
        }

        return savingsList;
    }

    private int getCurrentRouteDemand(IRoute route) {
        if (route == null || route.getRoute() == null)
            return -1;

        int routeDemand = 0;
        for (Integer customer : route.getRoute()) {
            routeDemand += demands.getDemand(customer);
        }
        return routeDemand;
    }

    private boolean isInRoutes(VRPSolution solution, int nodeId) {
        for (IRoute route : solution.getRoutes()) {
            if (route.contains(nodeId))
                return true;
        }

        return false;
    }

    public ISolution run() {
        // Calculate savings list
        VRPSolution solution = new VRPSolution();
        List<SavingsNode> savingsNodes = createSavingsList();

        // Go through all the nodes
        for (SavingsNode node : savingsNodes) {
            // neither customers are in the routes
            if (!isInRoutes(solution, node.getFrom()) && !isInRoutes(solution, node.getTo())) {
                // make sure the requirements of the two customers don't go over the van's capacity
                if ((demands.getDemand(node.getFrom()) + demands.getDemand(node.getTo())) <= Q) {

                    // create a new route and add it to the solution
                    IRoute route = new VRPRoute();
                    route.add(node.getFrom());
                    route.add(node.getTo());

                    if (!solution.getRoutes().contains(route)) {
                        solution.addRoute((route));
                    }
                }
            }

            // else find a route that ends at 'from'
            else if (!isInRoutes(solution, node.getTo())) {
                // go through all the routes in the solution
                for (IRoute route : solution.getRoutes()) {
                    // if the last customer is the 'from' part of the node
                       if (route.getLastCustomer() == node.getFrom()) {
                        // make sure to check the new requirement
                        if ((getCurrentRouteDemand(route) + demands.getDemand(node.getTo())) <= Q) {
                            route.insert(node.getTo(), 0);
                            break;
                        }
                    }
                }

            }
            // else find a route that ends at 'to'
            else if (!isInRoutes(solution, node.getFrom())) {
                for(IRoute route : solution.getRoutes()) {
                    // if the last customer is the 'to' part of the node
                    if (route.getLastCustomer() == node.getTo()) {
                        // make sure to check the new requirement
                        if ((getCurrentRouteDemand(route) + demands.getDemand(node.getFrom())) <= Q) {
                            route.add(node.getFrom());
                            break;
                        }
                    }
                }
            }

            // check for route merging possibilities
            IRoute mergedRoute = null;
            for (IRoute routeX : solution.getRoutes()) {
                // If a route has been marked for merge, exit the loop
                if (mergedRoute != null) break;

                // If the last customer in the route coincides with the 'from' from the current node, then check to see if the 'to' node can be found in other routes
                if (routeX.getLastCustomer() == node.getFrom()) {
                    for (IRoute routeY : solution.getRoutes()) {
                        if (routeY.getFirstCustomer() == node.getTo()) {
                            if (routeX != routeY) {
                                if ((getCurrentRouteDemand(routeX) + getCurrentRouteDemand(routeY)) <= Q) {
                                    // Add all customers from routeY to routeX
                                    for (int customerId: routeY.getRoute())
                                        routeX.add(customerId);
                                    mergedRoute = routeY;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (mergedRoute != null) {
                solution.getRoutes().remove(mergedRoute);
            }
        }

        return new VRPSolution();
    }
}

