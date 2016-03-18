package com.silicolife.textmining.processes.corpora.loaders.pdf.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.utils.generic.CSVFileConfigurations;

public class CorpusLoadersUtils {
	
	public static String useNameAsOtherID = "usenameasotherid";
	public static String useMapBetweenFilePathAndOtherID = "usemapbetweenfilepathandotherid";

	public static int getNumberPublicationByType(File firectory, final String pdf) {
		int numberOFPDFDocuments = 0;
		if(firectory.isDirectory())
		{
			return firectory.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					if(pathname.getAbsolutePath().toLowerCase().endsWith(pdf.toLowerCase()))
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}).length;
		}
		return numberOFPDFDocuments;
	}
	
	public static File[] getFilesByType(File firectory, final String pdf) {
		if(firectory.isDirectory())
		{
			return firectory.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					if(pathname.getAbsolutePath().toLowerCase().endsWith(pdf.toLowerCase()))
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			});
		}
		return null;
	}

	public static Map<String, String> readFileAndGetFilePathAndOtherID(
			String file, CSVFileConfigurations csvfileconfigurations) throws IOException {
		Map<String,String> fileNameOtherID = new HashMap<String, String>();
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while((line = br.readLine())!=null)
		{
			String[] lin = line.split(csvfileconfigurations.getGeneralDelimiter().getValue());
			String key = lin[csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(LanguageProperties.getLanguageStream("pt.uminho.anote2.corpora.filepathorfilename")).getColumnNumber()];
			if(key!=null)
				key = key.replace(csvfileconfigurations.getTextDelimiter().getValue(),"");
			String value = lin[csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(LanguageProperties.getLanguageStream("pt.uminho.anote2.corpora.pmidorotherid")).getColumnNumber()];
			if(value!=null)
				value = value.replace(csvfileconfigurations.getTextDelimiter().getValue(),"");
			
			fileNameOtherID.put(key.replace("\\", "/"), value);
		}		
		return fileNameOtherID;
	}

}
