package dii.vrp.tp;

import com.sun.xml.internal.fastinfoset.sax.SystemIdResolver;
import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClarkWright {
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
                    //if (!savingsList.contains(newNode))
                    savingsList.add(newNode);
                }
            }
        }

        // Sort the savings
        savingsList.sort(Comparator.comparing(SavingsNode::getSavings));
        Collections.reverse(savingsList);

        return savingsList;
    }

    /**
     * Applies the Clark&Wright algorithm to optimize the given solution and savingNodes.
     * @param p_solution The solution to start from.
     * @param savingsNodes The list of savings nodes.
     * @return The new optimized solution.
     */
    private ISolution run(VRPSolution p_solution, List<SavingsNode> savingsNodes, int start, int end) {
        VRPSolution solution = (VRPSolution) p_solution.clone();

        // Iterate through all the saving nodes
        for (int iteration = start; iteration <= end; iteration++) {
            SavingsNode node = savingsNodes.get(iteration);
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

    /**
     * Clones a List of SavingsNode.
     * @param src The source List
     * @return A new list wit
     */
    private List<SavingsNode> cloneSavings(List<SavingsNode> src) {
        ArrayList<SavingsNode> newList = new ArrayList<>();
        for (SavingsNode node: src)
            newList.add(node.clone());
        return newList;
    }

    private VRPSolution prepareInitialSolution() {
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
        return solution;
    }

    /**
     * Runs a naive Clark&Wright algorithm without look-ahead technique.
     * @return The optimized solution.
     */
    public ISolution naiveRun() {
        long startTime = System.nanoTime();
        List<SavingsNode> savings = createSavingsList();
        ISolution s = run(prepareInitialSolution(), savings, 0, savings.size() - 1);
        long duration = System.nanoTime() - startTime;
        System.out.println("Time elapsed " + new DecimalFormat("#.#########").format(((double)duration / 1000000000)) + " s");
        return s;
    }

    /**
     * Runs a PILOT'ed Clark&Wright algorithm using a look-ahead technique.
     * @param k The heuristic value. Determines how many different solutions are created on each iteration
     *          of the optimization.
     * @return The optimized solution.
     */
    public ISolution pilotedRun(int k) {
        return pilotedRun(k, 1);
    }

    /**
     * Runs a PILOT'ed Clark&Wright algorithm using a look-ahead technique.
     * @param k The heuristic value. Determines how many different solutions are created on each iteration
     *          of the optimization.
     * @param coefficient The coefficient applied to the heuristic. Changing this value can lead to better results.
     * @return The optimized solution.
     */
    public ISolution pilotedRun(int k, int coefficient) {
        if (k <= 1)
            return naiveRun();

        long startTime = System.nanoTime();

        // initialSolution
        VRPSolution initialSolution = prepareInitialSolution();
        List<SavingsNode> initialSavings = createSavingsList();

        VRPSolution bestSolution = null;
        VRPSolution bestPartialSolution = initialSolution;
        List<SavingsNode> bestSavings = initialSavings;

        // Find the best solution in k solutions for each iteration of the algorithm
        for (int iteration = 0; iteration < initialSavings.size(); iteration++) {
            ISolution[] partialSolutions = new VRPSolution[k];
            ISolution[] endSolutions = new VRPSolution[k];
            ArrayList<SavingsNode>[] iterationSavings = new ArrayList[k];

            // Can do this in parallel
            for (int swap = 0; swap < k; swap++) {
                final int jump = swap * coefficient;
                final int iterationOffset = iteration + jump;

                // Swap element at 'iteration' index with element 'swap' steps away
                if (iterationOffset < initialSavings.size()) {
                    // Generate a different savings list by swapping 2 items
                    ArrayList<SavingsNode> newSavings = (ArrayList<SavingsNode>)cloneSavings(bestSavings);
                    SavingsNode iterationNode = newSavings.get(iteration);
                    SavingsNode swapNode = newSavings.get(iterationOffset);
                    newSavings.set(iteration, swapNode);
                    newSavings.set(iterationOffset, iterationNode);

                    // Run one iteration and keep the state for a possible late use
                    VRPSolution iterationSolution = (VRPSolution) run(bestPartialSolution, newSavings, iteration, iteration);
                    partialSolutions[swap] = iterationSolution;
                    iterationSavings[swap] = newSavings;

                    // Compute the rest of the solution, if possible, else just take it as it is.
                    if (iteration+1 <= initialSavings.size()-1)
                        endSolutions[swap] = run(iterationSolution, newSavings, iteration+1, initialSavings.size()-1);
                    else
                        endSolutions[swap] = iterationSolution;
                }
            }

            // Change best solution when we've found a better one
            for (int i = 0; i < k; i++) {
                ISolution sol = endSolutions[i];
                if (sol != null && (bestSolution == null || sol.getOF() < bestSolution.getOF())) {
                    bestSolution = (VRPSolution) endSolutions[i];
                    bestPartialSolution = (VRPSolution) partialSolutions[i];
                    bestSavings = iterationSavings[i];
                }
            }
        }

        long duration = System.nanoTime() - startTime;
        System.out.println("Time elapsed " + new DecimalFormat("#.#########").format(((double)duration / 1000000000)) + " s");

        return bestSolution;
    }
}
