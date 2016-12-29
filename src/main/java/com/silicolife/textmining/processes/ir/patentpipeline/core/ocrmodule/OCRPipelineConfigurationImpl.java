package com.silicolife.textmining.processes.ir.patentpipeline.core.ocrmodule;

public class OCRPipelineConfigurationImpl implements IOCRPipelineConfiguration  {

	private String inputDirectory;
	private boolean ocrAlreadyDone;
	
	public OCRPipelineConfigurationImpl(String inputDirectory, boolean ocrAlreadyDone) {
		this.inputDirectory=inputDirectory;
		this.ocrAlreadyDone=ocrAlreadyDone;
	}
	
	@Override
	public String geInputDirectory() {
		return inputDirectory;
	}

	@Override
	public boolean ocrAlreadyDoneOnPatentsPDF() {
		return ocrAlreadyDone;
	}

}
