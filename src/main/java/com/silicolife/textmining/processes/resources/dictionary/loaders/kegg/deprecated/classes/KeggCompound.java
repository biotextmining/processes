package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.classes;

import java.io.File;

import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.AKeggClassLoader;

@Deprecated
public class KeggCompound extends AKeggClassLoader{
	
	public static final String klass = "Compound";

	public KeggCompound() {
		super();
	}

	public boolean checkFile(File file) {
		return checkAllFile(file,klass);
	}

//	public IResourceUpdateReport loadTerms(IDicionaryLoaderConfiguration configuration) throws ANoteException, IOException {
//		Properties properties = configuration.getProperties();
//		File file = configuration.getFlatFile();
//		String organism = "";
//		if(properties.containsKey(KeggFlatFilesLoader.propertyorganism))
//		{
//			organism=properties.getProperty(KeggFlatFilesLoader.propertyorganism);
//		}
//		loadFileCoumpounds(file,organism);
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
