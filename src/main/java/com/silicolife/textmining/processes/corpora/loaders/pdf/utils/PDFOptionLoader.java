package com.silicolife.textmining.processes.corpora.loaders.pdf.utils;

import java.util.Properties;

public enum PDFOptionLoader {
	
	Default,
	FileNameASOtherID,
	UseFileMetaInfo;
	
	public static Properties getProperties(PDFOptionLoader pdfLoaderOptions) {
		Properties propreties = new Properties();
		if(pdfLoaderOptions.equals(PDFOptionLoader.Default))
		{
			
		}
		else if(pdfLoaderOptions.equals(PDFOptionLoader.FileNameASOtherID))
		{
			propreties.put(CorpusLoadersUtils.useNameAsOtherID, "true");	
		}
		else if(pdfLoaderOptions.equals(PDFOptionLoader.UseFileMetaInfo))
		{
			propreties.put(CorpusLoadersUtils.useMapBetweenFilePathAndOtherID, "true");	
		}
		return propreties;
	}
}
