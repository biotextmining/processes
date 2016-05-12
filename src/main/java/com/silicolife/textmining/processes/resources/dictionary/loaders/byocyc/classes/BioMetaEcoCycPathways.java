package com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes;

import java.io.File;

import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.ABioCycMetaCycFileImportAvailable;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.BioMetaEcoCycFlatFileLoader;

public class BioMetaEcoCycPathways extends ABioCycMetaCycFileImportAvailable{
	
	public final static String pathways = "Pathways";

	
	public BioMetaEcoCycPathways() {
	}

	public boolean checkFile(File file) {
		return BioMetaEcoCycFlatFileLoader.cheackGenericFile(file,"# File Name: pathways.dat");

	}

	@Override
	public String getClassLoader() {
		return pathways;
	}

	@Override
	public String toString() {
		return pathways;
	}


}
