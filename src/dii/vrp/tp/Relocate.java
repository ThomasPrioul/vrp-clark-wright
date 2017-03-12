package dii.vrp.tp;

import dii.vrp.data.IDemands;
import dii.vrp.data.IDistanceMatrix;


/**
 * Implements the relocate neighborhood for the VRP
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 25, 2016
 *
 */
public class Relocate implements INeighborhood {

	/**
	 * Internal calss modeling a relocation movement
	 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
	 * @version %I%, %G%
	 * @since Jan 25, 2016
	 */
	private class Relocation{
		/**
		 * The route from where the node is extracted
		 */
		int rOut;
		/**
		 * The position in route <code>rOut</code> from where the node is extracted
		 */
		int iOut;
		/**
		 * The delta in the cost of route <code>rOut</code>
		 */
		double deltaOFROut;
		/**
		 * The route where the node is to be inserted
		 */
		int rIn;
		/**
		 * The position in route <code>rIn</code> at which the node is to be inserted
		 */
		int iIn;
		/**
		 * The delta in the cost of route <code>rIn</code>
		 */
		double deltaOFRIn;	
		/**
		 * The demand of the relocated node
		 */
		double demand;
		/**
		 * Construcs a new Relocate move
		 * @param rOut
		 * @param iOut
		 * @param deltaOFROut
		 * @param rIn
		 * @param iIn
		 * @param deltaOFRIn
		 * @param demand
		 */
		public Relocation(int rOut, int iOut, double deltaOFROut, int rIn, int iIn, double deltaOFRIn, double demand){

			this.rOut=rOut;
			this.iOut=iOut;
			this.deltaOFROut=deltaOFROut;

			this.rIn=rIn;
			this.iIn=iIn;
			this.deltaOFRIn=deltaOFRIn;

			this.demand=demand;

		}
	}

	/**
	 * The exploration strategy. Default value {@link ExplorationStrategy#BEST_IMPROVEMENT}.
	 */
	private ExplorationStrategy strategy=ExplorationStrategy.BEST_IMPROVEMENT;
	/**
	 * The distance matrix
	 */
	private final IDistanceMatrix distances;
	/**
	 * The customer demands
	 */
	private final IDemands demands;
	/**
	 * The capacity of the truck
	 */
	private final double Q;
	/**
	 * Tolerance
	 */
	private final double epsilon=0.00001;
	
	/**
	 * Construcs a new Relocate neighborhood
	 * @param distances the distance matrix
	 * @param demands the customer demands
	 * @param Q the capacity of the trucks
	 */
	public Relocate(IDistanceMatrix distances, IDemands demands, double Q){
		this.distances=distances;
		this.demands=demands;
		this.Q=Q;
	}

	@Override
	public void setExplorationStrategy(ExplorationStrategy strategy) {
		this.strategy=strategy;
	}

	@Override
	public ExplorationStrategy getStrategy() {
		return this.strategy;
	}

	@Override
	public VRPSolution explore(final ISolution s) {

		//Set best solution found
		VRPSolution best=(VRPSolution) s.clone(); //Make a copy of the initial solution

		//Initilize auxiliary variables
		Relocation bestMove=null;			//The best relocate move we have found so far
		double bestDelta=0; 				//The best OF improvement we have found
		double delta=Double.NaN;			//The last OF delta we have evaluated. Improving moves have a negative delta.
		int node=-1;						//The ID of the node we are trying to relocate
		double savings=Double.NaN;			//The savings
		double cost=Double.NaN;				//The cost
		double demand=Double.NaN;			//The demand of the node we are trying to relocate
		double load=Double.NaN;				//The load of the last considered route

		//Check all possible movements
		for(int route=0;route<best.size();route++){
			for(int position=1;position<best.size(route)-1;position++){ //We assume the depot appears at the begining and end of each route
				node=best.getNode(route, position); //The node to relocate
				demand=demands.getDemand(node);
				savings=this.getSavings(best, route, position);
				//Try re-locating the node at each possible possition
				for(int r=0;r<best.size()-1;r++){
					load=best.getLoad(r);
					if(load+demand<Q||route==r) //If the move is feasible in terms of the capacity constraint
						for(int i=1; i<best.size(r)-1;i++){
							cost=route==r&&(i-position==1||i-position==0)?Double.MAX_VALUE:this.getCost(best, r, i, node); //the relocation leads to the same solution
							delta=cost-savings;
							if(delta<bestDelta-epsilon)//If the delta in the objective function is better than the best delta
								if(this.strategy==ExplorationStrategy.FIRST_IMPROVEMENT)
									return this.executeMove(new Relocation(route,position,savings,r,i,cost,demand),best);
								else{
									bestMove=new Relocation(route,position,savings,r,i,cost,demand);
									bestDelta=delta;
								}
						}
				}
			}
		}
		return this.executeMove(bestMove,best);
	}
	/**
	 * Executes a relocate move on a solution
	 * @param m the movement to execute
	 * @param s the solution to which the move is to executed
	 * @return a modified and up-to-date solution
	 */
	private VRPSolution executeMove(Relocation m,VRPSolution s){
		
		if(m==null)
			return null;
		
		//Make the move
		if(m.rOut==m.rIn&&m.iOut<m.iIn)
			s.insert(s.remove(m.rOut, m.iOut), m.rIn, m.iIn-1);
		else
			s.insert(s.remove(m.rOut, m.iOut), m.rIn, m.iIn);
		//Update the cost
		s.setCost(m.rOut,s.getCost(m.rOut)-m.deltaOFROut);
		s.setCost(m.rIn,s.getCost(m.rIn)+m.deltaOFRIn);
		s.setOF(s.getOF()-m.deltaOFROut+m.deltaOFRIn);
		//Update load
		if(m.rIn!=m.rOut){
			s.setLoad(m.rOut,s.getLoad(m.rOut)-m.demand);
			s.setLoad(m.rIn,s.getLoad(m.rIn)+m.demand);
		}
		s.removeRoutesBysize(2);
		return s;
	}

	/**
	 * Computes the saving generated by extracting the node in position <code>i</code> from route <code>r</code> in solution <code>s</code>
	 * @param s the solution
	 * @param r the route
	 * @param i the position of the node to extract
	 * @return the saving generated by extracting the node in position <code>i</code> from route <code>r</code> in solution <code>s</code>
	 */
	private double getSavings(final VRPSolution s,int r, int i){
		int node=s.getNode(r, i);
		int pIOut=s.getNode(r, i-1);
		int sIOut=s.getNode(r, i+1);
		return distances.getDistance(pIOut,node)+distances.getDistance(node,sIOut)-distances.getDistance(pIOut,sIOut);
	}
	/**
	 * Computes the cost generated by inserting node <code>node</code> into position <code>i</code> of route <code>r<code> in solution <code>s</code>
	 * @param s the solution
	 * @param r the route
	 * @param i the position in the route
	 * @param node the node to insert
	 * @return the cost generated by inserting node <code>node</code> into position <code>i</code> of route <code>r<code> in solution <code>s</code>
	 */
	private double getCost(final VRPSolution s, int r, int i, int node){
		int pIIn=s.getNode(r, i-1);
		int sIIn=s.getNode(r, i);
		return distances.getDistance(pIIn,node)+distances.getDistance(node,sIIn)-distances.getDistance(pIIn,sIIn);
	}


}
