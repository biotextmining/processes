package com.silicolife.textmining.processes.ie.re.kineticre.configuration;

public interface IREKineticAdvancedConfiguration {
	
	/**
	 * 	get Max distance (offset) between value and unit
	 * 
	 * @return 
	 */
	public int getMaxDistanceBetweenValueAndUnit();
	
	/**
	 * get Max distance between two pari value-unit with "and" between them
	 * 
	 * @return
	 */
	public int getMaxDistanceBetweenTwoVUPairsWithAndInside();
		
}
