package dii.vrp.tp;

import dii.vrp.data.PrinsExample;

/**
 * Tests the implementation of the split algorithm using the example in Prins (2004).
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 21, 2016
 *
 */
public class TSplit {

	public static void main(String[] args) {
		Split s=new Split(PrinsExample.getDistances(),PrinsExample.getDemands(),PrinsExample.getCapacity());
		System.out.println(s.split(PrinsExample.getTSPTour()));
	}

}
