package dii.vrp.tp;

/**
 * Implements a simple GRASP algorithm
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 19, 2016
 *
 */
public class GRASP implements ILocalSearch{

	/**
	 * The heuristic responsible for generating initial solutions
	 */
	private final IRandomizedHeuristic h;
	/**
	 * The local search procedure
	 */
	private final ILocalSearch ls;
	/**
	 * The number of iterations
	 */
	private final int T;
	/**
	 * Constructs a new GRASP heuristic
	 * @param initSolGenerator the heuristic responsible for generating initial solutions
	 * @param localSearch the local search procedure
	 * @param iterations the number of iterations
	 */
	public GRASP(IRandomizedHeuristic initSolGenerator, ILocalSearch localSearch, int iterations){
		h=initSolGenerator;
		this.ls=localSearch;
		this.T=iterations;
	}

	@Override
	public ISolution run(final ISolution initSol, final OptimizationCriterion sense) {
		ISolution best;
		if(initSol==null)
			best=h.run();
		else
			best=initSol.clone();
		for(int t=1;t<=T;t++){
			ISolution s=h.run();
			s=ls.run(s.clone(),sense);
			if(sense==OptimizationCriterion.MINIMIZATION&&s.getOF()<best.getOF()||
					sense==OptimizationCriterion.MAXIMIZATION&&s.getOF()>best.getOF()
					)
				best=s;
		}
		return best;
	}

}
