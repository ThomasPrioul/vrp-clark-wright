package dii.vrp.tp;

import java.util.Random;

import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;
import dii.vrp.data.XMLDataReader;
import dii.vrp.data.XMLDataWriter;


public class DIIRouter {

	/**
	 * Runs the DII router
	 * @param args [0] the file holding the arcs' information
	 * @param args [1] the file holding the nodes' information
	 * @param args [2] the truck capacity
	 * @param args [3] the GRASP iterations
	 * @param args [4] the random number generator seed
	 * @param args [5] the randomization factor
	 * @param args [6] the pathname of the output file
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		//Read data
		IDistanceMatrix distances=null;
		IDemands demands=null;
		double Q=Double.valueOf(args[2]);
		int T=Integer.valueOf(args[3]);
		long seed=Long.valueOf(args[4]);
		int K=Integer.valueOf(args[5]);
		try(XMLDataReader parser=new XMLDataReader(args[0],args[1])){
			demands=parser.readDemands();
			distances=parser.readDistances();
		}

		//Set up GRASP
		NNHeuristic nn=new NNHeuristic(distances);
		Split split=new Split(distances, demands, Q);
		SFRSHeuristic h=new SFRSHeuristic(nn, split);
		h.setRandomized(true);
		h.setRandomizationFactor(K);
		h.setRandomGen(new Random(seed));
		Relocate relocate=new Relocate(distances, demands, Q);
		VND vnd=new VND(relocate);
		GRASP grasp=new GRASP(h, vnd, T);

		//Run GRASP
		VRPSolution s=(VRPSolution)grasp.run(null,OptimizationCriterion.MINIMIZATION);

		//Report solution
		XMLDataWriter.writeSolToXML(args[1], args[6], s);

	}

}
