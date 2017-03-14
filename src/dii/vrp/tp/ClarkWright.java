package dii.vrp.tp;

import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Valentin on 12/03/2017.
 */
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

    public ISolution run() {
        // Calculate savings list

        createSavingsList();

        return new VRPSolution();
    }

    public ArrayList<SavingsNode> createSavingsList() {

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
}

