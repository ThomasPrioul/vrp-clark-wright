package dii.vrp.test;


import java.util.Random;

import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;
import dii.vrp.data.VRPREPInstanceReader;
import dii.vrp.tp.ExplorationStrategy;
import dii.vrp.tp.GRASP;
import dii.vrp.tp.NNHeuristic;
import dii.vrp.tp.OptimizationCriterion;
import dii.vrp.tp.Relocate;
import dii.vrp.tp.SFRSHeuristic;
import dii.vrp.tp.Split;
import dii.vrp.tp.VND;
import dii.vrp.tp.VRPSolution;

public class TGRASP {

	public static void main(String[] args) {

		//Parameters
		String file = "./data/christofides-et-al-1979-cmt/CMT01.xml"; //Instance
		int T = 1000;
		
		//Read data from an instance file
		IDistanceMatrix distances = null;
		IDemands demands = null;
		double Q = Double.NaN;
		try (VRPREPInstanceReader parser = new VRPREPInstanceReader(file)) {
			distances = parser.getDistanceMatrix();
			demands = parser.getDemands();
			Q = parser.getCapacity("0");
		}
			
		//Set up GRASP
		NNHeuristic nn = new NNHeuristic(distances);
		Split split = new Split(distances, demands, Q);
		SFRSHeuristic h = new SFRSHeuristic(nn, split);
		h.setRandomized(true);
		h.setRandomizationFactor(3);
		h.setRandomGen(new Random(1));
		Relocate relocate = new Relocate(distances, demands, Q);
		relocate.setExplorationStrategy(ExplorationStrategy.FIRST_IMPROVEMENT);
		VND vnd = new VND(relocate);
		GRASP grasp = new GRASP(h, vnd, T);
		
		//Run GRASP
		VRPSolution s = (VRPSolution)grasp.run(null,OptimizationCriterion.MINIMIZATION);
		
		//Report solution
		System.out.println(s);
	}
}
