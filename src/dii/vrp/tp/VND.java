package dii.vrp.tp;

import java.util.ArrayList;
/**
 * Implemenst a variable neighborhood descent
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 25, 2016
 *
 */
public class VND implements ILocalSearch {
	/**
	 * The list of neighborhoods
	 */
	private final ArrayList<INeighborhood> neighborhoods;
	/**
	 * Construcs a new variable neighborhood descend algorithm
	 * @param neighborhood the neighborhood in position 1 of the list
	 */
	public VND(final INeighborhood neighborhood){
		neighborhoods=new ArrayList<INeighborhood>(); 
		neighborhoods.add(neighborhood);
	}
	/**
	 * Adds a new neighborhood
	 * @param neigborhood the neighborhood to add
	 */
	public void addNeighborhood(final INeighborhood neigborhood){
		neighborhoods.add(neigborhood);
	}

	@Override
	public ISolution run(final ISolution initSol, final OptimizationCriterion sense) {
		ISolution best=initSol.clone();
		for(int k=0; k<neighborhoods.size(); k++){
			boolean stop=false;
			while(!stop){
				INeighborhood n=neighborhoods.get(k);
				ISolution s=n.explore(best.clone());
				if(s==null)//The neighborhood exploration was sucessfull
					stop=true;
				else
					if((sense==OptimizationCriterion.MINIMIZATION&&s.getOF()<best.getOF())||(sense==OptimizationCriterion.MAXIMIZATION&&s.getOF()>best.getOF())){
						//Update best solution
						best=s;
						k=0;
					}
			}
		}
		return best;
	}

}
