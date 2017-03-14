package dii.vrp.tp;

import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;

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


        return new VRPSolution();
    }
}

