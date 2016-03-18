package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.classes;

import java.io.File;

import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.AKeggClassLoader;

public class KeggEnzymes extends AKeggClassLoader{
	
	public static final String klass = "Enzyme";

	
	public KeggEnzymes() {
		super();
	}

	public boolean checkFile(File file) {
		return checkAllFile(file, klass);
	}
	
//	public IResourceUpdateReport loadTerms(IDicionaryLoaderConfiguration configuration) throws ANoteException, IOException {
//		Properties properties = configuration.getProperties();
//		File file = configuration.getFlatFile();
//		String organism = "";
//		if(properties.containsKey(KeggFlatFilesLoader.propertyorganism))
//		{
//			organism=properties.getProperty(KeggFlatFilesLoader.propertyorganism);
//		}
//		this.loadFileEnzymes(file,organism);
//		return getReport();
//	}

	@Override
	public String getClassLoader() {
		return klass;
	}
	
	public String toString() {
		return klass;
	}
}
