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

        for (int i = 1; i < distances.size(); i++) {
            for (int j = 1; j < distances.size(); j++) {
                if (i != j) {
                    double saving = distances.getDistance(0, i) + distances.getDistance(0, j) - distances.getDistance(i, j);
                    SavingsNode newNode = new SavingsNode(i, j, saving);

                    // Don't make symmetrical duplicates
                    if (!savingsList.contains(newNode))
                        savingsList.add(newNode);
                }
            }
        }

        // Sort the savings
        savingsList.sort(Comparator.comparing(SavingsNode::getSavings));
        Collections.reverse(savingsList);
//        for (SavingsNode saving : savingsList) {
//            System.out.println("saving("+ saving.getFrom() + ", " + saving.getTo() + ")=" + saving.getSavings());
//        }
        return savingsList;
    }

    public ISolution naiveRun() {
        // Master solution (naive version)
        VRPSolution solution = new VRPSolution();

        // Create all the single routes
        for (int i = 1; i < distances.size(); i++) {
            VRPRoute route = new VRPRoute();
            route.add(0);
            route.add(i);
            route.add(0);
            route.setLoad(demands.getDemand(i));
            route.setCost(distances.getDistance(0, i) * 2);
            solution.addRoute(route);
        }

        return run(solution, createSavingsList());
    }

    public ISolution pilotedRun() {
        // initialSolution
        VRPSolution initialSolution = new VRPSolution();

        // Create all the single routes
        for (int i = 1; i < distances.size(); i++) {
            VRPRoute route = new VRPRoute();
            route.add(0);
            route.add(i);
            route.add(0);
            route.setLoad(demands.getDemand(i));
            route.setCost(distances.getDistance(0, i) * 2);
            initialSolution.addRoute(route);
        }

        // Calculate savings list
        List<SavingsNode> initialSavings = createSavingsList();

        return run(initialSolution, initialSavings);
    }

    private ISolution run(VRPSolution solution, List<SavingsNode> savingsNodes) {
        // Iterate through all the saving nodes
        for (SavingsNode node : savingsNodes) {
            int mergedRouteIndex = -1;

            // Find a route whose last customer is C1
            for (int x = 0; x < solution.size(); x++) {

                // If a route has been marked for merge, exit this loop
                if (mergedRouteIndex != -1)
                    break;

                // Test if the route's last customer is C1
                if (solution.getNode(x, solution.size(x) - 2) == node.getFrom()) {

                    // Find the other route whose first customer is C2
                    for (int y = 0; y < solution.size(); y++) {
                        if ((solution.getNode(y, 1) == node.getTo()) &&
                                (x != y) &&
                                (solution.getLoad(x) + solution.getLoad(y)) <= Q) {

                            // Merge the routes (add routeY nodes at position last - 1 in routeX
                            // but skip the first/last which are the depot)
                            for (int i = 1; i < (solution.size(y) - 1); i++) {
                                solution.insert(solution.getNode(y, i), x, solution.size(x) - 1);
                            }

                            solution.setCost(x, solution.getCost(x) + solution.getCost(y) - node.getSavings());
                            solution.setLoad(x, solution.getLoad(x) + solution.getLoad(y));
                            mergedRouteIndex = y;
                            break;
                        }
                    }
                }
            }

            if (mergedRouteIndex != -1) {
                solution.remove(mergedRouteIndex);
            }
        }

        // Set total cost of solution
        double total = 0;
        for (int i = 0; i < solution.size(); i++) {
            total += solution.getCost(i);
        }
        solution.setOF(total);
        return solution;
    }
}
