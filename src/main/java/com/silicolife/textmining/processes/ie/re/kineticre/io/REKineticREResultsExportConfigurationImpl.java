package com.silicolife.textmining.processes.ie.re.kineticre.io;

import com.silicolife.textmining.core.interfaces.process.IE.IRESchema;
import com.silicolife.textmining.processes.ie.re.kineticre.core.REKineticConfigurationClasses;

public class REKineticREResultsExportConfigurationImpl implements IREKineticREResultsExportConfiguration{
	
	private String exportFile;
	private IRESchema reSchema;
	private REKineticConfigurationClasses reKineticConfigurationClasses;
	private boolean sentencesToExport;
	
	public REKineticREResultsExportConfigurationImpl(String exportFile, IRESchema reSchema,
			REKineticConfigurationClasses reKineticConfigurationClasses, boolean sentencesToExport) {
		super();
		this.exportFile = exportFile;
		this.reSchema = reSchema;
		this.reKineticConfigurationClasses = reKineticConfigurationClasses;
		this.sentencesToExport = sentencesToExport;
	}

	public String getExportFile() {
		return exportFile;
	}

	public IRESchema getRESchema() {
		return reSchema;
	}

	public REKineticConfigurationClasses getREKineticConfigurationClasses() {
		return reKineticConfigurationClasses;
	}

	public boolean isSentencesToExport() {
		return sentencesToExport;
	}

}
