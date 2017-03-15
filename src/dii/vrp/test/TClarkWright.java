package dii.vrp.test;

import dii.vrp.data.CMT01;
import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;
import dii.vrp.data.VRPREPInstanceReader;
import dii.vrp.tp.CVRPRouteEvaluator;
import dii.vrp.tp.ClarkWright;
import dii.vrp.tp.VRPSolution;
import java.io.File;

public class TClarkWright {
    public static void main(String[] args) {


        // Instances to test
        File instancesDirectory = new File("./data/christofides-et-al-1979-cmt/");

        // Test every instance
        for (File instance : instancesDirectory.listFiles()) {
            String filename = instance.getAbsolutePath();

            // Only read XML files
            if (!filename.endsWith(".xml"))
                continue;

            if (!filename.endsWith("CMT01.xml"))
                continue;

            // Read data from the instance file and fire up the piloted Clark&Wright
            try (VRPREPInstanceReader parser = new VRPREPInstanceReader(filename)) {
                IDistanceMatrix distances = parser.getDistanceMatrix();
                IDemands demands = parser.getDemands();
                double Q = parser.getCapacity("0");

                System.out.println("Optimal CMT01:");
                CVRPRouteEvaluator eval = new CVRPRouteEvaluator(distances, demands);
                System.out.println(CMT01.getOptimalSolution(eval));

                System.out.println("Testing instance " + instance.getName());


                ClarkWright cw = new ClarkWright(distances, demands, Q);
                //VRPSolution solution = (VRPSolution) cw.naiveRun();
                VRPSolution solution = (VRPSolution) cw.pilotedRun(10);
                System.out.println(solution);
            }
            catch (Exception e)
            {
                System.err.print(e);
            }
        }
    }
}
