package dii.vrp.tp;

import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;

public class ClarkWright {
    public ClarkWright(IDistanceMatrix matrix, IDemands demands, double Q) {

    }

    public ISolution run() {
        return new VRPSolution();
    }
}

