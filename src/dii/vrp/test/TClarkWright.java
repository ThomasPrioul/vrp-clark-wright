package dii.vrp.test;

import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;
import dii.vrp.data.VRPREPInstanceReader;
import dii.vrp.tp.ClarkWright;
import dii.vrp.tp.VRPSolution;
import java.io.File;
import java.util.Arrays;

public class TClarkWright {
    public static void main(String[] args) {
        // Instances to test
        File[] instances = new File("./data/christofides-et-al-1979-cmt/").listFiles((file, s) -> s.endsWith(".xml"));
        Arrays.sort(instances);

        // Test every instance
        for (File instance : instances) {
            // Read data from the instance file and fire up the piloted Clark&Wright
            try (VRPREPInstanceReader parser = new VRPREPInstanceReader(instance.getAbsolutePath())) {
                IDistanceMatrix distances = parser.getDistanceMatrix();
                IDemands demands = parser.getDemands();
                double Q = parser.getCapacity("0");

                System.out.println("Testing instance " + instance.getName());
                ClarkWright cw = new ClarkWright(distances, demands, Q);
                //VRPSolution solution = (VRPSolution) cw.naiveRun();
                VRPSolution solution = (VRPSolution) cw.pilotedRun(2, 100);
                System.out.println(solution);
            }
            catch (Exception e) {
                System.err.print(e);
            }
        }
    }
}
