package dii.vrp.test;

import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;
import dii.vrp.data.VRPREPInstanceReader;
import dii.vrp.tp.ISplit;
import dii.vrp.tp.ITSPHeuristic;
import dii.vrp.tp.NNHeuristic;
import dii.vrp.tp.SFRSHeuristic;
import dii.vrp.tp.Split;

public class TSFRSHeuristic {


	public static void main(String[] args){

		//Parameters
		String file= "./data/christofides-et-al-1979-cmt/CMT01.xml"; //Instance

		//Read data from an instance file
		IDistanceMatrix distances=null;
		IDemands demands=null;
		double Q=Double.NaN;
		try(VRPREPInstanceReader parser=new VRPREPInstanceReader(file)){
			distances=parser.getDistanceMatrix();
			demands=parser.getDemands();
			Q=parser.getCapacity("0");
		}

		//Initialie the sequence-first, route-second heuristic
		ITSPHeuristic rnn=new NNHeuristic(distances);
		ISplit s=new Split(distances, demands, Q);
		SFRSHeuristic h=new SFRSHeuristic(rnn, s);
		//Run the heuristic and report the results
		System.out.println(h.run());
	}

}
