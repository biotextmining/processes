package com.silicolife.textmining.processes.ie.re.kineticre.io;

import com.silicolife.textmining.core.interfaces.process.IE.IRESchema;
import com.silicolife.textmining.processes.ie.re.kineticre.core.REKineticConfigurationClasses;

public interface IREKineticREResultsExportConfiguration {
	
	/**
	 * Path file to export data
	 * 
	 * @return
	 */
	public String getExportFile();
	
	/**
	 * Kinetic RESchema to export
	 * 
	 * @return
	 */
	public IRESchema getRESchema();
	
	/**
	 * Mapping classes
	 * 
	 * @return
	 */
	public REKineticConfigurationClasses getREKineticConfigurationClasses();
	
	/**
	 * Is sentence to export
	 * 
	 * @return
	 */
	public boolean isSentencesToExport();
}
