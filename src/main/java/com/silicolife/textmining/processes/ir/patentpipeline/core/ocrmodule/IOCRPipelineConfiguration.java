package com.silicolife.textmining.processes.ir.patentpipeline.core.ocrmodule;

public interface IOCRPipelineConfiguration {
	
	/**
	 * Return the directory where PDF files are included for PDF to text conversion 
	 * OR
	 * the directory where txt files resulting from OCR application are included
	 * @return
	 */
	
	public String geInputDirectory();
	
	/**
	 * Return if PDF files were already converted to text
	 * @return
	 */
	public boolean ocrAlreadyDoneOnPatentsPDF();
	
	

}
