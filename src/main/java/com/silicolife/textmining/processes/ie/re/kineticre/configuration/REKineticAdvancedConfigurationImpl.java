package com.silicolife.textmining.processes.ie.re.kineticre.configuration;

public class REKineticAdvancedConfigurationImpl implements IREKineticAdvancedConfiguration{
	
	private int maxDistanceBetweenValueAndUnit;
	private int maxDistanceBetweenTwoVUPairsWithAndInside;
	
	public REKineticAdvancedConfigurationImpl()
	{
		super();
		this.maxDistanceBetweenValueAndUnit = 3;
		this.maxDistanceBetweenTwoVUPairsWithAndInside = 30;
	}

	public REKineticAdvancedConfigurationImpl(int maxDistanceBetweenValueAndUnit, int maxDistanceBetweenTwoVUPairsWithAndInside) {
		super();
		this.maxDistanceBetweenValueAndUnit = maxDistanceBetweenValueAndUnit;
		this.maxDistanceBetweenTwoVUPairsWithAndInside = maxDistanceBetweenTwoVUPairsWithAndInside;
	}

	@Override
	public int getMaxDistanceBetweenValueAndUnit() {
		return maxDistanceBetweenValueAndUnit;
	}

	@Override
	public int getMaxDistanceBetweenTwoVUPairsWithAndInside() {
		return maxDistanceBetweenTwoVUPairsWithAndInside;
	}

}
